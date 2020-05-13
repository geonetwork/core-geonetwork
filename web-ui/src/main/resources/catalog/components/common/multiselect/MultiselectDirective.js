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
     * Only support array of object with id and label props.
     *
     * TODO: Add drag&drop
     */
  module.directive('gnMultiselect', [
    function() {

      return {
        restrict: 'A',
        scope: {
          'selected': '=gnMultiselect',
          'choices': '='
        },
        templateUrl: '../../catalog/components/common/multiselect/partials/' +
            'multiselect.html',
        link: function(scope, element, attrs) {

          var sortOnSelection = true;

          //
          scope.currentSelectionLeft = [];
          scope.currentSelectionRight = [];

          // When selection list is updated, filter selected one
          // from the list of choices
          scope.$watch('selected', function(n, o) {
            if (n !== o) {
              scope.options = [];
              for (var i = 0; i < scope.choices.length; i++) {
                var e = scope.choices[i];
                var isInSelection = false;
                for (var j = 0; j < scope.selected.length; j++) {
                  if (scope.selected[j].id === e.id) {
                    isInSelection = true;
                    break;
                  }
                }
                if (!isInSelection) {
                  scope.options.push(e);
                }
              }
              // Sort both list
              scope.options.sort(scope.sortFn);
              scope.selected.sort(scope.sortFn);
            }
          });
          scope.selected = scope.selected || [];

          /**
          * Select a single element or the list of currently
          * selected element.
          */
          scope.select = function(k) {
            var elementsToAdd = [];
            if (!k) {
              angular.forEach(scope.currentSelectionLeft, function(value) {
                elementsToAdd.push($.grep(scope.choices, function(n) {
                  return n.id == value;
                })[0]);
              });
            } else {
              elementsToAdd.push(k);
            }

            angular.forEach(elementsToAdd, function(e) {
              scope.selected.push(e);
              var idx = null;
              for (var i = 0; i < scope.options.length; i++) {
                if (scope.options[i].id == e.id) {
                  idx = i;
                  break;
                }
              }
              scope.options.splice(idx, 1);
            });

            if (sortOnSelection) {
              scope.selected.sort(scope.sortFn);
              scope.options.sort(scope.sortFn);
            }

            scope.currentSelectionLeft = [];
            scope.currentSelectionRight = [];
          };

          scope.sortFn = function(a, b) {
            if (a.langlabel && b.langlabel) {
              return a.langlabel.toLowerCase() > b.langlabel.toLowerCase();
            } else {
              return a.name.toLowerCase() > b.name.toLowerCase();
            }
          };

          scope.unselect = function(k) {
            var elementsToRemove = k ?
                [k.id] : scope.currentSelectionRight;
            scope.selected = $.grep(scope.selected, function(n) {
              var unselect = false;
              for (var i = 0; i < elementsToRemove.length; i++) {
                if (elementsToRemove[i] == n.id) {
                  unselect = true;
                  scope.options.push(n);
                  break;
                }
              }

              scope.currentSelectionLeft = [];
              scope.currentSelectionRight = [];

              return !unselect;
            });

            if (sortOnSelection) {
              scope.selected.sort(scope.sortFn);
              scope.options.sort(scope.sortFn);
            }
          };
        }
      };
    }]);
})();
