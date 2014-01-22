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
     *  :param async: ``Boolean``   false to run in synchrone mode. Default is true.
     *  
     *  Send a GET query to server url. A query is a KVP string.
     *
     */
    doQuery: function(query, cat, startRecord, onSuccess, onFailure, updateStore, metadataStore, summaryStore, async){
        OpenLayers.Request.GET({
            url: cat.services.rootUrl + metadataStore.service + "?" + query,
            async: async === false ? false : true,
            success: function(result){
            
                if (updateStore) {
                    /* TODO : improve */
                    var getRecordsFormat = new OpenLayers.Format.GeoNetworkRecords();
                    var currentRecords = getRecordsFormat.read(result.responseText);
                    var values = currentRecords.records;
                    
                    // Only update status if the target metadata store is the catalogue store
                    // Improve, move the status update on the data change event TODO
                    var isCatalogueMdStore = cat.metadataStore === metadataStore, 
                    	isCatalogueSStore = cat.summaryStore === summaryStore;

                    if (values && values.length > 0) {
                        metadataStore.loadData(currentRecords);
                    }
                    
                    if (isCatalogueSStore) {
                        var summary = currentRecords.summary;
                        var type = summaryStore.root.split('.');
                        var root = (type !== undefined ? type[0] : 'keywords');
                        var subroot = (type !== undefined  ? type[1] : 'keyword');
                        // added check for summary.keywords.keyword otherwise if result has no keywords the loadData on store fails
                        if (summary && summary.count > 0 && summary[root] && summary[root][subroot] && summaryStore) {
                            summaryStore.loadData(summary);
                        }
                    }
                    
                    if (cat && isCatalogueMdStore) {
                    	if (currentRecords.from) {
                    		cat.updateStatus(currentRecords.from + '-' + currentRecords.to +
                                            OpenLayers.i18n('resultBy') +
                                            currentRecords.summary.count);
                    	}
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
    doQueryFromForm: function(formId, cat, startRecord, onSuccess, onFailure, updateStore, metadataStore, summaryStore, async){
        var query = GeoNetwork.util.SearchTools.buildQueryFromForm(Ext.getCmp(formId), startRecord, GeoNetwork.util.SearchTools.sortBy, metadataStore.fast);
        GeoNetwork.util.SearchTools.doQuery(query, cat, startRecord, onSuccess, onFailure, updateStore, metadataStore, summaryStore, async);
    },
    doQueryFromParams: function(params, cat, startRecord, onSuccess, onFailure, updateStore, metadataStore, summaryStore, async){
        var filters = [], query;
        GeoNetwork.util.SearchTools.addFiltersFromPropertyMap(params, filters, startRecord);
        
        query = GeoNetwork.util.SearchTools.buildQueryGET(filters, startRecord, 
                    GeoNetwork.util.SearchTools.sortBy, metadataStore.fast);
        
        GeoNetwork.util.SearchTools.doQuery(query, cat, startRecord, onSuccess, onFailure, updateStore, metadataStore, summaryStore, async);
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
     *
     *  Search field may not be visible if in a collapsed fieldset (TODO)
     *  Test with radio, dates and geometry (TODO)
     */
    populateFormFromParams: function(form, map, createNewField){
        form.cascade(function(cur){
            if (cur.getName) {
                var name = cur.getName();
                if (map[name]) {
                    var value = map[name];
                    delete map[name];
                    
                    if (cur.getXType() === 'superboxselect') {
                        if(value.indexOf(' or ') !== -1) {
                            value = value.split(' or ');
                        }
                        if (cur.mode === 'local') {
                            // Wait the store to load to set the value (eg. categories, catalogues)
                            cur.getStore().on('load', function() {
                                cur.setValue(value);
                            });
                            // and set the value too if the store is already loaded (eg. spatial rep.)
                            cur.setValue(value);
                            // The store should be loaded only once, so it should be fine.
                        } else {
                            // Force the value (ie. usually autocomplete combo)
                            cur.setValue(value, true);
                        }
                    } else {
                        // Hack to set sort order which is based on 
                        // 2 inputs and one combo
                        // FIXME
                        if (name === 'E_sortBy') {
                            var cb =  cur.linkedCombo;
                            if (map['E_sortOrder']) {
                                cb.setValue(value + '#' + map['E_sortOrder']);
                            } else {
                                var idx = cb.getStore().find('id', new RegExp(value + '*'));
                                if (idx !== -1) {
                                    cb.setValue(cb.getStore().getAt(idx).get('id'));
                                    cb.fireEvent('change', cb, cb.getValue());
                                }
                            }
                        } else {
                            cur.setValue(value);
                        }
                    }
                    
                    // If form field is in a collapsed fieldset, expand it
                    Ext.each(cur.findParentByType('fieldset'), function(f) {
                        if (f.rendered && f.collapsed) {
                            f.expand();
                        }
                    });
                }
            }
        });
        
        // Populate the search form with simple text input ...
        if (createNewField) {
            for (var searchCriteria in map) {
                if (map.hasOwnProperty(searchCriteria) && map[searchCriteria] !== '') {
                    form.insert(form.items.length ++, new Ext.form.TextField({
                        name: searchCriteria,
                        fieldLabel: OpenLayers.i18n(searchCriteria.substring(searchCriteria.indexOf('_') + 1)),
                        value: map[searchCriteria],
                        inputType: 'text',
                        extraCriteria: true
                    }));
                }
            }
        }
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
        if (field) {
            var list = field[3].split('|');
            for (i = 0; i < list.length; ++i) {
                GeoNetwork.util.SearchTools.addFilterImpl(filters, field[2], list[i], value);
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
            if (typeof(value) == 'object') {
                 for (var i=0; i< value.length; i++) {
                     filters.push(name + "=" + encodeURIComponent(value[i]) + "");
                 }
            } else {
                filters.push(name + "=" + encodeURIComponent(value) + "");
            }
        } else if (type === 'B') { //boolean
            filters.push(name + "=" + (value ? 'on' : 'off') + "");
        } else if (type === 'O') { //optional boolean
            if (value) {
                filters.push(name + "=" + 'on');
            }
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
        var result = {};
        form.cascade(function(cur){
            if (cur.disabled !== true && !(cur.getName && OpenLayers.String.startsWith(cur.getName(),'ext-comp'))) { // Check element is
                // enabled
                // and rendered (ie. visible, eg. field in a collapsed fieldset)
                if (cur.isXType('boxselect') || cur.isXType('combo')) {
                    if (cur.getValue && cur.getValue() && cur.getValue() !== "") {
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
                } else if (cur.isXType('multislider')) {
                } else {
                    if (cur.getValue && cur.getValue() && cur.getValue() !== "") {
                        GeoNetwork.util.SearchTools.addFieldValue(result, cur.getName(), cur.getValue());
                    } else { 
                        
                    }
                }
            }
            return true;
        });
        return result;
    },
    addFieldValue: function (result, name, value) {
        if (result[name] === undefined) {
            result[name] = new Array(value);
        } else {
            result[name].push(value);
        }
    },
    parseFacets: function(response) {
        var facets = response.responseXML.childNodes[0].childNodes[1];
        var store = new Ext.data.ArrayStore({
            // store configs
            autoDestroy: true,
            storeId: 'myStore',
            // reader configs
            idIndex: 0,  
            fields: [
               'facet',
               'name',
               'count'
            ]
        });
        if (facets.nodeName === 'summary') {
             Ext.each(facets.childNodes, function(facet) {
                 if (facet.nodeName != '#text' && facet.childNodes.length > 0) {
                     Ext.each(facet.childNodes, function(node) {
                        if (node.getAttribute) {
                            var data = {
                                facet : node.nodeName,
                                name : node.getAttribute('name'),
                                count : node.getAttribute('count')
                            };
                            var r = new store.recordType(data); 
                            store.add(r);
                        }
                     });
                 }
            });
        }
        return store;
    }
};
