/*
 * Ext JS Library 0.30
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

/*
 * Useful debugging function similar to console.dir for air.
 * This was ported from AS3 and the original code came from Adobe's help system.
 */
Ext.air.dir = function (obj, indent) {
    indent = indent || 0;
    var indentString = "";    
    
    for (var i = 0; i < indent; i++) {
        indentString += "\t";
    }
    
    var val;
    for (var prop in obj) {
        val = obj[prop];
        if (typeof(val) == "object") {
            air.trace(indentString + " " + prop + ": [Object]");
            Ext.air.dir(val, indent + 1);
        } else {
            air.trace(indentString + " " + prop + ": " + val);
        }
    }
};