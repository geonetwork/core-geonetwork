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

(function() {
  goog.provide('gn_dashboard_wfs_indexing_controller');

  var module = angular.module('gn_dashboard_wfs_indexing_controller',
      ['bsTable']);

  module.controller('GnDashboardWfsIndexingController', [
    '$q',
    '$scope',
    '$location',
    '$http',
    '$translate',
    '$element',
    'gnMetadataManager',
    'gnHttp',
    'gnAlertService',
    'gnLangs',
    function($q, $scope, $location, $http, $translate, $element, gnMetadataManager, gnHttp, gnAlertService, gnLangs) {
      // this returns a valid xx_XX language code based on available locales in bootstrap-table
      // if none found, return 'en'
      // FIXME: use a global service
      function getBsTableLang() {
        var iso2 = gnLangs.getIso2Lang(gnLangs.getCurrent());
        var locales = Object.keys($.fn.bootstrapTable.locales);
        var lang = 'en';
        locales.forEach(function (locale) {
          if (locale.startsWith(iso2)) {
            lang = locale;
            return true;
          }
        });
        return lang;
      }

      $scope.url = decodeURIComponent($location.search()['wfs-indexing']);

      // sample CRON expressions
      $scope.cronExp = [
        '0 0 12 * * ?',
        '0 15 10 * * ?',
        '0 0/5 14 * * ?',
        '0 15 10 ? * MON-FRI',
        '0 15 10 15 * ?'
      ];

      // URL of the index service endpoint
      $scope.indexUrl = gnHttp.getService('featureindexproxy') + '?_=_search';

      // URL of the message producer CRUD API endpoint
      $scope.messageProducersApiUrl = gnHttp.getService('wfsMessageProducers');

      // URL of the WFS indexing actions
      $scope.wfsWorkersApiUrl = gnHttp.getService('wfsWorkersActions');

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
        var mdUuid = div.attr('data-md-uuid');
        var link = div.children('.md-title');
        var title = link.children('span');
        var spinner = div.children('.fa-spinner');
        var md = $scope.mdCache[mdUuid];

        if (!md) {
          return;
        }
        spinner.remove();
        if (md.title) {
          title.text(md.title);
        }
        link.css({ display: 'block' });
      }

      // bind to table events to handle dynamic content
      $element.on('post-body.bs.table', function() {
        // bind buttons
        $element.find('button[data-job-key]').click(function(event) {
          var btn = $(event.currentTarget);
          $scope.$apply(function() {
            $scope.openScheduleSettings(btn.attr('data-job-key'));
          });
        });
        $element.find('button[data-trigger-job-key]').click(function(event) {
          var btn = $(event.currentTarget);
          $scope.$apply(function() {
            $scope.triggerIndexing(btn.attr('data-trigger-job-key'));
          });
        });

        // bind md title
        $element.find('div[data-md-uuid]').each(function() {
          updateMdTitle(this);
        });
      });

      $scope.filterItemsinArray = function(stringValue, data) {
        return data.filter(function(item) {
          if (item) {
            return item
              .toString()
              .toLowerCase()
              .indexOf(stringValue.toLowerCase()) >= 0;
          }
          return false;
        });
      };
      $scope.jobsArrayFiltered = function(job) {
        var filteredJob = {};
        // properties are based on the columns displayed in the table
        filteredJob.title = job.md.title;
        filteredJob.url = job.url;
        filteredJob.featureCount = job.featureCount;
        filteredJob.endDate = job.endDate;
        filteredJob.status = job.status;
        return filteredJob;
      };


      $scope.filterWfsBsWithInput = function() {
        $scope.refreshBsTable(
          $scope.jobsArray.filter(function (job) {
          return $scope.filterItemsinArray(
            $scope.wfsFilterValue, Object.values($scope.jobsArrayFiltered(job))).length > 0;
        }));
      };

      $scope.wfsFilterValue = null;

      $scope.refreshJobList = function() {
        $scope.jobs = {};
        $scope.error = null;
        $scope.loading = true;

        var indexQuery = $http.post($scope.indexUrl, {
          'query': {
            'query_string': {
              'query': 'docType:harvesterReport'
            }
          },
          'size': 10000
        });
        var apiQuery = $http.get($scope.messageProducersApiUrl);

        $q.all([indexQuery, apiQuery]).then(function(results) {
          var indexResults = results[0];
          var apiResults = results[1];

          $scope.loading = false;
          try {
            indexResults.data.hits.hits.forEach(function (hit) {
              var source = hit._source;

              var infos = decodeURIComponent(source.id).split('#');
              $scope.jobs[infos[0] + '#' + infos[1]] = {
                url: infos[0],
                featureType: infos[1],
                featureCount: source.totalRecords_i || 0,
                status: source.endDate_dt === undefined ? 'ongoing' : source.error_ss ? 'error' : source.status_s,
                mdUuid: source.parent,
                error: source.error_ss,
                endDate: source.endDate_dt,
                cronScheduleExpression: null,
                cronScheduleProducerId: null
              };
            });

            apiResults.data.forEach(function(producer) {
              var url = producer.wfsHarvesterParam.url;
              var featureType = producer.wfsHarvesterParam.typeName;
              var key = url + '#' + featureType;

              if ($scope.jobs[key]) {
                $scope.jobs[key].cronScheduleExpression = producer.cronExpression;
                $scope.jobs[key].cronScheduleProducerId = producer.id;
              } else {
                $scope.jobs[key] = {
                  url: url,
                  featureType: featureType,
                  status: 'not started',
                  mdUuid: producer.wfsHarvesterParam.metadataUuid,
                  cronScheduleExpression: producer.cronExpression,
                  cronScheduleProducerId: producer.id
                };
              }
            });

            angular.forEach($scope.jobs, function(job) {
              if (!job.mdUuid) return;
              if ($scope.mdCache[job.mdUuid]) {
                job.md = $scope.mdCache[job.mdUuid];
                return;
              }

              gnMetadataManager.getMdObjByUuid(job.mdUuid).then(function(md) {
                if (!md['geonet:info']) {
                  job.md = {
                    error: 'wfsIndexingMetadataNotFound'
                  };
                  return;
                }
                $scope.mdCache[job.mdUuid] = job.md = md;

                $element.find('div[data-md-uuid=' + job.mdUuid + ']').each(function() {
                  updateMdTitle(this);
                });
              })
            });

            $scope.jobsArray = Object.keys($scope.jobs).sort().map(function (key) {
              return $scope.jobs[key];
            });
            $scope.refreshBsTable($scope.jobsArray);
          } catch(e) {
            $scope.error = e.message;
          }
        }, function(result) {
          $scope.loading = false;
          $scope.error = result.data.error ?
            result.data.error.reason : 'Could not reach index';
        });
      };
      $scope.refreshJobList();

      $scope.refreshBsTable = function(jobsArray) {
        $scope.bsTableControl = {
          options: {
            data: jobsArray,
            sidePagination: 'client',
            pagination: true,
            paginationLoop: true,
            paginationHAlign: 'right',
            paginationVAlign: 'bottom',
            paginationDetailHAlign: 'left',
            paginationPreText: 'previous',
            paginationNextText: 'Next page',
            style: 'min-height:100',
            classes: 'table table-responsive full-width',
            sortName: 'endDate',
            sortOrder: 'desc',
            columns: [{
              field: 'mdUuid',
              title: $translate.instant('wfsIndexingMetadata'),
              formatter: function(value, row) {
                return row.mdUuid ?
                  '<div data-md-uuid="' + row.mdUuid + '">' +
                  '  <a class="md-title" style="display: none" href="catalog.search#/metadata/' + row.mdUuid + '">' +
                  '    <span>' + $translate.instant('recordWithNoTitle') + '</span>' +
                  '  </a>' +
                  '  <i class="fa fa-spinner fa-spin"/>' +
                  '</div>' +
                  '<code>' + row.mdUuid + '</code>' :
                  // '<a href="catalog.search#/metadata/' + row.mdUuid + '">' + row.mdUuid + '</a>' :
                  '<span class="text-muted">' + $translate.instant('noRecordFound') + '</span>';
              },
              sortable: true
            }, {
              field: 'url',
              title: $translate.instant('wfsurl'),
              formatter: function(value, row) {
                var wfsUrl = row.url; // TODO: transform to getcapabilities
                var label = $translate.instant('wfsIndexingFeatureType');
                return '<a href="' + row.url + '">' + row.url + '</a> - <a href="' + wfsUrl + '">GetCapabilities</a><br>' +
                  '<span>' + label + '</span>: <code>' + row.featureType + '<code>';
              },
              sortable: true
            }, {
              field: 'featureCount',
              title: $translate.instant('featureCount'),
              sortable: true
            }, {
              field: 'endDate',
              title: $translate.instant('wfsIndexingEndDate'),
              sortable: true,
              formatter: function(value, row) {
                return value ? moment(value).format('LLLL') : null;
              }
            }, {
              field: 'status',
              title: $translate.instant('status'),
              formatter: function(value, row) {
                return '<span class="label label-' + $scope.getLabelClass(row.status) + '" ' +
                  'title="' + (row.error || '') + '">' +
                  row.status + '</span>';
              },
              sortable: true
            }, {
              field: 'cronScheduleProducerId',
              title: $translate.instant('harvesterSchedule'),
              formatter: function(value, row) {
                var labelAdd = $translate.instant('wfsIndexingAddSchedule');
                var labelEdit = $translate.instant('wfsIndexingEditSchedule');
                var key = row.url + '#' + row.featureType;
                return row.cronScheduleProducerId ?
                  '<button data-job-key="' + key + '"' +
                  '        type="button" class="btn btn-sm btn-default btn-primary">' +
                  '  <span class="fa fa-clock-o"></span>' +
                  '  <span>' + labelEdit + '</span>' +
                  '</button>' :
                  '<button data-job-key="' + key + '"' +
                  '        type="button" class="btn btn-sm btn-default btn-success">' +
                  '  <span class="fa fa-clock-o"></span>' +
                  '  <span>' + labelAdd + '</span>' +
                  '</button>';
              },
              sortable: true
            }, {
              field: 'manual-run',
              title: '',
              formatter: function(value, row) {
                var label = $translate.instant('wfsIndexingTrigger');
                var key = row.url + '#' + row.featureType;
                return '<button data-trigger-job-key="' + key + '"' +
                  '        type="button" class="btn btn-sm btn-default btn-primary">' +
                  '  <span class="fa fa-play-circle"></span>' +
                  '  <span>' + label + '</span>' +
                  '</button>';
              }
            }],
            locale: getBsTableLang()
          }
        };
      }

      $scope.getLabelClass = function(status) {
        switch (status.toLowerCase()) {
          case 'success': return 'success';
          case 'error': return 'danger';
          default: return 'default';
        }
      };

      $scope.currentJob = null;
      $scope.settingsLoading = false;
      $scope.settingsError = null;
      $scope.settingsErrorIsDelete = false;

      var settingsModal = $('#gn-indexing-schedule');

      $scope.openScheduleSettings = function(key) {
        $scope.currentJob = key !== undefined ? angular.merge({}, $scope.jobs[key]) : { isNew: true };
        settingsModal.modal();
      };

      $scope.updateSchedule = function(job) {
        $scope.settingsLoading = true;
        var payload = {
          wfsHarvesterParam: {
            url: job.url,
            typeName: job.featureType,
            metadataUuid: job.mdUuid
          },
          cronExpression: job.cronScheduleExpression
        };

        var query = job.cronScheduleProducerId ?
          $http.put($scope.messageProducersApiUrl + '/' + job.cronScheduleProducerId, payload) :
          $http.post($scope.messageProducersApiUrl, payload);

        query.then(function(response) {
          var savedJob = response.data;
          $scope.settingsLoading = false;

          var key = savedJob.wfsHarvesterParam.url + '#' + savedJob.wfsHarvesterParam.typeName;
          angular.merge($scope.jobs[key], {
            url: savedJob.wfsHarvesterParam.url,
            featureType: savedJob.wfsHarvesterParam.typeName,
            status: 'not started',
            mdUuid: savedJob.wfsHarvesterParam.metadataUuid,
            cronScheduleExpression: savedJob.cronExpression,
            cronScheduleProducerId: savedJob.id
          });

          settingsModal.modal('hide');
        }, function(error) {
          $scope.settingsLoading = false;
          $scope.settingsErrorIsDelete = false;
          $scope.settingsError = error && error.data && error.data.message;
        });
      };

      $scope.deleteSchedule = function(job) {
        if (job.cronScheduleProducerId === null) { return }

        $scope.settingsLoading = true;

        $http.delete($scope.messageProducersApiUrl + '/' + job.cronScheduleProducerId)
          .then(function() {
            $scope.settingsLoading = false;

            var key = job.url + '#' + job.featureType;
            $scope.jobs[key].cronScheduleExpression = null;
            $scope.jobs[key].cronScheduleProducerId = null;

            settingsModal.modal('hide');
          }, function(error) {
            $scope.settingsLoading = false;
            $scope.settingsErrorIsDelete = true;
            $scope.settingsError = error && error.data && error.data.message;
          });
      };

      settingsModal.on('hidden.bs.modal', function() {
        $scope.currentJob = null;
        $scope.settingsLoading = false;
        $scope.settingsError = null;
      });

      $scope.triggerIndexing = function(key) {
        var job = $scope.jobs[key];
        $http.put($scope.wfsWorkersApiUrl + '/start', {
          typeName: job.featureType,
          url: job.url,
          version: "1.1.0",
          metadataUuid: job.mdUuid
        }).then(function() {
          gnAlertService.addAlert({
            msg: $translate.instant('wfsIndexingTriggerSuccess'),
            type: 'success'
          });
        }, function() {
          gnAlertService.addAlert({
            msg: $translate.instant('wfsIndexingTriggerError'),
            type: 'error'
          });
        });
      };
    }]);

})();
