var GeoNetworkApp = function(){
    // get the selected language code parameter from url (if exists)
    var params = Ext.urlDecode(window.location.search.substring(1));
    Ext.form.Field.prototype.msgTarget = 'side';
    
    return {
        init: function(){
            // Language chooser combobox
            var store = new Ext.data.ArrayStore({
                fields: ['code', 'language', 'charset'],
                data: [['en', 'English', ''], ['fr', 'Français', '']]
            });
            var app = this;
            var combo = new Ext.form.ComboBox({
                renderTo: 'languages',
                store: store,
                displayField: 'language',
                typeAhead: true,
                mode: 'local',
                triggerAction: 'all',
                emptyText: 'Select a language...',
                selectOnFocus: true,
                listeners: {
                    select: function(cb, record, idx){
                        //console.log(record.get("code"));
                        this.loadi18n(record.get("code"), this);
                    },
                    scope: app
                }
            });
            
            if (params.lang) {
                this.loadi18n(params.lang);
            } else {
                this.setup();
            }
        },
        loadi18n: function(lang, app){
            var url = String.format("../../ext/src/locale/ext-lang-{0}.js", lang);
            
            Ext.Ajax.request({
                url: url,
                success: function(response, opts){
                    eval(response.responseText);
                    var url = String.format("../lib/GeoNetwork/lang/{0}.js", lang);
                    
                    Ext.Ajax.request({
                        url: url,
                        success: function(response,opts) {this.onSuccess(response,opts,lang)},
                        failure: this.onFailure,
                        scope: app
                    });
                    
                },
                failure: this.onFailure,
                scope: app
            });
        },
        onSuccess: function(response, opts, lang){
            console.log('loaded');
            eval(response.responseText);
            OpenLayers.Lang.setCode(lang)

            console.log('loaded');
            var viewers = Ext.DomQuery.select('.i18n');
            console.log(viewers);
            Ext.get("searchForm").remove();
            this.setup();
        },
        onFailure: function(){
            Ext.Msg.alert('Failure', 'Failed to load locale file.');
            this.setup();
        },
        setup: function(){
            Ext.getDom('TITLE').innerHTML = OpenLayers.i18n('title');
            
            
            catalogue = new GeoNetwork.Catalogue({
                servlet: GeoNetwork.URL
            });
            
            catalogue.metadataStore = GeoNetwork.data.MetadataResultsStore();
            
            var fullTextField = new Ext.form.TextField({
                name: 'E_any',
                fieldLabel: 'Full text'
            });
            
            var searchForm = new Ext.FormPanel({
                url: '',
                id: 'searchForm',
                border: false,
                renderTo: 'search-form',
                width: 400,
                defaultType: 'textfield',
                items: [fullTextField],
                buttons: [{
                    text: 'Search',
                    id: 'searchBt',
                    icon: '../resources/images/default/find.png',
                    // FIXME : iconCls : 'md-mn-find',
                    iconAlign: 'right',
                    listeners: {
                        click: function(){
                            catalogue.search('searchForm', null, null, catalogue.startRecord, true);
                        }
                    }
                }]
            });
            
            var metadataResultsView = new GeoNetwork.MetadataResultsView({
                catalogue: catalogue,
                tpl: GeoNetwork.Templates.FULL
            });
            
            var resultPanel = new Ext.Panel({
                id: 'resultsPanel',
                border: false,
                renderTo: 'metadata-view',
                bodyCssClass: 'md-view',
                autoWidth: true,
                autoHeight: true,
                // height : 200,
                autoScroll: true,
                layout: 'fit',
                items: metadataResultsView,
            });
            
        }
    };
}();
Ext.onReady(GeoNetworkApp.init, GeoNetworkApp);
