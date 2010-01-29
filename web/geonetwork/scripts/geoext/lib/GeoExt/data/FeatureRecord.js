/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: (define)
 *  module = GeoExt.data
 *  class = FeatureRecord
 *  base_link = `Ext.data.Record <http://extjs.com/deploy/dev/docs/?class=Ext.data.Record>`_
 */
Ext.namespace("GeoExt.data");

/** api: constructor
 *  .. class:: FeatureRecord
 *  
 *      A record that represents an ``OpenLayers.Feature.Vector``. This record
 *      will always have at least the following fields:
 *
 *      * feature ``OpenLayers.Feature.Vector``
 *      * state ``String``
 *      * fid ``String``
 *
 */
GeoExt.data.FeatureRecord = Ext.data.Record.create([
    {name: "feature"}, {name: "state"}, {name: "fid"}
]);

/** api: classmethod[create]
 *  :param o: ``Array`` Field definition as in ``Ext.data.Record.create``. Can
 *      be omitted if no additional fields are required.
 *  :return: ``Function`` A specialized :class:`GeoExt.data.FeatureRecord`
 *      constructor.
 *  
 *  Creates a constructor for a :class:`GeoExt.data.FeatureRecord`, optionally
 *  with additional fields.
 */
GeoExt.data.FeatureRecord.create = function(o) {
    var f = Ext.extend(GeoExt.data.FeatureRecord, {});
    var p = f.prototype;

    p.fields = new Ext.util.MixedCollection(false, function(field) {
        return field.name;
    });

    GeoExt.data.FeatureRecord.prototype.fields.each(function(f) {
        p.fields.add(f);
    });

    if(o) {
        for(var i = 0, len = o.length; i < len; i++){
            p.fields.add(new Ext.data.Field(o[i]));
        }
    }

    f.getField = function(name) {
        return p.fields.get(name);
    };

    return f;
};
