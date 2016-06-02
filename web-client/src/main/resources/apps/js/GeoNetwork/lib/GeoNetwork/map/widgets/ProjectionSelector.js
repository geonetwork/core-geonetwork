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
 * Function: GeoNetwork.ProjectionSelector
 * Constructor
 *
 * Parameters:
 * config - {Object}
 */
GeoNetwork.ProjectionSelector = function(config){
    Ext.apply(this, config);
    GeoNetwork.ProjectionSelector.superclass.constructor.call(this);
};

/**
 * Class: GeoNetwork.ProjectionSelector
 * Extends Ext.form.ComboBox.
 * A combo box to select a SRS (Spatial Reference System) on the map
 * Xtype is 'gn_projectionselector'.
 */
Ext.extend(GeoNetwork.ProjectionSelector, Ext.form.ComboBox, {

    /**
     * APIProperty: projections
     * {Array} array of projections to use
    */
    projections: null,

    /**
     * Method: initComponent
     * Constructor of the projection selector
    */
    initComponent : function() {
        GeoNetwork.ProjectionSelector.superclass.initComponent.call(this);
        this.on('select', this.reproject, this);
        this.valueField = 'value';
        this.autoWidth = true;
        this.autoHeight = true;
        this.displayField = 'text';
        this.triggerAction = 'all';
        this.mode = 'local';

        this.store = new Ext.data.Store({
            reader: new Ext.data.ArrayReader({}, [
                   {name: 'value'},
                   {name: 'text'}
            ]),
            data: this.projections
        });
        this.value = this.map.getProjection();
    
    },

    /**
     * Method: reproject
     * Reproject the map
     *
     * Parameters:
     * combo - {<Ext.form.ComboBox>} the combo box
     * record - {<Ext.data.Record>} the active record
    */
    reproject: function(combo, record) {
        GeoNetwork.OGCUtil.reprojectMap(this.map, 
            new OpenLayers.Projection(record.get('value')), false);
    }

});
Ext.reg('gn_projectionselector', GeoNetwork.ProjectionSelector);
