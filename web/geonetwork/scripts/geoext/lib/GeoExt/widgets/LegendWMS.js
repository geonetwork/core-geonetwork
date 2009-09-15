/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/**
 * @include GeoExt/widgets/LegendImage.js
 */

/** api: (define)
 *  module = GeoExt
 *  class = LegendWMS
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
Ext.namespace('GeoExt');

/** api: constructor
 *  .. class:: LegendWMS(config)
 *
 *  Show a legend image for a WMS layer.
 */
GeoExt.LegendWMS = Ext.extend(Ext.Panel, {

    /** api: config[imageFormat]
     *  ``String``  
     *  The image format to request the legend image in if the url cannot be
     *  determined from the styles field of the layer record. Defaults to
     *  image/gif.
     */
    imageFormat: "image/gif",
    
    /** api: config[defaultStyleIsFirst]
     *  ``String``
     *  The WMS spec does not say if the first style advertised for a layer in
     *  a Capabilities document is the default style that the layer is
     *  rendered with. We make this assumption by default. To be strictly WMS
     *  compliant, set this to false, but make sure to configure a STYLES
     *  param with your WMS layers, otherwise LegendURLs advertised in the
     *  GetCapabilities document cannot be used.
     */
    defaultStyleIsFirst: true,

    /** api: config[layer]
     *  ``OpenLayers.Layer.WMS``
     *  The WMS layer to request the legend for. Not required if record is
     *  provided.
     */
    layer: null,
    
    /** api: config[record]
     *  ``Ext.data.Record``
     *  optional record containing the layer 
     */
    record: null,

    /** api: config[bodyBorder]
     *  ``Boolean``
     *  Show a border around the legend image or not. Default is false.
     */
    bodyBorder: false,

    /** private: method[initComponent]
     *  Initializes the WMS legend. For group layers it will create multiple
     *  image box components.
     */
    initComponent: function() {
        GeoExt.LegendWMS.superclass.initComponent.call(this);
        if(!this.layer) {
            this.layer = this.record.get("layer");
        }
        this.createLegend();
    },

    /** private: method[getLegendUrl]
     *  :param layer: ``OpenLayers.Layer.WMS`` The OpenLayers WMS layer object
     *  :param layerName: ``String`` The name of the layer 
     *      (used in the LAYERS parameter)
     *  :return: ``String`` The url of the SLD WMS GetLegendGraphic request.
     *
     *  Get the url for the SLD WMS GetLegendGraphic request.
     */
    getLegendUrl: function(layerName) {
        return this.layer.getFullRequestString({
            REQUEST: "GetLegendGraphic",
            WIDTH: null,
            HEIGHT: null,
            EXCEPTIONS: "application/vnd.ogc.se_xml",
            LAYER: layerName,
            LAYERS: null,
            SRS: null,
            FORMAT: this.imageFormat
        });
    },

    /** private: method[createLegend]
     *  Add one BoxComponent per sublayer to this panel.
     */
    createLegend: function() {
        var layers = (this.layer.params.LAYERS instanceof Array) ? 
            this.layer.params.LAYERS : this.layer.params.LAYERS.split(",");
        var styleNames = this.layer.params.STYLES &&
            this.layer.params.STYLES.split(",");
        var styles = this.record && this.record.get("styles");
        var url, layerName, styleName;
        for (var i = 0, len = layers.length; i < len; i++){
            layerName = layers[i];
            if(styles) {
                styleName = styleNames && styleNames[i];
                if(styleName) {
                    Ext.each(styles, function(s) {
                        url = (s.name == styleName && s.legend) && s.legend.href;
                        return !url;
                    })
                } else if(this.defaultStyleIsFirst === true){
                    url = styles[0].legend && styles[0].legend.href;
                }
            }
            var legend = new GeoExt.LegendImage({url:
                url || this.getLegendUrl(layerName)});
            this.add(legend);
        }
    }

});
