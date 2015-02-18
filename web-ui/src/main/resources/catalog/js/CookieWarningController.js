(function() {
  goog.provide('cookie_warning_controller');

  var module = angular.module('cookie_warning_controller', ['ngCookies']);

  module.controller('CookieWarningController', ['$cookieStore', '$scope',
    '$rootScope', '$cookies',
    function($cookieStore, $cookies, $rootScope, $scope) {
      $rootScope.showCookieWarning = true;

      if ($cookieStore.get('cookiesAccepted')) {
        $rootScope.showCookieWarning = false;
      }

      $rootScope.close = function($event) {
        $rootScope.showCookieWarning = false;
        $cookieStore.put('cookiesAccepted', true);
        angular.element('.cookie-warning').hide();
      };

      $rootScope.goAway = function($event) {
        angular.forEach($cookies, function(cookie, key) {
          if (key.indexOf('NAV-') > -1) {
            $window.sessionStorage.setItem(key, cookie);
            delete $cookies[key];
          }
        });

        window.history.back();
      };
    }]);

})();
