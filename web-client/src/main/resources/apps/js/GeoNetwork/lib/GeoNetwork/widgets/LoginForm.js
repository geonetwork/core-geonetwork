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
 * @require Catalogue.js
 */
/** api: (define)
 *  module = GeoNetwork
 *  class = LoginForm
 *  base_link = `Ext.FormPanel <http://extjs.com/deploy/dev/docs/?class=Ext.FormPanel>`_
 */
/** api: constructor 
 *  .. class:: LoginForm(config)
 *
 *     Create a GeoNetwork login form.
 */
/** api: example
 *
 *
 *  .. code-block:: javascript
 *  
 *     var loginForm2 = new GeoNetwork.LoginForm({
 *        renderTo: 'login-form',
 *        id: 'loginForm',
 *        catalogue: catalogue,
 *        layout: 'hbox'
 *      });
 *      
 *      ...
 */
GeoNetwork.LoginForm = Ext.extend(Ext.FormPanel, {
    url: '',
    /** api: config[catalogue] 
     * ``GeoNetwork.Catalogue`` Catalogue to use
     */
    catalogue: undefined,
    defaultConfig: {
        border: false,
        layout: 'form',
        id: 'loginForm',
        /** api: config[displayLabels] 
         * In hbox layout, labels are not displayed, set to true to display field labels.
         */
        hideLoginLabels: true,
        /** api: config[withUserMenu] 
         * Create complete user menu with access to profile administration, quick search links
         * and admin functionnalities.
         */
        withUserMenu: false,
        /** api: config[searchForm] 
         * The search form to use for quick search menu in user menu.
         */
        searchForm: undefined,
        width: 340,
        /** api: config[userInfoTpl] 
         * Template to render user login name
         */
        userInfoTpl: new Ext.XTemplate('<tpl for=".">', 
                '<img title="Avatar" class="gn-avatar" src="http://gravatar.com/avatar/{hash}?s=18"/>',
                '<span class="gn-login">{name} {surname}</span><br/>',
                '</tpl>'),
        /** api: config[searchForm] 
         * Template to render user information in tooltip user profile section
         */
        userInfoToolTipTpl: new Ext.XTemplate('<tpl for=".">', 
                '<span class="gn-login">{name} {surname}</span><br/>',
                '<span class="gn-role">{[OpenLayers.i18n(values.role)]}</span><br/>',
                '<img title="Avatar" class="gn-avatar" src="http://gravatar.com/avatar/{hash}?s=80"/>',
                '</tpl>')
    },
    defaultType: 'textfield',
    /** private: property[userInfo]
     * Use to display user information (name, password, profil).
     */
    userInfo: undefined,
    userInfoTooltip: undefined,
    linksPanel: undefined,
    tooltipMenu: undefined,
    harvestingMenu: undefined,
    importMetadataMenu: undefined,
    newMetadataMenu: undefined,
    username: undefined,
    password: undefined,
    /** private: property[toggledFields]
     * List of fields to hide on login.
     */
    toggledFields: [],
    /** private: property[toggledFields]
     * List of fields to display on login.
     */
    toggledFieldsOff: [],
    /** private: method[initComponent] 
     *  Initializes the login form results view.
     */

    keys: [{
        key: [Ext.EventObject.ENTER], 
        handler: function () {
            Ext.getCmp('btnLoginForm').fireEvent('click');
        }
    }],
    createQuickLinks: function () {
        
        var quickLinks = GeoNetwork.Settings.userQuickLinks || {'Editor': [{
                label : OpenLayers.i18n('myMetadata'),
                criteria : {"E__owner" : this.catalogue.identifiedUser.id}
            }, {
                label : OpenLayers.i18n("myDraft"),
                criteria : {"E__owner" : this.catalogue.identifiedUser.id, "E__status" : "1"}  // My draft
            }, {
                label : OpenLayers.i18n('templates'),
                criteria : {"E_template" : "y"}
            }],
        'Reviewer': [{
                label : OpenLayers.i18n('myMetadata'),
                criteria : {"E__owner" : this.catalogue.identifiedUser.id}
            }, {
                label : OpenLayers.i18n("Draft"),
                criteria : {"E__status" : "1"}  // Draft
            }, {
                label : OpenLayers.i18n("LastSubmitted"),
                criteria : {"E__status" : "4", "E_sortBy" : "changeDate"}  // Submitted
            }], 
        'RegisteredUser': [{
                label : OpenLayers.i18n("lastUpdates"),
                criteria : {"E_sortBy" : "changeDate"}
        }],
        'Administrator': [{
                label : OpenLayers.i18n('myMetadata'),
                criteria : {"E__owner" : this.catalogue.identifiedUser.id, "E__isHarvested" : "n"}
            }, {
                label : OpenLayers.i18n("lastUpdates"),
                criteria : {"E_sortBy" : "changeDate"}
            }, {
                label : OpenLayers.i18n("RecordWithIndexingError"),
                criteria : {"E__indexingError" : "1"}
            }, {
                label : OpenLayers.i18n('fromHarvestedCatalog'),
                criteria : {"E__isHarvested" : "y"}
            }]
        };
        this.linksPanel.removeAll();
        
        var userLinks = quickLinks[this.catalogue.identifiedUser.role];
        if (!userLinks) {
            return;
        }
        
        this.linksPanel.add(new Ext.form.Label({text: OpenLayers.i18n('QuickSearch')}));
        
        var searchForm = this.searchForm;
        var handler = function () {
            searchForm.reset({nosearch: true});
            GeoNetwork.util.SearchTools.populateFormFromParams(searchForm, Ext.apply({}, this), true);
            searchForm.search();
        };
        
        for ( var i = 0; i < userLinks.length; i++) {
            var criteria = userLinks[i].criteria, label = userLinks[i].label;
            
            this.linksPanel.add(new Ext.Button({
                text: label,
                listeners: {
                    click: handler,
                    scope: criteria
                }
            }));
        }
        this.linksPanel.doLayout();
    },
    createUserMenu: function () {
        var panel = this;
        
        this.linksPanel = new Ext.Panel();
        
        this.userInfoTooltip = new Ext.Panel({
            columnWidth : .50,
            tpl: this.userInfoToolTipTpl,
            listeners: {
                'render': function () {
                    this.userInfoTooltip.update(this.catalogue.identifiedUser);
                },
                scope: panel
            }
        });
        
        // Create user link menu
        var userItems = [];
        
        if (!this.catalogue.casEnabled) {
            // Add update user info or password - if CAS, LDAP is used for that
            userItems.push(new Ext.Button({
                text: OpenLayers.i18n('updateUserInfo'),
                listeners: {
                    click: function () {
                        this.catalogue.moveToURL(this.catalogue.services.updateUserInfo + this.catalogue.identifiedUser.username);
                    },
                    scope: this
                }
            }));
        }
        
        userItems.push(new Ext.Button({
            text: OpenLayers.i18n('logout'),
            renderTo: Ext.get('admin-menu'),
            iconCls: 'md-mn mn-logout',
            listeners: {
                click: function () {
                    this.catalogue.logout();
                },
                scope: this
            }
        }));
        
        var userPanel = {
                xtype: 'panel',
                layout: 'column',
                defaults: {
                    border: false
                },
                items: [this.userInfoTooltip, {
                        items: userItems
                    }
                    
                ]
            };
        
        this.harvestingMenu = new Ext.Button({
            text: OpenLayers.i18n('harvestingAdmin'),
            listeners: {
                click: function () {
                    this.catalogue.moveToURL(this.catalogue.services.harvestingAdmin)
                },
                scope: this
            }
        });
        this.newMetadataMenu = new Ext.Button({
            text: OpenLayers.i18n('newMetadata'),
            ctCls: 'gn-bt-main',
            iconCls: 'addIcon',
            listeners: {
                click: function () {
                    if (catalogue.isIdentified()) {
                        var actionCtn = Ext.getCmp('resultsPanel').getTopToolbar();
                        actionCtn.createMetadataAction.handler.apply(actionCtn);
                    }
                },
                scope: this
            }
        });
        this.importMetadataMenu = new Ext.Button({
            text: OpenLayers.i18n('importMetadata'),
            handler: function () {
                var actionCtn = Ext.getCmp('resultsPanel').getTopToolbar();
                actionCtn.mdImportAction.handler.apply(actionCtn);
            }
        });
        var adminPanel = {
                xtype: 'panel',
                items: [
                    this.newMetadataMenu,
                    this.importMetadataMenu,
                    this.harvestingMenu,
                    new Ext.Button({
                        text: OpenLayers.i18n('administration'),
                        //iconCls : 'md-mn md-mn-advanced',
                        listeners: {
                            click: function () {
                                this.catalogue.admin();
                            },
                            scope: this
                        }
                    })
                ]
            };
        
        this.tooltipMenu = new Ext.ToolTip({
            id: 'user-info-tip',
            target: 'login-form',
            anchor: 'bottom',
            width: 300,
            autoHide: false,
            bodyBorder: false,
            border: false,
            defaults: {
                border: false
            },
            items: [userPanel, this.linksPanel, adminPanel]
        });
        var toolTip = this.tooltipMenu;
        toolTip.on('show', function () {
            toolTip.getEl().on('click', function () {
                toolTip.hide();
            });
        });
    },
    initComponent: function () {
        Ext.applyIf(this, this.defaultConfig);

        var form = this;
        var loginBt = new Ext.Button({
                width: 85,
                text: OpenLayers.i18n('login'),
                iconCls: 'md-mn mn-login',
                id: 'btnLoginForm',
                listeners: {
                    click: function () {
                        this.catalogue.login(this.username.getValue(), this.password.getValue());
                    },
                    scope: form
                }
            }),
            adminBt = new Ext.Button({
                width: 75,
                text: OpenLayers.i18n('administration'),
                //iconCls : 'md-mn md-mn-advanced',
                listeners: {
                    click: function () {
                        this.catalogue.admin();
                    },
                    scope: this
                }
            }),
            logoutBt = new Ext.Button({
                width: 95,
                text: OpenLayers.i18n('logout'),
                iconCls: 'md-mn mn-logout',
                listeners: {
                    click: function () {
                        this.catalogue.logout();
                    },
                    scope: this
                }
            });
        this.username = new Ext.form.TextField({
            id: 'username',
            name: 'username',
            width: 85,
            autoCreate: {tag: 'input'},
            hideLabel: false,
            allowBlank: false,
            fieldLabel: OpenLayers.i18n('username'),
            emptyText: OpenLayers.i18n('username')
        });
        this.password = new Ext.form.TextField({
            name: 'password',
            width: 85,
            hideLabel: false,
            allowBlank: false,
            fieldLabel: OpenLayers.i18n('password'),
            emptyText: OpenLayers.i18n('password'),
            inputType: 'password'
        });
        this.userInfo = new Ext.form.Label({
            width: 170,
            tpl: this.userInfoTpl,
            cls: 'loginInfo',
            listeners: {
                'render': function () {
                    this.userInfo.update(this.catalogue.identifiedUser);
                },
                scope: form
            }
        });
        
        
        
        if (this.hideLoginLabels) {
            this.toggledFields.push( 
                    this.username,
                    this.password,
                    loginBt);
        } else if (this.withUserMenu) {
            this.toggledFields.push( 
                    this.username,
                    this.password,
                    loginBt);
        } else {
            // hbox layout does not display TextField labels, create a label then
            var usernameLb = new Ext.form.Label({html: OpenLayers.i18n('username')}),
                passwordLb = new Ext.form.Label({html: OpenLayers.i18n('password')});
        
            this.toggledFields.push(usernameLb, 
                    this.username,
                    passwordLb,
                    this.password,
                    loginBt);
        }
        
        if (this.withUserMenu) {
            this.toggledFieldsOff.push(this.userInfo);
            this.createUserMenu();
        } else {
            this.toggledFieldsOff.push(this.userInfo, 
                    logoutBt);
            if (this.catalogue.adminAppUrl !== '') {
                this.toggledFieldsOff.push(adminBt);
            }
        }
        
        
        this.items = [this.toggledFields, this.toggledFieldsOff];
        GeoNetwork.LoginForm.superclass.initComponent.call(this);
        
        // check user on startup with a kind of ping service
        var loggedIn = this.catalogue.isLoggedIn();
        this.login(this.catalogue, loggedIn); // FIXME : login expect a user not a boolean
        this.catalogue.on('afterLogin', this.login, this);
        this.catalogue.on('afterLogout', this.login, this);

    },
    
    /** private: method[login]
     *  Update layout according to login/out operation
     */
    login: function (cat, user) {
        var status = user ? true : false;
        var loginForm = this; 
        Ext.each(this.toggledFields, function(item) {
        	var visible = !status;
        	if (item == loginForm.password || item == loginForm.username) {
        		visible = visible && !cat.casEnabled
        	}
        	item.setVisible(visible);
        });
        Ext.each(this.toggledFieldsOff, function (item) {
            item.setVisible(status);
        });
        
        this.doLayout(false, true);
        
        if (cat.identifiedUser && cat.identifiedUser.username) {
            if (this.userInfo.rendered) {
                this.userInfo.update(cat.identifiedUser);
            }
            
            if (this.tooltipMenu) {
                this.tooltipMenu.enable();
                if (this.userInfoTooltip.rendered) {
                    this.userInfoTooltip.update(cat.identifiedUser);
                }
                this.createQuickLinks();
                
                if (this.catalogue.identifiedUser) {
                    this.harvestingMenu.setVisible(this.catalogue.identifiedUser.role === 'Administrator' || this.catalogue.identifiedUser.role === 'UserAdmin');
                    
                    var isRegisteredUser = this.catalogue.identifiedUser.role === 'RegisteredUser';
                    this.importMetadataMenu.setVisible(!isRegisteredUser);
                    this.newMetadataMenu.setVisible(!isRegisteredUser);
                }
            }
        } else {
            if (this.userInfo.rendered) {
                this.userInfo.update();
            }
            
            if (this.tooltipMenu) {
                this.tooltipMenu.disable();
                if (this.linksPanel.rendered) {
                    this.linksPanel.removeAll();
                    this.userInfoTooltip.update();
                }
            }
        }
        this.doLayout(false, true);
    }
});

/** api: xtype = gn_loginform */
Ext.reg('gn_loginform', GeoNetwork.LoginForm);