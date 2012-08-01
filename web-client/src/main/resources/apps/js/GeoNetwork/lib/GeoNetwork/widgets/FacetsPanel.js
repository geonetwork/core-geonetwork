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
        autoScroll: true
    },
    facetsStore: new Ext.data.JsonStore({
        fields : [ 'facet', 'node', 'count' ]
    }),
    searchForm: null,
    serviceUrl: null,
    catalogue: null,
    afterFacetClick: function(){
        
    },
    
    refresh: function (response) {
        var facets = response.responseXML.childNodes[0].childNodes[1],
            zappette = '<div id="breadcrumb"></div>', store = this.facetsStore, recId = 0;
        
        store.removeAll();

        if (facets.nodeName === 'summary') {
            // TODO : Use template
            Ext.each(facets.childNodes, function(facet) {
                if (facet.nodeName != '#text' && facet.childNodes.length > 0) {
                    zappette += "<h1 class='facet'>" + OpenLayers.i18n(facet.nodeName) + "</h1>";
                    Ext.each(facet.childNodes, function(node) {
                        if (node.getAttribute) {
                            var data = {
                                facet : node.nodeName,
                                node : node.getAttribute('name'),
                                count : node.getAttribute('count')
                            };
                            var r = new store.recordType(data, ++recId); 
                            store.add(r);
                            zappette += "<h2><a href='javascript:void(0);' class='facet' id='" + recId + "'>" 
                                + node.getAttribute('name') + "&nbsp;(" + node.getAttribute('count') + ")</a></h2>"
                        }
                    });
                }
            });
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
    addFacet: function(id) {
        var r = this.facetsStore.getById(id);
        var form = this.searchForm;
        // FIXME HACK 
        var key = (r.get('facet') === 'organizationName' ? "orgName" : r.get('facet'));
        
        var field = Ext.getCmp('E_' + key);
        
        if (field) {
            // TODO
            alert('More than one criteria on a facet is not yet supported.');
            return;
            // TODO field.setValue(field.getValue() + ' AND ' + r.get('node') + '');
        } else {
            form.insert(0, new Ext.form.TextField({
                id: 'E_' +key,
                name: 'E_' + key,
                value: r.get('node'),
                inputType: 'hidden'
            }));
        }
        
        // TODO escape '
        if (Ext.getDom('breadcrumb')) {
            // TODO : i18n
            var id = 'facet_' + key + '_' + id;
            Ext.getDom('breadcrumb').innerHTML += '<div class="bcfacet" id="' + id + '" title="Click to remove filter.">'
            + '<a href="javascript:void(0);">' 
            + OpenLayers.i18n(r.get('facet')) + ": " + r.get('node') + "</a>&nbsp;>&nbsp;</div>";
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
        Ext.getDom('breadcrumb') && Ext.get(id).remove();
        var field = Ext.getCmp('E_' + id.split('_')[1]);
        this.searchForm.remove(field);
        this.searchForm.fireEvent('search');
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
