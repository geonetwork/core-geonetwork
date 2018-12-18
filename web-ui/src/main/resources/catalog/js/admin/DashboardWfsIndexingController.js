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

  var INDEX_URL = '';

  module.controller('GnDashboardWfsIndexingController', [
    '$scope', '$location', '$http', 'gnMetadataManager', 'gnHttp',
    function($scope, $location, $http, gnMetadataManager, gnHttp) {
      $scope.url = decodeURIComponent($location.search()['wfs-indexing']);

      // URL of the index service endpoint
      $scope.indexUrl = gnHttp.getService('featureindexproxy') + '?_=_search';

      // list of wfs indexing jobs received from the index
      // null means loading
      $scope.jobs = null;

      // error on request or results parsing
      $scope.error = null;

      // true if request pending
      $scope.loading = false;

      $scope.refreshJobList = function() {
        $scope.jobs = null;
        $scope.error = null;
        $scope.loading = true;
        $http.post($scope.indexUrl, {
          'query': {
            'query_string': {
              'query': 'docType:harvesterReport'
            }
          }
        }).then(function(result) {
          $scope.loading = false;
          try {
            $scope.jobs = result.data.hits.hits.map(function (hit) {
              var source = hit._source;
              var infos = decodeURIComponent(source.id).split('#');
              return {
                url: infos[0],
                featureType: infos[1],
                featureCount: source.totalRecords_i || 0,
                status: source.endDate_dt === undefined ? 'ongoing' : source.status_s,
                mdUuid: source.parent,
                error: source.error_ss
              };
            });

            $scope.jobs.forEach(function(job) {
              gnMetadataManager.getMdObjByUuid(job.mdUuid).then(function(md) {
                if (!md['geonet:info']) {
                  job.md = {
                    error: 'wfsIndexingMetadataNotFound'
                  }
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
    }]);

})();
