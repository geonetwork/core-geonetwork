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
  goog.provide("gn_dashboard_record_link_controller");

  goog.require("gn_utility_service");

  var module = angular.module("gn_dashboard_record_link_controller", [
    "gn_utility_service"
  ]);

  module.controller("GnDashboardRecordLinksController", [
    "$scope",
    "$routeParams",
    "$http",
    "$rootScope",
    "$translate",
    "$element",
    "gnLangs",
    "$compile",
    "gnHumanizeTimeService",
    "$window",
    "getBsTableLang",
    function (
      $scope,
      $routeParams,
      $http,
      $rootScope,
      $translate,
      $element,
      gnLangs,
      $compile,
      gnHumanizeTimeService,
      $window,
      getBsTableLang
    ) {
      $scope.filter = {};
      $scope.selections = [];
      $scope.selectedSelection = {};
      $scope.selectionFilter = "";
      $scope.groupLinkFilter = null;
      $scope.groupOwnerIdFilter = null;
      $scope.excludeHarvestedMetadataFilter = true;
      $scope.linkStatusFilter = "";
      $scope.urlFilter = "";
      $scope.uuidFilter = "";

      $scope.linkStatusValues = ["ok", "ko", "unknown"];
      $scope.httpStatusValues = ["-200", "200", "404", "401", "500"];
      $scope.httpStatusValueFilter = $scope.httpStatusValues[0];

      $scope.processesRunning = false;

      $scope.refreshProcesses = false;

      $http.get("../api/selections").then(function (r) {
        Object.keys(r.data).map(function (k) {
          $scope.selections.push({
            id: k,
            count: r.data[k],
            label: $translate.instant(k) + (r.data[k] ? " (" + r.data[k] + ")" : "")
          });
        });
        $scope.selections = $scope.selections.filter(function (s) {
          return s.count > 0;
        });
        $scope.selections.push({ id: "", label: "" });
      });

      $scope.processesFinished = function () {
        $scope.processesRunning = false;
        $scope.triggerSearch();
      };

      $scope.resetForm = function () {
        $scope.selectionFilter = "";
        $scope.selectedSelection.id = null;
        $scope.groupLinkFilter = null;
        $scope.groupOwnerIdFilter = null;
        $scope.excludeHarvestedMetadataFilter = true;
        $scope.linkStatusFilter = "";
        $scope.urlFilter = "";
        $scope.uuidFilter = "";
        $scope.httpStatusValueFilter = $scope.httpStatusValues[0];

        $scope.triggerSearch();
      };

      $scope.triggerSearch = function () {
        $("#bstable").bootstrapTable("refresh");
      };

      $scope.analyzeLinks = function () {
        $scope.processesRunning = true;
        $http.post("../api/records/links/analyze?analyze=true").then(function () {
          // Force to update the process list
          $scope.refreshProcesses = true;
        });
      };
      $scope.testLink = function (url) {
        $scope.processesRunning = true;

        $http
          .post("../api/records/links/analyzeurl?url=" + encodeURIComponent(url))
          .then(function () {
            // Force to update the process list
            $scope.refreshProcesses = true;
          });
      };

      $scope.downloadAsCsv = function () {
        var maxPageSize = 20000;
        window.open(
          "../api/records/links/csv?" +
            (!!$scope.groupIdFilter && $scope.groupIdFilter != "undefined"
              ? "groupIdFilter=" + $scope.groupIdFilter
              : "") +
            (!!$scope.groupOwnerIdFilter && $scope.groupOwnerIdFilter != "undefined"
              ? "&groupOwnerIdFilter=" + $scope.groupOwnerIdFilter
              : "") +
            ($scope.excludeHarvestedMetadataFilter
              ? "excludeHarvestedMetadataFilter=" + $scope.excludeHarvestedMetadataFilter
              : "") +
            (!!$scope.filter.filter && $scope.filter.filter != "undefined"
              ? "&filter=" + encodeURIComponent($scope.filter.filter)
              : "") +
            "&page=0&size=" +
            maxPageSize +
            "&sort=lastState%2Cdesc"
        );
      };

      $scope.removeAll = function () {
        $http.delete("../api/records/links").then(function () {
          $scope.triggerSearch();
          // Force to update the process list
          $scope.refreshProcesses = true;
        });
      };

      $window.lastState = {
        ok: $translate.instant("valid-1"),
        ko: $translate.instant("valid-0"),
        unknown: $translate.instant("valid--1")
      };

      $scope.$watch("selectedSelection.id", function (n, o) {
        if (angular.isDefined(n) && n !== o) {
          if (n != "") {
            $http.get("../api/selections/" + n).then(function (r) {
              $scope.selectionFilter = r.data.join(" ");
            });
          } else {
            $scope.selectionFilter = "";
          }
        }
      });

      $element.on("post-body.bs.table", function () {
        $element.find("a[data-job-key]").click(function (event) {
          var btn = $(event.currentTarget);
          $scope.$apply(function () {
            $scope.testLink(btn.attr("data-job-key"));
          });
          event.preventDefault();
        });
      });

      $scope.bsTableControl = {
        options: {
          url: "../api/records/links",
          sidePagination: "server",
          queryParamsType: "page,size",
          contentType: "application/x-www-form-urlencoded",
          pagination: true,
          paginationLoop: true,
          paginationHAlign: "right",
          paginationVAlign: "bottom",
          paginationDetailHAlign: "left",
          paginationPreText: "previous",
          paginationNextText: "Next page",
          filterControl: true,
          style: "min-height:100",
          classes: "table table-responsive full-width",
          height: "800",
          sortName: "lastState",
          sortOrder: "desc",

          responseHandler: function (res) {
            return {
              rows: res.content,
              total: res.totalElements,
              pageNumber: res.number,
              pageSize: res.size
            };
          },
          ajaxOptions: {
            method: "POST",
            headers: {
              "X-XSRF-TOKEN": $rootScope.csrf
            }
          },
          queryParams: function (params) {
            var filter = {};

            if ($scope.selectionFilter != "") {
              filter.records = $scope.selectionFilter;
            }

            if ($scope.linkStatusFilter != "") {
              filter.lastState = $scope.linkStatusFilter;
            }

            if ($scope.urlFilter != "") {
              filter.url = $scope.urlFilter;
            }

            if ($scope.uuidFilter != "") {
              filter.records = $scope.uuidFilter;
            }

            params.filter = angular.toJson(filter);

            $scope.filter = {
              groupIdFilter:
                $scope.groupIdFilter == undefined ? "" : $scope.groupIdFilter,
              groupOwnerIdFilter:
                $scope.groupOwnerIdFilter == undefined ? "" : $scope.groupOwnerIdFilter,
              httpStatusValueFilter:
                $scope.httpStatusValueFilter == undefined
                  ? ""
                  : $scope.httpStatusValueFilter,
              excludeHarvestedMetadataFilter:
                $scope.excludeHarvestedMetadataFilter === false
                  ? ""
                  : $scope.excludeHarvestedMetadataFilter,
              filter: params.filter,
              page: params.pageNumber - 1,
              size: params.pageSize,
              sort: params.sortName + "," + params.sortOrder
            };
            return $scope.filter;
          },
          columns: [
            {
              field: "lastState",
              title: $translate.instant("linkStatus"),
              titleTooltip: "",
              formatter: function (val, row) {
                var _class = "fa-question text-muted";
                // as I can't upgrade bstable version, defining key so is a very dirty fix for
                // https://github.com/wenzhixin/bootstrap-table/commit/961eed40b81b7133578e21358b5299629d642825
                // key is bound with  $window.lastState key
                var _key = "unknown";
                if (val == -1) {
                  _class = "fa-exclamation-triangle text-danger";
                  _key = "ko";
                } else if (val == 1) {
                  _class = "fa-check text-success";
                  _key = "ok";
                }
                return (
                  "<div><i class='fa fa-fw fa-2x " +
                  _class +
                  "'><p class='hidden'>" +
                  _key +
                  "</p></i></div>"
                );
              }.bind(this)
            },
            {
              field: "url",
              title: $translate.instant("url"),
              titleTooltip: $translate.instant("url"),
              sortable: true,
              formatter: function (val, row) {
                return "<a href='" + row.url + "' target='_blank'>" + row.url + "</a>";
              }.bind(this)
            },
            {
              field: "lastCheck",
              title: $translate.instant("lastCheck"),
              titleTooltip: $translate.instant("lastCheck"),
              sortable: true,
              formatter: function (val, row) {
                if (row.lastCheck && row.lastCheck.dateAndTimeUtc) {
                  var date = gnHumanizeTimeService(
                    row.lastCheck.dateAndTimeUtc,
                    null,
                    true
                  );
                  return '<div title="' + date.title + '">' + date.value + "</div>";
                } else {
                  return "";
                }
              }.bind(this)
            },
            {
              field: "linkStatus.statusValue",
              title: $translate.instant("requestStatus"),
              titleTooltip: $translate.instant("requestStatus"),
              sortable: true,
              formatter: function (val, row) {
                if (row.linkStatus && row.linkStatus[0]) {
                  return (
                    row.linkStatus[0].statusValue +
                    (row.linkStatus[0].statusInfo != ""
                      ? ": " + row.linkStatus[0].statusInfo
                      : "")
                  );
                } else {
                  return "";
                }
              }.bind(this)
            },
            {
              field: "records",
              title: $translate.instant("associatedRecords"),
              titleTooltip: $translate.instant("associatedRecords"),
              sortable: false,
              formatter: function (val, row) {
                var ulElem = "<ul>";
                for (var i = 0; i < row.records.length; i++) {
                  var record = row.records[i];
                  var aElem =
                    "<li><a href='catalog.search#/metadata/" +
                    record.metadataUuid +
                    "' target='_blank'>" +
                    record.metadataUuid +
                    "</a></li>";
                  ulElem = ulElem + aElem;
                }
                ulElem = ulElem + "</ul>";
                return ulElem;
              }.bind(this)
            },
            {
              title: $translate.instant("testLink"),
              formatter: function (value, row) {
                var key = row.url;
                return (
                  '<a class="btn btn-xs btn-block btn-default" data-job-key="' +
                  key +
                  '"><icon class="fa fa-fw fa-play"></icon></a>'
                );
              }.bind(this)
            }
          ],
          locale: getBsTableLang()
        }
      };
    }
  ]);

  var STATUS_UNDEFINED = 0;
  var STATUS_PROBABLE = 1;
  var STATUS_INPROGRESS = 2;
  var STATUS_FINISHED = 3;
  var STATUS_ERRORS = 4;

  var ANALYZE_RECORD_LABEL = [
    "taskUndefined",
    "taskProbable",
    "analyseRecordRunning",
    "analyseRecordFinished",
    "analyseRecordFinishedWithErrors"
  ];

  var TEST_LINK_LABEL = [
    "taskUndefined",
    "taskProbable",
    "testLinkRunning",
    "testLinkFinished"
  ];
  var ICON = [
    "fa-question",
    "fa-question",
    "fa-spinner fa-spin",
    "fa-check",
    "fa-exclamation-triangle"
  ];
  var CLASS = ["", "", "", "success", "warning"];

  module.directive("gnDashboardRecordLinksProcessesContainer", [
    "$http",
    "gnConfig",
    function ($http, gnConfig) {
      return {
        restrict: "E",
        scope: {
          refresh: "<",
          processesFinishedCallback: "&"
        },
        templateUrl:
          "../../catalog/components/admin/recordlink/partials/recordlinksanalyseprocesscontainer.html",
        controllerAs: "ctrl",
        controller: [
          "$scope",
          "$element",
          "$attrs",
          function ($scope) {
            this.tasks = [];
            var me = this;

            $scope.$watch("refresh", function (newValue, oldValue) {
              if (newValue === true) {
                $scope.ctrl.refresh();
              }
            });

            this.getStatusCode = function (errors, processed, total) {
              if (total === -1) {
                return STATUS_PROBABLE;
              }
              if (total > processed + errors) {
                return STATUS_INPROGRESS;
              }
              if (total === processed) {
                return STATUS_FINISHED;
              }
              if (total === processed + errors) {
                return STATUS_ERRORS;
              }
              return STATUS_UNDEFINED;
            };

            this.getProcessRatio = function (processedErrorOrNot, total) {
              return Math.round((1000 * processedErrorOrNot) / total) * 0.001;
            };

            this.refresh = function () {
              $http
                .get(
                  "../../jolokia/read/" +
                    "geonetwork-" +
                    gnConfig["system.site.siteId"] +
                    ":name=url-check,idx=*"
                )
                .then(function (result) {
                  if (!result.data || !result.data.value) {
                    return;
                  }

                  me.tasks = [];
                  var probes = Object.values(result.data.value);
                  probes.sort(function (a, b) {
                    return b.AnalyseMdDate - a.AnalyseMdDate;
                  });

                  var processesFinished = false;

                  probes.forEach(function (probe) {
                    var probeName = probe.ObjectName.objectName;
                    if (probeName && !probeName.includes("empty-slot")) {
                      var analyzeRecordStatus = me.getStatusCode(
                        probe.MetadataNotAnalysedInError,
                        probe.MetadataAnalysed,
                        probe.MetadataToAnalyseCount
                      );

                      var addProbe = true;

                      if (probe.ProcessFinished) {
                        processesFinished = true;

                        var finishDate = moment(new Date(probe.FinishDate));
                        var now = moment();

                        var diff = now.diff(finishDate, "minutes");

                        addProbe = diff <= 5;
                      } else {
                        processesFinished = false;
                      }

                      if (addProbe) {
                        var testLinkStatus = me.getStatusCode(
                          0,
                          probe.UrlChecked,
                          probe.UrlToCheckCount
                        );
                        me.tasks.push({
                          id: probeName,
                          records: {
                            errors: probe.MetadataNotAnalysedInError,
                            processed: probe.MetadataAnalysed,
                            total: probe.MetadataToAnalyseCount,
                            label: ANALYZE_RECORD_LABEL[analyzeRecordStatus],
                            class: CLASS[analyzeRecordStatus],
                            icon: ICON[analyzeRecordStatus],
                            ratio: me.getProcessRatio(
                              probe.MetadataNotAnalysedInError + probe.MetadataAnalysed,
                              probe.MetadataToAnalyseCount
                            )
                          },
                          links: {
                            errors: 0,
                            processed: probe.UrlChecked,
                            total: probe.UrlToCheckCount,
                            label: TEST_LINK_LABEL[testLinkStatus],
                            class: CLASS[testLinkStatus],
                            icon: ICON[testLinkStatus],
                            ratio: me.getProcessRatio(
                              probe.UrlChecked,
                              probe.UrlToCheckCount
                            )
                          }
                        });
                      }
                    }
                  });

                  if (processesFinished) {
                    $scope.processesFinishedCallback();
                  } else {
                    setTimeout(me.refresh, 5000);
                  }
                });
            };
            this.refresh();
          }
        ]
      };
    }
  ]);

  module.directive("gnDashboardRecordLinksProcessesStatus", [
    function () {
      return {
        restrict: "E",
        scope: { taskInfo: "<" },
        templateUrl:
          "../../catalog/components/admin/recordlink/partials/recordlinksanalyseprocessstatus.html"
      };
    }
  ]);
})();
