(function() {
  goog.provide('gn_vcs_controller');

  goog.require('gn_vcs');

  var module = angular.module('gn_vcs_controller',
      ['gn_vcs']);


  /**
   *
   */
  module.controller('GnVcsController', [
    '$scope', '$routeParams', '$http', 'gnVcsService',
    function($scope, $routeParams, $http, gnVcsService) {
      $scope.logs = [];
      gnVcsService.getLog().then(function(data) {
        if (data !== 'null') {
          $scope.logs = data;
        }
      });
    }]);

})();
