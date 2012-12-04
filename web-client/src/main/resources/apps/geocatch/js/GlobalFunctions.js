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
    hideSearch();
    hideBigMap();
    hideMetadata();

    show("browser");
    show("latest-metadata");
    show("popular-metadata");

    app.breadcrumb.setPrevious([]);
    app.breadcrumb.setCurrent(app.breadcrumb.defaultSteps[0]);
}

function hideBrowse() {
    hide("browser");
    hide("latest-metadata");
    hide("popular-metadata");
}

function showBigMap() {
    hideBrowse();
    hideSearch();
    hideMetadata();

    // Hide advanced search
    hide('advanced-search-options');
    show('show-advanced');

    // show map
    show("big-map-container");

    app.breadcrumb.setDefaultPrevious(2);
    app.breadcrumb.setCurrent({
        text : OpenLayers.i18n("Map"),
        func : "showBigMap()"
    });

    // Resize the map, to cover all space available:
    resizeMap();

    Ext.getCmp('big-map').doLayout();


    // Printpanel can be only initiazed once the map is rendered
    // Trigger the print panel init only when the big map is displayed
    // the first time. It will check if the print panel is already initiliazed or not
    app.mapApp.initPrint();
}

function hideBigMap() {
    hide("big-map-container");

    hide("secondary-aside");
    hide("resultsPanel");
    hide("main-aside");
}

function showSearch() {
    hideBrowse();
    hideMetadata();
    hideBigMap();

    show("secondary-aside");
    Ext.getCmp('resultsPanel').show();
    Ext.get('resultsPanel').show();
    show("main-aside");

    app.breadcrumb.setDefaultPrevious(1);
    app.breadcrumb.setCurrent(app.breadcrumb.defaultSteps[1]);
}

function hideSearch() {
    // Comment the following if you want a push-down and not a hiding
    // for the results
    hide("secondary-aside");
    hide("resultsPanel");
    hide("main-aside");
}

function showMetadata() {
    hideBrowse();
    hideSearch();

    show("metadata-info");

    app.breadcrumb.setDefaultPrevious(2);
}

function hideMetadata() {
    hide("metadata-info");
    hide("share-capabilities");

    // Destroy potential existing panel
    Ext.getCmp('metadata-panel') && Ext.getCmp('metadata-panel').destroy();
    Ext.getCmp('editorPanel') && Ext.getCmp('editorPanel').destroy();
}

function resizeMap() {
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
        - Ext.get("search-form").getHeight();

    setChildrens(div);
    div.setHeight(height);

    app.mapApp.getMap().updateSize();
}

function showAdvancedSearch() {
    app.hideBigMap();
    hide('show-advanced');
    show('legend-search');
    Ext.get("search-form-fieldset").dom.style.border = "1px solid #fff";
    show('advanced-search-options');
    if (Ext.getCmp('advanced-search-options-content-form')) {
        Ext.getCmp('advanced-search-options-content-form').doLayout();
    }
}

function hideAdvancedSearch() {
    hide('advanced-search-options');
    hide('legend-search');
    Ext.get("search-form-fieldset").dom.style.border = "none";
    show('show-advanced');
}
function toggleMoreAdvancedOptions() {

    if (Ext.get("where_adv_search").isDisplayed()
        && Ext.get("what_adv_search").isDisplayed()
        && Ext.get("when_adv_search").isDisplayed()
        && Ext.get("inspire_adv_search").isDisplayed()) {
        hide('where_adv_search');
        hide('what_adv_search');
        hide('when_adv_search');
        hide('inspire_adv_search');
        if (Ext.isIE) {
            Ext.get("show_more_search_options").dom.innerText = OpenLayers
                .i18n('Show More');
        } else {
            Ext.get("show_more_search_options").dom.textContent = OpenLayers
                .i18n('Show More');
        }
    } else {
        show('where_adv_search');
        show('what_adv_search');
        show('when_adv_search');
        show('inspire_adv_search');
        if (Ext.isIE) {
            Ext.get("show_more_search_options").dom.innerText = OpenLayers
                .i18n('Show Less');
        } else {
            Ext.get("show_more_search_options").dom.textContent = OpenLayers
                .i18n('Show Less');
        }
    }
}


/**
 * Return a URL to metadata view page. TODO what is the URL for the NGR2 GUI for
 * this ?
 *
 * @param uuid
 * @return {String}
 */
function metadataViewURL(uuid) {
    return window.location.protocol + '//' + window.location.host
        + Env.locService + '/main.home?searchuuid=' + uuid;
}

