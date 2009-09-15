/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/**
 * @requires GeoExt/widgets/tree/LayerContainer.js
 */
Ext.namespace("GeoExt.tree");

/** api: (define)
 *  module = GeoExt.tree
 *  class = OverlayLayerContainer
 */

/** api: (extends)
 * GeoExt/widgets/tree/LayerContainer.js
 */

/** api: constructor
 * .. class:: OverlayLayerContainer
 * 
 *     A layer container that will collect all overlay layers of an OpenLayers
 *     map. Only layers that have displayInLayerSwitcher set to true will be
 *     included.
 * 
 *     To use this node type in ``TreePanel`` config, set nodeType to
 *     "gx_overlaylayerontainer".
 */
GeoExt.tree.OverlayLayerContainer = Ext.extend(GeoExt.tree.LayerContainer, {

    /** private: method[constructor]
     *  Private constructor override.
     */
    constructor: function(config) {
        config.text = config.text || "Overlays";
        GeoExt.tree.OverlayLayerContainer.superclass.constructor.apply(this,
            arguments);
    },

    /** private: method[addLayerNode]
     *  :param layerRecord: ``Ext.data.Record`` The layer record containing the
     *      layer to be added.
     *  :param index: ``Number`` Optional index for the new layer.  Default is 0.
     *  
     *  Adds a child node representing a overlay layer of the map.
     */
    addLayerNode: function(layerRecord, index) {
        var layer = layerRecord.get("layer");
        if (layer.isBaseLayer == false) {
            GeoExt.tree.OverlayLayerContainer.superclass.addLayerNode.apply(
                this, arguments
            );
        }
    },
    
    /** private: method[removeLayerNode]
     *  :param layerRecord: ``Ext.data.Record`` the layer record to remove the
     *      node for
     *      
     * Removes a child node representing an overlay layer of the map.
     */
    removeLayerNode: function(layerRecord) {
        var layer = layerRecord.get("layer");
        if (layer.isBaseLayer == false) {
            GeoExt.tree.OverlayLayerContainer.superclass.removeLayerNode.apply(
                this, arguments
            );
    	}
    }
});

/**
 * NodeType: gx_overlaylayercontainer
 */
Ext.tree.TreePanel.nodeTypes.gx_overlaylayercontainer = GeoExt.tree.OverlayLayerContainer;
