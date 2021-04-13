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
  goog.provide('gn_bounding_directive');


  var module = angular.module('gn_bounding_directive', []);

  /**
   * @ngdoc directive
   * @name gn_bounding.directive:gnBoundingPolygon
   *
   * @description
   * This directive gives the user the possibility to define a bounding polygon,
   * either by drawing it manually on a map or copy-pasting data in the desired
   * format. The user can also select an input projection.
   * The directive has a hidden output in GML.
   *
   * @attribute {string} polygonXml existing geometry in GML format
   * @attribute {string} identifier id of the hidden input
   * that will hold the value entered by the user
   * @attribute {string} outputCrs EPSG:XXXX ref to use for geometry output;
   * if undefined, the current projection selected by the user will be used
   */
  module.directive('gnBoundingPolygon', [
    'gnMap',
    function(gnMap) {
      return {
        restrict: 'E',
        scope: {
          polygonXml: '@',
          identifier: '@'
        },
        templateUrl: '../../catalog/components/edit/bounding/' +
            'partials/boundingpolygon.html',
        controllerAs: 'ctrl',
        bindToController: true,
        controller: [
          '$scope',
          '$attrs',
          '$http',
          'gnMap',
          'gnMapsManager',
          'gnGeometryService',
          function BoundingPolygonController(
            $scope,
            $attrs,
            $http,
            gnMap,
            gnMapsManager,
            gnGeometryService) {
            var ctrl = this;

            // set read only
            ctrl.readOnly = $scope.$eval($attrs['readOnly']);

            // init map
            ctrl.map = gnMapsManager.createMap(gnMapsManager.EDITOR_MAP);
            ctrl.map.get('creationPromise').then(function() {
              ctrl.initValue();
            });

            // Get interaction layer
            var layer = gnGeometryService.getCommonLayer(ctrl.map);
            var source = layer.getSource();

            // Draw interaction variables
            var activeDrawInteraction;
            ctrl.activeDrawType = null;

            // this is used to deactivate zoom on draw end
            ctrl.zoomInteraction = null;
            ctrl.map.getInteractions().forEach(function(interaction) {
              if (interaction instanceof ol.interaction.DoubleClickZoom) {
                ctrl.zoomInteraction = interaction;
              }
            });

            // Clear existing features on draw end & save feature
            function handleDrawEnd(event) {
              clearActiveDrawInteraction();
              ctrl.fromTextInput = false;
              source.clear(event.feature);
              ctrl.updateOutput(event.feature);
              $scope.$digest();

              // prevent interference by zoom interaction
              // see https://github.com/openlayers/openlayers/issues/3610
              if (ctrl.zoomInteraction) {
                ctrl.zoomInteraction.setActive(false);
                setTimeout(function() {
                  ctrl.zoomInteraction.setActive(true);
                }, 251);
              }
            }

            // Update text field when geometry has modified
            function handleModifyEnd(event) {
              ctrl.fromTextInput = false;
              ctrl.updateOutput(event.features.item(0));
              $scope.$digest();
            }

            // Removes event handler from current draw interaction and
            // removes interaction from map. Clears interaction variables.
            function clearActiveDrawInteraction() {
              if (!activeDrawInteraction) return;
              activeDrawInteraction.un('drawend', handleDrawEnd);
              ctrl.map.removeInteraction(activeDrawInteraction);
              activeDrawInteraction = null;
              ctrl.activeDrawType = null;
            }

            // Add new draw interaction based on given geometry type name.
            // Also add event handler that fires when drawing ends.
            ctrl.setActiveDrawInteraction = function(geometryType) {
              clearActiveDrawInteraction();
              activeDrawInteraction = new ol.interaction.Draw({
                source: source,
                type: geometryType
              });
              activeDrawInteraction.on('drawend', handleDrawEnd);
              ctrl.map.addInteraction(activeDrawInteraction);
              ctrl.activeDrawType = geometryType;
            }

            // Add modify interaction (always active when not readOnly)
            if (!ctrl.readOnly) {
              var modifyInteraction = new ol.interaction.Modify({
                features: source.getFeaturesCollection()
              });
              modifyInteraction.on('modifyend', handleModifyEnd);
              ctrl.map.addInteraction(modifyInteraction);
            }

            // output for editor (equals input by default)
            ctrl.outputPolygonXml = surroundGmlWithGmdPolygon(ctrl.polygonXml);

            // projection list
            ctrl.projections = gnMap.getMapConfig().projectionList;
            ctrl.currentProjection = ctrl.projections[0].code;
            ctrl.dataProjection = 'EPSG:4326';

            // available input formats
            ctrl.formats = ['WKT', 'GeoJSON', 'GML'];
            ctrl.currentFormat = ctrl.formats[0];

            function isProjAvailable(code) {
              for (var i = 0; i < ctrl.projections.length; i++) {
                if (ctrl.projections[i].code === code) {
                  return true;
                }
              }
              return false;
            }

            /**
             * Calculates an enlarged extent for the given feature.
             * For polygons and lines, the extent will be twice the size of its smallest dimension.
             * For point features, the extent will be 1 square decimal degree, centered around the point.
             * If needed, the LL point extent will be reprojected to fit the map projection.
             *
             * @param {ol.feature} feature  Input feature for which to return an enlarged extent
             * @returns {Array.<number>}    Enlarged extent
             */
            function getEnlargedExtent(feature) {

              var extent = feature.getGeometry().getExtent();

              // Get buffer distance of 50% of the smallest extent dimension (width or height)
              var buffer = Math.min.apply(null, ol.extent.getSize(extent)) * 0.5;

              if (buffer > 0) {
                // Feature has a size: apply calculated buffer
                return ol.extent.buffer(extent, buffer);
              } else {
                // Feature probably is a point, causing the extent to have 0 width and height:
                // Get the extent center (= point), reproject to LL, buffer by 0.5 dd,
                // reproject new extent back to map projection and return it
                var mapProj = ctrl.map.getView().getProjection();
                var center = ol.proj.toLonLat(ol.extent.getCenter(extent), mapProj);
                extent = ol.extent.buffer(ol.extent.boundingExtent([center, center]), 0.5);
                return ol.proj.transformExtent(extent, 'EPSG:4326', mapProj, 8);
              }
            }

            // parse initial input coordinates to display shape
            ctrl.initValue = function() {
              if (ctrl.polygonXml) {
                var srsName = ctrl.polygonXml.match(
                    new RegExp('srsName=\"([^"]*)\"'));
                ctrl.dataProjection = srsName && srsName.length === 2 ?
                    srsName[1] : 'EPSG:4326';

                ctrl.dataOlProjection = ol.proj.get(ctrl.dataProjection)

                if(ctrl.dataOlProjection) {
                  if (!isProjAvailable(ctrl.dataProjection)) {
                    ctrl.projections.push({
                      code: ctrl.dataProjection,
                      label: ctrl.dataProjection
                    });
                  }

                  ctrl.currentProjection = ctrl.dataProjection;

                  // parse first feature from source XML & set geometry name
                  try {
                    var geometry = gnGeometryService.parseGeometryInput(
                      ctrl.map,
                      ctrl.polygonXml,
                      {
                        crs: ctrl.currentProjection,
                        format: 'gml'
                      }
                    );
                  } catch (e) {
                    console.warn('Could not parse geometry');
                    console.warn(e);
                  }

                  if (!geometry) {
                    console.warn('Could not parse geometry from extent polygon');
                    return;
                  }

                  var feature = new ol.Feature({
                    geometry: geometry
                  });

                  // add to map
                  source.clear();
                  source.addFeature(feature);
                }
                ctrl.updateOutput(feature, true);
              }
            };

            // update output with gml
            ctrl.updateOutput = function(feature, forceFitView) {

              if (!feature) return;

              // fit view if geom is valid & not empty
              if ((forceFitView || ctrl.fromTextInput) && feature.getGeometry() &&
                  !ol.extent.isEmpty(feature.getGeometry().getExtent())) {
                ctrl.map.getView().fit(getEnlargedExtent(feature), ctrl.map.getSize());
              }

              var outputCrs = $attrs['outputCrs'] ? $attrs['outputCrs'] :
                  ctrl.currentProjection;

              ctrl.dataOlProjection = ol.proj.get(outputCrs);

              // print output (skip if readonly)
              if (!ctrl.readOnly) {
                // GML 3.2.1 is used for ISO19139:2007
                // TODO: ISO19115-3:2018
                ctrl.outputPolygonXml = surroundGmlWithGmdPolygon(gnGeometryService.printGeometryOutput(
                  ctrl.map,
                  feature,
                  {
                    crs: outputCrs,
                    format: 'gml'
                  }
                ).replace(
                  /http:\/\/www.opengis.net\/gml"/g,
                  'http://www.opengis.net/gml/3.2"'))
              }

              // update text field (unless geometry was entered manually)
              if (!ctrl.fromTextInput) {
                ctrl.updateInputTextFromGeometry(feature);
              }
            };

            // this will receive errors from the geometry tool input parsing
            ctrl.parseError = null;
            ctrl.fromTextInput = false;

            // handle input change & outputs gml for the editor
            ctrl.handleInputChange = function() {
              if (!ctrl.inputGeometry) {
                return;
              }
              ctrl.parseError = null;
              ctrl.fromTextInput = true;

              // parse geometry
              try {
                var geometry = gnGeometryService.parseGeometryInput(
                    ctrl.map,
                    ctrl.inputGeometry,
                    {
                      crs: ctrl.currentProjection,
                      format: ctrl.currentFormat
                    }
                    );
              } catch (e) {
                ctrl.parseError = e.message;
              }

              // create a new feature & print the GML
              var feature = new ol.Feature({
                geometry: geometry
              });
              source.clear();
              source.addFeature(feature);
              ctrl.updateOutput(feature);
            };

            // options (proj, format) change:
            // either update text or try a new parse
            ctrl.handleInputOptionsChange = function() {
              if (ctrl.fromTextInput) {
                ctrl.handleInputChange();
              } else {
                ctrl.updateInputTextFromGeometry();
                // Update output if projection change
                ctrl.updateOutput(source.getFeatures()[0]);
              }
            };

            // update text in input according to displayed feature
            ctrl.updateInputTextFromGeometry = function(feature) {
              var feature = feature || source.getFeatures()[0];
              if (!feature) {
                ctrl.inputGeometry = '';
                return;
              }

              ctrl.inputGeometry = gnGeometryService.printGeometryOutput(
                  ctrl.map,
                  feature,
                  {
                    crs: ctrl.currentProjection,
                    format: ctrl.currentFormat
                  }
                  );
            };

            function surroundGmlWithGmdPolygon(gmlString) {
              return '<gmd:polygon xmlns:gmd="http://www.isotc211.org/2005/gmd">' +
                 gmlString +
                '</gmd:polygon>';

            }
          }
        ]
      };
    }]);
})();
