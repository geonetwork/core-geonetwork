Ext.namespace('cat');

cat.who = function() {
	
	return {
		createCmp : function(services) {
			
			var groupField = GeoNetwork.util.SearchFormTools.getGroupField(services.getGroups, true);

			return new Ext.Panel({
			    title: OpenLayers.i18n('who'),
			    autoHeight: true,
			    autoWidth: true,
			    collapsible: true,
			    collapsed: true,
			    defaultType: 'checkbox',
			    layout: 'form',
			    defaults: {
			        width: 160
			    },
			    items: groupField
			});
		}
	}
}();