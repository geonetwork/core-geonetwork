/*
 * Copyright (C) 2001-2011 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * 
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
Ext.namespace("GeoNetwork.data");

/** api: (define)
 *  module = GeoNetwork.data
 *  class = OpenSearchSuggestionReader
 *  base_link = `Ext.data.JsonReader <http://extjs.com/deploy/dev/docs/?class=Ext.data.JsonReader>`_
 */
/** api: constructor 
 *  .. class:: OpenSearchSuggestionReader(meta, recordType)
 *
 *  Create a new OpenSearch suggestion reader object.
 *
 *  An Ext.ArrayReader could not be used because it seems that
 *  we cannot retrieve the root based on an array index.
 *  In OpenSearch, root is the 2nd column of the array:
 *
 *  .. code-block:: javascript
 *
 *    ["sea",["sears","search engines","search engine",
 *
 */
GeoNetwork.data.OpenSearchSuggestionReader = function(meta, recordType){
    meta = meta || {};
    GeoNetwork.data.OpenSearchSuggestionReader.superclass.constructor.call(this, meta, recordType || meta.fields);
    
};

Ext.extend(GeoNetwork.data.OpenSearchSuggestionReader, Ext.data.JsonReader, {
    /**
     * Index of the root element in the Array
     */
    rootId: undefined,
    
    /** private: method[readRecords] 
     *  :arg data: ``DOMElement or String or Array`` A document element or XHR response
     * string. As an alternative to fetching attributes data
     * from a remote source, an array of attribute objects can
     * be provided given that the properties of each attribute
     * object map to a provided field name. :return: ``Object``
     * A data block which is used by an ``Ext.data.Store`` as a
     * cache of ``Ext.data.Records``.
     *
     * Create a data block containing Ext.data.Records from an
     * OpenSearch suggestion document.
     */
    readRecords: function(o){
        this.arrayData = o;
        var i, j;
        var s = this.meta, sid = s ? Ext.num(s.idIndex, s.id) : null, recordType = this.recordType, fields = recordType.prototype.fields, records = [], success = true, v;
        
        // FIXME : should be from constructor
        this.rootId = 1;
        var root = (!this.rootId ? this.getRoot(o) : o[this.rootId]);
        
        for (i = 0, len = root.length; i < len; i++) {
            var n = root[i], values = {}, id = ((sid || sid === 0) &&
            n[sid] !== undefined &&
            n[sid] !== "" ? n[sid] : null);
            for (j = 0, jlen = fields.length; j < jlen; j++) {
                var f = fields.items[j], k = f.mapping !== undefined &&
                f.mapping !== null ? f.mapping : j;
                v = n[k] !== undefined ? n[k] : f.defaultValue;
                v = f.convert(v, n);
                values[f.name] = n;
            }
            var record = new recordType(values, id);
            record.json = n;
            records[records.length] = record;
        }
        
        var totalRecords = records.length;
        
        if (s.totalProperty) {
            v = parseInt(this.getTotal(o), 10);
            if (!isNaN(v)) {
                totalRecords = v;
            }
        }
        if (s.successProperty) {
            v = this.getSuccess(o);
            if (v === false || v === 'false') {
                success = false;
            }
        }
        return {
            success: true,
            records: records,
            totalRecords: records.length
        };
        
    }
    
});