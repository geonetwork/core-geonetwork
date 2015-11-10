(function() {
  goog.provide('gn_openwis_check_subscription_button');

  var module = angular.module('gn_openwis_check_subscription_button', []);

  module.controller('gnOpenWISCheckSubscriptionButton', [
      '$scope',
      '$http',
      '$attrs',
      '$timeout',
      function($scope, $http, $attrs, $timeout) {

        $scope.isVisible = false;
        $scope.exists = false;
        $scope.isAvailable = false;
        $scope.isRequested = false;
        $scope.directDownload = false;
        $scope.hasChildren = false;

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

            $scope.isVisible = md['geonet:info'].download
                && $scope.user.username;

            $http.get('openwis.cache.check?urn=' + md['geonet:info'].uuid)
                .success(function(data) {
                  $scope.isAvailable = data;
                }).error(function(data) {
                  $scope.isAvailable = false;
                });
            

            $http.get(
                'xml.relation?_content_type=json&type=children&uuid=' + md['geonet:info'].uuid)
                .success(function(data) {
                  $scope.hasChildren = data && data.relation;
                }).error(function(data) {
                  $scope.hasChildren = false;
                });


            $scope.checkDirectDownload();
          }
        });

        $scope.checkDirectDownload = function() {
          $http.get(
              'openwis.processrequest.check?urn=' + $scope.md['geonet:info'].uuid)
              .success(function(data) {
                if (!data) {
                  $scope.isRequested = false;
                  $scope.directDownload = false;
                } else {
                  $scope.isRequested = true;
                  if ($.isNumeric(data)) {
                    $scope.directDownload = false;
                    //try until we get the url directDownload
                    $timeout($scope.checkDirectDownload, 5000);
                  } else {
                    $scope.directDownload = data;
                  }
                }
              }).error(function(data) {
                $scope.isRequested = false;
                $scope.directDownload = false;
                
                //wait a bit and try again, some error?
                $timeout($scope.checkDirectDownload, 5000);
              });
        };
      }
  ]);

})();
