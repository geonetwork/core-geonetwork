(function() {
  goog.provide('gn_admin_controller');













  goog.require('gn_adminmetadata_controller');
  goog.require('gn_admintools_controller');
  goog.require('gn_cat_controller');
  goog.require('gn_classification_controller');
  goog.require('gn_dashboard_controller');
  goog.require('gn_harvest_controller');
  goog.require('gn_report_controller');
  goog.require('gn_settings_controller');
  goog.require('gn_standards_controller');
  goog.require('gn_usergroup_controller');
  goog.require('gn_admin_menu');

  var module = angular.module('gn_admin_controller',
      ['gn_dashboard_controller', 'gn_usergroup_controller',
       'gn_admintools_controller', 'gn_settings_controller',
       'gn_adminmetadata_controller', 'gn_classification_controller',
       'gn_harvest_controller', 'gn_standards_controller',
       'gn_report_controller','gn_admin_menu']);


  var tplFolder = '../../catalog/templates/admin/';

  module.config(['$routeProvider', function($routeProvider) {
    $routeProvider.
        when('/metadata', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnAdminMetadataController'}).
        when('/metadata/:tab', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnAdminMetadataController'}).
        when('/metadata/schematron/:schemaName', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnAdminMetadataController'}).
        when('/metadata/schematron/:schemaName/:schematronId', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnAdminMetadataController'}).
        when('/metadata/:tab/:metadataAction/:schema', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnAdminMetadataController'}).
        when('/dashboard', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnDashboardController'}).
        when('/dashboard/:tab', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnDashboardController'}).
        when('/organization', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnUserGroupController'}).
        when('/organization/:tab', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnUserGroupController'}).
        when('/organization/:tab/:userOrGroup', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnUserGroupController'}).
        when('/classification', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnClassificationController'}).
        when('/classification/:tab', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnClassificationController'}).
        when('/tools', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnAdminToolsController'}).
        when('/tools/:tab', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnAdminToolsController'}).
        when('/tools/:tab/select/:selectAll/process/:processId', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnAdminToolsController'}).
        when('/harvest', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnHarvestController'}).
        when('/harvest/:tab', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnHarvestController'}).
        when('/settings', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnSettingsController'}).
        when('/settings/:tab', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnSettingsController'}).
        when('/standards', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnStandardsController'}).
        when('/reports', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnReportController'}).
        when('/reports/:tab', {
          templateUrl: tplFolder + 'page-layout.html',
          controller: 'GnReportController'}).
        otherwise({templateUrl: tplFolder + 'admin.html'});
  }]);

  /**
   * The admin console controller.
   *
   * Example:
   *
   *     <body ng-controller="GnAdminController">
   */
  module.controller('GnAdminController', [
    '$scope', '$http', '$q', '$rootScope', '$route', '$routeParams',
    'gnUtilityService', 'gnAdminMenu',
    function($scope, $http, $q, $rootScope, $route, $routeParams,
        gnUtilityService,gnAdminMenu) {
      $scope.menu = gnAdminMenu;
      /**
       * Define menu position on the left (nav-stacked)
       * or on top of the page.
       */
      $scope.navStacked = true;

      $scope.getTpl = function(pageMenu) {
        $scope.type = pageMenu.defaultTab;
        $.each(pageMenu.tabs, function(index, value) {
          if (value.type === $routeParams.tab) {
            $scope.type = $routeParams.tab;
          }
        });
        return tplFolder + pageMenu.folder + $scope.type + '.html';
      };

      $scope.csvExport = function(json, e) {
        $(e.target).next('pre.gn-csv-export').remove();
        $(e.target).after("<pre class='gn-csv-export'>" +
                gnUtilityService.toCsv(json) + '</pre>');
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
