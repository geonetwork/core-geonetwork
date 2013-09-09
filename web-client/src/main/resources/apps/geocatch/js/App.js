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

    var createLatestUpdate = function() {
        var latestView = new GeoNetwork.MetadataResultsView({
            catalogue : catalogue,
            autoScroll : true,
            tpl : GeoNetwork.Templates.SIMPLE
        });
        var latestStore = new GeoNetwork.Settings.mdStore();
        latestView.setStore(latestStore);
        latestStore.on('load', function() {
            Ext.ux.Lightbox.register('a[rel^=lightbox]');
        });
        var p = new Ext.Panel({
            border : false,
            bodyCssClass : 'md-view',
            items : latestView,
            renderTo : 'latest-metadata'
        });
        latestView.tpl = GeoNetwork.Templates.SIMPLE;
        catalogue.kvpSearch("fast=index&from=1&to=5&sortBy=changeDate",
                function(e) {
                    Ext.each(Ext.DomQuery.select('.md-action-menu'), function(
                            el) {
                        hide(el);
                    });
                }, null, null, true, latestView.getStore());
    };

    /**
     * Create a language switcher menu. This one changes the url so the language
     * is now on Jeeves mode.
     * 
     */
    var createLanguageSwitcher = function(lang) {

        var items = [];
        Ext.each(GeoNetwork.Util.locales, function(locale) {
            var current = false;
            Ext.each(locale, function(l) {
                if (lang === l) {
                    current = true;
                }
            });
            if (!current) {
                var button = new Ext.Button({
                    text : locale[1],
                    value : locale[2],
                    cls : 'langButton'
                });

                if (items.length > 0) {
                    items.push(new Ext.form.Label({
                        text : "|"
                    }));
                }

                button.on('click', function() {
                    var lang = /srv\/([a-z]{3})/.exec(window.location.href);

                    if (lang === null) {
                        window.location.pathname = window.location.pathname
                                .replace('/srv/geocat', '/srv/' + this.value
                                        + '/geocat');
                    } else {
                        window.location.pathname = window.location.pathname
                                .replace(lang[1], this.value);
                    }
                }, button);

                items.push(button);
            }
        });

        return new Ext.Panel({
            renderTo : 'lang-form',
            border : false,
            layout : {
                type : 'hbox',
                pack : 'end'
            },
            width : 300,
            style : {
                background : "transparent"
            },
            hidden : GeoNetwork.Util.locales.length === 1 ? true : false,
            items : items
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

            var lang = OpenLayers.Lang.getCode();
            if (urlParameters.extent) {
                urlParameters.bounds = new OpenLayers.Bounds(
                        urlParameters.extent[0], urlParameters.extent[1],
                        urlParameters.extent[2], urlParameters.extent[3]);
            }

            createLanguageSwitcher(lang);

            // Init cookie
            cookie = new Ext.state.CookieProvider({
            // expires : new Date(new Date().getTime()
            // + (1000 * 60 * 60 * 24 * 365))
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
                        summaryStore : GeoNetwork.data.MetadataSummaryStore()
                    /*
                     * , editMode : 2, metadataEditFn : edit, metadataShowFn :
                     * show
                     */
                    });

            if (cookie.get('user')) {
                catalogue.isLoggedIn();
            }

            // set a permalink provider which will be the main state provider.
            /*
             * Ext.state.Manager .setProvider(new
             * GeoNetwork.state.PermalinkProvider({ encodeType : false }));
             * 
             * 
             * Ext.state.Manager.getProvider().on({ statechange :
             * function(provider, name, value) { url = provider.getLink(); url =
             * url.substring(url.indexOf("#")); location.hash = url; } });
             */

            this.searchApp = new GeoNetwork.searchApp();
            this.searchApp.init();

            app.mapApp = new GeoNetwork.mapApp();
            app.mapApp.init();

            catalogue.resultsView.addMap(app.mapApp.getMap());

            this.loginApp = new GeoNetwork.loginApp();
            this.loginApp.init();
        },

        initializeAppLayout : function() {

            var margins = '35 0 0 0';

            var formpanel = {
                id : 'search-metacontainer',
                region : 'center',
                bodyStyle : 'padding:15px',
                border : true,
                split : true,
                minHeight : 170,
                layout : 'border',
                forceLayout : true,
                items : [
                        {
                            id : 'search-container',
                            region : 'center',
                            labelWidth : 70,
                            bodyStyle : 'padding:15px',
                            border : false,
                            forceLayout : true,
                            padding : 5,
                            items : [ app.searchApp.simpleSearchForm,
                                    app.searchApp.advSearchForm ]
                        }, app.searchApp.switcher ]
            };

            var mappanel = // Map panel
            {
                region : 'south',
                contentEl : 'map-div',
                id : 'map-container',
                border : true,
                minHeight : 250,
                split : true,
                height : 250,
                maxHeight : 500,
                bodyStyle : 'background-color: #cccccc',
                listeners : {
                    resize : function() {
                        var map = app.mapApp.getMap();
                        var size = Ext.get("map-container").getSize();
                        Ext.get(map.div).setSize(size.width, size.height);
                        map.updateSize();
                    }
                }
            };

            this.viewport = new Ext.Viewport({
                layout : 'border',
                id : 'vp',
                forceLayout : true,
                items : [// Header
                {
                    region : 'north',
                    id : 'north',
                    contentEl : 'header',
                    border : false,
                    margins : '0 0 0 0',
                    autoHeight : true
                }, {
                    region : 'west',
                    id : 'west',
                    margins : '0 0 0 0',
                    split : true,
                    minWidth : 350,
                    width : 450,
                    maxWidth : 450,
                    autoScroll : true,
                    forceLayout : true,
                    border : false,
                    layoutConfig : {
                        animate : true
                    },
                    layout : 'border',
                    items : [ formpanel, mappanel ]

                },
                // Search results
                {
                    region : 'center',
                    id : 'center-container',
                    contentEl : 'search-results',
                    border : false,
                    autoScroll : true,
                    height : '100%',
                    margins : '0 0 0 0',
                    layout : 'fit'
                },
                // Filter panel
                {
                    id : 'facets-container',
                    region : 'east',
                    contentEl : 'search-filter',
                    split : true,
                    title : OpenLayers.i18n("refineSearch"),
                    collapsible : true,
                    layout : 'fit',
                    autoScroll : true,
                    border : false,
                    width : 250,
                    minWidth : 100,
                    maxWidth : 500,
                    height : '100%'
                } ]
            });
        },

        init : function() {
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

    GeoNetwork.Settings.GeoserverUrl = geoserverUrl;

    // Swisstopo specific
    var catalog_store = geoNetworkStores["sources_groups"];
    for (i = 0; i < catalog_store.length; i++) {
        var el = catalog_store[i];
        var index = el[0].indexOf("/");
        if (index > 0) {
            catalog_store[i][0] = el[0].substring(index + 1);
        }
    }
    GeoNetwork.Settings.Stores = geoNetworkStores;

    // Language handling
    var lang = /srv\/([a-z]{3})/.exec(location.href);

    if (lang === null) {
        lang = GeoNetwork.Util.defaultLocale;
    }
    if (Ext.isArray(lang)) {
        if (lang.length > 1) {
            lang = lang[1];
        } else {
            lang = lang[0];
        }
    }

    var url = /(.*)\/srv/.exec(location.href)[1];
    GeoNetwork.Util.setLang(lang, url + '/apps');

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

    // initShortcut();
    if (Ext.getDom('E_any')) {
        Ext.getDom('E_any').focus(true);
    }

    // Restoring session (if any)
    // Ext.state.Manager.getProvider().restore(location.href);

    // Register events where the search should be triggered
    var events = [ 'afterDelete', 'afterRating', 'afterLogout', 'afterLogin' ];

    // Do we have a search on the parameter url?

    if (OpenLayers.Util.getParameters().hasOwnProperty("s_search")) {

        // E__owner
        if (OpenLayers.Util.getParameters().hasOwnProperty("_E__owner")) {
            Ext.each(Ext.query("input[name=E__owner]"), function(input) {
                Ext.getCmp(input.id).setValue(
                        OpenLayers.Util.getParameters()._E__owner);
            });
        }

        // _E_siteId
        if (OpenLayers.Util.getParameters().hasOwnProperty("_E_siteId")) {
            Ext.each(Ext.query("input[name=E_siteId]"), function(input) {
                Ext.getCmp(input.id).setValue(
                        OpenLayers.Util.getParameters()._E_siteId);
            });
        }

        // _E__isHarvested=y
        if (OpenLayers.Util.getParameters().hasOwnProperty("_E__isHarvested")) {
            Ext.each(Ext.query("input[name=E__isHarvested]"), function(input) {
                Ext.getCmp(input.id).setValue(
                        OpenLayers.Util.getParameters()._E__isHarvested);
            });
        }

        // _E_template=y
        if (OpenLayers.Util.getParameters().hasOwnProperty("_E_template")) {
            Ext.each(Ext.query("input[name=E__isTemplate]"), function(input) {
                Ext.getCmp(input.id).setValue(
                        OpenLayers.Util.getParameters()._E_template);
            });
        }

        // dateFrom
        if (OpenLayers.Util.getParameters().hasOwnProperty("dateFrom")) {
            Ext.each(Ext.query("input[name=E_extFrom]"), function(input) {
                Ext.getCmp(input.id).setValue(
                        new Date(OpenLayers.Util.getParameters().dateFrom
                                + " 00:00:00"));
            });
        }

        // dateTo
        if (OpenLayers.Util.getParameters().hasOwnProperty("dateTo")) {
            Ext.each(Ext.query("input[name=E_extTo]"), function(input) {
                Ext.getCmp(input.id).setValue(
                        new Date(OpenLayers.Util.getParameters().dateTo));
            });
        }

        // customFilter
        if (OpenLayers.Util.getParameters().hasOwnProperty("customFilter")) {
            Ext.each(Ext.query("input[name=customFilter]"), function(input) {
                var param = OpenLayers.Util.getParameters().customFilter;
                Ext.getCmp(input.id).setValue(param);
            });
        }

        showAdvancedSearch();

        Ext.getCmp('advanced-search-options-content-form').fireEvent('search');
    }

    if (OpenLayers.Util.getParameters().hasOwnProperty("uuid")) {
        catalogue.metadataShow(OpenLayers.Util.getParameters().uuid, true);
    }

    app.viewport.doLayout();

    Ext.getBody().unmask()

});
