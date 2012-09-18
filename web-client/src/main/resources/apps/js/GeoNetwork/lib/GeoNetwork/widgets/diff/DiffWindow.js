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
GeoNetwork.view.DiffWindow = Ext.extend(Ext.Window, {
    defaultConfig: {
        layout: 'fit',
        width: 1200,
        height: 740,
        border: false,
        /** api: config[lang]
         *  The language to use to call GeoNetwork services in the print mode (which is opened in a new window).
         */
        lang: 'en',
        autoScroll: true,
        /** api: config[closeAction]
         *  The close action. Default is 'destroy'.
         */
        closeAction: 'destroy',
        /** api: config[currTab]
         *  The default view mode to use. Default is 'simple'.
         */
        currTab: 'simple',
        /** api: config[relationTypes]
         *  List of types of relation to be displayed in header.
         *  Do not display feature catalogues (gmd:contentInfo) and sources (gmd:lineage) by default.
         *  Set to '' to display all.
         */
        relationTypes: 'service|children|related|parent|dataset|fcat',
        maximizable: false,
        maximized: false,
        collapsible: true,
        collapsed: false
    },
    serviceUrl: undefined,
    edit: false,
    catalogue: undefined,
    record: undefined,
    resultsView: undefined,
    actionMenu: undefined,
    title: OpenLayers.i18n('diffMetadata'),

    /** private: method[initComponent]
     *  Initializes the metadata view window.
     */
    initComponent: function(){
        Ext.applyIf(this, this.defaultConfig);

        GeoNetwork.view.DiffWindow.superclass.initComponent.call(this);
        this.setTitle(this.title);
        this.add(new GeoNetwork.view.DiffPanel({
            serviceUrl: this.serviceUrl,
            edit: this.edit,
            lang: this.lang,
            currTab: GeoNetwork.defaultViewMode || 'simple',
            catalogue: this.catalogue,
            resultsView: this.resultsView,
            border: false,
            frame: false,
            autoScroll: true
        }));

        this.on('beforeshow', function(el) {
            el.setSize(
                el.getWidth() > Ext.getBody().getWidth() ? Ext.getBody().getWidth() : el.getWidth(),
                el.getHeight() > Ext.getBody().getHeight() ? Ext.getBody().getHeight() : el.getHeight());
        });
    }
});

/** api: xtype = gn_view_diffwindow */
Ext.reg('gn_view_diffwindow', GeoNetwork.view.DiffWindow);
