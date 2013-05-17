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

/** api: (define) 
 *  module = GeoNetwork
 *  class = MetadataResultsToolbar
 *  base_link = `Ext.Toolbar <http://extjs.com/deploy/dev/docs/?class=Ext.Toolbar>`_
 *
 */
/** api: constructor 
 *  .. class:: MetadataResultsToolbar(config)
 *
 *     Create a metadata results tool bar which interact with
 *     :class:`GeoNetwork.MetadataResultsView`
 *
 *
 */
GeoNetwork.MetadataResultsToolbar = Ext.extend(Ext.Toolbar, {
    defaultConfig: {
        /** api: config[withPaging] 
         * ``boolean`` Add paging button. Default is false.
         */
        withPaging: false,
        /** api: config[searchCb] 
         * ``Function`` The search callback to call while paging
         */
        searchCb: null
    },
    /** api: config[catalogue] 
     * ``GeoNetwork.Catalogue`` Catalogue to use
     */
    catalogue: undefined,
    
    /** api: config[metadataResultsView] 
     * ``GeoNetwork.MetadataResultsView`` to interact with
     */
    metadataResultsView: undefined,
    
    /** property: config[mdSelectionInfoCmp] 
     *  Component to use to display selection information (number of selected records)
     */
    mdSelectionInfoCmp: undefined,
    
    /** api: config[mdSelectionAsMenu] 
     * ``boolean`` Display metadata selection with a menu or a set of button.
     */
    mdSelectionAsMenu: true,
    
    /** api: config[searchBtCmp] 
     *  Search button component use to trigger search
     *  when sort parameter change. Maybe passing
     *  search form and trigger submit could be better.
     */
    searchBtCmp: undefined,
    /** api: config[searchFormCmp] 
     *  Search form component use to trigger search
     *  when sort parameter change.
     */
    searchFormCmp: undefined,
    
    /** api: config[sortByCmp] 
     *  Sort by component to keep in synch with toolbar sort by combo.
     */
    sortByCmp: undefined,
    
    /** property: config[sortByCombo] 
     *  Sort by combo
     */
    sortByCombo: undefined,
    
    /**
     * Array of additionnal other Actions
     */
    customOtherActions: undefined,
    
    mdSelectionInfo: 'md-selection-info',
    
    selectionActions: [],
    
    deleteAction: undefined,
    
    otherItem: undefined,
    
    ownerAction: undefined,
    
    updateCategoriesAction: undefined,
    
    updatePrivilegesAction: undefined,
    
    updateStatusAction: undefined,
    
    updateVersionAction: undefined,
    
    createMetadataAction: undefined,
    
    newMetadataWindow: undefined,
    
    mdImportAction: undefined,
    
    adminAction: undefined,
    
    addLayerAction: undefined,
    
    permalinkProvider: undefined,
    
    actionMenu: undefined,
    
    item: null,
    
    actionOnSelectionMenu: undefined,
    
    admin: false,
    /** private: method[initComponent] 
     *  Initializes the toolbar results view.
     */
    initComponent: function(){
        Ext.applyIf(this, this.defaultConfig);
        
        var cmp = [];
        if (this.withPaging) {
            cmp.push(this.createPaging());
        }
        cmp.push(['->']);
        var sortOption = this.getSortByCombo();
        cmp.push(OpenLayers.i18n('sortBy'), sortOption);
        cmp.push(['|']);
        cmp.push(this.createTemplateMenu());
        cmp.push(this.createSelectionToolBar());
        cmp.push(this.createOtherActionMenu());
        
        // Permalink
        if(this.permalinkProvider) {
            var l = this.permalinkProvider.getLink;
            cmp.push(['|']);
            cmp.push(GeoNetwork.Util.buildPermalinkMenu(l, this.permalinkProvider));
        }
        
        GeoNetwork.MetadataResultsToolbar.superclass.initComponent.call(this);
        
        this.add(cmp);
        this.catalogue.on('selectionchange', this.updateSelectionInfo, this);
        this.catalogue.on('afterLogin', this.updatePrivileges, this);
        this.catalogue.on('afterLogout', this.updatePrivileges, this);
        
        Ext.Ajax.request({
 		   url: this.catalogue.services.mdSelect,
 		   success: function(response, opts) {
 			  var numSelected = response.responseXML.getElementsByTagName('Selected')[0].firstChild.nodeValue;
 			  this.updateSelectionInfo(this.catalogue, parseInt(numSelected));
 		  },
 		  scope:this
 		});
        
    },
    getSortByCombo: function(){
        var tb = this;
        
        this.sortByCombo = new Ext.form.ComboBox({
            mode: 'local',
            id: 'sortByToolBar',
            triggerAction: 'all',
            value: 'relevance',
            width: 130,
            store: GeoNetwork.util.SearchFormTools.getSortByStore(),
            valueField: 'id',
            displayField: 'name',
            listeners: {
                select: function(cb, record, idx){
                    if (this.sortByCmp) {
                        this.sortByCmp.setValue(cb.getValue());
                        
                        /* Adapt sort order according to sort field */
                        var tokens = cb.getValue().split('#');
                        Ext.getCmp('E_sortBy').setValue(tokens[0]);
                        if(Ext.getCmp('sortOrder')) {
                            Ext.getCmp('sortOrder').setValue(tokens[1]);
                        }
                    }
                    if (this.searchFormCmp) {
                        this.searchFormCmp.fireEvent('search');
                    } else if (this.searchBtCmp) {
                        this.searchBtCmp.fireEvent('click');
                    }
                },
                scope: tb
            }
        });
        
        return this.sortByCombo;
    },
    clickTemplateMenu: function(item, pressed){
        if (pressed) {
            this.applyTemplate(item.getId());
        }
        this.initRatingWidget();
    },
    createMassiveActionMenu: function(hide){
        /* FIXME : GUEST users should not see them */
        this.deleteAction = new Ext.menu.Item({
            text: OpenLayers.i18n('delete'),
            iconCls: 'cancel',
            id: 'deleteAction',
            handler: function(){
                this.catalogue.massiveOp('Delete', function() {
                    this.catalogue.metadataSelectNone();
                });
            },
            scope: this,
            hidden: hide
        });
        this.ownerAction = new Ext.menu.Item({
            text: OpenLayers.i18n('newOwner'),
            id: 'ownerAction',
            handler: function(){
                this.catalogue.massiveOp('NewOwner');
            },
            scope: this,
            hidden: hide
        });
        
        this.updateCategoriesAction = new Ext.menu.Item({
            text: OpenLayers.i18n('updateCategories'),
            id: 'updateCategoriesAction',
            handler: function(){
                this.catalogue.massiveOp('Categories');
            },
            scope: this,
            hidden: hide
        });
        this.updatePrivilegesAction = new Ext.menu.Item({
            text: OpenLayers.i18n('updatePrivileges'),
            id: 'updatePrivilegesAction',
            iconCls : 'privIcon',
            handler: function(){
                this.catalogue.massiveOp('Privileges');
            },
            scope: this,
            hidden: hide
        });
        this.updateStatusAction = new Ext.menu.Item({
            text: OpenLayers.i18n('updateStatus'),
            id: 'updateStatusAction',
            iconCls : 'statusIcon',
            handler: function(){
                this.catalogue.massiveOp('Status');
            },
            scope: this,
            hidden: hide
        });
        this.updateVersionAction = new Ext.menu.Item({
            text: OpenLayers.i18n('updateVersion'),
            id: 'updateVersionAction',
            iconCls : 'versioningIcon',
            handler: function(){
                this.catalogue.massiveOp('Versioning');
            },
            scope: this,
            hidden: hide
        });
        
        this.selectionActions.push(this.deleteAction, this.ownerAction, this.updateCategoriesAction, 
                this.updatePrivilegesAction, this.updateStatusAction, this.updateVersionAction);

        if(!this.catalogue.isReadOnly()) {
            this.actionMenu.addItem(this.ownerAction);
            this.actionMenu.addItem(this.updateCategoriesAction);
            this.actionMenu.addItem(this.updatePrivilegesAction);
            this.actionMenu.addItem(this.updateStatusAction);
            this.actionMenu.addItem(this.updateVersionAction);
            this.actionMenu.addItem(this.deleteAction);
        }

    },
    createPaging: function () {
        var self = this;
        var previousAction = new Ext.Action({
            id: 'previousBt',
            text: '&lt;&lt;',
            handler: function () {
                var from = catalogue.startRecord - parseInt(Ext.getCmp('E_hitsperpage').getValue(), 10);
                if (from > 0) {
                    catalogue.startRecord = from;
                    self.searchCb();
                }
            }
        });
        
        var nextAction = new Ext.Action({
            id: 'nextBt',
            text: '&gt;&gt;',
            handler: function () {
                catalogue.startRecord += parseInt(Ext.getCmp('E_hitsperpage').getValue(), 10);
                self.searchCb();
            }
        });
        
        return [previousAction, {
                xtype: 'tbtext',
                text: '',
                id: 'info'
            }, nextAction];
    },
    /** private: method[createAdminMenu] 
     *  Create quick admin action menu to not require to go to
     *  the admin page.
     */
    createAdminMenu: function(hide){
        this.actionMenu.addItem('-');
        this.otherItem = new Ext.menu.TextItem({
            html: '<b class="menu-title">' + OpenLayers.i18n('adminAction') + '</b>',
            hidden: true
        });
        this.actionMenu.addItem(this.otherItem);
        this.createMetadataAction = new Ext.menu.Item({
            text: OpenLayers.i18n('newMetadata'),
            ctCls: 'gn-bt-main',
            iconCls: 'addIcon',
            handler: function(){
                // FIXME : could be improved. Here we clean the window
                // A simple template reload could be enough probably
                if (this.newMetadataWindow) {
                    this.newMetadataWindow.close();
                    this.newMetadataWindow = undefined;
                }
                
                // Create a window to choose the template and the group
                if (!this.newMetadataWindow) {
                    var newMetadataPanel = new GeoNetwork.editor.NewMetadataPanel({
                                getGroupUrl: this.catalogue.services.getGroups,
                                catalogue: this.catalogue
                            });
                    
                    this.newMetadataWindow = new Ext.Window({
                        title: OpenLayers.i18n('newMetadataTitle'),
                        width: 600,
                        height: 420,
                        layout: 'fit',
                        modal: true,
                        items: newMetadataPanel,
                        closeAction: 'hide',
                        constrain: true,
                        iconCls: 'addIcon'
                    });
                }
                this.newMetadataWindow.show();
            },
            scope: this,
            hidden: hide
        });
        if(!this.catalogue.isReadOnly()) {
            this.actionMenu.addItem(this.createMetadataAction);
        }

        this.mdImportAction = new Ext.menu.Item({
            text: OpenLayers.i18n('importMetadata'),
            handler: function(){
                this.catalogue.metadataImport();
            },
            scope: this,
            hidden: hide
            });
        if(!this.catalogue.isReadOnly()) {
            this.actionMenu.addItem(this.mdImportAction);
        }

        this.adminAction = new Ext.menu.Item({
            text: OpenLayers.i18n('administration'),
            handler: function(){
                this.catalogue.admin();
            },
            scope: this,
            hidden: hide
            });
        this.actionMenu.addItem(this.adminAction);

    },
    /** private: method[createTemplateMenu] 
     *  Create template menu which is toggle menu with icon.
     *  Templates are defined in attached data view.
     *  Template icon is based on template key lower-cased.
     */
    createTemplateMenu: function(){
        var tpls = this.metadataResultsView.getTemplates();
        var data = [];
        var t;
        for (t in tpls) {
            if (tpls.hasOwnProperty(t)) {
                var tg = new Ext.Button({
                    text: '',
                    enableToggle: true,
                    toggleGroup: 'tpl',
                    id: t,
                    iconCls: 'mn-view-' + t.toLowerCase(),
                    toggleHandler: this.clickTemplateMenu,
                    scope: this.metadataResultsView,
                    pressed: (t === 'FULL' ? true : false)
                });
                data.push(tg);
            }
        }
        
        return [data, '|'];
    },
    /** private: method[createOtherActionMenu] 
     *
     */
    createOtherActionMenu: function(){
        this.actionMenu = new Ext.menu.Menu();
        
        /* Export action
         */
        var csvExportAction = new Ext.Action({
            text: OpenLayers.i18n('exportCsv'),
            handler: function(){
                this.catalogue.csvExport();
            },
            scope: this
        });
        
        var mefExportAction = new Ext.Action({
            text: OpenLayers.i18n('exportZip'),
            iconCls: 'md-mn-zip',
            handler: function(){
                this.catalogue.mefExport();
            },
            scope: this
        });
        
        var printAction = new Ext.Action({
                text: OpenLayers.i18n('printSel'),
                iconCls: 'md-mn-pdf',
                handler: function(){
                    var sortField;
                    var sortFieldOrder;

                    var sortByEl = Ext.getCmp('E_sortBy');

                    if (sortByEl) {
                        sortField =  sortByEl.getValue();

                        var sortByOrderEl = Ext.getCmp('sortOrder');
                        if (sortByOrderEl) {
                            sortFieldOrder = sortByOrderEl.getValue();
                        }

                    }

                    this.catalogue.pdfExport(sortField, sortFieldOrder);
                },
                scope: this
            });
        
        this.addLayerAction = new Ext.menu.Item({
            text: OpenLayers.i18n('addLayerSelection'),
            id: 'addLayerAction',
            iconCls : 'addLayerIcon',
            handler: function(){
            	Ext.Ajax.request({
            		url: this.catalogue.services.mdExtract,
            		success: function(response) {
            			var layers = response.responseXML.getElementsByTagName('layer');
            			var l=[];
            			app.switchMode('1', true);
            			for(var i=0;i<layers.length;i++) {
            				l.push([layers[i].getAttribute('title'), 
            				        layers[i].getAttribute('owsurl'), 
            				        layers[i].getAttribute('layername'), 
            				        layers[i].getAttribute('mdid')
            				 ]);
            			}
            			app.getIMap().addWMSLayer(l);
            		}
        		});
            },
            scope: this
        });
        
        this.selectionActions.push(mefExportAction, csvExportAction, printAction, this.addLayerAction);
        
        this.actionMenu.add(
            '<b class="menu-title">' + OpenLayers.i18n('onSelection') + '</b>',
            mefExportAction, 
            csvExportAction, 
            printAction,
            this.addLayerAction// ,{
        // text : 'Display selection only'
        // }
        );
        
        if(this.customOtherActions) {
        	this.actionMenu.add(this.customOtherActions);
        }
        this.createMassiveActionMenu(!this.catalogue.isIdentified());
        this.createAdminMenu(!this.catalogue.isIdentified());
        
        this.actionOnSelectionMenu = new Ext.Button({
            text: OpenLayers.i18n('otherActions'),
            menu: this.actionMenu
        });
        
        return this.actionOnSelectionMenu;
    },
    /** private: method[createSelectionToolBar] 
     *
     */
    createSelectionToolBar: function(){
        /* Selection action and events */
        var selectAllInPageAction = new Ext.Action({
            text: OpenLayers.i18n('allInPage'),
            handler: function(){
                this.catalogue.metadataSelectNone();
                /* Select currently selected nodes assuming catalogue action will work fine */
                this.metadataResultsView.selectAllInPage();
            },
            scope: this
        });
        var selectAllAction = new Ext.Action({
            text: OpenLayers.i18n('all'),
            handler: function(){
                this.catalogue.metadataSelectAll();
                this.metadataResultsView.selectAll();
            },
            scope: this
        });
        var selectNoneAction = new Ext.Action({
            text: OpenLayers.i18n('none'),
            handler: function(){
                this.catalogue.metadataSelectNone();
                this.metadataResultsView.selectNone();
            },
            scope: this
        });
        
        this.catalogue.on('selectionchange', this.updateSelectionInfo, this);
        
        /* Selection information status as a combo or a set of button */
        if (this.mdSelectionAsMenu) {
            return [{
                id: 'md-selection-info',
                text: OpenLayers.i18n('noneSelected'),
                menu : {
                    xtype: 'menu',
                    items: [
                        '<b class="menu-title">' + OpenLayers.i18n('select') + '</b>',
                        selectAllAction, 
                        selectAllInPageAction, 
                        selectNoneAction]
                }
            }];
        } else {
            return [{
                id: 'md-selection-info',
                xtype: 'tbtext',
                text: OpenLayers.i18n('noneSelected')
            }, '|', {
                xtype: 'tbtext',
                text: OpenLayers.i18n('select')
            }, selectAllInPageAction, {
                xtype: 'tbtext',
                text: ', '
            }, selectAllAction, {
                xtype: 'tbtext',
                text: ', '
            }, selectNoneAction
            ];
        }
    },
    /** api: method[updateSelectionInfo] 
     *  Update selection and selection information
     */
    updateSelectionInfo: function(catalogue, nb){
        if (!this.mdSelectionInfoCmp) {
            this.mdSelectionInfoCmp = Ext.getCmp(this.mdSelectionInfo);
        }
        
        Ext.each(this.selectionActions, function(e){
            if (nb === 0) {
                e.disable();
            } else {
                e.enable();
            }
        });
        
        this.mdSelectionInfoCmp && this.mdSelectionInfoCmp.setText(nb + ' ' + OpenLayers.i18n('selected'));
    },
    /** api: method[updatePrivileges] 
     *  Update privileges after user login
     */
    updatePrivileges: function(catalogue, user){
        var editingActions = [this.deleteAction, this.updateCategoriesAction, 
                        this.updatePrivilegesAction, this.createMetadataAction,
                        this.mdImportAction],
            adminActions = [this.ownerAction],
            actions = [this.adminAction, this.otherItem];
        
        Ext.each(actions, function(){
            this.setVisible(user);
        });
        // Do not display editing action for registered users
        Ext.each(editingActions, function(){
            this.setVisible(user && user.role !== 'RegisteredUser');
        });
        // Change owners are only available for admins (#781)
        Ext.each(adminActions, function(){
            this.setVisible(user && (user.role === 'Administrator' || user.role === 'UserAdmin'));
        });
    },
    /** private: method[onDestroy] 
     *
     *  Private method called during the destroy
     *  sequence.
     *
     *  TODO : what to do in destroy ?
     */
    onDestroy: function(){
//        if (this.mdSelectionInfoCmp) {
//            // TODO ?
//        }
        GeoNetwork.MetadataResultsToolbar.superclass.onDestroy.apply(this, arguments);
    }
});

/** api: xtype = gn_metadataresultstoolbar */
Ext.reg('gn_metadataresultstoolbar', GeoNetwork.MetadataResultsToolbar);
