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
Ext.namespace('GeoNetwork.data');

/** api: (define) 
 *  module = GeoNetwork.data
 *  class = ThesaurusStore
 */
/** api: method[ThesaurusStore]
 *  A pre-configured `Ext.data.Store <http://extjs.com/deploy/dev/docs/?class=Ext.data.Store>`_
 *  for GeoNetwork SKOS thesaurus.
 *
 *  :param url: ``String`` Usually the xml.thesaurus.getList service URL.
 */
GeoNetwork.data.ThesaurusStore = function(config){
    var DataRecord = Ext.data.Record.create([{
        name: 'filename'
    }, {
        name: 'title'
    }, {
        name: 'theme',
        mapping: 'dname'
    }, {
        name: 'id',
        mapping: 'key'
    }, {
        name: 'defaultNamespace',
    }, {
        name: 'type'
    }, {
        name: 'activated'
    }]);
    
    var store = new Ext.data.Store({
        url: config.url, // FIXME
        reader: new Ext.data.XmlReader({
            record: 'thesaurus'
        }, DataRecord),
        sortInfo: config.sortInfo || {
            field: 'title',
            direction: 'ASC'
        },
        fields: ['filename', 'theme', 'id', 'title', 'type', 'activated', 'defaultNamespace'],
        listeners: config.listeners
    });
    
    if (config.allOption) {
        // add the "any thesaurus" record
        var record = new DataRecord({
            filename: OpenLayers.i18n('anyThesaurus'),
            activated: 'y'
        });
        record.set('id', '');
        store.add(record);
    }
    
    if (config.activatedOnly) {
        // Filter activated thesaurus only
        store.on('load', function() {
            var coll = this.query('activated', 'n');
            coll.each(function (item, idx) {
                this.removeAt(this.indexOf(item));
                }, store);
        }, store);
    }
    
    store.load({
        add: true
    });
    
    return store;
};
