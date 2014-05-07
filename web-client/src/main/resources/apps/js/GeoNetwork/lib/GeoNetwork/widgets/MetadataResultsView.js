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
 *  TODO:
 *  
 *      * Add links to the contextual menu of a record
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
    styleInitialized: false,
    
    /** api: property[mdSelectionUuids] 
     *  Current selection uuids
     */
    mdSelectionUuids: [],
    
    /** api: property[layer_style_selected] 
     *  Selected layer style. Unused
     */
    layer_style_selected: OpenLayers.Util.extend({}, OpenLayers.Feature.Vector.style['default']),
    
    defaultConfig: {
        /** api: property[featurecolor] 
         *  Color for record's bounding box. By default #ee9900.
         *  
         *  Example ::
         *  
         *    metadataResultsView = new GeoNetwork.MetadataResultsView({
         *      catalogue: catalogue,
         *      // Use a custom single color for bounding box
         *      featurecolor: '#FFFFFF'
         */
        featurecolor: '#ee9900',
        
        /** api: property[colormap] 
         *  Array of colors to be used for record's bounding box.
         *  
         *  Example for configuration of feature color, color map or custom layer style::
         *  
         *    metadataResultsView = new GeoNetwork.MetadataResultsView({
         *            catalogue: catalogue,
         *            displaySerieMembers: true,
         *            autoScroll: true,
         *            tpl: GeoNetwork.Templates.FULL
         *            // Use a custom single color for bounding box
         *            , featurecolor: '#FFFFFF'
         *            // Use a random color map with 2 colors 
         *             , colormap: GeoNetwork.Util.generateColorMap(5)
         *            // Use a default color map with 10 colors
         *            , colormap: GeoNetwork.Util.defaultColorMap
         *            // Use a custom CSS rules
         *            , featurecolorCSS: "border-width: 5px;border-style: solid; border-color: ${featurecolor}"
         *            // Use a custom style
         *            , layer_style:  new OpenLayers.Style({ 
         *                           strokeOpacity: 1,
         *                           strokeWidth: 4,
         *                           strokeColor: "${featurecolor}",
         *                           fillColor: "${featurecolor}",
         *                           fillOpacity: 0.2
         *                       })
         *             , layer_style_hover:  new OpenLayers.Style({ 
         *                 strokeOpacity: 1,
         *                 strokeWidth: 10,
         *                 strokeColor: "${featurecolor}",
         *                 fillColor: "${featurecolor}",
         *                 fillOpacity: 0
         *             })
         *     });
         */
        colormap: undefined,
        
        /** api: property[layer_style] 
         *  Layer style which could reference colormap colors. Example::
         *  
         *    new OpenLayers.Style({ 
         *          strokeOpacity: 1,
         *          strokeWidth: 1,
         *          strokeColor: "${color}",
         *          fillColor: "${color}",
         *          fillOpacity: 0
         *      }),
         */
        layer_style:  new OpenLayers.Style({ 
            strokeOpacity: 1,
            strokeWidth: 1,
            fillOpacity: 0,
            strokeColor: '${featurecolor}',
            fillColor: '${featurecolor}'
        }),
        
        /** api: property[layer_style_hover] 
         *  Hover layer style which could reference colormap colors.
         */
        layer_style_hover: new OpenLayers.Style({ 
            strokeOpacity: 1,
            strokeWidth: 3,
            fillOpacity: 0.3,
            strokeColor: '${featurecolor}',
            fillColor: '${featurecolor}'
        })
    },
    /** api: property[features] 
     *  Current features used by first maps
     */
    features: [],
    
    /** api: property[features] 
     *  Current hover feature for each maps
     */
    hover_feature: [],

    /** api: config[displayContextualMenu] 
     *  Display contextual menu for metadata record displayed on right click event
     */
    displayContextualMenu: true,
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
    
    /** api: property[displaySerieMembers] 
     *  Display metadata series member embedded in search results view
     *  as a list of items.
     */
    displaySerieMembers: false,

    /** api: property[maxOfMembers] 
     *  (TODO : UNUSED) Maximum numbers of members to display.
     */
    maxOfMembers: 50,
    
    /** api: property[relatedTpl] 
     *  Template use for related metadata if displaySerieMembers is true.
     */
    relatedTpl: undefined,
    
    /** api: property[ratingWidget] 
     *  Rating widget
     */
    newMetadataWindow: undefined,
    plugins: [ // new Ext.DataView.DragSelector()
    ],
    listeners: {
        /**
         * Zoom to feature
         */
        dblclick: {
            fn: function(dv, idx, node, e){
                if (this.maps.length !== 0) {
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
                
                if (this.maps.length !== 0) {
                    var record = this.getStore().getAt(idx);
                    var uuid = record.get('uuid');
                    
                    for (j = 0; j < this.maps.length; j++) {
                        var l = this.maps[j].layer;
                        if (l.features) {
                            for (i = 0; i < l.features.length; i++) {
                                if (uuid === l.features[i].attributes.id) {
                                    if (!this.hover_feature[j]) {
                                        l.drawFeature(l.features[i], 'hover');
                                        this.hover_feature[j] = l.features[i];
                                    }
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
                
                for (i = 0; i < this.maps.length; i++) {
                    if (this.hover_feature[i]) {
                        var l = this.maps[i].layer;
                        l.drawFeature(this.hover_feature[i], 'default');
                        this.hover_feature[i] = null;
                        continue;
                    }
                }
            }
        },
        contextmenu: {
            fn: function(dv, idx, node, e){
                if (this.displayContextualMenu) {
                    this.contextMenuNodeId = idx;
                    this.createMenu(idx, dv);
                    this.contextMenu.showAt(e.getXY());
                    e.stopEvent(); // Do not trigger browser event
                }
            }
        }
    },
    actionMenuInit: function(idx, node){
        this.acMenu = Ext.get(Ext.DomQuery.selectNode('span.md-action-menu', node));
        if(this.acMenu) {
            this.acMenu.on('click', function(){
                this.createMenu(idx, this);
                this.contextMenu.showAt([this.acMenu.getX(), this.acMenu.getY() + this.acMenu.getHeight()]);
            }, this);
            this.acMenu.show();
        }
    },
    
    addCustomAction: function() {
    	
    },
    createMenu: function(id, dv){
        var record = this.getStore().getAt(id);
        
        if (!this.contextMenu) {
            this.contextMenu = new GeoNetwork.MetadataMenu({
                floating: true,
                catalogue: catalogue,
                record: record,
                resultsView: dv,
                addCustomAction: this.addCustomAction
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
    initComponent: function(){
        var i;
        Ext.applyIf(this, this.defaultConfig);
        
        // TODO : add utility to add/remove templates
        this.templates = this.templates || {
            SIMPLE: GeoNetwork.Templates.SIMPLE,
            THUMBNAIL: GeoNetwork.Templates.THUMBNAIL,
            FULL: GeoNetwork.Templates.FULL
        };
        
        GeoNetwork.MetadataResultsView.superclass.initComponent.call(this);
        
        this.setStore(this.catalogue.metadataStore);
        this.relatedTpl = new Ext.XTemplate(this.relatedTpl || GeoNetwork.Templates.Relation.SHORT);
        
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
       
        this.on('selectionchange', this.selectionChange);
    },
    /** api: method[setStore] 
     *  :param store: ``GeoNetwork.data.MetadataResultsStore`` A metadata store
     *
     *  Set metadata view store.
     */
    setStore: function(store){
        // Unregister previous events
        if (this.getStore()) {
            this.getStore().un("load", this.resultsLoaded, this);
            this.getStore().un("clear", this.destroyMetadataBbox, this);
        }
        
        // Register events on metadata results store
        this.store = store;
        
        this.getStore().on({
            "load": this.resultsLoaded,
            "clear": this.destroyMetadataBbox,
            scope: this
        });
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
        if (!this.styleInitialized) {
            this.initStyle();
            this.styleInitialized = true;
            // TODO : a map may be initialized twice by 2 differents results views
        }
        
        // TODO : Translate layer name based on a search name ?
        var l = new OpenLayers.Layer.Vector(OpenLayers.i18n("mdResultsLayer"), {
            styleMap: new OpenLayers.StyleMap({'default': this.layer_style, 'hover': this.layer_style_hover})
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
     *  
     *  Remove a template
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
     *  
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
        // A one colormap by default
        if (!this.colormap) {
            this.colormap = [this.featurecolor];
        }
        // Define a custom CSS rules if more than one color used
        if (this.colormap.length > 1) {
            this.featurecolorCSS = this.featurecolorCSS || "border-left-width: 3px;border-left-style: solid; border-left-color: ${featurecolor}";
        } else {
            this.featurecolorCSS = undefined;
        }
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
        this.dislayLinks(records);
        this.dislayRelations(records);
        //this.initMenu();
    },
    /** private: method[dislayLinks]
     *  Create link menu in the div for each records
     *  
     */
    dislayLinks: function (records) {
        var view = this;

        Ext.each(records, function (r) {
            var links = r.get('links'),
                id = r.get('id'),
                uuid = r.get('uuid'),
                div = Ext.query('#md-links-' + id, view.el.dom.body),
                el = Ext.get(div[0]);

            // Add permanent link (can copy for bookmark)
            var menu = new Ext.menu.Menu(),
                bHref = this.catalogue.services.rootUrl + 'search?&uuid=' + escape(uuid);

            var permalinkMenu = new Ext.menu.TextItem({text: '<input value="' + bHref + '"/></br><a href="' + bHref + '">Link</a>'});
            menu.add('<b class="menu-title">' 
                      + OpenLayers.i18n('permalinkInfo') + '</b>',
                      permalinkMenu);
            view.addLinkMenu(id, [{
              text: 'Bookmark Link',
              href: bHref,
              menu: menu
            }], OpenLayers.i18n('Bookmark Link'), 'bookmark', el);

            
            if (links.length > 0) {
                
                // The template may not defined a md-links placeholder
                if (el) {
                    var store = new Ext.data.ArrayStore({
                        autoDestroy: true,
                        idIndex: 0,  
                        fields: [
                            {name: 'href', mapping: 'href'}, 
                            {name: 'name', mapping: 'name'}, 
                            {name: 'protocol', mapping: 'protocol'}, 
                            {name: 'title', mapping: 'title'}, 
                            {name: 'type', mapping: 'type'}
                        ],
                        data: links
                    });
                    store.sort('type');
                    
                    
                    var linkButton = [], label = null, currentType = null, bt,
                         allowDynamic = r.get('dynamic'), allowDownload = r.get('download'),
                         hasDownloadAction = 0;
                
                    var nid = 0;
                    store.each(function (record) {
                        nid += 1;
                        var linkId = nid+"-"+uuid;
                        // Avoid empty URL
                        if (record.get('href') !== '') {
                            // Check that current record type is the same as the previous record
                            // In such case, add the previous button if exist
                            // or create a new button to be added later
                            if (currentType === null || currentType !== record.get('type')) {
                                if (linkButton.length !== 0) {
                                    view.addLinkMenu(linkId, linkButton, label, currentType, el);
                                }
                                linkButton = [];
                                currentType = record.get('type');
                                var labelKey = 'linklabel-' + currentType;
                                label = OpenLayers.i18n(labelKey);
                                if (label === labelKey) { // Default label if not found in translation
                                    label = OpenLayers.i18n('linklabel-');
                                }
                            }
                            
                            var text = null, handler = null;
                            
                            // Only display WMS link if dynamic property set to true for current user & record
                            if (currentType === 'application/vnd.ogc.wms_xml' || (currentType.indexOf('OGC:WMS') > -1)) {
                                if (allowDynamic) {
                                    linkButton.push({
                                        text: record.get('title') || record.get('name'),
                                        handler: function (b, e) {
                                            // FIXME : ref to app
                                            app.switchMode('1', true);
                                            app.getIMap().addWMSLayer([[record.get('title'), record.get('href'), record.get('name'), uuid]]);
                                        },
                                        href: record.get('href')
                                    });
                                }
                            } else if (currentType === 'application/vnd.ogc.wmc') {
                                linkButton.push({
                                    text: record.get('title') || record.get('name'),
                                    handler: function (b, e) {
                                        // FIXME : ref to app
                                        app.switchMode('1', true);
                                        app.getIMap().addWMC(record.get('href'));
                                    },
                                    href: record.get('href')
                                });
                            } else {
                                // If link is uploaded to GeoNetwork the resources.get service or file.disclaimer service is used
                                // Check if allowDownload 
                                var displayLink = true;
                                if ((record.get('href').indexOf('resources.get') !== -1) || (record.get('href').indexOf('file.disclaimer') !== -1)) {
                                    displayLink = allowDownload;
                                    if (displayLink) hasDownloadAction++;
                                } else if (currentType === 'application/vnd.google-earth.kml+xml') {
                                    // Google earth link is provided when a WMS is provided
                                    displayLink = allowDynamic;
                                }
                                if (displayLink) {
                                    linkButton.push({
                                        text: (record.get('title') || record.get('name')),
                                        href: record.get('href')
                                    });
                                }
                            }
                            
                        }
                        
                    });
                    // Add the last button
                    nid++;
                    var linkId = nid+"-"+uuid;
                    if (linkButton !== null && linkButton.length !== 0) {
                        view.addLinkMenu(linkId, linkButton, label, currentType, el);
                    }
                    
                    // Add the download selector/all button if more than one
                    // download link on this record
                    if (hasDownloadAction > 1) {
                        nid++;
                        linkId = nid+"-"+uuid;
                        view.addLinkMenu(linkId, [{
                            text: 'download',
                            handler: function () {
                                // FIXME : this call require the catalogue to be named catalogue
                                catalogue.metadataPrepareDownload(id);
                            },
                        }], OpenLayers.i18n('prepareDownload'), 'downloadAllIcon', el);
                    }
                }
            }
        }, this);
    },
    /** private: method[addLinkMenu]
     *  Display a menu with links for a metadata record for a protocol.
     *  If there is only one element in the linkButton array, display a menu
     *  and display a dropdown menu if not.
     */
    addLinkMenu: function (parentId, linkButton, label, currentType, el) {
        var buttonId = label+'-'+parentId;
        if (Ext.get(buttonId)) { // don't need to add them again
          return;
        }

        var href = linkButton[0].href,
            isDownload = (currentType === 'downloadAllIcon') || (href.indexOf('resources.get') !== -1) || (href.indexOf('file.disclaimer') !== -1);

        if (linkButton.length === 1) {
            var handler = linkButton[0].handler || function () {
                window.open(linkButton[0].href);
            };
						var tTip = label;
            if (href) tTip += ' ' + href;
            if (linkButton[0].menu) {
              bt = new Ext.Button({
                id: buttonId,
                tooltip: tTip,
                menu: linkButton[0].menu,
                iconCls: GeoNetwork.Util.protocolToCSS(currentType, isDownload),
                renderTo: el
              });
            } else {
              bt = new Ext.Button({
                id: buttonId,
                tooltip: tTip,
                handler: handler,
                iconCls: GeoNetwork.Util.protocolToCSS(currentType, isDownload),
                renderTo: el
              });
            }
        } else {
						if (linkButton[0].handler) { // if handlers then create button list
							var items = [];
            	Ext.each(linkButton, function (button) {
              	items.push(new Ext.Button({
                	handler: button.handler,
                	text: button.text
              	}));
					  	});
            	bt = new Ext.Button({
                	id: buttonId,
                	tooltip: label,
                	menu: new Ext.menu.Menu({cls: 'links-mn', items: items}),
                	iconCls: GeoNetwork.Util.protocolToCSS(currentType, isDownload),
                	renderTo: el
            	});
						} else {
            	bt = new Ext.Button({
                	id: buttonId,
                	tooltip: label,
                	menu: new Ext.menu.Menu({cls: 'links-mn', items: linkButton}),
                	iconCls: GeoNetwork.Util.protocolToCSS(currentType, isDownload),
                	renderTo: el
            	});
						}
        }
    },
    /** private: method[dislayRelations]
     *  Search for children for all records which are series.
     *  All members are displayed in a UL element identified by "md-relation-{metadata_id}".
     *  
     *  If the div has a max-height attribute and scrolling is required,
     *  a "more" class is added to the element. This class
     *  could be used to do custom styling of the div (eg. expand to display
     *  all members).
     *  
     *  TODO : Improve search performance of relation service
     */
    dislayRelations: function(records){
        var relationTypes = 'children', view = this;
        
        if (this.displaySerieMembers) {
            Ext.each(records, function(r) {
                var isSerie = GeoNetwork.Settings.results.loadRelationForAll || r.get('type') === 'series',
                    id = r.get('id');
                if (isSerie) {
                    //TODO : use this.maxOfMembers
                    //var store = new GeoNetwork.data.MetadataResultsFastStore();
                    var store = new GeoNetwork.data.MetadataRelationStore(
                            this.catalogue.services.mdRelation + '?type=' + relationTypes + '&fast=false&uuid=' + escape(r.get('uuid')) , null, true);
                    store.load();
                    store.on('load', function(store, records){
                        Ext.each(records, function(md) {
                            var div = Ext.query('#md-relation-' + id, view.el.dom.body),
                                el = Ext.get(div[0]),
                                container = el.parent();
                            // Add custom class to identify when large number of members
                            // has to be displayed. Use CSS to control layout.
                            if (container && container.isScrollable()) {
                                container.addClass('more');
                            }
                            if (el) {
                                el.insertHtml('beforeEnd', view.relatedTpl.apply(md.data));
                                container.setVisible(true);
                            }
                        }, this);
                    });
                }
            }, this);
        }
    },
    /** private: method[drawMetadataBbox]
     *
     */
    drawMetadataBbox: function(view, records, options){
        var i, j;

        // No maps registered for this results view.
        if (this.maps.length === 0) {
            return;
        }
        
        this.features = [];
        Ext.each(records, function(r, idx){
            
            var featurecolor = this.colormap && this.colormap[idx % this.colormap.length]; // is used below to set the color property of the record and the corresponding feature
            r.set('featurecolor', featurecolor); // could be used in the Templates to set CSS properties
            r.set('featurecolorCSS', this.featurecolorCSS ? OpenLayers.String.format(
                this.featurecolorCSS, { 
                    featurecolor: featurecolor
                }
            ) : '');
                
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
                    id: r.get('uuid'),
                    featurecolor: featurecolor
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
        }
        Ext.each(this.getRecords(this.getNodes()), function(r){
        	var uuid = r.get('uuid');
        	this.catalogue.metadataSelect('add', [uuid]);
        }, this);
        // FIXME : selection calls may not end in call order
        // then selection indicator may be wrong
    },
    selectNone: function(){
        var checkboxes = Ext.DomQuery.select('input.selector'), idx;
        for (idx = 0; idx < checkboxes.length; ++idx) {
            checkboxes[idx].checked = false;
        }
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
