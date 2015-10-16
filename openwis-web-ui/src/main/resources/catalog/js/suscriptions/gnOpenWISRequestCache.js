(function() {
  goog.provide('gn_openwis_request_cache');

  var module = angular.module('gn_openwis_request_cache', []);

  module.controller('gnOpenWISRequestCache', [
      '$scope',
      function($scope) {
        $scope.next = function() {
        setTimeout($("li.active", "#requestCacheModal").next('li').find('a')
            .trigger('click'));
      }
      $scope.prev = function() {
        setTimeout($("li.active", "#requestCacheModal").prev('li').find('a')
            .trigger('click'));
      }
      $scope.save = function() {
        alert("Not implemented");
      }
      }
  ]);

})();
