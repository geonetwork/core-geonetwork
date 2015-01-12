(function() {
  goog.provide('gn_map_service');

  goog.require('gn_ows');


  var module = angular.module('gn_map_service', [
    'gn_ows',
    'ngeo'
  ]);

  module.provider('gnMap', function() {
    this.$get = [
      'ngeoDecorateLayer',
      'gnOwsCapabilities',
      'gnConfig',
      '$log',
      function(ngeoDecorateLayer, gnOwsCapabilities, gnConfig, $log) {

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
            proj4.defs('EPSG:2154', '+proj=lcc +lat_1=49 +lat_2=44 +lat_0' +
                '=46.5 +lon_0=3 +x_0=700000 +y_0=6600000 +ellps=GRS80 +' +
                'towgs84=0,0,0,0,0,0,0 +units=m +no_defs');
            if (proj4 && angular.isArray(gnConfig['map.proj4js'])) {
              angular.forEach(gnConfig['map.proj4js'], function(item) {
                proj4.defs(item.code, item.value);
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
           * @param {Object} md
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
           * @param {Array} coord must be an array of points (array with
           * dimension 2) or a point
           * @return {String} coordinates as text with format :
           * 'x1 y1,x2 y2,x3 y3'
           */
          getTextFromCoordinates: function(coord) {
            var text;

            var addPointToText = function(point) {
              if (text) {
                text += ',';
              }
              else {
                text = '';
              }
              text += point[0] + ' ' + point[1];
            };

            if (angular.isArray(coord) && coord.length > 0) {
              if (angular.isArray(coord[0])) {
                for (var i = 0; i < coord.length; ++i) {
                  var point = coord[i];
                  if (angular.isArray(point) && point.length == 2) {
                    addPointToText(point);
                  }
                }
              } else if (coord.length == 2) {
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

          addWmsToMap: function(map, layerParams, layerOptions, index) {

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
              isNcwms: options.isNcwms,
              cextent: options.extent
            });
            ngeoDecorateLayer(olLayer);
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
           * @param {ol.map} map
           * @param {Object} getCapLayer
           * @return {*}
           */
          addWmsToMapFromCap: function(map, getCapLayer) {

            var legend, attribution, metadata;
            if (getCapLayer) {
              var layer = getCapLayer;

              // TODO: parse better legend & attribution
              if (angular.isArray(layer.Style) && layer.Style.length > 0) {
                legend = layer.Style[layer.Style.length - 1]
                    .LegendURL[0].OnlineResource;
              }
              if (angular.isDefined(layer.Attribution)) {
                if (angular.isArray(layer.Attribution)) {

                } else {
                  attribution = layer.Attribution.Title;
                }
              }
              if (angular.isArray(layer.MetadataURL)) {
                metadata = layer.MetadataURL[0].OnlineResource;
              }
              var isNcwms = false;
              if (angular.isArray(layer.Dimension)) {
                for (var i = 0; i < layer.Dimension.length; i++) {
                  if (layer.Dimension[i].name == 'elevation') {
                    isNcwms = true;
                    break;
                  }
                }
              }

              return this.addWmsToMap(map, {
                LAYERS: layer.Name
              }, {
                url: layer.url,
                label: layer.Title,
                attribution: attribution,
                legend: legend,
                group: layer.group,
                metadata: metadata,
                isNcwms: isNcwms,
                extent: gnOwsCapabilities.getLayerExtentFromGetCap(map, layer)
              }
              );
            }
          },

          /**
           * Add a WMTS layer to the map.
           *
           * @param {ol.map} map
           * @param {Object} srcParams
           * @param {Object} layerParams
           * @return {ol.layer.Tile}
           */
          addWMTSToMap: function(map, srcParams, layerParams) {

            var source = new ol.source.WMTS({
              url: srcParams.url,
              layer: srcParams.layerId,
              matrixSet: srcParams.matrixId,
              format: srcParams.format || 'image/png',
              projection: srcParams.projection,
              tileGrid: new ol.tilegrid.WMTS({
                origin: ol.extent.getTopLeft(srcParams.projection.getExtent()),
                resolutions: srcParams.resolutions,
                matrixIds: srcParams.matrixIds
              }),
              style: srcParams.style || 'default'
            });

            var olLayer = new ol.layer.Tile({
              extent: srcParams.projection.getExtent(),
              label: layerParams.title,
              opacity: layerParams.opacity,
              visible: layerParams.visible,
              source: source,
              title: srcParams.layerId,
              url: srcParams.url
            });
            ngeoDecorateLayer(olLayer);
            olLayer.displayInLayerManager = true;

            map.addLayer(olLayer);
            return olLayer;
          },

          /**
           * Parse an object describing a layer from
           * a getCapabilities document parsing. Create a ol.Layer WMS
           * from this object and add it to the map with all known
           * properties.
           *
           * @param {ol.map} map
           * @param {Object} getCapLayer
           * @return {*}
           */
          addWmtsToMapFromCap: function(map, getCapLayer, capabilities) {

            var legend, attribution, metadata;
            if (getCapLayer) {
              var layer = getCapLayer;

              var url = capabilities.operationsMetadata.GetTile.
                  dcp.http.get[0].url;

              var projection = map.getView().getProjection();

              // Try to guess which matrixId to use depending projection
              var matrixSetsId;
              for (var i = 0; i < layer.tileMatrixSetLinks.length; i++) {
                if (layer.tileMatrixSetLinks[i].tileMatrixSet ==
                    projection.getCode()) {
                  matrixSetsId = layer.tileMatrixSetLinks[i].tileMatrixSet;
                  break;
                }
              }
              if (!matrixSetsId) {
                matrixSetsId = layer.tileMatrixSetLinks[0].tileMatrixSet;
              }
              var matrixSet = capabilities.tileMatrixSets[matrixSetsId];
              var nbMatrix = matrixSet.matrixIds.length;

              var projectionExtent = projection.getExtent();
              var resolutions = new Array(nbMatrix);
              var matrixIds = new Array(nbMatrix);
              for (var z = 0; z < nbMatrix; ++z) {
                var matrixId = matrixSet.matrixIds[z];
                var size = ol.extent.getWidth(projectionExtent) /
                    matrixId.tileWidth;
                resolutions[z] = matrixId.scaleDenominator * 0.00028 /
                    projection.getMetersPerUnit();
                matrixIds[z] = matrixId.identifier;
              }

              return this.addWMTSToMap(map, {
                url: url,
                layerId: layer.identifier,
                matrixId: matrixSet.identifier,
                projection: projection,
                matrixIds: matrixIds,
                resolutions: resolutions,
                style: 'default',
                format: layer.formats[0]
              }, {
                title: layer.title
              });
            }
          },

          /**
           * Zoom by delta with animation
           * @param {ol.map} map
           * @param {float} delta
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
              var newResolution = view.constrainResolution(
                  currentResolution, delta);
              view.setResolution(newResolution);
            }
          },

          /**
           * Creates an ol.layer for a given type. Useful for contexts
           * @param {string} type
           * @return {ol.layer}
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
                    key: 'Ak-dzM4wZjSqTlzveKz5u0d4I' +
                        'Q4bRzVI309GxmkgSVr1ewS6iPSrOvOKhA-CJlm3',
                    imagerySet: 'Aerial'
                  }),
                  title: 'Bing Aerial'
                });
              case 'wmts':
                var projection = ol.proj.get('EPSG:3857');
                var projectionExtent = projection.getExtent();
                var size = ol.extent.getWidth(projectionExtent) / 256;
                var resolutions = new Array(16);
                var matrixIds = new Array(16);
                for (var z = 0; z < 16; ++z) {
                  // generate resolutions and matrixIds arrays for this WMTS
                  resolutions[z] = size / Math.pow(2, z);
                  matrixIds[z] = 'EPSG:3857:' + z;
                }

                return new ol.layer.Tile({
                  opacity: 0.7,
                  extent: projectionExtent,
                  title: 'Sextant',
                  source: new ol.source.WMTS({
                    url: 'http://visi-sextant.ifremer.fr:8080/' +
                        'geowebcache/service/wmts?',
                    layer: 'Sextant',
                    matrixSet: 'EPSG:3857',
                    format: 'image/png',
                    projection: projection,
                    tileGrid: new ol.tilegrid.WMTS({
                      origin: ol.extent.getTopLeft(projectionExtent),
                      resolutions: resolutions,
                      matrixIds: matrixIds
                    }),
                    style: 'default'
                  })
                });
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
