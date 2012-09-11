Ext.namespace('cat');

cat.where = function() {

	return {
		createCmp : function() {

			return new Ext.Panel({
				title : OpenLayers.i18n('Where'),
				autoHeight : true,
				autoWidth : true,
				collapsible : false,
				collapsed : false,
				defaultType : 'checkbox',
				layout: 'fit',
				bodyStyle: 'padding: 15px 40px 15px',
				items : GeoNetwork.util.SearchFormTools.getSimpleMap(
						GeoNetwork.map.BACKGROUND_LAYERS,
						GeoNetwork.map.MAP_OPTIONS, true, {
							cls : 'where-toolbar',
							manageNavBar : function() {
								this.getTopToolbar().add('->',
										this.zoomAllAction, this.clearAction, 
										this.ExtAction, this.zoomOutAction,
										this.zoomInAction, this.panAction);
							},
							mousePosition: false,
							bodyStyle: 'border: 1px solid #D0D0D0',
							width : 300,
							height : 227
						})
			});
		}
	}
}();