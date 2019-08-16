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
      []);

  module.controller('GnDashboardWfsIndexingController', [
    '$q', '$scope', '$location', '$http', 'gnMetadataManager', 'gnHttp',
    function($q, $scope, $location, $http, gnMetadataManager, gnHttp) {
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

      // dictionary of wfs indexing jobs received from the index
      // key is url#typename
      // null means loading
      $scope.jobs = {};

      // error on request or results parsing
      $scope.error = null;

      // true if request pending
      $scope.loading = false;

      $scope.refreshJobList = function() {
        $scope.jobs = {};
        $scope.error = null;
        $scope.loading = true;

        var indexQuery = $http.post($scope.indexUrl, {
          'query': {
            'query_string': {
              'query': 'docType:harvesterReport'
            }
          }
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
                status: source.endDate_dt === undefined ? 'ongoing' : source.status_s,
                mdUuid: source.parent,
                error: source.error_ss,
                endDate: source.endDate_dt ? moment(source.endDate_dt).format('LLLL') : null,
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
              gnMetadataManager.getMdObjByUuid(job.mdUuid).then(function(md) {
                if (!md['geonet:info']) {
                  job.md = {
                    error: 'wfsIndexingMetadataNotFound'
                  };
                  return;
                }
                job.md = md;
              })
            });
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

      var settingsModal = $('#gn-indexing-schedule');

      $scope.openScheduleSettings = function(job) {
        $scope.currentJob = angular.merge({}, job);
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
          $scope.jobs[key] = angular.merge({}, $scope.jobs[key], {
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
          $scope.settingsError = error;
        });
      };

      $scope.deleteSchedule = function(job) {
        if (job.cronScheduleProducerId === null) { return }

        job.deleteLoading = true;

        $http.delete($scope.messageProducersApiUrl + '/' + job.cronScheduleProducerId)
          .then(function() {
            job.deleteLoading = false;

            var key = job.url + '#' + job.featureType;
            $scope.jobs[key].cronScheduleExpression = null;
            $scope.jobs[key].cronScheduleProducerId = null;

            settingsModal.modal('hide');
          }, function(error) {
            job.deleteLoading = false;
          });
      };

      settingsModal.on('hidden.bs.modal', function() {
        $scope.currentJob = null;
        $scope.settingsLoading = false;
        $scope.settingsError = null;
      });
    }]);

})();
