/**
 * CSWSearchTools is used to build CSW queries from a form
 * and send GET or POST queries to a CSW server.
 * Results could be processed using OpenLayers.Format.CSWGetRecords
 * 
 * Example:
 * <pre>
 *      CSWSearchTools.doCSWQueryFromForm(this.id, url, 1, this.showResults, null, Ext.emptyFn);
 *      
 *      ...
 *      
 *      showResults: function(response) {       
 *        var getRecordsFormat = new OpenLayers.Format.CSWGetRecords();
 *        var r = getRecordsFormat.read(response.responseText);
 *        var values = r.records;
 *        ...
 *      }
 * </pre>
 * 
 */
var CSWSearchTools = {
	/**
	 * Default method used. 
	 */
    cswMethod: 'POST',

    /**
     * Define results mode. "results_with_summary" is used
     * to have GeoNetwork specific result mode.
     */
    resultsMode: 'results',
    //resultsMode: 'results_with_summary',
    
    /**
     * Send a GET or POST query to CSW server url. A form is composed of one
     * or more fields which are processed by buildCSWQueryFromForm.
     * 
     */
    doCSWQueryFromForm: function(formId, url, recordNum, onSuccess, onFailure, addFilters) {
        var query = CSWSearchTools.buildCSWQueryFromForm(CSWSearchTools.cswMethod, Ext.getCmp(formId), recordNum, app.sortBy, addFilters);
        if (CSWSearchTools.cswMethod == 'POST') {
            var getQuery = CSWSearchTools.buildCSWQueryFromForm('GET', Ext.getCmp(formId), recordNum, app.sortBy, addFilters);
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
     *  B: Booleancon
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
        var values = CSWSearchTools.getFormValues(form);
        var filters = [];

        CSWSearchTools.addFiltersFromPropertyMap(values, filters);

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
            return CSWSearchTools.buildCSWQueryPOST(and, startRecord, sortBy);
        } else {
            return CSWSearchTools.buildCSWQueryGET(and, startRecord, sortBy);
        }
    },

    /**
     * Add property in an OGC filter 
     */
    addFiltersFromPropertyMap: function(values, filters) {

        // TODO : we should probably extend Filter and add similarity as an attribute
        // to CSW clause (like the matchCase attribute).
    	var defaultSimilarity = ".8";
        var similarity = values['E_similarity'];
        if (similarity != null) {
            defaultSimilarity = values['E_similarity'];
            CSWSearchTools.addFilter(filters, 'E_similarity', defaultSimilarity, defaultSimilarity);
        }

        for (var key in values) {
            var value = values[key];
            if (value != "" && key != 'E_similarity') {
                CSWSearchTools.addFilter(filters, key, value, defaultSimilarity);
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
                    CSWSearchTools.addFilterImpl(values.length>1?or:filters, field[2], field[3], values[i], defaultSimilarity);
                }
                if (values.length > 1) {
                    filters.push(new OpenLayers.Filter.Logical({
                        type: OpenLayers.Filter.Logical.OR,
                        filters: or
                    }));
                }
            } else {
                CSWSearchTools.addFilterImpl(filters, field[2], field[3], value, defaultSimilarity);
            }
        }
    },

    /**
     * Build filter according to type name define by form fields name.
     */
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
            var splitted = value.split(" ");
            for (var i = 0; i < splitted.length; ++i) {
                filters.push(new OpenLayers.Filter.Comparison({
                    type: OpenLayers.Filter.Comparison.EQUAL_TO,
                    property: name,
                    value: splitted[i]
                }));
            }
        } else if (type == 'B') {    //boolean
            filters.push(new OpenLayers.Filter.Comparison({
                type: OpenLayers.Filter.Comparison.EQUAL_TO,
                property: name,
                value: value ? 1 : 0
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

    /**
     * Define default sort order to use for each kind of sort field.
     */
    sortByMappings: {
        relevance: {name:'relevance', order: 'D'},
        rating: {name: 'rating', order: 'D'},
        popularity: {name: 'popularity', order: 'D'},
        date: {name: 'date', order: 'D'},
        title: {name: 'title', order: 'A'}
    },

    /**
     * Create a query to POST based on an OGC filter.
     */
    buildCSWQueryPOST: function(filter, startRecord, sortBy) {

        var result = '<?xml version="1.0" encoding="UTF-8"?>\n' +
                     '<csw:GetRecords xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" service="CSW" version="2.0.2" resultType="'+
                     this.resultsMode +'" startPosition="' + startRecord + '" maxRecords="' + app.nbResultPerPage + '">\n' +
                     '  <csw:Query typeNames="csw:Record">\n' +
                     '    <csw:ElementSetName>full</csw:ElementSetName>\n';

        if (sortBy) {
            var searchInfo = CSWSearchTools.sortByMappings[sortBy];
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

    /**
     * Build a GET query based on an OGC filter.
     */
    buildCSWQueryGET: function(filter, startRecord, sortBy) {

        var result = {
            request: 'GetRecords',
            service:'CSW',
            version: '2.0.2',
            resultType: this.resultsMode,
            namespace:'csw:http://www.opengis.net/cat/csw/2.0.2',
            typeNames:'csw:Record',
            constraintLanguage: 'FILTER',
            constraint_language_version: '1.1.0',
            elementSetName: 'full',
            startPosition: startRecord,
            maxRecords: app.nbResultPerPage
        };

        if (sortBy) {
            var searchInfo = CSWSearchTools.sortByMappings[sortBy];
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

    /**
     * According to field type, get form values.
     */
    getFormValues: function(form) {
        var result = form.getForm().getValues() || {};
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
    }
};