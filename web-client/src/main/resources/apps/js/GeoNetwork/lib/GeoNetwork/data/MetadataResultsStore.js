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
 *  class = MetadataResultsStore
 */
/** api: constructor 
 *  .. class:: GeoNetwork.data.MetadataResultsStore()
 *
 *   A pre-configured `Ext.data.JsonStore <http://extjs.com/deploy/dev/docs/?class=Ext.data.JsonStore>`_
 *   for GeoNetwork results.
 *
 *   Depends on ``OpenLayers.Format.GeoNetworkRecords``.
 *
 *   Fields available are:
 *
 *   * id : The metadata internal identifier
 *   * uuid : The metadata unique identifier
 *   * schema : The schema (eg. dublin-code, iso19139)
 *   * title
 *   * abstract
 *   * subject  ``Array()``
 *   * thumbnail
 *   * links    ``Array()``
 *   * contacts ``Array()``
 *   * uri  Not used
 *   * isharvested
 *   * source
 *   * rating
 *   * popularity
 *   * download
 *   * ownername
 *   * valid : The validation status
 *   * valid_details : The validation information for each level of validation
 *   * edit
 *   * bbox ``Array()``
 *
 *   FIXME : this is not a constructor, it's a function.
 *   Is that good enough ? Comment also applied to other Stores
 *
 *   TODO : handle multiple thumbnail ?
 */
GeoNetwork.data.MetadataResultsStore = function(){

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
        var i, validity = [], validInfo;
        if (record.geonet_info && record.geonet_info.valid_details) {
            validInfo = record.geonet_info.valid_details;
            for (i = 0; i < validInfo.length; i++) {
                validity.push({
                            valid: validInfo.status[i].value,
                            type: validInfo.type[i].value,
                            ratio: validInfo.ratio[i].value
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
                currentUri = record.image[i].value;
                // Return the first URL even if not http (FIXME ?)
                if (currentUri.indexOf('http') !== -1 || i === 0) {
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
                el = record.responsibleParty[i];
                
                name = (record.responsibleParty.organisationName && record.responsibleParty.organisationName[i] ? 
                            record.responsibleParty.organisationName[i].value : '');
                contact.push({
                            applies: el.appliesTo,
                            logo: el.logo,
                            role: el.role,
                            name: name
                        });
            }
        }
        return contact;
    }

    function getOrganization(v, record) {
        var orgName, el;
        if (record.responsibleParty) {
                for (i = 0; i < record.responsibleParty.length; i++) {
                        var tokens = record.responsibleParty[i].value.split(GeoNetwork.data.MetadataResultsFastStore.separator);
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
                        var tokens = record.responsibleParty[i].value.split(GeoNetwork.data.MetadataResultsFastStore.separator);
                        if(tokens[4]) {
                                email = tokens[4];
                                break;
                            }
                    }
            }
        return email;
    }

    function getLinks(v, record){
        if (record.link) {
            return record.link;
        }
        return [];
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
    
    function getPopularity(v, record){
        if (record.geonet_info && record.geonet_info.popularity) {
            return record.geonet_info.popularity[0].value;
        } else {
            return '';
        }
    }
    
    function getRating(v, record){
        if (record.geonet_info && record.geonet_info.rating) {
            return record.geonet_info.rating[0].value;
        } else {
            return '';
        }
    }
    
    function getDownload(v, record){
        if (record.geonet_info && record.geonet_info.download) {
            return record.geonet_info.download[0].value;
        } else {
            return '';
        }
    }
    
    function getOwnerName(v, record){
        if (record.geonet_info && record.geonet_info.ownername) {
            return record.geonet_info.ownername[0].value;
        } else {
            return '';
        }
    }

    function getPublicationDate(v, record) {
        if (record.geonet_info && record.geonet_info.publicationDate) {
            return record.geonet_info.publicationDate[0].value;
        } else {
            return '';
        }
    }

    function getIsHarvested(v, record){
        if (record.geonet_info && record.geonet_info.isHarvested) {
            return record.geonet_info.isHarvested[0].value;
        } else {
            return '';
        }
    }
    function getHarvesterType(v, record){
        if (record.geonet_info && record.geonet_info.harvestInfo && record.geonet_info.harvestInfo.type) {
            return record.geonet_info.harvestInfo.type[0].value;
        } else {
            return '';
        }
    }
    function getCategory(v, record){
        if (record.geonet_info && record.geonet_info.category) {
            return record.geonet_info.category;
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
        if (record['abstract']) {
            return record['abstract'][0].value;
        } else {
            return '';
        }
    }
    function getType(v, record){
        if (record['type']) {
            return record['type'][0].value;
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
        if (record.geonet_info && record.geonet_info.displayOrder) {
            return record.geonet_info.displayOrder[0].value;
        } else {
            return 0;
        }
    }
    
    return new Ext.data.JsonStore({
        totalProperty: 'summary.count',
        root: 'records',
        fast: 'false',
        service: 'xml.search',
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
            name: 'thumbnail',
            convert: getThumbnails
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
            name : 'publicationdate',
            convert : this.getPublicationDate
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
        }, {
            name: 'displayOrder',
            convert: getDisplayOrder,
            sortType: 'asInt'
        }, {
            name: 'valid',
            mapping: 'geonet_info.valid[0].value',
            defaultValue: '-1'
        }, {
            name: 'valid_details',
            convert: getValidationInfo
        }]
    });
};
