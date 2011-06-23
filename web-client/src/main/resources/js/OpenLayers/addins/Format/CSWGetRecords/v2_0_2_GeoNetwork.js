/* Copyright (c) 2006-2008 MetaCarta, Inc., published under the Clear BSD
 * license.  See http://svn.openlayers.org/trunk/openlayers/license.txt for the
 * full text of the license. */

/**
 * @requires OpenLayers/Format/XML.js
 * @requires OpenLayers/Format/CSWGetRecords.js
 * @requires OpenLayers/Format/Filter/v1_0_0.js
 * @requires OpenLayers/Format/Filter/v1_1_0.js
 * @requires OpenLayers/Format/CSWGetRecords/v2_0_2.js
 */

/**
 * Class: OpenLayers.Format.CSWGetRecords.v2_0_2_GeoNetwork
 *     A format for creating CSWGetRecords v2.0.2 transactions to a GeoNetwork node. 
 *     Create a new instance with the
 *     <OpenLayers.Format.CSWGetRecords.v2_0_2_GeoNetwork> constructor.
 *	   If CSW queries made using results_with_summary parameters, then geonet:info
 *	   elements are also retrived.
 *
 *     GeoNetworkRecords and xml search service are recommended to use instead, this
 *     format was experimental.
 *
 *
 * Inherits from:
 *  - <OpenLayers.Format.CSWGetRecords.v2_0_2>
 */
OpenLayers.Format.CSWGetRecords.v2_0_2_GeoNetwork = OpenLayers.Class(OpenLayers.Format.CSWGetRecords.v2_0_2, {
    
    /**
     * Property: namespaces
     * {Object} Mapping of namespace aliases to namespace URIs.
     */
    namespaces: {
        xlink: "http://www.w3.org/1999/xlink",
        xsi: "http://www.w3.org/2001/XMLSchema-instance",
        csw: "http://www.opengis.net/cat/csw/2.0.2",
        dc: "http://purl.org/dc/elements/1.1/",
        dct: "http://purl.org/dc/terms/",
        ows: "http://www.opengis.net/ows",
        geonet: "http://www.fao.org/geonetwork"
    },
    
    /**
     * Constructor: OpenLayers.Format.CSWGetRecords.v2_0_2_GeoNetwork
     * A class for parsing and generating CSWGetRecords v2.0.2 transactions.
     *
     * Parameters:
     * options - {Object} Optional object whose properties will be set on the
     *     instance.
     *
     * Valid options properties (documented as class properties):
     * - requestId
     * - resultType
     * - outputFormat
     * - outputSchema
     * - startPosition
     * - maxRecords
     * - DistributedSearch
     * - ResponseHandler
     * - Query
     */
    initialize: function(options) {
        OpenLayers.Format.CSWGetRecords.v2_0_2.prototype.initialize.apply(this, [options]);
    },
    
    /**
     * Property: readers
     * Contains public functions, grouped by namespace prefix, that will
     *     be applied when a namespaced node is found matching the function
     *     name.  The function will be applied in the scope of this parser
     *     with two arguments: the node being read and a context object passed
     *     from the parent.
     */
    readers: {
        "csw": {
            "GetRecordsResponse": function(node, obj) {
                obj.records = [];
                this.readChildNodes(node, obj);
                var version = this.getAttributeNS(node, "", 'version');
                if (version != "") {
                    obj.version = version;
                }
            },
            "RequestId": function(node, obj) {
                obj.RequestId = this.getChildValue(node);
            },
            "SearchStatus": function(node, obj) {
                obj.SearchStatus = {};
                var timestamp = this.getAttributeNS(node, "", 'timestamp');
                if (timestamp != "") {
                    obj.SearchStatus.timestamp = timestamp;
                }
            },
            "SearchResults": function(node, obj) {
                this.readChildNodes(node, obj);
                var attrs = node.attributes;
                var SearchResults = {};
                for(var i=0, len=attrs.length; i<len; ++i) {
                    if ((attrs[i].name == "numberOfRecordsMatched") ||
                        (attrs[i].name == "numberOfRecordsReturned") ||
                        (attrs[i].name == "nextRecord")) {
                        SearchResults[attrs[i].name] = parseInt(attrs[i].nodeValue);
                    } else {
                        SearchResults[attrs[i].name] = attrs[i].nodeValue;
                    }
                }
                obj.SearchResults = SearchResults;
            },
            "SummaryRecord": function(node, obj) {
                var record = {type: "SummaryRecord"};
                this.readChildNodes(node, record);
                obj.records.push(record);
            },
            "BriefRecord": function(node, obj) {
                var record = {type: "BriefRecord"};
                this.readChildNodes(node, record);
                obj.records.push(record);
            },
            "DCMIRecord": function(node, obj) {
                var record = {type: "DCMIRecord"};
                this.readChildNodes(node, record);
                obj.records.push(record);
            },
            "Record": function(node, obj) {
                var record = {type: "Record"};
                this.readChildNodes(node, record);
                obj.records.push(record);
            },
            "*": function(node, obj) {
            	var info = node.localName
            	if (!(obj["geonet_info"][info] instanceof Array)) {
                    obj["geonet_info"][info] = new Array();
                }
            	var value = this.getChildValue(node);
        		obj["geonet_info"][info].push(value);
        		//console.log("add " + info + ":" + value);
            }
        },
        "dc": {
            // audience, contributor, coverage, creator, date, description, format,
            // identifier, language, provenance, publisher, relation, rights,
            // rightsHolder, source, subject, title, type, URI
            "*": function(node, obj) {
                var name = node.localName || node.nodeName.split(":").pop();
                if (!(obj[name] instanceof Array)) {
                    obj[name] = new Array();
                }
                var dc_element = {};
                var attrs = node.attributes;
                for(var i=0, len=attrs.length; i<len; ++i) {
                    dc_element[attrs[i].name] = attrs[i].nodeValue;
                }
                dc_element.value = this.getChildValue(node);
                obj[name].push(dc_element);
            }
        },
        "dct": {
            // abstract, modified, spatial
            "*": function(node, obj) {
                var name = node.localName || node.nodeName.split(":").pop();
                if (!(obj[name] instanceof Array)) {
                    obj[name] = new Array();
                }
                obj[name].push(this.getChildValue(node));
            }
        },
        "geonet": {
            // info
            "info": function(node, obj) {
        		//var geonet_info = {type: "geonet_info"};
        		if (!(obj["geonet_info"] instanceof Array)) {
                    obj["geonet_info"] = new Array();
                }
	        	this.readChildNodes(node, obj);
	        	//obj["geonet_info"].push(obj);
            }
        },
        "ows": {
            "WGS84BoundingBox": function(node, obj) {
                // LowerCorner = "min_x min_y"
                // UpperCorner = "max_x max_y"
                if (!(obj.BoundingBox instanceof Array)) {
                    obj.BoundingBox = new Array();
                }
                //this.readChildNodes(node, bbox);
                var lc = this.getChildValue(
                    this.getElementsByTagNameNS(
                        node,
                        this.namespaces["ows"],
                        "LowerCorner"
                    )[0]
                ).split(' ', 2);
                var uc = this.getChildValue(
                    this.getElementsByTagNameNS(
                        node,
                        this.namespaces["ows"],
                        "UpperCorner"
                    )[0]
                ).split(' ', 2);

                var boundingBox = {
                    value: [
                        parseFloat(lc[0]),
                        parseFloat(lc[1]),
                        parseFloat(uc[0]),
                        parseFloat(uc[1])
                    ]
                };
                // store boundingBox attributes
                var attrs = node.attributes;
                for(var i=0, len=attrs.length; i<len; ++i) {
                    boundingBox[attrs[i].name] = attrs[i].nodeValue;
                }
                obj.BoundingBox.push(boundingBox);
            },

            "BoundingBox": function(node, obj) {
                // FIXME: We consider that BoundingBox is the same as WGS84BoundingBox
                // LowerCorner = "min_x min_y"
                // UpperCorner = "max_x max_y"
                // It should normally depend on the projection
                this.readers['ows']['WGS84BoundingBox'].apply(this, [node, obj]);
            }
        }
    },
    
    CLASS_NAME: "OpenLayers.Format.CSWGetRecords.v2_0_2_GeoNetwork" 
});
