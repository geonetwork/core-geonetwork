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
 * Function: GeoNetwork.OpacitySlider
 * Constructor
 *
 * Parameters:
 * config - {Object}
 */
GeoNetwork.OpacitySlider = function(config){
    Ext.apply(this, config);
    GeoNetwork.OpacitySlider.superclass.constructor.call(this);
};

/**
 * Class: GeoNetwork.OpacitySlider
 * Extends Ext.Slider.
 * A slider to change the opacity of a Layer.
 * Xtype is 'gn_opacityslider'.
 */
Ext.extend(GeoNetwork.OpacitySlider, Ext.Slider, {

    /**
     * APIProperty: layer
     * {<OpenLayers.Layer>}
     */
    layer: null,

    /**
     * Method: initComponent
     * Initialize this component.
     */
    initComponent: function() {
        GeoNetwork.OpacitySlider.superclass.initComponent.call(this);
        this.minValue = 0;
        this.maxValue = 100;
        this.value = this.getInitialValue();
        this.on('change', this.setOpacity, this);
        if (this.selModel) {
            this.selModel.on('selectionchange', this.handleSelectionChange, this);
        }
    },

    /**
     * Method: onDestroy
     * Called when this component is destroyed, unset events
     */
    onDestroy: function() {
        this.un('change', this.setOpacity, this);
        this.selModel.un('selectionchange', this.handleSelectionChange, this);
        GeoNetwork.OpacitySlider.superclass.onDestroy.call(this);
    },

    /**
     * Method: handleSelectionChange
     * If the selection in the TOC changes, attach another layer to the slider.
     */
    handleSelectionChange: function(selmodel, node) {
        if ((node) && (node.attributes.layer)) {
            this.setLayer(node.attributes.layer);
        }
    },

    /**
     * Function: getInitialValue
     * Get the initial opacity value to start the slider with
     *
     * Returns:
     * {Float}
     */
    getInitialValue: function() {
        if (this.layer === null) {
            return 100;
        }
        var opacity = (typeof this.layer.opacity == "number") ? this.layer.opacity * 100 : 100;
	return opacity;
    },

    /**
     * Method: setOpacity
     * Sets the opacity on the layer object
     *
     * Parameters:
     * el - {Object} The slider itself
     * value - {Float} The slider value
     */
    setOpacity: function(el, value) {
        var opacity = value / 100;
        if (this.layer) {
            this.layer.setOpacity(opacity);
        }
    },

    /**
     * APIMethod: setLayer
     * Attach a layer to the slider
     *
     * Parameters:
     * {<OpenLayers.Layer>}
     */
    setLayer: function(layer) {
        this.layer = layer;
        if (this.rendered) {
            this.setValue(this.getInitialValue());
	    this.syncThumb();
        }
    },

    /**
     * Method: afterRender
     * After render set the initial value for the opacity on the slider.
     */
    afterRender: function() {
        if (this.layer) {
            this.setValue(this.getInitialValue());
        }
    }

});

Ext.reg('gn_opacityslider', GeoNetwork.OpacitySlider);
