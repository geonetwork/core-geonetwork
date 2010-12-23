/**
 * Extent map editor/viewer.
 * 
 * Take bounding box and extent (bounding polygon)
 * from an ISO 19139 record and display on a map
 * component.
 * 
 * TODO : 
 *  Add tooltip to button
 * 
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

var extentMap = {
    map: null,
    maps: new Array(),
    vectorLayer: null,
    vectorLayerStyle: OpenLayers.Feature.Vector.style['default'],
    targetPolygon: null,
    watchedBbox: null,
    mode: null,
    edit: null,
    eltRef: null,
    digits: 5, // FIXME : how many decimals is fine ? This should be define according to the projection ?

    /**
     * A class for a geometry referenced by its ID
     */
    MultiPolygonReference: OpenLayers.Class(OpenLayers.Geometry, {
        id: null,

        initialize: function(id) {
            this.id = id;
        },

        CLASS_NAME: "extentMap.MultiPolygonReference"
    }),

    /**
     * Define the map projection. Bounding box are stored in WGS84 in metadata records
     * and polygon also.
     */
    mainProjCode: "EPSG:4326",
    wgsProj: new OpenLayers.Projection("EPSG:4326"),
    mainProj: null,
    units: 'm', //degrees
    alternateProj: null,
    
    /**
     * APIFunction: initMapDiv
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
    initMapDiv: function () {
        
        var viewers, idFunc;
        extentMap.mainProj = new OpenLayers.Projection(extentMap.mainProjCode);
        extentMap.alternateProj = extentMap.mainProj;
        
        // some pages have prototype and other have access to ext so do a check
        // and choose the one that is available
        if (Ext) {
            viewers = Ext.DomQuery.select('.extentViewer');
            idFunc = Ext.id;
        } else {
            viewers = $$('.extentViewer');
            idFunc = identify();
        }

        for (var idx = 0; idx < viewers.length; ++idx) {
            var viewer = viewers[idx];
            extentMap.targetPolygon = viewer.getAttribute("target_polygon");
            extentMap.watchedBbox = viewer.getAttribute("watched_bbox");
            extentMap.edit = viewer.getAttribute("edit") == 'true';
            extentMap.eltRef = viewer.getAttribute("elt_ref");
            extentMap.mode = viewer.getAttribute("mode");
            
            
            var children = viewer.childNodes;
            var tmp = [];

            for (var i = 0; i < children.length; i++) {
                if (children[i].nodeType == 1) {
                    tmp.push(children[i]);
                }
            }
            
            children = tmp;
            
            // If already initialized
            if (children.length > 1) continue;

            // Creates map component
            var id;
            if (Ext) {
                id = Ext.id(viewer);
            } else {
                id = viewer.identify();
            }
            var map = extentMap.createMap();
            extentMap.maps[extentMap.eltRef] = map;
            
            // Create toolbar with:
            // * polygon control
            // * bbox control
            // * choose existing polygon
            // * clear
            // * rollback
            if (extentMap.edit) {
	            var tbarItems = [], control;
	
	            
	            // Bbox drawing control
	            if (extentMap.mode == 'bbox') {
		            control = new OpenLayers.Control.DrawFeature(
		            		extentMap.vectorLayer,
		            		OpenLayers.Handler.RegularPolygon,
		            		{
		            			handlerOptions: {
		            			    irregular: true,
		            			    sides: 4
		            			},
		            			featureAdded: function(feature) {
		            				
		            				// a box was drawn, update the input text and input
		                            // hidden fields
		                            var bounds = feature.geometry.getBounds();
		                            // If current map projection is not WGS84, reproject bounds
		                            // coordinate to store WGS84 in metadata record.
		                            var boundsReproj = bounds.clone().transform(
		                                extentMap.mainProj, extentMap.wgsProj);
	
		                            var wsen = this.watchedBbox.split(',');	// Here we don't round coordinates to store full value
		                            Ext.get("_" + wsen[0]).dom.value = boundsReproj.left;
		                            Ext.get("_" + wsen[1]).dom.value = boundsReproj.bottom;
		                            Ext.get("_" + wsen[2]).dom.value = boundsReproj.right;
		                            Ext.get("_" + wsen[3]).dom.value = boundsReproj.top;
		
		                            // Refresh all inputs
		                            extentMap.watchRadios(this.watchedBbox, this.eltRef);
		                            
		            			}.bind({watchedBbox: extentMap.watchedBbox, eltRef: extentMap.eltRef})
		            		}
		            );
		            tbarItems.push(new GeoExt.Action({
		                map: map,
		                control: control,
		                text: translate('drawRectangle'),
		                pressed: false,
		                allowDepress: true,
		                toggleGroup: "tool",
		                iconCls: "drawRectangle"
		            }));
	            }
	            
	            
	            
	            
	            
	            // Polygon drawing control 
	            if (extentMap.mode == 'polygon') {
	            	control = new OpenLayers.Control.DrawFeature(
	            		extentMap.vectorLayer,
	            		OpenLayers.Handler.Polygon, {
	            			featureAdded: function(feature) {
	            				// Update form input
	            				document.getElementById('_X' + this).value = extentMap.convertToGml(feature, extentMap.mainProjCode);
	            		    }.bind(extentMap.targetPolygon)
	            		}
	            	);
	            
		            tbarItems.push(new GeoExt.Action({
		                map: map,
		                control: control,
		                text: translate('drawPolygon'),
		                pressed: false,
		                allowDepress: true,
		                toggleGroup: "tool",
		                iconCls: "drawPolygon"
		            }));
		            control = new OpenLayers.Control.DrawFeature(
	            		extentMap.vectorLayer,
	            		OpenLayers.Handler.RegularPolygon,
	            		{
	            			handlerOptions: {
	            			    irregular: true,
	            			    sides: 60
	            			},
	            			featureAdded: function(feature) {
	            				// Update form input
	            				document.getElementById('_X' + this).value = extentMap.convertToGml(feature, extentMap.mainProjCode);
	            		    }.bind(extentMap.targetPolygon)
	            		}
		            );
	            
		            tbarItems.push(new GeoExt.Action({
		                map: map,
		                control: control,
		                text: translate('drawCircle'),
		                pressed: false,
		                allowDepress: true,
		                toggleGroup: "tool",
		                iconCls: "drawCircle"
		            }));
		            
//					TODO : this button define a popup which allow to select a geographic
//		            feature from a WFS service (defined by serviceUrl parameter). This popup
//		            needs to add a dependcy to MapFish in order to work. The list of layers
//		            served by the WFS will be listed (using GetCapabilities info). Then clicking
//		            on the map select one feature to be used as bounding polygon.
//		            More test needed.
//		            
//		            tbarItems.push({	
//		            	text: "Choose a geographic feature", // TODO : i18n
//		            	tooltip: "",
//		            	handler: function() {
//		            	   
//		                   var featureSelectionPanel = new app.FeatureSelectionPanel({
//		                       serviceUrl: "/geoserver/wfs",
//		                       width: 700,
//		                       height: 350
//		                   });
//
//		                   var fsWin = new Ext.Window({
//		                       title: 'featureSelection',//translate('featureSelection'),
//		                       layout: 'fit',
//		                       modal: true,
//		                       items: featureSelectionPanel
//		                   });
//		                   fsWin.show();
//		                   
//		                   featureSelectionPanel.on('featureselected', function(panel, feature) {
//		                       fsWin.close();
//
//		                       if (this.selectionFeature) {
//		                           this.vectorLayer.destroyFeatures(this.selectionFeature);
//		                       }
//		                       this.selectionFeature = new OpenLayers.Feature.Vector(feature.geometry, {}, this.selectionStyle);
//		                       this.vectorLayer.addFeatures(this.selectionFeature);
//
//	                            document.getElementById('_X' + this.targetPolygon).value = extentMap.convertToGml(feature, extentMap.mainProjCode);
//		                       //this.map.zoomToExtent(this.selectionFeature.geometry.getBounds());
//		                   }, this);
//		            	},
//		            	scope: {
//		            		targetPolygon: extentMap.targetPolygon,
//		            		vectorLayer: extentMap.vectorLayer
//		            	}
//		            });
	            }
	            
	            
	            
	            
	            // Clear button
	            tbarItems.push({
	            	text: translate('clear'),
	            	iconCls: "clearPolygon",
	            	handler: function() {
	            		// Destroy geometry
	            		this.vectorLayer.destroyFeatures();
	            		
	            		// Clean form inputs
	            		var targetPolygonInput = document.getElementById('_X' + this.targetPolygon);
	            		if (targetPolygonInput != null)
	            			targetPolygonInput.value = '';
	            		
	            		if (this.targetBbox != '') {
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
	            		vectorLayer: extentMap.vectorLayer,
                        targetPolygon: extentMap.targetPolygon,
                        targetBbox: extentMap.watchedBbox,
                        eltRef: extentMap.eltRef
                    }
	            });
            }
            
            var mapPanel = new GeoExt.MapPanel({
                renderTo: id,
                height: 300,	// TODO : make config file see with ELE.
                width: 600,
                map: map,
                //title: 'Geographic extent', // TODO : i18n,
                tbar: (extentMap.edit?tbarItems:null)
            });
            
            if (children.length > 0) {
                extentMap.readFeature(children[0].innerHTML, {
                    format: 'WKT',
                    zoomToFeatures: true,
                    from: extentMap.wgsProj,	// Always reproject LatLounBoundingBox
                    to: extentMap.mainProj
                });
            }
//          FIXME : GML parsing sounds not trivial. Using WKT parser instead for now.  
//            if (extentMap.targetPolygon != '') {
//                var gml = document.getElementById('_X' + extentMap.targetPolygon).value;
//                extentMap.readFeature(gml, {
//                    format: 'GML',
//                    zoomToFeatures: true,
//                    from: extentMap.wgsProj,
//                    to: extentMap.mainProj
//                });
//            }
            
            if (extentMap.watchedBbox != '') {
                // watch the input text fields, i.e. update the input
                // hidden fields when the input text fields are changed
                extentMap.watchRadios(extentMap.watchedBbox, extentMap.eltRef);
                extentMap.watchBbox(extentMap.vectorLayer, extentMap.watchedBbox, extentMap.eltRef, extentMap.map);
            }
        }
    },
    /**
     * Convert a feature to a GML geometry.
     */
    convertToGml: function(feature, proj) {
		var mainProj = new OpenLayers.Projection(proj);
        var writer = (mainProj==extentMap.wgsProj?
        		new OpenLayers.Format.GML.v3():
        		new OpenLayers.Format.GML.v3({
        			externalProjection: mainProj,
        			internalProjection: extentMap.wgsProj})
        		);
         var child = writer.writeNode("feature:_geometry", feature.geometry);
         return OpenLayers.Format.XML.prototype.write.call(writer, child.firstChild);
    },
    /**
     * Create default map with one vector layer for drawing.
     * 
     */
    createMap: function() {
    	OpenLayers.ImgPath = "../../scripts/openlayers/img/";
    	
		// Set main projection same as map viewer projection
    	extentMap.mainProj = new OpenLayers.Projection(mapOptions.projection);

    	// TODO : how to define one common map with background in config file.
    	/*var options = {
    		    units: extentMap.units,
    		    projection: extentMap.mainProjCode,
    		    theme: null
    		}; */

    	var options = mapOptions;
    	options.theme = null;
    	
        var map = extentMap.map = new OpenLayers.Map(
        		options
        );
        
        
        
        // Disable mouse wheel and navigation toolbar in view mode.
        // User can still pan the map.
        if(!extentMap.edit) {
        	var navigationControl = map.getControlsByClass('OpenLayers.Control.Navigation')[0];
        	navigationControl.disableZoomWheel();
        	map.removeControl(map.getControlsByClass('OpenLayers.Control.PanZoom')[0]);
        }
        
        // Add mouse position control to display coordintate.
        map.addControl(new OpenLayers.Control.MousePosition());
        
        // Add layers.
        // TODO : after migration from Intermap to OpenLayers
        // configuration parameters should be define in order
        // to define map layers. Currently using the same
        // WMS as Intermap.
        
        for (var i=0; i<backgroundLayers.length; i++) {
            var layer = new OpenLayers.Layer.WMS(backgroundLayers[i][0],
                    backgroundLayers[i][1],
                    backgroundLayers[i][2],
                    backgroundLayers[i][3])
            map.addLayer(layer);    
        }
        
        
        // Add vector layer to draw features (ie. bbox or polygon)
        extentMap.vectorLayer = new OpenLayers.Layer.Vector(
                "VectorLayer", {
                    //displayOutsideMaxExtent: true,
                    //alwaysInRange: true,
                    //displayInLayerSwitcher: false,
                    //tyle: extentMap.vectorLayerStyle, // TODO add as option
            });
        map.addLayer(extentMap.vectorLayer);
        
        
        // Clean existing features before drawing
        extentMap.vectorLayer.events.on({
            "sketchstarted": function() {
        	   this.destroyFeatures();
        	},
        	scope: extentMap.vectorLayer
        });

        map.zoomToMaxExtent();

        return map;
    },

    /**
     * Change bbox when bounding box input text fields are updated.
     */
    watchBbox: function(vectorLayer, watchedBbox, eltRef, map) {
    	var wsen = watchedBbox.split(',');
        
        for (var i = 0; i < wsen.length; ++i) {
        	// register a "change" listen on each input text element
        	Ext.get(wsen[i]).on('change', function() {
                // update the value of the corresponding input hidden elements
                // and update the box drawn on the map
                extentMap.updateBbox(map, watchedBbox, eltRef, false);
            });
        	
        	// register a "change" listen on each input text element
        	Ext.get("_" + wsen[i]).on('change', function() {
        		extentMap.updateBbox(map, watchedBbox, eltRef, true);
        	});
        }
    },

    updateBbox: function(map, targetBbox, eltRef, mainProj) {
    	var vectorLayer = map.getLayersByName("VectorLayer")[0]; // That supposed that only one vector layer is on the map
    	// In map projection
    	var bounds;
    	// In WGS84 projection
    	var boundsProjected;
    	var wsen = targetBbox.split(',');
    	
    	// Update bbox from main projection information
    	if (mainProj) {
    		var values = new Array(wsen.length);
            values[0] = Ext.get("_" + wsen[0]).getValue();
            values[1] = Ext.get("_" + wsen[1]).getValue();
            values[2] = Ext.get("_" + wsen[2]).getValue();
            values[3] = Ext.get("_" + wsen[3]).getValue();
            
            bounds = OpenLayers.Bounds.fromArray(values);
			boundsProjected = bounds.clone();
            
            // TODO : reproject if another projection is used
            /*if (extentMap.mainProj != extentMap.wgsProj) {
            	bounds = bounds.clone().transform(
            			extentMap.mainProj, extentMap.wgsProj);
            }*/

            boundsProjected = bounds.clone();

            // Always reproject to WGS84
 			if (extentMap.mainProj != extentMap.wgsProj) {
            	boundsProjected.transform(extentMap.mainProj, wgsProj);
            }	
            
    	} else {
    		var values = new Array(wsen.length);
            values[0] = Ext.get(wsen[0]).getValue();
            values[1] = Ext.get(wsen[1]).getValue();
            values[2] = Ext.get(wsen[2]).getValue();
            values[3] = Ext.get(wsen[3]).getValue();
            bounds = OpenLayers.Bounds.fromArray(values);
            
            var toProj = null;
            var radio = document.getElementsByName("proj_" + eltRef);
            for( i = 0; i < radio.length; i++ ) {
            	if(radio[i].checked == true)
            		toProj = radio[i].value;
            }
            
			var selProj = new OpenLayers.Projection(toProj);
			boundsProjected = bounds.clone();

			// Always reproject to WGS84
			if (selProj != extentMap.wgsProj) {
	            boundsProjected.transform(selProj, extentMap.wgsProj);
    		}

    		// Bounds in map projection to draw rectangle
    		bounds.transform(selProj,  extentMap.mainProj);


            /*if (toProj != extentMap.mainProjCode) {
                // the bounds read from the input text fields are
                // equal to main projection, transform them before updating the input
                // hidden fields
                bounds.transform(
                    new OpenLayers.Projection(toProj), extentMap.mainProj
                );
            } else {
                // the bounds read from the input text fields are
                // the default one, transform the values passed to updateBBox
                var b = bounds.clone();
                b.transform(extentMap.mainProj, extentMap.alternateProj);
                values[0] = b.left;
                values[1] = b.bottom;
                values[2] = b.right;
                values[3] = b.top;
            }*/

    	}
    	
        // Set main projection coordinates (WGS84)
        Ext.get("_" + wsen[0]).dom.value = boundsProjected.left;
    	Ext.get("_" + wsen[1]).dom.value = boundsProjected.bottom;
        Ext.get("_" + wsen[2]).dom.value = boundsProjected.right;
        Ext.get("_" + wsen[3]).dom.value = boundsProjected.top;

    	// Validate fields content
    	Ext.get(wsen[0]).dom.onkeyup();
    	Ext.get(wsen[1]).dom.onkeyup();
    	Ext.get(wsen[2]).dom.onkeyup();
    	Ext.get(wsen[3]).dom.onkeyup();

       	// Draw new bounds
        var feature = new OpenLayers.Feature.Vector(bounds.toGeometry());
        vectorLayer.destroyFeatures();
        vectorLayer.addFeatures(feature);

        extentMap.zoomToFeatures(map, vectorLayer);

        // Update all inputs
        extentMap.watchRadios(targetBbox, eltRef);

    },

    // Regions extents are in WGS84
    updateBboxForRegion: function(map, targetBbox, eltRef) {
    	var vectorLayer = map.getLayersByName("VectorLayer")[0]; // That supposed that only one vector layer is on the map
    	// In map projection
    	var bounds;
    	var wsen = targetBbox.split(',');

        // Update bbox from main projection information (values from regions extents in WGS84)
        var values = new Array(wsen.length);
        values[0] = Ext.get("_" + wsen[0]).getValue();
        values[1] = Ext.get("_" + wsen[1]).getValue();
        values[2] = Ext.get("_" + wsen[2]).getValue();
        values[3] = Ext.get("_" + wsen[3]).getValue();
        bounds = OpenLayers.Bounds.fromArray(values);

        // Bounds in map projection to draw rectangle
        bounds.transform(extentMap.wgsProj,  extentMap.mainProj);

    	// Validate fields content
    	Ext.get(wsen[0]).dom.onkeyup();
    	Ext.get(wsen[1]).dom.onkeyup();
    	Ext.get(wsen[2]).dom.onkeyup();
    	Ext.get(wsen[3]).dom.onkeyup();

       	// Draw new bounds
        var feature = new OpenLayers.Feature.Vector(bounds.toGeometry());
        vectorLayer.destroyFeatures();
        vectorLayer.addFeatures(feature);
        
        extentMap.zoomToFeatures(map, vectorLayer);
        
        // Update all inputs
        extentMap.watchRadios(targetBbox, eltRef);
        
    },
    
    /**
     * Change projection when radio button change
     */
    watchRadios: function(watchedBbox, eltRef) {
        function updateInputTextFields(watchedBbox, toProj, digits) {
        	var wsen = watchedBbox.split(',');

            // Get WGS84 values
            var w = Ext.get("_" + wsen[0]).getValue();
            var s = Ext.get("_" + wsen[1]).getValue();
            var e = Ext.get("_" + wsen[2]).getValue();
            var n = Ext.get("_" + wsen[3]).getValue();

            var l = w != "" ? w : "0";
            var b = s != "" ? s : "0";
            var r = e != "" ? e : "0";
            var t = n != "" ? n : "0";

            var bounds = OpenLayers.Bounds.fromString(
                l + "," + b + "," + r + "," + t
            );

            /*if (!toProj.equals(extentMap.mainProj)) {
                bounds.transform(extentMap.mainProj, toProj);
            }*/

            var wgsProj = new OpenLayers.Projection("EPSG:4326");
            bounds.transform(wgsProj, toProj);

            if (w != "") {
                w = bounds.left.toFixed(digits) + "";
            }
            Ext.get(wsen[0]).dom.value = w;
            if (s != "") {
                s = bounds.bottom.toFixed(digits) + "";
            }
            Ext.get(wsen[1]).dom.value = s;
            if (e != "") {
                e = bounds.right.toFixed(digits) + "";
            }
            Ext.get(wsen[2]).dom.value = e;
            if (n != "") {
                n = bounds.top.toFixed(digits) + "";
            }
            Ext.get(wsen[3]).dom.value = n;
        }
        
        // Register onclick event for radio related to current map
        $$('input.proj').each(function(input) {
        	if (input.id.indexOf(eltRef) != -1) {
	        	// According to current selection, update coordinates
	        	if (input.checked) {
	        		updateInputTextFields(watchedBbox, new OpenLayers.Projection(input.value), extentMap.digits);		
				}
	        	
	        	Ext.get(input.id).on('click', function() {
	                updateInputTextFields(watchedBbox, new OpenLayers.Projection(input.value), extentMap.digits);
	            });
        	}
        });
    },
    
    /**
     * Returns a string representing the feature geometry
     *
     * @param options parameters describing how to format the geoemtry
     *        options.from the current projection of the feature (either both 'from' and 'to' are defined or neither)
     *        options.to   the projection to reproject the feature
     *        options.format the format to write the geometry in 'WKT', 'GML', etc...  Default is WKT
     */
    writeFeature: function(options) {
        var format = 'WKT';
        var from,to;
    	if( options!=null ){
            format = options.format || 'WKT';
            from = options.from;
            to = options.to;
    	}

        var writer = new OpenLayers.Format[format]();
        if( from != null && to != null ){
        	writer.internalProjection = from;
        	writer.externalProjection = to;
        }
        if (!this.vectorLayer.features.length) return null;
        // Gets the last drawn feature
        var feature = this.vectorLayer.features[this.vectorLayer.features.length-1];
        return writer.write(feature);
    },
    
    /**
     * @param string string containing the feature data
     * @param option parameters describing how to format the geoemtry
     *        option.from the current projection of the feature (either both 'from' and 'to' are defined or neither)
     *        option.to   the projection to reproject the feature
     *        option.format the format to write the geometry in 'WKT', 'GML', etc...  Default is WKT
     *        option.zoomToFeatures boolean, if true, zooms to feature(s) extent
     */
    readFeature: function(string, options) {
    	// An empty geometry.
    	if (string == '')
    		return false;
    	
    	var format = 'WKT';
        var from,to;
        if( options!=null){
            format = options.format || 'WKT';
            from = options.from;
            to = options.to;
        }
        var reader = new OpenLayers.Format[format]();
    
        if( from != null && to != null ){
            reader.externalProjection = from;
            reader.internalProjection = to;
        }

        // FIXME : When creating WKT, cariage return are added to the string.
        string = string.replace(/\n/g, '');
        
        var feature = reader.read(string);
        
        // reader is subject to returning an object or an array depending on the format
        if (!feature) return false;
        if (feature.length) feature = feature[0];
        
        this.vectorLayer.addFeatures(feature);
        // optionally, zoom on the layer features extent
        if (options.zoomToFeatures) {
            this.zoomToFeatures(this.map, this.vectorLayer);
        }
        return true;
    },
    
    zoomToFeatures: function(map, vectorLayer) {
    	var extent = vectorLayer.getDataExtent();
    	if (extent && !isNaN(extent.left)) {
            var width = extent.getWidth()/2;
            var height = extent.getHeight()/2;
            extent.left -= width;
            extent.right += width;
            extent.bottom -= height;
            extent.top += height;
            map.zoomToExtent(extent);
        } else {
            map.zoomToMaxExtent();
        }
    }
};

extentMap.prev_geometry = OpenLayers.Format.GML.Base.prototype.writers.feature._geometry;
OpenLayers.Format.GML.Base.prototype.writers.feature._geometry = function(geometry) {
    if (geometry.CLASS_NAME == "extentMap.MultiPolygonReference") {
        var gml = this.createElementNS(this.namespaces.gml, "gml:MultiPolygon");
        var gmlNode = this.createElementNS(this.namespaces.gml, "gml:MultiPolygon");
        gmlNode.setAttribute("gml:id", geometry.id);
        gml.appendChild(gmlNode);
        return gml;
    } else {
        return extentMap.prev_geometry.apply(this, arguments);
    }
};
