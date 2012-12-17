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

Ext.namespace('GeoNetwork', 'GeoNetwork.wms');

/**
 * Class: GeoNetwork.wms.WMSLayerInfo
 *      Gets an OpenLayers.Layer.WMS with WMS information related
 *
 */

/**
 * Constructor: GeoNetwork.wms.WMSLayerInfo
 * Create an instance of GeoNetwork.wms.WMSLayerInfo
 *
 * Parameters:
 * config - {Object} A config object used to set the properties
 */
GeoNetwork.wms.WMSLayerInfo = function(config) {
    Ext.apply(this, config);
};

GeoNetwork.wms.WMSLayerInfo.prototype = {

    /**
     * APIProperty: layerParams
     * {Object} - a set of URL parameters to use on the OpenLayers.Layer.WMS
     *     layers
     */
    layerParams: {format:'image/png', transparent:'TRUE'},

    /**
     * APIProperty: layerOptions
     * {Object} - a set of options to use on the OpenLayers.Layer.WMS
     *     layers
     */
    layerOptions: {ratio:1, singleTile: true, isBaseLayer: false},

    /**
     * APIProperty: callback
     * {Function} callback function to use when the Layer is ready
     */
    callback: null,

    /**
     * APIProperty: scope
     * {Object} scope to use for the click and the callback function
     */
    scope: null,

    /**
     * APIProperty: layer
     * {Object} layer to get information from WMS Capabilities
     */
    layer: null,

    /**
     * APIMethod: loadWMS
     * load a WMS layer list and return the WMS layer requested through a callback
     * function
     *
     * Parameters:
     * onlineResource - {String} the online resource / base url of the WMS
     */
    loadWMS: function(onlineResource, layer) {
        this.layer = layer;

        var onlineResourceCheck = onlineResource.toLowerCase();

        var containsVersion = (onlineResourceCheck.indexOf('version=') > -1);
        var containsService = (onlineResourceCheck.indexOf('service=wms') > -1);
        var containsRequest = (onlineResourceCheck.indexOf('request=getcapabilities') > -1);
        var containsLanguage = (onlineResourceCheck.indexOf('language=') > -1);

        var params = {};

        if (!containsVersion) {
            params['version'] = GeoNetwork.OGCUtil.getProtocolVersion();
        }

        if (!containsService) {
            params['service'] = 'WMS';
        }

        if (!containsRequest) {
            params['request'] = 'GetCapabilities';
        }

        if (!containsLanguage) {
            params['language'] = GeoNetwork.OGCUtil.getLanguage();
        }

        var paramString = OpenLayers.Util.getParameterString(params);
        var separator = (onlineResource.indexOf('?') > -1) ? '&' : '?';
        onlineResource += separator + paramString;
        var req = Ext.Ajax.request({
            url: onlineResource,
            method: 'GET',
            failure: this.processFailure,
            success: this.processSuccess,
            timeout: 10000,
            //disableCaching: false,
            scope: this
        });
    },

    /**
     * Method: processSuccess
     * Process the WMS GetCapabilities response
     *
     * Parameters:
     * response - {Object} The XHR object which contains the parsed XML doc
     */
    processSuccess: function(response) {
        if (!this.parser) {
            this.parser = new OpenLayers.Format.WMSCapabilities();
        }
        var caps = this.parser.read(response.responseXML || response.responseText);
        var node;
        if (caps.capability) {
            node = this.processLayers(caps, caps.capability.nestedLayers);
        }
        // return the newly created Layer through a callback function
        Ext.callback(this.callback, this.scope, [node, this.layer]);
    },

    /**
     * Method: processFailure
     * Process the WMS GetCapabilities response when failed
     *
     * Parameters:
     * response - {Object} The XHR object which contains the parsed XML doc
     */
    processFailure: function(response) {
        Ext.callback(this.callback, this.scope, [null, this.layer]);
    },

    /**
     * APIMethod: createWMSLayer
     * create a OpenLayers.Layer.WMS from parameters
     *
     * Parameters:
     * layer - {Object} an object with the layer's properties
     * url - {String} the url at which the layer is available
     *
     * Returns: {<OpenLayers.Layer.WMS>} the layer created
     */
    createWMSLayer: function(layer, url) {
        return new OpenLayers.Layer.WMS(layer.title, url,
                OpenLayers.Util.extend({layers: layer.name}, this.layerParams),
                OpenLayers.Util.extend({minScale: layer.minScale,
                    queryable: layer.queryable, maxScale: layer.maxScale,
                    description: layer["abstract"],
                    keywords: layer.keywords,
                    metadataURLs: layer.metadataURLs,
                    metadataURL: layer.metadataURL,
                    llbbox: layer.llbbox},
                        this.layerOptions));
    },

    /**
     * Method: processLayer
     * Recursive function to process a layer and their childLayers in the
     * capabilities doc, searching for requested layer
     *
     * Parameters:
     * caps - {Object} capabilities from the WMSCapabilities parser
     * layers - {Object} list of layers objects from the WMSCapabilities parser
     */
    processLayers: function(caps, layers) {
        var findedLayer = null;

        for (var i = 0, len = layers.length; i < len; ++i) {
            var lr = layers[i];

            try {
                var layerName = lr.name.split(",");
                if (layerName.indexOf(this.layer.params.LAYERS) != -1) {
                    findedLayer = this.createWMSLayer(lr, caps.service.href);
                    break;
                }
            } catch(e) {
            }

            if (typeof(lr.nestedLayers) != "undefined") {
                findedLayer = this.processLayers(caps, lr.nestedLayers);
                if (findedLayer != null) break;
            }

        }	// for

        return findedLayer;
    }
};