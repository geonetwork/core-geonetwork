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
    var map1 = new OpenLayers.Map();
    var map2 = new OpenLayers.Map();
    var map3 = new OpenLayers.Map();
    var layer1 = new OpenLayers.Layer.WMS("Global Imagery", "http://maps.opengeo.org/geowebcache/service/wms", {
        layers: "bluemarble"
    });
    var layer2 = new OpenLayers.Layer.WMS("World Map", "http://labs.metacarta.com/wms-c/Basic.py?", {
        layers: 'basic',
        format: 'image/png'
    });
    var layer3 = new OpenLayers.Layer.WMS("OSM", ["http://t1.hypercube.telascience.org/tiles?", "http://t2.hypercube.telascience.org/tiles?", "http://t3.hypercube.telascience.org/tiles?", "http://t4.hypercube.telascience.org/tiles?"], {
        layers: 'osm-4326',
        format: 'image/png'
    });
    map1.addLayer(layer1);
    map1.addControl(new OpenLayers.Control.LayerSwitcher());
    map2.addLayer(layer2);
    map3.addLayer(layer3);
    
    var mapPanel1 = new GeoExt.MapPanel({
        title: "MapPanel 1",
        renderTo: "mappanel1",
        height: 300,
        width: 300,
        map: map1,
        center: new OpenLayers.LonLat(5, 45),
        zoom: 4
    });
    var mapPanel2 = new GeoExt.MapPanel({
        title: "MapPanel 2",
        renderTo: "mappanel2",
        height: 300,
        width: 300,
        map: map2,
        center: new OpenLayers.LonLat(5, 45),
        zoom: 4
    });
    var mapPanel3 = new GeoExt.MapPanel({
        title: "MapPanel 3",
        renderTo: "mappanel3",
        height: 300,
        width: 300,
        map: map3,
        center: new OpenLayers.LonLat(5, 45),
        zoom: 4
    });
    
    var panel = new Ext.Panel({
        id: 'images-view',
        border: false,
        width: 900,
        title: 'Search results',
        height: 300,
        collapsible: true,
        autoScroll: true,
        layout: 'fit',
        renderTo: 'metadata-view-full',
        items: new GeoNetwork.MetadataResultsView({
            tpl: GeoNetwork.Templates.THUMBNAIL,
            // renderTo: 'metadata-view-full',
            catalogue: catalogue,
            maps: [{
                map: map1
            }, {
                map: map2,
                zoomToExtentOnSearch: true
            }, {
                map: map3
            }]
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
