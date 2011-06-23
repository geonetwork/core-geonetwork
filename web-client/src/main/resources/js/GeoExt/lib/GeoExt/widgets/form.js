/**
 * Copyright (c) 2008-2010 The Open Source Geospatial Foundation
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
 *  :param wildcard: ``Integer`` Determines the wildcard behaviour of like
 *      queries. This behaviour can either be: none, prepend, append or both.
 *
 *  :return: ``OpenLayers.Filter``
 *
 *  Create an {OpenLayers.Filter} object from a {Ext.form.BasicForm}
 *      or a {Ext.form.FormPanel} instance.
 */
GeoExt.form.toFilter = function(form, logicalOp, wildcard) {
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

        if (type === OpenLayers.Filter.Comparison.LIKE) {
            switch(wildcard) {
                case GeoExt.form.ENDS_WITH:
                    value = '.*' + value;
                    break;
                case GeoExt.form.STARTS_WITH:
                    value += '.*';
                    break;
                case GeoExt.form.CONTAINS:
                    value = '.*' + value + '.*';
                    break;
                default:
                    // do nothing, just take the value
                    break;
            }
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

GeoExt.form.ENDS_WITH = 1;
GeoExt.form.STARTS_WITH = 2;
GeoExt.form.CONTAINS = 3;

/** private: function[recordToField]
 *  :param record: ``Ext.data.Record``, typically from an attributeStore
 *
 *  :return: ``Object`` An object literal with a xtype property, use
 *  ``Ext.ComponentMgr.create`` (or ``Ext.create`` in Ext 3) to create
 *  an ``Ext.form.Field`` from this object.
 *
 *  This function can be used to create an ``Ext.form.Field`` from
 *  an ``Ext.data.Record`` containing name, type, restriction and
 *  label fields.
 */
GeoExt.form.recordToField = function(record) {
    var type = record.get("type");

    if(typeof type === "object" && type.xtype) {
        // we have an xtype'd object literal in the type
        // field, just return it
        return type;
    }

    var field;

    var name = record.get("name");
    var label = record.get("label");
    var restriction = record.get("restriction") || {};

    // use name for label if label isn't defined in the record
    if(label == null) {
        label = name;
    }

    type = type.split(":").pop(); // remove ns prefix

    var r = GeoExt.form.recordToField.REGEXES;

    if(type.match(r["text"])) {
        var maxLength = restriction["maxLength"] !== undefined ?
            parseFloat(restriction["maxLength"]) : undefined;
        var minLength = restriction["minLength"] !== undefined ?
            parseFloat(restriction["minLength"]) : undefined;
        field = {
            xtype: "textfield",
            name: name,
            fieldLabel: label,
            maxLength: maxLength,
            minLength: minLength
        };
    } else if(type.match(r["number"])) {
        var maxValue = restriction["maxInclusive"] !== undefined ?
            parseFloat(restriction["maxInclusive"]) : undefined;
        var minValue = restriction["minInclusive"] !== undefined ?
            parseFloat(restriction["minInclusive"]) : undefined;
        field = {
            xtype: "numberfield",
            name: name,
            fieldLabel: label,
            maxValue: maxValue,
            minValue: minValue
        };
    } else if(type.match(r["boolean"])) {
        field = {
            xtype: "checkbox",
            name: name,
            boxLabel: label
        };
    } else if(type.match(r["date"])) {
        field = {
            xtype: "datefield",
            fieldLabel: label,
            name: name
        };
    }

    return field;
}

/** private: constant[REGEXES]
  *  ``Object`` Regular expressions for determining what type
  *  of field to create from an attribute record.
  */
GeoExt.form.recordToField.REGEXES = {
    "text": new RegExp(
        "^(text|string)$", "i"
    ),
    "number": new RegExp(
        "^(number|float|decimal|double|int|long|integer|short)$", "i"
    ),
    "boolean": new RegExp(
        "^(boolean)$", "i"
    ),
    "date": new RegExp(
        "^(dateTime)$", "i"
    )
};
