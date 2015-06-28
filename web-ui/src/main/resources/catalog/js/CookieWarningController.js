(function() {
  goog.provide('cookie_warning_controller');

  var module = angular.module('cookie_warning_controller', ['ngCookies']);

  module.controller('CookieWarningController', [
    '$cookies', '$rootScope',
    function($cookies, $rootScope) {
      $rootScope.showCookieWarning =
          window.localStorage.getItem('cookiesAccepted') !== 'true';
      $rootScope.close = function($event) {
        $rootScope.showCookieWarning = false;
        window.localStorage.setItem('cookiesAccepted', true);
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
