/*
 * Copyright (C) 2009 GeoNetwork
 *
 * This file is part of GeoNetwork
 *
 * GeoNetwork is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoNetwork is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoNetwork.  If not, see <http://www.gnu.org/licenses/>.
 */

Ext.namespace('GeoNetwork');

/**
 * Class: GeoNetwork.SingletonWindowManager
 *      Singleton window manager for windows
 *
 */
GeoNetwork.SingletonWindowManager = function() {
    // private
    var windowsList = new Object();    // Associative array 
    var hiddenWindows = new Array();

    // public
    return {
        registerWindow: function(id, classz, configz) {
            var window1 = new classz(configz);

            windowsList[id] = {windowz: window1, classz: classz, configz: configz};
        },

        getWindow: function(id) {
            if (windowsList[id]) {
                return windowsList[id].windowz;
            } else {
                return null;
            }
        },

        showWindow: function(id) {
            if (windowsList[id]) {
                if (Ext.isEmpty(Ext.getCmp(id))) {
                	var w =  windowsList[id];

                    var ww = new w.classz(w.configz);

                    windowsList[id] = {windowz: ww, classz: w.classz, configz: w.configz};
                }
                windowsList[id].windowz.show();
                return true;
            } else {
                return false;
            }
        },

        hideAllWindows: function() {
            for(key in windowsList) {
                if (windowsList[key].windowz.isVisible()) {
                    windowsList[key].windowz.setVisible(false);
                    hiddenWindows[hiddenWindows.length] = key;
                }
            }                 
        },

        restoreHiddenWindows: function() {
			for (var index = 0, len = hiddenWindows.length; index < len; ++index) {
          		windowsList[hiddenWindows[index]].windowz.setVisible(true);
          	}
          	hiddenWindows = new Array();
        }


    };
};

GeoNetwork.WindowManager = new GeoNetwork.SingletonWindowManager();
