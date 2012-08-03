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
 *     Default metadata store to use could be overriden when setting
 *     GeoNetwork.Settings.mdStore variables. Default is :class:`GeoNetwork.data.MetadataResultsStore`.
 *
 */
GeoNetwork.editor.NewMetadataPanel = Ext.extend(Ext.Panel, {
    defaultConfig: {
        border: false,
        frame: false,
        isTemplate: 'n',
        layout: 'anchor'
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
    initComponent: function(){
        Ext.applyIf(this, this.defaultConfig);
        var checkboxSM, colModel;
        
        
        this.createBt = new Ext.Button({
            text: OpenLayers.i18n('create'),
            iconCls: 'addIcon',
            ctCls: 'gn-bt-main',
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
        
        var cmp = [];
        
        // Only add template if not already defined (ie. duplicate action)
        if (!this.selectedTpl) {
            this.tplStore = GeoNetwork.Settings.mdStore ? GeoNetwork.Settings.mdStore() : GeoNetwork.data.MetadataResultsFastStore();
            this.tplStore.setDefaultSort('displayOrder');
            
            // Create grid with template list
            checkboxSM = new Ext.grid.CheckboxSelectionModel({
                singleSelect: this.singleSelect,
                header: ''
            });
            
            var tplDescription = function(value, p, record){
                return String.format(
                        '<span class="tplTitle">{0}</span><div class="tplDesc">{1}</div>',
                        record.data.title, record.data['abstract']);
            };
            var tplType = function(value, p, record){
                var label = OpenLayers.i18n(record.data.type) || '';
                
                if(record.data.spatialRepresentationType) {
                    label += " / " + OpenLayers.i18n(record.data.spatialRepresentationType);
                }
                
                return String.format('{0}',label);
            };
            
            colModel = new Ext.grid.ColumnModel({
                defaults: {
                    sortable: true
                },
                columns: [
                    checkboxSM,
                    {header: OpenLayers.i18n('metadatatype'), renderer: tplType, dataIndex: 'type'},
                    {id: 'title', header: OpenLayers.i18n('tplTitle'), renderer: tplDescription, dataIndex: 'title'},
                    {header: 'Schema', dataIndex: 'schema'},
                    {header: 'Order', hidden: true, dataIndex: 'displayOrder'}
                ]});
            
            var grid = new Ext.grid.GridPanel({
                border: false,
                anchor: '100% -30',
                store: this.tplStore,
                colModel: colModel,
                sm: checkboxSM,
                autoExpandColumn: 'title'
            });
            
            grid.getSelectionModel().on('rowselect', function(sm, rowIndex, r) {
                if (sm.getCount() !== 0) {
                    this.selectedTpl = r.data.id;
                } else {
                    this.selectedTpl = undefined;
                }
                this.validate();
            }, this);
            
            // Focus on first row
            grid.getStore().on('load', function(store) {
                grid.getSelectionModel().selectFirstRow();
                grid.getView().focusEl.focus();
            }, grid);
            cmp.push(grid);
            this.catalogue.search({E_template: 'y'}, null, null, 1, true, this.tplStore, null);
        }
        
        cmp.push(new Ext.form.ComboBox({
            name: 'E_group',
            mode: 'local',
            anchor: '100%',
            emptyText: OpenLayers.i18n('chooseGroup'),
            triggerAction: 'all',
            fieldLabel: OpenLayers.i18n('group'),
            store: this.groupStore,
            allowBlank: false,
            valueField: 'id',
            displayField: 'name',
            tpl: '<tpl for="."><div class="x-combo-list-item">{[values.label.' + GeoNetwork.Util.getCatalogueLang(OpenLayers.Lang.getCode()) + ']}</div></tpl>',
            listeners: {
                select: function(field, record, idx){
                    this.selectedGroup = record.get('id');
                    this.validate();
                },
                scope: this
            }
        }));
        
        cmp.push({
                    xtype: 'textfield',
                    name: 'isTemplate',
                    hidden: true,
                    value: this.isTemplate
                });
        
        this.add(cmp);
        
    }
});

/** api: xtype = gn_editor_newmetadatapanel */
Ext.reg('gn_editor_newmetadatapanel', GeoNetwork.editor.NewMetadataPanel);
