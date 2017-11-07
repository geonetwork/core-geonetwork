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
  goog.provide('gn_heatmap_directive');

  var module = angular.module('gn_heatmap_directive', [
  ]);

  /**
   * @ngdoc directive
   * @name gn_heatmap.directive:gnHeatmap
   *
   * @description
   * Given a feature type, this directive will query the ElasticSearch backend
   * to render a heatmap of features on the map.
   * The heatmap is actually several box features which gives info when
   * hovered (feature count, etc.). These features are redrawn on every map
   * move.
   */
  module.directive('gnHeatmap', ['gnHeatmapService',
    function(gnHeatmapService) {
      return {
        restrict: 'E',
        scope: {
          map: '<',
          featureType: '<',
          enabled: '<'
        },
        bindToController: true,
        controllerAs: 'ctrl',
        controller: ['$scope', function($scope) {
          var ctrl = this;

          // create a vector layer to hold the features
          ctrl.source = new ol.source.Vector({
            features: []
          });
          ctrl.layer = new ol.layer.Vector({
            source: ctrl.source,
            style: gnHeatmapService.getCellStyle()
          });
          ctrl.map.addLayer(ctrl.layer);

          // this will refresh the heatmap
          ctrl.refresh = function() {
            gnHeatmapService.requestHeatmapData(ctrl.featureType, ctrl.map)
              .then(function(cells) {
                // add cells as features
                ctrl.source.clear();
                ctrl.source.addFeatures(cells);
              });
          }

          // watch "enabled" param to show/hide layer
          $scope.$watch("ctrl.enabled", function(newValue, oldValue) {
            ctrl.layer.setVisible(!!newValue);
            if (newValue) {
              ctrl.refresh();
            }
          });

          // refresh features on map move
          ctrl.map.on('moveend', function() {
            if (!ctrl.enabled) {
              return;
            }
            // console.log('refresh heatmap, features: ' + ctrl.featureType);
            ctrl.refresh();
          });
        }],
        link: function(scope, element, attrs) {

        }
      };
    }]);

})();
