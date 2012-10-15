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
 *  class = MetadataRelationStore
 */
/** api: method[MetadataRelationStore]
 *  A pre-configured `Ext.data.JsonStore <http://extjs.com/deploy/dev/docs/?class=Ext.data.JsonStore>`_
 *  for GeoNetwork metadata relation (relation to services, datasets, feature catalogues
 *  sources, relations and parent records).
 *
 *  :param url: ``String`` Usually the xml.relation service URL.
 *  
 *  :param grouping: ``boolean`` Set to true to return a Ext.data.GroupingStore
 */
GeoNetwork.data.MetadataRelationStore = function(url, params, grouping){
    var fields = [{
        name: 'id'
    }, {
        name: 'uuid'
    }, {
        name: 'title'
    }, {
        name: 'abstract'
    }, {
        name: 'keyword'
    }, {
        name: 'type',
        mapping: '@type'
    }, {
        name: 'subType',
        mapping: '@subType'
		}];
    
    if (grouping) {
        var reader = new Ext.data.XmlReader({
            record: 'relation',
            idProperty: 'uuid'
        }, fields);
        
        return new Ext.data.GroupingStore({
            autoDestroy: true,
            proxy: new Ext.data.HttpProxy({
                method: 'POST',
                url: url,
                params: params,
                disableCaching: false
            }),
            reader: reader,
            groupField: 'type',
						sortInfo: {
							field: 'type',
							direction: "ASC"
						}
        });
    } else {
        return new Ext.data.XmlStore({
            autoDestroy: true,
            proxy: new Ext.data.HttpProxy({
                method: 'GET',
                url: url,
                disableCaching: false
            }),
            record: 'relation',
            idPath: 'uuid',
            fields: fields
        });
    }
};
