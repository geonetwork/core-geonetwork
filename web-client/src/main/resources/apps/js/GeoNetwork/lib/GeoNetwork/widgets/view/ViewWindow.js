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
Ext.namespace('GeoNetwork.view');

/** api: (define)
 *  module = GeoNetwork.view
 *  class = ViewWindow
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor 
 *  .. class:: ViewWindow(config)
 *
 *     Create a GeoNetwork metadata view window composed of a GeoNetwork.view.Panel
 *     to display a metadata record.
 *
 */
GeoNetwork.view.ViewWindow = Ext.extend(Ext.Window, {
    defaultConfig: {
        layout: 'fit',
        width: 700,
        height: 740,
        border: false,
        /** api: config[lang] 
         *  The language to use to call GeoNetwork services in the print mode (which is opened in a new window).
         */
        lang: 'eng',
        /** api: config[closeAction] 
         *  The close action. Default is 'destroy'.
         */
        closeAction: 'destroy',
        /** api: config[currTab] 
         *  The default view mode to use. Default is 'simple'.
         */
        currTab: GeoNetwork.defaultViewMode || 'simple',
        /** api: config[displayTooltip] 
         *  Display tooltips or not. Default is true.
         */
        displayTooltip: true,
        /** api: config[printDefaultForTabs]
         *  Define if default mode should be used for HTML print output instead of tabs
         *  (eg. metadata tag in advanced view will be replaced by default view)
         */
        printDefaultForTabs: false,
        printMode: undefined,
        printUrl: 'print.html',
        /** api: config[relationTypes] 
         *  List of types of relation to be displayed in header. 
         *  Do not display feature catalogues (gmd:contentInfo) and sources (gmd:lineage) by default. 
         *  Set to '' to display all.
         */
        relationTypes: 'service|children|related|parent|dataset|fcat',
        maximizable: true,
        maximized: false,
        collapsible: true,
        showFeedBackButton: false,
        collapsed: false,
        /** api: config[permalink]
         *  Define if permalink button should be displayed or not. Default is true.
         */
        permalink: true
    },
    serviceUrl: undefined,
    catalogue: undefined,
    metadataUuid: undefined,
    record: undefined,
    resultsView: undefined,
    actionMenu: undefined,
    tipTpl: undefined,
    panel: undefined,
    metadataSchema: undefined,
    cache: {},
    tooltips: [],
    /** api: property[relatedTpl] 
     *  Template use for related metadata links.
     */
    relatedTpl: undefined,
    /** private: method[initComponent] 
     *  Initializes the metadata view window.
     */
    getPanel: function() {
        return this.panel;
    },
    initComponent: function(){
        Ext.applyIf(this, this.defaultConfig);
        
        this.tools = [{
            id: 'newwindow',
            qtip: OpenLayers.i18n('newWindow'),
            handler: function(e, toolEl, panel, tc){
                window.open(GeoNetwork.Util.getBaseUrl(location.href) + "?uuid=" + this.metadataUuid);
                this.hide();
            },
            scope: this
        }];
        
        GeoNetwork.view.ViewWindow.superclass.initComponent.call(this);
        this.setTitle(this.record ? this.record.get('title') : '');
        
        this.panel = new GeoNetwork.view.ViewPanel({
            serviceUrl: this.serviceUrl,
            lang: this.lang,
            currTab: GeoNetwork.defaultViewMode || 'simple',
            printDefaultForTabs: GeoNetwork.printDefaultForTabs || false,
            printUrl: this.printUrl,
            catalogue: this.catalogue,
            metadataUuid: this.metadataUuid,
            record: this.record,
            resultsView: this.resultsView,
            showFeedBackButton: this.showFeedBackButton,
            border: false,
            frame: false,
            autoScroll: true,
            permalink: this.permalink
        });
        this.add(this.panel);
        
        this.on('beforeshow', function(el) {
            el.setSize(
                el.getWidth() > Ext.getBody().getWidth() ? Ext.getBody().getWidth() : el.getWidth(),
                el.getHeight() > Ext.getBody().getHeight() ? Ext.getBody().getHeight() : el.getHeight()); 
        });
    }
});

/** api: xtype = gn_view_viewwindow */
Ext.reg('gn_view_viewwindow', GeoNetwork.view.ViewWindow);
