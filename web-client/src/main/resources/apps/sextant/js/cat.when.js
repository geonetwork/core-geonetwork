Ext.namespace('cat');

cat.when = function() {
	
	return {
		createCmp : function() {
			
			return new Ext.Panel({
			    title: OpenLayers.i18n('when'),
			    autoHeight: true,
			    autoWidth: true,
			    collapsible: true,
			    collapsed: true,
			    defaultType: 'datefield',
			    cls: 'search-form-panel',
			    layout: 'form',
			    defaults: {
			        width: 160
			    },
			    items: GeoNetwork.util.SearchFormTools.getTemporalExtentField(false)
			});
		}
	}
}();