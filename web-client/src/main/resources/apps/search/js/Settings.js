GeoNetwork.Settings = {};

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
        return url;
    } else {
        return OpenLayers.ProxyHostURL + encodeURIComponent(url);
    }
};


GeoNetwork.Util.defaultLocale = 'eng';
// Restrict locales to a subset of languages
//GeoNetwork.Util.locales = [
//            ['fr', 'Français']
//    ];
GeoNetwork.searchDefault = {
    activeMapControlExtent: false
};
GeoNetwork.advancedFormButton = true;

GeoNetwork.Settings.editor = {
    defaultViewMode : 'simple',
    editHarvested: false
//    defaultViewMode : 'inspire'
};

// Define if default mode should be used for HTML print output instead of tabs only
GeoNetwork.printDefaultForTabs = false;

// Define if label needs to be displayed for login form next to username/password fields
GeoNetwork.hideLoginLabels = true;


// Define which type of search to use
// Old mode (xml.search with lucene, db access and XSL formatting)
//GeoNetwork.Settings.mdStore = GeoNetwork.data.MetadataResultsStore;
// IndexOnly mode (xml.search with lucene only) - recommended
GeoNetwork.Settings.mdStore = GeoNetwork.data.MetadataResultsFastStore;

// List of facet to display. If none, the server configuration is use.
GeoNetwork.Settings.facetListConfig = [{name: 'orgNames'}, 
                                       {name: 'types'},  
                                       {name: 'serviceTypes'}, 
                                       {name: 'denominators'}, 
                                       {name: 'keywords'}, 
                                       {name: 'createDateYears'}];
GeoNetwork.Settings.facetMaxItems = 7;

// Latest update info query
GeoNetwork.Settings.latestQuery = "from=1&to=6&sortBy=changeDate&fast=index";
GeoNetwork.Settings.latestTpl = GeoNetwork.Templates.THUMBNAIL;
GeoNetwork.Settings.results = {
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
        featurecolorCSS: undefined,
        // Look for relation for all records (true) or only series (default).
        // Only for series is recommended to not trigger to much queries when
        // displaying search results. It may be relevant to search for all
        // if record related to a dataset using largerWorkCitation is used.
        //loadRelationForAll: true
        loadRelationForAll: undefined
};
GeoNetwork.MapModule = true;
GeoNetwork.ProjectionList = [['EPSG:4326', 'WGS84 (lat/lon)']];
GeoNetwork.WMSList = [['Geoserver', 'http://localhost/geoserver/wms?']];

GeoNetwork.defaultViewMode = 'view-simple';

Ext.BLANK_IMAGE_URL = '../js/ext/resources/images/default/s.gif';
