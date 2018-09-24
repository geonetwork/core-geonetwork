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
  goog.provide('gn_projectionswitcher');

  var module = angular.module('gn_projectionswitcher', []);

  /**
   * @ngdoc directive
   * @name gn_projectionswitcher.directive:gnProjectionSwitcher
   * 
   * @description The `gnProjectionSwitcher` directive adds a button to the map
   *              that allows you to change the projection.
   */
  module
      .directive(
          'gnProjectionSwitcher',
          [
              'gnViewerSettings',
              function (gnViewerSettings) {
                return {
                  restrict : 'A',
                  templateUrl : '../../catalog/components/viewer/projectionSwitcher/'
                      + 'partials/projectionSwitcher.html',
                  scope : {
                    map : '=gnProjectionSwitcher'
                  },
                  controllerAs : 'gnProjectionSwitcherCtrl',
                  controller : [
                      '$scope',
                      'gnViewerSettings',
                      'gnMap',
                      '$rootScope',
                      function (scope, gnViewerSettings, gnMap, $rootScope) {
                        
                        scope.listOpen = false;
                        
                        scope.toggleList = function() {
                          scope.listOpen = !scope.listOpen;
                        }

                        // Change the projection of an existing layer
                        scope.changeLayerProjection = function (layer, oldProj,
                            newProj) {
                          if (layer instanceof ol.layer.Group) {
                            layer.getLayers().forEach(
                                function (subLayer) {
                                  this.changeLayerProjection(subLayer, oldProj,
                                      newProj);
                                });
                          } else if (layer instanceof ol.layer.Tile) {
                            var tileLoadFunc = layer.getSource()
                                .getTileLoadFunction();
                            layer.getSource().setTileLoadFunction(tileLoadFunc);
                          } else if (layer instanceof ol.layer.Vector) {
                            var features = layer.getSource().getFeatures();
                            for (var i = 0; i < features.length; i += 1) {
                              features[i].getGeometry().transform(oldProj,
                                  newProj);
                            }
                          }
                        };

                        // Main function to switch the projection
                        scope.switchProjection = function (projection) {
                          var view = scope.map.getView();
                          var oldProj = view.getProjection();
                          var newProj = ol.proj.get(projection);

                          if (oldProj.getCode() == newProj.getCode()) {
                            // There is no real change, don't do anything
                            return;
                          }

                          // First, get all the info to populate the map

                          var projectionConfig = {};

                          gnViewerSettings.mapConfig.switcherProjectionList
                              .forEach(function (config) {
                                if (config['code'] == projection) {
                                  projectionConfig = config;
                                }
                              });

                          var newExtent = ol.proj.transformExtent(view
                              .calculateExtent(scope.map.getSize()), oldProj,
                              newProj);

                          if (newProj.getExtent()) {
                            newExtent = newProj.getExtent();
                          }

                          var mapsConfig = {
                            projection : newProj,
                            extent : newExtent
                          };

                          if (projectionConfig.resolutions
                              && projectionConfig.resolutions.length
                              && projectionConfig.resolutions.length > 0) {
                            angular.extend(mapsConfig, {
                              resolutions : projectionConfig.resolutions
                            })
                          }

                          // Set the view
                          var newView = new ol.View(mapsConfig);
                          scope.map.setView(newView);

                          // Rearrange base layers to adapt (if possible) to new
                          // projection
                          var layersToRemove = [];
                          var bgLayers = [];

                          gnViewerSettings.bgLayers
                              .forEach(function (layer) {
                                // is this layer coming from original context?
                                // layers from original context should be kept
                                if (!layer.get("fromGNSettings")) {
                                  bgLayers.push(layer);
                                  // Remember current background, if possible
                                  layer.set("currentBackground", layer
                                      .getVisible());
                                }
                              });

                          // Loop over all background layers currently on the
                          // map
                          // to start from scratch
                          scope.map.getLayers().forEach(
                              function (layer) {
                                if (layer.get("group") == 'Background layers'
                                    || !layer.displayInLayerManager) {

                                  // is this layer coming from original context?
                                  // layers from original context should be kept
                                  if (layer.get("fromGNSettings")) {
                                    layersToRemove.push(layer);
                                  }
                                }
                              });

                          // Remove from map all base layers that don't belong
                          // to this projection
                          // different loop from previous one as it may break
                          // forEach
                          layersToRemove.forEach(function (layer) {
                            scope.map.removeLayer(layer);
                          });

                          // Renew base layers from settings
                          var layers = gnViewerSettings.mapConfig['map-viewer'].layers;
                          if (layers && layers.length) {
                            layers.forEach(function (layerInfo) {
                              gnMap.createLayerFromProperties(layerInfo,
                                  scope.map).then(function (layer) {
                                if (layer) {
                                  layer.displayInLayerManager = false;
                                  layer.set("group", "Background layers");
                                  layer.set("fromGNSettings", true);
                                  layer.set("currentBackground", false);
                                  bgLayers.push(layer);
                                }
                              });
                            });
                          }

                          // We have all the info, change the map

                          // Update Background Layers to trigger tool changes
                          gnViewerSettings.bgLayers = bgLayers;

                          // Reproject all layers in the map
                          scope.map.getLayers().forEach(
                              function (layer) {
                                scope.changeLayerProjection(layer, oldProj,
                                    newProj);
                              });

                          // Reproject all controls in the map
                          scope.map.getControls().forEach(function (control) {
                            if (typeof control.setProjection === "function") {
                              control.setProjection(newProj);
                            }
                          });

                          // Relocate map to extent
                          scope.map.getView().fit(newExtent,
                              scope.map.getSize());
                          
                          scope.listOpen = false;

                        };

                      } ],
                  link : function (scope, element, attrs) {
                    scope.projections = gnViewerSettings.mapConfig.switcherProjectionList;
                  }
                };
              } ]);

})();
