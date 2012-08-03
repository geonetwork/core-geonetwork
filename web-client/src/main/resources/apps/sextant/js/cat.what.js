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

			var groupStore = GeoNetwork.data.GroupStore(services.getGroups),
				tpl = '<tpl for="."><div class="x-combo-list-item">{[values.label.' + lang + ']}</div></tpl>';
			groupStore.load();
			var config = {
					name: 'E__groupPublished',
					mode: 'local',
					triggerAction: 'all',
					fieldLabel: OpenLayers.i18n('Catalogue'),
					store: groupStore,
					valueField: 'name',
					displayField: 'name',
					tpl: tpl
				};
			Ext.apply(config, {
				valueDelimiter: ' or ',
				stackItems: true,
				displayFieldTpl: '{[values.label.' + lang + ']}'});
			//var catalogueField = new Ext.form.ComboBox(config); - Can't display translation
			var catalogueField = new Ext.ux.form.SuperBoxSelect(config);
			
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
