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
 *  class = NewMetadataPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor 
 *  .. class:: NewMetadataPanel(config)
 *
 *     Create a GeoNetwork form for metadata creation
 *
 *
 */
GeoNetwork.editor.NewMetadataPanel = Ext.extend(Ext.Panel, {
    defaultConfig: {
        border: false,
        frame: false,
        isTemplate: 'n'
    },
    editor: undefined,
    getGroupUrl: undefined,
    groupStore: undefined,
    catalogue: undefined,
    tplStore: undefined,
    selectedGroup: undefined,
    selectedTpl: undefined,
    isChild: undefined,
    filter: undefined,
    createBt: undefined,
    validate: function(){
        if (this.selectedGroup !== undefined && this.selectedTpl !== undefined) {
            this.createBt.setDisabled(false);
        } else {
            this.createBt.setDisabled(true);
        }
    },
    
    /** private: method[initComponent] 
     *  Initializes the help panel.
     */
    initComponent: function(config){
        Ext.apply(this, config);
        Ext.applyIf(this, this.defaultConfig);
        var checkboxSM, colModel;
        
        
        this.createBt = new Ext.Button({
            text: OpenLayers.i18n('create'),
            iconCls: 'addIcon',
            disabled: true,
            handler: function(){
                // FIXME could be improved
                this.catalogue.metadataEdit(this.selectedTpl, true, this.selectedGroup, this.isChild, this.isTemplate);
                this.ownerCt.hide();
            },
            scope: this
        });
        
        this.buttons = [this.createBt, {
            text: OpenLayers.i18n('cancel'),
            iconCls: 'cancel',
            handler: function(){
                this.ownerCt.hide();
            },
            scope: this
        }];
        
        GeoNetwork.editor.NewMetadataPanel.superclass.initComponent.call(this);
        
        this.groupStore = GeoNetwork.data.GroupStore(this.getGroupUrl);
        // TODO filter internet and al groups
        this.groupStore.load();
        
        // Only add template if not already defined (ie. duplicate action)
        if (!this.selectedTpl) {
            this.tplStore = GeoNetwork.data.MetadataResultsStore();
            this.tplStore.setDefaultSort('displayOrder');
            
            // Create grid with template list
            checkboxSM = new Ext.grid.CheckboxSelectionModel({
                singleSelect: this.singleSelect,
                header: ''
            });
            
            colModel = new Ext.grid.ColumnModel({
                defaults: {
                    sortable: true
                },
                columns: [
                    checkboxSM,
                    {header: 'Schema', dataIndex: 'schema'},
                    {id: 'title', header: 'Title', dataIndex: 'title'},
                    {header: 'Order', hidden: true, dataIndex: 'displayOrder'}
                ]});
            
            this.add(new Ext.grid.GridPanel({
                layout: 'fit',
                title: OpenLayers.i18n('chooseTemplate'),
                border: false,
                autoScroll: true,
                height: 330,
                store: this.tplStore,
                colModel: colModel,
                sm: checkboxSM,
                autoExpandColumn: 'title',
                listeners: {
                    rowclick: function(grid, rowIndex, e) {
                        var data = grid.getStore().getAt(rowIndex).data;
                        if (grid.getSelectionModel().getCount() !== 0) {
                            this.selectedTpl = data.id;
                        } else {
                            this.selectedTpl = undefined;
                        }
                        this.validate();
                    },
                    scope : this
                }
            }));
            
            this.catalogue.search({E_template: 'y'}, null, null, 1, true, this.tplStore, null);
        }
        
        this.add(new Ext.form.ComboBox({
            name: 'E_group',
            mode: 'local',
            emptyText: OpenLayers.i18n('chooseGroup'),
            triggerAction: 'all',
            fieldLabel: OpenLayers.i18n('group'),
            store: this.groupStore,
            allowBlank: false,
            valueField: 'id',
            displayField: 'name',
            tpl: '<tpl for="."><div class="x-combo-list-item">{[values.label.' + OpenLayers.Lang.getCode() + ']}</div></tpl>',
            listeners: {
                select: function(field, record, idx){
                    this.selectedGroup = record.get('id');
                    this.validate();
                },
                scope: this
            }
        }));
        
        this.add({
                    xtype: 'textfield',
                    name: 'isTemplate',
                    hidden: true,
                    value: this.isTemplate
                });
    }
});

/** api: xtype = gn_editor_newmetadatapanel */
Ext.reg('gn_editor_newmetadatapanel', GeoNetwork.editor.NewMetadataPanel);
