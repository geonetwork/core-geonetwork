(function() {
  goog.provide('sxt_panier_service');

  var module = angular.module('sxt_panier_service', [
  ]);


  module.service('sxtPanierService', [
    '$http', 'wfsFilterService',
    function($http, wfsFilterService) {

      var panierUrl = 'extractor.doExtract';

      var callExtractService = function(panier) {
        panier.layers.forEach(function(l) {
          var es = wfsFilterService.getEsObject(l.input.linkage, l.output.name);
          if(!es) {
            console.error('ES object is null maybe because spec are different' +
              'between download and wfsfilter');
            return;
          }
          l.input.filter = wfsFilterService.toCQL(es);
        });
        return $http({
          url: panierUrl,
          method: 'POST',
          data: panier,
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
                var esObject = layer.get('solrObject');
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
                  // panierItem.wps = p;
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
