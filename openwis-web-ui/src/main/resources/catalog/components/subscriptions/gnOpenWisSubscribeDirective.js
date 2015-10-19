(function() {
  goog.provide('gn_openwis_subscribe_directive');

  var module = angular.module('gn_openwis_subscribe_directive', []);

  module
      .directive(
          'gnOpenwisSubscribeDirective',
          function() {
            return {
              restrict : 'AE',
              link : function(scope, elem, attrs) {
                scope.type = attrs.type;
                $('.panel-heading h4 > a', elem).on(
                    'click',
                    function(e) {
                      if ($(this).parents('.panel').children('.panel-collapse')
                          .hasClass('in')) {
                        e.stopPropagation();
                      }
                    });
              },
              replace : true,
              templateUrl : '../../catalog/components/subscriptions/partials/request.html'
            };
          });

})();
