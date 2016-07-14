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
  goog.provide('gn_ows_directive');

  angular.module('gn_ows_directive', [])

      .directive(
      'gnLayersGrid',
      [
       'gnOwsCapabilities',
       function(gnOwsCapabilities) {
         return {
           restrict: 'A',
           templateUrl: '../../catalog/components/common/ows/' +
           'partials/layersGrid.html',
           scope: {
             selection: '=',
             layers: '=',
             selectionMode: '=gnSelectionMode'
           },
           link: function(scope, element, attrs) {
             // Manage layers selection
             if (scope.selectionMode) {
               scope.isSelected = function(layerName) {
                 if (layerName) {
                   for (var i = 0; i < scope.selection.length; i++) {
                      if (scope.selection[i].Name === layerName) {
                        return true;
                      }
                   }
                 }
                 return false;
               };
               scope.select = function(layer) {
                 if (scope.selectionMode.indexOf('multiple') >= 0) {
                   if (scope.selection.indexOf(layer) < 0) {
                     scope.selection.push(layer);
                   }
                   else {
                     scope.selection.splice(scope.selection.indexOf(layer), 1);
                   }
                 }
                 else {
                   scope.selection.pop();
                   scope.selection.push(layer);
                 }
               };
             }
           }
         };
       }]);
})();
