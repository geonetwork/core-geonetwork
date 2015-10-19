(function() {
  goog.provide('gn_openwis_check_subscription_button');

  var module = angular.module('gn_openwis_check_subscription_button', []);

  module.controller('gnOpenWISCheckSubscriptionButton', [
      '$scope',
      '$http',
      function($scope, $http) {

        $scope.isVisible = false;
        $scope.exists = false;
        $scope.isAvailable = false;

        $scope.$watch('md', function(md) {

          if (!md) {
            $scope.isVisible = false;
            $scope.exists = false;
            $scope.isAvailable = false;
          } else {
            $http.get(
                'openwis.productmetadata.get?urn=' + md['geonet:info'].uuid)
                .success(function(data) {
                  $scope.exists = true;
                }).error(function(data) {
                  $scope.exists = false;
                });
            $http.get(
                'openwis.productmetadata.get?urn=' + md['geonet:info'].uuid)
                .success(function(data) {
                  $scope.isVisible = true;
                }).error(function(data) {
                  $scope.isVisible = false;
                });
            $http.get(
                'openwis.productmetadata.get?urn=' + md['geonet:info'].uuid)
                .success(function(data) {
                  $scope.isAvailable = true;
                }).error(function(data) {
                  $scope.isAvailable = false;
                });
          }
        });
      }
  ]);

})();
