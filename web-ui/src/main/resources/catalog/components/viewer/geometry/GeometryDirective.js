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
  goog.provide('gn_geometry_directive');

  var module = angular.module('gn_geometry_directive', []);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnGeometryTool
   *
   * @description
   * This directive adds behavior to an interactive element (link, button).
   * When clicked, the provided map will enter draw mode for the specified type
   * of geometry, and update the linked output object.
   * Possible output formats are: 'object' (ol.Geometry reference), 'gml',
   * 'geojson', 'wkt'; default if undefined is 'object'
   */
  module.directive('gnGeometryTool', [
    function() {
      return {
        restrict: 'E',
        scope: {
          map: '=',
          geometryType: '=',
          output: '=',
          outputFormat: '=',
          allowReset: '@',
          allowModify: '@'
        },
        templateUrl: '../../catalog/components/viewer/geometry/' +
            'partials/geometrytool.html',
        link: function (scope, element, attrs) {
          // internal vars
          var source = new ol.source.Vector({
            useSpatialIndex: false
          });
          var layer = new ol.layer.Vector({
            source: source,
            style: [
              new ol.style.Style({          // this is the default editing style
                fill: new ol.style.Fill({
                  color: 'rgba(255, 255, 255, 0.5)'
                }),
                stroke: new ol.style.Stroke({
                  color: 'white',
                  width: 5
                })
              }),
              new ol.style.Style({
                stroke: new ol.style.Stroke({
                  color: 'rgba(0, 153, 255, 1)',
                  width: 3
                }),
                image: new ol.style.Circle({
                  radius: 6,
                  fill: new ol.style.Fill({
                    color: 'rgba(0, 153, 255, 1)'
                  }),
                  stroke: new ol.style.Stroke({
                    color: 'white',
                    width: 1.5
                  })
                })
              })
            ]
          });
          var drawInteraction = new ol.interaction.Draw({
            type: scope.geometryType,
            source: source
          });
          var modifyInteraction = new ol.interaction.Modify({
            features: source.getFeaturesCollection()
          });

          // add our layer&interactions to the map
          scope.map.addLayer(layer);
          scope.map.addInteraction(drawInteraction);
          scope.map.addInteraction(modifyInteraction);
          drawInteraction.setActive(false);
          modifyInteraction.setActive(false);

          // modifies the output value
          var updateOutput = function (feature) {
            if (!feature) {
              scope.output = null;
              return;
            }

            var formatLabel = (scope.outputFormat || '').toLowerCase();
            var format;
            var outputValue;
            switch (formatLabel) {
              case 'json':
              case 'geojson':
                format = new ol.format.GeoJSON();
                outputValue = format.writeGeometry(feature.getGeometry());
                break;

              case 'wkt':
                format = new ol.format.WKT();
                outputValue = format.writeGeometry(feature.getGeometry());
                break;

              case 'gml':
                format = new ol.format.GML();
                outputValue = format.writeGeometryNode(feature.getGeometry())
                  .innerHTML;
                break;

              // no valid format specified: output as object + give warning
              default:
                console.warn('No valid output format specified for '+
                  'gn-geometry-tool (value=' + scope.outputFormat + '); ' +
                  'outputting geometry as object');

              case 'object':
                outputValue = feature.getGeometry().clone();
                break;
            }

            scope.output = outputValue;
          }

          // clear existing features on draw end
          drawInteraction.on('drawend', function (event) {
            source.clear();
            stop();
            updateOutput(event.feature);
          });

          // update output on modify end
          modifyInteraction.on('modifyend', function (event) {
            updateOutput(event.feature);
          })

          // state handling
          scope.currentMode = 0;    // 0: nothing, 1: drawing, 2: modifying
          scope.startDraw = function () {
            scope.currentMode = 1;
            drawInteraction.setActive(true);
          }
          scope.toggleModify = function () {
            if (scope.currentMode === 0) {
              scope.currentMode = 2;
              modifyInteraction.setActive(true);
            } else {
              stop();
            }
          }
          var stop = function () {
            scope.currentMode = 0;
            drawInteraction.setActive(false);
            modifyInteraction.setActive(false);
          }
          scope.reset = function () {
            scope.currentMode = 0;
            source.clear();
          }
        }
      };
    }]);
})();
