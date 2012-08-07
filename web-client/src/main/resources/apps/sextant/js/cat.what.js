Ext.namespace('cat');

cat.what = function() {
	
	var advancedFields = [];
	
	var createSep = function() {
		return {
			xtype: 'box',
			autoEl: {
				tag:'hr',
				cls: 'search_form_separator'}
		}
	};

	return {
		createCmp : function(services) {
			
			//var catalogueField = GeoNetwork.util.SearchFormTools.getCatalogueField(services.getSources, services.logoUrl, false);
			// Catalogue field is for Sextant the groupPublished
			// TODO : here we have to restrict the list according to index content
			// using autocompletion
			var lang = GeoNetwork.Util.getCatalogueLang(OpenLayers.Lang.getCode());

			var groupFieldStore = new GeoNetwork.data.OpenSearchSuggestionStore({
	            url: services.opensearchSuggest,
	            rootId: 1,
	            baseParams: {
	                field: '_groupPublished'
	            }
	        });
	        var catalogueField = new Ext.ux.form.SuperBoxSelect({
	            hideLabel: false,
	            minChars: 0,
	            queryParam: 'q',
	            hideTrigger: false,
	            id: 'E__groupPublished',
	            name: 'E__groupPublished',
	            store: groupFieldStore,
	            valueField: 'value',
	            displayField: 'value',
	            valueDelimiter: ' or ',
	//            tpl: tpl,
	            fieldLabel: OpenLayers.i18n('Catalogue')
	        });
			
			var categoryTree = new GeoNetwork.CategoryTree({
				url : services.getCategories,
				rootVisible: false,
				label: OpenLayers.i18n('Themes'),
				width: 250
			});
			
			var sep1 = createSep();
			var sep2 = createSep();
			
			var searchField = new GeoNetwork.form.OpenSearchSuggestionTextField({
				width: 160,
				minChars: 2,
				loadingText: '...',
				fieldLabel: OpenLayers.i18n('fullTextSearch'),
				hideLabel: false,
				hideTrigger: true,
				url: services.opensearchSuggest
			});
			advancedFields.push(categoryTree, catalogueField);
			
			return new Ext.Panel({
				title: OpenLayers.i18n('What'),
				autoHeight: true,
				autoWidth: true,
				collapsible: true,
				collapsed: false,
				layout: 'form',
				defaultType: 'checkbox',
				bodyCssClass: 'hidden',
				defaults: {
					itemCls: 'search_label'
				},
				listeners: {
					'afterrender': function(o) {
						o.header.on('click', function() {
							if(o.collapsed) o.expand();
							else o.collapse();
						});
					}
				},
				items: [searchField, sep1, catalogueField, sep2, categoryTree]
			});
		},
		
		getAdvancedFields : function() {
			return advancedFields;
		}
	}
}();
