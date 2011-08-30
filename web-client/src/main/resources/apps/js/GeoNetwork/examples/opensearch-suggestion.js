//OpenLayers.ProxyHost = "/cgi-bin/proxy.cgi?url=";

var searchForm;
var metadataStore;
var catalogue;

Ext.onReady(function() {
    catalogue = new GeoNetwork.Catalogue({
        servlet: GeoNetwork.URL
    });

	catalogue.metadataStore = GeoNetwork.data.MetadataResultsStore();

	new GeoNetwork.MetadataResultsView( {
		tpl : GeoNetwork.Templates.FULL,
		renderTo : 'metadata-view-full',
		catalogue : catalogue
	});
console.log(catalogue.services.opensearchSuggest);
	var fields = [];
	var search = new GeoNetwork.form.OpenSearchSuggestionTextField({
                hideLabel: true,
                width: 285, // FIXME
                minChars: 2,
                loadingText: '...',
                hideTrigger: true,
                url: catalogue.services.opensearchSuggest
            });
	
	var title = new GeoNetwork.form.OpenSearchSuggestionTextField({
                hideLabel: false,
                minChars: 1,
                hideTrigger: false,
                url: catalogue.services.opensearchSuggest,
                field: 'orgName', 
                name: 'E_orgName', 
                fieldLabel: OpenLayers.i18n('org')
            });
    
	fields.push(search, title);
	fields.push(GeoNetwork.util.SearchFormTools.getOptions());

	searchForm = new Ext.FormPanel( {
		border : false,
		id : 'searchForm',
		renderTo : 'search-form',
		width : 350,
		defaults : {
			width : 230
		},
		defaultType : 'textfield',
		items : fields,
		buttons : [ {
			text : 'Search',
			id : 'searchBt',
			icon : '../resources/images/default/find.png',
			// FIXME : iconCls : 'md-mn-find',
			iconAlign : 'right',
			listeners : {
				click : function() {
					catalogue.search('searchForm', null, null, null, true);
				}
			}
		} ]
	});

	// Launch search when enter key press
		var map = new Ext.KeyMap("searchForm", [ {
			key : [ 10, 13 ],
			fn : function() {
				Ext.getCmp('searchBt').fireEvent('click');
			}
		} ]);

		// Focus on full text search field
		search.focus(true);
	});
