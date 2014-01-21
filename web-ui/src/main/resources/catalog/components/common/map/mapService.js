(function() {
  goog.provide('gn_map_service');


  var module = angular.module('gn_map_service', [
  ]);

  module.provider('gnMap', function() {
    this.$get = [
      'gnConfig',
      function(gnConfig) {
        return {

          /**
           * Reproject a given extent. Extent is an object
           * defined as
           * {left,bottom,right,top}
           *
           */
          reprojExtent: function(extent, src, dest) {
            if (src == dest) {
              return extent;
            }
            else {
              return ol.proj.transform(extent,
                  src, dest);
            }
          },

          isPoint: function(extent) {
             return (extent[0] == extent[2] 
               && extent[1]) == extent[3];
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
            return gnConfig['map.config'];
          },
          
          getLayersFromConfig: function() {
            var conf = this.getMapConfig();
            var source;
            
            if(conf.useOSM) {
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
           * Transform map extent into dublin-core schema for
           * dc:coverage metadata element. 
           * Ex :
           * North 90, South -90, East 180, West -180. Global
           */
          getDcExtent : function(extent) {
            var dc = 'North ' + extent[3] + ', ' +
              'South ' + extent[1] + ', ' +
              'East ' + extent[0] + ', ' +
              'West ' + extent[2] + '. Global';
            
            return dc;
          },
        };
      }];
  });
})();
