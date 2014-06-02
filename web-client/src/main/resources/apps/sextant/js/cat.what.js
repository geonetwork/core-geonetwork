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
			var mode = 'remote',
        configwhatInput = Ext.query('input[id*=configwhat]'),
        configlistThesaurus = Ext.query('input[id=configlistThesaurus]');

			groupFieldStore = new GeoNetwork.data.OpenSearchSuggestionStore({
					url : services.opensearchSuggest,
					rootId : 1,
					baseParams : {
						field : '_groupPublished',
						sortBy: 'ALPHA'
					}
				});
			
			var filtered = false;
			var groupToRemove = [];
			var groupToDisplay = [];
      var listOfThesaurus = configlistThesaurus &&
                            configlistThesaurus[0] &&
                            configlistThesaurus[0].value;

			// optionnaly configwhat define some elements to remove using -GROUPNAME flag for non identified users
			if(configwhatInput && configwhatInput[0] && configwhatInput[0].value) {
			    configwhat = configwhatInput[0].value;
			    var data = configwhat.split(',');
			    for (var i = 0; i < data.length; i++) {
			        if (data[i].substring(0, 1) === '-') {
			            groupToRemove.push(data[i].trim());
			        } else {
			            groupToDisplay.push(data[i].trim());
			        }
			    }
			    // https://forge.ifremer.fr/mantis/view.php?id=15954
			    // Filter group starting with - from the store for non authentified users
			    // Filter group if configwhat is defined
			    userGroupStore = GeoNetwork.data.GroupStore(catalogue.services.getGroups);

			    groupFieldStore.on('load', function (s) {
			        s.filterBy(function (record, id) {
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
                    catalogue.reseting = false;
                    catalogue.fireEvent('afterReset');
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

//	        User groups and group field will be updated on load when trying to log in
//	        if (userGroupStore) {
//    	        userGroupStore.load({callback: function () {
//                    groupFieldStore.load();
//                }});
//	        } else {
//	            groupFieldStore.load();
//	        }



      // Build thesaurus list or tree view based
      // on thesaurus configuration
      // Default configuration
      var defaultListOfThesaurus = [{
        id: 'local.theme.sextant-theme',
        labelFromThesaurus: true,
        field: 'sextantTheme',
        tree: true,
        label: {eng: 'Sextant', fre:'Sextant'}},
        {
          id: 'external.theme.inspire-theme',
          field: 'inspiretheme_en',
          tree: false,
          label: {eng: 'INSPIRE', fre:'INSPIRE'}}];

      if (listOfThesaurus != '') {
        try {
          listOfThesaurus = Ext.decode(listOfThesaurus);
        } catch (e) {
          if (console.log) {
            console.log('Failed to parse list of thesaurus JSON configuration: ' +
              listOfThesaurus);
            console.log(e);
            console.log('Using default configuration with Sextant and INSPIRE.');
          }
          listOfThesaurus = defaultListOfThesaurus;
        }
      } else {
        listOfThesaurus = defaultListOfThesaurus;
      }

      var catCookie = cookie.get('cat.searchform.cat');
      if(catCookie == undefined) {
        catCookie = 'E_' + listOfThesaurus[0].field;
      }
      var radios = [];
      var thesaurusFields = [];
      var radioChange = function(radio, checked) {
        var id = radio.inputValue;
        radio.setVisible(true);
        Ext.each(thesaurusFields, function(field) {
          if (field.getId() == id) {
            field.setVisible(checked);
            field.setDisabled(!checked);
            if (checked) {
              cookie.set('cat.searchform.cat', id);
            }
          }
        });
      };


      Ext.each(listOfThesaurus, function(thesaurus) {
        // Label is the one defined in GUI language or
        // the one in french or the thesaurus id.
        var label = thesaurus.label[catalogue.lang] ||
          thesaurus.label['fre'] ||
          thesaurus.id,
          isSavedInCookie = catCookie == ('E_' + thesaurus.field) ? true : false;

        // Create radio selector first
        var radio = new Ext.form.Radio({
          boxLabel: label,
          name: 'themes',
          inputValue: 'E_' + thesaurus.field,
          checked: isSavedInCookie,
          listeners: {
            check: radioChange
          }
        });
        radios.push(radio);

        var labelStore = null;
        if (thesaurus.labelFromThesaurus) {
          // Retrieve label from thesaurus
          labelStore = new Ext.data.Store({
            url: services.searchKeyword,
            baseParams: {
                pNewSearch: true,
                pTypeSearch: 1,
                pThesauri: thesaurus.id,
                pMode: 'searchBox',
                maxResults: 200
            },
            reader: new Ext.data.XmlReader({
                record: 'keyword',
                id: 'name'
            }, Ext.data.Record.create([{
                name: 'label',
                mapping: 'value'
            }, {
                name: 'name',
                mapping: 'uri'
            }])),
            fields: ["name", "label"],
            listeners: {
            }
        });
          labelStore.load();
        }

        // Create tree or list
        var suggestionParameters = {
          field : thesaurus.field,
          threshold: 1,
          origin: 'RECORDS_FIELD_VALUES'
        };

        //if configwhat then send _groupPublished to the
        // suggestion service to filter
        if (groupToDisplay.length > 0) {
          suggestionParameters.groupPublished = groupToDisplay.join(' or ');
        }

        var suggestionStore = new GeoNetwork.data.OpenSearchSuggestionStore({
          url : services.opensearchSuggest,
          rootId : 1,
          baseParams : suggestionParameters
        });

        var thesaurusField;
        if (thesaurus.tree) {
          thesaurusField = new GeoNetwork.CategoryTree({
            store : suggestionStore,
            lang: cat.language,
            storeLabel: labelStore || suggestionStore,
            rootVisible: false,
            autoWidth: true,
            id : 'E_' + thesaurus.field,
            name : 'E_' + thesaurus.field,
            hidden: !isSavedInCookie,
            disabled: !isSavedInCookie
          });
        } else {
          thesaurusField = new GeoNetwork.CategoryTree({
            store : suggestionStore,
            lang: cat.language,
            storeLabel: labelStore || suggestionStore,
            separator: '',
            rootVisible: false,
            autoWidth: true,
            id : 'E_' + thesaurus.field,
            name : 'E_' + thesaurus.field,
            root: new Ext.tree.TreeNode({
              expanded: true,
              text: label
            }),
            prefixPattern: '',
            hidden: !isSavedInCookie,
            disabled: !isSavedInCookie
          });
        }
        new Ext.tree.TreeSorter(thesaurusField, {
          folderSort: true,
          dir: "asc"
        });
        thesaurusFields.push(thesaurusField);

      });
      if(catCookie == undefined) {
        cookie.set('cat.searchform.cat', radios.items[0].id);
      }
      var radioGroup = new Ext.form.RadioGroup({
          columns: radios.length,
          fieldLabel: OpenLayers.i18n('Themes'),
          items: radios,
          reset: Ext.emptyFn
      });

      var sep1 = createSep();
      var sep2 = createSep();

      // reload each categoryTree store
      // depending on selected catalogs
      var updateCatTree = function(cb) {
        Ext.each(thesaurusFields, function(f) {
          var store = f.store;
          store.baseParams.groupPublished =
            cb.getValue() ? cb.getValue() : groupToDisplay.join(' or ');
          f.loadStore();
        });
      };

      // Attach event to the first
      var isEventAttached = false;
      Ext.each(thesaurusFields, function(f) {
        if (!isEventAttached) {
          f.store.on('load', function() {
            if(catalogueField.getValue()) {
                updateCatTree(catalogueField);
            }
            catalogueField.on('additem', updateCatTree);
            catalogueField.on('removeitem', updateCatTree);
            catalogueField.on('reset', updateCatTree);
          }, this, {single:true});
          isEventAttached = true;
        }
      });

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

      var resourceTypeHiddenField = new Ext.form.Hidden({
        name: 'E_type',
        id: 'E_type',
        value: 'dataset or series or publication or nonGeographicDataset or feature or featureCatalog'
      });
      advancedFields.push(radioGroup, catalogueField);
      Ext.each(radios, function (item) {
        advancedFields.push(item);
      });
      Ext.each(thesaurusFields, function (item) {
        advancedFields.push(item);
      });
      advancedFields.push(resourceTypeHiddenField);
      var items = [searchField, sep1, catalogueField, sep2, radioGroup];
      Ext.each(thesaurusFields, function(f) {
        items.push(f);
      });
      items.push(resourceTypeHiddenField);
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
        items: items
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
		updateUserGroups : function(cb) {
		    if (userGroupStore) {
    		    userGroupStore.reload({callback: function () {
    		        groupFieldStore.reload({callback: function () {
    		            cb && cb();
    		        }});
                }});
		    } else {
		        cb && cb();
		    }
		}
	}
}();
