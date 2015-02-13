/*
 * Copyright (C) 2001-2013 Food and Agriculture Organization of the
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
 *     Create a GeoNetwork panel use to display metadata records related resources.
 *     The panel provides action to create the different type of relation
 *     according to the metadata type using the GeoNetwork.editor.LinkResourcesWindow.
 *
 *  TODO: this panel could be used without a metadata in edit mode. 
 *  In that case, it will require to not update the related EditorPanel.
 */
GeoNetwork.editor.LinkedMetadataPanel = Ext.extend(Ext.Panel, {
    /** api: property[title] 
     * ``String`` Panel title
     */
    title: undefined,
    /** api: property[editor] 
     * ``String`` The metadata editor panel linked to this relation panel.
     * The editor panel is refreshed according to the related panel events.
     */
    editor: undefined,
    /** api: property[metadataId] 
     * ``String`` The metadata internal identifier (use to retrieve list of relation)
     * 
     * TODO: use only the UUID ?
     */
    metadataId: undefined,
    /** api: property[metadataUuid] 
     * ``String`` The metadata unique identifier
     */
    metadataUuid: undefined,
    /** api: property[metadataSchema] 
     * ``String`` The metadata schema. The configuration could be based on the schema.
     */
    metadataSchema: undefined,
    /** api: property[store] 
     * ``Ext.data.Store`` Store for the metadata relation.
     */
    store: undefined,
    /** api: property[imagePath] 
     * ``String`` Path to image (needed for item selector component).
     */
    imagePath: undefined,
    /** api: property[resourcesTypes] 
     * ``Object`` Type of resources to display
     */
    resourcesTypes: undefined,
    /** api: property[imagePath] 
     * ``boolean`` Defined when the metadata relation is loaded and set to
     * true if the record has children. If true, the update children action
     * is displayed.
     */
    hasChildren: false,
    /** api: property[defaultConfig] 
     * ``Object`` Default configuration
     */
    defaultConfig: {
        border: false,
        frame: false,
        /** api: property[sep] 
         *  Used in button identifier to properly initialized them.
         */
        sep: '^',
        iconCls: 'linkIcon',
        cls: 'linkPanel',
        collapsible: true,
        collapsed: false,
        /** api: config[addMenuByType] 
         *  Create menu for each type of relation if true. Dropdown menu with
         *  a list of action by default.
         */
        addMenuByType: false,
        /** api: property[resourcesTypesCfg] 
         *  Define configuration per schema. For ISO19139 profiles, all relations are displayed.
         */
        resourcesTypesCfg: {
            iso19139: ['thumbnail', 'onlinesrc', 'parent', 'children', 'service', 'dataset', 'sources', 'fcats', 'sibling'],
//            'iso19139.xyz': ['thumbnail', 'parent', 'children', 'service', 'dataset', 'fcats', 'sibling'],
            'dublin-core': ['children']
        },
        tpl: null
    },
    /** public: method[clear] 
     *  Remove all related metadata from the store and clean the panel content.
     */
    clear: function () {
        this.store.removeAll();
        this.update('<div></div>');
    },
    /** public: method[reload] 
     *  Reload the relation for the current metadata
     */
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
    /** public: method[getChildrenIds] 
     *  Return an array of the children's uuid.
     */
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
    /** public: method[addRelation] 
     *  Open the GeoNetwork.editor.LinkResourcesWindow to add a relation
     */
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
                setThumbnail: this.catalogue.services.mdSetThumbnail,
                bodyStyle: 'padding:10px;background-color:white',
                imagePath: this.imagePath
            };
        if (type === 'thumbnail') {
            config.height = 300;
            config.width = 500;
        } else if (type === 'onlinesrc' || type === 'service') {
            config.height = 700;
            config.width = 800;
        } else if (type === 'sibling') {
            config.height = 450;
            config.width = 700;
        }
        
        window = new GeoNetwork.editor.LinkResourcesWindow(config);
        window.show();
    },
    /** public: method[removeThumbnail] 
     *  Remove a thumbnail. It requires to call a custom service (not an XSL process)
     *  to properly delete the file.
     */
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
    /** public: method[removeUploadedFile] 
     *  Remove an uploaded file
     */
    removeUploadedFile: function (label, parameters) {
        var panel = this,
            url = this.catalogue.services.delResource + '?id=' + this.metadataId + parameters;
        
        OpenLayers.Request.GET({
            url: url,
            success: function (response) {
                panel.editor.init(panel.metadataId);
            },
            failure: function (response) {
            }
        });
    },
    /** public: method[removeRelation] 
     *  Remove a relation calling the appropiate XSL process with parameters.
     */
    removeRelation: function (type, uuid, id) {
        // Define which metadata to be modified
        // It could be the on in current editing or a related one
        var targetMetadataUuid = this.metadataUuid;
        var parameters = "", msg = "";
        
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
            msg = OpenLayers.i18n('ChildrenUpdated');
        } else if (type === 'fcats') {
            // Define the target feature catalogue to detach
            parameters += "&uuidref=" + uuid;
        } else if (type === 'datasets') {
            parameters += "&uuidref=" + uuid;
        } else if (type === 'sources') {
            parameters += "&sourceUuid=" + uuid;
        } else if (type === 'services') {
            parameters += "&uuidref=" + this.metadataUuid;
            targetMetadataUuid = uuid;
            msg = OpenLayers.i18n('RelatedServiceUpdated');
        } else if (type === 'sibling') {
            parameters += "&uuidref=" + uuid;
        } else if (type === 'onlinesrc') {
            parameters += "&url=" + encodeURIComponent(id);
            parameters += "&name=" + encodeURIComponent(uuid.trim().split(' ')[0]);
            
            // if a file is upload remove the file before removing the link
            if (uuid.indexOf('WWW:DOWNLOAD-1.0-http--download') !== -1) {
                this.removeUploadedFile(uuid, parameters);
                return;
            }
        }
        
        
//        console.log('remove:' + uuid + " target: " + targetMetadataUuid +
//                " type: " + type + 
//                " process: " + type + '-remove' +
//                " param:" + parameters);
        
        
        var action = this.catalogue.services.mdProcessing + 
            "?uuid=" + targetMetadataUuid + 
            "&process=" + type + '-remove' +
            parameters;
        
        if (targetMetadataUuid !== this.metadataUuid) {
            var request = Ext.Ajax.request({
                url: action,
                method: 'GET',
                success: function (result, request) {
                    GeoNetwork.Message().msg({
                        title: OpenLayers.i18n('info'),
                        msg: msg
                    });
                },
                failure: function (result, request) {
                    GeoNetwork.Message().msg({
                        title: OpenLayers.i18n('error'),
                        msg: result.responseText    // TODO improve
                    });
                }
            });
        } else {
            this.editor.process(action);
        }
    },
    /** private: method[generateAddMenu] 
     * Generate a menu of actions to add relation.
     */
    generateAddMenu: function () {
        var actions = [], schema = this.metadataSchema, panel = this;
        
        Ext.each(this.resourcesTypes, function (type) {
            // Provide update children action when editing the parent
            if (type === 'children') {
                if (this.hasChildren) {
                    actions.push(new Ext.Action({
                        text: OpenLayers.i18n('updateChildren'),
                        handler: function () {
                            panel.editor.catalogue.modalAction(
                                    OpenLayers.i18n('updateChildren'), 
                                    panel.editor.catalogue.services.mdMassiveChildrenForm + "?parentUuid=" + document.mainForm.uuid.value + 
                                            "&schema=" + document.mainForm.schema.value + 
                                            "&childrenIds=" + panel.getChildrenIds().join(','));
                        }
                    }));
                }
            } else {
                actions.push(new Ext.Action({
                    text: OpenLayers.i18n('add-' + type),
                    iconCls: 'cat-' + type,
                    handler: function () {
                        panel.addRelation(type);
                    }
                }));
            }
        }, this);
        
        var addMenu = new Ext.Button({
            menu: new Ext.menu.Menu({
                cls: 'links-mn', 
                items: actions
            }),
            iconCls: "addMenu",
            renderTo: "add-menu-content"
        });
        
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
                '<tpl if="type === \'thumbnail\' && sibling === false">',
                  '<li alt="{title}">',
                     '<tpl if="(typeof id != \'undefined\') && id != \'\'">',
                        '<a rel="lightbox-set" href="{id}"><img class="thumb-small" src="{id}"/></a>',
                     '</tpl>',
                    '<span class="button" id="remove' + this.sep + '{type}' + this.sep + '{title}' + this.sep + '{id}"></span>',
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
            // Generate HTML layout
            var html = '', schema = this.metadataSchema;
            
            this.hasChildren = false;
            
            // Default list of types to iso19139 schema for ISO profile
            if (this.metadataSchema.indexOf('iso19139.') !== -1 && 
                    this.resourcesTypesCfg[this.metadataSchema] === undefined) {
                this.resourcesTypes = this.resourcesTypesCfg['iso19139'];
            } else {
                this.resourcesTypes = this.resourcesTypesCfg[schema];
            }
            
            // Generate relation panel content according to the relation service response
            Ext.each(this.resourcesTypes, function (type) {
                // Group title with a place for actions
                var id = 'add' + this.sep + type;
                var mds = store.query('type', type);
                mds.items.type = type;
                if (panel.addMenuByType || (panel.addMenuByType === false && mds.items.length !== 0)) {
                    html += '<h2>' + OpenLayers.i18n(type) + '<span class="button" id="' + id + '"></span>' + 
                    '</h2>';
                   html += this.tpl.apply(mds.items);
                }
                
                if (type === 'children' && mds.items.length !== 0) {
                    this.hasChildren = true;
                }
            }, this);
            this.update('<div><div id="add-menu-content"></div>' + html + '</div>');
            
            
            
            if (!this.addMenuByType) {
                this.generateAddMenu();
            }
            
            // Register actions
            var buttons = Ext.query('.button', this.body.dom);
            Ext.each(buttons, function (button) {
                var bt, id = button.getAttribute('id');
                var info = id.split(panel.sep);
                
                if (panel.addMenuByType && info[0] === 'add') {
                    
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
    }
});

/** api: xtype = gn_editor_linkedmetadatapanel */
Ext.reg('gn_editor_linkedmetadatapanel', GeoNetwork.editor.LinkedMetadataPanel);
