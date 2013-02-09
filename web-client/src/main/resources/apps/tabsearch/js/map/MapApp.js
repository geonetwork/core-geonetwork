/*
 * Copyright (C) 2009 GeoNetwork
 *
 * This file is part of GeoNetwork
 *
 * GeoNetwork is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoNetwork is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoNetwork.  If not, see <http://www.gnu.org/licenses/>.
 */
Ext.namespace('GeoNetwork');

var mapInit = false;

var activeIndex = 0;


// create application
GeoNetwork.mapApp = function() {
    // private vars:
    var toolbar, toctoolbar, tocbbar, viewport;

    var tree, legendPanel, mapLateralPanel, printPanel, printProvider, printPage, pageLayer;
    
    var featureinfo;

    var activeNode;

    // private
    var map;

    var fixedScales;

    var featureinfolayer;

    var layerLoadingMask;

    var layers;

    var setMap = function(mapC) {
        map = mapC;
    };
    
    // private functions

    /**
     * Creates the OL Map 
     *
     */
    var createMap = function(mapOptions, scales) {
        var options = mapOptions || {
            projection: "EPSG:4326",
            units: "degrees",
            maxExtent: new OpenLayers.Bounds(-180,-90,180,90),
            restrictedExtent: new OpenLayers.Bounds(-180,-90,180,90),
            controls: []
        };

        if (GeoNetwork.map.CONTEXT) {
            // Load map context
            var request = OpenLayers.Request.GET({
                url: GeoNetwork.map.CONTEXT,
                async: false
            });
            if (request.responseText) {
                
                var text = request.responseText;
                var format = new OpenLayers.Format.WMC();
                map = format.read(text, {map:options});
            }
        } 
        else if (GeoNetwork.map.OWS) {
            // Load map context
            var request = OpenLayers.Request.GET({
                url: GeoNetwork.map.OWS,
                async: false
            });
            if (request.responseText) {
                var parser = new OpenLayers.Format.OWSContext();
                var text = request.responseText;
                map = parser.read(text, {map: options});
            }
        }
        else {
            map = new OpenLayers.Map('ol_map', options);
            fixedScales = scales;
        }
    };

    /**
     * Adds a layer to the map 
     *
     */
    var createWmsLayer = function(name, url, params, options) {
        map.addLayer(new OpenLayers.Layer.WMS(name, url, params, options));
    };

    var createLayer = function(olLayer) {
        map.addLayer(olLayer);
    };


    var createDummyBaseLayer = function() {
        var graphic = new OpenLayers.Layer.Image('Dummy', '../../../js/OpenLayers/img/blank.gif', new OpenLayers.Bounds(0, 300000, 300000, 625000),
                map.getSize(), {isBaseLayer: true, displayInLayerSwitcher: false});
        map.addLayer(graphic);
    };

    /**
     * Configure the map controls
     *
     */
    var addMapControls = function() {
        map.addControl(new GeoNetwork.Control.ZoomWheel());
        map.addControl(new OpenLayers.Control.LoadingPanel());
        //map.addControl(new OpenLayers.Control.ScaleBar());
    };
    
    /**
     * Handler to manage the remove of a layer in the map
     *
     */
     var removeLayerHandler = function(node) {
        if (node) {
            var layer;
                layer = node.attributes.layer;
                if (layer) {
                    if (!layer.isBaseLayer) {
                        if ((typeof(layer.isLoading) == "undefined") ||  // Layers added from WMC
                            (layer.isLoading == false)) {
                            map.removeLayer(layer);
                            //Ext.getCmp('legendwms').forceRemoveLegend(node.attributes.layer.id);

                            if (activeNode == node) activeNode = null;
                            refreshTocToolbar(activeNode);
                            Ext.getCmp('toctree').getSelectionModel().clearSelections();
                       }
                    }
                }
            }
    };

    var removeLayerHandlerContextMenu = function(){
        var node = Ext.getCmp('toctree').getSelectionModel().getSelectedNode();
        removeLayerHandler(node);
    };
    /**
     * Handler to manage WMS time layers
     *
     */
    var wmsTimeHandler = function(node) {
        if (node) {
            var layer;
            layer = node.attributes.layer;
            if (layer && layer.dimensions && layer.dimensions.time) {
               GeoNetwork.WindowManager.showWindow("wmstime");
               GeoNetwork.WindowManager.getWindow("wmstime").setLayer(layer);
            }
        }
    };

    var wmsTimeHandlerContextMenu = function() {
       var node = Ext.getCmp('toctree').getSelectionModel().getSelectedNode();
       wmsTimeHandler(node);
    };

    /**
     * Handler to manage the selection of WMS layers with several SLD styles defined
     *
     */
    var stylesLayerHandler = function(node) {
        if (node) {
            var layer;
            layer = node.attributes.layer;
            if ((layer) && (layer.styles) && (layer.styles.length > 1)) {
                GeoNetwork.WindowManager.showWindow("layerstyles");
                GeoNetwork.WindowManager.getWindow("layerstyles").showLayerStyles(layer);
            }
        }
    };

    var stylesLayerHandlerContextMenu = function() {
       var node = Ext.getCmp('toctree').getSelectionModel().getSelectedNode();
       stylesLayerHandler(node);
    };

    /**
     * Handler to manage the WMS info window for the layers
     *
     */
    var metadataLayerHandler = function(node) {
        if (node) {
            var layer;
            layer = node.attributes.layer;
            if (layer) {
                GeoNetwork.WindowManager.showWindow("wmsinfo");
                GeoNetwork.WindowManager.getWindow("wmsinfo").showLayerInfo(layer);
            }
        }
    };

    var metadataLayerHandlerContextMenu = function() {
        var node = Ext.getCmp('toctree').getSelectionModel().getSelectedNode();
        metadataLayerHandler(node);
    };

    
    /**
     * Updates the TOC toolbar buttons depending on TOC layer selected 
     *
     */
    var refreshTocToolbar = function(node) {
    	
    	activeNode = node;
        
       if ((node) && (node.attributes.layer)) {
            if (node.parentNode.attributes.nodeType == "gx_baselayercontainer") {
                Ext.getCmp("tbRemoveButton").disable();
            } else {
                Ext.getCmp("tbRemoveButton").enable();
            }

            var layer = node.attributes.layer;

            if (layer && layer.dimensions && layer.dimensions.time) {
                 Ext.getCmp("tbWmsTimeButton").enable();
            } else {
                Ext.getCmp("tbWmsTimeButton").disable();
            }

            if ((layer) && ((!layer.styles) || (layer.styles.length < 2))) {
                Ext.getCmp("tbStylesButton").disable();
            } else {
                Ext.getCmp("tbStylesButton").enable();
            }

            Ext.getCmp("tbMetadataButton").enable();
            Ext.getCmp("btnZoomToExtent").enable();



        } else {
            Ext.getCmp("tbRemoveButton").disable();
            Ext.getCmp("tbWmsTimeButton").disable();
            Ext.getCmp("tbStylesButton").disable();
            Ext.getCmp("tbMetadataButton").disable(); 
            Ext.getCmp("btnZoomToExtent").disable();
        }
    };
	var createPrintPanel = function() {
        // The printProvider that connects us to the print service
        printProvider = new GeoExt.data.PrintProvider({
            method: "POST",
            url: GeoNetwork.map.printCapabilities,
            autoLoad: true
        });
        
        printPage = new GeoExt.data.PrintPage({
            printProvider: printProvider
        });
        
        pageLayer = new OpenLayers.Layer.Vector(OpenLayers.i18n('printLayer'), {visibility: false});
        pageLayer.addFeatures(printPage.feature);
        
        map.addLayer(pageLayer);
        
        map.events.register('moveend', map, function(){
            printPage.fit(this, {
                mode: "screen"
            });
        });
        // The form with fields controlling the print output
        printPanel = new Ext.form.FormPanel({
            title: OpenLayers.i18n("mf.print.print"),
            bodyStyle: "padding:5px",
            labelAlign: "top",
            defaults: {anchor: "100%"},
            items: [{
                xtype: "textarea",
                name: "comment",
                value: "",
                fieldLabel: OpenLayers.i18n("mf.print.comment"),
                plugins: new GeoExt.plugins.PrintPageField({
                    printPage: printPage
                })
            }, {
                xtype: "combo",
                store: printProvider.layouts,
                displayField: "name",
                fieldLabel: OpenLayers.i18n("layout"),
                typeAhead: true,
                mode: "local",
                triggerAction: "all",
                plugins: new GeoExt.plugins.PrintProviderField({
                    printProvider: printProvider
                })
            }, {
                xtype: "combo",
                store: printProvider.dpis,
                displayField: "name",
                fieldLabel: OpenLayers.i18n("mf.print.dpi"),
                tpl: '<tpl for="."><div class="x-combo-list-item">{name} dpi</div></tpl>',
                typeAhead: true,
                mode: "local",
                triggerAction: "all",
                plugins: new GeoExt.plugins.PrintProviderField({
                    printProvider: printProvider
                }),
                // the plugin will work even if we modify a combo value
                setValue: function(v) {
                    v = parseInt(v, 10) + " dpi";
                    Ext.form.ComboBox.prototype.setValue.apply(this, arguments);
                }
            }, {
                xtype: "combo",
                store: printProvider.scales,
                displayField: "name",
                fieldLabel: OpenLayers.i18n("mf.print.scale"),
                typeAhead: true,
                mode: "local",
                triggerAction: "all",
                plugins: new GeoExt.plugins.PrintPageField({
                    printPage: printPage
                })
            }, {
                xtype: "textfield",
                name: "rotation",
                fieldLabel: OpenLayers.i18n("mf.print.rotation"),
                plugins: new GeoExt.plugins.PrintPageField({
                    printPage: printPage
                })
            }],
            buttons: [{
                text: OpenLayers.i18n("mf.print.generatingPDF"),
                handler: function() {
                    printProvider.print(Ext.getCmp('mappanel'), printPage);
                }
            }],
            listeners: {
                beforeexpand: function() {
                    this.setVisibility(true);
                },
                beforecollapse: function() {
                    this.setVisibility(false);
                },
                scope: pageLayer
            }
        });
    };
    /**
     * Creates the map viewer toolbars
     */
    var createToolbars = function() {
        toctoolbar = [];
        // Layer TOC toolbar
        var action = new GeoExt.Action({
            handler: function() {
                GeoNetwork.WindowManager.showWindow("addwms");
            },
            iconCls: 'addLayer',
            tooltip: OpenLayers.i18n("addWMSButtonText")
        });

        toctoolbar.push(action);
        action = new GeoExt.Action({
            id: "tbRemoveButton",
            handler: function() {
                removeLayerHandler(activeNode);
            },
            iconCls: 'deleteLayer',
            tooltip: OpenLayers.i18n("removeButtonText")
        });
        
        toctoolbar.push(action);

        toctoolbar.push("-");	
        
        action = new GeoExt.Action({
            id: "tbStylesButton",
            handler: function() {
                stylesLayerHandler(activeNode);
                    },
            iconCls: 'layerStyles',
            tooltip: "Layer styles"
        });
        
        toctoolbar.push(action);

        toctoolbar.push("-");
        
        action = new GeoExt.Action({
            id: "tbMetadataButton",
            handler: function() {
                metadataLayerHandler(activeNode);
                    },
            iconCls: 'wmsInfo',
            tooltip: OpenLayers.i18n("metadataButtonText")
        });
        
        toctoolbar.push(action);

        toctoolbar.push("-");
        
        action = new GeoExt.Action({
            id: "tbWmsTimeButton",
            handler: function() {
                wmsTimeHandler(activeNode);
                    },
            iconCls: 'wmsTime',
            tooltip: "WMS Time"
        });
        
        toctoolbar.push(action);


        // Main toolbar
        toolbar = [];
   
        action = new GeoExt.Action({
            control: new OpenLayers.Control.ZoomToMaxExtent(),
            map: map,
            iconCls: 'zoomfull'
            	//,
            //tooltip: {title: OpenLayers.i18n("zoomToMaxExtentTooltipTitle"), text: OpenLayers.i18n("zoomToMaxExtentTooltipText")}
        });

        toolbar.push(action);

        toolbar.push("-");

        action = new GeoExt.Action({
            iconCls: 'zoomlayer',
            id: 'btnZoomToExtent',
            //tooltip: {title: OpenLayers.i18n("zoomlayerTooltipTitle"), text: OpenLayers.i18n("zoomlayerTooltipText")},
            handler: function() {
                var node = activeNode;
                var layer;

                if (node) {
                    layer = node.attributes.layer;
                    if (layer) {
                        if (layer.llbbox) {
                            // store info as wgs84
                            var mapProj = map.getProjectionObject();
                            var wgs84 = new OpenLayers.Projection("WGS84");

                            var minMapxy = new OpenLayers.LonLat(layer.llbbox[0], layer.llbbox[1]).transform(wgs84, mapProj);
                            var maxMapxy = new OpenLayers.LonLat(layer.llbbox[2], layer.llbbox[3]).transform(wgs84, mapProj);

                            var extent = new OpenLayers.Bounds();
                            extent.left = minMapxy.lon;
                            extent.right = maxMapxy.lon;
                            extent.top = maxMapxy.lat;
                            extent.bottom = minMapxy.lat;

                            map.zoomToExtent(extent);

                        // If layer has no boundingbox info, use full extent
                        } else {
                            map.zoomToMaxExtent();
                        }
                    } else {
                        Ext.MessageBox.alert(OpenLayers.i18n("zoomlayer.selectLayerTitle"),
                            OpenLayers.i18n("zoomlayer.selectLayerText"));
                    }
                } else {
                     Ext.MessageBox.alert(OpenLayers.i18n("zoomlayer.selectLayerTitle"),
                            OpenLayers.i18n("zoomlayer.selectLayerText"));
                }

            }
        });

        toolbar.push(action);
        
        toolbar.push("-");
        
        action = new GeoExt.Action({
            control: new OpenLayers.Control.ZoomBox(),
            map: map,
            toggleGroup: "move",
            allowDepress: false,
            iconCls: 'zoomin'
            //tooltip: {title: OpenLayers.i18n("zoominTooltipTitle"), text: OpenLayers.i18n("zoominTooltipText")}
        });

        toolbar.push(action);

        action = new GeoExt.Action({
            control:  new OpenLayers.Control.ZoomBox({
                    displayClass: 'ZoomOut',
                    out: true
                }),
            toggleGroup: "move",
            allowDepress: false,
            map: map,
            iconCls: 'zoomout'
            //tooltip:  {title: OpenLayers.i18n("zoomoutTooltipTitle"), text: OpenLayers.i18n("zoomoutTooltipText")}
        });

        toolbar.push(action);

        action = new GeoExt.Action({
            control: new OpenLayers.Control.DragPan({
                    isDefault: true
                }),
            toggleGroup: "move",
            allowDepress: false,
            pressed: true,
            map: map,
            iconCls: 'pan'
            //tooltip:  {title: OpenLayers.i18n("dragTooltipTitle"), text: OpenLayers.i18n("dragTooltipText")}
        });

        toolbar.push(action);
        
        toolbar.push("-");

        featureinfo = new OpenLayers.Control.WMSGetFeatureInfo({drillDown: true, infoFormat: 'application/vnd.ogc.gml'});

        var moveLayerToTop = function(layertomove) {
            var idx = -1;
            for (var i=0, len = map.layers.length; i<len; i++) {
                var layer = map.layers[i];
                if (layer != layertomove) {
                    idx = Math.max(map.getLayerIndex(
                        map.layers[i]), idx);
                }
            }
            if (map.getLayerIndex(layertomove) < idx) {
                map.setLayerIndex(layertomove, idx+1);
            }
        };

        featureinfolayer = new OpenLayers.Layer.Vector("Feature info", {displayInLayerSwitcher: false,
            styleMap: new OpenLayers.StyleMap({
                externalGraphic: OpenLayers.Util.getImagesLocation() + "marker.png",
                pointRadius: 12
            })
        });


        featureinfo.events.on({
            'getfeatureinfo': function(evt) {
                var lonlat = map.getLonLatFromViewPortPx(evt.xy);
                var point = new OpenLayers.Geometry.Point(lonlat.lon, lonlat.lat);
                featureinfolayer.destroyFeatures();
                featureinfolayer.addFeatures(new OpenLayers.Feature.Vector(point));
                moveLayerToTop(featureinfolayer);
                GeoNetwork.WindowManager.showWindow("featureinfo");
                GeoNetwork.WindowManager.getWindow("featureinfo").setMap(map);
                GeoNetwork.WindowManager.getWindow("featureinfo").setFeatures(evt.features);
            },
            'deactivate': function() {
                featureinfolayer.destroyFeatures();
            }
        });


        action = new GeoExt.Action({
            control: featureinfo,
            toggleGroup: "move",
            allowDepress: false,
            pressed: false,
            map: map,
            iconCls: 'query'
            //tooltip: {title: OpenLayers.i18n('featureInfoTooltipTitle'), text: OpenLayers.i18n('featureInfoTooltipText') }
        });
        
        toolbar.push(action);
        
        toolbar.push("-");

        // Navigation history - two "button" controls
        ctrl = new OpenLayers.Control.NavigationHistory();
        map.addControl(ctrl);

        action = new GeoExt.Action({
            control: ctrl.previous,
            disabled: true,
            map: map,
            iconCls: 'back'
            //tooltip: {title: OpenLayers.i18n("previousTooltipTitle"), text: OpenLayers.i18n("previosTooltipText")}
        });
        toolbar.push(action);

        action = new GeoExt.Action({
            control: ctrl.next,
            disabled: true,
            map: map,
            iconCls: 'next'
            //tooltip: {title: OpenLayers.i18n("nextTooltipTitle"), text: OpenLayers.i18n("nextTooltipText")}
        });
        toolbar.push(action);

        toolbar.push("-");
        
        action = new GeoExt.Action({
            iconCls: 'savewmc',
            //tooltip: {title: OpenLayers.i18n("savewmcTooltipTitle"), text: OpenLayers.i18n("savewmcTooltipText")},
            handler: function() {
                GeoNetwork.WMCManager.saveContext(map);
            }
        });

        toolbar.push(action);
        
        action = new GeoExt.Action({
            iconCls: 'loadwmc',
            //tooltip: {title: OpenLayers.i18n("loadwmcTooltipTitle"), text: OpenLayers.i18n("loadwmcTooltipText")},
            handler: function() {
                GeoNetwork.WindowManager.showWindow("loadwmc");
            }
        });

        toolbar.push(action);
        
        // create split button for measure controls
//
//        var measureSplit = new Ext.SplitButton({
//            iconCls: "icon-measure-length",
//            tooltip: "Measure",
//            enableToggle: true,
//            toggleGroup: "move"	, // Ext doesn't respect this, registered with ButtonToggleMgr below
//            allowDepress: false, // Ext doesn't respect this, handler deals with it
//            handler: function(button, event) {
//                // allowDepress should deal with this first condition
//                if(!button.pressed) {
//                    button.toggle();
//                } else {
//                    button.menu.items.itemAt(activeIndex).setChecked(true);
//                }
//            },
//            listeners: {
//                toggle: function(button, pressed) {
//                    // toggleGroup should handle this
//                    if(!pressed) {
//                        button.menu.items.each(function(i) {
//                            i.setChecked(false);
//                        });
//                    }
//                },
//                render: function(button) {
//                    // toggleGroup should handle this
//                    Ext.ButtonToggleMgr.register(button);
//                }
//            },
//            menu: new Ext.menu.Menu({
//                items: [
//                    new Ext.menu.CheckItem(
//                        new GeoExt.Action({
//                            text: "Length",
//                            iconCls: "icon-measure-length",
//                            toggleGroup: "measure",
//                            group: "move",
//                            allowDepress: false,
//                            map: map,
//                            control: createMeasureControl(
//                                OpenLayers.Handler.Path, "Length"
//                            )
//                        })
//                    ),
//                    new Ext.menu.CheckItem(
//                        new GeoExt.Action({
//                            text: "Area",
//                            iconCls: "icon-measure-area",
//                            toggleGroup: "measure",
//                            group: "move",
//                            allowDepress: false,
//                            map: map,
//                            control: createMeasureControl(
//                                OpenLayers.Handler.Polygon, "Area"
//                            )
//                        })
//                    )
//                ]
//            })
//        });
//        
//        measureSplit.menu.items.each(function(item, index) {
//            item.on({checkchange: function(item, checked) {
//                measureSplit.toggle(checked);
//                if(checked) {
//                    activeIndex = index;
//                    measureSplit.setIconClass(item.iconCls);
//                }
//            }});
//        });
//        
//        toolbar.push(measureSplit);

        toolbar.push('->');
        toolbar.push({xtype: 'gn_projectionselector', projections: GeoNetwork.ProjectionList, fieldLabel: OpenLayers.i18n("projectionTitle"), map: map});	
    };

    /**
     * Map overlay with scale image and scale selector
     * 
     */
    var createMapOverlay = function() {
        var scaleLinePanel = new Ext.Panel({
            cls: 'olControlScaleLine overlay-element overlay-scaleline',
            border: false
        });

        scaleLinePanel.on('render', function(){
            var scaleLine = new OpenLayers.Control.ScaleLine({
                div: scaleLinePanel.body.dom
            });

            map.addControl(scaleLine);
            scaleLine.activate();
        }, this);

        var zoomStore;

        if (fixedScales && fixedScales.length > 0) {
            var zooms = [];
            var scales = fixedScales;
            var units = map.baseLayer.units;

            for (var i=scales.length-1; i >= 0; i--) {
                var scale = scales[i];
                zooms.push({
                    level: i,
                    resolution: OpenLayers.Util.getResolutionFromScale(scale, units),
                    scale: scale
                });
            }

            zoomStore = new GeoExt.data.ScaleStore({});
            zoomStore.loadData(zooms);

        } else {
            zoomStore = new GeoExt.data.ScaleStore({
                map: map
            });
        }

        var zoomSelector = new Ext.form.ComboBox({
            emptyText: 'Zoom level',
            tpl: '<tpl for="."><div class="x-combo-list-item">1 : {[parseInt(values.scale)]}</div></tpl>',
            editable: false,
            triggerAction: 'all',
            mode: 'local',
            store: zoomStore,
            width: 110
        });

        zoomSelector.on('click', function(evt){evt.stopEvent();});
        zoomSelector.on('mousedown', function(evt){evt.stopEvent();});

        zoomSelector.on('select', function(combo, record, index) {
                map.zoomTo(record.data.level);
            },
            this);

        var zoomSelectorWrapper = new Ext.Panel({
            items: [zoomSelector],
            cls: 'overlay-element overlay-scalechooser',
            border: false });

        map.events.register('zoomend', this, function() {
            var scale = zoomStore.queryBy(function(record){
                return map.getZoom() == record.data.level;
            });

            if (scale.length > 0) {
                scale = scale.items[0];
                zoomSelector.setValue("1 : " + parseInt(scale.data.scale, 10));
            } else {
                if (!zoomSelector.rendered) {
                    return;
                }
                zoomSelector.clearValue();
            }
        });

        var mapOverlay = new Ext.Panel({
            // title: "Overlay",
            cls: 'map-overlay',
            items: [
                scaleLinePanel,
                zoomSelectorWrapper
            ]
        });


        mapOverlay.on("afterlayout", function(){
            scaleLinePanel.body.dom.style.position = 'relative';
            scaleLinePanel.body.dom.style.display = 'inline';

            mapOverlay.getEl().on("click", function(x){x.stopEvent();});
            mapOverlay.getEl().on("mousedown", function(x){x.stopEvent();});
        }, this);

        return mapOverlay;
    };
        
    /**
     * Measure control
     * 
     */
    var createMeasureControl = function(handlerType, title) {         
        var styleMap = new OpenLayers.StyleMap({
            "default": new OpenLayers.Style(null, {
                rules: [new OpenLayers.Rule({
                    symbolizer: {
                        "Point": {
                            pointRadius: 4,
                            graphicName: "square",
                            fillColor: "white",
                            fillOpacity: 1,
                            strokeWidth: 1,
                            strokeOpacity: 1,
                            strokeColor: "#333333"
                        },
                        "Line": {
                            strokeWidth: 3,
                            strokeOpacity: 1,
                            strokeColor: "#666666",
                            strokeDashstyle: "dash"
                        },
                        "Polygon": {
                            strokeWidth: 2,
                            strokeOpacity: 1,
                            strokeColor: "#666666",
                            fillColor: "white",
                            fillOpacity: 0.3
                        }
                    }
                })]
            })
        });

        var cleanup = function() {
            if (measureToolTip) {
                measureToolTip.destroy();
            }   
        };

        var makeString = function(metricData) {
            var metric = metricData.measure;
            var metricUnit = metricData.units;
           
            measureControl.displaySystem = "english";
           
            var englishData = metricData.geometry.CLASS_NAME.indexOf("LineString") > -1 ?
            measureControl.getBestLength(metricData.geometry) :
            measureControl.getBestArea(metricData.geometry);

            var english = englishData[0];
            var englishUnit = englishData[1];
           
            measureControl.displaySystem = "metric";
            var dim = metricData.order == 2 ?
            '<sup>2</sup>' :
            '';
           
            return metric.toFixed(2) + " " + metricUnit + dim + "<br>" +
                english.toFixed(2) + " " + englishUnit + dim;
        };
       
        var measureToolTip;
        var measureControl = new OpenLayers.Control.Measure(handlerType, {
            persist: true,
            handlerOptions: {layerOptions: {styleMap: styleMap}},
            eventListeners: {
                measurepartial: function(event) {
                    cleanup();
                    measureToolTip = new Ext.ToolTip({
                        html: makeString(event),
                        title: title,
                        autoHide: false,
                        closable: true,
                        draggable: false,
                        mouseOffset: [0, 0],
                        showDelay: 1,
                        listeners: {hide: cleanup}
                    });
                    if(event.measure > 0) {
                        var px = measureControl.handler.lastUp;
                        var p0 = Ext.getCmp('mappanel').getPosition();
                        measureToolTip.targetXY = [p0[0] + px.x, p0[1] + px.y];
                        measureToolTip.show();
                    }
                },
                measure: function(event) {
                    cleanup();                   
                    measureToolTip = new Ext.ToolTip({
                        target: Ext.getBody(),
                        html: makeString(event),
                        title: title,
                        autoHide: false,
                        closable: true,
                        draggable: false,
                        mouseOffset: [0, 0],
                        showDelay: 1,
                        listeners: {
                            hide: function() {
                                measureControl.cancel();
                                cleanup();
                            }
                        }
                    });
                },
                deactivate: cleanup,
                scope: this
            }
        });

        return measureControl;
    };

    
    /**
     * Creates the layers tree control
     *
     */
    var createTree = function() {
    
        // using OpenLayers.Format.JSON to create a nice formatted string of the
        // configuration for editing it in the UI
        var treeConfig = new OpenLayers.Format.JSON().write([{
            nodeType: "gx_baselayercontainer",
            text: OpenLayers.i18n('baseLayerList')
        }, {
            nodeType: "gx_overlaylayercontainer",
            text: OpenLayers.i18n('overlaysList'),
            expanded: true,
            // render the nodes inside this container with a radio button,
            // and assign them the group "foo".
            loader: {
                baseAttrs: {
                    radioGroup: "foo",
                    slider: "layeropacityslider",
                    uiProvider: "layernodeui"
                }
            }
        }], true);

        var LayerNodeUI = Ext.extend(
                GeoExt.tree.LayerNodeUI, new GeoExt.tree.TreeNodeUIEventMixin() 
            );

        // create the tree with the configuration from above
        tree = new Ext.tree.TreePanel({
            title: OpenLayers.i18n('layerTree'),
            id: "toctree",
            enableDD: true,
            loader: new Ext.tree.TreeLoader({
                // applyLoader has to be set to false to not interfer with loaders
                // of nodes further down the tree hierarchy
                applyLoader: false,
                uiProviders: {
                    "layernodeui": LayerNodeUI
                }
            }),
            plugins: [
//              new GeoExt.plugins.TreeNodeRadioButton({
//	              listeners: {
//	                  "radiochange": function(node) {
//	                	  activeNode = node;
//	                  }
//	              }
//              }),
              new GeoExt.tree.LayerOpacitySliderPlugin({
                  listeners: { 
                      "opacityslide": function(node, value) {
                      }                
                  }
              })
            ],
            root: {
                nodeType: "async",
                // the children property of an Ext.tree.AsyncTreeNode is used to
                // provide an initial set of layer nodes. We use the treeConfig
                // from above, that we created with OpenLayers.Format.JSON.write.
                children: Ext.decode(treeConfig)
            },          
            listeners:{
                contextmenu:function(node,e){
                    if ((node.attributes.nodeType != "gx_overlaylayercontainer") &&
                         (node.attributes.nodeType != "gx_baselayercontainer"))
                    {
                        node.select();
                        var c = node.getOwnerTree().contextMenu;
                        if (node.parentNode.attributes.nodeType == "gx_baselayercontainer") {
                            c.items.get("removeMenu").disable();
                        } else {
                            c.items.get("removeMenu").enable();
                        }

                        c.items.get("addMenu").hide();
                        
                        var layer = node.attributes.layer;

                        if (layer && layer.dimensions && layer.dimensions.time) {
                            c.items.get("wmsTimeMenu").enable();
                        } else {
                            c.items.get("wmsTimeMenu").disable();
                        }

                        if ((layer) && ((!layer.styles) || (layer.styles.length < 2))) {
                            c.items.get("stylesMenu").disable();
                        } else {
                            c.items.get("stylesMenu").enable();
                        }

                        c.contextNode=node;
                        c.showAt(e.getXY());
                    } else {
                    
                        if (node.attributes.nodeType == "gx_overlaylayercontainer") {
                        
                            node.select();
                            c = node.getOwnerTree().contextMenu;
                            
                            c.items.get("addMenu").show();
                            c.items.get("removeMenu").hide();
                            c.items.get("wmsTimeMenu").hide();
                            c.items.get("stylesMenu").hide();			
                            c.items.get("metadataMenu").hide();
            
                            c.contextNode=node;
                            c.showAt(e.getXY());
                        }
                    }
            },scope:this},
            contextMenu:new Ext.menu.Menu({
                items:[{
                    text: OpenLayers.i18n("addWMSButtonText"),
                    id: "addMenu",
                    handler: function () {
                        GeoNetwork.WindowManager.showWindow("addwms");
                    }
                },
                {
                    text:OpenLayers.i18n("removeButtonText"),
                    id: "removeMenu",
                    handler: removeLayerHandlerContextMenu
                },
                {
                    text: OpenLayers.i18n("metadataButtonText"),
                    id: "metadataMenu",
                    handler: metadataLayerHandlerContextMenu
                },
                {
                    text: "Styles",
                    id: "stylesMenu",
                    handler: stylesLayerHandlerContextMenu
                },
                {
                    text: OpenLayers.i18n('WMSTimeWindowTitle'),
                    id: "wmsTimeMenu",
                    disabled: true,
                    handler: wmsTimeHandlerContextMenu
                }

            ]}),
            tbar:  toctoolbar,
            rootVisible: false,
            lines: false,
            border: false,
            region: 'center'			
        });

    };

    /**
     * Creates the legend panel control
     *
     */
    var createLegendPanel = function() {
        legendPanel = new GeoExt.LegendPanel({
            defaults: {
                labelCls: 'mylabel',
                style: 'padding:5px'
            },
            title: 'Legend',
            height:200,
            autoScroll: true,
            split: true,
            collapsible: true,
            collapsed: true,
            border: false,
            region: 'south'
        });
    };

    /**
     * Creates the map viewport
     *
     */
    var createViewport = function() {
        createToolbars();         
        createTree();
        createLegendPanel();
        createPrintPanel();
        
        var mapOverlay = createMapOverlay();
       
        // Accordion panel with layer tree and advanced print config
        var accordion = new Ext.Panel({
            region: 'center',
            border: false,
            layout: 'accordion',
            deferredRender:false, 
            items: [
                tree, printPanel
            ]
        });
       
        viewport = new Ext.Panel({
            layout: 'border',
            border: false,
            //renderTo:'map_container',
            items: [{
                    id: 'layerManager',
                    region: 'east',
                    xtype: 'panel',
                    collapsible: true,
                    collapseMode: "mini",
                    split:true,
                    border: false,
                    width:170,
                    minSize: 170,
                    maxSize: 300,
                    layout: 'border',
                    items: [accordion, legendPanel]
                },{
                    region: 'center',
                    layout: 'fit',
                    frame: false,
                    border: false,
                    margins: '0 0 0 0',
                    items: [{
                        id: 'mappanel',
                        xtype: 'gx_mappanel',
                        map: map,
                        tbar: toolbar,
                        border: false,
                        extent: GeoNetwork.map.EXTENT,
                        items: [mapOverlay]
                    }]
                }
            ]
        });
        
        
        Ext.getCmp("toctree").on({
            "click": function(node) {
                if (node.ui.radio) {
                    node.ui.radio.checked = true;
                    node.ui.fireEvent("radiochange", node);

                }

                refreshTocToolbar(node);

            },
            scope: this
        });
        
        Ext.getCmp("toctree").on("nodedragover", function(evt) {
            // Only allow to move layers in the gx_overlaylayercontainer (user layers)
            if ((evt.dropNode.parentNode.attributes.nodeType == "gx_baselayercontainer") ||  // restrict move baselayers
                (evt.target.attributes.nodeType == "gx_baselayercontainer") ||               // restrict move layers to baselayer container
                (evt.target.parentNode.attributes.nodeType == "gx_baselayercontainer") ||
                (evt.target.parentNode == evt.tree.root))  {                                 // restrict move layers to outside of layercontainers
                 evt.cancel=true;
            }
        }); 
        
        refreshTocToolbar(activeNode);
    };


    /**
     * Method: processLayersSuccess
     * Called when the GetCapabilities response is in.
     *
     * Parameters:
     * response - {Object} The response object
     */
var processLayersSuccess = function(response) {
        layerLoadingMask.hide();

        var parser = new OpenLayers.Format.WMSCapabilities();
        var caps = parser.read(response.responseXML || response.responseText);
        if (caps.capability) {
            // GetCapabilities disclaimer
            var accessContraints = caps.service.accessContraints;

            if ((accessContraints) && (accessContraints.toLowerCase() != "none") &&
              (accessContraints != "-")) {
                var disclaimerWindow = new GeoNetwork.DisclaimerWindow({
                    disclaimer: accessContraints
                });
                disclaimerWindow.show();
                disclaimerWindow = null;
            }

            if (map) {
                for(var i = 0, len=layers.length; i < len; i++) {
                    var name = layers[i][0];
                    var url = layers[i][1];
                    var layer = layers[i][2];
                    var metadata_id = layers[i][3];

                    var ol_layer = new OpenLayers.Layer.WMS(name, url,
                        {layers: layer, format: 'image/png', transparent: 'TRUE', version: caps.version, language: GeoNetwork.OGCUtil.getLanguage()},
                        {queryable: true, singleTile: true, ratio: 1, buffer: 0, transitionEffect: 'resize', metadata_id: metadata_id} );

                    if (!GeoNetwork.OGCUtil.layerExistsInMap(ol_layer, map)) {
                        // TODO: these events are never removed?
                        ol_layer.events.on({"loadstart": function() {
                                this.isLoading = true;
                            }});

                        ol_layer.events.on({"loadend": function() {
                                this.isLoading = false;
                            }});

                        var layerCap = getLayer(caps, caps.capability.layers, ol_layer);

                        if (layerCap) {
                            ol_layer.queryable = layerCap.queryable;
                            ol_layer.name = layerCap.title || ol_layer.name;
                            ol_layer.llbbox = layerCap.llbbox;
                            ol_layer.styles = layerCap.styles;
                            ol_layer.dimensions = layerCap.dimensions;
                        }

                        map.addLayer(ol_layer);
                    }
                }

            }
        }
    };

    /**
     * Method: processLayersFailure
     * Called when the GetCapabilities response is in and failed.
     *
     * Parameters:
     * response - {Object} The response object
     */
    var processLayersFailure = function(response) {
        layerLoadingMask.hide();

        Ext.MessageBox.alert(OpenLayers.i18n("loadLayer.error.title"),
            OpenLayers.i18n("loadLayer.error.message"));
    };
    
    
    /**
     * Function: getLayer
     * Recursive function to process a layer and their childLayers in the
     * capabilities document, searching for the requested layer
     *
     * Parameters:
     * caps - {Object} capabilities from the WMSCapabilities parser
     * caplayers - {Object} list of layer objects from the WMSCapabilities parser
     * layer - {Object} layer to return from capabilities document
     *
     * Returns:
     * {Object} The layer if found, otherwise null
     */
    var getLayer = function(caps, caplayers, layer) {
        var findedLayer = null;

        for (var i = 0, len = caplayers.length; i < len; ++i) {
            var lr = caplayers[i];

            try {
                var layerName = lr.name.split(",");

                if (layerName.indexOf(layer.params.LAYERS) != -1) {
                    findedLayer = lr;
                    break;
                }
            } catch(e) {
            }

            if (typeof(lr.childLayers) != "undefined") {
                findedLayer = getLayer(caps, lr.childLayers, layer);
                if (findedLayer !== null) {
                    break;
                }
            }

        }

        return findedLayer;
    };

    // public space:
    return {
        init: function(layers, mapOptions, fixedScales) {
            //Ext.QuickTips.init();

            createMap(mapOptions, fixedScales);
    
            for (var i=0; i<layers.length; i++) {                
                createLayer(layers[i].clone());
            }
            
            // Fix for the toctree to get the correct mappanel (i
            GeoExt.MapPanel.guess = function() {
                return Ext.getCmp('mappanel');
            };
            
            createViewport();

            addMapControls();

            // Register windows in WindowManager
            GeoNetwork.WindowManager.registerWindow("addwms", GeoNetwork.AddWmsLayerWindow, {map: map, id:"addwms"});
            GeoNetwork.WindowManager.registerWindow("wmsinfo", GeoNetwork.WmsLayerMetadataWindow, {map: map, id:"wmsinfo"});
            GeoNetwork.WindowManager.registerWindow("loadwmc", GeoNetwork.LoadWmcWindow, {map: map, id:"loadwmc"});
            GeoNetwork.WindowManager.registerWindow("featureinfo", GeoNetwork.FeatureInfoWindow, {map: map, id:"featureinfo", control: featureinfo});
            GeoNetwork.WindowManager.registerWindow("layerstyles", GeoNetwork.LayerStylesWindow, {map: map, id:"layerstyles"});
            GeoNetwork.WindowManager.registerWindow("wmstime", GeoNetwork.WMSTimeWindow, {map: map, id:"wmstime"});
            
            map.addLayer(featureinfolayer);
        },

        /**
         * Add a list of WMS layers to the map
         *
         * @param layers    List of layers to load
         *                  [[name, url, layer, metadata_id], [name, url, layer, metadata_id], ....]
         */
        addWMSLayer:  function(layerList) {
        	if (layerList.length === 0) {
                return;
            }
        	var onlineResource = layerList[0][1];
            
        	/* if null layer name, open the WMS Browser panel */
        	if (layerList[0][2]=='') {
        	    GeoNetwork.WindowManager.showWindow("addwms");
        	    var panel = Ext.getCmp(GeoNetwork.WindowManager.getWindow("addwms").browserPanel.id);
        	    panel.setURL(onlineResource);
        	    return;
        	}
        	
            layerLoadingMask = new Ext.LoadMask(map.div, {
                msg: OpenLayers.Lang.translate('loadLayer.loadingMessage')});
            layerLoadingMask.show();
            

            layers = layerList;

            var params = {'service': 'WMS', 'request': 'GetCapabilities',
                'version': '1.1.1'};
            var paramString = OpenLayers.Util.getParameterString(params);
            var separator = (onlineResource.indexOf('?') > -1) ? '&' : '?';
            onlineResource += separator + paramString;

            var req = OpenLayers.Request.GET({
                url: onlineResource, //OpenLayers.Util.removeTail(OpenLayers.ProxyHostURL),
                //method: 'GET',
                //params: {url: onlineResource},
                success: processLayersSuccess,
                failure: processLayersFailure,
                timeout: 10000
            });
        },

        getViewport: function() {
            return viewport;
        },
        
        refreshViewport: function() {
            // IE shows the west panel hidden when the map is shown after a window resize
            Ext.get("west_panel").setWidth(westPanelWidth);

            viewport.doLayout();
        },
        
        getMap: function() {
            return map;
        }
    };
}; // end of app

// Extension to add title in ExtJs collapsed panels
//Ext.onReady(function(){
//    Ext.layout.BorderLayout.Region.prototype.getCollapsedEl = Ext.layout.BorderLayout.Region.prototype.getCollapsedEl.createSequence(function() {
//        if ((this.position == 'north' || this.position == 'south') && !this.collapsedEl.titleEl) {
//            this.collapsedEl.titleEl = this.collapsedEl.createChild({cls: 'x-collapsed-title', cn: this.panel.title});
//        }
//    });
//});
