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

Ext.namespace('GeoNetwork', 'GeoNetwork.tree');

/**
 * Class: GeoNetwork.tree.WMSListGenerator
 *      WMSListGenerator generates an Ext.tree.TreeNode representing multiple
 *      Web Mapping Services and their layer lists.
 */

/**
 * Constructor: GeoNetwork.tree.WMSListGenerator
 * Create an instance of GeoNetwork.tree.WMSListGenerator
 *
 * Parameters:
 * config - {Object} A config object used to set the properties
 */
GeoNetwork.tree.WMSListGenerator = function(config){
    Ext.apply(this, config);
    if (this.node && this.wmsStore) {
        this.createWMSList();
    }
};

GeoNetwork.tree.WMSListGenerator.prototype = {

    /**
     * APIProperty: node
     * {<Ext.tree.TreeNode>} the node to which the WMS list will be appended
     */
    node: null,

    /**
     * APIProperty: wmsStore
     * {<Ext.data.Store>} a store of WMS services, which have a title and a 
     *     url property
     */
    wmsStore: null,

    /**
     * APIProperty: click
     * {Function} click function to use when clicked on the child nodes
     */
    click: null,

    /**
     * APIProperty: scope
     * {Object} scope to use for the click function
     */
    scope: null,

    /**
     * APIMethod: createWMSList
     * create a tree node per WMS and on click of that node load the layers
     *     thorugh the loadWMS API method.
     */
    createWMSList: function() {
        this.wmsStore.each(this.appendRecord, this);
    },

    /**
     * Method: appendRecord
     * Append an Ext.data.Record from the wmsStore to the treeview
     *
     * Parameters:
     * record - {<Ext.data.Record>} a record with a url and title property
     */
    appendRecord: function(record) {
        var wmsNode = new Ext.tree.TreeNode({url: record.get('url'),
            text: record.get('title'), cls: 'folder', leaf: false});
        // append a dummy child so that it will appear as a directory
        wmsNode.appendChild(new Ext.tree.TreeNode({text: '', dummy: true}));
        wmsNode.addListener("beforeexpand", this.addNodesFromWMS, this);
        this.node.appendChild(wmsNode);
    },

    /**
     * Method: replaceNode
     * Replace the node with the WMS title with the one extracted from the
     *     GetCapabilities response
     *
     * Parameters:
     * node - {<Ext.tree.TreeNode>} the node extracted from the GetCapabilities
     */
    replaceNode: function(node) {
        this.currentNode.parentNode.replaceChild(node, this.currentNode);
        node.ui.afterLoad();
        node.expand();
    },

    /**
     * Method: addNodesFromWMS
     * Before the node is expanded, load a new TreeNode from the
     *     GetCapabilities and when it is done replace the current node
     *     with the newly created one.
     *
     * Parameters:
     * node - {<Ext.tree.TreeNode>} the WMS node with only a title and url
     */
    addNodesFromWMS: function(node) {
        // first remove the dummy child
        if (node.firstChild && node.firstChild.attributes.dummy) {
            node.removeChild(node.firstChild);
            node.ui.beforeLoad();
            this.scope.currentNode = node;
            // start loading the actual layer tree, when done replace
            var treeGenerator = new GeoNetwork.tree.WMSTreeGenerator(
                {click: this.click, callback: this.replaceNode, 
                    scope: this.scope});
            treeGenerator.loadWMS(node.attributes.url);
        }
    }

};
