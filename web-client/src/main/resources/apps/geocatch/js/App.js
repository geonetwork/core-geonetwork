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

            // Application layout
            this.viewport = new Ext.Viewport({
                layout : 'border',
                layoutConfig : {
                    minWidth : 1080
                },
                items : [
                // Header
                {
                    region : 'north',
                    contentEl : 'header',
                    border : false,
                    margins : '0 0 0 0',
                    autoHeight : true
                },
                // Search/map
                {
                    region : 'west',
                    border : false,
                    split : true,
                    minWidth : 300,
                    maxWidth : 400,
                    width : 300,
                    margins : '0 0 0 0',
                    border : false,
                    layout : 'border',
                    items : [
                    // Search panel
                    {
                        region : 'center',
                        layout : 'border',
                        items : [ {
                            region : 'center',
                            contentEl : 'search',
                            border : false
                        }, {
                            region : 'south',
                            id : 'searchSwitch',
                            border : false,
                            contentEl : 'search-switcher'
                        } ]

                    },
                    // Map panel
                    {
                        region : 'south',
                        contentEl : 'map-div',
                        border : false,
                        height : 250,
                        border : false,
                        bodyStyle : 'background-color: #cccccc'
                    } ]

                },
                // Search results
                {
                    region : 'center',
                    contentEl : 'search-results',
                    border : false,
                    autoHeight : true,
                    margins : '0 0 0 0',
                    layout : 'fit'
                },
                // Filter panel
                {
                    id: 'facets-container',
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
                } ],
                listeners : {
                    render : function(e) {
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

    app.viewport.doLayout();
});

var translations = {
    "ContactSelectionTitle" : "Kontaktauswahl",
    "FormatSelectionTitle" : "Formatauswahl",
    "ExtentSelectionTitle" : "Ausdehnungsauswahl",
    "refine.titles" : "Title",
    "refine.keyword" : "Schlüsselworter",
    "refine.resolution" : "Räumliche Auflösung",
    "refine.category" : "Thematik",
    "refine.spatialRepresentation" : "Struktur der räumlichen Daten",
    "refine.organisationName" : "Organisation",
    "refine.serviceType" : "Art des Dienstes",
    "refine.type" : "Typ",
    "geocatNext" : "Vorwärts",
    "geocatPrevious" : "Zurück",
    "replaceSharedObjects" : "Wiederverwendbare Elemente bestätigen",
    "spatialRepresentation" : "Struktur der räumlichen Daten",
    "selectedElementsAction" : "Ausgewählte Metadaten",
    "resolution" : "Räumliche Auflösung",
    "mainpageTitle" : "Suche nach Geodaten und Geodiensten",
    "refinements" : "Verfeinerungen",
    "refineSearch.title" : "Suche verfeinern",
    "keyword" : "Schlüsselwort",
    "theme" : "ISO Thema (TopicCategory)",
    "contact" : "Nachname",
    "rtitle" : "Titel",
    "organisationName" : "Organisation",
    "kantone" : "Kanton(e)",
    "theme" : "Thema",
    "formatTxt" : "Format",
    "toEdit" : "In Bearbeitung",
    "toPublish" : "Zu publizieren",
    "bbox" : "Im aktuellen Kartenausschnitt",
    "wherenone" : "Überall",
    "adminUnit" : "Administrative Einheit",
    "drawOnMap" : "Auf der Karte zeichnen",
    "startNewPolygon" : "Polygon löschen oder ein neues zeichnen",
    "startNewPolygonHelp" : "Bitte zeichnen Sie ein Polygon (mit Doppelklick beenden)",
    "valid" : "Gültig",
    "yes" : "Ja",
    "no" : "Nein",
    "intersectGeo" : "schneidet",
    "withinGeo" : "vollständig innerhalb oder gleich",
    "containsGeo" : "umfasst mindestens",
    "layerTree" : "Layer-Liste",
    "nothingFound" : "Kein Ergebnis",
    "source" : "Herkunft?",
    "catalog" : "Katalog(e)",
    "extended" : "Erweiterte Suche",
    "hideAdvancedOptions" : "Einfache Suche",
    "highlights" : "Highlights",
    "hitsPerPage" : "Ergebnisse pro Seite",
    "popXlink.about" : "Suche nach einem existierenden Objekt oder Klick auf `Neu`, wenn nichts passendes gefunden wurde.",
    "popXlink.contact.search" : "Suchen nach einem Kontakt in der Liste",
    "popXlink.contact.role" : "Wählen Sie die Rolle des Kontakts",
    "popXlink.add.action.tooltip" : "Das ausgewählte Element im Metadatendokument zufügen",
    "popXlink.create.action.tooltip" : "Falls das gesuchte Element nicht gefunden wird, ein neues Element vorschlagen",
    "popXlink.extent.search" : "Suche nach einer Ausdehnung in der Liste",
    "popXlink.format.search" : "Formatsuche in der Liste",
    "extentBbox" : "Nur Begrenzungsrechteck",
    "extentPolygon" : "Nur Polygon",
    "extentBboxAndPolygon" : "Begrenzungsrechteck und Polygon",
    "extentTypeCode" : "Zuständige Stelle Code",
    "inclusion" : "Einschliessend",
    "exclusion" : "Ausschliessend",
    "xlink.new" : "Neu",
    "xlink.newGeographic" : "Neu (nur geografisch)",
    "noXlink" : "Das selektierte Element ist ungültig.",
    "addLayerConfirmTitle" : "Hinzufügen eines neuen WMS-Layers",
    "addLayerConfirmText" : "Wollen Sie die bereits hinzugefügten WMS-Layer von der Karte entfernen?",
    "other" : "- Andere -",
    "noSearchCriteria" : "Bitte geben Sie ein Suchkriterium an",
    "noServer" : "Bitte wählen Sie einen Server für die Suche",
    "add" : "Hinzufügen",
    "drawRectangle" : "Ein Rechteck zeichnen",
    "drawPolygon" : "Ein Polygon zeichnen",
    "drawCircle" : "Einen Kreis zeichnen",
    "geoPublisherWindowTitle" : "Geo-Publikation:",
    "publish" : "Publizieren",
    "publishTooltip" : "Diesen Datensatz auf einem entfernten Knoten publizieren. Wenn die Daten bereits publiziert sind, werden sie aktualisiert.",
    "publishError" : "Publikation fehlgeschlagen.",
    "publishErrorCode" : "Fehlercode:",
    "publishSuccess" : "Publikation erfolgreich.",
    "publishLayerAdded" : "Layer in Kartenviewer hinzugefügt.",
    "unpublish" : "Depublizieren",
    "unpublishTooltip" : "Diesen Datensatz vom entfernten Knoten entfernen.",
    "unpublishError" : "Depublikation fehlgeschlagen.",
    "unpublishSuccess" : "Depublikation erfolgreich.",
    "check" : "Überprüfen",
    "errorConnectionRefused" : "Verbindung verweigert.",
    "errorDatasetNotFound" : "Daten nicht gefunden.",
    "datasetFound" : "Daten gefunden und zum Kartenviewer hinzugefügt.",
    "checkFailure" : "Überprüfung der Daten im entfernten Knoten fehlgeschlagen.",
    "addOnlineSource" : "WMS info als Online Ressource hinzufügen",
    "statusInformation" : "Status Information.",
    "mapPreview" : "Kartenviewer",
    "selectANode" : "Einen Knoten auswählen ...",
    "publishing" : "Publizieren ...",
    "logoSelectionWindow" : "Ein Logo wählen",
    "logoRegistered" : "Gespeicherte Logos",
    "logoAdd" : "Ein neues Logo hinzufügen",
    "selectedLogo" : "Ausgewähltes Logo:",
    "logoDel" : "Entfernen",
    "logoForNode" : "Für den Katalog übernehmen",
    "logoForNodeFavicon" : "Als Favicon verwenden",
    "logoSelect" : "Ein Bild als Logo verwenden ...",
    "shortcutHelp" : "Hilfe zu shortcuts?",
    "validationReport" : "Validierungsbericht",
    "getCapabilitiesLayer" : "GetCapabilities layer",
    "ServiceUpdateError" : "Fehler bei der Aktualisierung der Dienst-Metadaten",
    "NotOwnerError" : "Sie haben nich das Recht den Datensatz zu aktualisieren.",
    "NoServiceURLError" : "Überprüfen Sie, ob eine URL für den Dienst angegeben wurde.",
    "GetCapabilitiesDocumentError" : "Fehler beim Laden der GetCapabilities von der URL:",
    "layerNameHelp" : "Wenn Dienstmetadaten mit einem oder mehreren Datenmetadaten verlinkt werden, enthält das Attribut angehängte Ressource den Layernamen wie er im OGC-Dienst definiert ist. Die Liste der Layer soll dem Bearbeiter bei der Auswahl existierender Layer helfen.",
    "layerName" : "Layername",
    "createIfNotExistButton" : "Einen neuen Metadatensatz erstellen",
    "associateService" : "Dienst-Metadaten verlinken",
    "associateDataset" : "Daten-Metadaten verlinken",
    "parentSearch" : "Nach Eltern-Datensatz suchen",
    "linkedMetadataSelectionWindowTitle" : "Metadaten verlinken",
    "searchText" : "Alle Felder",
    "mdTitle" : "Titel der Metadaten",
    "createRelation" : "Verbindung erstellen",
    "export" : "Export",
    "translateWithGoogle.maxSize" : "Der zu übersetzende Text ist länger als 5000 Zeichen (siehe http://code.google.com/apis/ajaxlanguage/terms.html).",
    "translateWithGoogle.emptyInput" : "Hauptsprache fehlt. Wert in der Hauptsprache angeben bevor der Dienst genutzt werden kann.",
    "layersAdded" : "Ausgewählte Layer wurden hinzugefügt",
    "registrationFailed" : "Fehler, Registrierung fehlgeschlagen:",
    "tryAgain" : "Versuchen Sie es später noch einmal.",
    "cannotRetrieveGroup" : "Kann die Gruppen nicht finden",
    "selectNewOwner" : "Wählen Sie den Benutzer der neuer Besitzer werden soll",
    "selectOwnerGroup" : "Wählen Sie eine Gruppe zu welcher der ausgewählte Benutzer gehört",
    "selectOneFile" : "Sie sollten mindestens eine Datei zum Download auswählen!",
    "checkEmail" : "Bitte geben Sie eine gültige E-Mail-Adresse an",
    "addName" : "Bitte geben Sie einen Namen oder eine Organisation an",
    "noComment" : "Kein Kommentar",
    "rateMetadataFailed" : "Kann die Metadaten nicht bewerten.",
    "error" : "Fehler",
    "northSouth" : "Nord < Süd",
    "north90" : "Nord > 90 Grad",
    "south90" : "Süd < -90 Grad",
    "east180" : "Ost > 180 Grad",
    "west180" : "West < -180 Grad",
    "eastWest" : "Ost < West",
    "metadataSelectionError" : "Fehler bei der Auswahl von Metadaten.",
    "closeWindow" : "Fenster schliessen",
    "loseYourChange" : "Wenn Sie OK drücken gehen sämtliche Änderungen verloren!",
    "errorDeleteElement" : "Fehler: Element konnte nicht gelöscht werden",
    "errorFromDoc" : "vom Dokument:",
    "errorMoveElement" : "Fehler: Element konnte nicht verschoben werden",
    "errorAddElement" : "Fehler: Element konnte nicht hinzugefügt werden",
    "errorSaveFailed" : "Fehler: Element konnte nicht gespeichert werden",
    "errorOnAction" : "Fehler: Weitere Aktion konnte nicht ausgeführt werden",
    "errorChangeProtocol" : "Eine Datei wurde hochgeladen. Das Protokoll kann nicht geändert werden bevor die Datei entfernt wurde.",
    "selectOneFile" : "Durchsuchen, oder einen Dateinamen angeben bevor `hochladen` gedrückt wird!",
    "uploadFailed" : "Fehler: Upload fehlgeschlagen! - zurück",
    "uploadSetFileNameFailed" : "Fehler: Upload ausgeführt aber Dateiname konnte nicht gesetzt werden!",
    "cannotGetTooltip" : "Tooltips vom Server nicht erhältlich",
    "maxResults" : "Anzahl Resultate",
    "perThesaurus" : "pro Thesaurus",
    "anyThesaurus" : "Alle Thesauri",
    "selectedKeywords" : "Ausgewählte Schlüsselwörter",
    "foundKeywords" : "Verfügbare Schlüsselwörter",
    "keywordSelectionWindowTitle" : "Schlüsselwort auswählen",
    "crsSelectionWindowTitle" : "Koordinatensystem auswählen",
    "selectedCRS" : "Ausgewählte Koordinatensysteme",
    "foundCRS" : "Verfügbare Koordinatensysteme",
    "abstract" : "Zusammenfassung",
    "add" : "hinzufügen",
    "all" : "Alle",
    "any" : "- Alle -",
    "category" : "Kategorie",
    "city" : "Stadt",
    "clear" : "leeren",
    "close" : "Schliessen",
    "country" : "Staat",
    "create" : "Erstellen",
    "delete" : "Löschen",
    "edit" : "Bearbeiten",
    "emailAddressInvalid" : "E-Mail Adresse ist ungültig",
    "firstNameMandatory" : "Der Vorname ist verpflichtend",
    "from" : "Von",
    "helpLinkTooltip" : "Hilfe",
    "identifier" : "Geobasisdaten-ID",
    "insertFileMode" : "Dateiupload",
    "keywords" : "Schlüsselwörter",
    "lastNameMandatory" : "Der Nachname ist verpflichtend",
    "loading" : "Laden ...",
    "metadata.admin.index.failed" : "Index operation fehlgeschlagen.",
    "metadata.admin.index.success" : "Index operation erfolgreich gestartet.",
    "metadata.admin.index.wait" : "Index operation läuft bereits, bitte warten.",
    "doYouReallyWantToDoThis" : "Diese Operation kann bei grossen Katalogen einige Zeit in Anspruch nehmen und sollte nicht während Spitzenzeiten ausgeführt werden. Weiterfahren?",
    "none" : "nichts",
    "remove" : "entfernen",
    "resultsMatching" : "Suchergebnisse:",
    "search" : "Suche",
    "searching" : "...Suche nach Metadaten...",
    "selected" : "ausgewählt",
    "show" : "Metadaten",
    "sortBy" : "Geordnet nach",
    "spacesNot" : "Leerzeichen sind in diesem Feld nicht erlaubt",
    "template" : "Vorlage",
    "to" : "Nach",
    "type" : "Typ",
    "upload" : "Upload",
    "userAtLeastOneGroup" : "Bitte wählen Sie mindestens eine Gruppe aus.",
    "waitGetCap" : "Laden der Karten Layer, bitte warten...",
    "what" : "Was?",
    "when" : "Wann?",
    "where" : "Wo?",
    "yourRegistration" : "Ihre Registrierung..."
};

function translate(a) {
    return translations[a] || a
}

Ext
        .apply(
                translations,
                {
                    'sortByTypes' : [
                            [ "changeDate", "Aktualisierung Metadaten" ],
                            [ "popularity", "Popularität" ],
                            [ "relevance", "Relevanz" ], [ "title", "Title" ] ],
                    'outputTypes' : [ [ "full", "Komplett" ],
                            [ "text", "Nur Text" ] ],
                    'dataTypes' : [
                            [ '', '- Alle -' ],
                            [ "dataset", "Daten" ],
                            [ "basicgeodata", "Geobasisdaten" ],
                            [ "basicgeodata-federal", " + Geobasisdaten - Bund" ],
                            [ "basicgeodata-cantonal",
                                    " + Geobasisdaten - Kanton" ],
                            [ "basicgeodata-communal",
                                    " + Geobasisdaten - Gemeinde" ],
                            [ "basicgeodata-other", " + Geobasisdaten - Andere" ],
                            [ "service", "Dienste" ],
                            [ "service-OGC:WMS", " + WMS" ],
                            [ "service-OGC:WFS", " + WFS" ] ],
                    'hitsPerPageChoices' : [ [ "10", "10" ], [ "20", "20" ],
                            [ "50", "50" ], [ "100", "100" ], [ "10", "10" ],
                            [ "100", "100" ], [ "20", "20" ], [ "50", "50" ] ],
                    'topicCat' : [
                            [ '', '- Alle -' ],
                            [ "farming", "Landwirtschaft" ],
                            [ "biota", "Biologie" ],
                            [ "boundaries", "Grenzen" ],
                            [ "climatologyMeteorologyAtmosphere", "Atmosphäre" ],
                            [ "economy", "Wirtschaft" ],
                            [ "elevation", "Höhenangaben" ],
                            [ "environment", "Umwelt" ],
                            [ "geoscientificInformation", "Geowissenschaften" ],
                            [ "health", "Gesundheitswesen" ],
                            [ "imageryBaseMapsEarthCover",
                                    "Oberflächenbeschreibung" ],
                            [ "intelligenceMilitary", "Militär und Aufklärung" ],
                            [ "inlandWaters", "Binnengewässer" ],
                            [ "location", "Ortsangaben" ],
                            [ "oceans", "Meere" ],
                            [ "planningCadastre",
                                    "Planungsunterlagen, Kataster" ],
                            [ "society", "Gesellschaft" ],
                            [ "structure", "Bauwerke" ],
                            [ "transportation", "Verkehrswesen" ],
                            [ "utilitiesCommunication",
                                    "Ver- und Entsorgung, Kommunikation" ] ],
                    'sources_groups' : [
                            [ "_groupOwner/54", "AEW Energie AG" ],
                            [ "_groupOwner/39",
                                    "Bundesamt für Bevölkerungsschutz BABS" ],
                            [ "_groupOwner/40", "Bundesamt für Energie BFE" ],
                            [ "_groupOwner/38", "Bundesamt für Gesundheit BAG" ],
                            [ "_groupOwner/27",
                                    "Bundesamt für Kommunikation BAKOM" ],
                            [ "_groupOwner/37", "Bundesamt für Kultur BAK" ],
                            [ "_groupOwner/6",
                                    "Bundesamt für Landestopografie swisstopo" ],
                            [ "_groupOwner/25",
                                    "Bundesamt für Landwirtschaft BLW" ],
                            [ "_groupOwner/34",
                                    "Bundesamt für Meteorologie und Klimatologie MeteoSchweiz" ],
                            [ "_groupOwner/22",
                                    "Bundesamt für Raumentwicklung ARE" ],
                            [ "_groupOwner/8", "Bundesamt für Statistik BFS" ],
                            [ "_groupOwner/23", "Bundesamt für Strassen ASTRA" ],
                            [ "_groupOwner/52", "Bundesamt für Umwelt BAFU" ],
                            [ "_groupOwner/26", "Bundesamt für Verkehr BAV" ],
                            [ "_groupOwner/36",
                                    "Bundesamt für Veterinärwesen BVET" ],
                            [ "_groupOwner/41",
                                    "Bundesamt für Zivilluftfahrt BAZL" ],
                            [ "_groupOwner/61", "Caricaie" ],
                            [ "_groupOwner/51",
                                    "Eidg Dept für Verteidigung,Bevölkerungsschutz und Sport VBS" ],
                            [ "_groupOwner/57",
                                    "Eidg. Forschungsanstalt für Wald, Schnee und Landschaft WSL" ],
                            [ "_groupOwner/53",
                                    "Eidgenössisches Nuklearsicherheitsinspektorat ENSI" ],
                            [ "_groupOwner/10",
                                    "Forschungsanstalt Agroscope Reckenholz-Tänikon ART" ],
                            [ "_groupOwner/13", "Fürstentum Liechtenstein" ],
                            [ "_groupOwner/50", "Geoportal des Bundes" ],
                            [ "_groupOwner/24", "Kanton Basel-Stadt" ],
                            [ "_groupOwner/3", "Kanton Freiburg (SYSIF)" ],
                            [ "_groupOwner/7", "Kanton Neuenburg (SITN)" ],
                            [ "_groupOwner/16", "Kanton St. Gallen" ],
                            [ "_groupOwner/19", "Kanton Thurgau" ],
                            [ "_groupOwner/20", "Kanton Wallis" ],
                            [ "_groupOwner/5", "Kanton Zug" ],
                            [ "_groupOwner/4", "Kantone Obwalden und Nidwalden" ],
                            [ "_groupOwner/18", "Raumdatenpool Kanton Luzern" ],
                            [ "_groupOwner/49", "Stadt Bern" ],
                            [ "_groupOwner/56", "System monitor" ],
                            [ "_groupOwner/42", "TestGroup" ],
                            [ "_groupOwner/55", "TestPartner" ],
                            [ "_groupOwner/17", "Zentraler Katalog" ],
                            [ "_groupOwner/21", "geoProRegio" ],
                            [ "_source/c83d6356-e6d2-4611-8276-62dfba5d11e2",
                                    "ASIT-VD" ],
                            [ "_source/7c703f99-083b-40f6-be06-dc65e6495b9b",
                                    "Basel Landschaft" ],
                            [ "_source/2cbf03e5-10d4-4a5e-b398-241289a97878",
                                    "Genève (SITG)" ],
                            [ "_source/3d0e7213-74b5-4de4-8d36-b23edd56886e",
                                    "IG-GIS" ],
                            [ "_source/558db0c4-2161-401b-b63a-ff7afe1d01ba",
                                    "Kanton Schaffhausen" ],
                            [ "_source/1f4db83c-68b1-4749-899c-09c89f233d6c",
                                    "Kanton Solothurn (SO!GIS)" ],
                            [ "_source/65eb4418-359a-4251-97ce-46492f60c8d2",
                                    "Kanton Zürich" ],
                            [ "_source/c767ffd0-1e49-4a28-b6fb-1d1c4e6c5b97",
                                    "Stadt Zürich" ],
                            [ "_source/7ea582d4-9ddf-422e-b28f-29760a4c0147",
                                    "_geocat.ch direct partners" ] ],
                    'formats' : [
                            [ '', '- Alle -' ],
                            [ "TTN InRoads", "TTN InRoads (-)" ],
                            [ "BMBLT", "BMBLT (-)" ],
                            [ "BMP_Exportformat", "BMP (Exportformat)" ],
                            [ "BMP_Exportformat (Windows only)",
                                    "BMP (Exportformat (Windows only))" ],
                            [ "CIT Microstation_1", "CIT Microstation (1)" ],
                            [ "CODEAU", "CODEAU (-)" ],
                            [ "DGN Microstation_SE", "DGN Microstation (SE)" ],
                            [ "DTM InRoads", "DTM InRoads (-)" ],
                            [ "ESRI ArcInfo Coverage_8.x",
                                    "ESRI ArcInfo Coverage (8.x)" ],
                            [ "ESRI ArcInfo export format (.e00)",
                                    "ESRI ArcInfo export format (.e00) (-)" ],
                            [ "ESRI ArcInfo Generate File",
                                    "ESRI ArcInfo Generate File (-)" ],
                            [ "ESRI Personal Geodatabase_8.3",
                                    "ESRI Personal Geodatabase (8.3)" ],
                            [ "ESRI Personal Geodatabase_9.1",
                                    "ESRI Personal Geodatabase (9.1)" ],
                            [ "ESRI Personal Geodatabase",
                                    "ESRI Personal Geodatabase (-)" ],
                            [ "FileMaker pro_7", "FileMaker pro (7)" ],
                            [ "GeoTIFF", "GeoTIFF (-)" ],
                            [ "GIF - Graphics Interchange Format",
                                    "GIF - Graphics Interchange Format (-)" ],
                            [ "Google KMZ_2.1", "Google KMZ (2.1)" ],
                            [ "GWS Geomedia_5.2", "GWS Geomedia (5.2)" ],
                            [ "Illustrator / Freehand_9",
                                    "Illustrator / Freehand (9)" ],
                            [ "IMAGE (.img)", "IMAGE (.img) (-)" ],
                            [ "Image Catalog", "Image Catalog (-)" ],
                            [ "MapInfo .TAB_8", "MapInfo .TAB (8)" ],
                            [ "MapInfo MIF/MID", "MapInfo MIF/MID (-)" ],
                            [ "MDB Geomedia", "MDB Geomedia (-)" ],
                            [ "MMBL", "MMBL (-)" ],
                            [ "MMBLT", "MMBLT (-)" ],
                            [ "MS Access", "MS Access (-)" ],
                            [ "MS Excel", "MS Excel (-)" ],
                            [ "Online DB", "Online DB (-)" ],
                            [ "PNG", "PNG (-)" ],
                            [ "PostGIS Layer", "PostGIS Layer (-)" ],
                            [ "RINEX_2.11", "RINEX (2.11)" ],
                            [ "RTCM_3", "RTCM (3)" ],
                            [ "Text_ASCII", "Text (ASCII)" ],
                            [ "Text (.csv), comma separated",
                                    "Text (.csv), comma separated (-)" ],
                            [ "Text (.csv), semicolon separated",
                                    "Text (.csv), semicolon separated (-)" ],
                            [ "VRML_1", "VRML (1)" ],
                            [ "SVG-XML", "SVG-XML (-)" ],
                            [ "Oracle Spatial SDO_10g",
                                    "Oracle Spatial SDO (10g)" ],
                            [ "TIFF LZW", "TIFF LZW (-)" ],
                            [ "N/A_N/A", "N/A (N/A)" ],
                            [ "TIFF Packbits", "TIFF Packbits (-)" ],
                            [ "ESRI ArcInfo ASCII GRID",
                                    "ESRI ArcInfo ASCII GRID (-)" ],
                            [ "Kashmir3D", "Kashmir3D (-)" ],
                            [ "ESRI File Geodatabase",
                                    "ESRI File Geodatabase (-)" ],
                            [ "ESRI xmi", "ESRI xmi (-)" ],
                            [ "INTERLIS_2", "INTERLIS (2)" ],
                            [ "DXF_14", "DXF (14)" ],
                            [ "Text (.txt)", "Text (.txt) (-)" ],
                            [ "ASCII XYZ", "ASCII XYZ (-)" ],
                            [ "DXF_12", "DXF (12)" ],
                            [ "ESRI Shapefile", "ESRI Shapefile (-)" ],
                            [ "DWG", "DWG (-)" ],
                            [ "JPG", "JPG (-)" ],
                            [ "TIFF", "TIFF (-)" ],
                            [ "INTERLIS_1", "INTERLIS (1)" ],
                            [ "kein Vertrieb_Nur Erfaqssung im Mapserver",
                                    "kein Vertrieb (Nur Erfaqssung im Mapserver)" ],
                            [ "ESRI Enterprise Geodatabase",
                                    "ESRI Enterprise Geodatabase (-)" ],
                            [ "DWG_14", "DWG (14)" ],
                            [ "plano (gedruckt - imprimé - stampato - print)",
                                    "plano (gedruckt - imprimé - stampato - print) (-)" ],
                            [ "DXF", "DXF (-)" ],
                            [ "ESRI Geodatabase (.mdb)",
                                    "ESRI Geodatabase (.mdb) (-)" ],
                            [
                                    "Majorité des formats SIG raster standard (.ecw, (geo).tif, .jpg, .png, etc)_sur demande",
                                    "Majorité des formats SIG raster standard (.ecw, (geo).tif, .jpg, .png, etc) (sur demande)" ],
                            [
                                    "Majorité de formats SIG de grilles standard (ascii grid, fichiers .yxz,etc.)_sur demande",
                                    "Majorité de formats SIG de grilles standard (ascii grid, fichiers .yxz,etc.) (sur demande)" ],
                            [ "PDF", "PDF (-)" ],
                            [ "MapInfo .TAB", "MapInfo .TAB (-)" ],
                            [ "Text", "Text (-)" ],
                            [ "Format1_F1", "Format1 (F1)" ],
                            [ "CityGML_1.0", "CityGML (1.0)" ],
                            [ "ESRI Shape", "ESRI Shape (-)" ],
                            [ "abcdefg_1", "abcdefg (1)" ],
                            [ "anderes", "anderes (-)" ],
                            [ "Punktfile_xyz", "Punktfile (xyz)" ],
                            [ "Tabelle", "Tabelle (-)" ],
                            [ "ECW", "ECW (-)" ],
                            [ "DXF_2000", "DXF (2000)" ],
                            [ "DWG_2000", "DWG (2000)" ],
                            [ "CAD", "CAD (-)" ],
                            [ "vnd.wap.wbmp", "vnd.wap.wbmp (-)" ],
                            [
                                    "Majorité des formats SIG (.tab, .shp, .dxf)_sur demande",
                                    "Majorité des formats SIG (.tab, .shp, .dxf) (sur demande)" ],
                            [ "IMG raster", "IMG raster (-)" ],
                            [ "IMG Raster", "IMG Raster (-)" ] ]
                });