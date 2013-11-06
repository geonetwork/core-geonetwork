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
 *  class = Templates
 *  base_link = `Ext.XTemplate <http://extjs.com/deploy/dev/docs/?class=Ext.XTemplate>`_
 */
/** api: constructor
 *  .. class:: GeoNetwork.Geocatch.Templates()
 *
 *   A template for harvester (experimental)
 */
GeoNetwork.Geocatch = {};

GeoNetwork.Geocatch.Templates = Ext.extend(Ext.XTemplate, {
    compiled: false,
    disableFormats: false,
    catalogue : null,
    sortOrder: 0,
    abstractMaxSize : 50,
    xmlTplMarkup : [
        '<?xml version="1.0" encoding="UTF-8"?>',
        '<node id="{id}" type="{type}">',
        '<site>',
        '<name>{site_name}</name>',
        '<tpl if="values.site_ogctype">',
        '<ogctype>{site_ogctype}</ogctype>',
        '</tpl>',
        '<tpl if="values.site_url">',
        '<url>{site_url}</url>',
        '</tpl>',
        '<tpl if="values.site_icon">',
        '<icon>{site_icon}</icon>',
        '</tpl>',
        '<tpl if="values.site_account_use">',
        '<account>',
        '<use>{site_account_use}</use>',
        '<username>{site_account_username}</username>',
        '<password>{site_account_password}</password>',
        '</account>',
        '</tpl>',
        '</site>',
        '<options>',
        '<tpl if="values.options_every">',
        '<every>{options_every}</every>',
        '</tpl>',
        '<tpl if="values.options_onerunonly">',
        '<oneRunOnly>{options_onerunonly}</oneRunOnly>',
        '</tpl>',
        '<tpl if="values.options_lang">',
        '<lang>{options_lang}</lang>',
        '</tpl>',
        '<tpl if="values.options_topic">',
        '<topic>{options_topic}</topic>',
        '</tpl>',
        '<tpl if="values.options_createthumbnails">',
        '<createThumbnails>{options_createthumbnails}</createThumbnails>',
        '</tpl>',
        '<tpl if="values.options_uselayer">',
        '<useLayer>{options_uselayer}</useLayer>',
        '</tpl>',
        '<tpl if="values.options_uselayermd">',
        '<useLayerMd>{options_uselayermd}</useLayerMd>',
        '</tpl>',
        '<tpl if="values.options_datasetcategory">',
        '<datasetcategory>{options_datasetcategory}</datasetcategory>',
        '</tpl>',
        '</options>',
        '<content>',
        '</content>',
        '<privileges>',
        '</privileges>',
        '<group id="1">',
        '<operation name="view" />',
        '<operation name="dynamic" />',
        '</group>',
        '<categories>',
        '</categories>',
        '<tpl if="values.info_result_total">',
        '</tpl>',
        '</node>'
        // TODO : other properties - display if available depends on harvester
    ],
    initComponent: function() {
        GeoNetwork.Geocatch.Templates.superclass.initComponent.call(this);
    },
    /** api: method[getHarvesterTemplate]e
     *
     *  :return: A template for harvestr configuration
     */
    getHarvesterTemplate: function() {
        return new Ext.XTemplate(this.xmlTplMarkup);
    }
});

GeoNetwork.Geocatch.Templates.RELATIONS = 
    '<div class="relation" title="{[ + OpenLayers.i18n("relateddatasets")]}">\
        <span></span>\
        <ul id="md-relation-{id}"></ul>\
    </div>';

GeoNetwork.Geocatch.Templates.ABSTRACT = 
    '<p class="abstract"><i>{[OpenLayers.i18n("abstract")]}: </i>{[Ext.util.Format.ellipsis(values.abstract, 350, true)]}\
     <tpl if="revisionDate && revisionDate!=\'\'"><br/>(<i>{[OpenLayers.i18n("modified")]}: </i>{revisionDate})</tpl>\
	 <tpl if="this.isAdmin() && ownerid && ownerid!=\'\'"><br/>(<i>{[OpenLayers.i18n("owner")]}:</i><a href="user.edit?id={ownerid}" target="_useredit">{ownername}</a>)</tpl>\
     {values}</p>';

GeoNetwork.Geocatch.Templates.LINKS = 
    '<div class="md-links" id="md-links-{id}"></div>';




GeoNetwork.Geocatch.Templates.LOGO = 
	'<span class="md-logo" xmlns="http://www.w3.org/1999/html"> \
	<tpl if="groupWebsite!=\'\'"><a href="{groupWebsite}" target="_blank"/><img title="{[OpenLayers.i18n("Loading") + "..."]}"  class="loadCatalogName{catalogName}" src="{[catalogue.URL]}/images/logos/{groupLogoUuid}"/></a>  </tpl>\
	<tpl if="groupWebsite==\'\'"><img title="{[OpenLayers.i18n("Loading") + "..."]}" class="loadCatalogName{catalogName}" src="{[catalogue.URL]}/images/logos/{groupLogoUuid}"/></tpl>\
	</span>';

GeoNetwork.Geocatch.Templates.TITLE = 
    '<h1>\
     <input type="checkbox" \
	    <tpl if="selected==\'true\'">checked="true"</tpl>\
		    class="selector" \
		    onclick="javascript:catalogue.metadataSelect((this.checked?\'add\':\'remove\'), [\'{uuid}\']);"/>'+
	    GeoNetwork.Geocatch.Templates.LOGO+
        '<a class="metadatatitle" href="#" onclick="javascript:catalogue.metadataShow(\'{uuid}\');return false;">{title}\
            <span class="metadatatitletooltip">{fulltitle}</span> \
        </a>\
        <tpl if="isdataset==true"><img src="{[catalogue.URL]}/images/dataset.gif" title="dataset" alt="dataset"/></tpl>\
        <tpl if="isservice==true"><img src="{[catalogue.URL]}/images/service.gif" title="service" alt="service"/></tpl>\
        <tpl if="historicalArchive==true"><img src="{[catalogue.URL]}/apps/geocatch/images/archived.png" title="archive" alt="archive"/></tpl>\
		<tpl if="hasLinks==true"><img src="{[catalogue.URL]}/apps/images/default/link.png" title="link" alt="link"/></tpl>\
        <span class="md-action-menu"> - <a rel="mdMenu">&nbsp;</a></span>\
    </h1>';

GeoNetwork.Geocatch.Templates.FULL = new Ext.XTemplate(
        '<ul style="padding-left: 10px">',
          '<tpl for=".">',
                '<li class="md md-full" style="{featurecolorCSS}">',
                    '<table>\
                        <tr>',
                            '<td id="{uuid}">',
                                GeoNetwork.Geocatch.Templates.TITLE,
                                GeoNetwork.Geocatch.Templates.ABSTRACT,
                                GeoNetwork.Geocatch.Templates.LINKS,
                            '</td>\
                        </tr>\
                    </table>',
                    GeoNetwork.Templates.RELATIONS,
                '</li>',
            '</tpl>',
        '</ul>',
    {
       
        isAdmin: function() {
        	return catalogue.identifiedUser && catalogue.identifiedUser.role === 'Administrator';
        },
        hasDownloadLinks: function(values) {
            var i;
            for (i = 0; i < values.length; i ++) {
                if (values[i].type === 'application/x-compressed') {
                    return true;
                }
            }
            return false;
        }
    }
);
