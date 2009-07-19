
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
	message : "If you press OK you will LOSE any changes you've made to the metadata!",
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
  mess = "If you press OK you will LOSE any changes you've made to the metadata!"; // TODO
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
	// sort out +
	if (bottomElement(el) && !orElement(el)) elDescs[0].show();
	else elDescs[0].hide();

	// sort out x
	if (bottomElement(el)) {
		if (min == 0) elDescs[1].show();
		else elDescs[1].hide();
	} else elDescs[1].show();

	// sort out ^
	elDescs[2].hide();

	// sort out v
	if (bottomElement(el)) elDescs[3].hide();
	else elDescs[3].show();
}

function doRemoveElementAction(action, ref, parentref, id, min)
{
	var metadataId = document.mainForm.id.value;
	var pars = "&id="+metadataId+"&ref="+ref+"&parent="+parentref;
	var thisElement = $(id);
	var nextElement = thisElement.next();
	var prevElement = thisElement.previous();

	var myAjax = new Ajax.Request(
		getGNServiceURL(action),
		{
			method: 'get',
			parameters: pars,
			onSuccess: function(req) {
				var html = req.responseText;
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
			onFailure: function(req) { 
				alert("ERROR: Could not delete element "+name+" from document: status "+req.status+" text: "+req.statusText+" - Try again later?");
				setBunload(true); // reset warning for window destroy
			}
		}
	);
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
				alert("ERROR: Could not move element "+ref+": status "+req.status+" text: "+req.statusText+" - Try again later?");
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
	// + x ^ _ or _ x ^ _ is default on add
	if (orElement) elDescs[0].hide();
	else elDescs[0].show();
	elDescs[1].show();
	elDescs[2].show();
	elDescs[3].hide();

	// special cases - if this is topElement - need + x _ _ or _ x _ _
	if (topElement(el)) {
		elDescs[2].hide();
	} else { // otherwise fix up previous element
		var prevEl = el.previous();
		var prevDescs = getControlsFromElement(prevEl);
		prevDescs[0].hide();
		prevDescs[1].show();
		if (topElement(prevEl)) prevDescs[2].hide();
		else prevDescs[2].show();
		prevDescs[3].show();
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
					alert("doNewElementAjax: invalid what: "+what+" should be one of replace, after or before");
				}
				
				// Check elements
				validateMetadataFields();
				
				setBunload(true); // reset warning for window destroy
			},
			onFailure: function(req) { 
				alert("ERROR: Could not add element "+name+" to document: status "+req.status+" text: "+req.statusText+" - Try again later?");
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
					alert("ERROR: Could not save form: status "+req.status+" text: "+req.statusText+" - Try again later?");
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
					alert("ERROR: Could not save form: status "+req.status+" text: "+req.statusText+" - Try again later?");
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
				alert("ERROR: Could not do action "+action+" : status "+req.status+" text: "+req.statusText+" - Try again later?");
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
			alert('A file may have been uploaded. You cannot change the protocol until you remove that file'); // TODO
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
	Modalbox.show(getGNServiceURL('resources.prepare.upload') + "?ref=" + fref + "&id=" + id, {title: 'File Upload', height: 200, width: 600});
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
		alert("Browse for, or enter a file name before pressing upload!");
		return false;
	}

	AIM.submit(form, 
		{ 'onStart': function() {
				Modalbox.deactivate();
			},
			'onComplete': function(doc) {
				Modalbox.activate();
				if (doc.body == null) { // error - upload failed for some reason
					alert("Error: Upload failed! - returned "+doc);
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
						alert("Error: Upload succeeded but unable to set filename!");
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

function setRegion(westField, eastField, southField, northField, choice)
{
		
	if (choice != "") {
		coords = choice.split(";")
		westField.value  = coords[0];
		eastField.value  = coords[1];
		southField.value = coords[2];
		northField.value = coords[3];
	} else {
		westField.value  = "";
		eastField.value  = "";
		southField.value = "";
		northField.value = "";
	}
	
	westField.onkeyup();
	eastField.onkeyup();
	southField.onkeyup();
	northField.onkeyup();
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
 * Retrieve all page's input and textarea element
 * and check the onkeyup and onchange event (Usually used to check
 * user entry. @see validateNonEmpty and validateNumber).
 * 
 * @return
 */
function validateMetadataFields() {
    $$('input,textarea,select').each(function(input) {
    	// Process only onchange and onkeyup event having validate in event name.
    	if ((input.onchange && input.readAttribute("onchange").indexOf("validate") == -1) ||
    			(input.onkeyup && input.readAttribute("onkeyup").indexOf("validate") == -1))
    		return;
    	
        if (input.onkeyup) input.onkeyup();
        if (input.onchange) input.onchange();
    });
}
