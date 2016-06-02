// Some JS function used by the editor in Sextant only
//  * in EMODNET editing view
/**
 * Add the elements from a list with an attribute or not
 * 
 * @param nameOldList
 *            name where the selected elements are added
 * @param ref
 *            reference of the parent element
 * @param name
 *            name of the element to add
 * @param nameNewList
 *            name of the list where the elements are selected
 * @param id
 *            identifier of the previous element where the new element should be
 *            added
 * @param nameAttr
 *            name of the attribute of the added elements
 * @param valueAttr
 *            value of the attribute of the added elements
 * @param deletePreviousElem
 *            true if you want to delete the previous element
 * 
 * @return
 */
function doAddElementAction(nameOldList, ref, name, nameNewList, id, nameAttr,
		valueAttr, deletePreviousElem) {

	// Get the elements already selected in the list (the elements already
	// added)
	var tab = document.getElementsByName(nameOldList);
	var oldSelectedItems = tab.item(0).getElementsByTagName("input");

	// Count of the element already added
	var nbOldSelectedItems = oldSelectedItems.length;
	// Count of the element selected
	var nbNewSelectedItems = 0;

	// Get the elements selected in the list
	var newSelectedItems = new Array();
	var allPossibleItems = Ext.getDom('list_' + id);
	for ( var i = 0; i < allPossibleItems.options.length; i++) {
		if (allPossibleItems.options[i].selected) {
			newSelectedItems[nbNewSelectedItems] = allPossibleItems.options[i].value;
			nbNewSelectedItems++;
		}
	}
	// There are elements to delete
	if (nbOldSelectedItems > nbNewSelectedItems) {
		// Get the identifier of the elements already added
		var idOldItems = new Array();
		var tmpList = tab.item(0).getElementsByTagName("tr");
		var count = 0
		for ( var i = 0; i < tmpList.length; i++) {
			if (tmpList[i].id.length > 0) {
				idOldItems[count] = tmpList[i].id;
				count++;
			}
		}

		// Simulate the time between the deletions
		var now = 0;
		var lastclick = 0;

		// Remove the number of elements to delete
		for ( var i = nbNewSelectedItems; i < nbOldSelectedItems; i++) {

			// Never remove the first element
			if (nbNewSelectedItems != 0) {
				// Get the reference of the element
				var childRef = oldSelectedItems[i].id.substring(1);
				// for gmd:keyword element, the reference is the one of the
				// parent of the element
				// if(idOldItems[i].match(new RegExp("gmd:keyword","g"))){
				// Get the reference of the parent of the element
				if (deletePreviousElem == true) {

					childRef = childRef - 1;
				}
				// Remove the element
				doRemoveElementAction('/metadata.elem.delete', childRef, ref,
						idOldItems[i], 1);
			}

			// Wait 0,75 since last remove
			while ((now - lastclick) > 750) {
				now = (new Date()).valueOf();
			}
			setBunload(false);
			lastclick = now;
		}

		// There is as many elements added as many element selected
		nbOldSelectedItems = nbNewSelectedItems;
	}

	// There are elements to add
	if (nbNewSelectedItems > 0) {

		// console.log("There are elements to add");
		// Replace the value of the existing elements
		for ( var i = 0; i < nbOldSelectedItems; i++) {
			oldSelectedItems[i].value = newSelectedItems[i];
		}

		// Simulate the time between the addition
		var now = 0;
		var lastclick = 0;
		// Add the new elements with this value and this attribute
		for ( var i = nbOldSelectedItems; i < nbNewSelectedItems; i++) {
			doAddElementAjax(ref, name, id, newSelectedItems[i], nameAttr,
					valueAttr);

			// Wait 0,75 since last add
			while ((now - lastclick) > 750) {
				now = (new Date()).valueOf();
			}
			setBunload(false);
			lastclick = now;
		}
	}

	// There are not elements to add or delete. Replace the value of existing
	// elements
	if (nbOldSelectedItems == nbNewSelectedItems) {
		// There are no elements selected
		if (nbOldSelectedItems == 0)
			oldSelectedItems[0].value = "";
		// Replace the value of the existing elements
		for ( var i = 0; i < nbOldSelectedItems; i++) {
			oldSelectedItems[i].value = newSelectedItems[i];
		}
	}

}

/**
 * Add an elements with an attribute or not
 * 
 * @param ref
 *            reference of the parent element
 * @param name
 *            name of the element to add
 * @param id
 *            identifier of the previous element where the new element should be
 *            added
 * @param value
 *            value of the added element
 * @param nameAttr
 *            name of the attribute of the added elements
 * @param valueAttr
 *            value of the attribute of the added elements
 * 
 * @return
 */
function doAddElementAjax(ref, name, id, value, nameAttr, valueAttr) {
	var metadataId = document.mainForm.id.value;
	// The element have an attribute to add or not
	var attrParams = "";
	if ((typeof nameAttr != 'undefined' && typeof valueAttr != 'undefined')
			&& (nameAttr != null && valueAttr != null)) {
		attrParams = "&nameAttr=" + nameAttr + "&valAttr=" + valueAttr;
	} else if (typeof nameAttr != "undefined" && nameAttr != null) {
		attrParams = "&nameAttr=" + nameAttr;
	}
	// Parameters necessary of the addition
	var params = "&id=" + metadataId + "&ref=" + ref + "&name=" + name
			+ attrParams;

	var thisElement = Ext.get(id);

	// Add the element
	Ext.Ajax.request({
		url : catalogue.services.rootUrl + '/metadata.elem.add',
		method : 'GET',
		params : params,
		success : function(req, request) {
			var html = req.responseText;

			// Add the value of the element
			var regexp = new RegExp('value=""', "g");
			var newHtml = html.replace(regexp, 'value="' + value + '"');

			// Insert the element in the html page
			// thisElement.insert({after:newHtml});
			thisElement && thisElement.insertHtml('afterEnd', newHtml, true);
			// initCalendar();
			// // Check elements
			// validateMetadataFields();
			// // reset warning for window destroy
			// setBunload(true);

		},
		failure : function(req) {
			alert(translate("errorAddElement") + name
					+ translate("errorFromDoc") + " / status " + req.status
					+ " text: " + req.statusText + " - "
					+ translate("tryAgain"));
			// reset warning for window destroy
			// setBunload(true);
		}
	});
};
