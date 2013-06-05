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

Ext.namespace('GeoNetwork', 'GeoNetwork.OGCUtil');

GeoNetwork.OGCUtil.getProtocolVersion = function() {
    return "1.3.0";
};

GeoNetwork.OGCUtil.getLanguage = function() {
    return catalogue.lang; // FIXME : global
};
/**
 * APIFunction: GeoNetwork.OGCUtil.ensureProperUrlEnd
 * Function to make sure that the last char of the URL is either a ?
 * or a &, to make adding of parameters safe
 *
 * Parameters:
 * url - {String} the url to make safe
 *
 * Returns:
 * {String} the url made safe
*/
GeoNetwork.OGCUtil.ensureProperUrlEnd = function (url) {
    if (url.indexOf("?") == -1) {
        url += "?";
    }
    else {
        // url can be like http://.../...?var=value, make sure that it will
        // end correctly
        var lastChar = url.substring(url.length-1);
        if (lastChar != "&" && lastChar !="?") {
            url+="&";
        }
    }
    return url;
};

/**
 * APIMethod: GeoNetwork.OGCUtil.reprojectMap
 * Reproject a map and all its layers
 *
 * Parameters:
 * map - {<OpenLayers.Map>} the map object to reproject
 * newProjection - {<OpenLayers.Projection>} the projection to transform to
 * noZoom - {Boolean} if true do not zoom the map
*/
GeoNetwork.OGCUtil.reprojectMap = function(map, newProjection, noZoom) {
    // check if the projection has actually changed
    if (map.projection != newProjection.projCode) {
        map.baseLayer.options.scales = map.scales;
        var oldProjection = map.getProjectionObject();
        map.projection = newProjection.projCode;
        if (newProjection.getUnits() === null) {
            map.units = 'degrees';
        } else {
            map.units = newProjection.getUnits();
        }
        // make sure the cursor pos control shows coordinates with the
        // right precision
        var cursorPos = null;
        if (map.getControlsByClass('GeoNetwork.Control.CursorPos').length > 0) {
            cursorPos = map.getControlsByClass('GeoNetwork.Control.CursorPos')[0];
        }
        if (map.units == 'm' && cursorPos !== null) {
            cursorPos.numdigits = 0;
        } else if (map.units == 'degrees' && cursorPos !== null) {
            cursorPos.numdigits = 4;
        }

        map.maxExtent = map.maxExtent.transform(oldProjection,
            newProjection);
        map.baseLayer.extent = map.maxExtent;

        var bounds = map.getExtent().transform(oldProjection,
            newProjection);

        for (var i=0; i< map.layers.length; i++) {
            var layer = map.layers[i];
            layer.units = map.units;
            layer.projection = newProjection;
            layer.maxExtent = map.maxExtent;

            if (layer.isBaseLayer) {
                layer.initResolutions();
            } else {
                // just copy it from the baselayer
                layer.resolutions =  map.baseLayer.resolutions;
                layer.minResolution = map.baseLayer.minResolution;
                layer.maxResolution = map.baseLayer.maxResolution;
            }
            if (layer instanceof OpenLayers.Layer.Vector) {
                for (var j=0; j < layer.features.length; j++) {
                    var feature = layer.features[j];
                    if (feature.geometry.projection != map.projection) {
                        feature.geometry.transform(
                            new OpenLayers.Projection(
                                feature.geometry.projection),
                            map.getProjectionObject() );
                        feature.geometry.projection  = map.projection;
                    }
                }
            }
        }
        if (!noZoom) {
            map.zoomToExtent(bounds);
        }
    }
};

/**
 * APIFunction: GeoNetwork.OGCUtil.layerExistsInMap
 * Check if the layer already exists in the map, using the SERVICE,
 * LAYERS params and url
 *
 * Parameters:
 * layer - {<OpenLayers.Layer.WMS>} the layer to be checked
 * map - {<OpenLayers.Map>} the map object
 *
 * Returns:
 * {Boolean} false if the layer does not exist
 * {<OpenLayers.Layer.WMS>} the layer which has been found
*/
GeoNetwork.OGCUtil.layerExistsInMap = function (layer, map) {
    var layerExists = false;
    for (var i=0, len=map.layers.length; i<len; i++) {
        if (map.layers) {
            var lr = map.layers[i];
            if (lr.params) {
                try {
                    var layers = lr.params.LAYERS.split(",");
                    if (((layers.indexOf(layer.params.LAYERS) != -1) || (lr.params.LAYERS == layer.params.LAYERS)) &&
                      lr.params.SERVICE == layer.params.SERVICE &&
                        lr.url == layer.url) {
                        layerExists = lr;
                        break;
                    }
                } catch(e) {}
            }
        }
    }
    return layerExists;
};
 
