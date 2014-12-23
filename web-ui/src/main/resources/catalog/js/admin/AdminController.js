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

  var module = angular.module('gn_admin_controller',
      ['gn_dashboard_controller', 'gn_usergroup_controller',
       'gn_admintools_controller', 'gn_settings_controller',
       'gn_adminmetadata_controller', 'gn_classification_controller',
       'gn_harvest_controller', 'gn_standards_controller',
       'gn_report_controller']);


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
    'gnUtilityService',
    function($scope, $http, $q, $rootScope, $route, $routeParams,
        gnUtilityService) {
      /**
       * Define admin console menu for each type of user
       */
      var userAdminMenu = [
        {name: 'harvesters', route: '#harvest',
          classes: 'btn-primary', icon: 'fa-cloud-download'},
        {name: 'statisticsAndStatus', route: '#dashboard',
          classes: 'btn-success', icon: 'fa-dashboard'},
        {name: 'reports', route: '#reports',
          classes: 'btn-success', icon: 'fa-file-text-o'},
        {name: 'usersAndGroups', route: '#organization',
          classes: 'btn-default', icon: 'fa-group'}

      ];
      $scope.menu = {
        UserAdmin: userAdminMenu,
        Administrator: [
          // TODO : create gn classes
          {name: 'metadatasAndTemplates', route: '#metadata',
            classes: 'btn-primary', icon: 'fa-archive'},
          {name: 'io',
            // Metadata import is made in the widget apps
            url: 'catalog.edit?debug#/import',
            classes: 'btn-primary',
            icon: 'fa-upload'},
          {name: 'harvesters', route: '#harvest', //url: 'harvesting',
            classes: 'btn-primary', icon: 'fa-cloud-download'},
          {name: 'statisticsAndStatus', route: '#dashboard',
            classes: 'btn-success', icon: 'fa-dashboard'},
          {name: 'reports', route: '#reports',
            classes: 'btn-success', icon: 'fa-file-text-o'},
          {name: 'classificationSystems', route: '#classification',
            classes: 'btn-info', icon: 'fa-tags'},
          {name: 'standards', route: '#standards',
            classes: 'btn-info', icon: 'fa-puzzle-piece'},
          {name: 'usersAndGroups', route: '#organization',
            classes: 'btn-default', icon: 'fa-group'},
          {name: 'settings', route: '#settings',
            classes: 'btn-warning', icon: 'fa-gear'},
          {name: 'tools', route: '#tools',
            classes: 'btn-warning', icon: 'fa-medkit'}]
        // TODO : add other role menu
      };

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
