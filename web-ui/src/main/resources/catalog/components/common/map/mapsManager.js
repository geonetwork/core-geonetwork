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
    'gn_ows',
    'ngeo'
  ]);

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
    function($q, gnMap, gnOwsContextService) {
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
            controls: type !== this.EDITOR_MAP ? [] : [
              new ol.control.Zoom()
            ]
          });

          // no config found: return empty map
          if (!config) {
            console.warn('Map config not found for type \'' + type + '\'');
            return map;
          }

          // config found: load context if any, and apply extent & layers
          // (this is done through a promise anyway)
          var promise = $q(function(resolve, reject) {
            if (config.context) {
              gnOwsContextService.loadContextFromUrl(config.context, map)
                  .then(function() {
                    resolve();
                  });
            } else {
              // force async resolution
              setTimeout(resolve, 0);
            }
          }).then(function() {
            // do a render of the map
            map.renderSync();

            // extent
            if (config.extent && ol.extent.getWidth(config.extent) &&
                ol.extent.getHeight(config.extent) && map.getSize()) {
              map.getView().fit(config.extent, map.getSize());
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
          });

          // save the promise on the map
          map.set('creationPromise', promise);

          return map;
        }
      };
    }
  ]);
})();
