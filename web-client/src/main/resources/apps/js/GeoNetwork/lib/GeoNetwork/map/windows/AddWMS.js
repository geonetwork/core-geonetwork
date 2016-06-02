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
 * Class: GeoNetwork.AddWmsWindow Window to load WMS layers in map application
 * 
 * Inherits from: - {GeoNetwork.BaseWindow}
 */

/**
 * Constructor: GeoNetwork.AddWmsWindow Create an instance of
 * GeoNetwork.AddWmsWindow
 * 
 * Parameters: config - {Object} A config object used to set the addwmslayer
 * window's properties.
 */
GeoNetwork.AddWmsLayerWindow = function(config) {
	Ext.apply(this, config);
	GeoNetwork.AddWmsLayerWindow.superclass.constructor.call(this);
};

Ext.extend(GeoNetwork.AddWmsLayerWindow, GeoNetwork.BaseWindow, {
    
    iconCls: 'addLayerIcon',
    browserPanel : null,
	/**
	 * Method: init Initialize this component.
	 */
	initComponent : function() {
        
		GeoNetwork.AddWmsLayerWindow.superclass.initComponent.call(this);

		this.title = this.title || OpenLayers.i18n("addWMSWindowTitle");

		this.width = 600;
		this.height = 500;

		var ds = new Ext.data.Store( {
			data : GeoNetwork.WMSList,
			reader : new Ext.data.ArrayReader( {}, [ {
				name : 'title'
			}, {
				name : 'url'
			} ])
		});

		this.browserPanel = {
		        id : this.id + 'wmsbrowserpanel',
                xtype : 'gn_wmsbrowserpanel',
                mode : GeoNetwork.wms.BrowserPanel.ADDWMS,
                wmsStore : ds,
                map : this.map
            };

		var tabs = new Ext.Panel( {
			border : false,
			deferredRender : false,
			layout : 'fit',
			items : [
			/*
			 * {xtype: 'gn_wmsbrowserpanel', title:
			 * OpenLayers.i18n("WMSBrowserTab1"), wmsStore: ds, map: map},
			 */
			/*
			 * {title: OpenLayers.i18n("WMSBrowserTab3"), xtype:
			 * 'gn_wmsbrowserpanel', mode: GeoNetwork.wms.BrowserPanel.ADDWMS,
			 * map: this.map}
			 */
			this.browserPanel
			]
		});

		this.add(tabs);

		this.doLayout();
	}
});
