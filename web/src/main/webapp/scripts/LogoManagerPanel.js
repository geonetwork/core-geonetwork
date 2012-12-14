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
    view: undefined,
    uploadForm: undefined,
    tb: undefined,
    height: 600,
    autoWidth: true,
    layout: 'column',
    border: false,
    /**
     * private: method[initComponent] Initializes the logo view
     */
    initComponent: function (renderTo) {
        this.renderTo = renderTo;

        var tpl = new Ext.XTemplate(
                '<tpl for="."><div class="logo-wrap"><div id="{name}" class="logo">',
                '<img src="../../images/harvesting/{name}" title="{name}"/><span>{name}</span></div></div>',
                '</tpl>');

        var logo = Ext.data.Record.create([ {
            name: 'name',
            mapping: ''
        } ]);

        this.store = new Ext.data.Store({
            autoDestroy: true,
            proxy: new Ext.data.HttpProxy({
                method: 'GET',
                url: 'xml.harvesting.info?type=icons',
                disableCaching: false
            }),
            reader: new Ext.data.XmlReader({
                record: 'icon',
                id: 'icon'
            }, logo),
            fields: [ 'name' ]
        });

        var lp = this;
        this.tb = new Ext.Toolbar({
            disabled: true,
            items: [{
                xtype: 'tbtext',
                text: translate('selectedLogo')
            }, {
                xtype: 'button',
                text: translate('logoDel'),
                listeners: {
                    click: function () {
                        this.removeSelectedLogo();
                    },
                    scope: lp
                }
            }, {
                xtype: 'button',
                text: translate('logoForNode'),
                listeners: {
                    click: function() {
                        this.setSelectedLogo(0);
                    },
                    scope: lp
                }
            }, {
                xtype: 'button',
                text: translate('logoForNodeFavicon'),
                listeners: {
                    click: function() {
                        this.setSelectedLogo(1);
                    },
                    scope: lp
                }
            } ]
        });

        this.view = new Ext.DataView({
            store: this.store,
            tpl: tpl,
            singleSelect: true,
            selectedClass: 'logo-selected',
            overClass: 'logo-over',
            itemSelector: 'div.logo-wrap',
            autoScroll: true,   // Does not look to work
            height: 500,
            listeners: {
                selectionchange: function() {
                    var selection = this.view
                            .getSelectedIndexes();
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
            columnWidth: .7,
            baseCls: 'x-plain',
            items: [new Ext.Panel({
                title: translate('logoRegistered'),
                bbar: this.tb,
                border: false,
                items: [ this.view ]
            })]
        }, {
            columnWidth: .3, 
            baseCls: 'x-plain',
            items: [this.getUploadForm()]
        }];
        this.store.on('load', function () {
            // autoScroll property does not work. So set the overflow.
            this.view.el.dom.style.overflow = 'auto';
        }, this);
        
        this.store.load();

        GeoNetwork.LogoManagerPanel.superclass.initComponent
                .call(this);

    },
    /** private: method[onDestroy] 
     * 
     *  Private method called during
     *  the destroy sequence.
     */
    onDestroy: function () {
        GeoNetwork.LogoManagerPanel.superclass.onDestroy.apply(
                this, arguments);
    },
    /** private: method[removeSelectedLogo] 
     * 
     *  Call logo.delete service to remove selected one.
     */
    removeSelectedLogo: function () {
        var lp = this;
        var selection = this.view.getSelectedIndexes();
        var record = this.view.getStore().getAt(selection[0]);
        var name = record.get('name');
        OpenLayers.Request.GET({
            url: 'logo.delete?fname=' + name,
            success: function (response) {
                lp.store.reload();
            },
            failure: function (response) {
                Ext.Msg.alert('Error', response.responseText);
            }
        });
    },
    /** private: method[setSelectedLogo] 
     * 
     *  Call logo.set service to remove selected one.
     */
    setSelectedLogo: function (favicon) {
        var lp = this;
        var selection = this.view.getSelectedIndexes();
        var record = this.view.getStore().getAt(selection[0]);
        var name = record.get('name');
        OpenLayers.Request.GET({
            url: 'logo.set?fname=' + name + "&favicon=" + favicon,
            success: function (response) {
            },
            failure: function (response) {
                Ext.Msg.alert('Error', response.responseText);
            }
        });
    },
    getUploadForm: function () {
        var the = this;
        this.uploadForm = new Ext.FormPanel({
            title: translate('logoAdd'),
            fileUpload: true,
            region: 'west',
            minWidth: 250,
            width: 250,
            split: true,
            items: {
                xtype: 'fileuploadfield',
                id: 'form-file',
                allowBlank: false,
                emptyText: translate('logoSelect'),
                hideLabel: true,
                name: 'fname'
            },
            buttons: [ {
                text: translate('upload'),
                scope: the,
                handler: function () {
                    if (this.uploadForm.getForm()
                            .isValid()) {
                        this.uploadForm
                                .getForm()
                                .submit({
                            url: 'logo.add',
                            scope: this,
                            success: function (fp, action) {
                                this.store
                                        .reload();
                            },
                            failure: function (response) {
                                Ext.Msg
                                        .alert(
                                                'Error',
                                                response.responseText);
                            }
                        });
                    }
                }
            } ]
        });
        return this.uploadForm;
    }
});

/** api: xtype = gn_LogoManagerPanel */
Ext.reg('gn_logomanagerpanel', GeoNetwork.LogoManagerPanel);
