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

Ext.namespace('GeoNetwork', 'GeoNetwork.MapStateManager');

GeoNetwork.MapStateManager = function() {
    // private
    var LAYER_SEP = "@";
    var DATA_FIELD_SEP = "|";

    var COOKIE_MAPLAYERS = "maplayers";
    var COOKIE_MAPEXTENT = "mapextent";

    var mapLayers = "";
    var mapExtent = "";

    var getCookieValue = function(cookiename) {
        var cookietext = "";
        var cookieStart = document.cookie.indexOf(cookiename+"=");
        if (cookieStart!=-1) {
            cookieStart += cookiename.length+1;
            var cookieEnd=document.cookie.indexOf(";",cookieStart);
            if (cookieEnd==-1) {
                cookieEnd=document.cookie.length;
            }
            cookietext = document.cookie.substring(cookieStart,cookieEnd);
        }

        return cookietext;
    };

    var storeCookieValue = function(cookiename, value) {
        var cookietext = cookiename+"="+value;

        var exdate=new Date();
        exdate.setDate(exdate.getDate()+1);
        cookietext += ";expires="+exdate.toGMTString();

        // == write the cookie ==
        document.cookie=cookietext;
    };


    var processValue = function(value, defaultValue) {
        if (typeof(value)!="undefined") {
            return value;
        } else {
            if (typeof(defaultValue)!="undefined") {
                return defaultValue;
            } else {
                return '';
            }
        }
    };

    // public
    return {
        /**
         * APIMethod: storeMapLayersState
         * Stores map layers state in cookies
         *
         * Parameters:
         * map - {<OpenLayers.Map>}
         */
        storeMapLayersState: function(map) {
            var layers = map.layers;

            var cookietext = '';

            for(var i = 0; i < layers.length; i++) {
                if ((!layers[i].isBaseLayer) && (layers[i].displayInLayerSwitcher)) {
                    var params = Object.toJSON(layers[i].params);
                    var options = Object.toJSON(layers[i].options);
                    var opacity = processValue(layers[i].opacity, 1);

                    var li = layers[i].name+DATA_FIELD_SEP+layers[i].url+DATA_FIELD_SEP+params+
                    			DATA_FIELD_SEP+options+DATA_FIELD_SEP+opacity;

                    if (cookietext.length > 0) cookietext = cookietext+LAYER_SEP;
                    cookietext = cookietext+li;
                }
            }

            storeCookieValue(COOKIE_MAPLAYERS, cookietext);
        },

        stoteMapExtextState: function(map) {
             // Map properties
            var mapExtent = map.getExtent();
            var cookietext = mapExtent.left+DATA_FIELD_SEP+mapExtent.bottom+DATA_FIELD_SEP+mapExtent.right+DATA_FIELD_SEP+mapExtent.top;
            storeCookieValue(COOKIE_MAPEXTENT, cookietext);
        },

        /**
         * APIMethod: loadMapState
         * Load map state from cookies
         *
         * Parameters:
         * map - {<OpenLayers.Map>}
         * xml - {String} The Web Map Context XML string
         */
        loadMapState: function() {
             // Get layers from cookie
            var cookietext = getCookieValue(COOKIE_MAPLAYERS);
            if (cookietext != '') {
              mapLayers = cookietext;
            }

            // Map properties
            cookietext = getCookieValue(COOKIE_MAPEXTENT);
            if (cookietext != '') {
              mapExtent = cookietext;
            }
        },

        /**
         * APIMethod: applyMapState
         * Apply map state from cookies
         *
         * Parameters:
         * map - {<OpenLayers.Map>}
         * xml - {String} The Web Map Context XML string
         */
        applyMapState: function(map) {
            // Map extent
            if (mapLayers != "") {
                var layerCookieList = mapLayers.split(LAYER_SEP);

                for(var i = 0; i < layerCookieList.length; i++) {
                    var layerInfo = layerCookieList[i].split(DATA_FIELD_SEP);

                    if (layerInfo.length == 5) {
                    	var name = layerInfo[0];
                        var url = layerInfo[1];
                        var params = layerInfo[2].evalJSON(true);
                        var options = layerInfo[3].evalJSON(true);
                        var opacity = layerInfo[4];

                    	var ol_layer = new OpenLayers.Layer.WMS(name, url,
                            params, options);

                        if (!GeoNetwork.OGCUtil.layerExistsInMap(ol_layer, map)) {
                            if (opacity) ol_layer.setOpacity(parseFloat(opacity));
                            map.addLayer(ol_layer);
                        }
                    }
                }
            }

            // Map properties
            if (mapExtent) {
                var mapExtentValues = mapExtent.split(DATA_FIELD_SEP);
                if (mapExtentValues.length == 4) {
                    var mapExtentOL = new OpenLayers.Bounds(mapExtentValues[0], mapExtentValues[1], mapExtentValues[2], mapExtentValues[3]);
                    map.zoomToExtent(mapExtentOL);
                }
            }
        }
    };
};

GeoNetwork.MapStateManager = new GeoNetwork.MapStateManager();