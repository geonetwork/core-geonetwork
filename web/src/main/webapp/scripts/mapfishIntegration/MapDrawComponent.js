/**
 * Class: MapDrawComponent
 */
var MapDrawComponent = OpenLayers.Class({

    map: null, // mandatory
    toolbar: null,
    toolbarControlToggleGroup: 'map',
    toolbarControlClearIconCls: 'clearPolygon',
    controlOptions: null,

    vectorLayer: null,
    vectorLayerStyle: OpenLayers.Feature.Vector.style['default'],

    controls: null,
    activate: false, // activates draw component?

    /**
     * APIMethod: onClearFeatures
     * Called when the features are cleared.
     */
    onClearFeatures: function() {},

    /**
     * Constructor: MapDrawComponent
     *
     * Parameters:
     * map - {<OpenLayers.Map>} OpenLayers Map
     * options - {Object} Options object
     *
     */
    initialize: function(map, options) {
        this.map = map;
        OpenLayers.Util.extend(this, options);

        if (!this.map) OpenLayers.Console.error('An OpenLayers.Map should be passed in options');

        this.vectorLayer = this.vectorLayer || new OpenLayers.Layer.Vector(
            "ShapeFilter", {
                //displayOutsideMaxExtent: true,
                //alwaysInRange: true,
                //displayInLayerSwitcher: false,
                style: this.vectorLayerStyle
        });
        this.map.addLayer(this.vectorLayer);

        var _self = this;
        if( !options.hideDrawControls ) {
            this.controls = [];
            if (!options.hideDrawPolygon) {
                this.controls.push(
                    new OpenLayers.Control.DrawFeature(
                        this.vectorLayer,
                        OpenLayers.Handler.Polygon, {
                            toolbarControlDrawIconCls: 'drawPolygon',
                            featureAdded: function(feature){
                                if( options.controlOptions)
                                    options.controlOptions.featureAdded.call(options.controlOptions.scope, feature);
                            },
                            handlerOptions: {
                                createFeature: function(pixel) {
                                    _self.vectorLayer.destroyFeatures();
                                    return OpenLayers.Handler.Polygon.prototype.createFeature.apply(this, arguments);
                                }
                            }
                        }
                    )
                );
            }
            this.controls.push(
                // FIXME: destroyFeatures() should be called on startBox
                new OpenLayers.Control.DrawFeature(
                    this.vectorLayer,
                    OpenLayers.Handler.Box, {
                        toolbarControlDrawIconCls: 'drawRectangle',
                        drawFeature: function(bounds) {
                            if (!bounds.toGeometry) return;
                            var g1 = this.map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.left, bounds.bottom));
                            var g2 = this.map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.right, bounds.top));
                            bounds.left = g1.lon;
                            bounds.bottom = g1.lat;
                            bounds.right = g2.lon;
                            bounds.top = g2.lat;
                            var feature = new OpenLayers.Feature.Vector(bounds.toGeometry());
                            this.vectorLayer.addFeatures([feature]);
                            if( options.controlOptions)
                                options.controlOptions.featureAdded.call(options.controlOptions.scope, feature);
                        }.bind(this),
                        handlerOptions: {
                            startBox: function(xy) {
                                // Why is this not called?
                                _self.vectorLayer.destroyFeatures();
                                return OpenLayers.Handler.Box.prototype.startBox.apply(this, arguments);
                            }
                        }
                    }
                )
            );
        }

        if (this.toolbar && !options.hideDrawControls) {
            this.toolbar.add(new Ext.Toolbar.Separator());
            for (var i=0; i < this.controls.length; i++) {
                this.toolbar.add(new GeoExt.Action({
                    control: this.controls[i],
                    map: this.map,
                    iconCls: this.controls[i].toolbarControlDrawIconCls,
                    toggleGroup: this.toolbarControlToggleGroup
                }));
            }
            this.toolbar.add({
                iconCls: this.toolbarControlClearIconCls,
                //toggleGroup: this.toolbarControlToggleGroup,
                handler: function(button) { this.clearFeatures(); },
                scope: this
            });
        }

        if ((this.activate || !this.toolbar) && this.controls && this.controls.length) this.controls[0].activate();

    },

    clearFeatures: function() {
        this.destroyFeatures();
        this.onClearFeatures();
    },

    destroyFeatures: function() {
        this.vectorLayer.destroyFeatures();
    },

    /**
     * Returns a string representing the feature geometry
     *
     * @param options parameters describing how to format the geoemtry
     *        options.from the current projection of the feature (either both 'from' and 'to' are defined or neither)
     *        options.to   the projection to reproject the feature
     *        options.format the format to write the geometry in 'WKT', 'GML', etc...  Default is WKT
     */
    writeFeature: function(options) {
        var format = 'WKT';
        var from,to;
    	if( options!=null ){
            format = options.format || 'WKT';
            from = options.from;
            to = options.to;
    	}

        var writer = new OpenLayers.Format[format]();
        if( from != null && to != null ){
        	writer.internalProjection = from;
        	writer.externalProjection = to;
        }
        if (!this.vectorLayer.features.length) return null;
        // Gets the last drawn feature
        var feature = this.vectorLayer.features[this.vectorLayer.features.length-1];
        return writer.write(feature);
    },

    /**
     * @param string string containing the feature data
     * @param option parameters describing how to format the geoemtry
     *        option.from the current projection of the feature (either both 'from' and 'to' are defined or neither)
     *        option.to   the projection to reproject the feature
     *        option.format the format to write the geometry in 'WKT', 'GML', etc...  Default is WKT
     *        option.zoomToFeatures boolean, if true, zooms to feature(s) extent
     */
    readFeature: function(string, options) {
        var format = 'WKT';

        var from,to;
        if( options!=null){
            format = options.format || 'WKT';
            from = options.from;
            to = options.to;
        }
        var reader = new OpenLayers.Format[format]();
        if( from != null && to != null ){
            reader.externalProjection = from;
            reader.internalProjection = to;
        }

        // linereturn in wkt break the reader so we remove them
        var feature = reader.read(string.replace(/(\r\n|\n|\r)/gm,""));
        // reader is subject to returning an object or an array depending on the format
        if (!feature) return false;
        if (feature.length) feature = feature[0];
        this.vectorLayer.addFeatures(feature);
        // optionally, zoom on the layer features extent
        if (options.zoomToFeatures) {
            this.zoomToFeatures();
        }
        return true;
    },

    updateBbox: function(e, s, w, n) {
        var bounds = new OpenLayers.Bounds(e, s, w, n);
        var feature = new OpenLayers.Feature.Vector(bounds.toGeometry());
        this.destroyFeatures();
        this.vectorLayer.addFeatures(feature);
        if (this.zoomToFeatures) {
            this.zoomToFeatures();
        }
    },

    zoomToFeatures: function() {
        var extent = this.vectorLayer.getDataExtent();
        if (extent) {
            var width = extent.getWidth()/2;
            var height = extent.getHeight()/2;
            extent.left -= width;
            extent.right += width;
            extent.bottom -= height;
            extent.top += height;
            this.map.zoomToExtent(extent);
        } else {
            this.map.zoomToMaxExtent();
        }
    }
});
