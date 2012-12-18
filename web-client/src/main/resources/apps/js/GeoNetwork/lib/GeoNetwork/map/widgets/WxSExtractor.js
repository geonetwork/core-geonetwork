/*
 * Copyright (C) 2001-2012 Food and Agriculture Organization of the
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
Ext.namespace('GeoNetwork');

/** api: (define)
 *  module = GeoNetwork
 *  class = WxSExtractor
 *  base_link = `Ext.FormPanel <http://extjs.com/deploy/dev/docs/?class=Ext.FormPanel>`_
 */
/** api: constructor 
 *  .. class:: WxSExtractor(config)
 *  
 *  WxSExtractor panel provide a simple form to define an area of interest,
 *  projection and output format in order to call an OGC service (ie. WFS)
 *  to retrieve features for this AOI.
 *  
 *  TODO : support WCS
 */
GeoNetwork.WxSExtractor = Ext.extend(Ext.form.FormPanel, {
    defaultConfig: {
        border: false,
        layout: 'form',
        /** api: config[west] 
         * Default west bound coordinate.
         */
        west: -180,
        /** api: config[south] 
         * Default south bound coordinate.
         */
        south: -90,
        /** api: config[east] 
         * Default east bound coordinate.
         */
        east: 180,
        /** api: config[north] 
         * Default north bound coordinate.
         */
        north: 90,
        /** api: config[hideCoordinates] 
         * Display bounds coordinates in the form or not. It may be not necessary if a map is attached.
         */
        hideCoordinates: false,
        /** api: config[projection] 
         * Default projection to use
         */
        projection: 'EPSG:4326',
        /** api: config[projection] 
         * Default projection to use
         */
        /** api: config[projectionList] 
         *  An array of projections with their labels. The projection must be supported by the server.
         */
        projectionList: [['EPSG:4326', 'WGS84']],
        /** api: config[version] 
         * Default version to use
         */
        version: '1.1.0',
        /** api: config[versionList] 
         *  An array of projections with their labels. The projection must be supported by the server.
         */
        versionList: [['1.0.0'], ['1.1.0']],
        /** api: config[outputFormat] 
         *  Default output format to use. It must be in the list of format provided in the GetCapabilities
         *  document of the service
         */
        outputFormat: 'GML2',
        /** api: config[preferredOutputFormat] 
         *  List of preferred output format in order of preference.
         */
        preferredOutputFormat: ['SHAPE-ZIP', 'GML2'],
        /** api: config[downloadCb] 
         *  A custom callback to use when the user click the download button. Callback parameters are
         *  the GetFeature url and the panel object.
         */
        downloadCb: function (url, panel) {
            window.open(url);
        },
        /** api: config[updateExtentCb] 
         * Callback to do some more things when extent is updated. 
         * Parameters are west, south, east, north
         * 
         * TODO : unused
         */
        updateExtentCb: null,
        /** api: config[map] 
         *  Define a map use to set the AOI.
         *  The AOI could be define using the map extent (if mapExtentMode set to true) or
         *  using a vector layer and drawing a rectangle on the map.
         */
        map: null,
        /** api: config[mapExtentMode] 
         *  true to use the map extent as extraction area
         */
        mapExtentMode: false
    },
    url: null,
    layer: null,
    outputFormatStore: null,
    projectionStore: null,
    featureTypeStore: null,
    versionStore: null,
    getCapabilitiesUrl: null,
    layerSelector: null,
    outputFormatSelector: null,
    coordinateCtrl: {},
    boxLayer: null,
    /**
     * Retrieve GetCapabilities document
     */
    getCapabilities: function (url, layer, type, version) {
        var panel = this;
        // Empty current records
        this.outputFormatStore.removeAll();
        this.featureTypeStore.removeAll();
        
        // Set values or use currents
        this.layer = layer || this.layer;
        this.type = type || "WFS";
        this.version = version || this.version;
        this.url = url || this.url;
        this.getCapabilitiesUrl = this.url.indexOf('GetCapabilities') !== -1 ? 
                    this.url : 
                    this.url + "?REQUEST=GetCapabilities&SERVICE=" + this.type + "&VERSION=" + this.version;
        
        // Load capabilities
        OpenLayers.Request.GET({
            url: this.getCapabilitiesUrl,
            success: function (result) {
                var wfsCapabilitiesFormat = new OpenLayers.Format.WFSCapabilities();
                var capabilities = wfsCapabilitiesFormat.read(result.responseText);
                
                // Get capability output formats
                var outputFormats = [];
                if (capabilities.operationsMetadata) {
                    for (var format in capabilities.operationsMetadata.GetFeature.parameters.outputFormat) {
                        if (capabilities.operationsMetadata.GetFeature.parameters.outputFormat.hasOwnProperty(format)) {
                            outputFormats.push([format, OpenLayers.i18n(format)]);
                        }
                    }
                } else {
                    Ext.each(capabilities.capability.request.getfeature.formats, function (format) {
                            outputFormats.push([format, OpenLayers.i18n(format)]);
                        }
                    );
                }
                
                panel.outputFormatStore.loadData(outputFormats);
                panel.featureTypeStore.loadData(capabilities.featureTypeList.featureTypes);
            },
            failure: function (response) {
                GeoNetwork.Message().msg({
                    title: OpenLayers.i18n('error'), 
                    msg: OpenLayers.i18n('wxs-extract-service-not-found'), 
                    tokens: {
                        misc: response.status,
                        url: panel.url
                    }, 
                    status: 'warning',
                    target: panel.getId()
                });
            }
        });
    },
    /**
     * Create form with:
     *  * layer selection (current layer selected)
     *  * projection selection
     *  * extent selection
     */
    getItems: function () {
        var items = [];
        
        // List of layers
        this.layerSelector = new Ext.form.ComboBox({
            mode: 'local',
            allowBlank: false,
            triggerAction: 'all',
            fieldLabel: OpenLayers.i18n('chooseALayer'),
            store: this.featureTypeStore,
            valueField: 'name',
            displayField: 'title',
            listeners: {
                change: function (f, newValue, oldValue) {
                    this.layer = newValue;
                    this.updateFeatureTypeInfo(this.layer);
                },
                scope: this
            }
        });
        items.push(this.layerSelector);
        
        // Projection
        items.push(new Ext.form.ComboBox({
            mode: 'local',
            allowBlank: false,
            triggerAction: 'all',
            fieldLabel: OpenLayers.i18n('projectionTitle'),
            store: this.projectionStore,
            valueField: 'id',
            displayField: 'name',
            value: this.projection,
            listeners: {
                change: function (f, newValue, oldValue) {
                    this.projection = newValue;
                },
                scope: this
            }
        }));
        
        // Version
        items.push(new Ext.form.ComboBox({
            mode: 'local',
            allowBlank: false,
            triggerAction: 'all',
            fieldLabel: OpenLayers.i18n('wfsVersion'),
            store: this.versionStore,
            valueField: 'id',
            displayField: 'id',
            value: this.version,
            listeners: {
                change: function (f, newValue, oldValue) {
                    this.version = newValue;
                    // Refresh getCapabilities
                    this.getCapabilities();
                },
                scope: this
            }
        }));
        
        // OutputFormat
        this.outputFormatSelector = new Ext.form.ComboBox({
            mode: 'local',
            allowBlank: false,
            triggerAction: 'all',
            fieldLabel: OpenLayers.i18n('outputFormat'),
            store: this.outputFormatStore,
            valueField: 'id',
            displayField: 'name',
            value: this.outputFormat,
            listeners: {
                change: function (f, newValue, oldValue) {
                    this.outputFormat = newValue;
                },
                scope: this
            }
        });
        items.push(this.outputFormatSelector);
        
        // TODO add MAXFEATURES, TIME
        
        // Extraction area
        var coords = ['west', 'south', 'east', 'north'];
        Ext.each(coords, function (item) {
            this.coordinateCtrl[item] = new Ext.form.TextField({
                fieldLabel: OpenLayers.i18n(item),
                hidden: this.hideCoordinates,
                listeners: {
                    change: function (f, newValue, oldValue) {
                        this.updateMapExtent(item, newValue);
                    },
                    scope: this
                }
            });
            items.push(this.coordinateCtrl[item]);
        }, this);
        
        return items;
    },
    download: function (extent) {
        var url = this.url + (this.url.indexOf('?') === -1 ? '?' : '');
        if (url.indexOf('GetCapabilities') !== -1) {
            url = url.replace(new RegExp("GetCapabilities", "g"), 'GetFeature');
        } else {
            url += "&REQUEST=GetFeature";
        }
        var downloadUrl = url + "&SERVICE=" + this.type + "&VERSION=" + this.version + 
                          "&TYPENAME=" + this.layer + 
                          "&BBOX=" + this.coordinateCtrl['west'].getValue() + "," + this.coordinateCtrl['south'].getValue() + 
                          "," + this.coordinateCtrl['east'].getValue() + "," + this.coordinateCtrl['north'].getValue() +
                          "&SRSNAME=" + this.projection +
                          "&OUTPUTFORMAT=" + this.outputFormat;
        this.downloadCb(downloadUrl, this);
    },
    updateExtent: function (newBounds) {
        var bounds = newBounds || this.map.getExtent();
        bounds.transform(this.map.getProjection(), new OpenLayers.Projection(this.projection));
        var boundsArr = bounds.toArray();
        this.updateExtentCoordinates(boundsArr[0], boundsArr[1], boundsArr[2], boundsArr[3]);
    },
    /**
     * Update form values from map information
     */
    updateExtentCoordinates: function (west, south, east, north) {
        // Update form values
        this.coordinateCtrl.west.setValue(west);
        this.coordinateCtrl.south.setValue(south);
        this.coordinateCtrl.east.setValue(east);
        this.coordinateCtrl.north.setValue(north);
    },
    /**
     * Update map bbox from form value
     */
    updateMapExtent: function (coord, value) {
        if (this.map) {
            var bounds = new OpenLayers.Bounds(this.coordinateCtrl.west.getValue(), 
                                           this.coordinateCtrl.south.getValue(), 
                                           this.coordinateCtrl.east.getValue(), 
                                           this.coordinateCtrl.north.getValue());
            bounds.transform(new OpenLayers.Projection(this.projection), this.map.getProjection());
            if (this.mapExtentMode) {
                this.map.zoomToExtent(bounds);
            } else {
                var feature = new OpenLayers.Feature.Vector(bounds.toGeometry());
                this.boxLayer.destroyFeatures();
                this.boxLayer.addFeatures(feature);
            }
        }
    },
    updateFeatureTypeInfo: function (featureTypeName) {
        var record = this.featureTypeStore.query('name', featureTypeName, true, false);
        var layer = record.items[0];
        if (record.length === 1) {
            this.layerSelector.setValue(layer.get('name'));
            
            var bounds = layer.get('bounds');
            if (bounds) {
                this.updateExtentCoordinates(bounds.left, bounds.bottom, bounds.right, bounds.top);
            }
        } else {
            GeoNetwork.Message().msg({
                title: OpenLayers.i18n('warning'), 
                msg: OpenLayers.i18n('wxs-extract-layer-not-found'), 
                tokens: {
                    layer: this.layer,
                    url: this.url
                }, 
                status: 'warning',
                target: this.getId()
            });
        }
    },
    initComponent: function () {
        Ext.applyIf(this, this.defaultConfig);
        
        this.outputFormatStore = new Ext.data.ArrayStore({
            id: 0,
            fields: ['id', 'name'], 
            listeners: {
                load: function (store, formats) {
                    for (var i = 0; i < this.preferredOutputFormat.length; i ++) {
                        var preferredFormat = this.preferredOutputFormat[i];
                        var theFormat = store.query('id', preferredFormat);
                        if (theFormat.length === 1) {
                            this.outputFormatSelector.setValue(preferredFormat);
                            this.outputFormatSelector.fireEvent('change', null, preferredFormat);
                            break;
                        }
                    }
                },
                scope: this
            }
        });
        
        this.featureTypeStore = new Ext.data.JsonStore({
            id: 0,
            fields: ['name', 'title', 'abstract', 'keywords', 'bounds', 'srs'],
            listeners: {
                load: function (store, records) {
                    this.updateFeatureTypeInfo(this.layer.split(':').pop());
                },
                scope: this
            }
        });
        
        this.projectionStore = new Ext.data.ArrayStore({
            id: 0,
            fields: ['id', 'name'],
            data: this.projectionList
        });
        this.versionStore = new Ext.data.ArrayStore({
            id: 0,
            fields: ['id'],
            data: this.versionList
        });
        this.items = this.getItems();
        
        GeoNetwork.WxSExtractor.superclass.initComponent.call(this);
        
        this.addButton(new Ext.Button({
            text: OpenLayers.i18n('dataDownload'),
            iconCls: 'WFSDownloadIcon',
            listeners: {
                click: this.download,
                scope: this
            }
        }));
        
        
        if (this.north) {
            this.updateExtentCoordinates(this.west, this.south, this.east, this.north);
        }
        if (this.url) {
            this.getCapabilities(this.url, this.layer, this.type, this.version);
        }
        
        if (this.map) {
            if (this.mapExtentMode) {
                this.map.events.register('moveend', this, this.updateExtent);
            } else {
                var panel = this;
                this.boxLayer = new OpenLayers.Layer.Vector("Data extraction area");
                // Remove all features before adding a new one.
                this.boxLayer.events.register('beforefeatureadded', this, function () {
                    this.boxLayer.removeAllFeatures();
                });
                this.map.addLayer(this.boxLayer);
                
                var control = new OpenLayers.Control.DrawFeature(this.boxLayer,
                        OpenLayers.Handler.RegularPolygon, {
                    handlerOptions: {
                        sides: 4,
                        citeCompliant: true,
                        irregular: true
                    },
                    featureAdded: function (feature) {
                        panel.updateExtent(feature.geometry.getBounds());
                    }
                });
                this.map.addControl(control);
                control.activate();
                this.add(new Ext.Button({
                    text: OpenLayers.i18n('defineExtractionArea'),
                    pressed: true,
                    allowDepress: true,
                    toggleGroup: 'drawCtr',
                    listeners: {
                        toggle: function (bt, pressed) {
                            if (pressed) {
                                this.boxLayer.removeAllFeatures();
                                control.activate();
                            } else {
                                control.deactivate();
                            }
                        },
                        scope: this
                    }
                }));
                
                // The map layer and control must be removed from the map when the panel is destroyed
                this.on('beforedestroy', function () {
                    this.map.removeControl(control);
                    this.map.removeLayer(this.boxLayer);
                });
            }
        }
    }
});

/** api: xtype = gn_wxsextractor */
Ext.reg('gn_wxsextractor', GeoNetwork.WxSExtractor);