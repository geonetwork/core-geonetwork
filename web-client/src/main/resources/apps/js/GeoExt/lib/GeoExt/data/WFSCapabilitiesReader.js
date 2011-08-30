/**
 * Copyright (c) 2008-2010 The Open Source Geospatial Foundation
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
 *  base_link = `Ext.data.DataReader <http://dev.sencha.com/deploy/dev/docs/?class=Ext.data.DataReader>`_
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
                {name: "title", type: "string"},
                {name: "namespace", type: "string", mapping: "featureNS"},
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

        var featureTypes = data.featureTypeList.featureTypes;
        var fields = this.recordType.prototype.fields;

        var featureType, values, field, v, parts, layer, values;
        var layerOptions, protocolOptions;

        var protocolDefaults = {
            url: data.capability.request.getfeature.href.post
        };

        var records = [];

        for(var i=0, lenI=featureTypes.length; i<lenI; i++) {
            featureType = featureTypes[i];
            if(featureType.name) {
                values = {};

                for(var j=0, lenJ=fields.length; j<lenJ; j++) {
                    field = fields.items[j];
                    v = featureType[field.mapping || field.name] ||
                        field.defaultValue;
                    v = field.convert(v);
                    values[field.name] = v;
                }

                protocolOptions = {
                    featureType: featureType.name,
                    featureNS: featureType.featureNS
                };
                if(this.meta.protocolOptions) {
                    Ext.apply(protocolOptions, this.meta.protocolOptions, 
                        protocolDefaults);
                } else {
                    Ext.apply(protocolOptions, {}, protocolDefaults);
                }

                layerOptions = {
                    protocol: new OpenLayers.Protocol.WFS(protocolOptions),
                    strategies: [new OpenLayers.Strategy.Fixed()]
                };
                if(this.meta.layerOptions) {
                    Ext.apply(layerOptions, this.meta.layerOptions);
                }

                values.layer = new OpenLayers.Layer.Vector(
                    featureType.title || featureType.name,
                    layerOptions
                );

                records.push(new this.recordType(values, values.layer.id));
            }
        }
        return {
            totalRecords: records.length,
            success: true,
            records: records
        };
    }
});
