(function() {
  goog.provide('gn_openwis_deliver');

  var module = angular.module('gn_openwis_deliver', []);

  module.controller('gnOpenWISDeliver', [
      '$scope',
      function($scope) {
        
        $scope.next = function() {
          setTimeout($("li.active", "#deliverModal").next('li').find('a')
              .trigger('click'));
        }
        $scope.prev = function() {
          setTimeout($("li.active", "#deliverModal").prev('li').find('a')
              .trigger('click'));
        }
        $scope.save = function() {
          alert("Not implemented");
        }
      }
  ]);

})();
