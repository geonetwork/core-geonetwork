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
 * @requires GeoNetwork/lang/nl.js
 */

Ext.namespace('GeoNetwork', 'GeoNetwork.wms');

/**
 * Class: GeoNetwork.wms.PreviewPanel
 * A panel to show a preview image of a WMS layer before it gets added to
 *     the map. It uses a BoxComponent for the image itself.
 *
 * Inherits from:
 *  - {Ext.Panel}
 */

/**
 * Constructor: GeoNetwork.wms.PreviewPanel
 * Create an instance of GeoNetwork.wms.PreviewPanel
 *
 * Parameters:
 * config - {Object} A config object used to set the WMS preview
 *     panel's properties.
 */
GeoNetwork.wms.PreviewPanel = function(config){
    Ext.apply(this, config);
    GeoNetwork.wms.PreviewPanel.superclass.constructor.call(this);
};

Ext.extend(GeoNetwork.wms.PreviewPanel, Ext.Panel, {

    /**
     * APIProperty: title
     * {String} The title to use in the header of the preview panel
     */
    title: OpenLayers.i18n("WMSBrowserPreviewTitle"),

    /**
     * APIProperty: baseCls
     * {String} The base CSS class to apply to this panel's element 
     *     (defaults to 'x-plain'). 
     */
    baseCls: 'x-plain',

    /**
     * APIProperty: cls
     * {String} An optional extra CSS class that will be added to this 
     *     component's Element (defaults to 'x-panel-title-variant1').
     */
    cls: 'x-panel-title-variant1',

    /**
     * APIProperty: imgCls
     * {String} css class to put on the <img> (defaults to 'preview-image')
     */
    imgCls: 'preview-image',

    /**
     * APIProperty: width
     * {Integer} the width of this panel (defaults to 250)
     */
    width: 250,

    /**
     * APIProperty: height
     * {Integer} the height of this panel (defaults to 250)
     */
    height: 250,

    /**
     * APIProperty: currentLayer
     * {<OpenLayers.Layer.WMS>} the current layer being previewed
     */
    currentLayer: null,

    /**
     * Method: initComponent
     * Initializes this component
     */
    initComponent: function() {
        GeoNetwork.wms.PreviewPanel.superclass.initComponent.call(this);
        this.image = new Ext.BoxComponent({autoEl: {tag: 'img',
            'class': this.imgCls, src: Ext.BLANK_IMAGE_URL, 
            width: this.width, height: this.height}});
        this.add(this.image);
    },

    /**
     * Method: hideMask
     * Hides the Ext.LoadMask
     */
    hideMask: function() {
        if (this.mask) {
            this.mask.hide();
        }
    },

    /**
     * Method: showMask
     * Shows the Ext.LoadMask
     */
    showMask: function() {
        if (!this.mask) {
            this.mask = new Ext.LoadMask(this.getEl(), {
                msg:  OpenLayers.i18n("WMSBrowserPreviewWaitMsg")});
            Ext.EventManager.addListener(this.image.getEl(), 'load', 
                this.hideMask, this);
            Ext.EventManager.addListener(this.image.getEl(), 'error', 
                this.hideMask, this);
        }
        this.mask.show();
    },

    /**
     * Method: calculateBBOX
     * Calculate the BBOX to use for the preview, based on scales etc.
     *
     * Parameters:
     * layer - {<OpenLayers.Layer.WMS>}
     *
     * Returns:
     * {String} the BBOX parameter as a string
     */
     calculateBBOX: function(layer) {
        var bbox;
        var reverseAxisOrder = (parseFloat(layer.params.VERSION)>=1.3);

        if (layer.llbbox) {
            if (this.map.getProjection() !== 'EPSG:4326') {
                // reproject the latlon boundingbox to the map projection
                var llbounds = OpenLayers.Bounds.fromArray(layer.llbbox);
                llbounds = llbounds.transform(new OpenLayers.Projection('EPSG:4326'),
                    this.map.getProjectionObject() );
                bbox = llbounds.toArray(reverseAxisOrder);
            } else {
                var llbounds = OpenLayers.Bounds.fromArray(layer.llbbox);
                bbox = llbounds.toArray(reverseAxisOrder);
            }
        } else {
            bbox = this.map.maxExtent.toArray(reverseAxisOrder);
        }
        var center = OpenLayers.Bounds.fromArray(bbox).getCenterLonLat();
        // change the bbox so that the WMS returns an image that is inside
        // the scale-range of the layer
        if (layer.minScale > 0) {
            var midScale;
            if (layer.maxScale > 0) {
                midScale = (parseFloat(layer.maxScale) + parseFloat(layer.minScale)) / 2;
            }
            else {
                // take less than 100%, because of small differences in
                // calculating WMS scales
                midScale = 0.9 * parseFloat(layer.minScale);
            }
            // determine the new bbox based on the center
            // and scale range of the WMS Layer
            var res = OpenLayers.Util.getResolutionFromScale(midScale,
                this.map.units);
            var dX = Math.round(res * this.width);
            var dY = Math.round(res * this.height);
            var cX = center.lon;
            var cY = center.lat;
            if (dX !== 0 && dY !== 0) {
                if (reverseAxisOrder) {
                    bbox = [cY - 0.5*dY, cX - 0.5*dX, cY + 0.5*dY, cX + 0.5*dX];
                } else {
                    bbox = [cX - 0.5*dX, cY - 0.5*dY, cX + 0.5*dX, cY + 0.5*dY];
                }
            }
        }
        return bbox.join(",");
    },

    /**
     * APIMethod: showPreview
     * Shows a preview image of the WMS layer
     *
     * Parameters:
     * layer - {<OpenLayers.Layer.WMS>}
     */
    showPreview : function (layer){
        if (!layer) {
            return;
        }
        this.showMask();
        
        // if the layer has not been added to the map yet, we need to set its
        // map property otherwise getFullRequestString will not work.
        var previousMap = layer.map;
        if (previousMap === null) {
            layer.map = this.map;
        }

        var url = layer.getFullRequestString({
            BBOX: this.calculateBBOX(layer),
            WIDTH: this.width,
            HEIGHT: this.height
        });

        if (previousMap === null) {
            layer.map = previousMap;
        }

        this.currentLayer = layer;
        this.image.getEl().dom.src = url;
    },

    showPreviewLegend : function (urlLegend) {
        this.remove(this.image);
        this.image = null;

        this.image = new Ext.BoxComponent({autoEl: {tag: 'img',
            'class': this.imgCls, src: urlLegend}});
        this.add(this.image);
        
        this.doLayout();
    }
});

Ext.reg('gn_wmspreview', GeoNetwork.wms.PreviewPanel);
