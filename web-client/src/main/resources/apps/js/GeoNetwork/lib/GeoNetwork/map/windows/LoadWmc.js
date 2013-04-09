/*
 * Copyright (C) 2009 GeoNetwork
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

/**
 * @requires GeoNetwork/windows/BaseWindow.js
 */

Ext.namespace('GeoNetwork');

/**
 * Class: GeoNetwork.LoadWmcWindow
 *      Window to load WMS layers in map application
 *
 * Inherits from:
 *  - {GeoNetwork.BaseWindow}
 */

/**
 * Constructor: GeoNetwork.LoadWmcWindow
 * Create an instance of GeoNetwork.LoadWmcWindow
 *
 * Parameters:
 * config - {Object} A config object used to set the addwmslayer
 *     window's properties.
 */
GeoNetwork.LoadWmcWindow = function(config) {
    Ext.apply(this, config);
    GeoNetwork.LoadWmcWindow.superclass.constructor.call(this);
};

Ext.extend(GeoNetwork.LoadWmcWindow, GeoNetwork.BaseWindow, {

	 
    /**
     * Method: init
     *     Initialize this component.
     */
    initComponent: function() {
        GeoNetwork.LoadWmcWindow.superclass.initComponent.call(this);

        //Ext.QuickTips.init();
        this.width = 480;
        
        this.title = this.title || OpenLayers.i18n("selectWMCFile.windowTitle");

        this.resizable = false;

        this.charset = "UTF-8";

        var fp = new Ext.FormPanel({
            //renderTo: 'form_wmc',
            fileUpload: true,
            //width: 420,
            height: 100,
            bodyStyle: 'padding: 10px 10px 0 10px;',
            labelWidth: 0,
            plain: true,
            frame: true,
            border: false,
            defaults: {
                anchor: '90%',
                msgTarget: 'side',
                allowBlank: false
            },
            items: [
                {
                    xtype: 'fileuploadfield',
                    id: 'form-file',
                    width: 120,
                    emptyText: OpenLayers.i18n("selectWMCFile"),
                    hideLabel : true,
                    buttonText: '',
                    name: 'Fileconten',
                    buttonCfg: {
                        text: '',
                        iconCls: 'selectfile'
                    }
                }
            ],
            buttons: [{
                text: OpenLayers.i18n("selectWMCFile.loadButtonText"),
                scope: this,
                handler: function() {
                    if (fp.getForm().isValid()) {
                        fp.getForm().submit({
                            url: '../../wmc/load.wmc',
                            success: this.onSuccessLoad,
                            failure: this.onFailure,
                            scope: this
                        });
                    }
                }
            },{
                text: OpenLayers.i18n("selectWMCFile.mergeButtonText"),
                scope: this,
                handler: function() {
                   if (fp.getForm().isValid()) {
                        fp.getForm().submit({
                            url: '../../wmc/load.wmc',
                            success: this.onSuccessMerge,
                            failure: this.onFailure,
                            scope: this
                        });
                    }
                }
            }]
        });

        this.add(fp);

        this.doLayout();
    },

    onSuccessLoad: function(form, action) {
        var json = action.response.responseText;
        var o = Ext.decode(json);
        if (o.success) {
            var cb = OpenLayers.Function.bind(this.parseWMCLoad, this);
            OpenLayers.Request.GET({
                url: o.url, 
                scope: this,
                callback: cb
            });
        } else {
            this.onAjaxFailure();
        }
    },

    onSuccessMerge: function(form, action) {
        var json = action.response.responseText;
        var o = Ext.decode(json);
        if (o.success) {
            var cb = OpenLayers.Function.bind(this.parseWMCMerge, this);
            OpenLayers.loadURL(o.url, null, null, cb);
        } else {
            this.onAjaxFailure();
        }
    },

    onFailure: function(form, action) {
         Ext.MessageBox.show({icon: Ext.MessageBox.ERROR,
                    title: OpenLayers.i18n("errorTitle"), msg:
                    OpenLayers.i18n("InvalidWMC"),
                    buttons: Ext.MessageBox.OK});
    },

    /**
     * parseWMCLoad
     * Load the WMC and close the dialog
     *
     * Parameters:
     * response - {<OpenLayers.Ajax.Response>}
    */
    parseWMCLoad: function(response)
    {
        GeoNetwork.WMCManager.loadWmc(this.map, response.responseText);
        Ext.WindowMgr.getActive().close();
    },

    /**
     * parseWMCMerge
     * Merge the WMC and close the dialog
     *
     * Parameters:
     * response - {<OpenLayers.Ajax.Response>}
    */
    parseWMCMerge: function(response)
    {
        GeoNetwork.WMCManager.mergeWmc(this.map, response.responseText);
        Ext.WindowMgr.getActive().close();
    }
});
