/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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
  goog.provide("gn_auditable_directive");
  goog.require("gn_auditable_service");

  var module = angular.module("gn_auditable_directive", ["gn_auditable_service"]);

  module.directive("gnAuditableHistory", [
    "gnAuditableService",
    "gnConfigService",
    "gnConfig",
    function (gnAuditableService, gnConfigService, gnConfig) {
      return {
        restrict: "A",
        replace: true,
        scope: {
          id: "=gnAuditableHistory",
          type: "@"
        },
        templateUrl: "../../catalog/components/auditable/partials/auditableHistory.html",
        link: function (scope, element, attrs) {
          scope.history = [];

          gnConfigService.load().then(function (c) {
            if (gnConfig["system.auditable.enable"]) {
              scope.$watch("id", function (n, o) {
                if (n !== o && n !== undefined) {
                  scope.history = [];

                  gnAuditableService
                    .getEntityHistory(scope.type, scope.id)
                    .then(function (response) {
                      scope.history = response.data;
                    });
                }
              });
            }
          });
        }
      };
    }
  ]);
})();
