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
  goog.provide('gn_csw_test_controller');


  var module = angular.module('gn_csw_test_controller',
      []);


  /**
   * GnCSWTestController provides simple testing
   * of CSW service
   *
   */
  module.controller('GnCSWTestController', [
    '$scope', '$http',
    function($scope, $http) {

      /**
       * CSW tests
       */
      $scope.cswTests = {};
      $scope.currentTestId = null;
      $scope.currentTest = null;
      $scope.currentTestResponse = null;
      $scope.cswUrl = 'csw';
      $scope.cswVirtual = null;

      function loadCSWTest() {
        $http.get('../../xml/csw/test/csw-tests.json').success(function(data) {
          $scope.cswTests = data;
        });

        $http.get('../api/csw/virtuals').
            success(function(data) {
              $scope.cswVirtual = data;
            });
      }

      $scope.setCswUrl = function(url) {
        $scope.cswUrl = url;
      };

      $scope.$watch('currentTestId', function() {
        if ($scope.currentTestId !== null) {
          $http.get('../../xml/csw/test/' + $scope.currentTestId + '.xml', {headers: {
              Accept: 'application/xml'
            }})
              .success(function(data) {
                $scope.currentTest = data;
                $scope.runCSWRequest();
              });
        }
      });

      $scope.runCSWRequest = function() {
        $scope.currentTestResponse = '';
        $http.post($scope.cswUrl, $scope.currentTest, {
          headers: {'Content-type': 'application/xml'}
        }).success(function(data) {
          $scope.currentTestResponse = data;
        });
      };

      loadCSWTest();

    }]);

})();
