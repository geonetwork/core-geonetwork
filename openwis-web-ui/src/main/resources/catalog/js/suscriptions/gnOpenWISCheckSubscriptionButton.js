(function() {
  goog.provide('gn_openwis_check_subscription_button');

  var module = angular.module('gn_openwis_check_subscription_button', []);

  module.controller('gnOpenWISCheckSubscriptionButton', [
      '$scope',
      '$http',
      '$attrs',
      function($scope, $http, $attrs) {

        $scope.isVisible = false;
        $scope.exists = false;
        $scope.isAvailable = false;

        $scope.addMetadataObject = function(type, md) {

          data = $("*[data-ng-controller=" + type + "]").data().$scope.data;

          data.title = $attrs.mdtitle;
          data.metadataUrn = $attrs.md;
          data.username = $scope.user.username;
        }

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

            $scope.isVisible = md['geonet:info'].download && $scope.user.username;

            $http.get('openwis.cache.check?urn=' + md['geonet:info'].uuid)
                .success(function(data) {
                  $scope.isAvailable = data;
                }).error(function(data) {
                  $scope.isAvailable = false;
                });
          }
        });
      }
  ]);

})();
