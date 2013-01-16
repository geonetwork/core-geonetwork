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
Ext.namespace('GeoNetwork.admin');

/**
 * @require Catalogue.js
 */
/** api: (define)
 *  module = GeoNetwork.admin
 *  class = ThesaurusManagerPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor 
 *  .. class:: ThesaurusManagerPanel(config)
 *
 *     Create a GeoNetwork thesaurus manager panel (alpha)
 *     to add, delete, update and select available thesauri.
 *
 */
GeoNetwork.admin.ThesaurusManagerPanel = Ext.extend(Ext.Panel, {
    defaultConfig: {
        feed: null,
        autoWidth : true,
        layout : 'border',
        maxResults: 50,
        defaultThesaurusType: 'theme'
    },
    border: false,
    frame: false,
    selectedThesaurus: undefined,
    layout: 'border',
    catalogue: undefined,
    toolbar: undefined,
    thesaurusGrid: undefined,
    thesaurusStore: undefined,
    keywordGrid: undefined,
    createEmptyThesaurusWindow: undefined,
    createThesaurusFromLocalFileWindow: undefined,
    createThesaurusFromRemoteFileWindow: undefined,
    createEmptyThesaurusForm: undefined,
    createThesaurusFromLocalFileForm: undefined,
    createThesaurusFromRemoteFileForm: undefined,

    
    /** private: method[initComponent] 
     *  Initializes the thesaurus manager panel.
     *  
     *  TODO : Add a refresh action (after import)
     *  TODO : init type of directory by URL parameter
     */
    initComponent: function(){
        Ext.applyIf(this, this.defaultConfig);
        
        // Thesaurus store
        // A store with the list of available thesauri
        this.thesaurusStore = new GeoNetwork.data.ThesaurusStore({
            url: this.catalogue.services.getThesaurus});

        var Keyword = Ext.data.Record.create([{
            name: 'id'
        }, {
            name: 'value'
        }, {
            name: 'thesaurus'
        }, {
            name: 'definition'
        }, {
            name: 'uri'
        }, {
            name: 'east'
        }, {
            name: 'west'
        }, {
            name: 'south'
        }, {
            name: 'north'
        }]);
        
        // Keyword store
        this.keywordStore = new Ext.data.Store({
            proxy: new Ext.data.HttpProxy({
                url: this.catalogue.services.searchKeyword, // FIXME : global var
                method: 'GET'
            }),
            baseParams: {
                pNewSearch: true,
                pTypeSearch: 1,
                pThesauri: '',
                pMode: 'searchBox',
                maxResults: this.maxResults
            },
            reader: new Ext.data.XmlReader({
                record: 'keyword',
                id: 'id'
            }, Keyword),
            fields: ["id", "value", "definition", "thesaurus", "uri", "east", "west", "south", "north"],
            sortInfo: {
                field: "value"
            }
        });

        GeoNetwork.admin.ThesaurusManagerPanel.superclass.initComponent.call(this);
        
        this.initThesaurusView();
        this.initKeywordView();
        
        // Build the layout
        var thesaurusView = {
            region: 'west',
            split: true,
            autoScroll: true,
            minWidth: 280,
            width: 400,
            items: [
                this.thesaurusGrid
            ]
        };
        var keywordView = {
            region: 'center',
            split: true,
            autoScroll: true,
            minHeigth: 400,
            items: [
                this.keywordGrid
            ]
        };
        
        this.add(thesaurusView);
        this.add(keywordView);
                
    },

    /**
     * Set of components to display, add, delete thesauri.
     * Thesauri can be selected (via the checkbox column) in order to be available for users
     */
    initThesaurusView: function() {
        var panel = this;
                
        var ynValues = [['y'], ['n']];
        
        var thColumnModel = new Ext.grid.ColumnModel({
            // specify any defaults for each column
            defaults: {
                sortable: true // columns are not sortable by default           
            },
            columns: [{
                id: 'title',
                header: OpenLayers.i18n('ThesaurusName'),
                dataIndex: 'title',
                editable: false
            }, {
                id: 'filename',
                header: OpenLayers.i18n('id'),
                dataIndex: 'filename',
                editable: false
            }, {
                id: 'theme',
                header: OpenLayers.i18n('Theme'),
                dataIndex: 'theme',
                editable: false
            }, {
              id: 'type',
              header: OpenLayers.i18n('Type'),
              dataIndex: 'type',
              editable: false
            }, {
                id: 'activated',
                header: OpenLayers.i18n('Activated'),
                dataIndex: 'activated',
                width: 50,
                // use shorthand alias defined above
                editor: new Ext.form.ComboBox({
                    mode: 'local',
                    editable: false,
                    typeAhead: true,
                    triggerAction: 'all',
                    lazyRender: true,
                    allowBlank: false,
                    listClass: 'x-combo-list-small',
                    store: new Ext.data.ArrayStore({
                        id: 0,
                        fields: ['id'],
                        data: ynValues
                    }),
                    valueField: 'id',
                    displayField: 'id'
                })
            }]
        });


        // Thesaurus grid
        this.thesaurusGrid = new Ext.grid.EditorGridPanel({
            layout: 'fit',
            autoHeight: true,
            border: false,
            store: this.thesaurusStore,
            cm: thColumnModel,
            autoExpandColumn: 'title',
            clicksToEdit: 1,
            listeners: {
                scope: this,
                afteredit: function(e) {
                    Ext.Ajax.request({
                            scope: this,
                            url: this.catalogue.services.thesaurusActivate + "?ref=" + e.record.data.id + "&activated=" + e.record.data.activated,
                            failure: function() {
                              // TODO: display a message describing the reason of the error
                              Ext.Msg.alert('Fail');
                            }
                        });
                },
                dblclick : function(e){
                    this.keywordStore.setBaseParam('pKeyword', '*');
                    this.keywordStore.reload();
                }
            },
            sm: new Ext.grid.RowSelectionModel({
                singleSelect: true,
                listeners: {
                    scope: this,
                    selectionchange: function(smObj) {
                        this.updateKeywordGridAndToolbar();
                        this.updateThesaurusToolbar();
                        this.updateKeywordToolbar();
                    }
                }
            }),
            tbar: {
                disabled: false,
                items: [{
                    text: OpenLayers.i18n('add'),
                    iconCls: 'addIcon',
                    menu: {
                        xtype: 'menu',
                        plain: true,
                        items: [{
                                    text: OpenLayers.i18n('emptyThesaurus'),
                                    scope: this,
                                    handler: function() {
                                        if(!this.createEmptyThesaurusWindow){
                                            this.createEmptyThesaurusWindow = this.getCreateEmptyThesaurusWindow();
                                        }
                                        this.createEmptyThesaurusWindow.show(this);
                                    }
                                }, {
                                    text: OpenLayers.i18n('thesaurusFromFile'),
                                    scope: this,
                                    handler: function() {
                                        if(!this.createThesaurusFromLocalFileWindow){
                                            this.createThesaurusFromLocalFileWindow = this.getCreateThesaurusFromLocalFileWindow();
                                        }
                                        this.createThesaurusFromLocalFileWindow.show(this);
                                    }
                                }, {
                                    text: OpenLayers.i18n('thesaurusFromURL'),
                                    scope: this,
                                    handler: function() {
                                        if(!this.createThesaurusFromRemoteFileWindow){
                                            this.createThesaurusFromRemoteFileWindow = this.getCreateThesaurusFromRemoteFileWindow();
                                        }
                                        this.createThesaurusFromRemoteFileWindow.show(this);
                                    }
                                }
                            ]
                        }
                },{
                    id:         'delete_thesaurus_button',
                    disabled:   true,
                    text:       OpenLayers.i18n('delete'),
                    iconCls:    'md-mn-del',
                    handler: function(){
                        OpenLayers.Request.GET({
                            scope: this,
                            url: this.catalogue.services.thesaurusDelete + "?ref=" + this.keywordStore.baseParams.pThesauri,
                            success: function(response){
                                this.thesaurusStore.removeAll();
                                this.thesaurusStore.reload();
                            },
                            failure: function(response){
                            }
                        });
                    },
                    scope: this
                },{
                    id:         'download_thesaurus_button',
                    disabled:   true,
                    text:       OpenLayers.i18n('download'),
                    iconCls: 'xmlIcon',
                    handler: function(){
                        location.replace(this.catalogue.services.thesaurusDownload + "?ref=" + this.keywordStore.baseParams.pThesauri);
                    },
                    scope: this
                }]
            }
        });
    },
    
    /**
     * Editor for the thesaurus contents
     */
    initKeywordView: function() {

        var kwColumnModel = new Ext.grid.ColumnModel({
            defaults: {
                sortable: true // columns are not sortable by default
            },
            columns: [{
                id: 'id',
                header: OpenLayers.i18n('id'),
                dataIndex: 'uri',
                editor: new Ext.form.TextField({
                    allowBlank: false
                })
            }, {
                id: 'hidden_id',
                header: OpenLayers.i18n('uri'),
                dataIndex: 'id',
                hidden: true,
                editor: new Ext.form.TextField({
                    allowBlank: false
                })
            }, {
                id: 'value',
                header: OpenLayers.i18n('label'),
                dataIndex: 'value',
                editor: new Ext.form.TextField({
                    allowBlank: false
                })
            }, {
                id: 'definition',
                header: OpenLayers.i18n('definition'),
                dataIndex: 'definition',
                editor: new Ext.form.TextField({
                    allowBlank: false
                })
            }, {
                id: 'west',
                header: OpenLayers.i18n('xmin'),
                dataIndex: 'west',
                hidden: true,
                editor: new Ext.form.TextField({
                    allowBlank: true
                })
            }, {
                id: 'east',
                header: OpenLayers.i18n('xmax'),
                dataIndex: 'east',
                hidden: true,
                editor: new Ext.form.TextField({
                    allowBlank: true
                })
            }, {
                id: 'south',
                header: OpenLayers.i18n('ymin'),
                dataIndex: 'south',
                hidden: true,
                editor: new Ext.form.TextField({
                    allowBlank: true
                })
            }, {
                id: 'north',
                header: OpenLayers.i18n('ymax'),
                dataIndex: 'north',
                hidden: true,
                editor: new Ext.form.TextField({
                    allowBlank: true
                })
            }]
        });
    
        // Keywords grid
        this.keywordGrid = new Ext.grid.EditorGridPanel({
            layout: 'fit',
            autoHeight: true,
            height: 500,
            border: false,
            store: this.keywordStore,
            cm: kwColumnModel,
            autoExpandColumn: 'value',
            sm: new Ext.grid.CellSelectionModel({
                listeners: {
                    scope: this,
                    selectionchange: function(smObj) {
                        this.updateKeywordToolbar();
                    }
                }
            }),
            listeners: {
                scope: this,
                afteredit: function(e) {
                    var ref = this.keywordStore.baseParams.pThesauri;
                    var refType = ref.split(".")[1];
                    var tokens = e.record.data.uri.split("#");
                    var namespace = tokens[0];
                    var newId = tokens[1];
                    var oldId = newId;
                    // Edit keyword in current GUI language
                    // TODO : improved to select the language to be updated
                    var lang = this.catalogue.lang;
                    var prefLab = e.record.data.value;
                    var definition = e.record.data.definition;
                    var requestPayLoad;
                    
                    if(e.field === "uri") {
                        oldId = e.originalValue.split('#')[1];
                    }
                    
                    // Creation of an ajax request to update the edited keyword
                    if(refType === 'place') {
                        var south = e.record.data.south;
                        var north = e.record.data.north;
                        var west = e.record.data.west;
                        var east = e.record.data.east;
                        
                        requestPayLoad = '<request><oldid>' + oldId + '</oldid><newid>' +
                            newId + '</newid><lang>' + lang + '</lang><ref>' + ref + '</ref><definition>' +
                            definition + '</definition><namespace>' + namespace + '#</namespace>' + '<north>' + north + '</north><south>' +
                            south + '</south><east>' + east + '</east><prefLab>' + prefLab + '</prefLab><west>' +
                            west + '</west><refType>' + refType + '</refType></request>';

                        Ext.Ajax.request({
                            scope: this,
                            url: this.catalogue.services.thesaurusConceptUpdate,
                              failure: function() {
                                  // TODO: display a message describing the reason of the error
                                  Ext.Msg.alert('Fail');
                                  },
                              xmlData: requestPayLoad
                        });
                    } else {
                        requestPayLoad = '<request><newid>' + newId + '</newid><refType>' + refType + '</refType><definition>'
                        + definition + '</definition><namespace>' + namespace + '#</namespace><ref>' + ref + '</ref><oldid>'
                        + oldId + '</oldid><lang>' + lang + '</lang><prefLab>' + prefLab + '</prefLab></request>';
                        
                        Ext.Ajax.request({
                            scope: this,
                            url: this.catalogue.services.thesaurusConceptUpdate,
                              failure: function() {
                                  // TODO: display a message describing the reason of the error
                                  Ext.Msg.alert('Fail');
                                  },
                              xmlData: requestPayLoad
                        });
                    }
                    
                    e.record.commit();
                }
            },
            tbar: {
                disabled: true,
                items: [{
                    id: 'add_keyword_button',
                    text: OpenLayers.i18n('add'),
                    iconCls: 'addIcon',
                    handler: function(){
                        var RecType = this.keywordGrid.store.recordType;
                        var thesaurusRecordIdx = this.thesaurusStore.find('id', this.keywordStore.baseParams.pThesauri);
                        var namespace = this.thesaurusStore.getAt(thesaurusRecordIdx).get('defaultNamespace');
                        // The uri of the new keyword is based on a timestamp in order to avoid
                        // two keywords with the same uri
                        var newUri = (namespace+'#' + new Date().getTime()).replace(/#+/,'#');
                        
                        // Send a request to add a new keyword in the database
                        var ref = this.keywordStore.baseParams.pThesauri;
                        var refType = ref.split(".")[1];
                        var newId = newUri.split("#")[1];
                        var lang = this.catalogue.lang;
                        var prefLab = OpenLayers.i18n('newLabel');
                        var definition = OpenLayers.i18n('newDefinition');
                        var requestPayLoad;
                        
                        if(refType === 'place') {
                            var south = -90;
                            var north = 90;
                            var west = -180;
                            var east = 180;
                            
                            requestPayLoad = '<request><oldid/><newid>' +
                                newId + '</newid><lang>' + lang + '</lang><ref>' + ref + '</ref><definition>' +
                                definition + '</definition><namespace>'+ namespace +'</namespace>' + '<north>' + north + '</north><south>' +
                                south + '</south><east>' + east + '</east><prefLab>' + prefLab + '</prefLab><west>' +
                                west + '</west><refType>' + refType + '</refType></request>';

                            Ext.Ajax.request({
                                scope: this,
                                url: this.catalogue.services.thesaurusConceptAdd,
                                  // TODO: display a message describing the reason of the error
                                  failure: function() { Ext.Msg.alert('Fail'); },
                                  xmlData: requestPayLoad
                            });
                        } else {
                            requestPayLoad = '<request><newid>' + newId + '</newid><refType>' + refType + '</refType><definition>'
                            + definition + '</definition><namespace>'+namespace+'</namespace><ref>' + ref + '</ref><oldid/><lang>' + lang + '</lang><prefLab>' + prefLab + '</prefLab></request>';
                            
                            Ext.Ajax.request({
                                scope: this,
                                url: this.catalogue.services.thesaurusConceptAdd,
                                  // TODO: display a message describing the reason of the error
                                  failure: function() { Ext.Msg.alert('Fail'); },
                                  xmlData: requestPayLoad
                            });
                        }
                        
                        
                        // Insertion of the new record in the grid
                        var newRec = new RecType({
                            uri: newUri,
                            value: prefLab,
                            definition: definition,
                            west: -180,
                            east: 180,
                            south: -90,
                            north: 90
                        });
                        this.keywordGrid.stopEditing();
                        this.keywordGrid.store.insert(0, newRec);
                        this.keywordGrid.startEditing(0, 0);
                    },
                    scope: this
                },{
                    id: 'delete_keyword_button',
                    text: OpenLayers.i18n('delete'),
                    iconCls: 'md-mn-del',
                    handler: function(){
                        var selectedCells = this.keywordGrid.getSelectionModel().getSelectedCell();
                        var rec = this.keywordGrid.store.getAt(selectedCells[0]);
                        var thesaurus = this.keywordStore.baseParams.pThesauri;
                        var tokens = rec.data.uri.split("#");
                        var namespace = tokens[0];
                        var id = tokens[1];
                        Ext.Ajax.request({
                                scope: this,
                                url: this.catalogue.services.thesaurusConceptDelete + '?pThesaurus=' + thesaurus + '&namespace=' + namespace + '%23&id=' + id,
                                // TODO: display a message describing the reason of the error
                                failure: function() { Ext.Msg.alert('Fail'); }
                        });
                        // Delete the datastore record
                        this.keywordGrid.store.removeAt(selectedCells[0]);
                    },
                    scope: this
                },
                '->',
                this.getKeyword(),
                OpenLayers.i18n('maxResults') + ' ' + OpenLayers.i18n('perThesaurus'),
                this.getLimitInput()]
            }
        });
    },
    
    /**
     * Method: updateThesaurusToolbar
     */
    updateThesaurusToolbar: function(){
        var record = this.thesaurusGrid.getSelectionModel().getSelected();
        Ext.getCmp('delete_thesaurus_button').setDisabled(!record);
        Ext.getCmp('download_thesaurus_button').setDisabled(!record);
    },
    
    /**
     * Method: updateKeywordToolbar
     * 
     * Activate delete button for local thesaurus concepts only
     */
    updateKeywordToolbar: function(){
        var record = this.thesaurusGrid.getSelectionModel().getSelected();
        if (record) {
            var thesaurusType = record.data.type;
            var isLocal = thesaurusType === 'local';
            if (isLocal) {
                var selectedCells = this.keywordGrid.getSelectionModel().getSelectedCell();
                Ext.getCmp('delete_keyword_button').setDisabled(!selectedCells);
            }
        }
    },

    /**
     * Method: updateKeywordGridAndToolbar
     */
    updateKeywordGridAndToolbar: function(){
        var record = this.thesaurusGrid.getSelectionModel().getSelected();
        
        if (record){
            this.keywordGrid.getTopToolbar().enable();

            var thesaurusTheme = record.data.theme;
            var thesaurusType = record.data.type;
            var isLocal = thesaurusType === 'local';
            var thesaurusId = record.data.id;
            
            this.keywordStore.removeAll();
                        
            this.keywordStore.baseParams.pThesauri = thesaurusId;
            
            // if the theme of the selected thesaurus is equal to "place" then
            // the east, west,... columns have to be visible
            var hidden = (thesaurusTheme !== "place");

            this.keywordGrid.getColumnModel().setHidden(
                    this.keywordGrid.getColumnModel().getIndexById('east'), hidden);
            this.keywordGrid.getColumnModel().setHidden(
                    this.keywordGrid.getColumnModel().getIndexById('west'), hidden);
            this.keywordGrid.getColumnModel().setHidden(
                    this.keywordGrid.getColumnModel().getIndexById('south'), hidden);
            this.keywordGrid.getColumnModel().setHidden(
                    this.keywordGrid.getColumnModel().getIndexById('north'), hidden);
            
            // Column are not editable for external thesaurus
            for (var i=0; i < this.keywordGrid.getColumnModel().getColumnCount(); i++) {
                this.keywordGrid.getColumnModel().setEditable(i, isLocal);
            }
            Ext.getCmp('delete_keyword_button').setDisabled(!isLocal);
            Ext.getCmp('add_keyword_button').setDisabled(!isLocal);
          
        } else {
            this.keywordGrid.getTopToolbar().disable();
        }
        
    },

    /**
     * Method: getCreateEmptyThesaurusWindow
     */
    getCreateEmptyThesaurusWindow: function(){
        var win = new Ext.Window({
            width:600,
            height:200,
            resizable: false,
            title: OpenLayers.i18n('thesaurusCreation'),
            closeAction:'hide',
            plain:true,
            bodyStyle:'padding:5px;',
            modal: true,
            items: [this.getCreateEmptyThesaurusForm()],
            constrain: true,
            buttons: [{
                text:OpenLayers.i18n('add'),
                scope: this,
                handler: function(){
                    if (this.createEmptyThesaurusForm.getForm().isValid()) {
                        this.createEmptyThesaurusForm.getForm().getEl().dom.enctype="application/x-www-form-urlencoded";
                        this.createEmptyThesaurusForm.bodyCfg.enctype="application/x-www-form-urlencoded";
                        
                        Ext.Ajax.request({
                            scope: this,
                            url: this.catalogue.services.thesaurusAdd,
                              success: function() {
                                  this.thesaurusStore.removeAll();
                                  this.thesaurusStore.reload();
                              },
                              failure: function() { Ext.Msg.alert('Fail'); },
                              xmlData: '<request><fname>' + Ext.getCmp('empty_thesaurusName').getValue()
                                + '</fname><dname>' + Ext.getCmp('empty_themeCmb').getValue() + '</dname><type>'
                                + Ext.getCmp('empty_thesaurusType').getValue() + '</type></request>'
                              });
                        
                        win.hide();
                    }
                }
            }]
        });
        
        return win;
    },

    /**
     * Method: getCreateThesaurusFromLocalFileWindow
     */
    getCreateThesaurusFromLocalFileWindow: function(){
        var win = new Ext.Window({
            width:600,
            height:200,
            resizable: false,
            title: OpenLayers.i18n('thesaurusCreation'),
            closeAction:'hide',
            plain:true,
            bodyStyle:'padding:5px;',
            modal: true,
            items: [this.getCreateThesaurusFromLocalFileForm()],
            constrain: true,
            buttons: [{
                text:OpenLayers.i18n('add'),
                scope: this,
                handler: function(){
                    console.log(this.createThesaurusFromLocalFileForm);
                    if (this.createThesaurusFromLocalFileForm.getForm().isValid()) {
                        this.createThesaurusFromLocalFileForm.getForm().getEl().dom.enctype="multipart/form-data";
                        this.createThesaurusFromLocalFileForm.bodyCfg.enctype="multipart/form-data";
                        this.createThesaurusFromLocalFileForm.getForm().submit({
                            url: this.catalogue.services.thesaurusUpload,
                            scope: this,
                            success: function(fp, action){
                                this.thesaurusStore.removeAll();
                                this.thesaurusStore.reload();
                            },
                            failure: function(response){
                                Ext.Msg.alert(OpenLayers.i18n('failure'), response.responseText);
                            }
                        });
                        win.hide();
                    }
                }
            }]
        });
        
        return win;
    },
    
    /**
     * Method: getCreateThesaurusFromRemoteFileWindow
     */
    getCreateThesaurusFromRemoteFileWindow: function(){
        var win = new Ext.Window({
            width:600,
            height:200,
            resizable: false,
            title: OpenLayers.i18n('thesaurusCreation'),
            closeAction:'hide',
            plain:true,
            bodyStyle:'padding:5px;',
            modal: true,
            items: [this.getCreateThesaurusFromRemoteFileForm()],
            constrain: true,
            buttons: [{
                text:OpenLayers.i18n('add'),
                scope: this,
                handler: function(){
                    if (this.createThesaurusFromRemoteFileForm.getForm().isValid()) {
                        this.createThesaurusFromRemoteFileForm.getForm().getEl().dom.enctype="application/x-www-form-urlencoded";
                        this.createThesaurusFromRemoteFileForm.bodyCfg.enctype="application/x-www-form-urlencoded";
                        
                        this.createThesaurusFromRemoteFileForm.getForm().submit({
                            url: this.catalogue.services.thesaurusUpload,
                            scope: this,
                            success: function(response){
                                this.thesaurusStore.removeAll();
                                this.thesaurusStore.reload();
                            },
                            failure: function(response){
                                Ext.Msg.alert(OpenLayers.i18n('failure'), response.responseText);
                            }
                        });
                        
                        win.hide();
                    }
                }
            }]
        });
        
        return win;
    },
 

    /**
     * Method: getCreateEmptyThesaurusForm
     */
    getCreateEmptyThesaurusForm: function(){
        this.createEmptyThesaurusForm = new Ext.form.FormPanel({
            fileUpload: false,
            region: 'center',
            autoHeight: true,
            baseCls: 'x-plain',
            labelWidth: 170,
            split: true,
            items: [{
                xtype: 'textfield',
                fieldLabel: OpenLayers.i18n('ThesaurusName'),
                id: 'empty_thesaurusName',
                name: 'fname',
                anchor: '-15'
            }, this.createTypeCombo('empty_themeCmb'), {
                xtype: 'textfield',
                fieldLabel: OpenLayers.i18n('thesaurusType'),
                id: 'empty_thesaurusType',
                name: 'type',
                value: 'local',
                hidden: true,
                anchor: '-15'
            }]
        });
        return this.createEmptyThesaurusForm;
    },

  getCreateThesaurusFromLocalFileForm: function(){
  this.createThesaurusFromLocalFileForm = new Ext.form.FormPanel({
      fileUpload: true,
      region: 'center',
      autoHeight: true,
      baseCls: 'x-plain',
      errorReader: new Ext.data.XmlReader({
            record : 'record'
        }, ['Thesaurus']
      ),
      labelWidth: 170,
      split: true,
      items: [{
          xtype: 'fileuploadfield',
          allowBlank: true,
          fieldLabel: OpenLayers.i18n('thesaurusFilePath'),
          id: 'local_fname',
          name: 'fname',
          buttonCfg: {
              text: OpenLayers.i18n('selectFile')
          }
      }, this.createTypeCombo('local_themeCmb'), {
        xtype: 'textfield',
          fieldLabel: OpenLayers.i18n('thesaurusType'),
          id: 'local_thesaurusType',
          name: 'type',
          value: 'external',
          hidden: true,
          anchor: '-15'
      }, {
          xtype: 'textfield',
          hidden: true,
          name: 'mode',
          value: 'file'
      }]
  });
  return this.createThesaurusFromLocalFileForm;
},
    
    getCreateThesaurusFromRemoteFileForm: function(){
        this.createThesaurusFromRemoteFileForm = new Ext.form.FormPanel({
            fileUpload: false,
            region: 'center',
            autoHeight: true,
            baseCls: 'x-plain',
            labelWidth: 170,
            split: true,
            errorReader: new Ext.data.XmlReader({
                    record : 'record'
                }, ['Thesaurus']
            ),
            listeners: {
                scope: this,
                afterrender : function(cmp) {
                    Ext.getCmp('remote_thesaurusURL').setVisible(true);
                    Ext.getCmp('remote_thesaurusCmb').setVisible(false);
                    Ext.getCmp('remote_themeCmb').setVisible(true);
                    }
            },
            items: [{
                xtype: 'radiogroup',
                id: 'creationModeRadioGroup',
                columns: 1,
                submitValue: false,
                fieldLabel: OpenLayers.i18n('creationMode'),
                items: [
                    {boxLabel: OpenLayers.i18n('createThesaurusFromURL'), name: 'rb-auto', inputValue: 1, checked: true},
                    {boxLabel: OpenLayers.i18n('createThesaurusFromRepository'), name: 'rb-auto', inputValue: 2}
                ],
                listeners: {
                    scope: this,
                    change: function(rg, radio) {
                            Ext.getCmp('remote_thesaurusURL').setVisible(radio.inputValue === 1);
                            Ext.getCmp('remote_thesaurusCmb').setVisible(radio.inputValue === 2);
                            Ext.getCmp('remote_themeCmb').setVisible(radio.inputValue !== 2);
                    }
                }
            }, {
                xtype: 'textfield',
                fieldLabel: OpenLayers.i18n('externalThesaurusURL'),
                id: 'remote_thesaurusURL',
                name: 'url',
                anchor: '-15'
            }, {
                fieldLabel: OpenLayers.i18n('thesaurusFromRepository'),
                id: 'remote_thesaurusCmb',
                anchor: '-15',
                xtype:          'combo',
                mode:           'local',
                triggerAction:  'all',
                forceSelection: true,
                editable:       false,
                displayField:   'title',
                valueField:     'link',
                submitValue:    false,
                store:          new GeoNetwork.data.ThesaurusFeedStore(this.feed),
                listeners: {
                    scope: this,
                    select: function(cmb, record, index) {
                            Ext.getCmp('remote_thesaurusURL').setValue(record.data.link);
                            Ext.getCmp('remote_themeCmb').setValue(record.data.category);
                    }
                }
            }, this.createTypeCombo('remote_themeCmb'), {
                xtype: 'textfield',
                fieldLabel: OpenLayers.i18n('thesaurusType'),
                id: 'remote_thesaurusType',
                name: 'type',
                value: 'external',
                hidden: true,
                anchor: '-15'
            }]
        });
        return this.createThesaurusFromRemoteFileForm;
    },
    createTypeCombo: function(id) {
        
        return new Ext.form.ComboBox({
                fieldLabel: OpenLayers.i18n('Theme'),
                id: id,
                name: 'themeCmb',
                anchor: '-15',
                mode:           'local',
                value:          this.defaultThesaurusType,
                triggerAction:  'all',
                forceSelection: true,
                editable:       false,
                displayField:   'name',
                valueField:     'value',
                hiddenName:     'dir',
                store:          new Ext.data.JsonStore({
                    fields : ['name', 'value'],
                    data   : [
                              {name : 'Discipline',   value: 'discipline'},
                              {name : 'Place',  value: 'place'},
                              {name : 'Stratum',  value: 'stratum'},
                              {name : 'Temporal',   value: 'temporal'},
                              {name : 'Theme',   value: 'theme'}
                    ]
                })
            });
    },
    /**
     * Method: getKeyword
     *
     *
     */
    getKeyword: function(){
        
        return new GeoNetwork.form.SearchField({
            id: 'keywordSearchField',
            width: 240,
            store: this.keywordStore,
            paramName: 'pKeyword'
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
            value: this.maxResults,
            width: 40,
            listeners: {
                scope: this,
                change: function(field, newValue, oldValue) {
                    this.keywordStore.baseParams.maxResults = newValue;
                }
            }
        };
    }
});

/** api: xtype = gn_admin_thesaurusmanagerpanel */
Ext.reg('gn_admin_thesaurusmanagerpanel', GeoNetwork.admin.ThesaurusManagerPanel);