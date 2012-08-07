Ext.namespace('cat');

cat.who = function() {
	
	return {
		createCmp : function(services) {
			
			//var groupField = GeoNetwork.util.SearchFormTools.getGroupField(services.getGroups, true);
			var groupFieldStore = new GeoNetwork.data.OpenSearchSuggestionStore({
	            url: services.opensearchSuggest,
	            rootId: 1,
	            baseParams: {
	                field: 'credit'
	            }
	        });
	        var groupField = new Ext.ux.form.SuperBoxSelect({
	            hideLabel: false,
	            minChars: 0,
	            queryParam: 'q',
	            hideTrigger: false,
	            id: 'E_credit',
	            name: 'E_credit',
	            store: groupFieldStore,
	            valueField: 'value',
	            displayField: 'value',
	            valueDelimiter: ' or ',
//	            tpl: tpl,
	            fieldLabel: OpenLayers.i18n('orgs')
	        });
				
			return new Ext.Panel({
			    title: OpenLayers.i18n('Who'),
			    autoHeight: true,
			    autoWidth: true,
			    collapsible: true,
			    collapsed: true,
			    defaultType: 'checkbox',
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
			    items: groupField
			});
		}
	}
}();