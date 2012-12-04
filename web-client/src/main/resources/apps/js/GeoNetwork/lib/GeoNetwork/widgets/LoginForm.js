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
        width: 340
    },
    defaultType: 'textfield',
    /** private: property[userInfo]
     * Use to display user information (name, password, profil).
     */
    userInfo: undefined,
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

    initComponent: function () {
        Ext.applyIf(this, this.defaultConfig);

        var form = this;
        var loginBt = new Ext.Button({
                width: 50,
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
                width: 80,
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
                width: 80,
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
            width: 70,
            autoCreate: {tag: 'input'},
            hideLabel: false,
            allowBlank: false,
            fieldLabel: OpenLayers.i18n('username'),
            emptyText: OpenLayers.i18n('username')
        });
        this.password = new Ext.form.TextField({
            name: 'password',
            width: 70,
            hideLabel: false,
            allowBlank: false,
            fieldLabel: OpenLayers.i18n('password'),
            emptyText: OpenLayers.i18n('password'),
            inputType: 'password'
        });
        this.userInfo = new Ext.form.Label({
            width: 170,
            text: '',
            cls: 'loginInfo'
        });
        
        
        
        if (this.hideLoginLabels) {
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
        this.toggledFieldsOff.push(this.userInfo, 
                logoutBt);
        if (this.catalogue.adminAppUrl !== '') {
            this.toggledFieldsOff.push(adminBt);
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
        this.doLayout(false, true);
    }
});

/** api: xtype = gn_loginform */
Ext.reg('gn_loginform', GeoNetwork.LoginForm);