/**
 * Copyright (c) 2008-2010 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: (define)
 *  module = GeoExt
 *  class = LayerLegend
 *  base_link = `Ext.Container <http://dev.sencha.com/deploy/dev/docs/?class=Ext.Container>`_
 */

Ext.namespace('GeoExt');

/** api: constructor
 *  .. class:: LayerLegend(config)
 *
 *      Base class for components of :class:`GeoExt.LegendPanel`.
 */
GeoExt.LayerLegend = Ext.extend(Ext.Container, {

    /** api: config[layerRecord]
     *  :class:`GeoExt.data.LayerRecord`  The layer record for the legend
     */
    layerRecord: null,

    /** api: config[showTitle]
     *  ``Boolean``
     *  Whether or not to show the title of a layer. This can be overridden
     *  on the LayerStore record using the hideTitle property.
     */
    showTitle: true,
    
    /** api: config[legendTitle]
     *  ``String``
     *  Optional title to be displayed instead of the layer title.  If this is
     *  set, the value of ``showTitle`` will be ignored (assumed to be true).
     */
    legendTitle: null,

    /** api: config[labelCls]
     *  ``String``
     *  Optional css class to use for the layer title labels.
     */
    labelCls: null,
    
    /** private: property[layerStore]
     *  :class:`GeoExt.data.LayerStore`
     */
    layerStore: null,

    /** private: method[initComponent]
     */
    initComponent: function() {
        GeoExt.LayerLegend.superclass.initComponent.call(this);
        this.autoEl = {};
        this.add({
            xtype: "label",
            text: this.getLayerTitle(this.layerRecord),
            cls: 'x-form-item x-form-item-label' +
                (this.labelCls ? ' ' + this.labelCls : '')
        });
        if (this.layerRecord && this.layerRecord.store) {
            this.layerStore = this.layerRecord.store;
            this.layerStore.on("update", this.onStoreUpdate, this);
        }
    },

    /** private: method[onStoreUpdate]
     *  Update a the legend. Gets called when the store fires the update event.
     *  This usually means the visibility of the layer, its style or title
     *  has changed.
     *
     *  :param store: ``Ext.data.Store`` The store in which the record was
     *      changed.
     *  :param record: ``Ext.data.Record`` The record object corresponding
     *      to the updated layer.
     *  :param operation: ``String`` The type of operation.
     */
    onStoreUpdate: function(store, record, operation) {
        // if we don't have items, we are already awaiting garbage
        // collection after being removed by LegendPanel::removeLegend, and
        // updating will cause errors
        if (record === this.layerRecord && this.items.getCount() > 0) {
            var layer = record.getLayer();
            this.setVisible(layer.getVisibility() &&
                layer.calculateInRange() && layer.displayInLayerSwitcher &&
                !record.get('hideInLegend'));
            this.update();
        }
    },

    /** private: method[update]
     *  Updates the legend.
     */
    update: function() {
        var title = this.getLayerTitle(this.layerRecord);
        if (this.items.get(0).text !== title) {
            // we need to update the title
            this.items.get(0).setText(title);
        }
    },
    
    /** private: method[getLayerTitle]
     *  :arg record: :class:GeoExt.data.LayerRecord
     *  :returns: ``String``
     *
     *  Get a title for the layer.  If the record doesn't have a title, use the 
     *  name.
     */
    getLayerTitle: function(record) {
        var title = this.legendTitle || "";
        if (this.showTitle && !title) {
            if (record && !record.get("hideTitle")) {
                title = record.get("title") || 
                    record.get("name") || 
                    record.getLayer().name || "";
            }
        }
        return title;
    },
    
    /** private: method[beforeDestroy]
     */
    beforeDestroy: function() {
        this.layerStore &&
            this.layerStore.un("update", this.onStoreUpdate, this);
        GeoExt.LayerLegend.superclass.beforeDestroy.apply(this, arguments);
    }

});

/** class: method[getTypes]
 *  :param layerRecord: class:`GeoExt.data.LayerRecord` A layer record to get
 *      legend types for. If not provided, all registered types will be
 *      returned.
 *  :param preferredTypes: ``Array(String)`` Types that should be considered.
 *      first. If not provided, all legend types will be returned in the order
 *      they were registered.
 *  :return: ``Array(String)`` xtypes of legend types that can be used with
 *      the provided ``layerRecord``.
 *  
 *  Gets an array of legend xtypes that support the provided layer record,
 *  with optionally provided preferred types listed first.
 */
GeoExt.LayerLegend.getTypes = function(layerRecord, preferredTypes) {
    var types = (preferredTypes || []).concat();
    var goodTypes = [];
    for(var type in GeoExt.LayerLegend.types) {
        if(GeoExt.LayerLegend.types[type].supports(layerRecord)) {
            // add to goodTypes if not preferred
            types.indexOf(type) == -1 && goodTypes.push(type);
        } else {
            // preferred, but not supported
            types.remove(type);
        }
    }
    // take the remaining preferred types, and add other good types 
    return types.concat(goodTypes);
};
    
/** private: method[supports]
 *  :param layerRecord: :class:`GeoExt.data.LayerRecord` The layer record
 *      to check support for.
 *  :return: ``Boolean`` true if this legend type supports the layer
 *      record.
 *  
 *  Checks whether this legend type supports the provided layerRecord.
 */
GeoExt.LayerLegend.supports = function(layerRecord) {
    // to be implemented by subclasses
};

/** class: constant[GeoExt.LayerLegend.types]
 *  An object containing a name-class mapping of LayerLegend subclasses.
 *  To register as LayerLegend, a subclass should add itself to this object:
 *  
 *  .. code-block:: javascript
 *  
 *      GeoExt.GetLegendGraphicLegend = Ext.extend(GeoExt.LayerLegend, {
 *      });
 *      
 *      GeoExt.LayerLegend.types["getlegendgraphic"] =
 *          GeoExt.GetLegendGraphicLegend;
 */
GeoExt.LayerLegend.types = {};
