(function() {
  goog.provide('gn_dashboard_controller');



  goog.require('gn_dashboard_content_stat_controller');
  goog.require('gn_dashboard_search_stat_controller');
  goog.require('gn_dashboard_status_controller');
  goog.require('gn_vcs_controller');

  var module = angular.module('gn_dashboard_controller',
      ['gn_dashboard_status_controller',
       'gn_dashboard_search_stat_controller',
       'gn_dashboard_content_stat_controller',
       'gn_vcs_controller']);


  /**
   *
   */
  module.controller('GnDashboardController', ['$scope', '$http',
    function($scope, $http) {


      $scope.pageMenu = {
        folder: 'dashboard/',
        defaultTab: 'status',
        tabs:
            [{
              type: 'status',
              label: 'status',
              icon: 'fa-dashboard',
              href: '#/dashboard/status'
            },{
              type: 'statistics-search',
              label: 'searchStatistics',
              icon: 'fa-search',
              href: '#/dashboard/statistics-search'
            },{
              type: 'statistics-content',
              label: 'contentStatistics',
              icon: 'fa-bar-chart',
              href: '#/dashboard/statistics-content'
            },{
              type: 'information',
              label: 'information',
              icon: 'fa-list-ul',
              href: '#/dashboard/information'
            },{
              type: 'versioning',
              label: 'versioning',
              icon: 'fa-rss',
              href: '#/dashboard/versioning'
            }]
      };

      $scope.info = {};

      $http.get($scope.url + 'xml.config.info@json').success(function(data) {
        $scope.info = data;
      }).error(function(data) {
        // TODO
      });

    }]);

})();
