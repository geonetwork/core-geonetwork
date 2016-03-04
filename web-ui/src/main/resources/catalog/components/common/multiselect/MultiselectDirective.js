/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

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
          'sortFn': '&',
          'labelProp': '@'  // Function or property
        },
        templateUrl: '../../catalog/components/common/multiselect/partials/' +
            'multiselect.html',
        link: function(scope, element, attrs) {

          var sortOnSelection = true;
          scope.currentSelectionLeft = [];
          scope.currentSelectionRight = [];
          scope.selected = [];

          /**
           * Return the label of the element
           * It could be a property of the object
           * or a custom function which build the label
           */
          scope.getLabel = function(e) {
            if (angular.isString(e[scope.labelProp])) {
              return e[scope.labelProp];
            } else if (angular.isFunction(e[scope.labelProp])) {
              return e[scope.labelProp]();
            }
          };
          /**
          * Select a single element or the list of currently
          * selected element.
          */
          scope.select = function(k) {
            var elementsToAdd = [];
            if (!k) {
              angular.forEach(scope.currentSelectionLeft, function(value) {
                elementsToAdd.push($.grep(scope.choices, function(n) {
                  return scope.getLabel(n) === value;
                })[0]);
              });
            } else {
              elementsToAdd.push(k);
            }

            angular.forEach(elementsToAdd, function(e) {
              scope.selected.push(e);
              scope.choices = $.grep(scope.choices, function(n) {
                return scope.getLabel(n) !== scope.getLabel(e);
              });
            });

            if (sortOnSelection) {
              scope.selected.sort(scope.sortFn);
            }
          };


          scope.unselect = function(k) {
            var elementsToRemove = k ?
                [scope.getLabel(k)] : scope.currentSelectionRight;
            scope.selected = $.grep(scope.selected, function(n) {
              var toUnselect =
                  $.inArray(scope.getLabel(n), elementsToRemove) !== -1;
              if (toUnselect) {
                scope.choices.push(n);
              }
              return !toUnselect;
            });

            if (sortOnSelection) {
              scope.choices.sort(scope.sortFn);
            }
          };

        }
      };
    }]);
})();
