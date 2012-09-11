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
 * @requires GeoNetwork/widgets/wms/PreviewPanel.js
 */

Ext.namespace('GeoNetwork', 'GeoNetwork.wms');

/**
 * Class: GeoNetwork.wms.LayerInfoPanel
 *        LayerInfoPanel groups together a GridPanel and a WMSPreviewPanel
 *      to show WMS layer information.
 */

/**
 * Constructor: GeoNetwork.wms.InfoLayerPanel
 * Create an instance of GeoNetwork.wms.InfoLayerPanel
 *
 * Parameters:
 * config - {Object} A config object used to set the properties
 */
GeoNetwork.wms.LayerInfoPanel = function(config) {
    Ext.apply(this, config);
    GeoNetwork.wms.LayerInfoPanel.superclass.constructor.call(this);
};


Ext.extend(GeoNetwork.wms.LayerInfoPanel, Ext.Panel, {

    /**
     * APIProperty: previewPanel
     * {<GeoNetwork.WMSPreviewPanel>} the panel used for previewing
     *     WMS layers
     */
    previewPanel: null,

    /**
     * APIProperty: map
     * {<OpenLayers.Map>} the map object
     */
    map: null,

    /**
     * APIProperty: previewCenterPoint
     * {<OpenLayers.Geometry.Point>} it preview can be centered on this point,
     *     but defaults to the map center
     */
    previewCenterPoint: null,

    /**
     * APIProperty: onlineresource
     * {<String>} onlineresource for GetCapabilities
     */
    onlineresource: null,

    /**
     * APIProperty: layer
     * {<OpenLayers.Layer>} layer to get WMS information
     */
    layer: null,

    /**
     * initComponent
     * Initialize this component
     */
    initComponent: function() {
        GeoNetwork.wms.LayerInfoPanel.superclass.initComponent.call(this);

        this.layout = 'border';
	this.border = false;

        this.previewPanel = new GeoNetwork.wms.PreviewPanel(
            {map: this.map, previewCenterPoint: this.previewCenterPoint});

        this.store = new Ext.data.SimpleStore({
            reader: new Ext.data.ArrayReader({}, [
                        {name: 'title',        type: 'string'},
                        {name: 'field',       type: 'string'}
            ]),
            fields: ['title', 'field']
        });

        // Grid to show layer WMS info
        this.gridPanel = new Ext.grid.GridPanel({
            title: "",
            store: this.store,
            autoScroll: true,
            hideHeaders: false,
            columns: []
        });

        this.layerInfo = new GeoNetwork.wms.WMSLayerInfo(
            {callback: this._showLayerInfo, scope: this});

        var center = {region: 'center', layout: 'fit', items: [this.gridPanel],
            split: true, width: 300, minWidth: 300};

        var east = {region: 'east', items: [this.previewPanel], split: true,
            plain: true, cls: 'popup-variant1', width:250, maxSize: 250,
            minSize: 250};

        this.add(center);
        this.add(east);

        this.doLayout();

    },

    showLayerInfo: function() {
        this.previewPanel.showPreview(this.layer);
        this.layerInfo.loadWMS(this.onlineresource, this.layer);
    },

    /**
     * Method: showLayerInfo
     * Show the layer information in the GridPanel when has been loaded
     *
     * Parameters:
     * layer - {<OpenLayers.Layer.WMS>} the newly created layer
     */
    _showLayerInfo: function(layer) {
       if (!layer) {
            Ext.MessageBox.alert(OpenLayers.i18n("errorTitle"),
                    OpenLayers.i18n("WMSBrowserConnectError"));
            this.body.dom.style.cursor = 'default';
        } else {
            var info = [
                [OpenLayers.i18n("layerInfoPanel.titleField"), layer.title || layer.name],
                [OpenLayers.i18n("layerInfoPanel.descriptionField"), layer.description],
                [OpenLayers.i18n("layerInfoPanel.queryableField"), layer.queryable]
            ];

			this.gridPanel.reconfigure(this.store, new Ext.grid.ColumnModel([
                {header: 'Field', dataIndex: "title", sortable: true},
                {id: 'value', header: 'Value', dataIndex: "field", sortable: true}
            ]));

	    this.gridPanel.autoExpandColumn = 'value';

            this.gridPanel.getStore().loadData(info);
				}

        this.body.dom.style.cursor = 'default';
    }

});

Ext.reg('gn_infolayerpanel', GeoNetwork.wms.LayerInfoPanel);
