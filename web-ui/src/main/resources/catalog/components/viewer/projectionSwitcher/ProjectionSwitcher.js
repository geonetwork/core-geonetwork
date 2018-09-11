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
  goog.provide('gn_projectionswitcher');

  var module = angular.module('gn_projectionswitcher', [
  ]);


  /**
   * @ngdoc directive
   * @name gn_projectionswitcher.directive:gnProjectionSwitcher
   * 
   * @description The `gnProjectionSwitcher` directive adds a button to the map
   *              that allows you to change the projection.
   */
  module.directive('gnProjectionSwitcher', [
    'gnViewerSettings',
    function(gnViewerSettings) {
      return {
        restrict: 'A',
        templateUrl: '../../catalog/components/viewer/projectionSwitcher/' +
            'partials/projectionSwitcher.html',
        scope: {
          map: '=gnProjectionSwitcher'
        },
        controllerAs: 'gnProjectionSwitcherCtrl',
        controller: ['$scope', 'gnViewerSettings', 'gnMap', '$rootScope', 
          function(scope, gnViewerSettings, gnMap, $rootScope) {

          scope.changeLayerProjection = function(
            layer, oldProj, newProj) {
            if (layer instanceof ol.layer.Group) {
              layer.getLayers().forEach(
                  function(subLayer) {
                    this.changeLayerProjection(
                      subLayer, oldProj,
                      newProj);
                  });
            } else if (layer instanceof ol.layer.Tile) {
              var tileLoadFunc = layer.getSource()
                .getTileLoadFunction();
              layer.getSource().setTileLoadFunction(
                tileLoadFunc);
            } else if (layer instanceof ol.layer.Vector) {
              var features = layer.getSource().getFeatures();
              for (var i = 0; i < features.length; i += 1) {
                features[i].getGeometry().transform(
                  oldProj, newProj);
              }
            }
          };
          
          scope.switchProjection = function(projection) {
              var view = scope.map.getView();
              var oldProj = view.getProjection();
              var newProj = ol.proj.get(projection);
              
              if(oldProj == newProj) {
                return;
              }
              
              var projectionConfig = {};
              
              $.each(gnViewerSettings.mapConfig.switcherProjectionList, function(i, config) {
                if(config['code'] == projection) {
                  projectionConfig = config;
                }
              });
              
              var newExtent = ol.proj.transformExtent(
                  view.calculateExtent(scope.map.getSize()), oldProj, newProj);
              
              if(newProj.getExtent()) {
                newExtent = newProj.getExtent();
              }
              
              var newCenter = ol.proj.transform(scope.map.getView().getCenter(), oldProj, newProj);
              
              if(projectionConfig.center) {
                newCenter = JSON.parse(projectionConfig.center);
              }

              var mapsConfig = {
                projection: newProj,
                extent: newExtent,
                center: newCenter
              };
              
              if (projectionConfig.resolutions) {
                angular.extend(mapsConfig, {resolutions: JSON.parse(projectionConfig.resolutions)})
              }

              var newView = new ol.View(mapsConfig);

              // Set the view
              scope.map.setView(newView);

              // Rearrange base layers to adapt (if possible) to new projection
              var layersToRemove = [];

              scope.map.getLayers().forEach(function(layer) {
                if (layer.get("group") == 'Background layers' 
                  || !layer.displayInLayerManager) {
                  layersToRemove.push(layer);
                }
              });
              
              for (var i = 0; i < layersToRemove.length; i++) {
                scope.map.removeLayer(layersToRemove[i]);
              }


              // Renew base layers
              gnViewerSettings.bgLayers = [];
              var layers = gnViewerSettings.mapConfig['map-viewer'].layers;
              if (layers && layers.length) {
                layers.forEach(function(layerInfo) {
                  gnMap.createLayerFromProperties(layerInfo, scope.map)
                    .then(function(layer) {
                      if (layer) {
                        layer.displayInLayerManager = false;
                        layer.set("group", "Background layers");
                        gnViewerSettings.bgLayers.push(layer);
                      }
                    });
                });
              }

              // Reproject layers
              scope.map.getLayers().forEach(function(layer) {
                scope.changeLayerProjection(layer, oldProj, newProj);
              });

              // Reproject controls
              scope.map.getControls().forEach(function(control) {
                if (typeof control.setProjection === "function") {
                  control.setProjection(newProj);
                }
              });
              
              scope.map.getView().fit(newExtent, scope.map.getSize());

          };
        
          
        }],
        link: function(scope, element, attrs) {
          scope.projections = gnViewerSettings.mapConfig.switcherProjectionList;
        }
      };
    }]);

})();
