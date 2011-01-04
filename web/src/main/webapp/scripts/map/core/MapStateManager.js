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
                    var li = layers[i].name+DATA_FIELD_SEP+layers[i].url+DATA_FIELD_SEP+layers[i].params.LAYERS+
                                DATA_FIELD_SEP+processValue(layers[i].opacity, 1)+DATA_FIELD_SEP+processValue(layers[i].queryable, false)+
                                DATA_FIELD_SEP+processValue(layers[i].llbbox)+DATA_FIELD_SEP+processValue(layers[i].styles)+
                                DATA_FIELD_SEP+processValue(layers[i].dimensions)+DATA_FIELD_SEP+processValue(layers[i].visibility, true)+
                                DATA_FIELD_SEP+processValue(layers[i].projection.projCode, "EPSG:4326")+DATA_FIELD_SEP+processValue(layers[i].units)+
                                DATA_FIELD_SEP+processValue(layers[i].metadata_id);
                                
                    if (cookietext.length > 0) cookietext = cookietext+LAYER_SEP;                    
                    cookietext = cookietext+li;
                }
            }     
					var GNCookie = Ext.state.Manager.getProvider();
					GNCookie.set(COOKIE_MAPLAYERS, {mapLayers: cookietext});
        },

        stoteMapExtextState: function(map) {
             // Map properties
            var mapExtent = map.getExtent();
            var cookietext = mapExtent.left+DATA_FIELD_SEP+mapExtent.bottom+DATA_FIELD_SEP+mapExtent.right+DATA_FIELD_SEP+mapExtent.top;
					var GNCookie = Ext.state.Manager.getProvider();
					GNCookie.set(COOKIE_MAPEXTENT, {mapExtent: cookietext});
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
						var GNCookie = Ext.state.Manager.getProvider();
            var cookie =GNCookie.get(COOKIE_MAPLAYERS);
            if (cookie && cookie.mapLayers != '') {
              mapLayers = cookie.mapLayers;            
            }
                     
            // Map properties
						var GNCookie = Ext.state.Manager.getProvider();
            cookie =GNCookie.get(COOKIE_MAPEXTENT);
            if (cookie && cookie.mapExtent != '') {
              mapExtent = cookie.mapExtent;
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
                    
                    if (layerInfo.length == 12) {
                        var name = layerInfo[0];
                        var url = layerInfo[1];
                        var layer = layerInfo[2];
                        var opacity = layerInfo[3];
                        var queryable = (layerInfo[4] == 'true');
                        var llbbox = layerInfo[5];
                        var styles = layerInfo[6];
                        var dimensions = layerInfo[7];
                        var visibility = (layerInfo[8] == 'true');
                        var projCode = layerInfo[9];
                        var units = layerInfo[10];
                        var metadata_id = layerInfo[11];
                               
                        var ol_layer = new OpenLayers.Layer.WMS(name, url,
                            {layers: layer, format: 'image/png', transparent: 'TRUE'},
                            {queryable: queryable, singleTile: true, ratio: 1, buffer: 0, 
                                projection: projCode, units: units, transitionEffect: 'resize', 
                                metadata_id: metadata_id, llbbox: llbbox, styles: styles, dimensions: dimensions, visibility: visibility} );
                                                
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
