(function() {
  goog.provide('gn_dashboard_status_controller');

  goog.require('gn_gauge');

  var module = angular.module('gn_dashboard_status_controller',
      ['gn_gauge']);


  /**
   *
   */
  module.controller('GnDashboardStatusController', [
    '$scope', '$routeParams', '$http',
    function($scope, $routeParams, $http) {
      $scope.healthy = undefined;
      $scope.gauges = [];
      $scope.hits = 20;

      $http.get('../../criticalhealthcheck').success(function(data) {
        $scope.healthy = true;
        $scope.healthcheck = data;
      }).error(function(data) {
        $scope.healthy = false;
        $scope.healthcheck = data;
      });


      $http.get(
          'qi@json?fast=index&sortBy=changeDate&' +
              '_indexingError=1&from=1&to=' +
              $scope.hits).success(function(data) {
        // TODO : mutualize search results formatting
        if (data.metadata instanceof Array) {
          $scope.mdWithIndexingError = data.metadata;
        } else {
          $scope.mdWithIndexingError = [data.metadata];
        }
      }).error(function(data) {
        // TODO
      });
    }]);

})();
