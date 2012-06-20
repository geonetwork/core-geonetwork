var allMaps = [];

OpenLayers.Util.onImageLoadError = function() {
    this._attempts = (this._attempts) ? (this._attempts + 1) : 1;
    if (this._attempts <= OpenLayers.IMAGE_RELOAD_ATTEMPTS) {
        //noinspection SillyAssignmentJS
        this.src = this.src;
    }
    this.style.display = "none";
};

function updateMapSizes() {
    for (var i = 0; i < allMaps.length; ++i) {
        allMaps[i].updateSize();
    }
}

function createMap(divId, navigationTools) {
    var controls = [
        new OpenLayers.Control.Navigation(),
        new OpenLayers.Control.ArgParser(),
        new OpenLayers.Control.Attribution()
    ];
    if (navigationTools) {
        controls.push(new OpenLayers.Control.PanZoom());
    }

    var extent = new OpenLayers.Bounds.fromArray([420000, 30000, 900000, 350000]);
    var map = new OpenLayers.Map($(divId), {
        controls: controls,
        projection: "EPSG:21781",
        units: "m",
        style: null,
        maxExtent: extent,
        restrictedExtent: extent,
        scales: [20000000,10000000,6500000,5000000,2500000,1000000,500000,200000,100000,50000,25000]
    });

    OpenLayers.DOTS_PER_INCH = 254;
    OpenLayers.IMAGE_RELOAD_ATTEMPTS = 1;
    var topo = new OpenLayers.Layer.TileCache("Background", [
        'http://t0.tilecache.prod-swisstopo.camptocamp.net/cache/',
        'http://t1.tilecache.prod-swisstopo.camptocamp.net/cache/',
        'http://t2.tilecache.prod-swisstopo.camptocamp.net/cache/'
    ], 'Pixelmap_color_smaller', {
        format: 'image/png',
        displayInLayerSwitcher: false,
        isBaseLayer: true,
        buffer: 0
    });
    map.addLayer(topo);

    allMaps.push(map);
    return map;
}