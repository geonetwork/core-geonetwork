Ext.namespace("app");

/**
 * Class: app.CRSSelectionPanel
 */
app.CRS = {};

var CRS = Ext.data.Record.create([
  {name: 'authority'},
  {name: 'code'},
  {name: 'version'},
  {name: 'codeSpace'},  
  {name: 'description'}
]);

app.CRS.crsStore = new Ext.data.Store({
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

app.CRSSelectionPanel = Ext.extend(Ext.FormPanel, {
    border: false,
    first: null,
    
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
     * Property: ref
     */
    ref: null,
    
    /**
     * APIProperty: crsSelected
     */
    crsSelected: "",
    
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
        
        app.CRS.crsStore.on({
            'loadexception': function() {},
            'beforeload': function(store, options) {
            	if (Ext.getCmp('maxResults')) {
            		store.baseParams.maxResults = Ext.getCmp('maxResults').getValue();
        		}
            	if (!this.loadingMask) {
            		this.loadingMask = new Ext.LoadMask(this.itemSelector.fromMultiselect.getEl(), 
            				{msg: translate('searching')});
        		}
            	this.loadingMask.show();
        	},
            'load': function() {
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
              this.buildCRSXmlList();
            },
            scope: this
        }];

        app.CRSSelectionPanel.superclass.initComponent.call(this);
    },
    
    getCRS: function() {

        return new Ext.app.SearchField({
            id: 'crsSearchField',
            width:240,
            store: app.CRS.crsStore,
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
        
        app.CRS.crsTypeStore = new Ext.data.Store({
            url: 'crs.types',
            reader: new Ext.data.XmlReader({
                record: 'type'
            }, CRSType),
            fields: ['id']
        });

        var record = new CRSType({filename: translate('any')});
        record.set('id', '');
        
        app.CRS.crsTypeStore.add(record);
        app.CRS.crsTypeStore.load({add: true});

        return {
            xtype: 'combo',
            width: 150,
            id: 'search-crs',
            value: 0,
            store: app.CRS.crsTypeStore,
            triggerAction: 'all',
            mode: 'local',
            displayField: 'id',
            valueField: 'id',
            listWidth: 250,
            listeners: {
                select: function(combo, record, index) {
                    app.CRS.crsStore.removeAll();
                    app.CRS.crsStore.baseParams['type'] = combo.getValue();
                    var value = Ext.getCmp('crsSearchField').getValue();
                    if (value.length < 1) {
                    	app.CRS.crsStore.baseParams['name'] = '';
                    }
                    else {
                    	app.CRS.crsStore.baseParams['name'] = value;
                    }
                    app.CRS.crsStore.reload();
                },
                clear: function(combo) {
                    app.CRS.crsStore.load();
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
        
        this.itemSelector = new Ext.ux.ItemSelector({
            name:"itemselector",
            fieldLabel:"ItemSelector",
            dataFields:["code", "codeSpace", "authority", "description", "version"],
            toData:[],
            msWidth:320,
            msHeight:230,
            valueField:"code",
            fromTpl: tpl,
            toTpl: tpl,
            toLegend: translate('selectedCRS'),
            fromLegend:translate('foundCRS'),
            fromStore: app.CRS.crsStore,
            fromAllowTrash: false,
            fromAllowDup: true,
            toAllowDup: false,
            drawUpIcon: false,
            drawDownIcon: false,
            drawTopIcon: false,
            drawBotIcon: false,
            imagePath: javascriptsLocation + 'ext-ux/MultiselectItemSelector-3.0/icons',
            toTBar:[{
                text:translate('clear'),
                handler:function(){
                    var i = this.getForm().findField("itemselector");
                    i.reset.call(i);
                },
                scope: this
            }]
        });
        
        // enable the validate button only if there are selected keywords
        this.itemSelector.on({
            'change': function(component) {
                Ext.getCmp('crsSearchValidateButton').setDisabled(component.toStore.getCount() < 1);
            }
        });

        return this.itemSelector;
    },

    /**
     * APIMethod: setRef
     * Set the element reference
     */
    setRef: function(ref) {
    	this.ref = ref;
    },
    
    /**
     * Method: buildCrsXmlList
     *
     * populate crsSelected with xml string
     */
    buildCRSXmlList: function() {

      this.crsSelected = "";
      
      var store = this.itemSelector.toMultiselect.store;
      this.first = true;
      store.each(
    	function(record) {
            var tpl = "<gmd:referenceSystemInfo xmlns:gmd='http://www.isotc211.org/2005/gmd'  xmlns:gco='http://www.isotc211.org/2005/gco'>" +
                      "<gmd:MD_ReferenceSystem>" +
							"<gmd:referenceSystemIdentifier>" +
								"<gmd:RS_Identifier>" +
									"<gmd:code>" +
									// Add description in the code tag. This information will be index and 
									// more useful than only the code. This could be improved later on
										"<gco:CharacterString>" + record.data.description + "</gco:CharacterString>" +
									"</gmd:code>" +
									"<gmd:codeSpace>" +
										"<gco:CharacterString>" + record.data.codeSpace + "</gco:CharacterString>" +
									"</gmd:codeSpace>" +
									"<gmd:version>" +
										"<gco:CharacterString>" + record.data.version + "</gco:CharacterString>" +
									"</gmd:version>" +
								"</gmd:RS_Identifier>" +
							"</gmd:referenceSystemIdentifier>" +
						"</gmd:MD_ReferenceSystem>" +
                    "</gmd:referenceSystemInfo>";
	    	
	    	this.crsSelected += (this.first?"":"&amp;&amp;&amp;") + tpl;
	    	this.first = false;
	    }, 
	    this
      );
      
      if (this.crsSelected != "") {
          // firing the event & closing the window
          this.fireEvent('crsSelected', this.crsSelected);
          this.ownerCt.hide();
      }
    }
});
