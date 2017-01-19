(function() {
  goog.provide('sxt_panier_service');

  var module = angular.module('sxt_panier_service', [
  ]);


  module.service('sxtPanierService', [
      '$http',
    function($http) {

      var panierUrl = 'extractor.doExtract';

      var callExtractService = function(panier) {
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
                var esConfig = layer.get('esConfig');
                if(esConfig) {
                  var g = esConfig.geometry;
                  if(g) {
                    var extent = [g[0][0], g[1][1], g[1][0], g[0][1]];
                    panierItem.filter = {
                      extent: extent
                    }
                  }
                }
              }
              else {
                panierItem.layer = null;
              }
            });
          }
        });
      };
    }
  ]);

})();
