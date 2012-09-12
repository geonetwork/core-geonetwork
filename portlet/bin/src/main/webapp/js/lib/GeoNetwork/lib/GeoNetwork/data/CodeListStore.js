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
 *  class = CodeListStore
 */
/** api: method[CodeListStore]
 *  A pre-configured `Ext.data.Store <http://extjs.com/deploy/dev/docs/?class=Ext.data.Store>`_
 *  for GeoNetwork codeList. Codelist are defined by a code, a label and a description
 *
 *  :param url: ``String`` Usually the xml.schema.info service URL.
 */
GeoNetwork.data.CodeListStore = function(config){

    var schema = config.schema || 'iso19139';
    var requestBody = '<request><codelist schema="' + schema +
                            '" name="' + config.codeListName +
                        '" /></request>';
    var model = [{
            name: 'code'
        }, {
            name: 'label'
        }, {
            name: 'description'
        }];
        
    var DataRecord = Ext.data.Record.create(model);
    var store = new Ext.data.Store({
        fields: model
    });
    
    // Improve ? Here we use OL to load the response (how to POST body using Ext)
    // and load results in the returned store.
    OpenLayers.Request.POST({
        url: config.url,
        data: requestBody,
        success: function(response){
            var help = response.responseXML.getElementsByTagName('entry');
            
            for (var i = 0; i < help.length; i++) {
                var record = new DataRecord({
                    code: help[i].getElementsByTagName('code')[0].firstChild.nodeValue,
                    label: help[i].getElementsByTagName('label')[0].firstChild.nodeValue,
                    description: help[i].getElementsByTagName('description')[0].firstChild.nodeValue
                });
                store.add(record);
            }
        }
    });
    
    return store;
};