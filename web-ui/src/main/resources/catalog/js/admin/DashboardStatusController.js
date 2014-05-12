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
      $scope.hits = 200;
      $scope.mdWithIndexingError = null;

      $http.get('../../criticalhealthcheck').success(function(data) {
        $scope.healthy = true;
        $scope.healthcheck = data;
      }).error(function(data) {
        $scope.healthy = false;
        $scope.healthcheck = data;
      });

      gnSearchManagerService.gnSearch({
        _indexingError: 1,
        sortBy: 'changeDate',
        fast: 'index',
        from: 1,
        to: $scope.hits
      }).then(function(response) {
        $scope.mdWithIndexingError = response;
      });
    }]);

})();
