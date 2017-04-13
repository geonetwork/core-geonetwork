(function() {
  goog.provide('sxt_panier_service');

  var module = angular.module('sxt_panier_service', [
  ]);


  module.service('sxtPanierService', [
    '$http', 'wfsFilterService',
    function($http, wfsFilterService) {

      var panierUrl = 'extractor.doExtract';

      var callExtractService = function(panier) {
        // this object is temporary and will be sent to the extractor
        var panierData = {
          user: panier.user,
          layers: []
        };

        panier.layers.forEach(function(l) {
          var panierLayer = {};
          panierData.layers.push(panierLayer);

          // copying values from original object
          panierLayer.id = l.id;
          panierLayer.input = l.input;
          panierLayer.output = l.output;
          panierLayer.additionalInput = [];

          // include selected WPS processes as additionalInputs
          if (l._element && l._element.processes) {
            l._element.processes.forEach(function (process, index) {
              if (process && process.included) {
                // params object with existing WPS inputs
                var params = {};
                if (process.processDescription) {
                  var inputs = process.processDescription.dataInputs.input;
                  for (var i = 0; i < inputs.length; i++) {
                    var input = inputs[i];
                    params[input.identifier.value] = input.value;
                  }
                }

                // serializing params
                var paramString = '';
                var keys = Object.keys(params);
                for (var i = 0; i < keys.length; i++) {
                  paramString += keys[i] + '=' + params[keys[i]] + '&';
                }

                // final additionalInput object
                panierLayer.additionalInput.push({
                  protocol: 'WPS',
                  linkage: process.url,
                  params: paramString
                });
              }
            });
          }

          // fetch ElasticSearch; if not available, no filters are set
          var es = wfsFilterService.getEsObject(l.input.linkage, l.output.name);
          if(es && l.useFilters) {
            panierLayer.input.filter = wfsFilterService.toCQL(es);
          }
        });
        return $http({
          url: panierUrl,
          method: 'POST',
          data: panierData,
          headers: {'Content-Type': 'application/json'}
        })
      };

      this.extract = function(panier) {
        return callExtractService(panier);
      };

      /**
       * The basket contains only links. This method is used to bind downloads
       * object with respective layers.
       *
       * @param {Object} panier All layers to download.
       * @param {ol.Map} map viewer map.
       */
      this.bindPanierWithLayers = function(panier, map) {
        map.getLayers().forEach(function(layer){
          var downloads = layer.get('downloads');
          if(downloads) {

            // Find layers binded to download
            panier.forEach(function(panierItem) {
              if(downloads.some(function(d) {
                  var l = panierItem.link;
                  return d.url == l.url &&
                    d.name == l.name &&
                    d.protocol == l.protocol;
                })) {

                // 1. Check if a WFS filter is applied
                var esObject = layer.get('indexObject');
                if(esObject) {
                  var esConfig = esObject.getState();
                  var g = esConfig.geometry;
                  var filters = [];
                  angular.forEach(esConfig.qParams, function(obj, fName) {
                    if(Object.keys(obj.values).length) {
                      filters.push({
                        name: fName,
                        value: Object.keys(obj.values)[0]
                      });
                    }
                  });
                  panierItem.filter = {
                    params: filters.length ? filters : undefined,
                    any: esConfig.any
                  };
                  if(g) {
                    var extent = [g[0][0], g[1][1], g[1][0], g[0][1]];
                    panierItem.filter.extent = extent
                  }
                }

                // 2. Check if WPS is present
                var wps = layer.get('processes');
                wps.forEach(function(p) {
                  // TODO test synchrone
                  // TODO can we have loop ?
                  p.layer = layer;
                });
                panierItem.processes = wps;
              }
              else {
                panierItem.filter = null;
              }
            });
          }
        });
      };
    }
  ]);

})();
