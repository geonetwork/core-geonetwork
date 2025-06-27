/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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
  goog.provide("gn_index_report_directive");

  /**
   */
  angular.module("gn_index_report_directive", ["gn_utility"]).directive("gnIndexReport", [
    "gnCurrentEdit",
    "gnIndexService",
    function (gnCurrentEdit, gnIndexService) {
      return {
        restrict: "A",
        templateUrl:
          "../../catalog/components/edit/indexreport/" + "partials/indexreport.html",
        scope: {},
        link: function (scope, element, attrs) {
          scope.initialSectionsClosed =
            attrs.initialSectionStates == "closed" ? "true" : "false";
          if (attrs.initialSectionStates === undefined) {
            scope.initialSectionsClosed = "true"; //default
          }
          scope.alwaysOnTop = false;
          scope.gnCurrentEdit = gnCurrentEdit;
          scope.loading = false;

          scope.load = function () {
            scope.loading = true;

            gnIndexService
              .getIndexMessages(gnCurrentEdit.metadata.uuid)
              .then(function (report) {
                scope.warningMessages = report.warningMessages;
                scope.errorMessages = report.errorMessages;
              })
              .catch(function (error) {})
              .finally(function () {
                scope.loading = false;
              });
          };

          // When saving is done, refresh index report
          scope.$watch("gnCurrentEdit.saving", function (newValue) {
            if (newValue === false) {
              scope.load();
            }
          });
        }
      };
    }
  ]);
})();
