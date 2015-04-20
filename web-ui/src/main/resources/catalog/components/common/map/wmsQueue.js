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
      for (var i = 0; i < queue.length; i++) {
        var o = queue[i];
        if (o.name == layer.name && o.url == layer.url) {
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
     */
    this.add = function(url, name) {
      queue.push({
        url: url,
        name: name
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
     */
    this.isPending = function(url, name) {
      var layer = {
        url: url,
        name: name
      };
      return getLayerIndex(queue, layer) >= 0;
    };

  }]);

})();
