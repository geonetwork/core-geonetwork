Ext.namespace('GeoNetwork');

var catalogue;
var app;
var cookie;

GeoNetwork.app = function(){
    // private vars:
    var geonetworkUrl;
    var searching = false;
    var editorWindow;
    var editorPanel;
    
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
    var iMap, searchForm, resultsPanel, metadataResultsView, tBar, bBar,
        mainTagCloudViewPanel, tagCloudViewPanel, infoPanel,
        visualizationModeInitialized = false;
    
    // private function:
    /**
     * Create a radio button switch in order to change perspective from a search
     * mode to a map visualization mode.
     */
    function createModeSwitcher(){
        var ms = {
            xtype: 'radiogroup',
            id: 'ms',
            hidden: !GeoNetwork.MapModule,
            items: [{
                name: 'mode',
                ctCls: 'mn-main',
                boxLabel: OpenLayers.i18n('discovery'),
                id: 'discoveryMode',
                width: 110,
                inputValue: 0,
                checked: true
            }, {
                name: 'mode',
                ctCls: 'mn-main',
                width: 140,
                boxLabel: OpenLayers.i18n('visualization'),
                id: 'visualizationMode',
                inputValue: 1
            }],
            listeners: {
                change: function(rg, checked){
                    app.switchMode(checked.getGroupValue(), false);
                    /* TODO : update viewport */
                }
            }
        };
        
        return new Ext.form.FormPanel({
            renderTo: 'mode-form',
            border: false,
            layout: 'hbox',
            items: ms
        });
    }
    
    
    function initMap(){
        iMap = new GeoNetwork.mapApp();
        iMap.init(GeoNetwork.map.BACKGROUND_LAYERS, GeoNetwork.map.MAIN_MAP_OPTIONS);
        metadataResultsView.addMap(iMap.getMap());
        visualizationModeInitialized = true;
    }
    
    
    /**
     * Create a language switcher mode
     *
     * @return
     */
    function createLanguageSwitcher(lang){
        return new Ext.form.FormPanel({
            renderTo: 'lang-form',
            width: 95,
            border: false,
            layout: 'hbox',
            hidden:  GeoNetwork.Util.locales.length === 1 ? true : false,
            items: [new Ext.form.ComboBox({
                mode: 'local',
                triggerAction: 'all',
                width: 95,
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
            hideLoginLabels: GeoNetwork.hideLoginLabels
        });
        
        catalogue.on('afterBadLogin', loginAlert, this);

        // Store user info in cookie to be displayed if user reload the page
        // Register events to set cookie values
        catalogue.on('afterLogin', function(){
            cookie.set('user', catalogue.identifiedUser);
        });
        catalogue.on('afterLogout', function(){
            cookie.set('user', undefined);
        });
        
        // Refresh login form if needed
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
        
        
        var catalogueField = GeoNetwork.util.SearchFormTools.getCatalogueField(services.getSources, services.logoUrl, true);
        var groupField = GeoNetwork.util.SearchFormTools.getGroupField(services.getGroups, true);
        var metadataTypeField = GeoNetwork.util.SearchFormTools.getMetadataTypeField(true);
        var categoryField = GeoNetwork.util.SearchFormTools.getCategoryField(services.getCategories, '../images/default/category/', true);
        var validField = GeoNetwork.util.SearchFormTools.getValidField(true);
        var spatialTypes = GeoNetwork.util.SearchFormTools.getSpatialRepresentationTypeField(null, true);
        var denominatorField = GeoNetwork.util.SearchFormTools.getScaleDenominatorField(true);
        var statusField = GeoNetwork.util.SearchFormTools.getStatusField(services.getStatus, true);
        
        // Add hidden fields to be use by quick metadata links from the admin panel (eg. my metadata).
        var ownerField = new Ext.form.TextField({
            name: 'E__owner',
            hidden: true
        });
        var isHarvestedField = new Ext.form.TextField({
            name: 'E__isHarvested',
            hidden: true
        });

        var hideInspirePanel = catalogue.getInspireInfo().enable == "false";

        var inspire = new Ext.form.FieldSet({
            title: OpenLayers.i18n('inspireSearchOptions'),
            autoWidth: true,
            hidden: hideInspirePanel,
            collapsible: true,
            collapsed: true,
            items:  GeoNetwork.util.INSPIRESearchFormTools.getINSPIREFields(catalogue.services, true)
        });

        advancedCriteria.push(themekeyField, orgNameField, categoryField, 
                                when, spatialTypes, denominatorField, 
                                catalogueField, groupField, 
                                metadataTypeField, validField, statusField, ownerField, isHarvestedField, inspire);
        var adv = new Ext.form.FieldSet({
            title: OpenLayers.i18n('advancedSearchOptions'),
            autoHeight: true,
            autoWidth: true,
            collapsible: true,
            collapsed: urlParameters.advanced!==undefined ? false : true,
            defaultType: 'checkbox',
            defaults: {
                width: 160
            },
            items: advancedCriteria
        });
        var formItems = [];
        formItems.push(GeoNetwork.util.SearchFormTools.getSimpleFormFields(catalogue.services, 
                    GeoNetwork.map.BACKGROUND_LAYERS, GeoNetwork.map.MAP_OPTIONS, true, 
                    GeoNetwork.searchDefault.activeMapControlExtent, undefined, {width: 290}),
                    adv, GeoNetwork.util.SearchFormTools.getOptions(catalogue.services, undefined));
        // Add advanced mode criteria to simple form - end
        
        
        // Hide or show extra fields after login event
        var adminFields = [groupField, metadataTypeField, validField, statusField];
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
        
        
        return new GeoNetwork.SearchFormPanel({
            id: 'searchForm',
            stateId: 's',
            border: false,
            searchCb: function(){
                if (metadataResultsView && Ext.getCmp('geometryMap')) {
                   metadataResultsView.addMap(Ext.getCmp('geometryMap').map, true);
                }
                var any = Ext.get('E_any');
                if (any) {
                    if (any.getValue() === OpenLayers.i18n('fullTextSearch')) {
                        any.setValue('');
                    }
                }
                
                catalogue.startRecord = 1; // Reset start record
                search();
            },
            padding: 5,
            defaults: {
                width : 180
            },
            items: formItems
        });
    }
    
    function search(){
        searching = true;
        catalogue.search('searchForm', app.loadResults, null, catalogue.startRecord, true);
    }
    
    function initPanels(){
        var infoPanel = Ext.getCmp('infoPanel'), 
        resultsPanel = Ext.getCmp('resultsPanel'),
        tagCloudPanel = Ext.getCmp('tagCloudPanel');
        if (infoPanel.isVisible()) {
            infoPanel.hide();
        }
        if (!resultsPanel.isVisible()) {
            resultsPanel.show();
        }
        if (!tagCloudPanel.isVisible()) {
            tagCloudPanel.show();
        }
        
        // Init map on first search to prevent error
        // when user add WMS layer without initializing
        // Visualization mode
        if (GeoNetwork.MapModule && !visualizationModeInitialized) {
            initMap();
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
    function createResultsPanel(permalinkProvider){
        metadataResultsView = new GeoNetwork.MetadataResultsView({
            catalogue: catalogue,
            displaySerieMembers: true,
            autoScroll: true,
            tpl: GeoNetwork.Templates.FULL,
            featurecolor: GeoNetwork.Settings.results.featurecolor,
            colormap: GeoNetwork.Settings.results.colormap,
            featurecolorCSS: GeoNetwork.Settings.results.featurecolorCSS
        });
        
        catalogue.resultsView = metadataResultsView;
        
        tBar = new GeoNetwork.MetadataResultsToolbar({
            catalogue: catalogue,
            searchFormCmp: Ext.getCmp('searchForm'),
            sortByCmp: Ext.getCmp('E_sortBy'),
            metadataResultsView: metadataResultsView,
            permalinkProvider: permalinkProvider
        });
        
        bBar = createBBar();
        
        resultPanel = new Ext.Panel({
            id: 'resultsPanel',
            border: false,
            hidden: true,
            bodyCssClass: 'md-view',
            autoWidth: true,
            layout: 'fit',
            tbar: tBar,
            items: metadataResultsView,
            // paging bar on the bottom
            bbar: bBar
        });
        return resultPanel;
    }
    function loadCallback(el, success, response, options){
        
        if (success) {
            createMainTagCloud();
            createLatestUpdate();
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
    function createInfoPanel(){
        return new Ext.Panel({
            border: true,
            id: 'infoPanel',
            baseCls: 'md-info',
            autoWidth: true,
            contentEl: 'infoContent',
            autoLoad: {
                url: 'home_' + catalogue.LANG + '.html',
                callback: loadCallback,
                scope: this,
                loadScripts: false
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
            baseCls: 'none',
            id: 'helpPanel',
            autoWidth: true,
            renderTo: 'shortcut',
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
                // Unsuported. Needs some kind of component to store minimized windows
                maximizable: false,
                maximized: true,
                resizable: true,
//                constrain: true,
                width: 980,
                height: 800
            });
            this.editorPanel.setContainer(this.editorWindow);
            this.editorPanel.on('editorClosed', function(){
            	Ext.getCmp('searchForm').fireEvent('search');
            });
        }
        
        if (metadataId) {
            this.editorWindow.show();
            this.editorPanel.init(metadataId, create, group, child);
        }
    }
    
    function createHeader(){
        var info = catalogue.getInfo();
        Ext.getDom('title').innerHTML = '<img class="catLogo" src="../../images/logos/' + info.siteId + '.gif"/>&nbsp;' + info.name;
        document.title = info.name;
    }
    
    // public space:
    return {
        init: function(){
            geonetworkUrl = GeoNetwork.URL || window.location.href.match(/(http.*\/.*)\/apps\/search.*/, '')[1];

            urlParameters = GeoNetwork.Util.getParameters(location.href);
            var lang = urlParameters.hl || GeoNetwork.Util.defaultLocale;
            if (urlParameters.extent) {
                urlParameters.bounds = new OpenLayers.Bounds(urlParameters.extent[0], urlParameters.extent[1], urlParameters.extent[2], urlParameters.extent[3]);
            }
            
            // Init cookie
            cookie = new Ext.state.CookieProvider({
                expires: new Date(new Date().getTime()+(1000*60*60*24*365))
            });
            
            // set a permalink provider which will be the main state provider.
            permalinkProvider = new GeoExt.state.PermalinkProvider({encodeType: false});
            
            Ext.state.Manager.setProvider(permalinkProvider);
            
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
            
            // Extra stuffs
            infoPanel = createInfoPanel();
            createHelpPanel();
            tagCloudViewPanel = createTagCloud();
            
            createHeader();
            
            // Search form
            searchForm = createSearchForm();
            
            // Search result
            resultsPanel = createResultsPanel(permalinkProvider);
            
            // Top navigation widgets
            createModeSwitcher();
            createLanguageSwitcher(lang);
            createLoginForm();
            edit();
            
            
            // Register events on the catalogue
            
            var margins = '35 0 0 0';
            
            var viewport = new Ext.Viewport({
                layout: 'border',
                id: 'vp',
                items: [{
                    region: 'west',
                    id: 'west',
                    split: true,
                    minWidth: 300,
                    width: 300,
                    maxWidth: 400,
                    autoScroll: true,
                    collapsible: true,
                    hideCollapseTool: true,
                    collapseMode: 'mini',
                    margins: margins,
                    //layout : 's',
                    forceLayout: true,
                    layoutConfig: {
                        animate: true
                    },
                    items: [searchForm, tagCloudViewPanel]
                }, {
                    region: 'center',
                    id: 'center',
                    split: true,
                    margins: margins,
                    items: [infoPanel, resultPanel]
                }, {
                    region: 'east',
                    id: 'east',
                    layout: 'fit',
                    split: true,
                    collapsible: true,
                    hideCollapseTool: true,
                    collapseMode: 'mini',
                    collapsed: true,
                    hidden: !GeoNetwork.MapModule,
                    margins: margins,
                    minWidth: 300,
                    width: 500
                }]
            });
            
            /* Trigger visualization mode if mode parameter is 1 
             TODO : Add visualization only mode with legend panel on
             */
            if (urlParameters.mode) {
                app.switchMode(urlParameters.mode, false);
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
            if (GeoNetwork.searchDefault.activeMapControlExtent) {
                Ext.getCmp('geometryMap').setExtent();
            }
            if (urlParameters.bounds) {
                Ext.getCmp('geometryMap').map.zoomToExtent(urlParameters.bounds);
            }
            
            resultPanel.setHeight(Ext.getCmp('center').getHeight());
            
            var events = ['afterDelete', 'afterRating', 'afterLogout', 'afterLogin'];
            Ext.each(events, function (e) {
                catalogue.on(e, function(){
                    if (searching === true) {
                        searchForm.fireEvent('search');
                    }
                });
            });

            // Hack to run search after all app is rendered within a sec ...
            // It could have been better to trigger event in SearchFormPanel#applyState
            // FIXME
            if (urlParameters.s_search !== undefined) {
                setTimeout(function(){searchForm.fireEvent('search');}, 500);
            }
        },
        getIMap: function(){
            // init map if not yet initialized
            if (!iMap) {
                initMap();
            }
            
            // TODO : maybe we should switch to visualization mode also ?
            return iMap;
        },
        getCatalogue: function(){
            return catalogue;
        },
        /**
         * Do layout
         *
         * @param response
         * @return
         */
        loadResults: function(response){
            
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
            
            resultsPanel.syncSize();
            resultsPanel.setHeight(Ext.getCmp('center').getHeight());
            
            Ext.getCmp('west').syncSize();
            Ext.getCmp('center').syncSize();
            Ext.ux.Lightbox.register('a[rel^=lightbox]');
            
        },
        /**
         * Switch from one mode to another
         *
         * @param mode
         * @param force
         * @return
         */
        switchMode: function(mode, force){
            var ms = Ext.getCmp('ms'),
                e = Ext.getCmp('east'),
                c = Ext.getCmp('center'),
                w = Ext.getCmp('west'),
                currentMode;
            
            // Set discovery mode as default if undefined
            if (mode === null) {
                currentMode = ms.getValue().getGroupValue();
                if (currentMode === '0') {
                    ms.onSetValue(Ext.getCmp('visualizationMode'), true);
                } else {
                    ms.onSetValue(Ext.getCmp('discoveryMode'), true);
                }
                mode = currentMode = ms.getValue().getGroupValue();
            }
            
            if (force) {
                if (mode === '1') {
                    ms.onSetValue(Ext.getCmp('visualizationMode'), true);
                } else {
                    ms.onSetValue(Ext.getCmp('discoveryMode'), true);
                }
            }
            
            if (mode === '1' && !visualizationModeInitialized) {
                initMap();
            }
            
            if (mode === '1' && iMap) {
                e.add(iMap.getViewport());
                e.doLayout();
                if (e.collapsed) {
                    e.toggleCollapse();
                }
                if (!w.collapsed) {
                    w.toggleCollapse();
                }
                
                Ext.getCmp('vp').syncSize();
            } else {
                if (!e.collapsed) {
                    e.toggleCollapse();
                }
                if (w.collapsed) {
                    w.toggleCollapse();
                }
            }
        }
    };
};

Ext.onReady(function(){
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
    
    /* Focus on full text search field */
    Ext.getDom('E_any').focus(true);

});
