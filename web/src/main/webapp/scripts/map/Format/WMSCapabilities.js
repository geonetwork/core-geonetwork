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

if (!window.GeoNetwork) {
    window.GeoNetwork = {};
}
if (!GeoNetwork.Format) {
    GeoNetwork.Format = {};
}

/**
 * Class: GeoNetwork.Format.WMSCapabilities
 * Read WMS Capabilities.
 * 
 * Inherits from:
 *  - <OpenLayers.Format.XML>
 */
GeoNetwork.Format.WMSCapabilities = OpenLayers.Class(OpenLayers.Format.XML, {
    
    /**
     * APIProperty: defaultVersion
     * {String} Version number to assume if none found.  Default is "1.1.1".
     */
    defaultVersion: "1.1.1",
    
    /**
     * APIProperty: version
     * {String} Specify a version string if one is known.
     */
    version: null,

    /**
     * Constructor: OpenLayers.Format.WMSCapabilities
     * Create a new parser for WMS capabilities.
     *
     * Parameters:
     * options - {Object} An optional object whose properties will be set on
     *     this instance.
     */
    initialize: function(options) {
        OpenLayers.Format.XML.prototype.initialize.apply(this, [options]);
        this.options = options;
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
        var root = data.documentElement;
        var version = this.version;
        if(!version) {
            version = root.getAttribute("version");
            if(!version) {
                version = this.defaultVersion;
            }
        }
        var constructor = GeoNetwork.Format[
            "WMSCapabilities_" + version.replace(/\./g, "_")
        ];
        if(!constructor) {
            throw "Can't find a WMS capabilities parser for version " + version;
        }
        var parser = new constructor(this.options);
        var capabilities = parser.read(data);
        capabilities.version = version;
        return capabilities;
    },
    
    CLASS_NAME: "GeoNetwork.Format.WMSCapabilities" 

});
