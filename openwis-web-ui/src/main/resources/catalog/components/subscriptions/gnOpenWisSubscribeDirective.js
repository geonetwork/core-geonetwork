(function() {
  goog.provide('gn_openwis_subscribe_directive');

  var module = angular.module('gn_openwis_subscribe_directive', []);

  module
      .directive(
          'gnOpenwisSubscribeDirective',
          function() {
            return {
              restrict : 'AE',
              replace : true,
              templateUrl : '../../catalog/components/subscriptions/partials/subscribe.html'
            };
          });

})();
