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
    editAction: undefined,
    deleteAction: undefined,
    zoomToAction: undefined,
    otherActions: undefined,
    adminMenuSeparator: undefined,
    duplicateAction: undefined,
    createChildAction: undefined,
    statusAction: undefined,
    cancelEditSessionAction: undefined,
    changeEditSessionOwnerAction: undefined,
    versioningAction: undefined,
    adminAction: undefined,
    categoryAction: undefined,
    viewAction: undefined,
    viewWorkspaceCopyAction: undefined,
    diffWorkspaceCopyAction: undefined,
    viewOriginalCopyAction: undefined,
    diffOriginalCopyAction: undefined,
    diffOriginalCopyEditModeAction: undefined,
    printAction: undefined,
    printWorkspaceCopyAction: undefined,
    viewXMLAction: undefined,
    getMEFAction: undefined,
    ratingWidget: undefined,
    defaultConfig: {},
    /** private: method[setRecord] 
     *  Create the menu.
     *  
     */
    create: function(){
        /* Edit menu */
        /* TODO : only displayer for logged in users */
        this.editAction = new Ext.Action({
            text: OpenLayers.i18n('edit'),
            iconCls: 'md-mn-edit',
            handler: function(){
                var id = this.record.get('id');
                this.catalogue.metadataEdit(id);
            },
            scope: this
        });
        this.add(this.editAction);
        this.deleteAction = new Ext.Action({
            text: OpenLayers.i18n('delete'),
            iconCls: 'md-mn-del',
            handler: function(){
                var id = this.record.get('uuid');
                this.catalogue.metadataDelete(id);
            },
            scope: this
        });
        this.add(this.deleteAction);
        
        /* Other actions */
        this.duplicateAction = new Ext.Action({
            text: OpenLayers.i18n('duplicate'),
            iconCls: 'md-mn-copy',
            handler: function(){
                var id = this.record.get('id');
                GeoNetwork.editor.EditorTools.showNewMetadataWindow(this, id, OpenLayers.i18n('duplicate'), false);
            },
            scope: this
        });
        this.createChildAction = new Ext.Action({
            text: OpenLayers.i18n('createChild'),
            iconCls: 'childIcon',
            handler: function(){
                var id = this.record.get('id');
                GeoNetwork.editor.EditorTools.showNewMetadataWindow(this, id, OpenLayers.i18n('createChild'), true);
            },
            scope: this
        });
        // FIXME : tooltip for actions does not work
        this.adminAction = new Ext.Action({
            text: OpenLayers.i18n('privileges'),
            tooltip: OpenLayers.i18n('privilegesTT'),
            iconCls : 'privIcon',
            handler: function(){
                var id = this.record.get('id');
                this.catalogue.metadataAdmin(id);
            },
            scope: this
        });
        this.cancelEditSessionAction = new Ext.Action({
            text: OpenLayers.i18n('cancelEditSession'),
            tooltip: OpenLayers.i18n('cancelEditSessionTT'),
            iconCls : 'unlockIcon',
            handler: function(){
                var id = this.record.get('id');
                this.catalogue.metadataCancelEditSession(id);
            },
            scope: this
        });
        this.changeEditSessionOwnerAction = new Ext.Action({
            text: OpenLayers.i18n('changeEditSessionOwner'),
            tooltip: OpenLayers.i18n('changeEditSessionOwnerTT'),
            iconCls : '',
            handler: function(){
                var id = this.record.get('id');
                var lockedBy = this.record.get('lockedBy');
                this.catalogue.metadataChangeEditSessionOwner(id, lockedBy);
            },
            scope: this
        });

        this.statusAction = new Ext.Action({
            text: OpenLayers.i18n('status'),
            tooltip: OpenLayers.i18n('statusTT'),
            iconCls : 'statusIcon',
            handler: function(){
                var id = this.record.get('id');
                this.catalogue.metadataStatus(id);
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
            //iconCls : 'md-mn-copy',
            handler: function(){
                var id = this.record.get('id');
                this.catalogue.metadataCategory(id);
            },
            scope: this
        });
        
        this.otherActions = new Ext.menu.Item({
            text: OpenLayers.i18n('otherActions'),
            menu: {
                items: [this.duplicateAction, this.createChildAction, this.adminAction, this.cancelEditSessionAction, this.changeEditSessionOwnerAction, this.statusAction, this.versioningAction, this.categoryAction]
            }
        });
        this.add(this.otherActions);
        
        this.adminMenuSeparator = new Ext.menu.Separator();
        this.add(this.adminMenuSeparator);
        
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
        this.add(this.viewAction);
        
        this.viewWorkspaceCopyAction = new Ext.Action({
            text: OpenLayers.i18n('viewWorkspaceCopy'),
            tooltip: OpenLayers.i18n('viewWorkspaceCopyTT'),
            iconCls : 'md-mn-view',
            handler: function(){
                var id = this.record.get('uuid');
                this.catalogue.metadataWorkspaceCopyShow(id);
            },
            scope: this
        });
        this.add(this.viewWorkspaceCopyAction);
        

        this.diffWorkspaceCopyAction = new Ext.Action({
            text: OpenLayers.i18n('diffWorkspaceCopy'),
            tooltip: OpenLayers.i18n('diffWorkspaceCopyTT'),
            iconCls : 'md-mn-view',
            handler: function(){
                var id = this.record.get('id');
                this.catalogue.metadataWorkspaceCopyDiff(id);
            },
            scope: this
        });
        this.add(this.diffWorkspaceCopyAction);

        this.viewOriginalCopyAction = new Ext.Action({
            text: OpenLayers.i18n('viewOriginalCopy'),
            tooltip: OpenLayers.i18n('viewOriginalCopyTT'),
            iconCls : 'md-mn-view',
            handler: function(){
                var id = this.record.get('uuid');
                this.catalogue.metadataShow(id);
            },
            scope: this
        });
        this.add(this.viewOriginalCopyAction);

        this.diffOriginalCopyAction = new Ext.Action({
            text: OpenLayers.i18n('diffOriginalCopy'),
            tooltip: OpenLayers.i18n('diffOriginalCopyTT'),
            iconCls : 'md-mn-view',
            handler: function(){
                var id = this.record.get('id');
                this.catalogue.metadataWorkspaceCopyDiff(id,false);
            },
            scope: this
        });

        this.add(this.diffOriginalCopyAction);

        this.diffOriginalCopyEditModeAction = new Ext.Action({
            text: OpenLayers.i18n('diffOriginalCopyEditMode'),
            tooltip: OpenLayers.i18n('diffOriginalCopyEditModeTT'),
            iconCls : 'md-mn-view',
            handler: function(){
                var id = this.record.get('id');
                this.catalogue.metadataWorkspaceCopyDiff(id,true);
            },
            scope: this
        });

        this.add(this.diffOriginalCopyEditModeAction);

        this.zoomToAction = new Ext.Action({
            text: OpenLayers.i18n('zoomTo'),
            iconCls: 'zoomlayer',
            handler: function(){
                var uuid = this.record.get('uuid');
                this.resultsView.zoomTo(uuid);
            },
            scope: this
        });
        this.add(this.zoomToAction);
        
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
        this.add(this.viewXMLAction);
        
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
        this.add(this.viewRDFAction);
        
        this.printAction = new Ext.Action({
                text: OpenLayers.i18n('printSel'),
                iconCls: 'md-mn-pdf',
                handler: function(){
                    this.catalogue.metadataPrint(this.record.get('uuid'), false);
                },
                scope: this
            });
        this.add(this.printAction);
        
        this.printWorkspaceCopyAction = new Ext.Action({
            text: OpenLayers.i18n('printSelWorkspaceCopy'),
            iconCls: 'md-mn-pdf',
            handler: function(){
                this.catalogue.metadataPrint(this.record.get('uuid'), true);
            },
            scope: this
        });
        this.add(this.printWorkspaceCopyAction);

        this.getMEFAction = new Ext.Action({
            text: OpenLayers.i18n('getMEF'),
            iconCls: 'md-mn-zip',
            handler: function(){
                this.catalogue.metadataMEF(this.record.get('uuid'));
            },
            scope: this
        });
        this.add(this.getMEFAction);
        
        
        
        /* Rating menu */
        if (Ext.ux.RatingItem) { // Check required widget are loaded before displaying context menu
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
            this.add(this.ratingWidget);
        }
        
        /* TODO : add categories / privileges / create child */
        this.updateMenu();
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
            isHarvested = this.record.get('isharvested') === 'y' ? true : false,
            harvesterType = this.record.get('harvestertype'),
            identified = this.catalogue.isIdentified();

        var isSymbolicLocking  = this.record.get('symbolicLocking') === 'y';
        var isLocked = this.record.get('locked') === 'y';
        var isWorkspace = this.record.get('workspace') === 'true';
        var isOwner = this.record.get('owner') === 'true';
        var sameOwnerName = (this.catalogue.identifiedUser) && (this.record.get('ownername') === this.catalogue.identifiedUser.username);

        var sameLockedByAndLoggedUser = (this.catalogue.identifiedUser) && (this.record.get('lockedBy') === this.catalogue.identifiedUser.id);

        var isReviewer = (this.catalogue.identifiedUser) && (this.catalogue.identifiedUser.role === 'Reviewer');
        var isAdmin = (this.catalogue.identifiedUser) && (this.catalogue.identifiedUser.role === 'Administrator');
        var isUserAdmin = (this.catalogue.identifiedUser) && (this.catalogue.identifiedUser.role === 'UserAdmin');
        var isEditor = (this.catalogue.identifiedUser) && (this.catalogue.identifiedUser.role === 'Editor');
        var isUserRegistered = (this.catalogue.identifiedUser) && (this.catalogue.identifiedUser.role === 'RegisteredUser');

        /* Actions and menu visibility for logged in user */
        if (!identified || isUserRegistered) {
            this.editAction.hide();
            this.deleteAction.hide();
            this.viewWorkspaceCopyAction.hide();
            this.printWorkspaceCopyAction.hide();
            this.diffWorkspaceCopyAction.hide();
            this.viewOriginalCopyAction.hide();
            this.diffOriginalCopyAction.hide();
            this.diffOriginalCopyEditModeAction.hide();
        } else {
            this.editAction.show();
            this.deleteAction.show();
            this.viewAction.show();

            if (!isLocked) {
                this.viewWorkspaceCopyAction.hide();
                this.printWorkspaceCopyAction.hide();
                this.diffWorkspaceCopyAction.hide();

                this.viewOriginalCopyAction.hide();
                this.diffOriginalCopyAction.hide();
                this.diffOriginalCopyEditModeAction.hide();
            } else {
                if(!isWorkspace) {
                    this.viewOriginalCopyAction.hide();
                    this.diffOriginalCopyAction.hide();
                    this.diffOriginalCopyEditModeAction.hide();
                    this.viewWorkspaceCopyAction.show();
                    this.printWorkspaceCopyAction.show();
                    this.diffWorkspaceCopyAction.show();
                }
                else {
                    this.viewAction.hide();
                    //this.viewWorkspaceCopyAction.hide();
                    this.printWorkspaceCopyAction.hide();
                    this.diffWorkspaceCopyAction.hide();

                    this.viewWorkspaceCopyAction.show();
                    this.viewOriginalCopyAction.show();
                    this.diffOriginalCopyAction.show();
                    this.diffOriginalCopyEditModeAction.show();
                }
            }
        }

        if (isReviewer || isAdmin || isUserAdmin || (isEditor && isSymbolicLocking)) {
            this.changeEditSessionOwnerAction.show();
        } else {
            this.changeEditSessionOwnerAction.hide();
        }

        this.otherActions.setVisible(identified && !isUserRegistered);
        this.adminMenuSeparator.setVisible(identified && !isUserRegistered);

        
        /* Actions status depend on records */
        this.editAction.setDisabled(!((isLocked && sameLockedByAndLoggedUser) || (!isLocked && isEditable)));
        this.adminAction.setDisabled(!(!isLocked || sameLockedByAndLoggedUser));
        this.statusAction.setDisabled(!(isOwner && (!isHarvested || GeoNetwork.Settings.editor.editHarvested)));
        this.cancelEditSessionAction.setDisabled(!(isLocked && (isAdmin || sameLockedByAndLoggedUser)));
        this.changeEditSessionOwnerAction.setDisabled(!(isLocked && (isOwner || (isEditable && isSymbolicLocking))));
        this.viewWorkspaceCopyAction.setDisabled(!(isLocked && isEditable));
        this.diffWorkspaceCopyAction.setDisabled(!(isLocked && isEditable));
        this.viewOriginalCopyAction.setDisabled(!(isLocked && isEditable));
        this.diffOriginalCopyAction.setDisabled(!(isLocked && isEditable));
        this.diffOriginalCopyEditModeAction.setDisabled(!(isLocked && isEditable));
        this.printWorkspaceCopyAction.setDisabled(!(isLocked && isEditable));
        this.versioningAction.setDisabled(!((!isLocked || sameLockedByAndLoggedUser)  && (!isHarvested || GeoNetwork.Settings.editor.editHarvested)));

        this.deleteAction.setDisabled(!((isLocked && sameLockedByAndLoggedUser) || (!isLocked && isEditable)));
        
        if (this.ratingWidget) {
            this.ratingWidget.reset();
            if (isHarvested && harvesterType !== 'geonetwork') {
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
        
        this.create();
    }
});

/** api: xtype = gn_MetadataMenu */
Ext.reg('gn_metadatamenu', GeoNetwork.MetadataMenu);
