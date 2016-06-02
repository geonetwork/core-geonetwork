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

/**
 * @requires GeoNetwork/windows/BaseWindow.js
 */ 

Ext.namespace('GeoNetwork');

/**
 * Class: GeoNetwork.AddWmsWindow
 *      Window to load WMS layers in map application
 *
 * Inherits from:
 *  - {GeoNetwork.BaseWindow}
 */

/**
 * Constructor: GeoNetwork.AddWmsWindow
 * Create an instance of GeoNetwork.AddWmsWindow
 *
 * Parameters:
 * config - {Object} A config object used to set the addwmslayer
 *     window's properties.
 */
GeoNetwork.WmsLayerMetadataWindow = function(config) {
    Ext.apply(this, config);
    GeoNetwork.WmsLayerMetadataWindow.superclass.constructor.call(this);
};

Ext.extend(GeoNetwork.WmsLayerMetadataWindow, GeoNetwork.BaseWindow, {

    /**
     * APIProperty: layer
     * {<OpenLayers.Layers.WMS>}
     */
    layer: null,

    /**
     * Method: init
     *     Initialize this component.
     */
    initComponent: function() {
        GeoNetwork.WmsLayerMetadataWindow.superclass.initComponent.call(this);

        this.title = this.title || OpenLayers.i18n("layerInfoPanel.windowTitle");
        
        this.width = 575;
        this.height = 300;

        this.infoLayerPanel = new GeoNetwork.wms.LayerInfoPanel({
            map: this.map /*,
            layer: this.layer,
            onlineresource: this.layer.url */
        });

        this.add(this.infoLayerPanel);

        this.doLayout();
    },

    showLayerInfo: function(layer) {
        this.infoLayerPanel.layer = layer;
        this.infoLayerPanel.metadataId = layer.options.metadata_id;
        this.infoLayerPanel.onlineresource = layer.url;
        this.infoLayerPanel.showLayerInfo(); 
    }
});
