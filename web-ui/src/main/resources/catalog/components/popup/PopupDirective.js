(function() {
  goog.provide('gn_popup_directive');
  //  goog.require('gn_browsersniffer_service');

  var module = angular.module('gn_popup_directive', [
    //    'gn_browsersniffer_service'
  ]);

  module.directive('gnPopup',
      function() {
        return {
          restrict: 'A',
          transclude: true,
          scope: {
            toggle: '=gnPopup',
            optionsFunc: '&gnPopupOptions' // Options from directive
          },
          template:
              '<h4 class="popover-title gn-popup-title">' +
              '<span translate>{{options.title}}</span>' +
              '<button type="button" class="close" ng-click="close()">' +
              '&times;</button>' +
              '</h4>' +
              '<div class="popover-content gn-popup-content" ' +
              'ng-transclude>' +
              '</div>',

          link: function(scope, element, attrs) {

            // Get the popup options
            scope.options = scope.optionsFunc();

            if (!scope.options) {
              scope.options = {
                title: ''
              };
            }

            // Add close popup function
            scope.close = scope.options.close ||
                (function() {element.toggle();});

            // Move the popup to the correct position
            element.addClass('popover gn-popup');
            element.css({
              left: scope.options.x ||
                  $(document.body).width() / 2 - element.width() / 2,
              top: scope.options.y || 150
            });

            // Watch the shown property
            scope.$watch(
                'toggle',
                function(newVal, oldVal) {
                  if (newVal != oldVal) {
                    element.toggle();
                  }
                }
            );
          }
        };
      }
  );
})();
