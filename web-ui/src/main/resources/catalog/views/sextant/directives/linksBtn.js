(function() {

  goog.provide('sxt_linksbtn');

  var module = angular.module('sxt_linksbtn', []);


  module.directive('sxtLinksBtn', [
    function() {
      return {
        restrict: 'E',
        replace: true,
        scope: true,
        templateUrl: '../../catalog/views/sextant/directives/' +
            'partials/linksbtn.html',
        link: function(scope) {
        }
      };
    }
  ]);
})();
