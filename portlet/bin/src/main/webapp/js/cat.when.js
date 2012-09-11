Ext.namespace('cat');

cat.when = function() {
	
	return {
		createCmp : function() {
			
			return new Ext.form.FormPanel({
			    title: OpenLayers.i18n('when'),
			    autoHeight: true,
			    autoWidth: true,
			    collapsible: true,
			    collapsed: true,
			    defaultType: 'datefield',
			    cls: 'search-form-panel',
			    
			    defaults: {
			        width: 160
			    },
			    items: GeoNetwork.util.SearchFormTools.getTemporalExtentField(false)
			});
		}
	}
}();