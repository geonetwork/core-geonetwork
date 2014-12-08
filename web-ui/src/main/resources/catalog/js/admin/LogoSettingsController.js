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
