/**
 * Non-geocat specific stuff that could be reused
 */

var searchTools = {
    DEFAULT_SIMILARITY: 0.8,
    cswMethod: 'POST',

    createSearchButton: function(formId, url, onClick, onSuccess, onFailure, refineWidget, addFilters) {
        function onButtonClick() {
            onClick();
            refineWidget.refinements = {};
            searchTools.doCSWQueryFromForm(formId, url, 1, onSuccess, onFailure, addFilters);
        }

        return {
            id: 'searchBt',
            text: translate('search'),
            listeners: {
                'render': function() {
                    new Ext.KeyMap(formId, [{
                        key : [10, 13],
                        fn : onButtonClick
                    }]);
                }
            },
            handler: function() {
                onButtonClick();
            }
        };
    },

    doCSWQueryFromForm: function(formId, url, recordNum, onSuccess, onFailure, addFilters) {
        OpenLayers.Request.GET({
            url: 'metadata.select?selected=remove-all',
            success: function(){searchTools.execCswQuery(formId,url,recordNum, onSuccess, onFailure, addFilters);},
            failure: function(){searchTools.execCswQuery(formId,url,recordNum, onSuccess, onFailure, addFilters);}
        });
    },
    execCswQuery: function(formId, url, recordNum, onSuccess, onFailure, addFilters) {
        var query = searchTools.buildCSWQueryFromForm(searchTools.cswMethod, Ext.getCmp(formId), recordNum, geocat.sortBy, addFilters);
        if (searchTools.cswMethod === 'POST') {
            var getQuery = searchTools.buildCSWQueryFromForm('GET', Ext.getCmp(formId), recordNum, geocat.sortBy, addFilters);
            OpenLayers.Request.POST({
                url: url,
                data: query,
                success: function(result) {
                    onSuccess(result, getQuery);
                },
                failure: onFailure
            });
        } else {
            OpenLayers.Request.GET({
                url: url,
                params: query,
                success: function(result) {
                    onSuccess(result, query);
                },
                failure: onFailure
            });
        }
    },
    /**
     * It's assumed the field name follow a convention. It starts with a letter,
     * followed by an underscore. The letters are:
     *  S: Starts with
     *  C: Contains
     *  B: Boolean
     *  E: Equals
     *  T: Equals with words separated by spaces that must be all present
     *  E##: Equals with a specified similarity
     *  G: Geometry by ID
     *  V: field name specified in the value, separated by a '/' with the value
     *  >=: bigger or equal
     *  <=: smaller or equal
     *
     * If it starts with '[', it means its a list of values separated by ',' (ORed)
     */
    buildCSWQueryFromForm: function(method, form, startRecord, sortBy, addFilters) {
        var values = searchTools.getFormValues(form);

        // Process format and formatversion values
        if (values["E1.0_format"]) {
            var format_data = values["E1.0_format"].split("_");
            values["E1.0_format"] = format_data[0];
            if (format_data.length == 2) values["E1.0_formatversion"] = format_data[1];
        }

        var filters = [];

        searchTools.addFiltersFromPropertyMap(values, filters);

        addFilters(values, filters);
        if (filters.length == 0) {
            filters.push(new OpenLayers.Filter.Comparison({
                type: OpenLayers.Filter.Comparison.LIKE,
                property: "anyText",
                value: ".*"
            }));
        }

        var and = new OpenLayers.Filter.Logical({
            type: OpenLayers.Filter.Logical.AND,
            filters: filters
        });

        if (method == 'POST') {
            return searchTools.buildCSWQueryPOST(and, startRecord, sortBy);
        } else {
            return searchTools.buildCSWQueryGET(and, startRecord, sortBy);
        }
    },

    addFiltersFromPropertyMap: function(values, filters) {
        var defaultSimilarity = searchTools.DEFAULT_SIMILARITY;
        var similarity = values['E_similarity'];
        if (similarity != null) {
            defaultSimilarity = values['E_similarity'];
            searchTools.addFilter(filters, 'E_similarity', defaultSimilarity, defaultSimilarity);
        }

        for (var key in values) {
            var value = values[key];
            if (value != "" && key != 'E_similarity') {
                searchTools.addFilter(filters, key, value, defaultSimilarity);
            }
        }
    },

    addFilter: function(filters, key, value, defaultSimilarity) {
        var field = key.match("^(\\[?)([^_]+)_(.*)$");
        if (field) {
            if (field[1]=='[') {
                var or = [];
                var values = value.split(",");
                for(var i=0; i<values.length; ++i) {
                    searchTools.addFilterImpl(values.length>1?or:filters, field[2], field[3], values[i], defaultSimilarity);
                }
                if (values.length > 1) {
                    filters.push(new OpenLayers.Filter.Logical({
                        type: OpenLayers.Filter.Logical.OR,
                        filters: or
                    }));
                }
            } else {
                searchTools.addFilterImpl(filters, field[2], field[3], value, defaultSimilarity);
            }
        }
    },

    addFilterImpl: function(filters, type, name, value, defaultSimilarity) {
        if (type == 'S') {           //starts with
            filters.push(new OpenLayers.Filter.Comparison({
                type: OpenLayers.Filter.Comparison.LIKE,
                property: name,
                value: value + ".*"
            }));
        } else if (type == 'C') {    //contains
            filters.push(new OpenLayers.Filter.Comparison({
                type: OpenLayers.Filter.Comparison.LIKE,
                property: name,
                value: ".*" + value + ".*"
            }));
        } else if (type.charAt(0) == 'E') {    //equals
            if (type.length > 1) {
                // this means that we want to specificy the similarity
                filters.push(new OpenLayers.Filter.Comparison({
                    type: OpenLayers.Filter.Comparison.EQUAL_TO,
                    property: "similarity",
                    value: type.substring(1)
                }));
            }
            filters.push(new OpenLayers.Filter.Comparison({
                type: OpenLayers.Filter.Comparison.EQUAL_TO,
                property: name,
                value: value
            }));
            if (type.length > 1) {
                //restore to the previous similarity
                filters.push(new OpenLayers.Filter.Comparison({
                    type: OpenLayers.Filter.Comparison.EQUAL_TO,
                    property: "similarity",
                    value: defaultSimilarity
                }));
            }
        } else if (type == '>=') {    //bigger or equal
            filters.push(new OpenLayers.Filter.Comparison({
                type: OpenLayers.Filter.Comparison.GREATER_THAN_OR_EQUAL_TO,
                property: name,
                value: value
            }));
        } else if (type == '<=') {    //smaller or equal
            filters.push(new OpenLayers.Filter.Comparison({
                type: OpenLayers.Filter.Comparison.LESS_THAN_OR_EQUAL_TO,
                property: name,
                value: value
            }));
        } else if (type == 'T') {    //equals with words separated by spaces that must be all present
            var splitted = value.split(/ |-|_|\./);
            for (var i = 0; i < splitted.length; ++i) {
                filters.push(new OpenLayers.Filter.Comparison({
                    type: OpenLayers.Filter.Comparison.EQUAL_TO,
                    property: name,
                    value: splitted[i]
                }));
            }
        } else if (type == 'B') {    //boolean
			if(name == 'toEdit') {
				searchTools.toEditFilter(filters);
			} else {
	            filters.push(new OpenLayers.Filter.Comparison({
	                type: OpenLayers.Filter.Comparison.EQUAL_TO,
	                property: name,
	                value: value ? 'y' : 'n'
	            }));
			}
        } else if (type == 'N') {    //Numeric
            filters.push(new OpenLayers.Filter.Comparison({
                type: OpenLayers.Filter.Comparison.BETWEEN,
                property: name,
                lowerBoundary: parseInt(value),
                upperBoundary: parseInt(value)
            }));
        } else if (type == 'V') { //field name specified in the value, separated by a '/' with the value
            var subField = value.match("^([^/]+)/(.*)$");
            filters.push(new OpenLayers.Filter.Comparison({
                type: OpenLayers.Filter.Comparison.EQUAL_TO,
                property: "similarity",
                value: "1.0"
            }));
            filters.push(new OpenLayers.Filter.Comparison({
                type: OpenLayers.Filter.Comparison.EQUAL_TO,
                property: subField[1],
                value: subField[2]
            }));
            filters.push(new OpenLayers.Filter.Comparison({
                type: OpenLayers.Filter.Comparison.EQUAL_TO,
                property: "similarity",
                value: defaultSimilarity
            }));
        } else {
            alert("Cannot parse " + type);
        }
    },
	toEditFilter: function(filters) {
		var toEditFilters = [];
		toEditFilters.push(new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "_owner",
            value: geocat.session.userId
        }));
		for(i=0; geocat.session.groups.length > i; i++) {
			toEditFilters.push(new OpenLayers.Filter.Comparison({
	            type: OpenLayers.Filter.Comparison.EQUAL_TO,
	            property: "_op2",
	            value: geocat.session.groups[i]
	        }));
		}
		
		filters.push(new OpenLayers.Filter.Logical({
            type: OpenLayers.Filter.Logical.OR,
            filters: toEditFilters
        }));

		filters.push(new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "_isHarvested",
            value: 'n'
        }));
		
	},
    sortByMappings: {
        relevance: {name:'relevance', order: 'DESC'},
        popularity: {name: 'popularity', order: 'DESC'},
        revisionDate: {name: '_revisionDate', order: 'DESC'},
        dateStamp: {name: 'dateStamp', order: 'DESC'},
        title: {name: '_title', order: 'A'}
    },

    buildCSWQueryPOST: function(filter, startRecord, sortBy) {

        var result = '<?xml version="1.0" encoding="UTF-8"?>\n' +
                     '<csw:GetRecords xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" service="CSW" version="2.0.2" resultType="results_with_summary" startPosition="' + startRecord + '" maxRecords="' + geocat.nbResultPerPage + '">\n' +
                     '  <csw:Query typeNames="csw:Record">\n' +
                     '    <csw:ElementSetName>full</csw:ElementSetName>\n';

        if (sortBy) {
            var searchInfo = searchTools.sortByMappings[sortBy];
            result += '    <ogc:SortBy xmlns:ogc="http://www.opengis.net/ogc">\n' +
                      '      <ogc:SortProperty>\n' +
                      '        <ogc:PropertyName>' + searchInfo.name + '</ogc:PropertyName>\n' +
                      '        <ogc:SortOrder>' + searchInfo.order + '</ogc:SortOrder>\n' +
                      '      </ogc:SortProperty>\n' +
                      '    </ogc:SortBy>\n';
        }

        if (filter) {
            var filterXML = new OpenLayers.Format.XML().write(
                    new OpenLayers.Format.Filter().write(filter));
            filterXML = filterXML.replace(/^<\?xml[^?]*\?>/, "");
            result += '    <csw:Constraint version="1.0.0">\n';
            result += filterXML;
            result += '    </csw:Constraint>\n';
        }

        result += '  </csw:Query>\n' +
                  '</csw:GetRecords>';

        return result;
    },

    buildCSWQueryGET: function(filter, startRecord, sortBy) {

        var result = {
            request: 'GetRecords',
            service:'CSW',
            version: '2.0.2',
            resultType:'results_with_summary',
            namespace:'csw:http://www.opengis.net/cat/csw/2.0.2',
            typeNames:'csw:Record',
            constraintLanguage: 'FILTER',
            constraint_language_version: '1.0.0',
            elementSetName: 'full',
            startPosition: startRecord,
            maxRecords: geocat.nbResultPerPage
        };

        if (sortBy) {
            var searchInfo = searchTools.sortByMappings[sortBy];
            result.sortBy = searchInfo.name + ":" + searchInfo.order;
        }

        if (filter) {
            var filterXML = new OpenLayers.Format.XML().write(
                    new OpenLayers.Format.Filter().write(filter));
            filterXML = filterXML.replace(/^<\?xml[^?]*\?>/, "");
            result.constraint = filterXML;
        }

        return result;
    },

    transformXML: function(csw, xsl, targetEl) {
        var cswXsl = searchTools.loadXMLString(xsl);
        csw = searchTools.loadXMLString(csw); // use same parser to convert xml and xsl strings to documents
        if (window.ActiveXObject) {
            //code for IE
            targetEl.innerHTML = csw.transformNode(cswXsl);
        } else if (document.implementation && document.implementation.createDocument) {
            // code for Mozilla, Firefox, Opera, etc.
            var xsltProcessor = new XSLTProcessor();
            xsltProcessor.importStylesheet(cswXsl);
            var resultDocument = xsltProcessor.transformToFragment(csw, document);
            targetEl.innerHTML = "";
            targetEl.appendChild(resultDocument);
        } else {
            alert("No browser XSL support");
        }
    },

    loadXMLString: function(text) {
        var out;
        if (window.ActiveXObject) {
            // code for IE
            out = new ActiveXObject("Msxml2.DOMDocument.6.0");
            out.async = true;
            out.loadXML(text);
        } else if (document.implementation
                && document.implementation.createDocument) {
            // code for Mozilla, Firefox, Opera, etc.
            var parser = new DOMParser();
            out = parser.parseFromString(text, "text/xml");
        } else {
            alert('Your browser cannot handle this script');
        }
        return out;
    },

    readWFS: function(url, ns, type, properties, filter, queryOpts) {
        var queryPrefix = '<?xml version="1.0" ?>\n' +
                          '<wfs:GetFeature service="WFS" version="1.0.0"\n' +
                          '                xmlns:wfs="http://www.opengis.net/wfs"\n' +
                          '                xmlns:ogc="http://www.opengis.net/ogc"\n' +
                          '                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"\n' +
                          '                xsi:schemaLocation="http://www.opengis.net/wfs ../wfs/1.0.0/WFS-basic.xsd">\n' +
                          '  <wfs:Query typeName="' + ns + ':' + type + '">\n';
        var queryPostfix = '  </wfs:Query>\n' +
                           '</wfs:GetFeature>';
        var property = '';
        for (var i = 0; i < properties.length; ++i) {
            property += '    <ogc:PropertyName>' + ns + ':' + properties[i] + '</ogc:PropertyName>\n';
        }

        var filters = "";
        if (filter) {
            filters = new OpenLayers.Format.XML().write(
                    new OpenLayers.Format.Filter().write(filter));
        }

        var opts = {
            url: url,
            data: queryPrefix + property + filters + queryPostfix
        };
        OpenLayers.Util.applyDefaults(opts, queryOpts);
        return OpenLayers.Request.POST(opts);
    },

    getFormValues: function(form) {
        var result = {};
        form.cascade(function(cur) {
            if (cur.disabled != true) {
                if (cur.isXType('boxselect')) {
                    if (cur.getValue && cur.getValue()) {
                        result[cur.getName()] = cur.getValue();
                    }
                } else if (cur.isXType('combo')) {
                    if (cur.getValue && cur.getValue()) {
                        result[cur.getName()] = cur.getValue();
                    }
                } else if (cur.isXType('fieldset')) {
                    if (cur.checkbox) {
                        //support for checkboxes in the fieldset title
                        result[cur.checkboxName] = !cur.collapsed;
                    }
                } else if (cur.isXType('radiogroup')) {
                    //a radiogroup is not a container. So cascade doesn't visit it... don't ask...
                    var first = cur.items.get(0);
                    result[first.getName()] = first.getGroupValue();
                } else if (cur.isXType('checkbox')) {
                    result[cur.getName()] = cur.getValue();
                } else if (cur.isXType('datefield')) {
                    if (cur.getValue() != "") {
                        result[cur.getName()] = cur.getValue().format('Y-m-d') + (cur.postfix?cur.postfix:'');
                    }
                } else if (cur.getName) {
                    if (cur.getValue && cur.getValue() != "") {
                        result[cur.getName()] = cur.getValue();
                    }
                }
            }
            return true;
        });
        return result;
    },

    /**
     * A class for a geometry referenced by its ID
     */
    MultiPolygonReference: OpenLayers.Class(OpenLayers.Geometry, {
        id: null,

        initialize: function(id) {
            this.id = id;
        },

        CLASS_NAME: "searchTools.MultiPolygonReference"
    }),

    mainProj: new OpenLayers.Projection("EPSG:4326"),
    alternateProj: new OpenLayers.Projection("EPSG:21781"),

    /**
     * APIFunction: initMapDiv
     * Take all the DIVs of class extentViewer and places a map within. If it
     * contains a single div, look in it content for a geometry in WKT format
     * and add it in the map.
     *
     * The DIV can have some attributes:
     *   - edit: if 'true', add edition tools
     *   - target_polygon: the id of the input that must be updated with the GML
     *                     content of the polygon being edited
     *   - watched_bbox: the coma separated 4 ids of the input field (east, south,
     *                   west, north) to listen for modifications
     */
    initMapDiv: function () {
        var viewers, idFunc;

        // some pages have prototype and other have access to ext so do a check
        // and choose the one that is available
        if (Ext) {
            viewers = Ext.DomQuery.select('.extentViewer');
            idFunc = Ext.id;
        } else {
            viewers = $$('.extentViewer');
            idFunc = identify();
        }

        for (var idx = 0; idx < viewers.length; ++idx) {
            var viewer = viewers[idx];

            var targetPolygon = viewer.getAttribute("target_polygon");
            var watchedBbox = viewer.getAttribute("watched_bbox");
            var edit = viewer.getAttribute("edit") == 'true';
            var eltRef = viewer.getAttribute("elt_ref");

            var children = viewer.childNodes;

            var tmp = [];

            for (var i = 0; i < children.length; i++) {
                if (children[i].nodeType == 1) {
                    tmp.push(children[i]);
                }
            }

            children = tmp;

            if (children.length > 1) continue;

            // Creates map component
            var id;
            if (Ext) {
                id = Ext.id(viewer);
            } else {
                id = viewer.identify();
            }

            var mapCmp = new MapComponent(id, {
                panelWidth: edit ? 500 : 380,
                drawPanel: edit,
                displayLayertree: false,
                enableNavigation: edit
            });
            var drawCmp = new MapDrawComponent(mapCmp.map, {
                toolbar: mapCmp.toolbar,
                hideDrawControls: !edit,
                hideDrawPolygon: targetPolygon == '',
                controlOptions: {
                    title: 'Draw shape',
                    featureAdded: function(feature) {
                        if (this.targetPolygon != '') {
                            var mainProj = new OpenLayers.Projection("EPSG:4326");
                            var alternateProj = new OpenLayers.Projection("EPSG:21781");
                            var writer = new OpenLayers.Format.GML.v3({
                                externalProjection: mainProj,
                                internalProjection: alternateProj
                            });
                            var child = writer.writeNode("feature:_geometry", feature.geometry);
                            var gml = OpenLayers.Format.XML.prototype.write.call(writer, child.firstChild);
                            document.getElementById('_X' + this.targetPolygon).value = gml;
                        } else if (this.targetBbox != '') {
                            // a box was drawn, update the input text and input
                            // hidden fields
                            var bounds = feature.geometry.getBounds();
                            var boundsReproj = bounds.clone().transform(
                                searchTools.alternateProj, searchTools.mainProj);

                            var b; digits;

                            var wsen = this.targetBbox.split(',');
                            var wsenEl = searchTools.lookupWsen(wsen);

                            if (Ext.get("ch03_" + this.eltRef) && Ext.get("ch03_" + this.eltRef).dom && Ext.get("ch03_" + this.eltRef).dom.checked) {
                                b = bounds;
                                digits = 0;
                                type = "ch03";
                                Ext.get("_" + wsen[0]).dom.value = bounds.left+"ch";
                                Ext.get("_" + wsen[1]).dom.value = bounds.bottom+"ch";
                                Ext.get("_" + wsen[2]).dom.value = bounds.right+"ch";
                                Ext.get("_" + wsen[3]).dom.value = bounds.top+"ch";

                            } else {
                                b = boundsReproj;
                                digits = 3;
                                type = "wgs84";
                                Ext.get("_" + wsen[0]).dom.value = boundsReproj.left;
                                Ext.get("_" + wsen[1]).dom.value = boundsReproj.bottom;
                                Ext.get("_" + wsen[2]).dom.value = boundsReproj.right;
                                Ext.get("_" + wsen[3]).dom.value = boundsReproj.top;
                            }


                            wsenEl[0][type] = b.left.toFixed(digits);
                            wsenEl[1][type] = b.bottom.toFixed(digits);
                            wsenEl[2][type] = b.right.toFixed(digits);
                            wsenEl[3][type] = b.top.toFixed(digits);

                            wsenEl[0].dom.value = b.left.toFixed(digits);
                            wsenEl[1].dom.value = b.bottom.toFixed(digits);
                            wsenEl[2].dom.value = b.right.toFixed(digits);
                            wsenEl[3].dom.value = b.top.toFixed(digits);
                        }
                    },
                    scope: {
                        targetPolygon: targetPolygon,
                        targetBbox: watchedBbox,
                        eltRef: eltRef
                    }
                },
                activate: false,
                onClearFeatures: OpenLayers.Function.bind(function() {
                    var wsen = this.targetBbox.split(',');

                    // update the text fields
                    Ext.get(wsen[0]).dom.value = '';
                    Ext.get(wsen[1]).dom.value = '';
                    Ext.get(wsen[2]).dom.value = '';
                    Ext.get(wsen[3]).dom.value = '';

                    // update the input fields
                    Ext.get("_" + wsen[0]).dom.value = '';
                    Ext.get("_" + wsen[1]).dom.value = '';
                    Ext.get("_" + wsen[2]).dom.value = '';
                    Ext.get("_" + wsen[3]).dom.value = '';
                }, {targetBbox: watchedBbox})
            });

            if (children.length > 0) {
                drawCmp.readFeature(children[0].innerHTML, {
                    format: 'WKT',
                    zoomToFeatures: true,
                    from: searchTools.mainProj,
                    to: searchTools.alternateProj
                });
                /* Sometimes the dom element used as container for the map has a height/width 
                of 0 (zero). So the map has a wrong size because the js is executed before 
                the page content has finished rendering DESPITE the onload/ready event which is 
                *supposed* taking care of such situtation (and clearly fail with the complex 
                hierarchy of element in GN pages).
                One solution is to trigger the map init later, but how much time is needed may vary
                from one case to another so it is not reliable.
                Another solution would be to attach a onresize event on the container to update the map 
                when the container get its final size, but strangly the onresize event isnt 
                fired when the container get its final size.
                So the solution implemented here is simply a recuring timed call to check the container 
                size and trigger an update of the map when it is finally ready */
                if (viewer.offsetWidth == 0 || viewer.offsetHeight == 0) {
                    if (Ext) {
                        var task = new Ext.util.DelayedTask(function(){
                            var el = Ext.get(id);
                            if (el.getWidth() == 0 || el.getHeight() == 0 ) {
                                task.delay(100);
                            } else {
                                // container is ready, update map
                                mapCmp.map.updateSize();
                            }
                        });
                        // Wait 100ms 
                        task.delay(100);
                    } else {
                        // in which cases dont we have Ext ??
                        // TODO implement a non ext timout system
                    }
                }
            }

            if (watchedBbox != '') {
                // update the input text fields from the hidden fields

                var wsen = watchedBbox.split(',');


                var wsenEl = searchTools.lookupWsen(wsen);

                var w = wsenEl[0].getValue();
                var s = wsenEl[1].getValue();
                var e = wsenEl[2].getValue();
                var n = wsenEl[3].getValue();

                var l = w != "" ? w : "0";
                var b = s != "" ? s : "0";
                var r = e != "" ? e : "0";
                var t = n != "" ? n : "0";

                var bounds = OpenLayers.Bounds.fromString(
                    l + "," + b + "," + r + "," + t
                );

                wsenEl[0].wgs84 = bounds.left.toFixed(3);
                wsenEl[1].wgs84 = bounds.bottom.toFixed(3);
                wsenEl[2].wgs84 = bounds.right.toFixed(3);
                wsenEl[3].wgs84 = bounds.top.toFixed(3);

                var cheBounds;
                var nativeCoordsDiv;
                
                
                var nativeEl = Ext.get("native_"+eltRef);
                if (nativeEl) {
                    nativeEl = nativeEl.dom;
                    if(nativeEl.innerText !== undefined) {
                        nativeCoordsDiv = nativeEl.innerText;
                    } else {
                        nativeCoordsDiv = nativeEl.textContent;
                    }
                }
                if(nativeCoordsDiv === undefined) {
                    nativeCoordsDiv = "";
                } else {
                    nativeCoordsDiv = nativeCoordsDiv.trim();
                }
                if(nativeCoordsDiv.length > 0) {
                    var nativeCoordsString = nativeCoordsDiv.split(":")[1].trim();
                    cheBounds = OpenLayers.Bounds.fromString(nativeCoordsString);
                } else {
                    cheBounds = bounds.clone();
                    cheBounds.transform(searchTools.mainProj, searchTools.alternateProj);
                }

                wsenEl[0].ch03 = cheBounds.left.toFixed(0);
                wsenEl[1].ch03 = cheBounds.bottom.toFixed(0);
                wsenEl[2].ch03 = cheBounds.right.toFixed(0);
                wsenEl[3].ch03 = cheBounds.top.toFixed(0);

                var digits = 3;
                var ch03Radio = Ext.get("ch03_" + eltRef) && Ext.get("ch03_" + eltRef).dom;

                if (ch03Radio && ch03Radio.checked) {
                    digits = 0;

                    bounds = cheBounds;
                    if(nativeCoordsDiv.length > 0) {
                        Ext.get("_" + wsen[0]).dom.value = wsenEl[0].ch03+"ch";
                        Ext.get("_" + wsen[1]).dom.value = wsenEl[1].ch03+"ch";
                        Ext.get("_" + wsen[2]).dom.value = wsenEl[2].ch03+"ch";
                        Ext.get("_" + wsen[3]).dom.value = wsenEl[3].ch03+"ch";
                    }
                }

                if (w != "") {
                    w = bounds.left.toFixed(digits) + "";
                }
                Ext.get(wsen[0]).dom.value = w;
                if (s != "") {
                    s = bounds.bottom.toFixed(digits) + "";
                }
                Ext.get(wsen[1]).dom.value = s;
                if (e != "") {
                    e = bounds.right.toFixed(digits) + "";
                }
                Ext.get(wsen[2]).dom.value = e;
                if (n != "") {
                    n = bounds.top.toFixed(digits) + "";
                }
                Ext.get(wsen[3]).dom.value = n;

                // watch the input text fields, i.e. update the input
                // hiddel fields when the input text fields are changed
                searchTools.watchBbox(wsen, eltRef, drawCmp);
                searchTools.watchRadios(wsen, eltRef);
            }
        }
    },
    lookupWsen: function(wsen) {
        return [ Ext.get(wsen[0]),
                 Ext.get(wsen[1]),
                 Ext.get(wsen[2]),
                 Ext.get(wsen[3]) ]
    },
    watchBbox: function(wsen, eltRef, drawCmp) {
        for (var i = 0; i < wsen.length; ++i) {
            // register a "change" listen on each input text element
            OpenLayers.Event.observe(wsen[i], 'change', function() {
                // update the value of the corresponding input hidden elements
                // and update the box drawn on the map
                var wsenEl = searchTools.lookupWsen(wsen);

                var values = new Array(wsen.length);
                values[0] = wsenEl[0].getValue();
                values[1] = wsenEl[1].getValue();
                values[2] = wsenEl[2].getValue();
                values[3] = wsenEl[3].getValue();

                if (Ext.get("ch03_" + eltRef) && Ext.get("ch03_" + eltRef).dom && Ext.get("ch03_" + eltRef).dom.checked) {
                    wsenEl[0].ch03 = values[0];
                    wsenEl[1].ch03 = values[1];
                    wsenEl[2].ch03 = values[2];
                    wsenEl[3].ch03 = values[3];

                    wsenEl[0].wgs84 = undefined;
                    wsenEl[1].wgs84 = undefined;
                    wsenEl[2].wgs84 = undefined;
                    wsenEl[3].wgs84 = undefined;


                    Ext.get("_" + wsen[0]).dom.value = wsenEl[0].getValue()+"ch";
                    Ext.get("_" + wsen[1]).dom.value = wsenEl[1].getValue()+"ch";
                    Ext.get("_" + wsen[2]).dom.value = wsenEl[2].getValue()+"ch";
                    Ext.get("_" + wsen[3]).dom.value = wsenEl[3].getValue()+"ch";

                } else {
                    wsenEl[0].ch03 = undefined;
                    wsenEl[1].ch03 = undefined;
                    wsenEl[2].ch03 = undefined;
                    wsenEl[3].ch03 = undefined;

                    wsenEl[0].wgs84 = values[0];
                    wsenEl[1].wgs84 = values[1];
                    wsenEl[2].wgs84 = values[2];
                    wsenEl[3].wgs84 = values[3];

                    Ext.get("_" + wsen[0]).dom.value = wsenEl[0].getValue();
                    Ext.get("_" + wsen[1]).dom.value = wsenEl[1].getValue();
                    Ext.get("_" + wsen[2]).dom.value = wsenEl[2].getValue();
                    Ext.get("_" + wsen[3]).dom.value = wsenEl[3].getValue();



                    // the bounds read from the input text fields are
                    // wgs84, transform the values passed to updateBBox
                    var b = OpenLayers.Bounds.fromArray(values);
                    b.transform(
                        searchTools.mainProj, searchTools.alternateProj);
                    values[0] = b.left;
                    values[1] = b.bottom;
                    values[2] = b.right;
                    values[3] = b.top;
                }

                drawCmp.updateBbox.apply(drawCmp, values);
            });
        }
    },

    watchRadios: function(wsen, eltRef) {

        var currProj = Ext.get("ch03_" + eltRef) && Ext.get("ch03_" + eltRef).dom && Ext.get("ch03_" + eltRef).dom.checked ?
                       searchTools.alternateProj : searchTools.mainProj;

        function updateInputTextFields(toProj, digits,type) {
            if (toProj.equals(currProj)) {
                return;
            }

            var wsenEl = searchTools.lookupWsen(wsen);

            var w;
            var s;
            var e;
            var n;

            if(wsenEl[0][type] === undefined) {
                w = wsenEl[0].getValue();
                s = wsenEl[1].getValue();
                e = wsenEl[2].getValue();
                n = wsenEl[3].getValue();

                var l = w != "" ? w : "0";
                var b = s != "" ? s : "0";
                var r = e != "" ? e : "0";
                var t = n != "" ? n : "0";

                var bounds = OpenLayers.Bounds.fromString(
                    l + "," + b + "," + r + "," + t
                );

                if (!toProj.equals(searchTools.mainProj)) {
                    bounds.transform(searchTools.mainProj, toProj);
                } else {
                    bounds.transform(searchTools.alternateProj, toProj);
                }

                if (w != "") {
                    w = bounds.left.toFixed(digits) + "";
                }
                if (s != "") {
                    s = bounds.bottom.toFixed(digits) + "";
                }
                if (e != "") {
                    e = bounds.right.toFixed(digits) + "";
                }
                if (n != "") {
                    n = bounds.top.toFixed(digits) + "";
                }
            } else {
                w = wsenEl[0][type];
                s = wsenEl[1][type];
                e = wsenEl[2][type];
                n = wsenEl[3][type];
            }

            wsenEl[0].dom.value = w;
            wsenEl[1].dom.value = s;
            wsenEl[2].dom.value = e;
            wsenEl[3].dom.value = n;

            currProj = toProj;
        }

        Ext.get("ch03_" + eltRef) && Ext.get("ch03_" + eltRef).on('click', function() {
            digits = 0;
            updateInputTextFields(searchTools.alternateProj, digits,"ch03");
        });

        Ext.get("wgs84_" + eltRef) && Ext.get("wgs84_" + eltRef).on('click', function() {
            digits = 3;
            updateInputTextFields(searchTools.mainProj, digits,"wgs84");
        });
    }
};

searchTools.prev_geometry = OpenLayers.Format.GML.Base.prototype.writers.feature._geometry;
OpenLayers.Format.GML.Base.prototype.writers.feature._geometry = function(geometry) {
    if (geometry.CLASS_NAME == "searchTools.MultiPolygonReference") {
        var gml = this.createElementNS(this.namespaces.gml, "gml:MultiPolygon");
        var gmlNode = this.createElementNS(this.namespaces.gml, "gml:MultiPolygon");
        gmlNode.setAttribute("gml:id", geometry.id);
        gml.appendChild(gmlNode);
        return gml;
    } else {
        return searchTools.prev_geometry.apply(this, arguments);
    }
};
//OpenLayers.Format.GML.prototype.buildGeometry.multipolygonreference = function(geometry) {
//    var gml = this.createElementNS(this.gmlns, "gml:MultiPolygon");
//    gml.setAttribute("gml:id", geometry.id);
//    return gml;
//};

OpenLayers.Format.Filter.v1.prototype.filterMap.WITHIN =
OpenLayers.Filter.Spatial.WITHIN = 'WITHIN';

OpenLayers.Format.Filter.v1.prototype.filterMap.CONTAINS =
OpenLayers.Filter.Spatial.CONTAINS = 'CONTAINS';

OpenLayers.Format.Filter.v1.prototype.writers.ogc.WITHIN = function(filter) {
    var node = this.createElementNSPlus("ogc:Within");
    this.writeNode("PropertyName", filter, node);
    var child = this.writeNode("feature:_geometry", filter.value);
    node.appendChild(child.firstChild);
    return node;
};

OpenLayers.Format.Filter.v1.prototype.writers.ogc.CONTAINS = function(filter) {
    var node = this.createElementNSPlus("ogc:Contains");
    this.writeNode("PropertyName", filter, node);
    var child = this.writeNode("feature:_geometry", filter.value);
    node.appendChild(child.firstChild);
    return node;
};
