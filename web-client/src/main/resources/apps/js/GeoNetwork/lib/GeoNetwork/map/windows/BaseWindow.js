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
 * Class: GeoNetwork.BaseWindow
 *      Base window to set custom properties of application windows.
 *      All application windows must inherit from this class
 *
 * Inherits from:
 *  - {Ext.Window}
 */

/**
 * Constructor: GeoNetwork.BaseWindow
 * Create an instance of GeoNetwork.BaseWindow
 *
 * Parameters:
 * config - {Object} A config object used to set 
 *     window's properties.
 */
GeoNetwork.BaseWindow = function(config) {
    Ext.apply(this, config);
    GeoNetwork.BaseWindow.superclass.constructor.call(this);
};

Ext.extend(GeoNetwork.BaseWindow, Ext.Window, {

    /**
     * APIProperty: map
     * {<OpenLayers.Map>}
     */
    map: null,

    /**
     * Method: init
     *     Initialize this component.
     */
    initComponent: function() {
        GeoNetwork.BaseWindow.superclass.initComponent.call(this);

        this.constrainHeader = true;
        this.collapsible = true;
        this.layout = 'fit';
        this.plain = true;
        this.stateful = false;
    }
});
