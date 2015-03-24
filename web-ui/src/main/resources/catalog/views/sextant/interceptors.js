(function() {

  goog.provide('sxt_interceptors');

  var module = angular.module('sxt_interceptors', []);

  module.factory('sxtInterceptor', ['gnGlobalSettings',
    function(gnGlobalSettings) {

    return {
      'request': function(config) {
        if(config.url.indexOf('http://') < 0) {
          config.url = gnGlobalSettings.gnUrl + config.url;
        }
        return config;
      }
    };
  }]);

  module.config([
    '$httpProvider',
    'gnGlobalSettings',
    function($httpProvider, gnGlobalSettings) {

      if(gnGlobalSettings.gnUrl) {
        $httpProvider.interceptors.push('sxtInterceptor');
      }
  }]);
})();
