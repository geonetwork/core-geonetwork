Ext.namespace('cat');

cat.when = function() {

	return {
		createCmp : function() {

			GeoNetwork.util.SearchFormTools.registerDateVtype();
			var datePanel = GeoNetwork.util.SearchFormTools.getDateRangeFields(
					'E_extFrom', OpenLayers.i18n('beginDate'), 'extFrom', 
					'E_extTo', OpenLayers.i18n('endDate'), 'extTo', false
			);

			return new Ext.Panel({
				title : OpenLayers.i18n('When'),
				autoHeight : true,
				autoWidth : true,
				collapsible : true,
				collapsed : true,
				defaultType : 'datefield',
				cls : 'search-form-panel',
				layout : 'form',
				defaults : {
					width : 160
				},
				listeners : {
					'afterrender' : function(o) {
						o.header.on('click', function() {
							if (o.collapsed)
								o.expand();
							else
								o.collapse();
						});
					}
				},
				items : datePanel
			});
		}
	}
}();