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
 * Function: GeoNetwork.ScaleField
 * Constructor
 *
 * Parameters:
 * config - {Object}
 */
GeoNetwork.ScaleField = function(config) {
    Ext.apply(this, config);
    GeoNetwork.ScaleField.superclass.constructor.call(this);
};

/**
 * Class: GeoNetwork.ScaleField
 * Extends Ext.form.Field.
 * A form field which displays the current map scale, and gives the user
 * the opportunity to change the map scale (ENTER key).
 * Xtype is 'gn_scalefield'.
 */
Ext.extend(GeoNetwork.ScaleField, Ext.form.Field, {

    /**
     * APIProperty: map
     * {<OpenLayers.Map>}
     */
    map: null,

    /**
     * Method: initComponent
     * Initialize this component.
     */
    initComponent: function() {
        GeoNetwork.ScaleField.superclass.initComponent.call(this);
        this.setValue(this.map.getScale().toFixed(0));
        this.map.events.register( 'moveend', this, this.updateScale);
        var handlers = {
            specialkey: function(f, e){
                if(e.getKey()==e.ENTER) {
                    this.map.zoomToScale(this.getValue(), true);
                }
            }
        };
        this.on(handlers);
    },
    
    /**
     * Method: updateScale
     * Update the value in the text field with the scale from the map
     */
    updateScale: function() {
        var scale = this.map.getScale();
        this.setValue(scale.toFixed(0));
    },

    /**
     * Method: onDestroy
     * Clean up events when destroying.
     */
    onDestroy: function() {
        this.map.events.unregister("moveend", this, this.updateScale);
        GeoNetwork.ScaleField.superclass.onDestroy.call(this); 
    }
    
});

Ext.reg('gn_scalefield', GeoNetwork.ScaleField);
