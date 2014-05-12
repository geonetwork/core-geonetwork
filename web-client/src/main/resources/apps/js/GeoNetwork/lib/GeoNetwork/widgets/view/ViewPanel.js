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
Ext.namespace('GeoNetwork.view');

/** api: (define)
 *  module = GeoNetwork.view
 *  class = ViewPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor 
 *  .. class:: ViewPanel(config)
 *
 *     Create a GeoNetwork metadata view window
 *     to display a metadata record. The metadata view use the view service.
 *     
 *     A toolbar is provided with:
 *      
 *      * a view mode selector 
 *      * a metadata menu (:class:`GeoNetwork.MetadataMenu`)
 *      * a print mode menu (for pretty HTML printing)
 *      * a menu to turn off tooltips (on metadata descriptors)
 *      * an option to give metadata-specific feedback
 *
 */
GeoNetwork.view.ViewPanel = Ext.extend(Ext.Panel, {
    defaultConfig: {
        border: false,
        /** api: config[lang] 
         *  The language to use to call GeoNetwork services in the print mode (which is opened in a new window).
         */
        lang: 'en',
        autoScroll: true,

        /** api: config[currTab] 
         *  The default view mode to use. Default is 'simple'.
         */
        currTab: 'simple',
        /** api: config[displayTooltip] 
         *  Display tooltips or not. Default is true.
         */
        displayTooltip: true,
        /** api: config[printDefaultForTabs]
         *  Define if default mode should be used for HTML print output instead of tabs
         *  (eg. metadata tag in advanced view will be replaced by default view)
         */
        printDefaultForTabs: false,
        printMode: undefined,
        printUrl: 'print.html',
        /** api: config[permalink]
         *  Define if permalink button should be displayed or not. Default is false.
         */
        permalink: false,
        /** api: config[relationTypes] 
         *  List of types of relation to be displayed in header. 
         *  Do not display feature catalogues (gmd:contentInfo) and sources (gmd:lineage) by default. 
         *  Set to '' to display all.
         */
        relationTypes: 'service|children|related|parent|dataset|fcat|siblings|associated'
    },
    serviceUrl: undefined,
    catalogue: undefined,
    metadataUuid: undefined,
    record: undefined,
    showFeedBackButton: undefined,
    resultsView: undefined,
    actionMenu: undefined,
    permalinkMenu: undefined,
    tipTpl: undefined,
    metadataSchema: undefined,
    cache: {},
    tooltips: [],
    buttonWidth : undefined,
    buttonHeight : undefined,
		viewPanelButtonCSS : undefined,

    /** api: method[getLinkedData]
     *  Get related metadata records for current metadata using xml.relation service.
     */
    getLinkedData : function() {
        var store = new GeoNetwork.data.MetadataRelationStore(this.catalogue.services.mdRelation + '?type=' + this.relationTypes + '&fast=false&uuid=' + escape(this.metadataUuid)	, null, true),
            view = this;
        store.load();
        store.on('load', function(){
            this.each(view.displayLinkedData, view);
        });
    },
    /** private: method[displayLinkedData]
     *  Display the record in the metadata related table (only available in simple view mode).
     *  Elements are grouped by type.
     */
    displayLinkedData: function(record){
        var table = Ext.query('table.related', this.body.dom),
            type = record.get('type'),
						subType = record.get('subType');
				if (subType.length>0) subType += ': ';
        var el = Ext.get(table[0]);
        var exist = el.child('tr td[class*=' + type + ']');
        var link = this.relatedTpl.apply(record.data)
            
        if (exist !== null) {
            exist.next().child('li').insertHtml('afterEnd', link);
        } else {
            el.child('tr').insertHtml('beforeBegin', '<tr><td class="main ' + type + '"><span class="cat-' + type +' icon">' + OpenLayers.i18n('related' + type) + '</span></td>' + 
            '<td><ul>' + link + '</ul></td></tr>');
        }
    },
    extractorWindow: null,
    extractorPanel: null,
    showExtractor: function (url, layer, version) {
//        if (this.extractorWindow) {
//            this.extractorWindow.close();
//            this.extractorWindow = undefined;
//        }
        if (!this.extractorWindow) {
            var options = {
                    projection: GeoNetwork.map.PROJECTION,
                    theme: null,
                    maxExtent: GeoNetwork.map.EXTENT
                };
            var map = new OpenLayers.Map(options);
            map.addLayers(GeoNetwork.map.BACKGROUND_LAYERS);
            this.extractorPanel = new GeoNetwork.WxSExtractor({
                url: url,
                version: version || '1.1.0', 
                layer: layer,
                map: map,
                split: true,
                region: 'center'
            });
            
            this.extractorWindow = new Ext.Window({
                title: OpenLayers.i18n('extractorTitle') + url,
                width: 600,
                height: 350,
                plain: true,
                layout: 'border',
                modal: true,
                items: [this.extractorPanel, new GeoExt.MapPanel({
                    border: false,
                    map: map,
                    region: 'west',
                    split: true,
                    width: 200
                })],
                closeAction: 'hide',
                constrain: true,
                iconCls: 'WFSDownloadIcon'
            });
        } else {
            this.extractorPanel.getCapabilities(url, layer);
        }
        this.extractorWindow.setTitle(OpenLayers.i18n('extractorTitle') + url);
        this.extractorWindow.show();
    },
    /**
     * Hack to search for WFS link in the relation table and init a data extractor widget
     * TODO : use a class selector instead or use the link in the results store
     */
    createExtractor: function () {
        var rows = Ext.query('table.related tr', this.body.dom);
        var panel = this;
        
        Ext.each(rows, function (item) {
            // Check WFS is in protocol name
            var cols = Ext.get(item).select('td');
            var label = cols.item(0).child('span');
            
            if (label !== null && label.dom.innerHTML.indexOf('WFS') !== -1) {
                var layers = Ext.query("li", cols.item(1).dom);
                Ext.each(layers, function (layer) {
                    var el = Ext.get(layer);
                    var url = el.child('a').getAttribute('href');
                    layer = el.child('a').dom.innerHTML; // TODO if a description available
                    el.insertHtml('beforeEnd', '<span id="wfs-download-' + layer + '">');
                    var downloadBt = new Ext.Button({
                        text: OpenLayers.i18n('dataDownload'),
                        iconCls: 'WFSDownloadIcon',
                        renderTo: "wfs-download-" + layer,
                        handler: function () {
                            panel.showExtractor(url, layer);
                        }
                    });
                });
                
            }
        });
    },
    createActionMenu: function(){
        if (!this.actionMenu) {
        
            this.actionMenu = new GeoNetwork.MetadataMenu({
                catalogue: this.catalogue,
                record: this.record,
                resultsView: this.resultsView
            });
        }
        
        var actionButton = {
            text: OpenLayers.i18n('mdMenu'),
            menu: this.actionMenu
        };
        return actionButton;
    },
    // TODO : duplicate from EditorToolBar - start
    createViewMenu: function(modes){
        var items = ['<b class="menu-title">' + OpenLayers.i18n('chooseAView') + '</b>'];
        
        this.viewMenu = new Ext.menu.Menu({
            items: items
        });
        
        var viewButton = {
            text: OpenLayers.i18n('viewMode'),
            iconCls: 'viewModeIcon',
            menu: this.viewMenu
        };
        
        return viewButton;
    },
    updateViewMenu: function(){
        var modes = Ext.query('span.mode', this.body.dom), menu = [], i, j, e, cmpId = this.getId(), isSimpleModeActive = true;
        menu.push([OpenLayers.i18n('simpleViewMode'), 'view-simple', isSimpleModeActive]);
        
        this.printMode = this.currTab;
        
        for (i = 0; i < modes.length; i++) {
            if (modes[i].firstChild) {
                var id = modes[i].getAttribute('id');
                var label = modes[i].innerHTML;
                var next = Ext.get(modes[i]).next();
                var tabs = next.query('LI');
                var current = next.query('LI[id=' + this.currTab + ']');
                var activeMode = current.length === 1;
                
                // Remove mode and children tabs if not in current mode
                if (!activeMode) {
                    Ext.get(modes[i]).parent().remove();
                } else {
                    // Remove tab if only one tab in that mode
                    if (next && tabs.length === 1) {
                        next.remove();
                    } else {
                        // Register events when multiple tabs
                        for (j = 0; j < tabs.length; j++) {
                            e = Ext.get(tabs[j]);
                            if (this.printDefaultForTabs) {
                            	this.printMode = 'default';
                            }
                            e.on('click', function(){
                                Ext.getCmp(cmpId).switchToTab(this.getAttribute('id'));
                            });
                        }
                    }
                }
                menu.push([label, id, activeMode]);
                
                if (activeMode === true) {
                    isSimpleModeActive = false;
                }
            }
        }
        
        // If another mode is active turn off simple mode.
        menu[0][2] = isSimpleModeActive;
        this.updateToolbar(menu);
    },
    updateToolbar: function(modes){
        var i, m;
        this.viewMenu.removeAll();
        for (i = 0; i < modes.length; i++) {
            m = modes[i];
            this.viewMenu.add({
                text: m[0],
                checked: false,
                disabled: m[2], // Disable current mode
                group: 'mode',
                value: m[1],
                listeners: {
                    'checkchange': this.onViewCheck,
                    scope: this // FIXME : this needs to be editor
                }
            });
        }
        this.viewMenu.doLayout();
    },
    switchToTab: function(tab){
        this.currTab = tab;
        this.onViewCheck({
            value: this.currTab
        }, true);
    },
    onViewCheck: function(item, checked){
        if (checked) {
            this.removeAll();
            this.currTab = item.value;
            this.load({
                url: this.serviceUrl + '&currTab=' + this.currTab,
                callback: function () {
                    this.fireEvent('aftermetadataload', this);
                },
                scope: this
            });
        }
    },
    // TODO : duplicate from EditorToolBar - end
    afterMetadataLoad: function(){
        // Clear tooltip cache
        this.cache = {};
        this.tooltips = [];

        // Processing after content load
        this.updateViewMenu();
        
        // Create map panel for extent visualization
        this.catalogue.extentMap.initMapDiv();
        
        // Related metadata are only displayed in view mode with no tabs
        if (this.currTab === 'view-simple' || this.currTab === 'inspire' || this.currTab === 'simple') {
            this.createExtractor();
            this.getLinkedData();
        }
        
        this.registerTooltip();
    },
    createPrintMenu: function(){
        return new Ext.Button({
            width: this.buttonWidth,
            height: this.buttonHeight,
            iconCls: this.viewPanelButtonCSS ? this.viewPanelButtonCSS('viewpanel-print') : 'print',
            id : 'viewpanel-print',
            tooltip: OpenLayers.i18n('printTT'),
            listeners: {
                click: function(c, pressed){
                	window.open(this.printUrl + '?uuid=' + this.metadataUuid + '&currTab=' + this.printMode + "&hl=" + this.lang);
                },
                scope: this
            }
        });
    },
    createFeedbackMenu: function() {
        var disabledButton;
        if(this.showFeedBackButton) {
            disabledButton = false;
        }
        else {
            disabledButton = true;
        }
        return new Ext.Button({
            width: this.buttonWidth,
            height: this.buttonHeight,
            iconCls: this.viewPanelButtonCSS ? this.viewPanelButtonCSS('viewpanel-feedback') : 'feedback',
            id : 'viewpanel-feedback',
            tooltip: OpenLayers.i18n('Feedback'),
            disabled: disabledButton,
            listeners: {
                click: function(c, pressed) {
                    var feedbackWindow = new GeoNetwork.FeedbackForm(null, this.record);
                    feedbackWindow.show();
                },
                scope: this
            }
        });
    },
    createTooltipMenu: function(){
        return new Ext.Button({
            width: this.buttonWidth,
            height: this.buttonHeight,
            enableToggle: true,
            pressed: this.displayTooltip,
            iconCls: this.viewPanelButtonCSS ? this.viewPanelButtonCSS('viewpanel-tooltip') : 'book',
            id : 'viewpanel-tooltip',
            tooltip: OpenLayers.i18n('enableTooltip'),
            listeners: {
                toggle: function(c, pressed){
                    this.displayTooltip = pressed;
                    this.enableTooltip();
                },
                scope: this
            }
        });
    },
    /**
     * Look for all th element with an id and register
     * a tooltip
     */
    enableTooltip: function(){
        Ext.each(this.tooltips, function(item, idx){
            if (this.displayTooltip) {
                item.enable();
            } else {
                item.disable();
            }
        }, this);
    },
    /**
     * Look for all th element with an id and register
     * a tooltip
     */
    registerTooltip: function(){
    	// select the title element of simple metadata elements
        var formElements = Ext.query('th[id]', this.body.dom);
        // select the title element of complex metadata elements
        formElements = formElements.concat(Ext.query('legend[id]', this.body.dom));
        // select additional elements requiring a help link  
        formElements = formElements.concat(Ext.query('.helplink', this.body.dom));
        Ext.each(formElements, function(item, index, allItems){
            var e = Ext.get(item);
            var id = e.getAttribute('id');
            var classAtt = e.getAttribute('class');
            var classes = [];
            if (classAtt) {
            	classes = classAtt.split(/\s+/);
            }
            if (classes.contains("helplink")) {
                var section = e.up('FIELDSET');
            	this.loadHelp(id, section | e, -10, 500, 0);
            } else if (e.is('TH')) {
                var section = e.up('FIELDSET');
                var f = function(){
                    if (this.displayTooltip) {
                        this.loadHelp(id, section);
                    }
                };
                e.parent().on('mouseover', f, this);
                
            } else {
                var f = function(){
                    if (this.displayTooltip) {
                        this.loadHelp(id);
                    }
                };
                    e.on('mouseover', f, this);
                
            }
        }, this);
    },
    /**
     * Add a tooltip to an element. If sectionId is defined,
     * then anchor is on top (usually is a fieldset legend element)
     */
    loadHelp: function(id, sectionId, anchorOffset, showDelay, hideDelay){
        if (!this.cache[id]) {
            var panel = this;
            GeoNetwork.util.HelpTools.get(id, this.metadataSchema, this.catalogue.services.schemaInfo, function(r) {
                panel.cache[id] = panel.tipTpl.apply(r.records[0].data);
                    
                var t = new Ext.ToolTip({
                    target: id,
                    title: r.records[0].get('label'),
                    anchor: sectionId ? 'top' : 'bottom',
                    anchorOffset: anchorOffset == undefined ? 35 : anchorOffset,
                    showDelay: showDelay == undefined ? 300 : showDelay,
                    hideDelay: hideDelay == undefined ? 200 : hideDelay,
                    html: panel.cache[id]
                });
                // t.show();// This force the tooltip to be displayed once created
                // it may cause issue when user scroll, so tooltips are all dislayed for hovered element
                // If not present, the tooltip only appear when user come back to the element. FIXME
                panel.tooltips.push(t);
            });
        }
    },
    /** private: method[initComponent] 
     *  Initializes the metadata view window.
     */
    initComponent: function(){
        Ext.applyIf(this, this.defaultConfig);
        
        this.tipTpl = new Ext.XTemplate(GeoNetwork.util.HelpTools.Templates.SIMPLE);
        this.relatedTpl = new Ext.XTemplate(this.relatedTpl || GeoNetwork.Templates.Relation.SHORT);
        
        this.tbar = [this.createViewMenu(), this.createActionMenu(), '->', this.createPrintMenu(), this.createFeedbackMenu(), this.createTooltipMenu()];
        
        GeoNetwork.view.ViewPanel.superclass.initComponent.call(this);
        this.metadataSchema = this.record ? this.record.get('schema') : '';
        
        this.add(new Ext.Panel({
            autoLoad: {
                url: this.serviceUrl + '&currTab=' + this.currTab,
                callback: function() {
                    this.fireEvent('aftermetadataload', this);
                },
                scope: this
            },
            border: false,
            frame: false,
            autoScroll: true
        }));
        
        if (this.permalink) {
            // TODO : Add viewpanel state (ie. size for window, tab)
            var l = GeoNetwork.Util.getBaseUrl(location.href) + "?uuid=" + this.metadataUuid;
            this.getTopToolbar().add(GeoNetwork.Util.buildPermalinkMenu(l));
        }
        
        this.addEvents(
                /** private: event[search]
                 *  Fires search.
                 */
                "aftermetadataload"
            );
        this.on({
            "aftermetadataload": this.afterMetadataLoad,
            scope: this
        });
    }
});

/** api: xtype = gn_view_viewpanel */
Ext.reg('gn_view_viewpanel', GeoNetwork.view.ViewPanel);
