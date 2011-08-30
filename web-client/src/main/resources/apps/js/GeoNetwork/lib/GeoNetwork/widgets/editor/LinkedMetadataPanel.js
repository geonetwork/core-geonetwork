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
 *  class = LinkedMetadataPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor 
 *  .. class:: LinkedMetadataPanel(config)
 *
 *     Create a GeoNetwork help panel
 *
 *
 */
GeoNetwork.editor.LinkedMetadataPanel = Ext.extend(Ext.Panel, {
    title: undefined,
    editor: undefined,
    metadataId: undefined,
    selectedUuid: undefined,
    selectedType: undefined,
    metadataSchema: undefined,
    serviceUrl: undefined,
    store: undefined,
    parentAction: undefined,
    datasetAction: undefined,
    serviceAction: undefined,
    featureCatAction: undefined,
    delFeatureCatAction: undefined,
    updateChildrenAction: undefined,
    colModel: undefined,
    expander: undefined,
    defaultConfig: {
        border: false,
        frame: false,
        iconCls: 'linkIcon',
        collapsible: true,
        collapsed: false
    },
    createColumnDef: function() {
        this.expander = new Ext.grid.RowExpander({
            tpl: new Ext.XTemplate('<div title=""><a href="#" onclick="javascript:catalogue.metadataShow(\'{uuid}\');">{title}</a>' 
                + '</div>' 
                + '{msg}')
        });
        
        return [this.expander, {
                id: 'type',
                header: OpenLayers.i18n('type'),
                width: 60,
                sortable: true,
                hidden: true,
                dataIndex: 'type'
            }, {
                header: OpenLayers.i18n('title'),
                hidden: false,
                dataIndex: 'title'
            }];
    },
    createColumnModel: function() {
        
        this.colModel = new Ext.grid.ColumnModel({
            defaults: {
                width: 120,
                sortable: true
            },
            columns: this.createColumnDef()
        });
        return this.colModel;
    },
    updatePanel: function(){
        this.reload();
    },
    /** private: method[clear] 
     *  Remove all related metadata from the store
     */
    clear: function() {
        this.store.removeAll();
    },
    reload: function(e, id, schema){
        this.metadataId = id || this.metadataId;
        this.metadataSchema = schema || this.metadataSchema;
        if (this.collapsed) {
            return;
        }
        
        this.colModel.setConfig(this.createColumnDef());
        
        var isIso = this.metadataSchema.indexOf('iso19139') !== -1;
        var isIso19110 = this.metadataSchema==='iso19110';

        // TODO : is dataset
        this.parentAction.setDisabled(!isIso);
        this.serviceAction.setDisabled(!isIso);
        this.datasetAction.setDisabled(!isIso);
        
        // Only allow to set feature catalogue link from the dataset
        this.featureCatAction.setDisabled(isIso19110);
        
        this.store.reload({
            params: {
                id: this.metadataId,
                fast: 'false'
            }
        });
    },
    updateStatus: function(store, records, options){
        var children = store.find('type', 'children');
        this.updateChildrenAction.setDisabled(children===-1);
        var iso19110 = store.find('type', 'related');
        this.delFeatureCatAction.setDisabled(iso19110===-1);
    },
    selectionChangeEvent: function(g, idx, e){
         var record = g.getStore().getAt(idx);
         this.selectedUuid = record.get('uuid');
         this.selectedType = record.get('type');
         this.delFeatureCatAction.setDisabled(this.selectedType !== 'related');
    },
    getChildrenIds: function(){
        var uuidList = [];
        this.store.each(
            function(record){
                if (record.get('type')==='children') {
                    this.push(record.get('id'));
                }
            },
            uuidList
        );
        return uuidList;
    },
    /** private: method[initComponent] 
     *  Initializes the help panel.
     */
    initComponent: function(config){
        Ext.apply(this, config);
        Ext.applyIf(this, this.defaultConfig);
        
        this.title = OpenLayers.i18n('relatedResources');
        this.tools = [{
            id : 'refresh',
            handler : function (e, toolEl, panel, tc) {
                panel.reload(panel, panel.metadataId);
            }
        }];
    
        this.parentAction = new Ext.Action({
                        text: OpenLayers.i18n('parentMd'),
                        handler: function(){
                            this.switchToTab('metadata');
                        },
                        scope: this.editor
                    });
                    
        this.datasetAction = new Ext.Action({
                        text: OpenLayers.i18n('datasetMd'),
                        iconCls: 'cat-dataset',
                        handler: function(){
                            this.showLinkedServiceMetadataSelectionPanel('coupledResource', '', '');
                        },
                        scope: this.editor
                        // TODO : hide if dataset
                    });
        this.serviceAction = new Ext.Action({
                        // TODO : hide for service
                        text: OpenLayers.i18n('serviceMd'),
                        iconCls: 'cat-service',
                        handler: function(){
                            this.showLinkedServiceMetadataSelectionPanel('attachService', '', '');
                        },
                        scope: this.editor
                    });
        this.featureCatAction = new Ext.Action({
                        text: OpenLayers.i18n('featureCat'),
                        iconCls: 'cat-featureCatalogue',
                        handler: function(){
                            this.showLinkedMetadataSelectionPanel(null, 'iso19110');
                        },
                        scope: this.editor
                    });
        
        this.delFeatureCatAction = new Ext.Action({
            text: OpenLayers.i18n('delete19110Relation'),
            handler: function(){
                this.editor.catalogue.doAction(this.editor.catalogue.services.mdRelationDelete, 
                    {childUuid: this.selectedUuid, parentId: this.metadataId},
                    null, null, this.reload.bind(this), null);
            },
            scope: this,
            disabled: true
        });
        
        this.updateChildrenAction = new Ext.Action({
            text: OpenLayers.i18n('updateChildren'),
            handler: function(){
                this.editor.catalogue.modalAction(
                    OpenLayers.i18n('updateChildren'), 
                    this.editor.catalogue.services.mdMassiveChildrenForm + "?parentUuid=" + document.mainForm.uuid.value 
                                        + "&schema=" + document.mainForm.schema.value
                                        + "&childrenIds=" + this.getChildrenIds().join(','));
            },
            scope: this,
            disabled: true
        });
        
        
        this.bbar = new Ext.Toolbar({
            items: [{
                text: OpenLayers.i18n('createLink'),
                iconCls: 'relatedAddIcon',
                menu: new Ext.menu.Menu({
                    items: [this.parentAction, 
                            this.datasetAction, 
                            this.serviceAction, 
                            this.featureCatAction]
                })
            }, {
                text: OpenLayers.i18n('otherActions'),
                menu: new Ext.menu.Menu({
                     items: [this.updateChildrenAction,
                     this.delFeatureCatAction]})
            }]
        });
        
        GeoNetwork.editor.LinkedMetadataPanel.superclass.initComponent.call(this);
        
        this.store = new GeoNetwork.data.MetadataRelationStore(this.serviceUrl, {
            fast: false,
            id: this.metadataId
        }, true);
        var panel = this;
        var grid = new Ext.grid.GridPanel({
            store: this.store,
            colModel: this.createColumnModel(),
            loadMask: true,
            plugins: this.expander,
            view: new Ext.grid.GroupingView({
                forceFit: true,
                groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "' + OpenLayers.i18n('records') + '" : "' + OpenLayers.i18n('record') + '"]})'
            }),
            frame: false,
            height: 150,
            autoWidth: true,
            listeners: {
                rowclick: this.selectionChangeEvent,
                scope: panel
            }
        });
        this.add(grid);
        
        if (this.metadataId) {
            this.reload();
        }
        this.editor.on('editorClosed', this.clear, this);
        this.editor.on('metadataUpdated', this.reload, this);
        this.on('expand', this.reload);
        this.store.on('load', this.updateStatus, this);
    }
});

/** api: xtype = gn_editor_linkedmetadatapanel */
Ext.reg('gn_editor_linkedmetadatapanel', GeoNetwork.editor.LinkedMetadataPanel);
