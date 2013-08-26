(function() {
  goog.provide('gn_settings_controller');




  goog.require('gn_csw_settings_controller');
  goog.require('gn_logo_settings_controller');
  goog.require('gn_system_settings_controller');

  var module = angular.module('gn_settings_controller',
      ['gn_system_settings_controller',
       'gn_csw_settings_controller',
       'gn_logo_settings_controller']);


  /**
   *
   */
  module.controller('GnSettingsController', ['$scope', '$routeParams', '$http',
    function($scope, $routeParams, $http) {
      var templateFolder = '../../catalog/templates/admin/settings/';
      var availableTemplates = [
        'system', 'logo', 'csw', 'csw-virtual'
      ];

      $scope.defaultSettingType = 'system';

      $scope.getTemplate = function() {
        $scope.type = $scope.defaultSettingType;
        if (availableTemplates.indexOf($routeParams.settingType) > -1) {
          $scope.type = $routeParams.settingType;
        }
        return templateFolder + $scope.type + '.html';
      };
    }]);

})();
