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

                scope.$watch(function() {
                  return $("#" + scope.type + "Modal").find(
                      ".ng-invalid:visible").length;
                }, function(newValue, oldValue) {
                  scope.isValid = $("#" + scope.type + "Modal").find(
                      ".ng-invalid:visible").length == 0;
                });
                
                scope.next = function() {
                  setTimeout($("li.active", "#" + scope.type + "Modal").next('li').find('a')
                      .trigger('click'));
                }
                scope.prev = function() {
                  setTimeout($("li.active", "#" + scope.type + "Modal").prev('li').find('a')
                      .trigger('click'));
                }

                $('.panel-heading h4 > a', elem).on(
                    'click',
                    function(e) {
                      if ($(this).parents('.panel').children('.panel-collapse')
                          .hasClass('in')) {
                        e.stopPropagation();
                      }
                    });

                $('.panel-heading h5 > a', elem).on(
                    'click',
                    function(e) {
                      if ($(this).parent().parent().parent().children(
                          ".panel-collapse").hasClass("in")) {
                        e.stopPropagation();
                      }
                    });
              },
              replace : true,
              templateUrl : '../../catalog/components/subscriptions/partials/request.html'
            };
          });

})();
