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
  goog.provide('gn_logo_settings_controller');


  var module = angular.module('gn_logo_settings_controller',
      ['blueimp.fileupload']);


  /**
   * GnLogoSettingsController provides management interface
   * for catalog logo and harvester logo.
   *
   */
  module.controller('GnLogoSettingsController', [
    '$scope', '$http', '$rootScope', '$translate',
    function($scope, $http, $rootScope, $translate) {
      /**
         * The list of catalog logos
         */
      $scope.logos = [];

      /**
       * Load list of logos
       */
      loadLogo = function() {
        $scope.logos = [];
        $http.get('admin.logo.list?_content_type=json&type=icons').
            success(function(data) {
              $scope.logos = data[0];
            }).error(function(data) {
              // TODO
            });
      };

      /**
       * Callback when error uploading file.
       */
      loadLogoError = function(e, data) {
        $rootScope.$broadcast('StatusUpdated', {
          title: $translate('logoUploadError'),
          error: data.jqXHR.responseJSON,
          timeout: 0,
          type: 'danger'});
      };

      /**
       * Configure logo uploader
       */
      $scope.logoUploadOptions = {
        autoUpload: true,
        done: loadLogo,
        fail: loadLogoError
      };


      /**
       * Set the catalog logo and optionnaly the favicon
       * if favicon parameter is set to true.
       */
      $scope.setCatalogLogo = function(logoName, favicon) {
        var setFavicon = favicon ? '1' : '0';

        $http.get('admin.logo.update?fname=' + logoName +
            '&favicon=' + setFavicon)
            .success(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate('logoUpdated'),
                timeout: 2,
                type: 'success'});
              $rootScope.$broadcast('loadCatalogInfo');
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('logoUpdateError'),
                error: data,
                timeout: 0,
                type: 'danger'});
              loadLogo();
            });
      };

      /**
       * Remove the logo and refresh the list when done.
       */
      $scope.removeLogo = function(logoName) {
        $http.get('admin.logo.remove?fname=' + logoName)
            .success(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                msg: $translate('logoRemoved'),
                timeout: 2,
                type: 'success'});
              loadLogo();
            })
            .error(function(data) {
              $rootScope.$broadcast('StatusUpdated', {
                title: $translate('logoRemoveError'),
                error: data,
                timeout: 0,
                type: 'danger'});
              loadLogo();
            });
      };

      loadLogo();
    }]);

})();
