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
  goog.provide('gn_standards_controller');


  var module = angular.module('gn_standards_controller',
      []);


  /**
   * GnStandardsController provides administration tools
   * for standards.
   *
   * TODO: More testing required on add/update action
   *
   */
  module.controller('GnStandardsController', [
    '$scope', '$routeParams', '$http', '$rootScope', '$translate', '$compile',
    'gnSearchManagerService',
    'gnUtilityService',
    function($scope, $routeParams, $http, $rootScope, $translate, $compile,
            gnSearchManagerService, 
            gnUtilityService) {

      $scope.pageMenu = {
        folder: 'standards/',
        defaultTab: 'standards',
        tabs: []
      };

      $scope.schemas = [];

      function loadSchemas() {
        $http.get('admin.schema.list@json').success(function(data) {
          for (var i = 0; i < data.length; i++) {
            $scope.schemas.push({id: data[i]['#text'].trim(), props: data[i]});
          }
          $scope.schemas.sort();
        });
      }

      $scope.addStandard = function(formId, action) {
        $http.get('admin.schema.' + action + '?' + $(formId).serialize())
            .success(function(data) {
              loadSchemas();
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate('standardAdded'),
                timeout: 2,
                type: 'success'});
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('standardAddError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      $scope.removeStandard = function(s) {
        $http.get('admin.schema.remove@json?schema=' + s)
            .success(function(data) {
              if (data['@status'] === 'error') {
                $rootScope.$broadcast('StatusUpdated', {
                  title: $translate('standardsDeleteError'),
                  msg: data['@message'],
                  timeout: 0,
                  type: 'danger'});
              } else {
                loadSchemas();
              }
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('standardsDeleteError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

      loadSchemas();

    }]);

})();
