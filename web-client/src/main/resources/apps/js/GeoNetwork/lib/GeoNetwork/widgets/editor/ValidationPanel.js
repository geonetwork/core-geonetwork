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
 *  class = ValidationPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor 
 *  .. class:: ValidationPanel(config)
 *
 *     Create a GeoNetwork validation panel for XSD and schematron reporting.
 *
 *
 */
GeoNetwork.editor.ValidationPanel = Ext.extend(Ext.Panel, {
    title: undefined,
    editor: undefined,
    metadataId: undefined,
    serviceUrl: undefined,
    store: undefined,
    defaultConfig: {
        border: false,
        frame: false,
        collapsible: true,
        collapsed: true,
        iconCls: 'validateMetadata',
        cls: 'validatePanel'
    },
    validate: function () {
        this.editor.validate(function () {
            this.updateValidationReport();
        }.bind(this));
    },
    updateValidationReport: function () {
        if (this.collapsed) {
            this.toggleCollapse();
        }
        this.reload(this);
    },
    /** private: method[clear] 
     *  Remove validation report from the store
     */
    clear: function () {
        if (this.store.getTotalCount()>0) this.store.removeAll();
    },
    reload: function (e, id) {
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
    /** private: method[initComponent] 
     *  Initializes the validation report panel.
     */
    initComponent: function () {
        Ext.applyIf(this, this.defaultConfig);
        
        this.title = OpenLayers.i18n('validationReport');
        this.tools = [{
            id : 'refresh',
            handler : function (e, toolEl, panel, tc) {
                panel.reload(panel, panel.metadataId);
            }
        }];
        
        GeoNetwork.editor.ValidationPanel.superclass.initComponent.call(this);
        
        this.store = new GeoNetwork.data.ValidationRuleStore( 
                            this.serviceUrl, 
                            {id: this.metadataId}, 
                            true
                         );
        
        var xg = Ext.grid;
        
        var groupTpl = function (value, p, record){
            var rules = record.store.query('group', record.data.group);
            return String.format(
                    '{0} ({1} {2})',
                    OpenLayers.i18n(record.data.group), rules.getCount(), 
                    (rules.getCount() > 1 ? OpenLayers.i18n('rules') : OpenLayers.i18n('rule'))
                    );
        };
        var tpl = function(value, p, record){
            return String.format(
                    '<h3 title="{2}">{0} {1}</h3><div>{3}</div>',
                    record.data.statusIcon, record.data.title, record.data.details, record.data.msg);
        };
        
        var colModel = new Ext.grid.ColumnModel({
            defaults: {
                width: 120,
                sortable: true
            },
            columns: [{
                id: 'group',
                header: OpenLayers.i18n('group'),
                width: 60,
                sortable: true,
                hidden: true,
                renderer: groupTpl,
                dataIndex: 'group'
            }, {
                header: OpenLayers.i18n('status'),
                width: 18,
                resizable: false,
                sortable: true,
                hidden: true,
                dataIndex: 'statusIcon'
            }, {
                header: OpenLayers.i18n('title'),
                hidden: false,
                renderer: tpl,
                dataIndex: 'title'
            }]
        });
        
        var grid = new xg.GridPanel({
            store: this.store,
            colModel: colModel,
            loadMask: true,
            view: new Ext.grid.GroupingView({
                forceFit: true,
                groupRenderer: groupTpl
            }),
            
            frame: false,
            height: 300,
            autoWidth: true
        });
        this.add(grid);
        
        this.editor.on('editorClosed', this.clear, this);
        this.editor.on('metadataUpdated', this.reload, this);
        this.on('expand', this.reload);
    }
});

/** api: xtype = gn_editor_validationpanel */
Ext.reg('gn_editor_validationpanel', GeoNetwork.editor.ValidationPanel);
