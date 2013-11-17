(function() {
  goog.provide('gn_multiselect_directive');

  var module = angular.module('gn_multiselect_directive', []);

  /**
     * Provide 2 multiple select list and allow
     * selection of element by double click or
     * move to left/right button.
     *
     *
     * choices and selected MUST be object
     * with a getLabel method. The label will be
     * the value in the list.
     *
     * TODO: Add drag&drop
     */
  module.directive('gnMultiselect', ['$http', '$translate',
    function($http, $translate) {

      return {
        restrict: 'A',
        scope: {
          'selected': '=gnMultiselect',
          'choices': '=',
          'sort': '&sortFn'
        },
        templateUrl: '../../catalog/components/multiselect/partials/' +
            'multiselect.html',
        link: function(scope, element, attrs) {

          var sortOnSelection = true;
          scope.currentSelectionLeft = [];
          scope.currentSelectionRight = [];

          /**
          * Select a single element or the list of currently
          * selected element.
          */
          scope.select = function(k) {
            var elementsToAdd = [];
            if (!k) {
              angular.forEach(scope.currentSelectionLeft, function(value) {
                elementsToAdd.push($.grep(scope.choices, function(n) {
                  return n.getLabel() === value;
                })[0]);
              });
            } else {
              elementsToAdd.push(k);
            }

            angular.forEach(elementsToAdd, function(k) {
              scope.selected.push(k);
              scope.choices = $.grep(scope.choices, function(n) {
                return n !== k;
              });
            });

            if (sortOnSelection) {
              scope.selected.sort(scope.sort);
            }
          };


          scope.unselect = function(k) {
            var elementsToRemove = k ?
                [k.getLabel()] : scope.currentSelectionRight;
            scope.selected = $.grep(scope.selected, function(n) {
              var toUnselect =
                  $.inArray(n.getLabel(), elementsToRemove) !== -1;
              if (toUnselect) {
                scope.choices.push(n);
              }
              return !toUnselect;
            });

            if (sortOnSelection) {
              scope.choices.sort(scope.sort);
            }
          };

        }
      };
    }]);
})();
