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
      'gnOwsCapabilities',
          function(gnMap, gnUrlUtils, gnOwsCapabilities) {

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
              layer.label = "Super NCWMS";

              gnOwsCapabilities.getCapabilities('http://behemoth.nerc-essc.ac.uk/ncWMS/wms?service=WMS&request=GetCapabilities')
                  .then(function (capObj) {
                    layer.ncInfo = gnOwsCapabilities.getLayerInfoFromCap(layer.getSource().getParams().LAYERS, capObj);
                  });

              return layer;
            };

            /**
             * Compute ncWMS specific services url from parameters.
             * Could be `GetVerticalProfile` `GetTransect`.
             * @param layer
             * @param proj
             * @param geom
             * @param service
             * @returns {*}
             */
            this.getNcwmsServiceUrl = function(layer, proj, geom, service) {
              var p = {
                  FORMAT: 'image/png',
                  CRS: proj.getCode(),
                  LAYER: layer.getSource().getParams().LAYERS
              };
              if(service == 'time') {

              } else if (service == 'profile') {
                p.REQUEST = 'GetVerticalProfile';
                p.POINT = gnMap.getTextFromCoordinates(geom);

              } else if (service == 'transect') {
                p.REQUEST = 'GetTransect';
                p.LINESTRING = gnMap.getTextFromCoordinates(geom);
              }

              return url = gnUrlUtils.append(layer.getSource().getUrls(),
                  gnUrlUtils.toKeyValue(p));
            };
          }
  ]);
})();
