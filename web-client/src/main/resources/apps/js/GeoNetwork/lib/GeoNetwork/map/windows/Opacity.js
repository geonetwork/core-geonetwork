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

/**
 * Class: GeoNetwork.OpacityWindow
 *      Window to show a slider to select opacity for layers
 *
 * Inherits from:
 *  - {GeoNetwork.BaseWindow}
 */

/**
 * Constructor: GeoNetwork.OpacityWindow
 * Create an instance of GeoNetwork.OpacityWindow
 *
 * Parameters:
 * config - {Object} A config object used to set the opacity
 *     window's properties.
 */
GeoNetwork.OpacityWindow = function(config) {
    Ext.apply(this, config);
    GeoNetwork.OpacityWindow.superclass.constructor.call(this);
};

Ext.extend(GeoNetwork.OpacityWindow, GeoNetwork.BaseWindow, {

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
        GeoNetwork.OpacityWindow.superclass.initComponent.call(this);

        this.title = this.title || OpenLayers.i18n("opacityWindowTitle");
        this.width = 200;
        this.height = 100;

        this.opacitySlider = new GeoNetwork.OpacitySlider({layer: this.layer, selModel: this.selMode});

        this.add(this.opacitySlider);
        
        this.doLayout();
    },

    setLayer: function(layer) {
        this.opacitySlider.setLayer(layer);
    }
});
