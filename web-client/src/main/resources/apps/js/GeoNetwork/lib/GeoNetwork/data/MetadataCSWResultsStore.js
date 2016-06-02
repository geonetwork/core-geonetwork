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
 *  class = MetadataCSWResultsStore
 */
/** api: constructor 
 *  .. class:: GeoNetwork.data.MetadataCSWResultsStore()
 *
 *    A pre-configured `Ext.data.JsonStore <http://extjs.com/deploy/dev/docs/?class=Ext.data.JsonStore>`_
 *    for CSW results. A CSW store does not contains
 *    any catalogue information about metadata (ie. geonet:info:* elements). This store
 *    could only be used to do search and display results (not metadata
 *    management).
 *
 *    Records will be similar to :class:`GeoNetwork.data.MetadataResultsStore` records.
 *
 *    Depends on ``OpenLayers.Format.GeoNetworkRecords``.
 */
GeoNetwork.data.MetadataCSWResultsStore = function(){

    function getThumbnails(v, record){
        var i, uri;
        
        if (record.URI) {
            for (i = 0; i < record.URI.length; i++) {
                uri = record.URI[i];
                if (uri.name === 'thumbnail') {
                    if (uri.value.indexOf('http') === -1) {
                        return GeoNetwork.Catalogue.URL + "/srv/en/" + uri.value;
                    } else {
                        return uri.value;
                    }
                }
            }
        }
        return '';
    }
    
    function getCapabilitiesUrl(v, record){
        var i, uri;
        
        if (record.URI) {
            for (i = 0; i < record.URI.length; i++) {
                uri = record.URI[i];
                if (uri.protocol && uri.protocol.indexOf('http-get-capabilities') !== -1) {
                        return uri.value;
                }
            }
        }
        return '';
    }
    
    /**
     * Some convert function to face empty geonet_info parameters BUG in
     * GeoNetwork when retrieving iso19115 record through CSW
     */
    function getSource(v, record){
        if (record.geonet_info) {
            return record.geonet_info.source[0];
        } else {
            return '';
        }
    }
    
    function getPopularity(v, record){
        if (record.geonet_info) {
            return record.geonet_info.popularity[0];
        } else {
            return '';
        }
    }
    
    function getRating(v, record){
        if (record.geonet_info) {
            return record.geonet_info.rating[0];
        } else {
            return '';
        }
    }
    
    function getDownload(v, record){
        if (record.geonet_info) {
            return record.geonet_info.download[0];
        } else {
            return '';
        }
    }
    
    function getOwnerName(v, record){
        if (record.geonet_info) {
            return record.geonet_info.ownername[0];
        } else {
            return '';
        }
    }
    
    function getIsHarvested(v, record){
        if (record.geonet_info && record.geonet_info.is_harvested) {
            return record.geonet_info.is_harvested[0];
        } else {
            return '';
        }
    }
    
    function getEdit(v, record){
        if (record.geonet_info && record.geonet_info.edit) {
            return record.geonet_info.edit;
        } else {
            return 'false';
        }
    }
    
    
    return new Ext.data.JsonStore({
        totalProperty: 'SearchResults.numberOfRecordsMatched',
        root: 'records',
        fields: [{
            name: 'title',
            mapping: 'title[0].value',
            defaultValue: ''
        }, {
            name: 'abstract',
            mapping: 'abstract',
            defaultValue: ''
        }, {
            name: 'subject',
            mapping: 'subject'
        }, {
            name: 'uuid',
            mapping: 'identifier[0].value',
            defaultValue: ''
        }, {
            name: 'thumbnail',
            convert: getThumbnails
        }, {
            name: 'uri',
            mapping: 'uri',
            defaultValue: ''
        }, {
            name: 'getCapabilitiesUrl',
            convert: getCapabilitiesUrl
        }, {
            name: 'isharvested',
            convert: getIsHarvested
        }, {
            name: 'source',
            convert: getSource
        }, {
            name: 'rating',
            convert: getRating
        }, {
            name: 'popularity',
            convert: getPopularity
        }, {
            name: 'download',
            convert: getDownload
        }, {
            name: 'ownername',
            convert: getOwnerName
        }, {
            name: 'edit',
            convert: getEdit
        }, {
            name: 'bbox',
            mapping: 'BoundingBox',
            defaultValue: ''
        }]
    });
};