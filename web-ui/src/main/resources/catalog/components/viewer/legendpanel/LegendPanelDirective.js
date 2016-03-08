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
  goog.provide('gn_legendpanel_directive');

  var module = angular.module('gn_legendpanel_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnLegendPanel
   *
   * @description
   * This directive `gnLegendPanel` is a panel containing all legends of all
   * active layers in the map.
   */
  module.directive('gnLegendPanel', [
    '$filter',
    'gnLayerFilters',

    function($filter, gnLayerFilters) {

      return {
        restrict: 'A',
        scope: {
          map: '=gnLegendPanel'
        },
        templateUrl: '../../catalog/components/viewer/legendpanel/partials/' +
            'legendpanel.html',
        link: function(scope, element, attrs) {

          scope.layers = scope.map.getLayers().getArray();
          scope.layerFilterFn = gnLayerFilters.visible;
        }
      };
    }]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnLayerorderPanel
   *
   * @description
   * This directive `gnLayerorderPanel` is a panel which offers tools to change
   * active layers order in the map.
   */
  module.directive('gnLayerorderPanel', [
    '$filter',
    'gnLayerFilters',

    function($filter, gnLayerFilters) {

      return {
        restrict: 'A',
        scope: {
          map: '=gnLayerorderPanel'
        },
        templateUrl: '../../catalog/components/viewer/legendpanel/partials/' +
            'layerorderpanel.html',
        link: function(scope, element, attrs) {

          var map = scope.map;
          scope.layers = map.getLayers().getArray();
          scope.layerFilterFn = gnLayerFilters.visible;

          /**
           * Change layer index in the map.
           *
           * @param {ol.layer} layer
           * @param {float} delta
           */
          scope.moveLayer = function(layer, delta) {
            var layersCollection = map.getLayers();
            var index = layersCollection.getArray().indexOf(layer);
            layersCollection.removeAt(index);
            layersCollection.insertAt(index + delta, layer);
          };
        }
      };
    }]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnLayersourcesPanel
   *
   * @description
   * This directive `gnLayersourcesPanel` is a panel that displays a list of
   * all layers and their sources.
   */
  module.directive('gnLayersourcesPanel', [
    '$filter',
    'gnLayerFilters',

    function($filter, gnLayerFilters) {

      return {
        restrict: 'A',
        scope: {
          map: '=gnLayersourcesPanel'
        },
        templateUrl: '../../catalog/components/viewer/legendpanel/partials/' +
            'layersources.html',
        link: function(scope, element, attrs) {

          var map = scope.map;
          scope.layers = map.getLayers().getArray();
          scope.layerFilterFn = gnLayerFilters.visible;
        }
      };
    }]);

})();
