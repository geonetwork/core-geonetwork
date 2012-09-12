/**
 * Copyright (c) 2008-2010 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */
Ext.namespace("GeoExt.data");

/** api: (define)
 *  module = GeoExt.data
 *  class = PrintProvider
 *  base_link = `Ext.util.Observable <http://dev.sencha.com/deploy/dev/docs/?class=Ext.util.Observable>`_
 */

/** api: example
 *  Minimal code to print as much of the current map extent as possible as
 *  soon as the print service capabilities are loaded, using the first layout
 *  reported by the print service:
 * 
 *  .. code-block:: javascript
 *     
 *      var mapPanel = new GeoExt.MapPanel({
 *          renderTo: "mappanel",
 *          layers: [new OpenLayers.Layer.WMS("wms", "/geoserver/wms",
 *              {layers: "topp:tasmania_state_boundaries"})],
 *          center: [146.56, -41.56],
 *          zoom: 7
 *      });
 *      var printProvider = new GeoExt.data.PrintProvider({
 *          url: "/geoserver/pdf",
 *          listeners: {
 *              "loadcapabilities": function() {
 *                  var printPage = new GeoExt.data.PrintPage({
 *                      printProvider: printProvider
 *                  });
 *                  printPage.fit(mapPanel, true);
 *                  printProvider.print(mapPanel, printPage);
 *              }
 *          }
 *      });
 */

/** api: constructor
 *  .. class:: PrintProvider
 * 
 *  Provides an interface to a Mapfish or GeoServer print module. For printing,
 *  one or more instances of :class:`GeoExt.data.PrintPage` are also required
 *  to tell the PrintProvider about the scale and extent (and optionally
 *  rotation) of the page(s) we want to print. 
 */
GeoExt.data.PrintProvider = Ext.extend(Ext.util.Observable, {
    
    /** api: config[url]
     *  ``String`` Base url of the print service. Only required if
     *  ``capabilities`` is not provided. This
     *  is usually something like http://path/to/mapfish/print for Mapfish,
     *  and http://path/to/geoserver/pdf for GeoServer with the printing
     *  extension installed. This property requires that the print service is
     *  at the same origin as the application (or accessible via proxy).
     */
    
    /** private:  property[url]
     *  ``String`` Base url of the print service. Will always have a trailing
     *  "/".
     */
    url: null,
    
    /** api: config[autoLoad]
     *  ``Boolean`` If set to true, the capabilities will be loaded upon
     *  instance creation, and ``loadCapabilities`` does not need to be called
     *  manually. Setting this when ``capabilities`` and no ``url`` is provided
     *  has no effect. Default is false.
     */

    /** api: config[capabilities]
     *  ``Object`` Capabilities of the print service. Only required if ``url``
     *  is not provided. This is the object returned by the ``info.json``
     *  endpoint of the print service, and is usually obtained by including a
     *  script tag pointing to
     *  http://path/to/printservice/info.json?var=myvar in the head of the
     *  html document, making the capabilities accessible as ``window.myvar``.
     *  This property should be used when no local print service or proxy is
     *  available, or when you do not listen for the ``loadcapabilities``
     *  events before creating components that require the PrintProvider's
     *  capabilities to be available.
     */
    
    /** private: property[capabilities]
     *  ``Object`` Capabilities as returned from the print service.
     */
    capabilities: null,
    
    /** api: config[method]
     *  ``String`` Either ``POST`` or ``GET`` (case-sensitive). Method to use
     *  when sending print requests to the servlet. If the print service is at
     *  the same origin as the application (or accessible via proxy), then
     *  ``POST`` is recommended. Use ``GET`` when accessing a remote print
     *  service with no proxy available, but expect issues with character
     *  encoding and URLs exceeding the maximum length. Default is ``POST``.
     */
    
    /** private: property[method]
     *  ``String`` Either ``POST`` or ``GET`` (case-sensitive). Method to use
     *  when sending print requests to the servlet.
     */
    method: "POST",

    /** api: config[customParams]
     *  ``Object`` Key-value pairs of custom data to be sent to the print
     *  service. Optional. This is e.g. useful for complex layout definitions
     *  on the server side that require additional parameters.
     */
    
    /** api: property[customParams]
     *  ``Object`` Key-value pairs of custom data to be sent to the print
     *  service. Optional. This is e.g. useful for complex layout definitions
     *  on the server side that require additional parameters.
     */
    customParams: null,
    
    /** api: config[baseParams]
     *  ``Object`` Key-value pairs of base params to be add to every 
     *  request to the service. Optional. 
     */
    
    /** api: property[scales]
     *  ``Ext.data.JsonStore`` read-only. A store representing the scales
     *  available.
     *  
     *  Fields of records in this store:
     *  
     *  * name - ``String`` the name of the scale
     *  * value - ``Float`` the scale denominator
     */
    scales: null,
    
    /** api: property[dpis]
     *  ``Ext.data.JsonStore`` read-only. A store representing the dpis
     *  available.
     *  
     *  Fields of records in this store:
     *  
     *  * name - ``String`` the name of the dpi
     *  * value - ``Float`` the dots per inch
     */
    dpis: null,
        
    /** api: property[layouts]
     *  ``Ext.data.JsonStore`` read-only. A store representing the layouts
     *  available.
     *  
     *  Fields of records in this store:
     *  
     *  * name - ``String`` the name of the layout
     *  * size - ``Object`` width and height of the map in points
     *  * rotation - ``Boolean`` indicates if rotation is supported
     */
    layouts: null,
    
    /** api: property[dpi]
     *  ``Ext.data.Record`` the record for the currently used resolution.
     *  Read-only, use ``setDpi`` to set the value.
     */
    dpi: null,

    /** api: property[layout]
     *  ``Ext.data.Record`` the record of the currently used layout. Read-only,
     *  use ``setLayout`` to set the value.
     */
    layout: null,

    /** private:  method[constructor]
     *  Private constructor override.
     */
    constructor: function(config) {
        this.initialConfig = config;
        Ext.apply(this, config);
        
        if(!this.customParams) {
            this.customParams = {};
        }

        this.addEvents(
            /** api: event[loadcapabilities]
             *  Triggered when the capabilities have finished loading. This
             *  event will only fire when ``capabilities`` is not  configured.
             *  
             *  Listener arguments:
             *
             *  * printProvider - :class:`GeoExt.data.PrintProvider` this
             *    PrintProvider
             *  * capabilities - ``Object`` the capabilities
             */
            "loadcapabilities",
            
            /** api: event[layoutchange]
             *  Triggered when the layout is changed.
             *  
             *  Listener arguments:
             *
             *  * printProvider - :class:`GeoExt.data.PrintProvider` this
             *    PrintProvider
             *  * layout - ``Ext.data.Record`` the new layout
             */
            "layoutchange",

            /** api: event[dpichange]
             *  Triggered when the dpi value is changed.
             *  
             *  Listener arguments:
             *
             *  * printProvider - :class:`GeoExt.data.PrintProvider` this
             *    PrintProvider
             *  * dpi - ``Ext.data.Record`` the new dpi record
             */
            "dpichange",
            
            /** api: event[beforeprint]
             *  Triggered when the print method is called.
             *  
             *  Listener arguments:
             *
             *  * printProvider - :class:`GeoExt.data.PrintProvider` this
             *    PrintProvider
             *  * map - ``OpenLayers.Map`` the map being printed
             *  * pages - Array of :class:`GeoExt.data.PrintPage` the print
             *    pages being printed
             *  * options - ``Object`` the options to the print command
             */
            "beforeprint",
            
            /** api: event[print]
             *  Triggered when the print document is opened.
             *  
             *  Listener arguments:
             *
             *  * printProvider - :class:`GeoExt.data.PrintProvider` this
             *    PrintProvider
             *  * url - ``String`` the url of the print document
             */
            "print",

            /** api: event[printexception]
             *  Triggered when using the ``POST`` method, when the print
             *  backend returns an exception.
             *  
             *  Listener arguments:
             *
             *  * printProvider - :class:`GeoExt.data.PrintProvider` this
             *    PrintProvider
             *  * response - ``Object`` the response object of the XHR
             */
            "printexception",

            /** api: event[beforeencodelayer]
             *  Triggered before a layer is encoded. This can be used to
             *  exclude layers from the printing, by having the listener
             *  return false.
             *
             *  Listener arguments:
             *
             *  * printProvider - :class:`GeoExt.data.PrintProvider` this
             *    PrintProvider
             *  * layer - ``OpenLayers.Layer`` the layer which is about to be 
             *    encoded.
             */
            "beforeencodelayer",
            
            /** api: event[encodelayer]
             *  Triggered when a layer is encoded. This can be used to modify
             *  the encoded low-level layer object that will be sent to the
             *  print service.
             *  
             *  Listener arguments:
             *
             *  * printProvider - :class:`GeoExt.data.PrintProvider` this
             *    PrintProvider
             *  * layer - ``OpenLayers.Layer`` the layer which is about to be 
             *    encoded.
             *  * encodedLayer - ``Object`` the encoded layer that will be
             *    sent to the print service.
             */
            "encodelayer"

        );
        
        GeoExt.data.PrintProvider.superclass.constructor.apply(this, arguments);

        this.scales = new Ext.data.JsonStore({
            root: "scales",
            sortInfo: {field: "value", direction: "DESC"},
            fields: ["name", {name: "value", type: "float"}]
        });
        
        this.dpis = new Ext.data.JsonStore({
            root: "dpis",
            fields: ["name", {name: "value", type: "float"}]
        });
        
        this.layouts = new Ext.data.JsonStore({
            root: "layouts",
            fields: [
                "name",
                {name: "size", mapping: "map"},
                {name: "rotation", type: "boolean"}
            ]
        });
        
        if(config.capabilities) {
            this.loadStores();
        } else {
            if(this.url.split("/").pop()) {
                this.url += "/";            
            }
            this.initialConfig.autoLoad && this.loadCapabilities();
        }
    },
    
    /** api: method[setLayout]
     *  :param layout: ``Ext.data.Record`` the record of the layout.
     *  
     *  Sets the layout for this printProvider.
     */
    setLayout: function(layout) {
        this.layout = layout;
        this.fireEvent("layoutchange", this, layout);
    },
    
    /** api: method[setDpi]
     *  :param dpi: ``Ext.data.Record`` the dpi record.
     *  
     *  Sets the dpi for this printProvider.
     */
    setDpi: function(dpi) {
        this.dpi = dpi;
        this.fireEvent("dpichange", this, dpi);
    },

    /** api: method[print]
     *  :param map: ``GeoExt.MapPanel`` or ``OpenLayers.Map`` The map to print.
     *  :param pages: ``Array`` of :class:`GeoExt.data.PrintPage` or
     *      :class:`GeoExt.data.PrintPage` page(s) to print.
     *  :param options: ``Object`` of additional options, see below.
     *  
     *  Sends the print command to the print service and opens a new window
     *  with the resulting PDF.
     *  
     *  Valid properties for the ``options`` argument:
     *
     *      * ``legend`` - :class:`GeoExt.LegendPanel` If provided, the legend
     *        will be added to the print document. For the printed result to
     *        look like the LegendPanel, the following ``!legends`` block
     *        should be included in the ``items`` of your page layout in the
     *        print module's configuration file:
     *        
     *        .. code-block:: none
     *        
     *          - !legends
     *              maxIconWidth: 0
     *              maxIconHeight: 0
     *              classIndentation: 0
     *              layerSpace: 5
     *              layerFontSize: 10
     *
     *      * ``overview`` - :class:`OpenLayers.Control.OverviewMap` If provided,
     *        the layers for the overview map in the printout will be taken from
     *        the OverviewMap control. If not provided, the print service will
     *        use the main map's layers for the overview map. Applies only for
     *        layouts configured to print an overview map.
     */
    print: function(map, pages, options) {
        if(map instanceof GeoExt.MapPanel) {
            map = map.map;
        }
        pages = pages instanceof Array ? pages : [pages];
        options = options || {};
        if(this.fireEvent("beforeprint", this, map, pages, options) === false) {
            return;
        }

        var jsonData = Ext.apply({
            units: map.getUnits(),
            srs: map.baseLayer.projection.getCode(),
            layout: this.layout.get("name"),
            dpi: this.dpi.get("value")
        }, this.customParams);

        var pagesLayer = pages[0].feature.layer;
        var encodedLayers = [];
        Ext.each(map.layers, function(layer){
            if(layer !== pagesLayer && layer.getVisibility() === true) {
                var enc = this.encodeLayer(layer);
                enc && encodedLayers.push(enc);
            }
        }, this);
        jsonData.layers = encodedLayers;
        
        var encodedPages = [];
        Ext.each(pages, function(page) {
            encodedPages.push(Ext.apply({
                center: [page.center.lon, page.center.lat],
                scale: page.scale.get("value"),
                rotation: page.rotation
            }, page.customParams));
        }, this);
        jsonData.pages = encodedPages;
        
        if (options.overview) {
            var encodedOverviewLayers = [];
            Ext.each(options.overview.layers, function(layer) {
                var enc = this.encodeLayer(layer);
                enc && encodedOverviewLayers.push(enc);
            }, this);
            jsonData.overviewLayers = encodedOverviewLayers;
        }

        if(options.legend) {
            var legend = options.legend;
            var rendered = legend.rendered;
            if (!rendered) {
                legend = legend.cloneConfig({
                    renderTo: document.body,
                    hidden: true
                });
            }
            var encodedLegends = [];
            legend.items.each(function(cmp) {
                if(!cmp.hidden) {
                    var encFn = this.encoders.legends[cmp.getXType()];
                    encodedLegends = encodedLegends.concat(
                        encFn.call(this, cmp));
                }
            }, this);
            if (!rendered) {
                legend.destroy();
            }
            jsonData.legends = encodedLegends;
        }

        if(this.method === "GET") {
            var url = Ext.urlAppend(this.capabilities.printURL,
                "spec=" + encodeURIComponent(Ext.encode(jsonData)));
            window.open(url);
            this.fireEvent("print", this, url);
        } else {
            Ext.Ajax.request({
                url: this.capabilities.createURL,
                jsonData: jsonData,
                success: function(response) {
                    // In IE, using a Content-disposition: attachment header
                    // may make it hard or impossible to download the pdf due
                    // to security settings. So we'll display the pdf inline.
                    var url = Ext.decode(response.responseText).getURL +
                        (Ext.isIE ? "?inline=true" : "");
                    if(Ext.isOpera || Ext.isIE) {
                        // Make sure that Opera and IE don't replace the
                        // content tab with the pdf
                        window.open(url);
                    } else {
                        // This avoids popup blockers for all other browsers
                        window.location.href = url;                        
                    } 
                    this.fireEvent("print", this, url);
                },
                failure: function(response) {
                    this.fireEvent("printexception", this, response);
                },
                params: this.initialConfig.baseParams,
                scope: this
            });
        }
    },
    
    /** api: method[loadCapabilities]
     *
     *  Loads the capabilities from the print service. If this instance is
     *  configured with either ``capabilities`` or a ``url`` and ``autoLoad``
     *  set to true, then this method does not need to be called from the
     *  application.
     */
    loadCapabilities: function() {
        if (!this.url) {
            return;
        }
        var url = this.url + "info.json";
        Ext.Ajax.request({
            url: url,
            method: "GET",
            disableCaching: false,
            success: function(response) {
                this.capabilities = Ext.decode(response.responseText);
                this.loadStores();
            },
            params: this.initialConfig.baseParams,
            scope: this
        });
    },
    
    /** private: method[loadStores]
     */
    loadStores: function() {
        this.scales.loadData(this.capabilities);
        this.dpis.loadData(this.capabilities);
        this.layouts.loadData(this.capabilities);
        
        this.setLayout(this.layouts.getAt(0));
        this.setDpi(this.dpis.getAt(0));
        this.fireEvent("loadcapabilities", this, this.capabilities);
    },
        
    /** private: method[encodeLayer]
     *  :param layer: ``OpenLayers.Layer``
     *  :return: ``Object``
     * 
     *  Encodes a layer for the print service.
     */
    encodeLayer: function(layer) {
        var encLayer;
        for(var c in this.encoders.layers) {
            if(OpenLayers.Layer[c] && layer instanceof OpenLayers.Layer[c]) {
                if(this.fireEvent("beforeencodelayer", this, layer) === false) {
                    return;
                }
                encLayer = this.encoders.layers[c].call(this, layer);
                this.fireEvent("encodelayer", this, layer, encLayer);
                break;
            }
        }
        // only return the encLayer object when we have a type. Prevents a
        // fallback on base encoders like HTTPRequest.
        return (encLayer && encLayer.type) ? encLayer : null;
    },

    /** private: method[getAbsoluteUrl]
     *  :param url: ``String``
     *  :return: ``String``
     *  
     *  Converts the provided url to an absolute url.
     */
    getAbsoluteUrl: function(url) {
        var a;
        if(Ext.isIE) {
            a = document.createElement("<a href='" + url + "'/>");
            a.style.display = "none";
            document.body.appendChild(a);
            a.href = a.href;
            document.body.removeChild(a);
        } else {
            a = document.createElement("a");
            a.href = url;
        }
        return a.href;
    },
    
    /** private: property[encoders]
     *  ``Object`` Encoders for all print content
     */
    encoders: {
        "layers": {
            "WMS": function(layer) {
                var enc = this.encoders.layers.HTTPRequest.call(this, layer);
                Ext.apply(enc, {
                    type: 'WMS',
                    layers: [layer.params.LAYERS].join(",").split(","),
                    format: layer.params.FORMAT,
                    styles: [layer.params.STYLES].join(",").split(",")
                });
                var param;
                for(var p in layer.params) {
                    param = p.toLowerCase();
                    if(!layer.DEFAULT_PARAMS[param] &&
                    "layers,styles,width,height,srs".indexOf(param) == -1) {
                        if(!enc.customParams) {
                            enc.customParams = {};
                        }
                        enc.customParams[p] = layer.params[p];
                    }
                }
                return enc;
            },
            "OSM": function(layer) {
                var enc = this.encoders.layers.TileCache.call(this, layer);
                return Ext.apply(enc, {
                    type: 'OSM',
                    baseURL: enc.baseURL.substr(0, enc.baseURL.indexOf("$")),
                    extension: "png"
                });
            },
            "TMS": function(layer) {
                var enc = this.encoders.layers.TileCache.call(this, layer);
                return Ext.apply(enc, {
                    type: 'TMS',
                    format: layer.type
                });
            },
            "TileCache": function(layer) {
                var enc = this.encoders.layers.HTTPRequest.call(this, layer);
                return Ext.apply(enc, {
                    type: 'TileCache',
                    layer: layer.layername,
                    maxExtent: layer.maxExtent.toArray(),
                    tileSize: [layer.tileSize.w, layer.tileSize.h],
                    extension: layer.extension,
                    resolutions: layer.serverResolutions || layer.resolutions
                });
            },
            "KaMapCache": function(layer) {
                var enc = this.encoders.layers.KaMap.call(this, layer);
                return Ext.apply(enc, {
                    type: 'KaMapCache',
                    // group param is mandatory when using KaMapCache
                    group: layer.params['g'],
                    metaTileWidth: layer.params['metaTileSize']['w'],
                    metaTileHeight: layer.params['metaTileSize']['h']
                });
            },
            "KaMap": function(layer) {
                var enc = this.encoders.layers.HTTPRequest.call(this, layer);
                return Ext.apply(enc, {
                    type: 'KaMap',
                    map: layer.params['map'],
                    extension: layer.params['i'],
                    // group param is optional when using KaMap
                    group: layer.params['g'] || "",
                    maxExtent: layer.maxExtent.toArray(),
                    tileSize: [layer.tileSize.w, layer.tileSize.h],
                    resolutions: layer.serverResolutions || layer.resolutions
                });
            },
            "HTTPRequest": function(layer) {
                return {
                    baseURL: this.getAbsoluteUrl(layer.url instanceof Array ?
                        layer.url[0] : layer.url),
                    opacity: (layer.opacity != null) ? layer.opacity : 1.0,
                    singleTile: layer.singleTile
                };
            },
            "Image": function(layer) {
                return {
                    type: 'Image',
                    baseURL: this.getAbsoluteUrl(layer.getURL(layer.extent)),
                    opacity: (layer.opacity != null) ? layer.opacity : 1.0,
                    extent: layer.extent.toArray(),
                    pixelSize: [layer.size.w, layer.size.h],
                    name: layer.name
                };
            },
            "Vector": function(layer) {
                if(!layer.features.length) {
                    return;
                }
                
                var encFeatures = [];
                var encStyles = {};
                var features = layer.features;
                var featureFormat = new OpenLayers.Format.GeoJSON();
                var styleFormat = new OpenLayers.Format.JSON();
                var nextId = 1;
                var styleDict = {};
                var feature, style, dictKey, dictItem, styleName;
                for(var i=0, len=features.length; i<len; ++i) {
                    feature = features[i];
                    style = feature.style || layer.style ||
                    layer.styleMap.createSymbolizer(feature,
                        feature.renderIntent);
                    dictKey = styleFormat.write(style);
                    dictItem = styleDict[dictKey];
                    if(dictItem) {
                        //this style is already known
                        styleName = dictItem;
                    } else {
                        //new style
                        styleDict[dictKey] = styleName = nextId++;
                        if(style.externalGraphic) {
                            encStyles[styleName] = Ext.applyIf({
                                externalGraphic: this.getAbsoluteUrl(
                                    style.externalGraphic)}, style);
                        } else {
                            encStyles[styleName] = style;
                        }
                    }
                    var featureGeoJson = featureFormat.extract.feature.call(
                        featureFormat, feature);
                    
                    featureGeoJson.properties = OpenLayers.Util.extend({
                        _gx_style: styleName
                    }, featureGeoJson.properties);
                    
                    encFeatures.push(featureGeoJson);
                }
                
                return {
                    type: 'Vector',
                    styles: encStyles,
                    styleProperty: '_gx_style',
                    geoJson: {
                        type: "FeatureCollection",
                        features: encFeatures
                    },
                    name: layer.name,
                    opacity: (layer.opacity != null) ? layer.opacity : 1.0
                };
            }
        },
        "legends": {
            "gx_wmslegend": function(legend) {
                var enc = this.encoders.legends.base.call(this, legend);
                var icons = [];
                for(var i=1, len=legend.items.getCount(); i<len; ++i) {
                    icons.push(this.getAbsoluteUrl(legend.items.get(i).url));
                }
                enc[0].classes[0] = {
                    name: "",
                    icons: icons
                };
                return enc;
            },
            "gx_urllegend": function(legend) {
                var enc = this.encoders.legends.base.call(this, legend);
                enc[0].classes.push({
                    name: "",
                    icon: this.getAbsoluteUrl(legend.items.get(1).url)
                });
                return enc;
            },
            "base": function(legend){
                return [{
                    name: legend.items.get(0).text,
                    classes: []
                }];
            }
        }
    }
    
});
