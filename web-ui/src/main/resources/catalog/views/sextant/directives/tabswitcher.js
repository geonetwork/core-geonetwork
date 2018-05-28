(function() {

  goog.provide('sxt_tabswitcher');

  var module = angular.module('sxt_tabswitcher', []);


  module.directive('sxtTabSwitcher', ['gnGlobalSettings',
    function(gnGlobalSettings) {
      return {
        restrict: 'E',
        replace: true,
        templateUrl: '../../catalog/views/sextant/directives/' +
            'partials/tabswitcher.html',
        link: function linkFn(scope, attrs) {
          scope.isVisible = Object.keys(scope.mainTabs).length > 1;
          scope.allowLogin = typeof sxtSettings !== 'undefined' ?
            sxtSettings.allowLogin : true;
          scope.signInUrl = gnGlobalSettings.gnUrl + 'info?casLogin';
        }
      };
    }
  ]);
})();
