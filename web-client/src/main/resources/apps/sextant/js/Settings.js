GeoNetwork.Settings = {
  hideAngularEditor: true,
  hideExtEditor: false
};

// Default to absolute path without apps/search
// GeoNetwork.URL = '../..';

//OpenLayers.ProxyHostURL = '/cgi-bin/proxy.cgi?url=';
// GeoNetwork proxy is much more permissive than OL one
OpenLayers.ProxyHostURL = '../../proxy?url=';

OpenLayers.ProxyHost = function(url){
    /**
     * Do not use proxy for local domain.
     * This is required to keep the session activated.
     */
    if (url && url.indexOf(window.location.host) != -1) {
//if (url.indexOf('localhost:8080') < 0) {
        return url;
    } else {
        return OpenLayers.ProxyHostURL + encodeURIComponent(url);
    }
};


GeoNetwork.Util.defaultLocale = 'fre';
// Restrict locales to a subset of languages
//GeoNetwork.Util.locales = [
//            ['fr', 'Français']
//    ];
GeoNetwork.searchDefault = {
    activeMapControlExtent: false
};
GeoNetwork.advancedFormButton = true;

GeoNetwork.Settings.editor = {
    disableIfSubmittedForEditor: true,
    defaultViewMode : 'simple',
    // Define which edit mode to use by default
    // according to metadata schema
    editMode: {
        'iso19139.myocean': 'myocean',
        'iso19139.myocean.short': 'myocean.simple',
        'iso19139.sextant': 'sextant',
        'iso19139.sdn-product': 'sdnProduct',
        'iso19139.emodnet.chemistry': 'emodnet.chemistry',
        'iso19139.emodnet.hydrography': 'emodnet.hydrography'
    },
    editHarvested: false
//    defaultViewMode : 'inspire'
};

GeoNetwork.Settings.privileges = {
        // Customize column to be displayed and the order
        columnOrder: ['0', '5', '1', '2'],
        // Use topGroups to place those groups with internet, intranet groups
        // on top of the privileges panel.
        topGroups: ['82'] // GEOCATALOGUE group on top
};

GeoNetwork.Settings.facetListConfig = [{name: 'orgNames'},
                                       {name: 'keywords'},
//                                       {name: 'inspireThemes'},
                                       {name: 'denominators'},
                                       {name: 'createDateYears'}];
GeoNetwork.Settings.facetMaxItems = 5;

// Define if default mode should be used for HTML print output instead of tabs only
GeoNetwork.printDefaultForTabs = false;

// Define if label needs to be displayed for login form next to username/password fields
GeoNetwork.hideLoginLabels = true;


// Define which type of search to use
// Old mode (xml.search with lucene, db access and XSL formatting)
//GeoNetwork.Settings.mdStore = GeoNetwork.data.MetadataResultsStore;
// IndexOnly mode (xml.search with lucene only) - recommended
GeoNetwork.Settings.mdStore = GeoNetwork.data.MetadataResultsFastStore;

// Latest update info query
GeoNetwork.Settings.latestQuery = "from=1&to=6&sortBy=changeDate&fast=index";
GeoNetwork.Settings.latestTpl = GeoNetwork.Templates.THUMBNAIL;
GeoNetwork.Settings.results = {
        maxResultsInPDF: 200,
        // Parameters to set bounding box highlighter colors
        // Use a custom single color for bounding box
        featurecolor: 'orange',
        // Use a random color map with 2 colors 
        //colormap: GeoNetwork.Util.generateColorMap(2),
        // Use a default color map with 10 colors
        //colormap: GeoNetwork.Util.defaultColorMap,
        // Use a custom color map
        //colormap: ['red', 'green', 'blue'],
        colormap: undefined,
        // Use a custom CSS rules
        //featurecolorCSS: "border-width: 5px;border-style: solid; border-color: ${featurecolor}"
        featurecolorCSS: undefined
};

GeoNetwork.MapModule = true;
GeoNetwork.ProjectionList = [['EPSG:4326', 'WGS84 (lat/lon)']];
GeoNetwork.WMSList = [['Geoserver', 'http://localhost/geoserver/wms?']];

GeoNetwork.defaultViewMode = 'view-simple';

//Overloaded in catalogue.xhtml
Ext.BLANK_IMAGE_URL = 'images/s.gif';

GeoNetwork.AnnuaireGroupMapping = {
  "iso19139.sextant": undefined,
  "iso19139.myocean": "MYOCEAN-ALL-PRODUCTS " +
      "OR MYOCEAN-CORE-PRODUCTS " +
      "OR MYOCEAN-DOCUMENTS " +
      "OR MYOCEAN-INTERMEDIATE-PRODUCTS " +
      "OR MYOCEAN-SERVICES-AND-DATASETS " +
      "OR MYOCEAN-SPECIFIC-CORE " +
      "OR MYOCEAN-UPSTREAM-PRODUCTS",
  "iso19139.sdn-product" : "EMODNET_Chemistry " +
      "OR SEADATANET"
};
