OpenLayers.ProxyHostURL = '../../../../proxy?url=';

OpenLayers.ProxyHost = function(url){
    if (url && url.indexOf(window.location.host) != -1) {
        return url;
    } else {
        return OpenLayers.ProxyHostURL + encodeURIComponent(url);
    }
};

Ext.onReady(function() {
    var urlParameters = GeoNetwork.Util.getParameters(location.href);

	var map = new OpenLayers.Map({
		div : "map",
		controls : [ new OpenLayers.Control.TouchNavigation({
			dragPanOptions : {
				enableKinetic : true
			}
		}), new OpenLayers.Control.Zoom() ],
		layers : [ new OpenLayers.Layer.OSM("OpenStreetMap", null, {
			transitionEffect : "resize"
		}) ],
		center : new OpenLayers.LonLat(0, 0),
		zoom : 1
	});

	new GeoNetwork.WxSExtractor({
		renderTo : 'extractor',
		url: urlParameters.url || 'http://services.sandre.eaufrance.fr/geo/ouvrage',
		version: urlParameters.version,
		layer: 'repom',
		map: map
	});
});
