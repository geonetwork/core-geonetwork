/*
 * Copyright (C) 2010 GeoNetwork
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

// create application
GeoNetwork.miniapp = function() {
    // private vars:
    var toolbar, viewport;

    var miniMap;
    
    var synchMinimap;
    
    var extentBox;
  
    var regionControl;
    
    /**
     * Creates the OL Map 
     *
     */
    var createMap = function(mapOptions) {
        var options = mapOptions || {
            projection: "EPSG:4326",
            units: "m",
            maxExtent: new OpenLayers.Bounds(-180,-90,180,90),
            restrictedExtent: new OpenLayers.Bounds(-180,-90,180,90),
            controls: []
        };
        
        if (!options.controls) options.controls = [];
        miniMap = new OpenLayers.Map(options);
    };

    /**
     * Adds a layer to the map 
     *
     */
    var createWmsLayer = function(name, url, params, options) {
        miniMap.addLayer(new OpenLayers.Layer.WMS(name, url, params, options));
    };

    /**
     * Configure the map controls
     *
     */
    var addMapControls = function() {
        miniMap.addControl(new GeoNetwork.Control.ZoomWheel());
        miniMap.addControl(new OpenLayers.Control.LoadingPanel());
    };

    var setRegionControl = function(region) {
        regionControl = $(region);
    }
    
    /**
     * Creates the minimap map toolbar
     */
    var createToolbar = function() {
        toolbar = [];

        action = new GeoExt.Action({
            control: new OpenLayers.Control.ZoomToMaxExtent(),
            map: miniMap,
            iconCls: 'zoomfull',
            tooltip: {title: OpenLayers.i18n("zoomToMaxExtentTooltipTitle"), text: OpenLayers.i18n("zoomToMaxExtentTooltipText")}
        });
    
        toolbar.push(action);

        toolbar.push("-");

        action = new GeoExt.Action({
            control: new OpenLayers.Control.ZoomBox(),
            map: miniMap,
            toggleGroup: "move",
            allowDepress: false,
            iconCls: 'zoomin',
            tooltip: {title: OpenLayers.i18n("zoominTooltipTitle"), text: OpenLayers.i18n("zoominTooltipText")}
        });

        toolbar.push(action);

        action = new GeoExt.Action({
            control:  new OpenLayers.Control.ZoomBox({
                displayClass: 'ZoomOut',
                out: true
            }),
            map: miniMap,
            toggleGroup: "move",
            allowDepress: false,
            tooltip: {title: OpenLayers.i18n("zoomoutTooltipTitle"), text: OpenLayers.i18n("zoomoutTooltipText")},
            iconCls: 'zoomout'
        });

        toolbar.push(action);

        action = new GeoExt.Action({
            control: new OpenLayers.Control.DragPan({
                    isDefault: true
                }),
            toggleGroup: "move",
            allowDepress: false,
            pressed: true,
            map: miniMap,
            iconCls: 'pan',
            tooltip:  {title: OpenLayers.i18n("dragTooltipTitle"), text: OpenLayers.i18n("dragTooltipText")}
        });

        toolbar.push(action);

        toolbar.push("-");

        extentBox = new GeoNetwork.Control.ExtentBox({
                minxelement: Ext.get('westBL'), 
                maxxelement: Ext.get('eastBL'),
                minyelement: Ext.get('southBL'),
                maxyelement: Ext.get('northBL')
                });

        
        extentBox.events.register("finishBox", null, function(evt) {
             regionControl.selectedIndex=1;
             
             if (synchMinimap) {
                synchMinimap.synch(regionControl.selectedIndex);
             }
        });
        
        action = new GeoExt.Action({
            control: extentBox,
            toggleGroup: "move",
            allowDepress: false,
            map: miniMap,
            tooltip: {title: OpenLayers.i18n("selectExtentTooltipTitle"), text: OpenLayers.i18n("selectExtentTooltipText")},
            iconCls: 'selextent'
        });

        toolbar.push(action);
        
    }

    /**
     * Refreshes the map zooming to the extent box
     */
    var updateMap = function updateMap(zoom) {
        if (extentBox) {
                extentBox.updateMap();
                if(zoom) extentBox.zoomTo();                    
        }                       
    };


    /**
     * Creates the map viewport
     *
     */
    var createViewport = function(miniMapDiv) {	 
        createToolbar();
         
        var mapPanel = new GeoExt.MapPanel({
            id: "mini_mappanel_" + miniMapDiv,
            renderTo: miniMapDiv,
            height: 150,
            width: 210,
            border:false,
            map: miniMap,
            tbar: toolbar
        });
    };

    /**
     * Clears the extent box
     */
    var clearExtentBox = function clearExtentBox() {
        if (extentBox) {
            extentBox.clear();
            miniMap.zoomToMaxExtent();
        }
    }

    // public space:
    return {
        init: function(miniMapDiv, regionControl, layers, mapOptions) {
            if (!$(miniMapDiv)) return;
            Ext.QuickTips.init();

            setRegionControl(regionControl);
            
            createMap(mapOptions);

            for (var i=0; i<layers.length; i++) {                
                createWmsLayer(layers[i][0],layers[i][1],layers[i][2],layers[i][3]);
            }           
           
            createViewport(miniMapDiv);
            addMapControls();
            miniMap.zoomToMaxExtent();

            Ext.EventManager.on(Ext.get("westBL"), 'change', updateMap);
            Ext.EventManager.on(Ext.get("eastBL"), 'change', updateMap);
            Ext.EventManager.on(Ext.get("southBL"), 'change', updateMap);
            Ext.EventManager.on(Ext.get("northBL"), 'change', updateMap);
        },

        /**
         * Clears the extent selection box
         */
        clearExtentBox: function() {
            clearExtentBox();
        },

        /**
         * Updates the extent selection box
         */
        updateExtentBox: function() {
            updateMap(true);
        },

        /**
         * Returns the map
         */
        getMap: function() {
            return miniMap;
        },
        
        synch: function(regionIndex) {
            $(regionControl).selectedIndex = regionIndex;
            updateMap(false);
        },
        
        setSynchMinimap: function (minimap) {
            synchMinimap = minimap; 
        },
        
        addWmsLayer: function(name, url, params, options) {
            createWmsLayer(name, url, params, options);
        }
        
    };
}; // end of app

GeoNetwork.minimapSimpleSearch = new GeoNetwork.miniapp();
GeoNetwork.minimapAdvancedSearch = new GeoNetwork.miniapp();

//Ext.onReady(GeoNetwork.miniapp.init, GeoNetwork.miniapp);
