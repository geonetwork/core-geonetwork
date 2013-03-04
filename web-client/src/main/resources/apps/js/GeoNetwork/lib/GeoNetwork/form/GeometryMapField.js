/*
 * Copyright (C) 2001-2011 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
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
Ext.namespace('GeoNetwork.form');

/** api: (define)
 *  module = GeoNetwork.form
 *  class = GeometryMapField
 *  base_link = `GeoExt.MapPanel <http://www.geoext.org/lib/GeoExt/widgets/MapPanel.html>`_
 */

/** api: example
 * 
 * 
 *  .. code-block:: javascript
 *  
 *        var fields = [];
 *        var geomField = new Ext.form.TextField( {
 *            name : 'E_geometry',
 *            id : 'geometry',
 *            fieldLabel : 'WKT geometry',
 *            // toolTip : '(eg. POLYGON((-180 -90,180 -90,180 90,-180 90,-180
 *            // -90)) or POINT(6 10))',
 *            hideLabel : false
 *        });
 *        
 *        var geomWithMapField = new GeoNetwork.form.GeometryMapField({
 *            geometryFieldId : 'geometry',
 *            layers : [new OpenLayers.Layer.WMS(
 *                    "Global Imagery", "http://maps.opengeo.org/geowebcache/service/wms",
 *                    {layers: "bluemarble"}
 *                )],
 *            zoom: 1
 *        });
 *        fields.push(geomField, geomWithMapField);
 * 
 *        var searchForm = new Ext.FormPanel( {
 *             items : fields,
 *             ...
 * 
 */

/** api: constructor 
 *  .. class:: GeometryMapField(config)
 * 
 *  Create a small map which has to be related to an existing
 *  (hidden) text field for the geometry criteria. 
 *  On map move, the geometry field is updated with the current
 *  map extent as WKT.
 *  
 *  A Checkbox is available to turn off the field update.
 * 
 * 
 *  TODO :
 *   * Support other projection
 *   * Add draw buttons (like current minimap)
 *   * Add list of well know region to quickly zoom to an AOI or a Gazetter service like GeoNames.org ?
 */
GeoNetwork.form.GeometryMapField = Ext.extend(GeoExt.MapPanel, {
    // TODO : Add capablitiy to change layer_style and drawing layer_style 
    layer_style: OpenLayers.Util.extend({}, OpenLayers.Feature.Vector.style['default']),

    /** api: config[geometryFieldId] 
     *  ``String`` Identifier of an Ext.form.TextField to update
     *  with the WKT representation of the map extent. If undefined,
     *  a hidden text field is created to store the geometry.
     */
    geometryFieldId : undefined,

    /** api: property[geometryField] 
     *  ``Ext.form.TextField`` The TextField to update 
     */
    geometryField : undefined,
    mapOptions: undefined,
    extentBox : undefined,
    vectorLayer : undefined,
    /** private: property[nearYou] 
     *  Near you button 
     */
    nearYou : undefined,
    
    /** private: method[initStyle]
     *  Define default layer styles
     */
    initStyle: function(){
        // See http://dev.openlayers.org/releases/OpenLayers-2.8/doc/apidocs/files/OpenLayers/Feature/Vector-js.html for more style info
        this.layer_style.fillOpacity = 0;
        this.layer_style.strokeOpacity = 0.8;
        this.layer_style.strokeDashstyle = 'dash';
        this.layer_style.strokeWidth = 4;
    },
    createExtentControl: function() {
        var action;
        
        // Restrict to map extent action or draw polygon control
        if (this.restrictToMapExtent) {
            action = new Ext.form.Checkbox( {
                boxLabel : OpenLayers.i18n('restrictSearchToMap'),
                checked : this.activated ? true : false,
                listeners : {
                    check : function(cb, checked) {
                        this.geometryField.setValue('');
                        this.nearYou.toggle(false);
                        
                        if (checked) {
                            this.on('aftermapmove', this.setField, this);
                            this.fireEvent('aftermapmove');
                        } else {
                            this.un('aftermapmove', this.setField, this);
                        }
                    },
                    scope : panel
                }
            });
            this.getTopToolbar().add(action);
            
            if (this.activated) {
                this.on('aftermapmove', this.setField, this);
            }
        } else {
            this.initStyle();
            this.extentBox = new GeoNetwork.Control.ExtentBox({
                    wktelement: this.geometryField,
                    vectorLayerStyle: this.layer_style
             });
            action = new GeoExt.Action({
                control: this.extentBox,
                toggleGroup:  this.map.id + "move",
                allowDepress: false,
                pressed: this.activated ? true : false,
                map: this.map,
                tooltip: {
                    title: OpenLayers.i18n("selectExtentTooltipTitle"), 
                    text: OpenLayers.i18n("selectExtentTooltipText")
                },
                iconCls: 'selextent'
            });
            this.vectorLayer = this.extentBox.getOrCreateLayer();
            var clearAction = {
                iconCls: "clearPolygon",
                tooltip: {
                    title: OpenLayers.i18n("clearExtentTooltipTitle"), 
                    text: OpenLayers.i18n("clearExtentTooltipText")
                },
                handler: function(){
                    this.vectorLayer.destroyFeatures();
                    this.geometryField.setValue('');
                },
                scope: this
            };
            this.getTopToolbar().add(action, clearAction);
        }
        
        if (this.nearYouControl) {
            this.getTopToolbar().add(this.nearYou);
        }
        
        return action;
    },
    /** private: method[createToolbar] 
     *  Create the default toolbar 
     */
    createNavBar : function() {
        var items = [];
        var panel = this;
        
        this.nearYou = new Ext.Button( {
            iconCls : 'md-mn mn-user-location',
            iconAlign : 'right',
            tooltip: OpenLayers.i18n('mapNearYou'),
            enableToggle : true,
            toggleGroup:  this.map.id + "move",
            listeners : {
                toggle : function(bt, pressed) {
                    var o = this;
                    if (pressed) {
                        if (navigator.geolocation) {
                            navigator.geolocation.getCurrentPosition(function(position) {
                                o.map.panTo(new OpenLayers.LonLat(position.coords.longitude, position.coords.latitude));
                                o.geometryField.setValue('POINT(' + position.coords.latitude + ' ' + 
                                                                    position.coords.longitude + ')');
                            }); //, function(error){console.log(error);});
                        }
                    } else {
                        o.geometryField.setValue('');
                    }
                },
                scope : panel
            }
        });
        
        var zoomAllAction = new GeoExt.Action({
            control: new OpenLayers.Control.ZoomToMaxExtent(),
            map: this.map,
            iconCls: 'zoomfull',
            tooltip: {title: OpenLayers.i18n("zoomToMaxExtentTooltipTitle"), text: OpenLayers.i18n("zoomToMaxExtentTooltipText")}
        });
        
        var zoomInAction = new GeoExt.Action({
            control: new OpenLayers.Control.ZoomBox(),
            map: this.map,
            toggleGroup:  this.map.id + "move",
            allowDepress: false,
            iconCls: 'zoomin',
            tooltip: {title: OpenLayers.i18n("zoominTooltipTitle"), text: OpenLayers.i18n("zoominTooltipText")}
        });

        var zoomOutAction = new GeoExt.Action({
            control:  new OpenLayers.Control.ZoomBox({
                displayClass: 'ZoomOut',
                out: true
            }),
            map: this.map,
            toggleGroup:  this.map.id + "move",
            allowDepress: false,
            tooltip: {title: OpenLayers.i18n("zoomoutTooltipTitle"), text: OpenLayers.i18n("zoomoutTooltipText")},
            iconCls: 'zoomout'
        });

        var panAction = new GeoExt.Action({
            control: new OpenLayers.Control.DragPan({
                    isDefault: true
                }),
            toggleGroup:  this.map.id + "move",
            allowDepress: false,
            pressed: this.activated ? false : true,
            map: this.map,
            iconCls: 'pan',
            tooltip:  {title: OpenLayers.i18n("dragTooltipTitle"), text: OpenLayers.i18n("dragTooltipText")}
        });
        
        items.push(zoomAllAction, zoomInAction, zoomOutAction, panAction,  '->');
        
        //this.getTopToolbar().add(items);
        return items;
    },

    /** api: property[mapOptions] 
     *  ``Object`` The default mapOptions. By default WGS84 world based map.
     */
    /** api: property[width] 
     *  ``Number`` Width default to 290 // TODO Remove ?
     */
    /** api: property[height] 
     *  ``Number`` Height default to 180 // TODO Remove ?
     */
    /** api: property[restrictToMapExtent] 
     *  ``Boolean`` Define if extent should be restricted to map extent
     */
    /** api: property[border] 
     *  ``Boolean`` No border by default
     */
    /** api: property[activated] 
     *  ``Boolean`` Define if the control is activated on startup. 
     *  False by default to not update the geometry field. 
     */
    defaultConfig : {
        id : 'geometryMap', // FIXME : This is hardcoded 
        width: 290,
        height: 180,
        stateful: false,
        border : false,
        activated : false,
        restrictToMapExtent : false,
        nearYouControl : true
    },
    /** private: method[initComponent] 
     *  Initializes the component
     */
    initComponent : function() {
        Ext.applyIf(this, this.defaultConfig);
        
        var options = this.mapOptions || {
            projection: "EPSG:4326",
            units: "degrees",
            maxExtent: new OpenLayers.Bounds(-180,-90,180,90),
            restrictedExtent: new OpenLayers.Bounds(-180,-90,180,90),
            controls: []
        };
        
        if (!options.controls) {
            options.controls = [];
        }
        
        if (GeoNetwork.map.CONTEXT) {
            // Load map context
            var request = OpenLayers.Request.GET({
                url: GeoNetwork.map.CONTEXT,
                async: false
            });
            if (request.responseText) {
                var text = request.responseText;
                var format = new OpenLayers.Format.WMC();
                this.map = format.read(text, {map:options});
                this.layers = undefined;
            }
        }
        else if (GeoNetwork.map.OWS) {
            // Load map context
            var request = OpenLayers.Request.GET({
                url: GeoNetwork.map.OWS,
                async: false
            });
            if (request.responseText) {
                var parser = new OpenLayers.Format.OWSContext();
                var text = request.responseText;
                this.map = parser.read(text, {map: options});
                this.layers = undefined;
            }
        }
        
        this.map = this.map || new OpenLayers.Map('search_map', options);
        this.map.addControl(new GeoNetwork.Control.ZoomWheel());
        this.map.addControl(new OpenLayers.Control.LoadingPanel());
        this.map.addControl(new OpenLayers.Control.MousePosition());
        
        this.tbar = this.createNavBar();
        
        // Use an existing geometry fields or create a new hidden one
        if (this.geometryFieldId) {
            this.geometryField = Ext.getCmp(this.geometryFieldId);
        } else {
            var gmf = this;
            this.geometryField = new Ext.form.TextField({
                name: 'E_geometry',
                inputType: 'hidden',
                listeners: {
                    valid: function(cpt){
                        // Init extent box in map only if vector layer not
                        // yet initialized (eg. on startup when state is restored).
                        if (cpt.getValue() !== '' && this.extentBox.getOrCreateLayer().features.length === 0) {
                            this.extentBox.updateFieldsWKT(cpt.getValue());
                        }
                    },
                    scope: gmf
                }
            });
        }
        
        GeoNetwork.form.GeometryMapField.superclass.initComponent.call(this);
        this.add(this.geometryField);
        this.createExtentControl();
        // FIXME : can't trigger this after map rendered by MapPanel. Workaround used, this.setExtent
//        this.on('afterlayout', function(el){
//            if (this.activated) {
//                this.extentBox.updateFields(this.map.getMaxExtent());
//            }
//        }, this);

    },
    setExtent: function(bounds) {
        if (bounds) {
            this.extentBox.updateFields(bounds);
        } else {
            this.extentBox.updateFields(this.map.getMaxExtent());
        }
    },
    /** private: method[setField] 
     *  Update the geometry field with the current map extent
     */
    setField : function() {
        if (this.geometryField !== undefined) {
            var bounds = this.map.getExtent().toArray();
            // TODO : map projection to WGS84
            var wkt = 'POLYGON((' + bounds[0] + ' ' + bounds[1] + ','
                    + bounds[0] + ' ' + bounds[3] + ',' + bounds[2] + ' '
                    + bounds[3] + ',' + bounds[2] + ' ' + bounds[1] + ','
                    + bounds[0] + ' ' + bounds[1] + '))';

            this.geometryField.setValue(wkt);
            this.geometryField.fireEvent('change');
        }
    }
});

/** api: xtype = gn_geometrymapfield */
Ext.reg('gn_geometrymapfield', GeoNetwork.form.GeometryMapField);