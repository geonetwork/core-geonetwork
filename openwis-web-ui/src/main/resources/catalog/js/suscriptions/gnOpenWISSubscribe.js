(function() {
  goog.provide('gn_openwis_subscribe');

  var module = angular.module('gn_openwis_subscribe', []);

  module.controller('gnOpenWISSubscribe', [
      '$scope',
      function($scope) {
        $scope.next = function() {
          setTimeout($("li.active", "#subscribeModal").next('li').find('a')
              .trigger('click'));
        }
        $scope.prev = function() {
          setTimeout($("li.active", "#subscribeModal").prev('li').find('a')
              .trigger('click'));
        }
        $scope.save = function() {
          alert("Not implemented");
        }
      }
  ]);

})();
