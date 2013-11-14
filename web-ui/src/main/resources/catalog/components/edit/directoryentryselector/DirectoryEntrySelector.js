(function() {
  goog.provide('gn_directory_entry_selector');

  var module = angular.module('gn_directory_entry_selector', []);

  /**
   *
   *
   */
  module.directive('gnDirectoryEntrySelector', [
    function() {

      return {
        restrict: 'A',
        replace: true,
        transclude: true,
        scope: {
          mode: '@gnDirectoryEntrySelector'
        },
        templateUrl: '../../catalog/components/edit/' +
            'directoryentryselector/partials/' +
            'directoryentryselector.html',
        link: function(scope, element, attrs) {
          scope.openSelector = function() {
            console.log('open');
          };
        }
      };
    }]);
})();
