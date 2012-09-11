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
 *   TODO : On form reset should be empty
 *   TODO : Load all on startup
 */
GeoNetwork.FacetsPanel = Ext.extend(Ext.Panel, {
    defaultConfig: {
        border: false,
        breadcrumb: null,
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
    /** private: property[currentFilterStore]
     * Store current filters. This is used to not display current filter in facet list.
     */
    currentFilterStore: null,
    counter: 0,
    searchForm: null,
    serviceUrl: null,
    catalogue: null,
    afterFacetClick: function (){
        
    },
    /**
     * Read the response, populate the facet selector, activate click event
     * and populate the store. If no filter applied, copy the store to the startFacetStore.
     */
    refresh: function (response) {
        var facets = response.responseXML.childNodes[0].childNodes[1],
            zappette = '', 
            panel = this, 
            store = this.facetsStore;
        
        // Clean previous facets
        store.removeAll();
        
        if (facets.nodeName === 'summary') {
            // TODO : Use template
            if (this.facetListOrder.length > 0) {
                // Display only client requested facet
                Ext.each(this.facetListOrder, function (facetToDisplay) {
                    Ext.each(facets.getElementsByTagName(facetToDisplay.name), function (facet) {
                        if (facet.nodeName !== '#text' && facet.childNodes.length > 0) {
                            var nodeCount = 0;
                            var facetList = "";
                            Ext.each(facet.childNodes, function (node) {
                                if (this.count === undefined || (this.count && nodeCount < this.count)) {
                                    facetList += panel.displayFacetValue(node, store);
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
                Ext.each(facets.childNodes, function (facet) {
                    if (facet.nodeName !== '#text' && facet.childNodes.length > 0) {
                        var facetList = "";
                        Ext.each(facet.childNodes, function (node) {
                            facetList += panel.displayFacetValue(node, store);
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
        Ext.each(items, function (input) {
            Ext.get(input).on('click', function () {
                scope.addFacet(this.id);
            });
        });
        
        // If no filter applied yet save all facet and values in a store
        // use for switching facet value in breadcrumb.
        // TODO : cross browser test
        if (this.currentFilterStore.getCount() === 0) {
            var records = [];
            store.each(function (r) {
                records.push(r.copy());
            });
            this.startFacetsStore = new Ext.data.JsonStore({
                recordType: store.recordType
            });
            this.startFacetsStore.add(records);
        }
    },
    displayFacetValue: function (node, store) {
        if (node.getAttribute) {
            var data = {
                facet : node.nodeName,
                node : node.getAttribute('name'),
                count : node.getAttribute('count')
            };
            // Only display a facet if it's not part of current filter
            
            if (this.currentFilterStore.getCount() === 0 ||
            		this.currentFilterStore.query('value', data.node).length === 0) {
                var recId = store.getCount() + 1,
                     r = new store.recordType(data, recId); 
                store.add(r);
                return "<h2><a href='javascript:void(0);' class='facet' id='" + recId + "'>" + 
                              data.node + "&nbsp;(" + data.count + ")</a></h2>";
            }
        }
        return "";
    },
    addFacet: function (recordId) {
        var r = this.facetsStore.getById(recordId),
            form = this.searchForm, id = 'facet_' + this.counter ++;
        
        var data = {
             id: id, 
             facet: r.get('facet'), 
             value: r.get('node'), 
             bcid: 'bc_' + id, 
             fieldid: 'field_' + id
        };
        var filter = new this.currentFilterStore.recordType(data, this.currentFilterStore.getCount() + 1);
        this.currentFilterStore.insert(0, filter);
        
        form.insert(0, new Ext.form.TextField({
            id: data.fieldid,
            name: 'E_' + data.facet,
            value: data.value,
            // Switch to text for debugging
            inputType: 'hidden'
        }));
        
        if (this.breadcrumb) {
            var panel = this;
            
            var scrollMenu = new Ext.menu.Menu({cls: 'breadcrumb-mn'});
            scrollMenu.add({
                text: OpenLayers.i18n('removeFilter') + ' ' + OpenLayers.i18n(r.get('facet')),
                iconCls: 'md-mn-reset',
                handler: function (b, e) {
                    this.removeFacet(data.id);
                },
                scope: panel
            });
            scrollMenu.add('-');
            // Switch facet value
            var i = 0;
            this.startFacetsStore.query('facet', data.facet).each(function (item, idx) {
                scrollMenu.add({
                    group: data.id + '#',
                    id: data.id + '#' + i++,
                    altText : item.get('node'),
                    checked: item.get('node') === r.get('node') ? true : false,
                    text: item.get('node'),
                    handler: function (b, e) {
                        this.switchFacet(b);
                    },
                    scope: panel
                });
            });
            this.breadcrumb.add(new Ext.Button({
                id: data.bcid,
                text: data.value,
                menu: scrollMenu
            }));
            this.breadcrumb.doLayout();
        }
        this.searchForm.fireEvent('search');
    },
    /**
     * elemId is scrolling menu item id with structure: id-key#i
     */
    switchFacet: function (elem) {
        var elemId = elem.getId(), filterKey = elemId.split('#')[0], newValue = elem.text;
        
        // Search in current filter
        var filter = this.currentFilterStore.query('id', filterKey).get(0);
        
        var field = Ext.getCmp(filter.get('fieldid')),
            switcher = Ext.getCmp(filter.get('bcid'));
        
        // Update current filter
        filter.set('value', newValue);
        // Update value of search field
        field.setValue(newValue);
        // Update switcher label
        switcher.setText(newValue);
        
        this.searchForm.fireEvent('search');
    },
    /**
     * elemId is remove facet button id with structure: id-key
     * silent: do not trigger search event
     */
    removeFacet: function (filterKey, silent) {
        // Search in current filter
        var filter = this.currentFilterStore.query('id', filterKey).get(0);
        
        var field = Ext.getCmp(filter.get('fieldid')),
            switcher = Ext.getCmp(filter.get('bcid'));
        
        // Remove search form reference
        this.searchForm.remove(field);
        // Remove breadcrumb reference
        this.breadcrumb && this.breadcrumb.remove(switcher);
        
        this.currentFilterStore.remove(filter);
        
        if (!silent) {
            this.searchForm.fireEvent('search');
        }
    },
    /**
     * Clear all current filter in search form, breadcrumb
     */
    reset: function () {
       this.currentFilterStore.each(function (r) {
           this.removeFacet(r.get('id'), true)
       });
    },
    init: function () {
        this.currentFilterStore = new Ext.data.ArrayStore({
            fields: ['id', 'facet', 'value', 'fieldid', 'bcid'],
            idIndex: 0
        });
        
        this.update('<div id="facets" class="facets"></div>');
    },
    /** private: method[initComponent] 
     *  Initializes the metadata view window.
     */
    initComponent: function () {
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
