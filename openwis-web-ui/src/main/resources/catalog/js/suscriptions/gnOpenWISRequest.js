(function() {
  goog.provide('gn_openwis_request');

  var module = angular.module('gn_openwis_request', []);

  module.controller('gnOpenWISRequest', [
      '$scope',
      function($scope) {
        $scope.next = function() {
          setTimeout($("li.active", "#requestModal").next('li').find('a')
              .trigger('click'));
        }
        $scope.prev = function() {
          setTimeout($("li.active", "#requestModal").prev('li').find('a')
              .trigger('click'));
        }
        $scope.save = function() {
          alert("Not implemented");
        }
      }
  ]);

})();
