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
 * Class: GeoNetwork.Control.ExtentBox
 * The ExtentBox control enables the user to define an extent 
 * which is to be used in GeoNetwork searches, by drawing a box on the map.
 *
 * Inherits from:
 *  - <OpenLayers.Control>
 */
GeoNetwork.Control.ExtentBox = OpenLayers.Class(OpenLayers.Control, {

    /**
     * Property: type
     * {OpenLayers.Control.TYPE}
     */
    type: OpenLayers.Control.TYPE_TOOL,

    /** 
     * Property: wktelement
     * {Ext.Element} Element in search form for WKT geometry
     */
    wktelement: null,
    
    /** 
     * Property: minxelement
     * {Ext.Element} Element in search form for minX-coordinate
     */
    minxelement: null,

    /**
     * Property: minyelement
     * {Ext.Element} Element in search form for minY-coordinate
     */
    minyelement: null,

    /**
     * Property: maxxelement
     * {Ext.Element} Element in search form for maxX-coordinate
     */
    maxxelement: null,

    /**
     * Property: maxyelement
     * {Ext.Element} Element in search form for maxY-coordinate
     */
    maxyelement: null,
    
    /**
     * Constant: EVENT_TYPES
     *
     * Supported event types:
     * finishBox - Triggered when a the extent box is finished
     */
    EVENT_TYPES: ["finishBox"],

    /**
     * Constructor: GeoNetwork.Control.ExtentBox
     * Create a new extent box control
     * 
     * Parameters:
     * options - {Object} An optional object whose properties will be set on
     *     the control
     */
    initialize: function(options) {
        // concatenate events specific to vector with those from the base
        this.EVENT_TYPES =
            GeoNetwork.Control.ExtentBox.prototype.EVENT_TYPES.concat(
            OpenLayers.Control.prototype.EVENT_TYPES
        );
        OpenLayers.Control.prototype.initialize.apply(this, arguments);
        this.handler = new OpenLayers.Handler.RegularPolygon(this,
            {create: this.startBox, done: this.endBox},
            {
                irregular: true,
                // fix for misplaced drawn feature:
                down: function(evt) {
                      this.map.events.clearMouseCache();
                      evt.xy = this.map.events.getMousePosition(evt); 
                      return OpenLayers.Handler.RegularPolygon.prototype.down.call(this, evt);
                }
            }
        );
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
    },

    /**
     * Method: startBox
     * Event handler for OpenLayers.Handler.RegularPolygon.create
     */  
    startBox: function() {
        this.getOrCreateLayer();
        this.vectorLayer.destroyFeatures();
    },
        
    /**
     * Method: endBox
     * Event handler for OpenLayers.Handler.RegularPolygon.done
     */  
    endBox: function() {
        var bounds = this.handler.feature.geometry.getBounds();
        this.updateFields(bounds);
        this.events.triggerEvent("finishBox", null);
    },
    updateFieldsWKT: function(wkt) {
      // Reproject WGS extent to map projection
      // Before adding to the map
      var geom = OpenLayers.Geometry.fromWKT(wkt);
      var mapProj = this.map.getProjectionObject();
      var wgs84 = new OpenLayers.Projection("WGS84");
      geom = geom.transform(wgs84, mapProj)
      this.updateFields(geom.getBounds());
    },
    updateFields: function(bounds) {
        var polFeature = new OpenLayers.Feature.Vector(
            bounds.toGeometry(), null, this.vectorLayerStyle);
        this.vectorLayer.addFeatures([polFeature]);
        this.vectorLayer.refresh();

        var mapProj = this.map.getProjectionObject();
        var wgs84 = new OpenLayers.Projection("WGS84");
            
        var minxy = new OpenLayers.LonLat(bounds.left, bounds.bottom).transform(mapProj, wgs84);
        var maxxy = new OpenLayers.LonLat(bounds.right, bounds.top).transform(mapProj, wgs84);
        
        var minx = minxy.lon.toFixed(4), miny = minxy.lat.toFixed(4), 
            maxx  = maxxy.lon.toFixed(4), maxy =  maxxy.lat.toFixed(4); 
        // Update form elements
        if (this.wktelement) {
            this.wktelement.setValue('POLYGON((' + minx + ' ' + miny + ','
                    + minx + ' ' + maxy + ',' + maxx + ' '
                    + maxy + ',' + maxx + ' ' + miny + ','
                    + minx + ' ' + miny + '))');
        } else {
            if (this.minxelement) {
                this.minxelement.dom.value = minx;
            }
            if (this.maxxelement) {
                this.maxxelement.dom.value = maxx;
            }
            if (this.minyelement) {
                this.minyelement.dom.value = miny;
            }
            if (this.maxyelement) {
                this.maxyelement.dom.value = maxy;
            }
        }
    },
    /**
     * APIMethod: updateMap
     * Update the extent box when the form fields are changed
     */ 
    updateMap: function() {
        if ((!this.minxelement) || (!this.maxxelement) ||
            (!this.minyelement) || (!this.maxyelement)) {
                return;
        }

        this.getOrCreateLayer();
        
        var mapProj = this.map.getProjectionObject();
        var wgs84 = new OpenLayers.Projection("WGS84");
                
        var minMapxy = new OpenLayers.LonLat(this.map.getExtent().left,
            this.map.getExtent().bottom).transform(mapProj, wgs84);
        var maxMapxy = new OpenLayers.LonLat(this.map.getExtent().right, 
            this.map.getExtent().top).transform(mapProj, wgs84);

        // Validate extent values
        var aux = parseFloat(this.minxelement.dom.value);
        if (isNaN(aux)) {
            this.minxelement.dom.value = minMapxy.lon;
        }
        /*if (this.minxelement.dom.value < minMapxy.lon) {
            this.minxelement.dom.value = minMapxy.lon;
        }*/
                                
        aux = parseFloat(this.maxxelement.dom.value);
        if (isNaN(aux)) {
            this.maxxelement.dom.value = maxMapxy.lon;
        }
        /*if (this.maxxelement.dom.value > maxMapxy.lon) {
            this.maxxelement.dom.value = maxMapxy.lon;
        }
        if (this.maxxelement.dom.value < this.minxelement.dom.value) {
            this.maxxelement.dom.value = maxMapxy.lon;
        }*/
                                
        aux = parseFloat(this.minyelement.dom.value);
        if (isNaN(aux)) {
            this.minyelement.dom.value = minMapxy.lat;
        }
        /*if (this.minyelement.dom.value < minMapxy.lat) {
            this.minyelement.dom.value = minMapxy.lat;
        }*/
                
        aux = parseFloat(this.maxyelement.dom.value);
        if (isNaN(aux)) {
            this.maxyelement.dom.value = maxMapxy.lat;
        }
        /*if (this.maxyelement.dom.value < maxMapxy.lat) {
            this.maxyelement.dom.value = maxMapxy.lat;
        }
        if (this.maxyelement.dom.value < this.minyelement.dom.value) {
            this.maxyelement.dom.value = maxMapxy.lat;
        }*/
            
        // Format numbers
        this.minxelement.dom.value = parseFloat(this.minxelement.dom.value).toFixed(4);
        this.maxxelement.dom.value = parseFloat(this.maxxelement.dom.value).toFixed(4);
        this.minyelement.dom.value = parseFloat(this.minyelement.dom.value).toFixed(4);
        this.maxyelement.dom.value = parseFloat(this.maxyelement.dom.value).toFixed(4);
                
        this.vectorLayer.destroyFeatures();

        // Create new bounding box
        var minxy = new OpenLayers.LonLat(this.minxelement.dom.value, 
            this.minyelement.dom.value).transform(wgs84, mapProj);
        var maxxy = new OpenLayers.LonLat(this.maxxelement.dom.value, 
            this.maxyelement.dom.value).transform(wgs84, mapProj);

        var bounds = new OpenLayers.Bounds();
        bounds.extend(minxy);
        bounds.extend(maxxy);

        var polFeature = new OpenLayers.Feature.Vector(
            bounds.toGeometry(), null, this.vectorLayerStyle);

        this.vectorLayer.addFeatures([polFeature]);

        this.vectorLayer.refresh();
    },

    /**
     * Function: getOrCreateLayer
     * Returns the layer for extent box. It is created if null.
     * The layer is not created in setMap to prevent a problem in IE
     * that fails renderer assignment if page is not fully loaded
     * (VML.js: supported method fails)
     *
     * Returns:
     * {<OpenLayers.Layer.Vector>} The vector layer used for drawing.
     */
    getOrCreateLayer: function() {
        if (!this.vectorLayer) {
            this.vectorLayer = this.vectorLayer || new OpenLayers.Layer.Vector(
                "ExtentBox", {
                style: this.vectorLayerStyle
            });
            this.map.addLayer(this.vectorLayer);
        }
        this.map.setLayerIndex(this.vectorLayer, this.map.getNumLayers());
        return this.vectorLayer;
    },

    /**
     * Method: clear
     * Clears the vector layer containing the extent box.
     */
    clear: function() {
        if (this.vectorLayer) {
            this.vectorLayer.destroyFeatures();
        }
    },

    zoomTo: function() {
        var mapProj = this.map.getProjectionObject();
        var wgs84 = new OpenLayers.Projection("WGS84");

        // Create new bounding box
        var minxy = new OpenLayers.LonLat(this.minxelement.dom.value, 
            this.minyelement.dom.value).transform(wgs84, mapProj);
        var maxxy = new OpenLayers.LonLat(this.maxxelement.dom.value, 
            this.maxyelement.dom.value).transform(wgs84, mapProj);

        var bounds = new OpenLayers.Bounds();
        bounds.extend(minxy);
        bounds.extend(maxxy);
        
        this.map.zoomToExtent(bounds);
    },
    
    CLASS_NAME: "GeoNetwork.Control.ExtentBox"
});