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

/**
 *
 */
/** api: (define) 
 *  module = GeoNetwork
 *  class = TagCloudView
 *  base_link = `Ext.DataView <http://extjs.com/deploy/dev/docs/?class=Ext.DataView>`_
 */
/** api: example
 *
 *  Create a tag cloud with automatic loading of results using the query config
 *  parameter.
 *
 *
 *  .. code-block:: javascript
 *
 *    var tagCloudView = new GeoNetwork.TagCloudView( {
 *          catalogue : catalogue,
 *          query : 'fast=true&summaryOnly',
 *          renderTo : 'tag'
 *      });
 *
 */
/** api: constructor .. class:: TagCloudView(config)
 *
 *  Create a catalogue tag cloud data view which interacts:
 *
 *   * with :class:`GeoNetwork.Catalogue`
 *
 *  A summary store needs to be configured for the catalogue using:
 *
 *
 *  .. code-block:: javascript
 *
 *     catalogue.summaryStore = GeoNetwork.data.MetadataSummaryStore;
 *
 *
 *  TODO : this widget require a global variable named catalogue
 *  due to javascript:catalogue.kvpSearch(, event should probably used instead
 *  on item click ?
 *
 */
GeoNetwork.TagCloudView = Ext.extend(Ext.DataView, {
    /** api: config[catalogue] 
     * ``GeoNetwork.Catalogue`` Catalogue to use
     */
    catalogue: undefined,
    
    multiSelect: true,
    defaultConfig: {
        /**
         * Search field to trigger search on
         */
        searchField: 'keyword',
        /**
         * Define the location of the facet to display in the tag cloud
         */
        root: 'keywords.keyword',
        /** api: config[qurey] 
         *  If defined, trigger the KVP query and update the data view.
         */
        query: undefined
    },
    onSuccess: null,
    onFailure: null,
    overClass: 'tag-cloud-hover',
    itemSelector: 'li.tag-cloud',
    emptyText: '',
    autoWidth: true,
    
    /** private: method[initComponent] 
     *  Initializes the metadata results view.
     */
    initComponent: function(){
    	Ext.applyIf(this, this.defaultConfig);
        GeoNetwork.TagCloudView.superclass.initComponent.call(this);

        this.tpl = this.tpl || new Ext.XTemplate(
            '<ul>', 
                '<tpl for=".">', 
                    '<li class="tag-cloud tag-cloud-{class}">',
                        // TODO : hitsPerPage should take in account the current search form
                        '<a href="#" onclick="javascript:catalogue.kvpSearch(\'fast=' + this.catalogue.metadataStore.fast + '&summaryOnly=0&from=1&to=20&hitsPerPage=20&' + 
                             this.searchField + 
                            '={value}\', ' + this.onSuccess + ',' + this.onFailure + 
                            ', null);" alt="{value}" title="{count} records">{value}</a>', 
                    '</li>', 
                '</tpl>', 
            '</ul>');
        this.catalogue.summaryStore = new GeoNetwork.data.MetadataSummaryStore(this.root);
        this.store = this.catalogue.summaryStore;
        if (this.query) {
            // run a query in fast mode to retrieve a summary
            this.catalogue.kvpSearch(this.query, null, null, null, true);
        }
    }
});

/** api: xtype = gn_tagcloudview */
Ext.reg('gn_tagcloudview', GeoNetwork.TagCloudView);
