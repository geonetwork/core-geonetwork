(function() {
  goog.provide('gn_map_service');


  var module = angular.module('gn_map_service', [
  ]);

  module.provider('gnMap', function() {
    this.$get = [
      'gnConfig',
      function(gnConfig) {

        var defaultMapConfig = {
          'useOSM': 'true',
          'projection': 'EPSG:3857',
          'projectionList': [{
            'code': 'EPSG:4326',
            'label': 'WGS84 (EPSG:4326)'
          },{
            'code': 'EPSG:3857',
            'label': 'Google mercator (EPSG:3857)'
          }]
        };

        return {

          importProj4js: function() {
            Proj4js.defs['EPSG:3857'] = Proj4js.defs['EPSG:900913'];
            if (Proj4js && gnConfig['map.proj4js'] &&
                angular.isArray(gnConfig['map.proj4js'])) {
              angular.forEach(gnConfig['map.proj4js'], function(item) {
                Proj4js.defs[item.code] = item.value;
              });
            }
          },

          /**
           * Reproject a given extent. Extent is an object
           * defined as
           * {left,bottom,right,top}
           *
           */
          reprojExtent: function(extent, src, dest) {
            if (src == dest || extent === null) {
              return extent;
            }
            else {
              return ol.proj.transform(extent,
                  src, dest);
            }
          },

          isPoint: function(extent) {
            return (extent[0] == extent[2] &&
                extent[1]) == extent[3];
          },

          getPolygonFromExtent: function(extent) {
            return [
                    [
                     [extent[0], extent[1]],
                     [extent[0], extent[3]],
                     [extent[2], extent[3]],
                     [extent[2], extent[1]]
              ]
            ];
          },

          getMapConfig: function() {
            if (gnConfig['map.config'] &&
                angular.isObject(gnConfig['map.config'])) {
              return gnConfig['map.config'];
            } else {
              return defaultMapConfig;
            }
          },

          getLayersFromConfig: function() {
            var conf = this.getMapConfig();
            var source;

            if (conf.useOSM) {
              source = new ol.source.OSM();
            }
            else {
              source = new ol.source.TileWMS({
                url: conf.layer.url,
                params: {'LAYERS': conf.layer.layers,
                  'VERSION': conf.layer.version}
              });
            }
            return new ol.layer.Tile({
              source: source
            });
          },

          /**
           * Check if the extent is valid or not.
           */
          isValidExtent: function(extent) {
            var valid = true;
            if (extent && angular.isArray(extent)) {
              angular.forEach(extent, function(value, key) {
                if (!value || value == Infinity || value == -Infinity) {
                  valid = false;
                }
              });
            }
            else {
              valid = false;
            }
            return valid;
          },

          /**
           * Transform map extent into dublin-core schema for
           * dc:coverage metadata element.
           * Ex :
           * North 90, South -90, East 180, West -180. Global
           */
          getDcExtent: function(extent) {
            var dc = 'North ' + extent[3] + ', ' +
                'South ' + extent[1] + ', ' +
                'East ' + extent[0] + ', ' +
                'West ' + extent[2] + '. Global';

            return dc;
          },

          addWmsToMap : function(map, layerParams, layerOptions, index) {
            var createWmsLayer = function(params, options, index) {
              options = options || {};
              var attributions;

/*
              if (options.attribution) {
                attributions = [
                  gnMapUtils.getAttribution(options.attribution)
                ];
              }
*/

              var source = new ol.source.TileWMS({
                params: params,
                url: options.url,
                extent: options.extent,
                attributions: attributions,
                ratio: options.ratio || 1
              });

              var layer = new ol.layer.Tile({
                url: options.url,
                type: 'WMS',
                opacity: options.opacity,
                visible: options.visible,
                attribution: options.attribution,
                source: source
              });
              /*gaDefinePropertiesForLayer(layer);*/
              layer.preview = options.preview;
              layer.label = options.label;
              return layer;
            };

            var olLayer = createWmsLayer(layerParams, layerOptions);
            if (index) {
              map.getLayers().insertAt(index, olLayer);
            } else {
              map.addLayer(olLayer);
            }
            return olLayer;
          }
        };
      }];
  });
})();
