/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

(function() {
  goog.provide('gn_map_service');

  goog.require('gn_ows');
  goog.require('gn_wfs_service');


  var module = angular.module('gn_map_service', [
    'gn_ows',
    'ngeo',
    'gn_wfs_service'
  ]);

  /**
   * @ngdoc service
   * @kind function
   * @name gn_map.service:gnMap
   *
   * @description
   * The `gnMap` service is the main service that offer methods for interacting
   * with the map of the layers object. It is the interface with ol3 API and
   * provided lots of tools to help creating map content.
   */
  module.provider('gnMap', function() {
    this.$get = [
      'ngeoDecorateLayer',
      'gnOwsCapabilities',
      'gnConfig',
      '$log',
      'gnSearchLocation',
      '$rootScope',
      'gnUrlUtils',
      '$q',
      '$translate',
      'gnWmsQueue',
      'gnSearchManagerService',
      'Metadata',
      'gnWfsService',
      'gnGlobalSettings',
      'gnViewerSettings',
      'gnViewerService',
      'gnAlertService',
      function(ngeoDecorateLayer, gnOwsCapabilities, gnConfig, $log,
          gnSearchLocation, $rootScope, gnUrlUtils, $q, $translate,
          gnWmsQueue, gnSearchManagerService, Metadata, gnWfsService,
          gnGlobalSettings, gnViewerSettings, gnViewerService, gnAlertService) {

        /**
         * @description
         * Check if the layer is in the map to avoid adding duplicated ones.
         *
         * @param {ol.Map} map obj
         * @param {string} name of the layer
         * @param {string} url of the service
         * @return {boolean} true if layer is in the map
         */
        var isLayerInMap = function(map, name, url) {
          return getLayerInMap(map, name, url) !== null;
        };
        var getLayerInMap = function(map, name, url) {
          if (gnWmsQueue.isPending(url, name)) {
            return true;
          }

          if(getTheLayerFromMap(map, name, url) != null) {
            return true;
          }
          return null;
        };

        /**
         * @description
         * Returns a Layer already added to the map.
         *
         * @param {ol.Map} map obj
         * @param {string} name of the layer
         * @param {string} url of the service
         */
        var getTheLayerFromMap = function(map, name, url) {
          for (var i = 0; i < map.getLayers().getLength(); i++) {
            var l = map.getLayers().item(i);
            var source = l.getSource();
            if (source instanceof ol.source.WMTS &&
                l.get('url') == url) {
              if (l.get('name') == name) {
                return l;
              }
            }
            else if (source instanceof ol.source.TileWMS ||
                source instanceof ol.source.ImageWMS) {
              if (source.getParams().LAYERS == name &&
                  l.get('url').split('?')[0] == url.split('?')[0]) {
                return l;
              }
            }
          }
          return null;
        };

        var getImageSourceRatio = function(map, maxWidth) {
          var width = (map.getSize() && map.getSize()[0]) ||
              $('.gn-full').width();
          var ratio = maxWidth / width;
          ratio = Math.floor(ratio * 100) / 100;
          return Math.min(1.5, Math.max(1, ratio));
        };

        return {
          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#createLayerFromProperties
           *
           * @description
           * Creates an ol.layer based on an object containing properties
           * used to describe the layer. The `type` property is required, and
           * others can be used depending on the layer type.
           * Handled types are:
           *  * `osm`: OSM, no other prop required
           *  * `bing_aerial`: Bing Aerial background, required prop: `key`
           *  * `stamen`: Stamen layers, required prop: `name`
           *  * `wms`: generic WMS layer, required props: `name`, `url`
           *  * `wmts`: generic WMTS layer, required props: `name`, `url`
           *  * `tms`: generic TMS layer, required prop: `url`
           * If a `title` property is present on the layer info obj, it will be
           * applied on the layer.
           * This will return a promise with the layer in it
           *
           * @param {Object} layerInfo object containing all the properties
           * @param {ol.Map} map required for WMTS and WMS
           * @return {Promise} promise with the layer as result
           */
          createLayerFromProperties: function(layerInfo, map) {
            // this will be used to return a promise (whichever the layer type)
            var defer = $q.defer();

            // check layer info validity
            if (!layerInfo.type) {
              console.error('The layer info object is invalid:', layerInfo);
              defer.reject();
              return $q.defer().promise;
            }
            
            //Check if the layer has some projection restriction
            //If no restriction, just (try to) add it
            if(layerInfo.projectionList && layerInfo.projectionList.length 
                && layerInfo.projectionList.length > 0) {
              var addIt = false;
              
              $.each(layerInfo.projectionList, function(i, p){
                if(map.getView().getProjection().getCode() == p) {
                  addIt = true;
                }
              });
              
              if(!addIt) {
                defer.reject();
                return $q.defer().promise;
              }
            }

            switch (layerInfo.type) {
              case 'osm':
                defer.resolve(new ol.layer.Tile({
                  source: new ol.source.OSM(),
                  title: layerInfo.title || 'OpenStreetMap'
                }));
                break;

              case 'tms':            
                var prop = { 
                  // Settings are usually encoded
                    url: decodeURI(layerInfo.url)
                };
                
                if(layerInfo.projection) {
                  prop.projection = layerInfo.projection;
                }
                
                if(layerInfo.attribution) {
                  prop.attributions = [
                    new ol.Attribution({"html": layerInfo.attribution})
                  ]
                }
                
                defer.resolve(new ol.layer.Tile({
                  source: new ol.source.XYZ(prop),
                  title: layerInfo.title || 'TMS Layer'
                }));
                break;

              case 'bing_aerial':
                defer.resolve(new ol.layer.Tile({
                  preload: Infinity,
                  source: new ol.source.BingMaps({
                    key: layerInfo.key,
                    imagerySet: 'Aerial'
                  }),
                  title: layerInfo.title || 'Bing Aerial'
                }));
                break;

              case 'stamen':
                //We make watercolor the default layer
                var type = layerInfo.name ? layerInfo.name : 'watercolor',
                    source = new ol.source.Stamen({
                      layer: type
                    });
                source.set('type', type);
                defer.resolve(new ol.layer.Tile({
                  source: source,
                  title: layerInfo.title || 'Stamen'
                }));
                break;

              case 'wmts':
                if (!layerInfo.name || !layerInfo.url) {
                  $log.warn('One of the required parameters (name, url) ' +
                      'is missing in the specified WMTS layer:',
                      layerInfo);
                  defer.reject();
                  break;
                }
                this.addWmtsFromScratch(
                    map, layerInfo.url, layerInfo.name, true)
                    .then(function(layer) {
                      if (layerInfo.title) {
                        layer.set('title', layerInfo.title);
                        layer.set('label', layerInfo.title);
                      }
                      
                      if(layerInfo.attribution) {
                        layer.getSource().setAttributions(layerInfo.attribution);
                      }
                      
                      defer.resolve(layer);
                    });
                break;

              case 'wms':
                if (!layerInfo.name || !layerInfo.url) {
                  $log.warn('One of the required parameters (name, url) ' +
                      'is missing in the specified WMS layer:',
                      layerInfo);
                  defer.reject();
                  break;
                }
                this.addWmsFromScratch(map, layerInfo.url, layerInfo.name, true)
                    .then(function(layer) {
                      if (layerInfo.title) {
                        layer.set('title', layerInfo.title);
                        layer.set('label', layerInfo.title);
                      }
                      
                      if(layerInfo.attribution) {
                        layer.getSource().setAttributions(layerInfo.attribution);
                      }
                      
                      defer.resolve(layer);
                    });
                break;

              default:
                $log.warn('Unrecognized layer type: ', layerInfo.type);
                defer.reject();
            }

            return defer.promise;
          },

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#createOSMLayer
           *
           * @return {ol.layer} layer
           */
          createOSMLayer: function() {
            return this.createLayerFromProperties({ type: 'osm' });
          },

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#importProj4js
           *
           * @description
           * Import the proj4js projection that are specified in DB config.
           */
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
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#reprojExtent
           *
           * @description
           * Reproject a given extent. Extent is an object
           * defined as
           * {left,bottom,right,top}
           *
           * @param {Object} extent to reproj
           * @param {ol.Projection} src projection
           * @param {ol.Projection} dest projection
           *
           */
          reprojExtent: function(extent, src, dest) {
            if (src == dest || extent === null) {
              return extent;
            }
            else {
              return ol.proj.transformExtent(extent,
                  src, dest);
            }
          },

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#isPoint
           *
           * @description
           * Check if the extent is just a point.
           *
           * @param {Object} extent to check
           */
          isPoint: function(extent) {
            return (extent[0] == extent[2] &&
                extent[1]) == extent[3];
          },

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#getPolygonFromExtent
           *
           * @description
           * Build a coordinates based object (multypolygon) from a extent
           *
           * @param {Object} extent to convert
           *
           */
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
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#getBboxFromMd
           *
           * @description
           * Get the extent of the md.
           * It is stored in the object md.geoBox as an array of String
           * '150|-12|160|12'.
           * Returns it as an array of array of floats.
           *
           * @param {Object} md to extract bbox from
           */
          getBboxFromMd: function(md) {
            if (angular.isUndefined(md.geoBox)) return;
            var bboxes = [];
            angular.forEach(md.geoBox, function(bbox) {
              var c = bbox.split('|');
              if (angular.isArray(c) && c.length == 4) {
                bboxes.push([parseFloat(c[0]),
                      parseFloat(c[1]),
                      parseFloat(c[2]),
                      parseFloat(c[3])]);
              }
            });
            return bboxes;
          },

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#getBboxFeatureFromMd
           *
           * @description
           * Get the extent of the md.
           * Returns a feature
           *
           * @param {Object} md to extract bbox from
           * @param {Object} proj of the extent
           */
          getBboxFeatureFromMd: function(md, proj) {
            var feat = new ol.Feature();
            var extent = this.getBboxFromMd(md);
            var projExtent = proj.getExtent();
            if (extent) {
              var geometry;
              // If is composed of one geometry of type point
              if (extent.length === 1 &&
                  extent[0][0] === extent[0][2] &&
                  extent[0][1] === extent[0][3]) {
                geometry = new ol.geom.Point([extent[0][0], extent[0][1]]);
              } else {
                // Build multipolygon from the set of bboxes
                geometry = new ol.geom.MultiPolygon(null);
                for (var j = 0; j < extent.length; j++) {
                  // TODO: Point will not be supported in multi geometry
                  var projectedExtent = ol.extent.getIntersection(
                      ol.proj.transformExtent(extent[j], 'EPSG:4326', proj),
                      projExtent
                      );
                  var coords = this.getPolygonFromExtent(projectedExtent);
                  geometry.appendPolygon(new ol.geom.Polygon(coords));
                }
              }
              feat.setGeometry(geometry);
            }
            return feat;
          },

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#getTextFromCoordinates
           *
           * @description
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

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#getMapConfig
           *
           * @description
           * get the DB config of the map components (projection, map etc..)
           *
           * @return {Object} defaultMapConfig mapconfig
           */
          getMapConfig: function() {

            // Check for unsupported projections
            // To avoid to break the search page and map
            if(gnViewerSettings.mapConfig.projection && !ol.proj.get(gnViewerSettings.mapConfig.projection)) {
              console.warn('The map projection ' + gnViewerSettings.mapConfig.projection + ' is not supported.');
              console.log('Now using default projection EPSG:3857.');
              // Switching to default
              gnViewerSettings.mapConfig.projection = 'EPSG:3857';
            }

            return gnViewerSettings.mapConfig;
          },

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#getLayersFromConfig
           * @deprecated When creating a new map, use createMap(<TYPE>) instead
           *
           * @description
           * get the DB config of the layers list that should be in the map
           * by default
           * DO NOT USE THIS ANYMORE:
           * When creating a new map, use createMap(<TYPE>) instead
           *
           * @return {Object} defaultMapConfig layers config
           */
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
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#isValidExtent
           *
           * @description
           * Check if the extent is valid or not.
           *
           * @param {Array} extent to check
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
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#getDcExtent
           *
           * @description
           * Transform map extent into dublin-core schema for
           * dc:coverage metadata element.
           * Ex :
           * North 90, South -90, East 180, West -180
           * or
           * North 90, South -90, East 180, West -180. Global
           *
           * @param {Array} extent to transform
           */
          getDcExtent: function(extent, location) {
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

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#getResolutionFromScale
           *
           * @description
           * Compute the resolution from a given scale
           *
           * @param {ol.Projection} projection of the map
           * @param {number} scale to convert
           * @return {number} resolution
           */
          getResolutionFromScale: function(projection, scale) {
            return scale && scale * 0.00028 / projection.getMetersPerUnit();
          },

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#addGeoJSONToMap
           *
           * @description
           * Add a GeoJSON layer to the map from a given source.
           *
           * @param {string} name of the layer
           * @param {number} url of the GeoJSON source
           * @param {ol.Map} map object
           */
          addGeoJSONToMap: function(name, url, map) {
            if (!url || url == '') {
              return;
            }

            var GeoJSONSource = new ol.source.Vector({
              projection: map.getView().getProjection(),
              format: new ol.format.GeoJSON(),
              url: url
            });

            var vector = new ol.layer.Vector({
              source: GeoJSONSource,
              label: name
            });

            ngeoDecorateLayer(vector);
            vector.displayInLayerManager = true;
            map.getLayers().push(vector);
          },

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#addKmlToMap
           *
           * @description
           * Add a KML layer to the map from a given source.
           *
           * @param {string} name of the layer
           * @param {number} url of the kml sources
           * @param {ol.Map} map object
           */
          addKmlToMap: function(name, url, map) {
            if (!url || url == '') {
              return;
            }

            var kmlSource = new ol.source.Vector({
              url: url,
              projection: map.getView().getProjection(),
              format: new ol.format.KML()
            });

            var vector = new ol.layer.Vector({
              source: kmlSource,
              label: name
            });

            ngeoDecorateLayer(vector);
            vector.displayInLayerManager = true;
            map.getLayers().push(vector);
          },

          // Given only the url, it will show a dialog to select
          // what layers do we want to add to the map
          addOwsServiceToMap: function(url, type) {
            // move to map
            gnSearchLocation.setMap();
            // open dialog for WMS
            switch (type.toLowerCase()) {
              case 'wms':
                gnViewerService.openWmsTab(url);
                break;

              case 'wmts':
                gnViewerService.openWmtsTab(url);
                break;
            }
          },

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#createOlWMS
           *
           * @description
           * Create a new ol.Layer object, based on given options.
           *
           * @param {ol.Map} map to add the layer
           * @param {Object} layerParams contains the PARAMS that is given to
           *  the ol.source object
           * @param {Object} layerOptions options to pass to layer constructor
           * @param {Object} layerOptions options to pass to layer constructor
           */
          createOlWMS: function(map, layerParams, layerOptions) {

            var options = layerOptions || {};

            var source, olLayer;
            if (gnViewerSettings.singleTileWMS) {
              var config = {
                params: layerParams,
                url: options.url,
                projection: layerOptions.projection,
                ratio: getImageSourceRatio(map, 2048)
              };
              source = new ol.source.ImageWMS(
                  gnViewerSettings.mapConfig.isExportMapAsImageEnabled ?
                  angular.extend(config, {crossOrigin: 'anonymous'}) : config
                  );
            } else {
              var config = {
                params: layerParams,
                url: options.url,
                projection: layerOptions.projection,
                gutter: 15
              };
              source = new ol.source.TileWMS(
                  gnViewerSettings.mapConfig.isExportMapAsImageEnabled ?
                  angular.extend(config, {crossOrigin: 'anonymous'}) : config);
            }

            // Set proxy for Cesium to load
            // layers not accessible with CORS headers
            // This is optional if the WMS provides CORS
            if (gnViewerSettings.cesiumProxy) {
              source.set('olcs.proxy', function(url) {
                return gnGlobalSettings.proxyUrl + encodeURIComponent(url);
              });
            }

            if(layerParams.useProxy
                && options.url.indexOf(gnGlobalSettings.proxyUrl) != 0) {
              options.url = gnGlobalSettings.proxyUrl
                              + encodeURIComponent(options.url);
            }

            var layerOptions = {
              url: options.url,
              type: 'WMS',
              opacity: options.opacity,
              visible: options.visible,
              source: source,
              legend: options.legend,
              attribution: options.attribution,
              attributionUrl: options.attributionUrl,
              label: options.label,
              group: options.group,
              advanced: options.advanced,
              minResolution: options.minResolution,
              maxResolution: options.maxResolution,
              cextent: options.extent,
              name: layerParams.LAYERS
            };
            if (gnViewerSettings.singleTileWMS) {
              olLayer = new ol.layer.Image(layerOptions);
            } else {
              olLayer = new ol.layer.Tile(layerOptions);
            }

            if (options.metadata) {
              olLayer.set('metadataUrl', options.metadata);
              var params = gnUrlUtils.parseKeyValue(
                  options.metadata.split('?')[1]);
              var uuid = params.uuid || params.id;
              if (!uuid) {
                var res = new RegExp(/\#\/metadata\/(.*)/g).
                    exec(options.metadata);
                if (angular.isArray(res) && res.length == 2) {
                  uuid = res[1];
                }
              }
              if (uuid) {
                olLayer.set('metadataUuid', uuid);
              }
            }
            ngeoDecorateLayer(olLayer);
            olLayer.displayInLayerManager = true;

            var unregisterEventKey = olLayer.getSource().on(
                (gnViewerSettings.singleTileWMS) ?
                'imageloaderror' : 'tileloaderror',
                function(tileEvent, target) {
                  var url = tileEvent.tile && tileEvent.tile.getKey ?
                      tileEvent.tile.getKey() : '- no tile URL found-';

                  var layer = tileEvent.currentTarget &&
                      tileEvent.currentTarget.getParams ?
                      tileEvent.currentTarget.getParams().LAYERS :
                      layerParams.LAYERS;

                  var msg = $translate.instant('layerTileLoadError', {
                    url: url,
                    layer: layer
                  });
                  $rootScope.$broadcast('StatusUpdated', {
                    msg: msg,
                    timeout: 0,
                    type: 'danger'});
                  olLayer.get('errors').push(msg);
                  olLayer.getSource().unByKey(unregisterEventKey);

                  gnWmsQueue.error({
                    url: url,
                    name: layer,
                    msg: msg
                  });
                });
            return olLayer;
          },

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#createOlWMSFromCap
           *
           * @description
           * Parse an object describing a layer from
           * a getCapabilities document parsing. Create a ol.Layer WMS
           * from this object and add it to the map with all known
           * properties.
           *
           * @param {ol.map} map to add the layer
           * @param {Object} getCapLayer object to convert
           * @param {string} url of the wms service (we want this one instead
           *  of the one from the capabilities to be sure its persistent)
           * @param {string} style of the style to use
           * @return {ol.Layer} the created layer
           */
          createOlWMSFromCap: function(map, getCapLayer, url, style) {

            var legend, attribution, attributionUrl, metadata, errors = [];
            if (getCapLayer) {

              var isLayerAvailableInMapProjection = false;
              // OL3 only parse CRS from WMS 1.3 (and not SRS in WMS 1.1.x)
              // so a WMS 1.1.x will always failed on this
              // https://github.com/openlayers/ol3/blob/master/src/
              // ol/format/wmscapabilitiesformat.js
              /*
              if (layer.CRS) {
                var mapProjection = map.getView().getProjection().getCode();
                for (var i = 0; i < layer.CRS.length; i++) {
                  if (layer.CRS[i] === mapProjection) {
                    isLayerAvailableInMapProjection = true;
                    break;
                  }
                }
              } else {
                errors.push($translate.instant('layerCRSNotFound'));
                console.warn($translate.instant('layerCRSNotFound'));
              }
              if (!isLayerAvailableInMapProjection) {
                errors.push($translate.instant('layerNotAvailableInMapProj'));
                console.warn($translate.instant('layerNotAvailableInMapProj'));
              }
              */

              // TODO: parse better legend & attribution
              var requestedStyle = null;
              var legendUrl;

              if (style && this.containsStyles(getCapLayer)) {
                for (var i = 0; i < getCapLayer.Style.length; i++) {
                  var s = getCapLayer.Style[i];
                  if (s.Name === style.Name) {
                    requestedStyle = s;
                    legendUrl = s.LegendURL[0];
                    break;
                  }
                }
              }

              if (!requestedStyle && this.containsStyles(getCapLayer)) {
                legendUrl = (getCapLayer.Style[0].LegendURL) ?
                    getCapLayer.Style[0].LegendURL[0] : undefined;
              }

              if (legendUrl) {
                legend = legendUrl.OnlineResource;
              }

              if (angular.isDefined(getCapLayer.Attribution)) {
                if (angular.isArray(getCapLayer.Attribution)) {

                } else {
                  attribution = getCapLayer.Attribution.Title;
                  if (getCapLayer.Attribution.OnlineResource) {
                    attributionUrl = getCapLayer.Attribution.OnlineResource;
                  }
                }
              }
              if (angular.isArray(getCapLayer.MetadataURL)) {
                metadata = getCapLayer.MetadataURL[0].OnlineResource;
              }

              var layerParam = {LAYERS: getCapLayer.Name};
              if (getCapLayer.version) {
                layerParam.VERSION = getCapLayer.version;
              }
              if (requestedStyle) {
                layerParam.STYLES = requestedStyle.Name;
              } else {
                // The first style element is the default style
                var defaultStyle;
                if (this.containsStyles(getCapLayer)) {
                  defaultStyle = getCapLayer.Style[0];
                }
                if(defaultStyle) {
                  // Set a casual style if available
                  // to avoid issues on ESRI services
                  layerParam.STYLES = defaultStyle.Name;
                } else {
                  // This is a problem for ESRI services
                  // where STYLES is a mandatory field
                  layerParam.STYLES = '';
                }
              }

              var projCode = map.getView().getProjection().getCode();
              if (getCapLayer.CRS) {
                if (!getCapLayer.CRS.includes(projCode)) {
                  if (projCode == 'EPSG:3857' &&
                      getCapLayer.CRS.includes('EPSG:900913')) {
                  }
                  else if (getCapLayer.CRS.includes('EPSG:4326')) {
                    projCode = 'EPSG:4326';
                  }
                }
              }

              url = getCapLayer.url || url;
              if (url.slice(-1) === '?') {
                url = url.substring(0, url.length-1);
              }

              if(getCapLayer.useProxy
                  && url.indexOf(gnGlobalSettings.proxyUrl) != 0) {
                url = gnGlobalSettings.proxyUrl + encodeURIComponent(url);
              }
              var layer = this.createOlWMS(map, layerParam, {
                url: url,
                label: getCapLayer.Title,
                attribution: attribution,
                attributionUrl: attributionUrl,
                projection: projCode,
                legend: legend,
                group: getCapLayer.group,
                metadata: metadata,
                extent: gnOwsCapabilities.getLayerExtentFromGetCap(map,
                    getCapLayer),
                minResolution: this.getResolutionFromScale(
                    map.getView().getProjection(),
                    getCapLayer.MinScaleDenominator),
                maxResolution: this.getResolutionFromScale(
                    map.getView().getProjection(),
                    getCapLayer.MaxScaleDenominator)
              });

              if (angular.isArray(getCapLayer.Dimension)) {
                for (var i = 0; i < getCapLayer.Dimension.length; i++) {
                  var dimension = getCapLayer.Dimension[i];
                  if (dimension.name == 'elevation') {
                    layer.set('elevation', {
                      units: dimension.units,
                      values: dimension.values.split(',')
                    });
                  }
                  if (dimension.name == 'time') {
                    layer.set('time',
                        dimension.values.split(','));
                  }
                }
              }
              if (angular.isArray(getCapLayer.Style) &&
                  getCapLayer.Style.length > 1) {
                layer.set('style', getCapLayer.Style);
              }
              if (requestedStyle) {
                layer.set('currentStyle', requestedStyle);
              }

              layer.set('advanced', !!(layer.get('elevation') ||
                  layer.get('time') || layer.get('style')));

              layer.set('errors', errors);

              //add the capabilities info, having available formats
              layer.set('capRequest', getCapLayer.capRequest||null);

              return layer;
            }

          },

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#containsStyles
           *
           * @description
           * Check if CapabilityLayer contains a not empty
           * styles array
           *
           * @param {getCapLayer} Capability Layer
           * @return {boolean} true if contains a not empty Style array
           */
          containsStyles: function(capLayer) {
            if (angular.isArray(capLayer.Style) &&
                capLayer.Style.length > 0) {
              return true;
            } else {
              return false;
            }
          },

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#createOlWMFFromCap
           *
           * @description
           * Parse an object describing a layer from
           * a getCapabilities document parsing. Create a ol.Layer WFS
           * from this object and add it to the map with all known
           * properties.
           *
           * @param {ol.map} map to add the layer
           * @param {Object} getCapLayer object to convert
           * @return {ol.Layer} the created layer
           */
          createOlWFSFromCap: function(map, getCapLayer, url) {

            var legend, attribution, metadata, errors = [];
            if (getCapLayer) {
              var layer = getCapLayer;

              var isLayerAvailableInMapProjection = false;

              if (layer.CRS) {
                var mapProjection = map.getView().
                    getProjection().getCode();
                for (var i = 0; i < layer.CRS.length; i++) {
                  if (layer.CRS[i] === mapProjection) {
                    isLayerAvailableInMapProjection = true;
                    break;
                  }
                }
              } else if (layer.defaultSRS) {
                var mapProjection = map.getView().
                    getProjection().getCode();
                var srs = layer.defaultSRS;
                if ((srs.indexOf('urn:ogc:def:crs:EPSG::') === 0) ||
                    (srs.indexOf('urn:x-ogc:def:crs:EPSG::') === 0)) {
                  srs = 'EPSG:' + srs.split('::')[srs.split('::').length - 1];
                } else if ((srs.indexOf('urn:ogc:def:crs:EPSG:') === 0) ||
                           (srs.indexOf('urn:x-ogc:def:crs:EPSG:') === 0)) {
                  srs = 'EPSG:' + srs.split(':')[srs.split(':').length - 1];
                }
                if (srs === mapProjection) {
                  isLayerAvailableInMapProjection = true;
                }
              } else if (layer.otherSRS) {
                var mapProjection = map.getView().
                    getProjection().getCode();
                for (var i = 0; i < layer.otherSRS.length; i++) {
                  var srs = layer.otherSRS[i];
                  if ((srs.indexOf('urn:ogc:def:crs:EPSG::') === 0) ||
                      (srs.indexOf('urn:x-ogc:def:crs:EPSG::') === 0)) {
                    srs = 'EPSG:' + srs.split('::')[srs.split('::').length - 1];
                  } else if ((srs.indexOf('urn:ogc:def:crs:EPSG:') === 0) ||
                             (srs.indexOf('urn:x-ogc:def:crs:EPSG:') === 0)) {
                    srs = 'EPSG:' + srs.split(':')[srs.split(':').length - 1];
                  }
                  if (srs === mapProjection) {
                    isLayerAvailableInMapProjection = true;
                    break;
                  }
                }
              } else {
                gnAlertService.addAlert({
                  msg: $translate.instant('layerCRSNotFound'),
                  delay: 5000,
                  type: 'warning'});
              }

              if (!isLayerAvailableInMapProjection) {
				gnAlertService.addAlert({
                  msg: $translate.instant('layerNotAvailableInMapProj',{proj:mapProjection}),
                  delay: 5000,
                  type: 'warning'});
              }

              // TODO: parse better legend & attribution
              if (angular.isArray(layer.Style) && layer.Style.length > 0) {
                var urlLegend = layer.Style[layer.Style.length - 1]
                    .LegendURL[0];
                if (urlLegend) {
                  legend = urlLegend.OnlineResource;
                }
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


              var vectorFormat = null;

              if(getCapLayer.version == '1.0.0') {
                vectorFormat = new ol.format.WFS(
                {
                    gmlFormat : new ol.format.GML2({
                        featureNS: getCapLayer.name.prefix,
                        featureType: getCapLayer.name.localPart,
                        srsName: map.getView().getProjection().getCode()
                      })
                  }
               );
              } else {
                  //Default format
                  var vectorFormat = new ol.format.WFS();
              }

              var vectorSource = new ol.source.Vector({
                format: vectorFormat,
                loader: function(extent, resolution, projection) {
                  if (this.loadingLayer) {
                    return;
                  }

                  this.loadingLayer = true;

                  var parts = url.split('?');

                  var urlGetFeature = gnUrlUtils.append(parts[0],
                      gnUrlUtils.toKeyValue({
                        service: 'WFS',
                        request: 'GetFeature',
                        version: getCapLayer.version,
                        srsName: map.getView().getProjection().getCode(),
                        bbox: extent.join(','),
                        typename: getCapLayer.name.prefix + ':' +
                                   getCapLayer.name.localPart}));

                  //Fix, ArcGIS fails if there is a bbox:
                  if(getCapLayer.version == '1.1.0') {
                    urlGetFeature = gnUrlUtils.append(parts[0],
                        gnUrlUtils.toKeyValue({
                          service: 'WFS',
                          request: 'GetFeature',
                          version: getCapLayer.version,
                          srsName: map.getView().getProjection().getCode(),
                          typename: getCapLayer.name.prefix + ':' +
                                     getCapLayer.name.localPart}));
                  }



                  //If this goes through the proxy, don't remove parameters
                  if(getCapLayer.useProxy
                      && urlGetFeature.indexOf(gnGlobalSettings.proxyUrl) != 0) {
                    urlGetFeature = gnGlobalSettings.proxyUrl
                                        + encodeURIComponent(urlGetFeature);
                  }

                  $.ajax({
                    url: urlGetFeature
                  })
                      .done(function(response) {
                        // TODO: Check WFS exception
                        vectorSource.addFeatures(vectorFormat.
                            readFeatures(response));
                      })
                      .then(function() {
                        this.loadingLayer = false;
                      });
                },
                strategy: ol.loadingstrategy.bbox,
                projection: map.getView().getProjection().getCode(),
                url: url
              });

              var extent = null;

              //Add spatial extent
              if (layer.wgs84BoundingBox && layer.wgs84BoundingBox[0] &&
                  layer.wgs84BoundingBox[0].lowerCorner &&
                  layer.wgs84BoundingBox[0].upperCorner) {
                extent = ol.extent.boundingExtent(
                    [layer.wgs84BoundingBox[0].lowerCorner,
                     layer.wgs84BoundingBox[0].upperCorner]);

                extent = ol.proj.transformExtent(
                    extent,
                    'EPSG:4326',
                    map.getView().getProjection().getCode());
              }

              if (extent) {
                map.getView().fit(extent, map.getSize());
              }

              var layer = new ol.layer.Vector({
                source: vectorSource,
                extent: extent
              });
              layer.set('errors', errors);
              layer.set('featureTooltip', true);
              layer.set('url', url);
              layer.set('wfs', url);
              ngeoDecorateLayer(layer);
              layer.displayInLayerManager = true;
              layer.set('label', getCapLayer.title ||
                (getCapLayer.name.prefix + ':' + getCapLayer.name.localPart));
              return layer;
            }

          },

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#addWmsToMapFromCap
           *
           * @description
           * Add a new ol.Layer object to the map from a capabilities parsed
           * ojbect.
           *
           * @param {ol.map} map to add the layer
           * @param {Object} getCapLayer object to convert
           * @param {string} style of the style to use
           */
          addWmsToMapFromCap: function(map, getCapLayer, style) {
            var layer = this.createOlWMSFromCap(map, getCapLayer, null, style);
            map.addLayer(layer);
            return layer;
          },

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#addWfsToMapFromCap
           *
           * @description
           * Add a new ol.Layer object to the map from a capabilities parsed
           * ojbect.
           *
           * @param {ol.map} map to add the layer
           * @param {Object} getCapLayer object to convert
           * @param {String} url of the service
           */
          addWfsToMapFromCap: function(map, getCapLayer, url) {
            var layer = this.createOlWFSFromCap(map, getCapLayer, url);
            map.addLayer(layer);
            return layer;
          },

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#addWmsToMap
           *
           * @description
           * Create a new WMS layer from basic info object containing
           * the name of the layer and the url of the service.
           *
           * @param {ol.map} map to add the layer
           * @param {Object} layerInfo object
           * @return {ol.Layer} the created layer
           */
          addWmsToMap: function(map, layerInfo) {
            if (layerInfo) {
              var layer = this.createOlWMS(map, {
                LAYERS: layerInfo.name
              }, {
                url: layerInfo.url,
                label: layerInfo.name
              }
              );
              map.addLayer(layer);
              return layer;
            }
          },

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#addWmsFromScratch
           *
           * @description
           * Here is the method to use when you want to add a wms layer from
           * a url and a layername. It will call the WMS getCapabilities,
           * create the ol.Layer with maximum info we got from capabilities,
           * then add the layer to the map.
           *
           * If the layer is not found in the capability, a simple WMS layer
           * based on the name only will be created.
           *
           * Return a promise with ol.Layer as data is succeed, and url/name
           * if failure.
           * If createOnly, we don't add the layer to the map.
           * If the md object is given, we add it to the layer, or we try
           * to retrieve it in the catalog
           *
           * @param {ol.Map} map to add the layer
           * @param {string} url of the service
           * @param {string} name of the layer
           * @param {boolean} createOnly or add it to the map
           * @param {!Object} md object
           */
          addWmsFromScratch: function(map, url, name, createOnly, md, version, style) {
            var defer = $q.defer();
            var $this = this;

            if (!isLayerInMap(map, name, url)) {
              gnWmsQueue.add(url, name);
              gnOwsCapabilities.getWMSCapabilities(url).then(function(capObj) {
                var capL = gnOwsCapabilities.getLayerInfoFromCap(
                    name, capObj, md && md.getUuid && md.getUuid()),
                    olL;

                if (!capL) {
                  // If layer not found in the GetCapabilities
                  // Try to add the layer from the metadata
                  // information only. A tile error loading
                  // may be reported after the layer is added
                  // to the map and will give more details.
                  var errormsg = $translate.instant(
                      'layerNotfoundInCapability', {
                        layer: name,
                        type: 'wms',
                        url: encodeURIComponent(url)
                      });
                  var o = {
                    url: url,
                    name: name,
                    msg: errormsg
                  }, errors = [];
                  if (version) {
                    o.version = version;
                  }
                  olL = $this.addWmsToMap(map, o);

                  if(olL && md) {
                    olL.set('md', md);
                  }

                  if (!angular.isArray(olL.get('errors'))) {
                    olL.set('errors', []);
                  }
                  errors.push(errormsg);
                  console.warn(errormsg);

                  olL.get('errors').push(errors);

                  gnWmsQueue.error(o);
                  o.layer = olL;
                  defer.reject(o);
                } else {

                  //check if proxy is needed
                  var _url = url.split('/');
                  _url = _url[0] + '/' + _url[1] + '/' + _url[2] + '/';
                  if ($.inArray(_url, gnGlobalSettings.requireProxy) >= 0
                    && url.indexOf(gnGlobalSettings.proxyUrl) != 0) {
              	       capL.useProxy = true;
  	              }

                  olL = $this.createOlWMSFromCap(map, capL, url, style);

                  var finishCreation = function() {

                    $q.resolve(olL).
                        then(gnViewerSettings.getPreAddLayerPromise).
                        finally(
                        function() {
                          if (!createOnly) {
                            map.addLayer(olL);
                          }
                          gnWmsQueue.removeFromQueue(url, name);
                          defer.resolve(olL);
                        });
                  };

                  var feedMdPromise = md ?
                    $q.resolve(md).then(function(md) {
                      olL.set('md', md);
                    }) : $this.feedLayerMd(olL);

                  feedMdPromise.then(finishCreation);
                }

              }, function(error) {
                var o = {
                  url: url,
                  name: name,
                  msg: $translate.instant('getCapFailure') +
                    (error  ? ', ' + error : '')
                };
                gnWmsQueue.error(o);
                defer.reject(o);
              });
            } else {
            	var olL = getTheLayerFromMap(map, name, url);
                if(olL && md) {
                  olL.set('md', md);
                }
            }
            return defer.promise;
          },

          /**
           * Call a WMS getCapabilities and create ol3 layers for all items.
           * Add them to the map if `createOnly` is false;
           *
           * @param {ol.Map} map to add the layer
           * @param {string} url of the service
           * @param {string} name of the layer
           * @param {boolean} createOnly or add it to the map
           */
          addWmsAllLayersFromCap: function(map, url, createOnly) {
            var $this = this;

            return gnOwsCapabilities.getWMSCapabilities(url).
                then(function(capObj) {

                  var createdLayers = [];

                  var layers = capObj.layers || capObj.Layer;
                  for (var i = 0, len = layers.length; i < len; i++) {
                    var capL = layers[i];
                    var olL = $this.createOlWMSFromCap(map, capL);
                    if (!createOnly) {
                      map.addLayer(olL);
                    }
                    createdLayers.push(olL);
                  }
                  return createdLayers;
                });
          },

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#addWmtsFromScratch
           *
           * @description
           * Here is the method to use when you want to add a wmts layer from
           * a url and a layername. It will call the WMTS getCapabilities,
           * create the ol.Layer with maximum info we got from capabilities,
           * then add the layer to the map.
           *
           * If the layer is not found in the capability, the layer will not
           * be created.
           *
           * Return a promise with ol.Layer as data is succeed, and url/name
           * if failure.
           * If createOnly, we don't add the layer to the map.
           * If the md object is given, we add it to the layer, or we try
           * to retrieve it in the catalog
           *
           * @param {ol.Map} map to add the layer
           * @param {string} url of the service
           * @param {string} name of the layer
           * @param {boolean} createOnly or add it to the map
           * @param {!Object} md object
           */
          addWmtsFromScratch: function(map, url, name, createOnly, md) {
            var defer = $q.defer();
            var $this = this;

            if (!isLayerInMap(map, name, url)) {
              gnWmsQueue.add(url, name, map);
              gnOwsCapabilities.getWMTSCapabilities(url).then(function(capObj) {

                var capL = gnOwsCapabilities.getLayerInfoFromCap(
                    name, capObj, md && md.getUuid());
                if (!capL) {
                  gnWmsQueue.removeFromQueue(url, name, map);
                  // If layer not found in the GetCapabilities
                  gnAlertService.addAlert({
                    msg: $translate.instant('layerNotfoundInCapability', {
                    layer: name,
                    type: 'wmts',
                    url: encodeURIComponent(url)
                    }),
                    delay: 20000,
                    type: 'warning'});
                  var o = {
                    url: url,
                    name: name,
                    msg: ""
                  }, errors = [];
                  defer.reject(o);
                }
                else {
                  var olL = $this.createOlWMTSFromCap(map, capL, capObj);

                  var finishCreation = function() {
                    if (!createOnly) {
                      map.addLayer(olL);
                    }
                    gnWmsQueue.removeFromQueue(url, name, map);
                    defer.resolve(olL);
                  };

                  // attach the md object to the layer
                  if (md) {
                    olL.set('md', md);
                    finishCreation();
                  }
                  else {
                    $this.feedLayerMd(olL).finally(finishCreation);
                  }
                }
              }, function() {
                var o = {
                  url: url,
                  name: name,
                  msg: $translate.instant('getCapFailure')
                };
                gnWmsQueue.error(o);
                defer.reject(o);
              });
            }
            return defer.promise;
          },


          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#addWfsFromScratch
           *
           * @description
           * Here is the method to use when you want to add a wfs layer from
           * a url and a layername. It will call the WFS getCapabilities,
           * create the ol.Layer with maximum info we got from capabilities,
           * then add the layer to the map.
           *
           * If the layer is not found in the capabilities, a simple WFS layer
           * based on the name only will be created.
           *
           * Return a promise with ol.Layer as data is succeed, and url/name
           * if failure.
           * If createOnly, we don't add the layer to the map.
           * If the md object is given, we add it to the layer, or we try
           * to retrieve it in the catalog
           *
           * @param {ol.Map} map to add the layer
           * @param {string} url of the service
           * @param {string} name of the layer
           * @param {boolean} createOnly or add it to the map
           * @param {!Object} md object
           */
          addWfsFromScratch: function(map, url, name, createOnly, md) {
            var defer = $q.defer();
            var $this = this;

            gnWmsQueue.add(url, name, map);
            gnWfsService.getCapabilities(url).then(function(capObj) {
              var capL = gnOwsCapabilities.
                  getLayerInfoFromWfsCap(name, capObj, md.getUuid()),
                  olL;
              if (!capL) {
                gnWmsQueue.removeFromQueue(url, name, map);
                // If layer not found in the GetCapabilities
                gnAlertService.addAlert({
                  msg: $translate.instant('layerNotfoundInCapability', {
                  layer: name,
                  type: 'wfs',
                  url: encodeURIComponent(url)
                  }),
                  delay: 20000,
                  type: 'warning'});
                var o = {
                  url: url,
                  name: name,
                  msg: ""
                }, errors = [];
                defer.reject(o);
              } else {
                olL = $this.addWfsToMapFromCap(map, capL, url);

                // attach the md object to the layer
                if (md) {
                  olL.set('md', md);
                }
                else {
                  $this.feedLayerMd(olL);
                }

                gnWmsQueue.removeFromQueue(url, name, map);
                defer.resolve(olL);
              }

            }, function() {
              var o = {
                url: url,
                name: name,
                msg: $translate.instant('getCapFailure')
              };
              gnWmsQueue.error(o);
              defer.reject(o);
            });
            return defer.promise;
          },
          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#createOlWMTSFromCap
           *
           * @description
           * Parse an object describing a layer from
           * a getCapabilities document parsing. Create a ol.Layer WMS
           * from this object and add it to the map with all known
           * properties.
           *
           * @param {ol.map} map to add the layer to
           * @param {Object} getCapLayer object
           * @return {ol.layer.Tile} created layer
           */
          createOlWMTSFromCap: function(map, getCapLayer, capabilities) {

            var legend, attribution, metadata;
            if (getCapLayer && capabilities) {

              //Asking WMTS service about capabilities
              var cap = {
                  Contents: capabilities,
                  OperationsMetadata: capabilities.operationsMetadata
              };

              //OpenLayers expects an array of style objects having isDefault property
              angular.forEach(cap.Contents.Layer,function(l){
                if (!angular.isArray(l.Style)){ l.Style=[{Identifier:l.Style,isDefault:true}] };
              });

              var options = ol.source.WMTS.optionsFromCapabilities(cap, {
                layer: getCapLayer.Identifier,
                matrixSet: map.getView().getProjection().getCode(),
                projection: map.getView().getProjection().getCode()
              });

              //Configuring url for service
              var url = capabilities.operationsMetadata.GetCapabilities.DCP.HTTP.Get[0].href;

              //Configuring url for capabilities
              var urlCap = capabilities.operationsMetadata.GetCapabilities.DCP.HTTP.Get[0].href;
              var urlCapType = capabilities.operationsMetadata.GetCapabilities.
              DCP.HTTP.Get[0].Constraint[0].AllowedValues.Value[0].toLowerCase();

              if (urlCapType == 'restful') {
                if (urlCap.indexOf('/1.0.0/WMTSCapabilities.xml') == -1) {
                  urlCap = urlCap + '/1.0.0/WMTSCapabilities.xml';
                }
              } else {
                var parts = urlCap.split('?');

                urlCap = gnUrlUtils.append(parts[0],
                    gnUrlUtils.toKeyValue({
                      service: 'WMTS',
                      request: 'GetCapabilities',
                      version: '1.0.0'}));
              }

              //Create layer
              var olLayer = new ol.layer.Tile({
                extent: map.getView().getProjection().getExtent(),
                name: getCapLayer.Identifier,
                title: getCapLayer.Title,
                label: getCapLayer.Title,
                source: new ol.source.WMTS(options),
                url: url,
                urlCap: urlCap,
                cextent: gnOwsCapabilities.getLayerExtentFromGetCap(map,
                    getCapLayer)
              });

              //Add GN extras to layer
              ngeoDecorateLayer(olLayer);
              olLayer.displayInLayerManager = true;

              //Like add a link to metadata
              if (angular.isArray(getCapLayer.MetadataURL)) {
                var metadata = getCapLayer.MetadataURL[0].OnlineResource;
                olLayer.set('metadataUrl', metadata);

                var params = gnUrlUtils.parseKeyValue(
                    metadata.split('?')[1]);
                var uuid = params.uuid || params.id;
                if (!uuid) {
                  var res = new RegExp(/\#\/metadata\/(.*)/g).
                  exec(metadata);
                  if (angular.isArray(res) && res.length == 2) {
                    uuid = res[1];
                  }
                }
                if (uuid) {
                  olLayer.set('metadataUuid', uuid);
                }
              }

              return olLayer;
            } else {
              //With no capabilities, how are we supposed to...?
              //Should we show something on screen?
              console.warn("Called createOlWMTSFromCap with no capabilities...");
            }
          },

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#addWmtsToMapFromCap
           *
           * @description
           * Add a new WMTS ol.Layer object to the map from a capabilities
           * parsed ojbect.
           *
           * @param {ol.map} map to add the layer
           * @param {Object} getCapLayer object to convert
           */
          addWmtsToMapFromCap: function(map, getCapLayer, capabilities) {
            map.addLayer(this.createOlWMTSFromCap(map,
                getCapLayer, capabilities));
          },
          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#zoom
           *
           * @description
           * Zoom by delta with animation
           * @param {ol.map} map obj
           * @param {float} delta for zoom
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
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#zoomLayerToExtent
           *
           * @description
           * Zoom map to the layer extent if defined. The layer extent
           * is gotten from capabilities and store in cextent property
           * of the layer.
           *
           * @param {ol.Layer} layer for the extent
           * @param {ol.map} map obj
           */
          zoomLayerToExtent: function(layer, map) {
            if (layer.get('cextent')) {
              map.getView().fit(layer.get('cextent'), map.getSize());
            }
          },


          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#createLayerForType
           *
           * @description
           * Creates an ol.layer for a given type. Useful for contexts
           * DEPRECATED: use createLayerFromProperties instead!!
           *
           * @param {string} type of the layer to create
           * @param {Object} opt for url or layer name
           * @param {string} title optional title
           * @param {ol.Map} map required for WMTS and WMS
           * @return {ol.layer} layer
           */
          createLayerForType: function(type, opt, title, map) {
            switch (type) {
              case 'osm':
                return new ol.layer.Tile({
                  source: new ol.source.OSM(),
                  title: title ||  'OpenStreetMap'
                });
              //ALEJO: tms support
              case 'tms':
                return new ol.layer.Tile({
                  source: new ol.source.XYZ({
                        url: opt.url
                  }),
                  title: title ||  'TMS Layer'
                });
              case 'bing_aerial':
                return new ol.layer.Tile({
                  preload: Infinity,
                  source: new ol.source.BingMaps({
                    key: gnViewerSettings.bingKey,
                    imagerySet: 'Aerial'
                  }),
                  title: title ||  'Bing Aerial'
                });
              case 'stamen':
                //We make watercolor the default layer
                var type = opt && opt.name ? opt.name : 'watercolor',
                    source = new ol.source.Stamen({
                      layer: type
                    });
                source.set('type', type);
                return new ol.layer.Tile({
                  source: source,
                  title: title ||  'Stamen'
                });

              case 'wmts':
                if (!opt.name || !opt.url) {
                  $log.warn('One of the required parameters (name, url) ' +
                      'is missing in the specified WMTS layer:',
                      opt);
                  break;
                }
                this.addWmtsFromScratch(map, opt.url, opt.name)
                    .then(function(layer) {
                      if (title) {
                        layer.set('title', title);
                        layer.set('label', title);
                      }
                      return layer;
                    });
                break;

              case 'wms':
                if (!opt.name || !opt.url) {
                  $log.warn('One of the required parameters (name, url) ' +
                      'is missing in the specified WMS layer:',
                      opt);
                  break;
                }
                this.addWmsFromScratch(map, opt.url, opt.name)
                    .then(function(layer) {
                      if (title) {
                        layer.set('title', title);
                        layer.set('label', title);
                      }
                      return layer;
                    });
                break;
            }
          },

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#isLayerInMap
           *
           * @description
           * Check if the layer is in the map to avoid adding duplicated ones.
           *
           * @param {ol.Map} map obj
           * @param {string} name of the layer
           * @param {string} url of the service
           */
          isLayerInMap: isLayerInMap,

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#getLayerInMap
           *
           * @description
           * Return a layer if one found with same name and service
           *
           * @param {ol.Map} map obj
           * @param {string} name of the layer
           * @param {string} url of the service
           */
          getLayerInMap: getLayerInMap,

          /**
           * @ngdoc method
           * @methodOf gn_map.service:gnMap
           * @name gnMap#feedLayerMd
           *
           * @description
           * If the layer contains a metadataUrl, we check if it is on
           * the same host as the catalog, if yes i search for this md in
           * the catalog and bind it to the layer.
           *
           * @param {ol.Layer} layer to feed
           */
          feedLayerMd: function(layer) {
            var defer = $q.defer();
            var $this = this;

            defer.resolve(layer);

            if (layer.get('metadataUrl') && layer.get('metadataUuid')) {

              return gnSearchManagerService.gnSearch({
                uuid: layer.get('metadataUuid'),
                fast: 'index',
                _draft: 'n or e',
                _content_type: 'json'
              }).then(function(data) {
                if (data.metadata.length == 1) {
                  var md = new Metadata(data.metadata[0]);
                  layer.set('md', md);

                  var mdLinks = md.getLinksByType('#OGC:WMTS',
                      '#OGC:WMS', '#OGC:WMS-1.1.1-http-get-map');

                  angular.forEach(mdLinks, function(link) {
                    if (layer.get('url').indexOf(link.url) >= 0 &&
                        link.name == layer.getSource().getParams().LAYERS) {
                      this.feedLayerWithRelated(layer, link.group);
                      return;
                    }
                  }, $this);
                }
                return layer;
              });
            }
            return defer.promise;
          },

          /**
           * Check for online resource that could be bound to the layer.
           * WPS, downloads, WFS etc..
           *
           * @param {ol.Layer} layer
           * @param {string} linkGroup
           */
          feedLayerWithRelated: function(layer, linkGroup) {
            var md = layer.get('md');

            if (!linkGroup) {
              console.warn('The layer has not been found in any group: ' +
                  layer.getSource().getParams().LAYERS);
              return;
            }

            // We can bind layer and download/process
            if (md.getLinksByType(linkGroup, '#OGC:WMTS',
                '#OGC:WMS', '#OGC:WMS-1.1.1-http-get-map').length == 1) {

              var downloads = md && md.getLinksByType(linkGroup,
                  'WWW:DOWNLOAD-1.0-link--download', 'FILE', 'DB',
                  'WFS', 'WCS', 'COPYFILE');
              layer.set('downloads', downloads);

              var wfs = md && md.getLinksByType(linkGroup, '#WFS');
              layer.set('wfs', wfs);

              var process = md && md.getLinksByType(linkGroup, 'OGC:WPS');
              layer.set('processes', process);
            }
          },

          /**
           * Return a secured extent that is contained in projection max extent.
           * @param {Array} extent
           * @param {Array} proj
           * @return {ol.Extent} intersected extent
           */
          secureExtent: function(extent, proj) {
            return ol.extent.getIntersection(extent, proj.getExtent());
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
          return layer.displayInLayerManager && !layer.get('fromWps');
        },
        visible: function(layer) {
          return layer.displayInLayerManager && layer.visible;
        }
      };
    };
  });

  // isInteger polyfill for IE
  Number.isInteger = Number.isInteger || function(value) {
    return typeof value === 'number' &&
        isFinite(value) &&
        Math.floor(value) === value;
  };
})();
