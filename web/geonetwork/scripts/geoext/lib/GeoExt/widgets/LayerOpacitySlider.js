/* Copyright (C) 2008-2009 The Open Source Geospatial Foundation
 * Published under the BSD license.
 * See http://geoext.org/svn/geoext/core/trunk/license.txt for the full text
 * of the license.
 */

/**
 * @include GeoExt/widgets/tips/LayerOpacitySliderTip.js
 */

/** api: (define)
 *  module = GeoExt
 *  class = LayerOpacitySlider
 *  base_link = `Ext.Slider <http://extjs.com/deploy/dev/docs/?class=Ext.Slider>`_
 */
Ext.namespace("GeoExt");

/** api: example
 *  Sample code to render a slider outside the map viewport:
 *
 *  .. code-block:: javascript
 *
 *      var slider = new GeoExt.LayerOpacitySlider({
 *          renderTo: document.body,
 *          width: 200,
 *          layer: layer
 *      });
 *
 *  Sample code to add a slider to a map panel:
 *
 *  .. code-block:: javascript
 *
 *      var layer = new OpenLayers.Layer.WMS(
 *          "Global Imagery",
 *          "http://demo.opengeo.org/geoserver/wms",
 *          {layers: "bluemarble"}
 *      );
 *      var panel = new GeoExt.MapPanel({
 *          renderTo: document.body,
 *          height: 300,
 *          width: 400,
 *          map: {
 *              controls: [new OpenLayers.Control.Navigation()]
 *          },
 *          layers: [layer],
 *          extent: [-5, 35, 15, 55],
 *          items: [{
 *              xtype: "gx_opacityslider",
 *              layer: layer,
 *              aggressive: true,
 *              vertical: true,
 *              height: 100,
 *              x: 10,
 *              y: 20
 *          }]
 *      });
 */

/** api: constructor
 *  .. class:: LayerOpacitySlider(config)
 *
 *      Create a slider for controlling a layer's opacity.
 */
GeoExt.LayerOpacitySlider = Ext.extend(Ext.Slider, {

    /** api: config[layer]
     *  ``OpenLayers.Layer`` or :class:`GeoExt.data.LayerRecord`
     */
    layer: null,

    /** api: config[delay]
     *  ``Number`` Time in milliseconds before setting the opacity value to the
     *  layer. If the value change again within that time, the original value
     *  is not set. Only applicable if aggressive is true.
     */
    delay: 5,

    /** api: config[aggressive]
     *  ``Boolean``
     *  If set to true, the opacity is changed as soon as the thumb is moved.
     *  Otherwise when the thumb is released (default).
     */
    aggressive: false,

    /** private: property[minValue]
     *  ``Number``
     *  The minimum slider value, layer is fully transparent
     */
    minValue: 0,

    /** private: property[maxValue]
     *  ``Number``
     *  The maximum slider value, layer is fully opaque.
     */
    maxValue: 100,

    /** private: method[constructor]
     *  Construct the component.
     */
    constructor: function(config) {
        if (config.layer) {
            if (config.layer instanceof OpenLayers.Layer) {
                this.layer = config.layer;
            } else if (config.layer instanceof GeoExt.data.LayerRecord) {
                this.layer = config.layer.get('layer');
            }
            delete config.layer;
        }
        GeoExt.LayerOpacitySlider.superclass.constructor.call(this, config);
    },

    /** private: method[initComponent]
     *  Initialize the component.
     */
    initComponent: function() {
        // set the slider initial value
        if (this.layer && this.layer.opacity !== null) {
            this.value = parseInt(this.layer.opacity * 100);
        } else {
            // assume that the layer has no opacity
            this.value = 100;
        }

        GeoExt.LayerOpacitySlider.superclass.initComponent.call(this);

        if (this.aggressive === true) {
            this.on('change', this.opacityChanged, this, {
                buffer: this.delay
            });
        } else {
            this.on('changecomplete', this.opacityChanged, this);
        }
    },

    /** private: method[opacityChanged]
     *  :param slider: :class:`GeoExt.LayerOpacitySlider`
     *  :param value: ``Number`` The slider value
     *
     *  Updates the ``OpenLayers.Layer`` opacity value.
     */
    opacityChanged: function(slider, value) {
        if (this.layer) {
            this.layer.setOpacity(value / 100.0);
        }
    },

    /** private: method[addToMapPanel]
     *  :param panel: :class:`GeoExt.MapPanel`
     *
     *  Called by a MapPanel if this component is one of the items in the panel.
     */
    addToMapPanel: function(panel) {
        this.on({
            render: function() {
                var el = this.getEl();
                el.setStyle({
                    position: "absolute",
                    zIndex: panel.map.Z_INDEX_BASE.Control
                });
                el.on({
                    mousedown: this.stopMouseEvents,
                    click: this.stopMouseEvents
                });
            },
            scope: this
        });
    },

    /** private: method[removeFromMapPanel]
     *  :param panel: :class:`GeoExt.MapPanel`
     *
     *  Called by a MapPanel if this component is one of the items in the panel.
     */
    removeFromMapPanel: function(panel) {
        var el = this.getEl();
        el.un({
            mousedown: this.stopMouseEvents,
            click: this.stopMouseEvents,
            scope: this
        });
    },

    /** private: method[stopMouseEvents]
     *  :param e: ``Object``
     */
    stopMouseEvents: function(e) {
        e.stopEvent();
    }
});

/** api: xtype = gx_opacityslider */
Ext.reg('gx_opacityslider', GeoExt.LayerOpacitySlider);
