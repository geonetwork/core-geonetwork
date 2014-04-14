(function() {
  goog.provide('gn_onlinesrc_controller');


  var module = angular.module('gn_onlinesrc_controller',
      []);

  /**
   * Metadata editor controller - draft
   */
  module.controller('GnOnlineSrcControllerDepecrated', [
    '$scope', '$routeParams', '$http', '$rootScope', '$translate', '$compile',
    'gnSearchManagerService',
    'gnUtilityService',
    function($scope, $routeParams, $http, $rootScope, $translate, $compile,
            gnSearchManagerService, 
            gnUtilityService) {


      $scope.getTemplateNamesByType = function(type) {
        var tpls = [];
        for (var i = 0; i < $scope.mdList.metadata.length; i++) {
          var mdType = $scope.mdList.metadata[i].type;
          if (mdType instanceof Array) {
            if (mdType.indexOf(type) >= 0) {
              tpls.push($scope.mdList.metadata[i]);
            }
          }
          else if (mdType == type) {
            tpls.push($scope.mdList.metadata[i]);
          }
        }

        $scope.tpls = tpls;
        $scope.activeType = type;
        $scope.activeTpl = null;
        return false;
      };
    }
  ]);
})();
