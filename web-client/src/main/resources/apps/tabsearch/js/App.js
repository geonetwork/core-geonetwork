Ext.namespace('GeoNetwork');

var catalogue;
var app;

var mapTabAccessCount = 0;

GeoNetwork.app = function(){
    // private vars:
    var geonetworkUrl;
    var searching = false;
    var editorWindow;
    var editorPanel;
    var cookie;

    /**
     * Application parameters are :
     *
     *  * any search form ids (eg. any)
     *  * mode=1 for visualization
     *  * advanced: to open advanced search form by default
     *  * search: to trigger the search
     *  * uuid: to display a metadata record based on its uuid
     *  * extent: to set custom map extent
     */
    var urlParameters = {};

    /**
     * Catalogue manager
     */
    var catalogue;

    /**
     * An interactive map panel for data visualization
     */
    var iMap;

    var searchForm;

    var resultsPanel;

    var metadataResultsView;

    var tBar, bBar;

    var mainTagCloudViewPanel, tagCloudViewPanel, infoPanel;

    var visualizationModeInitialized = false;


    /**
     * Create a mapControl
     *
     * @return
     */

    function initMap(){
        iMap = new GeoNetwork.mapApp();
        iMap.init(GeoNetwork.map.BACKGROUND_LAYERS, GeoNetwork.map.MAIN_MAP_OPTIONS);
        metadataResultsView.addMap(iMap.getMap());
        visualizationModeInitialized = true;
        return iMap;
    }


    /**
     * Create a language switcher mode
     *
     * @return
     */
    function createLanguageSwitcher(lang){
        return new Ext.form.FormPanel({
            renderTo: 'lang-form',
            width: 80,
            border: false,
            layout: 'hbox',
            hidden:  GeoNetwork.Util.locales.length === 1 ? true : false,
            items: [new Ext.form.ComboBox({
                mode: 'local',
                triggerAction: 'all',
                width: 80,
                store: new Ext.data.ArrayStore({
                    idIndex: 2,
                    fields: ['id', 'name', 'id2'],
                    data: GeoNetwork.Util.locales
                }),
                valueField: 'id2',
                displayField: 'name',
                value: lang,
                listeners: {
                    select: function(cb, record, idx){
                        window.location.replace('?hl=' + cb.getValue());
                    }
                }
            })]
        });
    }


    /**
     * Create a default login form and register extra events in case of error.
     *
     * @return
     */
    function createLoginForm(){
        var loginForm = new GeoNetwork.LoginForm({
            renderTo: 'login-form',
            catalogue: catalogue,
            layout: 'hbox',
            bodyStyle:{"background-color":"transparent"},
            hideLoginLabels: GeoNetwork.hideLoginLabels
        });

        catalogue.on('afterBadLogin', loginAlert, this);

        // Store user info in cookie to be displayed if user reload the page
        // Register events to set cookie values
        catalogue.on('afterLogin', function(){
            var cookie = Ext.state.Manager.getProvider();
            cookie.set('user', catalogue.identifiedUser);
        });
        catalogue.on('afterLogout', function(){
            var cookie = Ext.state.Manager.getProvider();
            cookie.set('user', undefined);
        });

        // Refresh login form if needed
        var cookie = Ext.state.Manager.getProvider();
        var user = cookie.get('user');
        if (user) {
            catalogue.identifiedUser = user;
            loginForm.login(catalogue, true);
        }
    }

    /**
     * Error message in case of bad login
     *
     * @param cat
     * @param user
     * @return
     */
    function loginAlert(cat, user){
        Ext.Msg.show({
            title: 'Login',
            msg: 'Login failed. Check your username and password.',
            /* TODO : Get more info about the error */
            icon: Ext.MessageBox.ERROR,
            buttons: Ext.MessageBox.OK
        });
    }


    function getResultsMap() {
        // Create map panel
        var map = new OpenLayers.Map('results_map', GeoNetwork.map.MAP_OPTIONS);       
        map.addLayers(GeoNetwork.map.BACKGROUND_LAYERS);
        map.zoomToMaxExtent();
        
        mapPanel = new GeoExt.MapPanel({
            id: "resultsMap",
            height: 125,
            width: 250,
            map: map,
        });
  
        return mapPanel;

        // TODO: Add in a widget
        //return new GeoNetwork.map.SeachResultsMap();
    }

    /**
     * Create a default search form with advanced mode button
     *
     * @return
     */
    function createSearchForm(){
        // Add advanced mode criteria to simple form - start
        var advancedCriteria = [];
        var services = catalogue.services;
//        var orgNameField = new GeoNetwork.form.OpenSearchSuggestionTextField({
//            hideLabel: false,
//            minChars: 0,
//            hideTrigger: false,
//            url: services.opensearchSuggest,
//            field: 'orgName',
//            name: 'E_orgName',
//            fieldLabel: OpenLayers.i18n('org')
//        });
        // Multi select organisation field
        var orgNameStore = new GeoNetwork.data.OpenSearchSuggestionStore({
            url: services.opensearchSuggest,
            rootId: 1,
            baseParams: {
                field: 'orgName'
            }
        });

        var orgNameField = new Ext.ux.form.SuperBoxSelect({
            hideLabel: false,
            minChars: 0,
            queryParam: 'q',
            hideTrigger: false,
            id: 'E_orgName',
            name: 'E_orgName',
            store: orgNameStore,
            valueField: 'value',
            displayField: 'value',
            valueDelimiter: ' or ',
//            tpl: tpl,
            fieldLabel: OpenLayers.i18n('org')
        });

        // Multi select keyword
        var themekeyStore = new GeoNetwork.data.OpenSearchSuggestionStore({
            url: services.opensearchSuggest,
            rootId: 1,
            baseParams: {
                field: 'keyword'
            }
        });
//        FIXME : could not underline current search criteria in tpl
//        var tpl = '<tpl for="."><div class="x-combo-list-item">' +
//            '{[values.value.replace(Ext.getDom(\'E_themekey\').value, \'<span>\' + Ext.getDom(\'E_themekey\').value + \'</span>\')]}' +
//          '</div></tpl>';
        var themekeyField = new Ext.ux.form.SuperBoxSelect({
            hideLabel: false,
            minChars: 0,
            queryParam: 'q',
            hideTrigger: false,
            id: 'E_themekey',
            name: 'E_themekey',
            store: themekeyStore,
            valueField: 'value',
            displayField: 'value',
            valueDelimiter: ' or ',
//            tpl: tpl,
            fieldLabel: OpenLayers.i18n('keyword')
//            FIXME : Allow new data is not that easy
//            allowAddNewData: true,
//            addNewDataOnBlur: true,
//            listeners: {
//                newitem: function(bs,v, f){
//                    var newObj = {
//                            value: v
//                        };
//                    bs.addItem(newObj, true);
//                }
//            }
        });


        var when = new Ext.form.FieldSet({
            title: OpenLayers.i18n('when'),
            autoWidth: true,
            //layout: 'row',
            defaultType: 'datefield',
            collapsible: true,
            collapsed: true,
            items: GeoNetwork.util.SearchFormTools.getWhen()
        });

        var ownerField = new Ext.form.TextField({
            name: 'E__owner',
            hidden: true
        });
        var isHarvestedField = new Ext.form.TextField({
            name: 'E__isHarvested',
            hidden: true
        });

        var isLockedField = new Ext.form.TextField({
            name: 'E__isLocked',
            hidden: true
        });
        var catalogueField = GeoNetwork.util.SearchFormTools.getCatalogueField(services.getSources, services.logoUrl, true);
        var groupField = GeoNetwork.util.SearchFormTools.getGroupField(services.getGroups, true);
        var metadataTypeField = GeoNetwork.util.SearchFormTools.getMetadataTypeField(true);
        var categoryField = GeoNetwork.util.SearchFormTools.getCategoryField(services.getCategories, '../images/default/category/', true);
        var validField = GeoNetwork.util.SearchFormTools.getValidField(true);
        var spatialTypes = GeoNetwork.util.SearchFormTools.getSpatialRepresentationTypeField(null, true);
        var denominatorField = GeoNetwork.util.SearchFormTools.getScaleDenominatorField(true);

        advancedCriteria.push(themekeyField, orgNameField, categoryField,
            spatialTypes, denominatorField,
            catalogueField, groupField,
            metadataTypeField, validField, ownerField, isHarvestedField, isLockedField);
        var adv = {
            xtype: 'fieldset',
            title: OpenLayers.i18n('advancedSearchOptions'),
            autoHeight: true,
            autoWidth: true,
            collapsible: true,
            collapsed: (urlParameters.advanced?false:true),
            defaultType: 'checkbox',
            defaults: {
                width: 160
            },
            items: advancedCriteria
        };
        var formItems = [];
        formItems.push(GeoNetwork.util.SearchFormTools.getSimpleFormFields(catalogue.services,
            GeoNetwork.map.BACKGROUND_LAYERS, GeoNetwork.map.MAP_OPTIONS, true,
            GeoNetwork.searchDefault.activeMapControlExtent, undefined, {width: 290}),
            adv, GeoNetwork.util.SearchFormTools.getOptions(catalogue.services, undefined));
        // Add advanced mode criteria to simple form - end


        // Hide or show extra fields after login event
        var adminFields = [groupField, metadataTypeField, validField];
        Ext.each(adminFields, function(item){
            item.setVisible(false);
        });

        catalogue.on('afterLogin', function(){
            Ext.each(adminFields, function(item){
                item.setVisible(true);
            });
            GeoNetwork.util.SearchFormTools.refreshGroupFieldValues();
        });
        catalogue.on('afterLogout', function(){
            Ext.each(adminFields, function(item){
                item.setVisible(false);
            });
            GeoNetwork.util.SearchFormTools.refreshGroupFieldValues();
        });
        var hitsPerPage =  [['10'], ['20'], ['50'], ['100']];
        var hitsPerPageField = new Ext.form.ComboBox({
            id: 'E_hitsperpage',
            name: 'E_hitsperpage',
            mode: 'local',
            triggerAction: 'all',
            fieldLabel: OpenLayers.i18n('hitsPerPage'),
            value: hitsPerPage[1], // Set arbitrarily the second value of the
            // array as the default one.
            store: new Ext.data.ArrayStore({
                id: 0,
                fields: ['id'],
                data: hitsPerPage
            }),
            valueField: 'id',
            displayField: 'id'
        });


        var hideInspirePanel = catalogue.getInspireInfo().enable == "false";

        return new Ext.FormPanel({
            id: 'searchForm',
            bodyStyle: 'text-align: center;',
            border: false,
            //autoShow : true,
                listeners: {
                afterrender: function(){
                    //Ext.getCmp("advSearchTabs").getEl().toggle();
                    }
            },
            items:[
                // Simple search form and search buttons
                {
                    bodyStyle:{'padding-top':'50px'},
                    layout: {
                        type: 'hbox',
                        pack: 'center',
                        align: 'center'
                    },
                    border:false,
                    items:[
                        {html:'<h1>'+OpenLayers.i18n('Searchforspatialdataon')+'</h1>',margins:'5 10 5 10',border:false },
                        new GeoNetwork.form.OpenSearchSuggestionTextField({
                           //hideLabel: true,
                           width: 285,
                           height:40,
                           minChars: 2,
                           loadingText: '...',
                           hideTrigger: true,
                           url: catalogue.services.opensearchSuggest

                        }),
                        new Ext.Button({
                text: OpenLayers.i18n('search'),
                           id: 'searchBt', margins:'3 5 3 5',
                icon: '../js/GeoNetwork/resources/images/default/find.png',
                // FIXME : iconCls : 'md-mn-find',
                iconAlign: 'right',
                listeners: {
                    click: function(){

                                   /*if (Ext.getCmp('geometryMap')) {
                            metadataResultsView.addMap(Ext.getCmp('geometryMap').map, true);
                                   }*/
                        var any = Ext.get('E_any');
                        if (any) {
                            if (any.getValue() === OpenLayers.i18n('fullTextSearch')) {
                                any.setValue('');
                            }
                        }

                        catalogue.startRecord = 1; // Reset start record
                        search();
                                   setTab('results');
                               }
                           }
                        }),
                        new Ext.Button( {
                           text: OpenLayers.i18n('reset'),
                           tooltip: OpenLayers.i18n('resetSearchForm'),
                           // iconCls: 'md-mn-reset',
                           id: 'resetBt', margins:'3 5 3 5',
                           icon: '../images/default/cross.png',
                           iconAlign: 'right',
                           listeners: {
                               click: function(){
                                   Ext.getCmp('searchForm').getForm().reset();
                               }
                    }
                       })
                    ]
                },

                // Panel with Advanced search, Help and About Links
                {
                    layout: {
                        type: 'hbox',
                        pack: 'center',
                        align: 'center'
                    },
                    id:'advSearch',
                    autoScroll:true,
                    bodyStyle:{padding:'20px 0px 20px 0px'},
                    border:false,
                    height:60,
                    defaults:{bodyStyle:{},margins:'0 10 0 10',border:false},
                    bodyStyle: 'margin: auto;',
                    items: [
                        {html:'<a href=javascript:void(Ext.get("advSearchTabs").toggle())>'+OpenLayers.i18n('Advanced')+'</a>'},
                        {html:'<a href="javascript:void(app.getHelpWindow().show());">'+OpenLayers.i18n('Help')+'</a>'},
                        {html:'<a href="javascript:void(app.getAboutWindow().show());">'+OpenLayers.i18n('About')+'</a><br/><br/>'}
                    ]
                },
                //  Advanced search form
                {
                    id:'advSearchTabs',
                    layout: {
                        type: 'hbox',
                        pack:'center',
                        align: 'center'
                    },
                    plain:true,
                    autoHeight:true,
                    border:false,
                    deferredRender: false,
                    defaults:{bodyStyle:'padding:10px'},
                    items:[
                        // What panel
                        {
                            title:OpenLayers.i18n('What'),
                            margins:'0 5 0 0',
                            layout:'form',
                            items:[
                                advancedCriteria,GeoNetwork.util.SearchFormTools.getTypesField(GeoNetwork.searchDefault.activeMapControlExtent, true)
                            ]
                        },
                        // Where panel
                        {
                            title:OpenLayers.i18n('Where'),
                            margins:'0 5 0 5',
                            bodyStyle:'padding:0px',
                            layout:'form',
                            items:[
                                GeoNetwork.util.SearchFormTools.getSimpleMap(GeoNetwork.map.BACKGROUND_LAYERS, GeoNetwork.map.MAP_OPTIONS,false)
                                //,new GeoExt.ux.GeoNamesSearchCombo({ map: Ext.getCmp('geometryMap').map, zoom: 12})
                            ]
                        },
                        // When panel
                        {
                            title:OpenLayers.i18n('When'),
                            margins:'0 5 0 5',
                            defaultType: 'datefield',
                            layout:'form',
                            items:GeoNetwork.util.SearchFormTools.getWhen()
                        },
                        // INSPIRE panel
                        {
                            title:'INSPIRE',
                            margins:'0 5 0 5',
                            hidden: hideInspirePanel,
                            defaultType: 'datefield',
                            layout:'form',
                            items: GeoNetwork.util.INSPIRESearchFormTools.getINSPIREFields(catalogue.services, true)
                        }

                    ]
                }
            ]
        });
    }

    function search(){
        searching = true;
        catalogue.search('searchForm', app.loadResults, null, catalogue.startRecord, true);
    }

    function initPanels(){
        //var //infoPanel = Ext.getCmp('infoPanel'),
        var resultsPanel = Ext.getCmp('resultsPanel');
            //tagCloudPanel = Ext.getCmp('tagCloudPanel');
        if (!resultsPanel.isVisible()) {
            resultsPanel.show();
        }


    }
    /**
     * Bottom bar
     *
     * @return
     */
    function createBBar(){

        var previousAction = new Ext.Action({
            id: 'previousBt',
            text: '&lt;&lt;',
            handler: function(){
                var from = catalogue.startRecord - parseInt(Ext.getCmp('E_hitsperpage').getValue(), 10);
                if (from > 0) {
                    catalogue.startRecord = from;
                    search();
                }
            },
            scope: this
        });

        var nextAction = new Ext.Action({
            id: 'nextBt',
            text: '&gt;&gt;',
            handler: function(){
                catalogue.startRecord += parseInt(Ext.getCmp('E_hitsperpage').getValue(), 10);
                search();
            },
            scope: this
        });

        return new Ext.Toolbar({
            items: [previousAction, '|', nextAction, '|', {
                xtype: 'tbtext',
                text: '',
                id: 'info'
            }]
        });

    }

    /**
     * Results panel layout with top, bottom bar and DataView
     *
     * @return
     */
    function createResultsPanel(){
        metadataResultsView = new GeoNetwork.MetadataResultsView({
            catalogue: catalogue,
            displaySerieMembers: true,
            autoScroll: true,
            tpl: GeoNetwork.Templates.FULL
        });

        catalogue.resultsView = metadataResultsView;

        tBar = new GeoNetwork.MetadataResultsToolbar({
            catalogue: catalogue,
            searchBtCmp: Ext.getCmp('searchBt'),
            sortByCmp: Ext.getCmp('E_sortBy'),
            metadataResultsView: metadataResultsView
        });

        bBar = createBBar();

        resultPanel = new Ext.Panel({
            id: 'resultsPanel',
            border: false,
            hidden: true,
            bodyCssClass: 'md-view',
            autoScroll:true,
            autoWidth: true,
            tbar: tBar,
            layout: 'fit',
            items: metadataResultsView,
            // paging bar on the bottom
            bbar: bBar
        });
 
        return resultPanel;
    }
    function loadCallback(el, success, response, options){

        if (success) {
            //createMainTagCloud();
            //createLatestUpdate();
        } else {
            Ext.get('infoPanel').getUpdater().update({url:'home_eng.html'});
            Ext.get('helpPanel').getUpdater().update({url:'help_eng.html'});
        }
    }
    /** private: methode[createInfoPanel]
     *  Main information panel displayed on load
     *
     *  :return:
     */
    function creatAboutPanel(){
        return new Ext.Panel({
            border: true,
            id: 'infoPanel',
            baseCls: 'md-info',
            autoWidth: true,
            //contentEl: 'infoContent',
            autoLoad: {
                url: catalogue.services.rootUrl + '/about?modal=true',
                callback: loadCallback,
                scope: this,
                loadScripts: true
            }
        });
    }
    /** private: methode[createHelpPanel]
     *  Help panel displayed on load
     *
     *  :return:
     */
    function createHelpPanel(){
        return new Ext.Panel({
            border: false,
            frame: false,
            bodyStyle:{'background-color':'white',padding:'5px'},
            autoScroll:true,
            baseCls: 'none',
            id: 'helpPanel',
            autoWidth: true,
            autoLoad: {
                url: 'help_' + catalogue.LANG + '.html',
                callback: initShortcut,
                scope: this,
                loadScripts: false
            }
        });
    }

    /**
     * Main tagcloud displayed in the information panel
     *
     * @return
     */
    function createMainTagCloud(){
        var tagCloudView = new GeoNetwork.TagCloudView({
            catalogue: catalogue,
            query: 'fast=true&summaryOnly=true',
            renderTo: 'tag',
            onSuccess: 'app.loadResults'
        });

        return tagCloudView;
    }
    /**
     * Create latest metadata panel.
     */
    function createLatestUpdate(){
        var latestView = new GeoNetwork.MetadataResultsView({
            catalogue: catalogue,
            autoScroll: true,
            tpl: GeoNetwork.Settings.latestTpl
        });
        var latestStore = GeoNetwork.Settings.mdStore();
        latestView.setStore(latestStore);
        latestStore.on('load', function(){
            Ext.ux.Lightbox.register('a[rel^=lightbox]');
        });
        new Ext.Panel({
            border: false,
            bodyCssClass: 'md-view',
            items: latestView,
            renderTo: 'latest'
        });
        catalogue.kvpSearch(GeoNetwork.Settings.latestQuery, null, null, null, true, latestView.getStore());
    }
    /**
     * Extra tag cloud to displayed current search summary TODO : not really a
     * narrow your search component.
     *
     * @return
     */
    function createTagCloud(){
        var tagCloudView = new GeoNetwork.TagCloudView({
            catalogue: catalogue
        });

        return new Ext.Panel({
            id: 'tagCloudPanel',
            border: true,
            hidden: true,
            baseCls: 'md-view',
            items: tagCloudView
        });
    }

    function edit(metadataId, create, group, child){

        if (!this.editorWindow) {
            this.editorPanel = new GeoNetwork.editor.EditorPanel({
                defaultViewMode: GeoNetwork.Settings.editor.defaultViewMode,
                catalogue: catalogue,
                xlinkOptions: {CONTACT: true}
            });

            this.editorWindow = new Ext.Window({
                tools: [{
                    id: 'newwindow',
                    qtip: OpenLayers.i18n('newWindow'),
                    handler: function(e, toolEl, panel, tc){
                        window.open(GeoNetwork.Util.getBaseUrl(location.href) + "#edit=" + panel.getComponent('editorPanel').metadataId);
                        panel.hide();
                    },
                    scope: this
                }],
                title: OpenLayers.i18n('mdEditor'),
                id : 'editorWindow',
                layout: 'fit',
                modal: false,
                items: this.editorPanel,
                closeAction: 'hide',
                collapsible: true,
                collapsed: false,
                maximizable: true,
                maximized: true,
                resizable: true,
//                constrain: true,
                width: 980,
                height: 800
            });
            this.editorPanel.setContainer(this.editorWindow);
            this.editorPanel.on('editorClosed', function(){
                Ext.getCmp('searchBt').fireEvent('click');
            });
        }

        if (metadataId) {
            this.editorWindow.show();
            this.editorPanel.init(metadataId, create, group, child);
        }
    }

    function createHeader(){
        var info = catalogue.getInfo();
        Ext.getDom('title').innerHTML = '<img class="catLogo" src="images/banner_logo.png" title="'  + info.name + '"/>';
        document.title = info.name;
    }

    // public space:
    return {
        init: function(){
            geonetworkUrl = GeoNetwork.URL || window.location.href.match(/(http.*\/.*)\/apps\/search.*/, '')[1];

            urlParameters = GeoNetwork.Util.getParameters(location.href);
            var lang = urlParameters.hl || GeoNetwork.defaultLocale;
            if (urlParameters.extent) {
                urlParameters.bounds = new OpenLayers.Bounds(urlParameters.extent[0], urlParameters.extent[1], urlParameters.extent[2], urlParameters.extent[3]);
            }


            // Init cookie
            cookie = new Ext.state.CookieProvider({
                expires: new Date(new Date().getTime()+(1000*60*60*24*365))
            });
            Ext.state.Manager.setProvider(cookie);

            // Create connexion to the catalogue
            catalogue = new GeoNetwork.Catalogue({
                statusBarId: 'info',
                lang: lang,
                hostUrl: geonetworkUrl,
                mdOverlayedCmpId: 'resultsPanel',
                adminAppUrl: geonetworkUrl + '/srv/' + lang + '/admin',
                // Declare default store to be used for records and summary
                metadataStore: GeoNetwork.Settings.mdStore ? GeoNetwork.Settings.mdStore() : GeoNetwork.data.MetadataResultsStore(),
                metadataCSWStore : GeoNetwork.data.MetadataCSWResultsStore(),
                summaryStore: GeoNetwork.data.MetadataSummaryStore(),
                editMode: 2, // TODO : create constant
                metadataEditFn: edit
            });

            createHeader();

            // Search form
            searchForm = createSearchForm();



            // Top navigation widgets
            //createModeSwitcher();
            createLanguageSwitcher(lang);
            createLoginForm();
            edit();

            // Results map 
            resultsMap = getResultsMap();

            // Search result
            resultsPanel = createResultsPanel();

            // Extra stuffs
            //infoPanel = {};//createInfoPanel();
            //helpPanel = createHelpPanel();

            tagCloudViewPanel = createTagCloud();


            // Initialize map viewer
            if (GeoNetwork.MapModule) {
                initMap();
            }


            // Register events on the catalogue

            var margins = '0 0 0 0';

            if (!visualizationModeInitialized) initMap();

           var viewport = new Ext.Viewport({
                layout:'border',
                id:'vp',
                items:[   //todo: should add header here?
                    {id:'header',height:80,region:'north',border:false},
                    new Ext.TabPanel({
                        region:'center',
                        id:'GNtabs',
                        deferredRender:false,
                        plain:true,
                        autoScroll: true,
                        defaults:{ autoScroll:true },
                        margins:'0 0 0 0',
                        border: false,
                        activeTab:0,
                        items:[
                            {//basic search panel
                                title:OpenLayers.i18n('Home'),
                                //contentEl:'dvZoeken',
                                
                                layout:'fit',
                                closable:false,
                                autoScroll:true,
                                items:[        
                                    {id:'alignCenter',
                                    border:false,
                                    layout: 'column', 
                                    layoutConfig: { pack: 'center', align: 'center' },
                                items: [{
                                        columnWidth: .05,
                                        border: false,
                                        html: '&nbsp;'
                                },{
                                        columnWidth: .90,
                                        border: false,
                                        items: [
                                                    searchForm,
                                ]
                                    },{
                                        border: false,
                                        columnWidth: .05, 
                                        items: [tagCloudViewPanel]
                                    }]    
                                }]
                            },
                            {//search results panel
                                id:'results',
                                title:OpenLayers.i18n('List'),
                                layout: 'fit',
                                autoScroll:true,    
                                layout:'border',
                                items:[
                                    {//sidebar searchform
                                        region:'west',
                                        id:'west',
                                        border: true,
                                        width:250,                                        
                                        items: [resultsMap]
                                        //html: 'Facetet panel'

                                    },
                                    { //search results
                                        /*region:'center',
                                        id:'center',
                                        split:true,
                                        margins:margins,
                                        border: false,
                                        items:[resultsPanel]*/
                                        layout: 'fit',
                                        region:'center',                                        
                                        border: false,
                                autoScroll:true,
                                        items:[resultPanel]
                                    }
                                ],
                                /* Hide tab panel until a search is done
                                   Seem "hidden:true" as in other places doesn't work for Tabs, and need to use a listener! 
                                   
                                   See http://www.sencha.com/forum/showthread.php?65441-Starting-A-Tab-Panel-with-a-Hidden-Tab
                                */
                                listeners: {
                                    render: function(c){
                                      c.ownerCt.hideTabStripItem(c);
                                    }
                                }


                            },
                            {//map
                                id:'map',
                                title:OpenLayers.i18n('Map'),
                                layout:'fit',                              
                                margins:margins,
                                items: [iMap.getViewport()],
                                listeners: {
                                    afterLayout: function(c){
                                        if (mapTabAccessCount > 2) return;

                                        mapTabAccessCount++;

                                        // First time afterLayout is executed on page load, setting extent is not ok then
                                        // Set extent when first click on Map tab
                                        if (mapTabAccessCount == 2) {
                                            if (iMap) iMap.getMap().zoomToMaxExtent();
                                        }
                                    }
                                }
                            }
                        ]
                    }
                    ),
                    {
                        id:'footer',
                        region:'south',
                        align:'left',
                        bodyStyle:{'text-align':'left', padding:'0px 3px'},
                        border:true,
                        html:"<span class='madeBy'>"+ OpenLayers.i18n('Poweredby') +" <a href='http://geonetwork-opensource.org/'><img style='width:80px' src='../images/default/gn-logo.png' title='GeoNetwork OpenSource' border='0' /></a></span></div>",
                        layout:'fit'
                 }
                ]
            });


            // Hide advanced search options
            Ext.get("advSearchTabs").hide();

            //Ext.getCmp('mapprojectionselector').syncSize();
            //Ext.getCmp('mapprojectionselector').setWidth(130);

            /* Trigger visualization mode if mode parameter is 1
            TODO : Add visualization only mode with legend panel on

            if (urlParameters.mode) {
                app.switchMode(urlParameters.mode, false);
            } */

            /* Init form field URL according to URL parameters */
            GeoNetwork.util.SearchTools.populateFormFromParams(searchForm, urlParameters);

            /* Trigger search if search is in URL parameters */
            if (urlParameters.search !== undefined) {
                Ext.getCmp('searchBt').fireEvent('click');
            }
            if (urlParameters.edit !== undefined && urlParameters.edit !== '') {
                catalogue.metadataEdit(urlParameters.edit);
            }
            if (urlParameters.create !== undefined) {
                resultPanel.getTopToolbar().createMetadataAction.fireEvent('click');
            }
            if (urlParameters.uuid !== undefined) {
                catalogue.metadataShow(urlParameters.uuid, true);
            } else if (urlParameters.id !== undefined) {
                catalogue.metadataShowById(urlParameters.id, true);
            }

            // FIXME : should be in Search field configuration
            Ext.get('E_any').setWidth(285);
            Ext.get('E_any').setHeight(28);

            metadataResultsView.addMap( Ext.getCmp('resultsMap').map);

            if (GeoNetwork.searchDefault.activeMapControlExtent) {
                Ext.getCmp('geometryMap').setExtent();
            }
            if (urlParameters.bounds) {
                Ext.getCmp('geometryMap').map.zoomToExtent(urlParameters.bounds);
            }

            //resultPanel.setHeight(Ext.getCmp('center').getHeight());

            var events = ['afterDelete', 'afterRating', 'afterLogout', 'afterLogin', 'afterStatusChange', 'afterUnlock', 'afterGrabEditSession'];
            Ext.each(events, function (e) {
                catalogue.on(e, function(){
                    if (searching === true) {
                        Ext.getCmp('searchBt').fireEvent('click');
                    }
                });
            });
        },
        getIMap: function(){
            // init map if not yet initialized
            if (!iMap) {
                initMap();
            }

            // TODO : maybe we should switch to visualization mode also ?
            return iMap;
        },
        getHelpWindow: function(){
            return new Ext.Window({
                title: OpenLayers.i18n('Help'),
                layout: 'fit',
                height: 600,
                width: 600,
                closable: true,
                resizable: true,
                draggable: true,
                items: [createHelpPanel()]
            });
        },
        getAboutWindow: function(){
            return new Ext.Window({
                title: OpenLayers.i18n('About'),
                layout: 'fit',
                height: 600,
                width: 600,
                closable: true,
                resizable: true,
                draggable: true,
                items: [creatAboutPanel()]
            });
        },
        getCatalogue: function(){
            return catalogue;
        },
        getMetadataResultsView: function() {
            return metadataResultsView;    
        },
        /**
         * Do layout
         *
         * @param response
         * @return
         */
        loadResults: function(response){
            // Show "List results" panel
            var tabPanel = Ext.getCmp("GNtabs");            
            tabPanel.unhideTabStripItem(tabPanel.items.itemAt(1));

            initPanels();

            // FIXME : result panel need to update layout in case of slider
            // Ext.getCmp('resultsPanel').syncSize();

            Ext.getCmp('previousBt').setDisabled(catalogue.startRecord === 1);
            Ext.getCmp('nextBt').setDisabled(catalogue.startRecord +
                parseInt(Ext.getCmp('E_hitsperpage').getValue(), 10) > catalogue.metadataStore.totalLength);
            if (Ext.getCmp('E_sortBy').getValue()) {
                Ext.getCmp('sortByToolBar').setValue(Ext.getCmp('E_sortBy').getValue()  + "#" + Ext.getCmp('sortOrder').getValue() );

            } else {
                Ext.getCmp('sortByToolBar').setValue(Ext.getCmp('E_sortBy').getValue());

            }


            // Fix for width sortBy combo in toolbar
            // See this: http://www.sencha.com/forum/showthread.php?122454-TabPanel-deferred-render-false-nested-toolbar-layout-problem
            Ext.getCmp('sortByToolBar').syncSize();
            Ext.getCmp('sortByToolBar').setWidth(130);
        
            resultsPanel.syncSize();
            

            //resultsPanel.setHeight(Ext.getCmp('center').getHeight());

            //Ext.getCmp('west').syncSize();
            //Ext.getCmp('center').syncSize();
            //Ext.ux.Lightbox.register('a[rel^=lightbox]');
        },
        /**
         * Switch from one mode to another
         *
         * @param mode
         * @param force
         * @return
         */
        switchMode: function(mode, force){
            setTab('map');
            mode = '1';
            //console.log(visualizationModeInitialized);

            if (!visualizationModeInitialized) {
                initMap();
            }
            //console.log(    iMap);
            if (iMap) {
                var e = Ext.getCmp('map');
                e.add(iMap.getViewport());
                e.doLayout();
                Ext.getCmp('vp').syncSize();
                }


        }
    };
};

Ext.onReady(function () {
    var lang = /hl=([a-z]{3})/.exec(location.href);
    GeoNetwork.Util.setLang(lang && lang[1], '..');

    Ext.QuickTips.init();
    setTimeout(function(){
        Ext.get('loading').remove();
        Ext.get('loading-mask').fadeOut({remove:true});
    }, 250);

    app = new GeoNetwork.app();
    app.init();
    catalogue = app.getCatalogue();

    //overwrite default detail-click action
    catalogue.metadataShow = function (uuid) {
        console.log(uuid);
        tabPanel = Ext.getCmp("GNtabs");
        var tabs = tabPanel.find( 'id', uuid );
        if (tabs[0])
            tabPanel.setActiveTab( tabs[ 0 ] );
        else {
            // Retrieve information in synchrone mode    todo: this doesn't work here
            var store = GeoNetwork.data.MetadataResultsFastStore();
            catalogue.kvpSearch("fast=index", null, null, null, true, store, null, false);
            var record = store.getAt(store.find('uuid', uuid));

            var RowTitle = uuid;

            try{RowTitle=record.data.title;} catch (e) {}
            var RowLabel =  RowTitle;
            if (RowLabel.length > 18) RowLabel =  RowLabel.substr(0,17)+"...";

            var aResTab = new GeoNetwork.view.ViewPanel({
                    serviceUrl: catalogue.services.mdView + '?uuid=' + uuid,
                    lang: catalogue.lang,
                autoScroll:true,
                resultsView: app.getMetadataResultsView(),
                layout:'fit',
                //autoHeight:true,
                padding:'5px 25px',
                    currTab: GeoNetwork.defaultViewMode || 'simple',
                    printDefaultForTabs: GeoNetwork.printDefaultForTabs || false,
                    catalogue: catalogue,
                //maximized: true,
                    metadataUuid: uuid,
                record: record
            });

            // Override zoomToAction (maye better way?). TODO: Check as seem calling old handler code
            aResTab.actionMenu.zoomToAction.setHandler(function(){
                    var uuid = this.record.get('uuid');
                    this.resultsView.zoomTo(uuid);

                    // Custom code to display Map tab
                    tabPanel.setActiveTab( tabPanel.items.itemAt(2) );
                },
                aResTab.actionMenu);
           
            aResTab.actionMenu.viewAction.hide();

            tabPanel.add({
                title: RowLabel,
                tabTip:RowTitle,
                iconCls: 'tabs',
                id:uuid,
                closable:true,
                items: [
                    aResTab
                ]
            }).show();

        }


    }
});

function setTab(id){
    tabPanel = Ext.getCmp("GNtabs");
    var tabs = tabPanel.find( 'id', id );
    if (tabs[0]) tabPanel.setActiveTab( tabs[ 0 ] );
    else console.log(id);
}

    
function addWMSLayer(arr)
{
    app.switchMode('1', true);
    app.getIMap().addWMSLayer(arr);
}
