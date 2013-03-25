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
    catalogue.kvpSearch("fast=index&from=1&to=5&sortBy=changeDate", null, null,
            null, true);

    show("main");
    show("search-form");
    hide("pdok-loads");
    hide("foot-loads");

    hideSearch();
    hideBigMap();
    hideMetadata();

    show("browser");
    show("latest-metadata");
    show("popular-metadata");

    app.breadcrumb.setPrevious([]);
    app.breadcrumb.setCurrent(app.breadcrumb.defaultSteps[0]);

    Ext.get("pdok-loads").update("");
    Ext.get("foot-loads").update("");

    Ext.each(Ext.query('a', Ext.get("main-navigation").dom), function(a) {
        Ext.get(a).removeClass("selected");
    });

    Ext.get("browse-tab").addClass("selected");

    hideAdvancedSearch();
}

function hideBrowse() {
    hide("browser");
    hide("latest-metadata");
    hide("popular-metadata");
}

function showBigMap() {
    hideBrowse();
    hideSearch();
    hideAdvancedSearch();
    hideMetadata();
    show("search-form");

    // Hide advanced search
    hide('advanced-search-options');
    show('show-advanced');

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
}

function hideBigMap() {
    hide("big-map-container");
}

function showSearch() {
    hideBrowse();
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
}

function hideSearch() {
    // Comment the following if you want a push-down and not a hiding
    // for the results
    hide("secondary-aside");
    hide("resultsPanel");
    hide("main-aside");
}

function showMetadata() {

    show("search-form");
    hideBrowse();
    hideSearch();
    hideBigMap();
    hideAdvancedSearch();

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

function resizeResultsPanel() {
    var resultsPanel = Ext.get("resultsPanel");
    resultsPanel.setWidth(Ext.getBody().getWidth()
            - Ext.get("main-aside").getWidth() - 320);
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

function hideAdvancedSearch(updateSearch) {

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

        GeoNetwork.state.History.resumeEvents();

    }

    if (Ext.getCmp('facets-panel')) {
        Ext.getCmp('facets-panel').reset();
    }

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
        if (catalogue && catalogue.resultsView) {
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

// the following function is for bookmarking uuids:
// Chrome doesn't support it, then show permalink
function bookmarkMetadata(title, uuid) {
    var url = document.location.href;
    if (url.indexOf("#") > 0) {
        url = url.substring(0, url.indexOf("#"));
    }
    if (url.indexOf("?") > 0) {
        url = url.substring(0, url.indexOf("?"));
    }

    url = url + "|#" + uuid;

    if (window.sidebar) { // firefox
        window.sidebar.addPanel(title, url, "");
    } else if (window.opera && window.print) { // opera
        var elem = document.createElement('a');
        elem.setAttribute('href', url);
        elem.setAttribute('title', title);
        elem.setAttribute('rel', 'sidebar');
        elem.click();
    } else if (Ext.isIE || window.external.addFavorite) {
        window.external.AddFavorite(url, title);
    } else {
        // TODO show permalink
    }
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

// Load tabs from PDOK page:
function loadPDOK(el, page) {

    var base_url = "https://www.pdok.nl/";

    var url = base_url + page;

    Ext.get("pdok-loads").load(
            {
                url : url,
                callback : function(response, opts) {
                    var dom = Ext.get("pdok-loads").dom;
                    var toRemove = [ "header", "nav_main", "footer",
                            "footer_links", "nav_bar", "func_links",
                            "breadcrumb", "nav_sub" ];
                    Ext.each(toRemove, function(clas) {
                        Ext.each(Ext.query("." + clas, dom), function(el) {
                            Ext.get(el).remove();
                        });
                    });
                }
            });

    Ext.each(Ext.query('a', Ext.get("main-navigation").dom), function(a) {
        Ext.get(a).removeClass("selected");
    });

    Ext.get(el).addClass("selected");

    hide("main");
    hide("foot-loads");
    show("pdok-loads");
}

// Load foot text
function loadFoot(page) {

    var text_dut = "";
    var text_eng = "";

    switch (page) {
    case 'copyright':
        text_dut = "<p>Deze pagina geeft nadere uitleg over de copyright aspecten van software, teksten, afbeeldingen en geografische informatie (hierna te noemen: werk) binnen het NGR.</p>"
                + "<p>De gebruikte software voor dit register, GeoNetwork, is opensource software. Het wordt momenteel gebruikt in tal van Spatial Data Infrastructure initiatieven over de hele wereld. Voor informatie <a href=\"http://geonetwork-opensource.org/\">http://geonetwork-opensource.org/</a>.</p>"
                + "<p>In de metadata die in dit register toegang geeft tot datasets en services is bij overige beperkingen de van toepassing zijnde licentie opgenomen. In de meeste gevallen is dat de publiek domein verklaring. Op de kaart gebruikt in de kaartviewer en de zoekfuncties, de BRT Achtergrondkaart is de CC-BY-SA 3.0 licentie is van toepassing.</p>";
        text_eng = "<p>This page gives further explanation about the copyright aspects of software, text, graphics and geographic information (hereinafter referred to as work) within the NGR.</p>"
                + "<p>The software used for this register, GeoNetwork is open source software. It is currently used in numerous Spatial Data Infrastructure initiatives across the world. For information <a href=\"http://geonetwork-opensource.org/\">http://geonetwork-opensource.org/</a>.</p>"
                + "<p>The metadata in this register provides access to data sets and services. In the metadata element other restrictions the applicable license is included. In most cases is this the public domain statement. On the map used in the map viewer and search functions, the BRT Background Map, the CC-BY-SA 3.0 license applies.</p>";
        break;
    case 'privacy':
        text_dut = "Persoonsgegevens die via deze site worden verzameld, gebruikt NGR alleen voor het doel waarmee u ze hebt achtergelaten. Daarmee voldoet NGR aan de privacywetgeving.</p>"
                + "<p>Een persoonsgegeven is informatie die herleidbaar is tot een persoon. Voorbeeld van een persoonsgegeven is een naam of een huisadres, maar ook e-mailadressen kunnen persoonsgegevens zijn.</p>"
                + "<p>Daar waar uw persoonsgegevens verwerkt worden, staat vermeld welke gegevens voor welke doeleinden gebruikt worden. NGR gebruikt uw persoonsgegevens enkel voor het doel waarvoor u ze heeft achtergelaten. Dat betekent dat als u bijvoorbeeld uw naam en adres invult voor het geven van feedback, deze gegevens niet worden gebruikt voor andere doeleinden, zoals het toesturen van bijvoorbeeld persberichten.</p>"
                + "<p>Wij maken gebruik van Google Analytics voor statistische analyses van het bezoek en klikgedrag op de website. Daarmee kunnen wij de werking en uw gebruik van onze website analyseren, zodat we functionaliteiten daarop kunnen aanpassen. Ze zullen nooit worden gebruikt om gebruikers te volgen.</p>";
        text_eng = "<p>Personal information submitted through this site is being collected and stored. NGR will only use the information for the purpose for which you submitted that information. In this way, NGR complies with the privacy legislation.</p>"
                + "<p>Personal data is information that can be traced to an individual. Examples of personal information are - amongst others- name, home address, e-mail address, and so on.</p>"
                + "<p>In case your personal data items are requested and processed, you will find specific information regarding the purposes. NGR uses your personal information only for the purpose for which you have provided it. That means that if you submit your name and address because you wish to give feedback, then these data items are not used for other purposes, such as sending press releases unless you agree.</p>"
                + "<p>We use Google Analytics for statistical analyzes of the visit and clicks on the website. It promotes the functioning and analyze your use of our website, so we can customize functionalities thereon. They will never be used to track users.</p>";
        break;
    case 'cookies':
        text_dut = "<p>Per 5 juni 2012 is, als onderdeel van de Telecomwet, de Cookiewet van kracht. Deze stelt eisen aan het gebruik van cookies door websites. Cookies zijn kleine tekstbestandjes die worden opgeslagen op uw harde schijf of in het geheugen van uw computer. Ze zijn gekoppeld aan de webbrowser (Explorer, Chrome, Firefox, Safari, etc.) op de computer waarmee u een website bezoekt.</p>"
                + "<p>NGR heeft veel moeite gedaan om de website cookie-vrij te houden. In slechts een situaties wordt een noodzakelijke en wettelijk toegestane cookie gebruikt.</p>"
                + "<p>Als u dataprovider voor het NGR bent, is inloggen noodzakelijk en wordt een cookie bewaard.</p>"
                + "<p>Uiteraard kunt u ook in uw browser instellen dat er geen cookies worden gebruikt. De inlog- en registratiefunctionaliteit van het NGR kan dan niet worden gegarandeerd.</p>"
                + "<p>Op de website van het Nationaal Cyber Security Centrum van het Ministerie van Veiligheid en Justitie kunt u meer informatie vinden over veilig internetten.</p>";
        text_eng = "<p>New Dutch Telecom legislation became effective on the 5th of June 2012. The new law is sometimes referred to as the \"Cookie-law\" since the law includes some sections on the use of internet cookies. Cookies are small text files stored on the harddisk or in the memory of your local computer. These text files are used by the webbrowser (Explorer, Chrome, Firefox, Safari, etc.) on the computer that is used to visit websites.</p>"
                + "<p>NGR has made a significant effort to ban cookies from the website. There are only one instances where cookies are used in the website and this is in compliance with the law:</p>"
                + "<p>• If you are dataprovider to NGR, you will need to register and log-in. A cookie will be stored.</p>"
                + "<p>It is of course possible to ban cookies all together. The log-in and registration functionality of the NGR can then not be guaranteed.</p>"
                + "<p>The website of the National Cyber Security Centre of the Ministry of Justice and Security provides information about safe internet usage.</p>";
        break;
    }

    var lang = /srv\/([a-z]{3})\/search/.exec(window.location.href);

    if (Ext.isArray(lang)) {
        lang = lang[1];
    }

    if (lang === "eng") {
        Ext.get("foot-loads").update("<p><h1>" + page + "</h1></p>" + text_eng);
    } else {
        Ext.get("foot-loads").update("<p><h1>" + page + "</h1></p>" + text_dut);
    }
    hide("main");
    hide("pdok-loads");
    show("foot-loads");
}
