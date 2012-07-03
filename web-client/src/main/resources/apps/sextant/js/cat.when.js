Ext.namespace('cat');

cat.when = function() {
	
	return {
		createCmp : function() {
			
			return new Ext.Panel({
			    title: OpenLayers.i18n('When'),
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
			    listeners: {
			    	'afterrender': function(o) {
			    		o.header.on('click', function() {
			    			if(o.collapsed) o.expand();
			    			else o.collapse();
			    		});
			    	}
			    },
			    items: GeoNetwork.util.SearchFormTools.getTemporalExtentField(false)
			});
		}
	}
}();