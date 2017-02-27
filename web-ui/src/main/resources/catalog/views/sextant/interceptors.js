(function() {

  goog.provide('sxt_interceptors');

  var module = angular.module('sxt_interceptors', []);

  var isChrome = /Chrome/.test(navigator.userAgent) &&
      /Google Inc/.test(navigator.vendor);

  module.factory('sxtInterceptor', ['gnGlobalSettings',
    function(gnGlobalSettings) {

      return {
        'request': function(config) {
          return config;
          if(config.url.indexOf('http://') < 0 &&
              config.url.indexOf('https://') < 0) {
            if(!config.url.match(/(partials\/).*(.html)$/)) {

              // Temp for chrome to avoid CORS caching issue
              if(isChrome && config.method == 'GET' && config.cache != true
                  && config.url != 'q'
                  && config.url.indexOf('qi?') != 0
                  && config.url.indexOf('q?') != 0
                  && config.url.indexOf('GetFeatureInfo') < 0 ) {

                var param = {
                  __id: Math.floor(Math.random() * (99999999999 - 0))
                };
                config.params = angular.extend({}, config.params, param);
              }

              if(config.url[0] == 'q') {
                console.log(config.url);
              }

              // Use api url
              config.url = gnGlobalSettings.gnUrl + config.url;

            }
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
