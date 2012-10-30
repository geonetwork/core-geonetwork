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
 *  class = EditorPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor 
 *  .. class:: EditorPanel(config)
 *
 *     Create a GeoNetwork editor panel
 *
 *
 *  Known limitation: only one editor panel could be created in one application
 *
 */
GeoNetwork.editor.EditorPanel = Ext.extend(Ext.Panel, {
    border: false,
    editUrl: undefined,
    updateUrl: undefined,
    frame: false,
    tbarConfig: undefined,
    id: 'editorPanel', // Only one Editor panel allowed by Document
    defaultConfig: {
    	/** api: config[defaultViewMode] 
         *  Default view mode to open the editor. Default to 'simple'.
         *  View mode is keep in user session (on the server).
         */
        defaultViewMode: 'simple',
        /** api: config[thesaurusButton] 
         *  Use thesaurus selector and inline keyword selection 
         *  instead of keyword selection popup.
         */
        thesaurusButton: true,
        layout: 'border',
        height: 800,
        /** api: config[xlinkOptions] 
         *  Set properties for CONTACT, CRS, KEYWORD to enable XLink option 
         *  to the related selection panel.
         */
        xlinkOptions: {},
        // TODO : Add option to remove utilityPanel
        /** api: config[utilityPanelCollapsed] 
         *  Collapsed utility panel on startup. Default to false.
         */
        utilityPanelCollapsed: false,
        /** api: config[utilityPanelConfig] 
         *  Utility panel properties
         */
        utilityPanelConfig: {
            /** api: config[utilityPanelConfig.thumbnailPanel] 
             *  Collapsed thumbnail panel on startup. Default is false.
             */
            thumbnailPanel: {
                collapsed: false
            },
            /** api: config[utilityPanelConfig.relationPanel] 
             *  Collapsed relation panel on startup. Default is true.
             */
            relationPanel: {
                collapsed: true
            },
            /** api: config[utilityPanelConfig.validationPanel] 
             *  Collapsed validation panel on startup. Default is true.
             */
            validationPanel: {
                collapsed: true
            },
            /** api: config[utilityPanelConfig.suggestionPanel] 
             *  Collapsed suggestion panel on startup. Default is true.
             */
            suggestionPanel: {
                collapsed: true
            },
            /** api: config[utilityPanelConfig.helpPanel] 
             *  Collapsed thumbnail panel on startup. Default is false.
             */
            helpPanel: {
                collapsed: false
            }
        }
    },
    catalogue: undefined,
    toolbar: undefined,
    validationPanel: undefined,
    relationPanel: undefined,
    helpPanel: undefined,
    suggestionPanel: undefined,
    thumbnailPanel: undefined,
    editorMainPanel: undefined,
    metadataId: undefined,
    versionId: undefined,
    metadataMode: undefined,
    metadataSchema: undefined,
    metadataType: undefined,
    position: undefined,
    relatedMetadataStore: undefined,
    keywordSelectionWindow: undefined,
    contactSelectionWindow: undefined,
    subTemplateSelectionWindow: undefined,
    crsSelectionWindow: undefined,
    logoSelectionWindow: undefined,
    linkedMetadataSelectionWindow: undefined,
    geoPublisherWindow: undefined,
    lang: undefined,
    fileUploadWindow: undefined,
    mask: undefined,
    managerInitialized: false,
    container : undefined,
    setContainer : function (el) {
        if (!this.container) {
            this.container = el;
        }
    },
    /** api: method[showLogoSelectionPanel]
     * 
     * :param ref: ``String``  Form element identifier (eg. _235).
     * 
     *  Get related metadata records for current metadata using xml.relation service.
     */
    showLogoSelectionPanel : function(ref){
        if (!this.logoSelectionWindow) {
            var logoSelectionPanel = new GeoNetwork.editor.LogoSelectionPanel({
                        ref: ref, 
                        serviceUrl: this.catalogue.services.getIcons, 
                        logoAddUrl: this.catalogue.services.logoAdd, 
                        logoUrl: this.catalogue.services.harvesterLogoUrl,
                        listeners: {
                            logoselected : function(panel, idx){
                                var record = panel.store.getAt(idx);
                                Ext.getDom(panel.ref).value = panel.logoUrl + record.get('name');
                            }
                        }
                    });
            
            this.logoSelectionWindow = new Ext.Window({
                title: OpenLayers.i18n('logoSelectionWindow'),
                width: 300,
                height: 300,
                layout: 'fit',
                modal: true,
                items: logoSelectionPanel,
                closeAction: 'hide',
                constrain: true,
                iconCls: 'attached'
            });
        }
        this.logoSelectionWindow.items.get(0).setRef(ref);
        this.logoSelectionWindow.show();
    },
    /** api: method[showFileUploadPanel]
     * 
     *  :param id: ``String``  Metadata internal identifier.
     *  :param ref: ``String``  Form element identifier (eg. 235).
     *  
     *  Show panel to upload a file.
     */
    showFileUploadPanel: function(id, ref){
        var panel = this;
        
        // FIXME : could be improved. Here we clean the window.
        // Setting the current metadata id is probably better.
        if (this.fileUploadWindow) {
            this.fileUploadWindow.close();
            this.fileUploadWindow = undefined;
        }
        
        if (!this.fileUploadWindow) {
            var fileUploadPanel = new Ext.form.FormPanel({
                //autoLoad : this.catalogue.services.prepareUpload + "?ref=" + ref + "&id=" + id
                fileUpload: true,
                defaultType: 'textfield',
                items: [{
                    name: 'id',
                    allowBlank: false,
                    hidden: true,
                    value: this.metadataId
                }, {
                    name: 'access',
                    allowBlank: false,
                    hidden: true,
                    value: 'private' // FIXME
                }, {
                    name: 'ref',
                    allowBlank: false,
                    hidden: true,
                    value: ref
                }, {
                    name: 'proto',
                    hidden: true,
                    //allowBlank : false,
                    value: '' // FIXME
                }, {
                    name: 'overwrite',
                    fieldLabel: 'Overwrite',
                    checked: true,
                    xtype: 'checkbox'
                }, {
                    xtype: 'fileuploadfield',
                    emptyText: OpenLayers.i18n('selectFile'),
                    fieldLabel: 'File',
                    allowBlank: false,
                    name: 'f_' + ref,
                    buttonText: '',
                    buttonCfg: {
                        iconCls: 'uploadIconAdd'
                    }
                }],
                buttons: [{
                    text: OpenLayers.i18n('upload'),
                    iconCls: 'attachedAdd',
                    handler: function(){
                        if (fileUploadPanel.getForm().isValid()) {
                            fileUploadPanel.getForm().submit({
                                url: panel.catalogue.services.upload,
                                waitMsg: OpenLayers.i18n('uploading'),
                                success: function(fileUploadPanel, o){
                                    var fname = o.result.fname;
                                    var name = Ext.getDom('_' + ref);
                                    if (name) {
                                        name.value = fname;
                                    }
                                    // Trigger update
                                    panel.save();
                                    
                                    // Hide window
                                    panel.fileUploadWindow.hide();
                                }
                                // TODO : improve error message
                                // Currently return  Unexpected token < from ext doDecode
                            });
                        }
                    }
                }, {
                    text: OpenLayers.i18n('reset'),
                    handler: function(){
                        fileUploadPanel.getForm().reset();
                    }
                }]
            });
            
            this.fileUploadWindow = new Ext.Window({
                title: OpenLayers.i18n('fileUploadWindow'),
                width: 300,
                height: 300,
                layout: 'fit',
                modal: true,
                items: fileUploadPanel,
                closeAction: 'hide',
                constrain: true,
                iconCls: 'attached'
            });
        }
        
        this.fileUploadWindow.show();
    },
    
    /** api: method[showGeoPublisherPanel]
     * 
     *  Display geo publisher panel
     *
     *  :param  id: ``String`` Metadata internal identifier
     *  :param  uuid: ``String`` Metadata UUID
     *  :param  title: ``String`` Metadata title
     *  :param  name: ``String`` file name (usually a zip file which contains ESRI Shapefile)
     *  :param  accessStatus: ``String`` public/private according to privileges
     *  :param  nodeName: ``String`` Node name to insert (ie. gmd:online)
     *  :param  insertNodeRef: ``String`` Reference where XML fragement should be inserted.
     *  :param  extent: ``String`` Initial map extent.
     *
     */
    showGeoPublisherPanel: function(id, uuid, title, name, accessStatus, nodeName, insertNodeRef, extent){
        //Ext.QuickTips.init();
        var editorPanel = this;
        
        // Destroy all previously created windows which may
        // have been altered by save/check editor action.
        if (this.geoPublisherWindow) {
            this.geoPublisherWindow.close();
            this.geoPublisherWindow = undefined;
        }
        
        if (!this.geoPublisherWindow) {
            var geoPublisherPanel = new GeoNetwork.editor.GeoPublisherPanel({
                width: 650,
                height: 300,
                layers: GeoNetwork.map.BACKGROUND_LAYERS,
                extent: extent,
                serviceUrl: this.catalogue.services.geopublisher,
                listeners: {
                    addOnLineSource: function(panel, node, protocols){
                        var p;
                        var xml = "";
                        // There is no namespace prefix for Mapserver layer
                        var layerName = 
                            (node.get('id').indexOf("mapserver") === -1 ? node.get('namespacePrefix') + ":" : "") + this.layerName;
                        var id = '_X' + insertNodeRef + '_' + nodeName.replace(":", "COLON");
                        var wxsOnlineSource = 
                            '<gmd:onLine xmlns:gmd=&quot;http://www.isotc211.org/2005/gmd&quot; xmlns:gco=&quot;http://www.isotc211.org/2005/gco&quot;><gmd:CI_OnlineResource>' + 
                                '<gmd:linkage><gmd:URL>${ogcurl}</gmd:URL></gmd:linkage>' + 
                                '<gmd:protocol><gco:CharacterString>${protocol}</gco:CharacterString></gmd:protocol>' + 
                                '<gmd:name><gco:CharacterString>${layerName}</gco:CharacterString></gmd:name>' + 
                                '<gmd:description><gco:CharacterString>${metadataTitle}</gco:CharacterString></gmd:description>' + 
                            '</gmd:CI_OnlineResource></gmd:onLine>';

                        for (p in protocols) {
                            if (protocols.hasOwnProperty(p) && protocols[p].checked === true) {
                                xml += OpenLayers.String.format(wxsOnlineSource, {
                                    ogcurl: node.get(p + 'Url'),
                                    protocol: protocols[p].label,
                                    layerName: layerName,
                                    metadataTitle: this.metadataTitle + "(" + protocols[p].label + ")"
                                }) + "&&&";
                            }
                        }
                        GeoNetwork.editor.EditorTools.addHiddenFormField(id, xml);
                        
                        // Save
                        editorPanel.save();
                        editorPanel.geoPublisherWindow.hide();
                    }
                }
            });
            
            this.geoPublisherWindow = new Ext.Window({
                title: OpenLayers.i18n('geoPublisherWindowTitle'),
                layout: 'fit',
                modal: true,
                items: geoPublisherPanel,
                closeAction: 'hide',
                //resizable: false,
                constrain: true,
                iconCls: 'repository'
            });
        }
        this.geoPublisherWindow.items.get(0).setRef(id, uuid, title, name, accessStatus);
        this.geoPublisherWindow.setTitle(OpenLayers.i18n('geoPublisherWindowTitle') + " " + name);
        this.geoPublisherWindow.show();
        
    },
    /** api: method[showSubTemplateSelectionPanel]
     * 
     *  :param ref: ``String``  Form element identifier (eg. 235).
     *  :param name: ``String``  Sub template type name (eg. CI_ResponsibleParty).
     *  :param elementName: ``String``  Element tag name (eg. gmd:pointOfContact).
     *  
     *  Display contact selection panel
     *  Not available in trunk.
     */
    showSubTemplateSelectionPanel: function(ref, name, elementName){
        var editorPanel = this;
        
        // Destroy all previously created windows which may
        // have been altered by save/check editor action.
        if (this.subTemplateSelectionWindow) {
            this.subTemplateSelectionWindow.close();
            this.subTemplateSelectionWindow = undefined;
        }
        if (!this.subTemplateSelectionWindow) {
            var selectionPanel = new GeoNetwork.editor.SubTemplateSelectionPanel({
                        width : 620,
                        height : 300,
                        catalogue: this.catalogue,
                        listeners : {
                            subTemplateSelected : function(panel, subtemplates) {
                                GeoNetwork.editor.EditorTools.addHiddenFormFieldForFragment(panel, subtemplates, editorPanel);
                            }
                        }
                    });
    
            this.subTemplateSelectionWindow = new Ext.Window( {
                title : OpenLayers.i18n('SelectionWindowTitle'),
                layout : 'fit',
                items : selectionPanel,
                closeAction : 'hide',
                constrain : true,
                iconCls : 'searchIcon'
            });
        }
    
        this.subTemplateSelectionWindow.items.get(0).setRef(ref);
        this.subTemplateSelectionWindow.items.get(0).setName(name);
        this.subTemplateSelectionWindow.items.get(0).setElementName(elementName);
        
        this.subTemplateSelectionWindow.items.get(0).setAddAsXLink(this.xlinkOptions.CONTACT);
        this.subTemplateSelectionWindow.show();
    },
    /** api: method[showKeywordSelectionPanel]
     * 
     *  :param ref: ``String``  Form element identifier (eg. 235).
     *
     *  Display keyword selection panel. Xlink mode is activated if
     *  EditorPanel.xlinkOptions.KEYWORD is set to true.
     *
     */
    showKeywordSelectionPanel: function(ref, type, formBt) {
        var editorPanel = this;
        
        if (this.thesaurusButton) {
            GeoNetwork.editor.ConceptSelectionPanel.initThesaurusSelector(ref, type, formBt);
        } else {
            // Destroy all previously created windows which may
            // have been altered by save/check editor action.
            if (this.keywordSelectionWindow) {
                this.keywordSelectionWindow.close();
                this.keywordSelectionWindow = undefined;
            }
            
            if (!this.keywordSelectionWindow) {
                this.keywordSelectionPanel = new GeoNetwork.editor.KeywordSelectionPanel({
                    catalogue: this.catalogue,
                    listeners: {
                        keywordselected: function(panel, keywords){
                            GeoNetwork.editor.EditorTools.addHiddenFormFieldForFragment(panel, keywords, editorPanel);
                        }
                    }
                });
                
                this.keywordSelectionWindow = new Ext.Window({
                    title: OpenLayers.i18n('keywordSelectionWindowTitle'),
                    width: 620,
                    height: 300,
                    layout: 'fit',
                    modal: true,
                    items: this.keywordSelectionPanel,
                    closeAction: 'hide',
                    constrain: true,
                    iconCls: 'searchIcon'
                });
            }
            
            this.keywordSelectionWindow.items.get(0).setRef(ref);
            this.keywordSelectionWindow.items.get(0).setAddAsXLink(this.xlinkOptions.KEYWORD);
            this.keywordSelectionWindow.show();
        }
    },
    /** api: method[showCRSSelectionPanel]
     * 
     *  :param ref: ``String``  Form element identifier (eg. 235).
     *  :param name: ``String``  Element name.
     *
     * Display CRS selection panel.
     */
    showCRSSelectionPanel: function(ref, name){
        var editorPanel = this;
        
        if (!this.crsSelectionWindow) {
            this.crsSelectionPanel = new GeoNetwork.editor.CRSSelectionPanel({
                catalogue: this.catalogue,
                listeners: {
                    crsSelected: function(xml){
                        var id = '_X' + ref + '_' + name.replace(":", "COLON");
                        
                        GeoNetwork.editor.EditorTools.addHiddenFormField(id, xml);
                        
                        editorPanel.save();
                    }
                }
            });
            
            this.crsSelectionWindow = new Ext.Window({
                title: OpenLayers.i18n('crsSelectionWindowTitle'),
                layout: 'fit',
                width: 620,
                height: 300,
                modal: true,
                items: this.crsSelectionPanel,
                closeAction: 'hide',
                constrain: true,
                iconCls: 'searchIcon'
            });
        }
        
        this.crsSelectionWindow.items.get(0).setRef(ref);
        this.crsSelectionWindow.show();
    },
    /** api: method[linkedMetadataSelectionWindow]
     * 
     *  :param ref: ``String``  Reference of the element to update. If null, trigger action.
     *  :param name: ``String``  Type of element to update. Based on this information
     *     define if multiple selection is allowed and if hidden parameters need
     *     to be added to CSW query.
     *  :param mode: ``String`` Mode is set to name if not defined. Mode define which type of metadata
     *     to search for. Uuidref means all, iso19110 means feature catalogue only.
     * 
     *  The window in which we can select linked metadata
     *
     */
    showLinkedMetadataSelectionPanel: function(ref, name, mode){
        // Add extra parameters according to selection panel
        var mode = mode || name;
        var single = ((mode === 'uuidref' || mode === 'iso19110' || mode === '') ? true : false);
        var editorPanel = this;
        this.linkedMetadataSelectionPanel = new GeoNetwork.editor.LinkedMetadataSelectionPanel({
            ref: ref,
            catalogue: this.catalogue,
            singleSelect: single,
            mode: mode,
            listeners: {
                linkedmetadataselected: function(panel, metadata){
                    
                    if (single) {
                        if (this.ref !== null) {
                            Ext.get('_' + this.ref + (name !== '' ? '_' + name : '')).dom.value = metadata[0].data.uuid;
                        } else {
                            // Create relation between current record and selected one
                            if (this.mode === 'iso19110') {
                                var url = panel.catalogue.services.mdRelationInsert + 
                                            '?parentId=' + 
                                            document.mainForm.id.value + 
                                            '&childUuid=' + 
                                            metadata[0].data.uuid;
                                
                                var request = Ext.Ajax.request({
                                    url: url,
                                    method: 'GET',
                                    success: function(result, request){
                                        var urlProcessing = panel.catalogue.services.mdProcessing +
                                            '?uuidref=' +
                                            metadata[0].data.uuid +
                                            '&id=' +
                                             document.mainForm.id.value +
                                            "&process=update-attachFeatureCatalogue";

                                        editorPanel.process(urlProcessing);
                                        //editorPanel.relationPanel.reload();
                                    },
                                    failure: function(result, request){
                                        Ext.MessageBox.alert(
                                                OpenLayers.String.format(
                                                    OpenLayers.i18n('errorAndStatusMsg'), { 
                                                        status: result.status, 
                                                        text: result.statusText 
                                                    }
                                                ));
                                        setBunload(true); // reset warning for window destroy
                                    }
                                });
                                
                            }
                        }
                    } else {
                        var inputs = [];
                        var multi = metadata.length > 1 ? true : false;
                        Ext.each(metadata, function(md, index){
                            if (multi) {
                                name = name + '_' + index;
                            }
                            // Add related metadata uuid into main form.
                            inputs.push({
                                tag: 'input',
                                type: 'hidden',
                                id: name,
                                name: name,
                                value: md.data.uuid
                            });
                        });
                        var dh = Ext.DomHelper;
                        dh.append(Ext.get("hiddenFormElements"), inputs);
                    }
                }
            }
        });
        
        this.linkedMetadataSelectionWindow = new Ext.Window({
            title: OpenLayers.i18n('linkedMetadataSelectionWindowTitle'),
            width: 620,
            height: 300,
            layout: 'fit',
            items: this.linkedMetadataSelectionPanel,
            closeAction: 'hide',
            constrain: true,
            iconCls: 'linkIcon',
            modal: true
        });
        
        this.linkedMetadataSelectionWindow.show();
    },
    /** api: method[showLinkedServiceMetadataSelectionPanel]
     * 
     *  :param name: ``String``  Type of element to update (eg. 'attachService' if current dataset is a dataset metadata record, null if not). 
     *  :param serviceUrl: ``String`` Service GetCapabilities URL.
     *  :param uuid: ``String``  Metadata UUID.
     *  
     *  The window in which we can select service or dataset metadata to link.
     *  
     *  If the metadata is a dataset, then the following action are triggered on selection:
     *  
     *  * Update service (if current user has privileges), using XHR request to attache the dataset (in srv:operatesOn and srv:coupledResource)
     *  
     *  * Update current metadata (in online resource section).
     *  
     */
    showLinkedServiceMetadataSelectionPanel: function(name, serviceUrl, uuid){
        var editorPanel = this;
        serviceUrl = Ext.getDom('serviceUrl') && Ext.getDom('serviceUrl').value;
        
        var linkedMetadataSelectionPanel = new GeoNetwork.editor.LinkedMetadataSelectionPanel({
            mode: name,
            autoWidth: true,
            ref: null,
            serviceUrl: serviceUrl,
            catalogue: this.catalogue,
            region: 'north',
            uuid: uuid,
            //createIfNotExistURL: 'metadata.create.form?type=' + (name === 'attachService' ? 'service' : 'dataset'),
            /**
             * Create a relation between **one** service (current one)
             * and **one** dataset due to the definition of the layername
             * parameter.
             */
            singleSelect: true,
            listeners: {
                linkedmetadataselected: function(panel, metadata){
                    var layerName = Ext.getCmp('getCapabilitiesLayerName').getValue();

                    // update dataset metadata record
                    // and/or the service. Failed on privileges error.
                    if (name === 'attachService') {
                        // Current dataset is a dataset metadata record.
                        // 1. Update service (if current user has privileges), using XHR request
                        var serviceUpdateUrl = editorPanel.catalogue.services.mdProcessingXml + 
                                                    "?uuid=" + metadata[0].data.uuid + 
                                                    "&process=update-srv-attachDataset" + 
                                                    "&uuidref=" + document.mainForm.uuid.value +
                                                    "&scopedName=" + layerName;
                        
                        Ext.Ajax.request({
                            url: serviceUpdateUrl,
                            method: 'GET',
                            success: function(result, request){
                                var response = result.responseText;
                                
                                // Check error
                                if (response.indexOf('Not owner') !== -1) {
                                    Ext.MessageBox.alert(OpenLayers.i18n("NotOwnerError"));
                                } else if (response.indexOf('error') !== -1) {
                                    Ext.MessageBox.alert(OpenLayers.i18n("error") + response);
                                }
                                // 2. Update current metadata record, in current window
                                var datasetUpdateUrl = editorPanel.catalogue.services.mdProcessing + 
                                                            "?id=" + editorPanel.metadataId + 
                                                            "&process=update-onlineSrc" + 
                                                            "&desc=" + layerName + 
                                                            "&url=" + escape(metadata[0].data.uri) + 
                                                            "&scopedName=" + layerName;
                                
                                Ext.Ajax.request({
                                    url: datasetUpdateUrl,
                                    method: 'GET',
                                    success: function(result, request){
                                        editorPanel.updateEditor(result);
                                    },
                                    failure: function(result, request){
                                        Ext.MessageBox.alert(OpenLayers.i18n("datasetUpdateError"));
                                        setBunload(true);
                                    }
                                });
                            },
                            failure: function(result, request){
                                Ext.MessageBox.alert(OpenLayers.i18n("ServiceUpdateError"));
                                setBunload(true);
                            }
                        });
                        
                        
                    } else {
                        // Current dataset is a service metadata record.
                        // 1. Update dataset (if current user has privileges), using XHR request
                        var datasetUpdateUrl = editorPanel.catalogue.services.mdProcessingXml + 
                                                    "?uuid=" + metadata[0].data.uuid +
                                                    "&process=update-onlineSrc" + 
                                                    "&desc=" + layerName + 
                                                    "&url=" + serviceUrl + 
                                                    "&scopedName=" + layerName;
                        
                        Ext.Ajax.request({
                            url: datasetUpdateUrl,
                            method: 'GET',
                            success: function(result, request){
                                var response = result.responseText;
                                // Check error
                                if (response.indexOf('Not owner') !== -1) {
                                    Ext.MessageBox.alert(OpenLayers.i18n("NotOwnerError"));
                                } else if (response.indexOf('error') !== -1) {
                                    Ext.MessageBox.alert(OpenLayers.i18n("error") + response);
                                }
                                
                                // 2. Update current metadata record, in current window FIXME
                                editorPanel.process(editorPanel.catalogue.services.mdProcessing + 
                                                            "?uuid=" + document.mainForm.uuid.value +
                                                            "&process=update-srv-attachDataset" + 
                                                            "&uuidref=" + metadata[0].data.uuid + 
                                                            "&scopedName=" + layerName);
                            },
                            failure: function(result, request){
                                Ext.MessageBox.alert(OpenLayers.i18n("ServiceUpdateError"));
                                setBunload(true);
                            }
                        });
                    }
                },
                scope: this
            }
        });
        
        this.linkedMetadataSelectionWindow = new Ext.Window({
            title: (name === 'attachService' ? OpenLayers.i18n('associateService') : OpenLayers.i18n('associateDataset')),
            layout: 'fit',
            width: 620,
            height: 400,
            items: linkedMetadataSelectionPanel,
            closeAction: 'hide',
            constrain: true,
            iconCls: 'linkIcon',
            modal: true
        });
        
        this.linkedMetadataSelectionWindow.show();
    },
    /** api: method[switchToTab]
     * 
     *  :param tab: ``String``  The tab to open.
     *  
     *  Set the new tab value and trigger a save action.
     */
    switchToTab: function(tab){
        Ext.getDom('currTab').value = tab;
        this.save();
    },
    /** api: method[finish]
     * 
     *  Save current editing session and close the editor.
     */
    finish: function(){
        this.loadUrl('metadata.update.finish', undefined, this.closeCallback);
    },
    /** api: method[save]
     * 
     *  Save current editing session.
     */
    save: function(){
        this.loadUrl('metadata.update.new', undefined, this.loadCallback);
    },
    callAction: function(action){
        this.loadUrl(action, undefined, this.loadCallback);
    },
    /** api: method[validate]
     * 
     *  Validate metadata and open validation panel.
     *  
     */
    validate: function(){
        this.loadUrl('metadata.update.new', 'validate', this.loadCallback);
        this.validationPanel.expand(true);
    },
    /** api: method[reset]
     * 
     *  Reset current editing session calling 'metadata.update.forget.new' service.
     */
    reset: function(){
        this.loadUrl('metadata.update.forget.new', undefined, this.loadCallback);
    },
    /** api: method[cancel]
     * 
     *  Cancel current editing session and close the editor calling 'metadata.update.forgetandfinish' service.
     */
    cancel: function(){
        // TODO : check lost changes 
        this.loadUrl('metadata.update.forgetandfinish', undefined, this.closeCallback);
    },
    /** api: method[process]
     * 
     *  :param action: ``String``  The action URL of the process to run with parameters.
     *  
     *  Use for XslProcessing task
     */
    process: function(action) {
        this.loadUrl(action, undefined,  this.loadCallback, true);
    },
    /** api: method[init]
     * 
     *  :param metadataId: ``String``  Metadata internal id or template id on creation.
     *  :param create: ``Boolean``  Initialization create a new record. Default to false.
     *  :param group: ``String``  The metadata group (on creation).
     *  :param child: ``Boolean``  Initialization create a new record from a parent. Default to false.
     *  :param isTemplate: ``Boolean``  Metadata is a template. Default to false.
     *  
     *  
     *  Initialized the metadata editor. The method could be used to create a metadata record
     *  from a template or from a parent metadata record.
     */
    init: function(metadataId, create, group, child, isTemplate){
        var url;
        
        this.metadataId = metadataId;
        
        if (create) {
            url = this.createUrl + '?id=' + this.metadataId + '&group=' + group;
            if (child) {
                url += "&child=y";
            }
            if (isTemplate) {
                url += "&isTemplate=" + isTemplate;
            }
        } else {
            url = this.editUrl + '?id=' + this.metadataId;
        }
        url += '&currTab=' + (document.mainForm ? document.mainForm.currTab.value : this.defaultViewMode);
        
        this.loadUrl(url, undefined, this.loadCallback);
        
        if (this.disabled) {
            this.setDisabled(false);
        }
        this.container.show();
        this.ownerCt.show();
    },
    /** api: method[loadUrl]
     * 
     *  :param action: ``String``  Action URL.
     *  :param validate: ``Boolean``  If we are doing a validation then enable display of errors in editor. Default to false.
     *  :param cb: ``Function``  Callback after panel update.
     *  :param noPostParams: ``Boolean``  Do not POST params.
     *  
     *  Call URL and replace editor content with the response
     */
    loadUrl: function(action, validate, cb, noPostParams){
        this.toolbar && this.toolbar.setDisabled(true);
        if (document.mainForm) {
            if (typeof validate !== 'undefined') {
                document.mainForm.showvalidationerrors.value = "true";
            } else {
                document.mainForm.showvalidationerrors.value = "false";
            }
        }
        
        var mgr = this.editorMainPanel.getUpdater();
        
        mgr.update({
            url: (action.indexOf('/')!==-1 ? action : this.catalogue.services.rootUrl + action),
            params: !noPostParams && document.mainForm && Ext.fly('editForm') !== null ? Ext.Ajax.serializeForm('editForm') : undefined,
            callback: cb,
            scope: this,
            loadScripts: true,
            text: OpenLayers.i18n(action) + ' (' + action + ')'
        });
    },
    closeCallback: function(){
        this.onEditorClosed();
        this.ownerCt.hide();
        this.toolbar && this.toolbar.setDisabled(false);
        //Ext.Msg.alert('Editor', 'Finish editing', function () {this.hide()}, this);
    },
    /**
     * After metadata load, use this callback to check for error and display
     * alert on failure.
     *  
     */
    loadCallback: function(el, success, response, options){
        if (!this.managerInitialized) {
            this.initManager();
        }
        
        if (success) {
            this.metadataLoaded();
        } else {
            this.getError(response);
        }
        this.toolbar && this.toolbar.setDisabled(false);
    },
    /**
     * Hack to get error message inside HTML pages
     * returned by GeoNetwork. TODO : return XML response
     * in case of error.
     * TODO : move to more general class (Catalogue ?)
     */
    getError: function(response){
        if (response && response.responseText) {
            var errorPage = response.responseText, 
                errorTitle1, errorTitle, 
                errorMsg, errorMsg1, errorMsg2;
                
            // Try to extract the error title from the HTML content. This output are generated
            // according to services, could be from error-emedded.xsl, error-modal.xsl or metadata-error.xsl 
            // or error.xsl. To be improved
            errorTitle = errorPage.match(/<h2 class=\"error\"\>(.*)<\/h2\>/);
            errorTitle1 = errorTitle || errorPage.match(/<div id=\"error\"\>\n[ ]*<h2>(.*)<\/h2\>/);
            errorMsg = errorPage.match(/<\/h2\>\n[ ]*<p\><\/p\>(.*)/);
            if (!errorMsg) {
                 errorMsg1 = errorPage.match(/<p id=\"error\">(.*)<\/p\>/);
                 errorMsg2 = errorPage.match(/<p id=\"stacktrace\">(.*)<\/p\>/);
                 errorMsg = errorMsg1[1] + errorMsg2[1];
            } else {
                errorMsg = errorMsg[1];
            }
            
            if (errorTitle1) {
                Ext.Msg.alert(errorTitle1[1], (errorMsg ? errorMsg : ''), function(){
                    this.ownerCt.hide();
                }, this);
            }
        } 
//        else if (response.responseXML) {
//            // Do something else
//        }
    },
    /** api: method[updateEditor]
     * 
     *  :param html: ``String``  HTML to use for panel update
     *  
     *  Update editor content
     */
    updateEditor: function(html){
        this.editorMainPanel.update(html, false, this.metadataLoaded.bind(this));
    },
    /** private: method[metadataLoaded]
     * 
     *  Init editor after metadata loaded.
     */
    metadataLoaded: function(){
        this.metadataSchema = document.mainForm.schema.value;
        this.metadataType = Ext.getDom('template');
        this.metadataId = document.mainForm.id.value;
        this.versionId = document.mainForm.version.value;
        
        this.toolbar.setIsMinor(document.mainForm.minor.value);
        this.toolbar.setIsTemplate(this.metadataType.value);
        // If panel was disabled on startup, enable it after initialization
        if (this.disabled) {
            this.setDisabled(false);
        }
        
        this.onMetadataUpdated();
        
        //console.log("metadata schema: " + this.metadataSchema.value + " type:" + this.metadataType.value + " tab:" + this.metadataCurrTab.value);
        
        this.initCalendar();
        this.initMultipleSelect();
        this.validateMetadataFields();
        this.catalogue.extentMap.initMapDiv();
        
        // Create concept selection widgets where relevant
        GeoNetwork.editor.ConceptSelectionPanel.init();
        
        // TODO : Update toolbar metadata type value according to form content
        //Ext.get('template').dom.value=item.value;
        
        
        // Register event to form element to display help information 
        var formElements = Ext.query('th[id]', this.body.dom);
        //formElements = formElements.concat(Ext.query('legend[id]', this.body.dom));
        formElements = formElements.concat(Ext.query('legend[id]', this.body.dom));
        Ext.each(formElements, function(item, index, allItems){
            var e = Ext.get(item);
            var id = e.getAttribute('id');
            if (e.is('TH')) {
                var section = e.up('FIELDSET');
                // TODO : register event on custom widgets like Bbox
                e.parent().on('mouseover', function(){
                    this.helpPanel.updateHelp(id, section);
                }, this);
            } else {
                e.on('mouseover', function(){
                    this.helpPanel.updateHelp(id);
                }, this);
            }
        }, this);
        
        
        this.updateViewMenu();
    },
    /** private: method[validateMetadataField]
     * 
     * Trigger validating event of an element.
     *
     */
    validateMetadataField: function(input){
        // Process only onchange and onkeyup event having validate in event
        // name.
        
        var ch = input.getAttribute("onchange");
        var ku = input.getAttribute("onkeyup");
        // When retrieving a style attribute, IE returns a style object,
        // rather than an attribute value; retrieving an event-handling
        // attribute such as onclick, it returns the contents of the
        // event handler wrapped in an anonymous function;
        if (typeof ch  === 'function') {
            ch = ch.toString();
        }
        if (typeof ku === 'function') {
            ku = ku.toString();
        }
        
        if (!input || 
                (ch !== null && ch.indexOf("validate") === -1) || 
                (ku !== null && ku.indexOf("validate") === -1)) {
            return;
        }
        
        if (input.onkeyup) {
            input.onkeyup();
        }
        if (input.onchange) {
            input.onchange();
        }
    },
    /** private: method[validateMetadataFields]
     * 
     *  Retrieve all page's input and textarea element and check the onkeyup and
     *  onchange event (Usually used to check user entry.
     *
     *  @see validateNonEmpty and validateNumber).
     *
     */
    validateMetadataFields: function(){
        // --- display lang selector when appropriate
        var items = Ext.DomQuery.select('select.lang_selector');
        Ext.each(items, function(input){
            // --- language selector has a code attribute to be used to be
            // matched with GUI language in order to edit by default
            // element
            // in GUI language. If none, default language is selected.
            for (i = 0; i < input.options.length; i ++) {
                if (input.options[i].getAttribute("code").toLowerCase() === this.lang) {
                    input.options[i].selected = true;
                    i = input.options.length;
                }
            }
            // FIXME this.enableLocalInput(input, false);
        }, this);
        
        // --- display validator events when needed.
        items = Ext.DomQuery.select('input,textarea,select');
        Ext.each(items, function(input){
            this.validateMetadataField(input);
        }, this);
        
    },
    /** private: method[initCalendar]
     * 
     *  Initialize all calendar divs identified by class "cal".
     *
     *  All calendars are composed of one div and 1 or 2 inputs;
     *  one for the format, one for the value. According
     *  to the format, A DateTime or a DateField component
     *  are initialized.
     *
     *  TODO : Add vtype control for extent (start < end)
     */
    initCalendar: function(){
    
        var calendars = Ext.DomQuery.select('div.cal'), i;
        
        for (i = 0; i < calendars.length; i++) {
            var cal = calendars[i];
            var id = cal.id; // Give render div id to calendar input and change
            // its id.
            cal.id = id + 'Id'; // In order to get the input if a get by id is made
            // later (eg. gn_search.js).
            
            if (cal.firstChild === null || cal.childNodes.length === 0) { // Check if
                // already
                // initialized
                // or not
                var format = 'Y-m-d';
                var formatEl = Ext.getDom(id + '_format', this.editorMainPanel.dom);
                if (formatEl) {
                    format = formatEl.value;
                }
                
                var valueEl = Ext.getDom(id + '_cal', this.editorMainPanel.dom);
                var value = (valueEl ? valueEl.value : '');
                var showTime = format.indexOf('T') === -1 ? false : true;
                
                if (showTime) {
                    var dtCal = new Ext.ux.form.DateTime({
                        renderTo: cal.id,
                        name: id,
                        id: id,
                        value: value,
                        dateFormat: 'Y-m-d',
                        timeFormat: 'H:i',
                        hiddenFormat: 'Y-m-d\\TH:i:s',
                        dtSeparator: 'T'
                    });
                } else {
                    var dCal = new Ext.form.DateField({
                        renderTo: cal,
                        name: id,
                        id: id,
                        width: 160,
                        value: value,
                        format: 'Y-m-d'
                    });
                }
                
            }
        }
    },
    /** private: method[initMultipleSelect]
     * 
     *  Initialize all select with class codelist_multiple.
     *  
     *  Those select field on change will create an XML
     *  codelist fragment to be inserted into the record.
     *  It will allows when cardinality is greater than 1 to 
     *  not to have to deal with (+) control to add multiple
     *  values.
     */
    initMultipleSelect: function(){
        var selects = Ext.DomQuery.select('select.codelist_multiple'), i;
        Ext.each(selects, function(select){
            var input = Ext.get('X' + select.id);
            var tpl = input.dom.innerHTML;
            onchangeEvent = function(){
                input.dom.innerHTML = '';
                Ext.each(this.options, function(option){
                    if(option.selected){
                        input.dom.innerHTML += (input.dom.innerHTML === '' ? '' : '&&&') 
                                                   + tpl.replace(new RegExp('codeListValue=".*"'), 
                                                   'codeListValue="' + option.value + '"');
                    }
                });
            };
            
            select.onchange = onchangeEvent;
        });
    },
    /** private: method[updateViewMenu]
     * 
     *  Populate the toolbar view menu with the list of available views according
     *  to the metadata tabs defined in the returned HTML page and register the
     *  switchTab action.
     */
    updateViewMenu: function(){
        var modes = Ext.query('span.mode', this.body.dom),
            menu = [],
            i, j, e;
        
        for (i = 0; i < modes.length; i ++) {
            if (modes[i].firstChild) {
                var id = modes[i].getAttribute('id');
                var next = Ext.get(modes[i]).next();
                var label = modes[i].innerHTML;
                var tabs = next.query('LI');
                var current = next.query('LI[id=' + document.mainForm.currTab.value + ']');
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
                                Ext.getCmp('editorPanel').switchToTab(this);
                            }, e.getAttribute('id'));
                        }
                    }
                }
                menu.push([label, id, activeMode]);
            }
        }
        this.toolbar.updateViewMenu(menu);
    },
    /** private: method[onMetadataUpdated]
     * 
     */
    onMetadataUpdated: function(){
        this.fireEvent('metadataUpdated', this, this.metadataId, this.metadataSchema, this.versionId);
    },
    /** api: method[onEditorClosed]
     *  :param e: ``Object``
     *
     *  The "onEditorClosed" listener.
     *
     *  Listeners will be called with the following arguments:
     *
     *  ``this`` : GeoNetwork.Catalogue
     */
    onEditorClosed: function(){
        this.fireEvent('editorClosed', this);
    },
    /** private: method[initManager]
     * 
     */
    initManager: function(){
        var mgr = this.editorMainPanel.getUpdater();
        
        mgr.on('beforeupdate', function(el, url, params){
            this.position = this.editorMainPanel.getEl().parent().dom.scrollTop;
        }, this);
        
        mgr.on('update', function(el, response){
            this.editorMainPanel.getEl().parent().dom.scrollTop = this.position;
        }, this);
        
        this.managerInitialized = true;
    },
    /** private: method[initPanelLayout]
     * 
     */
    initPanelLayout : function () {
        if (this.container) {
            if (this.container.setIconClass) {
                this.container.setIconClass('editing');
            }
            if (this.container.setTitle) {
                if (this.metadataId) {
                    this.container.setTitle(OpenLayers.String.format(
                        OpenLayers.i18n('editing'), { 
                            title: '', 
                            uuid: this.metadataId // FIXME 
                        }
                    ));
                } else {
                    // Use title property or the default title
                    //this.container.setTitle(this.title || OpenLayers.i18n('mdEditor'));
                }
            }
        }
    },
    /** private: method[initComponent] 
     *  Initializes the Editor panel.
     */
    initComponent: function(){
        var optionsPanel;
        
        Ext.applyIf(this, this.defaultConfig);
        
        this.disabled = (this.metadataId ? false : true);
        this.lang = (this.catalogue.lang ? this.catalogue.lang : 'eng');
        
        this.editUrl = this.catalogue.services.mdEdit;
        this.createUrl = this.catalogue.services.mdCreate;
        this.updateUrl = this.catalogue.services.mdUpdate;
        
        
        GeoNetwork.editor.EditorPanel.superclass.initComponent.call(this);
        
        panel = this;
        
        
        
        // Create the main editor panel with toolbar
        this.editorMainPanel = new Ext.Panel({
            border: false,
            frame: false,
            id: 'editorMainPanel'
        });
        
        
        var tbarConfig = {editor: panel};
        Ext.apply(tbarConfig, this.tbarConfig);
        this.toolbar = new GeoNetwork.editor.EditorToolbar(tbarConfig);
        
        var editorPanel = {
            region: 'center',
            split: true,
            autoScroll: true,
            tbar: this.toolbar,
            minHeigth: 400,
            items: [this.editorMainPanel]
        };
        this.add(editorPanel);
        
        
        
        // Init utility panels
        this.validationPanel = new GeoNetwork.editor.ValidationPanel(Ext.applyIf({
            metadataId: this.metadataId,
            editor: this,
            serviceUrl: this.catalogue.services.mdValidate
        }, this.utilityPanelConfig.validationPanel));
        
        // TODO : Add option to not create help panel
        this.helpPanel = new GeoNetwork.editor.HelpPanel(Ext.applyIf({
            editor: this,
            html: ''
        }, this.utilityPanelConfig.helpPanel));
        
        this.relationPanel = new GeoNetwork.editor.LinkedMetadataPanel(Ext.applyIf({
            editor: this,
            metadataId: this.metadataId,
            metadataSchema: this.metadataSchema,
            serviceUrl: this.catalogue.services.mdRelation
        }, this.utilityPanelConfig.relationPanel));
        
        this.thumbnailPanel = new GeoNetwork.editor.ThumbnailPanel(Ext.applyIf({
            metadataId: this.metadataId,
            editor: this,
            getThumbnail: this.catalogue.services.mdGetThumbnail,
            setThumbnail: this.catalogue.services.mdSetThumbnail,
            unsetThumbnail: this.catalogue.services.mdUnsetThumbnail
        }, this.utilityPanelConfig.thumbnailPanel));
        
        this.suggestionPanel = new GeoNetwork.editor.SuggestionsPanel(Ext.applyIf({
            metadataId : this.metadataId,
            editor: this,
            catalogue: this.catalogue
        }, this.utilityPanelConfig.suggestionPanel));
        
        optionsPanel = {
            region: 'east',
            split: true,
            collapsible: true,
            collapsed: this.utilityPanelCollapsed,
            hideCollapseTool: true,
            collapseMode: 'mini',
            autoScroll: true,
            // layout: 'fit',
            minWidth: 280,
            width: 280,
            items: [
                this.thumbnailPanel, 
                this.relationPanel, 
                this.suggestionPanel,
                this.validationPanel, 
                this.helpPanel]
        };
        this.add(optionsPanel);
        /** private: event[metadataUpdated] 
         *  Fires after the metadata is refreshed (save, reset, change view mode).
         */
        /** private: event[editorClosed] 
         *  Fires before the editor is closed.
         */
        this.addEvents('metadataUpdated', 'editorClosed');
        
        this.on('added', function (el, container, index) {
            if (container) {
                this.setContainer(container);
            }
            this.initPanelLayout();
        }, this);
    }
});

/** api: xtype = gn_editor_editorpanel */
Ext.reg('gn_editor_editorpanel', GeoNetwork.editor.EditorPanel);
