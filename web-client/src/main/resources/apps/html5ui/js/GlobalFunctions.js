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

// The following functions are for the advanced search hiding and showing
function hide(id) {
    if (Ext.get(id)) {
        Ext.get(id).setVisibilityMode(Ext.Element.DISPLAY);
        Ext.get(id).hide();
    }
}
function show(id) {
    if (Ext.get(id)) {
        Ext.get(id).setVisibilityMode(Ext.Element.DISPLAY);
        Ext.get(id).show();
    }
}

function toggleLogin() {
    toggle('login-form');
    Ext.get('username').focus();
}

function toggle(id) {
    if (Ext.get(id)) {
        if (Ext.get(id).isDisplayed()) {
            hide(id);
        } else {
            show(id);
        }
    }
}

function showBrowse() {
    // Reset search for tag cloud
    // catalogue.kvpSearch("fast=index&from=1&to=5&sortBy=changeDate", null, null, null, true);

    show("main");
    hide("search-form");

    hideAbout();
    hideSearch();
    hideBigMap();
    hideMetadata();

    show("browser");
    //show("latest-metadata");
    //show("popular-metadata");

    app.breadcrumb.setPrevious([]);
    app.breadcrumb.setCurrent(app.breadcrumb.defaultSteps[0]);

    Ext.each(Ext.query('a', Ext.get("main-navigation").dom), function(a) {
        Ext.get(a).removeClass("selected");
    });

    Ext.get("browse-tab").addClass("selected");
}

function hideBrowse() {
    hide("browser");
    //hide("latest-metadata");
    //hide("popular-metadata");
}

function showAbout() {

    show("about");
    hide("search-form");

    hideBrowse();
    hideSearch();
    hideBigMap();
    hideMetadata();

    app.breadcrumb.setCurrent(app.breadcrumb.defaultSteps[2]);

    Ext.each(Ext.query('a', Ext.get("main-navigation").dom), function(a) {
        Ext.get(a).removeClass("selected");
    });

    Ext.get("about-tab").addClass("selected");
}

function hideAbout() {
    hide("about");
}

function showBigMap() {
    hideBrowse();
    hideSearch();
    hideAbout();
    hideMetadata();
    hide("search-form");

    // show map
    show("big-map-container");
    // Resize the map, to cover all space available:
    resizeMap();

    app.breadcrumb.setCurrent({
        text : OpenLayers.i18n("Map"),
        func : "showBigMap()"
    });

    // Printpanel can be only initiazed once the map is rendered
    // Trigger the print panel init only when the big map is displayed
    // the first time. It will check if the print panel is already initiliazed
    // or not
    app.mapApp.initPrint();
    

    Ext.each(Ext.query('a', Ext.get("main-navigation").dom), function(a) {
        Ext.get(a).removeClass("selected");
    });

    Ext.get("map-tab").addClass("selected");
}

function hideBigMap() {
    hide("big-map-container");
}

function showSearch() {
    hideBrowse();
    hideAbout();
    hideMetadata();
    hideBigMap();
    show("search-form");

    show("secondary-aside");
    Ext.getCmp('resultsPanel').show();
    Ext.get('resultsPanel').show();
    show("main-aside");

    app.breadcrumb.setDefaultPrevious(1);
    app.breadcrumb.setCurrent(app.breadcrumb.defaultSteps[1]);

    if (!app.searchApp.firstSearch) {
        app.searchApp.firstSearch = true;

        Ext.getCmp('advanced-search-options-content-form').fireEvent('search');
    }


    Ext.each(Ext.query('a', Ext.get("main-navigation").dom), function(a) {
        Ext.get(a).removeClass("selected");
    });

    Ext.get("catalog-tab").addClass("selected");
    
}

function hideSearch() {
    // Comment the following if you want a push-down and not a hiding
    // for the results
    hide("secondary-aside");
    hide("resultsPanel");
    hide("main-aside");
}

function showMetadata() {

    hide("search-form");
    hideBrowse();
    hideAbout();
    hideSearch();
    hideBigMap();

    show("metadata-info");

    app.breadcrumb.setDefaultPrevious(2);

    Ext.each(Ext.query('a', Ext.get("main-navigation").dom), function(a) {
        Ext.get(a).removeClass("selected");
    });

    Ext.get("catalog-tab").addClass("selected");
}

function hideMetadata() {
    hide("metadata-info");
    hide("share-capabilities");

    // Destroy potential existing panel
    Ext.getCmp('metadata-panel') && Ext.getCmp('metadata-panel').destroy();
    Ext.getCmp('editorPanel') && Ext.getCmp('editorPanel').destroy();
}

function resizeResultsPanel() {
    var resultsPanel = Ext.get("resultsPanel");
//    if(Ext.getBody().getWidth() > 1000) {
//        resultsPanel.setWidth(Ext.getBody().getWidth()
//                - Ext.get("main-aside").getWidth() - 320);
//    } else {
        resultsPanel.setWidth(Ext.getBody().getWidth()
                - Ext.get("main-aside").getWidth());
//    }
    
    Ext.get("result-panel").setWidth(resultsPanel.getWidth());
    Ext.each(resultsPanel.dom.children, function(div) {
        div = Ext.get(div);
        Ext.each(div.dom.children, function(child) {
            child = Ext.get(child);
            child.setWidth("100%");
            Ext.each(child.dom.children, function(gchild) {
                Ext.get(gchild).setWidth("100%");
            });
        });
    });
}

function resizeMap() {

    if (Ext.getCmp("big-map")) {
        Ext.getCmp("big-map").doLayout(false, true);

        var div = Ext.get("big-map");

        var setChildrens = function(children) {
            Ext.each(children, function(child) {
                child = Ext.get(child);
                var classN = child.dom.className;
                if (classN
                        && classN.indexOf
                        && (classN.indexOf("tbar") < 0 && classN
                                .indexOf("x-panel-header") < 0)) {
                    child.setHeight("100%");
                    if (child.id
                            && (child.id.indexOf("ViewPort") < 0 && child.id
                                    .indexOf("layerManager") < 0)) {
                        setChildrens(child.dom.children);
                    }
                }
            });
        };
        var height = Ext.getBody().getHeight() - Ext.get("footer").getHeight()
                - Ext.get("header").getHeight()
                - Ext.get("search-form").getHeight() - 20;

        setChildrens(div);
        div.setHeight(height);
    }

    if (Ext.getCmp("layerManager")) {
        Ext.getCmp("layerManager").collapse(false);
        Ext.getCmp("layerManager").expand(false);
    }
    app.mapApp.getMap().updateSize();

    if (app.mapApp.getMap().getZoom() < 4) {
        app.mapApp.getMap().zoomTo(4);
    }
}

function showAdvancedSearch() {
    hide('show-advanced');
    show('legend-search');
    show('hide-advanced');
    Ext.get("search-form-fieldset").dom.style.border = "1px solid #fff";
    show('advanced-search-options');
    if (Ext.getCmp('advanced-search-options-content-form')) {
        Ext.getCmp('advanced-search-options-content-form').doLayout();

        // For reset and submit buttons:

        t = Ext.getCmp('advanced-search-options-content-form').toolbars[0];
        document.getElementById(t.el.parent().id).style.width = "";
    }
    if (cookie && cookie.get('user')) {
        cookie.get('user').searchTemplate = 'FULL';
    } else if (cookie) {
        cookie.set('user', {});
        cookie.get('user').searchTemplate = 'FULL';
    }
    if (catalogue && catalogue.resultsView && catalogue.resultsView.autoSelectTemplate) {
        catalogue.resultsView.autoSelectTemplate();
    }
}

function resetAdvancedSearch(updateSearch) {

    if (Ext.getCmp('advanced-search-options-content-form')) {

        GeoNetwork.state.History.suspendEvents();

        Ext.getCmp('advanced-search-options-content-form').getForm().reset();

        var value = Ext.getCmp("fullTextField").getValue();

        if (value.length > 0)
            Ext.getCmp('E_trueany').setValue(value + "*");
        else
            Ext.getCmp('E_trueany').setValue(value);

        Ext.getCmp('E_dynamic').suspendEvents(false);
        Ext.getCmp('E_dynamic').checked = (Ext.getCmp("o_dynamic").getValue());
        Ext.getCmp('E_dynamic').resumeEvents();

        Ext.getCmp('E_download').suspendEvents(false);
        Ext.getCmp('E_download').checked = (Ext.getCmp("o_download").getValue());
        Ext.getCmp('E_download').resumeEvents();

        Ext.getCmp('E_nodynamicdownload').suspendEvents(false);
        Ext.getCmp('E_nodynamicdownload').checked = (
        Ext.getCmp("o_nodynamicdownload").getValue());
        Ext.getCmp('E_nodynamicdownload').resumeEvents();

				Ext.getCmp('sortByToolBar').setValue("relevance");

        GeoNetwork.state.History.resumeEvents();

    }

    if (Ext.getCmp('facets-panel')) {
        Ext.getCmp('facets-panel').reset();
    }
}

function hideAdvancedSearch(updateSearch) {

    hide('advanced-search-options');
    hide('legend-search');
    hide('hide-advanced');
    Ext.get("search-form-fieldset").dom.style.border = "none";
    show('show-advanced');
    if (updateSearch) {
        if (cookie && cookie.get('user')) {
            cookie.get('user').searchTemplate = 'THUMBNAIL';
        } else if (cookie) {
            cookie.set('user', {});
            cookie.get('user').searchTemplate = 'THUMBNAIL';
        }
        if (catalogue && catalogue.resultsView 
                && catalogue.resultsView.autoSelectTemplate) {
            catalogue.resultsView.autoSelectTemplate();
        }
    }
}

/**
 * This function only works in IE. In IE8, the user has to confirm a security
 * alert. Other browsers not tested.
 */
function copyToClipboard(text) {
    var textfield = Ext.getCmp("copy-clipboard-ie_");

    if (!textfield) {
        textfield = new Ext.form.TextField({
            id : "copy-clipboard-ie_",
            renderTo : "copy-clipboard-ie"
        });
    }
    show(textfield);
    textfield.setValue(text);
    textfield.focus();
    textfield.selectText();

    if (window.clipboardData && clipboardData.setData) {
        window.clipboardData.setData('Text', textfield.getValue());
    } else {
        CopiedTxt = document.selection.createRange();
        CopiedTxt.execCommand("Copy");
    }
    // clearSelection();
    hide(textfield);
}
/**
 * @param uuid
 * @return {String}
 */
function metadataViewURL(uuid) {
    return window.location.href.match(/(http.*\/.*)\/srv\.*/, '')[1] + '?uuid='
            + uuid;
}

// Validate a WMS or WFS against the Geonovum service
function validateWMSWFS(capsURL, el, type) {

    el = Ext.get(el);

    if (el)
        el = el.parent().parent().parent().parent().parent();

    if (!el)
        el = Ext.getBody();

    // Load mask to prevent more than one click:
    if (el) {
        var mask = new Ext.LoadMask(el, {
            msg : OpenLayers.i18n('disclaimer.loading')
        });
        mask.show();
    }

    // do NOT use the REQUEST=GetCapabilities etc, because the validator
    // seesm to trip over this
    capsURL = capsURL.replace(/REQUEST=GetCapabilities/i, "");
    // capsURL = capsURL.replace(/SERVICE=WMS/i, "");
    capsURL = capsURL.replace(/VERSION=1.1.1/i, "");
    capsURL = capsURL.replace(/&&/gi, "&"); // replace any double &
    capsURL = capsURL.replace(/\?&/gi, "?"); // replace any combi of ?&

    // now use that url for validation and presentation of the results
    var params = {
        type : type,
        wmsurl : capsURL
    };
    var totalUrl = 'validators.wms';

    Ext.Ajax.request({
        method : 'GET',
        url : totalUrl,
        params : params,
        success : function(response) {
            mask.hide();

            Ext.MessageBox.show({
                title : OpenLayers.i18n('validityInfo'),
                msg : response.responseText,
                buttons : Ext.MessageBox.OK,
                icon : Ext.MessageBox.INFO,
                minWidth : 600,
                maxWidth : 800
            });

        },
        failure : function(response) {
            mask.hide();

            Ext.MessageBox.show({
                title : OpenLayers.i18n('validityInfo'),
                msg : OpenLayers.i18n('wxs-extract-service-not-found', {
                    url : "validation",
                    misc : response.responseText
                }),
                buttons : Ext.MessageBox.OK,
                icon : Ext.MessageBox.ERROR,
                minWidth : 600,
                maxWidth : 800
            });
        }
    });
}

