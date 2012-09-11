/*
 * Copyright (C) 2001-2011 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * 
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
Ext.namespace('GeoNetwork.editor');

/**
 * Filter to create hidden form fields for CSW queries
 */
GeoNetwork.editor.Filter = {
    SERVICE: [{
        name: 'E_type',
        value: 'service'
    }],
    DATASET: [{
        name: 'E_type',
        value: 'dataset'
    }    // If dataset search should be restricted to ISO19139 or profil add criteria on schema. 
    //, {name: 'S__schema', value: 'iso19139'}
    ],
    FEATURE_CATALOGUE: [{
        name: 'E__schema',
        value: 'iso19110'
    }]
};


/** api: (define)
 *  module = GeoNetwork.editor
 *  class = LinkedMetadataSelectionPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor 
 *  .. class:: LinkedMetadataSelectionPanel()
 *
 *     Create a GeoNetwork selection panel
 *     for related metadata. It helps creating links
 *     to feature catalogues, datasets and services, parent and child.
 *
 *
 */
GeoNetwork.editor.linkedMetadata = {};

GeoNetwork.editor.LinkedMetadataSelectionPanel = Ext.extend(Ext.FormPanel, {
    catalogue: undefined,
    border: false,
    layout: 'fit',
    /**
     * URL to use to go to the metadata create page.
     * Extra parameter could be use to filter template list.
     *
     * If null does not display button to create
     * a new element based on this parameter.
     */
    createIfNotExistURL: null,
    
    /**
     * An array of hidden parameter for the form.
     * Default value is set to dataset.
     */
    hiddenParameters: undefined,
    
    /**
     * Define if multiple selection is allowed or not
     */
    singleSelect: true,
    
    /**
     * Property: loadingMask
     */
    loadingMask: null,
    mdStore: null,
    /**
     * Property: ref
     */
    ref: null,
    proxy: null,
    mode: null,
    serviceUrl: null,
    capabilitiesStore: null,
    linkedMetadataStore: undefined,
    initComponent: function(){
        /**
         * triggered when the user has selected an element
         */
        this.addEvents('linkedmetadataselected');
        
        this.mdStore = GeoNetwork.data.MetadataResultsStore();
        this.linkedMetadataStore = GeoNetwork.data.MetadataCSWResultsStore();
        if (this.mode === 'attachService' || this.mode === 'coupledResource') {
            this.capabilitiesStore = new GeoExt.data.WMSCapabilitiesStore({
                url: this.serviceUrl,
                id: 'capabilitiesStore',
                listeners: {
                    exception: function(proxy, type, action, options, res, arg){
                        Ext.MessageBox.alert(OpenLayers.i18n("error"));
                    },
                    beforeload: function(){
                        // Update store URL according to selected service.
                        if (this.mode === 'attachService') {
                            var selected = Ext.getCmp('linkedMetadataGrid').getSelectionModel().getSelections();
                            if (selected === undefined || selected[0].data.uri === '') {
                                Ext.MessageBox.alert(OpenLayers.i18n("noServiceURLError"));
                            }
                            this.capabilitiesStore.baseParams.url = selected[0].data.uri + 
                                        "?&SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1";
                        } else if (this.mode === 'coupledResource') {
                            this.capabilitiesStore.baseParams.url = this.serviceUrl;
                        }
                    },
                    loadexception: function(){
                        Ext.MessageBox.alert(OpenLayers.i18n("GetCapabilitiesDocumentError") + this.capabilitiesStore.baseParams.url);
                    },
                    scope: this
                }
            });
        }
        
        // Add extra parameters according to selection panel
        if (this.mode === 'attachService') {
            this.hiddenParameters = GeoNetwork.editor.Filter.SERVICE;
        } else if (this.mode === 'iso19110') {
            this.hiddenParameters = GeoNetwork.editor.Filter.FEATURE_CATALOGUE;
        }
        
        var checkboxSM = new Ext.grid.CheckboxSelectionModel({
            singleSelect: this.singleSelect,
            header: '',
            listeners: {
                selectionchange: function(){
                    Ext.getCmp('linkedMetadataValidateButton').setDisabled(this.getSelections().length < 1);
                }
            }
        });
        
        var tbarItems = [this.getSearchInput(), '->', OpenLayers.i18n('maxResults'), this.getLimitInput()];
        this.addHiddenFormInput(tbarItems);
        
        var grid = new Ext.grid.GridPanel({
            id: 'linkedMetadataGrid',
            xtype: 'grid',
            layout: 'fit',
            height: 280,
            //autoHeight: true,
            bodyStyle: 'padding: 0px;',
            border: true,
            loadMask: true,
            tbar: tbarItems,
            store: this.linkedMetadataStore,
            columns: [checkboxSM, {
                id: 'title',
                header: OpenLayers.i18n('mdTitle'),
                dataIndex: 'title'
            }, {
                id: 'subject',
                header: OpenLayers.i18n('keywords'),
                dataIndex: 'subject',
                hidden: true
            }, {
                id: 'uri',
                header: OpenLayers.i18n('uri'),
                dataIndex: 'getCapabilitiesUrl'
            }],
            sm: checkboxSM,
            autoExpandColumn: 'title',
            listeners: {
                rowclick: function(grid, rowIndex, e){
                    if (this.capabilitiesStore !== null && this.mode !== 'coupledResource') {
                        this.serviceUrl = grid.getStore().getAt(rowIndex).data.uri;
                        if (this.serviceUrl === '') {
                            this.capabilitiesStore.removeAll();
                        } else {
                            this.capabilitiesStore.baseParams.url = this.serviceUrl;
                            this.capabilitiesStore.reload();
                        }
                    }
                },
                scope: this
            }
        });
        
        
        if (this.mode === 'attachService' || this.mode === 'coupledResource') {
            this.items = this.getScopedNamePanel(grid);
        } else {
            this.items = grid;
        }
        
        var panel = this;
        this.bbar = ['->', {
            id: 'linkedMetadataValidateButton',
            iconCls: 'relatedAddIcon',
            text: OpenLayers.i18n('createRelation'),
            disabled: true,
            handler: function(){
                var selected = grid.getSelectionModel().getSelections();
                this.fireEvent('linkedmetadataselected', this, selected);
                // we assume that this panel is in a window
                this.ownerCt.close();
            },
            scope: panel
        }
        //, this.getCreateIfNotExistButton()
        ];
        
        
        
        this.linkedMetadataStore.on({
            'load': function(){
                if (this.loadingMask !== null) {
                    this.loadingMask.hide();
                }
            },
            scope: this
        });
        GeoNetwork.editor.LinkedMetadataSelectionPanel.superclass.initComponent.call(this);
        
    },
    
    /**
     * Create a button according to createIfNotExistURL.
     */
    getCreateIfNotExistButton: function(){
        if (!this.createIfNotExistURL) {
            return '';
        }
        
        return {
            id: 'createIfNotExistButton',
            iconCls: 'addIcon',
            text: OpenLayers.i18n('createIfNotExistButton'),
            handler: function(){
                window.location.replace(this.createIfNotExistURL);
            },
            scope: this
        };
        
    },
    
    /**
     * APIMethod: setRef
     * Set the element reference
     */
    setRef: function(ref){
        this.ref = ref;
    },
    
    /**
     * Add hidden textfields in an item list.
     */
    addHiddenFormInput: function(items){
        var i;
        if (this.hiddenParameters) {
            for (i = 0; i < this.hiddenParameters.length; i++) {
                items.push({
                    xtype: 'textfield',
                    fieldLabel: this.hiddenParameters[i].name,
                    name: this.hiddenParameters[i].name,
                    value: this.hiddenParameters[i].value,
                    hidden: true
                });
            }
        }
        return items;
    },
    /**
     * Return a full text search input with search button.
     */
    getSearchInput: function(){
        return new GeoNetwork.form.SearchField({
            name: 'E.8_AnyText',
            width: 240,
            store: this.linkedMetadataStore,
            triggerAction: function(scope){
                scope.doSearch();
                //scope.search('linkedMetadataGrid', null, null, 1, true, this.mdStore, null);
            },
            scope: this
        });
    },
    /**
     * Method: getLimitInput
     *
     */
    getLimitInput: function(){
        return {
            xtype: 'textfield',
            name: 'nbResultPerPage',
            id: 'nbResultPerPage',
            value: 20,
            width: 40
        };
    },
    
    getScopedNamePanel: function(grid){
    
        var combo = {
            xtype: 'combo',
            id: 'getCapabilitiesLayerNameCombo',
            fieldLabel: OpenLayers.i18n('getCapabilitiesLayer'),
            store: this.capabilitiesStore,
            valueField: 'name',
            displayField: 'title',
            triggerAction: 'all',
            //disabled: (serviceUrl==null?true:false),
            listeners: {
                select: function(combo, record, index){
                    Ext.getCmp('getCapabilitiesLayerName').setValue(combo.getValue());
                }
            }
        };
        var layerName = {
            xtype: 'textfield',
            id: 'getCapabilitiesLayerName',
            fieldLabel: OpenLayers.i18n('layerName'),
            valueField: 'name',
            displayField: 'title'
        };
        var panel = {
            xtype: 'panel',
            layout: 'form',
            bodyStyle: 'padding: 2px;',
            border: true,
            items: [grid, combo, layerName]
        };
        return panel;
    },
    
    /**
     *
     */
    doSearch: function(){
        if (!this.loadingMask) {
            this.loadingMask = new Ext.LoadMask(this.getEl(), {
                msg: OpenLayers.i18n('searching')
            });
        }
        this.loadingMask.show();
        
        GeoNetwork.editor.nbResultPerPage = 20;
        if (Ext.getCmp('nbResultPerPage')) {
            GeoNetwork.editor.nbResultPerPage = Ext.getCmp('nbResultPerPage').getValue();
        }
        GeoNetwork.util.CSWSearchTools.doCSWQueryFromForm(this.id, this.catalogue, 1, this.showResults.bind(this), null, Ext.emptyFn);
    },
    
    showResults: function(response){
        var getRecordsFormat = new OpenLayers.Format.CSWGetRecords.v2_0_2();
        var r = getRecordsFormat.read(response.responseText);
        var values = r.records;
        if (values.length > 0) {
            this.linkedMetadataStore.loadData(r);
        }
        if (this.loadingMask !== null) {
            this.loadingMask.hide();
        }
    }
});

/** api: xtype = gn_editor_linkedmetadataselectionpanel */
Ext.reg('gn_editor_linkedmetadataselectionpanel', GeoNetwork.editor.LinkedMetadataSelectionPanel);