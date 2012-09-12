var catalogue;
var searchForm;
var metadataStore;

Ext.onReady(function(){
    Ext.QuickTips.init();

    catalogue = new GeoNetwork.Catalogue({
        statusBarId: 'info', 
        servlet: GeoNetwork.URL
    });
    
    var fields = [];
    
    fields.push(GeoNetwork.util.SearchFormTools.getSimpleFormFields(catalogue.services, 
                    GeoNetwork.map.BACKGROUND_LAYERS, GeoNetwork.map.MAP_OPTIONS, true, 
                    false));
    
    
    searchForm = new Ext.FormPanel({
        id: 'searchForm',
        border: false,
        items: fields,
        buttons: [{
            text: 'Search',
            id: 'searchBt',
            iconCls: 'md-mn md-mn-find',
            iconAlign: 'right',
            listeners: {
                click: function(){
                    catalogue.search('searchForm', loadResults, null, catalogue.startRecord);
                }
            }
        }]
    });
    
    catalogue.metadataStore = GeoNetwork.data.MetadataResultsStore();
    
    var metadataResultsView = new GeoNetwork.MetadataResultsView({
        catalogue: catalogue,
        tpl: GeoNetwork.Templates.FULL
    });
    
    var tBar = new GeoNetwork.MetadataResultsToolbar({
        catalogue: catalogue,
        metadataResultsView: metadataResultsView
    });
    
    var previousAction = new Ext.Action({
        text: '<<',
        handler: function(){
            // TODO : disable/enable
            catalogue.startRecord -= parseInt(Ext.getCmp('E_hitsperpage').getValue());
            Ext.getCmp('searchBt').fireEvent('click');
        },
        scope: this
    });
    
    var nextAction = new Ext.Action({
        text: '>>',
        handler: function(){
            catalogue.startRecord += parseInt(Ext.getCmp('E_hitsperpage').getValue());
            Ext.getCmp('searchBt').fireEvent('click');
        },
        scope: this
    });
    
    var bBar = new Ext.Toolbar({
        items: [previousAction, '|', nextAction, '|', {
            xtype: 'tbtext',
            text: '',
            id: 'info'
        }]
    });
    
    var resultPanel = new Ext.Panel({
        id: 'resultsPanel',
        border: false,
        bodyCssClass: 'md-view',
        autoWidth: true,
        autoScroll: true,
        layout: 'fit',
        tbar: tBar,
        items: metadataResultsView,
        // paging bar on the bottom
        bbar: bBar
    });
    
    var viewport = new Ext.Viewport({
        layout: 'border',
        items: [{
            region: 'west',
            split: true,
            width: 340,
            margins: '35 0 5 5',
            layout: 'fit',
            layoutConfig: {
                animate: true
            },
            items: [searchForm]
        }, {
            region: 'center',
            margins: '35 5 5 0',
            layout: 'fit',
            autoScroll: true,
            items: [resultPanel]
        }]
    });
    
    // Launch search when enter key press
    var map = new Ext.KeyMap("searchForm", [{
        key: [10, 13],
        fn: function(){
            Ext.getCmp('searchBt').fireEvent('click');
        }
    }]);
    
    // Focus on full text search field
    Ext.getDom('E_any').focus(true);
    
});

function loadResults(response){
    Ext.getCmp('resultsPanel').syncSize();
};
