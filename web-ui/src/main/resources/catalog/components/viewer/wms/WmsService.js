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
  goog.provide('gn_wms_service');




  goog.require('gn_map_service');
  goog.require('gn_ows_service');
  goog.require('gn_urlutils_service');

  var module = angular.module('gn_wms_service', ['gn_map_service',
    'gn_ows_service', 'gn_urlutils_service']);

  module.service('gnWmsService', ['gnOwsCapabilities', '$q',
    'gnUrlUtils', 'gnMap',
    function(gnOwsCapabilities, $q, gnUrlUtils, gnMap) {
      /**
       * Do a getCapabilities request to the URL given in parameter.
       * @param {string} url WMS service URL.
       * @return {Promise} a promise that resolves into the parsed
       * capabilities document.
       */
      this.getCapabilities = function(url) {
        var defer = $q.defer();
        if (gnUrlUtils.isValid(url)) {

          gnOwsCapabilities.getWMSCapabilities(url).then(
              function(capabilities) {
                defer.resolve(capabilities);
              }, function(rejectedData) {
                defer.reject(rejectedData);
              });
        } else {
          defer.reject('invalid_url');
        }
        return defer.promise;
      };

      /**
       * Check if a layer name is present in the capabilities document.
       * @param {Object} capabilities the parsed capabilities document
       * (as returned by getCapabilities method)
       * @param {string} layers layers to check. It can include
       * multiple layer names separated by commas.
       * @return {boolean} true if all layers are present in the capabilities.
       */
      this.isLayerInCapabilities = function(capabilities, layers) {
        if (!capabilities || !layers) {
          return false;
        }

        var layersArray = layers.split(',');
        var capabilitiesLayers = capabilities.layers;
        var allLayersFound = capabilitiesLayers.length != 0;
        angular.forEach(layersArray, function(layerName, index) {
          var result = $.grep(capabilitiesLayers, function(capLayer) {
            return layerName === capLayer.Name;
          });
          allLayersFound = allLayersFound && result.length != 0;
        });

        return allLayersFound;
      };

      /**
       * Add a layer to the map.
       * @param {Object} layer a layer description from the
       * capabilities document.
       * @param {gnMap} map a GeoNetwork map where the layer will be added.
       */
      this.addLayerToMap = function(layer, map) {
        layer.capRequest = scope.capabilities.Request||null;
        gnMap.addWmsToMapFromCap(map, layer);
      };

      this.addWMSToMap = function(layerName, url, md, map) {
        if (layerName) {
          gnMap.addWmsFromScratch(map, url, layerName, false, md);
        }
      };


    }]);

})();
