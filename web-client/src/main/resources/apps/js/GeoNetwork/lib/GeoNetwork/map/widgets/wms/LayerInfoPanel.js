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
 *        LayerInfoPanel groups together a infoPanel and a WMSPreviewPanel
 *      to show WMS layer information.
 */

/**
 * Constructor: GeoNetwork.wms.InfoLayerPanel
 * Create an instance of GeoNetwork.wms.InfoLayerPanel
 *
 * Parameters:
 * config - {Object} A config object used to set the properties
 */
GeoNetwork.wms.LayerInfoPanel = function (config) {
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
     * APIProperty: metadataId
     * {<String>} catalogue metadata identifier
     */
    metadataId: null,
    /**
     * APIProperty: tpl
     * {<Ext.XTemplate>} template to display capability information about the layer
     */
    tpl: null,
    
    /**
     * initComponent
     * Initialize this component
     */
    initComponent: function () {
        GeoNetwork.wms.LayerInfoPanel.superclass.initComponent.call(this);
        
        this.tpl = new Ext.XTemplate(
            '<div class="info-wms">',
                '<h1>{name}</h1><p>{description}</p><ul>',
                '<li><span>' + OpenLayers.i18n('layerInfoPanel.queryableField') + '</span>{queryable}</li>',
                '<li><span>' + OpenLayers.i18n('url') + '</span><a href="{url}">{url}</a></li>',
                '<tpl if="values.keywords.length != 0">',
                    '<li><span>' + OpenLayers.i18n('keyword') + '</span><ul>',
                    '<tpl for="keywords">',
                         '<li>{value}</li>',
                    '</tpl>',
                    '</ul></li>',
                '</tpl>',
                '<tpl if="values.metadataURLs.length != 0 || values.metadataId">',
                     '<li><span>' + OpenLayers.i18n('webLink') + '</span><ul>',
                     '<tpl for="metadataURLs">',
                          '<li><a href="{href}">{href} ({type})</a></li>',
                     '</tpl>',
                     '<tpl if="values.metadataId != \'\'">',
                          '<li><a href="javascript:void(0);" onclick="javascript:catalogue.metadataShow(\'{metadataId}\');return false;">' + OpenLayers.i18n('md') + '</a></li>',
                     '</tpl>',
                     '</ul></li>',
                '</tpl>',
                '</ul>',
            '</div>'
            );
        this.layout = 'border';
        this.border = false;
        
        this.previewPanel = new GeoNetwork.wms.PreviewPanel(
            {map: this.map, previewCenterPoint: this.previewCenterPoint});
        
        this.infoPanel = new Ext.Panel();
        this.layerInfo = new GeoNetwork.wms.WMSLayerInfo(
            {callback: this._showLayerInfo, scope: this});
        
        var center = {region: 'center', layout: 'fit', items: [this.infoPanel],
            split: true, width: 300, minWidth: 300};
        
        var east = {region: 'east', items: [this.previewPanel], split: true,
            plain: true, cls: 'popup-variant1', width: 250, maxSize: 250,
            minSize: 250};
        
        this.add(center);
        this.add(east);
        
        this.doLayout();
    },
    showLayerInfo: function () {
        this.previewPanel.showPreview(this.layer);
        this.layerInfo.loadWMS(this.onlineresource, this.layer);
    },
    /**
     * Method: showLayerInfo
     * Show the layer information in the infoPanel when has been loaded
     *
     * Parameters:
     * layer - {<OpenLayers.Layer.WMS>} the newly created layer
     */
    _showLayerInfo: function (layer) {
        if (!layer) {
            GeoNetwork.Message().msg({
                title: OpenLayers.i18n('error'), 
                msg: OpenLayers.i18n("WMSBrowserConnectError"), 
                status: 'error', 
                target: this.getId() 
            });
        } else {
            layer.metadataId = this.metadataId || '';
            this.infoPanel.update(this.tpl.apply(layer));
        }
        this.body.dom.style.cursor = 'default';
    }
});

Ext.reg('gn_infolayerpanel', GeoNetwork.wms.LayerInfoPanel);