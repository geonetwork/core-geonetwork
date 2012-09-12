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
 * Class: GeoNetwork.wms.LayerStylesPanel
 *      LayerStylesPanel groups together a GridPanel and a WMSPreviewPanel
 *      to show WMS layer styles information.
 */

/**
 * Constructor: GeoNetwork.wms.LayerStylesPanel
 * Create an instance of GeoNetwork.wms.LayerStylesPanel
 *
 * Parameters:
 * config - {Object} A config object used to set the properties
 */
GeoNetwork.wms.LayerStylesPanel = function(config) {
    Ext.apply(this, config);
    GeoNetwork.wms.LayerStylesPanel.superclass.constructor.call(this);
};


Ext.extend(GeoNetwork.wms.LayerStylesPanel, Ext.Panel, {

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
     * APIProperty: layer
     * {<OpenLayers.Layer>} layer to get WMS information
     */
    layer: null,

    /**
     * APIProperty: selectedStyle
     * {<String>} selected style by user
     */
    selectedStyle: null,

    /**
     * APIProperty: selectedStyleLegendUrl
     * {<String>} legend url for selected style by user
     */

    /**
     * initComponent
     * Initialize this component
     */
    initComponent: function() {
        GeoNetwork.wms.LayerStylesPanel.superclass.initComponent.call(this);

        this.layout = 'border';
	this.border = false;

        this.previewPanel = new GeoNetwork.wms.PreviewPanel(
            {map: this.map, title: OpenLayers.i18n("layerStylesPreviewTitle")});

        this.store = new Ext.data.SimpleStore({
            reader: new Ext.data.ArrayReader({}, [
                        {name: 'name',   type: 'string'},
                        {name: 'title',  type: 'string'},
                        {name: 'legendUrl',  type: 'string'}
            ]),
            fields: ['name', 'title', 'legendUrl']
        });

        // Grid to show layer WMS info
        this.gridPanel = new Ext.grid.GridPanel({
            title: "",
	    border: false,
	    autoScroll: true,
            store: this.store,
            hideHeaders: false,
            columns:
            [{header: "Style", width: 120, dataIndex: 'name', sortable: false},
            {id: 'description', header: "Description", width: 180, dataIndex: 'title', sortable: false}],
	    autoExpandColumn: 'description'
        });

 				this.gridPanel.on('rowclick', this._selectStyle, this);

        var center = {region: 'center', layout: 'fit', items: [this.gridPanel],
            split: true, width: 300, minWidth: 300};

        var east = {region: 'east', items: [this.previewPanel], split: true,
            plain: true, cls: 'popup-variant1', width:250, maxSize: 250,
            minSize: 250};

        this.add(center);
        this.add(east);

        this.doLayout();

    },

    showLayerStyles: function(layer) {
        var info = [];

        for(var i = 0; i < layer.styles.length; i++) {
	    var legendUrl = '';
	    if (layer.styles[i].legend) legendUrl = layer.styles[i].legend.href;

            var style = [layer.styles[i].name, layer.styles[i].title, legendUrl];
            info.push(style);
        }

			/*this.gridPanel.reconfigure(this.store, new Ext.grid.ColumnModel([
                {dataIndex: "name", sortable: true},
                {dataIndex: "title", sortable: true}
            ]));*/

            this.gridPanel.getStore().loadData(info);
    },

    _selectStyle: function(grid, rowIndex, e) {
          var rec = grid.store.getAt(rowIndex);
      		this.selectedStyle = rec.get('name');


      		grid.getView().focusEl.focus();

            var legendUrl = rec.get('legendUrl');
	    if (legendUrl == '') return;

            legendUrl =unescape(legendUrl);
            this.selectedStyleLegendUrl = legendUrl;
	    var legendUrlStyle = rec.get('legendUrl') + '&style=' + this.selectedStyle;
            this.previewPanel.showPreviewLegend(unescape(legendUrlStyle));
    }


});

Ext.reg('gn_layerstylespanel', GeoNetwork.wms.LayerStylesPanel);
