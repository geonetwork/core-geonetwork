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
                        user = catalogue.login(Ext.get("username").getValue(),
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
                                hide("logout-div");
                                var user = catalogue.identifiedUser;
                                if (Ext.isIE) {
                                    Ext.get("username").dom.innerText = "";
                                    Ext.get("password").dom.innerText = "";
                                    Ext.get("username_label").dom.innerText = user.username;
                                    Ext.get("name_label").dom.innerText = ":"
                                            + user.surname;
                                    Ext.get("profile_label").dom.innerText = "("
                                            + user.role + ")";
                                } else {
                                    Ext.get("username").update("");
                                    Ext.get("password").update("");
                                    Ext.get("username_label").update(
                                            user.username);
                                    Ext.get("name_label").update(
                                            " " + user.surname + " ");
                                    Ext.get("profile_label").update(
                                            "(" + user.role + ")");
                                }

                                hide("login-form");
                                show("logout-div");

                                show("adminMenu");

                                // Show some advanced search options
                                show(Ext.getCmp("formatCombo").el.parent()
                                        .parent().parent().parent().parent()
                                        .parent(), true);
                                show(Ext.getCmp("isTemplate").el.parent()
                                        .parent().parent(), true);
                                show(Ext.getCmp("isValid").el.parent().parent()
                                        .parent(), true);
                                show(Ext.getCmp("toEdit").el.parent().parent()
                                        .parent(), true);
                                show(Ext.getCmp("toPublish").el.parent()
                                        .parent().parent(), true);
                            });
            catalogue.on('afterLogout', function() {
                cookie.set('user', undefined);
                if (Ext.isIE) {

                    Ext.get("username_label").dom.innerText = ("");
                    Ext.get("name_label").dom.innerText = ("");
                    Ext.get("profile_label").dom.innerText = ("");
                } else {
                    Ext.get("username_label").update("");
                    Ext.get("name_label").update("");
                    Ext.get("profile_label").update("");
                }
                hide("logout-div");
                show("login-form");

                hide("adminMenu");

                // Hide some advanced search options
                hide(Ext.getCmp("formatCombo").el.parent().parent().parent()
                        .parent().parent().parent());
                hide(Ext.getCmp("isTemplate").el.parent().parent().parent());
                hide(Ext.getCmp("isValid").el.parent().parent().parent());
                hide(Ext.getCmp("toEdit").el.parent().parent().parent());
                hide(Ext.getCmp("toPublish").el.parent().parent().parent());
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

            // Ext.Msg.show({
            // title : OpenLayers.i18n('Login.error'),
            // msg : OpenLayers.i18n('Login.error.message'),
            // /* TODO : Get more info about the error */
            // icon : Ext.MessageBox.ERROR,
            // buttons : Ext.MessageBox.OK
            // });
        }
    };
};