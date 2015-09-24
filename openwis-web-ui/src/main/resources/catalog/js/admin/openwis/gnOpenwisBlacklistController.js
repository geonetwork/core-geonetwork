(function() {
  goog.provide('gn_openwis_blacklist_controller');

  var module = angular.module('gn_openwis_blacklist_controller', []);

  module.controller('GnOpenwisBlacklistController', [
      '$scope',
      '$routeParams',
      '$http',
      function($scope, $routeParams, $http) {
       
        $scope.data = [];
        $scope.numResults = 20;

        $scope.updateData = function() {

          //TODO paginate
          $http.get(
              $scope.url + 'openwis.blacklisting.search?startWith='
                  + $scope.username + '&maxResults=' + $scope.numResults).success(
              function(data) {
                  $scope.data = data;

              }).error(function(data) {
            // TODO
          });
        };
        
        $scope.updateData();
      }
  ]);

})();
