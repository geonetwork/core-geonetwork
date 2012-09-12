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
 *  module = GeoNetwork.admin
 *  class = HarvesterPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor 
 *  .. class:: HarvesterPanel(config)
 *
 *     Create a GeoNetwork harvester panel (alpha)
 *
 *
 */
GeoNetwork.admin.HarvesterPanel = Ext.extend(Ext.Panel, {
    border: false,
    frame: false,
    layout: 'border',
    height: 800,
    catalogue: undefined,
    harvesterStore: undefined,
    harvesterTplMarkup: ['Information: {site_name}<br/>', 'Last run: {info_lastrun}<br/>', '<tpl if="info_result_total">', 'Total: {info_result_total}<br/>', 'Added: {info_result_added}<br/>', 'Removed: {info_result_removed}<br/>', 'Schema unknwon: {info_result_unknowSchema}<br/>', '</tpl>'    // TODO : other properties - display if available depends on harvester
    ],
    
    harvesterTpl: undefined,
    xmlTpl: undefined,
    harvesterGrid: undefined,
    currentHarvester: undefined,
    /** private: method[initComponent] 
     *  Initializes the harvester panel.
     */
    initComponent: function(){
        GeoNetwork.admin.HarvesterPanel.superclass.initComponent.call(this);
        panel = this;
        
        this.createGrid();
        
        // Information panel
        this.harvesterTpl = new Ext.XTemplate(this.harvesterTplMarkup);
        this.xmlTpl = new GeoNetwork.Templates().getHarvesterTemplate();
        
        this.harvesterGrid.getSelectionModel().on('rowselect', function(sm, rowIdx, r){
            var detailPanel = Ext.getCmp('harvesterDetailPanel');
            this.harvesterTpl.overwrite(detailPanel.body, r.data);
        }, this);
        
        var infoPanel = {
            id: 'harvesterDetailPanel',
            region: 'east',
            split: true,
            minWidth: 200,
            width: 200,
            collapsible: true,
            hideCollapseTool: true,
            collapseMode: 'mini',
            bodyStyle: {
                background: '#ffffff',
                padding: '7px'
            },
            html: 'Please select a harvester to see additional details.'
        };
        this.add(infoPanel);
        
        
        // Harvester configuration
        
        var editorPanel = {
            id: 'harvesterEditorPanel',
            region: 'south',
            title: 'Harvester configuration',
            split: true,
            autoScroll: true,
            minHeigth: 400,
            //autoHeight: true,
            //collapsible : true,
            items: [new Ext.FormPanel({
                border: false,
                frame: false,
                maxHeight: 600,
                id: 'harvesterEditorForm',
                items: [this.getHarvesterTypeField(), this.getSiteFields('geonetwork'), this.getOptionsFields('geonetwork')                //                        	         this.getPrivilegesFields('geonetwork'),
                //                        	         this.getCategoriesFields('geonetwork')
                ],
                buttons: [{
                    text: 'Save',
                    listeners: {
                        click: function(){
                            var form = Ext.getCmp('harvesterEditorForm').getForm();
                            if (form.isValid()) {
                            
                                form.updateRecord(this.currentHarvester); //console.log(this.currentHarvester);
                                var xml = this.xmlTpl.apply(this.currentHarvester.data);
                                // TODO : if id insert or update
                                this.addHarvester(this.catalogue.services.harvestingAdd, xml);
                            }
                        },
                        scope: panel
                    }
                }]
            })]
        };
        this.add(editorPanel);
        
    },
    
    // TODO : could we retrieve that from server ?
    harvesterType: [['geonetwork', 'GeoNetwork'], ['geonetwork20', 'GeoNetwork 2.0'], ['webdav', 'webdav'], ['csw', 'csw'], ['ogcwxs', 'OGC WxS'], ['thredds', 'thredds'], ['z3950', 'z3950'], ['oaipmh', 'oaipmh'], ['metadatafragments', 'metadatafragments'], ['arcsde', 'arcsde'], ['filesystem', 'filesystem']],
    switchMode: function(type){
        //		var form = Ext.getCmp('harvesterEditorForm');
        //		var result = form.getForm().getValues();
        //
        //		form.cascade(function(cur) {
        //			if (cur.disabled != true && cur.rendered) { // Check element is
        //					// enabled
        //					// and rendered (ie. visible, eg. field in a collapsed fieldset)
        //					if (cur.isXType('boxselect') || cur.isXType('combo') || cur.isXType('checkbox') || cur.isXType('textfield')) {
        //						cur.disable();
        //						cur.hide();
        //					}
        ////					else if () {
        ////						
        ////					}
        //			}
        //		});
        
        this.switchElement('site_servlet', false);
        this.switchElement('site_port', false);
        this.switchElement('site_host', false);
        this.switchElement('site_url', false);
        
        // Main 
        if (type == 'geonetwork' || type == 'geonetwork20') {
            this.switchElement('site_servlet', true);
            this.switchElement('site_port', true);
            this.switchElement('site_host', true);
        } else {
            this.switchElement('site_url', true);
        }
        
        
        if (type == 'ogcwxs') {
        }
        
    },
    /**
     *  disabled Be aware that conformant with the HTML specification, disabled Fields will not be submitted.
     */
    switchElement: function(el, on){
        var e = Ext.getCmp(el);
        var form = Ext.getCmp('harvesterEditorForm');
        var f = form.getForm().findField(el);
        if (on) {
            f.container.up('div.x-form-item').show();
            e.enable();
        } else {
            f.container.up('div.x-form-item').hide();
            e.disable();
        }
        form.doLayout(false, true); // FIXME
    },
    getHarvesterTypeField: function(){
        return new Ext.form.ComboBox({
            id: 'type',
            name: 'type',
            mode: 'local',
            triggerAction: 'all',
            fieldLabel: 'Harvester type',
            store: new Ext.data.ArrayStore({
                id: 0,
                fields: ['id', 'name'],
                data: this.harvesterType
            }),
            valueField: 'id',
            displayField: 'name',
            listeners: {
                change: function(field, newValue, oldValue){
                    this.switchMode(newValue);
                },
                scope: this
            }
        });
        
    },
    ogcwxsType: [['WMS1.0.0', 'WMS 1.0.0'], ['WMS1.1.1', 'WMS 1.1.1'], ['WFS1.0.0', 'WFS 1.0.0'], ['WFS1.1.0', 'WFS 1.1.0'], ['WCS1.0.0', 'WCS 1.0.0'], ['WPS0.4.0', 'WPS 0.4.0'], ['WPS1.0.0', 'WPS 1.0.0']],
    
    getSiteFields: function(type){
        var items = [];
        
        
        var id = new Ext.form.TextField({
            id: 'id',
            name: 'id',
            mode: 'local',
            hidden: true
        });
        var name = new Ext.form.TextField({
            name: 'site_name',
            mode: 'local',
            fieldLabel: 'Name'
        });
        items.push(id, name);
        
        /* OGC WxS */
        var url = new Ext.form.TextField({
            id: 'site_url',
            name: 'site_url',
            mode: 'local',
            fieldLabel: 'URL'
        });
        
        var ogctype = new Ext.form.ComboBox({
            id: 'site_ogctype',
            name: 'site_ogctype',
            mode: 'local',
            fieldLabel: 'Type of service',
            store: new Ext.data.ArrayStore({
                id: 0,
                fields: ['id', 'name'],
                data: this.ogcwxsType
            }),
            valueField: 'id',
            displayField: 'name'
        });
        
        
        items.push(url, ogctype);
        
        /* GeoNetwork */
        var servlet = new Ext.form.TextField({
            id: 'site_servlet',
            name: 'site_servlet',
            mode: 'local',
            fieldLabel: 'Servlet'
        });
        var host = new Ext.form.TextField({
            id: 'site_host',
            name: 'site_host',
            mode: 'local',
            fieldLabel: 'Host'
        });
        var port = new Ext.form.TextField({
            id: 'site_port',
            name: 'site_port',
            mode: 'local',
            fieldLabel: 'Port'
        });
        items.push(host, port, servlet);
        
        
        var site_account_use = new Ext.form.Checkbox({
            id: 'site_account_use',
            name: 'site_account_use',
            mode: 'local',
            fieldLabel: 'Use account'
        });
        var site_account_username = new Ext.form.TextField({
            id: 'site_account_username',
            name: 'site_account_username',
            mode: 'local',
            fieldLabel: 'Username'
        });
        var site_account_password = new Ext.form.TextField({
            id: 'site_account_password',
            name: 'site_account_password',
            mode: 'local',
            fieldLabel: 'Password'
        });
        
        items.push(site_account_use, site_account_username, site_account_password);
        
        
        return {
            xtype: 'fieldset',
            title: 'Harvester main information',
            collapsible: true,
            items: items
        };
        
    },
    getOptionsFields: function(){
        var items = [];
        /* OGC WxS */
        var options_createThumbnails = new Ext.form.Checkbox({
            id: 'options_createthumbnails',
            name: 'options_createthumbnails',
            mode: 'local',
            fieldLabel: 'Create thumbnail for WMS layers'
        });
        var options_useLayer = new Ext.form.Checkbox({
            id: 'options_uselayer',
            name: 'options_uselayer',
            mode: 'local',
            fieldLabel: 'Create metadata for layer elements using GetCapabilities information'
        });
        var options_uselayermd = new Ext.form.Checkbox({
            id: 'options_uselayermd',
            name: 'options_uselayermd',
            mode: 'local',
            fieldLabel: 'Create metadata for layer elements using MetadataURL attributes (if existing, if not use GetCapabilities)'
        });
        var options_lang = new Ext.form.TextField({
            id: 'options_lang',
            name: 'options_lang',
            mode: 'local',
            defaultValue: 'eng',
            fieldLabel: 'Language'
        });
        var options_topic = new Ext.form.TextField({
            id: 'options_topic',
            name: 'options_topic',
            mode: 'local',
            defaultValue: '',
            fieldLabel: 'Topic category'
        });
        
        items.push(options_createThumbnails, options_useLayer, options_uselayermd, options_lang, options_topic);
        
        /* For all */
        var options_oneRunOnly = new Ext.form.Checkbox({
            id: 'options_onerunonly',
            name: 'options_onerunonly',
            mode: 'local',
            fieldLabel: 'One run only'
        });
        
        var options_every = new Ext.form.TextField({
            id: 'options_every',
            name: 'options_every',
            mode: 'local',
            defaultValue: '90',
            fieldLabel: 'Every'
        });
        items.push(options_oneRunOnly, options_every);
        
        
        return {
            xtype: 'fieldset',
            title: 'Harvester options',
            collapsible: true,
            items: items
        };
    },
    getPrivilegesFields: function(){
        return;
    },
    getCategoriesFields: function(){
        return;
    },
    createEditor: function(){
    
        var Harvester = Ext.data.Record.create([{
            name: 'id',
            mapping: 'node/@id'
        }, // "mapping" property not needed if it is the same as "name"
        {
            name: 'occupation'
        } // This field will use "occupation" as the mapping.
]);
        
    },
    
    /** private: method[create]
     *  Create the form in a default hbox layout mode
     */
    createGrid: function(){
    
        var sm = new Ext.grid.CheckboxSelectionModel({
            singleSelect: true,
            header: '',
            listeners: {
                selectionchange: function(){
                    // Disable button according to choice
                    Ext.getCmp('harvestEditBt').setDisabled(this.getSelections().length < 1);
                    Ext.getCmp('harvestRemoveBt').setDisabled(this.getSelections().length < 1);
                    Ext.getCmp('harvestActivateBt').setDisabled(this.getSelections().length < 1);
                    Ext.getCmp('harvestDesactivateBt').setDisabled(this.getSelections().length < 1);
                    Ext.getCmp('harvestRunBt').setDisabled(this.getSelections().length < 1);
                }
            }
        });
        
        
        // No paging required
        this.harvesterGrid = new Ext.grid.GridPanel({
            store: this.harvesterStore,
            split: true,
            id: 'harvesterGrid',
            region: 'center',
            sm: sm,
            columns: [sm, {
                id: 'id',
                header: 'id',
                width: 60,
                sortable: true,
                dataIndex: 'id',
                hidden: true
            }, {
                header: 'Name',
                width: 125,
                sortable: true,
                dataIndex: 'site_name'
            }, {
                header: 'Type',
                width: 75,
                sortable: true,
                dataIndex: 'type'
            }, {
                header: 'Status',
                width: 80,
                sortable: true,
                dataIndex: 'site_status'
            }, {
                header: 'Running',
                width: 80,
                sortable: true,
                dataIndex: 'info_running'
            }, {
                header: 'Every',
                width: 80,
                sortable: true,
                dataIndex: 'options_every'
            }, {
                header: 'Last run',
                width: 160,
                sortable: true,
                dataIndex: 'info_lastrun'
            }],
            stripeRows: true,
            maxHeight: 250,
            height: 250,
            stateful: true,
            //border : false,
            stateId: 'grid',
            listeners: {
                rowclick: function(grid, rowIndex, e){
                    var h = this.harvesterGrid.getStore().getAt(rowIndex);
                    this.currentHarvester = h;
                    
                    console.log(h);
                    
                    var form = Ext.getCmp('harvesterEditorForm').getForm();
                    form.loadRecord(h);
                },
                scope: this
            },
            buttons: [{
                text: 'Add',
                listeners: {
                    click: function(){
                        // TODO
                    }
                }
            }, {
                text: 'Activate',
                id: 'harvestActivateBt',
                disabled: true,
                listeners: {
                    click: function(){
                        var sel = Ext.getCmp('harvesterGrid').getSelectionModel().getSelected();
                        var params = {
                            id: sel.data.id
                        };
                        this.doAction(this.catalogue.services.harvestingStart, params, this.refreshHarvester, null, null, 'Failed to activate the harvester');
                    },
                    scope: this
                }
            }, {
                text: 'Desactivate',
                id: 'harvestDesactivateBt',
                disabled: true,
                listeners: {
                    click: function(){
                        var sel = Ext.getCmp('harvesterGrid').getSelectionModel().getSelected();
                        var params = {
                            id: sel.data.id
                        };
                        this.doAction(this.catalogue.services.harvestingStop, params, this.refreshHarvester, null, null, 'Failed to desactivate the harvester');
                    },
                    scope: this
                }
            }, {
                // iconCls: 'md-mn-reset',
                id: 'harvestEditBt',
                text: 'Edit',
                disabled: true,
                //icon : '../images/default/cross.png',
                listeners: {
                    click: function(){
                        // TODO
                    },
                    scope: this
                }
            }, {
                text: 'Run',
                id: 'harvestRunBt',
                disabled: true,
                listeners: {
                    click: function(){
                        var sel = Ext.getCmp('harvesterGrid').getSelectionModel().getSelected();
                        var params = {
                            id: sel.data.id
                        };
                        this.doAction(this.catalogue.services.harvestingRun, params, this.refreshHarvester, null, null, 'Failed to start the harvester');
                    },
                    scope: this
                }
            }, {
                // iconCls: 'md-mn-reset',
                id: 'harvestRemoveBt',
                text: 'Remove',
                icon: '../images/default/cross.png',
                disabled: true,
                listeners: {
                    click: function(){
                        var sel = Ext.getCmp('harvesterGrid').getSelectionModel().getSelected();
                        Ext.Msg.confirm('Delete ?', 'Are you sure to delete selected harvester and all its records?', this.removeHarvester, sel.data.id);
                    },
                    scope: this
                }
            }, {
                text: 'Refresh',
                id: 'harvestRefreshBt',
                listeners: {
                    click: function(){
                        this.harvesterGrid.getStore().reload();
                    },
                    scope: this
                }
            }]
        });
        
        this.add(this.harvesterGrid);
    },
    addHarvester: function(url, xml){
    
        var opts = {
            url: url, // UPDATE
            data: xml
        };
        OpenLayers.Util.applyDefaults(opts, {
            success: function(response){
                Ext.Msg.alert('ok', response.responseText);
            },
            failure: function(response){
                Ext.Msg.alert('Failed', response.responseText);
            }
        });
        OpenLayers.Request.POST(opts);
    },
    /** 
     *  :param url: ``String`` The service url.
     *  :param params: ``Object`` The service parameters.
     *  :param onSuccess: ``Function`` Function to trigger on success.
     *  :param onFailure: ``Function`` Function to trigger on failure.
     *  :param msgSuccess: ``String`` If no function, display this message on success.
     *  :param msgFailure: ``String`` If no function, display this message on failure.
     */
    doAction: function(url, params, onSuccess, onFailure, msgSuccess, msgFailure){
        OpenLayers.Request.GET({
            url: url,
            params: params,
            success: function(result){
                if (msgSuccess) {
                    Ext.Msg.alert(msgSuccess, result.responseText);
                }
                
                if (onSuccess) {
                    onSuccess(result);
                }
            },
            failure: function(response){
                if (msgFailure) {
                    Ext.Msg.alert(msgFailure, response.responseText);
                }
                
                if (onSuccess) {
                    onSuccess(response);
                }
            }
        });
    },
    removeHarvester: function(btn){
        if (btn == 'yes') {
            var params = {
                id: this
            };
            this.doAction(this.catalogue.services.harvestingRemove, params, this.refreshHarvester, null, null, 'Failed to delete the harvester');
        }
    },
    refreshHarvester: function(){
        Ext.getCmp('harvestRefreshBt').fireEvent('click');
    }
});

/** api: xtype = gn_admin_harvesterpanel */
Ext.reg('gn_admin_harvesterpanel', GeoNetwork.admin.HarvesterPanel);