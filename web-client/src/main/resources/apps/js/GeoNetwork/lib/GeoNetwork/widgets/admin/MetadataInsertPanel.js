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
Ext.namespace('GeoNetwork.admin');

/** api: (define)
 *  module = GeoNetwork.editor
 *  class = MetadataInsertPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor 
 *  .. class:: MetadataInsertPanel()
 *
 *   Simple metadata insert panel used for subtemplates.
 *  
 *   TODO : improve to cover all metadata insert options in the existing admin panel.
 *
 */
GeoNetwork.admin.MetadataInsertPanel = Ext.extend(Ext.Panel, {
    defaultConfig: {
        layout: 'border',
        isTemplate: 'n'
    },
    uploadForm: undefined,
    getForm: function(){
        
        var groupStore = GeoNetwork.data.GroupStore(catalogue.services.getGroups);
        groupStore.load();
        
        this.uploadForm = new Ext.form.FormPanel({
            //fileUpload: true,
            region: 'center',
            split: true,
            border: false,
            frame: false,
            errorReader: new Ext.data.XmlReader({
                    record : 'response'
                }, ['id']
            ),
            items: [{
                xtype: 'textfield',
                hidden: true,
                name: 'insert_mode',
                value: '0'
            }, {
                xtype: 'textfield',
                hidden: true,
                name: 'template',
                value: this.isTemplate
            }, {
                xtype: 'textarea',
                name: 'data',
                fieldLabel: OpenLayers.i18n('xmlData'),
                allowBlank: false,
                anchor: '98% 90%'
            }, new Ext.form.ComboBox({
                name: 'group',
                hiddenName: 'group',
                mode: 'local',
                emptyText: OpenLayers.i18n('chooseGroup'),
                triggerAction: 'all',
                fieldLabel: OpenLayers.i18n('group'),
                store: groupStore,
                allowBlank: false,
                valueField: 'id',
                displayField: 'name',
                tpl: '<tpl for="."><div class="x-combo-list-item">{[values.label.' + GeoNetwork.Util.getCatalogueLang(OpenLayers.Lang.getCode()) + ']}</div></tpl>'
            })],
            buttons: [{
                text: OpenLayers.i18n('add'),
                scope: this,
                iconCls: 'thumbnailGoIcon',
                handler: function(){
                    if (this.uploadForm.getForm().isValid()) {
                        this.uploadForm.getForm().submit({
                            url: catalogue.services.mdXMLInsert,    // FIXME : global catalogue var
                            scope: this,
                            success: function(fp, action){
                                if (this.ownerCt) {
                                    this.ownerCt.close();
                                }
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
     *
     */
    initComponent: function(){
        Ext.applyIf(this, this.defaultConfig);
        
        this.items = [this.getForm()];
        
        GeoNetwork.admin.MetadataInsertPanel.superclass.initComponent.call(this);
    }
});

/** api: xtype = gn_admin_metadatainsertpanel */
Ext.reg('gn_admin_metadatainsertpanel', GeoNetwork.admin.MetadataInsertPanel);