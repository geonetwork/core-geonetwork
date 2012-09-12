/* Copyright (c) 2006-2008 MetaCarta, Inc., published under the Clear BSD
 * license.  See http://svn.openlayers.org/trunk/openlayers/license.txt for the
 * full text of the license. */

/**
 * @requires OpenLayers/Format/XML.js
 */

/**
 * Class: OpenLayers.Format.GeoNetworkRecords 
 * 
 * A format for creating xml search transactions to a GeoNetwork node. 
 * Create a new instance with the <OpenLayers.Format.GeoNetworkRecords> 
 * constructor.
 * 
 * Inherits from: - <OpenLayers.Format.CSWGetRecords.v2_0_2>
 */
OpenLayers.Format.GeoNetworkRecords = OpenLayers.Class(OpenLayers.Format.XML, {

	defaultPrefix : "nons",

	/**
	 * Property: namespaces {Object} Mapping of namespace aliases to namespace
	 * URIs.
	 */
	namespaces : {
		nons : "",
		geonet : "http://www.fao.org/geonetwork"
	},

	/**
	 * Constructor: OpenLayers.Format.GeoNetworkRecords A class for parsing and
	 * generating CSWGetRecords v2.0.2 transactions. All geonet:info 
	 * and summary information are retrieved.
	 * 
	 * Parameters: options - {Object} Optional object whose properties will be
	 * set on the instance.
	 * 
	 * Valid options properties (documented as class properties):
	 */
	initialize : function(options) {
		OpenLayers.Format.XML.prototype.initialize.apply(this, [ options ]);
	},

	/**
	 * APIMethod: read Parse the response from a GetRecords request.
	 */
	read : function(data) {
		if (typeof data == "string") {
			data = OpenLayers.Format.XML.prototype.read.apply(this, [ data ]);
		}
		if (data && data.nodeType == 9) {
			data = data.documentElement;
		}
		var obj = {};
		this.readNode(data, obj);
		return obj;
	},

	/**
	 * Property: readers Contains public functions, grouped by namespace prefix,
	 * that will be applied when a namespaced node is found matching the
	 * function name. The function will be applied in the scope of this parser
	 * with two arguments: the node being read and a context object passed from
	 * the parent.
	 */
	readers : {
		"nons" : {
			"response" : function(node, obj) {
				obj.records = [];
				this.readChildNodes(node, obj);
				var from = this.getAttributeNS(node, "", 'from');
				if (from != "") {
					obj.from = from;
				}
				var to = this.getAttributeNS(node, "", 'to');
				if (to != "") {
					obj.to = to;
				}
			},
			"summary" : function(node, obj) {
				obj.summary = {};
				var attrs = node.attributes;
				for ( var i = 0, len = attrs.length; i < len; ++i) {
					obj.summary[attrs[i].name] = attrs[i].nodeValue;
				}
				this.readChildNodes(node, obj.summary);
			},
			"metadata" : function(node, obj) {
				var record = {
					type : "metadata"
				};
				this.readChildNodes(node, record);
				obj.records.push(record);
			},
			"geoBox" : function(node, obj) {
				// LowerCorner = "min_x min_y"
				// UpperCorner = "max_x max_y"
				if (!(obj.BoundingBox instanceof Array)) {
					obj.BoundingBox = new Array();
				}
				
				var s, w, e, n;
				if(node.firstChild.nodeValue && node.firstChild.nodeValue.indexOf("|")) {
					var coords = node.firstChild.nodeValue.split("|");
					w = coords[0];
					s = coords[1];
					e = coords[2];
					n = coords[3];
				} else {
					for(var child=node.firstChild; child; child=child.nextSibling) {
		                switch(child.nodeName) {
		                    case "southBL": 
		                    	s = this.getChildValue(child);
		                    	break;
		                    case "westBL": 
		                    	w = this.getChildValue(child);
		                    	break;
		                    case "eastBL":
		                    	e = this.getChildValue(child);
		                    	break;
		                    case "northBL":
		                    	n = this.getChildValue(child);
		                    	break;
		                }
		            }
				}
				
				var boundingBox = {
					value : [ parseFloat(w), parseFloat(s),
							parseFloat(e), parseFloat(n) ]
				};
				obj.BoundingBox.push(boundingBox);
			},
			"*" : function(node, obj) {
				var name = node.localName || node.nodeName.split(":").pop();
				var parentName = node.parentNode.localName || node.parentNode.nodeName.split(":").pop();
				
				var value = {};
				value.value = this.getChildValue(node);

				if (parentName == 'info') {
					if (!(obj["geonet_info"][name] instanceof Array)) {
						obj["geonet_info"][name] = new Array();
					}
					obj["geonet_info"][name].push(value);
					if (name == 'harvestInfo' || name == 'valid_details')
					    this.readChildNodes(node, obj["geonet_info"][name]);
				} else {
					if (!(obj[name] instanceof Array)) {
						obj[name] = new Array();
					}
					
					var attrs = node.attributes;
					for ( var i = 0, len = attrs.length; i < len; ++i) {
						value[attrs[i].name] = attrs[i].nodeValue;
					}
					obj[name].push(value);
					this.readChildNodes(node, obj[name]);
				}
			}
		},
		"geonet" : {
			// info
		"info" : function(node, obj) {
			if (!(obj["geonet_info"] instanceof Array)) {
				obj["geonet_info"] = new Array();
			}
			this.readChildNodes(node, obj);
		}
	}
	},

	CLASS_NAME : "OpenLayers.Format.GeoNetworkRecords"
});