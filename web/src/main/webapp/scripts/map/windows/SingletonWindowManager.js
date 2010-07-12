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
 * Uses:
 *  - {Prototype.Hash}
 */
GeoNetwork.SingletonWindowManager = function() {
    // private
    var windows = new Hash();

    var hiddenWindows = new Array();

    // public
    return {
        registerWindow: function(id, classz, configz) {
            var window1 = new classz(configz);

            windows.set(id, {windowz: window1, classz: classz, configz: configz});
        },

        getWindow: function(id) {
            if (windows.get(id)) {
                return windows.get(id).windowz;
            } else {
                return null;
            }
        },

        showWindow: function(id) {
            if (windows.get(id)) {
                if (Ext.isEmpty(Ext.getCmp(id))) {
                	var w =  windows.get(id);

                    var ww = new w.classz(w.configz);

                    windows.set(id, {windowz: ww, classz: w.classz, configz: w.configz});
                }
                windows.get(id).windowz.show();
                return true;
            } else {
                return false;
            }
        },

        hideAllWindows: function() {
            windows.each(function(data) {
                if (data.value.windowz.isVisible()) {
                    data.value.windowz.setVisible(false);
                    hiddenWindows[hiddenWindows.length] = data.key;
                }
            })

        },

        restoreHiddenWindows: function() {
						for (var index = 0, len = hiddenWindows.length; index < len; ++index) {
          		windows.get(hiddenWindows[index]).windowz.setVisible(true);
          	}
          	hiddenWindows = new Array();
        }


    };
};

GeoNetwork.WindowManager = new GeoNetwork.SingletonWindowManager();
