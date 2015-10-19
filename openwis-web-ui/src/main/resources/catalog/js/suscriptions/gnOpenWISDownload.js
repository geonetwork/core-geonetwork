(function() {
  goog.provide('gn_openwis_download');

  var module = angular.module('gn_openwis_download', []);

  module.controller('gnOpenWISDownload', [
      '$scope',
      function($scope) {        
        $scope.next = function() {
        setTimeout($("li.active", "#downloadModal").next('li').find('a')
            .trigger('click'));
      }
      $scope.prev = function() {
        setTimeout($("li.active", "#downloadModal").prev('li').find('a')
            .trigger('click'));
      }
      $scope.save = function() {
        alert("Not implemented");
      }
      }
  ]);

})();
