var getGNServiceURL = function(service) {
	return Env.locService+"/"+service;
};

function findPos(obj) 
{
	var curtop = 0;
	if (obj) {
		var arr = obj.cumulativeOffset();
		curtop = arr[1]; // top
	}
	return curtop;
}

var Checks = {
	message : translate("loseYourChange"),
	_setMessage : function(str)
	{
		  this.message = str;
  },

	_onbeforeunload : function(e)
 	{
		if (opener) {
			editRed = opener.$$('.editing');
			if (editRed && editRed.length > 0) {
				editRed.invoke('removeClassName','editing');
			}
     	e.returnValue = this.message;
 		}
	}
};

function unloadMess()
{
  mess = translate("loseYourChange");
  return mess;
}

var	bfu = Checks._onbeforeunload.bindAsEventListener(Checks);

function setBunload(on)
{
 	if (on) {
		Event.observe(window, 'beforeunload', bfu);
		Checks._setMessage(unloadMess());
	} else {
		Event.stopObserving(window, 'beforeunload', bfu);
		Checks._setMessage(null);
	}
}

function gn_editKeyObserver(e)
{
	// nothing yet - but a key to save the metadata would be nice
}

function doEditorLoadActions() 
{
	setBunload(true);
	Event.observe(window,'keypress',gn_editKeyObserver);
}

Event.observe(window,'load',doEditorLoadActions);

// register event listeners on the Ajax requests to show/hide the busy 
// indicator
Ajax.Responders.register({
	onCreate: function() {
 		if (Ajax.activeRequestCount === 1) {
			var eBusy = $('editorBusy');
			if (eBusy) eBusy.show();
 		}
 	},
 	onComplete: function() {
 		if (Ajax.activeRequestCount === 0) {
			var eBusy = $('editorBusy');
			if (eBusy) eBusy.hide();
 		}
 	}
});

function doAction(action)
{
	setBunload(false);
	document.mainForm.action = action;
	goSubmit('mainForm');
}

function doTabAction(action, tab)
{
	disableEditForm();

	document.mainForm.currTab.value = tab;
	doAction(action);
}

function doCommonsAction(action, name, licenseurl, type, id)
{
	var top = findPos($(id));
	setBunload(false);
  document.mainForm.name.value = name;
  document.mainForm.licenseurl.value = licenseurl;
  document.mainForm.type.value = type;
	document.mainForm.position.value = top;
  doAction(action);
}

function doResetCommonsAction(action, name, licenseurl, type, id, ref)
{
	$(ref).value = '';
	document.mainForm.ref.value = '';
	doCommonsAction(action, name, licenseurl, type, id);
}

function getControlsFromElement(el) {
	var id = el.readAttribute('id');
	elButtons = $('buttons_'+id);
	return elButtons.immediateDescendants();
}

function topElement(el) 
{
	if (el.previous() == undefined) return true;
	else return (!isSameElement(el.previous(),el));
}

function bottomElement(el) 
{
	if (el.next() == undefined) return true;
	else return (!isSameElement(el.next(),el));
}

function getIdSplit(el) 
{
	var id = el.readAttribute('id');
	if (id == null) return null;
	return id.split("_");
}

function orElement(el) 
{
	if (el.next() == undefined) return false;
	else {
		var nextEl = getIdSplit(el.next());
		var thisEl = getIdSplit(el);
		if (nextEl == null || thisEl == null) return false;
		if (nextEl[0] == "child" && nextEl[1] == thisEl[0]) return true;
		else return false;
	}
}

function isSameElement(el1,el2) 
{
	var i1 = getIdSplit(el1);
	var i2 = getIdSplit(el2);
	if (i1 == null || i2 == null) return false;
	if (i1[0] == i2[0]) return true;
	else return false;
}

function topControls(el,min) 
{
	var elDescs = getControlsFromElement(el);
	
	// Check addXmlFragment control
	var index = 0;
	if (elDescs.length == 5) index = 1;
	
	// sort out +
	if (bottomElement(el) && !orElement(el)) elDescs[0].show();
	else elDescs[0].hide();
	
	// sort out +/x (addXmlFragment)
	if (index == 1) {
		if (bottomElement(el) && !orElement(el))
			elDescs[index].show();
		else
			elDescs[index].hide();
	}

	// sort out x
	if (bottomElement(el)) {
		if (min == 0) elDescs[1+index].show();
		else elDescs[1+index].hide();
	} else elDescs[1+index].show();

	// sort out ^
	elDescs[2+index].hide();

	// sort out v
	if (bottomElement(el)) elDescs[3+index].hide();
	else elDescs[3+index].show();
}

function doRemoveElementAction(action, ref, parentref, id, min)
{
	var metadataId = document.mainForm.id.value;
	var thisElement = $(id);
	var nextElement = thisElement.next();
	var prevElement = thisElement.previous();
	
	var myExtAJaxRequest = Ext.Ajax.request({
		url: getGNServiceURL(action),
		method: 'GET',
		params: {id:metadataId, ref:ref, parent:parentref},
		success: function(result, request) {
			var html = result.responseText;
			if (html.blank()) { // more than one left, no child-placeholder returned
				if (bottomElement(thisElement)) { 
					swapControls(thisElement,prevElement);
					thisElement.remove();
					thisElement = prevElement;
				} else {
					thisElement.remove();
					thisElement = nextElement;
				}
				if (topElement(thisElement)) topControls(thisElement,min); 
			} else { // last one, so replace with child-placeholder returned
				if (orElement(thisElement)) thisElement.remove();
				else thisElement.replace(html);
			} 
			setBunload(true); // reset warning for window destroy
		},
		failure:function (result, request) { 
			Ext.MessageBox.alert(translate("errorDeleteElement") + name + " " + translate("errorFromDoc") 
						+ " / status " + result.status + " text: " + result.statusText + " - " + translate("tryAgain"));
			setBunload(true); // reset warning for window destroy
		}
	});
}

function swapControls(el1,el2) 
{
	var el1Descs = getControlsFromElement(el1);
	var el2Descs = getControlsFromElement(el2);
			
	for (var index = 0; index < el1Descs.length; ++index) {
	 var visible1 = el1Descs[index].visible();
	 var visible2 = el2Descs[index].visible();
	 if (visible1) el2Descs[index].show();
	 else el2Descs[index].hide();
	 if (visible2) el1Descs[index].show();
	 else el1Descs[index].hide();
	}
}

function doMoveElementAction(action, ref, id)
{
	var metadataId = document.mainForm.id.value;
	var pars = "&id="+metadataId+"&ref="+ref;
	var thisElement = $(id);

	var myAjax = new Ajax.Request(getGNServiceURL(action),
		{
			method: 'get',
			parameters: pars,
			onSuccess: function(req) {
				if (action.include('elem.up')) { 
					var upElement = thisElement.previous();
					upElement = upElement.remove();
					thisElement.insert({'after':upElement});
					swapControls(thisElement,upElement);
				} else {
					var downElement = thisElement.next();
					downElement = downElement.remove();
					thisElement.insert({'before':downElement});
					swapControls(thisElement,downElement);
				}
				setBunload(true); // reset warning for window destroy
			},
			onFailure: function(req) { 
				alert(translate("errorMoveElement") + ref + " / status " + req.status 
						+ " text: " + req.statusText + " - " + translate("tryAgain"));
				setBunload(true); // reset warning for window destroy
			}
		}
	);
	setBunload(true);
}

function doNewElementAction(action, ref, name, id, what, max)
{
	var child = null;
	var orElement = false;
	doNewElementAjax(action, ref, name, child, id, what, max, orElement);
}

function doNewORElementAction(action, ref, name, child, id, what, max)
{
	var orElement = true;
	doNewElementAjax(action, ref, name, child, id, what, max, orElement);
}

function setAddControls(el, orElement) 
{
	elDescs = getControlsFromElement(el);
	
	// Check addXmlFragment control
	var index = 0;
	if (elDescs.length == 5) index = 1;
	
	// + x ^ _ or _ x ^ _ is default on add
	if (orElement) elDescs[0].hide();
	else elDescs[0].show();
	
	if (index == 1) {
		if (orElement) elDescs[index].hide();
		else elDescs[index].show();
	}
	elDescs[1+index].show();
	elDescs[2+index].show();
	elDescs[3+index].hide();

	// special cases - if this is topElement - need + x _ _ or _ x _ _
	if (topElement(el)) {
		elDescs[2+index].hide();
	} else { // otherwise fix up previous element
		var prevEl = el.previous();
		var prevDescs = getControlsFromElement(prevEl);
		var prevIndex = 0;
		if ( prevDescs.length == 5) prevIndex = 1;
		prevDescs[0].hide();
		if (prevIndex==1) prevDescs[prevIndex].hide();
		prevDescs[1+prevIndex].show();
		if (topElement(prevEl)) prevDescs[2+prevIndex].hide();
		else prevDescs[2+prevIndex].show();
		prevDescs[3+prevIndex].show();
	}
}

function doNewElementAjax(action, ref, name, child, id, what, max, orElement)
{
	var metadataId = document.mainForm.id.value;
	var pars = "&id="+metadataId+"&ref="+ref+"&name="+name;
	if (child != null) 
		pars += "&child="+child;
	var thisElement = $(id);

	var myAjax = new Ajax.Request(
	getGNServiceURL(action),
		{
			method: 'get',
			parameters: pars,
			onSuccess: function(req) {
				var html = req.responseText;
				if (what == 'replace') {
					thisElement.replace(html);
				} else if (what == 'add' || what == 'after') {
					thisElement.insert({'after':html});
					setAddControls(thisElement.next(), orElement);
				} else if (what == 'before') { // only for orElement = true
					thisElement.insert({'before':html});
					setAddControls(thisElement.previous(), orElement);
				} else {
					alert("doNewElementAjax: invalid what: " + what + " should be one of replace, after or before.");
				}
				
				// Init map if spatial extent editing
				if (name == 'gmd:geographicElement');
					extentMap.initMapDiv();
				
				// Check elements
				validateMetadataFields();
				
				setBunload(true); // reset warning for window destroy
			},
			onFailure: function(req) { 
				alert(translate("errorAddElement") + name + translate("errorFromDoc") 
						+ " / status " + req.status + " text: " + req.statusText + " - " + translate("tryAgain"));
				setBunload(true); // reset warning for window destroy
			}
		}
	);
}

function disableEditForm()
{
	var editorOverlay = new Element("div", { id: "editorOverlay" });
	$('editFormTable').insert({'top':editorOverlay});
	$('editorOverlay').setStyle({opacity: "0.65"});
}

function doSaveAction(action,validateAction)
{
	disableEditForm();

	// if we are doing a validation then enable display of errors in editor 
	// - by default false
	if (typeof validateAction != 'undefined') {
		document.mainForm.showvalidationerrors.value = "true";
	}

	var metadataId = document.mainForm.id.value;
	var divToRestore = null;
	if (opener) {
		divToRestore = opener.document.getElementById(metadataId);
	}

	if (action.include('finish')) { // save and then replace editor with viewer 
		var myAjax = new Ajax.Request(
			getGNServiceURL(action),
			{
				method: 'post',
				parameters: $('editForm').serialize(true),
				onSuccess: function(req) {
					var html = req.responseText;
					if (divToRestore) divToRestore.removeClassName('editing');
					if (html.startsWith("<?xml") < 0) { // service returns xml on success
						alert("Save failed: "+html);
					}
					
					setBunload(false);
					location.replace(getGNServiceURL('metadata.show?id='+metadataId));
				},
				onFailure: function(req) { 
					alert(translate("errorSaveFailed") + "/ status " + req.status + " text: " + req.statusText + " - " + translate("tryAgain"));
 					$('editorBusy').hide();
					setBunload(true); // reset warning for window destroy
				}
			}
		);
	} else {
		var myAjax = new Ajax.Updater(document.body,
			getGNServiceURL(action),
			{
				method: 'post',
				parameters: $('editForm').serialize(true),
				evalScripts: true,
				onComplete: function(req) {
					if (typeof validateAction != 'undefined') {
						doActionInWindow.delay(1,validateAction); // delay the validate
					}
					setBunload(true); // reset warning for window destroy
					validateMetadataFields();
				},
				onFailure: function(req) { 
					alert(translate("errorSaveFailed") + "/ status " + req.status+" text: " + req.statusText + " - " + translate("tryAgain"));
 					$('editorBusy').hide();
					setBunload(true); // reset warning for window destroy
				}
			}
		);
	}

}

function doActionInWindow(action)
{

	var metadataId = document.mainForm.id.value;
	var pars = "&id="+metadataId;

	var myAjax = new Ajax.Request(getGNServiceURL(action),
		{
			method: 'get',
			parameters: pars,
			onSuccess: function(req) {
				var html = req.responseText;
				var myWindow = window.open('about:blank',"popWindow","location=no, toolbar=no, directories=no, status=no, menubar=no, scrollbars=yes, resizable=yes, width=800, height=600");	
				myWindow.document.write(html);
				myWindow.document.close();
				
				// Check elements
				validateMetadataFields();
				
				setBunload(true); // reset warning for window destroy
			},
			onFailure: function(req) { 
				alert(translate("errorOnAction") + action + " / status " + req.status + " text: " + req.statusText + " - " + translate("tryAgain"));
   			$('editorBusy').hide();
				setBunload(true); // reset warning for window destroy
			}
		}
	);
}

function doCancelAction(action, message)
{
	if(confirm(message)) {
		doSaveAction(action);
		return true;
	}
	return false;
}

function doConfirm(action, message)
{
	if(confirm(message)) {
		doAction(action);
		return true;
	}
	return false;
}

function doEditorAlert(message)
{
	alert(message);
	setBunload(true); // reset warning for window destroy
}

// called when protocol select field changes in metadata form
function checkForFileUpload(fref, pref)
{
	var fileName = $('_'+fref);     // the file name input field
	var protoSelect = $('s_'+pref); // the protocol <select>
	var protoIn = $('_'+pref);        // the protocol input field to be submitted

	var fileUploaded = (fileName != null && fileName.value.length > 0);
	var protocol = protoSelect.value;
	var protocolDownload = (protocol.startsWith('WWW:DOWNLOAD') && protocol.indexOf('http')>0);

	// don't let anyone change the protocol if a file has already been uploaded 
	// unless its between downloaddata and downloadother
	if (fileUploaded) {
		if (!protocolDownload ) {
			alert(translate("errorChangeProtocol"));
			// protocol change is not ok so reset the protocol value
			protoSelect.value = protoIn.value; 
		} else { 
			// protocol change is ok so set the protocol value to that selected
			protoIn.value = protoSelect.value;
		}
		return;
	}

	// now hide the conventional name field and show a file upload button or
	// vice versa depending on protocol
	finput = $('di_'+fref);
	fbuttn = $('db_'+fref);
	if (protocolDownload) {
		if (finput != null) finput.hide();
		if (fbuttn != null) fbuttn.show();
	} else {
		if (finput != null) finput.show();
		if (fbuttn != null) fbuttn.hide();
	}

	// protocol change is ok so set the protocol value to that selected
	protoIn.value = protoSelect.value;
}

// called by upload button in file field of metadata form
function startFileUpload(id, fref)
{
	Modalbox.show(getGNServiceURL('resources.prepare.upload') + "?ref=" + fref + "&id=" + id, {title: translate('insertFileMode'), height: 200, width: 600});
}

// called by file-upload-list form 
function doFileUploadSubmit(form)
{
	setBunload(false);
	var fid = $('fileUploadForm');
	var ref = fid['ref'];
	var fref = fid['f_'+$F(ref)];
	var fileName = $F(fref);
	if (fileName == '') {
		alert(translate("selectOnFile"));
		return false;
	}

	AIM.submit(form, 
		{ 'onStart': function() {
				Modalbox.deactivate();
			},
			'onComplete': function(doc) {
				Modalbox.activate();
				if (doc.body == null) { // error - upload failed for some reason
					alert(translate("uploadFailed") + doc);
				} else {
					$('uploadresponse').innerHTML = doc.body.innerHTML;
				}

				// if response was ok then fname will be in an id attribute
				var fname = $('filename_uploaded');
				if (fname != null) { 
					var name = $('_'+$F(ref));
					if (name != null) {
						name.value = fname.readAttribute('title');
						$('di_'+$F(ref)).show();
						$('db_'+$F(ref)).hide();
						Modalbox.show(doc.body.innerHTML,{width:600});
					} else {
						alert(translate("uploadSetFileNameFailed"));
					}
				}
			}
		}
	);
}
			
function doFileRemoveAction(action, ref, access, id)
{
	// remove action passes ref via ajax to file remove service for update
	var top = findPos($(id));
	setBunload(false);
	document.mainForm.access.value = access;
	document.mainForm.ref.value = ref;
	document.mainForm.position.value = top;
	document.mainForm.action = action;
	goSubmit('mainForm');
}

function handleCheckboxAsBoolean (input, ref) {
	if (input.checked)	{
		$(ref).value='true';
	}
	else {	
		$(ref).value='false';
	}
}

/**
 * Update bounding box form element.
 * If description id is provided, set description character string.
 */
function setRegion(westField, eastField, southField, northField, region, eltRef, descriptionRef)
{
	var choice = region.value;
	var w = "";
	var e = "";
	var s = "";
	var n = "";
	
	if (choice != "") {
		coords = choice.split(",")
		w = coords[0];
		e = coords[1];
		s = coords[2];
		n = coords[3];
		$("_" + westField).value  = w;
		$("_" + eastField).value  = e;
		$("_" + southField).value = s;
		$("_" + northField).value = n;
		
		if ($("_" + descriptionRef) != null)
			$("_" + descriptionRef).value = region.text;
	} else {
		$("_" + westField).value  = "";
		$("_" + eastField).value  = "";
		$("_" + southField).value = "";
		$("_" + northField).value = "";
	}
	
	var viewers = Ext.DomQuery.select('.extentViewer');
	for (var idx = 0; idx < viewers.length; ++idx) {
	     var viewer = viewers[idx];
	     if (eltRef == viewer.getAttribute("elt_ref")) {
	    	 extentMap.updateBbox(extentMap.maps[eltRef], westField + "," + southField + "," + eastField + "," + northField, eltRef, true); // Region are in WGS84
	     };
	}
}

function clearRef(ref) 
{
	setBunload(false);
	var ourRef = "_"+ref+"_cal";
	$(ourRef).clear();
	setBunload(true); // reset warning for window destroy
}


/* Stop double clicks on metadata element controls */

var lastclick = 0;
function noDoubleClick() 
{
	var now = (new Date()).valueOf();
	if ((now - lastclick) > 500) { // 0.5 seconds since last click
		setBunload(false);
		lastclick = now;
		return true;
	} else {
		return false;
	}
}


/**
* Build duration format for gts:TM_PeriodDuration onkeyup or onchange
* events of duration widget define in metadata-iso19139.xsl.
*
* This only apply to iso19139 (or iso profil) metadata.
*
* Duration format is: PnYnMnDTnHnMnS and could be negative.
*
* Parameters:
* ref - {String} Identifier of a form element (ie. geonet:element/@ref)
*/
function buildDuration(ref) {
    if ($('Y' + ref).value == '')
    $('Y' + ref).value = 0;
    if ($('M' + ref).value == '')
    $('M' + ref).value = 0;
    if ($('D' + ref).value == '')
    $('D' + ref).value = 0;
    if ($('H' + ref).value == '')
    $('H' + ref).value = 0;
    if ($('MI' + ref).value == '')
    $('MI' + ref).value = 0;
    if ($('S' + ref).value == '')
    $('S' + ref).value = 0;
    
    $('_' + ref).value =
    ($('N' + ref).checked? "-": "") +
    "P" +
    $('Y' + ref).value + "Y" +
    $('M' + ref).value + "M" +
    $('D' + ref).value + "DT" +
    $('H' + ref).value + "H" +
    $('MI' + ref).value + "M" +
    $('S' + ref).value + "S";
}


/**
* Validate numeric value input form element.
* If invalid, set the class attribute to "error".
*
* TODO : here we could add a function to turn on/off
* save button if we would like to have more constraint
* in the editor. enableSave(true/false);
*
* Parameters:
* input - {Object} Form element
* nullValue - {Boolean} Allow null value
* decimals - {Boolean} Allow decimals
*/
function validateNumber(input, nullValue, decimals) {
    var text = input.value
    var validChars = "0123456789";
    
    if (! nullValue)
    	if (! validateNonEmpty(input))
    		return false;
    
    if (decimals)
    	validChars += '.';
    
    var isNumber = true;
    var char;
    
    for (i = 0; i < text.length && isNumber; i++) {
        char = text.charAt(i);
        if (char == '-' || char == "+") {
            if (i < 0)
            	isNumber = false;
        } else if (validChars.indexOf(char) == - 1) {
            isNumber = false;
        }
    }
    if (! isNumber) {
        input.addClassName('error');
        return false;
    } else {
        input.removeClassName('error');
        return true;
    }
}

/**
* Validate numeric value input form element.
* If invalid, set the class attribute to "error".
*
* Parameters:
* input - {Object} Form element
*/
function validateNonEmpty(input) {
    if (input.value.length < 1) {
        input.addClassName('error');
    } else {
        input.removeClassName('error');
    }
}

/**
 * Retrieve all page's input and textarea element and check the onkeyup and
 * onchange event (Usually used to check user entry.
 * 
 * @see validateNonEmpty and validateNumber).
 * 
 * @return
 */
function validateMetadataFields() {
	// --- display lang selector when appropriate
	$$('select.lang_selector')
			.each( function(input) {
				// --- language selector has a code attribute to be used to be
					// matched with GUI language in order to edit by default
					// element
					// in GUI language. If none, default language is selected.
					for (i = 0; i < input.options.length; i++)
						if (input.options[i].readAttribute("code")
								.toLowerCase() == Env.lang)
							input.options[i].selected = true;

					enableLocalInput(input, false)
				});

	// --- display validator events when needed.
	$$('input,textarea,select').each( function(input) {
		validateMetadataField(input);
	});

}

/**
 * Property: keywordSelectionWindow
 * The window in which we can select keywords
 */
var keywordSelectionWindow;

/**
 * Display keyword selection panel
 * 
 * @param ref
 * @param name
 * @return
 */
function showKeywordSelectionPanel(ref, name) {
    if (!keywordSelectionWindow) {
        var keywordSelectionPanel = new app.KeywordSelectionPanel({
            width: 620,
            height: 300,
            listeners: {
                keywordselected: function(panel, keywords) {
	        		var id = '_X' + this.ref + '_' + name.replace(":","COLON");
	        		var xml;
					var first = true;
	        		Ext.each(keywords, function(item, index) {
						// Format XML
	        			keywords[index] = item.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","")
							.replace(/\"/g,"&quot;").replace(/\r\n/g,"");
						if (first) {
							xml = keywords[index];
							first = false;
						} else
							xml += "&amp;&amp;&amp;" + keywords[index];
					});
					
					// Add XML fragments into main form.
					var input = {tag: 'input', type: 'hidden', id: id, name: id, value: xml};
					var dh = Ext.DomHelper;
					dh.append(Ext.get("hiddenFormElements"), input);
						
					// Save
					doAction('metadata.update');
                }
            }
        });

        keywordSelectionWindow = new Ext.Window({
            title: translate('keywordSelectionWindowTitle'),
            layout: 'fit',
            items: keywordSelectionPanel,
            closeAction: 'hide',
            constrain: true,
            iconCls: 'searchIcon'
        });
    }
    
    keywordSelectionWindow.items.get(0).setRef(ref);
    keywordSelectionWindow.show();
}



/**
 * Trigger validating event of an element.
 * 
 * @return
 */
function validateMetadataField(input) {
	// Process only onchange and onkeyup event having validate in event name.
	if (!input
			|| (input.onchange && input.readAttribute("onchange").indexOf(
					"validate") == -1)
			|| (input.onkeyup && input.readAttribute("onkeyup").indexOf(
					"validate") == -1))
		return;

	if (input.onkeyup)
		input.onkeyup();
	if (input.onchange)
		input.onchange();
}


/**
 * Multilingual widget is composed of one or more input or textarea (one for
 * each language) and on select list. If Google translation service is activated
 * then a suggestion div is also proposed (and clear on language selection).
 * 
 * Parameters:
 * 
 * @param node -
 *            {Object} node to enable
 * @param focus -
 *            {Object} focus
 */
function enableLocalInput(node, focus) {
	var ref = node.value;
	var parent = node.parentNode.parentNode;
	var nodes = parent.getElementsByTagName("input");
	var textarea = parent.getElementsByTagName("textarea");

	show(nodes, ref, focus);
	show(textarea, ref, focus);
};

function clearSuggestion(divSuggestion) {
	if ($(divSuggestion) != null)
		$(divSuggestion).innerHTML = "";
};

/**
 * 
 * Parameters:
 * 
 * @param node -
 *            {Object} node
 * @param ref -
 *            {String} ref
 * @param focus -
 *            {Object} focus
 */
function show(nodes, ref, focus) {
	for (index in nodes) {
		var input = nodes[index];
		if (input.style != null && input.style.display != "none")
			input.style.display = "none";
	}
	for (index in nodes) {
		var input = nodes[index];
		if (input.name == ref) {
			input.style.display = "block";
			if (focus)
				input.focus();
		}
	}
}

/**
 * Google translation API demo See:
 * http://code.google.com/apis/ajaxlanguage/documentation/
 * Google AJAX API Terms of Use http://code.google.com/apis/ajaxlanguage/terms.html
 * 
 * Translate a string using Google translation API.
 * 
 * Parameters:
 * 
 * @param ref -
 *            {String} ref identifier of source element (usually input box in
 *            default metadata language)
 * @param divSuggestion -
 *            {String} div element to suggest a translation. User need to do
 *            analyze and type the translation.
 * @param target -
 *            {String} target identifier of target element
 * @param fromLang -
 *            {String} fromLang language (default metadata language)
 * @param toLang -
 *            {String} toLang target language (current language selected in
 *            language list)
 */
function googleTranslate(ref, divSuggestion, target, fromLang, toLang) {
	// --- map language code to Google language code
	var map = {
		"GE" :"de",
		"SP" :"es",
		"CH" :"zh"
	};

	// --- map
	if (map[fromLang])
		fromLang = map[fromLang];
	if (map[toLang])
		toLang = map[toLang];

	// --- Check text to translate
	if ($(ref).value == "") {
		alert(translate("translateWithGoogle.emptyInput"));
		return;
	}
		
	if ($(ref).value.length > 5000) {
		// TODO : i18n
		alert(translate("translateWithGoogle.maxSize"));
		return;
	}

	if ($(divSuggestion) != null)
		$(divSuggestion).innerHTML = "";

	// --- translate
	google.language.translate($(ref).value, fromLang, toLang, function(result) {
		if (!result.error) {
			var suggestion = result.translation.replace(/&#39;/g, "'").replace(
					/&quot;/g, '"'); // FIXME : here we should take
			// care of other html entities ?
			if ($(target) != null)
				$(target).value = suggestion;
			if ($(divSuggestion) != null) {
				$(divSuggestion).innerHTML = suggestion;
				$(divSuggestion).style.display = "block";
			}
		} else {
			alert(result.error.message + " (" + result.error.code + ")");
			// Sometime 400 characters seems to be the limit.
		}

		validateMetadataField($(target));
	});
}

/**
 * Update upper cardinality in ISO 19110
 * depending on selected list value (0,1,n)
 * 
 * @param ref
 * @param value
 * @return
 */
function updateUpperCardinality(ref, value) {
    var isInf = ref + "_isInfinite";
        
    if (value == '0' || value == '1') {
        $(ref).value = value;
        $(isInf).value = 'false';
    } else if (value == 'n'){
        $(ref).value = '';
        $(isInf).value = 'true';
    } else {
        $(ref).value = '';
        $(isInf).value = 'false';
    }
}

