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
  goog.provide("gn_history_directive");
  goog.require("gn_history_service");

  var module = angular.module("gn_history_directive", []);

  /**
   * Display list of events, tasks and workflow status for a record.
   */
  module.directive("gnRecordHistory", [
    "$http",
    "gnConfig",
    "gnConfigService",
    "gnRecordHistoryService",
    function ($http, gnConfig, gnConfigService, gnRecordHistoryService) {
      return {
        restrict: "A",
        replace: true,
        scope: {
          md: "=gnRecordHistory",
          collapsed: "@"
        },
        templateUrl: "../../catalog/components/history/partials/recordHistory.html",
        link: function postLink(scope, element, attrs) {
          scope.lang = scope.$parent.lang;
          scope.user = scope.$parent.user;
          scope.history = [];
          scope.hasMoreRecords = false;
          var recordByPage = 5;

          scope.filter = {};

          gnConfigService.load().then(function (c) {
            var types = {};
            if (gnConfig["metadata.workflow.enable"]) {
              types.workflow = true;
            }
            // Currently the only task is DOI
            if (gnConfig["system.publication.doi.doienabled"]) {
              types.task = true;
            }
            types.event = true;

            scope.filter = {
              types: types,
              recordFilter: null,
              from: 0,
              size: recordByPage
            };
          });

          scope.getNumberOfTypes = function () {
            return Object.keys(scope.filter.types).length;
          };

          // Wait for metatada to be available
          scope.$watch("md", function (n, o) {
            if (angular.isDefined(n)) {
              loadHistory();
            }
          });

          scope.more = function () {
            scope.filter.size = scope.filter.size + recordByPage;
            loadHistory();
          };

          function loadHistory() {
            // History step removal is only allowed to admin
            // BTW allowRemoval attribute could control if remove button
            // is displayed or not.
            scope.allowRemoval = false;
            if (scope.user && scope.user.isAdministrator) {
              scope.allowRemoval = angular.isDefined(attrs.allowRemoval)
                ? attrs.allowRemoval == "true" && scope.user.isAdministrator()
                : scope.user.isAdministrator();
            }

            if (scope.md) {
              scope.filter.recordFilter = scope.md.id;
              gnRecordHistoryService.search(scope.filter).then(function (r) {
                scope.history = r.data;
                scope.hasMoreRecords = r.data.length >= scope.filter.size;
              });
            }
          }

          scope.$watch(
            "filter",
            function (n, o) {
              if (n !== o) {
                loadHistory();
              }
            },
            true
          );
        }
      };
    }
  ]);

  module.directive("gnRecordHistoryStep", [
    "gnDoiService",
    "gnRecordHistoryService",
    "$translate",
    "$window",
    function (gnDoiService, gnRecordHistoryService, $translate, $window) {
      return {
        restrict: "A",
        replace: true,
        scope: {
          h: "=gnRecordHistoryStep",
          noTitle: "@noTitle",
          noSourceViewOption: "@noTitle",
          noRecoverOption: "@noTitle",
          allowRemoval: "=allowRemoval"
        },
        templateUrl: "../../catalog/components/history/partials/historyStep.html",
        link: function postLink(scope, element, attrs) {
          scope.removeStep = function (s) {
            gnRecordHistoryService.delete(s).then(function (r) {
              scope.$parent.loadHistory();
            });
          };

          scope.restoreHistoryElement = function (statusId) {
            var confirmMessage = $translate.instant("confirmRestore");
            if ($window.confirm(confirmMessage)) {
              return gnRecordHistoryService.restoreHistoryElement(statusId).then(
                function (r) {
                  var message = $translate.instant("recordRestored");
                  scope.$emit("StatusUpdated", {
                    msg: message,
                    timeout: 0,
                    type: "info"
                  });
                  // Reload the page to load the new value
                  $window.location.reload();
                },
                function (response) {
                  var message = "";
                  if (response.status === 403) {
                    message = $translate.instant("notAllowedError");
                  } else if (response.status === 404) {
                    message = $translate.instant("notFoundError");
                  } else if (response.status === 500) {
                    message = $translate.instant("internalServerError");
                  } else {
                    message = $translate.instant("internalServerError");
                  }
                  scope.$emit("StatusUpdated", {
                    msg: message,
                    timeout: 0,
                    type: "danger"
                  });
                }
              );
            }
          };

          scope.closeTask = function (status) {
            // Close the related task
            gnRecordHistoryService.close(status).then(function () {
              scope.$parent.loadHistory();
            });
          };
        }
      };
    }
  ]);
  /**
   * Manager
   */
  module.directive("gnHistory", [
    "$http",
    "$filter",
    "gnConfig",
    "gnConfigService",
    "$translate",
    "gnSearchManagerService",
    "gnRecordHistoryService",
    function (
      $http,
      $filter,
      gnConfig,
      gnConfigService,
      $translate,
      gnSearchManagerService,
      gnRecordHistoryService
    ) {
      return {
        restrict: "A",
        replace: true,
        scope: {},
        templateUrl: "../../catalog/components/history/partials/history.html",
        link: function postLink(scope, element, attrs) {
          scope.lang = scope.$parent.lang;
          scope.user = scope.$parent.user;
          var recordByPage = 20;
          scope.history = [];
          scope.filter = {};

          gnConfigService.load().then(function (c) {
            var types = {};
            if (gnConfig["metadata.workflow.enable"]) {
              types.workflow = true;
            }
            types.task = true;
            types.event = true;

            scope.filter = {
              types: types,
              ownerFilter: null,
              authorFilter: null,
              uuid: null,
              dateFromFilter: null,
              dateToFilter: null,
              from: 0,
              size: recordByPage
            };
          });

          scope.hasMoreRecords = false;

          scope.defaultSearchObj = {
            params: {
              isTemplate: ["y", "n"],
              from: 1,
              to: 20
            },
            defaultParams: {
              isTemplate: ["y", "n"],
              from: 1,
              to: 20
            }
          };

          scope.more = function () {
            scope.filter.size = scope.filter.size + recordByPage;
            scope.loadHistory();
          };

          scope.loadHistory = function () {
            gnRecordHistoryService.search(scope.filter).then(function (r) {
              scope.history = r.data;
              scope.hasMoreRecords = r.data.length >= scope.filter.size;
            });
          };

          scope.$watch(
            "filter",
            function (n, o) {
              if (n !== o) {
                scope.loadHistory();
              }
            },
            true
          );

          scope.loadHistory();
        }
      };
    }
  ]);
})();
