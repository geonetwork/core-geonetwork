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
  goog.provide("gn_dashboard_wfs_indexing_controller");

  var module = angular.module("gn_dashboard_wfs_indexing_controller", ["bsTable"]);

  module.controller("GnDashboardWfsIndexingController", [
    "$q",
    "$scope",
    "$location",
    "$http",
    "$translate",
    "$element",
    "$timeout",
    "gnHttp",
    "gnAlertService",
    "gnLangs",
    "wfsFilterService",
    "gnHumanizeTimeService",
    "Metadata",
    function (
      $q,
      $scope,
      $location,
      $http,
      $translate,
      $element,
      $timeout,
      gnHttp,
      gnAlertService,
      gnLangs,
      wfsFilterService,
      gnHumanizeTimeService,
      Metadata
    ) {
      // this returns a valid xx_XX language code based on available locales in bootstrap-table
      // if none found, return 'en'
      // FIXME: use a global service
      function getBsTableLang() {
        var iso2 = gnLangs.getIso2Lang(gnLangs.getCurrent());
        var locales = Object.keys($.fn.bootstrapTable.locales);
        var lang = "en";
        locales.forEach(function (locale) {
          if (locale.startsWith(iso2)) {
            lang = locale;
            return true;
          }
        });
        return lang;
      }

      var defaultStrategy = "";

      $scope.wfsFilterValue = null;
      $scope.url = decodeURIComponent($location.search()["wfs-indexing"]);

      // sample CRON expressions
      $scope.cronExp = [
        "0 0 12 * * ?",
        "0 15 10 * * ?",
        "0 0/5 14 * * ?",
        "0 15 10 ? * MON-FRI",
        "0 15 10 15 * ?"
      ];

      // URL of the index service endpoint
      $scope.indexUrl = gnHttp.getService("featureindexproxy");

      // URL of the message producer CRUD API endpoint
      $scope.messageProducersApiUrl = "../api/msg_producers";

      // URL of the WFS indexing actions
      $scope.wfsWorkersApiUrl = "../api/workers/data/wfs/actions";

      // dictionary of wfs indexing jobs received from the index
      // key is url#typename
      // null means loading
      $scope.jobs = {};

      // same thing as a sorted array
      $scope.jobsArray = null;

      // cache of queried metadata records; key is uuid
      $scope.mdCache = {};

      // error on request or results parsing
      $scope.error = null;

      // true if request pending
      $scope.loading = false;

      // bs-table configuration
      $scope.bsTableControl = null;

      function updateMdTitle(containerDiv) {
        var div = $(containerDiv);
        var mdUuid = div.attr("data-md-uuid");
        var link = div.children(".md-title");
        var title = link.children("span");
        var spinner = div.children(".fa-spinner");
        var alert = div.children(".alert");
        var md = $scope.mdCache[mdUuid];

        if (!md) {
          return;
        }
        spinner.remove();
        if (md.error) {
          alert.text($translate.instant(md.error));
          alert.css({ display: "block" });
        } else if (md.resourceTitle) {
          title.text(md.resourceTitle);
          link.css({ display: "block" });
        }
      }

      // bind to table events to handle dynamic content
      $element.on("post-body.bs.table", function () {
        // bind buttons
        $element.find("a[data-job-key]").click(function (event) {
          var btn = $(event.currentTarget);
          $scope.$apply(function () {
            $scope.openScheduleSettings(btn.attr("data-job-key"));
          });
          event.preventDefault();
          $scope.filterWfsBsWithInput();
        });
        $element.find("a[data-trigger-job-key]").click(function (event) {
          var btn = $(event.currentTarget);
          $scope.$apply(function () {
            $scope.triggerIndexing(btn.attr("data-trigger-job-key"));
          });
          event.preventDefault();
          $scope.filterWfsBsWithInput();
        });
        $element.find("a[data-delete-key]").click(function (event) {
          var btn = $(event.currentTarget);
          $scope.$apply(function () {
            $scope.deleteWfsService(btn.attr("data-delete-key"));
          });
          event.preventDefault();
          $scope.filterWfsBsWithInput();
        });
        $element.find("div[data-md-uuid]").each(function () {
          updateMdTitle(this);
        });
      });

      $scope.filterItemsinArray = function (stringValue, data) {
        return data.filter(function (item) {
          if (item) {
            return item.toString().toLowerCase().indexOf(stringValue.toLowerCase()) >= 0;
          }
          return false;
        });
      };
      $scope.jobsArrayFiltered = function (job) {
        var filteredJob = {};
        // properties are based on the columns displayed in the table
        if (job.md && job.md.title) {
          filteredJob.title = job.md.title;
        }
        filteredJob.featureType = job.featureType;
        filteredJob.url = job.url;
        filteredJob.featureCount = job.featureCount;
        filteredJob.endDate = job.endDate;
        filteredJob.status = job.status;
        filteredJob.uuid = job.mdUuid;

        return filteredJob;
      };

      $scope.filterWfsBsWithInput = function () {
        if ($scope.wfsFilterValue == null) {
          return;
        }
        $scope.refreshBsTable(
          $scope.jobsArray.filter(function (job) {
            return (
              $scope.filterItemsinArray(
                $scope.wfsFilterValue,
                Object.values($scope.jobsArrayFiltered(job))
              ).length > 0
            );
          })
        );
      };

      $scope.wfsFilterValue = null;

      // dummy promise to make sure translations are available
      var langPromise = $translate("yes");

      $scope.refreshJobList = function () {
        $scope.jobs = {};
        $scope.uuids = {};
        $scope.error = null;
        $scope.loading = true;

        var indexQuery = $http.post($scope.indexUrl, {
          query: {
            query_string: {
              query: "docType:harvesterReport"
            }
          },
          size: 10000
        });
        var apiQuery = $http.get($scope.messageProducersApiUrl);

        $q.all([indexQuery, apiQuery, langPromise]).then(
          function (results) {
            var indexResults = results[0];
            var apiResults = results[1];

            $scope.loading = false;
            try {
              apiResults.data.forEach(function (producer) {
                var url = producer.wfsHarvesterParam.url;
                var featureType = producer.wfsHarvesterParam.typeName;
                var key = url + "#" + featureType;

                $scope.uuids[producer.wfsHarvesterParam.metadataUuid] = null;

                $scope.jobs[key] = {
                  url: url,
                  featureType: featureType,
                  status: "not started",
                  mdUuid: producer.wfsHarvesterParam.metadataUuid,
                  strategy: producer.wfsHarvesterParam.strategy || defaultStrategy,
                  cronScheduleExpression: producer.cronExpression,
                  cronScheduleProducerId: producer.id
                };
              });

              indexResults.data.hits.hits.forEach(function (hit) {
                var source = hit._source;

                var infos = decodeURIComponent(source.id).split("#");
                var key = infos[0] + "#" + infos[1];

                if ($scope.jobs[key]) {
                  $scope.jobs[key] = angular.merge($scope.jobs[key], {
                    featureCount: source.totalRecords_i || 0,
                    status:
                      source.endDate_dt === undefined
                        ? "ongoing"
                        : source.error_ss
                        ? "error"
                        : source.status_s,
                    error: source.error_ss,
                    endDate: source.endDate_dt
                  });
                }
              });
              $http
                .post(
                  "../api/search/records/_search",
                  {
                    query: {
                      bool: {
                        must: [{ terms: { uuid: Object.keys($scope.uuids) } }]
                      }
                    },
                    size: Object.keys($scope.uuids).length,
                    _source: ["resourceTitleObject"]
                  },
                  { cache: true }
                )
                .then(function (r) {
                  r.data.hits.hits.forEach(function (hit) {
                    $scope.mdCache[hit._id] = new Metadata(hit);
                    $element.find("div[data-md-uuid=" + hit._id + "]").each(function () {
                      updateMdTitle(this);
                    });
                  });
                  $scope.filterWfsBsWithInput();
                });

              $scope.jobsArray = Object.keys($scope.jobs)
                .sort()
                .map(function (key) {
                  return $scope.jobs[key];
                });
              $scope.refreshBsTable($scope.jobsArray);
            } catch (e) {
              $scope.error = e.message;
            }
          },
          function (result) {
            $scope.loading = false;
            $scope.error = result.data.error
              ? result.data.error.reason
              : "Could not reach index";
          }
        );
      };
      $scope.refreshJobList();

      var pageSize = 10;

      $scope.refreshBsTable = function (jobsArray) {
        $scope.bsTableControl = {
          options: {
            data: jobsArray,
            sidePagination: "client",
            pagination: true,
            pageSize: pageSize,
            paginationLoop: true,
            paginationHAlign: "right",
            paginationVAlign: "bottom",
            paginationDetailHAlign: "left",
            paginationPreText: $translate.instant("previous"),
            paginationNextText: $translate.instant("next"),
            style: "min-height:100",
            classes: "table table-responsive full-width",
            sortName: "endDate",
            sortOrder: "desc",
            columns: [
              {
                field: "mdUuid",
                title: $translate.instant("wfsIndexingMetadata"),
                formatter: function (value, row) {
                  return row.mdUuid
                    ? '<div data-md-uuid="' +
                        row.mdUuid +
                        '">' +
                        '  <a class="md-title" style="display: none" href="catalog.search#/metadata/' +
                        row.mdUuid +
                        '">' +
                        "    <span>" +
                        $translate.instant("recordWithNoTitle") +
                        "</span>" +
                        "  </a>" +
                        '  <div class="alert alert-danger small" style="display: none" role="alert"></div>' +
                        '  <i class="fa fa-spinner fa-spin"></i>' +
                        "</div>" +
                        "<code>" +
                        row.mdUuid +
                        "</code>"
                    : // '<a href="catalog.search#/metadata/' + row.mdUuid + '">' + row.mdUuid + '</a>' :
                      '<span class="text-muted">' +
                        $translate.instant("noRecordFound") +
                        "</span>";
                },
                sortable: true
              },
              {
                field: "url",
                title: $translate.instant("wfsurl"),
                formatter: function (value, row) {
                  var wfsUrl = row.url; // TODO: transform to getcapabilities
                  var label = $translate.instant("wfsIndexingFeatureType");
                  return (
                    '<a href="' +
                    row.url +
                    '">' +
                    row.url +
                    '</a> - <a href="' +
                    wfsUrl +
                    '">GetCapabilities</a><br>' +
                    "<span>" +
                    label +
                    "</span>: <code>" +
                    row.featureType +
                    "<code>"
                  );
                },
                sortable: true
              },
              {
                field: "featureCount",
                title: $translate.instant("featureCount"),
                sortable: true
              },
              {
                field: "endDate",
                title: $translate.instant("wfsIndexingEndDate"),
                sortable: true,
                sorter: function (a, b) {
                  return a && a.localeCompare(b);
                },
                formatter: function (value, row) {
                  if (value) {
                    var date = gnHumanizeTimeService(value, null, true);
                    return '<div title="' + date.title + '">' + date.value + "</div>";
                  } else {
                    return null;
                  }
                }
              },
              {
                field: "status",
                title: $translate.instant("status"),
                formatter: function (value, row) {
                  return (
                    '<span class="label label-' +
                    $scope.getLabelClass(row.status) +
                    '" ' +
                    'title="' +
                    (row.error || "") +
                    '">' +
                    row.status +
                    "</span>"
                  );
                },
                sortable: true
              },
              {
                field: "cronScheduleExpression",
                title: $translate.instant("wfsIndexingScheduled"),
                formatter: function (value) {
                  if (value !== null) {
                    var cronLabel = $translate.instant("cron-" + value);
                    return (
                      '<span title="' +
                      (cronLabel || value) +
                      '">' +
                      $translate.instant("yes") +
                      "</span>"
                    );
                  } else {
                    return $translate.instant("no");
                  }
                },
                sortable: true
              },
              {
                field: "cronScheduleProducerId",
                title: "",
                formatter: function (value, row, index) {
                  var key = row.url + "#" + row.featureType;
                  var labelEdit = $translate.instant("wfsIndexingEditSchedule");
                  var labelNow = $translate.instant("wfsIndexingTrigger");
                  var labelDelete = $translate.instant("wfsDeleteWfsIndexing");

                  return (
                    '<a class="btn btn-xs btn-block btn-default" data-job-key="' +
                    key +
                    '"><icon class="fa fa-fw fa-calendar"></icon>' +
                    labelEdit +
                    "</a>" +
                    '<a class="btn btn-xs btn-block btn-default" data-trigger-job-key="' +
                    key +
                    '"><icon class="fa fa-fw fa-play text-primary"></icon>' +
                    labelNow +
                    "</a>" +
                    '<a class="btn btn-xs btn-block btn-default" ' +
                    '   data-delete-key="' +
                    key +
                    '"><icon class="fa fa-fw fa-times text-danger"></icon>' +
                    labelDelete +
                    "</a>"
                  );
                }
              }
            ],
            locale: getBsTableLang()
          }
        };
      };

      $scope.getLabelClass = function (status) {
        switch (status.toLowerCase()) {
          case "success":
            return "success";
          case "error":
            return "danger";
          default:
            return "default";
        }
      };

      $scope.currentJob = null;
      $scope.settingsLoading = false;
      $scope.settingsError = null;

      var settingsModal = $("#gn-indexing-schedule");

      $scope.openScheduleSettings = function (key) {
        $scope.currentJob =
          key !== undefined ? angular.merge({}, $scope.jobs[key]) : { isNew: true };
        settingsModal.modal();
      };

      $scope.updateSchedule = function (job) {
        $scope.settingsLoading = true;
        var wfsHarvesterParams = {
          url: job.url,
          typeName: job.featureType,
          metadataUuid: job.mdUuid
        };

        var payload = {
          wfsHarvesterParam: wfsHarvesterParams,
          cronExpression:
            job.cronScheduleExpression !== "" ? job.cronScheduleExpression : null
        };

        var query = job.cronScheduleProducerId
          ? $http.put(
              $scope.messageProducersApiUrl + "/" + job.cronScheduleProducerId,
              payload
            )
          : $http.post($scope.messageProducersApiUrl, payload);

        query.then(
          function (response) {
            var savedJob = response.data;
            $scope.settingsLoading = false;

            var key =
              savedJob.wfsHarvesterParam.url + "#" + savedJob.wfsHarvesterParam.typeName;
            $scope.jobs[key] = $scope.jobs[key] || {};
            angular.merge($scope.jobs[key], {
              url: savedJob.wfsHarvesterParam.url,
              featureType: savedJob.wfsHarvesterParam.typeName,
              status: "not started",
              mdUuid: savedJob.wfsHarvesterParam.metadataUuid,
              cronScheduleExpression: savedJob.cronExpression,
              cronScheduleProducerId: savedJob.id
            });

            settingsModal.modal("hide");

            $scope.refreshJobList();
          },
          function (error) {
            $scope.settingsLoading = false;
            $scope.settingsError = error && error.data && error.data.message;
          }
        );
      };

      settingsModal.on("hidden.bs.modal", function () {
        $scope.currentJob = null;
        $scope.settingsLoading = false;
        $scope.settingsError = null;
      });

      $scope.updateParamsFromApplicationProfile = function (job) {
        var params = {
          typeName: job.featureType,
          url: job.url,
          strategy: job.strategy || defaultStrategy,
          metadataUuid: job.mdUuid,
          tokenizedFields: null,
          treeFields: null
        };

        return wfsFilterService
          .getApplicationProfile(null, job.mdUuid, job.featureType, job.url, "WFS")
          .then(function (response) {
            if (response.status == 200) {
              var appProfile = angular.fromJson(response.data["0"]);
              params.tokenizedFields = (appProfile && appProfile.tokenizedFields) || null;
              params.treeFields = (appProfile && appProfile.treeFields) || null;
              return params;
            }
          })
          .catch(function () {
            return params;
          });
      };

      $scope.triggerIndexing = function (key) {
        var job = $scope.jobs[key];
        $scope.updateParamsFromApplicationProfile(job).then(function (params) {
          $http.put($scope.wfsWorkersApiUrl + "/start", params).then(
            function () {
              gnAlertService.addAlert({
                msg: $translate.instant("wfsIndexingTriggerSuccess"),
                type: "success"
              });
            },
            function () {
              gnAlertService.addAlert({
                msg: $translate.instant("wfsIndexingTriggerError"),
                type: "danger"
              });
            }
          );
        });
      };

      // this will delete both the message producer and the indexed data
      $scope.deleteWfsService = function (key) {
        var job = $scope.jobs[key];

        if (job.cronScheduleProducerId === null) {
          return;
        }

        var urlParams =
          "?serviceUrl=" +
          encodeURIComponent(job.url) +
          "&typeName=" +
          encodeURIComponent(job.featureType);

        $q.all([
          $http.delete($scope.wfsWorkersApiUrl + urlParams),
          $http.delete($scope.messageProducersApiUrl + "/" + job.cronScheduleProducerId)
        ]).then(
          function () {
            gnAlertService.addAlert({
              msg: $translate.instant("wfsDeleteWfsIndexingSuccess"),
              type: "success"
            });
            // slight delay to let ES update its response accordingly
            $timeout(function () {
              $scope.refreshJobList();
            }, 750);
          },
          function () {
            gnAlertService.addAlert({
              msg: $translate.instant("wfsDeleteWfsIndexingError"),
              type: "danger"
            });
          }
        );
      };
    }
  ]);
})();
