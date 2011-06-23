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

Ext.namespace('GeoNetwork', 'GeoNetwork.CatalogueInterface');

/**
 * Class: GeoNetwork.CatalogueInterface
 * Used to add layers to the map through the WMS GetCapabilities
 * interface of the layers. It will show a loading mask whilst processing.
 */
GeoNetwork.CatalogueInterface = function() {

    // private
    var map;

    var layerLoadingMask;

    var layers;

    var setMap = function(mapC) {
        map = mapC;
    };
    
    /**
     * Function: getLayer
     * Recursive function to process a layer and their childLayers in the
     * capabilities document, searching for the requested layer
     *
     * Parameters:
     * caps - {Object} capabilities from the WMSCapabilities parser
     * caplayers - {Object} list of layer objects from the WMSCapabilities parser
     * layer - {Object} layer to return from capabilities document
     *
     * Returns:
     * {Object} The layer if found, otherwise null
     */
    var getLayer = function(caps, caplayers, layer) {
        var findedLayer = null;

        for (var i = 0, len = caplayers.length; i < len; ++i) {
            var lr = caplayers[i];

            try {
                var layerName = lr.name.split(",");

                if (layerName.indexOf(layer.params.LAYERS) != -1) {
                    findedLayer = lr;
                    break;
                }
            } catch(e) {
            }

            if (typeof(lr.nestedLayers) != "undefined") {
                findedLayer = getLayer(caps, lr.nestedLayers, layer);
                if (findedLayer !== null) {
                    break;
                }
            }

        }

        return findedLayer;
    };

    /**
     * Method: processLayersSuccess
     * Called when the GetCapabilities response is in.
     *
     * Parameters:
     * response - {Object} The response object
     */
    var processLayersSuccess = function(response) {
        layerLoadingMask.hide();
        
        var parser = new OpenLayers.Format.WMSCapabilities();
        var caps = parser.read(response.responseXML || response.responseText);
        if (caps.capability) {
            // GetCapabilities disclaimer
            var accessContraints = caps.service.accessContraints;

            if ((accessContraints) && (accessContraints.toLowerCase() != "none") && 
              (accessContraints != "-")) {
                var disclaimerWindow = new GeoNetwork.DisclaimerWindow({
                    disclaimer: accessContraints
                });
                disclaimerWindow.show();
                disclaimerWindow = null;
            }

            if (map) {
                for(var i = 0, len=layers.length; i < len; i++) {
                    var name = layers[i][0];
                    var url = layers[i][1];
                    var layer = layers[i][2];
                    var metadata_id = layers[i][3];

                    var ol_layer = new OpenLayers.Layer.WMS(name, url,
                        {layers: layer, format: 'image/png', transparent: 'TRUE', version: caps.version, language: GeoNetwork.OGCUtil.getLanguage()},
                        {queryable: true, singleTile: true, ratio: 1, buffer: 0, transitionEffect: 'resize', metadata_id: metadata_id} );

                    if (!GeoNetwork.OGCUtil.layerExistsInMap(ol_layer, map)) {
                        // TODO: these events are never removed?
                        ol_layer.events.on({"loadstart": function() {
                                this.isLoading = true;
                            }});

                        ol_layer.events.on({"loadend": function() {
                                this.isLoading = false;
                            }});

                        var layerCap = getLayer(caps, caps.capability.layers, ol_layer);

                        if (layerCap) {
                            ol_layer.queryable = layerCap.queryable;
                            ol_layer.name = layerCap.title || ol_layer.name;
                            ol_layer.llbbox = layerCap.llbbox;
                            ol_layer.styles = layerCap.styles;
                            ol_layer.dimensions = layerCap.dimensions;
                        }

                        map.addLayer(ol_layer);
                    }
                }

            }
        }
    };

    /**
     * Method: processLayersFailure
     * Called when the GetCapabilities response is in and failed.
     *
     * Parameters:
     * response - {Object} The response object
     */
    var processLayersFailure = function(response) {
        layerLoadingMask.hide();

        Ext.MessageBox.alert(OpenLayers.i18n("loadLayer.error.title"),
            OpenLayers.i18n("loadLayer.error.message"));
    };

    // public
    return {
        /**
         * APIMethod: init
         * Inits the catalogue interface
         *
         * Parameters:
         * (map - {Object} Openlayers map
         */
        init: function(map) {
            setMap(map);
        },

        /**
         * APIMethod: addLayers
         * Add a list of layers to the map, using the GetCapabilities
         * to get the properties for a layer.
         *
         * Parameters:
         * layerList - {Array} One or more layers belonging to the same WMS
         */
        addLayers: function(layerList)  {
            if (layerList.length === 0) {
                return;
            }

        	var onlineResource = layerList[0][1];
            
        	/* if null layer name, open the WMS Browser panel */
        	if (layerList[0][2]==='') {
        	    GeoNetwork.WindowManager.showWindow("addwms");
        	    var panel = Ext.getCmp(GeoNetwork.WindowManager.getWindow("addwms").browserPanel.id);
        	    panel.setURL(onlineResource);
        	    return;
        	}
        	
            layerLoadingMask = new Ext.LoadMask(map.div, {
                msg: OpenLayers.Lang.translate('loadLayer.loadingMessage')});
            layerLoadingMask.show();

            layers = layerList;

            var params = {'service': 'WMS', 'request': 'GetCapabilities',
                'version': GeoNetwork.OGCUtil.getProtocolVersion(), language: GeoNetwork.OGCUtil.getLanguage()};
            var paramString = OpenLayers.Util.getParameterString(params);
            var separator = (onlineResource.indexOf('?') > -1) ? '&' : '?';
            onlineResource += separator + paramString;

            var req = Ext.Ajax.request({
                url: onlineResource,
                method: 'GET',
                //params: {url: onlineResource},
                success: processLayersSuccess,
                failure: processLayersFailure,
                timeout: 10000
            });
        }

    };
};

GeoNetwork.CatalogueInterface = new GeoNetwork.CatalogueInterface();
