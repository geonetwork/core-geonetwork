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
})();
