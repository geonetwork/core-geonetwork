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
 *     Create a faceted search panel which aims to provide a "Narrow your search" module 
 *     aka as faceted search (http://en.wikipedia.org/wiki/Faceted_search). This module 
 *     allows aggregation based on criteria (e.g. keywords, organization, dates, ...) 
 *     with frequency for the current search.
 *     
 *     On the client side, user interacts with facets using:
 *     
 *      * the facets summary used to select new filter
 *      * the facet breadcrumb which indicates which filter has been applied
 *     
 *     To customize default facet configuration provided by the server, the facetListConfig
 *     properties could be use to overrides the server configuration.
 *     
 *     
 *  .. code-block:: javascript
 *     
 *       GeoNetwork.Settings.facetListConfig = [
 *          {name: 'orgNames'}, 
 *          {name: 'types'}, 
 *          {name: 'denominators'}, 
 *          {name: 'keywords'}, 
 *          {name: 'createDateYears'}];
 *          
 *    ...
 *    
 *    
 */
/** api: example
*
*
*  .. code-block:: javascript
*  
*      var breadcrumb = new Ext.Panel({
*                layout:'table',
*                cls: 'breadcrumb',
*                defaultType: 'button',
*                border: false,
*                split: false,
*                layoutConfig: {
*                    columns:3
*                }
*            });
*      var facetsPanel = new GeoNetwork.FacetsPanel({
*                searchForm: searchForm,
*                breadcrumb: breadcrumb,
*                facetListConfig: GeoNetwork.Settings.facetListConfig || []
*            });
*      ...
*/
GeoNetwork.FacetsPanel = Ext.extend(Ext.Panel, {
    defaultConfig: {
        border: false,
        /** api: config[searchForm] 
         *  The search form to link the facet to. When clicking on a facet a hidden field
         *  is added to the search form panel and search event triggered.
         */
        searchForm: null,
        /** api: config[breadcrumb] 
         *  The breadcrumb panel use to display active filters.
         */
        breadcrumb: null,
        /** api: config[facetListConfig] 
         *  Array of facets to display on the client side. This could be used to restrict the facet provided by 
         *  the server. Example: [{name: 'keywords', count: 15}, {name: 'spatialRepresentations', count: 2}]
         *  will display 15 keywords and all spatial representation types.
         */
        facetListConfig: [],
        /** api: config[maxDisplayedItem] 
         *  If the number of values for a facet is greater than that number, a more/less button is displayed
         *  to display all values returned by the server for this facet.
         */
        maxDisplayedItem: undefined,
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
     * Store current filters. This store is used to not display current filter in facet list
     * and to keep track of active filter and related elements in the search form and the 
     * breadcrumb.
     */
    currentFilterStore: null,
    /** private: property[counter]
     * Facet counter.
     */
    counter: 0,
    afterFacetClick: function () {
        
    },
    /** private: method[refresh]
     *  :param response: ``Object`` the facet response object.
     *  
     *   Read the response, populate the facet selector, activate click event
     *   and populate the store. If no filter applied, copy the store to the startFacetStore.
     */
    refresh: function (response) {
        var facets = response.responseXML.getElementsByTagName('summary')[0],
            zappette = '', 
            panel = this, 
            store = this.facetsStore,
            moreBt =  "<li class='facet-more-bt'><a href='javascript:void(0);'>" + 
                OpenLayers.i18n('facetMore') + "</a></li>",
            lessBt = "<li class='facet-less-bt' style='display:none;'><a href='javascript:void(0);'>" + 
                OpenLayers.i18n('facetLess') + "</a></li>";
        
        // Clean previous facets
        store.removeAll();
        
        if (facets.nodeName === 'summary') {
            // TODO : Use template
            if (this.facetListConfig.length > 0) {
                // Display only client requested facet
                Ext.each(this.facetListConfig, function (facetToDisplay) {
                    Ext.each(facets.getElementsByTagName(facetToDisplay.name), function (facet) {
                        if (facet.nodeName !== '#text') {
                            // Property to see if more action link should be displayed
                            facet.setAttribute('moreAction', 'false');
                            if (facet.nodeName !== '#text' && facet.childNodes.length > 0) {
                                var nodeCount = 0;
                                var facetList = "";
                                Ext.each(facet.childNodes, function (node) {
                                    if (node.nodeName !== '#text') {
                                        var visible = (nodeCount < this.count) || (nodeCount < panel.maxDisplayedItems);
                                        if (facet.getAttribute('moreAction') === 'false' && !visible) {
                                            facet.setAttribute('moreAction', 'true');
                                            facetList += moreBt;
                                        }
                                        facetList += panel.displayFacetValue(node, visible);
                                        nodeCount ++;
                                    }
                                }, this);
                                if (facetList !== "") {
                                    zappette += "<li>" + OpenLayers.i18n(facet.nodeName) + "</li><ul>";
                                    zappette += facetList;
                                    if (facet.getAttribute('moreAction') === 'true') {
                                        zappette += lessBt;
                                    }
                                    zappette += "</ul>";
                                }
                            }
                        }
                    }, facetToDisplay);
                });
            } else {
                // Display all
                Ext.each(facets.childNodes, function (facet) {
                    if (facet.nodeName !== '#text') {
                        // Property to see if more action link should be displayed
                        facet.setAttribute('moreAction', 'false');
                        if (facet.nodeName !== '#text' && facet.childNodes.length > 0) {
                            var facetList = "";
                            var nodeCount = 0;
                            Ext.each(facet.childNodes, function (node) {
                                if (node.nodeName !== '#text') {
                                    var visible = (nodeCount < panel.maxDisplayedItems);
                                    if (facet.getAttribute('moreAction') === 'false' && !visible) {
                                        facet.setAttribute('moreAction', 'true');
                                        facetList += moreBt;
                                    }
                                    facetList += panel.displayFacetValue(node, visible);
                                    nodeCount ++;
                                }
                            });
                            if (facetList !== "") {
                                zappette += "<li>" + OpenLayers.i18n(facet.nodeName) + "</li><ul>";
                                zappette += facetList;
                                if (facet.getAttribute('moreAction') === 'true') {
                                    zappette += lessBt;
                                }
                                zappette += "</ul>";
                            }
                        }
                    }
                });
            }
        }
        Ext.getDom('facets').innerHTML = "<ul>" + zappette + "</ul>";
        
        // Register click event
        var items = Ext.DomQuery.select('a.facet-link');
        var scope = this;
        Ext.each(items, function (input) {
            Ext.get(input).on('click', function () {
                scope.addFacet(this.id);
            });
        });
        
        items = Ext.DomQuery.select('li.facet-more-bt');
        scope = this;
        Ext.each(items, function (input) {
            Ext.get(input).on('click', function () {
                scope.displayMoreFacet(this, true);
            });
        });
        
        items = Ext.DomQuery.select('li.facet-less-bt');
        scope = this;
        Ext.each(items, function (input) {
            Ext.get(input).on('click', function () {
                scope.displayMoreFacet(this, false);
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
    /** private: method[displayFacetValue]
     *  :param node: ``String`` the key of the facet to add
     *  
     *  Return a facet value as HTML.
     */
    displayFacetValue: function (node, visible) {
        var data = {
            facet : node.nodeName,
            node : node.getAttribute('name'),
            label : node.getAttribute('label'),
            count : node.getAttribute('count')
        };
        // Only display a facet if it's not part of current filter
        
        if (this.currentFilterStore.getCount() === 0 ||
                this.currentFilterStore.query('value', data.node).length === 0) {
            var recId = this.facetsStore.getCount() + 1,
                 r = new this.facetsStore.recordType(data, recId); 
            this.facetsStore.add(r);
            return "<li class='" + (visible ? '' : 'facet-more') + "' style='" + (visible ? '' : 'display:none;') + "'><a href='javascript:void(0);' class='facet-link' id='" + recId + "'>" + 
                    (data.label != null ? data.label : data.node) + "<span class='facet-count'>(" + data.count + ")</span></a></li>";
        }
        return '';
    },
    /** private: method[displayMoreFacet]
     *  :param li: ``Object`` the li element corresponding to the more or less button
     *  :param more: ``boolean`` true for displaying element with facet-more class, false otherwise.
     *  
     *  Display or hide facet extra values.
     */
    displayMoreFacet: function (li, more) {
        var el = Ext.get(li);
        
        // Hide the clicked element
        el.setVisibilityMode(Ext.Element.DISPLAY);
        el.setVisible(false);
        
        // Search for all siblings with a facet-more class and switch the visibility
        Ext.each(li.parent().query('[class=facet-more]'), function(item) {
            var node = Ext.get(item);
            node.setVisibilityMode(Ext.Element.DISPLAY);
            node.setVisible(more, true);
        });
        
        // Display less or more button accordingly
        if (more) {
            li.next('[class=facet-less-bt]').setVisible(true, true);
        } else {
            li.prev('[class=facet-more-bt]').setVisible(true, true);
        }
    },
    /** private: method[addFacet]
     *  :param recordId: ``String`` the id of the facet to add in the facetsStore
     *  
     *  Add a new facet value filter.
     *  
     *  All active filters are registered in currentFilterStore.
     */
    addFacet: function (recordId) {
        var r = this.facetsStore.getById(recordId),
            form = this.searchForm, id = 'facet_' + this.counter ++;
        
        var data = {
            id: id, 
            facet: r.get('facet'), 
            value: r.get('node'), 
            label: r.get('label'),
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
                    text: (item.get('label') != null ? item.get('label') : item.get('node')),
                    handler: function (b, e) {
                        this.switchFacet(b);
                    },
                    scope: panel
                });
            });
            this.breadcrumb.add(new Ext.Button({
                id: data.bcid,
                text: (data.label != null ? data.label : data.node),
                menu: scrollMenu
            }));
            this.breadcrumb.doLayout();
        }
        this.searchForm.fireEvent('search');
    },
    /** private: method[switchFacet]
     *  :param elem: ``String`` the key of the facet to be removed
     *  
     *  Switch from one facet value to another updating the related
     *  element in the search form, updating the switcher label
     *  and triggering the search again.
     */
    switchFacet: function (elem) {
        var elemId = elem.getId(), filterKey = elemId.split('#')[0], newValue = elem.altText;
        
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
    
    /** private: method[removeFacet]
     *  :param filterKey: ``String`` the key of the facet to be removed
     *  :param silent: ``Boolean`` do not trigger search event of related search form
     *  
     *  Remove a facet from current filter list.
     *  
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
    /** public: method[reset]
     *  Clear all current filter in search form and breadcrumb (if set)
     */
    reset: function () {
        this.currentFilterStore.each(function (r) {
            this.removeFacet(r.get('id'), true);
        }, this);
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
