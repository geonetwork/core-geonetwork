/*
 * Copyright (C) 2012 Food and Agriculture Organization of the
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
 *  class = ConceptSelectionPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor 
 *  .. class:: ConceptSelectionPanel()
 *
 *     Create a GeoNetwork keyword selection panel
 *     using thesaurus loaded in GeoNetwork.
 *
 *
 */
GeoNetwork.editor.ConceptSelectionPanel = Ext.extend(Ext.Panel, {
    defaultConfig: {
        /**
         * thesaurus identifier to select by default (null by default)
         */
        defaultThesaurus: null,
        /**
         * search language (default is GUI language)
         */
        lang: null,
        /**
         * display search filter or not (default is true)
         */
        allowSearch: null,
        /**
         * trigger search on load
         */
        searchOnLoad: null,
        mode: null,
        border: false,
//        autoHeight: true,
        layout: 'table',
        thesaurusInfoTpl: new Ext.XTemplate(
                '<tpl for=".">',
                    '<div class="thesaurusInfo"><span class="title">{title}</span><span class="theme">{theme}</span><span class="filename">({filename})</span></div>',
                '</tpl>'
        ),
        keywordsTpl: new Ext.XTemplate(
            '<tpl for=".">',
                // TODO : add definiton ?
                '<div class="badge">{value}<span class="ctrl">x</span></div>',
            '</tpl>'
        )
    },
    /**
     * null if popup mode, identifier of the DOM element for inline mode
     */
    renderTo: null,
    /**
     * element type to edit (eg. gmd:descriptiveKeywords)
     */
    elementType: null,
    /**
     * array of transformations (use to override the server configuration)
     */
    transformations: [],
    transformation: null,
    defaultTransformation: 'to-iso19139-keyword',
    /**
     * array of thesaurus (use to override the server configuration)
     */
    thesaurus: null,
    thesaurusIdentifier: null,
    thesaurusSelector: null,
    nbResultsField: null,
    /**
     * Store of select keyword (uri, value, thesaurus, xmin, ymin, xmax, ymax)
     */
    keywords: null,
    KeywordRecord: Ext.data.Record.create([{
            name: 'value'
        }, {
            name: 'thesaurus',
            mapping: 'thesaurus/key'
        }, {
            name: 'uri'
        }]),
    keywordStore: null,
    selectedKeywordStore: null,
    initialKeyword: [],
    loadKeywords: function () {
        
    },
    generateDataView: function () {
        
        // Add keyword view
        var dv = new Ext.DataView({
            store: this.selectedKeywordStore,
            tpl: this.keywordsTpl,
            autoHeight: true,
            multiSelect: false,
            overClass: 'badge-warning',
            itemSelector: 'div',
            listeners: {
                // Remove clicked element from the selection store
                click: function (dv, index, node, e) {
                    dv.getStore().removeAt(index);
                }
            }
        });
        
        return dv;
    },
    /**
     * Provide a simple combo box for selection of one keyword
     */
    generateSimpleCombo: function () {
        // TODO : load current keyword
        // TODO : Validate mode : combo mode is only available if number of keyword
        // is 0 or 1
        var combo = new Ext.form.ComboBox({
            store: this.keywordStore,
            triggerAction: 'all',
            mode: 'local',
            displayField: 'value',
            valueField: 'uri',
            listWidth: 200,
            listeners: {
                select: function (combo, record, index) {
                    console.log("Keyword selected");
                    this.selectedKeywordStore.add([record]);
                    console.log(this.selectedKeywordStore);
                    
                },
                scope: this
            }
        });
        return [combo, this.generateDataView()];
    },
    generateFilterField: function () {
        return new GeoNetwork.form.SearchField({
            width: 200,
            store: this.keywordStore,
            paramName: 'pKeyword'
        });
    },
    /**
     * Provide a multiple selection list for selecting keywords
     */
    generateSelectionList: function (withFilter) {
        // TODO : Validate mode : combo mode is only available if number of keyword
        // is 0 or 1
        var search = this.generateFilterField();
        
        var dv = new Ext.DataView({
            store: this.keywordStore,
            tpl: this.keywordsTpl,
            //autoHeight: true,
            multiSelect: false,
            boxMaxHeight: 350,
            height: 100,
            overClass: 'badge-success',
            itemSelector: 'div',
            listeners: {
                // Remove clicked element from the selection store
                click: function (dv, index, node, e) {
                    this.selectedKeywordStore.add([dv.getStore().getAt(index)]);
                    dv.getStore().removeAt(index);
                },
                scope: this
            }
        });
        return [search, dv];
    },
    /**
     * Provide a multiple item selector
     */
    getKeywordsItemSelector: function (withFilter) {
    
        var tpl = '<tpl for="."><div class="badge ux-mselect-item';
        if (Ext.isIE || Ext.isIE7) {
            tpl += '" unselectable=on';
        } else {
            tpl += ' x-unselectable"';
        }
        tpl += '>{id} {value} </div></tpl>';
        //<span class="ux-mselect-item-thesaurus">({thesaurus})</span>
        
        this.itemSelector = new Ext.ux.ItemSelector({
            name: "itemselector",
            fieldLabel: "ItemSelector",
            dataFields: ["value", "thesaurus"],
            //toData: [],
            toStore: this.selectedKeywordStore,
            msWidth: 350,
            msHeight: 200,
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
            fromTBar: [this.generateFilterField(), '->', 
                       OpenLayers.i18n('maxResults'), this.getLimitInput()],
            toTBar: [{
                // control to clear all select keywwords and refresh the XML.
                text: OpenLayers.i18n('clear'),
                handler: function () {
                    var i = this.itemSelector;
                    i.reset.call(i);
                    this.generateXML();
                },
                scope: this
            }]
        });
        
        // enable the validate button only if there are selected keywords
        this.itemSelector.on({
            'change': function (component) {
                //Ext.getCmp('keywordSearchValidateButton').setDisabled(component.toStore.getCount() < 1);
            }
        });
        
        return this.itemSelector;
    },
    generateTransformationSelector: function () {
        var radios = [], self = this;
        if (this.transformations.length === 1) {
            this.transformation = this.transformations[0];
        } else if (this.transformations.length > 1) {
            Ext.each(this.transformations, function (item, idx) {
                radios.push(new Ext.form.Radio({
                    checked: (item === self.transformation ? true : false), // Check current transformation if defined
                    boxLabel: OpenLayers.i18n(item),
                    name: self.thesaurusIdentifier + '_transformation', // FIXME : if more than one block with same thesaurus
                    inputValue: item
                }));
            });
                
            return new Ext.form.RadioGroup({
                fieldLabel: 'Auto Layout',
    //            autoHeight: true,
    //            autoWidth: true,
                height: 100,
                width: 300,
                items: radios,
                listeners: {
                    change: function (group, checked) {
                        this.transformation = checked.getGroupValue();
                        this.generateXML();
                    },
                    scope: self
                }
            });
        } else {
            console.log('No transformation defined. Using default transformation');
        }
    },
    
    /**
     * Build XML snippet according to configuration
     */
    generateXML: function () {
        var xml = "", ids = [], self = this, 
            serviceUrl = this.catalogue.services.getKeyword, // FIXME : depends on app
            transfo = (this.transformation ? "&transformation=" + this.transformation : "");
        
        // Encode "#" as "%23"
        self.selectedKeywordStore.each(function (item) {
            ids.push(item.id.replace("#", "%23"));
        });
        
        var url = serviceUrl + 
                            '?thesaurus=' + this.thesaurusIdentifier + 
                            '&id=' + ids.join(',') +
                            '&multiple=' + (ids.length > 1 ? true : false) +
                            transfo;
        
        // Call transformation service
        Ext.Ajax.request({
            url: url,
            method: 'GET',
            scope: this,
            success: function (response) {
                // Populate formField
                document.getElementById(this.xmlField).value = response.responseText;
            }
        });
    },
    /**
     * Get combo box for limiting the number of results
     */
    getLimitInput: function () {
        this.nbResultsField = new Ext.form.TextField({
            name: 'maxResults',
            value: '50',
            width: 40
        });
        return this.nbResultsField;
    },
    /**
     * Create thesaurus store and thesaurus selector
     */
    getThesaurusSelector: function () {
        var self = this;
        // TODO : load registered thesaurus for this element
        // TODO : the list of thesaurus could be shared for all widgets, but load events may be overlapping ?
        this.thesaurusStore = new GeoNetwork.data.ThesaurusStore({
            url: this.catalogue.services.getThesaurus,
// Display all options if searching across all thesaurus is required
//            allOption: false,
            activatedOnly: true, 
            listeners: {
                load: function (store, records, options) {
                    // Check that requested thesaurus is available
                    var thesaurus = store.query('id', self.thesaurusIdentifier);
                    if (thesaurus.getCount() === 1) {
                        self.thesaurusSelector.setValue(thesaurus.get(0).get('id'));
                        self.setThesaurusInfo(thesaurus.get(0));
                        self.thesaurusSelector.fireEvent('select');
                    } else {
                        console.log('Error: thesaurus not found in catalog.');
                    }
                }
            }
        });
        
        
        this.thesaurusSelector = new Ext.form.ComboBox({
            width: 150,
//            value: this.thesaurusIdentifier,
            store: this.thesaurusStore,
            triggerAction: 'all',
            mode: 'remote',
            // Hide thesaurus selector if one thesaurus is provided
            hidden: this.thesaurusIdentifier ? true : false,
            displayField: 'title',
            valueField: 'id',
            listWidth: 250,
            listeners: {
                select: function (combo, record, index) {
                    this.keywordStore.removeAll();
                    this.keywordStore.baseParams.pThesauri = self.thesaurusSelector.getValue();
                    if (this.nbResultsField !== null) {
                        this.keywordStore.baseParams.maxResults = this.nbResultsField.getValue();
                    }
                    
                    // Once a thesaurus is selected, search or load
                    // all keywords
                    this.keywordStore.reload();
                    
                    // TODO : only if no search fields are provided
                },
                scope: this
            }
        });
        
        return this.thesaurusSelector;
    },
    setThesaurusInfo: function (record) {
        this.thesaurusInfoTpl.overwrite(this.infoField.body, record.data);
    },
    /**
     * Create keyword store and selector
     */
    initKeywordStore: function () {
        var self = this;
        
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
            }, this.KeywordRecord),
            fields: ["value", "thesaurus", "uri"],
            sortInfo: {
                field: "thesaurus"
            },
            listeners: {
                exception: function (misc) {
                    console.log(misc);
                },
                'beforeload': function (store, options) {
                    if (this.nbResultsField !== null) {
                        this.keywordStore.baseParams.maxResults = this.nbResultsField.getValue();
                    }
                },
                load: function (store, records, options) {
                },
                scope: this
            }
        });
        
        // One store for current selection
        this.selectedKeywordStore = new Ext.data.Store({
            reader: new Ext.data.XmlReader({
                record: 'keyword',
                id: 'uri'
            }, this.KeywordRecord),
            fields: ["value", "thesaurus", "uri"],
            listeners: {
                load: function () {
                    this.generateXML();
                },
                add: function () {
                    this.generateXML();
                },
                remove: function () {
                    this.generateXML();
                },
                scope: this
            }
        });
        
        
        // Check if current keywords are available in the thesaurus
        // Search by name to get list of identifiers.
        Ext.each(this.initialKeyword, function (item) {
            // Search for that keyword and add it to the current selection if found
            if (item !== "") {
                this.keywordSearch(this.thesaurusIdentifier, item);
            }
        }, this);
    },
    /**
     * Method: retrieveKeywordData
     *
     * Load keyword data,
     */
    retrieveKeywordData: function (url) {

    },
    keywordSearch: function (thesaurus, value, cb) {
        // Call transformation service
        Ext.Ajax.request({
            url: this.catalogue.services.searchKeyword,
            method: 'POST', 
            params: {
                pNewSearch: true,
                pTypeSearch: 0, // Exact match
                pMode: 'searchBox',
                pKeyword: value,
                pThesauri: thesaurus
            },
            scope: this,
            success: function (response) {
                this.selectedKeywordStore.loadData(response.responseXML, true);
                // TODO : if current keyword is not found - avoid error
            }
        });
    },
    initComponent: function () {
        Ext.applyIf(this, this.defaultConfig);
        
        this.thesaurusIdentifier = this.thesaurus.replace('geonetwork.thesaurus.', '');
        this.initKeywordStore();
        
        GeoNetwork.editor.ConceptSelectionPanel.superclass.initComponent.call(this);
        
        this.infoField = new Ext.Panel({
                html: '&nbsp;',
                border: false
            });
        var fields = [this.infoField];
        
        // Add thesaurus choice
        fields.push(this.getThesaurusSelector());
        
        
        // Add keyword selection
        if (this.mode === null) {
            //fields.push(this.generateSimpleCombo());
            //fields.push(this.generateSelectionList());
            fields.push(this.getKeywordsItemSelector());
        }
        //this.add(fields);
        
        // Choose transformation field
        var r = this.generateTransformationSelector();
        fields.push(r);
        
        var p = new Ext.Panel({
            layout: 'vbox',
            border: false,
            height: 300,    // TODO : autoHeight ?
            items: fields
        });
        this.add(p);
    }
});

GeoNetwork.editor.ConceptSelectionPanel.init = function () {
    var thesaurusPickers = Ext.DomQuery.select('.thesaurusPickerCfg');
    
    for (var idx = 0; idx < thesaurusPickers.length; ++idx) {
        var thesaurusPicker = thesaurusPickers[idx];
        if (thesaurusPicker !== null) {
            var id = thesaurusPicker.getAttribute("id"), 
                config = thesaurusPicker.getAttribute("config"),
                jsonConfig = Ext.decode(config);
            
            console.log(jsonConfig);
            var panel = new GeoNetwork.editor.ConceptSelectionPanel({
                catalogue: catalogue,
                thesaurus: jsonConfig.thesaurus,
                initialKeyword: jsonConfig.keywords,
                transformations: jsonConfig.transformations,
                transformation: jsonConfig.transformation,
                xmlField: id + '_xml',
                renderTo: id + '_panel'
            });
        }
    }
};

/** api: xtype = gn_editor_conceptselectionpanel */
Ext.reg('gn_editor_conceptselectionpanel', GeoNetwork.editor.ConceptSelectionPanel);