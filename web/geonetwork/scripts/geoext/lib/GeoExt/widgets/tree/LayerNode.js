/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

Ext.namespace("GeoExt.tree");

/** private: constructor
 *  .. class:: LayerNodeUI
 *
 *      Place in a separate file if this should be documented.
 */
GeoExt.tree.LayerNodeUI = Ext.extend(Ext.tree.TreeNodeUI, {
    
    /** private: property[radio]
     *  ``Ext.Element``
     */
    radio: null,
    
    /** private: method[constructor]
     */
    constructor: function(config) {
        GeoExt.tree.LayerNodeUI.superclass.constructor.apply(this, arguments);
    },
    
    /** private: method[render]
     *  :param bulkRender: ``Boolean``
     */
    render: function(bulkRender) {
        var a = this.node.attributes;
        if (a.checked === undefined) {
            a.checked = this.node.layer.getVisibility();
        }
        GeoExt.tree.LayerNodeUI.superclass.render.call(this, bulkRender);
        if (a.radioGroup && this.radio === null) {
            this.radio = Ext.DomHelper.insertAfter(this.checkbox,
                ['<input type="radio" class="gx-tree-layer-radio" name="',
                a.radioGroup, '_radio"></input>'].join(""));
        }
        if(a.checkedGroup) {
            // replace the checkbox with a radio button
            var radio = Ext.DomHelper.insertAfter(this.checkbox,
                ['<input type="radio" name="', a.checkedGroup,
                '_checkbox" class="', this.checkbox.className, 
                this.checkbox.checked ? '" checked="checked"' : '',
                '"></input>'].join(""));
            Ext.get(this.checkbox).remove();
            this.checkbox = radio;
        }
    },
    
    /** private: method[onClick]
     *  :param e: ``Object``
     */
    onClick: function(e) {
        if (e.getTarget('.gx-tree-layer-radio', 1)) {
            this.fireEvent("radiochange", this.node);
        } else if(e.getTarget('.x-tree-node-cb', 1)) {
            GeoExt.tree.LayerNodeUI.superclass.onCheckChange.call(this);
        } else {
            GeoExt.tree.LayerNodeUI.superclass.onClick.call(this, e);
        }
    },
    
    /** private: method[toggleCheck]
     *  :param value: ``Boolean``
     */
    toggleCheck: function(value) {
        GeoExt.tree.LayerNodeUI.superclass.toggleCheck.call(this, value);
        var node = this.node;
        var layer = this.node.layer;
        node.visibilityChanging = true;
        if(this.checkbox && (layer.getVisibility() != this.isChecked())) {
            layer.setVisibility(this.isChecked());
        }
        node.visibilityChanging = false;
    },
    
    /** private: method[destroy]
     */
    destroy: function() {
        delete this.radio;
        GeoExt.tree.LayerNodeUI.superclass.destroy.call(this);
    }
});

/** api: (define)
 *  module = GeoExt.tree
 *  class = LayerNode
 *  base_link = `Ext.tree.TreeNode <http://extjs.com/deploy/dev/docs/?class=Ext.tree.TreeNode>`_
 */

/** api: constructor
 *  .. class:: LayerNode(config)
 * 
 *      A subclass of ``Ext.tree.TreeNode`` that is connected to an
 *      ``OpenLayers.Layer`` by setting the node's layer property. Checking or
 *      unchecking the checkbox of this node will directly affect the layer and
 *      vice versa. The default iconCls for this node's icon is
 *      "gx-tree-layer-icon", unless it has children.
 * 
 *      Setting the node's layer property to a layer name instead of an object
 *      will also work. As soon as a layer is found, it will be stored as layer
 *      property in the attributes hash.
 * 
 *      The node's text property defaults to the layer name.
 *      
 *      If the node has a checkedGroup attribute configured, it will be
 *      rendered with a radio button instead of the checkbox. The value of
 *      the checkedGroup attribute is a string, identifying the options group
 *      for the node.
 * 
 *      If the node has a radioGroup attribute configured, the node will be
 *      rendered with a radio button next to the checkbox. This works like the
 *      checkbox with the checked attribute, but radioGroup is a string that
 *      identifies the options group. Clicking the radio button will fire a
 *      radioChange event.
 * 
 *      To use this node type in a ``TreePanel`` config, set ``nodeType`` to
 *      "gx_layer".
 */
GeoExt.tree.LayerNode = Ext.extend(Ext.tree.TreeNode, {
    
    /** api: config[layer]
     *  ``OpenLayers.Layer or String``
     *  The layer that this layer node will
     *  be bound to, or the name of the layer (has to match the layer's
     *  name property). If a layer name is provided, ``layerStore`` also has
     *  to be provided.
     */

    /** api: property[layer]
     *  ``OpenLayers.Layer``
     *  The layer this node is bound to.
     */
    layer: null,
    
    /** api: config[layerStore]
     *  :class:`GeoExt.data.LayerStore` ``or "auto"``
     *  The layer store containing the layer that this node represents.  If set
     *  to "auto", the node will query the ComponentManager for a
     *  :class:`GeoExt.MapPanel`, take the first one it finds and take its layer
     *  store. This property is only required if ``layer`` is provided as a
     *  string.
     */
    layerStore: null,
    
    /** api: config[childNodeType]
     *  ``Ext.tree.Node or String``
     *  Node class or nodeType of childnodes for this node. A node type provided
     *  here needs to have an add method, with a scope argument. This method
     *  will be run by this node in the context of this node, to create child nodes.
     */
    childNodeType: null,
    
    /** private: property[visibilityChanging]
     * {Boolean} private property indicating layer visibility being changed
     *     by this node in order to prevent visibilitychanged events bouncing
     *     back and forth
     */
    visibilityChanging: false,
    
    /** private: method[constructor]
     *  Private constructor override.
     */
    constructor: function(config) {
        config.leaf = config.leaf || !config.children;
        
        config.iconCls = typeof config.iconCls == "undefined" &&
            !config.children ? "layer-icon" : config.iconCls;
        
        this.defaultUI = this.defaultUI || GeoExt.tree.LayerNodeUI;
        this.addEvents(
            /** api: event[radiochange]
             *  Notifies listener when a differnt radio button was selected.
             *  Will be called with the currently selected node as argument.
             */
            "radiochange"
        );
        
        Ext.apply(this, {
            layer: config.layer,
            layerStore: config.layerStore,
            childNodeType: config.childNodeType
        });
        GeoExt.tree.LayerNode.superclass.constructor.apply(this, arguments);
    },

    /** private: method[render]
     *  :param bulkRender: ``Boolean``
     */
    render: function(bulkRender) {
        var layer = this.layer instanceof OpenLayers.Layer && this.layer;
        if(!layer) {
            // guess the store if not provided
            if(!this.layerStore || this.layerStore == "auto") {
                this.layerStore = GeoExt.MapPanel.guess().layers;
            }
            // now we try to find the layer by its name in the layer store
            var i = this.layerStore.findBy(function(o) {
                return o.get("title") == this.layer;
            }, this);
            if(i != -1) {
                // if we found the layer, we can assign it and everything
                // will be fine
                layer = this.layerStore.getAt(i).get("layer");
            }
        }
        if (!this.rendered || !layer) {
            var ui = this.getUI();
            
            if(layer) {
                this.layer = layer;
                if(!this.text) {
                    this.text = layer.name;
                }
                
                if(this.childNodeType) {
                    this.addChildNodes();
                }
                
                ui.show();
                this.addVisibilityEventHandlers();
            } else {
                ui.hide();
            }
            
            if(this.layerStore instanceof GeoExt.data.LayerStore) {
                this.addStoreEventHandlers(layer);
            }            
        }
        GeoExt.tree.LayerNode.superclass.render.call(this, bulkRender);
    },
    
    /** private: method[addVisibilityHandlers]
     *  Adds handlers that sync the checkbox state with the layer's visibility
     *  state
     */
    addVisibilityEventHandlers: function() {
        this.layer.events.on({
            "visibilitychanged": this.onLayerVisibilityChanged,
            scope: this
        }); 
        this.on({
            "checkchange": this.onCheckChange,
            scope: this
        });
    },
    
    /** private: method[onLayerVisiilityChanged
     *  handler for visibilitychanged events on the layer
     */
    onLayerVisibilityChanged: function() {
        if(!this.visibilityChanging &&
                this.attributes.checked != this.layer.getVisibility()) {
            this.getUI().toggleCheck(this.layer.getVisibility());
        }
    },
    
    /** private: method[onCheckChange]
     *  :param node: ``GeoExt.tree.LayerNode``
     *  :param checked: ``Boolean``
     *
     *  handler for checkchange events 
     */
    onCheckChange: function(node, checked) {
        if (checked && this.layer.isBaseLayer && this.layer.map) {
            this.layer.map.setBaseLayer(this.layer);
        }
        this.layer.setVisibility(checked);
    },
    
    /** private: method[addStoreEventHandlers]
     *  Adds handlers that make sure the node disappeares when the layer is
     *  removed from the store, and appears when it is re-added.
     */
    addStoreEventHandlers: function() {
        this.layerStore.on({
            "add": this.onStoreAdd,
            "remove": this.onStoreRemove,
            "update": this.onStoreUpdate,
            scope: this
        });
    },
    
    /** private: method[onStoreAdd]
     *  :param store: ``Ext.data.Store``
     *  :param records: ``Array(Ext.data.Record)``
     *  :param index: ``Number``
     *
     *  handler for add events on the store 
     */
    onStoreAdd: function(store, records, index) {
        var l;
        for(var i=0; i<records.length; ++i) {
            l = records[i].get("layer");
            if(this.layer == l) {
                this.getUI().show();
            } else if (this.layer == l.name) {
                // layer is a string, which means the node has not yet
                // been rendered because the layer was not found. But
                // now we have the layer and can render.
                this.render(bulkRender);
                return;
            }
        }
    },
    
    /** private: method[onStoreRemove]
     *  :param store: ``Ext.data.Store``
     *  :param record: ``Ext.data.Record``
     *  :param index: ``Number``
     *
     *  handler for remove events on the store 
     */
    onStoreRemove: function(store, record, index) {
        if(this.layer == record.get("layer")) {
            this.getUI().hide();
        }
    },

    /** private: method[onStoreUpdate]
     *  :param store: ``Ext.data.Store``
     *  :param record: ``Ext.data.Record``
     *  :param operation: ``String``
     *  
     *  Listener for the store's update event.
     */
    onStoreUpdate: function(store, record, operation) {
    	var layer = record.get("layer");
        if(this.layer == layer && record.isModified("title") &&
                                    record.modified["title"] == this.text) {
            this.setText(record.get("title"));
        }
    },

    /** private: method[addChildNodes]
     *  Calls the add method of a node type configured as ``childNodeType``
     *  to add children.
     */
    addChildNodes: function() {
        if(typeof this.childNodeType == "string") {
            Ext.tree.TreePanel.nodeTypes[this.childNodeType].add(this);
        } else if(typeof this.childNodeType.add === "function") {
            this.childNodeType.add(this);
        }
    },
    
    /** private: method[destroy]
     */
    destroy: function() {
        var layer = this.layer;
        if (layer instanceof OpenLayers.Layer) {
            layer.events.un({
                "visibilitychanged": this.onLayerVisibilityChanged,
                scope: this
            });
        }
        delete this.layer;
        var layerStore = this.layerStore;
        if(layerStore) {
            layerStore.un("add", this.onStoreAdd, this);
            layerStore.un("remove", this.onStoreRemove, this);
            layerStore.un("update", this.onStoreUpdate, this);
        }
        delete this.layerStore;
        this.un("checkchange", this.onCheckChange, this);

        GeoExt.tree.LayerNode.superclass.destroy.call(this);
    }
});

/**
 * NodeType: gx_layer
 */
Ext.tree.TreePanel.nodeTypes.gx_layer = GeoExt.tree.LayerNode;
