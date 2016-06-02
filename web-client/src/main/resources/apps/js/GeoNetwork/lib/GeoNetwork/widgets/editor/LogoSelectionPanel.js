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
Ext.namespace('GeoNetwork.editor');


/** api: (define)
 *  module = GeoNetwork.editor
 *  class = LogoSelectionPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor 
 *  .. class:: LogoSelectionPanel()
 *
 *     Create a GeoNetwork logo selection panel.
 *     Logos could be added to metadata contact for ISO19139 records.
 *
 *     More information available `here <http://trac.osgeo.org/geonetwork/wiki/proposals/OrganisationLogo>`_
 */
GeoNetwork.editor.LogoSelectionPanel = Ext.extend(Ext.Panel, {
    defaultConfig: {
        autoScroll: true,
        autoWidth: true,
        layout: 'border'
    },
    store: undefined,
    view: undefined,
    uploadForm: undefined,
    serviceUrl: undefined,
    logoAddUrl: undefined,
    /**
     * Property: ref
     */
    ref: undefined,
    /**
     * APIMethod: setRef
     * Set the element reference
     */
    setRef: function(ref){
        this.ref = ref;
    },
    getUploadForm: function(){
        this.uploadForm = new Ext.form.FormPanel({
            fileUpload: true,
            region: 'south',
            height: 80,
            split: true,
            items: {
                xtype: 'fileuploadfield',
                allowBlank: false,
                emptyText: OpenLayers.i18n('logoSelect'),
                hideLabel: true,
                name: 'fname',
                buttonCfg: {
                    iconCls: 'thumbnailAddIcon'
                }
            },
            buttons: [{
                text: OpenLayers.i18n('upload'),
                scope: this,
                iconCls: 'thumbnailGoIcon',
                handler: function(){
                    if (this.uploadForm.getForm().isValid()) {
                        this.uploadForm.getForm().submit({
                            url: this.logoAddUrl,
                            scope: this,
                            success: function(fp, action){
                                this.store.reload();
                            },
                            failure: function(response){
                                Ext.Msg.alert(OpenLayers.i18n('failure'), response.responseText);
                            }
                        });
                    }
                }
            }]
        });
        return this.uploadForm;
    },
    /**
     * APIProperty:
     * {Object} Hash table of selected contacts with their XML raw data
     */
    initComponent: function(){
        Ext.applyIf(this, this.defaultConfig);
        
        // TODO : move to lib. Could be use elsewhere
        var tpl = new Ext.XTemplate('<tpl for="."><div class="logo-wrap"><div id="{name}" class="logo">', 
                                        '<img src="' + this.logoUrl + '{name}" title="{name}"/><span>{name}</span></div></div>', 
                                    '</tpl>');
        
        var logo = Ext.data.Record.create([{
            name: 'name',
            mapping: ''
        }]);
        
        this.store = new Ext.data.Store({
            autoDestroy: true,
            proxy: new Ext.data.HttpProxy({
                method: 'GET',
                url: this.serviceUrl,
                disableCaching: false
            }),
            reader: new Ext.data.XmlReader({
                record: 'icon',
                id: 'icon'
            }, logo),
            fields: ['name']
        });
        
        
        this.view = new Ext.DataView({
            store: this.store,
            tpl: tpl,
            singleSelect: true,
            selectedClass: 'logo-selected',
            overClass: 'logo-over',
            itemSelector: 'div.logo-wrap',
            autoScroll: true,
            listeners: {
                selectionchange: function(dv, selections){
                    var idx = this.view.getSelectedIndexes();
                    if (selections) {
                        this.fireEvent('logoselected', this, idx);
                        this.ownerCt.hide();
                    }
                },
                scope: this
            }
        });
        
        this.items = [
            new Ext.Panel({
                region: 'center',
                split: true,
                border: true,
                autoScroll: true,
                items: [
                    this.view]
                }), 
            this.getUploadForm()
        ];
        
        GeoNetwork.editor.LogoSelectionPanel.superclass.initComponent.call(this);
        
        /**
         * triggered when the user has selected a keyword
         */
        this.addEvents('logoselected');
        
        this.store.load();
    }
});

/** api: xtype = gn_editor_logoselectionpanel */
Ext.reg('gn_editor_logoselectionpanel', GeoNetwork.editor.LogoSelectionPanel);
