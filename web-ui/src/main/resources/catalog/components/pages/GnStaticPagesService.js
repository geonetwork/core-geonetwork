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
  goog.provide("gn_static_pages_service");

  var module = angular.module("gn_static_pages_service", ["ngSanitize"]);

  module.service("gnStaticPagesService", [
    "$http",
    function ($http) {
      var loadPages = function (language, section) {
        return $http({
          method: "GET",
          url: "../api/pages?language=" + language + "&section=" + section.toUpperCase(),
          config: {
            cache: true
          }
        });
      };

      function buildMenu(pagesMenu, staticPages, pagesConfig) {
        pagesConfig.forEach(function (menu) {
          if (typeof menu === "string") {
            if (staticPages[menu]) {
              pagesMenu.push(staticPages[menu]);
            } else {
              console.warn(
                menu +
                  " not found in pages configuration." +
                  " Check your UI configuration."
              );
            }
          } else if (angular.isObject(menu)) {
            var key = Object.keys(menu)[0],
              submenu = {
                label: key,
                type: "submenu",
                pages: []
              };
            buildMenu(submenu.pages, staticPages, menu[key]);
            pagesMenu.push(submenu);
          }
        });
        return pagesMenu;
      }

      return {
        loadPages: loadPages,
        buildMenu: buildMenu
      };
    }
  ]);
})();
