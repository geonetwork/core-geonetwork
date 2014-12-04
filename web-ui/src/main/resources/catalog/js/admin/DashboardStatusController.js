(function() {
  goog.provide('gn_dashboard_status_controller');


  var module = angular.module('gn_dashboard_status_controller',
      []);


  /**
   *
   */
  module.controller('GnDashboardStatusController', [
    '$scope', '$routeParams', '$http', 'gnSearchManagerService',
    function($scope, $routeParams, $http, gnSearchManagerService) {
      $scope.healthy = undefined;

      $http.get('../../criticalhealthcheck').success(function(data) {
        $scope.healthy = true;
        $scope.healthcheck = data;
      }).error(function(data) {
        $scope.healthy = false;
        $scope.healthcheck = data;
      });

      $scope.searchObj = {
        params: {
          _indexingError: 1,
          sortBy: 'changeDate'
        }
      };
    }]);

})();
