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

(function () {
  goog.provide("gn_geometry_directive");

  var module = angular.module("gn_geometry_directive", []);

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
  module.directive("gnGeometryTool", [
    function () {
      return {
        restrict: "E",
        scope: {
          map: "<",
          geometryType: "@",
          output: "=",
          outputFormat: "@",
          outputCrs: "@",
          allowReset: "@",
          allowModify: "@",
          outputAsFeatures: "@",
          input: "=",
          inputFormat: "@",
          inputCrs: "@",
          inputErrorHandler: "=",
          nameType: "@",
          activeGeometryTool: "="
        },
        templateUrl:
          "../../catalog/components/viewer/geometry/partials/geometrytool.html",
        controllerAs: "ctrl",
        bindToController: true,
        controller: [
          "$scope",
          "$attrs",
          "olDecorateInteraction",
          "gnGeometryService",
          function GeometryToolController(
            $scope,
            $attrs,
            olDecorateInteraction,
            gnGeometryService
          ) {
            var ctrl = this;

            this.$onInit = function () {
              var layer = gnGeometryService.getCommonLayer(ctrl.map, ctrl.nameType);
              ctrl.source = layer.getSource();
              ctrl.features = new ol.Collection();
              ctrl.geomIsMulti = ctrl.geometryType.contains("Multi");

              ctrl.drawInteraction = new ol.interaction.Draw({
                type: ctrl.geometryType,
                source: ctrl.source
              });
              ctrl.modifyInteraction = new ol.interaction.Modify({
                features: ctrl.features
              });

              // this is used to deactivate zoom on draw end
              ctrl.zoomInteraction = null;
              ctrl.map.getInteractions().forEach(function (interaction) {
                if (interaction instanceof ol.interaction.DoubleClickZoom) {
                  ctrl.zoomInteraction = interaction;
                }
              });

              // add our layer&interactions to the map
              ctrl.map.addInteraction(ctrl.drawInteraction);
              ctrl.map.addInteraction(ctrl.modifyInteraction);
              ctrl.drawInteraction.setActive(false);
              ctrl.modifyInteraction.setActive(false);
              olDecorateInteraction(ctrl.drawInteraction);
              olDecorateInteraction(ctrl.modifyInteraction);

              ctrl.drawInteraction.on("drawstart", function () {
                angular.forEach(ctrl.map.getInteractions(), function (interaction) {
                  if (
                    interaction instanceof ol.interaction.Draw &&
                    ctrl.drawInteraction != interaction
                  ) {
                    interaction.setActive(false);
                  }
                });
              });

              ctrl.drawInteraction.on("modifystart", function () {
                angular.forEach(ctrl.map.getInteractions(), function (interaction) {
                  if (
                    interaction instanceof ol.interaction.Modify &&
                    ctrl.modifyInteraction != interaction
                  ) {
                    interaction.setActive(false);
                  }
                });
              });

              // clear existing features on draw end & save feature
              ctrl.drawInteraction.on("drawend", function (event) {
                if (!ctrl.geomIsMulti) {
                  removeMyFeatures();
                  ctrl.drawInteraction.active = false;
                }
                // for multi geom, we append the feature to the existing one.
                else {
                  var geom = gnGeometryService.appendToMultiGeometry(
                    event.feature.getGeometry(),
                    ctrl.features.getArray()
                  );
                  event.feature.setGeometry(geom);
                }
                ctrl.features.push(event.feature);

                updateOutput(event.feature);

                // prevent interference by zoom interaction
                // see https://github.com/openlayers/openlayers/issues/3610
                if (ctrl.zoomInteraction) {
                  ctrl.zoomInteraction.setActive(false);
                  setTimeout(function () {
                    ctrl.zoomInteraction.setActive(true);
                  }, 251);
                }

                // prevent interference from GFI
                ctrl.map.set("disable-gfi", true);
                setTimeout(function () {
                  ctrl.map.set("disable-gfi", false);
                }, 1000);
              });

              // update output on modify end
              ctrl.modifyInteraction.on("modifyend", function (event) {
                updateOutput(event.features.item(0), true);
              });

              ctrl.drawInteraction.on("change:active", setActiveGeometryTool);
              ctrl.modifyInteraction.on("change:active", setActiveGeometryTool);

              $scope.$watchCollection("ctrl.activeGeometryTool", function (n) {
                if (n && n.current !== $scope.$id) {
                  ctrl.drawInteraction.setActive(false);
                  ctrl.modifyInteraction.setActive(false);
                } else {
                  ctrl.drawInteraction.setActive(true);
                }
              });

              $scope.$watch(function () {
                return ctrl.input + ctrl.inputCrs + ctrl.inputFormat;
              }, handleInputUpdate);
            };

            // remove all my features from the map
            function removeMyFeatures() {
              ctrl.source.clear();
              ctrl.features.clear();
            }

            // cleanup when scope is destroyed
            $scope.$on("$destroy", function () {
              removeMyFeatures();
              ctrl.map.removeInteraction(ctrl.drawInteraction);
              ctrl.map.removeInteraction(ctrl.modifyInteraction);
            });

            // modifies the output value
            function updateOutput(feature, isNew) {
              // no feature: clear output
              if (!feature) {
                ctrl.output = null;
                return;
              }

              ctrl.output = gnGeometryService.printGeometryOutput(ctrl.map, feature, {
                crs: ctrl.outputCrs,
                format: ctrl.outputFormat,
                outputAsWFSFeaturesCollection: ctrl.outputAsFeatures
                // TODO: make sure this works everytime?
              });

              // When adding a geom to an existing feature we must update the layer
              if (ctrl.geomIsMulti && !isNew) {
                ctrl.source.refresh();
              }
            }

            // reset drawing
            ctrl.reset = function () {
              removeMyFeatures();
              updateOutput();
            };

            // watch parameter changes
            function handleInputUpdate() {
              if (!ctrl.input) {
                return;
              }

              // parse geometry from text
              try {
                var geometry = gnGeometryService.parseGeometryInput(
                  ctrl.map,
                  ctrl.input,
                  {
                    crs: ctrl.inputCrs,
                    format: ctrl.inputFormat
                  }
                );

                // clear features & add a new one
                removeMyFeatures();
                var feature = new ol.Feature({
                  geometry: geometry
                });
                feature.setId("geometry-tool-output");
                source.addFeature(feature);
                ctrl.features.push(feature);
              } catch (e) {
                // send back error
                if (ctrl.inputErrorHandler) {
                  ctrl.inputErrorHandler(e.message);
                }
              }
            }

            // Only one draw interaction at a time
            function setActiveGeometryTool(e) {
              if (
                (ctrl.activeGeometryTool.current === undefined ||
                  ctrl.activeGeometryTool.current !== $scope.$id) &&
                e.oldValue === false
              ) {
                $scope.ctrl.activeGeometryTool.current = $scope.$id;
              }
            }
          }
        ]
      };
    }
  ]);
})();
