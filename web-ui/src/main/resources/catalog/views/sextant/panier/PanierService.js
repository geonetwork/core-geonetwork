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
      }

      this.extract = function(panier) {
        return callExtractService(panier);
      };

      this.bindPanierWithLayers = function(panier, map) {
        map.getLayers().forEach(function(layer){
          var downloads = layer.get('downloads');
          if(downloads) {
            panier.forEach(function(panierItem) {
              if(downloads.some(function(d) {
                  var l = panierItem.link;
                  return d.url == l.url &&
                    d.name == l.name &&
                    d.protocol == l.protocol;
                })) {
                var esObject = layer.get('solrObject');
                if(esObject) {
                  var esConfig = esObject.getState();
                  var g = esConfig.geometry;
                  var filters = [];
                  angular.forEach(esConfig.qParams, function(obj, fName) {
                    value = obj.values;
                    filters.push({
                      name: fName,
                      value: Object.keys(value)[0]
                    });
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
