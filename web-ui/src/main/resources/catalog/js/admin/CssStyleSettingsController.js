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
  goog.provide('gn_cssstyle_settings_controller');

  var module = angular.module('gn_cssstyle_settings_controller',
      ['color.picker']);


  /**
   * GnCssStyleSettingsController provides management interface
   * for change the style in the catalog.
   *
   */
  module.controller('GnCssStyleSettingsController', [
    '$scope', '$http', '$rootScope', '$translate', '$window',
    function($scope, $http, $rootScope, $translate, $window) {

      $http({
        method: 'GET',
        url: '../api/customstyle',
        headers: {'Content-Type': 'text/plain'}
      }).then(function success(response) {
        $scope.gnCssStyle = response.data;
      },
      function error(response) {});

      $scope.saveCssStyleSettings = function(formId) {

    	  $http.post('../api/customstyle',
    			  formId)
    			  .success(function(data) {
    				  $http({
    					  method: 'GET',
    					  url: '../../static/wroAPI/reloadModel',
    					  headers: {'Content-Type': 'text/plain'}
    				  }).then(function(data) {
    					  $http({
    						  method: 'GET',
    						  url: '../../static/wroAPI/reloadCache',
    						  headers: {'Content-Type': 'text/plain'}
    					  }).then(function(data) {
    						  $window.location.reload(); });
    				  });
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate.instant('settingsUpdateError'),
                error: data,
                timeout: 0,
                type: 'danger'});
            });
      };

    }]);

})();
