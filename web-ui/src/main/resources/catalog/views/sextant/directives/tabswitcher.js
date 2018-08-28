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

          // signin url is https://host/geonetwork/signin?service=<currentUrl>
          var currentUrl = window.location.origin + window.location.pathname;
          scope.signInUrl = gnGlobalSettings.gnUrl + '../../signin?service=' +
            currentUrl;
        }
      };
    }
  ]);
})();
