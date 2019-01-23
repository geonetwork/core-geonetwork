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
  goog.provide('gn_sources_controller');


  var module = angular.module('gn_sources_controller',
      []);

  module.controller('GnSourcesController', [
    '$scope', '$http', '$rootScope', '$translate',
    function($scope, $http, $rootScope, $translate) {
      $scope.sources = [];
      $scope.source = null;
      $scope.selectSource = function(source) {
        $scope.source = source;
      };

      function loadSources() {
        $http.get('../api/sources')
            .success(function(data) {
              $scope.sources = data;
            });
      }

      $scope.addSubPortal = function() {
        $scope.source = {
          type: 'subportal',
          name: '',
          logo: '',
          uiConfig: '',
          filter: ''
        };
        // TODO: init labels
      };

      $scope.updateSource = function() {
        var url = '../api/sources/' + (
          angular.isDefined($scope.source.uuid) ?
            $scope.source.uuid : '');
        $http.put(url,
                  $scope.source)
            .success(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate.instant('sourceUpdated'),
                timeout: 2,
                type: 'success'});

              loadSources();
            })
            .error(function(data) {
                  $rootScope.$broadcast('StatusUpdated', {
                    title: $translate.instant('sourceUpdateError'),
                    error: data,
                    timeout: 0,
                    type: 'danger'});
                });
      };

      loadSources();

    }]);
})();
