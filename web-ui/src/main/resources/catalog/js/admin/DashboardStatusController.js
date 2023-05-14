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
  goog.provide("gn_dashboard_status_controller");

  var module = angular.module("gn_dashboard_status_controller", []);

  module.filter("count", function () {
    return function (input) {
      return !input ? "" : input.length;
    };
  });
  module.filter("ellipses", function () {
    return function (input) {
      if (input && input.length > 35) {
        return input.substring(0, 39) + " ...";
      } else {
        return input;
      }
    };
  });

  /**
   *
   */
  module.controller("GnDashboardStatusController", [
    "$scope",
    "$routeParams",
    "$http",
    "$rootScope",
    "$translate",
    "gnESFacet",
    "gnUtilityService",
    function (
      $scope,
      $routeParams,
      $http,
      $rootScope,
      $translate,
      gnESFacet,
      gnUtilityService
    ) {
      $scope.healthy = undefined;
      $scope.nowarnings = undefined;
      $scope.threadSortField = undefined;
      $scope.threadSortReverse = false;
      $scope.threadInfoLoading = false;
      $scope.hasIndexingError = false;

      $scope.indexStatus = null;
      function getIndexStatus() {
        $http.get("../api/site/index/synchronized").then(function (response) {
          $scope.indexStatus = response.data;
        });
      }
      getIndexStatus();

      function getRecordsWithIndexingErrors() {
        return $http.post("../api/search/records/_search?bucket=ie", {
          query: {
            bool: {
              must: { terms: { indexingError: [true] } }
            }
          },
          from: 0,
          size: 0
        });
      }
      getRecordsWithIndexingErrors().then(function (r) {
        $scope.hasIndexingError = r.data.hits.total.value > 0;
      });

      $scope.setThreadSortField = function (field) {
        if (field === $scope.threadSortField) {
          $scope.threadSortReverse = !$scope.threadSortReverse;
        } else {
          if (field === "name" || field === "state") {
            $scope.threadSortReverse = false;
          } else {
            $scope.threadSortReverse = true;
          }
          $scope.threadSortField = field;
        }
      };
      $scope.threadSortClass = function (field) {
        if ($scope.threadSortField === field) {
          if ($scope.threadSortReverse) {
            return ["fa", "fa-sort-up"];
          } else {
            return ["fa", "fa-sort-down"];
          }
        }
        return "";
      };
      $scope.toggleThreadContentionMonitoring = function () {
        $http
          .get(
            "../api/site/threads/debugging/true/" +
              $scope.threadStatus.threadContentionMonitoringEnabled
          )
          .then(function (response) {
            $scope.threadStatus = response.data;
          });
      };
      $scope.toggleThreadCpuTime = function () {
        $http
          .get(
            "../api/site/threads/debugging/false/" +
              $scope.threadStatus.threadCpuTimeEnabled
          )
          .then(function (response) {
            $scope.threadStatus = response.data;
          });
      };
      $scope.openThreadActivity = function (leaveOpen) {
        var threadActivityEl = $("#threadActivity");
        if (!leaveOpen) {
          threadActivityEl.collapse("toggle");
        }
        $scope.threadInfoLoading = true;
        $http.get("../api/site/threads/status").then(
          function (response) {
            $scope.threadInfoLoading = false;
            $scope.threadStatus = response.data;

            if (!leaveOpen) {
              $("html, body").animate(
                {
                  scrollTop: $("#threadActivityHeading").offset().top
                },
                1000
              );
            }

            setTimeout(function () {
              if (threadActivityEl.hasClass("in")) {
                $scope.openThreadActivity(true);
              }
            }, 2000);
          },
          function (response) {
            $scope.threadInfoLoading = false;
          }
        );
      };
      $scope.showStackTrace = function (thread, $event) {
        $scope.selectedThread = thread;
        $scope.threadStackTrace = "Loading...";
        $("#stackTrace").modal("toggle");
        $http.get("../api/site/threads/trace/" + thread.id).then(function (response) {
          $scope.threadStackTrace = response.data.stackTrace;
        });
      };
      $http.get("../../criticalhealthcheck").then(
        function (response) {
          $scope.healthy = true;
          $scope.criticalhealthcheck = response.data;
        },
        function (response) {
          $scope.healthy = false;
          $scope.criticalhealthcheck = response.data;
        }
      );

      $http.get("../../warninghealthcheck").then(
        function (response) {
          $scope.nowarnings = true;
          $scope.warninghealthcheck = response.data;
        },
        function (response) {
          $scope.nowarnings = false;
          $scope.warninghealthcheck = response.data;
        }
      );

      // log activity
      $scope.openLogActivity = function (leaveOpen) {
        var logActivityEl = $("#logActivity");
        var collapseIn = logActivityEl.hasClass("in");
        if (!leaveOpen && collapseIn === false) {
          $scope.visibleLogView = true;
        } else if (leaveOpen === true && collapseIn === true) {
          $scope.visibleLogView = true;
        } else {
          $scope.visibleLogView = false;
        }
        if (!leaveOpen) {
          logActivityEl.collapse("toggle");
        }
        $scope.logInfoLoading = true;
        $http.get("../api/site/logging/activity").then(
          function (response) {
            $scope.logInfoLoading = false;
            $scope.logActivity = response.data;

            if (!leaveOpen) {
              $("html, body").animate(
                {
                  scrollTop: $("#logActivityHeading").offset().top
                },
                1000
              );
            }

            setTimeout(function () {
              if (logActivityEl.hasClass("in")) {
                $scope.openLogActivity(true);
              }
            }, 2000);
          },
          function (response) {
            $scope.logInfoLoading = false;
          }
        );
      };

      $scope.downloadLog = function () {
        window.location = "../api/site/logging/activity/zip";
      };

      $scope.indexRecordsWithErrors = function () {
        getRecordsWithIndexingErrors().then(function () {
          // Select
          $http.put("../api/selections/ie").then(function () {
            $http.get("../api/records/index?bucket=ie").then(function (response) {
              var res = response.data;
              $rootScope.$broadcast("StatusUpdated", {
                msg: $translate.instant("selection.indexing.count", res),
                timeout: 2,
                type: res.success ? "success" : "danger"
              });
            });
          });
        });
      };

      $scope.getListOfUuids = function () {
        return gnUtilityService.getSelectionListOfUuids("ies");
      };

      $scope.indexMessages = function (md) {
        if (angular.isArray(md.indexingErrorMsg)) {
          return md.indexingErrorMsg;
        }

        return [md.indexingErrorMsg];
      };
      $scope.indexMessageTitle = function (errorMsg) {
        if (errorMsg === undefined) {
          return "Empty error message";
        }
        return errorMsg.split("|")[0];
      };
      $scope.indexMessageReason = function (errorMsg) {
        if (errorMsg === undefined) {
          return "Empty error message";
        }
        return errorMsg.split("|")[1];
      };
      $scope.rawIndexMessageDetail = function (errorMsg) {
        if (errorMsg === undefined) {
          return "Empty error message";
        }
        return errorMsg.split("|")[2];
      };
      $scope.restrictMessageWidth = function (detail) {
        var maxLine = 80,
          indentPattern = /(\s*).*/;

        if (!detail || detail.trim() == "") {
          return "";
        }

        var lines = detail.split("\n");

        detail = "";

        var nextSpace = function (line) {
          for (var j = maxLine; j < line.length; j++) {
            if (" " === line.charAt(j)) {
              return j;
            }
          }
          return line.length;
        };

        for (var i = 0; i < lines.length; i++) {
          var line = lines[i];
          var indent = indentPattern.exec(line)[1] + "    ";
          while (line.length > maxLine) {
            var ns = nextSpace(line);
            detail += line.substring(0, ns) + "\n";
            line = indent + line.substring(ns);
          }
          detail += line + "\n";
        }
        return detail;
      };
      $scope.indexMessageDetail = function (errorMsg) {
        return $scope.restrictMessageWidth($scope.rawIndexMessageDetail(errorMsg));
      };
      $scope.searchObj = {
        configId: "recordsWithErrors",
        selectionBucket: "ies",
        defaultParams: {
          indexingErrorMsg: "*",
          sortBy: "changeDate",
          sortOrder: "desc"
        }
      };

      $scope.searchObj.params = angular.extend({}, $scope.searchObj.defaultParams);
      $scope.facetConfig = gnESFacet.configs.recordsWithErrors.facets;
    }
  ]);
})();
