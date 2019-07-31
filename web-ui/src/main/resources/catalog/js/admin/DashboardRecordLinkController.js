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
  goog.provide('gn_dashboard_record_link_controller');


  var module = angular.module('gn_dashboard_record_link_controller',
      []);

  /**
   *
   */
  module.controller('GnDashboardRecordLinksController', [
    '$scope', '$routeParams', '$http', '$rootScope', '$translate',
    function($scope, $routeParams, $http, $rootScope, $translate) {
      $scope.links = [];
      $scope.loading = true;
      $scope.from = 0;
      $scope.size = 200;
      $scope.sizeDefault = $scope.size;

      $scope.loadLinks = function(more) {
        $scope.size = more ? $scope.size + $scope.sizeDefault : $scope.sizeDefault;

        $scope.loading = true;
        $http.get('../api/records/links?from=' + $scope.from + '&size=' + $scope.size).then(function(r) {
          $scope.links = r.data;
          $scope.loading = false;
        }, function(r) {
          $scope.error = r.data;
          $scope.loading = false;
        })
      };
      $scope.more = function() {
        $scope.loadLinks(true);
      };

      $scope.analyzeLinks = function() {
        $scope.loading = true;
        $http.post('../api/records/links?analyze=true').then(function(r) {
          $scope.loadLinks();
          $scope.loading = false;
        }, function(r) {
          $scope.loadLinks();
          $scope.loading = false;
        })
      };

      $scope.removeAll = function() {
        $scope.loading = true;
        $http.delete('../api/records/links').then($scope.loadLinks, function(r) {
          $scope.error = r.data;
          $scope.loadLinks();
          $scope.loading = false;
        })
      };

      $scope.loadLinks();
    }]);

})();
