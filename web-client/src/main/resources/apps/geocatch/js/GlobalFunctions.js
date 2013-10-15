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
    if (id.el) {
        id = id.el;
    }

    if (id.id) {
        id = id.id;
    }

    if (Ext.get(id)) {
        Ext.get(id).setVisibilityMode(Ext.Element.DISPLAY);
        Ext.get(id).hide();
    }
}
function show(id, recursive) {
    if (id.el) {
        id = id.el;
    }

    if (id.id) {
        id = id.id;
    }

    if (Ext.get(id)) {
        Ext.get(id).setVisibilityMode(Ext.Element.DISPLAY);
        Ext.get(id).show();

        if (recursive) {
            Ext.get(id).dom.className = Ext.get(id).dom.className.replace(
                    "x-input-hidden", "");

            Ext.get(id).dom.className = Ext.get(id).dom.className.replace(
                    "x-hide-display", "");

            Ext.each(Ext.get(id).dom.children, function(child) {
                show(child, true);
            });
        }
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

    // app.breadcrumb.setPrevious([]);
    // app.breadcrumb.setCurrent(app.breadcrumb.defaultSteps[0]);
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
    // the first time. It will check if the print panel is already initiliazed
    // or not
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
    if (Ext.getCmp('resultsPanel')) {
        Ext.getCmp('resultsPanel').show();
    }
    show('resultsPanel');
    show("main-aside");

    // app.breadcrumb.setDefaultPrevious(1);
    // app.breadcrumb.setCurrent(app.breadcrumb.defaultSteps[1]);
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

    // app.breadcrumb.setDefaultPrevious(2);
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
    hide('show-advanced');
    show('hide-advanced');
    hide(Ext.getCmp('simple-search-options-content-form'));
    show(Ext.getCmp('advanced-search-options-content-form'));
    Ext.getCmp("vp").doLayout();

    if (!catalogue.identifiedUser) {
        // Hide some advanced search options
        hide(Ext.getCmp("formatCombo").el.parent().parent().parent().parent()
                .parent().parent());
        hide(Ext.getCmp("isTemplate").el.parent().parent().parent());
        hide(Ext.getCmp("isValid").el.parent().parent().parent());
    } else {
        show(Ext.getCmp("formatCombo").el.parent().parent().parent().parent()
                .parent().parent(), true);
        show(Ext.getCmp("isTemplate").el.parent().parent().parent(), true);
        show(Ext.getCmp("isValid").el.parent().parent().parent(), true);
    }
    // Restore map restrictions
    Ext.each(Ext.query("input[name=G_whereType]"), function(el) {
        if (el.checked && Ext.getCmp(el.id)) {
            Ext.getCmp(el.id).setValue(false);
            Ext.getCmp(el.id).setValue(true);
        }
    });
}

function hideAdvancedSearch() {
    show('show-advanced');
    hide('hide-advanced');
    hide(Ext.getCmp('advanced-search-options-content-form'));
    show(Ext.getCmp('simple-search-options-content-form'));

    // Restore map restrictions
    if (app.mapApp && app.mapApp.getMap()) {
        var el = Ext.getCmp("kantoneComboBox");
        if (el) {
            el.fireEvent("change", el);
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
