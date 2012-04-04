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
 *  class = CategoryView
 *  base_link = `Ext.DataView <http://extjs.com/deploy/dev/docs/?class=Ext.DataView>`_
 */
/** api: example
 *
 *  Create a list of categories for fast searching from homepage
 *
 *
 *  .. code-block:: javascript
 *
 *    var CategoryView = new GeoNetwork.CategoryView( {
 *          catalogue : catalogue,
 *          query : 'fast=true&summaryOnly',
 *          renderTo : 'tag'
 *      });
 *
 */
/** api: constructor .. class:: CategoryView(config)
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
GeoNetwork.CategoryView = Ext.extend(Ext.DataView, {
    /** api: config[catalogue]
     * ``GeoNetwork.Catalogue`` Catalogue to use
     */
    catalogue: undefined,

    /** api: config[url]
     * url to get categories from
     */
    url: "xml.info?type=categories",

    /** api: config[imgUrl]
     * url for prefix images
     */
    imgUrl: "",

    /** api: config[searchField]
     * field to search on
     */
    searchField: 'category',

    onSuccess: null,
    onFailure: null,

    emptyText: '',

    autoWidth: true,

    /** private: method[initComponent]
     *  Initializes the metadata results view.
     */
    initComponent: function(){
        GeoNetwork.CategoryView.superclass.initComponent.call(this);

        this.tpl = this.tpl || new Ext.XTemplate(
            '<tpl for="."><div class="category-view-item">',
            '<a href="javascript:void(catalogue.kvpSearch(\'fast=' + this.catalogue.metadataStore.fast + '&summaryOnly=0&from=1&to=20&hitsPerPage=20&' + this.searchField + '={name}\', ' + this.onSuccess + ',' + this.onFailure + ', null));" alt="{name}">',
            this.imgUrl ? '<img src="' + this.imgUrl + '{name}.png" border="0" />' : '',
            '{[values.label.' + OpenLayers.Lang.getCode() + ']}</a></div></tpl>');

        this.store = GeoNetwork.data.CategoryStore(this.url);
        this.store.load();

    }
});

/** api: xtype = gn_categoryview */
Ext.reg('gn_categoryview', GeoNetwork.CategoryView);

