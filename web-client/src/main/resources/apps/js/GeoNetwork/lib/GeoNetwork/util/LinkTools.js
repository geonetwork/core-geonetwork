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
Ext.namespace("GeoNetwork.util");

/** api: (define)
 *  module = GeoNetwork.util
 *  class = LinkTools
 */
/** api: example
 *  LinkTools creates a set of button menus representing the links that a 
 *  metadata record contains.
 *
 *
 *  .. code-block:: javascript
 *
 *   GeoNetwork.util.LinkTools.addLinks(catalogue, record, el, protocolToCSS);
 *  
 *  ...
 *
 */
GeoNetwork.util.LinkTools = {
		 	catalogue: undefined,
		 	protocolToCSS: GeoNetwork.Util.protocolToCSS, // override with app defined 
     /** api: method[addLinks]
      *  Display a menu with links for a metadata record.
			*
			* :param catalogue: GeoNetwork catalogue object.
			* :param record: GeoNetwork metadata record object.
			* :param el: DOM element to push button menus into.
			* :param protocolToCSS: function that maps protocol/mime types to CSS. If not specified then we use GeoNetwork.Util.ProtocolToCSS.
      */
     addLinks: function (catalogue, record, el, protocolToCSS) {
             var r = record,
								 links = r.get('links'),
                 id = r.get('id'),
                 uuid = r.get('uuid'),
								 view = this, 
								 nid = 0;

						 if (!el) { // Why?
						 	//console.log('Cannot do el for '+r.get('title'));
						 	return;
						}

						 if (protocolToCSS) view.protocolToCSS = protocolToCSS;
						 view.catalogue = catalogue;

             // Add permanent link (can copy for bookmark)
             var menu = new Ext.menu.Menu(),
                 bHref = view.catalogue.services.rootUrl + 'search?&uuid=' + escape(uuid);
 
             var permalinkMenu = new Ext.menu.TextItem({text: '<input value="' + bHref + '"/></br><a href="' + bHref + '">Link</a>'});
             menu.add('<b class="menu-title">' 
                       + OpenLayers.i18n('permalinkInfo') + '</b>',
                       permalinkMenu);
             view.addLinkMenu(el.id+nid, [{
               text: 'Bookmark Link',
               href: bHref,
               menu: menu
             }], OpenLayers.i18n('Bookmark Link'), 'bookmark', el);
 
            	 
             if (links.length > 0) {
                 
                 // The template may not defined a md-links placeholder
                 if (el) {
                     var store = new Ext.data.ArrayStore({
                         autoDestroy: true,
                         idIndex: 0,  
                         fields: [
                             {name: 'href', mapping: 'href'}, 
                             {name: 'name', mapping: 'name'}, 
                             {name: 'protocol', mapping: 'protocol'}, 
                             {name: 'title', mapping: 'title'}, 
                             {name: 'type', mapping: 'type'}
                         ],
                         data: links
                     });
                     store.sort('type');
                     
                     
                     var linkButton = [], label = null, currentType = null, bt,
                          allowDynamic = r.get('dynamic'), allowDownload = r.get('download'),
                          hasDownloadAction = 0;
                 
                     store.each(function (record) {
                     		 nid++;
                         // Avoid empty URL
                         if (record.get('href') !== '') {
                             // Check that current record type is the same as the previous record
                             // In such case, add the previous button if exist
                             // or create a new button to be added later
                             if (currentType === null || currentType !== record.get('type')) {
                                 if (linkButton.length !== 0) {
                                     view.addLinkMenu(el.id+nid, linkButton, label, currentType, el);
                                 }
                                 linkButton = [];
                                 currentType = record.get('type');
                                 var labelKey = 'linklabel-' + currentType;
                                 label = OpenLayers.i18n(labelKey);
                                 if (label === labelKey) { // Default label if not found in translation
                                     label = OpenLayers.i18n('linklabel-');
                                 }
                             }
                             
                             var text = null, handler = null;
                             
                             // Only display WMS link if dynamic property set to true for current user & record
                             if (currentType === 'application/vnd.ogc.wms_xml' || (currentType.indexOf('OGC:WMS') > -1)) {
                                 if (allowDynamic) {
                                     linkButton.push({
                                         text: record.get('title') || record.get('name'),
                                         handler: function (b, e) {
                                             // FIXME : ref to app
                                             app.switchMode('1', true);
                                             app.getIMap().addWMSLayer([[record.get('title'), record.get('href'), record.get('name'), uuid]]);
                                         },
                                         href: record.get('href')
                                     });
                                 }
                             } else if (currentType === 'application/vnd.ogc.wmc') {
                                 linkButton.push({
                                     text: record.get('title') || record.get('name'),
                                     handler: function (b, e) {
                                         // FIXME : ref to app
                                         app.switchMode('1', true);
                                         app.getIMap().addWMC(record.get('href'));
                                     },
                                     href: record.get('href')
                                 });
                             } else {
                                 // If link is uploaded to GeoNetwork the resources.get service or file.disclaimer service is used
                                 // Check if allowDownload 
                                 var displayLink = true;
                                 if ((record.get('href').indexOf('resources.get') !== -1) || (record.get('href').indexOf('file.disclaimer') !== -1)) {
                                     displayLink = allowDownload;
                                     if (displayLink) hasDownloadAction++;
                                 } else if (currentType === 'application/vnd.google-earth.kml+xml') {
                                     // Google earth link is provided when a WMS is provided
                                     displayLink = allowDynamic;
                                 }
                                 if (displayLink) {
                                     linkButton.push({
                                         text: (record.get('title') || record.get('name')),
                                         href: record.get('href')
                                     });
                                 }
                             }
                             
                         }
                         
                     });
                     // Add the last button
                     nid++;
                     if (linkButton !== null && linkButton.length !== 0) {
                         view.addLinkMenu(el.id+nid, linkButton, label, currentType, el);
                     }
                     
                     // Add the download selector/all button if more than one
                     // download link on this record
                     if (hasDownloadAction > 1) {
                         nid++;
                         view.addLinkMenu(el.id+nid, [{
                             text: 'download',
                             handler: function () {
                                 // FIXME : this call require the catalogue to be named catalogue
                                 view.catalogue.metadataPrepareDownload(id);
                             }
                         }], OpenLayers.i18n('prepareDownload'), 'downloadAllIcon', el);
                     }
                 }
             }
     },

     /** private: method[addLinkMenu]
      *  Display a menu with links for a metadata record for a protocol.
      *  If there is only one element in the linkButton array, display a menu
      *  and display a dropdown menu if not.
      */
     addLinkMenu: function (id, linkButton, label, currentType, el) {

         if (Ext.get(id)) { // don't need to add them again
           return;
         }
 
 
         var href = linkButton[0].href,
             isDownload = (currentType === 'downloadAllIcon') || (href.indexOf('resources.get') !== -1) || (href.indexOf('file.disclaimer') !== -1);
 
         if (linkButton.length === 1) {
             var handler = linkButton[0].handler || function () {
                 window.open(linkButton[0].href);
             };
 						var tTip = label;
             if (href) tTip += ' ' + href;
             if (linkButton[0].menu) {
               bt = new Ext.Button({
                 id: id,
                 tooltip: tTip,
                 menu: linkButton[0].menu,
                 iconCls: this.protocolToCSS(currentType, isDownload),
                 renderTo: el
               });
             } else {
               bt = new Ext.Button({
                 id: id,
                 tooltip: tTip,
                 handler: handler,
                 iconCls: this.protocolToCSS(currentType, isDownload),
                 renderTo: el
               });
             }
         } else {
 						if (linkButton[0].handler) { // if handlers then create button list
 							var items = [];
             	Ext.each(linkButton, function (button) {
               	items.push(new Ext.Button({
                 	handler: button.handler,
                 	text: button.text
               	}));
 					  	});
             	bt = new Ext.Button({
                 	id: id,
                 	tooltip: label,
                 	menu: new Ext.menu.Menu({cls: 'links-mn', items: items}),
                 	iconCls: this.protocolToCSS(currentType, isDownload),
                 	renderTo: el
             	});
 						} else {
             	bt = new Ext.Button({
                 	id: id,
                 	tooltip: label,
                 	menu: new Ext.menu.Menu({cls: 'links-mn', items: linkButton}),
                 	iconCls: this.protocolToCSS(currentType, isDownload),
                 	renderTo: el
             	});
 						}
         }
     }
};
