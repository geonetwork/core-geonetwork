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

if (!window.GeoNetwork) {
    window.GeoNetwork = {};
}
if (!GeoNetwork.Control) {
    GeoNetwork.Control = {};
}

/**
 * Class: GeoNetwork.Control.WMSGetFeatureInfo
 * The WMS GetFeatureInfo control can be used to retrieve attribute information
 * for several WMS layers in a map.
 * It will query all visible and queryable WMS layers in the map.
 * In the future this class could be replaced by the default 
 * OpenLayers.Control.WMSGetFeatureInfo, but then ticket:2091 needs to be
 * resolved first [http://trac.openlayers.org/ticket/2091].
 *
 * Inherits:
 *  - <OpenLayers.Control>
 */
GeoNetwork.Control.WMSGetFeatureInfo = OpenLayers.Class(OpenLayers.Control, {

    /**
     * APIProperty: showMarker
     * {Boolean} Should we show a marker where the user clicked in the map?
     *     There will only be one marker at a time. Default is true.
     */
    showMarker: true,

    /**
     * APIProperty: markerIcon
     * {<OpenLayers.Icon>} the icon to use for the featureinfo marker
     */
    markerIcon: OpenLayers.Marker.defaultIcon(),

    /**
     * Constant: EVENT_TYPES
     *
     * Supported event types:
     * - *featureinfostart* Triggered when the user clicks in the map
     * - *featureinfoend* Triggered when all WMS GetFeatureInfo responses are in
     */ 
    EVENT_TYPES: ['featureinfostart', 'featureinfoend'],

    /**
     * Property: callbacks
     * {Object} The functions that are sent to the click handler for callback
     */
    callbacks: null,

    /**
     * Property: markerLayer
     * {<OpenLayers.Layer.Markers>} The marker layer in which the feature info
     *     marker will be drawn.
     */
    markerLayer: null,

    /**
     * Property: location
     * {<OpenLayers.LonLat>} The real-world location where the user clicked
     */
    location: null,

    /**
     * Property: feature
     * {<OpenLayers.Feature>} The feature from which the popup is created
     */
    feature: null,

    /**
     * Property; counter
     * {Integer} Counter is used to see how many of the WMS GetFeatureInfo 
     *     responses have been retrieved
    */
    counter: null,
    
    /**
     * Property; format
     * {<OpenLayers.Format>} The XML parser for GetFeatureInfo responses
    */
    format: new OpenLayers.Format.WMSGetFeatureInfo(),

    /**
     * Constructor: GeoNetwork.Control.WMSGetFeatureInfo
     * Create a new feature info control
     * 
     * Parameters:
     * options - {Object} An optional object whose properties will be set on
     *     the control
     */
    initialize: function(options) {
        this.EVENT_TYPES =
            GeoNetwork.Control.WMSGetFeatureInfo.prototype.EVENT_TYPES.concat(
                OpenLayers.Control.prototype.EVENT_TYPES); 

        OpenLayers.Control.prototype.initialize.apply(this, [options]);

        this.handler = new OpenLayers.Handler.Click(this, 
            OpenLayers.Util.extend({click: this.click}, this.callbacks));
    },

    /** 
     * Function: destroy
     * Destroy control.
     */
    destroy: function() {
        if (this.markerLayer) {
            this.markerLayer.destroy();
            this.markerLayer = null;
            if (this.feature) {
                this.feature.destroy();
            }
        }
        OpenLayers.Control.prototype.destroy.apply(this, arguments);
    },     

    /**
     * Method: performRequest
     * Build up the url for the WMS GetFeatureInfo request and retrieve it.
     * 
     * Parameters:
     * url - {String} the url of the service
     * queryLayers - {String} the comma separated list of wms layers to query
     * evt - {Event} The event object from the click event.
     */
    performRequest: function(url, queryLayers, evt) {
        var params = {
            REQUEST: 'GetFeatureInfo',
            EXCEPTIONS: 'application/vnd.ogc.se_xml',
            BBOX: this.map.getExtent().toBBOX(),
            X: evt.xy.x,
            Y: evt.xy.y,
            INFO_FORMAT: 'application/vnd.ogc.gml',
            QUERY_LAYERS: queryLayers,
            LAYERS: queryLayers,
            WIDTH: this.map.size.w,
            HEIGHT: this.map.size.h,
            VERSION: '1.1.1',
            SERVICE: 'WMS',
            SRS: this.map.getProjection()
        };
        url = GeoNetwork.OGCUtil.ensureProperUrlEnd(url) + OpenLayers.Util.getParameterString(params);
        OpenLayers.Request.GET({
            url: url,
            success: this.returnResponse,
            scope: this
        }); 
    },

    /**
     * Method: click
     * Capture the click event
     *
     * Parameters:
     * evt - {Event} The event object from the click event.
     */
    click: function (evt) {
        this.events.triggerEvent('featureinfostart');
        var layer;
        this.counter = 0;
        this.start();
        this.location =  this.map.getLonLatFromPixel(evt.xy);
        for (var i=0, len=this.map.layers.length;i<len;i++) {
            layer = this.map.layers[i];
            if (layer instanceof OpenLayers.Layer.WMS) {
                // issue a GetFeatureInfo request to all visible and 
                // queryable WMS layers
                if (layer.visibility && layer.queryable) {
                    this.addToRequestQueue(layer);
                }
            }
        }
        for (var j=0; j<this.queue.length; j++) {
            this.performRequest(this.queue[j].url,
                this.queue[j].layers.join(","), evt);
        }
        if (this.queue.length === 0) {
            // remove busy cursor
            OpenLayers.Element.removeClass(this.map.viewPortDiv, "olCursorWait"); 
        }
    },

    /**
     * Function: getLayerTitle
     * Look up the title of a WMS layer name
     *
     * Parameters:
     * name - {String} the name of the layer (used in the LAYERS parameter)
     *
     * Returns:
     * {String} The layer title
     */
    getLayerTitle: function(name) {
        for (var i=0, len=this.layerTitles.length; i<len; i++) {
            if (this.layerTitles[i].name === name) {
                return this.layerTitles[i].title;
            }
        }
    },

    /**
     * Method: addToRequestQueue
     * Add a layer to the request queue. Will group layers from the same
     * WMS to provide more efficiency.
     *
     * Parameters:
     * layer - {<OpenLayers.Layer.WMS>} The layer to be added.
     */
    addToRequestQueue: function(layer) {
        var arr = layer.params.LAYERS.split(",");
        var i, len;
        for (i=0, len=arr.length; i<len; i++) {
            this.layerTitles.push({name: arr[i], title: layer.name});
        }
        var url = layer.url;
        var found = false;
        var layers = layer.params.LAYERS;
        for (i=0, len=this.queue.length; i < len; i++) {
            var obj = this.queue[i];
            if (obj.url === url) {
                found = true;
                arr = layers.split(",");
                for (var j=0, lenj=arr.length; j<lenj; j++) {
                    obj.layers.push(arr[j]);
                }
            }
        }
        if (!found) {
            arr = layers.split(",");
            this.queue.push({url: url, layers: arr});
        }
    },

    /**
     * Method: returnResponse
     * Return the WMS GetFeatureInfo response and start up the processing
     *
     * Parameters:
     * response - {XMLHttpRequest}
    */
    returnResponse: function(response) {
        this.counter++;        
        this.info(response);
        if (this.counter === this.queue.length) {
            this.end();
        }
    },

    /**
     * Method: start
     * User clicked in the map, start up the processing.
     */
    start: function() {
        // add busy cursor
        OpenLayers.Element.addClass(this.map.viewPortDiv, "olCursorWait"); 
        this.featurelist = [];
        this.queue = [];
        this.layerTitles = [];
    },

    /**
     * Method: end
     * All GetFeatureInfo responses have been received
     */
    end: function() {
        if (this.showMarker) {
            // raise the markerLayer to the top of the map
            var idx = -1;
            for (var i=0, len = this.map.layers.length; i<len; i++) {
                var layer = this.map.layers[i];
                if (layer != this.markerLayer) {
                    idx = Math.max(this.map.getLayerIndex(
                        this.map.layers[i]), idx);
                }
            }
            if (this.map.getLayerIndex(this.markerLayer) < idx) {
                this.map.setLayerIndex(this.markerLayer, idx+1);
            }
            this.feature = new OpenLayers.Feature(this.markerLayer, 
                this.location, {icon: this.markerIcon});

            var marker = this.feature.createMarker();
            this.markerLayer.clearMarkers();
            this.markerLayer.addMarker(marker);
        }
        this.events.triggerEvent('featureinfoend', {
            featurelist: this.featurelist
        });
        // remove busy cursor when done
        OpenLayers.Element.removeClass(this.map.viewPortDiv, "olCursorWait"); 
    },

    /** 
     * Method: info
     * Handle the response retrieved from the WMS, add items to the featurelist
     *
     * Parameters:
     * response - {XMLHttpRequest} the response received
     */
    info: function(response) {
        var features = this.format.read(response.responseXML || 
            response.responseText);
        for (var i=0, len=features.length; i<len; i++) {
            var feature = features[i];
            var obj = {title: this.getLayerTitle(feature.type), features: [feature]};
            this.featurelist.push(obj);
        }
    },

    /**
     * Method: setMap
     * Attach a map object to this control.
     * 
     * Parameters:
     * map - {<OpenLayers.Map>}  The map object
     */
    setMap: function(map) {
        OpenLayers.Control.prototype.setMap.apply(this, arguments);
        if (this.showMarker) {
            this.markerLayer = new OpenLayers.Layer.Markers('featureinfo', 
                {displayInLayerSwitcher: false});
            this.map.addLayer(this.markerLayer);
        }
    },

    /**
     * Function: deactivate
     * Deactivate the control
     *
     * Returns: {Boolean} True if the control was effectively deactivated or
     *     false if the control was already inactive.
     */
    deactivate: function() {
        if (this.markerLayer) {
            this.markerLayer.clearMarkers();
        } 
        return OpenLayers.Control.prototype.deactivate.apply(this,arguments);
    },    

    CLASS_NAME: "GeoNetwork.Control.WMSGetFeatureInfo"

});
