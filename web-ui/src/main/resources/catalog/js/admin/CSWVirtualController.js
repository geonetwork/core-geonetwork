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
  goog.provide('gn_csw_virtual_controller');


  var module = angular.module('gn_csw_virtual_controller',
      []);


  /**
   * GnCSWVirtualController provides management interface
   * for virtual CSW configuration.
   *
   */
  module.controller('GnCSWVirtualController', [
    '$scope', '$http', '$rootScope', '$translate', '$timeout',
    function($scope, $http, $rootScope, $translate, $timeout) {

      /**
       * CSW virtual
       */
      $scope.cswVirtual = null;
      $scope.virtualCSWSelected = null;
      $scope.virtualCSWUpdated = false;
      $scope.virtualCSWSearch = '';
      $scope.groupsFilter = {};
      $scope.sourcesFilter = {};
      $scope.categoriesFilter = {};
      $scope.newFilter = {
        name: '_groupPublished',
        value: null,
        occur: '+'
      };
      $scope.occurs = ['+', ' ', '-'];
      $scope.showExplicitQuery = false;
      $scope.filterHelper = ['any', 'title', 'abstract', 'keyword',
        'denominator', '_source', '_cat', '_groupPublished'];

      var operation = '';

      /**
       * Load catalog settings and extract CSW settings
       */
      function loadCSWVirtual() {
        $scope.virtualCSWSelected = {};
        $http.get('../api/csw/virtuals').
            success(function(data) {
              $scope.cswVirtual = data;
            });
      }


      function loadFilterList() {
        $http.get('../api/groups').
            success(function(data) {
              $scope.groupsFilter = data;
            });
      }
      function loadCategories() {
        $http.get('../api/tags').
            success(function(data) {
              $scope.categories = data;
            });
      }

      $scope.selectVirtualCSW = function(v) {
        operation = 'updateservice';
        $http.get('../api/csw/virtuals/' + v.id)
            .success(function(data) {
              var params = [], formParams = ['abstract', 'title',
                '_source', '_cat', 'any', '_groupPublished', 'keyword',
                'denominator', 'type'];
              angular.copy(data.parameter, params);
              $scope.virtualCSWSelected = data;
              // $scope.virtualCSWSelected.serviceParameters = {};
              $scope.showExplicitQuery =
                  $scope.virtualCSWSelected.explicitQuery ? true : false;
              // angular.forEach(params,
              //     function(param) {
              //       $scope.virtualCSWSelected.
              //           serviceParameters[param.name] = {
              //             value: param.value,
              //             occur: param.occur};
              //     });
              $scope.virtualCSWUpdated = false;

              $timeout(function() {
                $('#servicename').focus();
              }, 100);
            }).error(function(data) {
              // TODO
            });
      };

      $scope.addFilter = function() {
        $scope.virtualCSWSelected.parameters.push(
            angular.copy($scope.newFilter)
        );
      };
      $scope.removeFilter = function(f) {
        angular.forEach($scope.virtualCSWSelected.parameters,
            function(idx, o) {
              if (o.name === f) {
                $scope.virtualCSWSelected.parameters.splice(idx, 1);
              }
            });
      };

      $scope.$watchCollection('virtualCSWSelected', function() {
        $scope.virtualCSWUpdated = true;
      });

      $scope.addVirtualCSW = function() {
        operation = 'newservice';
        $scope.virtualCSWSelected = {
          'id': '',
          'name': 'csw-servicename',
          'description': '',
          'className': '.services.main.CswDiscoveryDispatcher',
          'explicitQuery': '',
          'parameters': []
        };
        $timeout(function() {
          $('#servicename').focus();
        }, 100);
      };
      $scope.saveVirtualCSW = function() {

        $http.put('../api/csw/virtuals' + (
            $scope.virtualCSWSelected.id !== '' ?
            '/' + $scope.virtualCSWSelected.id : ''
            ), $scope.virtualCSWSelected)
            .then(function(r) {
              if (r.status === 400) {
                $rootScope.$broadcast('StatusUpdated', {
                  title: $translate.instant('virtualCswUpdateError'),
                  error: r.data,
                  timeout: 0,
                  type: 'danger'});
              } else {
                loadCSWVirtual();
                $rootScope.$broadcast('StatusUpdated', {
                  msg: $translate.instant('virtualCswUpdated'),
                  timeout: 2,
                  type: 'success'});
              }
            }, function(r) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant('virtualCswUpdateError'),
                error: r.data,
                timeout: 0,
                type: 'danger'});
            });
      };

      $scope.deleteVirtualCSW = function() {
        $http.delete('../api/csw/virtuals/' +
            $scope.virtualCSWSelected.id)
            .then(function(data) {
              loadCSWVirtual();
            }, function(response) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant('virtualCswDeleteError'),
                error: response.data,
                timeout: 0,
                type: 'danger'});
            });
      };

      $scope.sortByLabel = function(group) {
        return group.label[$scope.lang];
      };
      $scope.getCapabilitiesUrl = function(v) {
        if (v) {
          return v.name + '?SERVICE=CSW&REQUEST=GetCapabilities';
        } else {
          return null;
        }
      };

      loadCSWVirtual();
      loadFilterList();
      loadCategories();

    }]);

})();
