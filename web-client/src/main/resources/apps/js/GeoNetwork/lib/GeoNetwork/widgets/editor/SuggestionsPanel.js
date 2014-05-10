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
Ext.namespace('GeoNetwork.editor');

/** api: (define)
 *  module = GeoNetwork.editor
 *  class = SuggestionsPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor 
 *  .. class:: SuggestionsPanel(config)
 *
 *      Create a GeoNetwork suggestion panel
 *      See metadata.suggestion service doc for more information.
 *
 */
GeoNetwork.editor.SuggestionsPanel = Ext.extend(Ext.Panel, {
    defaultConfig: {
        border: false,
        frame: false,
        iconCls: 'suggestionIcon',
        cls: 'suggestionPanel',
        title: undefined,
        collapsible: true,
        collapsed: true
    },
    metadataId: undefined,
    processName: undefined,
    processParams: undefined,
    processParametersWindow: undefined,
    catalogue: undefined,
    editor: undefined,
    store: undefined,
    /** private: method[clear] 
     *  Remove all suggestions from the store
     */
    clear: function() {
        if (this.store.getTotalCount()>0) this.store.removeAll();
    },
    reload: function(e, id){
        this.metadataId = id || this.metadataId;
        
        if (this.collapsed) {
            return;
        }
        this.store.reload({
            params: {
                id: this.metadataId
            }
        });
    },
    runSuggestedProcess: function(){
        if (this.processName) {
            if (this.processParams) {
                this.generateForm();
            } else {
                var action = this.catalogue.services.mdProcessing + 
                                "?id=" + this.metadataId + 
                                "&process=" + this.processName;
               
               this.run(action);
            }
        }
    },
    run: function(action){
         // TODO : save before process
         this.editor.process(action);
         
         if (this.processParametersWindow) {
             this.processParametersWindow.hide();
             this.processParametersWindow.destroy();
             this.processParametersWindow = undefined;
         }
    },
    selectionChangeEvent: function(dv, selections){
        var records = dv.getRecords(selections);
        
        if (records[0]) {
            this.getBottomToolbar().setDisabled(false);
            this.processName = records[0].get('name');
            this.processParams = records[0].get('params');
        } else {
            this.getBottomToolbar().setDisabled(true);
            this.processName = undefined;
            this.processParams = undefined;
        }
    },
    /** private: method[generateForm] 
     *  Create form according to process parameters description
     */
    generateForm: function() {
        this.processParametersWindow = undefined;   // Maybe not the best way to recreate the object
        // destroy does not unset this property
        
        if (!this.processParametersWindow) {
            var processParametersPanel = new Ext.form.FormPanel({
                defaultType: 'textfield',
                items: [{
                    name: 'id',
                    allowBlank: false,
                    hidden: true,
                    value: this.metadataId
                }, {
                    name: 'process',
                    allowBlank: false,
                    hidden: true,
                    value: this.processName
                }],
                buttons: [{
                    text: OpenLayers.i18n('process'),
                    iconCls: 'suggestionRunIcon',
                    ctCls: 'gn-bt-main',
                    handler: function(){
                        if (processParametersPanel.getForm().isValid()) {
                            var action = this.catalogue.services.mdProcessing + 
                                "?" + processParametersPanel.getForm().getValues(true);
                            
                            this.run(action);
                        }
                    },
                    scope: this
                }, {
                    text: OpenLayers.i18n('reset'),
                    iconCls: 'cancel',
                    handler: function(){
                        processParametersPanel.getForm().reset();
                    }
                }]
            });
            
            // Dynamically add form field according to process parameters.
            var config = Ext.util.JSON.decode(this.processParams), 
                xtype,
                defaultValue = '',
                hide = false,
                c;
            
            for (c in config) {
                if (config.hasOwnProperty(c)) {
                    if(config[c].type === 'boolean') {
                        xtype = 'checkbox';
                    } else {
                        xtype = 'textfield';
                    }
                    
                    // Set some reserved word default values
                    //  * gurl is geonetwork base url
                    //  * lang is set to current catalogue language
                    if(c === 'gurl' && this.catalogue.URL) {
                        defaultValue = this.catalogue.URL;
                        hide = true;
                    } else if(c === 'lang' && this.catalogue.LANG) {
                        defaultValue = this.catalogue.LANG;
                        hide = true;
                    } else {
                        hide = false;
                        defaultValue = config[c].defaultValue;
                    }
                    
                    processParametersPanel.add({
                        xtype: xtype,
                        name: c,
                        fieldLabel: OpenLayers.i18n(this.processName + c),
                        hidden: hide,
                        value: defaultValue
                    });
                }
            }
            
            
            this.processParametersWindow = new Ext.Window({
                title: OpenLayers.i18n('processParametersWindow'),
                width: 300,
                height: 300,
                layout: 'fit',
                modal: true,
                items: processParametersPanel,
                closeAction: 'destroy',
                constrain: true,
                iconCls: 'suggestionIcon'
            });
        }
        
        this.processParametersWindow.show();
        
        
    },
    /** private: method[initComponent] 
     *  Initializes the harvester panel.
     */
    initComponent: function(){
        Ext.applyIf(this, this.defaultConfig);

        this.title = OpenLayers.i18n('suggestions');
        this.tools = [{
            id : 'refresh',
            handler : function (e, toolEl, panel, tc) {
                panel.reload(panel, panel.metadataId);
            }
        }];
        
        // TODO : check suggestion service exist on the catalogue ?
        
        
        this.bbar = new Ext.Toolbar({
            disabled: true,
            items: [{
                text: OpenLayers.i18n('applySelected'),
                iconCls: 'suggestionRunIcon',
                handler: this.runSuggestedProcess,
                scope: this
            }]
        });
        
        var tpl = new Ext.XTemplate('<ul><tpl for=".">', 
                                        '<li class="suggestion" title="{name}">{desc}', 
                                            '<span class="suggestion-{operational}"></span></li>', 
                                    '</tpl></ul>');
        
        
        this.store = new GeoNetwork.data.SuggestionStore( 
                            this.catalogue.services.mdSuggestion, 
                            {action: 'analyze', id: this.metadataId}, 
                            false
                         );
        
        // TODO : Use a grouping grid if number of processes increase too much
        var grid = new Ext.DataView({
            store: this.store,
            tpl: tpl,
            singleSelect: true,
            autoHeight: true,
            loadingText: '...',
            selectedClass: 'suggestion-selected',
            overClass:'suggestion-over',
            itemSelector: 'li.suggestion',
            emptyText: OpenLayers.i18n('noSuggestion'),
            listeners: {
                selectionchange: this.selectionChangeEvent,
                scope: this
            }
        });
        
        GeoNetwork.editor.SuggestionsPanel.superclass.initComponent.call(this);
        this.add(grid);
        
        this.editor.on('editorClosed', this.clear, this);
        this.editor.on('metadataUpdated', this.reload, this);
        this.on('expand', this.reload);
    }
});

/** api: xtype = gn_editor_suggestionspanel */
Ext.reg('gn_editor_suggestionspanel', GeoNetwork.editor.SuggestionsPanel);
