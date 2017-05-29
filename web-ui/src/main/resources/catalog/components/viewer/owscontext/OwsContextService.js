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
  goog.provide('gn_owscontext_service');







  goog.require('Filter_1_0_0');
  goog.require('GML_2_1_2');
  goog.require('OWC_0_3_1');
  goog.require('OWS_1_0_0');
  goog.require('SLD_1_0_0');
  goog.require('XLink_1_0');
  goog.require('gn_wfsfilter_service');

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
    '$rootScope',
    '$timeout',
    'gnGlobalSettings',
    'wfsFilterService',
    function(gnMap, gnOwsCapabilities, $http, gnViewerSettings,
             $translate, $q, $filter, $rootScope, $timeout, gnGlobalSettings,
             wfsFilterService) {


      var firstLoad = true;

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
            if (!(layer.get('fromUrlParams') && firstLoad)) {
              layersToRemove.push(layer);
            }
          }
        });
        for (var i = 0; i < layersToRemove.length; i++) {
          map.removeLayer(layersToRemove[i]);
        }

        // set the General.BoundingBox
        var bbox = context.general.boundingBox.value;
        var ll = bbox.lowerCorner;
        var ur = bbox.upperCorner;
        var projection = bbox.crs;

        if (projection == 'EPSG:4326') {
          ll.reverse();
          ur.reverse();
        }
        var extent = ll.concat(ur);
        // reproject in case bbox's projection doesn't match map's projection
        extent = ol.proj.transformExtent(extent,
            projection, map.getView().getProjection());

        extent = gnMap.secureExtent(extent, map.getView().getProjection());

        // store the extent into view settings so that it can be used later in
        // case the map is not visible yet
        gnViewerSettings.initialExtent = extent;

        // $timeout used to avoid map no rendered (eg: null size)
        $timeout(function() {
          map.getView().fit(extent, map.getSize(), { nearest: true });
        }, 0, false);

        // save this extent for later use (for example if the map
        // is not currently visible)
        map.set('lastExtent', extent);

        // load the resources
        var layers = context.resourceList.layer;
        var i, j, olLayer, bgLayers = [];
        var self = this;
        var promises = [];
        var overlays = [];
        if (angular.isArray(layers)) {
          for (i = 0; i < layers.length; i++) {
            var type, layer = layers[i];
            if (layer.name) {
              if (layer.group == 'Background layers') {

                // {type=bing_aerial} (mapquest, osm ...)
                var re = this.getREForPar('type');
                if (layer.name.match(re) &&
                    (type = re.exec(layer.name)[1]) != 'wmts') {
                  re = this.getREForPar('name');
                  var opt;
                  if (layer.name.match(re)) {
                    var lyr = re.exec(layer.name)[1];
                    opt = {name: lyr};
                  }
                  var olLayer =
                      gnMap.createLayerForType(type, opt, layer.title);
                  if (olLayer) {
                    bgLayers.push({layer: olLayer, idx: i});
                    olLayer.displayInLayerManager = false;
                    olLayer.background = true;
                    olLayer.set('group', 'Background layers');
                    olLayer.setVisible(!layer.hidden);
                  }
                }

                // {type=wmts,name=Ocean_Basemap} or WMS
                else {
                  promises.push(this.createLayer(layer, map, i).then(
                      function(olLayer) {
                        if (olLayer) {
                          bgLayers.push({
                            layer: olLayer,
                            idx: olLayer.get('bgIdx')
                          });
                          olLayer.displayInLayerManager = false;
                          olLayer.background = true;
                        }
                      }));
                }
              } else if (layer.server) {
                var server = layer.server[0];

                // load extension content (JSON)
                if (layer.extension && layer.extension.any) {
                  var extension = JSON.parse(layer.extension.any);

                  // import saved filters if available
                  if (extension.filters && extension.wfsUrl) {
                    var url = extension.wfsUrl;

                    // get ES object and save filters on it
                    // (will be used by the WfsFilterDirective
                    // when initializing)
                    var esObj =
                        wfsFilterService.registerEsObject(url, layer.name);
                    esObj.initialFilters = extension.filters;
                  }

                  // this object holds the WPS input values
                  var defaultInputs = extension.processInputs || {};
                }

                // create WMS layer
                if (server.service == 'urn:ogc:serviceType:WMS') {
                  var p = self.createLayer(layer, map, undefined, i);
                  promises.push(p);
                  p.then(function(layer) {
                    overlays[layer.get('tree_index')] = layer;

                    // get WPS processes on the layer
                    var processes = layer.get('processes');
                    if (!processes) { return; }

                    // add processes default inputs taken from OWS context
                    // this is done by modifying the applicationProfile value
                    processes.forEach(function(process) {
                      if (defaultInputs && defaultInputs[process.name]) {
                        var appProfile =
                            JSON.parse(process.applicationProfile ||
                            '{}');
                        appProfile.defaults = appProfile.defaults || {};

                        // apply new default values
                        defaultInputs[process.name].forEach(function(input) {
                          var id = input.identifier.value;
                          appProfile.defaults[id] = input.value ||
                              appProfile.defaults[id];
                        });

                        // rewrite modified appProfile to process desc
                        process.applicationProfile = JSON.stringify(appProfile);
                      }
                    });
                  });
                }
              }
            }
            firstLoad = false;
          }
        }

        // if there's at least one valid bg layer in the context use them for
        // the application otherwise use the defaults from config
        $q.all(promises).then(function() {
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
            if (firstVisibleBgLayer && gnViewerSettings.bgLayers.length) {
              var l = gnViewerSettings.bgLayers[0];
              l.setVisible(true);
              map.getLayers().insertAt(0, l);
              firstVisibleBgLayer = false;
            }
          }
          if (overlays.length > 0) {
            map.getLayers().extend(overlays.filter(function(l) {
              return !!l;
            }));
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
      this.loadContextFromUrl = function(url, map) {
        var self = this;
        //        if (/^(f|ht)tps?:\/\//i.test(url)) {
        //          url = gnGlobalSettings.proxyUrl + encodeURIComponent(url);
        //        }
        $http.get(url, {headers: {accept: 'application/xml'}}).then(function(r) {
          if (r.data === '') {
            var msg = $translate.instant('emptyMapLoadError', {
              url: url
            });
            $rootScope.$broadcast('StatusUpdated', {
              msg: msg,
              timeout: 0,
              type: 'danger'});
          }

          self.loadContext(r.data, map);
        }, function(r) {
          var msg = $translate.instant('mapLoadError', {
            url: url
          });
          $rootScope.$broadcast('StatusUpdated', {
            msg: msg,
            timeout: 0,
            type: 'danger'});
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
          } else if (source instanceof ol.source.Stamen) {
            name = '{type=stamen,name=' + layer.getSource().get('type') + '}';
          } else if (source instanceof ol.source.WMTS) {
            name = '{type=wmts,name=' + layer.get('name') + '}';
            params.server = [{
              onlineResource: [{
                href: layer.get('urlCap')
              }],
              service: 'urn:ogc:serviceType:WMS'
            }];
          } else if (source instanceof ol.source.ImageWMS) {
            var s = layer.getSource();
            name = s.getParams().LAYERS;
            params.server = [{
              onlineResource: [{
                href: s.getUrl()
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
          var url = '', version = null;
          var name;

          // background layers already taken into account
          if (layer.background) {
            return;
          }

          if (source instanceof ol.source.ImageWMS) {
            name = source.getParams().LAYERS;
            version = source.getParams().VERSION;
            url = source.getUrl();
          } else if (source instanceof ol.source.TileWMS ||
              source instanceof ol.source.ImageWMS) {
            name = source.getParams().LAYERS;
            url = layer.get('url');
          } else if (source instanceof ol.source.WMTS) {
            name = '{type=wmts,name=' + layer.get('name') + '}';
            url = layer.get('urlCap');
          }

          // fetch current filters state (the whole object will be saved)
          var esObj = layer.get('indexObject');
          if (esObj) {
            var filters = null;
            if (esObj && esObj.getState()) {
              filters = esObj.getState();
            }
          }

          // add processes inputs if available
          var processes = layer.get('processes');
          var processInputs = null;
          if (processes) {
            processes.forEach(function(process) {
              if (!process.processDescription ||
                  !process.processDescription.dataInputs) { return; }
              processInputs = processInputs || {};
              processInputs[process.name] =
                  process.processDescription.dataInputs.input;
            });
          }

          var layerParams = {
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
          };
          if (version) {
            layerParams.server[0].version = version;
          }

          // apply filters & processes inputs in extension if needed
          if (filters || processInputs) {
            var extension = {};
            if (esObj) {
              var wfsUrl = esObj.config.params.wfsUrl;
              if (wfsUrl) {
                extension.filters = filters;
                extension.wfsUrl = wfsUrl;
              }
            }
            if (processInputs) { extension.processInputs = processInputs; }

            layerParams.extension = {
              name: 'Extension',
              any: JSON.stringify(extension)
            };
          }

          resourceList.layer.push(layerParams);
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
        var storage = gnViewerSettings.storage ?
            window[gnViewerSettings.storage] : window.localStorage;
        if (map.getSize()[0] == 0 || map.getSize()[1] == 0) {
          // don't save a map which has not been rendered yet
          return;
        }
        var xml = this.writeContext(map);
        var xmlString = (new XMLSerializer()).serializeToString(xml);
        var key = 'owsContext_' +
            window.location.host + window.location.pathname;
        storage.setItem(key, xmlString);
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
       * @param {numeric} index of the layer in the tree
       */
      this.createLayer = function(layer, map, bgIdx, index) {

        var server = layer.server[0];
        var res = server.onlineResource[0];
        var reT = /type\s*=\s*([^,|^}|^\s]*)/;
        var reL = /name\s*=\s*([^,|^}|^\s]*)/;

        var createOnly = angular.isDefined(bgIdx) || angular.isDefined(index);

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
                  } else if (index) {
                    olL.set('tree_index', index);
                  }
                  return olL;
                }).catch(function() {});
          }
        }
        else { // we suppose it's WMS
          // TODO: Would be good to attach the MD
          // even when loaded from a context.
          return gnMap.addWmsFromScratch(
              map, res.href, layer.name,
              createOnly, null, server.version).
              then(function(olL) {
                if (olL) {
                  try {
                    // Avoid double encoding
                    if (layer.group) {
                      layer.group = decodeURIComponent(escape(layer.group));
                    }
                  } catch (e) {}
                  olL.set('group', layer.group);
                  olL.set('groupcombo', layer.groupcombo);
                  olL.set('tree_index', index);
                  olL.setOpacity(layer.opacity);
                  olL.setVisible(!layer.hidden);
                  if (layer.title) {
                    olL.set('title', layer.title);
                    olL.set('label', layer.title);
                  }
                  $rootScope.$broadcast('layerAddedFromContext', olL);
                  return olL;
                }
                return olL;
              }).catch(function() {});
        }
      };

      /**
       * @ngdoc method
       * @name gnOwsContextService#getREForPar
       * @methodOf gn_viewer.service:gnOwsContextService
       *
       * @description
       * Creates a regular expression for a given parameter
       *
       * * @param {Object} context parameter
       */
      this.getREForPar = function(par) {
        return re = new RegExp(par + '\\s*=\\s*([^,|^}|^\\s]*)');
      };

    }
  ]);
})();
