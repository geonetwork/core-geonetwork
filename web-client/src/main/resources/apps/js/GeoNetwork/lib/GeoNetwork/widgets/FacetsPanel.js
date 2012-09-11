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
    /** private: property[currentFilter]
     * Store current filter. This is used to not display current filter in facet list.
     */
    currentFilter: {},
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
            store = this.facetsStore, 
            currentFilter = this.currentFilter;
        
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
                                    facetList += panel.displayFacetValue(node, currentFilter, store);
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
                            facetList += panel.displayFacetValue(node, currentFilter, store);
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
        if (Object.keys(currentFilter).length === 0) {
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
                return "<h2><a href='javascript:void(0);' class='facet' id='" + recId + "'>" + 
                              data.node + "&nbsp;(" + data.count + ")</a></h2>";
            }
        }
        return "";
    },
    addFacet: function (recordId) {
        var r = this.facetsStore.getById(recordId),
            form = this.searchForm,
            key = r.get('facet'),
            value = r.get('node'),
            field = Ext.getCmp('E_' + key),
            groupId;
        
        this.counter ++;
        var id = 'facet_' + this.counter;
        
        if (this.currentFilter[key] === undefined) {
            this.currentFilter[key] = [value];
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
        
        if (this.breadcrumb) {
            var panel = this;
            
            var scrollMenu = new Ext.menu.Menu({cls: 'breadcrumb-mn'});
            scrollMenu.add({
                id: id + '-' + key,
                text: OpenLayers.i18n('removeFilter') + ' ' + OpenLayers.i18n(r.get('facet')),
                iconCls: 'md-mn-reset',
                handler: function (b, e) {
                    this.removeFacet(b.getId());
                },
                scope: panel
            });
            scrollMenu.add('-');
            // Switch facet value
            var i = 0;
            this.startFacetsStore.query('facet', key).each(function (item, idx) {
                groupId = id + '-' + key;
                scrollMenu.add({
                    group: groupId + '#',
                    id: groupId + '#' + i++,
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
                id: 'bc_' + groupId,
                altText: r.get('node'),
                text: r.get('node'),
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
        var elemId = elem.getId(), params = elemId.split('-'), 
            id = params[0], facet = params[1].split('#')[0], 
            btId = 'bc_' + elemId.split('#')[0], switcher = Ext.getCmp(btId);

        // Replace reference in current filter
        this.currentFilter[this.currentFilter[facet].indexOf(switcher.altText)] = elem.altText
        
        // Update value of search field
        var field = Ext.getCmp('field_' + id);
        field.setValue(elem.altText);
        
        // Update switcher label
        switcher.setText(elem.text);
        switcher.altText = elem.text;
        
        this.searchForm.fireEvent('search');
    },
    /**
     * elemId is remove facet button id with structure: id-key
     */
    removeFacet: function (elemId) {
        var params = elemId.split('-'), facet = params[1], id = params[0], 
            value = Ext.get(elemId) && Ext.get(elemId).getAttribute('alt');
        
        // Remove breadcrumb reference
        this.breadcrumb && this.breadcrumb.remove(Ext.getCmp('bc_' + elemId));
        
        // Remove search form reference
        var field = Ext.getCmp('field_' + id);
        this.searchForm.remove(field);
        this.searchForm.fireEvent('search');
        
        this.currentFilter[facet].splice(this.currentFilter[facet].indexOf(value), 1);
    },
    init: function () {
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
