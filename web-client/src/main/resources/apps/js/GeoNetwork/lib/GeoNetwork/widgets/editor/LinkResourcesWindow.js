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
 *     Create a GeoNetwork metadata view window composed of a GeoNetwork.view.Panel
 *     to display a metadata record.
 *
 */
GeoNetwork.editor.LinkResourcesWindow = Ext.extend(Ext.Window, {
    defaultConfig: {
        layout: 'fit',
        width: 700,
        height: 740,
        border: false,
        modal: true,
        /** api: config[closeAction] 
         *  The close action. Default is 'destroy'.
         */
        closeAction: 'destroy',
        maximizable: false,
        maximized: false,
        collapsible: true,
        collapsed: false,
        uploadThumbnail: true,
        uploadDocument: false,
        metadataSchema: 'iso19139',
        protocolForServices: ['application/vnd.ogc.wms_xml', 'application/vnd.ogc.wfs_xml'],
        hiddenParameters: {
            service: [{
                name: 'E_type',
                value: 'service'
            }],
            dataset: [{
                name: 'E_type',
                value: 'dataset'
            }    // If dataset search should be restricted to ISO19139 or profil add criteria on schema. 
            //, {name: 'S__schema', value: 'iso19139'}
            ],
            fcats: [{
                name: 'E__schema',
                value: 'iso19110'
            }]
        }
    },
    processMap: {
        parent: 'parentIdentifier-update',
        fcats: 'update-attachFeatureCatalogue',
        service: 'update-srv-attachDataset',
        dataset: 'update-srv-attachDataset',
        sibling: 'sibling-add',
        onlinesrc: 'onlinesrc-add',
        thumbnail: 'thumbnail-from-url-add'
    },
    serviceUrl: undefined,
    serviceProtocol: undefined,
    catalogue: undefined,
    metadataUuid: undefined,
    metadataId: undefined,
    versionId: undefined,
    idField: undefined,
    versionField: undefined,
    mdStore: undefined,
    selectedMd: undefined,
    associationType: undefined,
    initiativeType: undefined,
    /**
     * Form for thumbnail
     */
    uploadForm: undefined,
    formPanel: undefined,
    panel: undefined,
    /** private: method[initComponent] 
     *  Initializes the metadata view window.
     */
    getPanel: function () {
        return this.panel;
    },
    generateTypeSwitcher: function () {
        
    },
    /**
     * Return a full text search input with search button.
     */
    getSearchInput: function () {
        return new GeoNetwork.form.SearchField({
            name: 'E_any',
            width: 240,
            store: this.mdStore,
            triggerAction: function (scope) {
                scope.doSearch();
                //scope.search('linkedMetadataGrid', null, null, 1, true, this.mdStore, null);
            },
            scope: this
        });
    },
    /**
     * Add hidden textfields in an item list.
     */
    getHiddenFormInput: function (items) {
        var i;
        Ext.each(this.hiddenParameters[this.type], function (item) {
            items.push({
                xtype: 'textfield',
                name: item.name,
                value: item.value,
                hidden: true
            });
        });
    },
    getInitiativeTypeStore: function () {
        return GeoNetwork.data.CodeListStore({
            url: catalogue.services.schemaInfo,
            schema: this.metadataSchema,
            codeListName: 'gmd:DS_InitiativeTypeCode'
        });
    },
    getAssociationTypeStore: function () {
        return GeoNetwork.data.CodeListStore({
            url: catalogue.services.schemaInfo,
            schema: this.metadataSchema,
            codeListName: 'gmd:DS_AssociationTypeCode'
        });
    },
    getFormFieldForSibling: function (items) {
        if (this.type === 'sibling') {

            var associationType = {
                xtype: 'combo',
                fieldLabel: OpenLayers.i18n('associationType'),
                store: this.getAssociationTypeStore(),
                valueField: 'code',
                displayField: 'label',
                mode: 'local',
                listeners: {
                    select: function (combo, record, index) {
                        this.associationType = combo.getValue();
                    },
                    scope: this
                }
            };
            
            var initiativeType = {
                xtype: 'combo',
                fieldLabel: OpenLayers.i18n('initiativeType'),
                store: this.getInitiativeTypeStore(),
                valueField: 'code',
                displayField: 'label',
                mode: 'local',
                listeners: {
                    select: function (combo, record, index) {
                        this.initiativeType = combo.getValue();
                    },
                    scope: this
                }
            };
            
            items.push([associationType, initiativeType]);
        }
    },
    getFormFieldForService: function (items) {
        // TODO : when the current record is a service provide the URL to init the capabiities
        if (this.type === 'service' || this.type === 'dataset') {
            this.capabilitiesStore = new GeoExt.data.WMSCapabilitiesStore({
                url: this.serviceUrl,
                id: 'capabilitiesStore',
                // FIXME
                proxy: new Ext.data.HttpProxy({
                    url: this.serviceUrl,
                    method: 'GET'
                }),
                listeners: {
                    exception: function (proxy, type, action, options, res, arg) {
                        Ext.MessageBox.alert(OpenLayers.i18n("error"));
                    },
                    beforeload: function () {
                        // Update store URL according to selected service.
//                        if (this.mode === 'attachService') {
//                            var selected = Ext.getCmp('linkedMetadataGrid').getSelectionModel().getSelections();
//                            if (selected === undefined || selected[0].data.uri === '') {
//                                Ext.MessageBox.alert(OpenLayers.i18n("noServiceURLError"));
//                            }
//                            this.capabilitiesStore.baseParams.url = selected[0].data.uri + 
//                                        "?&SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1";
//                        } else 
                        if (this.type === 'service') {
                            this.capabilitiesStore.baseParams.url = this.serviceUrl;
                        }
                    },
                    loadexception: function(){
                        Ext.MessageBox.alert(OpenLayers.i18n("GetCapabilitiesDocumentError") + this.capabilitiesStore.baseParams.url);
                    },
                    scope: this
                }
            });
            var combo = {
                xtype: 'combo',
                id: 'getCapabilitiesLayerNameCombo',
                fieldLabel: OpenLayers.i18n('getCapabilitiesLayer'),
                store: this.capabilitiesStore,
                valueField: 'name',
                displayField: 'title',
                triggerAction: 'all',
                //disabled: (serviceUrl==null?true:false),
                listeners: {
                    select: function(combo, record, index){
                        Ext.getCmp('getCapabilitiesLayerName').setValue(combo.getValue());
                    }
                }
            };
            var layerName = {
                xtype: 'textfield',
                id: 'getCapabilitiesLayerName',
                fieldLabel: OpenLayers.i18n('layerName'),
                valueField: 'name',
                displayField: 'title',
                listeners: {
                    'change': function (field) {
                        this.layerName = field.getValue();
                        console.log(this.layerName);
                    },
                    scope: this
                }
            };
            
            items.push([combo, layerName]);
        }
    },
    /**
     * Form for online resource. Online resource could be a document to upload 
     * a reference to a online resource of another metadata record or 
     * a URL to a document.
     * 
     * A document is described by a URL, name, description and protocol.
     * 
     */
    generateOnlineSrcForm: function () {
        
    },
    /**
     * Thumbnail form
     */
    generateThumbnailForm: function (cancelBt) {
        var self = this;
        
        this.idField = new Ext.form.TextField({
            xtype: 'textfield',
            name: 'id',
            value: this.metadataId,
            hidden: true
        });
        this.versionField = new Ext.form.TextField({
            name: 'version',
            value: this.versionId,
            hidden: true
        });
        
        // TODO : create a cusom widget to be shared with ThumbnailPanel
        this.uploadForm = new Ext.form.FormPanel({
            fileUpload: true,
            defaults: {
                width: 350
            },
            items: [this.idField, this.versionField, {
                xtype: 'radio',
                checked: this.uploadThumbnail,
                fieldLabel: OpenLayers.i18n('uploadAnImage'),
                name: 'type',
                inputValue: 'large',
                listeners: {
                    check: function (radio, checked) {
                        this.uploadThumbnail = checked;
                    },
                    scope: this
                }
            }, {
                xtype: 'textfield',
                name: 'scalingDir',
                value: 'width',
                hidden: true
            }, {
                xtype: 'textfield',
                name: 'smallScalingDir',
                value: 'width',
                hidden: true
            }, {
                xtype: 'textfield',
                name: 'type',
                value: 'large',
                hidden: true
            }, {
                xtype: 'textfield',
                name: 'scalingFactor',
                value: '1000',
                hidden: true
            }, {
                xtype: 'textfield',
                name: 'smallScalingFactor',
                value: '180',
                hidden: true
            }, {
                xtype: 'fileuploadfield',
                emptyText: OpenLayers.i18n('selectImage'),
                //fieldLabel: OpenLayers.i18n('image'),
                name: 'fname',
                //allowBlank: false,
                buttonText: '',
                buttonCfg: {
                    iconCls: 'thumbnailAddIcon'
                }
            }, {
                xtype: 'checkbox',
                checked: false,
                //hideLabel: true,
                fieldLabel: '',
                labelSeparator: '',
                boxLabel: OpenLayers.i18n('createSmall'),
                name: 'createSmall',
                value: 'false'
            }, {
                xtype: 'radio',
                fieldLabel: OpenLayers.i18n('setAURL'),
                name: 'type',
                inputValue: 'small',
                checked: !this.uploadThumbnail,
                listeners: {
                    check: function (radio, checked) {
                        this.uploadThumbnail = !checked;
                    },
                    scope: this
                }
            }, {
                xtype: 'textfield',
                name: 'url',
                value: '',
                listeners: {
                    change: function (field) {
                        this.serviceUrl = field.getValue();
                        // TODO If change check the URL box
                    },
                    scope: this
                }
            }],
            buttons: [{
                text: OpenLayers.i18n('upload'),
                formBind: true,
                iconCls: 'thumbnailGoIcon',
                scope: this,
                handler: function () {
                    if (this.uploadForm.getForm().isValid()) {
                        var panel = this;
                        if (this.uploadThumbnail) {
                            this.uploadForm.getForm().submit({
                                url: this.setThumbnail,
                                waitMsg: OpenLayers.i18n('uploading'),
                                success: function (fp, o) {
                                    self.editor.init(self.metadataId);
                                    self.hide();
                                }
                            });
                        } else {
                            this.runProcess();
                        }
                    }
                }
            }, cancelBt]
        });
        return this.uploadForm;
    },
    /**
     * A metadata search form with a grid
     * to select a record to link.
     */
    generateMetadataSearchForm: function (cancelBt) {
        var self = this;
        
        // Metadata relation
        this.mdStore = GeoNetwork.data.MetadataResultsFastStore();
        // Create grid with template list
        var checkboxSM = new Ext.grid.CheckboxSelectionModel({
            singleSelect: this.singleSelect,
            header: ''
        });
        
        var tplDescription = function (value, p, record) {
            var links = "";
            if (self.type === 'service' || record.data.links) {
                Ext.each(record.data.links, function (link) {
                    // FIXME: restrict
                    if (self.protocolForServices.join(',').indexOf(link.protocol) !== -1) {
                        links += '<li><a target="_blank" href="' + link.href + '">' + link.href + '</a></li>';
                        // FIXME : when service contains multiple URL 
                        record.data.serviceUrl = link.href;
                        record.data.serviceProtocol = link.protocol;
                    }
                });
                
            }
            return String.format(
                    '<span class="tplTitle">{0}</span><div class="tplDesc">{1}</div><ul>{2}</ul>',
                    record.data.title, record.data['abstract'], links);
        };
        // TODO : add URL for services
        
        var tplType = function (value, p, record) {
            var label = OpenLayers.i18n(record.data.type) || '';
            
            if (record.data.spatialRepresentationType) {
                label += " / " + OpenLayers.i18n(record.data.spatialRepresentationType);
            }
            
            return String.format('{0}', label);
        };
        
        var colModel = new Ext.grid.ColumnModel({
            defaults: {
                sortable: true
            },
            columns: [
                checkboxSM,
                {header: OpenLayers.i18n('metadatatype'), renderer: tplType, dataIndex: 'type'},
                {id: 'title', header: OpenLayers.i18n('title'), renderer: tplDescription, dataIndex: 'title'},
                {header: 'Schema', dataIndex: 'schema', hidden: true},
                {header: 'Link', dataIndex: 'link', hidden: true}
                // TODO add other columns
            ]
        });
        
        var grid = new Ext.grid.GridPanel({
            border: false,
            anchor: '100% 80%',
            store: this.mdStore,
            colModel: colModel,
            sm: checkboxSM,
            autoExpandColumn: 'title'
        });
        
        grid.getSelectionModel().on('rowselect', function (sm, rowIndex, r) {
            if (sm.getCount() !== 0) {
                this.selectedMd = r.data.uuid;
                this.serviceUrl = r.data.serviceUrl;
                this.serviceProtocol = r.data.serviceProtocol;
                
                // FIXME : only the first metadata link is selected
                this.selectedLink = r.data.links && r.data.links[0];
            } else {
                this.selectedMd = undefined;
            }
            //this.validate();
        }, this);
        
        // Focus on first row
        grid.getStore().on('load', function (store) {
            grid.getSelectionModel().selectFirstRow();
            grid.getView().focusEl.focus();
        }, grid);
        
        var cmp = [];
        cmp.push(this.getSearchInput());
        cmp.push(grid);
        this.getHiddenFormInput(cmp);
        this.getFormFieldForService(cmp);
        this.getFormFieldForSibling(cmp);
        

        
        this.formPanel = new Ext.form.FormPanel({
            items: cmp,
            buttons: [{
                text: OpenLayers.i18n('link'),
                iconCls: 'linkIcon',
                scope: this,
                handler: function () {
                    this.runProcess();
                }
            }, cancelBt]
        });
        return this.formPanel;
    },
    generateDocumentUploadForm: function (cancelBt) {
        var self = this;
        
        this.idField = new Ext.form.TextField({
            xtype: 'textfield',
            name: 'id',
            value: this.metadataId,
            hidden: true
        });
        this.versionField = new Ext.form.TextField({
            name: 'version',
            value: this.versionId,
            hidden: true
        });
        
        // TODO : create a cusom widget to be shared with ThumbnailPanel
        this.uploadForm = new Ext.form.FormPanel({
            fileUpload: true,
            defaults: {
                width: 350
            },
            items: [this.idField, this.versionField, 
//                    {
//                xtype: 'radio',
//                checked: this.uploadDocument,
//                fieldLabel: OpenLayers.i18n('uploadADocument'),
//                name: 'type',
//                listeners: {
//                    check: function (radio, checked) {
//                        this.uploadDocument = checked;
//                        // TODO : protocol is not needed
//                    },
//                    scope: this
//                }
//            }, {
//                name: 'access',
//                allowBlank: false,
//                hidden: true,
//                value: 'private' // FIXME
//            }, {
//                name: 'overwrite',
//                fieldLabel: 'Overwrite',
//                checked: true,
//                xtype: 'checkbox'
//            }, {
//                xtype: 'fileuploadfield',
//                emptyText: OpenLayers.i18n('selectFile'),
//                fieldLabel: 'File',
//                allowBlank: false,
//                name: 'f_' + ref,
//                buttonText: '',
//                buttonCfg: {
//                    iconCls: 'uploadIconAdd'
//                }
//            }, {
//                xtype: 'radio',
//                fieldLabel: OpenLayers.i18n('documentURL'),
//                name: 'type',
//                checked: !this.uploadDocument,
//                listeners: {
//                    check: function (radio, checked) {
//                        this.uploadDocument = !checked;
//                    },
//                    scope: this
//                }
//            }, 
              {
                xtype: 'textfield',
                fieldLabel: OpenLayers.i18n('url'),
                name: 'href',
                value: ''
            }, {
                xtype: 'textfield',
                fieldLabel: OpenLayers.i18n('name'),
                name: 'name',
                value: ''
            }, {
                xtype: 'textarea',
                fieldLabel: OpenLayers.i18n('desc'),
                name: 'title',
                value: ''
            }, {
                // TODO : should be a combo
                xtype: 'textfield',
                fieldLabel: OpenLayers.i18n('protocol'),
                name: 'protocol',
                value: ''
            }],
            buttons: [{
                text: OpenLayers.i18n('upload'),
                formBind: true,
                iconCls: 'thumbnailGoIcon',
                scope: this,
                handler: function () {
                    if (this.uploadForm.getForm().isValid()) {
                        var panel = this;
                        if (this.uploadDocument) {
//                            TODO This mode require more work
//                            The service should upload the document
//                            and update the metadata record
//                            instead of only returning the URL of the doc 
//                            to be added to the metadata 
//                            
//                            if (this.uploadForm.getForm().isValid()) {
//                                this.uploadForm.getForm().submit({
//                                    url: catalogue.services.upload,
//                                    waitMsg: OpenLayers.i18n('uploading'),
//                                    success: function(fileUploadPanel, o){
////                                        var fname = o.result.fname;
////                                        var name = Ext.getDom('_' + ref);
////                                        if (name) {
////                                            name.value = fname;
////                                        }
//                                        // Trigger update
//                                        self.save();
//                                        self.hide();
//                                    }
//                                    // TODO : improve error message
//                                    // Currently return  Unexpected token < from ext doDecode
//                                });
//                            }
                        } else {
                            var form = this.uploadForm.getForm();
                            this.selectedLink = this.uploadForm.getForm().getValues();
                            this.runProcess();
                        }
                    }
                }
            }, cancelBt]
        });
        return this.uploadForm;
    },
    /**
     * According to the type of resource to link build the
     * form to populate process parameters.
     */
    generateMode: function () {
        var self = this;
        
        var cancelBt = {
                text: OpenLayers.i18n('cancel'),
                iconCls: 'cancel',
                scope: this,
                handler: function () {
                    self.hide();
                }
            };
        
        if (this.type === 'thumbnail') {
            this.add(this.generateThumbnailForm(cancelBt));
        } else if (this.type === 'onlinesrc') {
//            this.add(this.generateMetadataSearchForm(cancelBt));
//            this.add(this.generateDocumentUploadForm());
            this.add(new Ext.TabPanel({
                activeTab: 0,
                items: [{
                    title: OpenLayers.i18n('linkADocument'),
                    layout: 'fit',
                    items: this.generateDocumentUploadForm(cancelBt)
                }, {
                    title: OpenLayers.i18n('linkAMetadata'),
                    layout: 'fit',
                    items: this.generateMetadataSearchForm(cancelBt)
                }]
            }));
        } else {
            
            this.add(this.generateMetadataSearchForm(cancelBt));
            // TODO : add filter
            this.doSearch();
            //this.catalogue.search({E_template: 'n'}, null, null, 1, true, this.mdStore, null);
        }
    },
    runProcess: function () {
        // Define which metadata to be modified
        // It could be the on in current editing or a related one
        var targetMetadataUuid = this.metadataUuid;
        var parameters = "";
        if (this.type === 'parent') {
            // Define the parent metadata record to link to
            parameters += "&parentUuid=" + this.selectedMd;
        } else if (this.type === 'fcats') {
            // Define the target feature catalogue to use
            parameters += "&uuidref=" + this.selectedMd;
        } else if (this.type === 'service' || this.type === 'dataset') {
            // Add a link from the current record to the target service
            // And 
            // Add a link in the distribution section of the dataset record
            parameters += "&uuidref=" + this.selectedMd;
            parameters += "&scopedName=" + this.layerName;
            parameters += "&url=" + this.serviceUrl;
            parameters += "&protocol=" + this.serviceProtocol;
        } else if (this.type === 'thumbnail') {
            // Attach a thumbnail by URL
            parameters += "&thumbnail_url=" + this.serviceUrl;
            // TODO : set name and description
        } else if (this.type === 'sibling') {
            parameters += "&uuidref=" + this.selectedMd + 
                            "&initiativeType=" + this.initiativeType + 
                            "&associationType=" + this.associationType;
        } else if (this.type === 'onlinesrc') {
            
            parameters += "&uuidref=" + (this.selectedMd ? this.selectedMd : "") + 
                "&url=" + this.selectedLink.href + 
                "&desc=" + this.selectedLink.title + 
                "&protocol=" + this.selectedLink.protocol + 
                "&name=" + this.selectedLink.name;
        }
        var action = this.catalogue.services.mdProcessing + 
            "?id=" + this.metadataId + 
            "&process=" + this.processMap[this.type] +
            parameters;
        
        this.editor.process(action);
        
        // Process the related record 
        // TODO : check for errors
        
        this.close();
    },
    doSearch: function () {
//        if (!this.loadingMask) {
//            this.loadingMask = new Ext.LoadMask(this.getEl(), {
//                msg: OpenLayers.i18n('searching')
//            });
//        }
//        this.loadingMask.show();
        
        GeoNetwork.editor.nbResultPerPage = 20;
        this.mdStore.removeAll();
        GeoNetwork.util.SearchTools.doQueryFromForm(this.formPanel.getId(), this.catalogue, 1, Ext.emptyFn, null, Ext.emptyFn, this.mdStore);
    },
    initComponent: function () {
        Ext.applyIf(this, this.defaultConfig);
        
        GeoNetwork.editor.LinkResourcesWindow.superclass.initComponent.call(this);
        
        this.setTitle(OpenLayers.i18n('linkAResource-' + this.type));
        
        this.generateMode();
        
        
        //this.panel = 
//        this.add(this.panel);
        
//        this.on('beforeshow', function(el) {
//            el.setSize(
//                el.getWidth() > Ext.getBody().getWidth() ? Ext.getBody().getWidth() : el.getWidth(),
//                el.getHeight() > Ext.getBody().getHeight() ? Ext.getBody().getHeight() : el.getHeight()); 
//        });
    }
});

/** api: xtype = gn_editor_linkresourceswindow */
Ext.reg('gn_editor_linkresourceswindow', GeoNetwork.editor.LinkResourcesWindow);
