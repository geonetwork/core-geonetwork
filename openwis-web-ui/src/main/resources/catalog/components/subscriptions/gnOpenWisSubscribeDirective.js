(function() {
  goog.provide('gn_openwis_subscribe_directive');

  var module = angular.module('gn_openwis_subscribe_directive', []);

  module
      .directive(
          'gnOpenwisSubscribeDirective',
          function($timeout) {
            return {
              restrict : 'AE',
              link : function(scope, elem, attrs) {
                scope.type = attrs.type;

                scope.$watch(function() {
                  return $("li.active", "#" + scope.type + "Modal").find('a')
                      .attr("data-target");
                }, function(newValue, oldValue) {
                  $("li.active", "#" + scope.type + "Modal").find('a').attr(
                      "data-target") == "#subscribesummary2";
                });

                scope.next = function() {
                  $timeout(function() {
                    $("li.active", "#" + scope.type + "Modal").next('li').find(
                        'a').trigger('click')
                  });
                }
                scope.prev = function() {
                  $timeout(function() {
                    $("li.active", "#" + scope.type + "Modal").prev('li').find(
                        'a').trigger('click')
                  });
                }
                scope.close = function() {
                  $("#subscribeModal").modal('hide');
                  $timeout(function() {
                    $($("li", "#" + scope.type + "Modal")[0]).find('a')
                        .trigger('click')
                  });
                  scope.data = {
                    primary : {},
                    secondary : {}
                  };
                  if (scope.$parent && scope.$parent.$parent
                      && scope.$parent.$parent.$parent
                      && scope.$parent.$parent.$parent.user) {
                    scope.data.username = scope.$parent.$parent.$parent.user.username;
                  }
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
