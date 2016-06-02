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
 *  class = MetadataThumbnailStore
 */
/** api: method[MetadataThumbnailStore]
 *  A pre-configured `Ext.data.JsonStore <http://extjs.com/deploy/dev/docs/?class=Ext.data.JsonStore>`_
 *  for GeoNetwork metadata thumbnails.
 *
 *  :param url: ``String`` Usually the metadata.thumbnail service URL.
 *  
 *  
 *  .. code-block:: javascript
 *  
 *     this.store = new GeoNetwork.data.MetadataThumbnailStore(
 *                         catalogue.services.getThumbnail, 
 *                         {id: this.metadataId}
 *                  );
 *  
 */
GeoNetwork.data.MetadataThumbnailStore = function(url, params){

    var fields = [{
        name: 'href'
    }, {
        name: 'desc'
    }, {
        name: 'type'
    }, {
        name: 'mimetype'
    }];
    
    return new Ext.data.XmlStore({
        autoDestroy: true,
        proxy: new Ext.data.HttpProxy({
            method: 'POST',
            url: url,
            params: params,
            disableCaching: false
        }),
        record: 'thumbnail',
        idPath: 'href',
        fields: fields
    });
};