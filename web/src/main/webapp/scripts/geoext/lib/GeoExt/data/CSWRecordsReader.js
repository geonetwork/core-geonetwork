/* Copyright (C) 2008-2009 The Open Source Geospatial Foundation
 * Published under the BSD license.
 * See http://geoext.org/svn/geoext/core/trunk/license.txt for the full text
 * of the license.
 *
 * pending approval */

/** api: (define)
 *  module = GeoExt.data
 *  class = CSWRecordsReader
 *  base_link = `Ext.data.JsonReader <http://extjs.com/deploy/dev/docs/?class=Ext.data.JsonReader>`_
 */

Ext.namespace("GeoExt.data");

/** api: constructor
 *  .. class:: CSWRecordsReader(meta, recordType)
 *  
 *      :param meta: ``Object`` Reader configuration.
 *      :param recordType: ``Array | Ext.data.Record`` An array of field
 *          configuration objects or a record object.  Default is
 *          :class:`Ext.data.Record`.
 *   
 *      Data reader class to create an array of records from a CSW
 *      GetRecords response.
 */
GeoExt.data.CSWRecordsReader = function(meta, recordType) {
    meta = meta || {};
    if(!meta.format) {
        meta.format = new OpenLayers.Format.CSWGetRecords();
    }
    if(!meta.root) {
        meta.root = 'records';
    }

    // FIXME
    // There may be information of interest in the getRecords response
    // namely SearchResults.numberOfRecordsReturned and
    // SearchResults.numberOfRecordsMatched 

    GeoExt.data.CSWRecordsReader.superclass.constructor.call(
        this, meta, recordType
    );
};

Ext.extend(GeoExt.data.CSWRecordsReader, Ext.data.JsonReader, {

    /** private: method[read]
     *  :param request: ``Object`` The XHR object which contains
     *      the parsed XML document.
     *  :return: ``Object`` A data block which is used by an
     *      ``Ext.data.Store`` as a cache of ``Ext.data.Record``
     *      objects.
     */
    read: function(request) {
        var data = request.responseXML;
        if(!data || !data.documentElement) {
            data = request.responseText;
        }
        return this.readRecords(data);
    },

    /** private: method[readRecords]
     *  :param data: ``DOMElement | String | Object`` A document
     *      element or XHR response string.
     *  :return: ``Object`` A data block which is used by an
     *      ``Ext.data.Store`` as a cache of ``Ext.data.Record``
     *      objects.
     */
    readRecords: function(data) {
        if(typeof data === "string" || data.nodeType) {
            data = this.meta.format.read(data);
        }
        return GeoExt.data.CSWRecordsReader.superclass.readRecords.call(
            this, data
        );
    }
});
