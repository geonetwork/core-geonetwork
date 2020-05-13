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
  goog.provide('gn_map_wmsqueue');

  var module = angular.module('gn_map_wmsqueue', []);

  /**
   * Manage a queuing service for wms/wmts layer that are added to map.
   * Usually, a layer adding call a getCapabilities, and then add the layer
   * to map.
   * This service have a queue array for pending layers, and errors array
   * for layer for which the getCapabilities failed
   */
  module.service('gnWmsQueue', [function() {

    // wms pending layers list
    var queue = [];

    // wms for which getCapabilities failed
    var errors = [];

    this.queue = queue;
    this.errors = errors;

    var getLayerIndex = function(a, layer) {
      var idx = -1;
      for (var i = 0; i < a.length; i++) {
        var o = a[i];
        if (o.name == layer.name && o.style == layer.style
          && o.url == layer.url && o.map == layer.map) {
          idx = i;
        }
      }
      return idx;
    };

    var removeFromArray = function(a, layer) {
      a.splice(getLayerIndex(a, layer), 1);
    };

    /**
     * Add the layer to the queue
     * @param {string} url
     * @param {string} name
     * @param {ol.Map} map
     */
    this.add = function(url, name, style, map) {
      queue.push({
        url: url,
        name: name,
        map: map,
        style: style
      });
    };

    this.removeFromQueue = function(layer) {
      removeFromArray(queue, layer);
    };

    /**
     * Remove the layer from the queue and add it to errors list.
     * Usually when the getCapabilities failed
     * @param {Object} layer contains
     *  url - name - msg
     */
    this.error = function(layer) {
      this.removeFromQueue(layer);
      if (getLayerIndex(errors, layer) < 0) {
        errors.push(layer);
      }
    };

    this.removeFromError = function(layer) {
      removeFromArray(errors, layer);
    };

    /**
     *
     * @param {string} url
     * @param {string} name
     * @param {ol.Map} map
     */
    this.isPending = function(url, name, style, map) {
      var layer = {
        url: url,
        name: name,
        map: map,
        style: style
      };
      return getLayerIndex(queue, layer) >= 0;
    };

  }]);

})();
