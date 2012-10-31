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
        /** api: config[thesaurusInfoTpl] 
         *  ``Ext.XTemplate`` template to use to render thesaurus information in the widget header.
         */
        thesaurusInfoTpl: new Ext.XTemplate(
                '<tpl for=".">',
                    '<div class="thesaurusInfo"><span class="title">{title}</span><span class="theme">{theme}</span><span class="filename">({filename})</span></div>',
                '</tpl>'
        ),
        /** api: config[keywordsTpl] 
         *  ``Ext.XTemplate`` template to use to render keyword in data views.
         */
        keywordsTpl: new Ext.XTemplate(
            '<tpl for=".">',
                // TODO : add keyword definiton ?
                '<div class="ux-mselect-item">{value}</div>',
            '</tpl>'
        ),
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
        initialKeyword: []
    },
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

    keywords: null,
    /** private: property[KeywordRecord] 
     *  ``Ext.data.Record`` A record object for the keyword
     */
    KeywordRecord: Ext.data.Record.create([{
            name: 'value'
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
            mode: 'local',
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
        this.keywordStore.reload();
        this.keywordStore.on('load', function () {
            
            // Custom callback which load response to the selected set
            // and set the combo box value.
            var cb = function (response) {
                self.selectedKeywordStore.loadData(response.responseXML, true);
                combo.setValue(initKeyword);
            };

            // Get the first initial keyword in combo mode
            if (initKeyword) {
                self.keywordSearch(self.thesaurusIdentifier, initKeyword, cb);
            }
        });
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
        var search = this.generateFilterField(), self = this;
        
        // Custom number of max items
        var dv = new Ext.DataView({
            store: this.keywordStore,
            tpl: this.keywordsTpl,
            //autoHeight: true,
            simpleSelect: true,
            multiSelect: this.mode === 'multiplelist' ? true : false,
            singleSelect: true,
            width: 350,
            selectedClass: 'ux-mselect-selected',
            itemSelector: 'div.ux-mselect-item',
            listeners: {
                // On selection, remove all current selection 
                // and add the selected one
                // to the selected keyword store selection.
                selectionchange: function (dv, selections) {
                    this.selectedKeywordStore.removeAll();
                    var records = [];
                    Ext.each(selections, function (node) {
                        records.push(dv.getStore().getAt(dv.indexOf(node)));
                    });
                    self.selectedKeywordStore.add(records);
                },
                scope: this
            }
        });
        
        // Load all keyword for the current thesaurus
        this.keywordStore.baseParams.pThesauri = this.thesaurusIdentifier;
        this.keywordStore.baseParams.maxResults = this.maxKeywords;
        this.keywordStore.on('load', function () {
            
            // Custom callback which load response to the selected set
            // and set the dataview values.
            var cb = function (response) {
                self.selectedKeywordStore.loadData(response.responseXML, true);
            };

            // Get initial keyword in the data view and select them
            Ext.each(self.initialKeyword, function (initKeyword) {
                dv.select(self.keywordStore.find('value', initKeyword), true);
                self.keywordSearch(self.thesaurusIdentifier, initKeyword, cb);
            });
        });
        this.keywordStore.reload();

        return [search, dv];
    },
    /** private: method[getKeywordsItemSelector]
     * 
     *  Create a multiple item selector.
     */
    getKeywordsItemSelector: function (withFilter) {
        
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
            success: cb || function (response) {
                // Populate formField
                document.getElementById(this.xmlField).value = response.responseText;
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
            value: '50',
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
                    var thesaurus = store.query('id', self.thesaurusIdentifier);
                    if (thesaurus.getCount() === 1) {
                        self.thesaurusSelector.setValue(thesaurus.get(0).get('id'));
                        self.setThesaurusInfo(thesaurus.get(0));
                        self.thesaurusSelector.fireEvent('select');
                    } else {
                        // TODO : improve alert
                        console.log('Error: thesaurus not found in catalog.');
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
                    // Clean current content and init thesaurus store parameter
                    this.keywordStore.removeAll();
                    this.keywordStore.baseParams.pThesauri = self.thesaurusSelector.getValue();
                    if (this.nbResultsField !== null) {
                        this.keywordStore.baseParams.maxResults = this.nbResultsField.getValue();
                    }
                    
                    // Once a thesaurus is selected, search or load
                    // all keywords
                    if (this.searchOnThesaurusSelection) {
                        this.keywordStore.reload();
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
                id: 'uri'
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
        
        var cb = function () {
            this.generateXML();
        };
        
        // One store for current selection
        this.selectedKeywordStore = new Ext.data.Store({
            reader: new Ext.data.XmlReader({
                record: 'keyword',
                id: 'uri'
            }, this.KeywordRecord),
            fields: ["value", "thesaurus", "uri"],
            listeners: {
                load: cb,
                add: cb,
                remove: cb,
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
    /** private: method[keywordSearch]
     *  
     *  Search for the keyword by exact match on the name
     *  to populate the selected keyword store.
     */
    keywordSearch: function (thesaurus, value, cb) {
        // Call transformation service
        Ext.Ajax.request({
            url: this.catalogue.services.searchKeyword,
            method: 'POST', 
            params: {
                pNewSearch: true,
                pTypeSearch: 2, // Exact match
                pMode: 'searchBox',
                pKeyword: value,
                pThesauri: thesaurus
            },
            scope: this,
            success: cb || function (response) {
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
            //fields.push(this.generateSelectionList());
        } else if (this.mode === 'list' || this.mode === 'multiplelist') {
            fields.push(this.generateSelectionList());
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
GeoNetwork.editor.ConceptSelectionPanel.init = function () {
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
                    transformations: jsonConfig.transformations,
                    transformation: jsonConfig.transformation,
                    xmlField: id + '_xml',
                    renderTo: id + '_panel'
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
    // Get the list of thesaurus
    var thesaurusStore = new GeoNetwork.data.ThesaurusStore({
        url: catalogue.services.getThesaurus,
        activatedOnly: true,
        listeners: {
            load: function (store, records, options) {
                
                store.sort('title');
                
                var items = [{
                    xtype: 'menutextitem',
                    text: 'Add from thesaurus ...'
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
                                    var keywords = ["<gmd:descriptiveKeywords xmlns:gmd='http://www.isotc211.org/2005/gmd'>" +
                                                     response.responseText + "</gmd:descriptiveKeywords>"];
                                    GeoNetwork.editor.EditorTools.addHiddenFormFieldForFragment({ref: ref, name: type}, keywords, Ext.getCmp('editorPanel'));
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