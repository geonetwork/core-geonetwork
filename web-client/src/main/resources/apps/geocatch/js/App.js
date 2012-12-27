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

                        var lang = /srv\/([a-z]{3})/
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
        breadcrumb : null,

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
                        summaryStore : GeoNetwork.data.MetadataSummaryStore()
                    /*
                     * , editMode : 2, metadataEditFn : edit, metadataShowFn :
                     * show
                     */
                    });

            // reset the user from the cookie (this will be required to
            // show admin fields in the search form)
            var user = cookie.get('user');
            if (user) {
                catalogue.identifiedUser = user;
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

            this.loginApp = new GeoNetwork.loginApp();
            this.loginApp.init();

            // createLatestUpdate();

            this.breadcrumb = GeoNetwork.BreadCrumb();
            this.breadcrumb.setCurrent(this.breadcrumb.defaultSteps[0]);

        },

        initializeAppLayout : function() {

            app.mapApp = new GeoNetwork.mapApp();
            app.mapApp.init();

            var margins = '35 0 0 0';

            var formpanel = {
                id : 'search-container',
                region : 'center',
                labelWidth : 70,
                bodyStyle : 'padding:15px',
                border : false,
                forceLayout : true,
                padding : 5,
                items : [ app.searchApp.simpleSearchForm,
                        app.searchApp.advSearchForm, app.searchApp.switcher ]
            };

            var mappanel = // Map panel
            {
                region : 'south',
                contentEl : 'map-div',
                id : 'map-container',
                border : false,
                height : 250,
                bodyStyle : 'background-color: #cccccc'
            };

            this.viewport = new Ext.Viewport({
                layout : 'border',
                id : 'vp',
                listeners : {
                    render : function() {
                    }
                },
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
                    minWidth : 300,
                    width : 300,
                    maxWidth : 400,
                    autoScroll : true,
                    collapsible : true,
                    hideCollapseTool : true,
                    collapseMode : 'mini',
                    forceLayout : true,
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
                    autoHeight : true,
                    margins : '0 0 0 0',
                    layout : 'fit'
                },
                // Filter panel
                {
                    id : 'facets-container',
                    region : 'east',
                    contentEl : 'search-filter',
                    split : true,
                    title : 'Refine search',
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
    // Language handling
    var lang = /srv\/([a-z]{3})/.exec(location.href);

    if (lang === null) {
        lang = GeoNetwork.Util.defaultLocale;
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

    // initShortcut();
    if (Ext.getDom('E_any')) {
        Ext.getDom('E_any').focus(true);
    }

    // Restoring session (if any)
    // Ext.state.Manager.getProvider().restore(location.href);

    // Register events where the search should be triggered
    var events = [ 'afterDelete', 'afterRating', 'afterLogout', 'afterLogin' ];
    Ext.each(events, function(e) {
        catalogue.on(e, function() {
            // var e = Ext.getCmp('advanced-search-options-content-form');
            // e.getEl().fadeIn();
            // e.fireEvent('search');
        });
    });

    app.viewport.doLayout();
});