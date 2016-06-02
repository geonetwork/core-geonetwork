OpenLayers.ProxyHost = "/cgi-bin/proxy.cgi?url=";

var catalogue;
var searchForm;
var metadataStore;


function loadResults(response){
    // console.log('extra-processing');
    // console.log(response);
}

function onFailure(response){
    // console.log(response);

}

Ext.onReady(function(){

    // TODO : test MDWEB http://demo.geomatys.com/mdweb/WS/csw
    catalogue = new GeoNetwork.Catalogue({
        servlet: GeoNetwork.URL
    });
    catalogue.metadataStore = GeoNetwork.data.MetadataCSWResultsStore();
    
    var cswServerField = new Ext.form.ComboBox({
        id: 'cswServer',
        mode: 'local',
        fieldLabel: 'CSW Server',
        store: new Ext.data.ArrayStore({
            id: 0,
            fields: ['url', 'name'],
            data: [['', 'Local GeoNetwork'], ['http://demo.geomatys.com/mdweb/WS/csw', 'MDWEB Demo website']]
        }),
        valueField: 'url',
        displayField: 'name'
    });
    
    var resultsView = new GeoNetwork.MetadataResultsView({
        tpl: GeoNetwork.Templates.SIMPLE,
        renderTo: 'metadata-view',
        catalogue: catalogue
    });
    
    var fullTextField = new Ext.form.TextField({
        name: 'E.8_AnyText',
        fieldLabel: 'Full text search'
        // hideLabel : true
    });
    
    searchForm = new Ext.FormPanel({
        // labelWidth: 75,
        url: '',
        id: 'searchForm',
        renderTo: 'search-form',
        bodyStyle: 'padding:5px 5px 0',
        width: 350,
        defaults: {
            width: 230
        },
        defaultType: 'textfield',
        items: [cswServerField, fullTextField],
        buttons: [{
            text: 'Search',
            id: 'searchBt',
            icon: '../resources/images/default/find.png',
            // FIXME : iconCls : 'md-mn-find',
            iconAlign: 'right',
            listeners: {
                click: function(){
                    var csw = Ext.getCmp('cswServer').getValue();
                    
                    // Quick hack to change CSW server URL
                    if (csw) {
                        catalogue.services.csw = csw;
                    }
                    
                    catalogue.cswSearch('searchForm', loadResults, onFailure);
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
    fullTextField.focus(true);
});
