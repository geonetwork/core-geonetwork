/**
 * @requires OpenLayers/Format/XML.js
 * @requires OpenLayers/Format/CSWGetRecords.js
 * @requires OpenLayers/Format/Filter/v1_0_0.js
 * @requires OpenLayers/Format/Filter/v1_1_0.js
 */

/**
 * Class: OpenLayers.Format.CSWGetRecords.v2_0_2
 *     A format for creating CSWGetRecords v2.0.2 transactions. 
 *     Create a new instance with the
 *     <OpenLayers.Format.CSWGetRecords.v2_0_2> constructor.
 *
 * Inherits from:
 *  - <OpenLayers.Format.XML>
 */
OpenLayers.Format.CSWGetRecords.v2_0_2 = OpenLayers.Class(OpenLayers.Format.XML, {
    
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
        ows: "http://www.opengis.net/ows"
    },
    
    /**
     * Property: defaultPrefix
     */
    defaultPrefix: "csw",
    
    /**
     * Property: version
     * {String} CSW version number.
     */
    version: "2.0.2",
    
    /**
     * Property: schemaLocation
     * {String} http://www.opengis.net/cat/csw/2.0.2
     *   http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
     */
    schemaLocation: "http://www.opengis.net/cat/csw/2.0.2 http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd",
    
    /**
     * Constructor: OpenLayers.Format.CSWGetRecords.v2_0_2
     * A class for parsing and generating CSWGetRecords v2.0.2 transactions.
     *
     * Parameters:
     * options - {Object} Optional object whose properties will be set on the
     *     instance.
     *
     * Valid options properties:
     * requestId - {String} requestId for csw GetRecords request.
     * resultType - {String} resultType for csw GetRecords request. Default is
     *     'hits'.
     * outputFormat - {String} outputFormat for csw GetRecords request. Default
     *     is 'application/xml'
     * outputSchema - {String} outputSchema for csw GetRecords request.
     * startPosition - {Integer} startPosition for csw GetRecords request.
     *     Default is 1.
     * maxRecords - {Integer} maxRecords for csw GetRecords request.
     *     Default is 10.
     * DistributedSearch - {Object} DistributedSearch options for csw GetRecords request.
     * ResponseHandler - {Array} array of ResponseHandler values for csw GetRecords request.
     * Query - {Object} Query options for csw GetRecords request.
     */
    initialize: function(options) {
        OpenLayers.Format.XML.prototype.initialize.apply(this, [options]);
    },

    /**
     * Method: read
     * Parse the response from a transaction.
     */
    read: function(data) {
        if(typeof data == "string") { 
            data = OpenLayers.Format.XML.prototype.read.apply(this, [data]);
        }
        if(data && data.nodeType == 9) {
            data = data.documentElement;
        }
        var obj = {};
        this.readNode(data, obj);
        return obj;
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
                    }
                    else {
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
            }
        },
        "dc": {
            // audience, contributor, coverage, creator, date, description, format,
            // identifier, language, provenance, publisher, relation, rights,
            // rightsHolder, source, subject, title, type, URI
            "*": function(node, obj) {
                if (!(obj[node.localName] instanceof Array)) {
                    obj[node.localName] = new Array();
                }
                var dc_element = {};
                var attrs = node.attributes;
                for(var i=0, len=attrs.length; i<len; ++i) {
                    dc_element[attrs[i].name] = attrs[i].nodeValue;
                }
                dc_element.value = this.getChildValue(node);
                obj[node.localName].push(dc_element);
            }
        },
        "dct": {
            // abstract, modified, spatial
            "*": function(node, obj) {
                if (!(obj[node.localName] instanceof Array)) {
                    obj[node.localName] = new Array();
                }
                obj[node.localName].push(this.getChildValue(node));
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

                var BoundingBox = {
                    value: [
                        parseFloat(lc[0]),
                        parseFloat(lc[1]),
                        parseFloat(uc[0]),
                        parseFloat(uc[1])
                    ]
                };
                // store BoundingBox attributes
                var attrs = node.attributes;
                for(var i=0, len=attrs.length; i<len; ++i) {
                    BoundingBox[attrs[i].name] = attrs[i].nodeValue;
                }
                obj.BoundingBox.push(BoundingBox);
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
    
    /**
     * Method: write
     * Given an configuration js object, write a CSWGetRecords request. 
     *
     * Parameters:
     * options - {Object} A object mapping the request.
     *
     * Returns:
     * {String} A serialized CSWGetRecords request.
     */
    write: function(options) {
        var node = this.writeNode("csw:GetRecords", options);
        return OpenLayers.Format.XML.prototype.write.apply(this, [node]);
    },

    /**
     * Property: writers
     * As a compliment to the readers property, this structure contains public
     *     writing functions grouped by namespace alias and named like the
     *     node names they produce.
     */
    writers: {
        "csw": {
            "GetRecords": function(options) {
                if (!options) {
                    options = {};
                }
                var node = this.createElementNSPlus("csw:GetRecords", {
                    attributes: {
                        service: "CSW",
                        version: this.version,
                        requestId: options.requestId || this.requestId,
                        resultType: options.resultType || this.resultType,
                        outputFormat: options.outputFormat || this.outputFormat,
                        outputSchema: options.outputSchema || this.outputSchema,
                        startPosition: options.startPosition || this.startPosition,
                        maxRecords: options.maxRecords || this.maxRecords
                    }
                });
                if (options.DistributedSearch || this.DistributedSearch) {
                    this.writeNode(
                        "csw:DistributedSearch",
                        options.DistributedSearch || this.DistributedSearch,
                        node
                    );
                }
                var ResponseHandler = options.ResponseHandler || this.ResponseHandler;
                if (ResponseHandler &&
                    (ResponseHandler instanceof Array) &&
                    (ResponseHandler.length > 0)) {
                    // ResponseHandler must be a non-empty array
                    for(var i=0, len=ResponseHandler.length; i<len; i++) {
                        this.writeNode(
                            "csw:ResponseHandler",
                            ResponseHandler[i],
                            node
                        );
                    }
                }
                this.writeNode("Query", options.Query || this.Query, node);
                return node;
            },
            "DistributedSearch": function(options) {
                var node = this.createElementNSPlus("csw:DistributedSearch", {
                    attributes: {
                        hopCount: options.hopCount
                    }
                });
                return node;
            },
            "ResponseHandler": function(options) {
                var node = this.createElementNSPlus("csw:ResponseHandler", {
                    value: options.value
                });
                return node;
            },
            "Query": function(options) {
                if (!options) {
                    options = {};
                }
                var node = this.createElementNSPlus("csw:Query", {
                    attributes: {
                        typeNames: options.typeNames || "csw:Record"
                    }
                });
                var ElementName = options.ElementName;
                if (ElementName &&
                    (ElementName instanceof Array) &&
                    (ElementName.length > 0)) {
                    // ElementName must be a non-empty array
                    for(var i=0, len=ElementName.length; i<len; i++) {
                        this.writeNode(
                            "csw:ElementName",
                            ElementName[i],
                            node
                        );
                    }
                }
                else {
                    this.writeNode(
                        "csw:ElementSetName",
                        options.ElementSetName || {value: 'summary'},
                        node
                    );
                }
                if (options.Constraint) {
                    this.writeNode(
                        "csw:Constraint",
                        options.Constraint,
                        node
                    );
                }
                //TODO: not implemented in ogc filters?
                //if (options.SortBy) {
                    //this.writeNode(
                        //"ogc:SortBy",
                        //options.SortBy,
                        //node
                    //);
                //}
                return node;
            },
            "ElementName": function(options) {
                var node = this.createElementNSPlus("csw:ElementName", {
                    value: options.value
                });
                return node;
            },
            "ElementSetName": function(options) {
                var node = this.createElementNSPlus("csw:ElementSetName", {
                    attributes: {
                        typeNames: options.typeNames
                    },
                    value: options.value
                });
                return node;
            },
            "Constraint": function(options) {
                var node = this.createElementNSPlus("csw:Constraint", {
                    attributes: {
                        version: options.version
                    }
                });
                if (options.Filter) {
                    var format_filter = new OpenLayers.Format.Filter({
                        version: options.version
                    });
                    node.appendChild(format_filter.write(options.Filter));
                }
                else {
                    var child = this.createElementNSPlus("CqlText", {
                        value: options.CqlText.value
                    });
                    node.appendChild(child);
                }
                return node;
            }
        }
    },
   
    CLASS_NAME: "OpenLayers.Format.CSWGetRecords.v2_0_2" 
});
