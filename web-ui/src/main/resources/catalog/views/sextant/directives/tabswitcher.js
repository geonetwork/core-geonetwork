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
          // following #41968, we make sure there is always a query string in currentUrl
          scope.getSignInUrl = function() {
            var currentUrl = window.location.origin + window.location.pathname
                + (window.location.search || '?') + window.location.hash;
            return gnGlobalSettings.gnUrl + '../../signin?service=' +
              encodeURIComponent(currentUrl);
          }
        }
      };
    }
  ]);
})();
