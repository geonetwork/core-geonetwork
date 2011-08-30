var catalogue;
var searchForm;
var metadataStore;

Ext.onReady(function() {
    Ext.QuickTips.init();
    
    catalogue = new GeoNetwork.Catalogue({
        servlet: GeoNetwork.URL
    });

	catalogue.metadataStore = GeoNetwork.data.MetadataResultsStore();

	new GeoNetwork.MetadataResultsView( {
	    tpl : GeoNetwork.Templates.SIMPLE,
	    renderTo : 'metadata-view',
	    catalogue : catalogue
	});
	
	
	searchForm = new Ext.FormPanel( {
	    border : false,
	    id : 'searchForm',
	    renderTo : 'search-form',
	    width : 350,
	    defaults : {
		    width : 230
	    },
	    defaultType : 'textfield',
	    items : [
            GeoNetwork.util.SearchFormTools.getSimpleMap(
                    GeoNetwork.map.BACKGROUND_LAYERS,
                    GeoNetwork.map.MAP_OPTIONS,
                    true)
        ],
	    buttons : [ {
	        text : 'Search',
	        id : 'searchBt',
	        iconCls : 'md-mn md-mn-find',
	        iconAlign : 'right',
	        listeners : {
		        click : function() {
			        catalogue.search('searchForm', null, null);
		        }
	        }
	    } ]
	});
});
