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
 * Class: GeoNetwork.WMSTimeWindow
 *      Windows to show the time selector for time-aware WMS layers
 *
 * Inherits from:
 *  - {GeoNetwork.BaseWindow}
 */

/**
 * Constructor: GeoNetwork.WMSTimeWindow
 * Create an instance of GeoNetwork.WMSTimeWindow
 *
 * Parameters:
 * config - {Object}
 */
GeoNetwork.WMSTimeWindow = function(config) {
    Ext.apply(this, config);
    GeoNetwork.WMSTimeWindow.superclass.constructor.call(this);
};

Ext.extend(GeoNetwork.WMSTimeWindow, GeoNetwork.BaseWindow, {

    /**
     * Method: init
     * Initialize this component.
     */
    initComponent: function() {
        GeoNetwork.WMSTimeWindow.superclass.initComponent.call(this);

        this.title = this.title || OpenLayers.i18n("WMSTimeWindowTitle");

        this.width = 450;
        this.height = 300;

        this.timeSelector = new GeoNetwork.TimeSelector({bodyStyle: 'padding: 10px 10px 0 10px;'});
        this.add(this.timeSelector);

        this.doLayout();
    },

    /**
     * APIMethod: setLayer
     * Attach a layer to the components of this window
     *
     * Parameters:
     * layer - {<OpenLayers.Layer.WMS>}
     */
    setLayer: function(layer) {
        this.timeSelector.setLayer(layer);
    }
    
});
