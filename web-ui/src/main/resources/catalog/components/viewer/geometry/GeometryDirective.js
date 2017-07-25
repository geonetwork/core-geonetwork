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
   * If 'outputAsFeatures' is true, a FeatureCollection object will be output
   * instead of a single feature
   */
  module.directive('gnGeometryTool', [
    function() {
      return {
        restrict: 'E',
        scope: {
          map: '<',
          geometryType: '<',
          output: '=',
          outputFormat: '<',
          allowReset: '@',
          allowModify: '@',
          outputAsFeatures: '@'
        },
        templateUrl: '../../catalog/components/viewer/geometry/' +
            'partials/geometrytool.html',
        controller: [
          '$scope',
          'ngeoDecorateInteraction',
          'gnGeometryService',
          function GeometryToolController(
            $scope,
            ngeoDecorateInteraction,
            gnGeometryService) {
            var layer = gnGeometryService.getCommonLayer($scope.map);
            var source = layer.getSource();
            var myFeatures = new ol.Collection();

            $scope.drawInteraction = new ol.interaction.Draw({
              type: $scope.geometryType,
              source: source
            });
            $scope.modifyInteraction = new ol.interaction.Modify({
              features: myFeatures
            });

            // add our layer&interactions to the map
            $scope.map.addInteraction($scope.drawInteraction);
            $scope.map.addInteraction($scope.modifyInteraction);
            $scope.drawInteraction.setActive(false);
            $scope.modifyInteraction.setActive(false);
            ngeoDecorateInteraction($scope.drawInteraction);
            ngeoDecorateInteraction($scope.modifyInteraction);

            // cleanup when scope is destroyed
            $scope.$on('$destroy', function() {
              removeMyFeatures();
              $scope.map.removeInteraction($scope.drawInteraction);
              $scope.map.removeInteraction($scope.modifyInteraction);
            });

            // remove all my features from the map
            var removeMyFeatures = function () {
              myFeatures.forEach(function (feature) {
                source.removeFeature(feature);
              });
              myFeatures.clear();
            };

            // modifies the output value
            var updateOutput = function (feature) {
              if (!feature) {
                $scope.output = null;
                return;
              }

              // set id on feature
              feature.setId('geometry-tool-output');

              var formatLabel = ($scope.outputFormat || '').toLowerCase();
              var format;
              var outputValue;
              switch (formatLabel) {
                case 'json':
                case 'geojson':
                  format = new ol.format.GeoJSON();
                  if ($scope.outputAsFeatures) {
                    outputValue = format.writeFeatures([feature]);
                  } else {
                    outputValue = format.writeGeometry(feature.getGeometry());
                  }
                  break;

                case 'wkt':
                  format = new ol.format.WKT();
                  if ($scope.outputAsFeatures) {
                    outputValue = format.writeFeatures([feature]);
                  } else {
                    outputValue = format.writeGeometry(feature.getGeometry());
                  }
                  break;

                case 'gml':
                  format = new ol.format.GML({
                    featureNS: 'http://mapserver.gis.umn.edu/mapserver',
                    featureType: 'features'
                    // srsName: $scope.map.getView().getProjection().getCode()
                  });

                  // TODO: refactor this: first clone geom & transform,
                  // then if necessary create a new feature with this geom
                  var feature2 = new ol.Feature({
                    id: 'geometry-tool-output',
                    geometry: feature.getGeometry().clone()
                  });
                  feature2.getGeometry().transform(
                      $scope.map.getView().getProjection(), 'EPSG:4326');

                  if ($scope.outputAsFeatures) {
                    outputValue = '<wfs:FeatureCollection ' +
                        'xmlns:wfs="http://www.opengis.net/wfs">' +
                        format.writeFeatures([feature2]) +
                        '</wfs:FeatureCollection>';
                  } else {
                    outputValue = format.writeGeometryNode(
                        feature2.getGeometry())
                        .innerHTML;
                  }
                  break;

                // no valid format specified: output as object + give warning
                default:
                  console.warn('No valid output format specified for ' +
                      'gn-geometry-tool (value=' + $scope.outputFormat + '); ' +
                      'outputting geometry as object');

                case 'object':
                  if ($scope.outputAsFeatures) {
                    outputValue = [feature];
                  } else {
                    outputValue = feature.getGeometry().clone();
                  }
                  break;
              }

              $scope.output = outputValue;
            };

            // clear existing features on draw end & save feature
            $scope.drawInteraction.on('drawend', function(event) {
              removeMyFeatures();
              updateOutput(event.feature);
              $scope.drawInteraction.setActive(false);
              myFeatures.push(event.feature);
            });

            // update output on modify end
            $scope.modifyInteraction.on('modifyend', function(event) {
              updateOutput(event.features.item(0));
            });

            // reset drawing
            $scope.reset = function() {
              removeMyFeatures();
              updateOutput();
            };
          }
        ]
      };
    }]);
})();
