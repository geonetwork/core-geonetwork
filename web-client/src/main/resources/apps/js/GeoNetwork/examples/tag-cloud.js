//OpenLayers.ProxyHost = "/cgi-bin/proxy.cgi?url=";

var searchForm;
var metadataStore;
var catalogue;

Ext.onReady(function(){
    catalogue = new GeoNetwork.Catalogue({
        servlet: GeoNetwork.URL
    });
    catalogue.metadataStore = GeoNetwork.data.MetadataResultsStore();
    catalogue.summaryStore = GeoNetwork.data.MetadataSummaryStore();
    
    new GeoNetwork.MetadataResultsView({
        tpl: GeoNetwork.Templates.FULL,
        renderTo: 'metadata-view-full',
        catalogue: catalogue
    });
    
    new GeoNetwork.TagCloudView({
        renderTo: 'tag-cloud',
        catalogue: catalogue
        // TODO : onclick event should run a search ?
    });
    
    // FIXME :
    //	new GeoNetwork.widget.TagCloudView( {
    //		renderTo : 'tag-cloud-org',
    //		root : 'organizationNames.organizationName',
    //		searchField : 'any',
    //		catalogue : catalogue
    //			});
    
    searchForm = new Ext.FormPanel({
        border: false,
        id: 'searchForm',
        renderTo: 'search-form',
        width: 350,
        defaults: {
            width: 230
        },
        defaultType: 'textfield',
        items: GeoNetwork.util.SearchFormTools.getSimpleFormFields(catalogue.services),
        buttons: [{
            text: 'Search',
            id: 'searchBt',
            icon: '../resources/images/default/find.png',
            // FIXME : iconCls : 'md-mn-find',
            iconAlign: 'right',
            listeners: {
                click: function(){
                    catalogue.search('searchForm');
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
    
    // Focus on full text search field
    Ext.getDom('E_any').focus(true);
});

