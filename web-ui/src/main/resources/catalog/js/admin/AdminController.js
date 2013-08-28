(function() {
  goog.provide('gn_admin_controller');













  goog.require('gn_adminmetadata_controller');
  goog.require('gn_admintools_controller');
  goog.require('gn_cat_controller');
  goog.require('gn_classification_controller');
  goog.require('gn_dashboard_controller');
  goog.require('gn_settings_controller');
  goog.require('gn_translation');
  goog.require('gn_translation_controller');
  goog.require('gn_usergroup_controller');


  var module = angular.module('gn_admin_controller',
      ['gn_dashboard_controller', 'gn_usergroup_controller',
       'gn_admintools_controller', 'gn_settings_controller',
       'gn_adminmetadata_controller', 'gn_classification_controller']);


  module.config(['$routeProvider', function($routeProvider) {
    $routeProvider.
        when('/metadata', {
          templateUrl: '../../catalog/templates/admin/metadata.html',
          controller: 'GnAdminMetadataController'}).
        when('/metadata/:metadataTab', {
          templateUrl: '../../catalog/templates/admin/metadata.html',
          controller: 'GnAdminMetadataController'}).
        when('/metadata/metadata-and-template/:metadataAction/:schema', {
          templateUrl: '../../catalog/templates/admin/metadata.html',
          controller: 'GnAdminMetadataController'}).
        when('/dashboard', {
          templateUrl: '../../catalog/templates/admin/dashboard.html',
          controller: 'GnDashboardController'}).
        when('/dashboard/:dashboardType', {
          templateUrl: '../../catalog/templates/admin/dashboard.html',
          controller: 'GnDashboardController'}).
        when('/organization', {
          templateUrl: '../../catalog/templates/admin/organization.html',
          controller: 'GnUserGroupController'}).
        when('/organization/:userGroupTab', {
          templateUrl: '../../catalog/templates/admin/organization.html',
          controller: 'GnUserGroupController'}).
        when('/organization/groups/:groupId', {
          templateUrl: '../../catalog/templates/admin/organization.html',
          controller: 'GnUserGroupController'}).
        when('/classification', {
          templateUrl: '../../catalog/templates/admin/classification.html',
          controller: 'GnClassificationController'}).
        when('/classification/:tab', {
          templateUrl: '../../catalog/templates/admin/classification.html',
          controller: 'GnClassificationController'}).
        when('/tools', {
          templateUrl: '../../catalog/templates/admin/tools.html',
          controller: 'GnAdminToolsController'}).
        when('/tools/:toolTab', {
          templateUrl: '../../catalog/templates/admin/tools.html',
          controller: 'GnAdminToolsController'}).
        when('/settings', {
          templateUrl: '../../catalog/templates/admin/settings.html',
          controller: 'GnSettingsController'}).
        when('/settings/:settingType', {
          templateUrl: '../../catalog/templates/admin/settings.html',
          controller: 'GnSettingsController'}).
        otherwise({templateUrl: '../../catalog/templates/admin/admin.html'});
  }]);

  /**
   * The admin console controller.
   *
   * Example:
   *
   *     <body ng-controller="GnAdminController">
   */
  module.controller('GnAdminController', [
    '$scope', '$http', '$q', '$rootScope', '$route',
    function($scope, $http, $q, $rootScope, $route) {
      /**
       * Define admin console menu for each type of user
       */
      $scope.menu = {
        Administrator: [
          // TODO : create gn classes
          {name: 'metadatasAndTemplates', route: '#metadata',
            classes: 'btn-primary', icon: 'icon-archive'},
          {name: 'io', url: 'import', classes: 'btn-primary',
            icon: 'icon-upload'},
          {name: 'harvesters', url: 'harvesting',
            classes: 'btn-primary', icon: 'icon-cloud-upload'},
          {name: 'statisticsAndStatus', route: '#dashboard',
            classes: 'btn-success', icon: 'icon-dashboard'},
          {name: 'classificationSystems', route: '#classification',
            classes: 'btn-info', icon: 'icon-tags'},
          {name: 'standards', url: 'admin',
            classes: 'btn-info', icon: 'icon-puzzle-piece'},
          {name: 'usersAndGroups', route: '#organization',
            classes: 'btn-default', icon: 'icon-group'},
          {name: 'settings', route: '#settings',
            classes: 'btn-warning', icon: 'icon-gear'},
          {name: 'tools', route: '#tools',
            classes: 'btn-warning', icon: 'icon-medkit'}]
        // TODO : add other role menu
      };

      $scope.convertToCSV = function(objArray) {
        if (objArray === undefined) return;

        var array = (typeof objArray != 'object' ?
            JSON.parse(objArray) : objArray);
        // TODO : improve CSV conversion when nested objects
        var str = '';
        for (var i = 0; i < array.length; i++) {
          var line = '';
          for (var index in array[i]) {
            if (line != '') {
              line += ',';
            }
            line += array[i][index];
          }
          str += line + '\r\n';
        }
        return str;
      };

      $scope.csvExport = function(json, e) {
        $(e.target).next('pre.gn-csv-export').remove();
        $(e.target).after("<pre class='gn-csv-export'>" +
                $scope.convertToCSV(json) + '</pre>');
      };

      /**
       * Return menu Angular route or GeoNetwork legacy URLs
       * according to $scope.menu configuration.
       */
      $scope.getMenuUrl = function(menu) {
        if (menu.route) {
          return menu.route;
        } else if (menu.url) {
          return $scope.url + menu.url;
        }
      };

      /**
       * Return menu according to user profile
       */
      $scope.getMenu = function() {
        return $scope.menu[$scope.user.profile];
      };
    }]);

})();
