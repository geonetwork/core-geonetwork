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
 *  class = HelpPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor 
 *  .. class:: HelpPanel(config)
 *
 *     Create a GeoNetwork help panel
 *
 *
 */
GeoNetwork.editor.HelpPanel = Ext.extend(Ext.Panel, {
    defaultConfig: {
        border: false,
        frame: false,
        iconCls: 'book',
        title: undefined,
        collapsible: true,
        collapsed: false
    },
    editor: undefined,
    cache: {},
    /**
     * TODO : add depth parameter to display help on up sections until editor body root
     */
    loadHelp: function(id, sectionId, updateMessage, cb){
        var info = id.split('|');
        var panel = this;
        var schema = this.editor.metadataSchema;
        var requestBody = '<request><element schema="' + schema + 
                                '" name="' + info[1] + 
                                '" context="' + info[2] + 
                                '" fullContext="' + info[3] + 
                                '" isoType="' + info[4] + '" /></request>';
        
        OpenLayers.Request.POST({
            url: this.editor.catalogue.services.schemaInfo,
            data: requestBody,
            success: function(response){
                var help = response.responseXML.getElementsByTagName('element')[0];
                var title = help.getElementsByTagName('label')[0];
                var desc = help.getElementsByTagName('description')[0];
                var cond = help.getElementsByTagName('conditional')[0];
                
                // TODO : error message when no child
                var msg = "<div class='help'><span class='title'>" + 
                            (title ? title.firstChild.nodeValue : "") + 
                            "</span><br/> " + 
                            (desc && desc.firstChild ? desc.firstChild.nodeValue : "") + 
                            (cond && cond.firstChild ? "<br/> " + cond.firstChild.nodeValue : "") + 
                            "<br/>[" + schema + ":" + info[1] + "]" + 
                          "</div>";
                panel.cache[id] = msg;
                
                // -- Append section info to the message
                if (sectionId && panel.cache[sectionId]) {
                    msg += panel.cache[sectionId];
                }
                
                if (cb) {
                    panel.cb();
                }
                
                if (updateMessage) {
                    panel.update(panel.cache[id] + (panel.cache[sectionId] ? panel.cache[sectionId] : ""));
                }
            },
            failure: function(response){
            }
        });
        
    },
    updateHelp: function(id, section){
        // Don't load help when panel is collapsed to avoid too many useless queries
        if (this.collapsed) {
            return;
        }
        
        var key = id;
        var msg = '';
        var sectionId;
        var panel = this;
        
        if (section) {
            sectionId = section.first().getAttribute('id');
        }
        
        if (this.cache[id] && this.cache[sectionId]) {
            this.update(this.cache[id] + this.cache[sectionId]);
        } else {
            if (sectionId) {
                this.loadHelp(sectionId, null, false, panel.loadHelp(id, sectionId, true));
            } else {
                this.loadHelp(id, null, true);
            }
        }
    },
    /** private: method[initComponent] 
     *  Initializes the help panel.
     */
    initComponent: function(config){
        Ext.apply(this, config);
        Ext.applyIf(this, this.defaultConfig);
        
        this.title = OpenLayers.i18n('help');
        
        GeoNetwork.editor.HelpPanel.superclass.initComponent.call(this);
        panel = this;
    }
});

/** api: xtype = gn_editor_helppanel */
Ext.reg('gn_editor_helppanel', GeoNetwork.editor.HelpPanel);
