/*
 * Copyright (C) 2012 GeoNetwork
 *
 * This file is part of GeoNetwork
 *
 * GeoNetwork is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoNetwork is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoNetwork.  If not, see <http://www.gnu.org/licenses/>.
 */

Ext.namespace('GeoNetwork');

/**
 * Login related stuff. Utility class for App.js.
 */
GeoNetwork.loginApp = function() {
    return {
        init : function() {
            Ext.get("login_button").on(
                    'click',
                    function(e) {
                        app.loginApp.login(Ext.get("username").getValue(),
                                Ext.get("password").getValue());
                    });

            catalogue.on('afterBadLogin', this.loginAlert, this);

            // Store user info in cookie to be displayed if user reload the page
            // Register events to set cookie values
            catalogue
                    .on(
                            'afterLogin',
                            function(e) {
                                cookie.set('user', catalogue.identifiedUser);
                                hide("login-form");
                                Ext.getCmp('md-selection-info').show();

                                var user = catalogue.identifiedUser;
                                Ext.get("username").dom.value = ("");
                                Ext.get("password").dom.value = ("");
                                if (Ext.isIE) {
                                    Ext.get("user-button_label").dom.innerText = OpenLayers
                                            .i18n("logout");
                                    Ext.get("username_label").dom.innerText = user.username;
                                    Ext.get("name_label").dom.innerText = " "
                                            + user.surname + " ";
                                    Ext.get("profile_label").dom.innerText = "("
                                            + user.role + ")";
                                } else {
                                    Ext.get("user-button_label").update(
                                            OpenLayers.i18n("logout"));
                                    Ext.get("username_label").update(
                                            user.username);
                                    Ext.get("name_label").update(
                                            " " + user.surname + " ");
                                    Ext.get("profile_label").update(
                                            "(" + user.role + ")");
                                }
                                Ext.get("user-button").dom.href = "javascript:app.loginApp.logout();";

                                show("administration-button");

                                if (user.type && user.type === "advanced") {
                                    user.searchTemplate = 'FULL';
                                } else {
                                    user.searchTemplate = 'SIMPLE';
                                }
                            });
            catalogue.on('afterLogout', function() {
                cookie.set('user', undefined);

                Ext.getCmp('md-selection-info').hide();
                hide("administration-button");
                if (Ext.isIE) {
                    Ext.get("user-button_label").dom.innerText = OpenLayers
                            .i18n("login");

                    Ext.get("username_label").dom.innerText = ("");
                    Ext.get("name_label").dom.innerText = ("");
                    Ext.get("profile_label").dom.innerText = ("");
                } else {
                    Ext.get("user-button_label").update(OpenLayers.i18n("login"));

                    Ext.get("username_label").update("");
                    Ext.get("name_label").update("");
                    Ext.get("profile_label").update("");
                }
                Ext.get("username").dom.value = ("");
                Ext.get("password").dom.value = ("");
                Ext.get("user-button").dom.href = "javascript:toggleLogin();";

                // Clean previous user data
                catalogue.metadataStore.removeAll();
                catalogue.resultsView.store.removeAll();

                showBrowse();
            });

            // Refresh login form if needed
            var user = cookie.get('user');

            if (user) {
                catalogue.identifiedUser = user;
            }
        },
        /**
         * Error message in case of bad login
         * 
         * @param cat
         * @param user
         * @return
         */
        loginAlert : function(cat, user) {
            GeoNetwork.Message().msg({
                title : OpenLayers.i18n('warning'),
                msg : OpenLayers.i18n('Login.error.message'),
                status : 'warning',
                target : document.body
            });
        },// FIXME Until catalog is adapted to spring security login, use this
        login : function(username, password) {
            var app = this, user;
            var intervalID;
            var loginAttempts = 0;
            var loginWindow;
            Ext.Ajax.request({
                url : catalogue.services.rootUrl + '../../j_spring_security_check',
                params : {
                    username : username,
                    password : password
                },
                headers : {
                    "Content-Type" : "application/x-www-form-urlencoded"
                },
                success : this.isLoggedIn,
                failure : this.isLoggedIn,
                scope : this
            });
        },// FIXME Until catalog is adapted to spring security login, use this
        logout : function() {
            Ext.Ajax.request({
                url : catalogue.services.rootUrl + '../../j_spring_security_logout',
                headers : {
                    "Content-Type" : "application/x-www-form-urlencoded"
                },
                success : function() {
                    catalogue.identifiedUser = undefined;
                    catalogue.fireEvent('afterLogout', 
                                catalogue, catalogue.identifiedUser);
                },
                failure : function() {
                    catalogue.fireEvent('afterBadLogout', 
                            catalogue, catalogue.identifiedUser);
            },
                scope : this
            });
        },
        /**
         * api: method[isLoggedIn]
         * 
         * Get the xml.info for me. If user is not identified response xml will
         * have a me element with an authenticated attribute. If catalogue URL
         * is wrong, response status is 404 (check catalogue URL). In case of
         * exception continue catalogue connection validation using the
         * xml.main.error service (@see checkError).
         */
        isLoggedIn : function() {
            var response = OpenLayers.Request.GET({
                url : catalogue.services.rootUrl + 'xml.info?type=me',
                async : false
            }), exception, authenticated, me;

            me = response.responseXML.getElementsByTagName('me')[0];
            authenticated = me.getAttribute('authenticated') == 'true';

            // Check status and also check than an Exception is not described in
            // the HTML response
            // in case of bad startup
            exception = response.responseText.indexOf('Exception') !== -1;

            if (response.status === 200 && authenticated) {

                var username = me.getElementsByTagName('username')[0];
                var name = me.getElementsByTagName('name')[0];
                var surname = me.getElementsByTagName('surname')[0];
                var role = me.getElementsByTagName('profile')[0];

                catalogue.identifiedUser = {
                    username : username.innerText || username.textContent
                            || username.text,
                    name : name.innerText || name.textContent || name.text,
                    surname : surname.innerText || surname.textContent
                            || surname.text,
                    role : role.innerText || role.textContent || role.text
                };
                catalogue.onAfterLogin();
                return true;
            } else if (response.status === 404) {
                this.showError(OpenLayers.i18n('connectIssue'), OpenLayers
                        .i18n('connectIssueMsg')
                        + catalogue.services.rootUrl + '.');
            } else if (exception) {
                catalogue.checkError();
                return false;
            } else {
                // Reset user cookie information
                if (cookie) {
                    cookie.set('user', undefined);
                }

                catalogue.identifiedUser = undefined;
                catalogue.onAfterBadLogin();

                return false;
            }
        }
    };
};
