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


/** api: (define)
 *  module = GeoNetwork.admin
 *  class = SubTemplateManagerPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor 
 *  .. class:: SubTemplateManagerPanel(config)
 *
 *  Create a GeoNetwork sub template manager panel
 *  to add, delete, update and set privileges for sub-templates.
 *
 */
GeoNetwork.admin.SubTemplateManagerPanel = Ext.extend(Ext.Panel, {
    frame: false,
    record: undefined,
    catalogue: undefined,
    toolbar: undefined,
    /**
     * Panel providing simple search form and
     * results
     */
    searchPanel: undefined,
    searchForm: undefined,
    searchField: undefined,
    newMetadataWindow: undefined,
    searchResultGrid: undefined,
    /**
     * Panel to edit a sub-template
     */
    subTemplateEditor: undefined,
    /**
     *
     */
    subTemplateToolbar: undefined,
    subTemplateType: undefined,
    subTemplateTypeStore: undefined,
    defaultConfig: {
        //title: OpenLayers.i18n('manageDirectories'),
        defaultViewMode: 'simple',
        border: false,
        height: 800,
        autoWidth : true,
        iconCls: 'subtemplateIcon',
        hitsPerPage: 200
    },
    /**
     * Create a combo box with the list of root element possible.
     */
    getSubTemplateTypeField: function(){
        
        return {
            xtype: 'combo',
            name: 'E__root',
            mode: 'local',
            title: OpenLayers.i18n('selDirectoryTT'),
            triggerAction: 'all',
            store: this.subTemplateTypeStore,
            valueField: 'type',
            tpl: '<tpl for="."><div class="x-combo-list-item" title="{description}">{label} ({type})</div></tpl>',
            displayField: 'label'
        };
    },
    
    /** private: method[initComponent] 
     *  Initializes the sub-template manager panel.
     *  
     *  TODO : Add a refresh action (after import)
     *  TODO : init type of directory by URL parameter
     */
    initComponent: function(config){
    
        Ext.apply(this, config);
        Ext.applyIf(this, this.defaultConfig);
        
        // A store with the list of available sub-template
        this.subTemplateTypeStore = new GeoNetwork.data.SubTemplateTypeStore(this.catalogue.services.subTemplateType);
        
        // A store for the list of sub-template found
        // FIXME : sharing this store could cause trouble in a app ?
        this.catalogue.metadataStore = GeoNetwork.data.MetadataResultsStore();
        
        // FIXME : this function may be use somewhere else in the current application
        this.catalogue.editMode = 2; // TODO : improve
        this.catalogue.metadataEditFn = function(metadataId, create, group, child) {
            this.editorPanel.init(metadataId, create, group, child, 's');
        }.bind(this);
        
        GeoNetwork.admin.SubTemplateManagerPanel.superclass.initComponent.call(this);
        
        
        this.initSearch();
        this.initEditor();
        
        
        // Build the layout
        var searchView = {
            region: 'west',
            split: true,
            collapsible: true,
            collapsed: false,
            hideCollapseTool: true,
            collapseMode: 'mini',
            autoScroll: true,
            minWidth: 240,
            width: 280,
            items: [this.searchForm, this.searchResultGrid]
        };
        var editorView = {
            region: 'center',
            split: true,
            autoScroll: true,
            minHeigth: 400,
            items: [
                this.editorPanel
            ]
        };
        
        this.add(searchView);
        this.add(editorView);
        
        
        // this.refresh();
        this.searchField.focus(true);
        
    },
    /**
     * Search and clean current editing and disabled toolbar
     * (no record selected). 
     * 
     * TODO : Add warning if on editing
     */
    search: function() {
        this.editorPanel.cancel();
        this.catalogue.search('searchForm', null, null);
    },
    /**
     * Search form for sub-templates composed of a type field, a full text search field
     * and a hidden sub-template criteria field. Search is based on xml.search service
     * and display results in a Grid.
     */
    initSearch: function() {
        var panel = this;
        
        // Search form
        this.searchField = new GeoNetwork.form.SearchField({
            name: 'E_any',
            anchor: '100%',
            store: this.catalogue.metadataStore,
            triggerAction: function(scope){
                scope.record = undefined;
                scope.disableToolbar(true);
                scope.search();
            },
            scope: panel
        });
        this.searchForm = new Ext.FormPanel({
            id: 'searchForm',
            border: false,
            defaults: {
                anchor: '100%',
                hideLabel: true
            },
            items: [
                this.getSubTemplateTypeField(), 
                {
                    xtype: 'textfield',
                    name: 'E__isTemplate',
                    hidden: true,
                    value: 's'
                }, {
                    xtype: 'textfield',
                    name: 'E_hitsperpage',
                    hidden: true,
                    value: this.hitsPerPage
                },
                this.searchField
            ]
        });
        var events = ['afterDelete'];
        Ext.each(events, function (e) {
            this.catalogue.on(e, function(){
                this.search();
            }, this);
        }, this);
        
        // Results grid
        this.searchResultGrid = new Ext.grid.GridPanel({
            anchor: '100%',
            height: 625,
            store: this.catalogue.metadataStore,
            // TODO : it could be nice to be able to sort by mode information according to the type
            // of sub-template. For example, sort by organisation, name or email for contacts
            columns: [{
                id: 'title',
                header: OpenLayers.i18n('title'),
                sortable: true,
                dataIndex: 'title'
            }, {
                id: 'uuid',
                header: 'Uuid',
                sortable: true,
                dataIndex: 'uuid',
                hidden: true
            }],
            autoExpandColumn: 'title',
            listeners: {
                rowclick: function(grid, rowIndex, e){
                    this.record = grid.getStore().getAt(rowIndex).data;
                    this.editorPanel.init(this.record.id, false);
                    this.disableToolbar(false);
                },
                scope: panel
            },
            bbar: {
                items: [{
                    xtype: 'button',
                    text: OpenLayers.i18n('add'),
                    tooltip: OpenLayers.i18n('addTplElTT'),
                    iconCls: 'addIcon',
                    handler: function(){
                        GeoNetwork.admin.AdminTools.showImportMetadataWindow('s', OpenLayers.i18n('subTemplateImport'));
                    },
                    scope: this
                },{
                    text: OpenLayers.i18n('duplicate'),
                    tooltip: OpenLayers.i18n('copyTplElTT'),
                    iconCls: 'md-mn-copy',
                    disabled: true,
                    handler: function(){
                        var id = this.record.id;
                        GeoNetwork.editor.EditorTools.showNewMetadataWindow(this, id, OpenLayers.i18n('duplicate'), false);
                    },
                    scope: this
                },{
                    text: OpenLayers.i18n('delete'),
                    tooltip: OpenLayers.i18n('delTplElTT'),
                    iconCls: 'md-mn-del',
                    disabled: true,
                    handler: function(){
                        var id = this.record.uuid;
                        this.catalogue.metadataDelete(id);
                        // TODO : refresh search
                        // TODO : reset editor
                    },
                    scope: this
                },{
                    text: OpenLayers.i18n('privileges'),
                    tooltip: OpenLayers.i18n('privTplElTT'),
                    iconCls : 'privIcon',
                    disabled: true,
                    handler: function(){
                        var id = this.record.id;
                        this.catalogue.metadataAdmin(id);
                    },
                    scope: this
                }]
            }
        });
    },
    disableToolbar: function(disabled) {
        var bbar = this.searchResultGrid.getBottomToolbar();
        Ext.each(bbar.items, function (item, idx){
            if (idx !== 0) {// Not the add button
                bbar.get(idx).setDisabled(disabled);
            }
        }, bbar);
    },
    /**
     * Editor for sub-templates
     */
    initEditor: function() {
        
        // Add editor panel with custom view mode and utility panel collapsed
        // TODO : maybe we should turn off some utilities which are not relevant in 
        // sub-template editing scenatio.
        // TODO : Adapt the toolbar
        var disableConfig = {
                    collapsed: true,
                    hidden: true
                };
        this.editorPanel = new GeoNetwork.editor.EditorPanel({
            defaultViewMode: this.defaultViewMode,
            tbarConfig: {
                hideTypeMenu: true
            },
            utilityPanelConfig: {
                thumbnailPanel: disableConfig,
                relationPanel: disableConfig,
                validationPanel: {  // TODO : validation of sub-template returns error like
                // Cannot find the declaration of element 'gmd:pointOfContact'. (Element: gmd:pointOfContact with parent element: Unknown)
                // Not sure this could be avoid easily.
                // Keep the panel for the time being.
                    collapsed: true
                },
                suggestionPanel: disableConfig,
                helpPanel: {
                    collapsed: false
                }
            },
            catalogue: this.catalogue,
            utilityPanelCollapsed: true
        });
        
    },
    
    refresh: function() {
        this.subTemplateTypeStore.load();
    }
});

/** api: xtype = gn_admin_subtemplatemanagerpanel */
Ext.reg('gn_admin_subtemplatemanagerpanel', GeoNetwork.admin.SubTemplateManagerPanel);