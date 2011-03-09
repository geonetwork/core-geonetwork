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
 * @requires GeoNetwork/widgets/wms/PreviewPanel.js
*/

Ext.namespace('GeoNetwork', 'GeoNetwork.wms');

/**
 * Class: GeoNetwork.wms.BrowserPanel
 *      WMSBrowserPanel groups together a TreePanel and a WMSPreviewPanel
 *      as well as the button to add layers to the map.
 */

/**
 * Constructor: GeoNetwork.wms.BrowserPanel
 * Create an instance of GeoNetwork.wms.BrowserPanel
 *
 * Parameters:
 * config - {Object} A config object used to set the properties
 */
GeoNetwork.wms.BrowserPanel = function(config){
    Ext.apply(this, config);
    GeoNetwork.wms.BrowserPanel.superclass.constructor.call(this);
};

GeoNetwork.wms.BrowserPanel.ADDWMS = 0;
GeoNetwork.wms.BrowserPanel.WMSLIST = 1;

Ext.extend(GeoNetwork.wms.BrowserPanel, Ext.Panel, {

    /**
     * APIProperty: previewPanel
     * {<GeoNetwork.WMSPreviewPanel>} the panel used for previewing
     *     WMS layers
     */
    previewPanel: null,

    /**
     * APIProperty: treePanel
     * {<Ext.tree.TreePanel>} the panel used for the treeview
     */
    treePanel: null,

    /**
     * APIProperty: map
     * {<OpenLayers.Map>} the map object
     */
    map: null,

    /**
     * APIProperty: previewCenterPoint
     * {<OpenLayers.Geometry.Point>} it preview can be centered on this point,
     *     but defaults to the map center
    */
    previewCenterPoint: null,

    /**
     * APIProperty: mode
     * {Integer} what mode should the wms browser panel operate in?
     *     One of: ADDWMS or WMSLIST. Default is WMSLIST
     */
    mode: GeoNetwork.wms.BrowserPanel.WMSLIST,

    /**
     * APIProperty: wmsStore
     * {<Ext.data.Store>} a store of wms servers, which have a title and a
     *     url property. Only needed for mode = WMSLIST
     */
    wmsStore: null,

    /**
     * APIProperty: url
     * {<Ext.data.Store>} default url to show
     *     url property. Only needed for mode = WMSLIST
     */
    url: '',


    /**
     * initComponent
     * Initialize this component
    */
    initComponent: function() {
        GeoNetwork.wms.BrowserPanel.superclass.initComponent.call(this);

        this.layout = 'border';

        this.previewPanel = new GeoNetwork.wms.PreviewPanel(
            {map: this.map, previewCenterPoint: this.previewCenterPoint});

        var root;
        this.treePanel = new Ext.tree.TreePanel({rootVisible: false,
            autoScroll: true});
        root = new Ext.tree.TreeNode({text: '', draggable:false,
            cls: 'folder'});
        this.treePanel.setRootNode(root);

        var centerItems;
        if (this.mode == GeoNetwork.wms.BrowserPanel.ADDWMS) {
            this.form = new Ext.form.FormPanel({labelWidth: 25});

            var url = new Ext.form.TextField({
                fieldLabel: 'URL',
                name: 'wmsurl',
                value: this.url,
                width: 250, autoHeight: true
            });

            /*var protocol = new Ext.form.ComboBox({
                mode:           'local',
                value:          '1.1.1',
                triggerAction:  'all',
                forceSelection: true,
                editable:       false,
                fieldLabel:     'Protocol',
                name:           'wmsprotocol',
                hiddenName:     'wmsprotocol',
                displayField:   'name',
                valueField:     'value',
                store:          new Ext.data.JsonStore({
                    fields : ['name', 'value'],
                    data   : [
                        {name : '1.1.1', value: '1.1.1'},
                        {name : '1.3.0', value: '1.3.0'}
                    ]
                }),
                width: 100
            });*/

            this.form.add(url);
            //this.form.add(protocol);

            this.form.add(new Ext.Button({id: 'parse', text:
                OpenLayers.i18n("WMSBrowserConnectButton"),
                scope: this, handler: this.getWMSCaps}));

            this.form.add(this.treePanel);
            centerItems = [this.form];
        } else {
            centerItems = [this.treePanel];
        }

        var center = {autoScroll: true, region: 'center', items: centerItems,
            split: true, width: 300, minWidth: 300, border: false};

        var east = {region: 'east', border: false, items: [this.previewPanel], split: true,
            plain: true, cls: 'popup-variant1', width:250, maxSize: 250,
            minSize: 250};

        this.add(center);
        this.add(east);

        if (this.mode == GeoNetwork.wms.BrowserPanel.WMSLIST) {
            this.treeGen = new GeoNetwork.tree.WMSListGenerator(
                {click: this.nodeClick, scope: this,
                node: this.treePanel.getRootNode(), wmsStore: this.wmsStore});
        } else if (this.mode == GeoNetwork.wms.BrowserPanel.ADDWMS) {

            new GeoNetwork.tree.WMSListGenerator(
                {click: this.nodeClick, scope: this,
                node: this.treePanel.getRootNode(), wmsStore: this.wmsStore});

            this.treeGen = new GeoNetwork.tree.WMSTreeGenerator(
                {click: this.nodeClick, callback: this.showTree, scope: this});
        }

        this.addButton(OpenLayers.i18n("WMSBrowserAddButton"),
            this.addLayerToMap, this);

        this.doLayout();

        if (this.url != '') this.getWMSCaps();
    },

    /**
     * Method: showTree
     * Show the treeview of 1 WMS when the layer structure has been loaded
     *
     * Parameters:
     * node - {<Ext.tree.TreeNode>} the newly created node
     */
    showTree: function(node, capability) {
        if (!node) {
            Ext.MessageBox.alert(OpenLayers.i18n("errorTitle"),
                OpenLayers.i18n("WMSBrowserConnectError"));
            this.body.dom.style.cursor = 'default';
        }

        var accessContraints = capability.service.accessContraints;

        if ((accessContraints) && (accessContraints.toLowerCase() != "none") &&
          (accessContraints != "-")) {
            var disclaimerWindow = new GeoNetwork.DisclaimerWindow({
                disclaimer: accessContraints
            });
            disclaimerWindow.show();
            disclaimerWindow = null;
        }

        var root = this.treePanel.getRootNode();
        // remove previous WMS node
        /*while(root.firstChild){
          root.removeChild(root.firstChild);
        }*/
        if (node) {
            this.treePanel.getRootNode().appendChild(node);
            node.expand(true);
        }
        this.treePanel.show();
        this.body.dom.style.cursor = 'default';
    },


    loadUrl: function(url) {
         this.form.getForm().findField('wmsurl').setValue(url);
         this.getWMSCaps();
    },

    /**
     * Method: getWMSCaps
     * Load the WMS Capabilities through the tree generator
     *
     * Parameters:
     * btn - {<Ext.Button>} the button pressed
     */
    getWMSCaps: function(btn) {
        var url = this.form.getForm().findField('wmsurl').getValue();
        //var protocol = this.form.getForm().findField('wmsprotocol').getValue();
        // trim the string
        url = url.replace(/^\s+|\s+$/g, '');
        if (url != '') {
            this.body.dom.style.cursor = 'wait';
            this.treeGen.loadWMS(url);
        }
    },

    /**
     * Method: nodeClick
     * When a node is clicked on, its preview needs to be shown
     *
     * Parameters:
     * node - {<Ext.tree.TreeNode>} the node clicked on
     */
    nodeClick: function(node) {
        this.previewPanel.showPreview(node.attributes.wmsLayer);
    },

    /**
     * Method: addLayerToMap
     * Add the WMS layer to the main map
     */
    addLayerToMap: function () {
        if (this.previewPanel.currentLayer) {
            var layerExists = GeoNetwork.OGCUtil.layerExistsInMap(
                this.previewPanel.currentLayer, this.map);
            if (!layerExists) {
                this.previewPanel.currentLayer.events.on({"loadstart": function() {
                    this.isLoading = true;
                }});

                this.previewPanel.currentLayer.events.on({"loadend": function() {
                    this.isLoading = false;
                }});

                this.map.addLayers([this.previewPanel.currentLayer]);
            } else {
                Ext.MessageBox.alert(OpenLayers.i18n("infoTitle"),
                    OpenLayers.i18n("WMSBrowserDuplicateMsg"));
            }
        }
    }

});

Ext.reg('gn_wmsbrowserpanel', GeoNetwork.wms.BrowserPanel);