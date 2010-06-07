/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/**
 * @include GeoExt/data/LayerRecord.js
 */

/** api: (define)
 *  module = GeoExt.data
 *  class = WFSCapabilitiesReader
 *  base_link = `Ext.data.DataReader <http://extjs.com/deploy/dev/docs/?class=Ext.data.DataReader>`_
 */
Ext.namespace("GeoExt.data");

/** api: constructor
 *  .. class:: WFSCapabilitiesReader(meta, recordType)
 *  
 *      :param meta: ``Object`` Reader configuration.
 *      :param recordType: ``Array | Ext.data.Record`` An array of field
 *          configuration objects or a record object.  Default is
 *          :class:`GeoExt.data.LayerRecord`.
 *   
 *      Data reader class to create an array of
 *      :class:`GeoExt.data.LayerRecord` objects from a WFS GetCapabilities
 *      response.
 */
GeoExt.data.WFSCapabilitiesReader = function(meta, recordType) {
    meta = meta || {};
    if(!meta.format) {
        meta.format = new OpenLayers.Format.WFSCapabilities();
    }
    if(!(typeof recordType === "function")) {
        recordType = GeoExt.data.LayerRecord.create(
            recordType || meta.fields || [
                {name: "name", type: "string"},
                {name: "abstract", type: "string"}
            ]
        );
    }
    GeoExt.data.WFSCapabilitiesReader.superclass.constructor.call(
        this, meta, recordType
    );
};

Ext.extend(GeoExt.data.WFSCapabilitiesReader, Ext.data.DataReader, {

    /** private: method[read]
     *  :param request: ``Object`` The XHR object which contains the parsed XML
     *      document.
     *  :return: ``Object`` A data block which is used by an ``Ext.data.Store``
     *      as a cache of ``Ext.data.Record`` objects.
     */
    read: function(request) {
        var data = request.responseXML;
        if(!data || !data.documentElement) {
            data = request.responseText;
        }
        return this.readRecords(data);
    },

    /** private: method[readRecords]
     *  :param data: ``DOMElement | String | Object`` A document element or XHR
     *      response string.  As an alternative to fetching capabilities data
     *      from a remote source, an object representing the capabilities can
     *      be provided given that the structure mirrors that returned from the
     *      capabilities parser.
     *  :return: ``Object`` A data block which is used by an ``Ext.data.Store``
     *      as a cache of ``Ext.data.Record`` objects.
     *  
     *  Create a data block containing Ext.data.Records from an XML document.
     */
    readRecords: function(data) {
        if(typeof data === "string" || data.nodeType) {
            data = this.meta.format.read(data);
        }
        var records = [], layer, l, parts, layerOptions, protocolOptions;
        var featureTypes = data.featureTypeList.featureTypes;
        var protocolDefaults = {
            url: data.capability.request.getfeature.href.post
        };
        for(var i=0, len=featureTypes.length; i<len; i++) {
            layer = featureTypes[i];
            if(layer.name) {
                // create protocol
                parts = layer.name.split(":");
                if (parts.length > 1) {
                    protocolOptions = {
                        featureType: parts[1],
                        featurePrefix: parts[0]
                    };
                } else {
                    protocolOptions = {
                        featureType: parts[0],
                        featurePrefix: null
                    };
                }
                if(this.meta.protocolOptions) {
                    Ext.apply(protocolOptions, this.meta.protocolOptions, 
                        protocolDefaults);
                } else {
                    Ext.apply(protocolOptions, {}, protocolDefaults);
                }
                // create vector layer with protocol
                layerOptions = {
                    protocol: new OpenLayers.Protocol.WFS(protocolOptions),
                    strategies: [new OpenLayers.Strategy.Fixed()]
                };
                if(this.meta.layerOptions) {
                    Ext.apply(layerOptions, this.meta.layerOptions);
                }
                l = new OpenLayers.Layer.Vector(
                    layer.title || layer.name, 
                    layerOptions
                );
                records.push(new this.recordType(Ext.apply(layer, {
                    layer: l
                }), l.id));
            }
        }
        return {
            totalRecords: records.length,
            success: true,
            records: records
        };
    }
});
