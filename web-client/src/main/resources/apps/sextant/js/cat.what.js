Ext.namespace('cat');

cat.what = function() {
	
	var advancedFields = [];
	
	/** Restricted list of catalogues passed to the portlet **/
	var configwhat= "";
	
	/** List of catalogs form Field **/
	var catalogueField = undefined;
	
	/** What panel **/
	var panel = undefined;
	
	var createSep = function() {
		return {
			xtype: 'box',
			autoEl: {
				tag:'hr',
				cls: 'search_form_separator'}
		}
	};

	return {
		createCmp : function(services) {
			
			//var catalogueField = GeoNetwork.util.SearchFormTools.getCatalogueField(services.getSources, services.logoUrl, false);
			// Catalogue field is for Sextant the groupPublished
			// TODO : here we have to restrict the list according to index content
			// using autocompletion
			var lang = GeoNetwork.Util.getCatalogueLang(OpenLayers.Lang.getCode());
			
			// if configwhat is set, the groupFieldStore is loaded from data in configwhat
			var mode, groupFieldStore; 
			var configwhatInput = Ext.query('input[id*=configwhat]');
			if(configwhatInput && configwhatInput[0] && configwhatInput[0].value) {
				configwhat = configwhatInput[0].value;
				groupFieldStore =  new Ext.data.ArrayStore({
					fields: ['value']
				});
				var data = configwhat.split(',');
				for(i=0;i<data.length;i++) {
					data[i] = [data[i]];
				}
				groupFieldStore.loadData(data);
				mode='local';
			}
			else {
				 groupFieldStore = new GeoNetwork.data.OpenSearchSuggestionStore(
					{
						url : services.opensearchSuggest,
						rootId : 1,
						baseParams : {
							field : '_groupPublished'
						}
					});
				 mode='remote';
			}
	        catalogueField = new Ext.ux.form.SuperBoxSelect({
	            hideLabel: false,
	            width: 230,
	            minChars: 0,
	            queryParam: 'q',
	            hideTrigger: false,
	            id: 'E__groupPublished',
	            name: 'E__groupPublished',
	            mode: mode,
	            store: groupFieldStore,
	            valueField: 'value',
	            displayField: 'value',
	            valueDelimiter: ' or ',
	            fieldLabel: OpenLayers.i18n('Catalogue')
	        });
	        
            // Radio box
            var catCookie = cookie.get('cat.searchform.cat');
            if(catCookie == undefined) {
                catCookie = 1;
            }
            
            var radioChange = function(radio, checked) {
                if(radio.boxLabel == 'Sextant') {
                    categoryTree.setVisible(checked);
                    categoryTree.setDisabled(!checked);
                    if(checked) {
                        cookie.set('cat.searchform.cat', 1);
                    }
                }
                else if(radio.boxLabel == 'INSPIRE') {
                    themeINSPIREField.setVisible(checked);
                    themeINSPIREField.setDisabled(!checked);
                    if(checked){
                        cookie.set('cat.searchform.cat', 2);
                    }
                }
            };
            var radioSextant = new Ext.form.Radio({
                boxLabel: 'Sextant', 
                name: 'themes', 
                inputValue: 1,
                checked: catCookie==1?true:false,
                listeners: {
                    check: radioChange
                }
            });
            
            var radioINSPIRE = new Ext.form.Radio({
                  boxLabel: 'INSPIRE', 
                  name: 'themes',
                  checked: catCookie==2?true:false,
                  inputValue: 2,
                  listeners: {
                      check: radioChange
                  }
            });
            
	        var radios = new Ext.form.RadioGroup({
	            columns: 2,
	            fieldLabel: OpenLayers.i18n('Themes'),
	            items: [radioSextant,radioINSPIRE],
	            reset: Ext.emptyFn
	        });
			
	        // INSPIRE theme
            var themeINSPIREStore = new GeoNetwork.data.OpenSearchSuggestionStore({
                url : services.opensearchSuggest,
                rootId : 1,
                baseParams : {
                    field : 'inspiretheme',
                    threshold: 1
                }
            });
            var themeINSPIREField = new GeoNetwork.CategoryTree({
                store : themeINSPIREStore,
                lang: cat.language,
                storeLabel: themeINSPIREStore,
                rootVisible: false,
                autoWidth: true,
                id : 'E_inspiretheme',
                name : 'E_inspiretheme',
                root: new Ext.tree.TreeNode({
                    expanded: true,
                    text: 'inspire'
                }),
                prefixPattern: '',
                hidden: catCookie==2?false:true,
                disabled: catCookie==2?false:true
            });

	        // Use searchSuggestion to load categories (that way they can be filtered)
	        var baseParams = {
				field : '_cat',
				threshold: 1
			};
	        
	        //if configwhat then send _groupPublished to the suggestion service to filter cat
	        if(configwhat) {
	        	baseParams.groupPublished = configwhat;
	        }
	        var categoryStore = new GeoNetwork.data.OpenSearchSuggestionStore({
				url : services.opensearchSuggest,
				rootId : 1,
				baseParams : baseParams
			});
	
			var categoryTree = new GeoNetwork.CategoryTree({
				store : categoryStore,
				lang: cat.language,
				storeLabel: GeoNetwork.data.CategoryStore(services.getCategories),
				rootVisible: false,
				autoWidth: true,
				hidden: catCookie==1?false:true,
				disabled: catCookie==1?false:true
			});
			
			var sep1 = createSep();
			var sep2 = createSep();
			
			// reload categoryTree depending on selected catalogs
			var updateCatTree = function(cb, value, record) {
				categoryStore.baseParams.groupPublished = cb.getValue() ? cb.getValue() : configwhat;
				categoryTree.loadStore();
				
				themeINSPIREStore.baseParams.groupPublished = cb.getValue() ? cb.getValue() : configwhat;
				themeINSPIREField.loadStore();
			};
			categoryStore.on('load', function() {
			
			    if(catalogueField.getValue()) {
			        updateCatTree(catalogueField);
			    }
				catalogueField.on('additem', updateCatTree);
				catalogueField.on('removeitem', updateCatTree);
				catalogueField.on('reset', updateCatTree);
			}, this, {single:true});
			
			
			var searchField = new GeoNetwork.form.OpenSearchSuggestionTextField({
				width: 230,
				minChars: 2,
				loadingText: '...',
				fieldLabel: OpenLayers.i18n('fullTextSearch'),
				hideLabel: false,
				hideTrigger: true,
				startwith:true,
				url: services.opensearchSuggest
			});
			advancedFields.push(radios,  catalogueField, themeINSPIREField, categoryTree);
			
			panel = new Ext.Panel({
				title: OpenLayers.i18n('What'),
				autoHeight: true,
				autoWidth: true,
				collapsible: true,
				collapsed: false,
				layout: 'form',
				defaultType: 'checkbox',
				bodyCssClass: 'hidden',
				defaults: {
					itemCls: 'search_label'
				},
				listeners: {
					'afterrender': function(o) {
						o.header.on('click', function() {
							if(o.collapsed) o.expand();
							else o.collapse();
						});
					}
				},
				items: [searchField, sep1, catalogueField, sep2, radios, themeINSPIREField, categoryTree]
			});
		},
		
		getAdvancedFields : function() {
			return advancedFields;
		},
		
		getPanel : function() {
			return panel;
		},
		
		getConfigWhat: function() {
			return configwhat;
		},
		
		getCatalogueField : function() {
			return catalogueField;
		}
	}
}();
