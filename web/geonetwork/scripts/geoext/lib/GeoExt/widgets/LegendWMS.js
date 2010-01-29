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
 *  Show a legend image for a WMS layer. The image can be read from the styles
 *  field of a layer record (if the record comes e.g. from a
 *  :class:`GeoExt.data.WMSCapabilitiesReader`). If not provided, a
 *  GetLegendGraphic request will be issued to retrieve the image.
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
     *  Optional record containing the layer. If provided, and if the record
     *  has a styles property, the legend image associated with the layer's
     *  style will be used.
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
        this.updateLegend();
    },

    /** private: method[getLegendUrl]
     *  :param layerName: ``String`` A sublayer.
     *  :param layerNames: ``Array(String)`` The array of sublayers,
     *      read from this.layer if not provided.
     *  :return: ``String`` The legend URL.
     *
     *  Get the legend URL of a sublayer.
     */
    getLegendUrl: function(layerName, layerNames) {
        var url;
        var styles = this.record && this.record.get("styles");
        layerNames = layerNames ||
                             (this.layer.params.LAYERS instanceof Array) ?
                             this.layer.params.LAYERS :
                             this.layer.params.LAYERS.split(",");

        var styleNames = this.layer.params.STYLES &&
                             this.layer.params.STYLES.split(",");
        var idx = layerNames.indexOf(layerName);
        var styleName = styleNames && styleNames[idx];
        // check if we have a legend URL in the record's
        // "styles" data field
        if(styles && styles.length > 0) {
            if(styleName) {
                Ext.each(styles, function(s) {
                    url = (s.name == styleName && s.legend) && s.legend.href;
                    return !url;
                })
            } else if(this.defaultStyleIsFirst === true && !styleNames &&
                      !this.layer.params.SLD && !this.layer.params.SLD_BODY) {
                url = styles[0].legend && styles[0].legend.href;
            }
        }
        return url ||
               this.layer.getFullRequestString({
                   REQUEST: "GetLegendGraphic",
                   WIDTH: null,
                   HEIGHT: null,
                   EXCEPTIONS: "application/vnd.ogc.se_xml",
                   LAYER: layerName,
                   LAYERS: null,
                   STYLE: (styleName !== '') ? styleName: null,
                   STYLES: null,
                   SRS: null,
                   FORMAT: this.imageFormat
        });
    },

    /** private: method[updateLegend]
     *  :param url: ``String`` The legend URL, derived from the
     *      layer record or layer params (WMS GetLegendGraphic)
     *      if not provided.
     *
     *  Update the legend panel, adding, removing or updating
     *  the per-sublayer box component.
     */
    updateLegend: function(url) {
        var layerNames, layerName, i, len;
        
        layerNames = (this.layer.params.LAYERS instanceof Array) ? 
            this.layer.params.LAYERS :
            this.layer.params.LAYERS.split(",");

        if(this.items) {
            var destroyList = [];
            this.items.each(function(cmp) {
                i = layerNames.indexOf(cmp.itemId);
                if(i < 0) {
                    destroyList.push(cmp);
                } else {
                    layerName = layerNames[i];
                    var newUrl = url ||
                                 this.getLegendUrl(layerName, layerNames);
                    if(!OpenLayers.Util.isEquivalentUrl(newUrl, cmp.url)) {
                        cmp.updateLegend(newUrl);
                    }
                }
            }, this);
            for(i = 0, len = destroyList.length; i<len; i++) {
                var cmp = destroyList[i];
                // cmp.destroy() does not remove the cmp from
                // its parent container!
                this.remove(cmp);
                cmp.destroy();
            }
        }

        var doLayout = false;
        for(i = 0, len = layerNames.length; i<len; i++) {
            layerName = layerNames[i];
            if(!this.items || !this.getComponent(layerName)) {
                this.add({
                    xtype: "gx_legendimage",
                    url: url || this.getLegendUrl(layerName, layerNames),
                    itemId: layerName
                });
                doLayout = true;
            }
        }
        if(doLayout) {
            this.doLayout();
        }
    }
});
