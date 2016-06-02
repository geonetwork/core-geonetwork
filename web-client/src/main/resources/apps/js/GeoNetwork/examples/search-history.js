var catalogue;
var searchForm;
var metadataStore;

Ext.onReady(function(){
    Ext.History.init();
    
    catalogue = new GeoNetwork.Catalogue({
        servlet: GeoNetwork.URL
    });
    
    catalogue.metadataStore = GeoNetwork.data.MetadataResultsStore();
    
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
            iconCls: 'md-mn md-mn-find',
            iconAlign: 'right',
            listeners: {
                click: function(){
                    catalogue.search('searchForm', null, null);
                    
                    /* TODO : could we create a GeoNetwork SearchHistory
                     component dependant from the search form */
                    var values = GeoNetwork.util.SearchTools.getFormValues(Ext.getCmp('searchForm'));
                    var token = '';
                    for (var key in values) {
                        token += key + '=' + values[key] + ';';
                    };
                    console.log('History add :' + token);
                    Ext.History.add(token);
                }
            }
        }]
    });
    
    Ext.getDom('E_any').focus(true);
    
    /* Handle this change event in order to restore the UI to the
     appropriate history state*/
    Ext.History.on('change', function(token){
        if (token) {
            console.log('change' + token);
            var values = token.split(';');
            for (var i = 0; i < values.length; i++) {
                var key = values[i];
                if (key != '') {
                    var kvp = key.split('=');
                    var field = Ext.getDom(kvp[0]);
                    if (field != undefined) {
                        var value = kvp[1];
                        field.value = value;
                        /* TODO : handle radio, checkbox, ... */
                    }
                }
            }
            
            catalogue.search('searchForm', null, null);
        } else {
            /* This is the initial default state. Necessary if you navigate starting */
            catalogue.search('searchForm', null, null);
        }
    });
    
    /* Launch search when enter key press */
    var map = new Ext.KeyMap("searchForm", [{
        key: [10, 13],
        fn: function(){
            Ext.getCmp('searchBt').fireEvent('click');
        }
    }]);
    
});
