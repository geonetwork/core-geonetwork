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

if (!window.GeoNetwork) {
    window.GeoNetwork = {};
}
if (!GeoNetwork.Format) {
    GeoNetwork.Format = {};
}

/**
 * Class: GeoNetwork.Format.XLSLUS
 * Read/Wite XLS Location Utility Service (geocode/reverse geocode).
 * Create a new instance with the <GeoNetwork.Format.XLSLUS>
 * constructor.
 * 
 * Inherits from:
 *  - <OpenLayers.Format.XML>
 */
GeoNetwork.Format.XLSLUS = OpenLayers.Class(OpenLayers.Format.XML, {
    
    /**
     * APIProperty: defaultVersion
     * {String} Version number to assume if none found.  Default is "1.1.0".
     */
    defaultVersion: "1.1.0",
    
    /**
     * APIProperty: version
     * {String} Specify a version string if one is known.
     */
    version: null,
    
    /**
     * Property: parser
     * {Object} Instance of the versioned parser.  Cached for multiple read and
     *     write calls of the same version.
     */
    parser: null,

    /**
     * Constructor: GeoNetwork.Format.XLSLUS
     * Create a new parser for XLSLUS.
     *
     * Parameters:
     * options - {Object} An optional object whose properties will be set on
     *     this instance.
     */
    initialize: function(options) {
        OpenLayers.Format.XML.prototype.initialize.apply(this, [options]);
    },

    /**
     * APIMethod: writeGeocodeRequest
     * Write a GeocodeRequest document.
     *
     * Parameters:
     * address - {XLSAddress} An object representing the address.
     * options - {Object} Optional configuration object.
     *
     * Returns:
     * {String} An XLSLUS document string.
     */
    writeGeocodeRequest: function(address, options) {
        var version = (options && options.version) ||
                      this.version || this.defaultVersion;
        if(!this.parser || this.parser.VERSION != version) {
            var format = GeoNetwork.Format.XLSLUS[
                "v" + version.replace(/\./g, "_")
            ];
            if(!format) {
                throw "Can't find a XLSLUS parser for version " +
                      version;
            }
            this.parser = new format(options);
        }
        var root = this.parser.writeGeocodeRequest(address);
        return OpenLayers.Format.XML.prototype.write.apply(this, [root]);
    },
    
    /**
     * APIMethod: writeReverseGeocodeRequest
     * Write a ReverseGeocodeRequest document.
     *
     * Parameters:
     * position - {OpenLayers.Geometry.Point} An object representing the location.
     *            Also more complicated positions are allowed.
     * options - {Object} Optional configuration object.
     *
     * Returns:
     * {String} An XLSLUS document string.
     */
    writeReverseGeocodeRequest: function(position, options) {
        var version = (options && options.version) ||
                      this.version || this.defaultVersion;
        if(!this.parser || this.parser.VERSION != version) {
            var format = GeoNetwork.Format.XLSLUS[
                "v" + version.replace(/\./g, "_")
            ];
            if(!format) {
                throw "Can't find a XLSLUS parser for version " +
                      version;
            }
            this.parser = new format(options);
        }
        var root = this.parser.writeReverseGeocodeRequest(position);
        return OpenLayers.Format.XML.prototype.write.apply(this, [root]);
    },
    
    /**
     * APIMethod: read
     * Read and XLSLUS doc and return an object representing the XLSLUS.
     * The document could be a GeocodeResponse or a ReverseGeocodeResponse.
     *
     * Parameters:
     * data - {String | DOMElement} Data to read.
     *
     * Returns:
     * {Object} An object representing the XLSLUS.
     *          For a GeocodeResponse, an array (representing the
     *          "geocodeResponseList") of objects. Each object has a
     *          property named "features", being an array of
     *          <OpenLayers.Features.Vector>. Each feature has a geometry
     *          and in the attributes an attribute named "address", being
     *          an <GeoNetwork.Format.XLSAddress>.
     *          For a ReverseGeocodeResponse, an array (representing the
     *          reverseGeocodedLocation) of features (each feature as above).
     */
    read: function(data, options) {
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
        if(!this.parser || this.parser.VERSION != version) {
            var format = GeoNetwork.Format.XLSLUS[
                "v" + version.replace(/\./g, "_")
            ];
            if(!format) {
                throw "Can't find a XLSLUS parser for version " +
                      version;
            }
            this.parser = new format(options);
        }
        var xlslus = this.parser.read(data);
        return xlslus;
    },

    CLASS_NAME: "GeoNetwork.Format.XLSLUS" 
});

