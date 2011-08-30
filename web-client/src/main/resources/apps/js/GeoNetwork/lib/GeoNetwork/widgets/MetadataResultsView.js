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
Ext.namespace('GeoNetwork');

/**
 *
 */
/** api: (define)
 *  module = GeoNetwork
 *  class = MetadataResultsView
 *  base_link = `Ext.DataView <http://extjs.com/deploy/dev/docs/?class=Ext.DataView>`_
 */
/** api: constructor 
 *  .. class:: MetadataResultsView(config)
 *
 *     Create a metadata results data view which interacts:
 *
 *      * with :class:`GeoNetwork.Catalogue`
 *      * with :class:`GeoNetwork.MetadataResultsToolBar` if provided
 *
 *
 */
GeoNetwork.MetadataResultsView = Ext.extend(Ext.DataView, {
    /** api: config[catalogue] 
     * ``GeoNetwork.Catalogue`` Catalogue to use
     */
    catalogue: undefined,
    
    /** api: config[templates]
     *  An optional array of templates
     */
    templates: null,
    
    //multiSelect : true,
    overClass: 'md-over',
    
    itemSelector: 'li.md',
    
    emptyText: '',
    
    autoWidth: true,
    
    /** api: config[map]
     *  Optional OpenLayers map or an array of Object with OpenLayers maps
     *  and zoomToExtentOnSearch property to be used for interacting with search results
     *
     *  Set zoomToExtentOnSearch to false, to not to zoom to features extent
     *  after result store update.
     */
    maps: [],
    
    /** Projection MUST be the same for all maps linked to the metadata results view.
     *
     */
    mapsProjection: "EPSG:4326",
    projectionFrom: undefined,
    projectionTo: undefined,
    
    /** api: property[mdSelectionUuids] 
     *  Current selection uuids
     */
    mdSelectionUuids: [],
    
    /** api: property[layer_style] 
     *  Layer style
     */
    layer_style: OpenLayers.Util.extend({}, OpenLayers.Feature.Vector.style['default']),
    
    /** api: property[layer_style_hover] 
     *  Hover layer style
     */
    layer_style_hover: OpenLayers.Util.extend({}, OpenLayers.Feature.Vector.style['default']),
    
    /** api: property[layer_style_selected] 
     *  Selected layer style
     */
    layer_style_selected: OpenLayers.Util.extend({}, OpenLayers.Feature.Vector.style['default']),
    
    /** api: property[features] 
     *  Current features used by first maps
     */
    features: [],
    
    /** api: property[features] 
     *  Current hover feature for each maps
     */
    hover_feature: [],
    
    /** api: property[contextMenu] 
     *  Context menu for metadata record displayed on right click event
     */
    contextMenu: undefined,
    
    /** api: property[contextMenuNodeId] 
     *  Identifier of the last node with context menu activated
     */
    contextMenuNodeId: undefined,
    
    
    /** api: property[acMenu] 
     *  Current action menu for onmouseover node
     */
    acMenu: undefined,
    
    /** api: property[ratingWidget] 
     *  Rating widget
     */
    newMetadataWindow: undefined,
    plugins: [ // new Ext.DataView.DragSelector()
]    ,
    listeners: {
        /**
         * Zoom to feature
         */
        dblclick: {
            fn: function(dv, idx, node, e){
                if (this.maps) {
                    var record = this.getStore().getAt(idx);
                    var uuid = record.get('uuid');
                    this.zoomTo(uuid);
                }
            }
        },
        /**
         * Highligth bounding box on node over
         */
        mouseenter: {
            fn: function(dv, idx, node, e){
                var i, j;
                
                if (this.maps) {
                    var record = this.getStore().getAt(idx);
                    var uuid = record.get('uuid');
                    
                    for (j = 0; j < this.maps.length; j++) {
                        var l = this.maps[j].layer;
                        if (l.features) {
                            for (i = 0; i < l.features.length; i++) {
                                if (uuid === l.features[i].attributes.id) {
                                    l.drawFeature(l.features[i], this.layer_style_hover);
                                    this.hover_feature[j] = l.features[i];
                                    continue;
                                }
                            }
                        }
                    }
                }
                this.actionMenuInit(idx, node);
            }
        },
        mouseleave: {
            fn: function(dv, idx, node, e){
                var i;
                if (this.acMenu) {
                    this.acMenu.hide();
                }
                this.acMenu = undefined;
                
                if (this.maps) {
                    for (i = 0; i < this.maps.length; i++) {
                        if (this.hover_feature[i]) {
                            var l = this.maps[i].layer;
                            l.drawFeature(this.hover_feature[i], this.layer_style);
                            this.hover_feature[i] = null;
                            continue;
                        }
                    }
                }
            }
        },
        contextmenu: {
            fn: function(dv, idx, node, e){
                this.contextMenuNodeId = idx;
                this.createMenu(idx, dv);
                this.contextMenu.showAt(e.getXY());
                e.stopEvent(); // Do not trigger browser event
            }
        }
    },
    actionMenuInit: function(idx, node){
        this.acMenu = Ext.get(Ext.DomQuery.selectNode('span.md-action-menu', node));
        this.acMenu.on('click', function(){
            this.createMenu(idx, this);
            this.contextMenu.showAt([this.acMenu.getX(), this.acMenu.getY() + this.acMenu.getHeight()]);
        }
.bind(this));
        this.acMenu.show();
    },
    createMenu: function(id, dv){
        // var record = this.getStore().getAt(id);
        // TODO var isEditable = record.get('edit') === 'true' ? true : false; // FIXME : do not allow edit on harvested records ? 
        // var isHarvested = record.get('isharvested') === 'y' ? true : false;
        // var harvesterType = record.get('harvestertype');
        // is login ?
        var record = this.getStore().getAt(id);
        
        if (!this.contextMenu) {
            this.contextMenu = new GeoNetwork.MetadataMenu({
                floating: true,
                catalogue: catalogue,
                record: record,
                resultsView: dv
            });
        } else {
            this.contextMenu.setRecord(record);
        }
        
    },
    /** private: method[zoomTo] 
     *  Zoom to metadata bounding boxes for all registered maps.
     */
    zoomTo: function(uuid){
        var i, j;
        
        for (j = 0; j < this.maps.length; j++) {
            var l = this.maps[j].layer;
            if (l.features) {
                for (i = 0; i < l.features.length; i++) {
                    if (uuid === l.features[i].attributes.id) {
                        var bounds = l.features[i].geometry.getBounds();
                        if (bounds) {
                            this.maps[j].map.zoomToExtent(bounds);
                        }
                        break;
                    }
                }
            }
        }
    },
    /** private: method[initComponent] 
     *  Initializes the metadata results view.
     */
    initComponent: function(config){
        var i;
        
        // TODO : add utility to add/remove templates
        this.templates = {
            SIMPLE: GeoNetwork.Templates.SIMPLE,
            THUMBNAIL: GeoNetwork.Templates.THUMBNAIL,
            FULL: GeoNetwork.Templates.FULL
        };
        
        GeoNetwork.MetadataResultsView.superclass.initComponent.call(this);
        
        this.store = this.catalogue.metadataStore;
        this.initStyle();
        
        if (this.maps) {
            if (this.maps instanceof OpenLayers.Map) {
                var map = this.maps;
                this.maps = [];
                this.addMap(map); // Add one map
            } else {
                // Array
                for (i = 0; i < this.maps.length; i++) {
                    var m = this.maps[i];
                    this.initMap(m);
                }
            }
        } else {
            this.maps = [];
        }
        // TODO : only register if one map available ?
        // Register events on metadata results store
        this.getStore().on({
            "load": this.resultsLoaded,
            "clear": this.destroyMetadataBbox,
            scope: this
        });
        this.on('selectionchange', this.selectionChange);
    },
    /** api: method[addMap] 
     *  :param map: ``OpenLayers.Map`` An OpenLayers map
     *  :param zoomToExtentOnSearch: ``Boolean`` Zoom to results extent on store update
     *
     *  Register a new map to the results view.
     */
    addMap: function(map, zoomToExtentOnSearch){
        var found = false, i;
        for (i = 0; i < this.maps.length; i++) {
            if (this.maps[i].map.id === map.id) {
                found = true;
                
                break;
            }
        }
        if (!found) {
            var m = {
                map: map,
                zoomToExtentOnSearch: zoomToExtentOnSearch || false
            };
            this.mapsProjection = map.projection;
            this.projectionFrom = new OpenLayers.Projection("EPSG:4326");
            this.projectionTo = new OpenLayers.Projection(this.mapsProjection);
            this.maps.push(m);
            this.initMap(m);
        }
    },
    /** private: method[initMap]
     *  :param map: An OpenLayers map
     *  :param idx: Index
     *  Add a Vector layer to the map
     */
    initMap: function(map){
        // TODO : Translate layer name based on a search name ?
        var l = new OpenLayers.Layer.Vector(OpenLayers.i18n("mdResultsLayer"), {
            style: this.layer_style
        });
        this.addCurrentFeatures(l);
        map.layer = l;
        map.map.addLayer(l);
    },
    /** api: method[removeMap] 
     *  :param mapId: ``OpenLayers.Map.id`` An OpenLayers map id
     *
     *
     */
    removeMap: function(mapId){
        this.maps.splice(mapId, 1);
        //this.layers.splice(mapId, 1);
    },
    /** api: method[getTemplates] 
     *  Return templates
     */
    getTemplates: function(){
        return this.templates;
    },
    /** api: method[removeTemplate] 
     *  :param name: ``String`` template key
     *  Remove templates
     */
    removeTemplate: function(name){
        delete this.templates[name];
    },
    /** api: method[getTemplates] 
     *  :param name: ``String`` The template key
     *  :param template: ``XTempalte`` The template
     *
     *  Add templates
     */
    addTemplate: function(name, template){
        this.templates[name] = template;
    },
    /** api: method[applyTemplate]
     *  :param name: ``String`` template key
     *  Apply one templates
     */
    applyTemplate: function(name){
        this.tpl = this.templates[name];
        this.refresh();
    },
    /** private: method[initStyle]
     *  Define default layer styles
     */
    initStyle: function(){
        this.layer_style.fillOpacity = 0;
        this.layer_style.graphicOpacity = 1;
        
        this.layer_style_selected.fillOpacity = 0.1;
        
        this.layer_style_hover.fillOpacity = 0.3;
        this.layer_style_hover.strokeWidth = 3;
        
    },
    /** private: method[destroyMetadataBbox]
     *
     */
    destroyMetadataBbox: function(){
        var i;
        
        for (i = 0; i < this.maps.length; i++) {
            var l = this.maps[i].layer;
            if (l.features) {
                if (l.features.length > 0) {
                    this.features = [];
                    l.destroyFeatures();
                }
            }
        }
    },
    /** private: method[resultsLoaded]
     *  After a search, initialize data view.
     */
    resultsLoaded: function(view, records, options){
        this.drawMetadataBbox(view, records, options);
        this.contextMenu = undefined;
        this.initRatingWidget();
        this.initSelector();
        //this.initMenu();
        //this.initSelection(records);
    },
    /** private: method[drawMetadataBbox]
     *
     */
    drawMetadataBbox: function(view, records, options){
        var i, j;
        Ext.each(records, function(r){
            var bboxes = r.get('bbox');
            if (bboxes) {
                var polygons = [];
                for (j = 0; j < bboxes.length; j++) {
                    var bbox = bboxes[j].value;
                    var p1 = new OpenLayers.Geometry.Point(bbox[2], bbox[1]);
                    var p2 = new OpenLayers.Geometry.Point(bbox[2], bbox[3]);
                    var p3 = new OpenLayers.Geometry.Point(bbox[0], bbox[3]);
                    var p4 = new OpenLayers.Geometry.Point(bbox[0], bbox[1]);
                    
                    if (this.mapsProjection !== 'EPSG:4326') {
                        p1.transform(this.projectionFrom, this.projectionTo);
                        p2.transform(this.projectionFrom, this.projectionTo);
                        p3.transform(this.projectionFrom, this.projectionTo);
                        p4.transform(this.projectionFrom, this.projectionTo);
                    }
                    
                    var pointList = [p1, p2, p3, p4, p1];
                    var linearRing = new OpenLayers.Geometry.LinearRing(pointList);
                    var polygon = new OpenLayers.Geometry.Polygon([linearRing]);
                    
                    polygons.push(polygon.clone());
                }
                var multipolygon = new OpenLayers.Feature.Vector(new OpenLayers.Geometry.MultiPolygon(polygons), {
                    id: r.get('uuid')
                });
                this.features.push(multipolygon.clone());
            }
        }, this);
        
        for (i = 0; i < this.maps.length; i++) {
            this.addCurrentFeatures(this.maps[i].layer);
        }
        
        if (this.features.length > 0) {
            for (i = 0; i < this.maps.length; i++) {
                var m = this.maps[i];
                if (m.zoomToExtentOnSearch) {
                    m.map.zoomToExtent(m.layer.getDataExtent());
                }
            }
        }
        
    },
    /** private: method[initRatingWidget]
     *  After a search, retrieve rating div and init readonly widget.
     */
    initRatingWidget: function(){
        var ratingWidgets = Ext.DomQuery.select('div.rating'), idx;
        for (idx = 0; idx < ratingWidgets.length; ++idx) {
            if (Ext.ux.RatingItem) {
                var ri = new Ext.ux.RatingItem(ratingWidgets[idx], {
                    disabled: true,
                    name: 'rating' + idx
                });
            } else {
                ratingWidgets[idx].style.display = 'none';
            }
        }
    },
    /** private: method[initSelector]
     *  After a search, retrieve link div and init menu also available via context menu.
     */
    initSelector: function(){
        //        var checkboxes = Ext.DomQuery.select('input.selector');
        //        for (var idx = 0; idx < checkboxes.length; ++idx) {
        //            checkboxes[i].setAttribute('onclick', '');
        //        }
    },
    selectAll: function(){
        var checkboxes = Ext.DomQuery.select('input.selector'), idx;
        for (idx = 0; idx < checkboxes.length; ++idx) {
            checkboxes[idx].checked = true;
        }
    },
    selectAllInPage: function(){
        var checkboxes = Ext.DomQuery.select('input.selector'), idx;
        for (idx = 0; idx < checkboxes.length; ++idx) {
            checkboxes[idx].checked = true;
            Ext.each(this.getRecords(this.getNodes()), function(r){
                var uuid = r.get('uuid');
                this.catalogue.metadataSelect('add', [uuid]);
            }, this);
            // FIXME : selection calls may not end in call order
            // then selection indicator may be wrong
        }
    },
    selectNone: function(){
        var checkboxes = Ext.DomQuery.select('input.selector'), idx;
        for (idx = 0; idx < checkboxes.length; ++idx) {
            checkboxes[idx].checked = false;
        }
    },
    /** private: method[initSelection]
     *  After a search, initialize selected records
     *
     *  TODO : removed as dataview selection model is not used
     */
    initSelection: function(records){
        var selection = [];
        Ext.each(records, function(r){
            var isSelected = r.get('selected');
            if (isSelected === 'true') {
                selection.push(r);
            }
        });
        this.select(selection);
        this.on('selectionchange', this.selectionChange);
    },
    selectionChange: function(dv, nodes){
        /* Paging should not be considered as a selectionchange */
        if (this.catalogue) {
            var records = this.getRecords(nodes);
            var selected = {};
            var toAdd = [];
            var toRemove = [];
            var i;
            var uuid;
            
            for (i = 0; i < records.length; i++) {
                uuid = records[i].get('uuid');
                selected[uuid] = true;
            }
            
            /* Keep selected and remove others */
            for (i = 0; i < this.mdSelectionUuids.length; i++) {
                uuid = this.mdSelectionUuids[i];
                if (selected[uuid]) {
                    toAdd.push(uuid);
                } else {
                    toRemove.push(uuid);
                }
            }
            /* Add newly selected */
            for (uuid in selected) {
                if (selected.hasOwnProperty(uuid) && !this.mdSelectionUuids[uuid]) {
                    toAdd.push(uuid);
                }
            }
            
            this.catalogue.metadataSelect('add', toAdd);
            this.catalogue.metadataSelect('remove', toRemove);
            this.mdSelectionUuids = toAdd;
        }
    },
    /** private: method[addCurrentFeatures]
     *  Add current features, Clone feature before adding them to the maps
     *  And keep on set of feature if all maps are unregistered
     */
    addCurrentFeatures: function(layer){
        var len = this.features.length, j;
        if (len > 0) {
            var clones = [];
            clones = []; //new Array(len);
            var orig, clone;
            for (j = 0; j < len; j++) {
                orig = this.features[j];
                clone = orig.clone();
                clones[j] = clone;
            }
            
            layer.addFeatures(clones);
        }
    },
    /** private: method[onDestroy]
     *  Private method called during the destroy sequence.
     */
    onDestroy: function(){
        //        if (this.mdSelectionInfoCmp) {
        //            // TODO ?
        //        }
        GeoNetwork.MetadataResultsView.superclass.onDestroy.apply(this, arguments);
    }
});

/** api: xtype = gn_metadataresultsview */
Ext.reg('gn_metadataresultsview', GeoNetwork.MetadataResultsView);
