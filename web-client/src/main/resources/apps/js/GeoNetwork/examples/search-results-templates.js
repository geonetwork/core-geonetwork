var catalogue;
var searchForm;
var metadataStore;

Ext.onReady(function(){
    catalogue = new GeoNetwork.Catalogue({
        servlet: GeoNetwork.URL
    });
    catalogue.metadataStore = GeoNetwork.data.MetadataResultsStore();
    
    var t = GeoNetwork.Templates.THUMBNAIL;
    
    new GeoNetwork.MetadataResultsView({
        tpl: t,
        renderTo: 'metadata-view-thumbnail',
        catalogue: catalogue
    });
    
    new GeoNetwork.MetadataResultsView({
        tpl: GeoNetwork.Templates.FULL,
        renderTo: 'metadata-view-full',
        catalogue: catalogue
    });
    
    new GeoNetwork.MetadataResultsView({
        tpl: GeoNetwork.Templates.SIMPLE,
        renderTo: 'metadata-view',
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
        items: GeoNetwork.util.SearchFormTools.getSimpleFormFields(catalogue.services),
        buttons: [{
            text: 'Search',
            id: 'searchBt',
            icon: '../resources/images/default/find.png',
            // FIXME : iconCls : 'md-mn-find',
            iconAlign: 'right',
            listeners: {
                click: function(){
                    catalogue.search('searchForm', null, null);
                }
            }
        }]
    });
    
    Ext.getDom('E_any').focus(true);
});
