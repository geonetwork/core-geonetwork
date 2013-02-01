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
 *  class = LinkedMetadataPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor 
 *  .. class:: LinkedMetadataPanel(config)
 *
 *     Create a GeoNetwork panel use to link metadata records
 *
 *
 */
GeoNetwork.editor.LinkedMetadataPanel = Ext.extend(Ext.Panel, {
    title: undefined,
    editor: undefined,
    metadataId: undefined,
    metadataUuid: undefined,
    selectedUuid: undefined,
    selectedType: undefined,
    metadataSchema: undefined,
    serviceUrl: undefined,
    store: undefined,
    parentAction: undefined,
    datasetAction: undefined,
    serviceAction: undefined,
    featureCatAction: undefined,
    delFeatureCatAction: undefined,
    updateChildrenAction: undefined,
    colModel: undefined,
    expander: undefined,
    processMap: {
        'parent-remove': 'parentIdentifier-remove',
        'children-remove': 'parentIdentifier-remove',
        'onlinesrc-remove': 'onlinesrc-remove',
        'fcats-remove': 'update-detachFeatureCatalogue',
        'datasets-remove': 'update-srv-detachDataset',
        'sibling-remove': 'sibling-remove',
        'thumbnail-remove': 'thumbnail-from-url-remove'
    },
    defaultConfig: {
        border: false,
        frame: false,
        sep: '^',
        iconCls: 'linkIcon',
        cls: 'linkPanel',
        collapsible: true,
        collapsed: false,
        resourcesTypes: {
            iso19139: ['thumbnail', 'onlinesrc', 'parent', 'children', 'service', 'dataset', 'fcats', 'sibling'],
            'iso19139.myocean': ['thumbnail', 'onlinesrc', 'sibling'],
            'iso19139.myocean.short': ['thumbnail'],
            'dublin-core': ['children']
        }, // TODO : add missing ones
        tpl: null
    },
    updatePanel: function () {
        this.reload();
    },
    /** private: method[clear] 
     *  Remove all related metadata from the store and clean the panel content.
     */
    clear: function () {
        this.store.removeAll();
        this.update('<div></div>');
    },
    reload: function (e, id, schema, version) {
        this.metadataId = id || this.metadataId;
        this.metadataUuid = document.mainForm.uuid.value;
        this.versionId = version || this.versionId;
        this.metadataSchema = schema || this.metadataSchema;
        if (this.collapsed) {
            return;
        }
        this.store.reload({
            params: {
                id: this.metadataId,
                fast: 'false'
            }
        });
    },
    updateStatus: function (store, records, options) {
    },
    getChildrenIds: function () {
        var uuidList = [];
        this.store.each(
            function (record) {
                if (record.get('type') === 'children') {
                    this.push(record.get('id'));
                }
            },
            uuidList
        );
        return uuidList;
    },
    addRelation: function (type) {
        var window, config = {
                type: type,
                editor: this.editor,
                // Try to retrieve current service URL if the current metadata is a service
                // The URL is stored in a hidden form of the editor
                mdServiceUrl: Ext.getDom('serviceUrl') && Ext.getDom('serviceUrl').value,
                catalogue: this.catalogue,
                metadataUuid: this.metadataUuid,
                metadataId: this.metadataId,
                versionId: this.versionId,
                metadataSchema: this.metadataSchema,
                getThumbnail: this.catalogue.services.mdGetThumbnail,
                setThumbnail: this.catalogue.services.mdSetThumbnail,
                unsetThumbnail: this.catalogue.services.mdUnsetThumbnail
            };
        if (this.metadataSchema === 'iso19139.myocean') {
            window = new GeoNetwork.editor.MyOceanLinkResourcesWindow(config);
        } else {
            window = new GeoNetwork.editor.LinkResourcesWindow(config);
        }
        window.show();
    },
    removeThumbnail: function (thumbnailType) {
        var panel = this,
            url = this.catalogue.services.mdUnsetThumbnail + '?id=' + this.metadataId + 
                                            '&version=' + this.versionId + 
                                            '&type=' + (thumbnailType === 'thumbnail' ? 'small':'large');
        
        OpenLayers.Request.GET({
            url: url,
            success: function (response) {
                panel.editor.init(panel.metadataId);
            },
            failure: function (response) {
            }
        });
    },
    removeRelation: function (type, uuid, id) {
        // Define which metadata to be modified
        // It could be the on in current editing or a related one
        var targetMetadataUuid = this.metadataUuid;
        var parameters = "";
        
        // Thumbnail upload to the catalog are usually retrieved using the resources.get service
        // Use a XSL process if there is no need to remove the thumbnail to the data dir
        // which is the case when using a URL
        if (type === 'thumbnail') {
            if (id.indexOf('resources.get') !== -1) {
                // uuid contains type of thumbnail
                this.removeThumbnail(uuid);
                return;
            } else {
                parameters += "&thumbnail_url=" + id;
            }
        } else if (type === 'children') {
            // Define the children metadata record to detach
            targetMetadataUuid = uuid;
        } else if (type === 'fcats') {
            // Define the target feature catalogue to detach
            parameters += "&uuidref=" + uuid;
        } else if (type === 'datasets') {
            parameters += "&uuidref=" + uuid;
            // TODO ? detach the service in the dataset record ?
        } else if (type === 'sibling') {
            parameters += "&uuidref=" + uuid;
        } else if (type === 'onlinesrc') {
            parameters += "&url=" + encodeURIComponent(id);
        }
        
        
//        console.log('remove:' + uuid + " target: " + targetMetadataUuid +
//                " type: " + type + 
//                " process: " + this.processMap[type + '-remove'] +
//                " param:" + parameters);
        
        var action = this.catalogue.services.mdProcessing + 
            "?uuid=" + targetMetadataUuid + 
            "&process=" + this.processMap[type + '-remove'] +
            parameters;
        
        if (targetMetadataUuid !== this.metadataUuid) {
            var request = Ext.Ajax.request({
                url: action,
                method: 'GET',
                success: function (result, request) {
                    console.log('children updated.');
                },
                failure: function (result, request) {
                    console.log('children not updated.');
                }
            });
        } else {
            this.editor.process(action);
        }
    },
    /** private: method[initComponent] 
     *  Initializes the help panel.
     */
    initComponent: function () {
        Ext.applyIf(this, this.defaultConfig);
        
        
        this.title = OpenLayers.i18n('relatedResources');
        this.tools = [{
            id : 'refresh',
            handler : function (e, toolEl, panel, tc) {
                panel.reload(panel, panel.metadataId);
            }
        }];
        this.tpl = new Ext.XTemplate(
            '<ul class="gn-relation-{type}">',
            '<tpl for=".">',
              '<tpl for="data">',
                '<tpl if="type === \'thumbnail\'">',
                  '<li alt="{title}"><img class="thumb-small" src="{id}"/>',
                    '<span class="button" id="remove' + this.sep + '{type}' + this.sep + '{title}' + this.sep + '{id}"></span>',
                    '<a rel="lightbox-set" class="md-mn lightBox" href="{id}"></a>',
                  '</li>',
                '</tpl>',
                '<tpl if="type !== \'thumbnail\'">',
                  '<li alt="{abstract}">' + 
                    '<tpl if="type === \'onlinesrc\'">',
                      '<a href="{id}" target="_blank">{title}</a> ',
                    '</tpl>',
                    '<tpl if="type !== \'onlinesrc\'">',
                      '{title} ',
                    '</tpl>',
                    '<tpl if="subType"><span class="relation-type">({subType})</span></tpl>' +
                    '<tpl if="type === \'onlinesrc\'">',
                      '<span class="button" id="remove' + this.sep + '{type}' + this.sep + '{title}' + this.sep + '{id}"></span>',
                    '</tpl>',
                    '<tpl if="type !== \'onlinesrc\'">',
                      '<span class="button" id="remove' + this.sep + '{type}' + this.sep + '{uuid}"></span></li>',
                    '</tpl>',
                '</tpl>',
              '</tpl>',
            '</tpl>',
            '</ul>'
        );
        
        GeoNetwork.editor.LinkedMetadataPanel.superclass.initComponent.call(this);
        
        var panel = this;
        
        this.store = new GeoNetwork.data.MetadataRelationStore(this.catalogue.services.mdRelation, {
            fast: false,
            id: this.metadataId
        }, true);
        
        
        this.store.on('load', function (store, records) {
//            console.log(records);
            // Generate HTML layout
            var html = '', schema = this.metadataSchema;
            
            // Hack to move to iso19139 schema for profil
            if (this.resourcesTypes[this.metadataSchema] === undefined) {
                schema = 'iso19139';
            }
            Ext.each(this.resourcesTypes[schema], function (type) {
                // Group title with a place for actions
                var id = 'add' + this.sep + type;
                html += '<h2>' + OpenLayers.i18n(type) + '<span class="button" id="' + id + '"></span>' + 
                    '</h2>';
                var mds = store.query('type', type);
                mds.items.type = type;
                html += this.tpl.apply(mds.items);
            }, this);
            this.update('<div>' + html + '</div>');
            
            // Register actions
            var buttons = Ext.query('.button', this.body.dom);
            Ext.each(buttons, function (button) {
                var bt, id = button.getAttribute('id');
                var info = id.split(panel.sep);
                
                if (info[0] === 'add') {
                    
                    // Provide update children action when editing the parent
                    if (info[1] === 'children') {
                        var chbt = new Ext.Button({
                            text: OpenLayers.i18n('updateChildren'),
                            renderTo: button,
                            handler: function () {
                                panel.editor.catalogue.modalAction(
                                        OpenLayers.i18n('updateChildren'), 
                                        panel.editor.catalogue.services.mdMassiveChildrenForm + "?parentUuid=" + document.mainForm.uuid.value + 
                                                "&schema=" + document.mainForm.schema.value + 
                                                "&childrenIds=" + panel.getChildrenIds().join(','));
                            }
                        });
                    } else {
                        bt = new Ext.Button({
                            text: OpenLayers.i18n('add'),
                            renderTo: button,
                            handler: function () {
                                panel.addRelation(info[1]);
                            }
                        });
                    }
                } else if (info[0] === 'remove') {

                    bt = new Ext.Button({
                        text: OpenLayers.i18n('remove'),
                        renderTo: button,
                        handler: function () {
                            panel.removeRelation(info[1], info[2], info[3]);
                        }
                    });
                } 
                
            });
            
        }, this);
        
        if (this.metadataId) {
            this.reload();
        }
        this.editor.on('editorClosed', this.clear, this);
        this.editor.on('metadataUpdated', this.reload, this);
        this.on('expand', this.reload);
        this.store.on('load', this.updateStatus, this);
    }
});

/** api: xtype = gn_editor_linkedmetadatapanel */
Ext.reg('gn_editor_linkedmetadatapanel', GeoNetwork.editor.LinkedMetadataPanel);
