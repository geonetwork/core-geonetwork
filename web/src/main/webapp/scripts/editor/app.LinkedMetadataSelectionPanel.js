Ext.namespace("app");

/**
 * Filter to create hidden form fields for CSW queries
 */
app.Filter = {
		SERVICE: [{name: 'E_type', value: 'service'}],
	    DATASET: [{name: '[E_type', value: 'dataset,series'}
	    	// If dataset search should be restricted to ISO19139 or profil add criteria on schema. 
	    	//, {name: 'S__schema', value: 'iso19139'}
	    ],
	    FEATURE_CATALOGUE: [{name: 'E__schema', value: 'iso19110'}]
};

/**
 * Utility functions
 */
app.Utility = {
		convertSubjectAsCommaSeparatedValues: function  (v, record) {
			if (record.subject)
				return app.Utility.convertSeparatedValues (record.subject, ' ,');
			else
				return '';
		},

		/**
		 * Merge values separated by commas
		 */
		convertSeparatedValues: function  (values, s){
			var result = '';
			for (var i = 0; i < values.length; i ++) {
				if (i != 0)
					result += s;
				result += values[i].value;
			}
		    return result;
		},
		
		/**
		 * Check URI null values and return first
		 * URI attribute found.
		 * 
		 * It could happen that more than one URI is returned
		 * in a CSW response but there's no easy way to 
		 * check which is the one for the GetCapabilities.
		 */
		checkUriNullValues: function (v, record) {
			if (record.URI)
				return record.URI[0].value;
			else
				return '';
		}
};

/**
 * Class: app.LinkedMetadataSelectionPanel
 * 
 */
app.linkedMetadata = {};

/**
 * Datastore used to return CSW response content.
 * When defining a mapping, if the element is not available
 * in the response, (eg. title) error will be triggered in the grid completion.
 * 
 * This datastore assume that a title and an identifier will be available in the
 * response. 
 * 
 * Subject and URI are checked and converted.
 * 
 * TODO : Only the first URI is used.
 */
app.linkedMetadata.linkedMetadataStore = new Ext.data.JsonStore({
    fields: [{
        name: 'title', mapping: 'title[0].value', defaultValue: ''
    }, {
        name: 'subject', convert: app.Utility.convertSubjectAsCommaSeparatedValues, defaultValue: ''
    }, {
        name: 'uuid', mapping: 'identifier[0].value', defaultValue: ''
    }, {
        name: 'uri', convert: app.Utility.checkUriNullValues
    }
    ]
// TODO : add exception listener ?
//    ,
//    listeners: {
//		load: function() {
//			console.log(this)
//		},
//		scope: app.linkedMetadata.linkedMetadataStore
//	}
});


app.LinkedMetadataSelectionPanel = Ext.extend(Ext.FormPanel, {
    border: false,
    layout: 'fit',
    /**
     * URL to use to go to the metadata create page.
     * Extra parameter could be use to filter template list.
     * 
     * If null does not display button to create
     * a new element based on this parameter.
     */
    createIfNotExistURL: null,
    
    /**
     * An array of hidden parameter for the form.
     * Default value is set to dataset.
     */
    hiddenParameters: app.Filter.DATASET,
    
    /**
     * Define if multiple selection is allowed or not
     */
    singleSelect: true,
    
    /**
     * Property: loadingMask
     */
    loadingMask: null,
    
    /**
     * Property: ref
     */
    ref: null,
    proxy: null,
    mode: null,
    serviceUrl: null,
    capabilitiesStore: null,
    initComponent: function() {
	    this.addEvents(
            /**
             * triggered when the user has selected an element
             */
            'linkedmetadataselected'
        );

        if (this.mode=='attachService' || this.mode=='coupledResource') {
	        this.capabilitiesStore = new GeoExt.data.WMSCapabilitiesStore({
	            //url: null,
	            proxy : new Ext.data.HttpProxy({
	                method: 'GET',
	                prettyUrls: false,
	                url: this.proxy
	            }),
	            baseParams: {
	                url: this.serviceUrl
	        	},
	            id: 'capabilitiesStore',
	            listeners: {
	        		exception: function(proxy, type, action, options, res, arg) {
	        			Ext.MessageBox.alert(translate("error"));
	        		},
	        		beforeload: function() {
	        			// Update store URL according to selected service.
	        			if (this.mode=='attachService') {
	            			var selected = Ext.getCmp('linkedMetadataGrid').getSelectionModel().getSelections();
	        	            if (selected == undefined || selected[0].data.uri == '')
	        	            	Ext.MessageBox.alert(translate("NoServiceURLError"));
	        	            
	        	            var url;
//	        	            if(Env.proxy != '')
//	        	        		url = Env.proxy + encodeURIComponent(selected[0].data.uri);
	        	            this.capabilitiesStore.baseParams.url = selected[0].data.uri + "?&SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1";
	        			} else if (this.mode=='coupledResource') {
	        				this.capabilitiesStore.baseParams.url = this.serviceUrl;
	        			}
	        		},
	        		loadexception: function() {
	        			Ext.MessageBox.alert(translate("GetCapabilitiesDocumentError") + this.capabilitiesStore.baseParams.url);
	        		},
	        		scope: this
	        	}
	        });
        }
        
        // Add extra parameters according to selection panel
        if (this.mode=='attachService')
        	this.hiddenParameters = app.Filter.SERVICE;
        else if (this.mode=='iso19110')
        	this.hiddenParameters = app.Filter.FEATURE_CATALOGUE;
        
        var checkboxSM = new Ext.grid.CheckboxSelectionModel({
            singleSelect: this.singleSelect,
            header: '',
            listeners: {
        		selectionchange: function() {
        			Ext.getCmp('linkedMetadataValidateButton').setDisabled(this.getSelections().length < 1);
        		}
        	}
        });

        var tbarItems = [this.getSearchInput(),
                         '->',
                         translate('maxResults'),
                         this.getLimitInput()];
        this.addHiddenFormInput(tbarItems);
        
        var grid = new Ext.grid.GridPanel({
            id: 'linkedMetadataGrid',
        	xtype: 'grid',
            layout: 'fit',
            height: 280,
            //autoHeight: true,
            bodyStyle: 'padding: 0px;',
            border: true,
            loadMask: true,
            tbar: tbarItems,
            store: app.linkedMetadata.linkedMetadataStore,
            columns: [
                checkboxSM,
                {id: 'title', header: translate('mdTitle'), dataIndex: 'title'},
                {id: 'subject', header: translate('keywords'), dataIndex: 'subject'},
                {id: 'uri', header: translate('uri'), dataIndex: 'uri'}	// TODO : only for services
            ],
            sm: checkboxSM,
            autoExpandColumn: 'title',
            listeners: {
        		rowclick: function(grid, rowIndex, e) {
        			if (this.capabilitiesStore!=null && this.mode!='coupledResource') {
	        			this.serviceUrl = grid.getStore().getAt(rowIndex).data.uri;
	        			if (this.serviceUrl=='') {
	        				this.capabilitiesStore.removeAll();
	        			} else {
		        			this.capabilitiesStore.baseParams.url = this.serviceUrl;
		        			this.capabilitiesStore.reload();
	        			}
        			}
        		},
        		scope: this
        	}
        });
        
        
        if (this.mode=='attachService' || this.mode=='coupledResource') 
        	this.items = this.getScopedNamePanel(grid);
        else
        	this.items = grid;
        
        this.bbar = ['->', {
	            id: 'linkedMetadataValidateButton',
	            iconCls: 'linkIcon',
	            text: translate('createRelation'),
	            disabled: true,
	            handler: function() {
	                var selected = grid.getSelectionModel().getSelections();
	                this.fireEvent('linkedmetadataselected', this, selected);
	                // we assume that this panel is in a window
	                this.ownerCt.close();
	            },
	            scope: this
	        }, 
	        this.getCreateIfNotExistButton()
        ];
        
        
        
        app.linkedMetadata.linkedMetadataStore.on({
            'load': function () {
        		if (this.loadingMask != null)
        			this.loadingMask.hide();
        	},
        	scope: this
        });
        app.LinkedMetadataSelectionPanel.superclass.initComponent.call(this);
        
    },

    /**
     * Create a button according to createIfNotExistURL.
     */
    getCreateIfNotExistButton: function() {
    	if (this.createIfNotExistURL == null)
    		return '';
    	
    	return {
            id: 'createIfNotExistButton',
            iconCls: 'addIcon',
            text: translate('createIfNotExistButton'),
            handler: function() {
                window.location.replace(this.createIfNotExistURL);
            },
            scope: this
        };
    	
    },
    
    /**
     * APIMethod: setRef
     * Set the element reference
     */
    setRef: function(ref) {
    	this.ref = ref;
    },
    
    /**
     * Add hidden textfields in an item list.
     */
    addHiddenFormInput: function(items) {
    	for (var i = 0; i < this.hiddenParameters.length; i ++) {
    		items.push({
                	xtype: 'textfield',
                	fieldLabel: this.hiddenParameters[i].name,
                	name: this.hiddenParameters[i].name,
                	value: this.hiddenParameters[i].value,
                	hidden: true
                });
    	}
    	return items;
    },
    /**
     * Return a full text search input with search button.
     */
    getSearchInput: function() {
    	return new Ext.app.SearchField({
    		name: 'E.8_AnyText',
            width:240,
            store: app.linkedMetadata.linkedMetadataStore,
            triggerAction: function (scope) {
    			scope.doSearch();
    		},
            scope: this
        });
    },
    /**
     * Method: getLimitInput
     *
     */
    getLimitInput: function() {
      return {
        xtype: 'textfield',
        name: 'nbResultPerPage',
        id: 'nbResultPerPage',
        value: 20,
        width: 40
      };
    },
    
    getScopedNamePanel: function(grid) {
	
	    var combo = {
	    	xtype: 'combo',
	        id: 'getCapabilitiesLayerNameCombo',
	    	fieldLabel: translate('getCapabilitiesLayer'),
	    	store: this.capabilitiesStore,
	        valueField: 'name',
	        displayField: 'title',
	        triggerAction: 'all',
	        //disabled: (serviceUrl==null?true:false),
	        listeners: {
	    		select: function(combo, record, index) {
	    			Ext.getCmp('getCapabilitiesLayerName').setValue(combo.getValue()); 
	    		}
	    	}
	    };
	    var layerName = {
	    	xtype: 'textfield',
	        id: 'getCapabilitiesLayerName',
	    	fieldLabel: translate('layerName'),
	    	valueField: 'name',
	        displayField: 'title'
	    };	
		var panel = {
	        xtype: 'panel',
	        layout: 'form',
            bodyStyle: 'padding: 2px;',
	        border: true,
	        items: [grid, combo, layerName]
		};
		return panel;
    },
    
    /**
     * 
     */
    doSearch: function() {
        if (!this.loadingMask) {
    		this.loadingMask = new Ext.LoadMask(this.getEl(), 
    				{msg: translate('searching')});
		}
    	this.loadingMask.show();
    	
        var url = Env.locService + "/csw";
        app.nbResultPerPage = 20;
        if (Ext.getCmp('nbResultPerPage')) {
        	app.nbResultPerPage = Ext.getCmp('nbResultPerPage').getValue();
		}
        CSWSearchTools.doCSWQueryFromForm(this.id, url, 1, this.showResults, null, Ext.emptyFn);
    },

    showResults: function(response) {
    	var getRecordsFormat = new OpenLayers.Format.CSWGetRecords();
        var r = getRecordsFormat.read(response.responseText);
        var values = r.records;
        if (values != undefined) {
        	app.linkedMetadata.linkedMetadataStore.loadData(values);
        }
    }
});