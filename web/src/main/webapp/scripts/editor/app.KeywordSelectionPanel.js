Ext.namespace("app");

/**
 * Class: app.KeywordSelectionPanel
 */

app.keyword = {};

var Keyword = Ext.data.Record.create([
  {name: 'value'},
  {name: 'thesaurus', mapping:"thesaurus/key"},
  {name: 'uri'}
]);

app.keyword.keywordStore = new Ext.data.Store({
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

app.KeywordSelectionPanel = Ext.extend(Ext.FormPanel, {
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
     * Property: ref
     */
    ref: null,
    
    /**
     * APIProperty: keywordsSelected
     * {Object} Hash table of selected contacts with their XML raw data
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
        
        app.keyword.keywordStore.on({
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
        * triggered when the user has selected a keyword
        */
        this.addEvents('keywordselected');
        
        this.bbar = ['->', {
            id: 'keywordSearchValidateButton',
            iconCls: 'addIcon',
            disabled: true,
            text: translate('add'),
            handler: function() {
              this.buildKeywordXmlList();
              // The event will be fired on requests response completed for
              // every thesaurus
            },
            scope: this
        }];

        app.KeywordSelectionPanel.superclass.initComponent.call(this);
    },
    
    getKeyword: function() {

        return new Ext.app.SearchField({
            id: 'keywordSearchField',
            width:240,
            store: app.keyword.keywordStore,
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
        var Thesaurus = Ext.data.Record.create([
            {name: 'title'},
            {name: 'value', mapping: 'key'}
        ]);
        
        /**
         * Property: thesaurusStore
         */
        app.keyword.thesaurusStore = new Ext.data.Store({
            url: 'xml.thesaurus.getList',
            reader: new Ext.data.XmlReader({
                record: 'thesaurus'
            }, Thesaurus),
            fields: ['title', 'id']
        });

        // add the "any thesaurus" record
        var record = new Thesaurus({filename: translate('anyThesaurus')});
        record.set('value', '');
        
        app.keyword.thesaurusStore.add(record);
        
        app.keyword.thesaurusStore.load({add: true});

        return {
            xtype: 'combo',
            width: 150,
            id: 'search-thesauri',
            value: 0,
            store: app.keyword.thesaurusStore,
            triggerAction: 'all',
            mode: 'local',
            displayField: 'title',
            valueField: 'value',
            listWidth: 250,
            listeners: {
                select: function(combo, record, index) {
                    app.keyword.keywordStore.removeAll();
                    app.keyword.keywordStore.baseParams['pThesauri'] = combo.getValue();
                    var value = Ext.getCmp('keywordSearchField').getValue();
                    if (value.length < 1) {
                    	app.keyword.keywordStore.baseParams['pKeyword'] = '*';
                    }
                    else {
                    	app.keyword.keywordStore.baseParams['pKeyword'] = value;
                    }
                    app.keyword.keywordStore.reload();
                },
                clear: function(combo) {
                    app.keyword.keywordStore.load();
                },
                scope: this
            }
        };
    },

    
    getKeywordsItemSelector: function() {

        var tpl = '<tpl for="."><div class="ux-mselect-item';
        if(Ext.isIE || Ext.isIE7) {
            tpl+='" unselectable=on';
        } else {
            tpl+=' x-unselectable"';
        }
        tpl+='>{id} {value} <span class="ux-mselect-item-thesaurus">({thesaurus})</span></div></tpl>';
        
        this.itemSelector = new Ext.ux.ItemSelector({
            name:"itemselector",
            fieldLabel:"ItemSelector",
            dataFields:["value", "thesaurus"],
            toData:[],
            msWidth:320,
            msHeight:230,
            valueField:"value",
            fromTpl: tpl,
            toTpl: tpl,
            toLegend: translate('selectedKeywords'),
            fromLegend:translate('foundKeywords'),
            fromStore: app.keyword.keywordStore,
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
                Ext.getCmp('keywordSearchValidateButton').setDisabled(component.toStore.getCount() < 1);
            }
        });

        return this.itemSelector;
    },
    
    /**
     * Method: buildKeywordXmlList
     *
     * populate keywordsSelected array with xml strings
     */
    buildKeywordXmlList: function() {

      this.keywordsSelected = [];
      var self = this;
      this.ThesaurusCount = 0;
    
      var thesaurusCollection = [];
      var store = this.itemSelector.toMultiselect.store;
      thesaurusCollection = store.collect('thesaurus');
      Ext.each(thesaurusCollection, function(thesaurus, index, thesauri) {
        store.filter('thesaurus', thesaurus);
        var values = store.collect('uri');
        
        // Encode "#" as "%23"?
        Ext.each(values, function(item, index) {
          values[index] = item.replace("#","%23");
        });
        
        var serviceUrl = "xml.keyword.get";
        var multiple = (values.length > 1) ? true : false;
        var inputValue = serviceUrl +
          '?thesaurus=' + thesaurus +
          '&id=' + values.join(',')+
          '&multiple=' + multiple;
        
        ++self.ThesaurusCount;
        self.retrieveKeywordData(inputValue);
      });
      
      store.clearFilter();
    },

    /**
     * Method: retrieveKeywordData
     *
     * Load keyword data, transform it to a json object, & put it in selectedKeywordsJson
     */
    retrieveKeywordData: function(url) {

      Ext.getCmp('keywordSearchValidateButton').disable();

      Ext.Ajax.request({

        url: url,
        method: 'GET',
        scope: this,
        success: function(response) {
          var keyword = response.responseText;
          if (keyword.indexOf('<gmd:MD_Keywords') !== -1) {
              this.keywordsSelected.push("<gmd:descriptiveKeywords xmlns:gmd='http://www.isotc211.org/2005/gmd'>" 
                        + response.responseText + "</gmd:descriptiveKeywords>");
          }
          Ext.getCmp('keywordSearchValidateButton').enable();
          this.ThesaurusCount -= 1;
          if (this.ThesaurusCount == 0) {
            // Wait until the request for *each* thesaurus has ended before
            // firing the event & closing the window
            this.fireEvent('keywordselected', this, this.keywordsSelected);
            this.ownerCt.hide();
          }
        }

      });
    
    }
});
