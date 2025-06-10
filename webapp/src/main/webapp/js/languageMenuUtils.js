/* $This file is distributed under the terms of the license in LICENSE$ */

$(function() {

	$.extend(this, i18nStringsLangMenu);
	var theText = this.selectLanguage;
	var imgHTML = "";

	function showMenu() {
		$("a#lang-link").html(theText);
		$(this).addClass("hover");
		$('ul:first', this)
			.css('visibility', 'visible')
			.attr('aria-hidden', 'false');
		$(this).attr('aria-expanded', 'true');
		$("ul.language-dropdown ul.sub_menu li").css('width', ($("ul.language-dropdown").width() - 4) + 'px');
	}

	function hideMenu() {
		$("a#lang-link").html(imgHTML);
		$(this).removeClass("hover");
		$('ul:first', this)
			.css('visibility', 'hidden')
			.attr('aria-hidden', 'true');
		$(this).attr('aria-expanded', 'false');
	}

	$("ul.language-dropdown li#language-menu")
		.hover(showMenu, hideMenu)
		.on('focusin', showMenu)
		.on('focusout', function(e) {
			var relatedTarget = e.relatedTarget;
			if (!$.contains(this, relatedTarget)) {
				hideMenu.call(this);
			}
		});

	$("ul.language-dropdown li ul li:has(ul)").find("a:first").append(" &raquo; ");

	$("ul.language-dropdown ul.sub_menu li").each(function() {
		if ($(this).attr("status") == "selected") {
			$("a#lang-link").html($(this).children("a").html());
			imgHTML = $(this).children("a").html();
		}
	});

});
