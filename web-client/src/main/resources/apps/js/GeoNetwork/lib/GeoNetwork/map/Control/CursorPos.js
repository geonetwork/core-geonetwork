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

if (!window.GeoNetwork) {
    window.GeoNetwork = {};
}
if (!GeoNetwork.Control) {
    GeoNetwork.Control = {};
}

/**
 * Class: GeoNetwork.Control.CursorPos
 * Displays the cursor position X and Y coordinate in 2 ExtJS elements
 *
 * Inherits from:
 *  - <OpenLayers.Control.MousePosition>
 */
GeoNetwork.Control.CursorPos = OpenLayers.Class(OpenLayers.Control.MousePosition, {
    
    /** 
     * Property: xelement
     * {Ext.Element} Element in which the X-coordinate should display
     */
    xelement: null,

    /**
     * Property: yelement
     * {Ext.Element} Element in which the Y-coordinate should display
     */
    yelement: null,
    
	 /**
     * Property: numdigits
     * {Ext.Element} Number of decimal digits to format the coordinates
     */
    numdigits: 2,
	
    /**
     * Constructor: GeoNetwork.Control.CursorPos
     * Create a new cursor position control
     * 
     * Parameters:
     * options - {Object} Options for control.
     */
    initialize: function(options) {
        OpenLayers.Control.MousePosition.prototype.initialize.apply(this, arguments);
    },

    /**
     * Method: draw
     * Draws the control
     */    
    draw: function() {
        OpenLayers.Control.prototype.draw.apply(this, arguments);
        this.redraw();
    },
   
    /**
     * Method: redraw  
     * Redraws the control
     *
     * Parameters:
     * evt - {Event} The event object (mouse move)
     */
    redraw: function(evt) {

        var lonLat;

        if (evt == null) {
            lonLat = new OpenLayers.LonLat(0, 0);
        } else {
            if (this.lastXy == null ||
                Math.abs(evt.xy.x - this.lastXy.x) > this.granularity ||
                Math.abs(evt.xy.y - this.lastXy.y) > this.granularity)
            {
                this.lastXy = evt.xy;
                return;
            }

            lonLat = this.map.getLonLatFromPixel(evt.xy);
            if (!lonLat) { 
                // map has not yet been properly initialized
                return;
            }    
            this.lastXy = evt.xy;
            
        }
        if (this.xelement) {
            this.xelement.dom.value = lonLat.lon.toFixed(this.numdigits);
        }
        if (this.yelement) {
            this.yelement.dom.value = lonLat.lat.toFixed(this.numdigits);
        }
    },

    CLASS_NAME: "GeoNetwork.Control.CursorPos"
});
