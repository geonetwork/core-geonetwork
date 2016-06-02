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
 * Class: GeoNetwork.Format.XLSAddress
 * Represent an XLS (OGC Open Location Service) Address.
 * Either a freeFormAddress, or a structured address with street, building,
 * place and postalCode.
 */
GeoNetwork.Format.XLSAddress = OpenLayers.Class({
    
    addressee: null,

    /**
     * Property: countryCode
     * {String} two-letter ISO 3166 countrycode for the address.
     */
    countryCode: null,

    /**
     * Property: freeFormAddress
     * {String} address in free format.
     */
    freeFormAddress: null,

    /**
     * Property: street
     * {Array} List of street addresses. Each is either a simple string, or an
     * object with attributes: directionalPrefix, typePrefix,
     * officialName, typeSuffix, directionalSuffix, muniOctant.
     */
    street: null,

    /**
     * Property: Building
     * {Object} An object with attributes: number, subdivision, and buildingName.
     */
    building: null,

    place: null,

    postalCode: null,

    /**
     * Constructor: GeoNetwork.Format.XLSAddress.
     *
     * Parameters:
     * countryCode - 2-letter ISO 3166 countrycode for this address.
     * options - {Object} An optional object whose properties will be set on
     *     this instance.
     */
    initialize: function(countryCode, options) {
        this.street = [];
        this.place = {
            CountrySubdivision: null,
            CountrySecondarySubdivision: null,
            Municipality: null,
            MunicipalitySubdivision:null
        };
        OpenLayers.Util.extend(this, options);
	this.countryCode = countryCode;
    },

    /**
     * Function: format
     * Get the address as a single string. This method could be overridden
     * in subclasses to provide application specific formatting.
     * The <GeoNetwork.Format.XLSLUS> class accepts the option
     * "addressClass" with the application specific XLSAddress subclass
     * to be used when reading XLS XML documents.
     *
     * Returns: {String}
     */
    format: function() {
        if (this.freeFormAddress) {
            return this.freeFormAddress;
        } else {
            return this.getStreetText() + ' ' + this.getBuildingText() +
                ' ' + this.getPostalCodeText() + ' ' + this.getPlaceText();
        }
    },

    /**
     * Function: getStreetText
     * Get the street(s) as a single string. Useful when using the
     * OpenLayers String.format with a template. The template should
     * use this function name, and pass the address object (since
     * the format function calls the function without a this).
     * 
     * Parameters:
     * address - {<GeoNetwork.Format.XLSAddress>}. The address, if not
     *     specified, works on "this".
     *
     * Returns: {String}
     */
    getStreetText: function(address) {
	if (!address) { address = this; }
        var text = '';
        for (var si = 0; si < address.street.length; si++) {
            if (text !== '') { text += ' '; }
            text += address.formatObject(address.street[si], 
                GeoNetwork.Format.XLSAddress.formattedStreetProperties);
        }
        return text;
    },

    /**
     * Function: getBuildingText
     * Get the building as a single string. Useful when using the
     * OpenLayers String.format with a template. The template should
     * use this function name, and pass the address object (since
     * the format function calls the function without a this).
     *
     * Parameters:
     * address - {<GeoNetwork.Format.XLSAddress>}. The address, if not
     *     specified, works on "this".
     *
     * Returns: {String}
     */
    getBuildingText: function(address) {
	if (!address) { address = this; }
        return address.formatObject(address.building, GeoNetwork.Format.XLSAddress.formattedBuildingProperties);
    },

    /**
     * Function: getPostalCodeText
     * Get the postalCode as a string, an empty string if null. Useful when using the
     * OpenLayers String.format with a template. The template should
     * use this function name, and pass the address object (since
     * the format function calls the function without a this).
     *
     * Parameters:
     * address - {<GeoNetwork.Format.XLSAddress>}. The address, if not
     *     specified, works on "this".
     *
     * Returns: {String}
     */
    getPostalCodeText: function(address) {
	if (!address) { address = this; }
        return !address.postalCode ? '' : address.postalCode;
    },

    /**
     * Function: getPlaceText
     * Get the place as a single string. Useful when using the
     * OpenLayers.String.format with a template. The template should
     * use this function name, and pass the address object (since
     * the format function calls the function without a this).
     *
     * Parameters:
     * address - {<GeoNetwork.Format.XLSAddress>}. The address, if not
     *     specified, works on "this".
     *
     * Returns: {String}
     */
    getPlaceText: function(address) {
	if (!address) { address = this; }
        return address.formatObject(address.place, 
            GeoNetwork.Format.XLSAddress.formattedPlaceProperties);
    },

    /**
     * Function: formatObject
     * Private function to format an object as a string.
     *
     * Parameters:
     *
     * obj - {Object} the object to format.
     * props - {Array} array of property names from obj to put in the result.
     *
     * Returns: {String}
     */
    formatObject: function(obj, props) {
        if (!obj) { return ''; }
        var text = '';
        if (typeof obj == 'string') {
            text = obj;
        } else if (props instanceof Array) {
            for (var pi = 0; pi < props.length; pi++) {
                if (obj[props[pi]]) {
                    if (text !== '') { text += ' '; }
                    text += obj[props[pi]];
                }
            }
        }
        return text;
    },

    CLASS_NAME: "GeoNetwork.Format.XLSAddress" 
});

/**
 * Place properties to use for formatting an address as a string,
 * defining also the order of the place properties.
 */
GeoNetwork.Format.XLSAddress.formattedPlaceProperties = [
    'Municipality', 'MunicipalitySubdivision',
    'CountrySecondarySubdivision', 'CountrySubdivision'];

/**
 * Street properties to use for formatting an address as a string,
 * defining also the order of the street properties.
 * This applies only to streets using the structured attributes from OpenLS.
 */
GeoNetwork.Format.XLSAddress.formattedStreetProperties = [
    'directionalPrefix', 'typePrefix', 'officialName',
    'typeSuffix', 'directionalSuffix', 'muniOctant' ];

/**
 * Building properties to use for formatting an address as a string,
 * defining also the order of the building properties.
 */
GeoNetwork.Format.XLSAddress.formattedBuildingProperties = [
    'number', 'subdivision', 'buildingName' ];
