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
  module.controller('GnDashboardController', ['$scope', '$routeParams', '$http',
    function($scope, $routeParams, $http) {
      var templateFolder = '../../catalog/templates/admin/dashboard/';
      var availableTemplates = [
        'information', 'statistics-search', 'statistics-content', 'status'
      ];

      $scope.info = {};

      $http.get($scope.url + 'xml.config.info@json').success(function(data) {
        $scope.info = data;
      }).error(function(data) {
        // TODO
      });

      $scope.defaultDashType = 'status';

      $scope.getTemplate = function() {
        $scope.type = $scope.defaultDashType;
        if (availableTemplates.indexOf($routeParams.dashboardType) > -1) {
          $scope.type = $routeParams.dashboardType;
        }
        return templateFolder + $scope.type + '.html';
      };
    }]);

})();
