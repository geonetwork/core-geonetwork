/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/**
 * @include GeoExt/data/LayerStore.js
 */

/** api: (define)
 *  module = GeoExt
 *  class = MapPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
Ext.namespace("GeoExt");

/** api: example
 *  Sample code to create a panel with a new map:
 * 
 *  .. code-block:: javascript
 *     
 *      var mapPanel = new GeoExt.MapPanel({
 *          border: false,
 *          renderTo: "div-id",
 *          map: {
 *              maxExtent: new OpenLayers.Bounds(-90, -45, 90, 45)
 *          }
 *      });
 *     
 *  Sample code to create a map panel with a bottom toolbar in a Window:
 * 
 *  .. code-block:: javascript
 * 
 *      var win = new Ext.Window({
 *          title: "My Map",
 *          items: [{
 *              xtype: "gx_mappanel",
 *              bbar: new Ext.Toolbar()
 *          }]
 *      });
 */

/** api: constructor
 *  .. class:: MapPanel(config)
 *   
 *      Create a panel container for a map.
 */
GeoExt.MapPanel = Ext.extend(Ext.Panel, {

    /** api: config[map]
     *  ``OpenLayers.Map or Object``  A configured map or a configuration object
     *  for the map constructor.  A configured map will be available after
     *  construction through the :attr:`map` property.
     */

    /** api: property[map]
     *  ``OpenLayers.Map``  A configured map object.
     */
    map: null,
    
    /** api: config[layers]
     *  ``GeoExt.data.LayerStore or GeoExt.data.GroupingStore or Array(OpenLayers.Layer)``
     *  A store holding records. If not provided, an empty
     *  :class:`GeoExt.data.LayerStore` will be created.
     */
    
    /** api: property[layers]
     *  :class:`GeoExt.data.LayerStore`  A store containing
     *  :class:`GeoExt.data.LayerRecord` objects.
     */
    layers: null,

    
    /** api: config[center]
     *  ``OpenLayers.LonLat or Array(Number)``  A location for the map center.  If
     *  an array is provided, the first two items should represent x & y coordinates.
     */
    center: null,

    /** api: config[zoom]
     *  ``Number``  An initial zoom level for the map.
     */
    zoom: null,

    /** api: config[extent]
     *  ``OpenLayers.Bounds or Array(Number)``  An initial extent for the map (used
     *  if center and zoom are not provided.  If an array, the first four items
     *  should be minx, miny, maxx, maxy.
     */
    extent: null,
    
    /** private: method[initComponent]
     *  Initializes the map panel. Creates an OpenLayers map if
     *  none was provided in the config options passed to the
     *  constructor.
     */
    initComponent: function(){
        if(!(this.map instanceof OpenLayers.Map)) {
            this.map = new OpenLayers.Map(
                Ext.applyIf(this.map || {}, {allOverlays: true})
            );
        }
        var layers = this.layers;
        if(!layers || layers instanceof Array) {
            this.layers = new GeoExt.data.LayerStore({
                layers: layers,
                map: this.map
            });
        }
        
        if(typeof this.center == "string") {
            this.center = OpenLayers.LonLat.fromString(this.center);
        } else if(this.center instanceof Array) {
            this.center = new OpenLayers.LonLat(this.center[0], this.center[1]);
        }
        if(typeof this.extent == "string") {
            this.extent = OpenLayers.Bounds.fromString(this.extent);
        } else if(this.extent instanceof Array) {
            this.extent = OpenLayers.Bounds.fromArray(this.extent);
        }
        
        GeoExt.MapPanel.superclass.initComponent.call(this);       
    },
    
    /** private: method[updateMapSize]
     *  Tell the map that it needs to recalculate its size and position.
     */
    updateMapSize: function() {
        if(this.map) {
            this.map.updateSize();
        }
    },

    /** private: method[renderMap]
     *  Private method called after the panel has been rendered or after it
     *  has been laid out by its parent's layout.
     */
    renderMap: function() {
        var map = this.map;

        // hack: prevent map.updateSize (called from within map.render) from 
        // zooming to the map extent. This hack is a workaround for 
        // <http://trac.openlayers.org/ticket/2105> and must be
        // removed once this ticket is closed.
        var setCenter = map.setCenter;
        map.setCenter = function() {};
        map.render(this.body.dom);
        map.setCenter = setCenter;

        if(map.layers.length > 0) {
            if(this.center || this.zoom != null) {
                // both do not have to be defined
                map.setCenter(this.center, this.zoom);
            } else if(this.extent) {
                map.zoomToExtent(this.extent);
            } else {
                map.zoomToMaxExtent();
            }
        }
    },
    
    /** private: method[afterRender]
     *  Private method called after the panel has been rendered.
     */
    afterRender: function() {
        GeoExt.MapPanel.superclass.afterRender.apply(this, arguments);
        if(!this.ownerCt) {
            this.renderMap();
        } else {
            this.ownerCt.on("move", this.updateMapSize, this);
            this.ownerCt.on({
                "afterlayout": {
                    fn: this.renderMap,
                    scope: this,
                    single: true
                }
            });
        }
    },

    /** private: method[onResize]
     *  Private method called after the panel has been resized.
     */
    onResize: function() {
        GeoExt.MapPanel.superclass.onResize.apply(this, arguments);
        this.updateMapSize();
    },
    
    /** private: method[onBeforeAdd]
     *  Private method called before a component is added to the panel.
     */
    onBeforeAdd: function(item) {
        if(typeof item.addToMapPanel === "function") {
            item.addToMapPanel(this);
        }
        GeoExt.MapPanel.superclass.onBeforeAdd.apply(this, arguments);
    },
    
    /** private: method[remove]
     *  Private method called when a component is removed from the panel.
     */
    remove: function(item, autoDestroy) {
        if(typeof item.removeFromMapPanel === "function") {
            item.removeFromMapPanel(this);
        }
        GeoExt.MapPanel.superclass.remove.apply(this, arguments);
    },

    /** private: method[beforeDestroy]
     *  Private method called during the destroy sequence.
     */
    beforeDestroy: function() {
        if(this.ownerCt) {
            this.ownerCt.un("move", this.updateMapSize, this);
        }
        /**
         * If this container was passed a map instance, it is the
         * responsibility of the creator to destroy it.
         */
        if(!this.initialConfig.map ||
           !(this.initialConfig.map instanceof OpenLayers.Map)) {
            // we created the map, we destroy it
            if(this.map && this.map.destroy) {
                this.map.destroy();
            }
        }
        delete this.map;
        GeoExt.MapPanel.superclass.beforeDestroy.apply(this, arguments);
    }
    
});

/** api: function[guess]
 *  :return: ``GeoExt.MapPanel`` The first map panel found by the Ext
 *      component manager.
 *  
 *  Convenience function for guessing the map panel of an application. This
 *     can reliably be used for all applications that just have one map panel
 *     in the viewport.
 */
GeoExt.MapPanel.guess = function() {
    return Ext.ComponentMgr.all.find(function(o) { 
        return o instanceof GeoExt.MapPanel; 
    }); 
};


/** api: xtype = gx_mappanel */
Ext.reg('gx_mappanel', GeoExt.MapPanel); 
