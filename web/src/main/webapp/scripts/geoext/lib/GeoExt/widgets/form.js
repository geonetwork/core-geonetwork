/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

Ext.namespace("GeoExt.form");

/** private: function[toFilter]
 *  :param form: ``Ext.form.BasicForm|Ext.form.FormPanel``
 *  :param logicalOp: ``String`` Either ``OpenLayers.Filter.Logical.AND`` or
 *      ``OpenLayers.Filter.Logical.OR``, set to
 *      ``OpenLayers.Filter.Logical.AND`` if null or undefined
 *      
 *  :return: ``OpenLayers.Filter``
 *  
 *  Create an {OpenLayers.Filter} object from a {Ext.form.BasicForm}
 *      or a {Ext.form.FormPanel} instance.
 */
GeoExt.form.toFilter = function(form, logicalOp) {
    if(form instanceof Ext.form.FormPanel) {
        form = form.getForm();
    }
    var filters = [], values = form.getValues(false);
    for(var prop in values) {
        var s = prop.split("__");

        var value = values[prop], type;

        if(s.length > 1 && 
           (type = GeoExt.form.toFilter.FILTER_MAP[s[1]]) !== undefined) {
            prop = s[0];
        } else {
            type = OpenLayers.Filter.Comparison.EQUAL_TO;
        }

        filters.push(
            new OpenLayers.Filter.Comparison({
                type: type,
                value: value,
                property: prop
            })
        );
    }

    return new OpenLayers.Filter.Logical({
        type: logicalOp || OpenLayers.Filter.Logical.AND,
        filters: filters
    });
};

/** private: constant[FILTER_MAP]
 *  An object mapping operator strings as found in field names to
 *      ``OpenLayers.Filter.Comparison`` types.
 */
GeoExt.form.toFilter.FILTER_MAP = {
    "eq": OpenLayers.Filter.Comparison.EQUAL_TO,
    "ne": OpenLayers.Filter.Comparison.NOT_EQUAL_TO,
    "lt": OpenLayers.Filter.Comparison.LESS_THAN,
    "le": OpenLayers.Filter.Comparison.LESS_THAN_OR_EQUAL_TO,
    "gt": OpenLayers.Filter.Comparison.GREATER_THAN,
    "ge": OpenLayers.Filter.Comparison.GREATER_THAN_OR_EQUAL_TO,
    "like": OpenLayers.Filter.Comparison.LIKE
};
