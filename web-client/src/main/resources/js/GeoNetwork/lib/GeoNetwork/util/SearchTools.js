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
 *  class = SearchTools
 */
/** api: example 
 *  GeoNetwork.util.SearchTools is used to build queries from a form and send GET request.
 *  Results could be processed using OpenLayers.Format.GeoNetworkRecord.
 *
 *  GeoNetwork xml.search service is used with KVP search like
 *  http://localhost:8080/geonetwork/srv/fr/xml.search?any=africa&sortBy=relevance&hitsPerPage=10&output=full
 *
 *  .. code-block:: javascript
 *
 *      GeoNetwork.util.SearchTools.doQueryFromForm(this.formId, cat, 1, this.showResults, null, true);
 *      ...
 *      showResults: function(response) {
 *        var getRecordsFormat = new OpenLayers.Format.GeoNetworkRecord();
 *        var r = getRecordsFormat.read(response.responseText);
 *        var values = r.records;
 *        ...
 *      }
 *
 */
GeoNetwork.util.SearchTools = {

    /**
     * Define results mode. "results_with_summary" is used to have GeoNetwork
     * specific result mode.
     */
    fast: 'false',
    output: 'full',
    sortBy: 'relevance',
    hitsPerPage: '50',
    
    
    /** api:method[doQuery]
     *
     *  :param query: ``String``  the KVP query
     *  :param cat: ``GeoNetwork.Catalogue``    Catalogue to query
     *  :param recordNum: ``Number``    Optional start record number
     *  :param onSuccess: ``Function``  Optional function to trigger in case of success
     *  :param onFailure: ``Function``  Optional function to trigger in case of failure
     *  :param updateStore: ``Boolean``    true to update catalogue attached stores. false
     *    to not update them. If false, usually a onSuccess function is used to retrieve
     *    search results.
     *
     *  Send a GET query to server url. A query is a KVP string.
     *
     */
    doQuery: function(query, cat, startRecord, onSuccess, onFailure, updateStore, metadataStore, summaryStore){
        OpenLayers.Request.GET({
            url: cat.services.xmlSearch + "?" + query,
            success: function(result){
            
                if (updateStore) {
                    /* TODO : improve */
                    var getRecordsFormat = new OpenLayers.Format.GeoNetworkRecords();
                    var currentRecords = getRecordsFormat.read(result.responseText);
                    var values = currentRecords.records;
                    if (values.length > 0) {
                        metadataStore.loadData(currentRecords);
                    }
                    
                    var summary = currentRecords.summary;
                    if (summary && summary.count > 0 && summaryStore) {
                        summaryStore.loadData(summary);
                    }
                    if (cat) {
                        cat.updateStatus(currentRecords.from + '-' + currentRecords.to +
                                            OpenLayers.i18n('resultBy') +
                                            summary.count);
                    }
                }
                
                if (onSuccess) {
                    onSuccess(result, query);
                }
            },
            failure: function(response){
                if (onFailure) {
                    onFailure(response);
                }
            }
        });
    },
    /** api:method[doQueryFromForm]
     *
     *  :param formId: ``String``  Form identifier
     *  :param cat: ``GeoNetwork.Catalogue``    Catalogue to query
     *  :param recordNum: ``Number``    Optional start record number
     *  :param onSuccess: ``Function``  Optional function to trigger in case of success
     *  :param onFailure: ``Function``  Optional function to trigger in case of failure
     *  :param updateStore: ``Boolean``    true to update catalogue attached stores. false
     *    to not update them. If false, usually a onSuccess function is used to retrieve
     *    search results.
     *
     *  Build a GET query. A form is composed of one
     *  or more fields which are processed by buildQueryFromForm.
     *
     */
    doQueryFromForm: function(formId, cat, startRecord, onSuccess, onFailure, updateStore, metadataStore, summaryStore){
    
        var query = GeoNetwork.util.SearchTools.buildQueryFromForm(Ext.getCmp(formId), startRecord, GeoNetwork.util.SearchTools.sortBy, metadataStore.fast);
        GeoNetwork.util.SearchTools.doQuery(query, cat, startRecord, onSuccess, onFailure, updateStore, metadataStore, summaryStore);
    },
    doQueryFromParams: function(params, cat, startRecord, onSuccess, onFailure, updateStore, metadataStore, summaryStore){
        var filters = [], query;
        GeoNetwork.util.SearchTools.addFiltersFromPropertyMap(params, filters, startRecord);
        
        query = GeoNetwork.util.SearchTools.buildQueryGET(filters, startRecord, 
                    GeoNetwork.util.SearchTools.sortBy, metadataStore.fast);
        
        GeoNetwork.util.SearchTools.doQuery(query, cat, startRecord, onSuccess, onFailure, updateStore, metadataStore, summaryStore);
    },
    /** api:method[buildQueryFromForm]
     *
     *  :param formId: ``String``  Form identifier
     *  :param cat: ``GeoNetwork.Catalogue``    Catalogue to query
     *  :param recordNum: ``Number``    Optional start record number
     *  :param onSuccess: ``Function``  Optional function to trigger in case of success
     *  :param onFailure: ``Function``  Optional function to trigger in case of failure
     *
     *  It's assumed the field name follow a convention. It starts with a letter,
     *  followed by an underscore. The letters are:
     *
     *  * E: no meaning.
     *
     *  In order to define search fuzziness, add a hidden field named E_similarity
     *  with the default similarity to be used in Lucene search.
     *
     */
    buildQueryFromForm: function(form, startRecord, sortBy, fast){
        var values = GeoNetwork.util.SearchTools.getFormValues(form);
        var filters = [];
        
        GeoNetwork.util.SearchTools.addFiltersFromPropertyMap(values, filters, startRecord);
        
        
        return GeoNetwork.util.SearchTools.buildQueryGET(filters, startRecord, sortBy, fast);
    },
    /** api:method[populateFormFromParams]
     *
     *  :param formId: ``String``  Form identifier
     *  :param map: ``Object``    List of parameters in a map.
     *
     *  Populate form fields based on a list of parameters.
     *  Parameters name must be equal to field name.
     *  Prefix 'E_' could be ommitted.
     *
     *  Search field may not be visible if in a collapsed fieldset (TODO)
     *  Test with radio, dates and geometry (TODO)
     */
    populateFormFromParams: function(form, map){
    
        form.cascade(function(cur){
            if (cur.getName) {
                var name = cur.getName();
                if (name.indexOf('_') !== -1) { // Shortcut if URL params does not contain prefix
                    name = name.substring(2);
                    if (map[name]) {
                        cur.setValue(map[name]);
                    }
                } else if (map[name]) {
                    cur.setValue(map[name]);
                }
            }
        });
    },
    
    /** private: method[addFiltersFromPropertyMap]
     *  Add property in a GeoNetwork filter
     *  Check for similarity and set up start and end index of records to returned.
     */
    addFiltersFromPropertyMap: function(values, filters, startRecord){
        var defaultSimilarity = ".8", 
            key, 
            similarity = values.E_similarity, 
            hits = values.E_hitsperpage;
        
        // Add the similarity if defined
        if (similarity !== undefined) {
            defaultSimilarity = values.E_similarity;
            GeoNetwork.util.SearchTools.addFilter(filters, 'E_similarity', defaultSimilarity);
        }
        
        if (!hits) {
            hits = GeoNetwork.util.SearchTools.hitsPerPage;
        }
        var to = parseInt(startRecord, 10) + parseInt(hits, 10) - 1;
        GeoNetwork.util.SearchTools.addFilter(filters, 'E_from', startRecord);
        GeoNetwork.util.SearchTools.addFilter(filters, 'E_to', to);
        
        
        // Add all other criteria
        for (key in values) {
            if (values.hasOwnProperty(key)) {
                var value = values[key];
                if (value !== "" && key !== 'E_similarity') {
                    GeoNetwork.util.SearchTools.addFilter(filters, key, value);
                }
            }
        }
    },
    
    /** private: method[addFilter]
     *
     */
    addFilter: function(filters, key, value){
        var field = key.match("^(\\[?)([^_]+)_(.*)$"), 
            i, 
            or = [];
        // console.log("field:" + field + " value:" + value);
        if (field) {
            if (field[1] === '[') { // Not used
                var values = value.split(",");
                for (i = 0; i < values.length; ++i) {
                    GeoNetwork.util.SearchTools.addFilterImpl(values.length > 1 ? or : filters, field[2], field[3], values[i]);
                }
            } else {
                GeoNetwork.util.SearchTools.addFilterImpl(filters, field[2], field[3], value);
            }
        }
    },
    
    /** private: method[addFilterImpl]
     *  Build filter according to type name define by form fields name.
     *
     *  TODO : do we need to add wildcard query ?
     */
    addFilterImpl: function(filters, type, name, value){
        if (type.charAt(0) === 'E') { // equals
            filters.push(name + "=" + escape(value) + "");
        } else if (type === 'B') { //boolean
            filters.push(name + "=" + (value ? 'on' : 'off') + "");
        } else {
            alert("Cannot parse " + type);
        }
    },
    
    /** private: method[addFiltersFromPropertyMap]
     *  Define default sort order to use for each kind of sort field.
     */
    sortByMappings: {
        relevance: {
            name: 'relevance',
            order: ''
        },
        rating: {
            name: 'changeDate',
            order: ''
        },
        popularity: {
            name: 'popularity',
            order: ''
        },
        date: {
            name: 'date',
            order: ''
        },
        title: {
            name: 'title',
            order: 'reverse'
        }
    },
    /** private: method[buildQueryGET]
     *  Build a GET query based on an OGC filter.
     */
    buildQueryGET: function(filter, startRecord, sortBy, fast){
        var query = "fast=" + (fast ? fast : GeoNetwork.util.SearchTools.fast) + "&";
        
//        if (sortBy) {
//            // TODOvar searchInfo = GeoNetwork.util.SearchTools.sortByMappings[sortBy];
//            // result.sortBy = searchInfo.name + ":" + searchInfo.order;
//        }
        
        if (filter) {
            query += filter.join("&");
        }
        
        return query;
    },
    
    /** api: method[getFormValues]
     *  According to field type, get form values.
     */
    getFormValues: function(form){
        var result = form.getForm().getValues() || {};
        
        form.cascade(function(cur){
            if (cur.disabled !== true && cur.rendered) { // Check element is
                // enabled
                // and rendered (ie. visible, eg. field in a collapsed fieldset)
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
                        /* support for checkboxes in the fieldset title */
                        result[cur.checkboxName] = !cur.collapsed;
                    }
                } else if (cur.isXType('radiogroup')) {
                    /*
                     * a radiogroup is not a container. So cascade doesn't
                     * visit it... don't ask...
                     */
                    var first = cur.items.get(0);
                    result[first.getName()] = first.getGroupValue();
                } else if (cur.isXType('checkbox')) {
                    result[cur.getName()] = cur.getValue();
                } else if (cur.isXType('datefield')) {
                    if (cur.getValue() !== "") {
                        result[cur.getName()] = cur.getValue().format('Y-m-d') +
                        (cur.postfix ? cur.postfix : '');
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
