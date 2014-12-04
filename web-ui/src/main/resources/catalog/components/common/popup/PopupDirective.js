(function() {
  goog.provide('gn_popup_directive');

  var module = angular.module('gn_popup_directive', [
  ]);

  module.directive('gnModal',
      function() {
        return {
          restrict: 'A',
          transclude: true,
          scope: {
            toggle: '=gnPopup',
            optionsFunc: '&gnPopupOptions' // Options from directive
          },
          templateUrl: '../../catalog/components/common/popup/' +
              'partials/popup.html',

          link: function(scope, element, attrs) {

            // Get the popup options
            scope.options = scope.optionsFunc();

            if (!scope.options) {
              scope.options = {
                title: ''
              };
            }
            element.addClass('gn-popup modal fade');
          }
        };
      }
  );

  module.directive('gnPopup', [
    '$translate',
    function($translate) {
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
            '<button type="button" class="close" ng-click="close($event)">' +
            '&times;</button>' +
            '<i class="icon-print gn-popup-print hidden-print" ' +
            'title="{{titlePrint}}" ' +
            'ng-if="options.showPrint" ng-click="print()"></i>' +
            '</h4>' +
            '<div class="popover-content gn-popup-content" ' +
            'ng-transclude>' +
            '</div>',

        link: function(scope, element, attrs) {

          // Get the popup options
          scope.options = scope.optionsFunc();
          scope.titlePrint = $translate('print_action');

          if (!scope.options) {
            scope.options = {
              title: ''
            };
          }

          // Per default hide the print function
          if (!angular.isDefined(scope.options.showPrint)) {
            scope.options.showPrint = false;
          }

          // Move the popup to its original position, only used on desktop
          scope.moveToOriginalPosition = function() {
            element.css({
              left: scope.options.x ||
                  $(document.body).width() / 2 - element.width() / 2,
              top: scope.options.y || 89 //89 is the default size of the header
            });
          };

          // Add close popup function
          scope.close = scope.options.close ||
                  (function(event) {
                    if (event) {
                      event.stopPropagation();
                      event.preventDefault();
                    }
                    if (angular.isDefined(scope.toggle)) {
                      scope.toggle = false;
                    } else {
                      element.hide();
                    }
                  });

          scope.print = scope.options.print ||
                  (function() {
                    var contentEl = element.find('.ga-popup-content');
                    gaPrintService.htmlPrintout(contentEl.clone().html());
                  });

          element.addClass('popover');

          // Watch the shown property
          scope.$watch(
              'toggle',
              function(newVal, oldVal) {
                if (newVal != oldVal ||
                    (newVal != (element.css('display') == 'block'))) {
                  element.toggle(newVal);
                  scope.moveToOriginalPosition();
                }
              }
          );
        }
      };
    }
  ]);

})();
