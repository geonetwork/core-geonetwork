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
          'olDecorateInteraction',
          'gnGeometryService',
          function BoundingPolygonController(
              $scope,
              $attrs,
              $http,
              gnMap,
              gnMapsManager,
              olDecorateInteraction,
              gnGeometryService) {
            var ctrl = this;

            // set read only
            ctrl.readOnly = $scope.$eval($attrs['readOnly']);

            // init map
            ctrl.map = gnMapsManager.createMap(gnMapsManager.EDITOR_MAP);
            ctrl.map.get('sizePromise').then(function() {
              ctrl.initValue();
            });

            // interactions with map
            var layer = gnGeometryService.getCommonLayer(ctrl.map);
            var source = layer.getSource();

            ctrl.drawInteraction = new ol.interaction.Draw({
              type: 'MultiPolygon',
              source: source
            });
            ctrl.drawLineInteraction = new ol.interaction.Draw({
              type: 'LineString',
              source: source
            });
            ctrl.modifyInteraction = new ol.interaction.Modify({
              features: source.getFeaturesCollection()
            });

            // this is used to deactivate zoom on draw end
            ctrl.zoomInteraction = null;
            ctrl.map.getInteractions().forEach(function(interaction) {
              if (interaction instanceof ol.interaction.DoubleClickZoom) {
                ctrl.zoomInteraction = interaction;
              }
            });

            // add our layer&interactions to the map
            olDecorateInteraction(ctrl.drawInteraction);
            olDecorateInteraction(ctrl.drawLineInteraction);
            olDecorateInteraction(ctrl.modifyInteraction);
            ctrl.drawInteraction.active = false;
            ctrl.drawLineInteraction.active = false;
            ctrl.modifyInteraction.active = false;

            // add interactions to map
            ctrl.map.addInteraction(ctrl.drawInteraction);
            ctrl.map.addInteraction(ctrl.drawLineInteraction);
            ctrl.map.addInteraction(ctrl.modifyInteraction);

            // clear existing features on draw end & save feature
            function handleDrawEnd(event) {
              ctrl.fromTextInput = false;
              source.clear(event.feature);
              ctrl.updateOutput(event.feature);
              ctrl.drawInteraction.active = false;
              ctrl.drawLineInteraction.active = false;
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
            ctrl.drawInteraction.on('drawend', handleDrawEnd);
            ctrl.drawLineInteraction.on('drawend', handleDrawEnd);

            // update output on modify end
            ctrl.modifyInteraction.on('modifyend', function(event) {
              ctrl.fromTextInput = false;
              ctrl.updateOutput(event.features.item(0));
              $scope.$digest();
            });

            // output for editor (equals input by default)
            ctrl.outputPolygonXml = ctrl.polygonXml;

            // projection list
            ctrl.projections = gnMap.getMapConfig().projectionList;
            ctrl.currentProjection = ctrl.projections[0].code;
            ctrl.dataProjection = 'EPSG:4326';

            // available input formats
            // GML is not available as it cannot be parsed
            // without namespace info
            ctrl.formats = ['WKT', 'GeoJSON', 'GML'];
            ctrl.currentFormat = ctrl.formats[0];

            function isProjAvailable(code) {
              for (var i = 0; i < ctrl.projections.length; i++) {
                if (ctrl.projections[i].code === code) {
                  return true;
                }
              }
              return false;
            };

            // parse initial input coordinates to display shape
            ctrl.initValue = function() {
              if (ctrl.polygonXml) {
                var srsName = ctrl.polygonXml.match(
                    new RegExp('srsName=\"([^"]*)\"'));
                ctrl.dataProjection = srsName && srsName.length === 2 ?
                    srsName[1] : 'EPSG:4326';

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

                ctrl.updateOutput(feature, true);
              }
            };

            // update output with gml
            ctrl.updateOutput = function(feature, forceFitView) {
              // fit view if geom is valid & not empty
              if ((forceFitView || ctrl.fromTextInput) &&
                  feature.getGeometry() &&
                  !ol.extent.isEmpty(feature.getGeometry().getExtent())) {
                ctrl.map.getView().fit(feature.getGeometry(),
                    ctrl.map.getSize());
              }

              var outputCrs = $attrs['outputCrs'] ? $attrs['outputCrs'] :
                  ctrl.currentProjection;

              // print output (skip if readonly)
              if (!ctrl.readOnly) {
                // GML 3.2.1 is used for ISO19139:2007
                // TODO: ISO19115-3:2018
                ctrl.outputPolygonXml =
                    '<gmd:polygon xmlns:gmd="http://www.isotc211.org/2005/gmd">' +
                    gnGeometryService.printGeometryOutput(
                    ctrl.map,
                    feature,
                    {
                      crs: outputCrs,
                      format: 'gml'
                    }
                    ).replace(
                      /http:\/\/www.opengis.net\/gml"/g,
                      'http://www.opengis.net/gml/3.2"') +
                    '</gmd:polygon>';
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
          }
        ]
      };
    }]);
})();
