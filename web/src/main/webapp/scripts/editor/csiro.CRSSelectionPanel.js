Ext.namespace("csiro");

/**
 * Class: csiro.CRSSelectionPanel
 */
csiro.CRS = {};

var CRS = Ext.data.Record.create([
  {name: 'authority'},
  {name: 'code'},
  {name: 'version'},
  {name: 'codeSpace'},  
  {name: 'description'}
]);

csiro.CRS.crsStore = new Ext.data.Store({
  proxy: new Ext.data.HttpProxy({
      url: "crs.search",
      method: 'GET'
  }),
  baseParams: {
      name: '',
      type: '',
      maxResults: 50
  },
  reader: new Ext.data.XmlReader({
	  record: 'crs',
      id: 'code'
  }, CRS),
  fields: ["code", "codeSpace", "authority", "description", "version"],
  sortInfo: {
      field: "description"
  }
});

csiro.CRSSelectionPanel = Ext.extend(Ext.FormPanel, {
    border: false,
    
    /**
     * Property: itemSelector
     */
    itemSelector: null,
    
    /**
     * Property: loadingMask
     */
    loadingMask: null,
    
    /**
     * Property: CrsCount
     */
    crsCount: null,
   
	 	/**
		 * Property: minSelected
		 */
		minSelected: 0,

		/**
		 * Property: maxSelected
		 */
		maxSelected: Number.MAX_VALUE,

    /**
     * APIProperty: crsSelected
     */
    crsSelected: [],
    
    initComponent: function() {
        this.items = [{
            xtype: 'panel',
            layout: 'fit',
            bodyStyle: 'padding: 5px;',
            border: false,
            tbar: [
                this.getCRSTypeCombo(), ' ',
                this.getCRS(),
                '->',
                translate('maxResults'),
                this.getLimitInput()
            ],
            items: [this.getCRSItemSelector()]
        }];
        
        csiro.CRS.crsStore.on({
            'loadexception': function() {},
            'beforeload': function(store, options) {
            	if (Ext.getCmp('maxResults')) {
            		store.baseParams.maxResults = Ext.getCmp('maxResults').getValue();
        			}
            	if (!this.loadingMask) {
            		this.loadingMask = new Ext.LoadMask(this.itemSelector.getEl(), 
            				{msg: translate('searching')});
        			}
            	this.loadingMask.show();
        	},
            'load': function() {
							if (!this.loadingMask) {
								this.loadingMask = new Ext.LoadMask(this.itemSelector.getEl(),
										{msg: translate('searching')});
							}
              this.loadingMask.hide();
            },
            scope: this
        });
        
       /**
        * triggered when the user has selected a CRS
        */
        this.addEvents('crsSelected');
        
        this.bbar = ['->', {
            id: 'crsSearchValidateButton',
            iconCls: 'addIcon',
            disabled: true,
            text: translate('add'),
            handler: function() {
              this.buildCRSList();
            },
            scope: this
        }];

        csiro.CRSSelectionPanel.superclass.initComponent.call(this);
    },
    
    getCRS: function() {

        return new Ext.app.SearchField({
            id: 'crsSearchField',
            width:240,
            store: csiro.CRS.crsStore,
            paramName: 'name'
        });
    },
    
    /**
     * Method: getLimitInput
     *
     * 
     */
    getLimitInput: function() {
      return {
        xtype: 'textfield',
        name: 'maxResults',
        id: 'maxResults',
        value: 50,
        width: 40
      };
    },

    getCRSTypeCombo: function() {
        var CRSType = Ext.data.Record.create([
            {name: 'id'}
        ]);
        
        csiro.CRS.crsTypeStore = new Ext.data.Store({
            url: 'crs.types',
            reader: new Ext.data.XmlReader({
                record: 'type'
            }, CRSType),
            fields: ['id']
        });

        var record = new CRSType({filename: translate('any')});
        record.set('id', '');
        
        csiro.CRS.crsTypeStore.add(record);
        csiro.CRS.crsTypeStore.load({add: true});

        return {
            xtype: 'combo',
            width: 150,
            id: 'search-crs',
            value: 0,
            store: csiro.CRS.crsTypeStore,
            triggerAction: 'all',
            mode: 'local',
            displayField: 'id',
            valueField: 'id',
            listWidth: 250,
            listeners: {
                select: function(combo, record, index) {
                    csiro.CRS.crsStore.removeAll();
                    csiro.CRS.crsStore.baseParams['type'] = combo.getValue();
                    var value = Ext.getCmp('crsSearchField').getValue();
                    if (value.length < 1) {
                    	csiro.CRS.crsStore.baseParams['name'] = '';
                    }
                    else {
                    	csiro.CRS.crsStore.baseParams['name'] = value;
                    }
                    csiro.CRS.crsStore.reload();
                },
                clear: function(combo) {
                    csiro.CRS.crsStore.load();
                },
                scope: this
            }
        };
    },
    
    
    getCRSItemSelector: function() {

        var tpl = '<tpl for="."><div class="ux-mselect-item';
        if(Ext.isIE || Ext.isIE7) {
            tpl+='" unselectable=on';
        } else {
            tpl+=' x-unselectable"';
        }
        tpl+='>{description}</div></tpl>';
        
        this.itemSelector = new Ext.ux.Multiselect({
            store: csiro.CRS.crsStore,
            dataFields:["code", "codeSpace", "authority", "description", "version"],
            data:[],
            width:640,
            height:230,
						allowBlank: false,
						minLength: this.minSelected,
						maxLength: this.maxSelected,
						minLengthText:'Minimum {0} CRS(s) required',
						maxLengthText:'Maximum {0} CRS(s) allowed',
            displayField:"description",
            valueField:"code",
            name:"itemselector",
            fieldLabel:"ItemSelector",
            tpl: tpl,
            legend:translate('foundCRS')
        });
        
        // enable the validate button only if there are selected keywords
        this.itemSelector.on({
            'change': function(component) {
								var numSelected = component.view.getSelectedIndexes().length;
                Ext.getCmp('crsSearchValidateButton').setDisabled(numSelected < this.minSelected && numSelected > this.maxSelected);
            },
						scope: this
        });

        return this.itemSelector;
    },

    /**
     * Method: buildCrsList
     *
     * populate crsSelected with codes selected from combobox
     */
    buildCRSList: function() {

      this.crsSelected = {
				descriptions: [],
				codes: []
			};
      
      var store = this.itemSelector.store;
			var selectionsArray = this.itemSelector.view.getSelectedIndexes();
			for (var i=0; i<selectionsArray.length; i++) {
				var rec = store.getAt(selectionsArray[i]);
				this.crsSelected.descriptions.push(rec.get('description'));
				this.crsSelected.codes.push(rec.get('code'));
			}
      this.fireEvent('crsSelected', this, this.crsSelected);
      this.ownerCt.hide();
    }
});
