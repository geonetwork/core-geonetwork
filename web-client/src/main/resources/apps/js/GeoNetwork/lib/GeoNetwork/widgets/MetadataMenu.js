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
    versioningAction: undefined,
    adminAction: undefined,
    categoryAction: undefined,
    viewAction: undefined,
    printAction: undefined,
    getMEFAction: undefined,
    ratingWidget: undefined,
    wmsLinks: undefined,
    kmlLinks: undefined,
    inks: undefined,
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
                var uuid = this.record.get('uuid');
                this.catalogue.metadataDuplicate(uuid);
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
        /*this.statusAction = new Ext.Action({
            text: OpenLayers.i18n('status'),
            tooltip: OpenLayers.i18n('statusTT'),
            iconCls : 'statusIcon',
            handler: function(){
                var id = this.record.get('id');
                this.catalogue.metadataStatus(id);
            },
            scope: this
        });*/
        // TODO : enable only if SVN manager is on.
        /*this.versioningAction = new Ext.Action({
            text: OpenLayers.i18n('versioning'),
            tooltip: OpenLayers.i18n('versioningTT'),
            iconCls : 'versioningIcon',
            handler: function(){
                var id = this.record.get('id');
                this.catalogue.metadataVersioning(id);
            },
            scope: this
        });*/
        this.categoryAction = new Ext.Action({
            text: OpenLayers.i18n('categories'),
            //iconCls : 'md-mn-copy',
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

        this.viewISO19139Action = new Ext.Action({
            text: OpenLayers.i18n('saveXmlIso19139'),
            iconCls: 'xmlIcon',
            handler: function(){
                var id = this.record.get('uuid');
                this.catalogue.metadataXMLShow(id, 'iso19139');
            },
            scope: this
        });

        this.viewGM03Action = new Ext.Action({
         text: OpenLayers.i18n('saveGM03'),
         iconCls: 'xmlIcon',
         handler: function(){
             var id = this.record.get('uuid');
             this.catalogue.metadataGM03Show(id);
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
        // Swisstopo: Disable rating
        if ((Ext.ux.RatingItem) && (false)) { // Check required widget are loaded before displaying context menu
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

                    
/**
 * private: method[dislayLinks] Create link menu in the div for each records
 * Swisstopo specific
 * 
 */
    displayLinks : function(r) {
        var id = r.get('id');
        var uuid = r.get('uuid');

        var wmsLinks = [];
        var kmlLinks = [];
        var links = [];

        Ext
                .each(
                        r.get('links'),
                        function(record) {
                            // Avoid empty URL
                            if (record.href !== '') {
                                var currentType = record.type;

                                if (currentType === 'application/vnd.ogc.wms_xml'
                                        || currentType === 'OGC:WMS') {

                                    var handler = function() {
                                        app.mapApp
                                                .addWMSLayer([ [
                                                        record.title,
                                                        record.href,
                                                        record.name,
                                                        uuid ] ]);
                                    };
                                    wmsLinks
                                            .push(new Ext.Action(
                                                    {
                                                        text : record.title
                                                                || record.name,
                                                        handler : handler
                                                    }));

                                    var kmlLink = catalogue.URL
                                            + '/srv/'
                                            + catalogue.LANG
                                            + "/google.kml?uuid="
                                            + uuid + "&layers="
                                            + record.href;
                                    kmlLinks
                                            .push(new Ext.Action(
                                                    {
                                                        text : (record.title || record.name),
                                                        href : kmlLink
                                                    }));
                                }
                                if (currentType === 'OGC:KML') {
                                    kmlLinks
                                            .push(new Ext.Action(
                                                    {
                                                        text : (record.title || record.name),
                                                        href : record.href
                                                    }));

                                }

                                // Add the download 
                                // button
                                if (currentType === 'application/x-compressed' ) {
                                    links
                                            .push(new Ext.Action(
                                                    {
                                                        text : (record.title || record.name || record.href),
                                                        handler : function() {
                                                            catalogue
                                                                    .metadataPrepareDownload(id);
                                                        }
                                                    }));
                                }
                                // Add the download 
                                // button
                                if ( currentType === 'WWWLINK') {
                                    links
                                            .push(new Ext.Action(
                                                    {
                                                        text : (record.title || record.name || record.href),
                                                        handler: function() {
                                                            window.open(record.href, '');
                                                        }
                                                    }));
                                }
                            }

                        });

        if (wmsLinks.length > 0) {
            this.wmsLinks = new Ext.menu.Item({
                text : OpenLayers.i18n('WMS Layer'),
                iconCls : 'addLayer',
                menu : {
                    items : wmsLinks
                }
            });
        }

        if (kmlLinks.length > 0) {
            this.kmlLinks = new Ext.menu.Item({
                text : OpenLayers.i18n('KML Layer'),
                iconCls : 'md-mn-kml',
                menu : {
                    items : kmlLinks
                }
            });
        }

        if (links.length > 0) {
            this.links = new Ext.menu.Item({
                text : OpenLayers.i18n('Link'),
                iconCls : 'WWWLINK',
                menu : {
                    items : links
                }
            });
        }
    },
   
    composeMenu: function(){
        this.add(this.editAction);
        this.add(this.deleteAction);
        
        this.otherActions = new Ext.menu.Item({
            text: OpenLayers.i18n('otherActions'),
            menu: {
                items: [this.duplicateAction, this.createChildAction, this.adminAction, this.categoryAction] // this.statusAction,
                                                                                                                // this.versioningAction,
            }
        });
        this.add(this.otherActions);
        this.add(this.adminMenuSeparator);
        
        this.add(this.viewAction);
        this.add(this.zoomToAction);
        
        this.exportActions = new Ext.menu.Item({
            text: OpenLayers.i18n('export'),
            menu: {
                items: [this.viewXMLAction, this.viewISO19139Action, this.viewGM03Action,
                        this.viewRDFAction, this.printAction, this.getMEFAction],
                                                                                                                // this.versioningAction,
            }
        });
        
        this.add(this.exportActions);
        
        /* Rating menu */
        if (Ext.ux.RatingItem && this.ratingWidget) { // Check required widget are loaded before displaying context menu
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
            isHarvested = this.record.get('isharvested') === 'y' ? true : false,
            harvesterType = this.record.get('harvestertype'),
            identified = this.catalogue.isIdentified() && 
                (this.catalogue.identifiedUser && this.catalogue.identifiedUser.role !== 'RegisteredUser');

        /* Actions and menu visibility for logged in user */
        if (!identified) {
            this.editAction.hide();
            this.deleteAction.hide();
        } else {
            this.editAction.show();
            this.deleteAction.show();
        }
        if(this.otherActions) this.otherActions.setVisible(identified);
        this.adminMenuSeparator.setVisible(identified);
        
        /* Actions status depend on records */
        this.editAction.setDisabled(!isEditable);
        this.adminAction.setDisabled(!isEditable && !isHarvested);
        //this.statusAction.setDisabled(!isEditable && !isHarvested);
        //this.versioningAction.setDisabled(!isEditable && !isHarvested);
        this.categoryAction.setDisabled(!isEditable && !isHarvested);
        this.deleteAction.setDisabled(!isEditable && !isHarvested);
        
        if (this.ratingWidget) {
            this.ratingWidget.reset();
            if (isHarvested && harvesterType !== 'geonetwork') {
                /* TODO : add tooltip message to explain why */
                this.ratingWidget.disable();
            } else {
                this.ratingWidget.enable();
            }
        }
        
        this.remove(this.wmsLinks);
        this.remove(this.kmlLinks);
        this.remove(this.links);
        

        this.wmsLinks = null;
        this.kmlLinks = null;
        this.links = null;
        
        this.displayLinks(this.record);

        if(this.wmsLinks){
            this.add(this.wmsLinks);
        }
        if(this.kmlLinks){
            this.add(this.kmlLinks);
        }
        if(this.links){
            this.add(this.links);
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
