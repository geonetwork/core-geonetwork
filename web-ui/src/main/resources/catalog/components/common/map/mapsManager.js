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
  goog.provide('gn_maps_manager');

  goog.require('gn_ows');


  var module = angular.module('gn_maps_manager', [
    'gn_ows'
  ]);

  var configProjections = function(projectionConfig) {
    $.each(projectionConfig, function(i, p) {
      if (!ol.proj.get(p.code)) {
        if (p.def && p.code) {
          // Define an OL3 projection based on the included Proj4js projection
          try {
            // definition and set it's extent.
            proj4.defs(p.code, p.def);
            ol.proj.proj4.register(proj4);
          } catch (e) {
            console.error("Trying to use incorrectly defined projection '" + p.code +
              "'. Please update definitions on Admin > Settings.");
          }
        } else {
          console.error("Trying to use unknown projection '" + p.code +
              "'. Please update definitions on Admin > Settings.");
        }
      }

      var projection = ol.proj.get(p.code);
      if (projection) {
        if (p.extent && p.extent.length && p.extent.length === 4 &&
          !isNaN(p.extent[0]) && p.extent[0] != null &&
          !isNaN(p.extent[1]) && p.extent[1] != null &&
          !isNaN(p.extent[2]) && p.extent[2] != null &&
          !isNaN(p.extent[3]) && p.extent[3] != null) {
          projection.setExtent(p.extent);
        }
        if (p.worldExtent && p.worldExtent.length && p.worldExtent.length === 4 &&
          !isNaN(p.worldExtent[0]) && p.worldExtent[0] != null &&
          !isNaN(p.worldExtent[1]) && p.worldExtent[1] != null &&
          !isNaN(p.worldExtent[2]) && p.worldExtent[2] != null &&
          !isNaN(p.worldExtent[3]) && p.worldExtent[3] != null) {
          projection.setWorldExtent(p.worldExtent);
        }
      }
    });
  };

  module.config(['gnViewerSettings', function(gnViewerSettings) {
    configProjections(gnViewerSettings.mapConfig.switcherProjectionList);
  }]);

  /**
   * @ngdoc service
   * @kind function
   * @name gn_map.service:gnMapsManager
   *
   * @description
   * The `gnMapsManager` service is used to create maps throughout the
   * application in a standardized way.
   */
  module.service('gnMapsManager', [
    '$q',
    'gnMap',
    'gnOwsContextService',
    'gnViewerSettings',
    '$rootScope',
    '$location',
    'gnSearchLocation',
    function($q, gnMap, gnOwsContextService, gnViewerSettings, $rootScope,
             $location, gnSearchLocation) {

      var mapParams = {};
      if (gnSearchLocation.isMap()) {
        mapParams = $location.search();
      }

      return {
        /**
         * These are types used when creating a new map with createMap
         * The keys are used in the UI config, so that config.map.<KEY>
         * points to a description of the map context & layers.
         */
        VIEWER_MAP: 'viewer',
        SEARCH_MAP: 'search',
        EDITOR_MAP: 'editor',

        /**
         * @ngdoc method
         * @methodOf gn_map.service:gnMapsManager
         * @name gnMap#initProjections
         *
         * @description
         * Initialises the projections in OpenLayers. For each provided projection,
         * adds the definition to OL.
         *
         * @param {array} list of projection definitions (p.code,p.def,p.extent,p.worldExtent):
        **/

        initProjections: configProjections,

        /**
         * @ngdoc method
         * @methodOf gn_map.service:gnMapsManager
         * @name gnMap#createMap
         *
         * @description
         * Creates a new map according to current UI config.
         * The map will be created using a context (if specified) as well as
         * layers above it and an extent.
         * The corresponding map description must be an object like so:
         * {
         *   context: {string} optional, path to a XML file,
         *   extent: {ol.Extent} optional map extent,
         *   layers: {Array.<Object>} optional layers array: each layer must
         *     be an object compatible with createLayerFromProperties
         * }
         * First, the context is applied, then (if defined) extent & layers
         * The creation promise is available on the map with
         * map.get('creationPromise')
         * TODO: This method must become the standardized way of creating maps
         * throughout the application.
         *
         * @param {string} type of map: gnMapsManager.VIEWER_MAP, SEARCH_MAP
         * or EDITOR_MAP
         *
         * @return {ol.Map} map created with the correct parameters
         */
        createMap: function(type) {
          var config = gnMap.getMapConfig()['map-' + type];
          var map = new ol.Map({
            layers: [],
            view: new ol.View({
              center: [0, 0],
              projection: gnMap.getMapConfig().projection,
              zoom: 2
            }),
            // show zoom control in editor maps only
            controls: type !== this.EDITOR_MAP ? [new ol.control.Attribution()] : [
              new ol.control.Zoom(),
              new ol.control.Attribution()
            ]
          });

          var defer = $q.defer();
          var sizeLoadPromise = defer.promise;
          map.set('sizePromise', sizeLoadPromise);

          // This is done to have no delay for the map size $watch
          var unBindFn = $rootScope.$on('$locationChangeSuccess', function() {
            setTimeout(function() {
              $rootScope.$apply();
            });
          }.bind(this));

          var unWatchFn = $rootScope.$watch(function() {
            return map.getTargetElement() && Math.min(
                map.getTargetElement().offsetWidth,
                map.getTargetElement().offsetHeight
            );
          }, function(size) {
            if (size > 0) {
              map.updateSize();
              map.renderSync();
              defer.resolve();
              unWatchFn();
              unBindFn();
            }
          });

          // no config found: return empty map
          if (!config) {
            console.warn('Map config not found for type \'' + type + '\'');
            return map;
          }

          // config found: load context if any, and apply extent & layers
          // (this is done through a promise anyway)
          var mapReady;
          var urlContext;
          if (type === this.VIEWER_MAP) {

            $rootScope.$on('$locationChangeSuccess', function() {
              if (gnSearchLocation.isMap()) {
                var params = $location.search();
                var newContext = params.owscontext || params.map;
                if (newContext && newContext !== urlContext) {
                  urlContext = newContext;
                  gnOwsContextService.loadContextFromUrl(
                      urlContext, map);
                }
              }
            }.bind(this));

            urlContext = mapParams.owscontext || mapParams.map;
            if (urlContext) {
              mapReady = gnOwsContextService.loadContextFromUrl(
                  urlContext, map);
            } else {
              var storage = gnMap.getMapConfig().storage;
              if (storage) {
                storage = window[storage];
                var key = 'owsContext_' +
                    window.location.host + window.location.pathname;
                var context = storage.getItem(key);
                if (context) {
                  gnOwsContextService.loadContext(context, map);
                  mapReady = true;
                }
              }
            }
          }
          if (!mapReady) {
            if (config.context) {
              mapReady = gnOwsContextService.loadContextFromUrl(
                  config.context, map);
            }
          }
          var creationPromise = $q.when(mapReady).then(function() {

            // extent
            if (config.extent && ol.extent.getWidth(config.extent) &&
                ol.extent.getHeight(config.extent)) {
              if (type !== this.SEARCH_MAP) {
                // Because search map is fit by result md bbox
                var newPromise = map.get('sizePromise').then(function () {
                  map.getView().fit(config.extent, map.getSize(), {nearest: true});
                });

                map.set('sizePromise', newPromise);
              }
            }

            // layers
            if (config.layers && config.layers.length) {
              config.layers.forEach(function(layerInfo) {
                gnMap.createLayerFromProperties(layerInfo, map)
                    .then(function(layer) {
                      if (layer) {
                        map.addLayer(layer);
                      }
                    });
              });
            }
            if (type === this.VIEWER_MAP) {
              if (mapParams.wmsurl && mapParams.layername) {
                gnMap.addWmsFromScratch(map, mapParams.wmsurl,
                    mapParams.layername, true).

                    then(function(layer) {
                      layer.set('group', mapParams.layergroup);
                      map.addLayer(layer);
                    });
              }
            }
          }.bind(this));

          // save the promise on the map
          map.set('creationPromise', creationPromise);

          return map;
        }
      };
    }
  ]);
})();
