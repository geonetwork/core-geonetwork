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
 *  class = SubTemplateSelectionPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
/** api: constructor 
 *  .. class:: SubTemplateSelectionPanel()
 *
 *     Create a GeoNetwork subtemplate selector for a type
 *     of directory.
 *     
 *     See subtemplate manager (TODO add hyperlink) panel for configuration of subtemplates.
 *     
 *	More information available `here <http://trac.osgeo.org/geonetwork/wiki/proposals/SubTemplates>`_
 */
GeoNetwork.editor.SubTemplateSelectionPanel = Ext.extend(Ext.FormPanel, {
    border: false,
    catalogue: undefined,
    id: 'subTemplateSearchForm',
    contact: Ext.data.Record.create([{
        name: 'xlink'
    }, {
        name: 'name'
    }]),
    subTemplateStore: undefined,
    codeListStore: undefined,
    subTplTypeField: undefined,
    /**
     * relative imagePath for ItemSelector
     */
    imagePath: undefined,
    
    /**
     * Property: itemSelector
     */
    addAsXLink: false,
    
    /**	
     * Property: itemSelector
     */
    itemSelector: null,
    
    /**
     * Property: loadingMask
     */
    loadingMask: null,
    
    /**
     * Property: roleCount
     */
    subTemplateCount: null,
    
    /**
     * Property: ref
     */
    ref: null,
    
    /**
     * Property: name	The sub template type name
     */
    name: null,
    
    /**
     * Property: name   The node name with prefix
     */
    elementName: null,
    
    /**
     * Property: namespaceString   The namespace string to append to the element
     */
    namespaceString: '',
    
    /**
     * Property: name   The node name with prefix
     */
    codeListConfig: {
        // 'SubTemplateType': {
        //  'xpath': xpath expression
        //  'url': url to retrieve the codelist values
        'gmd:CI_ResponsibleParty': {
            xpath: 'gmd:role/gmd:CI_RoleCode/@codeListValue',
            url: '',
            label: OpenLayers.i18n('contactRole'),
            code: 'gmd:CI_RoleCode'
        }
    },
    codeListCombo: null,
    /**
     * Property: role
     */
    role: '',
    
    /**
     * Property: role
     * Default role to be used for all selected contacts.
     */
    defaultRole: 'pointOfContact',
    
    /**
     * APIProperty: SubTemplateSelected
     * {Object} Hash table of selected contacts with their XML raw data
     */
    SubTemplateSelected: [],
    initComponent: function(){
        this.subTemplateStore = GeoNetwork.data.MetadataResultsStore();

        this.items = [{
            xtype: 'panel',
            layout: 'fit',
            bodyStyle: 'padding: 5px;',
            border: false,
            items: [this.getSearchField(), this.getSubTemplateItemSelector()]
        }];
        
        this.subTemplateStore.on({
            'loadexception': function(){
            },
            'beforeload': function(store, options){
                if (!this.loadingMask) {
                    this.loadingMask = new Ext.LoadMask(this.itemSelector.fromMultiselect.getEl(), {
                        msg: OpenLayers.i18n('searching')
                    });
                }
                this.loadingMask.show();
            },
            'load': function(){
                this.loadingMask.hide();
            },
            scope: this
        });
        
        /**
         * triggered when the user has selected a Contact
         */
        this.addEvents('subTemplateSelected');
        
        this.bbar = ['->', {
            id: 'addAsXLinkCheckBox',
            xtype: 'checkbox',
            checked: false,
            boxLabel: OpenLayers.i18n('addAsXLink'),
            listeners: {
                check: function(c, checked){
                    this.addAsXLink = checked;
                },
                scope: this
            }
        }, this.getCodeListCombo(), ' ', {
            id: 'SubTemplateSearchValidateButton',
            iconCls: 'addIcon',
            disabled: true,
            text: OpenLayers.i18n('add'),
            handler: function(){
                this.buildSubTemplateXmlList();
                // The event will be fired on requests response completed for
                // every role
            },
            scope: this
        }];
        
        GeoNetwork.editor.SubTemplateSelectionPanel.superclass.initComponent.call(this);
    },
    
    getSearchField: function(){
        this.subTplTypeField = new Ext.form.TextField({
                name: 'E__root',
                hidden: true,
                value: ''
            });
        return [new GeoNetwork.form.SearchField({
            name: 'E_any',
            store: this.catalogue.metadataStore,
            triggerAction: function(scope){
                 scope.doSearch();
            },
            scope: this
        }),this.subTplTypeField
            , {
                xtype: 'textfield',
                name: 'E__isTemplate',
                hidden: true,
                value: 's'
            }];
    },
    
    /**
     * APIMethod: setRef
     * Set the element reference
     */
    setRef: function(ref){
        this.ref = ref;
    },
    /**
     * APIMethod: setClass
     * Set the element reference
     */
    setName: function(name){
        this.name = name;
    },
    setElementName: function(name){
        this.elementName = name;
        this.subTplTypeField.setValue(name);
        
        if (this.codeListConfig[name]){
            this.codeListCombo.setVisible(true);
            // FIXME : this.codeListCombo.label = this.codeListConfig[name].label;
            // TODO : url for the codeListStore
        } else {
            this.codeListCombo.setVisible(false);
            // this.codeListCombo.label = '';
        }
        
    },
    setNamespaces: function(namespaceString){
        this.namespaceString = namespaceString;
    },
    /**
     * APIMethod: setAddAsXLink
     * Set the add as XLink option
     */
    setAddAsXLink: function(xlink){
        this.addAsXLink = xlink || false;
        Ext.getCmp('addAsXLinkCheckBox').setValue(this.addAsXLink);
    },
    
    // TODO : this only applies to element set in codeListConfig
    getCodeListCombo: function(){
        /**
         * Property: codeListStore
         */
        this.codeListStore = GeoNetwork.data.CodeListStore({
            url: this.catalogue.services.schemaInfo,
            codeListName: 'gmd:CI_RoleCode'// FIXME this.codeListConfig[name].code
        });

        this.codeListCombo = new Ext.form.ComboBox({
            id: 'codeList',
            store: this.codeListStore,
            triggerAction: 'all',
            mode: 'local',
            fieldLabel: '',
            displayField: 'label',
            valueField: 'code'
        });
        
        this.codeListStore.on({
            'load': function(){
                Ext.getCmp('codeList').setValue(this.defaultRole);
            },
            scope: this
        });
        return this.codeListCombo;
    },
    
    
    getSubTemplateItemSelector: function(){
    
        var tpl = '<tpl for="."><div class="ux-mselect-item';
        if (Ext.isIE || Ext.isIE7) {
            tpl += '" unselectable=on';
        } else {
            tpl += ' x-unselectable"';
        }
        tpl += '>{title}</div></tpl>';
        
        this.itemSelector = new Ext.ux.ItemSelector({
            name: "itemselector",
            fieldLabel: "ItemSelector",
            dataFields: ["uuid", "title"],
            toData: [],
            msWidth: 320,
            msHeight: 230,
            valueField: "uuid",
            fromTpl: tpl,
            toTpl: tpl,
            toLegend: OpenLayers.i18n('selected'),
            fromLegend: OpenLayers.i18n('found'),
            fromStore: this.subTemplateStore,
            fromAllowTrash: false,
            fromAllowDup: true,
            toAllowDup: false,
            drawUpIcon: false,
            drawDownIcon: false,
            drawTopIcon: false,
            drawBotIcon: false,
            imagePath: this.imagePath,
            toTBar: [{
                text: OpenLayers.i18n('clear'),
                handler: function(){
                    var i = this.getForm().findField("itemselector");
                    i.reset.call(i);
                },
                scope: this
            }]
        });
        
        // enable the validate button only if there are selected Contacts
        this.itemSelector.on({
            'change': function(component){
                Ext.getCmp('SubTemplateSearchValidateButton').setDisabled(component.toStore.getCount() < 1);
            }
        });
        
        return this.itemSelector;
    },
    
    /**
     * Method: buildSubTemplateXmlList
     *
     * populate SubTemplateSelected array with xml strings
     */
    buildSubTemplateXmlList: function(){
        // Never try to create an XLink with a root element with undefined
        if (!this.elementName) {
           alert('Error: sub template root element name is not defined!');
           return;
        }
        
        var processParameter = '';
        var processParameterSeparator = '~';
        var store = this.itemSelector.toMultiselect.store;
        
        this.SubTemplateSelected = [];
        this.codeListValue = '';
        this.subTemplateCount = 0;

        var codelistValue = Ext.getCmp('codeList').getValue();
        
        if (this.codeListConfig[this.elementName] && this.codeListConfig[this.elementName].xpath && codelistValue !== "") {
            processParameter = "&process=" + this.codeListConfig[this.elementName].xpath + processParameterSeparator + codelistValue;
        }
        
        var self = this;
        xlinkCollection = store.collect('uuid');
        Ext.each(xlinkCollection, function(uuid, index, xlinks){
            
            var inputValue = self.catalogue.services.subTemplate + "?uuid=" + uuid + processParameter;
            
            ++self.subTemplateCount;
            if (self.addAsXLink) {
                var tpl = "<" + self.name + self.generateNamespaceDeclaration() + 
                    " xlink:href='" +
                    inputValue +
                    "'/>";
                self.SubTemplateSelected.push(tpl);
            } else {
                self.retrieveSubTemplate(inputValue);
            }
        });
        
        if (this.addAsXLink) {
            this.fireEvent('subTemplateSelected', this, this.SubTemplateSelected);
            this.ownerCt.hide();
        }
        
        store.clearFilter();
    },
    
    /**
     * Method: retrieveSubTemplate
     *
     * Load Contact data, transform it to a json object, & put it in selectedContactsJson
     */
    retrieveSubTemplate: function(url){
    
        Ext.getCmp('SubTemplateSearchValidateButton').disable();
        
        Ext.Ajax.request({
            url: url,
            method: 'GET',
            scope: this,
            success: function(response){
                var st = response.responseText;
                
                if (st.indexOf(this.elementName) !== -1) {    // In case of errors
                    this.SubTemplateSelected.push("<" + this.name + this.generateNamespaceDeclaration() + ">" + response.responseText + "</" + this.name + ">");
                }
                Ext.getCmp('SubTemplateSearchValidateButton').enable();
                this.subTemplateCount -= 1;
                
                if (this.subTemplateCount === 0) {
                    // Wait until the request for *each* role has ended before
                    // firing the event & closing the window
                    this.fireEvent('subTemplateSelected', this, this.SubTemplateSelected);
                    this.ownerCt.hide();
                }
            }
        });
    },
    generateNamespaceDeclaration: function() {
        return ' '+this.namespaceString;
    },
    
    doSearch: function(){
        if (!this.loadingMask) {
            this.loadingMask = new Ext.LoadMask(this.getEl(), {
                msg: OpenLayers.i18n('searching')
            });
        }
        this.loadingMask.show();
//        Paging ?
//        GeoNetwork.editor.nbResultPerPage = 20;
//        if (Ext.getCmp('nbResultPerPage')) {
//            GeoNetwork.editor.nbResultPerPage = Ext.getCmp('nbResultPerPage').getValue();
//        }
        GeoNetwork.util.SearchTools.doQueryFromForm(this.id, this.catalogue, 
            1, this.showResults.bind(this), null, 
            false, this.subTemplateStore);
    },
    
    showResults: function(response){
        var getRecordsFormat = new OpenLayers.Format.GeoNetworkRecords();
        var r = getRecordsFormat.read(response.responseText);
        var values = r.records;
        this.subTemplateStore.loadData(r);
    }
});

/** api: xtype = gn_editor_subtemplateselectionpanel */
Ext.reg('gn_editor_subtemplateselectionpanel', GeoNetwork.editor.SubTemplateSelectionPanel);
