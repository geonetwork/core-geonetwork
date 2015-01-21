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
 *     using a thesaurus loaded in GeoNetwork.
 *     
 *     Keyword selection could be handle with different mode:
 *     
 *     - combo to use a simple combo box
 *     
 *     - list to use a selection list (limited to one item)
 *     
 *     - multiplelist to use a multiple selection list
 *     
 *     - (default) an item selector.
 *
 *  TODO: Multilingual keyword support and thesaurus search language
 */
GeoNetwork.editor.ConceptSelectionPanel = Ext.extend(Ext.Panel, {
    defaultConfig: {
        /** api: config[defaultThesaurus] 
         *  ``String`` thesaurus identifier to select by default (null by default).
         *  If defined, the thesaurus selector is not displayed.
         */
        defaultThesaurus: null,
        /** api: config[lang] 
         *  ``String`` search language (default is GUI language)
         *  
         *  TODO : add support for changing search language
         */
        lang: null,
        /** api: config[searchOnThesaurusSelection] 
         *  ``String`` trigger search when thesaurus change. Only for default mode. 
         *  if combo or list mode use, the list of keyword is loaded automatically.
         */
        searchOnThesaurusSelection: false,
        /** api: config[mode] 
         *  ``String`` The widget mode:
         *     
         *     - combo to use a simple combo box
         *     
         *     - list to use a selection list (limited to one item)
         *     
         *     - multiplelist to use a multiple selection list
         *     
         *     - (default) an item selector.
         */
        mode: null,
        /** api: config[maxKeywords] 
         *  ``Integer`` The maximum number of item to load if widget mode is combo or list.
         *  In default mode, the maximum number of results could be changed using the input field.
         */
        maxKeywords: 100,
        /** api: config[border] 
         *  ``boolean`` false by default.
         */
        border: false,
        /** api: config[layout] 
         *  ``String`` auto by default. Delegate rendering to the browser.
         */
        layout: 'auto',
        /** api: config[autoHeight] 
         *  ``boolean`` true by default.
         */
        autoHeight: true,
        /** api: config[itemSelectorHeight] 
         *  ``Integer`` The height of the item selector.
         */
        itemSelectorHeight: 250,
        /** api: config[itemSelectorWidth] 
         *  ``Integer`` The width of the item selector.
         */
        itemSelectorWidth: 350,
        loadingMask: null,
        /** api: config[thesaurusInfoTpl] 
         *  ``Ext.XTemplate`` template to use to render thesaurus information in the widget header.
         */
        thesaurusInfoTpl: GeoNetwork.Templates.THESAURUS_HEADER,
        /** api: config[keywordsTpl] 
         *  ``Ext.XTemplate`` template to use to render keyword in data views.
         */
        keywordsTpl: GeoNetwork.Templates.KEYWORD_ITEM,
        /** api: config[renderTo] 
         *  ``String`` Id of the element
         */
        renderTo: null,
        /** api: config[transformations] 
         *  ``Array`` array of transformations (use to override the server configuration).
         *  The list of transformation is used to create radio control on the widget footer
         *  to define which transformation to use when requesting the XML fragment.
         */
        transformations: ['to-iso19139-keyword'],
        /** api: config[transformation] 
         *  ``String`` The default transformation to use. Default is to-iso19139-keyword.
         */
        transformation: 'to-iso19139-keyword',
        /** api: config[thesaurus] 
         *  ``String`` thesaurus to use.
         */
        thesaurus: null,
        /** api: config[initialKeyword] 
         *  ``Array`` A list of initial keywords
         */
        initialKeyword: [],
        /** api: config[identificationMode] 
         *  ``String`` Identify keyword by their label (default) or uri (requires to use gmx:Anchor in the metadata).
         */
        identificationMode: 'value',
        /** api: config[triggerSearch] 
         *  ``Boolean`` Trigger search when initialized (not used for combo and multiple list mode)
         */
        searchOnLoad: false,
        /**
         * relative imagePath for ItemSelector
         */
        imagePath: '../../apps/js/ext-ux/images'
    },
    initialKeywordLoaded: false,
    /** private: property[thesaurusIdentifier] 
     *  ``String`` thesaurus identifier to use.
     */
    thesaurusIdentifier: null,
    /** private: property[thesaurusSelector] 
     *  ``Ext.form.ComboBox`` thesaurus combo box.
     */
    thesaurusSelector: null,
    /** private: property[nbResultsField] 
     *  ``Ext.form.TextField`` number of results field.
     */
    nbResultsField: null,
    initialized: false,
    keywords: null,
    
    
    /** private: property[KeywordRecord] 
     *  ``Ext.data.Record`` A record object for the keyword
     */
    KeywordRecord: Ext.data.Record.create([{
            name: 'value'
        }, {
            name: 'definition'
        }, {
            name: 'thesaurus',
            mapping: 'thesaurus/key'
        }, {
            name: 'uri'
        }]),
    /** private: property[keywordStore] 
     *  ``Ext.data.Store`` Store of keywords in current search
     */
    keywordStore: null,
    /** private: property[selectedKeywordStore] 
     *  ``Ext.data.Store`` Store of selected keywords
     */
    selectedKeywordStore: null,
    loadingKeywordStore: null,
    /** private: method[generateSimpleCombo]
     * 
     *  Create a simple combo box for selection of only one keyword in a thesaurus.
     *  When created the full list of keyword is loaded (with a limit based 
     *  on the maxKeywords property).
     */
    generateSimpleCombo: function () {
        var self = this, 
            initKeyword = (self.initialKeyword.length === 1 && self.initialKeyword[0] !== '') ? self.initialKeyword[0] : null;
        
        var combo = new Ext.form.ComboBox({
            store: this.keywordStore,
            triggerAction: 'all',
            mode: 'remote',
            displayField: 'value',
            valueField: 'uri',
            listeners: {
                select: function (combo, record, index) {
                    this.selectedKeywordStore.removeAll();
                    this.selectedKeywordStore.add([record]);
                },
                scope: this
            }
        });
        
        // Load all keyword for the current thesaurus
        this.keywordStore.baseParams.pThesauri = this.thesaurusIdentifier;
        this.keywordStore.baseParams.maxResults = this.maxKeywords;
        this.keywordStore.on('load', function (store, records) {

            // Custom callback which load response to the selected set
            // and set the combo box value.
            var cb = function (response) {
                self.selectedKeywordStore.loadData(response.responseXML, true);
                combo.setRawValue(initKeyword);
            };

            // Get the first initial keyword in combo mode
            if (initKeyword) {
                self.keywordSearch(self.thesaurusIdentifier, initKeyword, cb);
            }
        });
        this.keywordStore.reload();
        return [combo];
    },
    /** private: method[generateFilterField]
     * 
     *  Create a simple search field linked to the keywordStore. 
     *  When searching the store is updated.
     */
    generateFilterField: function () {
        return new GeoNetwork.form.SearchField({
            width: 200,
            store: this.keywordStore,
            paramName: 'pKeyword'
        });
    },
    /** private: method[generateSelectionList]
     * 
     *  Create a multiple selection list for selecting keywords.
     *  If the mode is multiplelist, the multiple selection is allowed.
     */
    generateSelectionList: function (withFilter) {
        var self = this;
        
        var selectionChangeCb = function (dv, selections) {
            // Wait for all initial keywords to be loaded before activating
            // on selection change event
            if (self.initialKeywordLoaded) {
                this.selectedKeywordStore.removeAll();
                var records = [];
                Ext.each(selections, function (node) {
                    records.push(dv.getStore().getAt(dv.indexOf(node)));
                });
                self.selectedKeywordStore.add(records);
            }
            
            // Reset XML snippet if empty selection
            // Only the thesaurus reference will be part of the snippet
            // to not remove that element.
            if (selections.length === 0) {
                self.generateXML();
            }
        };
        
        // Custom number of max items
        var dv = new Ext.DataView({
            store: this.keywordStore,
            tpl: this.keywordsTpl,
//            autoHeight: true,
            simpleSelect: true,
            multiSelect: this.mode === 'multiplelist' ? true : false,
            singleSelect: true,
            width: this.itemSelectorWidth,
            height: this.itemSelectorHeight,
            selectedClass: 'ux-mselect-selected',
            itemSelector: 'div.ux-mselect-item',
            listeners: {
                // On selection, remove all current selection 
                // and add the selected one
                // to the selected keyword store selection.
                selectionchange: selectionChangeCb,
                afterrender: function () {
                    // Load all keyword for the current thesaurus
                    this.keywordStore.on('load', function () {
                        
                        // Custom callback which load response to the loading area
                        // When the initial keyword set will be loaded in the loading area
                        // the list of keywords will be added to the selected store.
                        var cb = function (response) {
                            self.loadingKeywordStore.loadData(response.responseXML, true);
                        };

                        // Get initial keyword in the data view and select them
                        Ext.each(self.initialKeyword, function (initKeyword) {
                            var filter = self.identificationMode || 'value';
                            dv.select(self.keywordStore.find(filter, initKeyword), true);
                            self.keywordSearch(self.thesaurusIdentifier, initKeyword, cb);
                        });
                    });
                    
                    // Load thesaurus keyword to populate the data view
                    this.keywordStore.baseParams.pThesauri = this.thesaurusIdentifier;
                    this.keywordStore.baseParams.maxResults = this.maxKeywords;
                    this.keywordStore.reload();
                },
                scope: this
            }
        });
        
        // If no keywords in the initial set, flag the component as initialized
        if (this.initialKeyword.length === 0 || this.initialKeyword[0] === '') {
            this.initialKeywordLoaded = true;
        }
        
        if (withFilter) {
            var search = this.generateFilterField();
            return [search, dv];
        } else {
            return [dv];
        }
    },
    /** private: method[getKeywordsItemSelector]
     * 
     *  Create a multiple item selector.
     */
    getKeywordsItemSelector: function (withFilter) {
        var self = this;
        this.itemSelector = new Ext.ux.ItemSelector({
            name: "itemselector",
            fieldLabel: "ItemSelector",
            dataFields: ["value", "thesaurus"],
            //toData: [],
            toStore: this.selectedKeywordStore,
            msWidth: 350,
            msHeight: 260,
            valueField: "value",
            toSortField: undefined,
            fromTpl: this.keywordsTpl,
            toTpl: this.keywordsTpl,
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
            imagePath: this.imagePath,
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
            }],
            listeners: {
                render: function () {
                    this.toMultiselect.view.on('selectionchange', function () {
                        self.generateXML();
                    });
                    this.on('changeend', function () {
                        self.generateXML();
                    });
                    this.toMultiselect.view.on('dropend', function () {
                        self.generateXML();
                    });
                    
                    if (self.searchOnLoad) {
                    	self.keywordStore.baseParams.pThesauri = self.thesaurusIdentifier;
                    	self.keywordStore.reload();
                    }
                }
            }
        });
       
        
        // Custom callback which load response to the loading area
        // When the initial keyword set will be loaded in the loading area
        // the list of keywords will be added to the selected store.
        var cb = function (response) {
            this.loadingKeywordStore.loadData(response.responseXML, true);
        };
        
        // Get initial keyword in the data view and select them
        Ext.each(this.initialKeyword, function (initKeyword) {
            this.keywordSearch(this.thesaurusIdentifier, initKeyword, cb);
        }, this);
        
        
        return this.itemSelector;
    },
    /** private: method[generateTransformationSelector]
     *  
     *  Create a group of radio for each transformation mode.
     */
    generateTransformationSelector: function () {
        var radios = [], self = this;
        if (this.transformations.length === 1) {
            this.transformation = this.transformations[0];
            return null;
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
                autoHeight: true,
                width: 350,
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
    /** private: method[generateXML]
     *  
     *  Build XML fragment according to configuration and popuplate the hidden
     *  textarea which contains the XML fragment.
     */
    generateXML: function (cb) {
        var xml = "", ids = [], self = this, 
            serviceUrl = this.catalogue.services.getKeyword,
            transfo = (this.transformation ? "&transformation=" + this.transformation : "");
//        if (!this.loadingMask) {
//            this.loadingMask = new Ext.LoadMask(this.getEl(), {
//                msg: OpenLayers.i18n('searching')
//            });
//        }
//        this.loadingMask.show();
        
        // Encode "#" as "%23"
        self.selectedKeywordStore.each(function (item) {
            ids.push(item.get('uri').replace("#", "%23"));
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
            async: false,
            success: cb || function (response) {
                // Populate formField
                if (response.responseText === '<?xml version="1.0" encoding="UTF-8"?>') {
                    console.log('Empty response returned from ' + url);
                } else {
                    document.getElementById(this.xmlField).value = response.responseText;
                }
//                this.loadingMask.hide();
            }
            // TODO : Error
        });
    },
    /** private: method[getLimitInput]
     *  
     *  Create combo box for limiting the number of results. Default value is 50.
     *  
     *  TODO : only allow integer
     */
    getLimitInput: function () {
        this.nbResultsField = new Ext.form.TextField({
            name: 'maxResults',
            value: this.maxKeywords || 50,
            width: 40
        });
        
        return this.nbResultsField;
    },
    /** private: method[getThesaurusSelector]
     *  
     *  Create thesaurus store and thesaurus selector.
     */
    getThesaurusSelector: function () {
        var self = this;
        // TODO : cache the list of thesaurus to share it for all widgets
        this.thesaurusStore = new GeoNetwork.data.ThesaurusStore({
            url: this.catalogue.services.getThesaurus,
            // Display all options if searching across all thesaurus is required
            // Not available in this widget
            // allOption: false,
            activatedOnly: true, 
            listeners: {
                load: function (store, records, options) {
                    // Check that requested thesaurus is available
                    var thesaurus = store.query('id', new RegExp("^" + self.thesaurusIdentifier + "$"));
                    if (thesaurus.getCount() === 1) {
                        self.thesaurusSelector.setValue(thesaurus.get(0).get('id'));
                        self.setThesaurusInfo(thesaurus.get(0));
                        self.thesaurusSelector.fireEvent('select');
                    } else {
                        // TODO : improve alert
                        console.log('Error: thesaurus ' + self.thesaurusIdentifier + ' not found in catalog.');
                    }
                }
            }
        });
        
        this.thesaurusSelector = new Ext.form.ComboBox({
            width: 150,
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

//                    // Clean current content and init thesaurus store parameter
                    this.keywordStore.baseParams.pThesauri = self.thesaurusSelector.getValue();
                    if (this.nbResultsField !== null) {
                        this.keywordStore.baseParams.maxResults = this.nbResultsField.getValue();
                    }
                },
                scope: this
            }
        });
        
        return this.thesaurusSelector;
    },
    /** private: method[setThesaurusInfo]
     *  
     *  Update thesaurus information in widget header.
     */
    setThesaurusInfo: function (record) {
        this.thesaurusInfoTpl.overwrite(this.infoField.body, record.data);
    },
    /** private: method[initKeywordStore]
     *  
     *  Create keyword store and selected keyword store
     */
    initKeywordStore: function () {
        var self = this;
        
        
        // Define which field to use as identifier.
        // As far as the keyword label is stored in the metadata
        // record, the identifier should be the value.
        // Using URI mode is safer to deal with duplicates
        // like "photographie aérienne" in GEMET which match 2 concepts
        // http://www.eionet.europa.eu/gemet/search?langcode=fr&query=photographie
        // In that case, using value mode, only one concept will be displayed.
        var idProp = (this.identificationMode === 'uri') ? 'uri' : 'value';
        
        // Main keyword store which contains all or part of
        // thesaurus keyword. If link to a filter, only part
        // of the thesaurus is loaded.
        this.keywordStore = new Ext.data.Store({
            proxy: new Ext.data.HttpProxy({
                url: this.catalogue.services.searchKeyword,
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
                id: idProp
            }, this.KeywordRecord),
            fields: ["value", "thesaurus", "uri"],
            sortInfo: {
                field: "thesaurus"
            },
            listeners: {
                exception: function (misc) {
                    // TODO : improve error
                    console.log(misc);
                },
                'beforeload': function (store, options) {
                    if (this.nbResultsField !== null) {
                        this.keywordStore.baseParams.maxResults = this.nbResultsField.getValue();
                    }
                },
                scope: this
            }
        });
        
        
        
        // One store for current selected keyword
        // When a keyword is added or removed, a XML
        // snippet corresponding to the selection is asked to 
        // the server
        
        
        this.selectedKeywordStore = new Ext.data.Store({
            reader: new Ext.data.XmlReader({
                record: 'keyword',
                id: idProp
            }, this.KeywordRecord),
            fields: ["value", "thesaurus", "uri"]
        });
        
        if (this.mode !== '') {
            // Callback function when a keyword is added to 
            // the current selection. For Multi item selector mode
            // the XML snippet is generated when the content change
            var cb = function () {
                this.generateXML();
            };
            
            this.selectedKeywordStore.on('add', cb, this);
            this.selectedKeywordStore.on('remove', cb, this);
        }
        
        
        this.loadingKeywordStore = new Ext.data.Store({
            reader: new Ext.data.XmlReader({
                record: 'keyword',
                id: idProp
            }, this.KeywordRecord),
            fields: ["value", "thesaurus", "uri"],
            listeners: {
                load: function () {
//                    console.log(' :: loading store contains: ' + this.loadingKeywordStore.getCount() + "/" + this.initialKeyword.length);
                    if (this.loadingKeywordStore.getCount() ===  this.initialKeyword.length) {
                        // Transfert record from the temporary selection
                        // to the selected store
                        this.selectedKeywordStore.removeAll();
                        var records = [];
                        this.loadingKeywordStore.each(function (record) {
                            records.push(record);
                        });
                        this.selectedKeywordStore.add(records);
                        this.loadingKeywordStore.removeAll();
                        
                        this.initialKeywordLoaded = true;
                    }
                },
                scope: this
            }
        });
    },
    /** private: method[keywordSearch]
     *  
     *  Search for the keyword by exact match on the name
     *  to populate the selected keyword store.
     */
    keywordSearch: function (thesaurus, value, cb) {
        // Do not search for all - only exact match on keyword name are made
        if (value === "") {
            return;
        }
        
        var params = {
            pNewSearch: true,
            pTypeSearch: 2, // Exact match
            pMode: 'searchBox',
            pThesauri: thesaurus
        };
        
        if (this.identificationMode === 'uri') {
            params.pUri = value;
        } else {
            params.pKeyword = value;
        }
        
        // Call transformation service
        Ext.Ajax.request({
            url: this.catalogue.services.searchKeyword,
            method: 'GET', 
            params: params,
            scope: this,
//            async: false,
            success: cb || function (response) {
                // Do not load keyword if none define
                this.selectedKeywordStore.loadData(response.responseXML, true);
                // TODO : if current keyword is not found - avoid error
            }
        });
    },
    /** private: method[constructor]
     *  Initializes the concept selection panel
     *
     */
    initComponent: function () {
        Ext.applyIf(this, this.defaultConfig);
        
        this.thesaurusIdentifier = this.thesaurus.replace('geonetwork.thesaurus.', '');
        this.initKeywordStore();
        
        
        // Add the header with thesaurus name information
        this.infoField = new Ext.Panel({
                html: '&nbsp;',
                border: false
            });
        var fields = [this.infoField];
        
        
        // Add thesaurus choice
        fields.push(this.getThesaurusSelector());
        
        
        // Add keyword selection
        // * in combobox mode
        if (this.mode === 'combo') {
            fields.push(this.generateSimpleCombo());
        } else if (this.mode === 'list' || this.mode === 'multiplelist' || this.mode === 'multiplelist_with_filter') {
            fields.push(this.generateSelectionList(this.mode.indexOf('with_filter') !== -1));
        } else {
            // * Default is an item selector with 2 lists
            fields.push(this.getKeywordsItemSelector());
        }
        
        
        // Choose transformation field
        var r = this.generateTransformationSelector();
        if (r !== null) {
            fields.push(r);
        }
        
        this.items = fields;
        
        GeoNetwork.editor.ConceptSelectionPanel.superclass.initComponent.call(this);
    }
});

/** api: method[GeoNetwork.editor.ConceptSelectionPanel.init]
 *  
 *  Search for div element with class thesaurusPickerCfg 
 *  in order to initialize the concept selection widget.
 *  
 *  .. code-block:: javascript
 *  
 *    <div class="thesaurusPickerCfg" id="thesaurusPicker_444" 
 *       config="{{mode: '', thesaurus:'', keywords: [''], 
 *       transformations: [], 
 *       transformation: ''}}"/>
 */
GeoNetwork.editor.ConceptSelectionPanel.init = function (cfg) {
    var thesaurusPickers = Ext.DomQuery.select('.thesaurusPickerCfg');
    
    for (var idx = 0; idx < thesaurusPickers.length; ++idx) {
        var thesaurusPicker = thesaurusPickers[idx];
        if (thesaurusPicker !== null) {
            var id = thesaurusPicker.getAttribute("id"), 
                config = thesaurusPicker.getAttribute("config"),
                jsonConfig = Ext.decode(config);
            var p = Ext.get(id + '_panel');
            if (p.dom.innerHTML === '') {
                var panel = new GeoNetwork.editor.ConceptSelectionPanel({
                    catalogue: catalogue,
                    thesaurus: jsonConfig.thesaurus,
                    mode: jsonConfig.mode,
                    initialKeyword: jsonConfig.keywords,
                    imagePath: cfg.imagePath,
                    maxKeywords: jsonConfig.maxKeywords,
                    searchOnLoad: jsonConfig.searchOnLoad == 'true',
                    transformations: jsonConfig.transformations,
                    transformation: jsonConfig.transformation,
                    identificationMode: jsonConfig.identificationMode,
                    xmlField: id + '_xml',
                    renderTo: id + '_panel',
                    itemSelectorWidth: jsonConfig.itemSelectorWidth,
                    itemSelectorHeight: jsonConfig.itemSelectorHeight
                });
            }
        }
    }
};


/** api: method[GeoNetwork.editor.ConceptSelectionPanel.initThesaurusSelector]
 *  
 *  Create a thesaurus selection menu which when clicking on a 
 *  thesaurus build a XML fragment, add a form element with the fragment
 *  and trigger the save action of the editor.
 */
GeoNetwork.editor.ConceptSelectionPanel.initThesaurusSelector = function (ref, type, formBt) {
    var tagName = 'gmd:descriptiveKeywords', editorPanel = Ext.getCmp('editorPanel');
    
    // Get the list of thesaurus
    var thesaurusStore = new GeoNetwork.data.ThesaurusStore({
        // Only retrieve thesaurus for this type of element (and for this metadata schema)
        url: catalogue.services.getThesaurus + "?element=" + tagName + "&schema=" + editorPanel.metadataSchema,
        activatedOnly: true,
        listeners: {
            load: function (store, records, options) {
                
                store.sort('title', 'ASC');
                
                var items = [{
                    xtype: 'menutextitem',
                    text: OpenLayers.i18n('addFromThesaurus')
                }];
                store.each(function (thesaurus) {
                    items.push({
                        text: thesaurus.get('title'),
                        handler: function () {
                            // Get an XML fragment to add the thesaurus to the current record
                            var url = catalogue.services.getKeyword + 
                                                '?thesaurus=' + thesaurus.get('id') + 
                                                '&id=' + 
                                                '&multiple=false' +
                                                '&transformation=to-iso19139-keyword';
                            
                            Ext.Ajax.request({
                                url: url,
                                method: 'GET',
                                scope: this,
                                success: function (response) {
                                    // Add the fragment and save the metadata
                                    var keywords = [response.responseText];
                                    GeoNetwork.editor.EditorTools.addHiddenFormFieldForFragment({ref: ref, name: type}, keywords, editorPanel);
                                }
                            });
                        }
                    });
                });
                
                // Display the floating menu
                var contextMenu = new Ext.menu.Menu({
                    floating: true,
                    items: items
                });
                
                // Add the contextual menu to the binocular control
                // Keep the current control as far as the old ThesaurusSelection is not deprecated.
                // TODO improve element control by using only the + control
                var binocular = Ext.get(formBt);
                contextMenu.showAt([binocular.getX(), binocular.getY() + binocular.getHeight()]);
            }
        }
    });
};

/** api: xtype = gn_editor_conceptselectionpanel */
Ext.reg('gn_editor_conceptselectionpanel', GeoNetwork.editor.ConceptSelectionPanel);
