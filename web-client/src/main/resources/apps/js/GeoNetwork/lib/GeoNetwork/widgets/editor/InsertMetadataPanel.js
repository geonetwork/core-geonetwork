Ext.namespace('GeoNetwork.editor');

/** api: (define)
 *  module = GeoNetwork.editor
 *  class = InsertMetadataPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor 
 *  .. class:: InsertMetadataPanel(config)
 *
 *     Create a GeoNetwork form for metadata import
 *
 *
 *     Default metadata store to use could be overriden when setting
 *     GeoNetwork.Settings.mdStore variables. Default is :class:`GeoNetwork.data.MetadataResultsStore`.
 *
 */
GeoNetwork.editor.InsertMetadataPanel = Ext.extend(Ext.form.FormPanel, {

    defaultConfig: {
      bodyStyle: 'padding:10px',
      frame: false,
      labelWidth: 120,
      autoScroll: true,
      buttonAlign: 'center',
      // Needed for the form submit- if no upload, then use ajax POST request instead of for submit
      fileUpload: true 
    },
    
    importXSLStore: undefined,
    groupStore : undefined,
    categoryStore : undefined,
    categoryCombo : undefined,
    statusPanel : undefined,
    
    /**
     * getImportXSLStore
     * Returns the styleSheet store. Creates it if doesn't exist.
     */
    getImportXSLStore: function () {
        if (!this.importXSLStore) {
            this.importXSLStore = new Ext.data.XmlStore({
                autoDestroy : true,
                autoLoad: true,
                storeId : 'importXSLStore',
                url : catalogue.services.getImportXSL,
                record : 'record',
                idPath : 'id',
                fields : ['name', 'id'],
                sortInfo: {
                    field: 'name',
                    direction: 'ASC'
                }
            });
        }
        return this.importXSLStore;
    },
    
    /**
     * 
     * getGroupStore
     * Returns the group store. Creates it if doesn't exist.
     * Filter result to just have intern groups and no public groups (internet, all, extranet, etc..)
     */
    getGroupStore: function () {
        if(!this.groupStore) {
            this.groupStore = new GeoNetwork.data.GroupStore(catalogue.services.getGroups + '&profile=Editor');
            this.groupStore.load({
                callback: function(){
                    // Remove public groups from the store
                    this.groupStore.each(function(record) {
                        if ((record.get('id') == '-1') || (record.get('id') == '0') || (record.get('id') == '1')) {
                            this.remove(record);
                        }
                    },  this.groupStore);
                    
                    this.groupStore.sort('labelInLang', 'ASC');
                    
                    // set first group as default value of the combo box
                    if (this.groupStore.getCount() > 0) {
                        var recordSelected = this.groupStore.getAt(0);
                        if (recordSelected) {
                            this.find('name', 'group')[0].setValue(recordSelected.data.id);
                        }
                    }
                },
                scope: this
            });
        }
        return this.groupStore;
    },
    
    /**
     * 
     * getCategoryStore
     * Returns the category store. Creates it if doesn't exist.
     */
    getCategoryStore: function () {
        if(!this.categoryStore) {
            var self = this;
            this.categoryStore = new GeoNetwork.data.CategoryStore(catalogue.services.getCategories);
            this.categoryStore.on('load', function () {
                self.categoryCombo.setVisible(this.totalLength !== 0);
                
            });
            this.categoryStore.load();
        }
        return this.categoryStore;
    },
    
    /**
     * switchInsertMode
     * Switch between file upload and copy/paste insert Mode.
     * Hide and disable upload field or data field depending on selection
     */
    switchInsertMode: function(group, radio) {
        this.find('name', 'mefFile')[0].setVisible(radio.inputValue).setDisabled(!radio.inputValue);
        this.find('name', 'fileType')[0].setVisible(radio.inputValue).setDisabled(!radio.inputValue);
        this.find('name', 'data')[0].setVisible(!radio.inputValue).setDisabled(radio.inputValue);
    },
    
    /**
     * switchFileType
     * Switch between one single file or multiple import.
     * If single, validate, style sheet, template type, group, category are visible.
     * Else, assign to current MD is visible
     */
    switchFileType: function(group, radio) {
        this.find('name', 'validate')[0].setVisible(radio.inputValue != 'mef').setDisabled(radio.inputValue == 'mef');
        this.find('name', 'styleSheet')[0].setVisible(radio.inputValue != 'mef').setDisabled(radio.inputValue == 'mef');
        this.find('name', 'template')[0].setVisible(radio.inputValue != 'mef').setDisabled(radio.inputValue == 'mef');
        this.find('name', 'group')[0].setVisible(radio.inputValue != 'mef').setDisabled(radio.inputValue == 'mef');
        this.find('name', 'category')[0].setVisible(radio.inputValue != 'mef').setDisabled(radio.inputValue == 'mef');
        this.find('name', 'assign')[0].setVisible(radio.inputValue != 'single').setDisabled(radio.inputValue == 'single');
    },

    /**
     * getItems
     * Return all field items of the form
     */
    getItems: function() {

        this.categoryCombo = new Ext.form.ComboBox({
                // Categories Combo Box
                xtype: 'combo',
                fieldLabel: OpenLayers.i18n('category'),
                typeAhead: true,
                triggerAction: 'all',
                name: 'category',
                hiddenName: 'category',
                lazyRender:true,
                emptyText: OpenLayers.i18n('chooseCategory'),
                mode: 'local',
                defaultValue: 1,
                store: this.getCategoryStore(),
                valueField: 'id',
                displayField: 'name',
                tpl: '<tpl for="."><div class="x-combo-list-item">{[values.label.' + catalogue.LANG + ']}</div></tpl>'
            });
        
        var items = [{
            // Insert Mode Radio Group
            xtype: 'radiogroup',
            fieldLabel: OpenLayers.i18n('insertMode'),
            items: [
                {boxLabel: OpenLayers.i18n('fileUpload'), name: 'insert_mode', inputValue: 1, checked: true},
                {boxLabel: OpenLayers.i18n('copyPaste'), name: 'insert_mode', inputValue: 0}
            ],
            listeners: {
                change: {
                    fn: this.switchInsertMode,
                    scope: this
                }
            }
        },{
            // File Type Radio Group
            xtype: 'radiogroup',
            fieldLabel: OpenLayers.i18n('fileType'),
            name: 'fileType',
            items: [
                {boxLabel: OpenLayers.i18n('singleFile'), name: 'file_type', inputValue: 'single', checked: true},
                {boxLabel: OpenLayers.i18n('mefFile'), name: 'file_type', inputValue: 'mef'}
            ],
            listeners: {
                change: {
                    fn: this.switchFileType,
                    scope: this
                }
            }
        },{
            // Upload File field
            xtype: 'fileuploadfield',
            emptyText: OpenLayers.i18n('metadata'),
            fieldLabel: OpenLayers.i18n('metadata'),
            name: 'mefFile',
            allowBlank: false,
            anchor: '60%',
            buttonText: '',
            buttonCfg: {
                iconCls: 'thumbnailAddIcon'
            }
        },{
            // data text area
            xtype: 'textarea',
            fieldLabel: OpenLayers.i18n('metadata'),
            height: 200,
            hidden: true,
            disabled: true,
            allowBlank: false,
            name: 'data',
            width: 300
        },{
            //Import actions radio group
            xtype: 'radiogroup',
            fieldLabel: OpenLayers.i18n('importActions'),
            itemCls: 'x-check-group-alt',
            columns: 1,
            items: [
                {boxLabel: OpenLayers.i18n('noActionOnImport'), name: 'uuidAction', inputValue: 'nothing', checked: true},
                {boxLabel: OpenLayers.i18n('overwriteMD'),      name: 'uuidAction', inputValue: 'overwrite'},
                {boxLabel: OpenLayers.i18n('generateUuid'),     name: 'uuidAction', inputValue: 'generateUUID'}
            ]
        },{
            // StyleSheet combobox
            xtype: 'combo',
            fieldLabel: OpenLayers.i18n('styleSheet'),
            typeAhead: true,
            width: 300,
            triggerAction: 'all',
            lazyRender:true,
            name: 'styleSheet',
            hiddenName: 'styleSheet',
            mode: 'local',
            store: this.getImportXSLStore(),
            valueField: 'id',
            displayField: 'name',
            valueNotFoundText: ''
        },{
            // validate check box
            xtype: 'checkbox',
            fieldLabel: OpenLayers.i18n('validate'),
            name :'validate'
        },{
            // Assign current category check box
            xtype: 'checkbox',
            fieldLabel: OpenLayers.i18n('assignCurCat'),
            name: 'assign',
            hidden: true,
            disabled: true
           
        },{
            // Type combo box (metadata, template, subtemplate)
            xtype: 'combo',
            fieldLabel: OpenLayers.i18n('kind'),
            triggerAction: 'all',
            mode: 'local',
            name: 'template',
            value: 'n',
            store: new Ext.data.ArrayStore({
                id: 0,
                fields: [
                    'id',
                    'kind'
                ],
                data: [['n', 'Metadata'], ['y', 'Template'], ['s', 'SubTemplate']]
            }),
            valueField: 'id',
            displayField: 'kind',
            hiddenName: 'template'
        },{
            // Groups combo box
            xtype: 'combo',
            fieldLabel: OpenLayers.i18n('group'),
            emptyText: OpenLayers.i18n('chooseGroup'),
            typeAhead: true,
            triggerAction: 'all',
            lazyRender:true,
            mode: 'local',
            name: 'group',
            store: this.getGroupStore(),
            valueField: 'id',
            displayField: 'name',
            hiddenName: 'group',
            tpl: '<tpl for="."><div class="x-combo-list-item">{[values.label.' + catalogue.LANG + ']}</div></tpl>'
        },
        this.categoryCombo];
        return items;
    },
    
    /**
     * getButtons
     * Return form buttons before init component
     */
    getButtons: function() {
        return [{
            xtype: 'button',
            text: OpenLayers.i18n('cancel'),
            handler: function(b,e) {
                // Close form window
                if(this.ownerCt.getXType() == 'window'){
                    this.ownerCt.close();
                }
            },
            scope: this
        },{
            // Submit form button
            xtype: 'button',
            text: OpenLayers.i18n('import'),
            iconCls: 'thumbnailGoIcon',
            handler: function(b,e) {
                if(this.getForm().isValid( )) {
                    
                    // Set default value _none_ to stylesheet combo
                    var SSCombo = this.find('name', 'styleSheet')[0];
                    if(!SSCombo.getValue()) {
                        SSCombo.setValue('_none_');
                    }
                    
                    // If we pasted data, then we call metadata.insert.past service
                    // with Ajax request cause the basic.Form is in fileUpload mode (so we can't
                    // use the form.submit() method
                    if(this.find('name', 'data')[0].isVisible()){
                        Ext.Ajax.request({
                            params: this.getForm().getValues(),
                            url: catalogue.services.mdInsertPaste,
                            success: function(response, opts) {
                                this.updateStatus(response, true);
                             },
                             failure: function(response, opts) {
                                 this.updateStatus(response, false);
                              },
                             scope: this
                        });
                    } 
                    // Else if we uploaded a file mef.import.ui service
                    else {
                        this.getForm().submit({
                            url: catalogue.services.mdInsertUpload,
                            success: function(form, action) {
                                this.updateStatus(action.result, true);
                             },
                             failure: function(form, action) {
                                 this.updateStatus(action.response, false);
                             },
                             scope: this
                        });
                    }
                }
            },
            scope: this
        }]
    },
    
    getStatusPanel: function() {
        if(!this.statusPanel) {
            this.statusPanel = new Ext.Panel({
                border: true,
                height: 120,
                bodyStyle: 'padding:15px',
                autoScroll: true,
                html: 'Test',
                bodyCssClass:'md-import-status-panel',
                hidden: true,
                anchor: '80%'
            });
        }
        return this.statusPanel;
    },
    
    /**
     * updateStatus
     * Update import status message in bottom info panel.
     * Message have success/failure css with appropriate colors
     */
    updateStatus: function(response, success) {
        this.statusPanel.setVisible(true);
        
        // Here the response elements are in response.responseXML object
        if(success) {
            
            var t = new Ext.XTemplate(
                '<div class="label-success-font">',
                '<div class="md-insert-status-title">'+OpenLayers.i18n('mdInsertResults')+'</div>',
                '<ul>', 
                '<tpl if="id">' + OpenLayers.i18n('mdInsertSuccess') + '<li><a href="javascript:catalogue.metadataShowById({id});">' + OpenLayers.i18n('mdIdentifier') + '{id}</a></li></tpl>',
                '<tpl if="uuid"><li>' + OpenLayers.i18n('mdUUID') + '{uuid}</li></tpl>',
                '<tpl if="records">',
                    '<li>'+ OpenLayers.i18n('mdRecordsProcessed') + ' : {records}</li>',
                    '<li>'+ OpenLayers.i18n('mdRecordsAdded') + ' : {records}</li>',
                '</tpl>',
                '</ul>',
                '</div>'
            );
            t.compile();
            
            // We got an XML response from paste Ajax request
            if(response.responseXML) {
                this.statusPanel.body.dom.innerHTML = t.apply({
                    id: Ext.DomQuery.selectNode('id', response.responseXML).childNodes[0].nodeValue,
                    uuid: Ext.DomQuery.selectNode('uuid', response.responseXML).childNodes[0].nodeValue,
                    records: null
                });
            }
            // We got a JSON response from the fileupload form.submit request
            else {
                this.statusPanel.body.dom.innerHTML = t.apply({
                    id: response.id,
                    uuid: '',
                    records: response.records
                });
            }
        }
        
        // The error message is contained in response.responseText element (xml string)
        else {
            var t = new Ext.Template([
                '<div class="label-error-font">',
                '<div class="md-insert-status-title">'+OpenLayers.i18n('mdInsertResults')+'</div>',
                '<p>' + OpenLayers.i18n('mdInsertFailure') + '</p>',
                '<p>{msg}</p>',
                '</div>'
            ]);
            t.compile();
            this.statusPanel.body.dom.innerHTML = t.apply({
                msg: response.responseText
            });
        }
    },
    
    /**
     * initComponent
     *   - merge config params
     *   - Add button
     *   - Initialize component
     *   - Add form fiels (items)
     */
    initComponent: function(){
        Ext.applyIf(this, this.defaultConfig);
        this.buttons = this.getButtons();
        GeoNetwork.editor.InsertMetadataPanel.superclass.initComponent.call(this);
        this.add(this.getItems());
        this.add({
            xtype: 'spacer',
            height: 25
        });
        this.add(this.getStatusPanel());
    }
});

/** api: xtype = gn_editor_insertmetadatapanel */
Ext.reg('gn_editor_insertmetadatapanel', GeoNetwork.editor.InsertMetadataPanel);