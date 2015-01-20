(function() {
  goog.provide('cookie_warning_directive');

  var module = angular.module('cookie_warning_directive', []);

  module
      .directive(
          'cookiewarning',
          function() {
            return {
              restrict: 'AE',
              replace: true,
              templateUrl:
              '../../catalog/components/cookieWarning/partials/cookieWarning.html'
            };
          });

})();
