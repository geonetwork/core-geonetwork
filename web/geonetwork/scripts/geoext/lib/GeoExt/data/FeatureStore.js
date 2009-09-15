/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/**
 * @include GeoExt/data/FeatureReader.js
 */

/** api: (define)
 *  module = GeoExt.data
 *  class = FeatureStore
 *  base_link = `Ext.data.DataStore <http://extjs.com/deploy/dev/docs/?class=Ext.data.DataStore>`_
 */
Ext.namespace("GeoExt.data");

/** api: constructor
 *  .. class:: FeatureStore
 *
 *      A store containing :class:`GeoExt.data.FeatureRecord` entries that
 *      optionally synchronizes with an ``OpenLayers.Layer.Vector``.
 */

/** api: example
 *  Sample code to create a store with features from a vector layer:
 *  
 *  .. code-block:: javascript
 *
 *      var store = new GeoExt.data.FeatureStore({
 *          layer: myLayer,
 *          features: myFeatures
 *      });
 */

/**
 * Class: GeoExt.data.FeatureStoreMixin
 * A store that synchronizes a features array of an {OpenLayers.Layer.Vector} with a
 * feature store holding {<GeoExt.data.FeatureRecord>} entries.
 * 
 * This class can not be instantiated directly. Instead, it is meant to extend
 * {Ext.data.Store} or a subclass of it:
 * (start code)
 * var store = new (Ext.extend(Ext.data.Store, GeoExt.data.FeatureStoreMixin))({
 *     layer: myLayer,
 *     features: myFeatures
 * });
 * (end)
 * 
 * For convenience, a {<GeoExt.data.FeatureStore>} class is available as a
 * shortcut to the Ext.extend sequence in the above code snippet. The above
 * is equivalent to:
 * (start code)
 * var store = new GeoExt.data.FeatureStore({
 *     layer: myLayer,
 *     features: myFeatures
 * });
 * (end)
 */
GeoExt.data.FeatureStoreMixin = {
    
    /** api: config[layer]
     *  ``OpenLayers.Layer.Vector``  Layer to synchronize the store with.
     */
    layer: null,
    
    /** api: config[features]
     *  ``Array(OpenLayers.Feature.Vector)``  Features that will be added to the
     *  store (and the layer if provided).
     */

    /** api: config[reader]
     *  ``Ext.data.DataReader`` The reader used to produce records from objects
     *  features.  Default is :class:`GeoExt.data.FeatureReader`.
     */
    reader: null,

    /** api: config[addFeatureFilter]
     *  ``Function`` This function is called before a feature record is added to
     *  the store, it receives the feature from which a feature record is to be
     *  created, if it returns false then no record is added.
     */
    addFeatureFilter: null,
    
    /** api: config[addRecordFilter]
     *  ``Function`` This function is called before a feature is added to the
     *  layer, it receives the feature record associated with the feature to be
     *  added, if it returns false then no feature is added.
     */
    addRecordFilter: null,
    
    /** api: config[initDir]
     *  ``Number``  Bitfields specifying the direction to use for the
     *  initial sync between the layer and the store, if set to 0 then no
     *  initial sync is done. Default is
     *  ``GeoExt.data.FeatureStore.LAYER_TO_STORE|GeoExt.data.FeatureStore.STORE_TO_LAYER``.
     */

    /** private */
    constructor: function(config) {
        config = config || {};
        config.reader = config.reader ||
                        new GeoExt.data.FeatureReader({}, config.fields);
        var layer = config.layer;
        delete config.layer;
        // 'features' option - is an alias 'data' option
        if (config.features) {
            config.data = config.features;
        }
        delete config.features;
        // "initDir" option
        var options = {initDir: config.initDir};
        delete config.initDir;
        arguments.callee.superclass.constructor.call(this, config);
        if(layer) {
            this.bind(layer, options);
        }
    },

    /** api: method[bind]
     *  :param layer: ``OpenLayers.Layer`` Layer that the store should be
     *      synchronized with.
     *  
     *  Bind this store to a layer instance, once bound the store
     *  is synchronized with the layer and vice-versa.
     */ 
    bind: function(layer, options) {
        if(this.layer) {
            // already bound
            return;
        }
        this.layer = layer;
        options = options || {};

        var initDir = options.initDir;
        if(options.initDir == undefined) {
            initDir = GeoExt.data.FeatureStore.LAYER_TO_STORE |
                      GeoExt.data.FeatureStore.STORE_TO_LAYER;
        }

        // create a snapshot of the layer's features
        var features = layer.features.slice(0);

        if(initDir & GeoExt.data.FeatureStore.STORE_TO_LAYER) {
            var records = this.getRange();
            for(var i=records.length - 1; i>=0; i--) {
                this.layer.addFeatures([records[i].get("feature")]);
            }
        }

        if(initDir & GeoExt.data.FeatureStore.LAYER_TO_STORE) {
            this.loadData(features, true /* append */);
        }

        layer.events.on({
            "featuresadded": this.onFeaturesAdded,
            "featuresremoved": this.onFeaturesRemoved,
            "featuremodified": this.onFeatureModified,
            scope: this
        });
        this.on({
            "load": this.onLoad,
            "clear": this.onClear,
            "add": this.onAdd,
            "remove": this.onRemove,
            "update": this.onUpdate,
            scope: this
        });
    },

    /** api: method[unbind]
     *  Unbind this store from the layer it is currently bound.
     */
    unbind: function() {
        if(this.layer) {
            this.layer.events.un({
                "featuresadded": this.onFeaturesAdded,
                "featuresremoved": this.onFeaturesRemoved,
                "featuremodified": this.onFeatureModified,
                scope: this
            });
            this.un("load", this.onLoad, this);
            this.un("clear", this.onClear, this);
            this.un("add", this.onAdd, this);
            this.un("remove", this.onRemove, this);
            this.un("update", this.onUpdate, this);

            this.layer = null;
        }
    },
   
   
    /** private: method[onFeaturesAdded]
     *  Handler for layer featuresadded event
     */
    onFeaturesAdded: function(evt) {
        if(!this._adding) {
            var features = evt.features, toAdd = features;
            if(typeof this.addFeatureFilter == "function") {
                toAdd = [];
                var i, len, feature;
                for(var i=0, len=features.length; i<len; i++) {
                    feature = features[i];
                    if(this.addFeatureFilter(feature) !== false) {
                        toAdd.push(feature);
                    }
                }
            }
            // add feature records to the store, when called with
            // append true loadData triggers an "add" event and
            // then a "load" event
            this._adding = true;
            this.loadData(toAdd, true /* append */);
            delete this._adding;
        }
    },
    
    /** private: method[onFeaturesRemoved]
     *  Handler for layer featuresremoved event
     */
    onFeaturesRemoved: function(evt){
        if(!this._removing) {
            var features = evt.features, feature, record, i;
            for(i=features.length - 1; i>=0; i--) {
                feature = features[i];
                record = this.getById(feature.id);
                if(record !== undefined) {
                    this._removing = true;
                    this.remove(record);
                    delete this._removing;
                }
            }
        }
    },

    /** private: method[onFeatureModified]
     *  Handler for layer featuremodified event
     */
    onFeatureModified: function(evt) {
        if(!this._updating) {
            var feature = evt.feature;
            var record = this.getById(feature.id);
            if(record !== undefined) {
                record.beginEdit();
                attributes = feature.attributes;
                if(attributes) {
                    var fields = this.recordType.prototype.fields;
                    for(var i=0, len=fields.length; i<len; i++) {
                        var field = fields.items[i];
                        var v = attributes[field.mapping || field.name] ||
                                field.defaultValue;
                        v = field.convert(v);
                        record.set(field.name, v);
                    }
                }
                // the calls to set below won't trigger "update"
                // events because we called beginEdit to start a
                // "transaction", "update" will be triggered by
                // endEdit
                record.set("state", feature.state);
                record.set("fid", feature.fid);
                record.set("feature", feature);
                this._updating = true;
                record.endEdit();
                delete this._updating;
            }
        }
    },

    /** private: method[addFeaturesToLayer]
     *  Given an array of records add features to the layer. This
     *  function is used by the onLoad and onAdd handlers.
     */
    addFeaturesToLayer: function(records) {
        var i, len, features, record;
        if(typeof this.addRecordFilter == "function") {
            features = []
            for(i=0, len=records.length; i<len; i++) {
                record = records[i];
                if(this.addRecordFilter(record) !== false) {
                    features.push(record.get("feature"));
                }
            }
        } else {
            features = new Array((len=records.length));
            for(i=0; i<len; i++) {
                features[i] = records[i].get("feature");
            }
        }
        if(features.length > 0) {
            this._adding = true;
            this.layer.addFeatures(features);
            delete this._adding;
        }
    },
   
    /** private: method[onLoad]
     *  :param store: ``Ext.data.Store``
     *  :param records: ``Array(Ext.data.Record)``
     *  :param options: ``Object``
     * 
     *  Handler for store load event
     */
    onLoad: function(store, records, options) {
        // if options.add is true an "add" event was already
        // triggered, and onAdd already did the work of 
        // adding the features to the layer.
        if(!options || options.add !== true) {
            this._removing = true;
            this.layer.removeFeatures(this.layer.features);
            delete this._removing;

            this.addFeaturesToLayer(records);
        }
    },
    
    /** private: method[onClear]
     *  :param store: ``Ext.data.Store``
     *      
     *  Handler for store clear event
     */
    onClear: function(store) {
        this._removing = true;
        this.layer.removeFeatures(this.layer.features);
        delete this._removing;
    },
    
    /** private: method[onAdd]
     *  :param store: ``Ext.data.Store``
     *  :param records: ``Array(Ext.data.Record)``
     *  :param index: ``Number``
     * 
     *  Handler for store add event
     */
    onAdd: function(store, records, index) {
        if(!this._adding) {
            // addFeaturesToLayer takes care of setting
            // this._adding to true and deleting it
            this.addFeaturesToLayer(records);
        }
    },
    
    /** private: method[onRemove]
     *  :param store: ``Ext.data.Store``
     *  :param records: ``Array(Ext.data.Record)``
     *  :param index: ``Number``
     *      
     *  Handler for store remove event
     */
    onRemove: function(store, record, index){
        if(!this._removing) {
            var feature = record.get("feature");
            if (this.layer.getFeatureById(feature.id) != null) {
                this._removing = true;
                this.layer.removeFeatures([record.get("feature")]);
                delete this._removing;
            }
        }
    },

    /** private: method[onUpdate]
     *  :param store: ``Ext.data.Store``
     *  :param record: ``Ext.data.Record``
     *  :param operation: ``String``
     *
     *  Handler for update.
     */
    onUpdate: function(store, record, operation) {
        if(!this._updating) {
            var feature = record.get("feature");
            if(record.fields) {
                var cont = this.layer.events.triggerEvent(
                    "beforefeaturemodified", {feature: feature}
                );
                if(cont !== false) {
                    record.fields.each(
                        function(field) {
                            feature.attributes[field.mapping || field.name] =
                                record.get(field.name);
                        }
                    );
                    this._updating = true;
                    this.layer.events.triggerEvent(
                        "featuremodified", {feature: feature}
                    );
                    delete this._updating;
                    if (this.layer.getFeatureById(feature.id) != null) {
                        this.layer.drawFeature(feature);
                    }
                }
            }
        }
    }
};

GeoExt.data.FeatureStore = Ext.extend(
    Ext.data.Store,
    GeoExt.data.FeatureStoreMixin
);

/**
 * Constant: GeoExt.data.FeatureStore.LAYER_TO_STORE
 * {Integer} Constant used to make the store be automatically updated
 * when changes occur in the layer.
 */
GeoExt.data.FeatureStore.LAYER_TO_STORE = 1;

/**
 * Constant: GeoExt.data.FeatureStore.STORE_TO_LAYER
 * {Integer} Constant used to make the layer be automatically updated
 * when changes occur in the store.
 */
GeoExt.data.FeatureStore.STORE_TO_LAYER = 2;
