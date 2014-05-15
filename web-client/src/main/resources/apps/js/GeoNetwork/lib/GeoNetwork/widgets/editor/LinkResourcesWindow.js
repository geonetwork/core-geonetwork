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
Ext.namespace('GeoNetwork.view');

/** api: (define)
 *  module = GeoNetwork.view
 *  class = LinkResourcesWindow
 *  base_link = `Ext.Window <http://extjs.com/deploy/dev/docs/?class=Ext.Window>`_
 */
/** api: constructor 
 *  .. class:: LinkResourcesWindow(config)
 *     
 *     Create a window which allows to set relationship between
 *     one metadata record and other type of resources. The metadata
 *     does not need to be in edit mode. All XSL processes are defined by
 *     metadata schema in the process folder of the schema.
 *     
 */
GeoNetwork.editor.LinkResourcesWindow = Ext.extend(Ext.Window, {
    defaultConfig: {
        layout: 'fit',
        width: 700,
        height: 740,
        border: false,
        modal: true,
        defaults: {
            border: false
        },
        /** api: config[closeAction] 
         *  The close action. Default is 'destroy'.
         */
        closeAction: 'destroy',
        maximizable: false,
        maximized: false,
        collapsible: true,
        collapsed: false,
        singleSelect: true,
        uploadThumbnail: true,
        uploadDocument: false,
        metadataSchema: 'iso19139',
        
        /**
         * relative imagePath for ItemSelector
         */
        imagePath: undefined,
        
        protocolForServices: ['application/vnd.ogc.wms_xml', 'application/vnd.ogc.wfs_xml'],
        /**
         * URL parameter separator mainly used
         * when multiple metadata record could be selected 
         * with extra descriptor (eg. initiative type and association 
         * type for a sibling.
         */
        separator: '%23',
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
            }],
            sibling: [],
            onlinesrc: []
        },
        /*
         * Configuration for sibling per profile and per initiative type
         * Required for some ISO profiles
         * Configuration for online source to restrict to a group
         */
        hiddenParametersValues: {
            sibling: {
                // Depends on schema
//                'iso19139.xyz': {
//                    // and depends on codelist value
//                    // Updated when codelist change
//                    document: [{
//                        id: 'E__groupPublished',
//                        value: 'A-GROUP'
//                    }]
//                }
            },
            onlinesrc: {
                // Depends on schema
                // Updated when window created
//                'iso19139.xyz': {
//                    'E__groupPublished': 'A-GROUP'
//                }
            }
        }
    },
    /**
     * The URL of the currently selected metadata
     */
    serviceUrl: undefined,
    /**
     * The URL of the current metadata in editing.
     * Define only if the metadata is a metadata of service.
     */
    mdServiceUrl: undefined,
    serviceProtocol: undefined,
    canEditTarget: false,
    catalogue: undefined,
    metadataUuid: undefined,
    metadataId: undefined,
    versionId: undefined,
    idField: undefined,
    versionField: undefined,
    mdStore: undefined,
    getCapabilitiesCombo: null,
    /**
     * Only used if multiple metadata selection form is provided.
     */
    mdSelectedStore: undefined,
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
            },
            scope: this
        });
    },
    /**
     * Add hidden textfields in an item list.
     */
    getHiddenFormInput: function (items) {
        var i, self = this;
        
        Ext.each(this.hiddenParameters[this.type], function (item) {
            var value = item.value;
            if (self.hiddenParametersValues[self.type] && self.hiddenParametersValues[self.type][self.metadataSchema]) {
                value = self.hiddenParametersValues[self.type][self.metadataSchema][item.name];
            }
            items.push({
                xtype: 'textfield',
                name: item.name,
                value: value,
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
    loadProtocolCodeListStore: function (protocolStore) {
        var protocols = GeoNetwork.util.HelpTools.get('|gmd:protocol|gmd:CI_OnlineResource|gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:protocol|', 
                this.metadataSchema, 
                catalogue.services.schemaInfo, function(protocols) {
            if (protocols.records[0]) {
                protocolStore.loadData(protocols.records[0].data.helper);
            }
        });
    },
    getFormFieldForSibling: function (items) {
        if (this.type === 'sibling') {

            var associationTypeCb = new Ext.form.ComboBox({
                fieldLabel: OpenLayers.i18n('associationType'),
                store: this.getAssociationTypeStore(),
                valueField: 'code',
                displayField: 'label',
                triggerAction: 'all',
                mode: 'local',
                listeners: {
                    select: function (combo, record, index) {
                        this.associationType = record;
                    },
                    scope: this
                }
            });
            
            associationTypeCb.getStore().on('load', function () {
                this.associationType = associationTypeCb.getStore().getAt(0);
                associationTypeCb.setValue(this.associationType.get('code'));
                // Hide the combo if only one value available
                if (associationTypeCb.getStore().getCount() === 1) {
                    associationTypeCb.setVisible(false);
                }
            }, this);
            
            var initiativeTypeCb = new Ext.form.ComboBox({
                fieldLabel: OpenLayers.i18n('initiativeType'),
                store: this.getInitiativeTypeStore(),
                valueField: 'code',
                displayField: 'label',
                mode: 'local',
                triggerAction: 'all',
                listeners: {
                    select: function (combo, record, index) {
                        this.initiativeType = record;
                        
                        var allValues = this.hiddenParametersValues.sibling[this.metadataSchema];
                        
                        var paramsValue = allValues && allValues[this.initiativeType.get('code')];
                        if (paramsValue) {
                            this.formPanel.getForm().setValues(paramsValue);
                            // Refresh search after form filter update
                            this.doSearch();
                        }
                    },
                    scope: this
                }
            });
            initiativeTypeCb.getStore().on('add', function () {
                var record = initiativeTypeCb.getStore().getAt(0);
                this.initiativeType = record;
                initiativeTypeCb.setValue(record.get('code'));
                initiativeTypeCb.fireEvent('select', initiativeTypeCb, this.initiativeType);
            }, this);
            
            items.push([associationTypeCb, initiativeTypeCb]);
        }
    },
    /**
     * Create a form to set the layer name. It could be a simple text
     * or selected from the service GetCapabilities information.
     * 
     * Only WMS service are supported.
     * 
     * TODO : Add other service type support.
     */
    getFormFieldForService: function (items) {
        var self = this;
        
        if (this.type === 'service' || this.type === 'dataset') {
            this.capabilitiesStore = new GeoExt.data.WMSCapabilitiesStore({
                url: (this.type === 'dataset' ? this.mdServiceUrl : this.serviceUrl),
                id: 'capabilitiesStore',
                proxy: new Ext.data.HttpProxy({
                    url: catalogue.services.proxy,
                    method: 'GET'
                }),
                listeners: {
                    exception: function (proxy, type, action, options, res, arg) {
                        GeoNetwork.Message().msg({
                            title: OpenLayers.i18n('error'),
                            msg: OpenLayers.i18n("GetCapabilitiesException")
                        });
                    },
                    loadexception: function () {
                        GeoNetwork.Message().msg({
                            title: OpenLayers.i18n('error'),
                            msg: OpenLayers.i18n("GetCapabilitiesDocumentError") + this.capabilitiesStore.baseParams.url
                        });
                    },
                    scope: this
                }
            });
            this.getCapabilitiesCombo = new Ext.form.ComboBox({
                fieldLabel: OpenLayers.i18n('getCapabilitiesLayer'),
                store: this.capabilitiesStore,
                valueField: 'name',
                displayField: 'title',
                triggerAction: 'all',
                listeners: {
                    select: function (combo, record, index) {
                        Ext.getCmp('getCapabilitiesLayerName').setValue(combo.getValue());
                    }
                }
            });
            var layerName = {
                xtype: 'textfield',
                id: 'getCapabilitiesLayerName',
                fieldLabel: OpenLayers.i18n('layerName'),
                valueField: 'name',
                displayField: 'title',
                listeners: {
                    'change': function (field) {
                        this.layerName = field.getValue();
                    },
                    scope: this
                }
            };
            
            items.push([this.getCapabilitiesCombo, layerName]);
        }
    },
    /**
     * Thumbnail form
     */
    generateThumbnailForm: function (cancelBt) {
        var self = this;
        
        this.createBt = new Ext.Button({
            text: OpenLayers.i18n('upload'),
            formBind: true,
            iconCls: 'thumbnailGoIcon',
            ctCls: 'gn-bt-main',
            scope: this,
            disabled: true,
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
        });
        
        this.idField = new Ext.form.TextField({
            name: 'id',
            value: this.metadataId,
            hidden: true
        });
        this.versionField = new Ext.form.TextField({
            name: 'version',
            value: this.versionId,
            hidden: true
        });
        this.previewImage = new Ext.BoxComponent({
            autoEl: {
                tag: 'img',
                'class': 'thumb-small',
                src: catalogue.URL + '/apps/images/default/nopreview.png'
            }
        });
        var urlField = new Ext.form.TextField({
                name: 'url',
                value: '',
                anchor: '100%',
                hideLabel: true,
                validator: function (value) {
                    if (self.uploadThumbnail !== true) {
                        var isUrl = Ext.form.VTypes.url(value);
                        if (isUrl) {
                            // Display the image
                            self.previewImage.getEl().set({'src': value});
                            return true;
                        } else {
                            self.createBt.setDisabled(true);
                            return false;
                        }
                    } else {
                        return true;
                    }
                },
                listeners: {
                    change: function (field) {
                        var value = field.getValue();
                        this.serviceUrl = value;
                        
                        if (value !== "") {
                            self.createBt.setDisabled(false);
                        }
                    },
                    scope: this
                }
            }),
            // A file upload field for the thumbnail
            fileUploadField = new Ext.form.FileUploadField({
                emptyText: OpenLayers.i18n('selectImage'),
                name: 'fname',
                width: 300,
                hideLabel: true,
                buttonText: '',
                buttonCfg: {
                    iconCls: 'thumbnailAddIcon'
                },
                listeners: {
                    fileselected: function (cmp, value) {
                        self.createBt.setDisabled(value === "");
                    }
                }
            });
        
        var fsUpload = new Ext.form.FieldSet({
            checkboxToggle: true,
            title: OpenLayers.i18n('uploadAnImage'),
            collapsed: !this.uploadThumbnail,
            items: [fileUploadField, {
                xtype: 'checkbox',
                checked: false,
                fieldLabel: '',
                hideLabel: true,
                labelSeparator: '',
                boxLabel: OpenLayers.i18n('createSmall'),
                name: 'createSmall',
                value: 'false'
            }],
            listeners: {
                collapse: {
                    fn: function () {
                        if (fsUrl.collapsed) {
                            fsUrl.expand();
                        }
                    },
                    scope: this
                },
                expand: {
                    fn: function () {
                        this.uploadThumbnail = true;
                        if(!fsUrl.collapsed) {
                            fsUrl.collapse();
                        }
                    },
                    scope: this
                }
            }
        });
        
        var fsUrl = new Ext.form.FieldSet({
            checkboxToggle: true,
            title: OpenLayers.i18n('setAThumbnailByURL'),
            collapsed: this.uploadThumbnail,
            items: [urlField, this.previewImage, {
                id: 'thumbnail_desc',
                name: 'thumbnail_desc',
                xtype: 'textfield',
                anchor: '100%',
                hideLabel: false,
                fieldLabel: OpenLayers.i18n('Description')
            }],
            listeners: {
                collapse: {
                    fn: function () {
                        if (fsUpload.collapsed) {
                            fsUpload.expand();
                        }
                    },
                    scope: this
                },
                expand: {
                    fn: function () {
                        this.uploadThumbnail = false;
                        if (!fsUpload.collapsed) {
                            fsUpload.collapse();
                        }
                    },
                    scope: this
                }
            }
        });
        
        // TODO : deprecate ThumbnailPanel
        this.uploadForm = new Ext.form.FormPanel({
            fileUpload: true,
            labelWidth: 90,
            defaults: {
//                hideLabels: true,
                xtype: 'textfield'
            },
            anchor: '80%',
            items: [this.idField, this.versionField, {  
                name: 'scalingDir',
                value: 'width',
                hidden: true
            }, {
                name: 'smallScalingDir',
                value: 'width',
                hidden: true
            }, {
                name: 'type',
                value: 'large',
                hidden: true
            }, {
                name: 'scalingFactor',
                value: '1000',
                hidden: true
            }, {
                xtype: 'textfield',
                name: 'smallScalingFactor',
                value: '180',
                hidden: true
            }, fsUpload, fsUrl],
            buttons: [this.createBt, cancelBt]
        });
        return this.uploadForm;
    },
    /**
     * Form for online resource 
     */
    generateDocumentUploadForm: function (cancelBt) {
        var self = this, protocolStore = new Ext.data.JsonStore({
            fields: ['label', 'title', 'value']
        });
        
        this.loadProtocolCodeListStore(protocolStore);
        
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
        

        var fsUrl = new Ext.form.FieldSet({
            checkboxToggle: true,
            title: OpenLayers.i18n('setAURL'),
            collapsed: this.uploadDocument,
            items: [{
                xtype: 'textfield',
                fieldLabel: OpenLayers.i18n('URL'),
                name: 'href',
                value: ''
            }],
            listeners: {
                collapse: {
                    fn: function () {
                        if (fsUpload.collapsed) {
                            fsUpload.expand();
                        }
                    },
                    scope: this
                },
                expand: {
                    fn: function () {
                        this.uploadDocument = false;
                        
                        protocolCombo.setVisible(true);
                        
                        if (!fsUpload.collapsed) {
                            fsUpload.collapse();
                        }
                    },
                    scope: this
                }
            }
        });
        
        var protocolCombo = new Ext.form.ComboBox({
            name: 'protocol',
            fieldLabel: OpenLayers.i18n('Protocol'),
            store: protocolStore,
            valueField: 'value',
            displayField: 'label',
            triggerAction: 'all',
            mode: 'local'
        });
        
        
        var fsUpload = new Ext.form.FieldSet({
            checkboxToggle: true,
            title: OpenLayers.i18n('selectFile'),
            collapsed: !this.uploadDocument,
            items: [{
                xtype: 'fileuploadfield',
                emptyText: OpenLayers.i18n('selectFile'),
                fieldLabel: OpenLayers.i18n('File'),
//                allowBlank: false,TODO validation
                name: 'filename',
                buttonText: '',
                buttonCfg: {
                    iconCls: 'uploadIconAdd'
                }
            }, {
                name: 'overwrite',
                fieldLabel: OpenLayers.i18n('Overwrite'),
                checked: true,
                xtype: 'checkbox'
            }],
            listeners: {
                collapse: {
                    fn: function () {
                        if (fsUrl.collapsed) {
                            fsUrl.expand();
                        }
                    },
                    scope: this
                },
                expand: {
                    fn: function () {
                        this.uploadDocument = true;
                        
                        protocolCombo.setVisible(false);
                        
                        if (!fsUrl.collapsed) {
                            fsUrl.collapse();
                        }
                    },
                    scope: this
                }
            }
        });
        
        
        this.uploadForm = new Ext.form.FormPanel({
            fileUpload: true,
            defaults: {
                width: 350
            },
            items: [this.idField, this.versionField, 
            {
                name: 'access',
                allowBlank: false,
                hidden: true,
                value: 'private' // FIXME
            }, fsUpload, fsUrl, 
            {
                xtype: 'textfield',
                fieldLabel: OpenLayers.i18n('Name'),
                name: 'name',
                value: ''
            }, {
                xtype: 'textarea',
                fieldLabel: OpenLayers.i18n('Description'),
                name: 'title',
                value: ''
            }, protocolCombo
            ],
            buttons: [{
                text: OpenLayers.i18n('upload'),
                formBind: true,
                iconCls: 'thumbnailGoIcon',
                scope: this,
                handler: function () {
                    if (this.uploadForm.getForm().isValid()) {
                        var self = this;
                        if (this.uploadDocument) {
                            this.uploadForm.getForm().submit({
                                url: catalogue.services.uploadResource,
                                waitMsg: OpenLayers.i18n('uploading'),
                                success: function (fp, o) {
                                    self.editor.init(self.metadataId);
                                    self.hide();
                                },
                                failure: function (response) {
                                    GeoNetwork.Message().msg({
                                        title: OpenLayers.i18n('error'),
                                        msg: OpenLayers.i18n("UploadError")
                                    });
                                }
                            });
                        } else {
                            this.selectedLink = this.uploadForm.getForm().getValues();
                            this.selectedLink.protocol = protocolCombo.getValue();
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
     * to select a record to link (deprecated).
     */
    generateMetadataSearchForm: function (cancelBt) {
        var self = this;
        
        // Metadata relation
        this.mdStore = GeoNetwork.data.MetadataResultsFastStore();
        // Create grid with template list
        var checkboxSM = new Ext.grid.CheckboxSelectionModel({
            singleSelect: self.type === 'onlinesrc' ? false : this.singleSelect,
            header: ''
        });
        
        var tplDescription = function (value, p, record) {
            var links = "";
            if (self.type === 'service') {
                Ext.each(record.data.links, function (link) {
                    // FIXME: restrict
                    if (self.protocolForServices.join(',').indexOf(link.type) !== -1) {
                        links += '<li><a target="_blank" href="' + link.href + '">' + link.href + '</a></li>';
                        // FIXME : when service contains multiple URL 
                        record.data.serviceUrl = link.href;
                        record.data.serviceProtocol = link.protocol;
                    }
                });
            } else if (record.data.links && self.type === 'onlinesrc') {
                Ext.each(record.data.links, function (link) {
                    // FIXME: restrict
                    links += '<li><a target="_blank" href="' + link.href + '">' + link.href + '</a></li>';
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
            store: this.mdStore,
            colModel: colModel,
            sm: checkboxSM,
            autoExpandColumn: 'title',
						anchor: '100% 80%'
        });
        
        grid.getSelectionModel().on('rowselect', function (sm, rowIndex, r) {
            if (sm.getCount() !== 0) {
                this.selectedMd = r.data.uuid;
                this.serviceUrl = r.data.serviceUrl;
                this.serviceProtocol = r.data.serviceProtocol;
                this.canEditTarget = r.data.edit === 'true';
                // FIXME : only the first metadata link is selected
                this.selectedLink = r.data.links;
                
                var url;
                // If linking a service, the combo needs to be updated
                // every time user select a new record in the results list.
                // Disable the getCapabilities combo
                if (this.type === 'service') {
                    url = this.serviceUrl;
                    this.getCapabilitiesCombo.setDisabled(url === undefined);
                } else if (this.type === 'dataset') {
                    url = this.mdServiceUrl;
                }
                
                // Reload the capabilities
                if (url !== undefined) {
                    if (url.indexOf('GetCapabilities') === -1) {
                        url += (url.indexOf("?") === -1 ? "?" : "") + "&SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1";
                    }
                    this.capabilitiesStore.baseParams.url = url;
                    this.capabilitiesStore.reload({params: {url: url}});
                } else {
                    this.capabilitiesStore && this.capabilitiesStore.removeAll();
                }
                
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
				var formCmp = [];
        this.getHiddenFormInput(formCmp);
        this.getFormFieldForSibling(formCmp);
        formCmp.push(this.getSearchInput());

				cmp.push(new Ext.form.FormPanel({ 
					anchor: '100%',
					items: formCmp,
				}));
        cmp.push(grid);

				var serviceCmp = [];
				this.getFormFieldForService(serviceCmp);
				cmp.push(new Ext.form.FormPanel({ 
					anchor: '100%',
					items: serviceCmp
				}));
       
			 	// This should be Ext.form.FormPanel but that doesn't lay out
				// the gridpanel properly (no rows displayed)!
        this.formPanel = new Ext.Panel({
						layout: 'anchor', // wouldn't be necessary in formpanel
            items: cmp,
            buttons: [{
                text: OpenLayers.i18n('createLink'),
                iconCls: 'linkIcon',
                ctCls: 'gn-bt-main',
                scope: this,
                handler: function () {
                    this.runProcess();
                }
            }, cancelBt]
        });
        return this.formPanel;
    },
    getMultipleMetadataSelectorForSibling: function (cancelBt) {
        var self = this;
        
        this.mdSelectedStore = GeoNetwork.data.MetadataResultsFastStore();
        this.mdStore = GeoNetwork.data.MetadataResultsFastStore();
        
        var fromTpl = new Ext.XTemplate(
                '<tpl for=".">',
                    // TODO : add keyword definiton ?
                    '<div class="ux-mselect-item">{title}</div>',
                '</tpl>'
            ), toTpl = new Ext.XTemplate(
                '<tpl for=".">',
                    // TODO : add keyword definiton ?
                    '<div class="ux-mselect-item">{title} ({associationTypeLabel} > {initiativeTypeLabel})</div>',
                '</tpl>'
            );
        
        var cmp = [];
        this.getHiddenFormInput(cmp);
        this.getFormFieldForSibling(cmp);

        cmp.push(this.getSearchInput());
        
        var itemSelector = new Ext.ux.ItemSelector({
            dataFields: ["title"],
            //toData: [],
            toStore: this.mdSelectedStore,
            msWidth: 300,
            msHeight: 260,
            valueField: "value",
            hideLabel: true,
            toSortField: undefined,
            fromTpl: fromTpl,
            toTpl: toTpl,
            toLegend: OpenLayers.i18n('Selected'),
            fromLegend: OpenLayers.i18n('Found'),
            fromStore: this.mdStore,
            fromAllowTrash: false,
            fromAllowDup: true,
            toAllowDup: false,
            drawUpIcon: false,
            drawDownIcon: false,
            drawTopIcon: false,
            drawBotIcon: false,
            imagePath: this.imagePath,
            toTBar: [{
                // control to clear all select keywwords and refresh the XML.
                text: OpenLayers.i18n('clear'),
                handler: function () {
                    var i = itemSelector;
                    itemSelector.reset.call(i);
                },
                scope: this
            }]
        });
        
        // Add the initiativeType and associationType info to
        // the added record.
        this.mdSelectedStore.on('add', function (store, records, index) {
            Ext.each(records, function (record) {
                record.data.initiativeType = self.initiativeType.get('code');
                record.data.associationType = self.associationType.get('code');
                record.data.initiativeTypeLabel = self.initiativeType.get('label');
                record.data.associationTypeLabel = self.associationType.get('label');
            });
        });
        
        cmp.push(itemSelector);
        
        this.formPanel = new Ext.form.FormPanel({
            items: cmp,
            border: false,
            buttons: [{
                text: OpenLayers.i18n('createLink'),
                iconCls: 'linkIcon',
                ctCls: 'gn-bt-main',
                scope: this,
                handler: function () {
                    this.runProcess();
                }
            }, cancelBt]
        });
        return this.formPanel;
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
            this.add(this.generateDocumentUploadForm(cancelBt));
        } else if (this.type === 'sibling') {
            this.add(this.getMultipleMetadataSelectorForSibling(cancelBt));
        } else {
            
            this.add(this.generateMetadataSearchForm(cancelBt));
            this.doSearch();
        }
    },
    runProcess: function () {
        // Define which metadata to be modified
        // It could be the on in current editing or a related one
        var targetMetadataUuid = this.metadataUuid, parameters = "";
        this.layerName = Ext.getCmp('getCapabilitiesLayerName') && Ext.getCmp('getCapabilitiesLayerName').getValue();
        
        if (this.type === 'parent') {
            // Define the parent metadata record to link to
            parameters += "&parentUuid=" + this.selectedMd;
        } else if (this.type === 'sources') {
            // Define the parent metadata record to link to
            parameters += "&sourceUuid=" + this.selectedMd;
        } else if (this.type === 'fcats') {
            // Define the target feature catalogue to use
            parameters += "&uuidref=" + this.selectedMd;
            
        } else if (this.type === 'service') {
            // Add a link from the current record to the target service if privileges
            if (this.canEditTarget) {
                // Current dataset is a dataset metadata record.
                // 1. Update service (if current user has privileges), using XHR request
                var serviceUpdateUrl = this.catalogue.services.mdProcessingXml + 
                                            "?uuid=" + this.selectedMd + 
                                            "&process=dataset-add" + 
                                            "&uuidref=" + targetMetadataUuid +
                                            "&scopedName=" + this.layerName;
                // TODO : it looks like the dataset identifier and not the 
                // metadata UUID should be set in the operatesOn element of 
                // the service metadata record.
                
                Ext.Ajax.request({
                    url: serviceUpdateUrl,
                    method: 'GET',
                    success: function (result, request) {
                    },
                    failure: function (result, request) {
                        GeoNetwork.Message().msg({
                            title: OpenLayers.i18n('error'),
                            msg: OpenLayers.i18n("ServiceUpdateError")
                        });
                    }
                });
            } else {
                // TODO : add a warning
            }
            // And 
            // Add a link in the distribution section of the dataset record
            parameters += "&uuidref=" + this.selectedMd;
            parameters += "&scopedName=" + this.layerName;
            parameters += "&desc=" + this.layerName;
            parameters += "&url=" + this.serviceUrl;
            parameters += "&protocol=" + this.serviceProtocol;
        } else if (this.type === 'dataset') {
            // Current dataset is a service metadata record.
            // 1. Update dataset (if current user has privileges), using XHR request
            if (this.canEditTarget) {
                var serviceUpdateUrl = this.catalogue.services.mdProcessingXml + 
                                            "?uuid=" + this.selectedMd + 
                                            "&process=onlinesrc-add" + 
                                            "&desc=" + this.layerName + 
                                            "&url=" + this.serviceUrl + 
                                            "&uuidref=" + targetMetadataUuid +
                                            "&name=" + this.layerName;
                // TODO : it looks like the dataset identifier and not the 
                // metadata UUID should be set in the operatesOn element of 
                // the service metadata record.
                
                Ext.Ajax.request({
                    url: serviceUpdateUrl,
                    method: 'GET',
                    success: function (result, request) {
                        GeoNetwork.Message().msg({
                            title: OpenLayers.i18n('message'),
                            msg: OpenLayers.i18n("DatasetUpdateSuccess")
                        });
                    },
                    failure: function (result, request) {
                        GeoNetwork.Message().msg({
                            title: OpenLayers.i18n('error'),
                            msg: OpenLayers.i18n("DatasetUpdateError")
                        });
                    }
                });
            } else {
                // TODO : add a warning
            }
            
            // Add a link from the current record to the target service
            // And 
            // Add a link in the distribution section of the dataset record
            parameters += "&uuidref=" + this.selectedMd;
            parameters += "&scopedName=" + this.layerName;
            parameters += "&url=" + this.serviceUrl;
            parameters += "&protocol=" + this.serviceProtocol;
        } else if (this.type === 'thumbnail') {
            // Attach a thumbnail by URL
            parameters += "&thumbnail_url=" + encodeURIComponent(this.serviceUrl) + 
                "&thumbnail_desc=" + Ext.getCmp('thumbnail_desc').getValue();
            // TODO : set name and description
        } else if (this.type === 'sibling') {
         // Combine all links if multiple selection is available
            if (this.mdSelectedStore && this.mdSelectedStore.getCount() > 0) {
                var uuids = [], sep = this.separator;
                this.mdSelectedStore.each(function (record) {
                    uuids.push(record.get('uuid') + 
                            sep + record.get('associationType') + 
                            sep + record.get('initiativeType'));
                });
                parameters += "&uuids=" + uuids.join(',');
            } 
            parameters += "&uuidref=" + (this.selectedMd ? this.selectedMd : "") + 
                            "&initiativeType=" + this.initiativeType.get('code') + 
                            "&associationType=" + this.associationType.get('code');
            
        } else if (this.type === 'onlinesrc') {
            // Combine all links if multiple selection is available
            if (this.mdSelectedStore && this.mdSelectedStore.getCount() > 0) {
                this.mdSelectedStore.each(function (record) {
                    parameters += "&extra_metadata_uuid=" + record.get('uuid');
                });
            } else {
                parameters += "&extra_metadata_uuid=" + (this.selectedMd ? this.selectedMd : "");
                if (this.selectedLink.href) {
                    parameters += "&url=" + this.selectedLink.href + 
                        "&desc=" + this.selectedLink.title + 
                        "&protocol=" + this.selectedLink.protocol + 
                        "&name=" + this.selectedLink.name;
                }
            }
        }
        var action = this.catalogue.services.mdProcessing + 
            "?id=" + this.metadataId + 
            "&process=" + this.type + "-add" +
            parameters;
        
        this.editor.process(action);
        
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
    }
});

GeoNetwork.editor.MyOceanLinkResourcesWindow = Ext.extend(GeoNetwork.editor.LinkResourcesWindow, {
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
            this.add(this.generateMultipleMetadataSelector(cancelBt));
        } else if (this.type === 'sibling') {
            this.add(this.getMultipleMetadataSelectorForSibling(cancelBt));
        } else {
            
            this.add(this.generateMetadataSearchForm(cancelBt));
            // TODO : add filter
            this.doSearch();
            //this.catalogue.search({E_template: 'n'}, null, null, 1, true, this.mdStore, null);
        }
    },
    
    /**
     * Custom for MyOcean to allow multiple selection
     */
    generateMultipleMetadataSelector: function (cancelBt) {
        
        this.mdSelectedStore = GeoNetwork.data.MetadataResultsFastStore();
        this.mdStore = GeoNetwork.data.MetadataResultsFastStore();
        
        var tpl = new Ext.XTemplate(
                '<tpl for=".">',
                    // TODO : add keyword definiton ?
                    '<div class="ux-mselect-item">{title}</div>',
                '</tpl>'
            );

        var cmp = [];
        this.getHiddenFormInput(cmp);
        cmp.push(this.getSearchInput());
        
        var itemSelector = new Ext.ux.ItemSelector({
            dataFields: ["title"],
            //toData: [],
            toStore: this.mdSelectedStore,
            msWidth: 320,
            msHeight: 260,
            valueField: "value",
            hideLabel: true,
            toSortField: undefined,
            fromTpl: tpl,
            toTpl: tpl,
            toLegend: OpenLayers.i18n('Selected'),
            fromLegend: OpenLayers.i18n('Found'),
            fromStore: this.mdStore,
            fromAllowTrash: false,
            fromAllowDup: true,
            toAllowDup: false,
            drawUpIcon: false,
            drawDownIcon: false,
            drawTopIcon: false,
            drawBotIcon: false,
            imagePath: this.imagePath,
            toTBar: [{
                // control to clear all select keywwords and refresh the XML.
                text: OpenLayers.i18n('clear'),
                handler: function () {
                    var i = itemSelector;
                    itemSelector.reset.call(i);
                },
                scope: this
            }]
        });
        cmp.push(itemSelector);
        
        this.formPanel = new Ext.form.FormPanel({
            items: [cmp],
            buttons: [{
                text: OpenLayers.i18n('createLink'),
                iconCls: 'linkIcon',
                ctCls: 'gn-bt-main',
                scope: this,
                handler: function () {
                    this.runProcess();
                }
            }, cancelBt]
        });
        return this.formPanel;
    },
    initComponent: function () {
        Ext.applyIf(this, this.defaultConfig);
        
        GeoNetwork.editor.LinkResourcesWindow.superclass.initComponent.call(this);
        
        this.setTitle(OpenLayers.i18n('linkAResource-' + this.type));
        
        this.generateMode();
    }
});

/** api: xtype = gn_editor_linkresourceswindow */
Ext.reg('gn_editor_linkresourceswindow', GeoNetwork.editor.LinkResourcesWindow);
