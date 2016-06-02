Ext.namespace('cat');

cat.who = function() {
	
	var configwho= "";
	
	/** the Who panel **/
	var panel;
	
	return {
		createCmp : function(services, what) {
			
			// if configwho is set, the groupFieldStore is loaded from data in configwho
			var mode, groupFieldStore;
			var configwhoInput = Ext.query('input[id*=configwho]');
			if(configwhoInput[0] && configwhoInput[0].value) {
				configwho = configwhoInput[0].value;
				groupFieldStore =  new Ext.data.ArrayStore({
					fields: ['value']
				});
				var data = configwho.split(',');
				for(i=0;i<data.length;i++) {
					data[i] = [data[i]];
				}
				groupFieldStore.loadData(data);
				mode='local';
			}
			else {
				groupFieldStore = new GeoNetwork.data.OpenSearchSuggestionStore({
	                url: services.opensearchSuggest,
	                rootId: 1,
	                baseParams: {
	                    field: 'orgName',
	                    threshold: 1,
                        origin: 'RECORDS_FIELD_VALUES'
	                },
                  sortInfo: {
                    field: 'value',
                    direction: 'ASC'
                  }
	            });
				mode='remote';
			}
			if(what.getGroups) {
				groupFieldStore.baseParams.groupPublished = what.getGroups().join(' or ');
			}
			var updateOrgList = function(cb, value, record) {
				groupFieldStore.baseParams.groupPublished = what.getCatalogueField().getValue() ? 
						what.getCatalogueField().getValue() : what.getGroups().join(' or ');
			};
			what.getCatalogueField().on('additem', updateOrgList);
            what.getCatalogueField().on('removeitem', updateOrgList);
            what.getCatalogueField().on('reset', updateOrgList);

      what.getCatalogueField().getStore().on('load', updateOrgList);

	        var groupField = new Ext.ux.form.SuperBoxSelect({
	            hideLabel: false,
	            width: 230,
	            minChars: 0,
	            queryParam: 'q',
	            hideTrigger: false,
	            id: 'E_orgName',
	            name: 'E_orgName',
	            store: groupFieldStore,
	            valueField: 'value',
	            displayField: 'value',
	            valueDelimiter: ' or ',
	            mode:mode,
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