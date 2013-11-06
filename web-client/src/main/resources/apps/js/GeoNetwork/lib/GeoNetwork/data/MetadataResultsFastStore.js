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

/**
 * api: (define) module = GeoNetwork.data class = MetadataResultsFastStore
 */
/**
 * api: constructor .. class:: GeoNetwork.data.MetadataResultsFastStore()
 * 
 * A pre-configured `Ext.data.JsonStore
 * <http://extjs.com/deploy/dev/docs/?class=Ext.data.JsonStore>`_ for GeoNetwork
 * results.
 * 
 * To be used with the "q" search service which use only index content.
 * 
 * 
 * TODO : Merge by extension with MetadataResultsStore
 * 
 */
GeoNetwork.data.MetadataResultsFastStore = function() {
    var separator = "|";

    function getFullTitle(v, record) {
    	var title;
        if (record.title && record.title[0]) {
            title = record.title[0].value;
        } else if (record.defaultTitle && record.defaultTitle[0]) {
        	title = record.defaultTitle[0].value;
        } else {
        	title = translate('missing');
        }

        return title;
    }
    function getTitle(v, record) {
    	var title;
        if (record.title && record.title[0]) {
            title = record.title[0].value;
        } else if (record.defaultTitle && record.defaultTitle[0]) {
        	title = record.defaultTitle[0].value;
        } else {
        	title = translate('missing');
        }
        if(title.length > 50) {
        	return title.substring(0,47)+"...";
        } else {
        	return title;
        }
    }

    function getGroupLogoUuid(v, record) {

        if (record.groupLogoUuid && record.groupLogoUuid[0]) {
            return record.groupLogoUuid[0].value + ".png";
        } else if (record.geonet_info && record.geonet_info.source
                && record.geonet_info.source[0]) {
            return record.geonet_info.source[0].value + ".gif";
        } else {
            return '';
        }
    }
    function getCatalogName(v, record) {

        if (record.groupLogoUuid && record.groupLogoUuid[0]) {
            return "group=" + encodeURI(record.groupLogoUuid[0].value);
        } else if (record.geonet_info && record.geonet_info.source
                && record.geonet_info.source[0]) {
            return "source=" + encodeURI(record.geonet_info.source[0].value);
        } else {
            return '';
        }
    }
    function getGroupWebsite(v, record) {
        if (record.geonet_info && record.geonet_info.groupWebsite) {
            return record.geonet_info.groupWebsite[0].value;
        } else {
            return '';
        }
    }
    function getValidationInfo(v, record) {
        if (record.valid) {
            return record.valid[0].value;
        } else {
            return '-1';
        }
    }
    function getIdxMsg(v, record) {
        if (record.idxMsg) {
            var info = record.idxMsg[0].value.split('|');
            return info;
        } else {
            return '';
        }
    }
    function getValidationDetails(v, record) {
        var i, validity = [], validInfo;
        for ( var key in record) {
            if (record.hasOwnProperty(key) && key.indexOf('valid_') !== -1) {
                var obj = record[key];
                validity.push({
                    valid : obj[0].value,
                    type : key.split('_')[1],
                    ratio : '' // TODO
                });
            }
        }
        return validity;
    }

    function getThumbnails(v, record) {
        var i;
        var uri = '';
        var currentUri;

        if (record.image) {

            for (i = 0; i < record.image.length; i++) {
                var tokens = record.image[i].value.split(separator);
                currentUri = tokens[1];
                // Return the first URL even if not http (FIXME ?)
                if (currentUri.indexOf('http') !== -1 || i === 0) {
                    uri = currentUri;
                }
            }
        }
        return uri;
    }

    function getContact(v, record) {
        var i, contact = [], el, name;

        if (record.responsibleParty) {
            for (i = 0; i < record.responsibleParty.length; i++) {
                var tokens = record.responsibleParty[i].value.split(separator);
                contact.push({
                    applies : tokens[1],
                    logo : tokens[3],
                    role : tokens[0],
                    name : tokens[2]
                });
            }
        }
        return contact;
    }

    function getLinks(v, record) {
        var links = [];
        if (record.service) {
            Ext.each(record.service, function(service) {
                Ext.each(record.service.url, function(url) {
                    links.push({
                        name : url.name,
                        title : url.name,
                        href : url.url,
                        protocol : url.type,
                        type : url.type,
                        uuid : url.uuid
                    });
                });
            });
        }
        if (record.link) {
            Ext.each(record.link, function(link) {
                var values = link.value.split("|");
                links.push({
                    name : values[0] || values[1],
                    title : values[0] || values[1],
                    href : values[2],
                    protocol : values[4],
                    type : "WWWLINK"
                });
            });
        }
        return links;
    }
    function hasLinks(v, record) {
    	var links = getLinks(v,record).length;
    	return links > 0;
    }
    /**
     * Some convert function to face empty geonet_info parameters BUG in
     * GeoNetwork when retrieving iso19115 record through CSW
     */
    function getSource(v, record) {
        if (record.geonet_info && record.geonet_info.source) {
            return record.geonet_info.source[0].value;
        } else {
            return '';
        }
    }

    function getCredit(v, record) {
        if (record.credit) {
            return record.credit;
        } else {
            return '';
        }
    }

    function getPopularity(v, record) {
        if (record.popularity) {
            return record.popularity[0].value;
        } else {
            return '';
        }
    }

    function getRating(v, record) {
        if (record.rating) {
            return record.rating[0].value;
        } else {
            return '';
        }
    }

    function getDownload(v, record) {
        if (record.geonet_info && record.geonet_info.download) {
            return (record.geonet_info.download[0].value === 'true');
        } else {
            return false;
        }
    }

    function getDynamic(v, record) {
        if (record.geonet_info && record.geonet_info.dynamic) {
            return (record.geonet_info.dynamic[0].value === 'true');
        } else {
            return false;
        }
    }

    function getOwnerId(v, record) {
        if (record.owner && record.owner.length > 0 && record.owner[0].value) {
            return record.owner[0].value;
        } else {
            return false;
        }
    }

    function getOwnerName(v, record) {
        if (record.userinfo && record.userinfo[0].value) {
            var userinfo = record.userinfo[0].value.split(separator);
            try {
                if(userinfo[2] !== userinfo[1]) {
                    return userinfo[2] + " " + userinfo[1];
                } else {
                    return userinfo[1];
                }
            } catch (e) {
                return '';
            }
        } else {
            return '';
        }
    }

    function getHistoricalArchive(v, record) {
    	return (record.historicalArchive && record.historicalArchive[0].value=='y');
    }
    function getIsHarvested(v, record) {
        if (record.isHarvested) {
            return record.isHarvested[0].value;
        } else {
            return '';
        }
    }
    function getHarvesterType(v, record) {
        // FIXME
        if (record.geonet_info && record.geonet_info.harvestInfo
                && record.geonet_info.harvestInfo.type) {
            return record.geonet_info.harvestInfo.type[0].value;
        } else {
            return '';
        }
    }
    function getCategory(v, record) {
        if (record.category) {
            return record.category;
        } else {
            return '';
        }
    }
    function getChangeDate(v, record) {
        if (record.geonet_info && record.geonet_info.changeDate) {
            return record.geonet_info.changeDate[0].value;
        } else {
            return '';
        }
    }
    function getCreateDate(v, record) {
        if (record.geonet_info && record.geonet_info.createDate) {
            return record.geonet_info.createDate[0].value;
        } else {
            return '';
        }
    }
    function getRevisionDate(v, record) {
        if (record['revisionDate'] && record['revisionDate'][0]) {
            return record['revisionDate'][0].value;
        } else {
            return '';
        }
    }
    function getSelected(v, record) {
        if (record.geonet_info && record.geonet_info.selected) {
            return record.geonet_info.selected[0].value;
        } else {
            return '';
        }
    }
    function getAbstract(v, record) {
        if (record['abstract'] && record['abstract'][0]) {
            return record['abstract'][0].value;
        } else if (record['defaultAbstract'] && record['defaultAbstract'][0]) {
            return record['defaultAbstract'][0].value;
        } else {
            return '';
        }
    }
    function getType(v, record) {
        if (record['type'] && record['type'][0]) {
            return record['type'][0].value;
        } else {
            return '';
        }
    }
    function getSpatialRepresentationType(v, record) {
        if (record['spatialRepresentationType']
                && record['spatialRepresentationType'][0]) {
            return record['spatialRepresentationType'][0].value;
        } else {
            return '';
        }
    }
    function getEdit(v, record) {
        if (record.geonet_info && record.geonet_info.edit) {
            return record.geonet_info.edit[0].value;
        } else {
            return 'false';
        }
    }
    function getDisplayOrder(v, record) {
        if (record.geonet_info && record.geonet_info.displayOrder) {
            return record.geonet_info.displayOrder[0].value;
        } else {
            return 0;
        }
    }
    function isService(v, record) {
        if (record.type) {
            for (i = 0; i < record.type.length; i++) {
                if (record.type[i].value == 'service')
                    return true;
            }
        }

        return false;
    }
    function isDataset(v, record) {
        if (record.type) {
            for (i = 0; i < record.type.length; i++) {
                if (record.type[i].value == 'dataset')
                    return true;
            }
        }

        return false;
    }

    return new Ext.data.JsonStore({
        totalProperty : 'summary.count',
        root : 'records',
        fast : 'index',
        service : 'q',
        fields : [ {
            name : 'title',
            convert : getTitle
        }, {
            name : 'fulltitle',
            convert : getFullTitle
        }, {
            name : 'abstract',
            convert : getAbstract
        }, {
            name : 'type',
            convert : getType
        }, {
            name : 'isservice',
            convert : isService
        }, {
            name : 'isdataset',
            convert : isDataset
        }, {
            name : 'groupLogoUuid',
            convert : getGroupLogoUuid
        },{
            name : 'catalogName',
            convert : getCatalogName
        }, {
            name : 'groupWebsite',
            convert : getGroupWebsite
        }, {
            name : 'subject',
            mapping : 'keyword',
            defaultValue : ''
        }, {
            name : 'spatialRepresentationType',
            convert : getSpatialRepresentationType
        }, {
            name : 'uuid',
            mapping : 'geonet_info.uuid[0].value',
            defaultValue : ''
        }, {
            name : 'id',
            mapping : 'geonet_info.id[0].value',
            defaultValue : ''
        }, {
            name : 'schema',
            mapping : 'geonet_info.schema[0].value',
            defaultValue : ''
        }, {
            name : 'contact',
            convert : getContact
        }, {
            name : 'credit',
            convert : getCredit
        }, {
            name : 'thumbnail',
            convert : getThumbnails
        }, {
            name : 'links',
            convert : getLinks
        },  {
            name : 'hasLinks',
            convert : hasLinks
        }, {
            name : 'uri',
            mapping : 'uri',
            defaultValue : ''
        },{
            name : 'historicalArchive',
            convert : getHistoricalArchive
        }, {
            name : 'isharvested',
            convert : getIsHarvested
        }, {
            name : 'harvestertype',
            convert : getHarvesterType
        }, {
            name : 'createdate',
            convert : getCreateDate
        }, {
            name : 'changedate',
            convert : getChangeDate
        }, {
            name : 'revisionDate',
            convert : getRevisionDate
        }, {
            name : 'selected',
            convert : getSelected
        }, {
            name : 'source',
            convert : getSource
        }, {
            name : 'category',
            convert : getCategory
        }, {
            name : 'rating',
            convert : getRating
        }, {
            name : 'popularity',
            convert : getPopularity
        }, {
            name : 'download',
            convert : getDownload
        }, {
            name : 'dynamic',
            convert : getDynamic
        }, {
            name : 'ownername',
            convert : getOwnerName
        }, {
        	name : 'ownerid',
        	convert : getOwnerId
        }, {
            name : 'edit',
            convert : getEdit
        }, {
            name : 'bbox',
            mapping : 'BoundingBox',
            defaultValue : ''
        }, {
            name : 'displayOrder',
            convert : getDisplayOrder,
            sortType : 'asInt'
        }, {
            name : 'valid',
            convert : getValidationInfo
        }, {
            name : 'valid_details',
            convert : getValidationDetails
        }, {
            name : 'idxMsg',
            convert : getIdxMsg
        } ]
    });
};
