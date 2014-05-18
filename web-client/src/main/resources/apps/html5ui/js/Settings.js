GeoNetwork.Settings = {};

// Default to absolute path without apps/search
//GeoNetwork.URL = '../';

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


//List of facet to display. If none, the server configuration is use.
GeoNetwork.Settings.facetListConfig = [{name: 'orgNames'}, 
                                    {name: 'keywords'}, 
                                    {name: 'licenses'}, 
                                    {name: 'types'},  
                                    {name: 'categories'},  
                                    {name: 'serviceTypes'}, 
                                    {name: 'denominators'}, 
                                    {name: 'createDateYears'}];
GeoNetwork.Settings.facetMaxItems = 7;

GeoNetwork.MapModule = true;
GeoNetwork.ProjectionList = [['EPSG:4326', 'WGS84 (lat/lon)']];
GeoNetwork.WMSList = [];

GeoNetwork.defaultViewMode = 'view-simple';

Ext.BLANK_IMAGE_URL = '../../apps/js/ext/resources/images/default/s.gif';

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
        featurecolorCSS: undefined
};


/** Provide a mapping between various GeoNetwork protocol and mime types and 
 *  the CSS icon class. Return a generic class if we don't have a mapping.
 */
GeoNetwork.Settings.protocolToCSS = function(type, useDownload) {
      var typesAndProtocols = {
        'application/vnd.ogc.wms_xml': 'fabutton fa fa-plus-circle md-mn-wms',
        'application/vnd.ogc.wmc': 'fabutton fa fa-plus-circle md-mn-wms',
        'OGC:WMS': 'fabutton fa fa-plus-circle md-mn-wms',
        'OGC:WMS-1.1.1-http-get-map': 'fabutton fa fa-plus-circle md-mn-wms',
        'OGC:WMS-1.3.0-http-get-map': 'fabutton fa fa-plus-circle md-mn-wms',
        'ACCESS MAP VIEWER': 'fabutton fa fa-plus-circle md-mn-wms',
        'application/vnd.google-earth.kml+xml': 'fabutton fa fa-globe md-mn-kml',
        'WWW:DOWNLOAD-1.0-http--download': 'fabutton fa fa-arrow-circle-down  md-mn-download',
        'text/html': 'fabutton fa fa-arrow-circle-right  md-mn-www',
        'text/plain': 'fabutton fa fa-arrow-circle-right  md-mn-www',
        'bookmark': 'fabutton fa fa-star  md-mn-bookmark'
      };

      var defaultCSS = 'fabutton fa fa-arrow-circle-right md-mn-www';
      if (useDownload) defaultCSS = 'fabutton fa fa-arrow-circle-down md-mn-download';
      return typesAndProtocols[type] || defaultCSS;
};

/** Provide CSS classes for iconCls on view panel buttons: print, feedback, 
 * tooltip, share, permalink
 */
GeoNetwork.Settings.viewPanelButtonCSS = function(buttonId) {
      var buttonsAndClasses = {
        'viewpanel-print':     'viewpanel-button fabutton fa fa-print md-vw-print',
        'viewpanel-feedback':  'viewpanel-button fabutton fa fa-comment md-vw-comment',
        'viewpanel-tooltip':   'viewpanel-button fabutton fa fa-flag md-vw-flag',
        'viewpanel-share':     'viewpanel-button fabutton fa fa-share md-vw-share',
        'viewpanel-permalink': 'viewpanel-button fabutton fa fa-link md-vw-link'
		  };
			return buttonsAndClasses[buttonId];
}

/** Provide CSS classes for icons in view panel when displaying related  
 * records eg. parent, sibling, children, fcat
 */
GeoNetwork.Settings.relationToCSS = function(type, subType) {
      /* 'servicess|children|related|parent|dataset|fcats|sibling|associated' */
      var typesAndClasses = {
        'parent':    'fabutton-1x fa fa-long-arrow-up',
        'sibling':   'fabutton-1x fa fa-arrows-h',
        'children':  'fabutton-1x fa fa-long-arrow-down',
				'services':   'fabutton-1x fa fa-cog',
				'fcats':     'fabutton-1x fa fa-table' 
		  };
			/* If the type isn't listed then the relationship won't be displayed */
			//console.log('Mapped type: '+type+' (subtype: '+subType+') to '+typesAndClasses[type]);
			return typesAndClasses[type];
}

