/*
 * Copyright (C) 2001-2011 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * 
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
Ext.namespace("GeoNetwork.util");


/** api: (define)
 *  module = GeoNetwork.util
 *  class = CSWSearchTools
 */
/** api: example
 *  CSWSearchTools is used to build CSW queries from an Ext form
 *  and send GET or POST queries to a CSW server.
 *
 *  Results could be:
 *
 *   * processed using OpenLayers.Format.CSWGetRecords using a onSucess function
 *   * or automatically loaded into the catalogue results store
 *
 *  .. code-block:: javascript
 *
 *      GeoNetwork.util.CSWSearchTools.doCSWQueryFromForm(this.formId, this.catalogue, 1, this.showResults, null, Ext.emptyFn);
 *      ...
 *      showResults: function(response) {
 *        var getRecordsFormat = new OpenLayers.Format.CSWGetRecords.v2_0_2();
 *        var r = getRecordsFormat.read(response.responseText);
 *        var values = r.records;
 *        ...
 *      }
 *
 */
GeoNetwork.util.CSWSearchTools = {
    /**
     * Default method used.
     */
    cswMethod: 'POST',
    
    /**
     * Define results mode. "results_with_summary" is used
     * to have GeoNetwork specific result mode.
     */
    //resultsMode: 'results',
    resultsMode: 'results_with_summary',
    sortBy: '',
    maxRecords: '50',
    
    /** api:method[doCSWQueryFromForm]
     *
     *  :param formId: ``String``  Form identifier
     *  :param cat: ``GeoNetwork.Catalogue``    Catalogue to query
     *  :param recordNum: ``Number``    Optional start record number
     *  :param onSuccess: ``Function``  Optional function to trigger in case of success
     *  :param onFailure: ``Function``  Optional function to trigger in case of failure
     *  :param addFilters: ``Function``  Not really used ?
     *
     *
     *  Send a GET or POST query to CSW server url. A form is composed of one
     *  or more fields which are processed by buildCSWQueryFromForm.
     *
     */
    doCSWQueryFromForm: function(formId, cat, recordNum, onSuccess, onFailure, addFilters){
        var url = cat.services.csw;
        var query = GeoNetwork.util.CSWSearchTools.buildCSWQueryFromForm(GeoNetwork.util.CSWSearchTools.cswMethod, Ext.getCmp(formId), recordNum, GeoNetwork.util.CSWSearchTools.sortBy, addFilters);
        
        if (GeoNetwork.util.CSWSearchTools.cswMethod === 'POST') {
            var getQuery = GeoNetwork.util.CSWSearchTools.buildCSWQueryFromForm('GET', Ext.getCmp(formId), recordNum, GeoNetwork.util.CSWSearchTools.sortBy, addFilters);
            OpenLayers.Request.POST({
                url: url,
                data: query,
                success: function(result){
                    // TODO : improve 
                    var getRecordsFormat = new OpenLayers.Format.CSWGetRecords.v2_0_2();
                    cat.currentRecords = getRecordsFormat.read(result.responseText);
                    
                    var values = cat.currentRecords.records;
                    if (values.length > 0) {
                        cat.metadataCSWStore.loadData(cat.currentRecords);
                    }
                    
                    if (onSuccess) {
                        onSuccess(result, getQuery);
                    }
                },
                failure: onFailure
            });
        } else {
            OpenLayers.Request.GET({
                url: url,
                params: query,
                success: function(result){
                    onSuccess(result, query);
                },
                failure: onFailure
            });
        }
    },
    
    
    /** api:method[buildCSWQueryFromForm]
     *  :param method: GET or POST method
     *  :param formId: ``String``  Form identifier
     *  :param startRecord: ``Number``    Optional start record number
     *  :param sortBy: ``String``    Optional sort by option
     *  :param addFilters: ``Function``  Not really used ?
     *
     *
     *  Build a CSW query.
     *
     *  It's assumed the field name follow a convention. It starts with a letter,
     *  followed by an underscore. The letters are:
     *
     *  * S: Starts with
     *  * C: Contains
     *  * B: Booleancon
     *  * E: Equals
     *  * T: Equals with words separated by spaces that must be all present
     *  * E##: Equals with a specified similarity
     *  * G: Geometry by ID
     *  * V: field name specified in the value, separated by a '/' with the value
     *  * >=: bigger or equal
     *  * <=: smaller or equal
     *
     *  If it starts with '[', it means its a list of values separated by ',' (ORed)
     *
     */
    buildCSWQueryFromForm: function(method, form, startRecord, sortBy, addFilters){
        var values = GeoNetwork.util.CSWSearchTools.getFormValues(form);
        var filters = [];
        
        GeoNetwork.util.CSWSearchTools.addFiltersFromPropertyMap(values, filters);
        
        addFilters(values, filters);
        if (filters.length === 0) {
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
        
        if (method === 'POST') {
            return GeoNetwork.util.CSWSearchTools.buildCSWQueryPOST(and, startRecord, sortBy);
        } else {
            return GeoNetwork.util.CSWSearchTools.buildCSWQueryGET(and, startRecord, sortBy);
        }
    },
    
    /** private: method[addFiltersFromPropertyMap]
     *  Add property in an OGC filter
     */
    addFiltersFromPropertyMap: function(values, filters){
    
        // TODO : we should probably extend Filter and add similarity as an attribute
        // to CSW clause (like the matchCase attribute).
        var defaultSimilarity = ".8", 
            similarity = values.E_similarity,
            key;
        if (similarity !== null && similarity !== undefined) {
            defaultSimilarity = values.E_similarity;
            GeoNetwork.util.CSWSearchTools.addFilter(filters, 'E_similarity', defaultSimilarity, defaultSimilarity);
        }

        for (key in values) {
            if (values.hasOwnProperty(key)) {
                var value = values[key];
                if (value !== "" && key !== 'E_similarity') {
                    GeoNetwork.util.CSWSearchTools.addFilter(filters, key, value, defaultSimilarity);
                }
            }
        }
    },
    /**  private: method[addFilter]
     *
     */
    addFilter: function(filters, key, value, defaultSimilarity){
        var field = key.match("^(\\[?)([^_]+)_(.*)$"),
            or = [],
            i;
            
        if (field) {
            if (field[1] === '[') {
                var values = value.split(",");
                for (i = 0; i < values.length; ++i) {
                    GeoNetwork.util.CSWSearchTools.addFilterImpl(values.length > 1 ? or : filters, field[2], field[3], values[i], defaultSimilarity);
                }
                if (values.length > 1) {
                    filters.push(new OpenLayers.Filter.Logical({
                        type: OpenLayers.Filter.Logical.OR,
                        filters: or
                    }));
                }
            } else {
                GeoNetwork.util.CSWSearchTools.addFilterImpl(filters, field[2], field[3], value, defaultSimilarity);
            }
        }
    },
    
    /** private: method[addFilterImpl]
     *  Build filter according to type name define by form fields name.
     */
    addFilterImpl: function(filters, type, name, value, defaultSimilarity){
        var i;
        
        if (type === 'S') { //starts with
            filters.push(new OpenLayers.Filter.Comparison({
                type: OpenLayers.Filter.Comparison.LIKE,
                property: name,
                value: value + ".*"
            }));
        } else if (type === 'C') { //contains
            filters.push(new OpenLayers.Filter.Comparison({
                type: OpenLayers.Filter.Comparison.LIKE,
                property: name,
                value: ".*" + value + ".*"
            }));
        } else if (type.charAt(0) === 'E') { //equals
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
        } else if (type === '>=') { //bigger or equal
            filters.push(new OpenLayers.Filter.Comparison({
                type: OpenLayers.Filter.Comparison.GREATER_THAN_OR_EQUAL_TO,
                property: name,
                value: value
            }));
        } else if (type === '<=') { //smaller or equal
            filters.push(new OpenLayers.Filter.Comparison({
                type: OpenLayers.Filter.Comparison.LESS_THAN_OR_EQUAL_TO,
                property: name,
                value: value
            }));
        } else if (type === 'T') { //equals with words separated by spaces that must be all present
            var splitted = value.split(" ");
            for (i = 0; i < splitted.length; ++i) {
                filters.push(new OpenLayers.Filter.Comparison({
                    type: OpenLayers.Filter.Comparison.EQUAL_TO,
                    property: name,
                    value: splitted[i]
                }));
            }
        } else if (type === 'B') { //boolean
            filters.push(new OpenLayers.Filter.Comparison({
                type: OpenLayers.Filter.Comparison.EQUAL_TO,
                property: name,
                value: value ? 1 : 0
            }));
        } else if (type === 'V') { //field name specified in the value, separated by a '/' with the value
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
    
    /** private: method[sortByMappings]
     *  Define default sort order to use for each kind of sort field.
     */
    sortByMappings: {
        relevance: {
            name: 'relevance',
            order: 'D'
        },
        rating: {
            name: 'rating',
            order: 'D'
        },
        popularity: {
            name: 'popularity',
            order: 'D'
        },
        date: {
            name: 'date',
            order: 'D'
        },
        title: {
            name: 'title',
            order: 'A'
        }
    },
    
    /** private: method[buildCSWQueryPOST]
     *  Create a query to POST based on an OGC filter.
     */
    buildCSWQueryPOST: function(filter, startRecord, sortBy){
    
        var result = '<?xml version="1.0" encoding="UTF-8"?>\n' + 
                     '<csw:GetRecords xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" service="CSW" version="2.0.2" resultType="' + 
                        this.resultsMode + 
                        '" startPosition="' + startRecord + 
                        '" maxRecords="' + this.maxRecords + 
                      '">\n' + 
                      '  <csw:Query typeNames="csw:Record">\n' + 
                      '    <csw:ElementSetName>full</csw:ElementSetName>\n'; 
        
        if (sortBy) {
            var searchInfo = GeoNetwork.util.CSWSearchTools.sortByMappings[sortBy];
            result += '    <ogc:SortBy xmlns:ogc="http://www.opengis.net/ogc">\n' + 
                      '      <ogc:SortProperty>\n' + 
                      '        <ogc:PropertyName>' + searchInfo.name + '</ogc:PropertyName>\n' + 
                      '        <ogc:SortOrder>' + searchInfo.order + '</ogc:SortOrder>\n' + 
                      '      </ogc:SortProperty>\n' + 
                      '    </ogc:SortBy>\n';
        }
        
        if (filter) {
            var filterXML = new OpenLayers.Format.XML().write(new OpenLayers.Format.Filter().write(filter));
            filterXML = filterXML.replace(/^<\?xml[^?]*\?>/, "");
            result += '    <csw:Constraint version="1.0.0">\n';
            result += filterXML;
            result += '    </csw:Constraint>\n';
        }
        
        result += '  </csw:Query>\n' + 
                  '</csw:GetRecords>';
        
        return result;
    },
    
    /** private: method[buildCSWQueryGET]
     *  Build a GET query based on an OGC filter.
     */
    buildCSWQueryGET: function(filter, startRecord, sortBy){
    
        var result = {
            request: 'GetRecords',
            service: 'CSW',
            version: '2.0.2',
            resultType: this.resultsMode,
            namespace: 'csw:http://www.opengis.net/cat/csw/2.0.2',
            typeNames: 'csw:Record',
            constraintLanguage: 'FILTER',
            constraint_language_version: '1.1.0',
            elementSetName: 'full',
            startPosition: startRecord,
            maxRecords: this.maxRecords
        };
        
        if (sortBy) {
            var searchInfo = GeoNetwork.util.CSWSearchTools.sortByMappings[sortBy];
            result.sortBy = searchInfo.name + ":" + searchInfo.order;
        }
        
        if (filter) {
            var filterXML = new OpenLayers.Format.XML().write(new OpenLayers.Format.Filter().write(filter));
            filterXML = filterXML.replace(/^<\?xml[^?]*\?>/, "");
            result.constraint = filterXML;
        }
        
        return result;
    },
    
    /** api: method[getFormValues]
     *  :return: ``Object`` All form fields and values
     *
     *  According to field type, get form values.
     *
     */
    getFormValues: function(form){
        var result = form.getForm().getValues() || {};
        form.cascade(function(cur){
            if (cur.disabled !== true) {
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
                    if (cur.getValue() !== "") {
                        result[cur.getName()] = cur.getValue().format('Y-m-d') + (cur.postfix ? cur.postfix : '');
                    }
                } else if (cur.getName) {
                    if (cur.getValue && cur.getValue() !== "") {
                        result[cur.getName()] = cur.getValue();
                    }
                }
            }
            return true;
        });
        return result;
    }
};
