/*
 * Ext JS Library 0.30
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

Ext.air.App = function() {
    return {
        launchOnStartup: function(launch) {
            air.NativeApplication.nativeApplication.startAtLogin = !!launch;
        },
        getActiveWindow: function() {
            return air.NativeApplication.activeWindow;
        }
    };
}();