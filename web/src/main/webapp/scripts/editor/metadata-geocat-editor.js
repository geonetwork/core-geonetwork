/*
 * This class contains miscellaneous edit functions required by geocat but not geonetwork trunk
 *
 * It also has the entry point for searching for shared objects
 */
 
 
/**
 * Check that GM03 distance are between 0.00 .. 9999999999.99
 */
function validateGM03Distance(input, nullValue, noDecimals) {

    if (validateNumber(input, nullValue, noDecimals)) {

        var value = Number(input.value);
        if (value < 0.00 || value > 9999999999.99) {
            enableSave(false);
            input.addClassName('error');
            return false;
        } else {
            enableSave(true);
            input.removeClassName('error');
            return true;
        }
    } else
    return false;

}
/**
 * Validate Interlis NAME
 * (composed of letters and digits, starting with a letter, ili-Refmanual, p. 23)
 */
function validateGM03NAME(input) {
    var text = input.value.toUpperCase();
    var validDigits = "0123456789";
    var validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    var chars = validChars + validDigits;
    var valid = true;
    if (text.length > 0) {
        var firstChar = text.charAt(0);
        if (validChars.indexOf(firstChar) == -1)
        valid = false;
    }
    for (i = 0; i < text.length; i++) {
        character = text.charAt(i);
        if (chars.indexOf(character) == -1)
        valid = false;
    }

    if (!valid) {
        input.addClassName('error');
        return false;
    } else {
        input.removeClassName('error');
        return true;
    }
}

/**
 * XLink Editor functions 
 */
function XLink() {}
/**
 * XLink title
 */
XLink.prototype.title = null;
/**
 * XLink href
 */
XLink.prototype.href = null;

Event.observe(window, 'load',
function() {
    xl = new XLink();
    xlinks = [xl];
});

var mode = null;
var dialogRequest = {
    action: '',
    ref: '',
    name: '',
    id: '',
    replacement: '',
    max: 1000
};

function displayXLinkSearchBox(ref, name, action, id, replacement, max) {
    // Clean href element on each popup init in order
    // to avoid mix of elements.
    xl.href = null;
    xlinks = [xl];
    
    // store the variables of the request for use by the Create button
    dialogRequest.action = action;
    dialogRequest.ref = ref;
    dialogRequest.name = name;
    dialogRequest.id = id;
    dialogRequest.replacement = replacement;
    dialogRequest.max = max;

    if (geocat.edit.Keyword.accepts(name)) {
        geocat.edit.Keyword.openWindow(ref, name, id);
    } else if (geocat.edit.Contact.accepts(name)) {
        geocat.edit.Contact.openWindow(name);
    } else if (geocat.edit.Extent.accepts(name)) {
        geocat.edit.Extent.openWindow(name);
    } else if (geocat.edit.Format.accepts(name)) {
        geocat.edit.Format.openWindow(name);
    } else {
        alert(name+" is not a recognized shared elements");

    }
    document.mainForm.ref.value = ref;
    document.mainForm.name.value = name;
}

function updateValidationReportVisibleRules(errorOnly) {
    $('validationReport').descendants().each(function(el) {
        if (el.nodeName == 'LI') {
            if (el.getAttribute('name') == 'pass' && errorOnly) {
                el.style.display = "none";
            } else {
                el.style.display = "block";
            }
        }
    });
}

function displayValidationReportBox(boxTitle) {
    contentDivId = "validationReport";
    $(contentDivId).style.display = 'block';
    if (!validationReportBox) {
        validationReportBox = new Ext.Window({
            title: boxTitle,
            // TODO : translate
            id: "validationReportBox",
            layout: 'fit',
            modal: false,
            constrain: true,
            width: 400,
            collapsible: true,
            autoScroll: true,
            closeAction: 'hide',
            listeners: {
                hide: function() {
                    $(contentDivId).style.display = 'none';
                }
            },
            contentEl: contentDivId
        });
    }
    if (validationReportBox) {
        validationReportBox.show();
        validationReportBox.setHeight(345);
        validationReportBox.setWidth(Ext.get(contentDivId).getWidth());
        //validationReportBox.center();
        validationReportBox.anchorTo(Ext.getBody(), 'tr-tr');
        // Align top right
    }

}




/**
 * Adds map component in extent popup
 *
 */
//Event.observe(window, 'load', initMapComponent);
function initMapComponent() {
    var pop = $('popXLink');
    var popupDimensions = pop.getDimensions();
    pop.style.display = 'block';
    var mapCmp = new MapComponent('extent.map', {
        displayLayertree: false,
        resizablePanel: false,
        panelWidth: popupDimensions.width - 20,
        panelHeight: 250
    });
    drawCmp = new MapDrawComponent(mapCmp.map, {
        toolbar: mapCmp.toolbar,
        activate: true,
        controlOptions: {
            title: 'Draw shape',
            featureAdded: updateExtentAutocompleter
        },
        onClearFeatures: updateExtentAutocompleter
    });
    pop.style.display = 'none';
}
function updateExtentAutocompleter() {
    if (!drawCmp) return;
    var geom = drawCmp.writeFeature({
        format: 'WKT'
    });
    var geomParam = geom ? "&geom=" + geom: "";
    if (!window._acc) return;
    if (!window._extent_acc_initialUrl) _extent_acc_initialUrl = _acc.url;
    // Tweaks Scriptaculous Autocompleter url
    _acc.url = _extent_acc_initialUrl + '' + geomParam;
    // Fires autocompletion
    _acc.getUpdatedChoices();
}





/**
 * GeoNetwork searcher Class
 *
 *    Use in the editor to search into the catalogue
 *    Dev made in Geosource project.
 *    TODO : should use CSW search for consistency.
 */


var GNSearcher;


/**
 * Set default values for the existing element in html form and
 * copy all options into object properties.
 */
function GeoNetworkParams(options) {
    for (var option in options) {
        this.OPTIONS[option] = options[option];
        var el = document.getElementById(option);
        if (el)
        el.value = options[option];
    }
}

/**
 * List of search options.
 */
GeoNetworkParams.prototype.OPTIONS = {
    hitsPerPage: 300
};

/**
 * Create URL to do the search using current options according
 * to user inputs
 */
GeoNetworkParams.prototype.get = function() {
    var paramsUrl = "";

    for (var option in this.OPTIONS) {
        var el = document.getElementById(option);
        if (el)
        this.OPTIONS[option] = document.getElementById(option).value;

        paramsUrl += option + "=" + this.OPTIONS[option] + "&";
    }
    return paramsUrl;
};

/**
 * GeoNetwork searcher class
 */
function GeoNetworkSearcher(resultPanelId, params, popId) {
    this.params = new GeoNetworkParams(params);
    // TODO : add default values
    this.resultPanel = document.getElementById(resultPanelId);
    this.panel = document.getElementById(popId);
}

GeoNetworkSearcher.prototype.params = null;
GeoNetworkSearcher.prototype.resultPanel = null;
GeoNetworkSearcher.prototype.searchPanel = null;
GeoNetworkSearcher.prototype.panel = null;
GeoNetworkSearcher.prototype.target = null;
GeoNetworkSearcher.prototype.DEFAULT_PARAMS = {
    method: "GET",
    service: "main.search.embedded"
};
GeoNetworkSearcher.prototype.req = null;

/**
 *
 */
GeoNetworkSearcher.prototype.search = function() {
    this.reset();
    this.req = new Ajax.Request(
    this.DEFAULT_PARAMS.service,
    {
        method: this.DEFAULT_PARAMS.method,
        parameters: this.params.get(),
        onSuccess: this.present.bind(this),
        onFailure: this.error.bind(this)
    }
    );
};

/**
 * Display results
 */
GeoNetworkSearcher.prototype.present = function(req) {
    this.resultPanel.innerHTML = req.responseText;
};

/**
 * Update target element,
 * and clean results.
 */
GeoNetworkSearcher.prototype.updateTarget = function(value) {
    document.getElementById(this.target).value = value;
    this.reset();
};

/**
 * Clean result and search form
 */
GeoNetworkSearcher.prototype.reset = function() {
    this.resultPanel.innerHTML = "";
};

/**
 * On error
 */
GeoNetworkSearcher.prototype.error = function() {
    alert("Error");
};

/* -----------   End XLink functions  ------------- */

/* -----------   Start Visibility functions  ------------- */
var DEFAULT_VISIBILITY_ARR = ['no', 'all'];

function toggleVisibilityEdit()
{
	// toggles visibility of element visibility edit icons
	$$('a.elementHiding').invoke('toggle');
}

function changeVisibility(ref, val)
{
	if (!ref)
	{
		alert('No ref id for this element, (sorry, this needs to be fixed)');
		return;
	}

	// Ref is a number: get input element
	var elVisibility = $('hide_' + ref);
	if (!elVisibility)
	{
		return;
	}

	// Determine new visibility
	var visibility;
	if (val)
	{
		// Value was passed (happens for recursive calls from parent)
		visibility = val;
	} else
	{
		// Visibility is determined by rotating visibility values

		// Default rotation of visibility values
		var visibilityArr = DEFAULT_VISIBILITY_ARR;

		// Restrict rotation on parent visibility value
		// i.e. the child can never be less restrictive
		var parents = $w(elVisibility.className);
		if (parents && parents.length > 0)
		{
			var parentRef = parents[0].sub('parent_', '', 1);
			if (parentRef)
			{
				var parentElm = $('hide_' + parentRef);
				if (parentElm)
				{
					var parentVal = parentElm.value;
					switch (parentVal)
					{
						case 'all':
							visibilityArr = ['all'];
							break;
						case 'no':
							// Use default rotation
							break;
					}
				}
			}
		}

		// rotate visibility
		var i=0;
		for (; i < visibilityArr.length; i++) {
			if (visibilityArr[i] == elVisibility.value) {
				break;
			}
		}

		// Determine new visibility by rotation
		visibility = visibilityArr[(i+1) % visibilityArr.length];
	}

	// Set in input form element
	elVisibility.value = visibility;

	// Change icon
	setVisibilityIcon(ref, visibility);

	// Now also propagate visibility to all descendents recursively
	var children = $$('input.parent_' + ref);
	children.each(function(inputElm)
	{
		changeVisibility(inputElm.id.sub('hide_', '', 1), visibility);
	});
}

function setVisibilityIcon(ref, visibility)
{
	var icon = $(ref + '_visibility_icon');
	if (!icon)
	{
		return;
	}
	var baseURL = Env.url + '/images/';

	switch (visibility)
			{
		case 'all':
			icon.setAttribute("src", baseURL + 'red-ball.gif');
			break;
		case 'no':
			icon.setAttribute("src", baseURL + 'green-ball.gif');
			break;
	}
}

/* -----------   End Visibility functions  ------------- */

/*****************************
***
***		Metadata for Services
***
******************************/

function enableCreateAsso(enable) {
	if (enable) {
		$('createAsso').disabled = false;
		$('createAssoCoupledResource').disabled = false;
	} else {
		var inputs = $('catResults').getElementsByTagName('input');
		var nbchecked = 0;
		for (var i=0; i < inputs.length; i++) {
		    if (inputs[i].checked) {
		    	nbchecked++;
		    }
		}
		if (nbchecked == 0) {
			$('createAsso').disabled = true ;
			$('createAssoCoupledResource').disabled = true ;
		}
	}
}

function updateCoupledResourceforServices() {
	// Get ModalBox values
	var ids = '';
	var inputs = $('catResults').getElementsByTagName('input');
	for (var i=0; i < inputs.length; i++) {
	    var input1 = inputs[i];
	    if (input1.checked) {
    		ids = input1.value;
	    }
	}
	var scopedName = $('scopedName').value;

	// update values in edit form.
	var input = document.getElementById('datasetIds');
	input.value = ids;

	var srvScopedName = document.getElementById('srvScopedName');
	srvScopedName.value = scopedName;

	doAction(Env.locService+'/metadata.services.attachDataset');
}

function updateMDforServices() {
    var updateMddCheckbox = $('updateMDD').checked;
    var scopedName = $('scopedName').value;

    if (!updateMddCheckbox) {
        // Check the services. If any it's not allowed to the user, shows an alert
        var edits = $$('#catResults input[type=hidden]');
        var servicesAllowed = true;
        var services = '';

        for (var i=0; i < edits.length; i++) {
            var edit = edits[i];
            var related_check = $(edit.id.replace("_edit", ""));

            if ((edit.value == 'false') && (related_check.checked)) {
                    Ext.MessageBox.alert(updateDatasetTitle, updateDatasetMsg);
                    servicesAllowed = false;
                    break;
            }
        }

        if (!servicesAllowed) return;
    }
    
	// Get ModalBox values
	var ids = '';
	var inputs = $('catResults').getElementsByTagName('input');
	var first = true;
	for (var i=0; i < inputs.length; i++) {
	    var input = inputs[i];

	    if (input.checked) {
            var edit = $(input.id + "_edit");
                if (first) {
                    ids = input.value;
                    first = false;
                } else
                    ids = ids + ','+input.value;
	    }
	}
    
	// update values in edit form.
	var srvInput = document.getElementById('srvIds');
	srvInput.value = ids;

	var updateMDD = document.getElementById('upMdd');
	updateMDD.value = updateMddCheckbox;

	var srvScopedName = document.getElementById('srvScopedName');
	srvScopedName.value = scopedName;

	doAction(Env.locService+'/metadata.update.onlineSrc');
}

function doProtocolChange(ref) {
	var selectObj = Ext.get('s_'+ref);
	var inputObj = Ext.get('_'+ref);
	var selection = selectObj.dom.value;
	if(selection == translate('other')) {
		inputObj.setStyle("display","block");
		selectObj.dom.name = 'disabled_'+ref;
	} else {
		inputObj.setStyle("display","none");
		selectObj.dom.name = 's_'+ref;
	}
}