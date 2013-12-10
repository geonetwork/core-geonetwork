Ext.namespace("csiro");

/** api: (define) 
 *  module = csiro 
 *  class = EditorExtensions
 *
 * showThesaurusSelectionPanel: Used in sensorML editing for 
 * picking terms from thesauri (but could be used in any schema) 
 *
 * showCRSSelectionPanel: Used in sensorML editing for 
 * picking CRS from crs.search service (but could be used in any schema) 
 *
 * showRelatedDatasetSelectionPanel: Used in sensorML editing for
 * picking related datasets and assigning the result to an input element
 * in the editing form (but could be used in any schema)
 */

csiro.SearchThesaurusSelectionPanel = null;
csiro.SearchCRSSelectionPanel = null;
csiro.SearchKeywordSelectionWindow = null;

csiro.showThesaurusSelectionPanel = function(thesaurusTitle, thesaurus, term, termId, thesaurusId) {

	var filterThesaurus = null;
	if (thesaurus != '') {
		filterThesaurus = thesaurus;
	}

	csiro.SearchThesaurusSelectionPanel = new csiro.ThesaurusTermSelectionPanel({
				filterThesaurus : filterThesaurus,
				minSelected: 1,
				maxSelected: 1,
				listeners : {
						keywordselected: function(panel, keywords) {
							for (var thesaurus in keywords) {
								if (keywords[thesaurus].uris.length > 0) {
									if (thesaurusId != '') { // thesaurus may not be present
										$(thesaurusId).value = thesaurus;
									}
									// set uri and value into appropriate inputs
									Ext.each(keywords[thesaurus].uris, function(uri, index) {
											$(termId).value = uri;
											if (term != '') {
												$(term).value = keywords[thesaurus].terms[index];
											}
										}, this);
								}
							}
						}
				}
	});

	var title = 'Search Keywords';
	/*
	if (thesaurus != '') {
		title += ' from ' + thesaurusTitle;
	} */
		
	csiro.SearchKeywordSelectionWindow = new Ext.Window({
       	width: 720,
        height: 330,
        layout: 'fit',
				title: title, 
        items: csiro.SearchThesaurusSelectionPanel,
        closeAction: 'hide',
        constrain: true,
        iconCls: 'searchIcon'
	});
	csiro.SearchKeywordSelectionWindow.show();	
};

csiro.showCRSSelectionPanel = function(crsId, useCode) {

	csiro.SearchCRSSelectionPanel = new csiro.CRSSelectionPanel({
				minSelected: 1,
				maxSelected: 1,
				listeners : {
						crsSelected: function(panel, crss) {
							if (useCode) {
								$(crsId).value = crss.codes[0];
							} else {
								// set description into appropriate input
								$(crsId).value = crss.descriptions[0];
							}
						}
				}
	});

	var title = 'Search Coordinate Reference Systems (CRS)';
		
	csiro.SearchCRSSelectionWindow = new Ext.Window({
       	width: 720,
        height: 330,
        layout: 'fit',
				title: title, 
        items: csiro.SearchCRSSelectionPanel,
        closeAction: 'hide',
        constrain: true,
        iconCls: 'searchIcon'
	});
	csiro.SearchCRSSelectionWindow.show();	
};

csiro.showRelatedDatasetSelectionPanel = function(xlinkRef, nameRef, nameValue) {

	var linkedMetadataSelectionPanel = new app.LinkedMetadataSelectionPanel({
		ref: xlinkRef,
		singleSelect: true,
		mode: name,
		listeners: {
			linkedmetadataselected: function(panel, metadata) {
				if (this.ref != null) {
				  $(this.ref).value = metadata[0].data.uuid;
					// post condition - if success then set nameRef.value to nameValue
					if (nameRef != null) {
						$(nameRef).value = nameValue;
					}
				}
			}
		}
	});

  var linkedMetadataSelectionWindow = new Ext.Window({
      title: translate('linkedMetadataSelectionWindowTitle'),
      width: 620,
      height: 300,
      layout: 'fit',
      items: linkedMetadataSelectionPanel,
      closeAction: 'hide',
      constrain: true,
      iconCls: 'linkIcon',
      modal: true
  });

  linkedMetadataSelectionWindow.show();
};
