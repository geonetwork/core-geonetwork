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

(function () {
  goog.provide("gn_owscontext_service");

  goog.require("Filter_1_0_0");
  goog.require("GML_2_1_2");
  goog.require("OWC_0_3_1");
  goog.require("OWS_1_0_0");
  goog.require("SLD_1_0_0");
  goog.require("XLink_1_0");
  goog.require("gn_wfsfilter_service");

  var module = angular.module("gn_owscontext_service", []);

  // OWC Client
  // Jsonix wrapper to read or write OWS Context
  var context = new Jsonix.Context(
    [XLink_1_0, OWS_1_0_0, Filter_1_0_0, GML_2_1_2, SLD_1_0_0, OWC_0_3_1],
    {
      namespacePrefixes: {
        "http://www.w3.org/1999/xlink": "xlink",
        "http://www.opengis.net/ows": "ows"
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
  module.service("gnOwsContextService", [
    "gnMap",
    "gnOwsCapabilities",
    "$http",
    "gnViewerSettings",
    "gnViewerService",
    "$translate",
    "$q",
    "$filter",
    "$rootScope",
    "$timeout",
    "gnGlobalSettings",
    "wfsFilterService",
    function (
      gnMap,
      gnOwsCapabilities,
      $http,
      gnViewerSettings,
      gnViewerService,
      $translate,
      $q,
      $filter,
      $rootScope,
      $timeout,
      gnGlobalSettings,
      wfsFilterService
    ) {
      var firstLoad = true;

      // Regex for matching type and layer in context layer name attribute.
      // eg. name="{type=arcgis,name=0,1,2,3,4}"
      var reT = /type=([^,}|^]*)/;
      var reL = /name=([^}]*)\}?\s*$/;
      var dimensions = ["TIME", "ELEVATION"];

      /**
       * @ngdoc method
       * @name gnOwsContextService#loadContext
       * @methodOf gn_viewer.service:gnOwsContextService
       *
       * @description
       * Loads a context, ie. creates layers and centers the map
       *
       * @param {string} text OWS context content
       * @param {ol.map} map map
       * @param {owsContextLayer} additionalLayers these layers will be added
       *  after the context layers (used to add layers from the map settings)
       */
      this.loadContext = function (text, map, additionalLayers) {
        // broadcast context load
        $rootScope.$broadcast("owsContextLoaded");

        var uiConfig = {};
        var mapConfig = gnMap.getMapConfig();
        var context = unmarshaller.unmarshalString(text).value;
        var mapType = map.get("type");
        if (mapType && mapConfig) {
          uiConfig = mapConfig["map-" + mapType];
        }
        var isMainViewer = mapType === "viewer";
        // first remove any existing layer
        var layersToRemove = [];
        map.getLayers().forEach(function (layer) {
          if (layer.displayInLayerManager) {
            if (!(layer.get("fromUrlParams") && firstLoad)) {
              layersToRemove.push(layer);
            }
          }
        });
        for (var i = 0; i < layersToRemove.length; i++) {
          map.removeLayer(layersToRemove[i]);
        }

        // Set the Map view (extent/projection) from the context
        var bbox = context.general.boundingBox.value;
        var ll = bbox.lowerCorner;
        var ur = bbox.upperCorner;
        var projection = bbox.crs;

        // Check if projection is available in ol
        if (!ol.proj.get(projection)) {
          console.warn(
            "Projection " +
              projection +
              " is not available, map will be projected in a spherical mercator projection"
          );
          projection = "EPSG:3857";
          ll = [-10026376, -15048966];
          ur = [10026376, 15048966];
        }

        if (projection == "EPSG:4326") {
          // WGS84 expects lat-lon (y,x) not lon-lat (x,y)
          ll.reverse();
          ur.reverse();
        }

        var extent = ll.concat(ur);

        // Apply extent override from UI settings, if any
        if (
          uiConfig &&
          uiConfig.extent &&
          ol.extent.getWidth(uiConfig.extent) &&
          ol.extent.getHeight(uiConfig.extent)
        ) {
          extent = uiConfig.extent;
          // Extent should be specified in the default map projection:
          // reproject to context projection, if this is not the case already
          if (mapConfig.projection !== projection) {
            extent = ol.proj.transformExtent(extent, mapConfig.projection, projection, 8);
          }
        }

        gnViewerSettings.initialExtent = extent;

        if (map.getView().getProjection().getCode() != projection) {
          var view = new ol.View({
            projection: projection
          });
          map.setView(view);
        }

        var loadPromise = map.get("sizePromise");
        if (loadPromise) {
          loadPromise.then(function () {
            map.getView().fit(extent, map.getSize());
          });
        } else {
          console.warn("Map must be created by mapsManager");
        }

        // load the resources & add additional layers if available
        var layers = context.resourceList.layer;
        if (additionalLayers) {
          layers = layers.concat(additionalLayers);
        }

        var i, j, olLayer;
        var self = this;
        var promises = [];
        var overlays = [];
        if (angular.isArray(layers)) {
          // ----  Clean bg layers
          if (map.getLayers().getLength() > 0) {
            map.getLayers().removeAt(0);
          }
          var bgLoadingLayer = new ol.layer.Image({
            loading: true,
            label: "loading",
            url: "",
            visible: false
          });
          map.getLayers().insertAt(0, bgLoadingLayer);

          if (!gnViewerSettings.bgLayers) {
            gnViewerSettings.bgLayers = [];
          }
          if (isMainViewer) {
            gnViewerSettings.bgLayers.length = 0;
          }

          var bgLayers = gnViewerSettings.bgLayers;
          bgLayers.fromCtx = true;
          var isFirstActiveBgLayer = false;
          // -------

          for (i = 0; i < layers.length; i++) {
            var type,
              layer = layers[i];
            if (layer.group == "Background layers") {
              var layerAttributionArray;
              if (layer.vendorExtension && layer.vendorExtension.attribution) {
                layerAttributionArray = [];
                for (var a = 0; a < layer.vendorExtension.attribution.length; a++) {
                  var attribution = layer.vendorExtension.attribution[a];
                  var layerAttribution = attribution.title;
                  // If href exist then make the title a link
                  if (attribution.onlineResource && attribution.onlineResource[0].href) {
                    var link = document.createElement("a");
                    link.href = attribution.onlineResource[0].href;
                    link.innerHTML = layerAttribution;
                    layerAttribution = link.outerHTML;
                  }
                  layerAttributionArray.push(layerAttribution);
                }
              }

              // Function to set the attribution on the layer
              // Parameter :
              //   ol layer to apply the attributions to.
              //   layerAttributionArray containing attributions to be added.
              var setLayerAttribution = function (olLayer, layerAttributionArray) {
                if (layerAttributionArray) {
                  // Only apply the layer if there is a source.
                  if (olLayer.getSource()) {
                    var attributionLike = olLayer.getSource().getAttributions();
                    if (typeof attributionLike === "function") {
                      attributionLike = attributionLike();
                    }
                    if (typeof attributionLike === "string") {
                      attributionLike = [attributionLike];
                    }
                    if (
                      typeof attributionLike === "object" &&
                      Array.isArray(attributionLike)
                    ) {
                      attributionLike.push.apply(attributionLike, layerAttributionArray);
                    } else {
                      attributionLike = layerAttributionArray;
                    }
                    olLayer.getSource().setAttributions(attributionLike);
                  } else {
                    console.log(
                      "Warning: Cannot add attributions to map as source is not defined"
                    );
                  }
                }
              };

              // {type=bing_aerial} (mapquest, osm ...)
              // {type=arcgis,name=0,1,2}
              // type=wms,name=lll
              type = layer.name && layer.name.match(reT) ? reT.exec(layer.name)[1] : null;
              if (type && type != "wmts" && type != "wms" && type != "arcgis") {
                var opt;
                if (layer.name && layer.name.match(reL)) {
                  var lyr = reL.exec(layer.name)[1];

                  if (layer.server) {
                    var server = layer.server[0];
                    var res = server.onlineResource[0].href;
                  }
                  opt = { name: lyr, url: res };
                }
                var olLayer = gnMap.createLayerForType(type, opt, layer.title, map);
                if (olLayer) {
                  olLayer.displayInLayerManager = false;
                  olLayer.background = true;
                  olLayer.set("group", "Background layers");
                  olLayer.setVisible(!layer.hidden);
                  setLayerAttribution(olLayer, layerAttributionArray);

                  if (isMainViewer) {
                    bgLayers.push(olLayer);
                  }

                  if (!layer.hidden && !isFirstActiveBgLayer) {
                    isFirstActiveBgLayer = true;
                    map.getLayers().setAt(0, olLayer);
                  }
                }
              }

              // {type=wmts,name=Ocean_Basemap} or WMS or arcgis
              else {
                // to push in bgLayers not in the map
                var loadingLayer = new ol.layer.Image({
                  loading: true,
                  label: "loading",
                  url: "",
                  visible: false
                });

                if (!layer.hidden && !isFirstActiveBgLayer) {
                  isFirstActiveBgLayer = true;
                  loadingLayer.set("bgLayer", true);
                }

                var layerIndex;
                if (isMainViewer) {
                  layerIndex = bgLayers.push(loadingLayer) - 1;
                }
                var p = self.createLayer(layer, map, "do not add");

                (function (idx, loadingLayer) {
                  p.then(function (layer) {
                    if (!layer) {
                      return;
                    }
                    if (layerIndex) {
                      bgLayers[idx] = layer;
                    }

                    layer.displayInLayerManager = false;
                    layer.background = true;

                    setLayerAttribution(layer, layerAttributionArray);

                    if (loadingLayer.get("bgLayer")) {
                      map.getLayers().setAt(0, layer);
                    }
                  });
                })(layerIndex, loadingLayer);
              }
            }
            // WMS layer not in background
            else if (layer.server) {
              var server = layer.server[0];
              var currentStyle;

              // load extension content (JSON)
              var extension =
                layer.extension && layer.extension.any
                  ? // Formatted XML may contains extra space and line feed
                    JSON.parse(layer.extension.any.replaceAll(/\n +/g, " "))
                  : {};

              var loadingId = extension.label ? extension.label : layer.name;
              if (extension.style) {
                currentStyle = { Name: extension.style };
                loadingId += " " + extension.style;
              }

              // import saved filters if available
              if (extension.filters && extension.wfsUrl) {
                var url = extension.wfsUrl;

                // get ES object and save filters on it
                // (will be used by the WfsFilterDirective
                // when initializing)
                var esObj = wfsFilterService.registerEsObject(url, layer.name);
                esObj.initialFilters = extension.filters;
              }

              // this object holds the WPS input values
              var defaultInputs = extension.processInputs || {};

              // create WMS layer
              if (server.service == "urn:ogc:serviceType:WMS") {
                var loadingLayer = new ol.layer.Image({
                  loading: true,
                  label: loadingId + "...",
                  url: "",
                  visible: false,
                  group: layer.group
                });

                loadingLayer.displayInLayerManager = true;

                if (extension.label) {
                  layer.title = extension.label;
                }
                if (extension.uuid) {
                  layer.metadataUuid = extension.uuid;
                }
                if (extension.enabled) {
                  layer.enabled = extension.enabled;
                }

                dimensions.forEach(function (dimension) {
                  if (extension[dimension.toLowerCase() + "DimensionValue"]) {
                    layer[dimension.toLowerCase() + "DimensionValue"] =
                      extension[dimension.toLowerCase() + "DimensionValue"];
                  }
                });

                var layerIndex = map.getLayers().push(loadingLayer) - 1;
                var p = self.createLayer(layer, map, undefined, i, currentStyle);
                loadingLayer.set("index", layerIndex);

                (function (idx, loadingLayer) {
                  p.then(function (layer) {
                    if (layer) {
                      map.getLayers().setAt(idx, layer);
                    } else {
                      loadingLayer.set("errors", ["load failed"]);
                    }
                  });
                })(layerIndex, loadingLayer);
              }
            }
            firstLoad = false;
          }
          if (!isFirstActiveBgLayer && bgLayers.length > 0) {
            console.warn(
              "Map context does not contain any active background layer. \n" +
                "Set the hidden parameter to false to at least one layer of the group Background layers. \n" +
                "Setting the first one in the group as active."
            );
            bgLayers[0].set("bgLayer", true);
            map.getLayers().setAt(0, bgLayers[0]);
          }

          if (gnGlobalSettings.gnCfg.mods.map.defaultToolAfterMapLoad) {
            gnViewerService.openTool(
              gnGlobalSettings.gnCfg.mods.map.defaultToolAfterMapLoad
            );
          }
        }
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
       * @param {owsContextLayer} additionalLayers these layers will be added
       *  after the context layers (used to add layers from the map settings)
       */
      this.loadContextFromUrl = function (url, map, additionalLayers) {
        var self = this;
        //        if (/^(f|ht)tps?:\/\//i.test(url)) {
        //          url = gnGlobalSettings.proxyUrl + encodeURIComponent(url);
        //        }
        return $http.get(url, { headers: { accept: "application/xml" } }).then(
          function (r) {
            if (r.data === "") {
              var msg = $translate.instant("emptyMapLoadError", {
                url: url
              });
              $rootScope.$broadcast("StatusUpdated", {
                msg: msg,
                timeout: 0,
                type: "danger"
              });
            }

            self.loadContext(r.data, map, additionalLayers);
          },
          function (r) {
            var contextUsingLanguage =
              gnViewerSettings.defaultContext.indexOf("{lang}") > -1;

            if (r.status == 404 && contextUsingLanguage) {
              // Check to load the context file for the default language
              var newUrl = gnViewerSettings.defaultContext.replace("{lang}", "eng");
              self.loadContextFromUrl(newUrl, map, additionalLayers);
            } else {
              var msg = $translate.instant("mapLoadError", {
                url: url
              });
              $rootScope.$broadcast("StatusUpdated", {
                msg: msg,
                timeout: 0,
                type: "danger"
              });
            }
          }
        );
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
      this.writeContext = function (map) {
        var extent = map.getView().calculateExtent(map.getSize());

        var general = {
          boundingBox: {
            name: {
              namespaceURI: "http://www.opengis.net/ows",
              localPart: "BoundingBox"
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
        angular.forEach(gnViewerSettings.bgLayers, function (layer) {
          // skip if no valid layer (ie: layer still loading)
          if (!layer) {
            return;
          }

          var source = layer.getSource();
          var name;
          var params = {
            hidden: map.getLayers().getArray().indexOf(layer) < 0,
            opacity: layer.getOpacity(),
            title: layer.get("title"),
            group: layer.get("group")
          };

          if (source instanceof ol.source.OSM) {
            name = "{type=osm}";
          } else if (source instanceof ol.source.BingMaps) {
            name = "{type=bing_aerial}";
          } else if (source instanceof ol.source.WMTS) {
            name = "{type=wmts,name=" + layer.get("name") + "}";
            params.server = [
              {
                onlineResource: [
                  {
                    href: layer.get("urlCap")
                  }
                ],
                service: "urn:ogc:serviceType:WMS"
              }
            ];
          } else if (source instanceof ol.source.ImageArcGISRest) {
            name =
              "{type=arcgis,name=" +
              layer.getSource().getParams().LAYERS.replace("show:", "") +
              "}";
            params.server = [
              {
                onlineResource: [
                  {
                    href: layer.get("url")
                  }
                ],
                service: "urn:ogc:serviceType:WMS"
              }
            ];
          } else if (
            source instanceof ol.source.ImageWMS ||
            source instanceof ol.source.TileWMS
          ) {
            name = layer.get("name");
            params.server = [
              {
                onlineResource: [
                  {
                    href: layer.get("url")
                  }
                ],
                service: "urn:ogc:serviceType:WMS"
              }
            ];
          } else if (source instanceof ol.source.TileImage) {
            name = "{type=tms,name=" + layer.get("name") + "}";

            params.server = [
              {
                onlineResource: [
                  {
                    href: layer.getSource().getUrls()[0]
                  }
                ],
                service: "urn:ogc:serviceType:WMTS"
              }
            ];
          } else {
            return;
          }
          params.name = name;
          resourceList.layer.push(params);
        });

        map.getLayers().forEach(function (layer) {
          var source = layer.getSource();
          var url = "",
            version = null;
          var name;

          // background layers already taken into account
          if (layer.background) {
            return;
          }

          if (source instanceof ol.source.ImageWMS) {
            name = source.getParams().LAYERS;
            version = source.getParams().VERSION;
            url = gnGlobalSettings.getNonProxifiedUrl(source.getUrl());
          } else if (
            source instanceof ol.source.TileWMS ||
            source instanceof ol.source.ImageWMS
          ) {
            name = source.getParams().LAYERS;
            url = gnGlobalSettings.getNonProxifiedUrl(layer.get("url"));
          } else if (source instanceof ol.source.WMTS) {
            name = "{type=wmts,name=" + layer.get("name") + "}";
            url = gnGlobalSettings.getNonProxifiedUrl(layer.get("urlCap"));
          } else if (source instanceof ol.source.ImageArcGISRest) {
            var layerId = layer.getSource().getParams().LAYERS;
            name = "{type=arcgis,name=" + (layerId || "") + "}";
            url = layer.get("url");
          } else {
            return;
          }

          // fetch current filters state (the whole object will be saved)
          var esObj = layer.get("indexObject");
          if (esObj) {
            var filters = null;
            if (esObj && esObj.getState()) {
              filters = esObj.getState();
            }
          }

          // add processes inputs if available
          var processes = layer.get("processes");
          var processInputs = null;
          if (processes) {
            processes.forEach(function (process) {
              if (!process.processDescription || !process.processDescription.dataInputs) {
                return;
              }
              processInputs = processInputs || {};
              processInputs[process.name] = process.processDescription.dataInputs.input;
            });
          }

          var layerParams = {
            hidden: !layer.getVisible(),
            opacity: layer.getOpacity(),
            name: name,
            title: layer.get("title"),
            group: layer.get("group"),
            groupcombo: layer.get("groupcombo"),
            server: [
              {
                onlineResource: [
                  {
                    href: url
                  }
                ],
                service: "urn:ogc:serviceType:WMS"
              }
            ]
          };
          if (version) {
            layerParams.server[0].version = version;
          }

          // apply filters & processes inputs in extension if needed
          var extension = {};

          if (layer.get("md") && layer.get("md")._id) {
            extension.uuid = layer.get("md")._id;
            extension.label = layer.get("label");
          }

          if (layer.get("currentStyle")) {
            extension.style = layer.get("currentStyle").Name;
          }

          if (esObj) {
            var wfsUrl = esObj.config.params.wfsUrl;
            if (wfsUrl) {
              extension.filters = filters;
              extension.wfsUrl = wfsUrl;
            }
          }
          if (processInputs) {
            extension.processInputs = processInputs;
          }
          if (layer.showInfo) {
            extension.enabled = true; // Enabled in layer manager
          }
          dimensions.forEach(function (dimension) {
            if (source.getParams()[dimension]) {
              extension[dimension.toLowerCase() + "DimensionValue"] =
                source.getParams()[dimension];
            }
          });

          layerParams.extension = {
            name: "Extension",
            any: JSON.stringify(extension)
          };

          resourceList.layer.push(layerParams);
        });

        var context = {
          version: "0.3.1",
          id: "ows-context-ex-1-v3",
          general: general,
          resourceList: resourceList
        };

        var xml = marshaller.marshalDocument({
          name: {
            localPart: "OWSContext",
            namespaceURI: "http://www.opengis.net/ows-context",
            prefix: "ows-context",
            string: "{http://www.opengis.net/ows-context}ows-context:OWSContext"
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
      this.saveToLocalStorage = function (map) {
        // Disable map storage.
        if (gnViewerSettings.mapConfig.storage === "") {
          return;
        }
        var storage = gnViewerSettings.mapConfig.storage
          ? window[gnViewerSettings.mapConfig.storage]
          : window.localStorage;
        if (map.getSize()[0] == 0 || map.getSize()[1] == 0) {
          // don't save a map which has not been rendered yet
          return;
        }
        var xml = this.writeContext(map);
        var xmlString = new XMLSerializer().serializeToString(xml);
        var key = "owsContext_" + window.location.host + window.location.pathname;
        storage.setItem(key, xmlString);
      };

      /**
       * @ngdoc method
       * @name gnOwsContextService#createLayer
       * @methodOf gn_viewer.service:gnOwsContextService
       *
       * @description
       * Create a WMS ol.Layer from context object
       * !! DEPRECATED: use gnMap.createLayerFromProperties instead
       *
       * @param {Object} layer layer
       * @param {ol.map} map map
       * @param {numeric} bgIdx if it is a background layer, index in the
       * dropdown
       * @param {numeric} index of the layer in the tree
       */
      this.createLayer = function (layer, map, bgIdx, index, style) {
        var res = { href: "" };
        if (layer.server) {
          var server = layer.server[0];
          res = server.onlineResource[0];
        }
        var createOnly = angular.isDefined(bgIdx) || angular.isDefined(index);

        function setMapLayerProperties(olL, layer) {
          olL.set("group", layer.group);
          olL.set("groupcombo", layer.groupcombo);
          olL.setOpacity(layer.opacity);
          olL.setVisible(!layer.hidden);
          var title = layer.title ? layer.title : olL.get("label");
          olL.set("title", title || "");
          olL.set("label", title || "");
          olL.set("metadataUuid", layer.metadataUuid || "");
          if (bgIdx) {
            olL.set("bgIdx", bgIdx);
          } else if (index) {
            olL.set("tree_index", index);
          }
          if (layer.enabled) {
            olL.showInfo = layer.enabled; // Enabled in layer manager
          }
          // WMTS layers for example doesn't have this type of information
          if (typeof olL.getSource().getParams === "function") {
            var params = olL.getSource().getParams() || {};
            dimensions.forEach(function (dimension) {
              if (layer[dimension.toLowerCase() + "DimensionValue"]) {
                params[dimension] = layer[dimension.toLowerCase() + "DimensionValue"];
                olL.getSource().updateParams(params);
              }
            });
          }
        }

        if (layer.name && layer.name.match(reT)) {
          var type = reT.exec(layer.name)[1];
          var name = reL.exec(layer.name)[1];
          var promise;

          if (type === "wmts") {
            promise = gnMap.addWmtsFromScratch(
              map,
              res.href,
              name,
              createOnly,
              layer.metadataUuid || null
            );
          } else if (type === "arcgis") {
            promise = gnMap.addEsriRestLayer(
              map,
              res.href,
              name,
              createOnly,
              layer.metadataUuid || null
            );
          }

          // if it's not WMTS, let's assume it is wms
          // (so as to be sure to return something)
          else {
            promise = gnMap.addWmsFromScratch(
              map,
              res.href,
              name,
              createOnly,
              layer.metadataUuid || null
            );
          }

          return promise
            .then(function (olL) {
              setMapLayerProperties(olL, layer);
              return olL;
            })
            .catch(function (error) {
              console.error(error);
            });
        } else {
          // we suppose it's WMS
          // TODO: Would be good to attach the MD
          // even when loaded from a context.
          return gnMap
            .addWmsFromScratch(
              map,
              res.href,
              layer.name,
              createOnly,
              layer.metadataUuid || null,
              server.version,
              style
            )
            .then(function (olL) {
              if (olL) {
                try {
                  // Avoid double encoding
                  if (layer.group) {
                    layer.group = decodeURIComponent(escape(layer.group));
                  }
                } catch (e) {}
                setMapLayerProperties(olL, layer);
                $rootScope.$broadcast("layerAddedFromContext", olL);
                return olL;
              }
              return olL;
            })
            .catch(function (error) {
              console.error(error);
            });
        }
      };
    }
  ]);
})();
