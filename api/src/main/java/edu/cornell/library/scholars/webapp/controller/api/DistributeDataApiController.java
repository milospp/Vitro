///* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api;

import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.EXECUTE;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.createSelectQueryContext;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.ActionFailedException;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.DataDistributorException;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.MissingParametersException;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.NoSuchActionException;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.NotAuthorizedException;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContextImpl;
import edu.cornell.mannlib.vitro.webapp.auth.objects.DataDistributorAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.controller.api.VitroApiServlet;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;

/**
 * Find a distributor description for the requested action. Create an instance
 * of that distributor. Write its data to the HTTP response.
 */
@WebServlet(name = "DistributeDataApi", urlPatterns = { "/api/dataRequest/*" })
public class DistributeDataApiController extends VitroApiServlet {
    private static final Log log = LogFactory.getLog(DistributeDataApiController.class);

    private static final String DISTRIBUTOR_FOR_SPECIFIED_ACTION = ""
            + "PREFIX : <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#> \n" + "SELECT ?distributor  \n" //
            + "WHERE { \n" //
            + "   ?distributor :actionName ?action . \n" //
            + "} \n";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Model model = ModelAccess.on(req).getOntModel(DISPLAY);

            String uri = findDistributorForAction(req, model);
            if (!PolicyHelper.isAuthorizedForActions(req, new DataDistributorAccessObject(uri), EXECUTE)) {
                throw new NotAuthorizedException();
            }
            DataDistributor instance = instantiateDistributor(req, uri, model);
            runIt(req, resp, instance);
        } catch (NoSuchActionException e) {
            do400BadRequest(e.getMessage(), resp);
        } catch (MissingParametersException e) {
            do400BadRequest(e.getMessage(), resp);
        } catch (NotAuthorizedException e) {
            do403Forbidden(resp);
        } catch (Exception e) {
            do500InternalServerError(e.getMessage(), e, resp);
        }
    }

    private String findDistributorForAction(HttpServletRequest req, Model model) throws NoSuchActionException {
        String action = req.getPathInfo();
        if (action == null || action.isEmpty()) {
            throw new NoSuchActionException("'action' path was not provided.");
        }
        if (action.startsWith("/")) {
            action = action.substring(1);
        }

        return findDistributorUri(model, action);
    }

    public static String findDistributorUri(Model model, String action) throws NoSuchActionException {
        List<String> uris = createSelectQueryContext(model, DISTRIBUTOR_FOR_SPECIFIED_ACTION)
                .bindVariableToPlainLiteral("action", action).execute().toStringFields("distributor").flatten();
        Collections.sort(uris);
        log.debug("Found URIs for action '" + action + "': " + uris);

        if (uris.isEmpty()) {
            throw new NoSuchActionException("Did not find a DataDistributor for '" + action + "'");
        }
        if (uris.size() > 1) {
            log.warn("Found more than one DataDistributor for '" + action + "': " + uris);
        }

        return uris.get(0);
    }

    private DataDistributor instantiateDistributor(HttpServletRequest req, String distributorUri, Model model)
            throws ActionFailedException {
        try {
            return new ConfigurationBeanLoader(model, req).loadInstance(distributorUri, DataDistributor.class);
        } catch (ConfigurationBeanLoaderException e) {
            throw new ActionFailedException("Failed to instantiate the DataDistributor: " + distributorUri, e);
        }
    }

    public static DataDistributor instantiateDistributor(String distributorUri, Model model)
            throws ActionFailedException {
        try {
            return new ConfigurationBeanLoader(model).loadInstance(distributorUri, DataDistributor.class);
        } catch (ConfigurationBeanLoaderException e) {
            throw new ActionFailedException("Failed to instantiate the DataDistributor: " + distributorUri, e);
        }
    }

    private void runIt(HttpServletRequest req, HttpServletResponse resp, DataDistributor instance)
            throws DataDistributorException {
        try {
            instance.init(new DataDistributorContextImpl(req));
            log.debug("Distributor is " + instance);

            resp.setContentType(instance.getContentType());
            resp.setCharacterEncoding("UTF-8");
            instance.writeOutput(resp.getOutputStream());
        } catch (Exception e) {
            log.error("Failed to execute the DataDistributor", e);
            instance.close();
            throw new ActionFailedException(e);
        }
    }

    private void do400BadRequest(String message, HttpServletResponse resp) throws IOException {
        log.debug("400BadRequest: " + message);
        resp.setStatus(400);
        resp.getWriter().println(message);
    }

    private void do403Forbidden(HttpServletResponse resp) throws IOException {
        log.debug("403Forbidden");
        resp.setStatus(403);
        resp.getWriter().println("Not authorized for this action.");
    }

    private void do500InternalServerError(String message, Exception e, HttpServletResponse resp) throws IOException {
        log.warn("500InternalServerError " + message, e);
        resp.setStatus(500);
        try {
            PrintWriter w = resp.getWriter();
            w.println(message);
            e.printStackTrace(w);
        } catch (IllegalStateException e1) {
            OutputStream os = resp.getOutputStream();
            os.write((message + "\n").getBytes());
            e.printStackTrace(new PrintStream(os));
        }

    }

    /**
     * If you want to use a post form, go ahead.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

}
