OpenLayers.DOTS_PER_INCH = 90.71;
OpenLayers.ImgPath = '../../apps/js/OpenLayers/img/';

OpenLayers.IMAGE_RELOAD_ATTEMPTS = 3;

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
// Proj4js.defs["EPSG:28992"] = "+proj=sterea +lat_0=52.15616055555555
// +lon_0=5.38763888888889 +k=0.9999079 +x_0=155000 +y_0=463000 +ellps=bessel
// +units=m +no_defs";
Proj4js.defs["EPSG:2154"] = "+proj=lcc +lat_1=49 +lat_2=44 +lat_0=46.5 +lon_0=3 +x_0=700000 +y_0=6600000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs";

GeoNetwork.map.printCapabilities = "../../pdf";

GeoNetwork.map.PROJECTION = "EPSG:900913";
GeoNetwork.map.EXTENT = new OpenLayers.Bounds(-20037508, -20037508,
        20037508, 20037508.34);
GeoNetwork.map.MAXEXTENT = new OpenLayers.Bounds(-20037508, -20037508,
        20037508, 20037508.34);
GeoNetwork.map.BACKGROUND_LAYERS = [
        new OpenLayers.Layer.OSM(),
        new OpenLayers.Layer.OSM("MapQuest-OSM Tiles", [
                "http://otile1.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.jpg",
                "http://otile2.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.jpg",
                "http://otile3.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.jpg",
                "http://otile4.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.jpg" ]),
        new OpenLayers.Layer.XYZ(
                "Imagery",
                [
                        "http://otile1.mqcdn.com/tiles/1.0.0/sat/${z}/${x}/${y}.png",
                        "http://otile2.mqcdn.com/tiles/1.0.0/sat/${z}/${x}/${y}.png",
                        "http://otile3.mqcdn.com/tiles/1.0.0/sat/${z}/${x}/${y}.png",
                        "http://otile4.mqcdn.com/tiles/1.0.0/sat/${z}/${x}/${y}.png" ],
                {
                    attribution : "Tiles Courtesy of <a href='http://open.mapquest.co.uk/' target='_blank'>MapQuest</a>. Portions Courtesy NASA/JPL-Caltech and U.S. Depart. of Agriculture, Farm Service Agency. <img src='http://developer.mapquest.com/content/osm/mq_logo.png' border='0'>",
                    transitionEffect : "resize"
                }) ];
GeoNetwork.map.RESOLUTIONS = [];

GeoNetwork.map.CONTEXT = "../../maps/geoserver_localhost.wmc";

GeoNetwork.map.MAP_OPTIONS = {
    projection : GeoNetwork.map.PROJECTION,
    maxExtent : GeoNetwork.map.MAXEXTENT,
    restrictedExtent : GeoNetwork.map.EXTENT,
    // resolutions : GeoNetwork.map.RESOLUTIONS,
    controls : [ ], //new OpenLayers.Control.Navigation() ],
    controls_ : [ ], //OpenLayers.Control.Navigation ]
};
GeoNetwork.map.MAIN_MAP_OPTIONS = {
    projection : GeoNetwork.map.PROJECTION,
    maxExtent : GeoNetwork.map.MAXEXTENT,
    restrictedExtent : GeoNetwork.map.EXTENT,
    // resolutions : GeoNetwork.map.RESOLUTIONS,
    controls : [ ], //new OpenLayers.Control.Navigation() ],
    controls_ : [ ] //OpenLayers.Control.Navigation ]
};
