Ext.namespace('cat');

cat.who = function() {
	
	return {
		createCmp : function(services) {
			
			var groupField = GeoNetwork.util.SearchFormTools.getGroupField(services.getGroups, true);

			return new Ext.form.FormPanel({
			    title: OpenLayers.i18n('who'),
			    autoHeight: true,
			    autoWidth: true,
			    collapsible: true,
			    collapsed: true,
			    defaultType: 'checkbox',
			    defaults: {
			        width: 160
			    },
			    items: groupField
			});
		}
	}
}();