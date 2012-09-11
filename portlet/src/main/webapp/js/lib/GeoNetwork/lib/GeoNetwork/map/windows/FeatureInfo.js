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
 * Class: GeoNetwork.LoadWmcWindow
 *      Window to load WMS layers in map application
 *
 * Inherits from:
 *  - {GeoNetwork.BaseWindow}
 */

/**
 * Constructor: GeoNetwork.FeatureInfoWindow
 * Create an instance of GeoNetwork.FeatureInfoWindow
 *
 * Parameters:
 * config - {Object} A config object used to set the addwmslayer
 *     window's properties.
 */
GeoNetwork.FeatureInfoWindow = function(config) {
    Ext.apply(this, config);
    GeoNetwork.FeatureInfoWindow.superclass.constructor.call(this);
};

Ext.extend(GeoNetwork.FeatureInfoWindow, GeoNetwork.BaseWindow, {

		control: null,

    /**
     * Method: init
     *     Initialize this component.
     */
    initComponent: function() {
        GeoNetwork.FeatureInfoWindow.superclass.initComponent.call(this);

        this.title = this.title || OpenLayers.i18n("featureInfoWindow.windowTitle");

        this.width = 600;
        this.height = 250;

        this.cls = 'popup-variant1';

       	var fp = new GeoNetwork.FeatureInfoPanel();

        this.add(fp);

        this.doLayout();
    },

    setFeatures: function(featureList) {
        this.items.items[0].showFeatures(featureList);
    },

    setMap: function(map) {
        this.items.items[0].setMap(map);
    }

});
