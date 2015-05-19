(function() {

  goog.provide('sxt_tabswitcher');

  var module = angular.module('sxt_tabswitcher', []);


  module.directive('sxtTabSwitcher', [
    function() {
      return {
        restrict: 'E',
        replace: true,
        templateUrl: '../../catalog/views/sextant/directives/' +
            'partials/tabswitcher.html',
        link: function linkFn(scope, element, attrs) {
        }
      };
    }
  ]);
})();
