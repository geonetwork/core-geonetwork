OpenLayers.DOTS_PER_INCH = 90.71;
// OpenLayers.ImgPath = '../js/OpenLayers/theme/default/img/';
OpenLayers.ImgPath = '../../apps/js/OpenLayers/img/';

OpenLayers.IMAGE_RELOAD_ATTEMPTS = 3;

// Define a constant with the base url to the MapFish web service.
// mapfish.SERVER_BASE_URL = '../../../../../'; // '../../';

// Remove pink background when a tile fails to load
OpenLayers.Util.onImageLoadErrorColor = "transparent";

// Lang
OpenLayers.Lang.setCode(GeoNetwork.defaultLocale);

OpenLayers.Util.onImageLoadError = function() {
    this._attempts = (this._attempts) ? (this._attempts + 1) : 1;
    if (this._attempts <= OpenLayers.IMAGE_RELOAD_ATTEMPTS) {
        this.src = this.src;
    } else {
        this.style.backgroundColor = OpenLayers.Util.onImageLoadErrorColor;
        this.style.display = "none";
    }
};

// add Proj4js.defs here
// Proj4js.defs["EPSG:27572"] = "+proj=lcc +lat_1=46.8 +lat_0=46.8 +lon_0=0
// +k_0=0.99987742 +x_0=600000 +y_0=2200000 +a=6378249.2 +b=6356515
// +towgs84=-168,-60,320,0,0,0,0 +pm=paris +units=m +no_defs";
Proj4js.defs["EPSG:21781"] = "+title=CH1903 / LV03 +proj=somerc +lat_0=46.95240555555556 +lon_0=7.439583333333333 +x_0=600000 +y_0=200000 +ellps=bessel +towgs84=674.374,15.056,405.346,0,0,0,0 +units=m +no_defs";
Proj4js.defs["EPSG:28992"] = "+proj=sterea +lat_0=52.15616055555555 +lon_0=5.38763888888889 +k=0.9999079 +x_0=155000 +y_0=463000 +ellps=bessel +units=m +no_defs";
Proj4js.defs["EPSG:2154"] = "+proj=lcc +lat_1=49 +lat_2=44 +lat_0=46.5 +lon_0=3 +x_0=700000 +y_0=6600000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs";
// new OpenLayers.Projection("EPSG:900913")

GeoNetwork.map.printCapabilities = "../../pdf";

// Config for WGS84 based maps
// GeoNetwork.map.PROJECTION = "EPSG:4326";
// GeoNetwork.map.EXTENT = new OpenLayers.Bounds(-180,-90,180,90);
// GeoNetwork.map.EXTENT = new OpenLayers.Bounds(-5.1,41,9.7,51);

// NGR Projection and layer definition
GeoNetwork.map.PROJECTION = "EPSG:21781";
GeoNetwork.map.EXTENT = new OpenLayers.Bounds(420000, 30000, 900000, 350000);
GeoNetwork.map.MAXEXTENT = new OpenLayers.Bounds(420000, 30000, 900000, 350000);
GeoNetwork.map.RESOLUTIONS = [ 2000, 1000, 650, 500, 250, 100, 50, 20, 10, 5,
        2.5 ];

// build the attribution target
switch (OpenLayers.Lang.code) {
case "fr":
    var href = "http://www.disclaimer.admin.ch/conditions_dutilisation.html";
    break;
case "en":
    var href = "http://www.disclaimer.admin.ch/terms_and_conditions.html";
    break;
case "de":
default:
    var href = "http://www.disclaimer.admin.ch/index.html";
    break;
}

var geocat = {
    defaultStyleColor : "#FFFFFF",
    defaultStyle : {
        fillColor : "#FFFF00",
        fillOpacity : 0.1,
        strokeColor : "#FFFF00",
        strokeOpacity : 1,
        strokeWidth : 2,
        pointRadius : 4,
        pointerEvents : "visiblePainted"
    },
    highlightStyleColor : "#dfe8f6",
    highlightStyle : {
        fillColor : this.highlightStyleColor,
        fillOpacity : 0.4,
        strokeColor : this.highlightStyleColor,
        strokeOpacity : 1,
        strokeWidth : 2,
        pointRadius : 4,
        pointerEvents : "visiblePainted"
    },
    selectionStyle : {
        fillColor : "#FFFF00",
        fillOpacity : 0,
        strokeColor : "#FFFF00",
        strokeOpacity : 1,
        strokeWidth : 3
    },
    queryStyle : {
        fillColor : "#0000FF",
        fillOpacity : 0.2,
        strokeColor : "#0000FF",
        strokeOpacity : 1,
        strokeWidth : 1,
        pointRadius : 4,
        pointerEvents : "visiblePainted"
    },
    openMetadataWindow: function(uuid) {
        catalogue.metadataShow(uuid);
    }
}

geocat.selectionHighlightLayer = new OpenLayers.Layer.WMS("Highlight",
        geocat.geoserverUrl + "wms", {
            styles : "Selection",
            layers : [ "chtopo:kantoneBB" ],
            format : "image/png",
            transparent : true
        }, {
            singleTile : true,
            ratio : 1,
            isBaseLayer : false,
            displayInLayerSwitcher : false,
            visibility : false,
            opacity : 1
        });

geocat.vectorLayer = new OpenLayers.Layer.Vector("Search", {
    displayOutsideMaxExtent : true,
    alwaysInRange : true,
    displayInLayerSwitcher : false,
    styleMap : new OpenLayers.StyleMap({
        "default" : geocat.defaultStyle,
        temporary : geocat.selectionStyle,
        highlight : geocat.highlightStyle,
        query : geocat.queryStyle,
        selection : geocat.selectionStyle
    })
});

geocat.drawFeature = new OpenLayers.Control.DrawFeature(
        geocat.vectorLayer,
        OpenLayers.Handler.Polygon,
        {
            drawFeature : function(b) {
                geocat.drawFeature.deactivate();
                if (geocat.selectionFeature) {
                    geocat.vectorLayer.destroyFeatures(geocat.selectionFeature)
                }
                geocat.selectionFeature = new OpenLayers.Feature.Vector(b, {},
                        geocat.selectionStyle);
                geocat.vectorLayer.addFeatures(geocat.selectionFeature);
                var a = Ext.get("drawPolygonSpan");
                Ext.DomHelper.overwrite(a,
                        '<span id="drawPolygonSpan"><a href="javascript:geocat.drawWherePolygon()">'
                                + translate("startNewPolygon") + "</a></span>");
                geocat.fixLayout()
            }
        });

geocat.wmts = new OpenLayers.Layer.WMTS({
    name : "karte",
    url : [ 'http://wmts0.geo.admin.ch/', 'http://wmts1.geo.admin.ch/',
            'http://wmts2.geo.admin.ch/', 'http://wmts3.geo.admin.ch/',
            'http://wmts4.geo.admin.ch/' ],
    layer : "ch.swisstopo.pixelkarte-farbe",
    style : "default",
    matrixSet : "21781",
    format : "image/jpeg",
    isBaseLayer : true,
    visibility : true,
    requestEncoding : "REST",
    dimensions : [ 'TIME' ],
    params : {
        'time' : '20120809'
    },
    formatSuffix : 'jpeg',
    zoomOffset : 12,
    serverResolutions : [ 4000, 3750, 3500, 3250, 3000, 2750, 2500, 2250, 2000,
            1750, 1500, 1250, 1000, 750, 650, 500, 250, 100, 50, 20, 10, 5,
            2.5, 2, 1.5, 1, 0.5, 0.25 ]
});

GeoNetwork.map.BACKGROUND_LAYERS = [ geocat.selectionHighlightLayer,
        geocat.wmts, geocat.vectorLayer ];

// // Config for OSM based maps
// GeoNetwork.map.PROJECTION = "EPSG:900913";
// //GeoNetwork.map.EXTENT = new OpenLayers.Bounds(-550000, 5000000, 1200000,
// 7000000);
// GeoNetwork.map.EXTENT = new OpenLayers.Bounds(-20037508, -20037508, 20037508,
// 20037508.34);
// GeoNetwork.map.BACKGROUND_LAYERS = [
// new OpenLayers.Layer.OSM()
// //new OpenLayers.Layer.Google("Google Streets");
// ];

// GeoNetwork.map.RESOLUTIONS = [];

GeoNetwork.map.CONTROLS = [ geocat.drawFeature,
        new OpenLayers.Control.ArgParser(),
        new OpenLayers.Control.Attribution(),
        new OpenLayers.Control.Navigation({
            title : OpenLayers.Lang.translate('mf.control.pan')
        }) ];

GeoNetwork.map.MAP_OPTIONS = {
    projection : GeoNetwork.map.PROJECTION,
    units : "m",
    maxExtent : GeoNetwork.map.MAXEXTENT,
    restrictedExtent : GeoNetwork.map.EXTENT,
    resolutions : GeoNetwork.map.RESOLUTIONS,
    controls : GeoNetwork.map.CONTROLS
};
GeoNetwork.map.MAIN_MAP_OPTIONS = {
    projection : GeoNetwork.map.PROJECTION,
    units : "m",
    maxExtent : GeoNetwork.map.MAXEXTENT,
    restrictedExtent : GeoNetwork.map.EXTENT,
    resolutions : GeoNetwork.map.RESOLUTIONS,
    controls : GeoNetwork.map.CONTROLS
};

// NGR custom geocoder
GeoNetwork.GEOCODER_URL = 'http://geodata.nationaalgeoregister.nl/geocoder/Geocoder?';
// GeoNetwork.GEOCODER_URL = 'http://geoserver.nl/geocoder/NLaddress.aspx';
