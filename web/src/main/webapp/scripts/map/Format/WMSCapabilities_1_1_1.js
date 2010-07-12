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

/*
 * Based on the work by Tim Schaub at:
 * http://trac.openlayers.org/ticket/1176
 *
 * Added: layer hierarchy
 */

/**
 * @requires GeoNetwork/Format/WMSCapabilities.js
 *
 * Class: GeoNetwork.Format.WMSCapabilities_1_1_1
 * Read WMS Capabilities version 1.1.1.
 *
 * Inherits from:
 *  - <GeoNetwork.Format.WMSCapabilities>
 */
GeoNetwork.Format.WMSCapabilities_1_1_1 = OpenLayers.Class(
    GeoNetwork.Format.WMSCapabilities, {

    /**
     * Constructor: GeoNetwork.Format.WMSCapabilities_1_1_1
     * Create a new parser for WMS capabilities version 1_1_1.
     *
     * Parameters:
     * options - {Object} An optional object whose properties will be set on
     *     this instance.
     */
    initialize: function(options) {
        GeoNetwork.Format.WMSCapabilities.prototype.initialize.apply(
            this, [options]
        );
    },

    /**
     * APIMethod: read
     * Read capabilities data from a string, and return a list of layers.
     *
     * Parameters:
     * data - {String} or {DOMElement} data to read/parse.
     *
     * Returns:
     * {Array} List of named layers.
     */
    read: function(data) {
        if(typeof data == "string") {
            data = OpenLayers.Format.XML.prototype.read.apply(this, [data]);
        }
        var capabilities = {};
        var root = data.documentElement;
        this.runChildNodes(capabilities, root);
        return capabilities;
    },

    /**
     * Method: runChildNodes
     * Run over all the child nodes
     * 
     * Parameters:
     * obj - {Object} The object which will be used to write all info to
     * node - {Object} The node from the DOM
     */
    runChildNodes: function(obj, node) {
        var children = node.childNodes;
        var childNode, processor;
        for(var i=0; i<children.length; ++i) {
            childNode = children[i];
            if(childNode.nodeType == 1) {
                processor = this["process" + childNode.nodeName];
                if(processor) {
                    processor.apply(this, [obj, childNode]);
                }
            }
        }
    },

    /**
     * Method: processRequest
     */
    processRequest: function(obj, node) {
        var request = {};
        this.runChildNodes(request, node);
        obj.request = request;
    },

    /**
     * Method: processGetMap
     */
    processGetMap: function(request, node) {
        var getmap = {
            formats: []
        };
        this.runChildNodes(getmap, node);
        request.getmap = getmap;
    },

    /**
     * Method: processDCPType
     * Super simplified HTTP href extractor.  Assumes the first online resource
     *     will work.
     */
    processDCPType: function(obj, node) {
        var children = node.getElementsByTagName("OnlineResource");
        if(children.length > 0) {
            this.processOnlineResource(obj, children[0]);
        }
    },

    /**
     * Method: processCapability
     */
    processCapability: function(capabilities, node) {
        var capability = {
            layers: []
        };
        this.runChildNodes(capability, node);
        capabilities.capability = capability;
    },

    /**
     * Method: processService
     */
    processService: function(capabilities, node) {
        var service = {};
        this.runChildNodes(service, node);
        capabilities.service = service;
    },

    /**
     * Method: processLayer
     */
    processLayer: function(capability, node, parentLayer) {
        var layer = {
            styles: [],
            dimensions: {}
        };

        layer.queryable = (node.getAttribute("queryable") == "1");

        // deal with property inheritance
        if(parentLayer) {
            if (!parentLayer.childLayers) {
                parentLayer.childLayers = [];
            }
            parentLayer.childLayers.push(layer);
            // add style
            layer.styles = layer.styles.concat(parentLayer.styles);
        }
        var children = node.childNodes;
        var childNode, nodeName, processor;
        for(var i=0; i<children.length; ++i) {
            childNode = children[i];
            nodeName = childNode.nodeName;
            processor = this["process" + childNode.nodeName];
            if(processor) {
                if(nodeName == "Layer") {
                    processor.apply(this, [capability, childNode, layer]);
                } else {
                    processor.apply(this, [layer, childNode]);
                }
            }
        }
        if (layer.childLayers) {
            capability.layers = [];
            capability.layers.push(layer);
        }
    },

    /**
     * Method: processName
     */
    processName: function(obj, node) {
        var name = this.getChildValue(node);
        if(name) {
            obj.name = name;
        }
    },

    /**
     * Method: processTitle
     */
    processTitle: function(obj, node) {
        var title = this.getChildValue(node);
        if(title) {
            obj.title = title;
        }
    },

    /**
     * Method: processAbstract
     */
    processAbstract: function(obj, node) {
        var abst = this.getChildValue(node);
        if(abst) {
            obj.abstrack = abst;
        }
    },

    /**
     * Method: processLatLonBoundingBox
     */
    processLatLonBoundingBox: function(layer, node) {
        layer.llbbox = [
            parseFloat(node.getAttribute("minx")),
            parseFloat(node.getAttribute("miny")),
            parseFloat(node.getAttribute("maxx")),
            parseFloat(node.getAttribute("maxy"))
        ];
    },

    /**
     * Method: processStyle
     */
    processStyle: function(layer, node) {
        var style = {};
        this.runChildNodes(style, node);
        layer.styles.push(style);
    },

    /**
     * Method: processDimension
     */
    processDimension: function(layer, node) {
        var name = node.getAttribute("name").toLowerCase(); 
        if (name in layer.dimensions) { 
            return;
        }
        var dim = { 
            name: name, 
            units: node.getAttribute("units"), 
            unitsymbol: node.getAttribute("unitSymbol") 
        }; 
        layer.dimensions[dim.name] = dim;         
    },

    /**
     * Method: processExtent
     */
    processExtent: function(layer, node){
        var name = node.getAttribute("name").toLowerCase();
        if (name in layer.dimensions) {
            var extent = layer.dimensions[name];
            extent.nearestVal = node.getAttribute("nearestValue") === "1"; 
            extent.multipleVal = node.getAttribute("multipleValues") === "1"; 
            extent.current = node.getAttribute("current") === "1"; 
            extent["default"] = node.getAttribute("default") || ""; 
            var values = this.getChildValue(node); 
            extent.values = values.split(","); 
        }
    },

    /**
     * Method: processLegendURL
     */
    processLegendURL: function(style, node) {
        var legend = {
            width: node.getAttribute('width'),
            height: node.getAttribute('height')
        };
        var links = node.getElementsByTagName("OnlineResource");
        if(links.length > 0) {
            this.processOnlineResource(legend, links[0]);
        }
        style.legend = legend;
    },

    /**
     * Method: processMetadataURL
     */
    processMetadataURL: function(layer, node) {
        var metadataURL = {};
        var links = node.getElementsByTagName("OnlineResource");
        if(links.length > 0) {
            this.processOnlineResource(metadataURL, links[0]);
        }
        layer.metadataURL = metadataURL.href;
    },

    /**
     * Function: calculateScale
     * Transform a scalehint value into a scale denominator
     *
     * Returns:
     * {Float} The scale denominator
     */
    calculateScale: function(scaleHint) {
        return Math.round( (OpenLayers.DOTS_PER_INCH * 39.3701 * scaleHint) / Math.sqrt(2) );
    },

    /**
     * Method: processScaleHint
     */
    processScaleHint: function(layer, node) {
        layer.minScale = this.calculateScale(node.getAttribute('max'));
        if (layer.minScale === 0) {
            layer.minScale = null;
        }
        layer.maxScale = this.calculateScale(node.getAttribute('min'));
    },

    /**
     * Method: processOnlineResource
     */
    processOnlineResource: function(obj, node) {
        obj.href = this.getAttributeNS(
            node, "http://www.w3.org/1999/xlink", "href");
    },

    /**
     * Method: processAccessConstraints
     */
    processAccessConstraints: function(obj, node) {
                obj.accessContraints = node.textContent;
    },

    /**
     * Method: processBoundingBox
     */
    processBoundingBox: function(obj, node) {
        if (obj.llbbox) return;
        
        var existingBoundingBox = ((node.getAttribute('minx')) && (node.getAttribute('miny')) &&
            (node.getAttribute('maxx')) && (node.getAttribute('maxy')));

        if (existingBoundingBox) {
            var srs = node.getAttribute('SRS');

           // store info as wgs84
            var layerProj = new OpenLayers.Projection(srs);
            var wgs84 = new OpenLayers.Projection("WGS84");

            var minMapxy = new OpenLayers.LonLat(parseFloat(node.getAttribute('minx')),
                parseFloat(node.getAttribute('miny'))).transform(layerProj, wgs84);
            var maxMapxy = new OpenLayers.LonLat(parseFloat(node.getAttribute('maxx')),
                parseFloat(node.getAttribute('maxy'))).transform(layerProj, wgs84);

            obj.llbbox = [
                parseFloat(minMapxy.lon),
                parseFloat(minMapxy.lat),
                parseFloat(maxMapxy.lon),
                parseFloat(maxMapxy.lat)
            ];

        }
    },
    
    CLASS_NAME: "GeoNetwork.Format.WMSCapabilities_1_1_1"

});
