OpenLayers.Util.onImageLoadError = function() {
    this._attempts = (this._attempts) ? (this._attempts + 1) : 1;
    if (this._attempts <= OpenLayers.IMAGE_RELOAD_ATTEMPTS) {
        //noinspection SillyAssignmentJS
        this.src = this.src;
    }
    this.style.display = "none";
};

OpenLayers.DOTS_PER_INCH = 254;
Ext.BLANK_IMAGE_URL = window.gMfLocation + '../ext/resources/images/default/s.gif';
OpenLayers.IMAGE_RELOAD_ATTEMPTS = 1;

/**
 * Class: MapComponent
 */
var MapComponent = OpenLayers.Class({

    displayLayertree: true,

    drawPanel: true,
    panelDivId: null,
    panelWidth: 500,
    panelHeight: 300,
    resizablePanel: true,
    panelMinWidth: 185,
    panelMaxWidth: 500,
    panelMinHeight: 100,
    panelMaxHeight: 410,
    enableNavigation: true,

    initialExtent: null,
    mapControls: null,
    mapLayers: null,
    resolutions: [2000, 1000, 650, 500, 250, 100, 50, 20, 10, 5, 2.5],
    //mapNavigation: false,

    map: null,
    toolbar: null,
    panel: null,


    /**
     * Constructor: MapDrawComponent
     *
     * Parameters:
     * divId - {string}
     * options - {Object} Options object
     *
     */
    initialize: function(divId, options) {
        this.panelDivId = divId;
        OpenLayers.Util.extend(this, options);

        var tcBounds = new OpenLayers.Bounds(420000, 30000, 900000, 350000);
        this.initialExtent = new OpenLayers.Bounds(473100, 67000, 891049, 301698);
        this.mapControls = [
            new OpenLayers.Control.ArgParser(),
            new OpenLayers.Control.Attribution()
        ];
        // build the attribution target
        switch(OpenLayers.Lang.code) {
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
        this.mapLayers = [
            /* disabled because it doesnt look nice with the new white border around wmts tiles */
            /*
            new OpenLayers.Layer.Image("BigBackground", "../../images/baseMap.jpg",
                new OpenLayers.Bounds(159000,-238000,1364100,569300),
                new OpenLayers.Size(1854, 1242),
                {
                    isBaseLayer: true,
                    displayInLayerSwitcher: false,
                    resolutions: this.resolutions,
                    attribution: "<a href='" + href + "' target='_blank'>&copy; swisstopo</a>"
                }
            ),
            */
            
            /* old tilecache background layer, replaced by the wmts one
            new OpenLayers.Layer.TileCache("Background", [
                'http://tile5.bgdi.admin.ch/geoadmin/', 'http://tile6.bgdi.admin.ch/geoadmin/',
                'http://tile7.bgdi.admin.ch/geoadmin/', 'http://tile8.bgdi.admin.ch/geoadmin/',
                'http://tile9.bgdi.admin.ch/geoadmin/'
            ], 'ch.swisstopo.pixelkarte-farbe', {
                format: 'image/jpeg',
                isBaseLayer: false,
                displayInLayerSwitcher: false,
                buffer: 0,
                maxExtent: tcBounds,
                serverResolutions: [4000,3750,3500,3250,3000,2750,2500,2250,2000,1750,1500,1250,1000,750,650,500,250,100,50,20,10,5,2.5,2,1.5,1,0.5],
                calculateInRange: function() {
                    if(this.map.getScale() >= 6500000) {
                        return false; //handled by BigBackground
                    }
                    return OpenLayers.Layer.TileCache.prototype.calculateInRange.apply(this, arguments);
                }
            })
            */
            new OpenLayers.Layer.WMTS({
                name: "karte",
                url: ['http://wmts0.geo.admin.ch/','http://wmts1.geo.admin.ch/','http://wmts2.geo.admin.ch/','http://wmts3.geo.admin.ch/','http://wmts4.geo.admin.ch/'],
                layer: "ch.swisstopo.pixelkarte-farbe",
                style: "default",
                matrixSet: "21781",
                format: "image/jpeg",
                isBaseLayer: true,
                visibility:true,
                requestEncoding: "REST",
                dimensions: ['TIME'],
                params: {'time': '20111027'},
                formatSuffix: 'jpeg',
                zoomOffset: 12,
                serverResolutions: [4000, 3750, 3500, 3250, 3000, 2750, 2500, 2250, 2000, 1750, 1500, 1250, 1000, 750, 650, 500, 250, 100, 50, 20, 10, 5, 2.5, 2, 1.5, 1, 0.5, 0.25]
           })
        ];

        var mapOptions = {
            controls: this.mapControls,
            projection: "EPSG:21781",
            units: "m",
            theme: null,
            maxExtent: tcBounds,
            restrictedExtent: tcBounds,
            resolutions: this.resolutions
        };
        if (!this.drawPanel && this.panelDivId) {
            mapOptions.div = this.panelDivId;
        }
        this.map = new OpenLayers.Map(mapOptions);
        this.map.addLayers(this.mapLayers);
        this.zoomToFullExtent();

        if (this.enableNavigation) {
            this.navigate = new OpenLayers.Control.Navigation({title: OpenLayers.Lang.translate('mf.control.pan')});
            this.map.addControl(this.navigate);
        }
        if (this.drawPanel) {
            this.toolbar = this.getToolbar();
            this.panel = this.getPanel();
        }

        this.map.updateSize();
    },

    // FIXME: still useful?
    getMap: function() {
        return this.map;
    },

    zoomToFullExtent: function() {
        this.map.zoomToExtent(this.initialExtent, true);
    },

    getToolbar: function() {
        if (this.toolbar) return this.toolbar;

        var items = [
            new GeoExt.Action({
                control: new OpenLayers.Control.ZoomToMaxExtent(),
                map: this.map,
                tooltip: OpenLayers.i18n('mf.control.zoomAll'),
                iconCls: 'zoomfull',
                toggleGroup: 'map'
            }),
            new GeoExt.Action({
                control: new OpenLayers.Control.ZoomBox(),
                map: this.map,
                tooltip: OpenLayers.i18n('mf.control.zoomIn'),
                iconCls: 'zoomin',
                toggleGroup: 'map'
            }),
            new GeoExt.Action({
                control: new OpenLayers.Control.ZoomBox({out: true}),
                map: this.map,
                tooltip: OpenLayers.i18n('mf.control.zoomOut'),
                iconCls: 'zoomout',
                toggleGroup: 'map'
            }),
            '-'
        ];

        if (this.navigate) {
            items.push(new GeoExt.Action({
                control: this.navigate,
                iconCls: 'pan',
                toggleGroup: 'map'
            }));
        }

        return new Ext.Toolbar({
            items: items,
            autoHeight: false,
            height: 26
        });
    },

    getPanel: function() {

/*
        var panelItems = [new GeoExt.MapPanel({
            region: 'center',
            layout: 'fit',
            map: this.map,
            tbar: this.getToolbar()
        })];
        if (this.displayLayertree) {
            panelItems.push({
                region: 'east',
                title: 'Layers',
                xtype: 'layertree',
                id: this.panelDivId+'LayerTree',
                map: this.map,
                enableDD: true,
                ascending: false,
                width: 150,
                minSize: 100,
                split: true,
                collapsible: true,
                collapsed: false,
                plugins: [
                    mapfish.widgets.LayerTree.createContextualMenuPlugin(['opacitySlide','remove'])
                ]
            });
        }

        this.panel = new Ext.Panel({
            renderTo: this.panelDivId,
            layout: 'border',
            width: this.panelWidth-5,
            height: this.panelHeight-5,
            border: true,
            items: panelItems
        });
*/
        this.panel = new GeoExt.MapPanel({
            renderTo: this.panelDivId,
            map: this.map,
            tbar: this.getToolbar(),
            width: this.panelWidth-5,
            height: this.panelHeight-5 - (Ext.isGecko ? 26 : 0), // FIXME: ugly hack
            border: true
        });

        if (this.resizablePanel) {
            var mapResizer = new Ext.Resizable(this.panelDivId, {
                pinned: true,
                minWidth: this.panelMinWidth,
                maxWidth: this.panelMaxWidth,
                minHeight: this.panelMinHeight,
                maxHeight: this.panelMaxHeight,
                width: this.panelWidth,
                height: this.panelHeight
            });
            mapResizer.on('resize', function(resizable, width, height) {
                this.panel.setSize(width-5, height-5);
                this.panel.doLayout();
                this.updateMapSizes();
            }, this);
        }

        return this.panel;
    },

    updateMapSizes: function() {
        this.map.updateSize();
    }
});
