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

var catalogue;
var app;
var cookie;
var urlParameters;

/**
 * Main App class. It should only contain specific GUI behaviour.
 */
GeoNetwork.app = function() {

    /**
     * Create a language switcher menu. This one changes the url so the language
     * is now on Jeeves mode.
     *
     */
    var createLanguageSwitcher = function(lang) {
        return new Ext.form.FormPanel({
            renderTo : 'lang-form',
            width : 400,
            border : false,
            layout : 'anchor',
            hidden : GeoNetwork.Util.locales.length === 1 ? true : false,
            items : [ new Ext.form.ComboBox({
                mode : 'local',
                triggerAction : 'all',
                height : '100%',
                store : new Ext.data.ArrayStore({
                    idIndex : 2,
                    fields : [ 'id', 'name', 'id2' ],
                    data : GeoNetwork.Util.locales
                }),
                valueField : 'id2',
                displayField : 'name',
                value : lang,
                listeners : {
                    select : function(cb, record, idx) {

                        var lang = /srv\/([a-z]{3})\/search/
                            .exec(window.location.href);

                        if (lang === null) {
                            window.location.pathname = window.location.pathname
                                .replace('/srv/geocat', '/srv/'
                                + cb.getValue() + '/geocat');
                        } else {
                            window.location.pathname = window.location.pathname
                                .replace(lang[1], cb.getValue());
                        }

                    }
                }
            }) ]
        });
    };


    // public space:
    return {
        mapApp : null,
        searchApp : null,

        /**
         * Initializes cookies, connections, url, etc,...
         */
        initializeEnvironment : function() {
            var geonetworkUrl = window.location.href.match(
                /(http.*\/.*)\/srv\.*/, '')[1];

            urlParameters = GeoNetwork.Util.getParameters(location.href);

            var lang = urlParameters.hl || GeoNetwork.Util.defaultLocale;
            if (urlParameters.extent) {
                urlParameters.bounds = new OpenLayers.Bounds(
                    urlParameters.extent[0], urlParameters.extent[1],
                    urlParameters.extent[2], urlParameters.extent[3]);
            }

            createLanguageSwitcher(lang);

            // Init cookie
            cookie = new Ext.state.CookieProvider({
                expires : new Date(new Date().getTime()
                    + (1000 * 60 * 60 * 24 * 365))
            });
            // Create connection to the catalogue
            catalogue = new GeoNetwork.Catalogue(
                {
                    // statusBarId: 'info',
                    lang : lang,
                    statusBarId : 'info',
                    hostUrl : geonetworkUrl,
                    mdOverlayedCmpId : 'resultsPanel',
                    adminAppUrl : geonetworkUrl + '/srv/' + lang + '/admin',
                    // Declare default store to be used for records and
                    // summary
                    metadataStore : GeoNetwork.Settings.mdStore ? new GeoNetwork.Settings.mdStore()
                        : new GeoNetwork.data.MetadataResultsStore(),
                    metadataCSWStore : GeoNetwork.data
                        .MetadataCSWResultsStore(),
                    summaryStore : GeoNetwork.data.MetadataSummaryStore() /*,
                    editMode : 2,
                    metadataEditFn : edit,
                    metadataShowFn : show*/
                });

            // reset the user from the cookie (this will be required to
            // show admin fields in the search form)
            var user = cookie.get('user');
            if (user) {
                catalogue.identifiedUser = user;
            }

            // set a permalink provider which will be the main state provider.
            /*Ext.state.Manager
                .setProvider(new GeoNetwork.state.PermalinkProvider({
                encodeType : false
            }));


            Ext.state.Manager.getProvider().on({
                statechange : function(provider, name, value) {
                    url = provider.getLink();
                    url = url.substring(url.indexOf("#"));
                    location.hash = url;
                }
            });*/


            this.loginApp = new GeoNetwork.loginApp();
            this.loginApp.init();
            //this.mapApp = new GeoNetwork.mapApp();
            //this.mapApp.init();
            this.searchApp = new GeoNetwork.searchApp();
            this.searchApp.init();

        },

        initializeAppLayout: function() {
            // Application layout
            /*var viewport = new Ext.Viewport({
                layout: 'border',
                items: [
                    // Header
                    {
                        region : 'north',
                        html : '<h1>Header content</h1>',
                        border : false
                    },
                    {
                        region : 'center',
                        html : '<h1>Center content</h1>',
                        border : false
                    }
                ],
                listeners: {
                    render: function() {
                        //get rid of the loading mask
                        //Ext.get(geocatConf.loadingElemId).remove();

                        //Ext.get("searchResults").show(); //search results where with "display:none" to avoid having the loading screen with a scrollbar
                    }
                }
            });*/
        },

        init: function() {
            // Initialize utils
            this.initializeEnvironment();

            this.initializeAppLayout();

            if (urlParameters.create !== undefined && catalogue.isIdentified()) {
                var actionCtn = Ext.getCmp('resultsPanel').getTopToolbar();
                actionCtn.createMetadataAction.handler.apply(actionCtn);
            }
        }
    }

};

Ext.onReady(function() {
    // Language handling
    var lang = /srv\/([a-z]{3})\/search/.exec(location.href);

    if (lang === null) {
        lang = "eng";
    }

    var url = /(.*)\/srv/.exec(location.href)[1];
    GeoNetwork.Util.setLang(lang && lang[1], url + '/apps');

    if (Ext.isIE6) {
        Ext.get(Ext.query("html")[0]).addClass("lt-ie9 lt-ie8 lt-ie7");
    } else if (Ext.isIE7) {
        Ext.get(Ext.query("html")[0]).addClass("lt-ie9 lt-ie8");
    } else if (Ext.isIE8) {
        Ext.get(Ext.query("html")[0]).addClass("lt-ie9");
    }

    Ext.QuickTips.init();
    app = new GeoNetwork.app();
    app.init();

    initShortcut();
    if (Ext.getDom('E_any')) {
        Ext.getDom('E_any').focus(true);
    }

    // Restoring session (if any)
    //Ext.state.Manager.getProvider().restore(location.href);

    // Register events where the search should be triggered
    var events = [ 'afterDelete', 'afterRating', 'afterLogout', 'afterLogin' ];
    Ext.each(events, function(e) {
        catalogue.on(e, function() {
            //var e = Ext.getCmp('advanced-search-options-content-form');
            //e.getEl().fadeIn();
            //e.fireEvent('search');
        });
    });
});
