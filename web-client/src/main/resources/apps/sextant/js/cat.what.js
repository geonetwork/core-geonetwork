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
	
	var groupFieldStore;
	var userGroupStore;
	return {
		createCmp : function(catalogue) {
			var services = catalogue.services;
			//var catalogueField = GeoNetwork.util.SearchFormTools.getCatalogueField(services.getSources, services.logoUrl, false);
			// Catalogue field is for Sextant the groupPublished
			var lang = GeoNetwork.Util.getCatalogueLang(OpenLayers.Lang.getCode());
			
			// if configwhat is set, the groupFieldStore is loaded from data in configwhat
			var mode, groupFieldStore; 
			var configwhatInput = Ext.query('input[id*=configwhat]');
			

			groupFieldStore = new GeoNetwork.data.OpenSearchSuggestionStore(
				{
					url : services.opensearchSuggest,
					rootId : 1,
					baseParams : {
						field : '_groupPublished'
					}
				});
			mode = 'remote';
			
			var filtered = false;
			var groupToRemove = [];
            var groupToDisplay = [];
            
			// optionnaly configwhat define some elements to remove using -GROUPNAME flag for non identified users
			if(configwhatInput && configwhatInput[0] && configwhatInput[0].value) {
			    configwhat = configwhatInput[0].value;
			    var data = configwhat.split(',');
			    for (var i = 0; i < data.length; i++) {
			        if (data[i].substring(0, 1) === '-') {
			            groupToRemove.push(data[i]);
			        } else {
			            groupToDisplay.push(data[i]);
			        }
			    }
			    // https://forge.ifremer.fr/mantis/view.php?id=15954
			    // Filter group starting with - from the store for non authentified users
			    // Filter group if configwhat is defined
			    userGroupStore = GeoNetwork.data.GroupStore(catalogue.services.getGroups);
			    userGroupStore.load();

			    groupFieldStore.on('load', function () {
			        this.filterBy(function (record, id) {
	                    if (groupToRemove.indexOf("-" + record.get('value')) !== -1) {
	                        // Group to remove if identify
	                        if (catalogue.isIdentified()) {
	                            // ... and if user is not a member of that group
	                            // Administrator will see all groups defined in configWhat
	                            // others, will see groups starting with "-" only if they are member of that group
	                            // ... and one metadata is published (due to suggestions)
	                            var userIsMemberOf = userGroupStore.query('name', record.get('value')).length !== 0;
	                            return userIsMemberOf ? true : false;
                            }
                            return false;
			            } else {
			                if (groupToDisplay.length > 0) {
			                    if (groupToDisplay.indexOf(record.get('value')) !== -1) {
                                    return true;
			                    } else {
			                        return false;
			                    }
                            }
			            }
	                    return true;
			        });
			    });
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
	            configwhat: groupToDisplay.length !== 0,
	            configwhatRemoveOnly: groupToRemove.length !== 0 && groupToDisplay.length === 0,
	            valueField: 'value',
	            displayField: 'value',
	            valueDelimiter: ' or ',
	            fieldLabel: OpenLayers.i18n('Catalogue')
	        });
	        groupFieldStore.load();
	        
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
                    field : 'inspiretheme_en',
                    threshold: 1
                }
            });
            var themeINSPIREField = new GeoNetwork.CategoryTree({
                store : themeINSPIREStore,
                lang: cat.language,
                storeLabel: themeINSPIREStore,
                separator: '',
                rootVisible: false,
                autoWidth: true,
                id : 'E_inspiretheme',
                name : 'E_inspiretheme_en',
                root: new Ext.tree.TreeNode({
                    expanded: true,
                    text: 'inspire'
                }),
                prefixPattern: '',
                hidden: catCookie==2?false:true,
                disabled: catCookie==2?false:true
            });
            new Ext.tree.TreeSorter(themeINSPIREField, {
                folderSort: true,
                dir: "asc"
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
		},
		updateUserGroups : function() {
		    userGroupStore.reload();
		}
	}
}();
