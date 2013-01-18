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
 *  .. class:: GeoNetwork.Ngr.Templates()
 *
 *   A template for harvester (experimental)
 */
GeoNetwork.Ngr = {};

GeoNetwork.Ngr.Templates = Ext.extend(Ext.XTemplate, {
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
        GeoNetwork.Ngr.Templates.superclass.initComponent.call(this);
    },
    /** api: method[getHarvesterTemplate]
     *
     *  :return: A template for harvester configuration
     */
    getHarvesterTemplate: function() {
        return new Ext.XTemplate(this.xmlTplMarkup);
    }
});

/**
 * Common templates
 */

/**
 * Shows Detailed Metadata on a new tab
 */
GeoNetwork.Ngr.Templates.DETAILED_METADATA =
    '<li class="{type}">',
    '<button onclick="javascript:catalogue.metadataShow(\'{uuid}\');return false;">{title}</button>',
    '</li>';

GeoNetwork.Ngr.Templates.RATING = 
    '<div class="rating-wd" id="rating-{id}" title="{[OpenLayers.i18n("RatingText")]}">',
    '<input type="radio" name="rating{values.uuid}" <tpl if="rating==\'1\'">checked="true"</tpl> value="1"/>' +
    '<input type="radio" name="rating{values.uuid}" <tpl if="rating==\'2\'">checked="true"</tpl> value="2"/>' +
    '<input type="radio" name="rating{values.uuid}" <tpl if="rating==\'3\'">checked="true"</tpl> value="3"/>' +
    '<input type="radio" name="rating{values.uuid}" <tpl if="rating==\'4\'">checked="true"</tpl> value="4"/>' +
    '<input type="radio" name="rating{values.uuid}" <tpl if="rating==\'5\'">checked="true"</tpl> value="5"/>' +
    '</div>';

/**
 * Title of the Metadata
 */
GeoNetwork.Ngr.Templates.SHORT_TITLE =
    '<h1 style="height:60px" >\
    <a href="javascript:void(0);" onclick="javascript:catalogue.metadataShow(\'{uuid}\');return false;">\
    {[Ext.util.Format.ellipsis(values.title, 30, true)]}</a>\
    </h1>';
GeoNetwork.Ngr.Templates.TITLE =
    '<h1>\
       <a href="javascript:void(0);" onclick="javascript:catalogue.metadataShow(\'{uuid}\');return false;">{title}</a>\
    </h1>';


/**
 * Rating (stars) for metadata
 */
GeoNetwork.Ngr.Templates.RATING_TPL = '<div class="rating">' +
    '<input type="radio" name="rating{values.uuid}" <tpl if="rating==\'1\'">checked="true"</tpl> value="1"/>' +
    '<input type="radio" name="rating{values.uuid}" <tpl if="rating==\'2\'">checked="true"</tpl> value="2"/>' +
    '<input type="radio" name="rating{values.uuid}" <tpl if="rating==\'3\'">checked="true"</tpl> value="3"/>' +
    '<input type="radio" name="rating{values.uuid}" <tpl if="rating==\'4\'">checked="true"</tpl> value="4"/>' +
    '<input type="radio" name="rating{values.uuid}" <tpl if="rating==\'5\'">checked="true"</tpl> value="5"/>' +
    '</div>';

/**
 * Source logo
 */
GeoNetwork.Ngr.Templates.LOGO =
    '<div class="md-logo">\
        <img src="{[catalogue.URL]}/images/harvesting/{groupName}.png"/>\
    </div>';


/**
 * Button to put the metadata on the map
 */
GeoNetwork.Ngr.Templates.SHOW_ON_MAP =
    '<tpl for="links">\
        <tpl if="values.type == \'application/vnd.ogc.wms_xml\'">\
            <button class="addLayer" title="{title}" alt="{title}" onclick="app.getIMap().addWMSLayer([[\'{title}\', \'{href}\', \'{name}\', \'{uuid}\']]);">\
                <img src="../../apps/images/default/map/add_layer.png"/>\
            </button>\
        </tpl>\
    </tpl>';

GeoNetwork.Ngr.Templates.LINKCONTAINER = 
    '<div class="md-links" id="md-links-{id}">\
    </div>';
/**
 * Button to download metadata
 */
GeoNetwork.Ngr.Templates.DOWNLOAD =
    '<div>\
  <button class="bookmark-icon" title="{[OpenLayers.i18n("saveXml") ]}"\
   onclick="catalogue.metadataXMLShow(\'{uuid}\', \'{schema}\');">\
          <img src="{[catalogue.URL]}/apps/ngr2/images/default/page_code.png"/>\
      </button>\
      <button class="bookmark-icon" title="{[OpenLayers.i18n("printSel")]}"\
     onclick="catalogue.metadataPrint(\'{uuid}\');">\
          <img src="{[catalogue.URL]}/apps/ngr2/images/default/page_white_acrobat.png"/>\
      </button>\
      <button class="bookmark-icon" title="{[OpenLayers.i18n(""getMEF")]}"\
     onclick="catalogue.metadataMEF(\'{uuid}\');">\
                <img src="{[catalogue.URL]}/apps/ngr2/images/default/page_white_zip.png"/>\
            </button>\
            <div class="md-links" id="md-links-{id}"/>\
        </div>';


/**
 * Shows if the metadata is valid
 * Validity and category information
 */
GeoNetwork.Ngr.Templates.VALID =
    '<tpl if="valid != \'-1\'">\
    <div class="validStatus">\
    <tpl if="valid == \'1\'"><img src="../../apps/ngr2/img/valid_metadata.png"</tpl>\
    <tpl if="valid == \'0\'"><img src="../../apps/ngr2/img/invalid_metadata.png"</tpl>\
    " title="{[OpenLayers.i18n("validityInfo")]}\
    <tpl for="valid_details">\
   {values.type}: \
     <tpl if="values.valid == \'1\'">{[OpenLayers.i18n("valid")]}</tpl>\
                <tpl if="values.valid == \'0\'">{[OpenLayers.i18n("notValid")]}</tpl>\
                <tpl if="values.valid == \'-1\'">{[OpenLayers.i18n("notDetermined")]}</tpl>\
                <tpl if="values.ratio != \'\'"> ({values.ratio}) </tpl> - \
            </tpl>\
        "></img></div>\
    </tpl>';

/**
 * Shows if the WMS is valid
 * 
 * WMS validation is made by MetadataResultsView.dislayLinks
 */
GeoNetwork.Ngr.Templates.WMS_VALID = "";

/**
 * Shows if the WFS is valid. WFS validation is made by MetadataResultsView.dislayLinks
 */
GeoNetwork.Ngr.Templates.WFS_VALID = "";


/**
 * Shows if there are other licenses (and which ones)
 */
GeoNetwork.Ngr.Templates.OTHER_LICENSES =
    '<tpl if="license != \'\' && license != \'otherConstraints\'">\
        <div class="otherLicenses">\
          <tpl if="licenseLink != \'\'"><a target="_blank" href="{[values.licenseLink]}"></tpl>\
            <tpl if="license == \'OtherConstraints\'"><img title="{[OpenLayers.i18n("Other Restrictions")]}" src="../../apps/ngr2/img/icon_access-restrictions.png" /></tpl>\
            <tpl if="license == \'CC0\'"><img title="{[OpenLayers.i18n("Creative Commons Zero")]}" src="../../apps/ngr2/img/icon_creative_commons.png" /></tpl>\
            <tpl if="license == \'Public Domain\'"><img title="{[OpenLayers.i18n("Public Domain")]}" src="../../apps/ngr2/img/icon_public_domein.png" /></tpl>\
            <tpl if="license == \'Geo Gedeeld licentie\'"><img src="../../apps/ngr2/img/icon_gebruiksvoorwaarden.png" /></tpl>\
          <tpl if="licenseLink != \'\'"></a></tpl>\
        </div>\
    </tpl>';
//<img src="../../apps/ngr2/img/icon_gebruiksvoorwaarden.png" /><span>{license}</span>\

/**
 * Shows Add to favourites
 */
GeoNetwork.Ngr.Templates.BOOKMARK =
    '<button class="bookmark-icon" value="{title}" \
    onclick="javascript:catalogue.metadataAddToBookmarks(\'{title}\', \'{uuid}\');">\
        <img title="{[OpenLayers.i18n("Add Bookmark")]}" src="../../apps/ngr2/img/bookmark-add-icon.png"/>\
    </button>';

/**
 * Display copy to clipboard icon if you are using IE.
 * @type {String}
 */
GeoNetwork.Ngr.Templates.COPYTOCLIPBOARD =
    '<tpl if="Ext.isIE"> \
     <button class="bookmark-icon" value="{title}" title="{[OpenLayers.i18n("Copy to Clipboard")]}" \
        onclick="copyToClipboard(metadataViewURL(\'{uuid}\'));">\
        <img src="../../apps/ngr2/img/clipboard.png"/>\
    </button> \
    </tpl>';

/**
 * Thumbnails
 */
GeoNetwork.Ngr.Templates.THUMB =
    '<div style="height:150px" class="thumbnail">\
        <tpl if="thumbnail">\
            <a href="javascript:catalogue.metadataShow(\'{uuid}\');return false;">\
                <img src="{thumbnail}" alt="Thumbnail"/>\
            </a>\
        </tpl>\
    </div>';

GeoNetwork.Ngr.Templates.CHANGE_DATE = 
    '<tpl if="edit==\'false\' || isharvested==\'y\'">\
        <div> {[OpenLayers.i18n("lastUpdate")]} {[values.changedate.split(\'T\')[0]]}</div>\
    </tpl>';

/**
 * Shows contact info.
 */
GeoNetwork.Ngr.Templates.CONTACT_INFO =
    '<div class="md-contact">\
<tpl for="contact">\
 <tpl if="applies==\'resource\'">\
     <div title="{role} - {applies}">\
         <tpl if="values.logo !== undefined && values.logo !== \'\'">\
             <img src="{logo}" class="orgLogo"/>\
         </tpl>{name}\
     </div>\
 </tpl>\
</tpl>\
<tpl if="edit==\'true\' && isharvested!=\'y\'">\
 <div class="md-mn md-mn-user" title="{[OpenLayers.i18n("ownerName")]}">{ownername} -  {[OpenLayers.i18n("lastUpdate")]}{[values.changedate.split(\'T\')[0]]}</div>\
        </tpl>' +
    GeoNetwork.Ngr.Templates.CHANGE_DATE + 
    '</div>';

GeoNetwork.Ngr.Templates.CONTACT_INFO_TOOLTIP =
  '<tpl for="contact"><tpl if="applies==\'resource\'"> - {name} ({role}) \n</tpl>\</tpl>';	// In one line to avoid extra space in tooltip

/**
 * Description
 */
GeoNetwork.Ngr.Templates.SUBJECT =
    '<tpl if="subject">\
        <span class="subject">\
        <tpl for="subject">\
            {value}{[xindex==xcount?"":", "]}\
        </tpl>\
        </span>\
    </tpl>';

/**
 * Description
 */
GeoNetwork.Ngr.Templates.CATEGORIES =
    '<td class="icon" title="{[OpenLayers.i18n("metadataCategories")]}">',
    '<tpl for="category">',
    '<div class="md-mn cat-{value}" title="{value}">&nbsp;</div>',
    '</tpl>',
    '</td>';

/**
 * Related metadata
 */
GeoNetwork.Ngr.Templates.RELATED_DATASETS =
    '<div class="relation" title="{[OpenLayers.i18n("relateddatasets")]}">\
    <span></span>\
    <ul id="md-relation-{id}"></ul>\
</div>';

/** api: constructor
 *  .. class:: GeoNetwork.Ngr.Templates.SIMPLE()
 *
 *   An instance of a pre-configured GeoNetwork.Ngr.Templates with title and
 *   keywords with abstract in tooltip.
 */
GeoNetwork.Ngr.Templates.SIMPLE = new Ext.XTemplate(
    '<ul>',
    '<tpl for=".">',
    '<li class="md md-simple" title="{abstract}" style="{featurecolorCSS}">',
    '<table>\
<tr>',
   '<td style="width:40px;">',
    GeoNetwork.Ngr.Templates.LOGO,
    '</td>',
   '<td id="{uuid}">',
    GeoNetwork.Ngr.Templates.TITLE,
    '</td>\
        </tr>\
    </table>',
    '<table>\
<tr>\
    <td>',
    GeoNetwork.Ngr.Templates.SUBJECT,
    '</td>\
    <td>',
    GeoNetwork.Ngr.Templates.CONTACT_INFO,
    '</td>\
        </tr>\
    </table>',
    '<table>\
<tr>\
    <td>',
//    Add to map and download link are handled by the JS
//    based on the store content to create list menu instead
//    of a long list of buttons.
//    GeoNetwork.Ngr.Templates.SHOW_ON_MAP,
//    '</td>\
//    <td>',
//    GeoNetwork.Ngr.Templates.DOWNLOAD,
//    '</td>\
//     <td>',
    GeoNetwork.Ngr.Templates.BOOKMARK,
    GeoNetwork.Ngr.Templates.LINKCONTAINER,
    '</td>\
     <td>',
    GeoNetwork.Ngr.Templates.COPYTOCLIPBOARD,
    '</td>\
     </tr>\
    </table>',
    '</li>',
    '</tpl>',
    '</ul>'
);



/** api: constructor
 *  .. class:: GeoNetwork.Ngr.Templates.THUMBNAIL()
 *
 *   An instance of a pre-configured GeoNetwork.Ngr.Templates with thumbnail view
 */
GeoNetwork.Ngr.Templates.THUMBNAIL = new Ext.XTemplate(
    '<ul>',
    '<tpl for=".">',
    '<li class="md md-thumbnail" style="{featurecolorCSS}">',
    '<div class="md-wrap" id="{uuid}" title="{abstract}\n',
    GeoNetwork.Ngr.Templates.CONTACT_INFO_TOOLTIP,
    '">',
    GeoNetwork.Ngr.Templates.SHORT_TITLE,
    GeoNetwork.Ngr.Templates.THUMB,
    GeoNetwork.Ngr.Templates.LINKCONTAINER,
      '<div class="md-contact">',
      GeoNetwork.Ngr.Templates.CHANGE_DATE,
      '</div>',
    '</div>',
    '</li>',
    '</tpl>',
    '</ul>'
);


/** api: constructor
 *  .. class:: GeoNetwork.Ngr.Templates.FULL()
 *
 *   An instance of a pre-configured GeoNetwork.Ngr.Templates with full view
 */
GeoNetwork.Ngr.Templates.FULL = new Ext.XTemplate(
    '<ul>',
    '<tpl for=".">',
    '<li class="md md-full" style="{featurecolorCSS}">',
    '<table>\
        <tr>',
            '<td class="left">',
            GeoNetwork.Ngr.Templates.LOGO,
            '</td>',
            '<td id="{uuid}" style="width:80%;">',
                GeoNetwork.Ngr.Templates.TITLE,
                '<p class="abstract">\
                <tpl if="values.abstract.length &gt;350">\
                {[values.abstract.substring(0, 350)]}...\
                </tpl>\
                <tpl if="values.abstract.length &lt;= 350">\
                {values.abstract}\
                </tpl>\
                </p>',    // FIXME : 250 as parameters
                GeoNetwork.Ngr.Templates.SUBJECT,
                GeoNetwork.Ngr.Templates.LICENSE,
            '</td>\
            <td class="thumb">',
                GeoNetwork.Ngr.Templates.RATING_TPL,
                GeoNetwork.Ngr.Templates.THUMB,
                GeoNetwork.Ngr.Templates.CONTACT_INFO,
            '</td>',
            GeoNetwork.Ngr.Templates.CATEGORIES,
        '</tr>',
    '</table>',
    '<table><tr>',
            '<td>',
            GeoNetwork.Ngr.Templates.BOOKMARK,
            GeoNetwork.Ngr.Templates.COPYTOCLIPBOARD,
            '</td>',
            '<td class="icon" colspan="2">',

            GeoNetwork.Ngr.Templates.OTHER_LICENSES,
            GeoNetwork.Ngr.Templates.WMS_VALID,
            GeoNetwork.Ngr.Templates.WFS_VALID,
            GeoNetwork.Ngr.Templates.LINKCONTAINER,
            GeoNetwork.Ngr.Templates.VALID,
            GeoNetwork.Ngr.Templates.RATING,

        //    GeoNetwork.Ngr.Templates.SHOW_ON_MAP,
        //    GeoNetwork.Ngr.Templates.DOWNLOAD,
            '</td>',
        '</tr>',
        '<tr>',
            '<td>',
            GeoNetwork.Ngr.Templates.RELATED_DATASETS,
            GeoNetwork.Ngr.Templates.DETAILED_METADATA,
            '</td>',
        '</tr>\
    </table>',
    '</li>',
    '</tpl>',
    '</ul>',
    {
        hasDownloadLinks: function(values) {
            var i;
            for (i = 0; i < values.length; i ++) {
                if (values[i].type === 'application/x-compressed') {
                    return true;
                }
            }
            return false;
        },
        hasWMSLinks: function(values) {
            var i;
            for (i = 0; i < values.length; i ++) {
                console.log(values[i]);
                if (values[i].type === 'application/vnd.ogc.wms_xml') {
                    return true;
                }
            }
            return false;
        },
        hasWFSLinks: function(values) {
            var i;
            for (i = 0; i < values.length; i ++) {
                if (values[i].type === 'application/vnd.ogc.wfs_xml' || values[i].type === 'OGC:WFS') {
                    return true;
                }
            }
            return false;
        }
    }
);

GeoNetwork.Ngr.Templates.Relation = {
    SHORT: ['<li class="{type}">',
        '<button onclick="javascript:catalogue.metadataShow(\'{uuid}\');return false;" title="{abstract}">{title}</button>',
        '</li>']
};
