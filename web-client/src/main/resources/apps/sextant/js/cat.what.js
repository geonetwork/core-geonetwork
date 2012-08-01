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
			
			var catalogueField = GeoNetwork.util.SearchFormTools.getCatalogueField(services.getSources, services.logoUrl, false);
			
			var categoryTree = new GeoNetwork.CategoryTree({
				url : services.getCategories,
				rootVisible: false,
				label: OpenLayers.i18n('Theme'),
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
