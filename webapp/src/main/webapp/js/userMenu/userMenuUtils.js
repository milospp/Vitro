/* $This file is distributed under the terms of the license in LICENSE$ */

$(function(){

    function showMenu() {
        $(this).addClass("hover");
        $('ul:first', this)
            .css('visibility', 'visible')
            .attr('aria-hidden', 'false');
        $(this)
            .attr('aria-expanded', 'true');
        if ($('ul.dropdown').width() > 88) {
            $('ul:first li', this).css('width', ($("ul.dropdown").width() - 22) + 'px');
        }
        $("ul.dropdown ul.sub_menu li:last-child").css('width', ($("ul.dropdown").width() - 14) + 'px');
    }

    function hideMenu() {
        $(this).removeClass("hover");
        $('ul:first', this)
            .css('visibility', 'hidden')
            .attr('aria-hidden', 'true');
        $(this)
            .attr('aria-expanded', 'false');
    }

    $("ul.dropdown li")
        .hover(showMenu, hideMenu)
        .on('focusin', showMenu)
        .on('focusout', function(e) {
            // Check if the newly focused element is still inside this li
            var relatedTarget = e.relatedTarget;
            if (!$.contains(this, relatedTarget)) {
                hideMenu.call(this);
            }
        });

    $("ul.dropdown li ul li:has(ul)").find("a:first").append(" &raquo; ");

});
