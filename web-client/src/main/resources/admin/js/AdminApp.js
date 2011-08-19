Ext.namespace('GeoNetwork');

var catalogue;
var app;

GeoNetwork.adminApp = function() {
    // private vars:
    /**
     * Default language to use
     */
    var defaultLocale = 'en';

    /**
     * Catalogue manager
     */
    var catalogue;
    
    
    var harvesterStore;
    
    /**
     * Create a language switcher mode
     * 
     * @return
     */
    function createLanguageSwitcher() {

        return new Ext.FormPanel({
                    renderTo : 'lang-form',
                    width : 80,
                    border : false,
                    layout : 'hbox',
                    items : [new Ext.form.ComboBox({
                                name : 'lang',
                                mode : 'local',
                                triggerAction: 'all',
                                width : 80,
                                store : new Ext.data.ArrayStore({
                                    id : 0,
                                    fields : ['id', 'name'],
                                    data : [['en', 'English']]
                                        /* TODO : Get language list from server */
                                    }),
                                valueField : 'id',
                                displayField : 'name',
                                value : defaultLocale
                            })]
                });
    }

    /**
     * Create a default login form and register extra events in case of error.
     * 
     * @return
     */
    function createLoginForm() {
        new GeoNetwork.LoginForm({
                    renderTo : 'login-form',
                    catalogue : catalogue
                });

        catalogue.on('afterBadLogin', loginAlert, this);
    }

    /**
     * Error message in case of bad login
     * 
     * @param cat
     * @param user
     * @return
     */
    function loginAlert(cat, user) {
        Ext.Msg.show({
                    title : 'Login',
                    msg : 'Login failed. Check your username and password.',
                    /* TODO : Get more info about the error */
                    icon : Ext.MessageBox.ERROR,
                    buttons : Ext.MessageBox.OK
                });
    }

    /**
     * private: method[createMetadataGroup]
     * 
     * Load user metadata on load
     */
    function createYourMetadataPanel() {

        
        var grid = new Ext.grid.GridPanel({
                    store : catalogue.metadataStore,
                    columns : [{
                                id : 'uuid',
                                header : 'uuid',
                                width : 60,
                                sortable : true,
                                dataIndex : 'uuid',
                                hidden : true
                            }, {
                                header : 'Schema',
                                width : 75,
                                sortable : true,
                                dataIndex : 'schema'
                            }, {
                                header : 'Title',
                                width : 200,
                                sortable : true,
                                dataIndex : 'title'
                            }, {
                                header : 'Last Updated',
                                width : 120,
                                sortable : true,
                                dataIndex : 'changedate'
                            }, {
                                header : 'Creation date',
                                width : 120,
                                sortable : true,
                                dataIndex : 'createdate'
                            }, {
                                header : 'Owner',
                                width : 60,
                                sortable : true,
                                dataIndex : 'ownername'
                            }, {
                                header : 'Editable',
                                width : 60,
                                sortable : true,
                                dataIndex : 'edit'
                            }],
                    stripeRows : true,
                    height : 350,
                    stateful : true,
                    border : false,
                    stateId : 'grid'
                });
        // TODO : add paging and selector for editing

        var panel = new Ext.Panel({
                    border : false,
                    frame : false,
                    layout : 'fit',
                    autoWidth : true,
                    autoHeight : true,
                    items : [grid],
                    listeners: {
                        enable: function() {
                            catalogue.kvpSearch("fast=false&", null, null, null, true); 
                            // TODO : Search your metadata only using owner id
                            // TODO : Load when tab is activated
                        }
                    }
                });
        return panel;
    }

    /**
     * api: property[templateStore] ``GeoNetwork.data.MetadataResultsStore``
     * Template available for user
     */
    var templateStore = GeoNetwork.data.MetadataResultsStore();

    var groupStore;

    /**
     * private: method[loadTemplate] Load template in template store
     */
    function loadTemplate(result) {
        var getRecordsFormat = new OpenLayers.Format.GeoNetworkRecords();
        var records = getRecordsFormat.read(result.responseText);
        if (records.records.length > 0) {
            templateStore.loadData(records);
        }
    }

    /**
     * private: methode[createNewMetadataPanel]
     * 
     * 
     */
    function createNewMetadataPanel() {

        catalogue.kvpSearch("fast=false&template=y", loadTemplate, null, null,
                false); // TODO : load when activated. Triggering many search
                // concurrently in same session could produced AlreadyCloseException 
                // in decRef. 

        var templateCb = new Ext.form.ComboBox({
            //name : 'uuid',
            emptyText: 'Select a template ...',
            allowBlank: false,
            typeAhead : true,
            triggerAction : 'all',
            lazyRender : true,
            mode : 'local',
            width : 400,
            tpl : '<tpl for="."><div class="x-combo-list-item">{title} ({schema})</div></tpl>',
            // TODO : Add tooltip with abstract - which could gave more info
            // about that template ?
            store : templateStore, // TODO : add schema in ()
            valueField : 'uuid',
            displayField : 'title',
            hiddenName: 'uuid',
            hiddenValue: 'uuid'
        });

        groupStore = GeoNetwork.data.GroupStore(catalogue.services.getGroups);
        groupStore.load();
        var groupCb = new Ext.form.ComboBox({
            emptyText: 'Select a group ...',
            allowBlank: false,
            typeAhead : true,
            triggerAction : 'all',
            lazyRender : true,
            mode : 'local',
            width : 400,
            // TODO : Add tooltip with abstract - which could gave more info
            // about that template ?
            store : groupStore,
            valueField : 'id',
            displayField : 'name',
            hiddenName: 'group',
            hiddenValue: 'id'
        });
        
        var panel = new Ext.FormPanel({
                    border : false,
                    // title : 'ee',
                    url : catalogue.services.mdDuplicate,
                    frame : false,
                    layout : 'form',
                    id : 'createMetadataForm',
                    autoWidth : true,
                    autoHeight : true,
                    standardSubmit : true,
                    items : [templateCb, groupCb],
                    buttons : [{
                                tooltip : 'Reset search form values.',
                                // iconCls: 'md-mn-reset',
                                id : 'resetBt',
                                icon : '../images/default/cross.png',
                                listeners : {
                                    click : function() {
                                        Ext.getCmp('createMetadataForm').getForm()
                                                .reset();
                                    }
                                }
                            },
                            {
                                text : 'Create metadata',
                                listeners : {
                                    click : function() {
                                       var fp = this.ownerCt.ownerCt, form = fp.getForm();
                                       if (form.isValid()) {
                                         form.submit();
                                       }
                                    }
                                }
                            }]
                });
        return panel;
    }

    /**
     * private: method[createMetadataGroup]
     * 
     */
    function createMetadataGroup() {
        return [{
                    // xtype: 'portal',
                    title : 'Metadata',
                    tabTip : '',
                    items : [{
                                style : 'padding:10px 0 10px 10px',
                                items : [
                                {
                                    html: 'This is an experimental demo interface for the administration panel'
                                },
                                createYourMetadataPanel()
                                // Statistics for current user ?
                                ]
                            }]
                }, {
                    title : 'New metadata',
                    layout : 'fit',
                    iconCls : 'x-icon-tickets',
                    tabTip : '',
                    style : 'padding: 10px;',
                    items : [createNewMetadataPanel()]
                }, {
                    title : 'Insert metadata',
                    iconCls : 'x-icon-subscriptions',
                    tabTip : '',
                    style : 'padding: 10px;',
                    layout : 'fit',
                    items : [{
                                // xtype : 'tabpanel',
                                // activeTab : 1,
                                items : [{
                                            title : 'Insert form',
                                            html : 'MEF / COPY-PASTE'
                                        }]
                            }]
//                }, {
//                    title : 'Batch import',
//                    iconCls : 'x-icon-users',
//                    tabTip : '',
//                    style : 'padding: 10px;',
//                    html : 'dddd'
                }, {
                    title : 'Sample metadata',
                    iconCls : 'x-icon-users',
                    tabTip : '',
                    style : 'padding: 10px;',
                    html : 'dddd'
                }];
    }

    function createHarvesterPanel () {

        // Get type of harvester and configuration options
        
        var panel = new Ext.FormPanel({
                border : false,
                // title : 'ee',
                frame : false,
                layout : 'fit',
                autoWidth : true,
                autoHeight : true,
                items : [],
                buttons : []
            });
        return panel;
    
        
    }
    
    function createSubTemplateManagerPanel() {
        var subTemplatePanel = new GeoNetwork.admin.SubTemplateManagerPanel({
           catalogue: catalogue
       });
       return subTemplatePanel;
   }
    
    function createThesaurusManagerPanel() {
        var thesaurusPanel = new GeoNetwork.admin.ThesaurusManagerPanel({
           catalogue: catalogue
       });
       return thesaurusPanel;
   }
   
    function createHarvestingPanel () {

        harvesterStore = GeoNetwork.data.HarvesterStore(catalogue.services.getHarvesters);
        harvesterStore.load();
        
        var panel = new GeoNetwork.admin.HarvesterPanel({
            catalogue: catalogue,
            harvesterStore : harvesterStore
        });
                  
        return panel;
        
    }
  
    
    // public space:
    return {
        subTemplatePanel: undefined,
        init : function() {
            var app = this;
            // TODO : get url param like language
            // TODO : check already logged in

            // Create connexion to the catalogue
            catalogue = new GeoNetwork.Catalogue({
                        statusBarId : 'info',
                        hostUrl: '../../',
                        lang: 'en', 
                        mdOverlayedCmpId : 'resultsPanel'
                    });
            // Declare default store to be used for records and summary
            catalogue.metadataStore = GeoNetwork.data.MetadataResultsStore();
            catalogue.summaryStore = GeoNetwork.data.MetadataSummaryStore();

            // Top navigation widgets
            createLanguageSwitcher();
            createLoginForm();

            // Register events on the catalogue

            // Could we create an array of
            // name : panel url and create Viewport content ?
            this.subTemplatePanel = createSubTemplateManagerPanel();
            this.thesaurusPanel = createThesaurusManagerPanel();

            var margins = '35 0 0 0';
            var viewport = new Ext.Viewport({
                layout : 'fit',
                items : [{
                    xtype : 'grouptabpanel',
                    tabWidth : 200,
                    style : {
                        top : '35px',
                        position : 'absolute'
                    },
                    activeGroup : 4,
                    items : [{
                                items : createMetadataGroup(),
                                listeners: {
                                    tabchange: function() {
                                        // TODO : On tab activation load required store console.log("Changed");
                                    }
                                }
                            }, {
                                expanded : false,
                                margins : margins,
                                items : [{
                                            title : 'Template',
                                            iconCls : 'x-icon-configuration',
                                            tabTip : 'Configuration tabtip',
                                            style : 'padding: 10px;',
                                            html : 'Template configuration'
                                        }, {
                                            title : 'Sort template',
                                            iconCls : 'x-icon-templates',
                                            tabTip : 'Templates tabtip',
                                            style : 'padding: 10px;',
                                            html : 'Sort your templates'
                                        }, {
                                            title : 'Add default templates',
                                            iconCls : 'x-icon-templates',
                                            tabTip : 'Templates tabtip',
                                            style : 'padding: 10px;',
                                            html : 'Add default templates'
                                        }]
                            }, {
                                expanded : false,
                                margins : margins,
                                items : [{
                                            title : OpenLayers.i18n('directory'),
                                            iconCls : 'x-icon-configuration',
                                            tabTip : 'Configuration tabtip',
                                            style : 'padding: 10px;',
                                            html : 'Directory configuration'
                                        }, {
                                            title : OpenLayers.i18n('manageDirectories'),
                                            iconCls : 'x-icon-templates',
                                            tabTip : 'Templates tabtip',
                                            style : 'padding: 10px;',
                                            items: this.subTemplatePanel
                                        }
                                ],
                                listeners: {
                                    tabchange: function(o, activeTab) {
                                        this.subTemplatePanel.refresh();
                                    },
                                    scope: this
                                }
                            }, {
                                expanded : false,
                                margins : margins,
                                items : [{
                                            title : 'Users & groups',
                                            iconCls : 'x-icon-configuration',
                                            tabTip : 'Configuration tabtip',
                                            style : 'padding: 10px;',
                                            html : ''
                                        }, {
                                            title : 'Change password',
                                            iconCls : 'x-icon-templates',
                                            tabTip : 'Templates tabtip',
                                            style : 'padding: 10px;',
                                            html : ''
                                        }, {
                                            title : 'Change user information',
                                            iconCls : 'x-icon-templates',
                                            tabTip : 'Templates tabtip',
                                            style : 'padding: 10px;',
                                            html : ''
                                        }, {
                                            title : 'User management',
                                            iconCls : 'x-icon-templates',
                                            tabTip : 'Templates tabtip',
                                            style : 'padding: 10px;',
                                            html : ''
                                        }, {
                                            title : 'Group management',
                                            iconCls : 'x-icon-templates',
                                            tabTip : 'Templates tabtip',
                                            style : 'padding: 10px;',
                                            html : ''
                                        }]
                            }, {
                                expanded : false,
                                margins : margins,
                                items : [
                                         {
                                            title : 'Thesaurus & category',
                                            iconCls : 'x-icon-configuration',
                                            tabTip : 'Configuration tabtip',
                                            style : 'padding: 10px;',
                                            html : ''
                                        }, {
                                            title : 'Category management',
                                            iconCls : 'x-icon-templates',
                                            tabTip : 'Templates tabtip',
                                            style : 'padding: 10px;',
                                            html : ''
                                        }, {
                                            title : 'Thesaurus management',
                                            iconCls : 'x-icon-templates',
                                            tabTip : 'Templates tabtip',
                                            style : 'padding: 10px;',
                                            items : this.thesaurusPanel
                                        }]
                            },{
                                expanded : true,
                                margins : margins,
                                items : [{
                                            title : 'Harvesting',
                                            iconCls : 'x-icon-configuration',
                                            tabTip : 'Harvesting configuration',
                                            style : 'padding: 10px;',
                                            items: createHarvestingPanel()
                                        }
                                        // It could be nice to have some more info about harvesting (eg. history, stats)
                                        ]
                             }, {
                                expanded : true,
                                margins : margins,
                                items : [{
                                            title : 'Catalogue',
                                            iconCls : 'x-icon-configuration',
                                            tabTip : 'Configuration tabtip',
                                            style : 'padding: 10px;',
                                            html : ''
                                        }, {
                                            title : 'System configuration', // /xml.config.get
                                            iconCls : 'x-icon-templates',
                                            tabTip : 'Templates tabtip',
                                            style : 'padding: 10px;',
                                            html : 'Add default templates'
                                        }, {
                                            title : 'System information',
                                            iconCls : 'x-icon-templates',
                                            tabTip : 'Templates tabtip',
                                            style : 'padding: 10px;',
                                            html : 'Add default templates'
                                        }, {
                                            title : 'Index management',
                                            iconCls : 'x-icon-templates',
                                            tabTip : 'Templates tabtip',
                                            style : 'padding: 10px;',
                                            html : 'Add default templates'
                                        }, {
                                            title : 'Localization',
                                            iconCls : 'x-icon-templates',
                                            tabTip : 'Templates tabtip',
                                            style : 'padding: 10px;',
                                            html : 'Add default templates'
                                        }]
                            }, {
                                expanded : false,
                                margins : margins,
                                items : [{
                                            title : 'Tests',
                                            iconCls : 'x-icon-configuration',
                                            tabTip : 'Configuration tabtip',
                                            style : 'padding: 10px;',
                                            html : ''
                                        }]
                            }]
                }]
            });
        },
        getCatalogue : function() {
            return catalogue;
        }
    };
};

Ext.onReady(function() {
    Ext.QuickTips.init();

    app = new GeoNetwork.adminApp();
    app.init();
    catalogue = app.getCatalogue();
        // initShortcut();
});