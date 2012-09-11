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
Ext.namespace('GeoNetwork.editor');

GeoNetwork.editor.EditorTools = {
    
    /**
     * Add an hidden form field to add an XML fragment
     * using the hidden div with id "hiddenFormElements"
     * provided by the editor.
     * 
     * Trigger a save action on the editorPanel if defined.
     * 
     * @param {Object} panel
     * @param {Array} xmlFragments  Array of XML fragments
     * @param {Object} editorPanel  The editor panel
     */
    addHiddenFormFieldForFragment: function(panel, xmlFragments, editorPanel){
        var id = '_X' + panel.ref + '_' + panel.name.replace(":", "COLON");
        var xml;
        var first = true;
        Ext.each(xmlFragments, function(item, index){
            // Format XML
            xmlFragments[index] = item.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "")
                                    .replace(/\&/g, "&amp;amp;")
                                    .replace(/\"/g, "&quot;")
                                    .replace(/\r\n/g, "");
            if (first) {
                xml = xmlFragments[index];
                first = false;
            } else {
                xml += "&amp;&amp;&amp;" + xmlFragments[index];
            }
        });
        
        // Create the form field and add it to the main form.
        GeoNetwork.editor.EditorTools.addHiddenFormField(id, xml);
        
        if (editorPanel) {
            editorPanel.save();
        }
    },
    /**
     * Create the form field and add it to the main form.
     * 
     * @param {Object} fieldId
     * @param {Object} fieldValue
     */
    addHiddenFormField: function(fieldId, fieldValue){
        var input = {
            tag: 'input',
            type: 'hidden',
            id: fieldId,
            name: fieldId,
            value: fieldValue
        };
        var dh = Ext.DomHelper;
        dh.append(Ext.get("hiddenFormElements"), input);
    },
    /**
     * Show and create a new metadata window for the scope element.
     * 
     * @param {Object} scope
     * @param {Object} id
     * @param {Object} title
     * @param {Object} isChild
     * @param {Object} filter
     */
    showNewMetadataWindow: function(scope, id, title, isChild, filter){
        // Destroy all previously created windows which may
        // have been altered by save/check editor action.
        if (scope.newMetadataWindow) {
            scope.newMetadataWindow.close();
            scope.newMetadataWindow = undefined;
        }
        
        // Create a window to choose the template and the group
        if (!scope.newMetadataWindow) {
        	var newMetadataPanel = new GeoNetwork.editor.NewMetadataPanel({
                selectedTpl: id,
                isChild: isChild,
                filter: filter,
                getGroupUrl: scope.catalogue.services.getGroups,
                catalogue: scope.catalogue
            });
            
            scope.newMetadataWindow = new Ext.Window({
                title: title === undefined ? OpenLayers.i18n('newMetadata') : title,
                width: 300,
                height: 100,
                layout: 'fit',
                modal: true,
                items: newMetadataPanel,
                closeAction: 'destroy',
                constrain: true,
                iconCls: 'addIcon'
            });
        }
        scope.newMetadataWindow.show();
    }
};

lastclick = 0;
Checks = {
    message: translate("loseYourChange"),
    _setMessage: function(str){
        this.message = str;
    },
    
    _onbeforeunload: function(e){
        // TODO : lock metadata in opener ?
        // if (opener) {
        // editRed = opener.$$('.editing');
        // if (editRed && editRed.length > 0) {
        // editRed.invoke('removeClassName', 'editing');
        // }
        // e.returnValue = this.message;
        // }
    }
};
//var bfu = Checks._onbeforeunload.bindAsEventListener(Checks);

function unloadMess(){
    mess = translate("loseYourChange");
    return mess;
}

function doEditorAlert(divId, imgId) {
    $(divId).style.display='block';
}

function setBunload(on){
    if (on) {
  //      Event.observe(window, 'beforeunload', bfu);
        Checks._setMessage(unloadMess());
    } else {
  //      Event.stopObserving(window, 'beforeunload', bfu);
        Checks._setMessage(null);
    }
}

/**
 *  Stop double clicks on metadata element controls
 */
function noDoubleClick(){
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
 * Form function
 * ================
 *
 */
function doNewElementAction(action, ref, name, id, what, max){
    var child = null;
    var orElement = false;
    doNewElementAjax(action, ref, name, child, id, what, max, orElement);
}

function doNewORElementAction(action, ref, name, child, id, what, max){
    var orElement = true;
    doNewElementAjax(action, ref, name, child, id, what, max, orElement);
}

function doNewAttributeAction(action, ref, name, id, what){
    var child = "geonet:attribute";
    var max = null;
    var orElement = false;
    doNewElementAjax(action, ref, name, child, id, what, max, orElement);
}

function doNewElementAjax(action, ref, name, child, id, what, max, orElement){
    var metadataId = document.mainForm.id.value;
    var pars = "&id=" + metadataId + "&ref=" + ref + "&name=" + name;
    if (child != null) pars += "&child=" + child;
    var thisElement = Ext.get(id);
    Ext.Ajax.request({
        url: catalogue.services.rootUrl + action,
        method: 'GET',
        params: pars,
        success: function(result, request){
            var html = result.responseText;
            
            if (child == "geonet:attribute") { // Need #122 to be fixed
                Ext.getCmp('editorPanel').save();
                return;
            }
            if (what == 'replace') {
                Ext.DomHelper.insertHtml('afterEnd', thisElement.dom, html);
                thisElement.remove();
                //thisElement.update(html);
            } else if (what == 'add' || what == 'after') {
                var el = thisElement.insertHtml('afterEnd', html, true);
                setAddControls(el, orElement);
            } else if (what == 'before') { // only for orElement = true
                // An or element in flat mode may be empty
                var el = thisElement.insertHtml('beforeBegin', html, true);
                setAddControls(el, orElement);
            } else {
                Ext.MessageBox.alert("doNewElementAjax: invalid what: " + what +
                " should be one of replace, after or before.");
            }
            
            Ext.getCmp('editorPanel').metadataLoaded();
            
            setBunload(true); // reset warning for window destroy
        },
        onFailure: function(req){
            Ext.MessageBox.alert(translate("errorAddElement") + name +
            translate("errorFromDoc") +
            " / status " +
            req.status +
            " text: " +
            req.statusText +
            " - " +
            translate("tryAgain"));
            setBunload(true); // reset warning for window destroy
        }
    });
}

function doRemoveElementAction(action, ref, parentref, id, min){
    var metadataId = document.mainForm.id.value;
    var thisElement = Ext.get(id);
    var nextElement = thisElement.next();
    var prevElement = thisElement.prev();
    Ext.Ajax.request({
        url: catalogue.services.rootUrl + action, // TODO : catalogue.url
        method: 'GET',
        params: {
            id: metadataId,
            ref: ref,
            parent: parentref
        },
        success: function(result, request){
            var html = result.responseText;
            if (html == "") { // more than one left, no child-placeholder
                // returned
                // in simple mode, returned snippets will be empty in all cases
                // because a geonet:child alone is not take into account.
                // No elements are suggested then and last element is removed.
                var originalElementType = id.split("_")[0];
                var prevElementType = ((prevElement == null)?"":prevElement.id.split("_")[0]);
                var nextElementType = ((nextElement == null)?"":nextElement.id.split("_")[0]);

                if (bottomElement(thisElement)) {
                    var doSwapControls = true;

                    if (document.mainForm.currTab.value === 'simple') {
                        // only swap if are the same element type in simple view
                        doSwapControls = (originalElementType === prevElementType);
                    }

                    if (doSwapControls) {
                        swapControls(thisElement, prevElement);
                        thisElement.remove();
                        thisElement = prevElement;

                        // only call topControls if same type for originalElement and prevElement
                        if (originalElementType === prevElementType) {
                            if (topElement(thisElement)) topControls(thisElement, min);
                        }
                    } else {
                        thisElement.remove();
                        thisElement = nextElement;

                        // only call topControls if same type for originalElement and nextElement
                        if (originalElementType === nextElementType) {
                            if (topElement(thisElement)) topControls(thisElement, min);
                        }
                    }

                } else {
                    thisElement.remove();
                    thisElement = nextElement;

                    // only  call topControls if same type for originalElement and nextElement
                    if (originalElementType === nextElementType) {
                        if (topElement(thisElement)) topControls(thisElement, min);
                    }
                }
              
            } else { // last one, so replace with child-placeholder returned
                if (orElement(thisElement)) {
                    thisElement.remove();
                } else {
                    Ext.DomHelper.insertHtml('afterEnd', thisElement.dom, html);
                    thisElement.remove();
                }
            }
            setBunload(true); // reset warning for window destroy
        },
        failure: function(result, request){
            Ext.MessageBox.alert(translate("errorDeleteElement") + name + " " +
            translate("errorFromDoc") +
            " / status " +
            result.status +
            " text: " +
            result.statusText +
            " - " +
            translate("tryAgain"));
            setBunload(true); // reset warning for window destroy
        }
    });
}

function doRemoveAttributeAction(action, ref, parentref)
{
	var metadataId = document.mainForm.id.value;
	var thisElement = Ext.get(ref + '_block');
	Ext.Ajax.request({
		url: catalogue.services.rootUrl + action,
		method: 'GET',
		params: {id:metadataId, ref:ref},
		success: function(result, request) {
			var html = result.responseText;
			//TODO replace element by result. Need #122 to be fixed thisElement.remove();
			var editor = Ext.getCmp('editorPanel');
			editor.save();
			setBunload(true); // reset warning for window destroy
		},
		failure:function (result, request) { 
			Ext.MessageBox.alert(translate("errorDeleteAttribute") + name + " " + translate("errorFromDoc") 
						+ " / status " + result.status + " text: " + result.statusText + " - " + translate("tryAgain"));
			setBunload(true); // reset warning for window destroy
		}
	});
}

function updateEditorContent(result, request){
    var editor = Ext.getCmp('editorPanel');
    editor.updateEditor(result.responseText);
    setBunload(true); // reset warning for window destroy
}



/**
 * Launch metadata processing
 * @return
 */
function doProcessing(config, cb){
    config.id = document.mainForm.id.value;
    config.remote = 'true';
    Ext.Ajax.request({
        url: catalogue.services.rootUrl + 'metadata.processing', // TODO : catalogue.url
        method: 'POST',
        params: config,
        success: function(result, request){
            updateEditorContent(result, request);
            if (cb) {
                cb();
            }
        },
        failure: function(req){
            Ext.MessageBox.alert(translate("errorSaveFailed") + "/ status " +
            req.status +
            " text: " +
            req.statusText +
            " - " +
            translate("tryAgain"));
            setBunload(true); // reset warning for window destroy
        }
    });
}


/**
 * Edition action
 * ==============
 */
function doMoveElementAction(action, ref, id){
    var thisElement = Ext.get(id);
    var config = {
        id: document.mainForm.id.value,
        ref: ref
    };
    
    Ext.Ajax.request({
        url: catalogue.services.rootUrl + action,
        method: 'POST',
        params: config,
        success: function(req){
            if (action.indexOf('elem.up') != -1) {
                var upElement = thisElement.prev();
                thisElement.insertSibling(upElement, 'after');
                swapControls(thisElement, upElement);
            } else {
                var downElement = thisElement.next();
                thisElement.insertSibling(downElement, 'before');
                swapControls(thisElement, downElement);
            }
            setBunload(true); // reset warning for window destroy
        },
        failure: function(req){
            Ext.MessageBox.alert(translate("errorMoveElement") + ref + " / status " + req.status +
            " text: " +
            req.statusText +
            " - " +
            translate("tryAgain"));
            setBunload(true); // reset warning for window destroy
        }
    });
}


/**
 * Use to remove file attached to a metadata record.
 *
 * @param action
 * @param ref
 * @param access
 * @param id
 */
function doFileRemoveAction(action, ref, access, id){
    // remove action passes ref via ajax to file remove service for update
    // FIXME : var top = findPos($(id));
    setBunload(false);
    document.mainForm.access.value = access;
    document.mainForm.ref.value = ref;
    //document.mainForm.position.value = top;
    document.mainForm.action = action;
    Ext.getCmp('editorPanel').callAction(action);
}


/**
 * Called when protocol select field changes in metadata form.
 * Protocol could not be changed if file is already uploaded.
 */
function checkForFileUpload(fref, pref, protocolBeforeEdit){
    var fileName = Ext.getDom('_' + fref); // the file name input field
    var protoSelect = Ext.getDom('s_' + pref); // the protocol <select>
    var protoIn = Ext.getDom('_' + pref); // the protocol input field to be submitted
    var fileUploaded = OpenLayers.String.startsWith(protocolBeforeEdit, 'WWW:DOWNLOAD'); // File name not displayed in editor if downloaded
    var protocol = protoSelect.value;
    var protocolDownload = (OpenLayers.String.startsWith(protocol, 'WWW:DOWNLOAD') && protocol.indexOf('http') > 0);
    
    // don't let anyone change the protocol if a file has already been uploaded 
    // unless its between downloaddata and downloadother
    if (fileUploaded) {
        if (!protocolDownload) {
            alert(OpenLayers.i18n("errorChangeProtocol")); 
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
    var finput = Ext.get('di_' + fref);
    var fbuttn = Ext.get('db_' + fref);
    
    // protocol change is ok so set the protocol value to that selected
    protoIn.value = protoSelect.value;

    if (protocolDownload) {
        if (finput !== null) {
            finput.hide();
        }
        if (fbuttn !== null) {
            fbuttn.show();
        }
        
        // Reload the editor in order to make the import file button
        // appear for the name element (if existing).
        Ext.getCmp('editorPanel').callAction('metadata.update.new');
    } else {
        if (finput !== null) {
            finput.show();
        }
        if (fbuttn !== null) {
            fbuttn.hide();
        }
    }
}

function handleCheckboxAsBoolean (input, ref) {
    if (input.checked) {
        Ext.getDom(ref).value='true';
    } else {
        Ext.getDom(ref).value='false';
    }
}

/**
 * Swap 2 controls when using up/down button or when removing elements
 *
 * @param el1
 * @param el2
 */
function swapControls(el1, el2){
    var el1Descs = getControlsFromElement(el1);
    var el2Descs = getControlsFromElement(el2);
    
    for (var index = 0; index < el1Descs.length; ++index) {
        var visible1 = el1Descs[index].isVisible();
        var visible2 = el2Descs[index] ? el2Descs[index].isVisible() : false;
        if (el2Descs[index]) {
            if (visible1) {
                el2Descs[index].show();
            } else {
                el2Descs[index].hide();
            }
        }
        if (visible2) {
            el1Descs[index].show();
        } else {
            el1Descs[index].hide();
       }
    }
}

/**
 * Return all controls for the element. Controls
 * is a <span class="buttons"> with <a/> for each controls.
 * Set visibility mode to use display by default.
 *
 * @param el
 * @returns	All controls
 */
function getControlsFromElement(el){
    if (el === null) {
        return [];
    }
    
    var id = el.getAttribute('id');
    var controls = Ext.get('buttons_' + id).query('a');
    for (var i = 0; i < controls.length; i++) {
        controls[i] = Ext.get(controls[i]).setVisibilityMode(Ext.Element.DISPLAY);
    }
    
    return controls;
}

function topElement(el){
    if (el.prev() == undefined) {
        return true;
    } else {
        return (!isSameElement(el.prev(), el));
    }
}

function bottomElement(el){
    if (el.next() == undefined) 
        return true;
    else 
        return (!isSameElement(el.next(), el));
}

function getIdSplit(el){
    var id = el.getAttribute('id');
    if (id == null)         
        return null;
    return id.split("_");
}

function orElement(el){
    if (el.next() == undefined) 
        return false;
    else {
        var nextEl = getIdSplit(el.next());
        var thisEl = getIdSplit(el);
        if (nextEl == null || thisEl == null)             
            return false;
        if (nextEl[0] == "child" && nextEl[1] == thisEl[0]) 
            return true;
        else 
            return false;
    }
}

function isSameElement(el1, el2){
    var i1 = getIdSplit(el1);
    var i2 = getIdSplit(el2);
    if (i1 == null || i2 == null)         
        return false;
    if (i1[0] == i2[0]) 
        return true;
    else 
        return false;
}

/**
 * Display control after delete
 *
 * @param el
 * @param min
 */
function topControls(el, min){
    var elDescs = getControlsFromElement(el);
    
    
    // Check addXmlFragment control
    var index = 0;
    if (elDescs.length == 5) index = 1;
    
    // sort out +
    if (bottomElement(el) && !orElement(el)) 
        elDescs[0].show();
    else 
        elDescs[0].hide();
    
    // sort out +/x (addXmlFragment)
    if (index == 1) {
        if (bottomElement(el) && !orElement(el)) 
            elDescs[index].show();
        else 
            elDescs[index].hide();
    }
    
    // sort out x
    if (bottomElement(el)) {
        if (min == 0) 
            elDescs[1 + index].show();
        else 
            elDescs[1 + index].hide();
    } else 
        elDescs[1 + index].show();
    
    // sort out ^
    elDescs[2 + index].hide();
    
    // sort out v
    if (bottomElement(el)) 
        elDescs[3 + index].hide();
    else 
        elDescs[3 + index].show();
}

/**
 * Display control after add
 *
 * Looks like topControls (merge ?)
 *
 * @param el
 * @param orElement
 */
function setAddControls(el, orElement){
    elDescs = getControlsFromElement(el);
    //	console.log(elDescs);
    
    // Check addXmlFragment control
    var index = 0;
    if (elDescs.length == 5) index = 1;
    
    // + x ^ _ or _ x ^ _ is default on add
    if (orElement) 
        elDescs[0].hide();
    else 
        elDescs[0].show();
    
    if (index == 1) {
        if (orElement) 
            elDescs[index].hide();
        else 
            elDescs[index].show();
    }
    elDescs[1 + index].show();
    elDescs[2 + index].show();
    if (elDescs[3 + index]) {
        elDescs[3 + index].hide();
    }
    
    // special cases - if this is topElement - need + x _ _ or _ x _ _
    if (topElement(el)) {
        elDescs[2 + index].hide();
    } else { // otherwise fix up previous element
        var prevEl = el.prev();
        var prevDescs = getControlsFromElement(prevEl);
        //		console.log(prevDescs);
        var prevIndex = 0;
        if (prevDescs.length == 5) prevIndex = 1;
        prevDescs[0].hide();
        if (prevIndex == 1) prevDescs[prevIndex].hide();
        prevDescs[1 + prevIndex].show();
        if (topElement(prevEl)) 
            prevDescs[2 + prevIndex].hide();
        else 
            prevDescs[2 + prevIndex].show();
        
        var el = prevDescs[3 + prevIndex];
        if (el) {
        	el.show();
        }
    }
}

/**
 * Validation function
 * ===================
 *
 */
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
function validateNumber(input, nullValue, decimals){
    var text = input.value;
    var validChars = "0123456789";
    
    if (!nullValue) 
if (!validateNonEmpty(input))         
        return false;
    
    if (decimals) validChars += '.';
    
    var isNumber = true;
    var c;
    
    for (i = 0; i < text.length && isNumber; i++) {
        c = text.charAt(i);
        if (c == '-' || c == "+") {
            if (i < 0) isNumber = false;
        } else if (validChars.indexOf(c) == -1) {
            isNumber = false;
        }
    }
    if (!isNumber) {
        Ext.get(input).addClass('error');
        return false;
    } else {
        Ext.get(input).removeClass('error');
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
function validateNonEmpty(input){
    if (input.value.length < 1) {
        Ext.get(input).addClass('error');
        return false;
    } else {
        Ext.get(input).removeClass('error');
        return true;
    }
}

/**
 * Validate email input form element.
 * If invalid, set the class attribute to "error".
 *
 * Parameters:
 * input - {Object} Form element
 */
function validateEmail(input){
    if (!Ext.form.VTypes.email(input.value)) {
        Ext.get(input).addClass('error');
        return false;
    } else {
        Ext.get(input).removeClass('error');
        return true;
    }
}

/**
 * Toggle visibility of an element
 * updating button icon.
 *
 * @param btn  The button with downBt or rightBt css class icon
 * @param elem The element to display or not
 */
function toggleFieldset(btn, elem){
    var b = Ext.get(btn);
    if (b.hasClass('tgDown')) {
        b.removeClass('tgDown');
        elem.style.display = 'none';
        b.addClass('tgRight');
    } else {
        b.removeClass('tgRight');
        elem.style.display = 'block';
        b.addClass('tgDown');
    }
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
function enableLocalInput(node, focus){
    var ref = node.value;
    var parent = node.parentNode.parentNode;
    var nodes = parent.getElementsByTagName("input");
    var textarea = parent.getElementsByTagName("textarea");
    
    showLocalInput(nodes, ref, focus);
    showLocalInput(textarea, ref, focus);
}


/**
 * Display input according to selected ref and set focus to input.
 * Parameters:
 *
 * @param node -
 *            {Object} node
 * @param ref -
 *            {String} ref
 * @param focus -
 *            {Object} focus
 */
function showLocalInput(nodes, ref, focus){
    for (index in nodes) {
        var input = nodes[index];
        if (input.style != null && input.style.display != "none") input.style.display = "none";
    }
    for (index in nodes) {
        var input = nodes[index];
        if (input.name == ref) {
            input.style.display = "block";
            if (focus) input.focus();
        }
    }
}

/**
 * Remove suggestion div content. Call by multilingual widget.
 * Suggestion should be improved in such case (see #325).
 *
 * @param divSuggestion
 */
function clearSuggestion(divSuggestion){
    if ($(divSuggestion) != null) $(divSuggestion).innerHTML = "";
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
function googleTranslate(ref, divSuggestion, target, fromLang, toLang){
    // --- map language code to Google language code
    // See http://code.google.com/apis/ajaxlanguage/documentation/reference.html#_intro_fonje 
    // for google code list
    var map = {
        "GE": "de",
        "SP": "es",
        "CH": "zh"
    };
    
    // --- map
    if (map[fromLang]) fromLang = map[fromLang];
    if (map[toLang]) toLang = map[toLang];
    
    // --- Check text to translate
    if ($(ref).value == "") {
        Ext.Msg.alert(translate("translateWithGoogle.emptyInput"));
        return;
    }
    
    if ($(ref).value.length > 5000) {
        // TODO : i18n
        Ext.Msg.alert(translate("translateWithGoogle.maxSize"));
        return;
    }
    
    if ($(divSuggestion) != null) $(divSuggestion).innerHTML = "";
    
    // --- translate
    try {
        google.language.translate($(ref).value, fromLang, toLang, function(result){
            if (!result.error) {
                var suggestion = result.translation.replace(/&#39;/g, "'").replace(/&quot;/g, '"'); // FIXME : here we should take
                // care of other html entities ?
                if ($(target) != null) $(target).value = suggestion;
                if ($(divSuggestion) != null) {
                    $(divSuggestion).innerHTML = suggestion;
                    $(divSuggestion).style.display = "block";
                }
            } else {
                Ext.Msg.alert(result.error.message + " (" + result.error.code + ")");
                // Sometime 400 characters seems to be the limit.
            }
            
            validateMetadataField($(target));
        });
    } 
    catch (err) {
        Ext.Msg.alert('Translation service error', 'Check google translation API is loaded: ' + err);
    }
}


/**
 * Run from children update from the related resource panel
 */
function updateChildren(div, url, onFailureMsg) {
    // This depends a lot on the current document content !
    var pars = "&id=" + document.mainForm.id.value 
                + "&parentUuid=" + Ext.getDom('parentUuid').value
                + "&schema=" + Ext.getDom('schema').value 
                + "&childrenIds=" + Ext.getDom('childrenIds').value;
    
    // handle checkbox values - section information
    var boxes = Ext.get(div).select('input[type="checkbox"]');
    boxes.each( function(s) {
        var e = Ext.getDom(s);
        if (e.checked) {
            pars += "&" + e.name + "=true";
        }
    });
    
    // handle radio value - replace or add
    var radios = Ext.get(div).select('input[type="radio"]');
    radios.each ( function(radio) {
        var e = Ext.getDom(radio);
        if(radio.checked) {
            pars += "&" + e.name + "=" + e.value;
        }
    });
    
    Ext.Ajax.request ({
        url: catalogue.services.rootUrl + url,
        method: 'GET',
        params: pars,
        success: function(result, request) {
            var xmlNode = result.responseXML;
            if (xmlNode.childNodes.length !== 0 &&
            xmlNode.childNodes[0].localName == "response") {
                var response = xmlNode.childNodes[0].childNodes[0].nodeValue;
                Ext.MessageBox.alert(OpenLayers.i18n('updateChildren'), response);
                Ext.get(div).hide();
            } else {
                Ext.MessageBox.alert(OpenLayers.i18n('updateChildren'), onFailureMsg);
            }
        },
        failure: function (result, request) { 
            Ext.MessageBox.alert(OpenLayers.i18n('updateChildren'), onFailureMsg);
        }
    });
}

