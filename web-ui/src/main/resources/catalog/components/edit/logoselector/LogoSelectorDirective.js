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
  goog.provide("gn_logo_selector_directive");

  var module = angular.module("gn_logo_selector_directive", []);

  /**
   * Add a toggle button with a list of logo next
   * to an input. Apply the directive to the parent
   * element of the input.
   */
  module.directive("gnLogoSelector", [
    "$http",
    function ($http) {
      return {
        restrict: "A",
        transclude: true,
        templateUrl:
          "../../catalog/components/edit/" + "logoselector/partials/logoselector.html",
        link: function (scope, element, attrs) {
          // TODO: Get path to image based
          scope.path =
            location.origin +
            "/" +
            location.pathname.split("/")[1] +
            "/images/harvesting/";
          scope.setLogo = function (i) {
            $(element).find("input").get(0).value = scope.path + i;
          };

          $http
            .get("admin.harvester.info@json?type=icons", { cache: true })
            .then(function (response) {
              scope.logos = response.data[0];
            });
        }
      };
    }
  ]);
})();
