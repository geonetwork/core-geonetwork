(function() {
  goog.provide('gn_openwis_request_directive');

  var module = angular.module('gn_openwis_request_directive', []);

  module
      .directive(
          'gnOpenwisRequestDirective',
          function() {
            return {
              restrict : 'AE',
              replace : true,
              templateUrl : '../../catalog/components/subscriptions/partials/request.html'
            };
          });

})();
