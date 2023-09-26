/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
(function () {
  var gnLandingPage = {};
  gnLandingPage.displayLanguage = function (lang, target) {
    target.parentElement.parentElement.querySelectorAll("li").forEach(function (li) {
      li.classList.remove("active");
    });
    target.parentElement.classList.add("active");

    var displayAll = lang === "";

    document
      .querySelectorAll("section.gn-md-side-access > div > a.btn-primary")
      .forEach(function (btn) {
        btn.setAttribute(
          "href",
          btn.getAttribute("href").replace(/(.*\/srv\/)([a-z]{3})/, "$1" + lang)
        );
      });

    document.querySelectorAll("div[xml\\:lang]").forEach(function (div) {
      if (displayAll) {
        div.classList.remove("hidden");
      } else {
        var isFirst = false;
        if (div.previousElementSibling == null) {
          isFirst = true;
        } else {
          if (div.previousElementSibling.getAttribute("xml:lang") === null) {
            isFirst = true;
          }
        }

        if (isFirst) {
          var translationAvailable = false;
          div.parentElement.querySelectorAll("div[xml\\:lang]").forEach(function (child) {
            // Last element is default lang.
            var isLast = true;
            if (
              child.nextElementSibling != null &&
              child.nextElementSibling.getAttribute("xml:lang") !== null
            ) {
              isLast = false;
            }

            if (child.getAttribute("xml:lang") === lang) {
              child.classList.remove("hidden");
              translationAvailable = true;
            } else if (isLast) {
              child.classList.add("hidden");
              if (!translationAvailable) {
                div.classList.remove("hidden");
              }
            } else {
              child.classList.add("hidden");
            }
          });
        }
      }
    });
  };
})();
