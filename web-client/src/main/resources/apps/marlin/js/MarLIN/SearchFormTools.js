/*
 * Copyright (C) 2001-2011 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * 
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
Ext.namespace("MarLIN");

/** api: (define) 
 *  module = MarLIN 
 *  class = SearchFormTools
 */

MarLIN.SearchKeywordSelectionPanel = null;
MarLIN.SearchKeywordSelectionWindow = null;
MarLIN.ThesaurusPanel = null;

MarLIN.Thesauri = [
	{
		thesaurus: 'geonetwork.thesaurus.register.project.urn:marine.csiro.au:projectregister',
		thesaurusShortName: 'register.project.urn:marine.csiro.au:projectregister',
		luceneFieldName:	'E_siblings_project',
		label:						'Project',
		valueField:				'value',
		displayField:			'definition',
		thesaurusField:		'value',
		multi:						true
	},
	{ 
		thesaurus: 'geonetwork.thesaurus.register.project.urn:marine.csiro.au:globalprojectregister',
		thesaurusShortName: 'register.project.urn:marine.csiro.au:globalprojectregister',
		luceneFieldName:	'E_siblings_project',
		label:						'Global Project',
		valueField:				'value',
		displayField:			'definition',
		thesaurusField:		'value',
		multi:						true
	},
	{ 
		thesaurus: 'geonetwork.thesaurus.register.survey.urn:marine.csiro.au:surveyregister',
		thesaurusShortName: 'register.survey.urn:marine.csiro.au:surveyregister',
		luceneFieldName:	'E_siblings_survey',
		label:						'Survey',
		valueField:				'value',
		displayField:			'definition',
		thesaurusField:		'value',
		multi:						true
	},
	/* Leave caab out for now as it needs different handling due to size
	{ 
		thesaurus: 'geonetwork.thesaurus.external.taxon.urn:marine.csiro.au:caabregister',
		thesaurusShortName: 'external.taxon.urn:marine.csiro.au:caabregister',
		luceneFieldName:	'E_keywordId',
		label:						'CAAB Code',
		valueField:				'uri',
		displayField:			'value',
		thesaurusField:		'uri',
		multi:						true
	},
	*/
	{ 
		thesaurus: 'geonetwork.thesaurus.register.theme.urn:marine.csiro.au:keywords:taxonomicGroup',
		thesaurusShortName: 'register.theme.urn:marine.csiro.au:keywords:taxonomicGroup',
		luceneFieldName:	'E_keywordId',
		label:						'Taxon Group',
		valueField:				'uri',
		displayField:			'definition',
		thesaurusField:		'uri',
		multi:						true
	},
	{ 
		thesaurus: 'geonetwork.thesaurus.register.theme.urn:marine.csiro.au:keywords:gcmd',
		thesaurusShortName: 'register.theme.urn:marine.csiro.au:keywords:gcmd',
		luceneFieldName:	'E_keywordId',
		label:						'Science Keyword',
		valueField:				'uri',
		displayField:			'definition',
		thesaurusField:		'uri',
		multi:						true
	},
	{ 
		thesaurus: 'geonetwork.thesaurus.register.theme.urn:marine.csiro.au:keywords:standardDataType',
		thesaurusShortName: 'register.theme.urn:marine.csiro.au:keywords:standardDataType',
		luceneFieldName:	'E_keywordId',
		label:						'Data Type',
		valueField:				'uri',
		displayField:			'value',
		thesaurusField:		'uri',
		multi:						true
	},
	{ 
		thesaurus: 'geonetwork.thesaurus.register.theme.urn:marine.csiro.au:keywords:cmarAOI',
		thesaurusShortName: 'register.theme.urn:marine.csiro.au:keywords:cmarAOI',
		luceneFieldName:	'E_keywordId',
		label:						'Areas of Interest',
		valueField:				'uri',
		displayField:			'value',
		thesaurusField:		'uri',
		multi:						true
	},
	{ 
		thesaurus: 'geonetwork.thesaurus.register.dataSource.urn:marine.csiro.au:keywords:dataSource',
		thesaurusShortName: 'register.dataSource.urn:marine.csiro.au:keywords:dataSource',
		luceneFieldName:	'E_keywordId',
		label:						'Data Source',
		valueField:				'uri',
		displayField:			'value',
		thesaurusField:		'uri',
		multi:						true
	},
	{ 
		thesaurus: 'geonetwork.thesaurus.register.theme.urn:marine.csiro.au:keywords:habitat',
		thesaurusShortName: 'register.theme.urn:marine.csiro.au:keywords:habitat',
		luceneFieldName:	'E_keywordId',
		label:						'Habitat',
		valueField:				'uri',
		displayField:			'definition',
		thesaurusField:		'uri',
		multi:						true
	},
	{ 
		thesaurus: 'geonetwork.thesaurus.register.equipment.urn:marine.csiro.au:keywords:equipment',
		thesaurusShortName: 'register.equipment.urn:marine.csiro.au:keywords:equipment',
		luceneFieldName:	'E_keywordId',
		label:						'Equipment',
		valueField:				'uri',
		displayField:			'definition',
		thesaurusField:		'uri',
		multi:						true
	},
	{ 
		thesaurus: 'geonetwork.thesaurus.register.theme.urn:marine.csiro.au:keywords:subject',
		thesaurusShortName: 'register.theme.urn:marine.csiro.au:keywords:subject',
		luceneFieldName:	'E_keywordId',
		label:						'Subject',
		valueField:				'uri',
		displayField:			'value',
		thesaurusField:		'uri',
		multi:						true
	},
	{ 
		thesaurus: 'geonetwork.thesaurus.register.theme.urn:marine.csiro.au:keywords:anzlicSearch',
		thesaurusShortName: 'register.theme.urn:marine.csiro.au:keywords:anzlicSearch',
		luceneFieldName:	'E_keywordId',
		label:						'ANZLIC',
		valueField:				'uri',
		displayField:			'value',
		thesaurusField:		'uri',
		multi:						true
	},
	{ 
		thesaurus: 'geonetwork.thesaurus.register.place.urn:marine.csiro.au:keywords:region',
		thesaurusShortName: 'register.place.urn:marine.csiro.au:keywords:region',
		luceneFieldName:	'E_keywordId',
		label:						'Region',
		valueField:				'uri',
		displayField:			'value',
		thesaurusField:		'uri',
		multi:						true
	}
]

MarLIN.ThesauriStore = new Ext.data.JsonStore({
		fields: [ 'thesaurus', 'thesaurusShortName', 'luceneFieldName', 'label', 'valueField', 'displayField', 'thesaurusField', 'multi' ]
});

MarLIN.buildKeywordRow = function(services, selector, thesaurusInfo) {
	var button = new Ext.Button({
			id: 'thesaurusBt'+thesaurusInfo.get('thesaurus'), 
			margins:'3 5 3 5',
			icon: '../images/default/map/icon_zoomin.png',
			iconAlign: 'right',
			toolTip: 'Search '+thesaurusInfo.get('label')+' List',
			listeners: {
						click: function(){
							MarLIN.getKeywordSelectorPanel(services, thesaurusInfo);
						}
			}
	});

			
	return [{
						xtype: 'panel',
						layout: 'column',
						border: false,
						items:
						[
							{
									xtype: 'panel',
									columnWidth: 0.95, 
									layout: 'form',
									border: false,
									items: selector 	
							},
							{
									xtype: 'panel',
									layout: 'form',
									border: false,
									columnWidth: 0.05,
									items: button
							}
						]
				}];
}

MarLIN.getKeywordSelectorPanel = function (services, thesaurusInfo) {
		
		if (MarLIN.SearchKeywordSelectionPanel == null) {

			  MarLIN.SearchKeywordSelectionPanel = new MarLIN.KeywordSelectionPanel({
					services : services,
					filterThesaurus : thesaurusInfo,
					listeners : {
						keywordselected: function(panel, keywords) {
							Ext.each(keywords, function(item,index) {
								var selections = [];
								var theIndex = MarLIN.ThesauriStore.findExact('thesaurusShortName', item.thesaurus);
								var vField = MarLIN.ThesauriStore.getAt(theIndex).get('valueField');
								// get combo box with thesaurus name and add item values
								var combo = Ext.getCmp(item.thesaurus);
								var store = combo.getStore();
								Ext.each(item.values, function(item, index) {
									var index = store.findExact('uri', item);
									if (index >= 0) {
										if (vField == 'value') {
											dataValue = store.getAt(index).data.value;
											selections.push(dataValue);
										} else {
											dataValue = store.getAt(index).data.uri;
											selections.push(dataValue);
										}
									}
								}, this);
								// now set the selected set into the superbox
								combo.setValue(selections);
							}, this);
						}
					}
			  });
		} else {
			MarLIN.SearchKeywordSelectionPanel.setThesaurus(thesaurusInfo);
		}

		if (MarLIN.SearchKeywordSelectionWindow == null) {
			MarLIN.SearchKeywordSelectionWindow = new Ext.Window({
          	width: 720,
            height: 330,
            layout: 'fit',
						title: 'Search Keywords from '+thesaurusInfo.get('label'), 
            items: MarLIN.SearchKeywordSelectionPanel,
            closeAction: 'hide',
            constrain: true,
            iconCls: 'searchIcon'
			});
		} else {
			MarLIN.SearchKeywordSelectionWindow.setTitle('Search Keywords from '+thesaurusInfo.get('label'));
		}

		MarLIN.SearchKeywordSelectionWindow.show();	
}

MarLIN.buildKeywordStore = function(services, thesaurus, sort) {
				var Keyword;

    		Keyword = Ext.data.Record.create([{
            name : 'id'
        }, {
            name : 'value'
        }, {
            name : 'definition'
        }, {
            name : 'uri'
    		}, { 
				    name: 'thesaurus'
				}]);

        // Keyword store
        return new Ext.data.Store({
            proxy : new Ext.data.HttpProxy({
            url : services.searchKeyword,
            method : 'GET'
         }),
         baseParams : {
                pNewSearch : true,
                pTypeSearch : 1,
                pKeyword: '*',
                pThesauri : thesaurus,
                pMode : 'searchBox',
                maxResults : '6000'
         },
         reader : new Ext.data.XmlReader({
                record : 'keyword',
                id : 'id'
         				}, Keyword),
         fields : [ "id", "value", "definition", "uri", "thesaurus" ],
         sortInfo : {
                field : sort 
         },
				 pruneModifiedRecords: true
        });
}

MarLIN.SearchFormTools = {

    /** api:method[getResourceTypeField]
     *  :param multi: Create fields with multiselection combobox.
     *
     *  :return: resource types combo box
     *
     */
    getResourceTypeField : function (multi) {
        var resourceTypes = [ 
															[ 'dataset',  'Dataset'  ],
														  [ 'project',  'Project'  ],
                              [ 'survey',   'Survey'   ],
                              [ 'service',  'Service'  ],
                              [ 'register', 'Register' ]
													  ];

        config = {
                id : 'marLINResourceType',
                name : 'E_type',
                mode : 'local',
                triggerAction : 'all',
                fieldLabel : 'Resource Type',
                store : new Ext.data.ArrayStore({
                    id : 0,
                    fields : [ 'id', 'label' ],
                    data : resourceTypes
                }),
                valueField : 'id',
                displayField : 'label'
            };
        if (multi) {
            Ext.apply(config, {
                valueDelimiter: ' or '
            });
            return new Ext.ux.form.SuperBoxSelect(config);
        } else {
            return new Ext.form.ComboBox(config);
        }
    },
    /** api:method[getOrganisationField] 
     * 
     *  :param services: Catalogue service URLs (eg. catalogue.services).
     *  
     *  :return: A multi-select field with autocompletion (based on Lucene field content - not a thesaurus)
     */
    getOrganisationField : function (services) {
        var orgNameStore = new GeoNetwork.data.OpenSearchSuggestionStore({
            url: services.opensearchSuggest,
            rootId: 1,
						sortInfo: {
							field: 'value',
							direction: 'ASC'
						},
            baseParams: {
                field: 'orgName'
            }
        });

        var orgNameField = new Ext.ux.form.SuperBoxSelect({
            hideLabel: false,
            minChars: 0,
            queryParam: 'q',
            hideTrigger: false,
            id: 'orgName',
            name: 'E_orgName',
            store: orgNameStore,
            valueField: 'value',
            displayField: 'value',
            valueDelimiter: ' or ',
            mode : 'local',
            fieldLabel: OpenLayers.i18n('org')
        });

				orgNameField.on('beforerender', function() {
							orgNameStore.load();
				});
        return orgNameField;
    },
    /** method[getThesaurusField] 
     *  :param thesaurus: Name of thesaurus to build combox box for.
     *  :param thesaurusInfo: Information on thesaurus to build combox box for.
     *  :param services: Catalogue service URLs (eg. catalogue.services).
     *  
     *  :return: A combo box and button for searcher
     * 
     *  Use xml.search.keywords service to retrieve the list of all Thesaurus
		 *  keywords.
     * 
     */
    getThesaurusField : function (thesaurusInfo, services) {
        var keyStore, selector;

        // Keyword store which is thesaurus terms filtered with values from
				// lucene field ThesaurusName
        keyStore = MarLIN.buildKeywordStore(services, 
																		thesaurusInfo.get('thesaurusShortName'), 
																		thesaurusInfo.get('displayField'));

        keyStore.on({
            'loadexception': function() {},
            'load': function() {

								// Load contents of lucene field with name of thesaurus and 
								// remove entries from thesaurus store so that only those 
								// actually being used in the index are shown to the user.
								var lStore = new GeoNetwork.data.OpenSearchSuggestionStore({
										url: services.opensearchSuggest,
										rootId: 1,
										autoLoad: true,
										baseParams: {
											field: thesaurusInfo.get('thesaurus') 
										}
								});

								var theField = thesaurusInfo.get('thesaurusField');
								//console.log('Checking '+theField);
								lStore.on({ 
									'load': function() { 

										//console.log('loaded from '+thesaurusInfo.get('thesaurus')+' records '+lStore.getTotalCount());
										if (lStore.getTotalCount() > 0) {
										/* Alternative not good
											keyStore.each(function(item) {
												if (lStore.findExact('value',item.get(theField)) < 0) {
														keyStore.remove(item);
												} else {
														//console.log('Found '+item.get(theField));
												}
											});
										*/
											var r = keyStore.getRange();
											for (var j = 0, l = r.length;j < l;j++) {
												if (lStore.findExact('value',r[j].get(theField)) < 0) {
													keyStore.remove(r[j]);
												}
											}
											keyStore.commitChanges();
										}
									},
									scope: this
								});
            },
            scope: this 
        });

        var config = {
                id : thesaurusInfo.get('thesaurusShortName'),
                name : thesaurusInfo.get('luceneFieldName'),
                mode : 'local',
                triggerAction : 'all',
                fieldLabel : thesaurusInfo.get('label'),
                store : keyStore,
                valueField : thesaurusInfo.get('valueField'),
                displayField : thesaurusInfo.get('displayField') 
            };
        if (thesaurusInfo.get('multi')) {
            Ext.apply(config, {
                valueDelimiter: ' or ',
                stackItems: true
                });
            selector = new Ext.ux.form.SuperBoxSelect(config);
        } else {
            selector = new Ext.form.ComboBox(config);
        }

				selector.on('beforerender', function() {
					keyStore.load();
				});

				return MarLIN.buildKeywordRow(services, selector, thesaurusInfo);
		},
		/** api:method[getFields]
     *  :param services: Catalogue service URLs (eg. catalogue.services).
     *  :param multi: Create fields with multiselection combobox.
     *  :return: A MarLIN form
     * 
     *  Create a MarLIN form with all fields or a subset of fields.
     */
    getFields : function (services, multi) {
			var f = [];

			MarLIN.ThesauriStore.loadData(MarLIN.Thesauri);

			MarLIN.ThesaurusPanel = new Ext.Panel({
				layout: 'form',
				border: false,
				autoWidth: true,
				autoHeight: true,
				items: [this.getResourceTypeField(multi), this.getOrganisationField(services)]
			});

			// now get all thesauri in use from Lucene index field thesaurusName
      var theStore = new GeoNetwork.data.OpenSearchSuggestionStore({
          	url: services.opensearchSuggest,
          	rootId: 1,
           	baseParams: {
           		field: 'thesaurusName'
           	}
      });

			// for each thesaurus that has been used in the Lucene index, create a 
			// combobox/superselectbox field and keyword selector to suit
			theStore.on({
					'load': function() { 
									var theValues = theStore.collect('value');
									//console.log('Loading '+theValues);
									Ext.each(theValues, function(item, index) {
										var found = MarLIN.ThesauriStore.find('thesaurus',item);
										if (found >= 0) {
											//console.log('Adding '+item+' '+MarLIN.ThesauriStore.getAt(found));
											MarLIN.ThesaurusPanel.add(MarLIN.SearchFormTools.getThesaurusField(MarLIN.ThesauriStore.getAt(found), services));
										}
									});
									MarLIN.ThesaurusPanel.doLayout();
					},
					scope: this 
			});
			theStore.load();

			// push form panel into the list of form items returned
			f.push(MarLIN.ThesaurusPanel);

      return f;
    }
};
