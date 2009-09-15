/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/**
 * @include GeoExt/widgets/tree/LayerNode.js
 */
Ext.namespace("GeoExt.tree");

/** api: (define)
 *  module = GeoExt.tree
 *  class = LayerContainer
 *  base_link = `Ext.tree.TreeNode <http://extjs.com/deploy/dev/docs/?class=Ext.tree.TreeNode>`_
 */

/** api: constructor
 *  .. class:: LayerContainer
 * 
 *      A subclass of ``Ext.tree.TreeNode`` that will collect all layers of an
 *      OpenLayers map. Only layers that have displayInLayerSwitcher set to true
 *      will be included. The childrens' iconCls defaults to
 *      "gx-tree-layer-icon".
 * 
 *      To use this node type in ``TreePanel`` config, set nodeType to
 *      "gx_layercontainer".
 */
GeoExt.tree.LayerContainer = Ext.extend(Ext.tree.TreeNode, {
    
    /** api: config[layerStore]
     *  :class:`GeoExt.data.LayerStore`
     *  The layer store containing layers to be displayed in the container.
     */
    layerStore: null,
    
    /** api: config[defaults]
     *  ``Object``
     *  A configuration object passed to all nodes that this container creates.
     */
    defaults: null,

    /** private: method[constructor]
     *  Private constructor override.
     */
    constructor: function(config) {
        this.layerStore = config.layerStore;
        this.defaults = config.defaults;
        GeoExt.tree.LayerContainer.superclass.constructor.apply(this, arguments);
    },

    /** private: method[render]
     *  :param bulkRender: ``Boolean``
     */
    render: function(bulkRender) {
        if (!this.rendered) {
            if(!this.layerStore) {
                this.layerStore = GeoExt.MapPanel.guess().layers;
            }
            this.layerStore.each(function(record) {
                this.addLayerNode(record);
            }, this);
            this.layerStore.on({
                "add": this.onStoreAdd,
                "remove": this.onStoreRemove,
                scope: this
            });
        }
        GeoExt.tree.LayerContainer.superclass.render.call(this, bulkRender);
    },
    
    /** private: method[onStoreAdd]
     *  :param store: ``Ext.data.Store``
     *  :param records: ``Array(Ext.data.Record)``
     *  :param index: ``Number``
     *  
     *  Listener for the store's add event.
     */
    onStoreAdd: function(store, records, index) {
        if(!this._reordering) {
            var nodeIndex = this.recordIndexToNodeIndex(index+records.length-1);
            for(var i=0; i<records.length; ++i) {
                this.addLayerNode(records[i], nodeIndex);
            }
        }
    },
    
    /** private: method[onStoreRemove]
     *  :param store: ``Ext.data.Store``
     *  :param record: ``Ext.data.Record``
     *  :param index: ``Number``
     *  
     *  Listener for the store's remove event.
     */
    onStoreRemove: function(store, record, index) {
        if(!this._reordering) {
            this.removeLayerNode(record);
        }
    },

    /** private: method[recordIndexToNodeIndex]
     *  :param index: ``Number`` The record index in the layer store.
     *  :return: ``Number`` The appropriate child node index for the record.
     */
    recordIndexToNodeIndex: function(index) {
        var store = this.layerStore;
        var count = store.getCount();
        var nodeCount = this.childNodes.length;
        var nodeIndex = -1;
        for(var i=count-1; i>=0; --i) {
            if(store.getAt(i).get("layer").displayInLayerSwitcher) {
                ++nodeIndex;
                if(index === i || nodeIndex > nodeCount-1) {
                    break;
                }
            }
        };
        return nodeIndex;
    },
    
    /** private: method[nodeIndexToRecordIndex]
     *  :param index: ``Number`` The child node index.
     *  :return: ``Number`` The appropriate record index for the node.
     *  
     *  Convert a child node index to a record index.
     */
    nodeIndexToRecordIndex: function(index) {
        var store = this.layerStore;
        var count = store.getCount();
        var nodeIndex = -1;
        for(var i=count-1; i>=0; --i) {
            if(store.getAt(i).get("layer").displayInLayerSwitcher) {
                ++nodeIndex;
                if(index === nodeIndex) {
                    break;
                }
            }
        }
        return i;
    },
    
    /** private: method[addLayerNode]
     *  :param layerRecord: ``Ext.data.Record`` The layer record containing the
     *      layer to be added.
     *  :param index: ``Number`` Optional index for the new layer.  Default is 0.
     *  
     *  Adds a child node representing a layer of the map
     */
    addLayerNode: function(layerRecord, index) {
        index = index || 0;
        var layer = layerRecord.get("layer");
        if (layer.displayInLayerSwitcher === true) {
            var Node = (this.defaults && this.defaults.nodeType) ?
                Ext.tree.TreePanel.nodeTypes[this.defaults.nodeType] :
                GeoExt.tree.LayerNode;
            var node = new Node(Ext.apply({
                iconCls: 'gx-tree-layer-icon',
                layer: layer,
                layerStore: this.layerStore
            }, this.defaults));
            var sibling = this.item(index);
            if(sibling) {
                this.insertBefore(node, sibling);
            } else {
                this.appendChild(node);
            }
            node.on("move", this.onChildMove, this);
        }
    },
    
    /** private: method[removeLayerNode]
     *  :param layerRecord: ``Ext.data.Record`` The layer record containing the
     *      layer to be removed.
     * 
     *  Removes a child node representing a layer of the map
     */
    removeLayerNode: function(layerRecord) {
        var layer = layerRecord.get("layer");
        if (layer.displayInLayerSwitcher == true) {
            var node = this.findChildBy(function(node) {
                return node.layer == layer;
            });
            if(node) {
                node.un("move", this.onChildMove, this);
                node.remove();
            }
    	}
    },
    
    /** private: method[onChildMove]
     *  :param tree: ``Ext.data.Tree``
     *  :param node: ``Ext.tree.TreeNode``
     *  :param oldParent: ``Ext.tree.TreeNode``
     *  :param newParent: ``Ext.tree.TreeNode``
     *  :param index: ``Number``
     *  
     *  Listener for child node "move" events.  This updates the order of
     *  records in the store based on new node order if the node has not
     *  changed parents.
     */
    onChildMove: function(tree, node, oldParent, newParent, index) {
        if(oldParent === newParent) {
            var newRecordIndex = this.nodeIndexToRecordIndex(index);
            var oldRecordIndex = this.layerStore.findBy(function(record) {
                return record.get("layer") === node.layer;
            });
            // remove the record and re-insert it at the correct index
            var record = this.layerStore.getAt(oldRecordIndex);
            this._reordering = true;
            this.layerStore.remove(record);
            this.layerStore.insert(newRecordIndex, [record]);
            delete this._reordering;
        }
    },

    /** private: method[destroy]
     */
    destroy: function() {
        if(this.layerStore) {
            this.layerStore.un("add", this.onStoreAdd, this);
            this.layerStore.un("remove", this.onStoreRemove, this);
        }
        GeoExt.tree.LayerContainer.superclass.destroy.apply(this, arguments);
    }
});

/**
 * NodeType: gx_layercontainer
 */
Ext.tree.TreePanel.nodeTypes.gx_layercontainer = GeoExt.tree.LayerContainer;
