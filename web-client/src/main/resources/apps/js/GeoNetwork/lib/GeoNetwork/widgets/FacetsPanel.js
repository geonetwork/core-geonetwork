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
Ext.namespace('GeoNetwork');

/** api: (define)
 *  module = GeoNetwork
 *  class = FacetsPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor 
 *  .. class:: FacetsPanel(config)
 *
 *     Create a faceted search panel
 *
 *   TODO : Breadcrumb as component not only a simple div
 *   TODO : On form reset should be empty
 *   TODO : Load all on startup
 */
GeoNetwork.FacetsPanel = Ext.extend(Ext.Panel, {
    defaultConfig: {
        border: false,
        /** api: config[lang] 
         *  The language to use to call GeoNetwork services in the print mode (which is opened in a new window).
         */
        lang: 'en',
        /** api: config[facetListOrder] 
         *  Array of facets to display on the client side. This could be used to restrict the facet provided by 
         *  the server. Example: [{name: 'keywords', count: 15}, {name: 'spatialRepresentations', count: 2}]
         *  will display 15 keywords and all spatial representation types.
         */
        facetListOrder: [],
        autoScroll: true
    },
    /** private: property[facetsStore]
     * Store containing current facet for the search.
     */
    facetsStore: new Ext.data.JsonStore({
        fields : [ 'facet', 'node', 'count' ]
    }),
    /** private: property[startFacetsStore]
     * Store the facet for first search used to provide facet switcher on breadcrumb.
     */
    startFacetsStore: new Ext.data.JsonStore({
        fields : [ 'facet', 'node', 'count' ]
    }),
    /** private: property[currentFilter]
     * Store current filter. This is used to not display current filter in facet list.
     */
    currentFilter: {},
    counter: 0,
    searchForm: null,
    serviceUrl: null,
    catalogue: null,
    afterFacetClick: function(){
        
    },
    refresh: function (response) {
        var facets = response.responseXML.childNodes[0].childNodes[1],
            zappette = '<div id="breadcrumb"></div>', 
            panel = this, 
            store = this.facetsStore, 
            currentFilter = this.currentFilter;
        
        store.removeAll();
        
        if (facets.nodeName === 'summary') {
            // TODO : Use template
            if (this.facetListOrder.length > 0) {
                // Display only client requested facet
                Ext.each(this.facetListOrder, function (facetToDisplay) {
                    Ext.each(facets.getElementsByTagName(facetToDisplay.name), function(facet) {
                        if (facet.nodeName != '#text' && facet.childNodes.length > 0) {
                            var nodeCount = 0;
                            var facetList = "";
                            Ext.each(facet.childNodes, function(node) {
                                if (this.count == undefined || (this.count && nodeCount < this.count)) {
                                	facetList += panel.displayFacetValue (node, currentFilter, store);
                                }
                            }, this);
                            if (facetList !== "") {
                                zappette += "<h1 class='facet'>" + OpenLayers.i18n(facet.nodeName) + "</h1>";
                                zappette += facetList;
                            }
                        }
                    }, facetToDisplay);
                });
            } else {
                // Display all
                Ext.each(facets.childNodes, function(facet) {
                    if (facet.nodeName != '#text' && facet.childNodes.length > 0) {
                        var facetList = "";
                        Ext.each(facet.childNodes, function(node) {
                            facetList += panel.displayFacetValue (node, currentFilter, store);
                        });
                        if (facetList !== "") {
                            zappette += "<h1 class='facet'>" + OpenLayers.i18n(facet.nodeName) + "</h1>";
                            zappette += facetList;
                        }
                    }
                });
            }
        }
        Ext.getDom('facets').innerHTML = zappette;
        
        // Register click event
        var items = Ext.DomQuery.select('a.facet');
        var scope = this;
        Ext.each(items, function(input){
            Ext.get(input).on('click', function() {
                scope.addFacet(this.id);
            });
        });
    },
    displayFacetValue: function (node, currentFilter, store) {
        if (node.getAttribute) {
            var data = {
                facet : node.nodeName,
                node : node.getAttribute('name'),
                count : node.getAttribute('count')
            };
            // Only display a facet if it's not part of current filter
            if (currentFilter[data.facet] === undefined ||
                  currentFilter[data.facet].indexOf(data.node) === -1) {
                var recId = store.getCount() + 1,
                     r = new store.recordType(data, recId); 
                store.add(r);
                return "<h2><a href='javascript:void(0);' class='facet' id='" + recId + "'>" 
                    + data.node + "&nbsp;(" + data.count + ")</a></h2>"
            }
        }
        return "";
    },
    addFacet: function(id) {
        var r = this.facetsStore.getById(id),
            form = this.searchForm,
            key = r.get('facet'),
            value = r.get('node');
        
        var field = Ext.getCmp('E_' + key);
        this.counter ++;
        var id = 'facet_' + this.counter;
        if (this.currentFilter[key] === undefined) {
            this.currentFilter[key] = new Array(value);
        } else {
            this.currentFilter[key].push(value);
        }
        
        form.insert(0, new Ext.form.TextField({
            id: 'field_' + id,
            name: 'E_' + key,
            value: value,
            // Switch to text for debugging
            inputType: 'hidden'
        }));
        
        // TODO escape '
        if (Ext.getDom('breadcrumb')) {
            // TODO : i18n
           
            Ext.getDom('breadcrumb').innerHTML += '<div class="bcfacet" id="' + id + '" title="' + OpenLayers.i18n('removeFilter') + '">'
            + '<a href="javascript:void(0);">' 
            + OpenLayers.i18n(r.get('facet')) + ": " + r.get('node') + "</a></div>";
            // TODO no > sign on last facet
            // FIXME : register event only on the newly created one 
            var items = Ext.DomQuery.select('div.bcfacet');
            var scope = this;
            Ext.each(items, function(input){
                Ext.get(input).on('click', function() {
                    scope.removeFacet(this.id);
                });
            });
        }
        this.searchForm.fireEvent('search');
    },

    removeFacet: function(id) {
        // Remove breadcrumb reference
        Ext.getDom('breadcrumb') && Ext.get(id) && Ext.get(id).remove();
        
        // Remove search form reference
        var field = Ext.getCmp('field_' + id);
        this.searchForm.remove(field);
        this.searchForm.fireEvent('search');
        
        this.currentFilter[key].splice(this.currentFilter[key].indexOf(value), 1);

    },
    init: function(){
        this.update('<div id="breadcrumb" class="breadcrumb"></div><div id="facets" class="facets"></div>');
    },
    /** private: method[initComponent] 
     *  Initializes the metadata view window.
     */
    initComponent: function(){
        Ext.applyIf(this, this.defaultConfig);
        GeoNetwork.FacetsPanel.superclass.initComponent.call(this);
        
        this.on({
            render: this.init,
            scope: this
        });
    }
});

/** api: xtype = gn_facetspanel */
Ext.reg('gn_facetspanel', GeoNetwork.FacetsPanel);
