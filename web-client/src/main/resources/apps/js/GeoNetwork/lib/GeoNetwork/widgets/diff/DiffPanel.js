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
 *  class = DiffPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor
 *  .. class:: DiffPanel(config)
 *
 *     Create a GeoNetwork metadata diff window between 2 metadata records or a metadata and related workspace copy.
 *
 *     A toolbar is provided with:
 *
 *      * switch option to interchange origin and target metadata (only if ocmparing 2 different metadata records)
 *
 */
GeoNetwork.view.DiffPanel = Ext.extend(Ext.Panel, {
    defaultConfig: {
        border: false,
        /** api: config[lang]
         *  The language to use to call GeoNetwork services in the print mode (which is opened in a new window).
         */
        lang: 'en',
        autoScroll: true,

        /** api: config[currTab]
         *  The default view mode to use. Default is 'simple'.
         */
        currTab: 'simple',

        /** api: config[relationTypes]
         *  List of types of relation to be displayed in header.
         *  Do not display feature catalogues (gmd:contentInfo) and sources (gmd:lineage) by default.
         *  Set to '' to display all.
         */
        relationTypes: 'service|children|related|parent|dataset'
    },
    switchButton: undefined,
    serviceUrl: undefined,
    catalogue: undefined,
    metadataIdOrigin: undefined,
    metadataIdTarget: undefined,
    record: undefined,
    resultsView: undefined,

    afterDiffLoad: function(){
        var source_md = Ext.query('.source-md');
        var target_md = Ext.query('.target-md');

        if (source_md.length == 1) {
            this.metadataIdOrigin = source_md[0].id.replace('source-','');
        }

        if (target_md.length == 1) {
            this.metadataIdTarget = target_md[0].id.replace('target-','');
        }

        if (this.metadataIdOrigin != this.metadataIdTarget)   {
            this.switchButton.setVisible(true);
        }

        // Create map panel for extent visualization
        this.catalogue.extentMap.initMapDiv();

        //set height of container to parent height
        //Ext.get('source-container').setHeight(this.parent.height);

    },
    getPanelTbar: function(){
        if (this.edit) return [this.createSwitchMenu(),'->',{text:'save',handler:function(){
            Ext.Ajax.request({
                method:"POST",
                form:Ext.get('diffEditForm'),
                success:function(){alert('Metadata Saved')},
                failure:function(){alert('Metadata save failed')
            }});

        }}];
        else return [this.createSwitchMenu()];
    },
    closeWin: function(){
        console.log(this);
    },
    createSwitchMenu: function(){
        this.switchButton =  new Ext.Button({
            iconCls: 'switchView',
            tooltip: OpenLayers.i18n('switchViewTT'),
            text: "&nbsp;" + OpenLayers.i18n('switchViewTT'),
            listeners: {
                click: function(c, pressed){
                    var mgr = this.body.getUpdater();
                    mgr.update({
                        url: this.serviceUrl + '?first=' + escape(this.metadataIdTarget) + '&second=' + escape(this.metadataIdOrigin),
                        callback: this.afterDiffLoad,
                        scope: this
                    });
                },
                scope: this
            }
        });

        this.switchButton.setVisible(false);

        return this.switchButton;
    },
    /** private: method[initComponent]
     *  Initializes the metadata view window.
     */
    initComponent: function(){
        Ext.applyIf(this, this.defaultConfig);

        GeoNetwork.view.DiffPanel.superclass.initComponent.call(this);

        this.add(new Ext.Panel({
            autoLoad: {
                url: this.serviceUrl,
                scripts:true,
                callback: this.afterDiffLoad,
                scope: this
            },
            border: false,
            frame: false,
            autoScroll: true,
            tbar: this.getPanelTbar()
        }));

    }
});

/** api: xtype = gn_view_viewpanel */
Ext.reg('gn_view_viewpanel', GeoNetwork.view.ViewPanel);
