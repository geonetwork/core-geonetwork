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
 *  class = EditorToolbar
 *  base_link = `Ext.Toolbar <http://extjs.com/deploy/dev/docs/?class=Ext.Toolbar>`_
 *
 */
/** api: constructor 
 *  .. class:: EditorToolbar(config)
 *
 *     Create a metadata results tool bar which interact with
 *     :class:`GeoNetwork.MetadataResultsView`
 *
 *
 */
GeoNetwork.editor.EditorToolbar = Ext.extend(Ext.Toolbar, {
    /** api: config[catalogue] 
     * ``GeoNetwork.Catalogue`` Catalogue to use
     */
    catalogue: undefined,
    editor: undefined,
    defaultConfig: {
        isTemplate: false,
        /**
         * Use this property to add the metadata type selector. Usually, not displayed 
         * for sub-template editing or when interface only allows to edit one kind of records.
         */
        hideTypeMenu: false,
        /**
         * Use this property to display or not the minor edit mode.
         */
        hideMinorEdit: false,
        editAttributes: false
    },
    mapOptions: undefined,
    layers: undefined,
    
    // Menus and actions
    typeMenu: undefined,
    configMenu: undefined,
    viewMenu: undefined,
    saveAction: undefined,
    saveAndCloseAction: undefined,
    checkAction: undefined,
    resetAction: undefined,
    cancelAction: undefined,
    minorCheckbox: undefined,
    minorEdit: false,
    
    /** private: method[initComponent] 
     *  Initializes the toolbar for the metadata editor.
     */
    initComponent: function(){
        Ext.applyIf(this, this.defaultConfig);
    
        var cmp = [];
        if (!this.hideTypeMenu) {
            cmp.push(this.createTypeMenu());
            cmp.push(['-']);
        }
        
        this.saveAction = new Ext.Action({
            text: OpenLayers.i18n('save'),
            iconCls: 'saveMetadata',
            ctCls: 'gn-bt-main',
            handler: function(){
                this.save();
            },
            scope: this.editor
        });
        
        this.checkAction = new Ext.Action({
            text: OpenLayers.i18n('saveAndCheck'),
            iconCls: 'validateMetadata',
            handler: function(){
                this.validationPanel.validate();
            },
            scope: this.editor
        });
        
        this.saveAndCloseAction = new Ext.Action({
            text: OpenLayers.i18n('saveAndClose'),
            iconCls: 'quitMetadata',
            handler: function(){
                this.finish();
            },
            scope: this.editor
        });
        
//        this.minorCheckbox = new Ext.form.Checkbox({
//            checked: false,
//            boxLabel: OpenLayers.i18n('minorEdit'),
//            listeners: {
//                check: function(c, checked){
//                    document.mainForm.minor.value = this.minorEdit = checked;
//                },
//                scope: this
//            }
//        });
        this.minorCheckbox = new Ext.Button({
            enableToggle: true,
            hidden: this.hideMinorEdit,
            text: OpenLayers.i18n('minorEdit'),
            tooltip: OpenLayers.i18n('minorEditTT'),
            listeners: {
                toggle: function(c, pressed){
                    document.mainForm.minor.value = this.minorEdit = pressed;
                },
                scope: this
            }
        });
        
        this.resetAction = new Ext.Action({
            text: OpenLayers.i18n('reset'),
            iconCls: 'refreshMetadata',
            handler: function(){
                this.reset();
                
            },
            scope: this.editor
        });
        
        this.cancelAction = new Ext.Action({
            text: OpenLayers.i18n('cancel'),
            iconCls: 'cancel',
            handler: function(){
                this.cancel();
            },
            scope: this.editor
        });
        
        cmp.push(this.createViewMenu(), ['-'], this.saveAction, this.checkAction, this.saveAndCloseAction, this.minorCheckbox, 
                ['->'], this.resetAction, this.cancelAction, this.configMenu());

        GeoNetwork.editor.EditorToolbar.superclass.initComponent.call(this);
        
        this.add(cmp);
    },
    setIsTemplate: function(is){
        if (!this.hideTypeMenu) {
            var isTemplate = is === 'y';
            Ext.getCmp('type_y').setChecked(isTemplate);
            Ext.getCmp('type_n').setChecked(!isTemplate);
        }
    },
    setIsMinor: function(minor){
        var state = true;
        if (minor==='' || minor==='false') {
            state = false;
        }
        this.minorCheckbox.toggle(state);
    },
    createTypeMenu: function(){
    
        this.typeMenu = {
            text: OpenLayers.i18n('type'),
            menu: {
                items: [
                    '<b class="menu-title">' + OpenLayers.i18n('chooseAType') + '</b>', {
                        text: OpenLayers.i18n('md'),
                        checked: true,
                        value: 'n',
                        id: 'type_n',
                        group: 'type',
                        checkHandler: this.onTypeCheck
                    }, {
                        text: OpenLayers.i18n('tpl'),
                        checked: true,
                        id: 'type_y',
                        group: 'type',
                        value: 'y',
                        checkHandler: this.onTypeCheck
                    }
                ]
            }
        };
        
        return this.typeMenu;
    },
    configMenu: function(){
        var tgVisibility = new Ext.menu.CheckItem({
                text: OpenLayers.i18n('collapseAll'),
                checked: false,
                checkHandler: function(){
                    Ext.each(Ext.DomQuery.select('span.toggle'), function(i) {
                        if (i.onclick) {
                            i.onclick();
                        }
                    });
                }
            });
        
        this.configMenu = {
            iconCls: 'configIcon',
            menu: {
                items: [tgVisibility,
                    {
                        text: OpenLayers.i18n('editAttributes'),
                        checked: this.editAttributes,
                        checkHandler: function (item, checked) {
                            this.editAttributes = checked;
                            this.editAttributesVisibility();
                        },
                        scope: this
                    }
                ]
            }
        };
        
        this.editor.on('metadataUpdated', function () {
            this.editAttributesVisibility();
            tgVisibility.setChecked(false, true);   // reset collapsed sections - section are never collapsed when rendered
        }, this);
        
        return this.configMenu;
    },
    editAttributesVisibility: function(){
        Ext.each(Ext.select('div.toggle-attr'), function(i) {
            i.setVisibilityMode(Ext.Element.DISPLAY);
            i.setVisible(this.editAttributes);
        }, this);
    },
    createViewMenu: function(modes){
        var items = ['<b class="menu-title">' + OpenLayers.i18n('chooseAView') + '</b>'];
        
        this.viewMenu = new Ext.menu.Menu({
            items: items
        });
        
        var viewButton = {
            text: OpenLayers.i18n('viewMode'),
            iconCls: 'viewModeIcon',
            menu: this.viewMenu
        };
        
        return viewButton;
    },
    updateViewMenu: function(modes){
        var i, m;
        this.viewMenu.removeAll();
        for (i = 0; i < modes.length; i++) {
            m = modes[i];
            this.viewMenu.add({
                text: m[0],
                checked: false,
                disabled: m[2], // Disable current mode
                group: 'mode',
                value: m[1],
                listeners: {
                    'checkchange': this.onViewCheck,
                    scope: this // FIXME : this needs to be editor
                }
            });
        }
        this.viewMenu.doLayout();
    },
    onViewCheck: function(item, checked){
        if (checked) {
            this.editor.editorMainPanel.getEl().parent().scrollTo('top', 0);
            document.mainForm.currTab.value = item.value;
            this.saveAction.execute();
        }
    },
    onTypeCheck: function(item, checked){
        if (checked) {
            Ext.get('template').dom.value = item.value;
        }
    },
    /** private: method[onDestroy] 
     *
     *  Private method called during the destroy
     *  sequence.
     *
     *  TODO : what to do in destroy ?
     */
    onDestroy: function(){
        GeoNetwork.editor.EditorToolbar.superclass.onDestroy.apply(this, arguments);
    }
});

/** api: xtype = gn_editor_editortoolbar */
Ext.reg('gn_editor_editortoolbar', GeoNetwork.editor.EditorToolbar);