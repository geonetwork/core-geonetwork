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
  goog.provide("gn_report_controller");

  var module = angular.module("gn_report_controller", []);

  /**
   * ReportController provides all necessary operations
   * to build reports.
   */
  module.controller("GnReportController", [
    "$scope",
    "$routeParams",
    "$http",
    "$rootScope",
    "$translate",
    function ($scope, $routeParams, $http, $rootScope, $translate) {
      $scope.pageMenu = {
        folder: "report/",
        defaultTab: "report-updated-metadata",
        tabs: [
          {
            type: "report-updated-metadata",
            label: "reportUpdatedMetadata",
            icon: "fa-check-square-o",
            href: "#/reports/report-updated-metadata"
          },
          {
            type: "report-internal-metadata",
            label: "reportInternalMetadata",
            icon: "fa-sign-in",
            href: "#/reports/report-internal-metadata"
          },
          {
            type: "report-fileupload-metadata",
            label: "reportFileUploadMetadata",
            icon: "fa-upload",
            href: "#/reports/report-fileupload-metadata"
          },
          {
            type: "report-filedownload-metadata",
            label: "reportFileDownloadMetadata",
            icon: "fa-download",
            href: "#/reports/report-filedownload-metadata"
          },
          {
            type: "report-users",
            label: "reportUsers",
            icon: "fa-users",
            href: "#/reports/report-users"
          }
        ]
      };

      $scope.groups = null;
      $scope.report = {};
      $scope.report.suggestedDate = "";

      $scope.report.dateFrom = new Date(moment().format("YYYY-MM-DD"));
      $scope.report.dateTo = new Date(moment().format("YYYY-MM-DD"));

      /**
       * Creates the records updated report
       */
      $scope.createReport = function (formId, service) {
        $(formId).attr("action", service);
        $(formId).submit();
      };

      /**
       * Listener for suggested date range selection to update
       * the date controls with the date range selected.
       */
      $scope.$watch("report.suggestedDate", function (newValue, oldValue) {
        // Ignore empty value: in initial setup and
        // if form already mirrors new value.
        if (
          newValue === "" ||
          newValue === oldValue ||
          $scope.report.suggestedDate.value === newValue
        ) {
          return;
        }

        // Calculate the dateFrom and dateTo values
        var today = moment();

        if (newValue === "currentMonth") {
          var month = today.format("MM");
          var year = today.format("YYYY");

          $scope.report.dateFrom = new Date(year + "-" + month + "-" + "01");
          $scope.report.dateTo = new Date(year + "-" + month + "-" + today.daysInMonth());
        } else if (newValue === "previousMonth") {
          // Set previous month
          today.add("months", -1);

          var month = today.format("MM");
          var year = today.format("YYYY");

          $scope.report.dateFrom = new Date(year + "-" + month + "-" + "01");
          $scope.report.dateTo = new Date(year + "-" + month + "-" + today.daysInMonth());
        } else if (newValue == "currentYear") {
          var year = today.format("YYYY");

          $scope.report.dateFrom = new Date(year + "-" + "01" + "-" + "01");
          $scope.report.dateTo = new Date(year + "-" + "12" + "-" + "31");
        } else if (newValue == "previousYear") {
          // Set previous year
          today.add("year", -1);

          var year = today.format("YYYY");

          $scope.report.dateFrom = new Date(year + "-" + "01" + "-" + "01");
          $scope.report.dateTo = new Date(year + "-" + "12" + "-" + "31");
        }
      });

      $scope.$watch("user.id", function (newId) {
        if (angular.isDefined(newId)) {
          loadGroups();
        }
      });

      function loadGroups() {
        if ($scope.user.profile == null) return;

        if ($scope.user.profile === "Administrator") {
          $http.get("../api/groups").then(
            function (response) {
              $scope.groups = response.data;
            },
            function (response) {
              // TODO
            }
          );
        } else {
          $http.get("../api/users/" + $scope.user.id + "/groups").then(
            function (response) {
              // Extract the group property from user groups array
              var groups = _.map(response.data, "group");

              // Get unique groups
              $scope.groups = _.uniqBy(groups, function (e) {
                return e.id;
              });
            },
            function (response) {
              // TODO
            }
          );
        }
      }
    }
  ]);
})();
