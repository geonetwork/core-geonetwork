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
 * Class: GeoNetwork.LayerStylesWindow
 *      Window to let the user select a named layer style
 *
 * Inherits from:
 *  - {GeoNetwork.BaseWindow}
 */

/**
 * Constructor: GeoNetwork.LayerStylesWindow
 * Create an instance of GeoNetwork.LayerStylesWindow
 *
 * Parameters:
 * config - {Object} A config object used to set the addwmslayer
 *     window's properties.
 */
GeoNetwork.LayerStylesWindow = function(config) {
    Ext.apply(this, config);
    GeoNetwork.LayerStylesWindow.superclass.constructor.call(this);
};

Ext.extend(GeoNetwork.LayerStylesWindow, GeoNetwork.BaseWindow, {
    
    /**
     * Method: init
     *     Initialize this component.
     */
    initComponent: function() {
        GeoNetwork.LayerStylesWindow.superclass.initComponent.call(this);

        this.title = this.title || OpenLayers.i18n("layerStylesWindowTitle");

        this.width = 575;
        this.height = 300;

        this.layerStylesPanel = new GeoNetwork.wms.LayerStylesPanel({
            map: this.map
        });

        this.add(this.layerStylesPanel);

	this.addButton(OpenLayers.i18n("selectStyleButton"),
            this._selectStyle, this);

        this.doLayout();
    },

    showLayerStyles: function(layer) {
        this.layer = layer;
        this.layerStylesPanel.showLayerStyles(layer);
    },

    _selectStyle: function() {
        this.layer.mergeNewParams({styles: this.layerStylesPanel.selectedStyle});
        this.layer.legendURL = this.layerStylesPanel.selectedStyleLegendUrl;

    }

});
