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

                        var lang = /srv\/([a-z]{3})\/geocat/
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

            // createLanguageSwitcher(lang);

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

            this.mapApp = new GeoNetwork.mapApp();

            this.searchApp = new GeoNetwork.searchApp();
            this.searchApp.init();

            this.loginApp = new GeoNetwork.loginApp();
            this.loginApp.init();

        },

        initializeAppLayout : function() {
            Ext
                    .override(
                            Ext.layout.BorderLayout,
                            {
                                onLayout : function(ct, target) {
                                    var collapsed;
                                    if (!this.rendered) {
                                        target.position();
                                        target.addClass('x-border-layout-ct');
                                        var items = ct.items.items;
                                        collapsed = [];
                                        for ( var i = 0, len = items.length; i < len; i++) {
                                            var c = items[i];
                                            var pos = c.region;
                                            if (c.collapsed) {
                                                collapsed.push(c);
                                            }
                                            c.collapsed = false;
                                            if (!c.rendered) {
                                                c.cls = c.cls ? c.cls
                                                        + ' x-border-panel'
                                                        : 'x-border-panel';
                                                c.render(target, i);
                                            }
                                            this[pos] = pos != 'center'
                                                    && c.split ? new Ext.layout.BorderLayout.SplitRegion(
                                                    this, c.initialConfig, pos)
                                                    : new Ext.layout.BorderLayout.Region(
                                                            this,
                                                            c.initialConfig,
                                                            pos);
                                            this[pos].render(target, c);
                                        }
                                        this.rendered = true;
                                    }

                                    var size = target.getViewSize();

                                    if (size.width < this.minWidth) {
                                        target.setStyle('width', this.minWidth
                                                + 'px');
                                        size.width = this.minWidth;
                                        target.up('').setStyle('overflow',
                                                'auto');
                                    } else {
                                        target.setStyle('width', '');
                                    }

                                    // test minHeight

                                    if (this.minHeight !== undefined) {
                                        // alert(size.height + '---' +
                                        // this.minHeight);
                                        if (size.height < this.minHeight) {
                                            // alert('1 --- ' + size.height +
                                            // '---' + this.minHeight);
                                            target.setStyle('height',
                                                    this.minHeight + 'px');
                                            size.height = this.minHeight;
                                            target.up('').setStyle('overflow',
                                                    'auto');
                                        } else {
                                            target.setStyle('height', '');
                                        }
                                    }

                                    if (size.width < 20 || size.height < 20) { // display
                                        // none?
                                        if (collapsed) {
                                            this.restoreCollapsed = collapsed;
                                        }
                                        return;
                                    } else if (this.restoreCollapsed) {
                                        collapsed = this.restoreCollapsed;
                                        delete this.restoreCollapsed;
                                    }

                                    var w = size.width, h = size.height;
                                    var centerW = w, centerH = h, centerY = 0, centerX = 0;

                                    var n = this.north, s = this.south, west = this.west, e = this.east, c = this.center;
                                    if (!c) {
                                        throw 'No center region defined in BorderLayout '
                                                + ct.id;
                                    }

                                    if (n && n.isVisible()) {
                                        var b = n.getSize();
                                        var m = n.getMargins();
                                        b.width = w - (m.left + m.right);
                                        b.x = m.left;
                                        b.y = m.top;
                                        centerY = b.height + b.y + m.bottom;
                                        centerH -= centerY;
                                        n.applyLayout(b);
                                    }
                                    if (s && s.isVisible()) {
                                        var b = s.getSize();
                                        var m = s.getMargins();
                                        b.width = w - (m.left + m.right);
                                        b.x = m.left;
                                        var totalHeight = (b.height + m.top + m.bottom);
                                        b.y = h - totalHeight + m.top;
                                        centerH -= totalHeight;
                                        s.applyLayout(b);
                                    }
                                    if (west && west.isVisible()) {
                                        var b = west.getSize();
                                        var m = west.getMargins();
                                        b.height = centerH - (m.top + m.bottom);
                                        b.x = m.left;
                                        b.y = centerY + m.top;
                                        var totalWidth = (b.width + m.left + m.right);
                                        centerX += totalWidth;
                                        centerW -= totalWidth;
                                        west.applyLayout(b);
                                    }
                                    if (e && e.isVisible()) {
                                        var b = e.getSize();
                                        var m = e.getMargins();
                                        b.height = centerH - (m.top + m.bottom);
                                        var totalWidth = (b.width + m.left + m.right);
                                        b.x = w - totalWidth + m.left;
                                        b.y = centerY + m.top;
                                        centerW -= totalWidth;
                                        e.applyLayout(b);
                                    }

                                    var m = c.getMargins();
                                    var centerBox = {
                                        x : centerX + m.left,
                                        y : centerY + m.top,
                                        width : centerW - (m.left + m.right),
                                        height : centerH - (m.top + m.bottom)
                                    };
                                    c.applyLayout(centerBox);

                                    if (collapsed) {
                                        for ( var i = 0, len = collapsed.length; i < len; i++) {
                                            collapsed[i].collapse(false);
                                        }
                                    }

                                    if (Ext.isIE && Ext.isStrict) { // workaround
                                        // IE strict
                                        // repainting
                                        // issue
                                        target.repaint();
                                    }
                                }
                            });

            // Application layout
            var viewport = new Ext.Viewport({
                id : 'appViewPort',
                layout : 'border',
                layoutConfig : {
                    minWidth : 1080
                },
                items : [
                // Header
                {
                    region : 'north',
                    contentEl : 'header',
                    border : false
                },
                // Search/map
                {
                    region : 'west',
                    contentEl : 'search',
                    border : false,
                    split : true,
                    minWidth : 300,
                    maxWidth : 500,
                    width : 300,
                    border : false,
                    collapsible : true,
                    layout : 'vbox',
                    items : [
                    // Search panel
                    {
                        contentEl : 'search',
                        border : false
                    },
                    // Map panel
                    {
                        border : false,
                        height: '50%',
                        width: '100%',
                        items : [ app.mapApp.init() ]
                    } ]

                },
                // Search results
                {
                    region : 'center',
                    contentEl : 'search-results',
                    border : false
                },
                // Filter panel
                {
                    region : 'east',
                    contentEl : 'search-filter',
                    split : true,
                    title : 'Refine search',
                    collapsible : true,
                    // collapsed: !geocat.expandRefineOnStartup,
                    autoScroll : true,
                    border : false,
                    width : 250,
                    minWidth : 100,
                    maxWidth : 500
                } ],
                listeners : {
                    render : function() {
                        // get rid of the loading mask
                        // Ext.get(geocatConf.loadingElemId).remove();

                        // Ext.get("searchResults").show(); //search results
                        // where with "display:none" to avoid having the loading
                        // screen with a scrollbar
                    }
                }
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
});
