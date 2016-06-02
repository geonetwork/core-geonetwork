//OpenLayers.ProxyHost = "/cgi-bin/proxy.cgi?url=";

var searchForm;
var metadataStore;
var catalogue;



Ext.onReady(function(){
    catalogue = new GeoNetwork.Catalogue({
        servlet: GeoNetwork.URL
    });
    
    catalogue.metadataStore = GeoNetwork.data.MetadataResultsStore();
    
    new GeoNetwork.MetadataResultsView({
        tpl: GeoNetwork.Templates.FULL,
        renderTo: 'metadata-view-full',
        catalogue: catalogue
    });
    
    
    searchForm = new Ext.FormPanel({
        border: false,
        id: 'searchForm',
        renderTo: 'search-form',
        width: 350,
        defaults: {
            width: 230
        },
        defaultType: 'textfield',
        items: GeoNetwork.util.INSPIRESearchFormTools.getINSPIREFields(catalogue.services, true),
        buttons: [{
            text: 'Search',
            id: 'searchBt',
            iconCls: 'md-mn md-mn-find',
            iconAlign: 'right',
            listeners: {
                click: function(){
                    catalogue.search('searchForm', null, null);
                }
            }
        }]
    });
    
    // Launch search when enter key press
    var map = new Ext.KeyMap("searchForm", [{
        key: [10, 13],
        fn: function(){
            Ext.getCmp('searchBt').fireEvent('click');
        }
    }]);
});

