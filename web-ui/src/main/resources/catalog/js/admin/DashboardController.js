(function() {
  goog.provide('gn_dashboard_controller');



  goog.require('gn_dashboard_content_stat_controller');
  goog.require('gn_dashboard_search_stat_controller');
  goog.require('gn_dashboard_status_controller');

  var module = angular.module('gn_dashboard_controller',
      ['gn_dashboard_status_controller',
       'gn_dashboard_search_stat_controller',
       'gn_dashboard_content_stat_controller']);


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
              icon: 'icon-dashboard',
              href: '#/dashboard/status'
            },{
              type: 'statistics-search',
              label: 'searchStatistics',
              icon: 'icon-search',
              href: '#/dashboard/statistics-search'
            },{
              type: 'statistics-content',
              label: 'contentStatistics',
              icon: 'icon-bar-chart',
              href: '#/dashboard/statistics-content'
            },{
              type: 'information',
              label: 'information',
              icon: 'icon-list-ul',
              href: '#/dashboard/information'
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
