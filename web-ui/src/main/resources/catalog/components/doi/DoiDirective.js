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
  goog.provide("gn_doi_directive");
  goog.require("gn_doi_service");

  var module = angular.module("gn_doi_directive", ["gn_doi_service"]);

  module.directive("gnDoiWizard", [
    "gnDoiService",
    function (gnDoiService) {
      return {
        restrict: "A",
        replace: true,
        scope: {
          uuid: "=gnDoiWizard",
          doiUrl: "=?",
          xsMode: "@?"
        },
        templateUrl: "../../catalog/components/doi/partials/doiwidget.html",
        link: function (scope, element, attrs) {
          scope.gnDoiService = gnDoiService;
          scope.response = {};
          scope.isUpdate = angular.isDefined(scope.doiUrl);
          scope.doiServers = [];
          scope.selectedDoiServer = null;

          gnDoiService.getDoiServersForMetadata(scope.uuid).then(function (response) {
            scope.doiServers = response.data;

            if (scope.isUpdate) {
              gnDoiService
                .getDoiServerForMetadataAndDoi(scope.uuid, scope.doiUrl)
                .then(function (server) {
                  if (server) {
                    scope.selectedDoiServer = server.id;
                  }
                });
            } else {
              if (scope.doiServers.length > 0) {
                scope.selectedDoiServer = scope.doiServers[0].id;
              }
            }
          });

          scope.updateDoiServer = function () {
            scope.response = {};
          };

          scope.check = function () {
            scope.response = {};
            scope.response["check"] = null;
            return gnDoiService.check(scope.uuid, scope.selectedDoiServer).then(
              function (r) {
                scope.response["check"] = r;
                scope.isUpdate = angular.isDefined(scope.doiUrl);
              },
              function (r) {
                scope.response["check"] = r;
                scope.isUpdate = r.data.code === "resource_already_exist";
              }
            );
          };

          scope.create = function () {
            return gnDoiService.create(scope.uuid, scope.selectedDoiServer).then(
              function (r) {
                scope.response["create"] = r;
                delete scope.response["check"];
                scope.doiUrl = r.data.doiUrl;
              },
              function (r) {
                scope.response["create"] = r;
              }
            );
          };
        }
      };
    }
  ]);
})();
