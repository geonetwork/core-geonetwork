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
Ext.namespace('GeoNetwork');

/**
 *
 */
/** api: (define) 
 *  module = GeoNetwork
 *  class = MetadataMenu
 *  base_link = `Ext.menu.Menu <http://extjs.com/deploy/dev/docs/?class=Ext.DataView>`_
 */
/** api: constructor 
 *  .. class:: MetadataMenu(config)
 *  
 *    Metadata menu allows users to access all metadata managment operations according to their privileges.
 *  
 *    Rating widget is available by default if Ext.ux.RatingItem is defined.
 *
 *	  Define a  GeoNetwork.Settings.editor.editHarvested to allow editing of harvested records 
 *    (TODO : retrieve from catalogue configuration).
 */
GeoNetwork.MetadataMenu = Ext.extend(Ext.menu.Menu, {
    /** private: property[record]
     * Record used for the current menu.
     */
    record: undefined,
    /** private: method[setRecord] 
     *  Set current menu record
     *  
     */
    setRecord: function(record) {
        this.record = record;
        this.updateMenu();
    },
    resultsView: undefined,
    catalogue: undefined,
    extEditorAction: undefined,
    angularEditorAction: undefined,
    deleteAction: undefined,
    zoomToAction: undefined,
    otherActions: undefined,
    adminMenuSeparator: undefined,
    duplicateAction: undefined,
    createChildAction: undefined,
    statusAction: undefined,
    enableWorkflowAction: undefined,
    versioningAction: undefined,
    adminAction: undefined,
    publicationToggleAction: undefined,
    categoryAction: undefined,
    viewAction: undefined,
    printAction: undefined,
    viewXMLAction: undefined,
    getMEFAction: undefined,
    ratingWidget: undefined,
    statusStore: undefined,
    defaultConfig: {},
    /** private: method[setRecord] 
     *  Create the menu.
     *  
     */
    create: function() {
        this.initAction();
        this.composeMenu();
        this.addCustomAction();
        this.updateMenu();
     },
     
     initAction: function(){
        
        this.extEditorAction = new Ext.Action({
            text: OpenLayers.i18n('edit'),
            iconCls: 'md-mn-edit',
            handler: function(){
                var id = this.record.get('id');
                this.catalogue.metadataEdit(id);
            },
            scope: this
        });
        this.angularEditorAction = new Ext.Action({
            text: OpenLayers.i18n('edit'),
            iconCls: 'md-mn-edit',
            handler: function(){
                var id = this.record.get('id');
                this.catalogue.metadataEdit2(id);
            },
            scope: this
        });
        this.deleteAction = new Ext.Action({
            text: OpenLayers.i18n('delete'),
            iconCls: 'md-mn-del',
            handler: function(){
                var id = this.record.get('uuid');
                this.catalogue.metadataDelete(id);
            },
            scope: this
        });
        /* Other actions */
        this.duplicateAction = new Ext.Action({
            text: OpenLayers.i18n('duplicate'),
            iconCls: 'md-mn-copy',
            handler: function(){
                var id = this.record.get('id');
                catalogue.metadataEdit2(id, true, null, false, 'n', null);
            },
            scope: this
        });
        this.createChildAction = new Ext.Action({
            text: OpenLayers.i18n('createChild'),
            iconCls: 'childIcon',
            handler: function(){
                var id = this.record.get('id');
                catalogue.metadataEdit2(id, true, null, true, 'n', null);
            },
            scope: this
        });
        // FIXME : tooltip for actions does not work
        this.adminAction = new Ext.Action({
            text: OpenLayers.i18n('privileges'),
            tooltip: OpenLayers.i18n('privilegesTT'),
            iconCls : 'privIcon',
            handler: function(){
                this.catalogue.metadataAdmin(this.record);
            },
            scope: this
        });
        this.publicationToggleAction = new Ext.Action({
            text: OpenLayers.i18n('publish'),
            handler: function(){
                this.catalogue.metadataPublish(this.record, Ext.getBody());
            },
            scope: this
        });
        this.enableWorkflowAction = new Ext.Action({
            text: OpenLayers.i18n('enableWorkflow'),
            tooltip: OpenLayers.i18n('enableWorkflowTT'),
            iconCls : 'statusIcon',
            handler: function(){
                var id = this.record.get('id'),
                    status = GeoNetwork.Settings.DraftStatus || 1;
                
                this.catalogue.metadataSetStatus(id, status, OpenLayers.i18n('enableWorkflow'));
            },
            scope: this
        });
        this.statusAction = new Ext.Action({
            text: OpenLayers.i18n('status'),
            tooltip: OpenLayers.i18n('statusTT'),
            iconCls : 'statusIcon',
            handler: function(){
                this.catalogue.metadataStatus(this.record);
            },
            scope: this
        });
         // TODO : enable only if SVN manager is on.
         this.versioningAction = new Ext.Action({
             text: OpenLayers.i18n('versioning'),
             tooltip: OpenLayers.i18n('versioningTT'),
             iconCls : 'versioningIcon',
             handler: function(){
                 var id = this.record.get('id');
                 this.catalogue.metadataVersioning(id);
             },
             scope: this
         });
         this.categoryAction = new Ext.Action({
             text: OpenLayers.i18n('categories'),
             iconCls : 'categoryIcon',
             handler: function(){
                 var id = this.record.get('id');
                 this.catalogue.metadataCategory(id);
             },
             scope: this
         });

        this.adminMenuSeparator = new Ext.menu.Separator();
        
        /* Public menu */
        this.viewAction = new Ext.Action({
            text: OpenLayers.i18n('view'),
            iconCls: 'md-mn-view',
            handler: function(){
                var id = this.record.get('uuid');
                this.catalogue.metadataShow(id);
            },
            scope: this
        });
        
        this.zoomToAction = new Ext.Action({
            text: OpenLayers.i18n('zoomTo'),
            iconCls: 'zoomlayer',
            handler: function(){
                var uuid = this.record.get('uuid');
                this.resultsView.zoomTo(uuid);
            },
            scope: this
        });
        
        this.viewXMLAction = new Ext.Action({
            text: OpenLayers.i18n('saveXml'),
            iconCls: 'xmlIcon',
            handler: function(){
                var id = this.record.get('uuid');
                var schema = this.record.get('schema');
                this.catalogue.metadataXMLShow(id, schema);
            },
            scope: this
        });
        
        this.viewRDFAction = new Ext.Action({
            text: OpenLayers.i18n('saveRdf'),
            //W3C Semantic Web Logo
            iconCls: 'rdfIcon',
            handler: function(){
                var id = this.record.get('uuid');
                var schema = this.record.get('schema');
                window.open(this.catalogue.services.mdRDFGet + "?uuid=" + id);
            },
            scope: this
        });
        
        this.printAction = new Ext.Action({
                text: OpenLayers.i18n('printSel'),
                iconCls: 'md-mn-pdf',
                handler: function(){
                    this.catalogue.metadataPrint(this.record.get('uuid'));
                },
                scope: this
            });
        
        this.getMEFAction = new Ext.Action({
            text: OpenLayers.i18n('getMEF'),
            iconCls: 'md-mn-zip',
            handler: function(){
                this.catalogue.metadataMEF(this.record.get('uuid'));
            },
            scope: this
        });

        /* Rating menu */
        if (!this.catalogue.isReadOnly() && Ext.ux.RatingItem) { // Check required widget are loaded before displaying context menu
            // If more actions are placed in context menu, this needs improvements.
            this.ratingWidget = new Ext.ux.RatingItem(null, {
                canReset: false,
                name: 'rating',
                disabled: true,
                nbStars: 5,
                cls: 'ux-menu-rating-item',
                listeners: {
                    change: function(e, value, star){
                        if (value) {
                            var uuid = this.record.get('uuid');
                            this.catalogue.metadataRate(uuid, value, this.hide());
                            this.ratingWidget.reset();
                        }
                        // TODO : if nb update current rating
                    },
                    scope: this
                }
            });
        }


        /* TODO : add categories / privileges / create child */
        this.updateMenu();
    },
    
    composeMenu: function(){
        if(!this.catalogue.isReadOnly()) {
            this.add(this.angularEditorAction);
            this.add(this.extEditorAction);
            this.add(this.deleteAction);
            this.otherActions = new Ext.menu.Item({
                text: OpenLayers.i18n('otherActions'),
                menu: {
                    items: [this.duplicateAction, this.createChildAction, 
                            this.adminAction, 
                            this.publicationToggleAction, this.statusAction, 
                            this.enableWorkflowAction, this.versioningAction, 
                            this.categoryAction]
                }
            });
        }
        else {
            this.otherActions = new Ext.menu.Item({
                text: OpenLayers.i18n('otherActions'),
                menu: {
                    items: []
                }
            });
        }

        this.add(this.otherActions);
        this.add(this.adminMenuSeparator);
        
        this.add(this.viewAction);
        this.add(this.zoomToAction);
        this.add(this.viewXMLAction);
        
        this.add(this.viewRDFAction);
        this.add(this.printAction);
        this.add(this.getMEFAction);
        
        /* Rating menu */
        if (!this.catalogue.isReadOnly() && Ext.ux.RatingItem) { // Check required widget are loaded before displaying context menu
            this.add(this.ratingWidget);
        }
    },
    
    /**
     * To override : Add optionnal custom action to the metadataMenu
     */
    addCustomAction: function() {
    	
    },
    
    /** private: method[updateMenu] 
     *  Update menu privileges according to information defined in current record.
     *  
     */
    updateMenu: function(){
        if (!this.record) {
            return; // TODO : improve. It happens when ViewWindow is opened without searching first.
        }
        
        var isEditable = this.record.get('edit') === 'true' ? 
        					// do not allow edit on harvested records by default
        					(this.record.get('isharvested') === 'y' ? GeoNetwork.Settings.editor.editHarvested || false : true) 
        					: 
        					false,
            status = this.record.get('status'),
            disableIfSubmittedForEditor = (status == 4 && this.catalogue.identifiedUser.role === 'Editor' ? true : false),
            isHarvested = this.record.get('isharvested') === 'y' ? true : false,
            isPublished = this.record.get('isPublishedToAll') === 'true' ? true : false,
            isAdmin = (this.catalogue.identifiedUser && this.catalogue.identifiedUser.role !== 'Administrator'),
            harvesterType = this.record.get('harvestertype'),
            identified = this.catalogue.isIdentified() && 
                (this.catalogue.identifiedUser && this.catalogue.identifiedUser.role !== 'RegisteredUser'),
            isReadOnly = this.catalogue.isReadOnly();


        this.extEditorAction.hide();
        this.angularEditorAction.hide();

        /* Actions and menu visibility for logged in user */
        if (!identified || isReadOnly) {
            this.extEditorAction.setText(OpenLayers.i18n('edit'));
            this.angularEditorAction.setText(OpenLayers.i18n('edit'));
            this.deleteAction.hide();
        } else {
            // TODO : if editor and status is submitted - turn off editing
            var statusIdx = this.statusStore.find('id', status);
            
            // Set button title with status information if not set to unkonwn (ie. workflow is enabled)
            this.extEditorAction.setText(OpenLayers.i18n('edit')
                    + (statusIdx !== -1 && status > 0 ? 
                            OpenLayers.String.format(OpenLayers.i18n('currentStatus'), {
                                status: this.statusStore.getAt(statusIdx).get('label')[catalogue.lang]
                            }) : '')
                    );

          if (GeoNetwork.Settings.hideExtEditor === false) {
            this.extEditorAction.show();
          }
            this.angularEditorAction.setText(OpenLayers.i18n('edit')
                    + (statusIdx !== -1 && status > 0 ? 
                            OpenLayers.String.format(OpenLayers.i18n('currentStatus'), {
                                status: this.statusStore.getAt(statusIdx).get('label')[catalogue.lang]
                            }) : '')
                    );
          // Display by default Angular editor
          if (!GeoNetwork.Settings.hideAngularEditor) {
            this.angularEditorAction.show();
          }
            this.deleteAction.show();
            
            // If status is unkown or undefined
            if (status == '' || status == '0') {
                this.enableWorkflowAction.show();
                this.statusAction.hide();
            } else {
                this.enableWorkflowAction.hide();
                this.statusAction.show();
            }
        }
        if(this.otherActions) this.otherActions.setVisible(identified);
        this.adminMenuSeparator.setVisible(identified);
        
        /* Actions status depend on records */
        if(GeoNetwork.Settings && GeoNetwork.Settings.editor && GeoNetwork.Settings.editor.disableIfSubmittedForEditor) {
            this.extEditorAction.setDisabled(disableIfSubmittedForEditor || !isEditable || isReadOnly);
            this.angularEditorAction.setDisabled(disableIfSubmittedForEditor || !isEditable || isReadOnly);
        } else {
            this.extEditorAction.setDisabled(!isEditable || isReadOnly);
            this.angularEditorAction.setDisabled(!isEditable || isReadOnly);
        }
        this.adminAction.setDisabled((!isEditable && !isHarvested) || isReadOnly);
        
        // Publish/Unpublish action is only enable for admin
        // TODO: check if a reviewer may have the privilege to publish to all 
        // (depends on if the user is reviewer for the metadata's groups)
        this.publicationToggleAction.setDisabled(!isEditable && !isHarvested && !isAdmin);
        if (isPublished) {
            // Update label and handler to unpublish
            this.publicationToggleAction.setText(OpenLayers.i18n('unpublish'));
        } else {
            this.publicationToggleAction.setText(OpenLayers.i18n('publish'));
        }
        
        
        this.statusAction.setDisabled((!isEditable && !isHarvested) || isReadOnly);
        this.enableWorkflowAction.setDisabled((!isEditable && !isHarvested) || isReadOnly);
        this.versioningAction.setDisabled((!isEditable && !isHarvested) || isReadOnly);
        this.categoryAction.setDisabled((!isEditable && !isHarvested) || isReadOnly);
        this.deleteAction.setDisabled((!isEditable && !isHarvested) || isReadOnly);
        this.duplicateAction.setDisabled(isReadOnly);
        this.createChildAction.setDisabled(!isEditable || isReadOnly);

        if (this.ratingWidget) {
            this.ratingWidget.reset();
            if ((isHarvested && harvesterType !== 'geonetwork') || isReadOnly) {
                /* TODO : add tooltip message to explain why */
                this.ratingWidget.disable();
            } else {
                this.ratingWidget.enable();
            }
        }
        
    },
    /** private: method[initComponent] 
     *  Initializes the metadata menu.
     */
    initComponent: function(){
        Ext.applyIf(this, this.defaultConfig);
        
        GeoNetwork.MetadataMenu.superclass.initComponent.call(this);
        
        this.statusStore = new GeoNetwork.data.StatusStore(this.catalogue.services.getStatus);
        this.statusStore.on('load', function () {
            this.create();
        }, this);
        this.statusStore.load();
    }
});

/** api: xtype = gn_MetadataMenu */
Ext.reg('gn_metadatamenu', GeoNetwork.MetadataMenu);
