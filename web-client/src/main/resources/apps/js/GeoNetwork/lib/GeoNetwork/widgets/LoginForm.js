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
 *
 *
 */
GeoNetwork.LoginForm = Ext.extend(Ext.FormPanel, {
    url: '',
    id: 'loginForm',
    border: false,
    layout: 'hbox',
    /** api: config[catalogue] 
     * ``GeoNetwork.Catalogue`` Catalogue to use
     */
    catalogue: undefined,
    defaultConfig: {
    	width: 340
    },
    defaultType: 'textfield',
    
    username: undefined,
    password: undefined,
    userInfo: undefined,
    loginBt: undefined,
    adminBt: undefined,
    logoutBt: undefined,
    
    /** private: method[initComponent] 
     *  Initializes the login form results view.
     */
    initComponent: function(config){
    	Ext.apply(this, config);
    	Ext.applyIf(this, this.defaultConfig);

    	var form = this;
        this.username = new Ext.form.TextField({
            name: 'username',
            width: 70,
            hideLabel: true,
            allowBlank: false,
            emptyText: OpenLayers.i18n('username')
        });
        this.password = new Ext.form.TextField({
            name: 'password',
            width: 70,
            hideLabel: true,
            allowBlank: false,
            emptyText: OpenLayers.i18n('password'),
            inputType: 'password'
        });
        this.userInfo = new Ext.form.Label({
            width: 170,
            text: '',
            cls: 'loginInfo'
        });
        this.loginBt = new Ext.Button({
            width: 50,
            text: OpenLayers.i18n('login'),
            iconCls: 'md-mn mn-login',
            listeners: {
                click: function(){
                    this.catalogue.login(this.username.getValue(), this.password.getValue());
                },
                scope: form
            }
        });
        this.adminBt = new Ext.Button({
            width: 80,
            text: OpenLayers.i18n('administration'),
            //iconCls : 'md-mn md-mn-advanced',
            listeners: {
                click: function(){
                    catalogue.admin();
                },
                scope: this
            }
        });
        this.logoutBt = new Ext.Button({
            width: 80,
            text: OpenLayers.i18n('logout'),
            iconCls: 'md-mn mn-logout',
            listeners: {
                click: function(){
                    catalogue.logout();
                },
                scope: this
            }
        });
        this.items = [this.username, this.password, this.userInfo, this.loginBt, this.adminBt, this.logoutBt];
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
    login: function(cat, user){
        var status = user ? true : false;
        
        this.username.setVisible(!status);
        this.password.setVisible(!status);
        
        if (cat.identifiedUser && cat.identifiedUser.username) {
            this.userInfo.setText(cat.identifiedUser.name +
            ' ' +
            cat.identifiedUser.surname +
            ' <br/>(' +
            cat.identifiedUser.role +
            ')', false);
        } else {
            this.userInfo.setText('');
        }
        
        this.userInfo.setVisible(status);
        this.loginBt.setVisible(!status);
        this.logoutBt.setVisible(status);
        if (this.catalogue.adminAppUrl !== '') {
            this.adminBt.setVisible(status);
        }
        this.doLayout(false, true);
    }
});

/** api: xtype = gn_loginform */
Ext.reg('gn_loginform', GeoNetwork.LoginForm);
