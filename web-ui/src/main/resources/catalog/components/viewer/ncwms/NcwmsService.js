(function() {
  goog.provide('gn_ncwms_service');

  var module = angular.module('gn_ncwms_service', []);

  module.constant('gnNcWmsConst', {
    elevation: [
      5,10,15,20,25,30,35,40,50,75,100,125,150,200,250,300,400,500
    ],
    colorscale : {
      min: 7,
      max: 15,
      step: 1
    }
  });

  module.service('gnNcWms', [
      'gnMap',
      'gnUrlUtils',
          function(gnMap, gnUrlUtils) {

            this.createNcWmsLayer = function(capLayer) {
              var source = new ol.source.TileWMS({
                params: {
                  LAYERS:'NCOF_MRCS/POT',
                  ELEVATION:'-5.0',
                  COLORSCALERANGE:'10.45782,15.45782'
                },
                url: 'http://behemoth.nerc-essc.ac.uk/ncWMS/wms'
              });
              var layer = new ol.layer.Tile({
                url: 'http://behemoth.nerc-essc.ac.uk/ncWMS/wms',
                type: 'WMS',
                source: source
              });
              return layer;
            };

            this.getNcwmsServiceUrl = function(layer, proj, geom, service) {
              var p = {
                  REQUEST: 'GetTransect',
                  FORMAT: 'image/png',
                  CRS: proj.getCode(),
                  LINESTRING: gnMap.getTextFromCoordinates(geom),
                  LAYER: layer.getSource().getParams().LAYERS
              };

              return url = gnUrlUtils.append(layer.getSource().getUrls(),
                  gnUrlUtils.toKeyValue(p));
            }
          }
  ]);
})();
