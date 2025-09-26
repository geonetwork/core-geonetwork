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
  goog.provide("gn_anonymous_access_directive");

  goog.require("gn_popup");
  goog.require("gn_anonymous_access_service");

  var module = angular.module("gn_anonymous_access_directive", [
    "gn_anonymous_access_service",
    "gn_popup"
  ]);

  /**
   * @ngdoc directive
   * @name gn_anonymous_access.directive:gnAnonymousAccess
   * @restrict A
   * @requires gnAnonymousAccessService
   * @requires $translate
   *
   * @description
   * The `gnAnonymousAccess` directive provides a form to display,
   * create and delete anonymous access for a metadata record.
   */
  module.directive("gnAnonymousAccess", [
    "gnAnonymousAccessService",
    "$translate",
    function (gnAnonymousAccessService, $translate) {
      return {
        restrict: "A",
        replace: false,
        templateUrl:
          "../../catalog/components/common/anonymousaccess/partials/" + "panel.html",
        scope: {
          id: "=gnAnonymousAccess"
        },
        link: function (scope) {
          var translations = null;
          $translate(["anonymousAccessCreated", "anonymousAccessCreatedError"]).then(
            function (t) {
              translations = t;
            }
          );

          angular.extend(scope, {
            lang: scope.$parent.lang,
            user: scope.$parent.user
          });

          if (angular.isUndefined(scope.id)) {
            scope.alertMsg = true;
          }

          scope.create = function () {
            return gnAnonymousAccessService.create(scope.id).then(
              function (response) {
                scope.$emit("AnonymousAccessCreated", response.data);
              },
              function (response) {
                scope.$emit("AnonymousAccessCreatedError", {
                  title: translations.anonymousAccessCreatedError,
                  error: response.data,
                  timeout: 0,
                  type: "danger"
                });
              }
            );
          };
        }
      };
    }
  ]);
})();
