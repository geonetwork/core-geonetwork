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
            tpl : GeoNetwork.HTML5UI.Templates.THUMBNAIL_SIMPLER
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
        latestView.tpl = GeoNetwork.HTML5UI.Templates.THUMBNAIL_SIMPLER;
        catalogue.kvpSearch(
                "fast=index&from=1&to=3&sortBy=changeDate",
                function(e) {
                    Ext.each(Ext.DomQuery.select('.md-action-menu'), function(
                            el) {
                        hide(el);
                    });
                }, null, null, true, latestView.getStore());
    };

    function createMainTagCloud() {
        var tagCloudView = new GeoNetwork.TagCloudView({
            catalogue : catalogue,
            query : 'fast=true&summaryOnly=true&from=1&to=4',
            renderTo : 'cloud-tag',
            onSuccess : 'app.loadResults',
            tpl: new Ext.XTemplate(
                    '<tpl for=".">', 
                        '<div class="tag-cloud">',
                           '{value}<a href="#" onclick="javascript:catalogue.kvpSearch(\'fast=' + catalogue.metadataStore.fast + '&summaryOnly=0&from=1&to=20&hitsPerPage=20&' + 
                           'themekey' + 
                                '={value}\', app.loadResults, null, null);" alt="{value}">({count})</a>', 
                        '</div>', 
                    '</tpl>')
        });

        return tagCloudView;
    }
    ;
    var createPopularUpdate = function() {
        var latestView = new GeoNetwork.MetadataResultsView({
            catalogue : catalogue,
            autoScroll : true,
            tpl : GeoNetwork.HTML5UI.Templates.THUMBNAIL_SIMPLER
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
            renderTo : 'popular-metadata'
        });
        latestView.tpl = GeoNetwork.HTML5UI.Templates.THUMBNAIL_SIMPLER;
        catalogue.kvpSearch(
                "fast=index&from=1&to=3&sortBy=popularity",
                function(e) {
                    Ext.each(Ext.DomQuery.select('.md-action-menu'), function(
                            el) {
                        hide(el);
                    });
                }, null, null, true, latestView.getStore());
    };

    /**
     * 
     * Given a metadata, load a map on the metadata associated map and creates
     * the checkbox for downloading or adding layers to the big map
     * 
     */
    var loadMetadataMap = function(metadata) {

        var checkboxes = [];
        var layers = [];

        // add data to map
        Ext.each(metadata.record.get("links"), function(link) {
            if (link.protocol === 'application/vnd.ogc.wms_xml'
                    || link.protocol.toUpperCase().indexOf("OGC:WMS") >= 0) {
                var layers = app.mapApp.getCapabilitiesWMS(link.href);
                Ext.each(layers, function(layer) {
                    var wms = new OpenLayers.Layer.WMS(link.name, link.href, {
                        layers : layer,
                        transparent : true
                    });

                    wms.setVisibility(true);

                    checkboxes.push(new Ext.form.Checkbox({
                        boxLabel : layer,
                        layer : wms
                    }));

                    layers.push(wms);
                });
            } else if (link.protocol === 'application/vnd.ogc.wfs_xml'
                    || link.protocol.toUpperCase().indexOf("OGC:WFS") >= 0) {
                var layers = [];
                try {
                    layers = app.mapApp.getCapabilitiesWFS(link.href);
                } catch (ex) {
                    layers = [];
                }
                var styleMap = new OpenLayers.StyleMap({
                    strokeWidth : 3,
                    strokeColor : "#333333"
                });
                var strategies = [ new OpenLayers.Strategy.BBOX() ];

                Ext.each(layers, function(layer) {
                    var wfs = new OpenLayers.Layer.Vector(link.name, {
                        strategies : strategies,
                        protocol : layer,
                        styleMap : styleMap
                    });
                    wfs.setVisibility(true);

                    checkboxes.push(new Ext.form.Checkbox({
                        boxLabel : layer.featurePrefix + ":"
                                + layer.geometryName,
                        layer : wfs
                    }));

                    layers.push(wfs);
                });
            }
        });

        var wfs_href = metadata.record.get("href");

        // var form = new Ext.form.FormPanel(
        // {
        // renderTo : "download_" + metadata.title,
        // defaultType : 'checkboxgroup',
        // buttonAlign : 'left',
        // width : 200,
        // layout : 'column',
        // items : [ checkboxes ],
        // fbar : {
        // xtype : 'toolbar',
        // items : [
        // {
        // text : OpenLayers.i18n('Add to map'),
        // handler : function() {
        // form.items
        // .each(function(c) {
        // if (c.checked && !c.added) {
        // c.added = true;
        // app.mapApp
        // .createLayer(c.layer);
        // }
        // });
        // }
        // },
        // {
        // text : OpenLayers.i18n('prepareDownload'),
        // handler : function() {
        // // FIXME : this call require the
        // // catalogue to be named
        // // catalogue
        // catalogue
        // .metadataPrepareDownload(metadata.record
        // .get('id'));
        // }
        // } ]
        // }
        // });
        var map = app.mapApp.generateAuxiliaryMap("map_" + metadata.title);
        Ext.each(layers, function(layer) {
            map.addLayer(layer);
        });

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

                        var lang = /srv\/([a-z]{3})\/search/
                                .exec(window.location.href);

                        if (Ext.isArray(lang)) {
                            lang = lang[1];
                        }

                        if (lang === null) {
                            window.location.pathname = window.location.pathname
                                    .replace('/srv/search', '/srv/'
                                            + cb.getValue() + '/search');
                        } else {
                            window.location.pathname = window.location.pathname
                                    .replace(lang, cb.getValue());
                        }

                    }
                }
            }) ]
        });
    };

    function edit(metadataId, create, group, child) {

        Ext.getCmp('metadata-panel') && Ext.getCmp('metadata-panel').destroy();
        var editorPanel = new GeoNetwork.editor.EditorPanel({
            defaultViewMode : GeoNetwork.Settings.editor.defaultViewMode,
            catalogue : catalogue,
            renderTo : 'metadata-info',
            layout : 'border',
            xlinkOptions : {
                CONTACT : true
            },
            listeners : {
                editorClosed : function() {
                    showSearch();
                    Ext.get("search-form").show();
                }
            }
        });

        editorPanel.init(metadataId, create, group, child);
        editorPanel.doLayout(false);
        showMetadata();
        hide("search-form");
        app.breadcrumb.setCurrent({
            text : OpenLayers.i18n('mdEditor'),
            func : "app.edit('" + metadataId + "', " + create + ", " + group
                    + "," + child + ")"
        });
    }

    function show(uuid, record, url, maximized, width, height, clean) {

        Ext.get("metadata-info").update("");

        var button_width = 101;
        var button_height = 38;
        var aResTab = new GeoNetwork.view.ViewPanel({
            serviceUrl : catalogue.services.mdView + '?uuid=' + uuid,
            lang : catalogue.lang,
            id : 'metadata-panel',
            renderTo : 'metadata-info',
            autoScroll : true,
            resultsView : catalogue.resultsView,
            layout : 'fit',
            // autoHeight:true,
            padding : '5px 25px',
            currTab : GeoNetwork.defaultViewMode || 'simple',
            printDefaultForTabs : GeoNetwork.printDefaultForTabs || false,
            printUrl : '../../apps/ngr2/print.html',
            catalogue : catalogue,
            // maximized: true,
            metadataUuid : uuid,
            record : record,
            buttonWidth : button_width,
            buttonHeight : button_height
        });

        // aResTab.on("afterrender", function() {
        // // Initialize map and links
        // loadMetadataMap({
        // record : record,
        // title : record.get('title')
        // });
        // });

        showMetadata();
        app.breadcrumb.setDefaultPrevious(2);
        app.breadcrumb.setCurrent({
            text : record.get('title'),
            func : "app.searchApp.addMetadata('" + uuid + "', true)"
        });

        // Get title, keywords and author for current record
        // in order to populate the META tag in the HEAD of the HTML page.
        var contacts = [];
        Ext.each(record.get('contact'), function(item) {
            contacts.push(item.name);
        });
        var subjects = [];
        Ext.each(record.get('subject'), function(item) {
            subjects.push(item.value);
        });
        GeoNetwork.Util.updateHeadInfo({
            title : catalogue.getInfo().name
                    + ' | '
                    + record.get('title')
                    + (record.get('type') ? ' (' + record.get('type') + ')'
                            : ''),
            meta : {
                subject : record.get('abstract'),
                keywords : subjects,
                author : contacts
            }
        });

        token = "|" + uuid;

        if (!GeoNetwork.state.History.getToken()
                || GeoNetwork.state.History.getToken().indexOf("edit=") != 0) {
            if (!Ext.state.Manager.getProvider().restoring) {
                GeoNetwork.state.History.eventsSuspended = true;
                GeoNetwork.state.History.suspendEvents();
                GeoNetwork.state.History.add(token);
                GeoNetwork.state.History.resumeEvents();
                GeoNetwork.state.History.eventsSuspended = false;
            }
        }

        Ext.state.Manager.getProvider().restoring = false;

        hide("share-capabilities");
        hide("permalink-div");

        // Adding social capabilities
        Ext.getCmp("metadata-panel").getTopToolbar().addButton({
            id : 'share-button',
            width : button_width,
            height : button_height,
            handler : function() {
                toggle("share-capabilities");
            },
            text : '',
            tooltip : 'Share this',
            type : 'submit',
            tooltip : OpenLayers.i18n('Social Share'),
            enableToggle : true
        });

        GeoNetwork.Util.removeMetaTags({
            'og:title' : true,
            'og:url' : true
        });

        GeoNetwork.Util.addMetaTag("og:title", record.get('title'));
        GeoNetwork.Util.addMetaTag("og:url", Ext.state.Manager.getProvider()
                .getPrettyLink());

        // Updating social
        Ext.get("custom-tweet-button").dom.href = "https://twitter.com/share?text="
                + record.get('title')
                + " "
                + Ext.state.Manager.getProvider().getPrettyLink();
        Ext.get("custom-tweet-button").dom.title = "Tweet this";

        var fb_url = "https://www.facebook.com/dialog/feed?app_id=307560442683468&redirect_uri="
                + Ext.state.Manager.getProvider().getPrettyLink()
                + '&link='
                + Ext.state.Manager.getProvider().getPrettyLink();

        Ext.get("fb-button")
                .update(
                        '<a href="' + fb_url + '">' + OpenLayers.i18n('Like!')
                                + '</a>');
        Ext.get("fb-button").dom.title = "Like this";
        // feedback window
//        var feedbackWindow;
//        var newFeedbackWindow = function() {
//            feedbackWindow = new GeoNetwork.FeedbackForm(null, record);
//            feedbackWindow.show();
//        };
//        
        // feedback button to open window
//        Ext.getCmp("metadata-panel").getTopToolbar().addButton({
//            id : 'feedback-button',
//            width : button_width,
//            height : button_height,
//            tooltip : 'Feedback',
//            handler : newFeedbackWindow,
//            text : '',
//            tooltip : OpenLayers.i18n('Feedback'),
//            type : 'submit'
//        });

        // Adding permalink
        Ext.getCmp("metadata-panel").getTopToolbar().addButton({
            id : 'permalink-button',
            width : button_width,
            height : button_height,
            handler : function() {
                var url = Ext.state.Manager.getProvider().getPrettyLink();
                Ext.get("permalink-div").update(url);
                toggle("permalink-div");
            },
            text : '',
            tooltip : OpenLayers.i18n('Permalink'),
            enableToggle : true,
            type : 'submit'
        });

        Ext.getCmp("metadata-panel").doLayout();
        // Add to recent viewed
        addToRecentViewed(record);

    }

    function addToRecentViewed(record) {
        var div = Ext.getCmp("recent-viewed");

        if (!div) {

            var store = new Ext.data.ArrayStore({
                autoDestroy : true,
                autoSave : true,
                storeId : 'recent-viewed-store',
                idIndex : 0,
                fields : [ 'thumbnail', 'description', 'uuid', 'title' ]
            });

            var tpl = new Ext.XTemplate(
                    '<tpl for=".">',
                    '<div class="thumb-wrap" id="recent-viewed_{uuid}">',
                    '<a href="javascript:app.searchApp.addMetadata(\'{uuid}\', true);"><div class="thumb">',
                    '<h1>{title}</h1>',
                    '<img src="{thumbnail}" title="{title}" alt="{title}">',
                    '<span>{description}</span>', '</div></a>', '</div>',
                    '</tpl>');
            div = new Ext.DataView({
                store : store,
                tpl : tpl,
                autoHeight : true,
                overClass : 'x-view-over',
                renderTo : "recent-viewed-div",
                id : "recent-viewed"
            });
        }

        var description = record.get('abstract');
        if (description.length > 140) {
            description = description.substring(0, 140) + "...";
        }

        var alreadyThere = false;

        Ext.each(div.store.data.items, function(e) {
            if (e.data.uuid === record.get('uuid')) {
                alreadyThere = true;
            }
        });

        if (!alreadyThere) {
            div.store.insert(0, new div.store.recordType({
                title : record.get('title'),
                uuid : record.get('uuid'),
                thumbnail : record.get('thumbnail'),
                description : description
            }));
        }
        while (div.store.data.length > 4) {
            div.store.remove(div.store.data.items[div.store.data.length - 1]);
        }

    }

    // public space:
    return {
        mapApp : null,
        searchApp : null,
        loginApp : null,
        breadcrumb : null,
        switchMode : function(i, j) {
        },
        /**
         * Deprecated, but maintained for compatibility with old UI versions. Do
         * not use, look for app.mapApp.maps or app.mapApp.getMap
         */
        getIMap : function() {
            return this.mapApp;
        },
        /**
         * Hides everything but the map, which becomes "fullscreen"
         */
        showBigMap : function() {
            showBigMap();
        },
        /**
         * Hides the "fullscreen" map in order to show the rest of the elements
         * (search and results, mostly).
         */
        hideBigMap : function() {
            hideBigMap();
        },
        /**
         * Initializes cookies, connections, url, etc,...
         */
        initializeEnvironment : function() {
            var geonetworkUrl = window.location.href.match(
                    /(http.*\/.*)\/srv\.*/, '')[1];

            urlParameters = GeoNetwork.Util.getParameters(location.href);

            var lang = /srv\/([a-z]{3})\/search/.exec(location.href);

            if (lang === null) {
                lang = GeoNetwork.Util.defaultLocale;
            } else if (Ext.isArray(lang)) {
                lang = lang[1];
            }

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
                        summaryStore : GeoNetwork.data.MetadataSummaryStore(),
                        editMode : 2,
                        metadataEditFn : edit,
                        metadataShowFn : show
                    });

            // Make sure we are still logged in:
            var response = OpenLayers.Request.GET({
                url : geonetworkUrl + '/srv/' + lang + '/admin',
                async : false
            }), exception;

            // Check status and also check than an Exception is not described in
            // the
            // HTML response
            // in case of bad startup
            exception = response.responseText.indexOf('Exception') !== -1;

            if (response.status !== 200 || exception) {
                delete cookie.state.user;
            } else {
                catalogue.identifiedUser = cookie.get('user');
            }

            // set a permalink provider which will be the main state provider.
            Ext.state.Manager
                    .setProvider(new GeoNetwork.state.PermalinkProvider({
                        encodeType : false
                    }));

            createLatestUpdate();
            createPopularUpdate();
            createMainTagCloud();

            this.breadcrumb = GeoNetwork.BreadCrumb();
            this.breadcrumb.setCurrent(this.breadcrumb.defaultSteps[0]);

        },
        /**
         * Function to initialize the App. Should only call utilities and their
         * init function. Keep it clean.
         */
        init : function() {

            this.initializeEnvironment();

            // Initialize utils
            this.loginApp = new GeoNetwork.loginApp();
            this.loginApp.init();
            this.mapApp = new GeoNetwork.mapApp();
            this.mapApp.init();
            this.searchApp = new GeoNetwork.searchApp();
            this.searchApp.init();

            if (urlParameters.create !== undefined && catalogue.isIdentified()) {
                var actionCtn = Ext.getCmp('resultsPanel').getTopToolbar();
                actionCtn.createMetadataAction.handler.apply(actionCtn);
            }
        },
        edit : function(uuid) {
            edit(uuid);
        },
        switchMode : function() {
            // Deprecated
        }
    };
};

Ext
        .onReady(function() {

            hideAdvancedSearch();

            var lang = /srv\/([a-z]{3})\/search/.exec(location.href);

            if (lang === null) {
                lang = GeoNetwork.Util.defaultLocale;
            } else if (Ext.isArray(lang)) {
                lang = lang[1];
            }

            var url = /(.*)\/srv/.exec(location.href)[1];

            GeoNetwork.Util.setLang(lang, url + '/apps');

            // This should be on search.xls, but IE9 is too fast
            var spiders = Ext.get("only_for_spiders");
            if (spiders) {
                spiders.setVisibilityMode(Ext.Element.DISPLAY);
                spiders.hide();
            }
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
            GeoNetwork.Util.updateHeadInfo({
                title : catalogue.getInfo().name
            });

            initShortcut();
            if (Ext.getDom('E_any')) {
                Ext.getDom('E_any').focus(true);
            }
            // Restoring session (if any)

            Ext.state.Manager.getProvider().restore(location.href);

            GeoNetwork.state.History.init();

            // Register events where the search should be triggered
            var events = [ 'afterDelete', 'afterRating', 'afterLogin' ];
            Ext
                    .each(
                            events,
                            function(e) {
                                catalogue
                                        .on(
                                                e,
                                                function() {
                                                    if (Ext.get(resultsPanel)
                                                            .isVisible()) {
                                                        var e = Ext
                                                                .getCmp('advanced-search-options-content-form');
                                                        e.getEl().fadeIn();
                                                        e.fireEvent('search');
                                                    }
                                                });
                            });

            if (!cookie.get("alreadyShowMessage")) {
                GeoNetwork
                        .Message()
                        .msg(
                                {
                                    title : OpenLayers.i18n('cookies'),
                                    msg : OpenLayers.i18n('cookies.warning')
                                            + "<p><input type='button' value='"
                                            + OpenLayers
                                                    .i18n('disclaimer.buttonClose')
                                            + "' onclick=\"Ext.get('cookie-warning').remove();\"/></p>",
                                    status : 'information',
                                    target : document.body,
                                    id : 'cookie-warning',
                                    pause : 40
                                });
                cookie.set('alreadyShowMessage', true);
            }

            Ext.getCmp("fullTextField").keyNav.enter = function(e) {
                Ext.getCmp('advanced-search-options-content-form').fireEvent(
                        'search');

            };

        });

/**
 * Resize maps and panels on window resize to acomodate content
 */
Ext.fly(window).on('resize', function(e, w) {
    resizeMap();
    resizeResultsPanel();

    var doLayout_children = function(obj) {

        if (obj.doLayout) {
            obj.doLayout(true, true);
        }

        if (obj.items && obj.items.items) {
            Ext.each(obj.items.items, function(e) {
                if (e.doLayout) {
                    doLayout_children(e);
                }
            });
        }

        if (obj.doLayout) {
            obj.doLayout(true, true);
        }
    };

    doLayout_children(Ext.getCmp("advSearchTabs"));
});