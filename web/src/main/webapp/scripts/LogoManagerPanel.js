/*
 * Copyright (C) 2001-2011 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
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
Ext.namespace('GeoNetwork');


/** api: (define) 
 *  module = GeoNetwork 
 *  class = LogoManagerPanel 
 *  base_link = `Ext.FormPanel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/**
 * api: constructor .. class:: LogoManagerPanel(config)
 * 
 * Logo manager panel to add/remove/set logos
 */
GeoNetwork.LogoManagerPanel = Ext.extend(Ext.Panel, {
    store: undefined,
    dview: undefined,
    tb: undefined,
    height: 500,
    border: false,
    layout: 'border',
    
    /**
     * private: method[initComponent] Initializes the logo view
     */
    initComponent : function(renderTo) {
        this.renderTo = renderTo;
        
        this.store = new Ext.data.Store({
            autoDestroy : true,
            proxy : new Ext.data.HttpProxy({
                method : 'GET',
                url : 'xml.harvesting.info?type=icons',
                disableCaching : false
            }),
            reader : new Ext.data.XmlReader({
                record : 'icon',
                id : 'icon'
            }, Ext.data.Record.create([{
                name : 'name',
                mapping : ''
            }])),
            fields : [ 'name' ]
        });

        var lp = this;
        this.tb = new Ext.Toolbar({
            disabled : true,
            items : [{
                xtype: 'tbtext',
                text : translate('selectedLogo')
            }, {
                xtype : 'button',
                text : translate('logoDel'),
                listeners : {
                    click : function() {
                        this.removeSelectedLogo();
                    },
                    scope : lp
                }
            }, {
                xtype : 'button',
                text : translate('logoForNode'),
                listeners : {
                    click : function() {
                        this.setSelectedLogo(0);
                    },
                    scope : lp
                }
            }, {
                xtype : 'button',
                text : translate('logoForNodeFavicon'),
                listeners : {
                    click : function() {
                        this.setSelectedLogo(1);
                    },
                    scope : lp
                }
            } ]
        });

        this.dview = new Ext.DataView({
            store: this.store,
            tpl: new Ext.XTemplate(
                '<tpl for="."><div class="logo-wrap"><div id="{name}" class="logo">',
                '<img src="../../images/harvesting/{name}" title="{name}"/><span>{name}</span></div></div>',
                '</tpl>'
            ),
            singleSelect: true,
            selectedClass: 'logo-selected',
            overClass: 'logo-over',
            itemSelector: 'div.logo-wrap',
            autoScroll: true,
            height: 445, // required for FF
            listeners: {
                "selectionchange": function() {
                    var selection = this.dview.getSelectedIndexes();
                    if (selection.length > 0) {
                        this.tb.enable();
                    } else {
                        this.tb.disable();
                    }
                },
                scope: lp
            }
        });

        this.items = [{
            title: translate('logoRegistered'),
            bbar: this.tb,
            region: 'center',
            layout: 'fit',
            border: true,
            items: [ this.dview ]
        }, {
            region: 'west',
            border: true,
            minWidth: 250,
            width: 250,
            split: true,
            layout: 'fit',
            title: translate('logoAdd'),
            bodyStyle: 'padding: 10px;',
            items: [{
                xtype: 'form',
                border: false,
                fileUpload: true,
                monitorValid: true,
                items: [{
                    xtype: 'fileuploadfield',
                    id: 'form-file',
                    allowBlank: false,
                    emptyText: translate('logoSelect'),
                    hideLabel: true,
                    name: 'fname'
                }],
                buttons: [{
                    text: translate('upload'),
                    formBind: true,
                    handler: function() {
                        if (this.ownerCt.getForm().isValid()) {
                            this.ownerCt.getForm().submit({
                                url: 'logo.add',
                                scope: lp,
                                success: function(fp,action) {
                                    this.store.reload();
                                },
                                failure : function(response) {
                                    Ext.Msg.alert('Error',response.responseText);
                                }
                            });
                        }
                    }
                }]
            }] 
        }];

        this.store.load();

        GeoNetwork.LogoManagerPanel.superclass.initComponent
                .call(this);
        
    },
    
    /** private: method[onDestroy] 
     * 
     *  Private method called during
     *  the destroy sequence.
     */
    onDestroy : function() {
        GeoNetwork.LogoManagerPanel.superclass.onDestroy.apply(
                this, arguments);
    },
    
    /** private: method[removeSelectedLogo] 
     * 
     *  Call logo.delete service to remove selected one.
     */
    removeSelectedLogo : function() {
        var lp = this;
        var selection = this.dview.getSelectedIndexes();
        var record = this.dview.getStore().getAt(selection[0]);
        var name = record.get('name');
        OpenLayers.Request.GET({
            url : 'logo.delete?fname=' + name,
            success : function(response) {
                lp.store.reload();
            },
            failure : function(response) {
                Ext.Msg.alert('Error', response.responseText);
            }
        });
    },
    
    /** private: method[setSelectedLogo] 
     * 
     *  Call logo.set service to remove selected one.
     */
    setSelectedLogo : function(favicon) {
        var lp = this;
        var selection = this.dview.getSelectedIndexes();
        var record = this.dview.getStore().getAt(selection[0]);
        var name = record.get('name');
        OpenLayers.Request.GET({
            url: 'logo.set?fname=' + name + "&favicon=" + favicon,
            success: function(response) {
                // nothing in here
            },
            failure: function(response) {
                Ext.Msg.alert('Error', response.responseText);
            }
        });
    }
});

/** api: xtype = gn_LogoManagerPanel */
Ext.reg('gn_logomanagerpanel', GeoNetwork.LogoManagerPanel);
