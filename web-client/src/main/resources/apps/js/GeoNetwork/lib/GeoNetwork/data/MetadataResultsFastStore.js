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
 *  class = MetadataResultsFastStore
 */
/** api: constructor 
 *  .. class:: GeoNetwork.data.MetadataResultsFastStore()
 *
 *   A pre-configured `Ext.data.JsonStore <http://extjs.com/deploy/dev/docs/?class=Ext.data.JsonStore>`_
 *   for GeoNetwork results.
 *   
 *   To be used with the "q" search service which use only index content.
 *   
 *   
 *   TODO : Merge by extension with MetadataResultsStore
 *
 */
GeoNetwork.data.MetadataResultsFastStore = function(){
	var separator = "|";
	
    function getTitle(v, record){
        if (record.title && record.title[0]) {
            return record.title[0].value;
        } else if (record.defaultTitle && record.defaultTitle[0]) {
            return record.defaultTitle[0].value;
        } else {
            return '';
        }
    }
    function getValidationInfo(v, record){
        if (record.valid) {
            return record.valid[0].value;
        } else {
            return '-1';
        }
    }
    function getIdxMsg(v, record){
        if (record.idxMsg) {
            var info = record.idxMsg[0].value.split('|');
            return info;
        } else {
            return '';
        }
    }
    function getValidationDetails(v, record){
        var i, validity = [], validInfo;
        for (var key in record) {
    	   if (record.hasOwnProperty(key) && key.indexOf('valid_') !== -1) {
    	     var obj = record[key];
    	     validity.push({
                 valid: obj[0].value,
                 type: key.split('_')[1],
                 ratio: '' // TODO
             });
    	   }
    	}
        return validity;
    }
    
    function getThumbnails(v, record){
        var i;
        var uri = '';
        var currentUri;
        
        if (record.image) {
        
            for (i = 0; i < record.image.length; i++) {
            	var tokens = record.image[i].value.split(separator);
                currentUri = tokens[1];
                // Return the first URL even if not http (FIXME ?)
                if (tokens[0] == 'thumbnail' && currentUri.indexOf('http') !== -1 || i === 0) {
                    uri = currentUri;
                }
            }
        }
        return uri;
    }

    function getOverview(v, record){
        var i;
        var uri = '';
        var currentUri;
        
        if (record.image) {
        
            for (i = 0; i < record.image.length; i++) {
            	var tokens = record.image[i].value.split(separator);
                currentUri = tokens[1];
                // Return the first URL even if not http (FIXME ?)
                if (tokens[0] == 'overview' && currentUri.indexOf('http') !== -1 || i === 0) {
                    uri = currentUri;
                }
            }
        }
        return uri;
    }
    
    function getContact(v, record){
        var i, contact = [], el, name;
        
        if (record.responsibleParty) {
            for (i = 0; i < record.responsibleParty.length; i++) {
                var tokens = record.responsibleParty[i].value.split(separator);
                contact.push({
                            applies: tokens[1],
                            logo: tokens[3],
                            role: tokens[0],
                            name: tokens[2]
                        });
            }
        }
        return contact;
    }
    function getOrganization(v, record) {
      var orgName, el;
        if (record.responsibleParty) {
                    for (i = 0; i < record.responsibleParty.length; i++) {
                            var tokens = record.responsibleParty[i].value.split(separator);
                            if(tokens[2]) {
                                    orgName = tokens[2];
                                    break;
                                }
                        }
                }
        return orgName;
    }

    function getEmail(v, record) {
            var email, el;
            if (record.responsibleParty) {
                    for (i = 0; i < record.responsibleParty.length; i++) {
                            var tokens = record.responsibleParty[i].value.split(separator);
                            if(tokens[4]) {
                                    email = tokens[4];
                                    break;
                                }
                        }
                }
            return email;
       }

    function getLinks(v, record){
    	var links = [];
        if (record.link) {
        	for (i = 0; i < record.link.length; i++) {
            	var tokens = record.link[i].value.split(separator);
            	links.push({
            		name: tokens[0],
            		title: tokens[1],
            		href: tokens[2],
            		protocol: tokens[3],
            		type: tokens[4]
            	});
            }
        }
        return links;
    }
    
    /**
     * Some convert function to face empty geonet_info parameters
     * BUG in GeoNetwork when retrieving iso19115 record through CSW
     */
    function getSource(v, record){
        if (record.geonet_info && record.geonet_info.source) {
            return record.geonet_info.source[0].value;
        } else {
            return '';
        }
    }

    function getCredit(v, record){
        if (record.credit) {
            return record.credit;
        } else {
            return '';
        }
    }
    
    function getPopularity(v, record){
        if (record.popularity) {
            return record.popularity[0].value;
        } else {
            return '';
        }
    }
    
    function getRating(v, record){
        if (record.rating) {
            return record.rating[0].value;
        } else {
            return '';
        }
    }
    
    function getDownload(v, record){
        if (record.geonet_info && record.geonet_info.download) {
            return (record.geonet_info.download[0].value === 'true');
        } else {
            return false;
        }
    }
    
    function getDynamic(v, record){
        if (record.geonet_info && record.geonet_info.dynamic) {
            return (record.geonet_info.dynamic[0].value === 'true');
        } else {
            return false;
        }
    }
    
    function getOwnerName(v, record){
        if (record.userinfo && record.userinfo[0].value) {
            var userinfo = record.userinfo[0].value.split(separator);
            try {
                return userinfo[2] + " " + userinfo[1]; // User profile + ' (' + OpenLayers.i18n(userinfo[3]) + ')';
			} catch (e) {
				return '';
			}
        } else {
            return '';
        }
    }
    
    function getIsHarvested(v, record){
        if (record.isHarvested) {
            return record.isHarvested[0].value;
        } else {
            return '';
        }
    }
    function getHarvesterType(v, record){
    	// FIXME
        if (record.geonet_info && record.geonet_info.harvestInfo && record.geonet_info.harvestInfo.type) {
            return record.geonet_info.harvestInfo.type[0].value;
        } else {
            return '';
        }
    }
    function getCategory(v, record){
        if (record.category) {
            return record.category;
        } else {
            return '';
        }
    }
    function getStatus(v, record){
        if (record.status) {
            return record.status;
        } else {
            return '';
        }
    }
    function getChangeDate(v, record){
        if (record.geonet_info && record.geonet_info.changeDate) {
            return record.geonet_info.changeDate[0].value;
        } else {
            return '';
        }
    }
    function getCreateDate(v, record){
        if (record.geonet_info && record.geonet_info.createDate) {
            return record.geonet_info.createDate[0].value;
        } else {
            return '';
        }
    }
    function getSelected(v, record){
        if (record.geonet_info && record.geonet_info.selected) {
            return record.geonet_info.selected[0].value;
        } else {
            return '';
        }
    }
    function getAbstract(v, record){
        if (record['abstract'] && record['abstract'][0]) {
            return record['abstract'][0].value;
        } else {
            return '';
        }
    }
    function getType(v, record){
        if (record['type'] && record['type'][0]) {
            return record['type'][0].value;
        } else {
            return '';
        }
    }
    function getSpatialRepresentationType(v, record){
        if (record['spatialRepresentationType'] && record['spatialRepresentationType'][0]) {
            return record['spatialRepresentationType'][0].value;
        } else {
            return '';
        }
    }
    function getEdit(v, record){
        if (record.geonet_info && record.geonet_info.edit) {
            return record.geonet_info.edit[0].value;
        } else {
            return 'false';
        }
    }
    function getDisplayOrder(v, record){
        if (record.displayOrder) {
            return record.displayOrder[0].value;
        } else {
            return 0;
        }
    }
    
    
    return new Ext.data.JsonStore({
        totalProperty: 'summary.count',
        root: 'records',
        fast: 'index',
        service: 'q',
        fields: [{
            name: 'title',
            convert: getTitle
        }, {
            name: 'abstract',
            convert: getAbstract
        }, {
            name: 'type',
            convert: getType
        }, {
            name: 'subject',
            mapping: 'keyword',
            defaultValue: ''
        }, {
            name: 'spatialRepresentationType',
            convert: getSpatialRepresentationType
        }, {
            name: 'uuid',
            mapping: 'geonet_info.uuid[0].value',
            defaultValue: ''
        }, {
            name: 'id',
            mapping: 'geonet_info.id[0].value',
            defaultValue: ''
        }, {
            name: 'schema',
            mapping: 'geonet_info.schema[0].value',
            defaultValue: ''
        }, {
            name: 'contact',
            convert: getContact
        }, {
            name: 'email',
            convert: getEmail
        }, {
            name: 'organization',
            convert: getOrganization
        }, {
            name: 'credit',
            convert: getCredit
        }, {
            name: 'thumbnail',
            convert: getThumbnails
        }, {
            name: 'overview',
            convert: getOverview
        }, {
            name: 'links',
            convert: getLinks
        }, {
            name: 'uri',
            mapping: 'uri',
            defaultValue: ''
        }, {
            name: 'isharvested',
            convert: getIsHarvested
        }, {
            name: 'harvestertype',
            convert: getHarvesterType
        }, {
            name: 'createdate',
            convert: getCreateDate
        }, {
            name: 'changedate',
            convert: getChangeDate
        }, {
            name: 'selected',
            convert: getSelected
        }, {
            name: 'source',
            convert: getSource
        }, {
            name: 'category',
            convert: getCategory
        }, {
            name: 'status',
            convert: getStatus
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
            name: 'dynamic',
            convert: getDynamic
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
        }, {
            name: 'displayOrder',
            convert: getDisplayOrder,
            sortType: 'asInt'
        }, {
            name: 'valid',
            convert: getValidationInfo
        }, {
            name: 'valid_details',
            convert: getValidationDetails
        }, {
            name: 'idxMsg',
            convert: getIdxMsg
        }
        ]
    });
};
