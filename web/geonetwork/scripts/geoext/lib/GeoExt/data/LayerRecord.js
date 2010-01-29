/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/** api: (define)
 *  module = GeoExt.data
 *  class = LayerRecord
 *  base_link = `Ext.data.Record <http://extjs.com/deploy/dev/docs/?class=Ext.data.Record>`_
 */
Ext.namespace("GeoExt.data");

/** api: constructor
 *  .. class:: LayerRecord
 *  
 *      A record that represents an ``OpenLayers.Layer``. This record
 *      will always have at least the following fields:
 *
 *      * layer ``OpenLayers.Layer``
 *      * title ``String``
 */
GeoExt.data.LayerRecord = Ext.data.Record.create([
    {name: "layer"},
    {name: "title", type: "string", mapping: "name"}
]);

/** api: method[clone]
 *  :param id: ``String`` (optional) A new Record id.
 *  :return: ``GeoExt.data.LayerRecord`` A new layer record.
 *  
 *  Creates a clone of this LayerRecord. 
 */
GeoExt.data.LayerRecord.prototype.clone = function(id) { 
    var layer = this.get("layer") && this.get("layer").clone(); 
    return new this.constructor( 
        Ext.applyIf({layer: layer}, this.data), 
        id || layer.id
    );
}; 

/** api: classmethod[create]
 *  :param o: ``Array`` Field definition as in ``Ext.data.Record.create``. Can
 *      be omitted if no additional fields are required.
 *  :return: ``Function`` A specialized :class:`GeoExt.data.LayerRecord`
 *      constructor.
 *  
 *  Creates a constructor for a :class:`GeoExt.data.LayerRecord`, optionally
 *  with additional fields.
 */
GeoExt.data.LayerRecord.create = function(o) {
    var f = Ext.extend(GeoExt.data.LayerRecord, {});
    var p = f.prototype;

    p.fields = new Ext.util.MixedCollection(false, function(field) {
        return field.name;
    });

    GeoExt.data.LayerRecord.prototype.fields.each(function(f) {
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
