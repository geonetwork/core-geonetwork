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

Ext.namespace('GeoNetwork', 'GeoNetwork.WMC');

GeoNetwork.WMC = function() {
    
    // public
    return {
        /**
         * APIMethod: loadWmc
         * Load in a Web Map Context document into the map.
         *
         * Parameters:
         * map - {<OpenLayers.Map>}
         * xml - {String} The Web Map Context XML string
         */
        loadWmc: function(map, xml) {
            try {
                var layers = map.layers;
                // remove all previous layers
                for(var i = layers.length-1; i > 0; i--) {
                    if (!layers[i].isBaseLayer) {
                        map.removeLayer(layers[i]);
                    }
                }
                Ext.getCmp('toctree').getSelectionModel().clearSelections();

                var format = new OpenLayers.Format.WMC({'layerOptions': {buffer: 0}});
                map = format.read(xml, {map: map});
            } catch(err) {
                Ext.MessageBox.alert(OpenLayers.i18n("selectWMCFile.errorLoadingWMC"));
            }
        },

        /**
         * APIMethod: mergeWmc
         * Load in a Web Map Context document into the map and merge it
         * with existing layers.
         *
         * Parameters:
         * map - {<OpenLayers.Map>}
         * xml - {String} The Web Map Context XML string
         */
        mergeWmc: function(map, xml) {
            try {
                var format = new OpenLayers.Format.WMC({'layerOptions': {buffer: 0}});
                map = format.read(xml, {map: map});
            } catch(err) {
                Ext.MessageBox.alert(OpenLayers.i18n("selectWMCFile.errorLoadingWMC"));
            }
        },

        /**
         * APIMethod: saveContext
         * Save the map as a Web Map Context document (uses server-side Java)
         *
         * Parameters:
         * map - {<OpenLayers.Map>}
         */
        saveContext: function(map) {
            var wmc = new OpenLayers.Format.WMC();

            OpenLayers.Request.POST({
                // TODO: there were problems with relative 
                // urls, should we change this?
                url:  "../../wmc/create.wmc",
                data: wmc.write(map),
                success: this.onSaveContextSuccess,
                failure: this.onSaveContextFailure
            });
        },

        /**
         * Method: onSaveContextSuccess
         * Success AJAX handler for saving WMC.
         *
         * Parameters:
         * response - {Object} The response object
         */
        onSaveContextSuccess: function(response) {
            var json = response.responseText;
            var o = Ext.decode(json);
            if (o.success) {
                window.location = o.url;
            } else {
                this.onSaveContextFailure();
            }
        },

        /**
         * Method: onSaveContextFailure
         * Failure AJAX handler for saving WMC.
         *
         * Parameters:
         * response - {Object} The response object
         */
        onSaveContextFailure: function(form, action) {
            Ext.MessageBox.show({icon: Ext.MessageBox.ERROR,
                title: OpenLayers.i18n("saveWMCFile.windowTitle"), msg:
                OpenLayers.i18n("saveWMCFile.errorSaveWMC"),
                buttons: Ext.MessageBox.OK});
        }

    };
};

GeoNetwork.WMCManager = new GeoNetwork.WMC();
