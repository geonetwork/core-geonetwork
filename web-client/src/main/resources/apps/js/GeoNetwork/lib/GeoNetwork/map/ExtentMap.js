/*
 * Copyright (C) 2001-2011 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * 
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
Ext.namespace('GeoNetwork.map');

/** api: (define)
 *  module = GeoNetwork.map
 *  class = ExtentMap
 */
/** api: constructor 
 *  .. class:: ExtentMap()
 *
 *  ExtentMap is a utility class used for map initialization in metadata view and edit mode.
 *
 *  Search for all maps defined in the current page and create a map 
 *  with bounding box in view mode and add editing tools when needed.
 *  
 *  Editing tools could be draw rectangle, draw polygon and draw circle.
 *
 */
/**
 * @include GeoExt/widgets/MapPanel.js
 * @include GeoExt/widgets/Action.js
 * @include OpenLayers/Control/DrawFeature.js
 * @include OpenLayers/Control/Navigation.js
 * @include OpenLayers/Geometry.js
 * @include OpenLayers/Feature/Vector.js
 * @include OpenLayers/Handler/RegularPolygon.js
 * @include OpenLayers/Handler/Polygon.js
 * @include OpenLayers/Control/DrawFeature.js
 * @include OpenLayers/Projection.js
 * @include OpenLayers/Format/GML.js
 * @include OpenLayers/Format/XML.js
 * @include OpenLayers/Map.js
 * @include OpenLayers/Control/PanZoom.js
 * @include OpenLayers/Control/MousePosition.js
 * @include OpenLayers/Layer/WMS.js
 * @include OpenLayers/Layer/Vector.js
 * @include OpenLayers/BaseTypes/Bounds.js
 * @include OpenLayers/Format.js
 */
GeoNetwork.map.ExtentMap = function(){
    var maps = [];
    var vectorLayers = [];
    var vectorLayerStyle = OpenLayers.Feature.Vector.style['default'];
    var targetPolygon = null;
    var watchedBbox = null;
    var mode = null;
    var edit = null;
    var eltRef = null;
    var digits = 5; // FIXME : how many decimals is fine ? This should be define according to the projection ?
    /**
     * A class for a geometry referenced by its ID
     */
    var MultiPolygonReference = OpenLayers.Class(OpenLayers.Geometry, {
        id: null,
        
        initialize: function(id){
            this.id = id;
        },
        
        CLASS_NAME: "GeoNetwork.map.ExtentMap.MultiPolygonReference"
    });
    
    /**
     * Define the map projection. Bounding box are stored in WGS84 in metadata records
     * and polygon also.
     */
    var mainProjCode = GeoNetwork.map.PROJECTION || "EPSG:4326";
    var wgsProjCode = "EPSG:4326";
    var wgsProj = new OpenLayers.Projection(wgsProjCode);
    var mainProj = null;
    var units = 'm'; //degrees
    var alternateProj = null;
    
    
    
    /**
     * Convert a feature to a GML geometry.
     */
    function convertToGml(feature, proj){
        var mainProj = new OpenLayers.Projection(proj);
        var writer = (mainProj === wgsProj ? new OpenLayers.Format.GML.v3() : new OpenLayers.Format.GML.v3({
            externalProjection: wgsProj,
            internalProjection: mainProj
        }));
        var child = writer.writeNode("feature:_geometry", feature.geometry);
        // FIXME : Here we should add the gml:id and srsName attribute
        // to avoid invalid document.
        return OpenLayers.Format.XML.prototype.write.call(writer, child.firstChild);
    }
    /**
     * Create default map with one vector layer for drawing.
     *
     */
    function createMap(){
        //        OpenLayers.ImgPath = "../../scripts/openlayers/img/";
        var i, 
            options = {
                units: units,
                projection: mainProjCode,
                theme: null
            },
            map = new OpenLayers.Map(options);
        
        
        
        // Disable mouse wheel and navigation toolbar in view mode.
        // User can still pan the map.
        if (!edit) {
            var navigationControl = map.getControlsByClass('OpenLayers.Control.Navigation')[0];
            navigationControl.disableZoomWheel();
            map.removeControl(map.getControlsByClass('OpenLayers.Control.PanZoom')[0]);
        }
        
        // Add mouse position control to display coordintate.
        map.addControl(new OpenLayers.Control.MousePosition());
        
        // Add layers.
        for (i = 0; i < GeoNetwork.map.BACKGROUND_LAYERS.length; i++) {
            map.addLayer(GeoNetwork.map.BACKGROUND_LAYERS[i].clone());
        }
        
        
        // Add vector layer to draw features (ie. bbox or polygon)
        var vectorLayer = new OpenLayers.Layer.Vector("VectorLayer", {            //displayOutsideMaxExtent: true,
            //alwaysInRange: true,
            //displayInLayerSwitcher: false,
            //tyle: GeoNetwork.map.ExtentMap.vectorLayerStyle, // TODO add as option
        });
        map.addLayer(vectorLayer);
        
        
        // Clean existing features before drawing
        vectorLayer.events.on({
            "sketchstarted": function(){
                this.destroyFeatures();
            },
            scope: vectorLayer
        });
        
        maps[eltRef] = map;
        vectorLayers[eltRef] = vectorLayer;
        return map;
    }
    
    /**
     * Change bbox when bounding box input text fields are updated.
     * Coordinates input are composed of:
     * * one hidden field with metadata coordinates (id starts with "_")
     * * one field with the coordinates reprojected according to radio projection selector.
     * 
     */
    function watchBbox(vectorLayer, watchedBbox, eltRef, map){
        var wsen = watchedBbox.split(','), 
            i;
        
        for (i = 0; i < wsen.length; ++i) {
            // register a "change" listen on each input text element
            Ext.get(wsen[i]).on('change', function(){
                // update the value of the corresponding input hidden elements
                // and update the box drawn on the map
                updateBbox(map, watchedBbox, eltRef, false);
            });
            
            // register a "change" listen on each input text element
            Ext.get("_" + wsen[i]).on('change', function(){
                updateBbox(map, watchedBbox, eltRef, true);
            });
        }
    }
    
    /**
     * mainProj: indicates if coordinate are read from the display coordinate field or from the hidden fields.
     */
    function updateBbox(map, targetBbox, eltRef, mainProj){
    	var vectorLayer = map.getLayersByName("VectorLayer")[0]; // That supposed that only one vector layer is on the map
        var bounds, values, boundsForMap;
        var wsen = targetBbox.split(',');
        values = [];
        
        // Update bbox from main projection information (from hidden fields which are always in WGS84)
        if (mainProj) {
            values[0] = Ext.get("_" + wsen[0]).getValue();
            values[1] = Ext.get("_" + wsen[1]).getValue();
            values[2] = Ext.get("_" + wsen[2]).getValue();
            values[3] = Ext.get("_" + wsen[3]).getValue();
            
            bounds = OpenLayers.Bounds.fromArray(values);
            boundsForMap = bounds.clone();
            
            // reproject if another projection is used
            if (mainProjCode !== wgsProj) {
            	boundsForMap.transform(new OpenLayers.Projection(wgsProjCode), new OpenLayers.Projection(mainProjCode));
            }
        } else {
        	// Update bounding box from input fields which
        	// may be in different projection.
            values[0] = Ext.get(wsen[0]).getValue();
            values[1] = Ext.get(wsen[1]).getValue();
            values[2] = Ext.get(wsen[2]).getValue();
            values[3] = Ext.get(wsen[3]).getValue();
            bounds = OpenLayers.Bounds.fromArray(values);
            boundsForMap = bounds.clone();
            
            // Loop for projection selectors value
            var toProj = null;
            var radio = document.getElementsByName("proj_" + eltRef);
            for (i = 0; i < radio.length; i++) {
                if (radio[i].checked === true) {
                    toProj = radio[i].value;
                }
            }
            
            // Reproject bounds to map projection if needed
            if (toProj !== mainProjCode) {
            	boundsForMap.transform(new OpenLayers.Projection(toProj), new OpenLayers.Projection(mainProjCode));
            }
            
            // Reproject coordinates to WGS84 to set lat long in coordinates hidden inputs
            if (toProj !== wgsProjCode) {
                bounds.transform(new OpenLayers.Projection(toProj), new OpenLayers.Projection(wgsProjCode));
            } 
            // Set main projection coordinates
            Ext.get("_" + wsen[0]).dom.value = bounds.left;
            Ext.get("_" + wsen[1]).dom.value = bounds.bottom;
            Ext.get("_" + wsen[2]).dom.value = bounds.right;
            Ext.get("_" + wsen[3]).dom.value = bounds.top;
           
        }
        
        // Validate fields content
        Ext.get(wsen[0]).dom.onkeyup();
        Ext.get(wsen[1]).dom.onkeyup();
        Ext.get(wsen[2]).dom.onkeyup();
        Ext.get(wsen[3]).dom.onkeyup();
        
        // Draw new bounds
        var feature = new OpenLayers.Feature.Vector(boundsForMap.toGeometry());
        vectorLayer.destroyFeatures();
        vectorLayer.addFeatures(feature);
        
        zoomToFeatures(maps[eltRef], vectorLayers[eltRef]);
        
        // Update all inputs
        watchRadios(targetBbox, eltRef);
        
    }
    
    /**
     * Change projection when radio button change
     */
    function watchRadios(watchedBbox, eltRef){
        function updateInputTextFields(watchedBbox, toProj, digits){
            var wsen = watchedBbox.split(',');
            
            // Get WGS84 values
            var w = Ext.get("_" + wsen[0]).getValue();
            var s = Ext.get("_" + wsen[1]).getValue();
            var e = Ext.get("_" + wsen[2]).getValue();
            var n = Ext.get("_" + wsen[3]).getValue();
            
            var l = w !== "" ? w : "0";
            var b = s !== "" ? s : "0";
            var r = e !== "" ? e : "0";
            var t = n !== "" ? n : "0";
            
            var bounds = OpenLayers.Bounds.fromString(l + "," + b + "," + r + "," + t);
            
            if (!toProj.equals(wgsProj)) {
                bounds.transform(wgsProj, toProj);
            }
            if (w !== "") {
                w = bounds.left.toFixed(digits) + "";
            }
            Ext.get(wsen[0]).dom.value = w;
            if (s !== "") {
                s = bounds.bottom.toFixed(digits) + "";
            }
            Ext.get(wsen[1]).dom.value = s;
            if (e !== "") {
                e = bounds.right.toFixed(digits) + "";
            }
            Ext.get(wsen[2]).dom.value = e;
            if (n !== "") {
                n = bounds.top.toFixed(digits) + "";
            }
            Ext.get(wsen[3]).dom.value = n;
        }
        
        // Register onclick event for radio related to current map
        var inputs = Ext.DomQuery.select('input.proj'), idx;
        for (idx = 0; idx < inputs.length; ++idx) {
            var input = inputs[idx];
            if (input.id.indexOf(eltRef) !== -1) {
                // According to current selection, update coordinates
                if (input.checked) {
                    updateInputTextFields(watchedBbox, new OpenLayers.Projection(input.value), digits);
                }
                var e = Ext.get(input.id);
                e.on('click', function(){
                    updateInputTextFields(watchedBbox, new OpenLayers.Projection(this.value), digits);
                }, e.dom);
            }
        }
    }
    
    /**
     * Returns a string representing the feature geometry
     *
     * @param options parameters describing how to format the geoemtry
     *        options.from the current projection of the feature (either both 'from' and 'to' are defined or neither)
     *        options.to   the projection to reproject the feature
     *        options.format the format to write the geometry in 'WKT', 'GML', etc...  Default is WKT
     */
    function writeFeature(options){
        var format = 'WKT';
        var from, to;
        if (options !== null) {
            format = options.format || 'WKT';
            from = options.from;
            to = options.to;
        }
        
        var writer = new OpenLayers.Format[format]();
        if (from !== null && to !== null) {
            writer.internalProjection = from;
            writer.externalProjection = to;
        }
        if (!this.vectorLayer.features.length) {
            return null;
        }
        
        // Gets the last drawn feature
        var feature = this.vectorLayer.features[this.vectorLayer.features.length - 1];
        return writer.write(feature);
    }
    
    /**
     * @param string string containing the feature data
     * @param option parameters describing how to format the geoemtry
     *        option.from the current projection of the feature (either both 'from' and 'to' are defined or neither)
     *        option.to   the projection to reproject the feature
     *        option.format the format to write the geometry in 'WKT', 'GML', etc...  Default is WKT
     *        option.zoomToFeatures boolean, if true, zooms to feature(s) extent
     */
    function readFeature(string, options, vectorLayer, map){
        // An empty geometry.
        if (string === '') {
            return false;
        }
        
        var format = 'WKT';
        var from, to;
        if (options !== null) {
            format = options.format || 'WKT';
            from = options.from;
            to = options.to;
        }
        var reader = new OpenLayers.Format[format]();
        
        if (from !== null && to !== null) {
            reader.externalProjection = from;
            reader.internalProjection = to;
        }
        
        // FIXME : When creating WKT, cariage return are added to the string.
        string = string.replace(/\n/g, '');

        var feature = reader.read(string);

        // reader is subject to returning an object or an array depending on the format
        if (!feature) {
            return false;
        }
        
        if (feature.length) {
            feature = feature[0];
        }
        
        vectorLayer.addFeatures(feature);
        // optionally, zoom on the layer features extent
        if (options.zoomToFeatures) {
            zoomToFeatures(map, vectorLayer);
        }
        return true;
    }
    
    // Regions extents are in WGS84
    function updateBboxForRegion(map, targetBbox, eltRef){
        var vectorLayer = map.getLayersByName("VectorLayer")[0]; // That supposed that only one vector layer is on the map
        // In map projection
        var bounds;
        var wsen = targetBbox.split(',');
        
        // Update bbox from main projection information (values from regions extents in WGS84)
        var values = [];
        values[0] = Ext.get("_" + wsen[0]).getValue();
        values[1] = Ext.get("_" + wsen[1]).getValue();
        values[2] = Ext.get("_" + wsen[2]).getValue();
        values[3] = Ext.get("_" + wsen[3]).getValue();
        bounds = OpenLayers.Bounds.fromArray(values);
        
        // Bounds in map projection to draw rectangle
        bounds.transform(wgsProj, mainProj);
        
        // Validate fields content
        Ext.get(wsen[0]).dom.onkeyup();
        Ext.get(wsen[1]).dom.onkeyup();
        Ext.get(wsen[2]).dom.onkeyup();
        Ext.get(wsen[3]).dom.onkeyup();
        
        // Draw new bounds
        var feature = new OpenLayers.Feature.Vector(bounds.toGeometry());
        vectorLayer.destroyFeatures();
        vectorLayer.addFeatures(feature);
        
        zoomToFeatures(map, vectorLayer);
        
        // Update all inputs
        watchRadios(targetBbox, eltRef);
        
    }
    
    
    function createRegionMenu(cb){
        var store = GeoNetwork.data.RegionStore(catalogue.services.getRegions); // FIXME : global var
        var combo = new Ext.form.ComboBox({
            store: store,
            //displayField: "labels['en']",
            tpl: '<tpl for="."><div class="x-combo-list-item">{[values.label[\'' + GeoNetwork.Util.getCatalogueLang(OpenLayers.Lang.getCode()) + '\']]}</div></tpl>',// TODO if language code does not exist in labels field store
            typeAhead: true,
            mode: 'local',
            triggerAction: 'all',
            emptyText: OpenLayers.i18n('selectRegion'),
            selectOnFocus: true,
            width: 135,
            listeners: {
                focus: {
                    fn: function(el){
                        el.getStore().load();
                    }
                },
                select: {
                    fn: cb
                }
            },
            iconCls: 'no-icon'
        });
        //store.load();
        return combo;
    }
    
    function zoomToFeatures(map, vectorLayer){
        var extent = vectorLayer.getDataExtent();
        if (extent && !isNaN(extent.left)) {
            var width = extent.getWidth() / 2;
            var height = extent.getHeight() / 2;
            extent.left -= width;
            extent.right += width;
            extent.bottom -= height;
            extent.top += height;
            map.zoomToExtent(extent);
        } else {
            map.zoomToMaxExtent();
        }
    }
    
    return {
        init: function(){
        },
        /** api: method[initMapDiv]
         *
         * Take all the DIVs of class extentViewer and places a map within. If it
         * contains a single div, look in it content for a geometry in WKT format
         * and add it in the map.
         *
         * The DIV can have some attributes:
         *   - edit: if 'true', add edition tools
         *   - target_polygon: the id of the input that must be updated with the GML
         *                     content of the polygon being edited
         *   - watched_bbox: the coma separated 4 ids of the input field (east, south,
         *                   west, north) to listen for modifications
         */
        initMapDiv: function(){
            var viewers, 
                idFunc, 
                idx, 
                descRef,
                i;
            mainProj = new OpenLayers.Projection(mainProjCode);
            alternateProj = mainProj;
            
            
            viewers = Ext.DomQuery.select('.extentViewer');
            idFunc = Ext.id;
            
            for (idx = 0; idx < viewers.length; ++idx) {
                var viewer = viewers[idx];
                targetPolygon = viewer.getAttribute("target_polygon");
                watchedBbox = viewer.getAttribute("watched_bbox");
                edit = viewer.getAttribute("edit") === 'true';
                eltRef = viewer.getAttribute("elt_ref");
                descRef = viewer.getAttribute("desc_ref");
                mode = viewer.getAttribute("mode");
                
                var children = viewer.childNodes;
                var tmp = [];
                
                for (i = 0; i < children.length; i++) {
                    if (children[i].nodeType === 1) {
                        tmp.push(children[i]);
                    }
                }
                
                children = tmp;
                
                // If already initialized
                if (children.length > 1) {
                    continue;
                }
                
                // Creates map component
                var id;
                id = Ext.id(viewer);
                
                var map = createMap();
                
                // Create toolbar with:
                // * polygon control
                // * bbox control
                // * choose existing polygon
                // * clear
                // * rollback
                if (edit) {
                    var tbarItems = [];
                    
                    
                    // Bbox drawing control
                    if (mode === 'bbox') {
                        var control = new OpenLayers.Control.DrawFeature(vectorLayers[eltRef], OpenLayers.Handler.RegularPolygon, {
                            handlerOptions: {
                                irregular: true,
                                sides: 4
                            },
                            featureAdded: function(feature){
                                // a box was drawn, update the input text and input
                                // hidden fields
                                var bounds = feature.geometry.getBounds(), boundsReproj;
                                // If current map projection is not WGS84, reproject bounds
                                // coordinate to store WGS84 in metadata record.
                                
                                if (mainProj !== wgsProj) {
                                    boundsReproj = bounds.clone().transform(mainProj, wgsProj);
                                } else {
                                    boundsReproj = bounds;
                                }
                                
                                var wsen = this.watchedBbox.split(','); // Here we don't round coordinates to store full value
                                Ext.get("_" + wsen[0]).dom.value = boundsReproj.left;
                                Ext.get("_" + wsen[1]).dom.value = boundsReproj.bottom;
                                Ext.get("_" + wsen[2]).dom.value = boundsReproj.right;
                                Ext.get("_" + wsen[3]).dom.value = boundsReproj.top;
                                
                                // Refresh all inputs
                                watchRadios(this.watchedBbox, this.eltRef);
                                
                            }.bind({
                                watchedBbox: watchedBbox,
                                eltRef: eltRef
                            })
                        });
                        tbarItems.push(new GeoExt.Action({
                            map: maps[eltRef],
                            control: control,
                            text: OpenLayers.i18n('drawRectangle'),
                            pressed: false,
                            allowDepress: true,
                            toggleGroup: "tool",
                            iconCls: "drawRectangle"
                        }));
                        
                        
                        tbarItems.push(createRegionMenu(function(c, r, idx){
                            var wsen = this.watchedBbox.split(','); // Here we don't round coordinates to store full value
                            Ext.get("_" + wsen[0]).dom.value = r.data.west;
                            Ext.get("_" + wsen[1]).dom.value = r.data.south;
                            Ext.get("_" + wsen[2]).dom.value = r.data.east;
                            Ext.get("_" + wsen[3]).dom.value = r.data.north;
                            updateBboxForRegion(maps[eltRef], watchedBbox, eltRef, true); // Region are in WGS84
                            if (Ext.get("_" + this.descRef) !== null) {
                                Ext.get("_" + this.descRef).dom.value = r.data.label[GeoNetwork.Util.getCatalogueLang(OpenLayers.Lang.getCode())];
                            }
                        }.bind({
                            extentMap: this,
                            map: maps[eltRef],
                            watchedBbox: watchedBbox,
                            eltRef: eltRef,
                            descRef: descRef
                        })));
                    }
                    // Polygon drawing control 
                    else if (mode === 'polygon') {
                        var polyControl = new OpenLayers.Control.DrawFeature(vectorLayers[eltRef], OpenLayers.Handler.Polygon, {
                            featureAdded: function(feature){
                                // Update form input
                                document.getElementById('_X' + this).value = convertToGml(feature, mainProjCode);
                                polyControl.deactivate();
                            }.bind(targetPolygon)
                        });
                        
                        tbarItems.push(new GeoExt.Action({
                            map: maps[eltRef],
                            control: polyControl,
                            text: OpenLayers.i18n('drawPolygon'),
                            tooltip: OpenLayers.i18n('drawPolygonTT'),
                            pressed: false,
                            allowDepress: true,
                            toggleGroup: "tool",
                            iconCls: "drawPolygon"
                        }));
                        control = new OpenLayers.Control.DrawFeature(vectorLayers[eltRef], OpenLayers.Handler.RegularPolygon, {
                            handlerOptions: {
                                irregular: true,
                                sides: 60
                            },
                            featureAdded: function(feature){
                                // Update form input
                                document.getElementById('_X' + this).value = convertToGml(feature, mainProjCode);
                                control.deactivate();
                            }.bind(targetPolygon)
                        });
                        
                        tbarItems.push(new GeoExt.Action({
                            map: maps[eltRef],
                            control: control,
                            text: OpenLayers.i18n('drawCircle'),
                            pressed: false,
                            allowDepress: true,
                            toggleGroup: "tool",
                            iconCls: "drawCircle"
                        }));
                        
                        
                        //                        TODO : this button define a popup which allow to select a geographic
                        //                        feature from a WFS service (defined by serviceUrl parameter). This popup
                        //                        needs to add a dependcy to MapFish in order to work. The list of layers
                        //                        served by the WFS will be listed (using GetCapabilities info). Then clicking
                        //                        on the map select one feature to be used as bounding polygon.
                        //                        More test needed.
                        //                        
                        //                        tbarItems.push({    
                        //                            text: "Choose a geographic feature", // TODO : i18n
                        //                            tooltip: "",
                        //                            handler: function() {
                        //                               
                        //                               var featureSelectionPanel = new app.FeatureSelectionPanel({
                        //                                   serviceUrl: "/geoserver/wfs",
                        //                                   width: 700,
                        //                                   height: 350
                        //                               });
                        //
                        //                               var fsWin = new Ext.Window({
                        //                                   title: 'featureSelection',//translate('featureSelection'),
                        //                                   layout: 'fit',
                        //                                   modal: true,
                        //                                   items: featureSelectionPanel
                        //                               });
                        //                               fsWin.show();
                        //                               
                        //                               featureSelectionPanel.on('featureselected', function(panel, feature) {
                        //                                   fsWin.close();
                        //
                        //                                   if (this.selectionFeature) {
                        //                                       this.vectorLayer.destroyFeatures(this.selectionFeature);
                        //                                   }
                        //                                   this.selectionFeature = new OpenLayers.Feature.Vector(feature.geometry, {}, this.selectionStyle);
                        //                                   this.vectorLayer.addFeatures(this.selectionFeature);
                        //
                        //                                    document.getElementById('_X' + this.targetPolygon).value = GeoNetwork.map.ExtentMap.convertToGml(feature, GeoNetwork.map.ExtentMap.mainProjCode);
                        //                                   //this.map.zoomToExtent(this.selectionFeature.geometry.getBounds());
                        //                               }, this);
                        //                            },
                        //                            scope: {
                        //                                targetPolygon: GeoNetwork.map.ExtentMap.targetPolygon,
                        //                                vectorLayer: GeoNetwork.map.ExtentMap.vectorLayer
                        //                            }
                        //                        });
                    }
                    
                    
                    
                    
                    // Clear button
                    tbarItems.push({
                        text: OpenLayers.i18n('clear'),
                        iconCls: "clearPolygon",
                        handler: function(){
                            // Destroy geometry
                            this.vectorLayer.destroyFeatures();
                            
                            // Clean form inputs
                            var targetPolygonInput = document.getElementById('_X' + this.targetPolygon);
                            if (targetPolygonInput !== null) {
                                targetPolygonInput.value = '';
                            }
                            
                            if (this.targetBbox !== '') {
                                var wsen = this.targetBbox.split(',');
                                
                                // update the input fields
                                Ext.get(wsen[0]).dom.value = '';
                                Ext.get(wsen[1]).dom.value = '';
                                Ext.get(wsen[2]).dom.value = '';
                                Ext.get(wsen[3]).dom.value = '';
                                Ext.get("_" + wsen[0]).dom.value = '';
                                Ext.get("_" + wsen[1]).dom.value = '';
                                Ext.get("_" + wsen[2]).dom.value = '';
                                Ext.get("_" + wsen[3]).dom.value = '';
                                
                                // Validate fields content
                                $(wsen[0]).onkeyup();
                                $(wsen[1]).onkeyup();
                                $(wsen[2]).onkeyup();
                                $(wsen[3]).onkeyup();
                            }
                        },
                        scope: {
                            vectorLayer: vectorLayers[eltRef],
                            targetPolygon: targetPolygon,
                            targetBbox: watchedBbox,
                            eltRef: eltRef
                        }
                    });
                }
                
                var mapPanel = new GeoExt.MapPanel({
                    renderTo: id,
                    height: 300, // TODO : make config file see with ELE.
                    width: 400,
                    map: maps[eltRef],
                    tbar: (edit ? tbarItems : null)
                });
                
                if (children.length > 0) {
                    readFeature(children[0].innerHTML, {
                        format: 'WKT',
                        zoomToFeatures: true,
                        from: wgsProj, // Always reproject LatLounBoundingBox
                        to: mainProj
                    }, vectorLayers[eltRef], maps[eltRef]);
                }
                //              FIXME : GML parsing sounds not trivial. Using WKT parser instead for now.  
                //                if (GeoNetwork.map.ExtentMap.targetPolygon != '') {
                //                    var gml = document.getElementById('_X' + GeoNetwork.map.ExtentMap.targetPolygon).value;
                //                    GeoNetwork.map.ExtentMap.readFeature(gml, {
                //                        format: 'GML',
                //                        zoomToFeatures: true,
                //                        from: GeoNetwork.map.ExtentMap.wgsProj,
                //                        to: GeoNetwork.map.ExtentMap.mainProj
                //                    });
                //                }
                
                if (watchedBbox !== '') {
                    // watch the input text fields, i.e. update the input
                    // hidden fields when the input text fields are changed
                    watchRadios(watchedBbox, eltRef);
                    watchBbox(vectorLayers[eltRef], watchedBbox, eltRef, maps[eltRef]);
                }
            }
        }
    };
};

GeoNetwork.map.ExtentMap.prev_geometry = OpenLayers.Format.GML.Base.prototype.writers.feature._geometry;
OpenLayers.Format.GML.Base.prototype.writers.feature._geometry = function(geometry){
    if (geometry.CLASS_NAME === "GeoNetwork.map.ExtentMap.MultiPolygonReference") {
        var gml = this.createElementNS(this.namespaces.gml, "gml:MultiPolygon");
        var gmlNode = this.createElementNS(this.namespaces.gml, "gml:MultiPolygon");
        gmlNode.setAttribute("gml:id", geometry.id);
        gml.appendChild(gmlNode);
        return gml;
    } else {
        return GeoNetwork.map.ExtentMap.prev_geometry.apply(this, arguments);
    }
};
