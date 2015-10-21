(function() {
  goog.provide('gn_openwis_subscribe');

  var module = angular.module('gn_openwis_subscribe', []);

  module
      .controller(
          'gnOpenWISSubscribe',
          [
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
                  if (!$("#subscribepublicDissemination").hasClass("in")) {
                    $scope.data.primary.email = null;
                    $scope.data.primary.host = null;
                  } else {
                    if (!$("#subscribemail").hasClass("in")) {
                      $scope.data.primary.email = null;
                    } else {
                      $scope.data.primary.host = null;
                    }
                  }
                  
                  if (!$("#subscribepublicDissemination2").hasClass("in")) {
                    $scope.data.primary.email = null;
                    $scope.data.primary.host = null;
                  } else {
                    if (!$("#subscribemail2").hasClass("in")) {
                      $scope.data.primary.email = null;
                    } else {
                      $scope.data.primary.host = null;
                    }
                  }
                  // TODO fix md extraction
                  var md = $(
                      "*[data-ng-controller=gnOpenWISCheckSubscriptionButton]")
                      .data().$scope.md;

                  $scope.data.metadataUrn = md['geonet:info'].uuid;
                  $scope.data.title = md['geonet:info'].title;
                  if ($scope.$parent && $scope.$parent.$parent
                      && $scope.$parent.$parent.$parent
                      && $scope.$parent.$parent.$parent.user) {
                    $scope.data.username = $scope.$parent.$parent.$parent.user.username;
                  }

                  $http({
                    url : 'openwis.subscription.new',
                    params : {
                      'data' : JSON.stringify($scope.data)
                    },
                    method : 'GET'
                  })
                      .success(
                          function() {
                            $("#subscribeModal").modal('hide');
                            $scope.data = {
                              primary : {},
                              secondary : {}
                            };
                            if ($scope.$parent && $scope.$parent.$parent
                                && $scope.$parent.$parent.$parent
                                && $scope.$parent.$parent.$parent.user) {
                              $scope.data.username = $scope.$parent.$parent.$parent.user.username;
                            }
                          }).error(
                          function(data) {
                            $rootScope.$broadcast('StatusUpdated', {
                              title : $translate('openwisError'),
                              error : data,
                              message : data.substring(
                                  data.indexOf("<body>") + 6, data
                                      .lastIndexOf("</body>")),
                              timeout : 0,
                              type : 'danger'
                            });
                          });
                }

              }
          ]);

})();
