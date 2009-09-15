/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: (define)
 *  module = GeoExt
 *  class = LegendPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */

Ext.namespace('GeoExt');

/** api: constructor
 *  .. class:: LegendPanel(config)
 *
 *  A panel showing legends of all layers in a layer store.
 *  Depending on the layer type, a legend renderer will be chosen.
 */
GeoExt.LegendPanel = Ext.extend(Ext.Panel, {

    /** api: config[dynamic]
     *  ``Boolean``
     *  If false the LegendPanel will not listen to the add, remove and change 
     *  events of the LayerStore. So it will load with the initial state of
     *  the LayerStore and not change anymore. 
     */
    dynamic: true,
    
    /** api: config[showTitle]
     *  ``Boolean``
     *  Whether or not to show the title of a layer. This can be a global
     *  setting for the whole panel, or it can be overridden on the LayerStore 
     *  record using the hideInLegend property.
     */
    showTitle: true,

    /** api: config[labelCls]
     *  ``String``
     *  Optional css class to use for the layer title labels.
     */
    labelCls: null,

    /** api: config[bodyStyle]
     *  ``String``
     *  Optional style to apply to the body of the legend panels.
     */
    bodyStyle: '',

    /** api: config[layerStore]
     *  ``GeoExt.data.LayerStore``
     *  The layer store containing layers to be displayed in the legend 
     *  container. If not provided it will be taken from the MapPanel.
     */
    layerStore: null,
    
    /** api: config[legendOptions]
     *  ``Object``
     *  Config options for the legend generator, i.e. the panel that provides
     *  the legend image.
     */

    /** private: method[initComponent]
     *  Initializes the legend panel.
     */
    initComponent: function() {
        GeoExt.LegendPanel.superclass.initComponent.call(this);
    },
    
    /** private: method[onRender]
     *  Private method called when the legend panel is being rendered.
     */
    onRender: function() {
        GeoExt.LegendPanel.superclass.onRender.apply(this, arguments);
        if(!this.layerStore) {
            this.layerStore = GeoExt.MapPanel.guess().layers;
        }
        this.layerStore.each(function(record) {
                this.addLegend(record);
            }, this);
        if (this.dynamic) {
            this.layerStore.on({
                "add": this.onStoreAdd,
                "remove": this.onStoreRemove,
                "update": this.onStoreUpdate,
                scope: this
            });
        }
        this.doLayout();
    },

    /** private: method[recordIndexToPanelIndex]
     *  Private method to get the panel index for a layer represented by a
     *  record.
     *
     *  :param index ``Integer`` The index of the record in the store.
     *
     *  :return: ``Integer`` The index of the sub panel in this panel.
     */
    recordIndexToPanelIndex: function(index) {
        var store = this.layerStore;
        var count = store.getCount();
        var panelIndex = -1;
        var legendCount = this.items ? this.items.length : 0;
        for(var i=count-1; i>=0; --i) {
            var layer = store.getAt(i).get("layer");
            var legendGenerator = GeoExt[
                "Legend" + layer.CLASS_NAME.split(".").pop()
            ];
            if(layer.displayInLayerSwitcher && legendGenerator &&
                (store.getAt(i).get("hideInLegend") !== true)) {
                    ++panelIndex;
                    if(index === i || panelIndex > legendCount-1) {
                        break;
                    }
            }
        }
        return panelIndex;
    },

    /** private: method[onStoreUpdate]
     *  Update a layer within the legend panel. Gets called when the store
     *  fires the update event. This usually means the visibility of the layer
     *  has changed.
     *
     *  :param store: ``Ext.data.Store`` The store in which the record was
     *      changed.
     *  :param record: ``Ext.data.Record`` The record object corresponding
     *      to the updated layer.
     *  :param operation: ``String`` The type of operation.
     */
    onStoreUpdate: function(store, record, operation) {
        var layer = record.get('layer');
        var legend = this.getComponent(layer.id);
        if ((this.showTitle && !record.get('hideTitle')) && 
            (legend && legend.items.get(0).text !== record.get('title'))) {
                // we need to update the title
                legend.items.get(0).setText(record.get('title'));
        }
        if (legend) {
            legend.setVisible(layer.getVisibility() && 
                layer.displayInLayerSwitcher && !record.get('hideInLegend'));
            if (record.get('legendURL')) {
                var items = legend.findByType('gx_legendimage');
                for (var i=0, len=items.length; i<len; i++) {
                    items[i].setUrl(record.get('legendURL'));
                }
            }
        }
    },

    /** private: method[onStoreAdd]
     *  Private method called when a layer is added to the store.
     *
     *  :param store: ``Ext.data.Store`` The store to which the record(s) was 
     *      added.
     *  :param record: ``Ext.data.Record`` The record object(s) corresponding
     *      to the added layers.
     *  :param index: ``Integer`` The index of the inserted record.
     */
    onStoreAdd: function(store, records, index) {
        var panelIndex = this.recordIndexToPanelIndex(index+records.length-1);
        for (var i=0, len=records.length; i<len; i++) {
            this.addLegend(records[i], panelIndex);
        }
        this.doLayout();
    },

    /** private: method[onStoreRemove]
     *  Private method called when a layer is removed from the store.
     *
     *  :param store: ``Ext.data.Store`` The store from which the record(s) was
     *      removed.
     *  :param record: ``Ext.data.Record`` The record object(s) corresponding
     *      to the removed layers.
     *  :param index: ``Integer`` The index of the removed record.
     */
    onStoreRemove: function(store, record, index) {
        this.removeLegend(record);
    },

    /** private: method[removeLegend]
     *  Remove the legend of a layer.
     *  :param record: ``Ext.data.Record`` The record object from the layer 
     *      store to remove.
     */
    removeLegend: function(record) {
        var legend = this.getComponent(record.get('layer').id);
        if (legend) {
            this.remove(legend, true);
            this.doLayout();
        }
    },

    /** private: method[createLegendSubpanel]
     *  Create a legend sub panel for the layer.
     *
     *  :param record: ``Ext.data.Record`` The record object from the layer
     *      store.
     *
     *  :return: ``Ext.Panel`` The created panel per layer
     */
    createLegendSubpanel: function(record) {
        var layer = record.get('layer');
        var mainPanel = this.createMainPanel(record);
        if (mainPanel !== null) {
            // the default legend can be overridden by specifying a
            // legendURL property
            var legend;
            if (record.get('legendURL')) {
                legend = new GeoExt.LegendImage({url: record.get('legendURL')});
                mainPanel.add(legend);
            } else {
                var legendGenerator = GeoExt[
                    "Legend" + layer.CLASS_NAME.split(".").pop()
                ];
                if (legendGenerator) {
                    legend = new legendGenerator(Ext.applyIf({
                        layer: layer,
                        record: record
                    }, this.legendOptions));
                    mainPanel.add(legend);
                }
            }
        }
        return mainPanel;
    },

    /** private: method[addLegend]
     *  Add a legend for the layer.
     *
     *  :param record: ``Ext.data.Record`` The record object from the layer 
     *      store.
     *  :param index: ``Integer`` The position at which to add the legend.
     */
    addLegend: function(record, index) {
        index = index || 0;
        var layer = record.get('layer');
        var legendSubpanel = this.createLegendSubpanel(record);
        if (legendSubpanel !== null) {
           legendSubpanel.setVisible(layer.getVisibility());
           this.insert(index, legendSubpanel);
        }
    },

    /** private: method[createMainPanel]
     *  Creates the main panel with a title for the layer.
     *
     *  :param record: ``Ext.data.Record`` The record object from the layer
     *      store.
     *
     *  :return: ``Ext.Panel`` The created main panel with a label.
     */
    createMainPanel: function(record) {
        var layer = record.get('layer');
        var panel = null;
        var legendGenerator = GeoExt[
            "Legend" + layer.CLASS_NAME.split(".").pop()
        ];
        if (layer.displayInLayerSwitcher && !record.get('hideInLegend') &&
            legendGenerator) {
            var panelConfig = {
                id: layer.id,
                border: false,
                bodyBorder: false,
                bodyStyle: this.bodyStyle,
                items: [
                    new Ext.form.Label({
                        text: (this.showTitle && !record.get('hideTitle')) ? 
                            layer.name : '',
                        cls: 'x-form-item x-form-item-label' +
                            (this.labelCls ? ' ' + this.labelCls : '')
                    })
                ]
            };
            panel = new Ext.Panel(panelConfig);
        }
        return panel;
    },

    /** private: method[onDestroy]
     *  Private method called during the destroy sequence.
     */
    onDestroy: function() {
        if(this.layerStore) {
            this.layerStore.un("add", this.onStoreAdd, this);
            this.layerStore.un("remove", this.onStoreRemove, this);
            this.layerStore.un("update", this.onStoreUpdate, this);
        }
        GeoExt.LegendPanel.superclass.onDestroy.apply(this, arguments);
    }
    
});

/** api: xtype = gx_legendpanel */
Ext.reg('gx_legendpanel', GeoExt.LegendPanel);
