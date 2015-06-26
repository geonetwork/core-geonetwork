(function() {
  goog.provide('gn_owscontext_service');







  goog.require('Filter_1_0_0');
  goog.require('GML_2_1_2');
  goog.require('OWC_0_3_1');
  goog.require('OWS_1_0_0');
  goog.require('SLD_1_0_0');
  goog.require('XLink_1_0');

  var module = angular.module('gn_owscontext_service', []);

  // OWC Client
  // Jsonix wrapper to read or write OWS Context
  var context = new Jsonix.Context(
      [XLink_1_0, OWS_1_0_0, Filter_1_0_0, GML_2_1_2, SLD_1_0_0, OWC_0_3_1],
      {
        namespacePrefixes: {
          'http://www.w3.org/1999/xlink': 'xlink',
          'http://www.opengis.net/ows': 'ows'
        }
      }
      );
  var unmarshaller = context.createUnmarshaller();
  var marshaller = context.createMarshaller();

  /**
   * @ngdoc service
   * @kind function
   * @name gn_viewer.service:gnOwsContextService
   * @requires gnMap
   * @requires gnOwsCapabilities
   * @requires gnEditor
   * @requires gnViewerSettings
   *
   * @description
   * The `gnOwsContextService` service provides tools to load and store OWS
   * Context.
   */
  module.service('gnOwsContextService', [
    'gnMap',
    'gnOwsCapabilities',
    '$http',
    'gnViewerSettings',
    '$translate',
    '$q',
    '$filter',
    'gnWmsQueue',
    function(gnMap, gnOwsCapabilities, $http, gnViewerSettings,
             $translate, $q, $filter) {

      /**
       * @ngdoc method
       * @name gnOwsContextService#loadContext
       * @methodOf gn_viewer.service:gnOwsContextService
       *
       * @description
       * Loads a context, ie. creates layers and centers the map
       *
       * @param {Object} context object
       */
      this.loadContext = function(text, map) {
        var context = unmarshaller.unmarshalString(text).value;
        // first remove any existing layer
        var layersToRemove = [];
        map.getLayers().forEach(function(layer) {
          if (layer.displayInLayerManager) {
            layersToRemove.push(layer);
          }
        });
        for (var i = 0; i < layersToRemove.length; i++) {
          map.removeLayer(layersToRemove[i]);
        }

        // set the General.BoundingBox
        var bbox = context.general.boundingBox.value;
        var ll = bbox.lowerCorner;
        var ur = bbox.upperCorner;
        var extent = ll.concat(ur);
        var projection = bbox.crs;
        // reproject in case bbox's projection doesn't match map's projection
        extent = ol.proj.transformExtent(extent, map.getView().getProjection(),
            projection);

        // store the extent into view settings so that it can be used later in
        // case the map is not visible yet
        gnViewerSettings.initialExtent = extent;
        map.getView().fitExtent(extent, map.getSize());

        // load the resources
        var layers = context.resourceList.layer;
        var i, j, olLayer, bgLayers = [];
        var self = this;
        var re = /type\s*=\s*([^,|^}|^\s]*)/;
        var promises = [];
        for (i = 0; i < layers.length; i++) {
          var layer = layers[i];
          if (layer.name) {
            if (layer.group == 'Background layers' &&
                layer.name.match(re)) {
              var type = re.exec(layer.name)[1];
              if (type != 'wmts') {
                var olLayer = gnMap.createLayerForType(type);
                if (olLayer) {
                  bgLayers.push({layer: olLayer, idx: i});
                  olLayer.displayInLayerManager = false;
                  olLayer.background = true;
                  olLayer.set('group', 'Background layers');
                  olLayer.setVisible(!layer.hidden);
                }
              }
              else {
                promises.push(this.createLayer(layer, map, i).then(
                    function(olLayer) {
                      bgLayers.push({
                        layer: olLayer,
                        idx: olLayer.get('bgIdx')
                      });
                      olLayer.displayInLayerManager = false;
                      olLayer.background = true;
                    }));
              }
            } else {
              var server = layer.server[0];
              if (server.service == 'urn:ogc:serviceType:WMS') {
                self.createLayer(layer, map);
              }
            }
          }
        }

        // if there's at least one valid bg layer in the context use them for
        // the application otherwise use the defaults from config
        $q.all(promises).finally (function() {
          if (bgLayers.length > 0) {
            // make sure we remove any existing bglayer
            if (map.getLayers().getLength() > 0) {
              map.getLayers().removeAt(0);
            }

            // first clear settings bgLayers
            if (!gnViewerSettings.bgLayers) {
              gnViewerSettings.bgLayers = [];
            }

            gnViewerSettings.bgLayers.length = 0;

            var firstVisibleBgLayer = true;
            bgLayers = $filter('orderBy')(bgLayers, 'idx');

            $.each(bgLayers, function(index, item) {
              gnViewerSettings.bgLayers.push(item.layer);
              // the first visible bg layer wins and get displayed in the map
              if (item.layer.getVisible() && firstVisibleBgLayer) {
                map.getLayers().insertAt(0, item.layer);
                firstVisibleBgLayer = false;
              }
            });
          }
        });
      };

      /**
       * @ngdoc method
       * @name gnOwsContextService#loadContextFromUrl
       * @methodOf gn_viewer.service:gnOwsContextService
       *
       * @description
       * Loads a context from an URL.
       * @param {string} url URL to context
       * @param {ol.map} map map
       */
      this.loadContextFromUrl = function(url, map, useProxy) {
        var self = this;
        if (useProxy) {
          url = '../../proxy?url=' + encodeURIComponent(url);
        }
        $http.get(url).success(function(data) {
          self.loadContext(data, map);
        });
      };

      /**
       * @ngdoc method
       * @name gnOwsContextService#writeContext
       * @methodOf gn_viewer.service:gnOwsContextService
       *
       * @description
       * Creates a javascript object based on map context then marshals it
       *    into XML
       * @param {ol.Map} context object
       */
      this.writeContext = function(map) {

        var extent = map.getView().calculateExtent(map.getSize());

        var general = {
          boundingBox: {
            name: {
              'namespaceURI': 'http://www.opengis.net/ows',
              'localPart': 'BoundingBox'
            },
            value: {
              crs: map.getView().getProjection().getCode(),
              lowerCorner: [extent[0], extent[1]],
              upperCorner: [extent[2], extent[3]]
            }
          }
        };

        var resourceList = {
          layer: []
        };

        // add the background layers
        // todo: grab this from config
        angular.forEach(gnViewerSettings.bgLayers, function(layer) {
          var source = layer.getSource();
          var name;
          var params = {
            hidden: map.getLayers().getArray().indexOf(layer) < 0,
            opacity: layer.getOpacity(),
            title: layer.get('title'),
            group: layer.get('group')
          };

          if (source instanceof ol.source.OSM) {
            name = '{type=osm}';
          } else if (source instanceof ol.source.MapQuest) {
            name = '{type=mapquest}';
          } else if (source instanceof ol.source.BingMaps) {
            name = '{type=bing_aerial}';
          } else if (source instanceof ol.source.WMTS) {
            name = '{type=wmts,name=' + layer.get('name') + '}';
            params.server = [{
              onlineResource: [{
                href: layer.get('urlCap')
              }],
              service: 'urn:ogc:serviceType:WMS'
            }];
          } else {
            return;
          }
          params.name = name;
          resourceList.layer.push(params);
        });

        map.getLayers().forEach(function(layer) {
          var source = layer.getSource();
          var url = '';
          var name;

          // background layers already taken into account
          if (layer.background) {
            return;
          }

          if (source instanceof ol.source.ImageWMS) {
            name = source.getParams().LAYERS;
            url = source.getUrl();
          } else if (source instanceof ol.source.TileWMS) {
            name = source.getParams().LAYERS;
            url = source.getUrls()[0];
          } else if (source instanceof ol.source.WMTS) {
            name = '{type=wmts,name=' + layer.get('name') + '}';
            url = layer.get('urlCap');
          }
          resourceList.layer.push({
            hidden: !layer.getVisible(),
            opacity: layer.getOpacity(),
            name: name,
            title: layer.get('title'),
            group: layer.get('group'),
            groupcombo: layer.get('groupcombo'),
            server: [{
              onlineResource: [{
                href: url
              }],
              service: 'urn:ogc:serviceType:WMS'
            }]
          });
        });

        var context = {
          version: '0.3.1',
          id: 'ows-context-ex-1-v3',
          general: general,
          resourceList: resourceList
        };

        var xml = marshaller.marshalDocument({
          name: {
            localPart: 'OWSContext',
            namespaceURI: 'http://www.opengis.net/ows-context',
            prefix: 'ows-context',
            string: '{http://www.opengis.net/ows-context}ows-context:OWSContext'
          },
          value: context
        });
        return xml;
      };

      /**
       * @ngdoc method
       * @name gnOwsContextService#writeContext
       * @methodOf gn_viewer.service:gnOwsContextService
       *
       * @description
       * Saves the map context to local storage
       *
       * @param {ol.Map} map object
       */
      this.saveToLocalStorage = function(map) {
        if (map.getSize()[0] == 0 || map.getSize()[1] == 0) {
          // don't save a map which has not been rendered yet
          return;
        }
        var xml = this.writeContext(map);
        var xmlString = (new XMLSerializer()).serializeToString(xml);
        window.localStorage.setItem('owsContext', xmlString);
      };

      /**
       * @ngdoc method
       * @name gnOwsContextService#createLayer
       * @methodOf gn_viewer.service:gnOwsContextService
       *
       * @description
       * Create a WMS ol.Layer from context object
       *
       * @param {Object} layer layer
       * @param {ol.map} map map
       * @param {numeric} bgIdx if it is a background layer, index in the
       * dropdown
       */
      this.createLayer = function(layer, map, bgIdx) {

        var server = layer.server[0];
        var res = server.onlineResource[0];
        var reT = /type\s*=\s*([^,|^}|^\s]*)/;
        var reL = /name\s*=\s*([^,|^}|^\s]*)/;

        var createOnly = angular.isDefined(bgIdx);

        if (layer.name.match(reT)) {
          var type = reT.exec(layer.name)[1];
          var name = reL.exec(layer.name)[1];

          if (type == 'wmts') {
            return gnMap.addWmtsFromScratch(map, res.href, name, createOnly).
                then(function(olL) {
                  olL.set('group', layer.group);
                  olL.set('groupcombo', layer.groupcombo);
                  olL.setOpacity(layer.opacity);
                  olL.setVisible(!layer.hidden);
                  if (layer.title) {
                    olL.set('title', layer.title);
                    olL.set('label', layer.title);
                  }
                  if (bgIdx) {
                    olL.set('bgIdx', bgIdx);
                  }
                  return olL;
                });
          }
        }
        else { // we suppose it's WMS

          return gnMap.addWmsFromScratch(map, res.href, layer.name, createOnly).
              then(function(olL) {
                olL.set('group', layer.group);
                olL.set('groupcombo', layer.groupcombo);
                olL.setOpacity(layer.opacity);
                olL.setVisible(!layer.hidden);
                if (layer.title) {
                  olL.set('title', layer.title);
                  olL.set('label', layer.title);
                }
                return olL;
              });
        }
      };
    }
  ]);
})();
