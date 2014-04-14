Ext.namespace("csiro");

/**
 * Class: csiro.ThesaurusTermSelectionPanel
 */

csiro.keyword = {};

csiro.Thesaurus = Ext.data.Record.create([
  {name: 'title'},
  {name: 'value', mapping: 'key'}
]);
        
var Keyword = Ext.data.Record.create([
  {name: 'value'},
  {name: 'thesaurus', mapping: 'thesaurus/key'},
  {name: 'uri'}
]);

csiro.keyword.keywordStore = new Ext.data.Store({
  proxy: new Ext.data.HttpProxy({
      url: "xml.search.keywords",
      method: 'GET'
  }),
  baseParams: {
      pNewSearch: true,
      pTypeSearch: 1,
      pThesauri: '',
      pMode: 'searchBox'
  },
  reader: new Ext.data.XmlReader({
      record: 'keyword',
      id: 'uri'
  }, Keyword),
  fields: ["value", "thesaurus", "uri"],
  sortInfo: {
      field: "thesaurus"
  }
});

csiro.ThesaurusTermSelectionPanel = Ext.extend(Ext.FormPanel, {
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
     * Property: ThesaurusCount
     */
    ThesaurusCount: null,
    
    /**
     * Property: minSelected
     */
    minSelected: 0,
    
    /**
     * Property: maxSelected
     */
    maxSelected: Number.MAX_VALUE,
    
    /**
     * Property: filterThesaurus - set this if a single thesaurus is preset    
		 * to the name (value) of the thesaurus
     */
    filterThesaurus: null,
    
    /**
     * APIProperty: keywordsSelected
     * {Object} JSON object with selected information on a per thesaurus basis
     */
    keywordsSelected: [],

		
    initComponent: function() {
        this.items = [{
            xtype: 'panel',
            layout: 'fit',
            bodyStyle: 'padding: 5px;',
            border: false,
            tbar: [
								this.getThesaurusCombo(), ' ',
                this.getKeyword(),
                '->',
                translate('maxResults') + ' ' + translate('perThesaurus'),
                this.getLimitInput()
            ],
            items: [this.getKeywordsItemSelector()]
        }];
        
        csiro.keyword.keywordStore.on({
            'loadexception': function() {},
            'beforeload': function(store, options) {
            	if (Ext.getCmp('maxResults')) {
            		store.baseParams.maxResults = Ext.getCmp('maxResults').getValue();
        			}
        			if (!this.loadingMask) {
        				this.loadingMask = new Ext.LoadMask(this.itemSelector.getEl(), {msg: translate('searching')});
        			}
            	this.loadingMask.show();
        		},
            'load': function() {
        			if (!this.loadingMask) {
        				this.loadingMask = new Ext.LoadMask(this.itemSelector.getEl(), {msg: translate('searching')});
        			}
              this.loadingMask.hide();
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
              this.buildTermLists();
            },
            scope: this
        }];

        csiro.ThesaurusTermSelectionPanel.superclass.initComponent.call(this);
    },
    
    getKeyword: function() {

        return new Ext.app.SearchField({
            id: 'keywordSearchField',
            width:240,
            store: csiro.keyword.keywordStore,
            paramName: 'pKeyword'
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

    getThesaurusCombo: function() {
        /**
         * Property: thesaurusStore
         */
        csiro.keyword.thesaurusStore = new Ext.data.Store({
            url: 'xml.thesaurus.getList',
            reader: new Ext.data.XmlReader({
                record: 'thesaurus'
            }, csiro.Thesaurus),
            fields: ['title', 'id'],
						autoLoad: false
        });

        // add the "any thesaurus" record
        var record = new csiro.Thesaurus({filename: translate('anyThesaurus')});
        record.set('value', '');
        
        csiro.keyword.thesaurusStore.add(record);
       	 
        var tCombo = new Ext.form.ComboBox({
            //xtype: 'combo',
            width: 150,
            id: 'search-thesauri',
            value: 0,
            store: csiro.keyword.thesaurusStore,
            triggerAction: 'all',
            mode: 'local',
            displayField: 'title',
            valueField: 'value',
            listWidth: 250,
            listeners: {
                select: function(combo, record, index) {
                    csiro.keyword.keywordStore.removeAll();
                    csiro.keyword.keywordStore.baseParams['pThesauri'] = combo.getValue();

										var value = '';
										if (Ext.getCmp('keywordSearchField')) {
                    	value = Ext.getCmp('keywordSearchField').getValue();
										}
                    if (value == null || value.length < 1) {
                    	csiro.keyword.keywordStore.baseParams['pKeyword'] = '*';
                    } else {
                    	csiro.keyword.keywordStore.baseParams['pKeyword'] = value;
                    }
                    csiro.keyword.keywordStore.reload();
                },
                clear: function(combo) {
                    csiro.keyword.keywordStore.load();
                },
                scope: this
            }
        });


				// set to filterThesaurus and load keywords via select event
				csiro.keyword.thesaurusStore.on({
					'load': function() {
						if (this.filterThesaurus != null) { 
							var index = csiro.keyword.thesaurusStore.find('value', this.filterThesaurus, 0, false, true);
							if (index != -1) {
								tCombo.setValue(this.filterThesaurus);
								tCombo.fireEvent('select', tCombo, csiro.keyword.thesaurusStore.getAt(index), index);
							}
						}
					}, 
					scope: this
				});

				// now everything is ready, load the thesaurusStore
        csiro.keyword.thesaurusStore.load({add: true});

				return tCombo;
    },

    
    getKeywordsItemSelector: function() {

        var tpl = '<tpl for="."><div class="ux-mselect-item';
        if(Ext.isIE || Ext.isIE7) {
            tpl+='" unselectable=on';
        } else {
            tpl+=' x-unselectable"';
        }
        tpl+='>{id} {value} <span class="ux-mselect-item-thesaurus">({thesaurus})</span></div></tpl>';
        
        this.itemSelector = new Ext.ux.Multiselect({
            store: csiro.keyword.keywordStore,
            dataFields:["value", "thesaurus"],
            data:[],
            width:640,
            height:230,
						allowBlank: false,
						minLength: this.minSelected,
						maxLength: this.maxSelected,
						minLengthText:'Minimum {0} term(s) required',
						maxLengthText:'Maximum {0} term(s) allowed',
            displayField:"thesaurus",
            valueField:"value",
            name:"itemselector",
            fieldLabel:"ItemSelector",
            tpl: tpl,
            legend:translate('foundKeywords')
        });
        
        // enable the validate button only if there are selected keywords
        this.itemSelector.on({
            'change': function(component) {
								var numSelected = component.view.getSelectedIndexes().length;
                Ext.getCmp('keywordSearchValidateButton').setDisabled(numSelected < this.minSelected && numSelected > this.maxSelected);
            },
						scope: this
        });

        return this.itemSelector;
    },
    
    /**
     * Method: buildTermLists
     *
     * populate keywordsSelected array with terms/term uris grouped by thesaurus
     */
    buildTermLists: function() {

      this.keywordsSelected = {};
  
			var thesauri = csiro.keyword.thesaurusStore.collect('value');
      Ext.each(thesauri, function(thesaurus, index, thesauri) {
				this.keywordsSelected[thesaurus] = {
					uris: [],
					terms: []
				};
			}, this);
			
      var store = this.itemSelector.store;
			var selectionsArray = this.itemSelector.view.getSelectedIndexes();
			for (var i=0; i<selectionsArray.length; i++) {
				var rec = store.getAt(selectionsArray[i]);
				var the = rec.get('thesaurus');
				this.keywordsSelected[the].uris.push(rec.get('uri').replace("#","%23"));
				this.keywordsSelected[the].terms.push(rec.get('value'));
			}

      this.fireEvent('keywordselected', this, this.keywordsSelected);
      this.ownerCt.hide();
    }
});
