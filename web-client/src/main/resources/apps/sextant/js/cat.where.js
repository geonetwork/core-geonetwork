Ext.namespace('cat');

cat.where = function() {
	
	return {
		createCmp : function() {
			
			return new Ext.Panel({
			    title: OpenLayers.i18n('where'),
			    autoHeight: true,
			    autoWidth: true,
			    collapsible: false,
			    collapsed: false,
			    defaultType: 'checkbox',
			    defaults: {
			        width: 300,
			        height: 227
			    },
			    items: GeoNetwork.util.SearchFormTools.getSimpleMap(
					GeoNetwork.map.BACKGROUND_LAYERS,
					GeoNetwork.map.MAP_OPTIONS,
					true,
					{
						cls: 'where-toolbar'
					})
			});
		}
	}
}();