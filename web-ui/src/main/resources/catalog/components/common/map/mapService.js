(function() {
  goog.provide('gn_map_service');

  goog.require('gn_ows');


  var module = angular.module('gn_map_service', [
    'gn_ows',
    'go'
  ]);

  module.provider('gnMap', function() {
    this.$get = [
      'goDecorateLayer',
      'gnOwsCapabilities',
      'gnConfig',
      '$log',
      function(goDecorateLayer, gnOwsCapabilities, gnConfig, $log) {

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
            proj4.defs("EPSG:2154","+proj=lcc +lat_1=49 +lat_2=44 +lat_0=46.5 +lon_0=3 +x_0=700000 +y_0=6600000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs");
            if (proj4 && angular.isArray(gnConfig['map.proj4js'])) {
              angular.forEach(gnConfig['map.proj4js'], function(item) {
                proj4.defs(item.code,item.value);
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
                     [extent[2], extent[1]],
                     [extent[0], extent[1]]
              ]
            ];
          },

          /**
           * Get the extent of the md.
           * It is stored in the object md.geoBox as a String
           * '150|-12|160|12'.
           * Returns it as an array of floats.
           *
           * @param md
           */
          getBboxFromMd: function(md) {
            if (angular.isUndefined(md.geoBox)) return;

            var bbox = angular.isArray(md.geoBox) ?
                md.geoBox[0] : md.geoBox;
            var c = bbox.split('|');
            if (angular.isArray(c) && c.length == 4) {
              return [parseFloat(c[0]),
                parseFloat(c[1]),
                parseFloat(c[2]),
                parseFloat(c[3])];
            }
          },

          /**
           * Convert coordinates object into text
           *
           * @param coord must be an array of points (array with
           * dimension 2) or a point
           * @returns coordinates as text with format : 'x1 y1,x2 y2,x3 y3'
           */
          getTextFromCoordinates: function(coord) {
            var text;

            var addPointToText = function(point) {
              if(text) {
                text += ',';
              }
              else {
                text = '';
              }
              text += point[0] + ' ' + point[1];
            };

            if(angular.isArray(coord) && coord.length > 0) {
              if(angular.isArray(coord[0])) {
                for(var i=0;i<coord.length;++i) {
                  var point = coord[i];
                  if(angular.isArray(point) && point.length == 2) {
                    addPointToText(point);
                  }
                }
              } else if(coord.length == 2) {
                addPointToText(coord);
              }
            }
            return text;
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
           * North 90, South -90, East 180, West -180
           * or
           * North 90, South -90, East 180, West -180. Global
           */
          getDcExtent: function(extent) {
            if (angular.isArray(extent)) {
              var dc = 'North ' + extent[3] + ', ' +
                  'South ' + extent[1] + ', ' +
                  'East ' + extent[0] + ', ' +
                  'West ' + extent[2];
              if (location) {
                dc += '. ' + location;
              }
              return dc;
            } else {
              return '';
            }
          },

          addWmsToMap : function(map, layerParams, layerOptions, index) {

            var options = layerOptions || {};

            var source = new ol.source.TileWMS({
              params: layerParams,
              url: options.url
            });

            var olLayer = new ol.layer.Tile({
              url: options.url,
              type: 'WMS',
              opacity: options.opacity,
              visible: options.visible,
              source: source,
              legend: options.legend,
              attribution: options.attribution,
              metadata: options.metadata,
              label: options.label,
              group: options.group,
              cextent: options.extent
            });
            goDecorateLayer(olLayer);
            olLayer.displayInLayerManager = true;

            if (index) {
              map.getLayers().insertAt(index, olLayer);
            } else {
              map.addLayer(olLayer);
            }
            return olLayer;
          },

          /**
           * Parse an object describing a layer from
           * a getCapabilities document parsing. Create a ol.Layer WMS
           * from this object and add it to the map with all known
           * properties.
           *
           * @param map
           * @param getCapLayer
           * @returns {*}
           */
          addWmsToMapFromCap : function(map, getCapLayer) {

            var legend, attribution, metadata;
            if (getCapLayer) {
              var layer = getCapLayer;

              // TODO: parse better legend & attribution
              if(angular.isArray(layer.Style) && layer.Style.length > 0) {
                legend = layer.Style[layer.Style.length-1].LegendURL[0].OnlineResource;
              }
              if(angular.isDefined(layer.Attribution) ) {
                if(angular.isArray(layer.Attribution)){

                } else {
                  attribution = layer.Attribution.Title;
                }
              }
              if(angular.isArray(layer.MetadataURL)) {
                metadata = layer.MetadataURL[0].OnlineResource;
              }

              return this.addWmsToMap(map, {
                    LAYERS: layer.Name
                  }, {
                    url: layer.url,
                    label: layer.Title,
                    attribution: attribution,
                    legend: legend,
                    metadata: metadata,
                    extent: gnOwsCapabilities.getLayerExtentFromGetCap(map, layer)
                  }
              );
            }
          },


          /**
           * Zoom by delta with animation
           * @param map
           * @param delta
           */
          zoom: function(map, delta) {
            var view = map.getView();
            var currentResolution = view.getResolution();
            if (angular.isDefined(currentResolution)) {
              map.beforeRender(ol.animation.zoom({
                resolution: currentResolution,
                duration: 250,
                easing: ol.easing.easeOut
              }));
              var newResolution = view.constrainResolution(currentResolution, delta);
              view.setResolution(newResolution);
            }
          },

          /**
           * Creates an ol.layer for a given type. Useful for contexts
           * @param type
           * @returns {ol.layer}
           */
          createLayerForType: function(type) {
            switch (type) {
              case 'mapquest':
                return new ol.layer.Tile({
                  style: 'Road',
                  source: new ol.source.MapQuest({layer: 'osm'}),
                  title: 'MapQuest'
                });
              case 'osm':
                return new ol.layer.Tile({
                  source: new ol.source.OSM(),
                  title: 'OpenStreetMap'
                });
              case 'bing_aerial':
                return new ol.layer.Tile({
                  preload: Infinity,
                  source: new ol.source.BingMaps({
                    key: 'Ak-dzM4wZjSqTlzveKz5u0d4IQ4bRzVI309GxmkgSVr1ewS6iPSrOvOKhA-CJlm3',
                    imagerySet: 'Aerial'
                  }),
                  title: 'Bing Aerial'
                })
            }
            $log.warn('Unsupported layer type: ', type);
          }
        };
      }];
  });

  module.provider('gnLayerFilters', function() {
    this.$get = function() {
      return {
        /**
         * Filters out background layers, preview
         * layers, draw, measure.
         * In other words, all layers that
         * were actively added by the user and that
         * appear in the layer manager
         */
        selected: function(layer) {
          return layer.displayInLayerManager;
        }
      };
    };
  });


})();
