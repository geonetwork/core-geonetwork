/*
 * Ext JS Library 0.30
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

/**
 * @class Ext.air.Clipboard
 * @singleton
 * Allows you to manipulate the native system clipboard and handle various formats.
 * This class is essentially a passthrough to air.Clipboard.generalClipboard at this
 * time, but may get more Ext-like functions in the future.
 *
 * The Clipboard has different types which it can hold:
 * CONSTANT - value
 * air.ClipboardFormats.TEXT_FORMAT - air:text
 * air.ClipboardFormats.HTML_FORMAT - air:html
 * air.ClipboardFormats.RICH_TEXT_FORMAT - air:rtf
 * air.ClipboardFormats.URL_FORMAT - air:url
 * air.ClipboardFormats.FILE_LIST_FORMAT - air:file list
 * air.ClipboardFormats.BITMAP_FORMAT - air:bitmap
 */
Ext.air.Clipboard = function() {
    var clipboard = air.Clipboard.generalClipboard;
    
    return {
        /**
         * Determine if there is any data in a particular format clipboard.
         * @param {String} format Use the air.ClipboardFormats CONSTANT or the string value
         */
        hasData: function(format) {
            return clipboard.hasFormat(format);
        },
        /**
         * Set the data for a particular format clipboard.
         * @param {String} format Use the air.ClipboardFormats CONSTANT or the string value
         * @param {Mixed} data Data to set 
         */
        setData: function(format, data) {
            clipboard.setData(format, data);
        },
        /**
         * Set the data handler for a particular format clipboard.
         * @param {String} format Use the air.ClipboardFormats CONSTANT or the string value
         * @param {Function} fn The function to evaluate when getting the clipboard data
         */
        setDataHandler: function(format, fn) {
            clipboard.setDataHandler(format, fn);
        },
        /**
         * Get the data for a particular format.
         * @param {String} format Use the air.ClipboardFormats CONSTANT or the string value
         * @param {String} transferMode 
         */
        getData: function(format, transferMode) {
            clipboard.getData(format, transferMode);
        },
        /**
         * Clear the clipboard for all formats.
         */
        clear: function() {
            clipboard.clear();
        },
        /**
         * Clear the data for a particular format.
         * @param {String} format Use the air.ClipboardFormats CONSTANT or the string value
         */
        clearData: function(format) {
            clipboard.clearData(format);
        }
    };
}();