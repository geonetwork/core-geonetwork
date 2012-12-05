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

/** api: (define)
 *  module = GeoNetwork.editor
 *  class = CRSSelectionPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor 
 *  .. class:: CRSSelectionPanel()
 *
 *     Create a GeoNetwork coordinate reference 
 *     system selection panel
 *
 *
 */
GeoNetwork.editor.CRSSelectionPanel = Ext.extend(Ext.FormPanel, {
    CRS: Ext.data.Record.create([{
        name: 'authority'
    }, {
        name: 'code'
    }, {
        name: 'version'
    }, {
        name: 'codeSpace'
    }, {
        name: 'description'
    }]),
    crsStore: undefined,
    catalogue: undefined,
    border: false,
    first: null,
    
    /**
     * Property: itemSelector
     */
    itemSelector: null,
    
    /**
     * Property: loadingMask
     */
    loadingMask: null,
    
    /**
     * Property: CrsCount
     */
    crsCount: null,
    
    /**
     * Property: ref
     */
    ref: null,
    
    /**
     * APIProperty: crsSelected
     */
    crsSelected: "",
    
    /**
     * relative imagePath for ItemSelector
     */
    imagePath: undefined,
    
    initComponent: function(){
        this.crsStore = new Ext.data.Store({
            proxy: new Ext.data.HttpProxy({
                url: this.catalogue.services.searchCRS,
                method: 'GET'
            }),
            baseParams: {
                name: '',
                type: '',
                maxResults: 50
            },
            reader: new Ext.data.XmlReader({
                record: 'crs',
                id: 'code'
            }, this.CRS),
            fields: ["code", "codeSpace", "authority", "description", "version"],
            sortInfo: {
                field: "description"
            }
        });
        
        this.items = [{
            xtype: 'panel',
            layout: 'fit',
            bodyStyle: 'padding: 5px;',
            border: false,
            tbar: [this.getCRSTypeCombo(), ' ', this.getCRS(), '->', OpenLayers.i18n('maxResults'), this.getLimitInput()],
            items: [this.getCRSItemSelector()]
        }];
        
        this.crsStore.on({
            'loadexception': function(){
            },
            'beforeload': function(store, options){
                if (Ext.getCmp('maxResults')) {
                    store.baseParams.maxResults = Ext.getCmp('maxResults').getValue();
                }
                if (!this.loadingMask) {
                    this.loadingMask = new Ext.LoadMask(this.itemSelector.fromMultiselect.getEl(), {
                        msg: OpenLayers.i18n('searching')
                    });
                }
                this.loadingMask.show();
            },
            'load': function(){
                this.loadingMask.hide();
            },
            scope: this
        });
        
        /**
         * triggered when the user has selected a CRS
         */
        this.addEvents('crsSelected');
        
        this.bbar = ['->', {
            id: 'crsSearchValidateButton',
            iconCls: 'addIcon',
            disabled: true,
            text: OpenLayers.i18n('add'),
            handler: function(){
                this.buildCRSXmlList();
            },
            scope: this
        }];
        
        GeoNetwork.editor.CRSSelectionPanel.superclass.initComponent.call(this);
    },
    
    getCRS: function(){
    
        return new GeoNetwork.form.SearchField({
            id: 'crsSearchField',
            width: 240,
            store: this.crsStore,
            paramName: 'name'
        });
    },
    
    /**
     * Method: getLimitInput
     *
     *
     */
    getLimitInput: function(){
        return {
            xtype: 'textfield',
            name: 'maxResults',
            id: 'maxResults',
            value: 50,
            width: 40
        };
    },
    
    getCRSTypeCombo: function(){
        var CRSType = Ext.data.Record.create([{
            name: 'id'
        }]);
        
        var crsTypeStore = new Ext.data.Store({
            url: this.catalogue.services.getCRSTypes,
            reader: new Ext.data.XmlReader({
                record: 'type'
            }, CRSType),
            fields: ['id']
        });
        
        var record = new CRSType({
            filename: OpenLayers.i18n('any')
        });
        record.set('id', '');
        
        crsTypeStore.add(record);
        crsTypeStore.load({
            add: true
        });
        
        return {
            xtype: 'combo',
            width: 150,
            id: 'search-crs',
            value: 0,
            store: crsTypeStore,
            triggerAction: 'all',
            mode: 'local',
            displayField: 'id',
            valueField: 'id',
            listWidth: 250,
            listeners: {
                select: function(combo, record, index){
                    this.crsStore.removeAll();
                    this.crsStore.baseParams.type = combo.getValue();
                    var value = Ext.getCmp('crsSearchField').getValue();
                    if (value.length < 1) {
                        this.crsStore.baseParams.name = '';
                    } else {
                        this.crsStore.baseParams.name = value;
                    }
                    this.crsStore.reload();
                },
                clear: function(combo){
                    this.crsStore.load();
                },
                scope: this
            }
        };
    },
    
    
    getCRSItemSelector: function(){
    
        var tpl = '<tpl for="."><div class="ux-mselect-item';
        if (Ext.isIE || Ext.isIE7) {
            tpl += '" unselectable=on';
        } else {
            tpl += ' x-unselectable"';
        }
        tpl += '>{description}</div></tpl>';
        
        this.itemSelector = new Ext.ux.ItemSelector({
            name: "itemselector",
            fieldLabel: "ItemSelector",
            dataFields: ["code", "codeSpace", "authority", "description", "version"],
            toData: [],
            msWidth: 320,
            msHeight: 230,
            valueField: "code",
            fromTpl: tpl,
            toTpl: tpl,
            toLegend: OpenLayers.i18n('selectedCRS'),
            fromLegend: OpenLayers.i18n('foundCRS'),
            fromStore: this.crsStore,
            fromAllowTrash: false,
            fromAllowDup: true,
            toAllowDup: false,
            drawUpIcon: false,
            drawDownIcon: false,
            drawTopIcon: false,
            drawBotIcon: false,
            imagePath: this.imagePath,
            toTBar: [{
                text: OpenLayers.i18n('clear'),
                handler: function(){
                    var i = this.getForm().findField("itemselector");
                    i.reset.call(i);
                },
                scope: this
            }]
        });
        
        // enable the validate button only if there are selected keywords
        this.itemSelector.on({
            'change': function(component){
                Ext.getCmp('crsSearchValidateButton').setDisabled(component.toStore.getCount() < 1);
            }
        });
        
        return this.itemSelector;
    },
    
    /**
     * APIMethod: setRef
     * Set the element reference
     */
    setRef: function(ref){
        this.ref = ref;
    },
    
    /**
     * Method: buildCrsXmlList
     *
     * populate crsSelected with xml string
     */
    buildCRSXmlList: function(){
    
        this.crsSelected = "";
        
        var store = this.itemSelector.toMultiselect.store;
        this.first = true;
        store.each(function(record){
            var tpl = "<gmd:referenceSystemInfo xmlns:gmd='http://www.isotc211.org/2005/gmd'  xmlns:gco='http://www.isotc211.org/2005/gco'>" +
                      "<gmd:MD_ReferenceSystem>" +
                        "<gmd:referenceSystemIdentifier>" +
                            "<gmd:RS_Identifier>" +
                                "<gmd:code>" +
                                    // Add description in the code tag. This information will be index and 
                                    // more useful than only the code. This could be improved later on
                                    "<gco:CharacterString>" +
                                        record.data.description +
                                    "</gco:CharacterString>" +
                                "</gmd:code>" +
                                "<gmd:codeSpace>" +
                                    "<gco:CharacterString>" +
                                        record.data.codeSpace +
                                    "</gco:CharacterString>" +
                                "</gmd:codeSpace>" +
                                "<gmd:version>" +
                                    "<gco:CharacterString>" +
                                        record.data.version +
                                    "</gco:CharacterString>" +
                                "</gmd:version>" +
                            "</gmd:RS_Identifier>" +
                        "</gmd:referenceSystemIdentifier>" +
                      "</gmd:MD_ReferenceSystem>" +
                    "</gmd:referenceSystemInfo>";
            
            this.crsSelected += (this.first ? "" : "&amp;&amp;&amp;") + tpl;
            this.first = false;
        }, this);
        
        if (this.crsSelected !== "") {
            // firing the event & closing the window
            this.fireEvent('crsSelected', this.crsSelected);
            this.ownerCt.hide();
        }
    }
});


/** api: xtype = gn_editor_crsselectionpanel */
Ext.reg('gn_editor_crsselectionpanel', GeoNetwork.editor.CRSSelectionPanel);