function createNewExtent() {
    var format = Ext.get('extent.format').getValue();
    var inclusion = Ext.get('extent.type.code').getValue();

    xlinks[0].href = 'local://xml.extent.get?wfs=default&typename=gn:non_validated&id=createNewExtent&format=' + format + '&extentTypeCode=' + inclusion;

    submitXLink();
}

function submitXLink() {
    if (xlinks.length == 0 || xlinks[0].href == null) {
        alert(translate("noXlink"));
        return;
    }
    
    disableEditForm();
    var eBusy = $('editorBusy');
    if (eBusy) eBusy.show();
    
    if(modalBox) {
      modalBox.hide();
    }
		
    var metadataId = document.mainForm.id.value;
    var thisElement = $(dialogRequest.id);
    doXLinkNewElementAjax(xlinks.length-1,metadataId,thisElement);
}


function createNewXLink() {
	var lowercaseName = dialogRequest.name.toLowerCase();
	if(lowercaseName.indexOf("resource") != -1) {
		xlinks[0].href = 'local://xml.format.get';
	} else if(lowercaseName.indexOf("contact") != -1) {
		xlinks[0].href = 'local://xml.user.get?schema=iso19139.che&role=' + Ext.get('contact.role').getValue();
	} else if(lowercaseName.indexOf("keyword") != -1) {
		xlinks[0].href = 'local://che.keyword.get?thesaurus=local._none_.non_validated&locales=en,it,de,fr';
	}
	submitXLink();
}

function doXLinkNewElementAjax(index, metadataId, thisElement) {
    var href = escape(xlinks[index].href);
    var pars = "&id=" + metadataId + "&ref=" + dialogRequest.ref + "&name=" + dialogRequest.name + "&href="+href;

    var myAjax = new Ajax.Request(
    getGNServiceURL(dialogRequest.action),
    {
        method: 'get',
        parameters: pars,
        onSuccess: function(req) {
            var eBusy = $('editorBusy');
            if (eBusy) eBusy.hide();
            $('editorOverlay').setStyle({display: "none"});

            if (index > 0) {
                doXLinkNewElementAjax(index - 1,metadataId,thisElement);
            }

            var html = req.responseText;
            
            var what = index == 0 ? dialogRequest.replacement : 'add';
            if (what == 'replace') {
                thisElement.replace(html);
            } else if (what == 'before') {
                thisElement.insert({
                    'before': html
                });
                setAddControls(thisElement.previous(), orElement);
            } else if (what == 'add') {
            	thisElement.insert({
            		'after': html
            	});
            	setAddControls(thisElement.next(), orElement);
            } else {
                alert("doNewElementAjax: invalid what: " + what + " should be replace or add.");
            }
            
            if (index == 0) {
                // Init map if spatial extent editing - usually bounding box or bounding polygon
                if (dialogRequest.name == 'gmd:geographicElement' || dialogRequest.name == 'gmd:polygon' 
                	|| dialogRequest.name == 'gmd:extent' || dialogRequest.name == 'gmd:serviceExtent'
            		|| dialogRequest.name == 'gmd:sourceExtent' || dialogRequest.name == 'gmd:spatialExtent'
            		|| dialogRequest.name == 'che:revisionExtent' ) {
                	if(typeof(searchTools) != "undefined")
                		searchTools.initMapDiv();
                }

                // Check elements
                validateMetadataFields();

                setBunload(true);
                // reset warning for window destroy
            }

        },
        onFailure: function(req) {
            var eBusy = $('editorBusy');
            if (eBusy) eBusy.hide();

            alert(translate("errorAddElement") + name + translate("errorFromDoc")
                  + " / status " + req.status + " text: " + req.statusText + " - " + translate("tryAgain"));
            setBunload(true);
            // reset warning for window destroy
        }
    }
    );
}

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
function XLink() {
    new Ajax.Autocompleter("xlink-s-contact",
    "xll",
    "shared.user.list?sortByValidated=true",
    {
        method: 'get',
        paramName: 'name',
        afterUpdateElement: updateXLink,
        indicator: 'xlink.contact.indicator'
    }
    );

    new Ajax.Autocompleter("xlink-s-format",
    "xll",
    "xml.format.list?order=validated",
    {
        method: 'get',
        paramName: 'name',
        afterUpdateElement: updateXLink,
        indicator: 'xlink.format.indicator'
    }
    );

    // _extent_acc made global in order to access it from updateExtentAutocompleter()
    window._acc = new Ajax.Autocompleter("xlink-s-extent",
    "xll",
    "extent.search.list?numResults=25&property=desc&method=loose&format=gmd_bbox",
    {
        method: 'get',
        frequency: 1,
        paramName: 'pattern',
        parameters: '',
        afterUpdateElement: updateXLink,
        indicator: 'xlink.extent.indicator'
    }
    );
}
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


/**
 * Popup for searching contact, keywords, crs elements and add the
 * xlink element to the metadata. This will set up an autocompletion
 * list on search action.
 */
function popXLink(action, ref, name, id) {
    /**
     * Display popup according to ref element.
     * Contact could be gmd:pointOfContact, gmd:contact, ...
     */
    if (name.toUpperCase().indexOf("FORMAT") != -1) {
        /**
		 * Distribution formats allow selection of
		 * a format defined in ...
		 */
        mode = "FORMAT";
        formatInit();
    } else if (name.toUpperCase().indexOf("CONTACT") != -1 ||
    name.toUpperCase().indexOf("PROCESSOR") != -1 ||
    name.toUpperCase().indexOf("PARTY") != -1 ||
    name.toUpperCase().indexOf("SOURCE") != -1
    ) {
        mode = "CONTACT";
        contactInit();
    } else if (name.toUpperCase().indexOf("KEYWORD") != -1) {
        /**
		 * Keywords allow selection of a keyword in
		 * a thesaurus registered in the catalogue.
		 */
        mode = "KEYWORD";
        keywordInit();
    } else {
        /**
		 * Extents allow selection of a geotag.
		 *
		 */
        mode = "EXTENTS";
        extentInit(name);

    }
}

/**
 * Update XLink on item selected. Autocompletion return a list of
 * elements having an xlink:href attribute which store the xlink to be
 * used to get the iso fragment to be include in the metadata.
 *
 * eg. <ul>
 * 	<li xlink:href="http://localhost:8080/geonetwork/srv/en/xml.user.get?id=1&role=pointOfContact">My contact</li>
 * </ul>
 *
 */
function updateXLink(text, li) {

    if (li != null) {
        xl.title = li.innerHTML;
        // According to namespace support in different browsers
        var uri;

        if (li.getAttributeNS) {
            uri = li.getAttributeNS("http://www.w3.org/1999/xlink", "href");
        }

        if (uri == '') {
            uri = li.getAttribute("xlink:href");
        }

        if (uri == '' || uri == undefined)
        uri = li.getAttributeNode("xlink:href").value;

        // TODO : Check on FF2, and more test on IE
        // Tested on FF3 and IE6
        xl.href = uri;
    }

    // TODO : multiple role
    if (mode == "CONTACT") {
        xl.href += ("&schema=" + $("xlink.schema").value);
        contactSetRole($("contact.role").value);
    } else if (mode == "EXTENTS") {
        extentTypeCode($("extent.type.code").value);
        extentSetFormat($("extent.format").value);
    }
}

/**
 * Contacts allow search in the contact directory
 * and selection of a role for the contact.
 */
function contactInit() {
    $("popXLink.contact").style.display = "block";
    $("xlink-s-contact").value = "";
    $("contact.role").selectedIndex = 0;
}

function contactSetRole(role) {
    if (role != "") {
        var add = "&role=" + role;
        if (!xl.href) xl.href = '';
        if (xl.href.indexOf("role") != -1)
        xl.href = xl.href.replace(/&role=.*/i, add);
        else
        xl.href += add;
    }
}

/**
 * Keywords allow search in the thesaurus directory
 */
function keywordInit() {
    $("popXLink.keyword").style.display = "block";
    $("keywordList").style.display = "block";

    // remove the "xlink-s" text input
    /*
    Ext.get("popXLink").first("#xlink-s").remove();
*/
    // insert divs for the text field and button
    Ext.DomHelper.insertFirst("popXLink.keyword", {
        tag: "div",
        id: "pop-kw",
        children: [
        {
            tag: "div",
            id: "pop-kw-txt",
            style: "float:left;margin-right:3px"
        },
        {
            tag: "div",
            id: "pop-kw-btn",
            style: "float:left;margin-right:3px;"
        }
        ]
    });

    // insert text field
    var tf = new Ext.form.TextField({
        id: "pop-kw-txt",
        renderTo: "pop-kw-txt"
    });

    // search button
    new Ext.Button({
        text: translate("search"),
        id: "pop-kw-btn",
        renderTo: "pop-kw-btn",
        handler: function() {
            Ext.get("xlink.keyword.indicator").show();
            Ext.Ajax.request({
                url: "xml.search.keywords",
                method: "GET",
                params: {
                    "pNewSearch": "true",
                    "pTypeSearch": "1",
                    "pMode": "searchCheckbox",
                    "pKeyword": tf.getValue()
                },
                success: function(response) {
                    var el = Ext.get("keywordList").select(".href");

                    if (el) {
                        el.remove();
                    }
                    var txt = response.responseText;

                    if (txt && txt.length > 0) {
                        Ext.DomHelper.append("keywordList", response.responseText);
                    }
                    Ext.get("xlink.keyword.indicator").hide();
                },
                failure: function() {
                    Ext.get("xlink.keyword.indicator").hide();
                    alert(translate("ajaxRequestFailed"));
                }
            });
        }
    });
}

/**
 * Format allow search in the format table
 */
function formatInit() {
    $("popXLink.format").style.display = "block";

    $("xlink-s-format").value = "";
}

/**
 * Search extents
 */
function extentInit(name) {

    if (name == "gmd:spatialExtent") {
        $("extent.format").value = "gmd_spatial_extent_polygon";
    } else {
        $("extent.format").value = "gmd_complete";
    }

    $("popXLink.extent").style.display = "block";
    $("extent.type.code").style.display = "block";

    $("extent.format").style.display = "block";
    $("xlink-s-extent").value = "";
    $("xlink.extent.indicator").style.display = "none";

    $("extent.xlink.create").style.display = "inline";
}


function extentTypeCode(code) {
    if (xl.href != null && code != "") {
        var add = "&extentTypeCode=" + code;

        if (xl.href.indexOf("extentTypeCode") != -1) {
            var index = xl.href.indexOf("extentTypeCode")

            if (xl.href.indexOf("&", index) != -1) add += "&"

            xl.href = xl.href.replace(/&extentTypeCode=[^&]*/i, add);
        } else
        xl.href += add;
    }
}

function extentSetFormat(code) {

    if (xl.href != null && code != "") {


        var add = "&format=" + code;

        if (xl.href.indexOf("format") != -1) {
            var index = xl.href.indexOf("format")
            if (xl.href.indexOf("&", index) != -1) add += "&"

            xl.href = xl.href.replace(/&format=[^&]*/i, add);
        } else
        xl.href += add;
    }
}

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

    // Hide optional fields (could be faster by getting els by Ext.get(id))
    /*
        var optionalFields = ['contact.role', 'extent.format', 'extent.map'];
        $('popXLink').descendants().each(function(el) {
            if (optionalFields.indexOf(el.id) != -1) el.style.display = "none";
        });
    */
    // Hide all form contents
    var optionalFields = ['popXLink.contact', 'popXLink.format', 'popXLink.keyword', 'popXLink.extent'];
    optionalFields.each(function(id) {
        $(id).style.display = 'none';
    });

    // there is a special create button for extents so hide extents and show common one extents can modify if desired
    $("extent.xlink.create").style.display = "none";
    $("common.xlink.create").style.display = "inline";

    // Clean keywords result list
    $('keywordList').innerHTML = '';


    if (name.toUpperCase().indexOf("KEYWORD") != -1) {
        xlinkShowKeywordSelectionPanel(ref, name, id)
    } else {
        displayModalBox('popXLink', translate('searchElements'));

        popXLink(action, ref, name, id);

    }
    document.mainForm.ref.value = ref;
    document.mainForm.name.value = name;
}

var modalBox;
var validationReportBox;

function displayModalBox(contentDivId, boxTitle) {

    /*
    var el;
    el = Ext.get("popXLink").first("#xlink-s");
    if (!el) {
        Ext.DomHelper.insertFirst("popXLink", {
            tag: "input", id: "xlink-s", value: ""
        });
    }
*/
    el = Ext.get("pop-kw");
    if (el) {
        el.remove();
    }
    el = Ext.get("editForm").select(".href");
    if (el) {
        el.remove();
    }

    $(contentDivId).style.display = 'block';
    if (!modalBox) {
        modalBox = new Ext.Window({
            title: boxTitle,
            id: "modalwindow",
            layout: 'fit',
            modal: true,
            constrain: true,
            autoScroll: true,
            iconCls: 'searchIcon',
            //width: 200,
            //height: 150,
            closeAction: 'hide',
            listeners: {
                hide: function() {
                    $(contentDivId).style.display = 'none';
                }
            },
            contentEl: contentDivId
        });
    }

    if (modalBox) {
    	try {
	        modalBox.show();
	        modalBox.setHeight(620);
	        modalBox.setTitle(boxTitle);
	        modalBox.setWidth(Ext.get(contentDivId).getWidth() + 14);
	        modalBox.center();
    	} catch (e) {
    		modalBox.destroy();
    		modalBox = undefined;
    		displayModalBox(contentDivId, boxTitle);
    	}
    }

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
                    $(contentDivId).style.display = 'none'
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
    _acc.url = _extent_acc_initialUrl + '' + geomParam
    // Fires autocompletion
    _acc.getUpdatedChoices()
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
}

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
}

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
}
GeoNetworkSearcher.prototype.req = null;

/**
 *
 */
GeoNetworkSearcher.prototype.search = function() {
    this.reset();
    this.req = new Ajax.Request(
    this.DEFAULT_PARAMS["service"],
    {
        method: this.DEFAULT_PARAMS["method"],
        parameters: this.params.get(),
        onSuccess: this.present.bind(this),
        onFailure: this.error.bind(this)
    }
    );
}

/**
 * Display results
 */
GeoNetworkSearcher.prototype.present = function(req) {
    this.resultPanel.innerHTML = req.responseText;
}

/**
 * Update target element,
 * and clean results.
 */
GeoNetworkSearcher.prototype.updateTarget = function(value) {
    document.getElementById(this.target).value = value;
    this.reset();
}

/**
 * Clean result and search form
 */
GeoNetworkSearcher.prototype.reset = function() {
    this.resultPanel.innerHTML = "";
}

/**
 * On error
 */
GeoNetworkSearcher.prototype.error = function() {
    alert("Error");
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
function xlinkShowKeywordSelectionPanel(ref, name, id) {
    if (!keywordSelectionWindow) {
        var port = window.location.port === "" ? "": ':' + window.location.port;
        var keywordSelectionPanel = new app.KeywordSelectionPanel({
            createKeyword: function() {
                doNewElementAction('/geonetwork/srv/eng/metadata.elem.add', ref, name, id);
            },
            addCreateXLinkButton: true,
            listeners: {
                keywordselected: function(panel, keywords) {
                    xlinks = [];
                    
                    var count = 2;

                    var store = panel.itemSelector.toMultiselect.store;
                    store.each(function(record) {
                        var uri = record.get("uri");
                        var thesaurus = record.get("thesaurus");
                        var xlink = new XLink();

                        xlink.href = "local://che.keyword.get?"+"thesaurus="+encodeURIComponent(thesaurus)+"&id="+encodeURIComponent(uri)+"&locales=fr,en,de,it";
                        xlinks.push(xlink);
                    });


                    // Save
                    submitXLink();
                }
            }
        });

        keywordSelectionWindow = new Ext.Window({
            width: 620,
            height: 300,
            title: translate('keywordSelectionWindowTitle'),
            layout: 'fit',
            items: keywordSelectionPanel,
            closeAction: 'hide'
        });
    } else {
      keywordSelectionWindow.items.get(0).itemSelector.toMultiselect.store.data.clear();
    }

    keywordSelectionWindow.items.get(0).setRef(ref);
    keywordSelectionWindow.show();
}

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
	    var input = inputs[i];
	    if (input.checked) {
    		ids = input.value;
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