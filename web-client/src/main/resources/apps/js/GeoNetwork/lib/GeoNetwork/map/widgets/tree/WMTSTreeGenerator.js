/*
 * Copyright (C) 2012 GeoNetwork
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

Ext.namespace('GeoNetwork', 'GeoNetwork.tree');

/**
 * Class: GeoNetwork.tree.WMTSTreeGenerator WMSTreeGenerator generates an
 * Ext.tree.TreeNode representing the layer list of a OGC:WMTS Web Mapping
 * Service.
 */

/**
 * Constructor: GeoNetwork.tree.WMTSTreeGenerator Create an instance of
 * GeoNetwork.tree.WMTSTreeGenerator
 * 
 * Parameters: config - {Object} A config object used to set the properties
 */
GeoNetwork.tree.WMTSTreeGenerator = function(config) {
    Ext.apply(this, config);
};

GeoNetwork.tree.WMTSTreeGenerator.prototype = {

    /**
     * APIProperty: layerParams {Object} - a set of URL parameters to use on the
     * OpenLayers.Layer.WMS layers
     */
    layerParams : {
        format : 'image/png',
        transparent : 'TRUE'
    },

    /**
     * APIProperty: layerOptions {Object} - a set of options to use on the
     * OpenLayers.Layer.WMS layers
     */
    layerOptions : {
        ratio : 1,
        singleTile : true,
        isBaseLayer : false
    },

    /**
     * APIProperty: click {Function} click function to use when clicked on the
     * child nodes
     */
    click : null,

    /**
     * APIProperty: callback {Function} callback function to use when the
     * TreeNode is ready
     */
    callback : null,

    /**
     * APIProperty: scope {Object} scope to use for the click and the callback
     * function
     */
    scope : null,

    /**
     * APIMethod: loadWMS load a WMS layer list and return a TreeNode through a
     * callback function
     * 
     * Parameters: onlineResource - {String} the online resource / base url of
     * the WMS
     */
    loadWMS : function(onlineResource) {
        var containsVersion = (onlineResource.indexOf('version=') > -1);

        var onlineResourceCheck = onlineResource.toLowerCase();

        var containsVersion = (onlineResourceCheck.indexOf('version=') > -1);
        var containsService = (onlineResourceCheck.indexOf('service=wmts') > -1);
        var containsRequest = (onlineResourceCheck
                .indexOf('request=getcapabilities') > -1);
        var containsLanguage = (onlineResourceCheck.indexOf('language=') > -1);

        var params = {};

        if (!containsVersion) {
            params['version'] = GeoNetwork.OGCUtil.getProtocolVersion();
        }

        if (!containsService) {
            params['service'] = 'WMTS';
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
            url : onlineResource,
            method : 'GET',
            failure : this.processFailure,
            success : this.processSuccess,
            disableCaching : false,
            scope : this
        });
    },

    /**
     * Method: processSuccess Process the WMS GetCapabilities response
     * 
     * Parameters: response - {Object} The XHR object which contains the parsed
     * XML doc
     */
    processSuccess : function(response) {
        if (!this.parser) {
            this.parser = new OpenLayers.Format.WMTSCapabilities();
        }
        var caps = this.parser.read(response.responseXML
                || response.responseText);

        if (!caps.service) {
            caps.service = {};
        }
        if (!caps.service.accessConstraints) {
            caps.service.accessContraints = '';
        }

        this.layerParams.VERSION = caps.version;
        var parentNode =  new Ext.tree.TreeNode({
                text : caps.serviceIdentification.title
            });
        if (caps.contents) {
            for ( var i = 0, len = caps.contents.layers.length; i < len; ++i) {
                var layer = caps.contents.layers[i];

                var node = this
                        .addLayer(layer, caps.serviceMetadataUrl, parentNode, caps);
                this.processLayer(layer, caps.serviceMetadataUrl, node, caps);
            }
        }
        // return the newly created TreeNode through a callback function
        Ext.callback(this.callback, this.scope, [ parentNode, caps ]);
    },

    /**
     * Method: processFailure Process the WMS GetCapabilities response when
     * failed
     * 
     * Parameters: response - {Object} The XHR object which contains the parsed
     * XML doc
     */
    processFailure : function(response) {
        Ext.callback(this.callback, this.scope, null);
    },

    /**
     * APIMethod: createWMSLayer create a OpenLayers.Layer.WMS from parameters
     * 
     * Parameters: layer - {Object} an object with the layer's properties url -
     * {String} the url at which the layer is available
     * 
     * Returns: {<OpenLayers.Layer.WMS>} the layer created
     */
    createWMSLayer : function(layer, url, capabilities) {
        var layermatrixSet = app.mapApp.getMap().projection;
        var projection = app.mapApp.getMap().projection;

        var tileMS = capabilities.contents.tileMatrixSets;
        for (key in tileMS) {
            if (key.toUpperCase().indexOf(projection) >= 0
                    || tileMS[key].supportedCRS.toUpperCase().indexOf(
                            projection) >= 0
                    || tileMS[key].identifier.toUpperCase().indexOf(projection) >= 0) {
                layermatrixSet = key;
            }
        }

        return this.parser.createLayer(capabilities, {
            name : layer.title,
            layer : layer.identifier,
            matrixSet : layermatrixSet,
            isBaseLayer : false,
            format: layer.formats[0]
        });
    },

    /**
     * Method: addLayer Add a layer to the TreeNode
     * 
     * Parameters: layer - {Object} layer object from the WMSCapabilities parser
     * url - {String} the OnlineResource of the WMS parentNode - {<Ext.tree.TreeNode>}
     * if there is a parentNode, the newly created node will be appended to the
     * parentNode
     * 
     * Returns: {<Ext.tree.TreeNode>}
     */
    addLayer : function(layer, url, parentNode, caps) {
        var wmsLayer = null;
        if (layer.identifier ) {
            wmsLayer = this.createWMSLayer(layer, caps, caps);
            if (layer.styles && layer.styles.length > 0) {
                var style = layer.styles[0];
                if (style.legend && style.legend.href) {
                    wmsLayer.legendURL = style.legend.href;
                }
            }
        }
        var node = new Ext.tree.TreeNode({
            wmsLayer : wmsLayer,
            text : layer.title
        });
        node.addListener("click", this.click, this.scope);
        if (parentNode) {
            parentNode.appendChild(node);
        }
        return node;
    },

    /**
     * Method: processLayer Recursive function to process a layer and their
     * childLayers
     * 
     * Parameters: layer - {Object} layer object from the WMSCapabilities parser
     * url - {String} the OnlineResource of the WMS node - {<Ext.tree.TreeNode>}
     */
    processLayer : function(layer, url, node, caps) {
        Ext.each(layer.layers, function(el) {
            var node2 = this.addLayer(el, url, node, caps);
            if (el.nestedLayers) {
                this.processLayer(el, url, node2, caps);
            }
        }, this);
    }

};