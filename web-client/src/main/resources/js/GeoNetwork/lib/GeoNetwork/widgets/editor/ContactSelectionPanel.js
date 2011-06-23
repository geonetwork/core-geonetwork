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

/**
 * Class: GeoNetwork.editor.ContactSelectionPanel
 * 
 * Deprecated : use SubTemplateSelectionPanel instead.
 */
GeoNetwork.editor.ContactSelectionPanel = Ext.extend(Ext.FormPanel, {
    border: false,
    catalogue: undefined,
    contact: Ext.data.Record.create([{
        name: 'xlink'
    }, {
        name: 'name'
    }]),
    contactStore: undefined,
    roleStore: undefined,
    
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
    contactCount: null,
    
    /**
     * Property: ref
     */
    ref: null,
    
    /**
     * Property: name	The node name with prefix
     */
    name: null,
    
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
     * APIProperty: ContactsSelected
     * {Object} Hash table of selected contacts with their XML raw data
     */
    ContactsSelected: [],
    
    initComponent: function(){
        this.contactStore = new Ext.data.Store({
            proxy: new Ext.data.HttpProxy({
                url: this.catalogue.services.rootUrl + "contacts.search",
                method: 'GET'
            }),
            baseParams: {
                ref: 'all',
                type: 'contains',
                mode: 'xml'
            },
            reader: new Ext.data.XmlReader({
                record: 'contact',
                id: 'xlink'
            }, this.contact),
            fields: ["xlink", "name"],
            sortInfo: {
                field: "name"
            }
        });
        
        this.items = [{
            xtype: 'panel',
            layout: 'fit',
            bodyStyle: 'padding: 5px;',
            border: false,
            tbar: [this.getContact()],
            items: [this.getContactsItemSelector()]
        }];
        
        this.contactStore.on({
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
        this.addEvents('contactSelected');
        
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
        }, this.getroleCombo(), ' ', {
            id: 'ContactSearchValidateButton',
            iconCls: 'addIcon',
            disabled: true,
            text: OpenLayers.i18n('add'),
            handler: function(){
                this.buildContactXmlList();
                // The event will be fired on requests response completed for
                // every role
            },
            scope: this
        }];
        
        GeoNetwork.editor.ContactSelectionPanel.superclass.initComponent.call(this);
    },
    
    getContact: function(){
    
        return new GeoNetwork.form.SearchField({
            id: 'ContactSearchField',
            width: 240,
            store: this.contactStore,
            paramName: 'name',
            value: ''
        });
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
    /**
     * APIMethod: setAddAsXLink
     * Set the add as XLink option
     */
    setAddAsXLink: function(xlink){
        this.addAsXLink = xlink || false;
        Ext.getCmp('addAsXLinkCheckBox').setValue(this.addAsXLink);
    },
    
    getroleCombo: function(){
        var role = Ext.data.Record.create([{
            name: 'code'
        }, {
            name: 'label'
        }, {
            name: 'description'
        }]);
        
        /**
         * Property: roleStore
         */
        this.roleStore = new Ext.data.Store({
            url: this.catalogue.services.rootUrl + 'contacts.getRole',
            reader: new Ext.data.XmlReader({
                record: 'entry'
            }, role),
            fields: ['code', 'label', 'description']
        });
        
        var combo = {
            xtype: 'combo',
            width: 150,
            id: 'contact-role',
            value: 0,
            store: this.roleStore,
            triggerAction: 'all',
            mode: 'local',
            emptyText: OpenLayers.i18n('contactRole'),
            displayField: 'label',
            valueField: 'code',
            listWidth: 250
        };
        
        // add the "" record
        var record = new role({
            label: ''
        });
        record.set('code', '');
        
        this.roleStore.add(record);
        
        this.roleStore.load({
            add: true
        });
        
        this.roleStore.on({
            'load': function(){
                Ext.getCmp('contact-role').setValue(this.defaultRole);
            },
            scope: this
        });
        
        
        return combo;
    },
    
    
    getContactsItemSelector: function(){
    
        var tpl = '<tpl for="."><div class="ux-mselect-item';
        if (Ext.isIE || Ext.isIE7) {
            tpl += '" unselectable=on';
        } else {
            tpl += ' x-unselectable"';
        }
        tpl += '>{name}</div></tpl>';
        
        this.itemSelector = new Ext.ux.ItemSelector({
            name: "itemselector",
            fieldLabel: "ItemSelector",
            dataFields: ["xlink", "name"],
            toData: [],
            msWidth: 320,
            msHeight: 230,
            valueField: "xlink",
            fromTpl: tpl,
            toTpl: tpl,
            toLegend: OpenLayers.i18n('selectedContacts'),
            fromLegend: OpenLayers.i18n('foundContacts'),
            fromStore: this.contactStore,
            fromAllowTrash: false,
            fromAllowDup: true,
            toAllowDup: false,
            drawUpIcon: false,
            drawDownIcon: false,
            drawTopIcon: false,
            drawBotIcon: false,
            imagePath: '../js/ext-ux/MultiselectItemSelector-3.0/icons', // FIXME
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
                Ext.getCmp('ContactSearchValidateButton').setDisabled(component.toStore.getCount() < 1);
            }
        });
        
        return this.itemSelector;
    },
    
    /**
     * Method: buildContactXmlList
     *
     * populate ContactsSelected array with xml strings
     */
    buildContactXmlList: function(){
    
        this.ContactsSelected = [];
        var self = this;
        this.role = Ext.getCmp('contact-role').getValue();
        this.contactCount = 0;
        var store = this.itemSelector.toMultiselect.store;
        xlinkCollection = store.collect('xlink');
        Ext.each(xlinkCollection, function(xlink, index, xlinks){
            var values = store.collect('uri');
            
            var inputValue = xlink + self.role;
            
            ++self.contactCount;
            if (self.addAsXLink) {
                // Could be gmd:contact, gmd:pointOfContact, ...
                var tpl = "<" + self.name + " xmlns:xlink='http://www.w3.org/1999/xlink' xmlns:gmd='http://www.isotc211.org/2005/gmd'  xmlns:gco='http://www.isotc211.org/2005/gco'" +
                " xlink:href='" +
                inputValue +
                "'/>";
                self.ContactsSelected.push(tpl);
            } else {
                self.retrieveContactData(inputValue);
            }
            
        });
        
        if (this.addAsXLink) {
            this.fireEvent('contactSelected', this, this.ContactsSelected);
            this.ownerCt.hide();
        }
        
        store.clearFilter();
    },
    
    /**
     * Method: retrieveContactData
     *
     * Load Contact data, transform it to a json object, & put it in selectedContactsJson
     */
    retrieveContactData: function(url){
    
        Ext.getCmp('ContactSearchValidateButton').disable();
        
        Ext.Ajax.request({
        
            url: url,
            method: 'GET',
            scope: this,
            success: function(response){
                var Contact = response.responseText;
                if (Contact.indexOf('<gmd:CI_ResponsibleParty') !== -1) {
                    this.ContactsSelected.push("<" + this.name + " xmlns:xlink='http://www.w3.org/1999/xlink' xmlns:gmd='http://www.isotc211.org/2005/gmd'>" + response.responseText + "</" + this.name + ">");
                }
                Ext.getCmp('ContactSearchValidateButton').enable();
                this.contactCount -= 1;
                
                if (this.contactCount === 0) {
                    // Wait until the request for *each* role has ended before
                    // firing the event & closing the window
                    this.fireEvent('contactSelected', this, this.ContactsSelected);
                    this.ownerCt.hide();
                }
            }
            
        });
        
    }
});


/** api: xtype = gn_editor_contactselectionpanel */
Ext.reg('gn_editor_contactselectionpanel', GeoNetwork.editor.ContactSelectionPanel);