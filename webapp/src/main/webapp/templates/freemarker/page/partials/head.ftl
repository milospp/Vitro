<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<meta charset="utf-8" />
<!-- Google Chrome Frame open source plug-in brings Google Chrome's open web technologies and speedy JavaScript engine to Internet Explorer-->
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

<#include "title.ftl">

<#include "stylesheets.ftl">

<#include "themeStylesheets.ftl">

<#include "headScripts.ftl">

<!--[if (gte IE 6)&(lte IE 8)]>
<script type="text/javascript" src="${urls.base}/js/selectivizr.js"></script>
<![endif]-->

<#if metaTags??>
    ${metaTags.list()}
</#if>

<#-- Inject head content specified in the controller. Currently this is used only to generate an rdf link on
an individual profile page. -->
${headContent!}

<style>
    :root {
        <#if themePrimaryColorLighter?? && themePrimaryColorLighter != "null">--primary-color-lighter: ${themePrimaryColorLighter};</#if>
        <#if themePrimaryColor?? && themePrimaryColor != "null">--primary-color: ${themePrimaryColor};</#if>
        <#if themePrimaryColorDarker?? && themePrimaryColorDarker != "null">--primary-color-darker: ${themePrimaryColorDarker};</#if>
        <#if themeBannerColor?? && themeBannerColor != "null">--banner-color: ${themeBannerColor};</#if>
        <#if themeSecondaryColor?? && themeSecondaryColor != "null">--secondary-color: ${themeSecondaryColor};</#if>
        <#if themeAccentColor?? && themeAccentColor != "null">--accent-color: ${themeAccentColor};</#if>
        <#if themeLinkColor?? && themeLinkColor != "null">--link-color: ${themeLinkColor};</#if>   
        <#if themeTextColor?? && themeTextColor != "null">--text-color: ${themeTextColor};</#if>
        <#if logoUrl?? && logoUrl != "null">--logo-url: url('${logoUrl}');</#if>
        <#if logoSmallUrl?? && logoSmallUrl != "null">--logo-small-url: url('${logoSmallUrl}');</#if>
    }
</style>

<link rel="shortcut icon" type="image/x-icon" href="${urls.base}/favicon.ico">
