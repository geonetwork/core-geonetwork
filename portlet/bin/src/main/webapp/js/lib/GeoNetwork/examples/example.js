GeoNetwork.URL = 'geonetwork';
GeoNetwork.map.PROJECTION = "EPSG:4326";
GeoNetwork.map.EXTENT = new OpenLayers.Bounds(-180,-90,180,90);
GeoNetwork.map.BACKGROUND_LAYERS = [
    new OpenLayers.Layer.WMS("Background layer", "/geoserver/wms", {layers: 'gn:world,gn:gboundaries', format: 'image/jpeg'}, {isBaseLayer: true})
    ];
GeoNetwork.map.MAP_OPTIONS = {
    projection: GeoNetwork.map.PROJECTION,
    maxExtent: GeoNetwork.map.EXTENT,
    restrictedExtent: GeoNetwork.map.EXTENT
};
