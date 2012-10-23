Ext.namespace('cat');

cat.where = function() {

	return {
		createCmp : function() {

			return new Ext.Panel({
				title : OpenLayers.i18n('Where'),
				autoHeight : true,
				autoWidth : true,
				collapsible : true,
				collapsed : false,
				defaultType : 'checkbox',
				layout: 'fit',
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
							height : 280
						}),
				listeners: {
					'afterrender': function(o) {
						o.header.on('click', function() {
							if(o.collapsed) o.expand();
							else o.collapse();
						});
					}
				}
			});
		}
	}
}();