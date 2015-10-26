(function() {
  goog.provide('gn_openwis_download');

  var module = angular.module('gn_openwis_download', []);

  module.controller('gnOpenWISDownload', [
              '$scope',
              '$http',
              '$element',
              '$rootScope',
              '$translate',
              function($scope, $http, $element, $rootScope, $translate) {

                $scope.data = {
                  primary : {},
                  secondary : {}
                };
                if ($scope.$parent && $scope.$parent.$parent
                    && $scope.$parent.$parent.$parent
                    && $scope.$parent.$parent.$parent.user) {
                  $scope.data.username = $scope.$parent.$parent.$parent.user.username;
                }

                $scope.save = function() {
                  if ($scope.$parent && $scope.$parent.$parent
                      && $scope.$parent.$parent.$parent
                      && $scope.$parent.$parent.$parent.user) {
                    $scope.data.username = $scope.$parent.$parent.$parent.user.username;
                  }

                  $http({
                    url : 'openwis.processrequest.new',
                    params : {
                      'data' : JSON.stringify($scope.data)
                    },
                    method : 'GET'
                  }).success(function() {
                    $scope.close();
                    $rootScope.$broadcast('StatusUpdated', {
                      title : $translate('openwisSuccessDeliver'),
                      message : $translate('openwisSuccessTrackID') + data,
                      timeout : 0,
                      type : 'success'
                    });
                  }).error(
                      function(data) {
                        $rootScope.$broadcast('StatusUpdated', {
                          title : $translate('openwisError'),
                          error : data,
                          message : data.substring(data.indexOf("<body>") + 6,
                              data.lastIndexOf("</body>")),
                          timeout : 0,
                          type : 'danger'
                        });
                      });
                }

              }
          ]);

})();
