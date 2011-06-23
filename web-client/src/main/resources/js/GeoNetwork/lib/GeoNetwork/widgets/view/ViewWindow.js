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
 *  class = ViewWindow
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor 
 *  .. class:: ViewWindow(config)
 *
 *     Create a GeoNetwork metadata view window
 *
 *
 */
GeoNetwork.view.ViewWindow = Ext.extend(Ext.Window, {
    defaultConfig: {
        layout: 'fit',
        width: 700,
        height: 740,
        border: false,
        autoScroll: true,
        closeAction: 'destroy',
        currTab: 'simple'
    },
    maximizable: true,
    maximized: false,
    collapsible: true,
    collapsed: false,
    serviceUrl: undefined,
    catalogue: undefined,
    metadataUuid: undefined,
    record: undefined,
    resultsView: undefined,
    actionMenu: undefined,
    /** 
     *  Get related metadata records for current metadata using xml.relation service.
     */
    getLinkedData : function() {
        var store = new GeoNetwork.data.MetadataRelationStore(this.catalogue.services.mdRelation + '?fast=false&uuid=' + this.metadataUuid, null, true),
            view = this;
        store.load();
        store.on('load', function(){
            this.each(view.displayLinkedData, view);
        });
    },
    /**
     * Display the record in the metadata related table (only available in simple view mode).
     */
    displayLinkedData: function(record){
        var table = Ext.query('table.related', this.body.dom),
            type = record.get('type');
        var el = Ext.get(table[0]);
        var exist = el.child('tr td[class*=' + type + ']');
        var link = '<li><a href="#" onclick="javascript:catalogue.metadataShow(\'' + 
            record.get('uuid') + '\');" ' + 
            'title="' + record.get('abstract') + '">' + 
            record.get('title') + '</a></li>';
        if (exist !== null) {
            exist.next().child('li').insertHtml('afterEnd', link);
        } else {
            el.child('tr').insertHtml('beforeBegin', '<tr><td class="main ' + type + '"><span class="cat-' + type +' icon">' + OpenLayers.i18n('related' + type) + '</span></td>' + 
            '<td><ul>' + link + '</ul></td></tr>');
        }
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
        var modes = Ext.query('span.mode', this.body.dom), menu = [], i, j, e, cmpId = this.getId();

        Ext.ux.Lightbox.register('a[rel^=lightbox-viewset]', true);

        for (i = 0; i < modes.length; i++) {
            if (modes[i].firstChild) {
                var id = modes[i].getAttribute('id');
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
                            
                            e.on('click', function(){
                                Ext.getCmp(cmpId).switchToTab(this);
                            }, e.getAttribute('id'));
                        }
                    }
                }
                
                menu.push([modes[i].innerHTML, id, activeMode]);
            }
        }
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
            this.currTab = item.value;
            this.load({
                url: this.serviceUrl + '&currTab=' + this.currTab,
                callback: this.afterMetadataLoad,
                scope: this
            });
        }
    },
    // TODO : duplicate from EditorToolBar - end
    afterMetadataLoad: function(){
        // Processing after content load
        this.updateViewMenu();
        
        // Create map panel for extent visualization
        this.catalogue.extentMap.initMapDiv();
        
        // Related metadata are only displayed in simple mode FIXME ?
        if (this.currTab === 'view-simple') {
            this.getLinkedData();
        }
    },
    /** private: method[initComponent] 
     *  Initializes the metadata view window.
     */
    initComponent: function(config){
        Ext.apply(this, config);
        Ext.applyIf(this, this.defaultConfig);
        this.tools = [{
            id: 'newwindow',
            qtip: OpenLayers.i18n('newWindow'),
            handler: function(e, toolEl, panel, tc){
                window.open(GeoNetwork.Util.getBaseUrl(location.href) + "#uuid=" + this.metadataUuid);
                this.hide();
            },
            scope: this
        }];
        this.tbar = [this.createViewMenu(), this.createActionMenu()];
        
        GeoNetwork.view.ViewWindow.superclass.initComponent.call(this);
        
        this.setTitle(this.record?this.record.get('title'):'');
        this.add(new Ext.Panel({
            autoLoad: {
                url: this.serviceUrl + '&currTab=' + this.currTab,
                callback: this.afterMetadataLoad,
                scope: this
            },
            border: false,
            frame: false,
            autoScroll: true
        }));
        
        this.on('beforeshow', function(el) {
            el.setSize(
                el.getWidth() > Ext.getBody().getWidth() ? Ext.getBody().getWidth() : el.getWidth(),
                el.getHeight() > Ext.getBody().getHeight() ? Ext.getBody().getHeight() : el.getHeight()); 
        });
    }
});

/** api: xtype = gn_view_viewwindow */
Ext.reg('gn_view_viewwindow', GeoNetwork.view.ViewWindow);
