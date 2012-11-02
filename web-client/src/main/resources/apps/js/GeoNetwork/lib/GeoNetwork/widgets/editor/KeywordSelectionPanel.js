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

GeoNetwork.editor.keyword = {};


/** api: (define)
 *  module = GeoNetwork.editor
 *  class = KeywordSelectionPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor 
 *  .. class:: KeywordSelectionPanel()
 *
 *     Create a GeoNetwork keyword selection panel
 *     using thesaurus loaded in GeoNetwork.
 *
 *
 */
GeoNetwork.editor.KeywordSelectionPanel = Ext.extend(Ext.FormPanel, {
    catalogue: undefined,
    
    Keyword: Ext.data.Record.create([{
        name: 'value'
    }, {
        name: 'thesaurus',
        mapping: 'thesaurus/key'
    }, {
        name: 'uri'
    }]),
    keywordStore: undefined,
    
    border: false,
    
    /**
     * Property: addAsXLink
     */
    addAsXLink: false,
    
    /**
     * Property: itemSelector
     */
    itemSelector: null,
    
    /**
     * Property: loadingMask
     */
    loadingMask: null,
    
    /**
     * Property: ThesaurusCount
     */
    ThesaurusCount: null,
    
    /**
     * Property: ref
     */
    ref: null,
    
    name: 'gmd:descriptiveKeywords',
    /**
     * APIProperty: keywordsSelected
     * {Object} Hash table of selected contacts with their XML raw data
     */
    keywordsSelected: [],
    
    initComponent: function(){
    
    
        this.keywordStore = new Ext.data.Store({
            proxy: new Ext.data.HttpProxy({
                url: this.catalogue.services.searchKeyword, // FIXME : global var
                method: 'GET'
            }),
            baseParams: {
                pNewSearch: true,
                pTypeSearch: 1,
                pThesauri: '',
                pMode: 'searchBox'
            },
            reader: new Ext.data.XmlReader({
                record: 'keyword',
                id: 'uri'
            }, this.Keyword),
            fields: ["value", "thesaurus", "uri"],
            sortInfo: {
                field: "thesaurus"
            }
        });
        
        this.items = [{
            xtype: 'panel',
            layout: 'fit',
            bodyStyle: 'padding: 5px;',
            border: false,
            tbar: [this.getThesaurusCombo(), ' ', this.getKeyword(), '->', OpenLayers.i18n('maxResults') + ' ' + OpenLayers.i18n('perThesaurus'), this.getLimitInput()],
            items: [this.getKeywordsItemSelector()]
        }];
        
        
        this.keywordStore.on({
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
         * triggered when the user has selected a keyword
         */
        this.addEvents('keywordselected');
        
        this.bbar = ['->', {
            id: 'addAsXLinkCheckBox',
            xtype: 'checkbox',
            checked: false,
            boxLabel: OpenLayers.i18n('addAsXLink'),
            listeners: {
                    check: function(c, checked) {
                        this.addAsXLink = checked;
                    },
                    scope: this
                }
            }, {
            id: 'keywordSearchValidateButton',
            iconCls: 'addIcon',
            disabled: true,
            text: OpenLayers.i18n('add'),
            handler: function(){
                this.buildKeywordXmlList();
                // The event will be fired on requests response completed for
                // every thesaurus
            },
            scope: this
        }];
        
        GeoNetwork.editor.KeywordSelectionPanel.superclass.initComponent.call(this);
    },
    
    getKeyword: function(){
    
        return new GeoNetwork.form.SearchField({
            id: 'keywordSearchField',
            width: 240,
            store: this.keywordStore,
            paramName: 'pKeyword'
        });
    },
    
    /**
     * APIMethod: setRef
     * Set the element reference
     */
    setRef: function(ref){
        this.ref = ref;
    },
    /**
     * APIMethod: setAddAsXLink
     * Set the add as XLink option
     */
    setAddAsXLink: function(xlink){
        this.addAsXLink = xlink || false;
        Ext.getCmp('addAsXLinkCheckBox').setValue(this.addAsXLink);
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
    
    getThesaurusCombo: function(){
        /**
         * Property: thesaurusStore
         */
        GeoNetwork.editor.keyword.thesaurusStore = new GeoNetwork.data.ThesaurusStore({
            url: this.catalogue.services.getThesaurus,
            allOption: true,
            activatedOnly: true
        });
        
        return {
            xtype: 'combo',
            width: 150,
            id: 'search-thesauri',
            value: 0,
            store: GeoNetwork.editor.keyword.thesaurusStore,
            triggerAction: 'all',
            mode: 'local',
            displayField: 'title',
            valueField: 'id',
            listWidth: 250,
            listeners: {
                select: function(combo, record, index){
                    this.keywordStore.removeAll();
                    this.keywordStore.baseParams.pThesauri = combo.getValue();
                    var value = Ext.getCmp('keywordSearchField').getValue();
                    if (value.length < 1) {
                        this.keywordStore.baseParams.pKeyword = '*';
                    } else {
                        this.keywordStore.baseParams.pKeyword = value;
                    }
                    this.keywordStore.reload();
                },
                clear: function(combo){
                    this.keywordStore.load();
                },
                scope: this
            }
        };
    },
    
    
    getKeywordsItemSelector: function(){
    
        var tpl = '<tpl for="."><div class="ux-mselect-item';
        if (Ext.isIE || Ext.isIE7) {
            tpl += '" unselectable=on';
        } else {
            tpl += ' x-unselectable"';
        }
        tpl += '>{id} {value} <span class="ux-mselect-item-thesaurus">({thesaurus})</span></div></tpl>';
        
        this.itemSelector = new Ext.ux.ItemSelector({
            name: "itemselector",
            fieldLabel: "ItemSelector",
            dataFields: ["value", "thesaurus"],
            toData: [],
            msWidth: 320,
            msHeight: 230,
            valueField: "value",
            toSortField: undefined,
            fromTpl: tpl,
            toTpl: tpl,
            toLegend: OpenLayers.i18n('selectedKeywords'),
            fromLegend: OpenLayers.i18n('foundKeywords'),
            fromStore: this.keywordStore,
            fromAllowTrash: false,
            fromAllowDup: true,
            toAllowDup: false,
            drawUpIcon: false,
            drawDownIcon: false,
            drawTopIcon: false,
            drawBotIcon: false,
            imagePath: '../js/ext-ux/images', // FIXME
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
                Ext.getCmp('keywordSearchValidateButton').setDisabled(component.toStore.getCount() < 1);
            }
        });
        
        return this.itemSelector;
    },
    
    /**
     * Method: buildKeywordXmlList
     *
     * populate keywordsSelected array with xml strings
     */
    buildKeywordXmlList: function(){
    
        this.keywordsSelected = [];
        var self = this;
        this.ThesaurusCount = 0;
        
        var thesaurusCollection = [];
        var store = this.itemSelector.toMultiselect.store;
        thesaurusCollection = store.collect('thesaurus');
        Ext.each(thesaurusCollection, function(thesaurus, index, thesauri){
            store.filter('thesaurus', thesaurus);
            var values = store.collect('uri');
            
            // Encode "#" as "%23"?
            Ext.each(values, function(item, index){
                values[index] = item.replace("#", "%23");
            });
            
            var serviceUrl = self.catalogue.services.getKeyword; // FIXME : depends on app
            var multiple = values.length > 1 ? true : false;
            var inputValue = serviceUrl + 
                                '?thesaurus=' + thesaurus + 
                                '&id=' + values.join(',') +
                                '&multiple=' + multiple;
            
            ++self.ThesaurusCount;
             if (self.addAsXLink) {
                var tpl = "<gmd:descriptiveKeywords xmlns:xlink='http://www.w3.org/1999/xlink' xmlns:gmd='http://www.isotc211.org/2005/gmd'  xmlns:gco='http://www.isotc211.org/2005/gco'" +
                " xlink:href='" + inputValue + "'/>";
                self.keywordsSelected.push(tpl);
            } else {
                self.retrieveKeywordData(inputValue);
            }
        });
        
        if (this.addAsXLink) {
          this.fireEvent('keywordselected', this, this.keywordsSelected);
          this.ownerCt.hide();
        }
        store.clearFilter();
    },
    
    /**
     * Method: retrieveKeywordData
     *
     * Load keyword data, transform it to a json object, & put it in selectedKeywordsJson
     */
    retrieveKeywordData: function(url){
    
        Ext.getCmp('keywordSearchValidateButton').disable();
        
        Ext.Ajax.request({
        
            url: url,
            method: 'GET',
            scope: this,
            success: function(response){
                var keyword = response.responseText;
                if (keyword.indexOf('<gmd:MD_Keywords') !== -1) {
                    this.keywordsSelected.push("<gmd:descriptiveKeywords xmlns:gmd='http://www.isotc211.org/2005/gmd'>" 
                            + response.responseText + "</gmd:descriptiveKeywords>");
                }
                Ext.getCmp('keywordSearchValidateButton').enable();
                this.ThesaurusCount -= 1;
                if (this.ThesaurusCount === 0) {
                    // Wait until the request for *each* thesaurus has ended before
                    // firing the event & closing the window
                    this.fireEvent('keywordselected', this, this.keywordsSelected);
                    this.ownerCt.hide();
                }
            }
            
        });
        
    }
});

/** api: xtype = gn_editor_keywordselectionpanel */
Ext.reg('gn_editor_keywordselectionpanel', GeoNetwork.editor.KeywordSelectionPanel);