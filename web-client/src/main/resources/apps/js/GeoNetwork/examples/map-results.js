var searchForm;
var metadataStore;
var catalogue;

Ext.onReady(function(){

    catalogue = new GeoNetwork.Catalogue({
        statusBarId: 'info',
        servlet: GeoNetwork.URL
    });
    catalogue.metadataStore = GeoNetwork.data.MetadataResultsStore();
    
    // Create map panel
    var map = new OpenLayers.Map();
    var layer = new OpenLayers.Layer.WMS("Global Imagery", "http://maps.opengeo.org/geowebcache/service/wms", {
        layers: "bluemarble"
    });
    map.addLayer(layer);
    map.addControl(new OpenLayers.Control.LayerSwitcher());
    
    mapPanel = new GeoExt.MapPanel({
        title: "MapPanel",
        renderTo: "mappanel",
        stateId: "mappanel",
        height: 400,
        width: 600,
        map: map,
        center: new OpenLayers.LonLat(5, 45),
        zoom: 4
    });
    
    var panel = new Ext.Panel({
        id: 'images-view',
        border: false,
        width: 835,
        title: 'Search results',
        // autoWidth:true,
        // autoHeight:true,
        height: 600,
        collapsible: true,
        autoScroll: true,
        layout: 'fit',
        renderTo: 'metadata-view-full',
        items: new GeoNetwork.MetadataResultsView({
            tpl: GeoNetwork.Templates.FULL,
            // renderTo: 'metadata-view-full',
            catalogue: catalogue,
            maps: map
        })
    });
    
    searchForm = new Ext.FormPanel({
        // labelWidth: 75,
        url: '',
        border: false,
        id: 'searchForm',
        renderTo: 'search-form',
        bodyStyle: 'padding:5px 5px 0',
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
                    catalogue.search('searchForm', null, function(response){
                        Ext.Msg.alert('Error: ' + response.status + '-' + response.statusText);
                    }, null, true);
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
