(function() {
  goog.provide('gn_openwis_request_cache_directive');

  var module = angular.module('gn_openwis_request_cache_directive', []);

  module
      .directive(
          'gnOpenwisRequestCacheDirective',
          function() {
            return {
              restrict : 'AE',
              replace : true,
              templateUrl : '../../catalog/components/subscriptions/partials/requestCache.html'
            };
          });

})();
