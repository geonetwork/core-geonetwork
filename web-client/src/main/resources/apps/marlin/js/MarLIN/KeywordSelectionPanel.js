Ext.namespace("MarLIN");

/**
 * Class: MarLIN.KeywordSelectionPanel
 */

MarLIN.keyword = {};

MarLIN.KeywordSelectionPanel = Ext.extend(Ext.FormPanel, {
		/**
		  * Property: services - catalog service URLs 
			*/
		services: null,

		/**
		  * Property: filterThesaurus - a Thesaurus Info object
			*/
		filterThesaurus: null,

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
     * Property: ref
     */
    ref: null,
    
    /**
     * APIProperty: keywordsSelected
     * {Object} Hash table of selected contacts with their XML raw data
     */
    keywordsSelected: [],
    
    initComponent: function() {
			 	MarLIN.keyword.keywordStore = MarLIN.buildKeywordStore(this.services, this.filterThesaurus.get('thesaurusShortName'), this.filterThesaurus.get('displayField'));
        this.items = [{
            xtype: 'panel',
            layout: 'fit',
            bodyStyle: 'padding: 5px;',
            border: false,
            tbar: [
                this.getKeyword(),
                '->',
                translate('maxResults') + ' ' + translate('perThesaurus'),
                this.getLimitInput()
            ],
            items: [this.getKeywordsItemSelector()]
        }];
       
        MarLIN.keyword.keywordStore.on({
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

								// Load contents of a Lucene field and use that to filter the 
								// entries from the Thesaurus so that only those Thesaurus 
								// entries actually being used in the index are shown to the 
								// user.
								var lStore = new GeoNetwork.data.OpenSearchSuggestionStore({
										url: this.services.opensearchSuggest,
										rootId: 1,
										autoLoad: true,
										baseParams: {
											field: this.filterThesaurus.get('thesaurus')
										}
								});

								lStore.on({ 
									'beforeload': function(store, options) {
											this.loadingMask.show();
									},
									'load': function() { 

										if (lStore.getTotalCount() > 0) {
											MarLIN.keyword.keywordStore.filterBy( 
												function(record) {
													return (lStore.findExact('value',record.data[this.filterThesaurus.get('thesaurusField')]) >= 0);
												}, this);
										}
               			this.loadingMask.hide();
									},
									scope: this
								});
            },
            scope: this 
        });
        
       /**
        * triggered when the user has selected a keyword
        */
        this.addEvents('keywordselected');
        
        this.bbar = ['->', {
            id: 'keywordSearchValidateButton',
            iconCls: 'addIcon',
            disabled: true,
            text: translate('add'),
            handler: function() {
              this.buildKeywordXmlList(this.services);
              // The event will be fired on requests response completed for
              // every thesaurus
            },
            scope: this
        }];

        MarLIN.KeywordSelectionPanel.superclass.initComponent.call(this);
    },
    
    /**
     * Method: getKeyword
     *
     * Build Keyword Search box widget.
		 * 
     */
    getKeyword: function() {
        return new Ext.app.SearchField({
            id: 'keywordSearchField',
            width:240,
            store: MarLIN.keyword.keywordStore,
            paramName: 'pKeyword'
        });
    },
    
    /**
     * APIMethod: setRef
     * Set the element reference
     */
    setRef: function(ref) {
    	this.ref = ref;
    },
    
    /**
     * APIMethod: setThesaurus
     *
     * Set the thesaurus that will be shown in the ItemSelector widget.
		 * 
     */
		setThesaurus: function(thesaurus) {
			this.filterThesaurus = thesaurus;

			MarLIN.keyword.keywordStore.baseParams['pThesauri'] = this.filterThesaurus.get('thesaurusShortName');
			MarLIN.keyword.keywordStore.sortInfo['field'] = this.filterThesaurus.get('displayField');
      var value = Ext.getCmp('keywordSearchField').getValue();
      if (value.length < 1) {
      	MarLIN.keyword.keywordStore.baseParams['pKeyword'] = '*';
      } else {
       	MarLIN.keyword.keywordStore.baseParams['pKeyword'] = value;
      }

      MarLIN.keyword.keywordStore.removeAll();
      MarLIN.keyword.keywordStore.reload();

			// now reset the to items list
			var i = this.getForm().findField("itemselector");
			i.reset.call(i);
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
        value: 1500,
        width: 40
      };
    },

    /**
     * Method: getKeywordsItemSelector
     *
     * Build Keywords ItemSelector widget.
		 * 
     */
    getKeywordsItemSelector: function() {

        var tpl = '<tpl for="."><div class="ux-mselect-item';
        if(Ext.isIE || Ext.isIE7) {
            tpl+='" unselectable=on';
        } else {
            tpl+=' x-unselectable"';
        }
        tpl+='>{definition}</span></div></tpl>';
        //tpl+='>{definition} <span class="ux-mselect-item-thesaurus">({value}) ({thesaurus})</span></div></tpl>';
        
        this.itemSelector = new Ext.ux.ItemSelector({
            name:"itemselector",
            fieldLabel:"ItemSelector",
            dataFields:["value", "thesaurus"],
            toData:[],
            msWidth:320,
            msHeight:240,
            valueField:"value",
            fromTpl: tpl,
            toTpl: tpl,
            toLegend: 'Selected Keywords',
            fromLegend:'Keywords',
            fromStore: MarLIN.keyword.keywordStore,
            fromAllowTrash: false,
            fromAllowDup: true,
            toAllowDup: false,
            drawUpIcon: false,
            drawDownIcon: false,
            drawTopIcon: false,
            drawBotIcon: false,
            imagePath: '../js/ext-ux/MultiselectItemSelector-3.0/icons',
            listeners: {
								afterRender: function() {
									MarLIN.keyword.keywordStore.reload();
								}
							},
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
                Ext.getCmp('keywordSearchValidateButton').setDisabled(component.toStore.getCount() < 1);
            }
        });

        return this.itemSelector;
    },
    
    /**
     * Method: buildKeywordXmlList
     *
     * Grab selected keywords, build a URL to validate them against the
		 * thesaurus, if valid then push selected keywords into keywordSelected
		 * 
     */
    buildKeywordXmlList: function(serviceUrl) {

      this.keywordsSelected = [];
      var self = this;
    
      var store = this.itemSelector.toMultiselect.store;
      var uris = store.collect('uri');
        
      // Encode "#" as "%23"?
      Ext.each(uris, function(item, index) {
        uris[index] = item.replace("#","%23");
      });
        
      var multiple = (uris.length > 1) ? true : false;
      var inputValue = serviceUrl.getKeyword +
          '?thesaurus=' + this.filterThesaurus.get('thesaurusShortName') +
          '&id=' + uris.join(',')+
          '&multiple=' + multiple;
        
      self.retrieveKeywordData(inputValue, uris);
    },

    /**
     * Method: retrieveKeywordData 
     *
     * Load keyword data to check that keywords are valid, then push keywords 
		 * into keywordsSelected and then fire keywordselected event so that any 
		 * listener on keywordselected event can do something with them.
     */
    retrieveKeywordData: function(url, uris) {

      Ext.getCmp('keywordSearchValidateButton').disable();

      Ext.Ajax.request({

        url: url,
        method: 'GET',
        scope: this,
        success: function(response) {
          var keyword = response.responseText;
          if (keyword.indexOf('<gmd:MD_Keywords') !== -1) {
              this.keywordsSelected.push({ values: uris, 
									thesaurus: this.filterThesaurus.get('thesaurusShortName')}); 
          }
          Ext.getCmp('keywordSearchValidateButton').enable();
          this.fireEvent('keywordselected', this, this.keywordsSelected);
          this.ownerCt.hide();
        }

      });
    
    }
});
