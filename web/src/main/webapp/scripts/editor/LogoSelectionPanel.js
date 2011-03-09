/*
 * Copyright (C) 2010-2011 GeoNetwork
 *
 * This file is part of GeoNetwork
 *
 * GeoNetwork is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoNetwork is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoNetwork.  If not, see <http://www.gnu.org/licenses/>.
 */
Ext.namespace('GeoNetwork.editor');

GeoNetwork.editor.LogoSelectionPanel = Ext.extend(Ext.Panel, {
    defaultConfig: {
        autoScroll: true,
        autoWidth: true,
        layout: 'border'
    },
    store: undefined,
    view: undefined,
    serviceUrl: undefined,
    logoAddUrl: 'logo.add',
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
        this.uploadForm = new Ext.FormPanel({
            fileUpload: true,
            region: 'south',
            height: 80,
            split: true,
            items: {
                xtype: 'fileuploadfield',
                id: 'form-file',
                allowBlank: false,
                emptyText: translate('logoSelect'),
                hideLabel: true,
                name: 'fname'
            },
            buttons: [{
                text: translate('upload'),
                scope: this,
                handler: function(){
                    if (this.uploadForm.getForm().isValid()) {
                        this.uploadForm.getForm().submit({
                            url: this.logoAddUrl,
                            scope: this,
                            success: function(fp, action){
                                this.store.reload();
                            },
                            failure: function(response){
                                Ext.Msg.alert('Error', response.responseText);
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
    initComponent: function(config){
        Ext.apply(this, config);
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
            store : this.store,
            tpl : tpl,
            singleSelect : true,
            selectedClass : 'logo-selected',
            overClass : 'logo-over',
            itemSelector : 'div.logo-wrap',
            autoScroll : true,
            listeners : {
                selectionchange : function(dv, selections) {
                    var idx = this.view
                            .getSelectedIndexes();
                    if (selections) {
                        this.fireEvent('logoselected', this, idx);
                        this.ownerCt.hide();
                    }
                },
                scope : this
            }
        });
        this.items = [new Ext.Panel({
            bbar: this.tb,
            region: 'center',
            split: true,
            border: true,
            autoScroll: true,
            items: [this.view]
        }), this.getUploadForm()];
        
        GeoNetwork.editor.LogoSelectionPanel.superclass.initComponent.call(this);
        
        /**
         * triggered when the user has selected a keyword
         */
        this.addEvents('logoselected');
        
        
        this.store.load();
    }
});

/** api: xtype = gn_editor_helppanel */
Ext.reg('gn_editor_logoselectionpanel', GeoNetwork.editor.LogoSelectionPanel);