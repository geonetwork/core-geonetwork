/********************************************************************
* gn_search.js
*
* This file contains functions related to the dynamic behavior of geonetwork:
* - Metadata search & reset
* - Area of Interest Map behavior
* - metadata present
* - metadata vote rating
*
********************************************************************/

/**********************************************************
***
***		SIMPLE SEARCH
***
**********************************************************/

var mainViewport;

function initSimpleSearch(wmc)
{
}

function gn_anyKeyObserver(e)
{
	if(e.keyCode == Event.KEY_RETURN)
		runSimpleSearch();
}

function runCsvSearch() {
    var serviceUrl = getGNServiceURL('csv.search');
    if ($("advanced_search_pnl").visible()) {
        serviceUrl = serviceUrl + "?" + fetchParam('template');
	}
    window.open(serviceUrl, 'csv')
    metadataselect(0, 'remove-all');
}


/**
 * Trigger PDF search.
 *
 * If run on selection run pdf.selection.search
 * to retrieve the PDF document and clean current selection.
 * NOTE: cleaning current selection breaks new widget apps so don't do it for
 * now
 *
 * If not, use the pdf.search service.
 */
function runPdfSearch(onSelection) {

	if (onSelection) {
		var serviceUrl = getGNServiceURL('pdf.selection.search');

        serviceUrl = serviceUrl + "?" + fetchParam('sortBy');
        serviceUrl = serviceUrl + fetchParam('sortOrder');

		if ($("advanced_search_pnl").visible()) {
            serviceUrl = serviceUrl + fetchParam('template');
		}

		location.replace (serviceUrl);
	//	metadataselect(0, 'remove-all');
	} else {
			var GNCookie = Ext.state.Manager.getProvider();
			var cookie = GNCookie.get('search');
	    if (cookie && cookie.searchTab=='advanced')
	        runAdvancedSearch("pdf");
	    else
	        runSimpleSearch("pdf");
	}
}

function runSimpleSearch(type)
{
    if (type != "pdf")
        preparePresent();

	setSort();

	var pars = $('simple_search_form').serialize(true);

	if (type == "pdf")
       gn_searchpdf(pars);
    else
	   // Load results via AJAX
	   gn_search(pars);
}

function resetWherePars() {

  setParam('region_simple',null);
	// Clear also region in advanced and remote search to keep synch
	setParam('region',null);
	setParam('region_remote',null);
    
  setParam('relation_simple','overlaps');	
  setParam('relation','overlaps');	
  setParam('relation_remote','overlaps');	

	resetMinimaps();
}

function resetSimpleSearch()
{
/* make sure all values are completely reset (instead of just using the default
   form.reset that would only return to the values stored in the session */
		var form = $('simple_search_form');
    clearFormValues(form);

		// Now set the special form fields to their defaults
		resetWherePars();
	
    setParam(form['requestedLanguage_simple'],      Env.lang);
    setParam(form['sortBy'],      'relevance');
    setParam(form['sortOrder'],   '');
    setParam(form['hitsPerPage_simple'], '10');
    setParam(form['output_simple'],      'full');
}


function resetMinimaps() {
	GeoNetwork.minimapSimpleSearch.clearExtentBox();
    var minimap =  GeoNetwork.minimapSimpleSearch.getMap();
    if (minimap) {
        var pnl = Ext.getCmp('mini_mappanel_ol_minimap1');
        pnl.map.setCenter(pnl.center, pnl.zoom);
    }	
	
	GeoNetwork.minimapAdvancedSearch.clearExtentBox();
    minimap =  GeoNetwork.minimapAdvancedSearch.getMap();
    if (minimap) {
        var pnl = Ext.getCmp('mini_mappanel_ol_minimap2');
        pnl.map.setCenter(pnl.center, pnl.zoom);
    }	

	GeoNetwork.minimapRemoteSearch.clearExtentBox();
    minimap =  GeoNetwork.minimapRemoteSearch.getMap();
    if (minimap) {
        var pnl = Ext.getCmp('mini_mappanel_ol_minimap3');
        pnl.map.setCenter(pnl.center, pnl.zoom);
    }	
}


/********************************************************************
*
*  Toggling between simple/advanced/remote search
*
********************************************************************/

function showAdvancedSearch(search)
{
	openSearch('advanced_search_pnl');
	var GNCookie = Ext.state.Manager.getProvider();
	GNCookie.set('search', {searchTab: 'advanced'});
	initAdvancedSearch();
	if (search == 'true') {
		runAdvancedSearch(); 
	} else {
		setFormWithQueryParams('advanced_search_form');
	}
}

function showSimpleSearch(search)
{
	openSearch('simple_search_pnl');
	var GNCookie = Ext.state.Manager.getProvider();
	GNCookie.set('search', {searchTab: 'default'});
	initSimpleSearch();
	if (search == 'true') {
		runSimpleSearch();
	} else {
		setFormWithQueryParams('simple_search_form');
	}
}

function showRemoteSearch(search)
{
	openSearch('remote_search_pnl');
	var GNCookie = Ext.state.Manager.getProvider();
	GNCookie.set('search', {searchTab: 'remote'});
	initRemoteSearch();
	if (search == 'true') {
		runRemoteSearch();
	} else {
		setFormWithQueryParams('remote_search_form');
	}
}

function setFormWithQueryParams(formName) {
	// TODO: Add special handling for date search fields?
	var GNCookie = Ext.state.Manager.getProvider();
	var cookie = GNCookie.get('params');
	if (cookie) {
		var params = cookie.searchParams;
		var theControls = $(formName).getElements();
		for (var param in params) {
			theControls.each(function(theControl){
				if (theControl.name == param) {
					if (theControl.tagName != 'SELECT') {
						if (theControl.type == 'checkbox' || theControl.type == 'radio') {
							if (params[param] == 'on') {
								theControl.checked = true; 
								if (theControl.onchange) theControl.onchange();
							}
						} else {
							theControl.value = params[param];
							if (theControl.onchange) theControl.onchange();
						}
					} else {
						for(index = 0; index < theControl.length; index++) {
							if (theControl[index].value == params[param]) {
								theControl.selectedIndex = index;
								if (theControl.onchange) theControl.onchange();
							}
						}
					}
				} 
			});
		}
	}
}

function clearFormValues(form) {
	var GNCookie = Ext.state.Manager.getProvider();
	var cookie = GNCookie.clear('params');

	var theControls = form.getElements();
	theControls.each(function(theControl){
		if (theControl.tagName != 'SELECT') {
			if (theControl.type == 'checkbox' || theControl.type == 'radio') {
				theControl.checked = false; 
			} else {
				theControl.value = '';
			}
		} else {
			theControl.selectedIndex = -1;
		}
	});

	// TODO: Add ajax call to clear params on server
}

function openSearch(s)
{
	if( ! Prototype.Browser.IE )
	{
		Effect.BlindDown(s);
	}
	else
	{
		$(s).show();
	}
}

function closeSearch(s)
{
	if( ! Prototype.Browser.IE )
	{
		Effect.BlindUp($(s));
	}
	else
	{
		$(s).hide();
	}
}


/**********************************************************
***
***		ADVANCED SEARCH
***
**********************************************************/

function initAdvancedSearch()
{
	//im_mm_init();

	new Ajax.Autocompleter('themekey', 'keywordList', 'portal.search.keywords?',{paramName: 'keyword', updateElement : addQuote});
	initCalendar();
}

function runAdvancedSearch(type)
{
  if (type != "pdf")
	   preparePresent();

	setSort();

	var pars = $('advanced_search_form').serialize(true);

    if (type == "pdf")
       gn_searchpdf(pars);
    else
	   // Load results via AJAX
	   gn_search(pars);
}

function resetAdvancedSearch()
{
	var form = $('advanced_search_form');

	// Clear all form values
	clearFormValues(form);

	// Now set the special form fields to their defaults
	radioSimil = form['similarity'];
	radioSimil[1].checked=true;

	resetWherePars();

	setBoolParam(form['radfrom0'],true);
	form['radfrom1'].disable();
	form['radfromext1'].disable();

    setParam(form['requestedLanguage'],      Env.lang);
 	setParam(form['sortBy'],      'relevance');
  setParam(form['hitsPerPage'], '10');
  setParam(form['output'],      'full');

	// reset the selectors so that new searches are done to fill them
	selectorIds = [];
}

/**********************************************************
***
***		REMOTE SEARCH
***
**********************************************************/

function initRemoteSearch() {}

function resetRemoteSearch() {

	var form = $('remote_search_form');

	// Clear all form values
	clearFormValues(form);

	// Now set the special form fields to their defaults
	resetWherePars();

 	setParam(form['hitsPerPage'], '10');
	setBoolParam(form['serverhtml'], false);
 	setParam(form['timeout'],'20');
	deselectAllServers(form);
}

function profileSelected()
{
	var form = $('remote_search_form');
	var serverList = form['profile'];
	if (serverList.selectedIndex > 0) {
		var serverArray = serverList.options[selectedIndex].value.split(' ');
		deselectAllServers(form);
		for (var i=0; i < serverArray.length; i++)
			selectServer(serverArray[i]);
	}
}
	
function deselectAllServers(form)
{
	var rservers = form['servers'];
	for (var i=0; i < rservers.length; i++)
		rservers.options[i].selected = false;
}
	
function selectServer(server)
{
	var form = $('remote_search_form');
	var rservers = form['servers'];
	for (var i=0; i < rservers.length; i++) {
		if (rservers.options[i].value == server) {
			rservers.options[i].selected = true;
		}
	}
}
		
function checkRemoteFields()
{
	var pars = $('remote_search_form').serialize(true);

	if (isWhitespace(pars['or']) && isWhitespace(pars['title']) && isWhitespace(pars['abstract']) && isWhitespace(pars['themekey']) && isWhitespace(pars['region'])) {
		alert(translate("noSearchCriteria"));
		return false;
	}
	if (pars['servers'].length == 0) {
		alert(translate("noServer"));
		return false;
	}
	return true;
}
		
function runRemoteSearch(type) 
{
	if (checkRemoteFields()) {
		if (type != "pdf") preparePresent('remote');

		var pars = $('remote_search_form').serialize(true);
		pars['remote']  = 'on';
		pars['attrset'] = 'geo';

		if (type == "pdf") {
			pars['hitsPerPage']='1000';
			location.replace(getGNServiceURL('pdf.search') + "?" + convertToParamString(pars));
		} else {
			// Load results via AJAX
			gn_search(pars);    
		}
	}
}

function convertToParamString(pars) {
	var parStr = '';
	for (var param in pars) {
			parStr += "&" + param + '=' + pars[param];
	}
	return parStr;
}

/**********************************************************
***
***		RATING
***
**********************************************************/

function showFields(img, div)
{
        var img = $(img);
		if (img) {
			var src = img.getAttribute('src');
			var ndx = src.lastIndexOf('/');
			var div = $(div);

			src = src.substring(0, ndx+1);

			if (div.visible())      img.setAttribute('src', src +'plus.gif');
					else                                    img.setAttribute('src', src +'minus.png');

			div.toggle();
		}
}


//-------------------------------------------------------------------

function setSort()
{
	if ($('sortBy').value=='title')
	   $('sortOrder').value = 'reverse';
	else
	   $('sortOrder').value = '';
	
	if ($('sortBy_simple').value=='title')
	   $('sortOrder_simple').value = 'reverse';
	else
	   $('sortOrder_simple').value = '';
}

//-------------------------------------------------------------------

function setSortAndSearch()
{
	$('sortBy').value = $F('sortBy.live');
	$('sortBy_simple').value = $F('sortBy.live');
	setSort();
	var GNCookie = Ext.state.Manager.getProvider();
	var cookie = GNCookie.get('search');
	if (cookie && cookie.searchTab=='advanced') 
		runAdvancedSearch();
	else 
		runSimpleSearch();
}

//-------------------------------------------------------------------

var ratingPopup = null;

//-------------------------------------------------------------------

function showRatingPopup(id)
{
	if (ratingPopup == null)
	{
		ker.loadURL('rating.popup', ker.wrap(this,
			function(t)
			{
				var p = document.createElement('div');

				p.className     = 'ratingBox';
				p.innerHTML     = t.responseText;
				p.style.display = 'none';
				p.style.zIndex  = 32000;

				p.setAttribute('id', 'rating.popup');
				$("content").appendChild(p);

				ratingPopup = p;

				setTimeout(ker.wrap(this, function(){ showRatingPopup(id); }), 10);
			}));

		return;
	}

	var pos = Position.positionedOffset($('rating.link.'+ id));

	ratingPopup.style.left = pos[0] -100 + "px";
	ratingPopup.style.top  = pos[1] +16 + "px";

	ratingPopup.setAttribute('mdid', id);

	Element.show(ratingPopup);
}

//-------------------------------------------------------------------

function hideRatingPopup()
{
	var popup = $('rating.popup');

	if (popup != null)
	{
		Element.hide(popup);
		Element.hide('rating.image');
	}
}

//-------------------------------------------------------------------

function rateMetadata(rating)
{
	var id = ratingPopup.getAttribute('mdid');

	Element.show('rating.image');

	var request =
		'<request>'+
		'   <id>'+ id +'</id>'+
		'   <rating>'+ rating +'</rating>'+
		'</request>';

	ker.send('xml.metadata.rate', request, ker.wrap(this, rateMetadata_OK));
}

//-------------------------------------------------------------------

function rateMetadata_OK(xmlRes)
{
	if (xmlRes.nodeName == 'error')
		ker.showError(translate('rateMetadataFailed'), xmlRes);
	else
		hideRatingPopup();
}


/********************************************************************
*** GET BOUNDINGBOX COORDINATES FOR A REGION
********************************************************************/

function doRegionSearch(regionlist)
{
    var region = $(regionlist).value;
    if(region=="") {
			resetMinimaps();
    }  else if (region=="userdefined") {
		// Do nothing. AoI is set by the user
    } else {
        getRegion(region);
    }

		$('region').value = $(regionlist).value;
		$('region_remote').value = $(regionlist).value;
		$('region_simple').value = $(regionlist).value;
}

function getRegion(region)
{
    if (region) 
			var pars = "id="+encodeURIComponent(region);

    var myAjax = new Ajax.Request(
        getGNServiceURL('xml.region.get'),
        {
            method: 'get',
            parameters: pars,
            onSuccess: function(req) {
    					//Response received
    					var node = req.responseXML;
    					var northcc = xml.evalXPath(node, 'regions/region/north');
    					var southcc = xml.evalXPath(node, 'regions/region/south');
    					var eastcc = xml.evalXPath(node, 'regions/region/east');
    					var westcc = xml.evalXPath(node, 'regions/region/west');

    					$('northBL').value=northcc;
    					$('southBL').value=southcc;
    					$('eastBL').value=eastcc;
    					$('westBL').value=westcc;
    					$('northBL_remote').value=northcc;
    					$('southBL_remote').value=southcc;
    					$('eastBL_remote').value=eastcc;
    					$('westBL_remote').value=westcc;
    					$('northBL_simple').value=northcc;
    					$('southBL_simple').value=southcc;
    					$('eastBL_simple').value=eastcc;
    					$('westBL_simple').value=westcc;

							GeoNetwork.minimapSimpleSearch.updateExtentBox();
							GeoNetwork.minimapAdvancedSearch.updateExtentBox();
							GeoNetwork.minimapRemoteSearch.updateExtentBox();
	
						},
            onFailure: getRegion_error
        }
    );
}


function getRegion_error() {
    alert(translate("error"));
}

function AoIrefresh() {
	$('region_simple').value="userdefined";
	$('region_remote').value="userdefined";
  $('region').value="userdefined";
}

function runRssSearch()
{
	var myAjax = new Ajax.Request(
		getGNServiceURL('metadata.latest.updated'),
		{
			method: 'get',
			parameters: null,
			onSuccess: gn_search_rss_complete
		}
	);
}

function gn_search_rss_complete(req) {
    var rlist = $('latest_updates');

    rlist.innerHTML = req.responseText;
}

/********************************************************************
*** DO THE SEARCH!
********************************************************************/

function preparePresent()
{
	// Display results area
	//clearNode('resultList');
	$('loadingMD').show();
}

function gn_search(pars)
{
	/*var parStr = '';
	for (var par in pars) {
		parStr += ' ' + par + '=' + '"' + pars[par] + '"';
	}
	console.log('Setting params to '+parStr);*/
	var GNCookie = Ext.state.Manager.getProvider();
	GNCookie.clear('params');
	GNCookie.set('params', {searchParams: pars});

	var myAjax = new Ajax.Request(
		getGNServiceURL('main.search.embedded'),
		{
			method: 'get',
			parameters: pars,
			onSuccess: gn_search_complete,
			onFailure: gn_search_error
		}
	);
}

function gn_searchpdf(pars)
{
    pars['hitsPerPage']=9999;
    // Update this value if more document required in PDF output
    // FIXME : Should be defined in service config.
    location.replace (
        getGNServiceURL('pdf.search') + "?" + convertToParamString(pars)
    );
}

function gn_present(frompage, topage)
{
	preparePresent();

	var pars = 'from=' + frompage + "&to=" + topage;

	var myAjax = new Ajax.Request(
		getGNServiceURL('main.present.embedded'),
		{
			method: 'get',
			parameters: pars,
			onSuccess: gn_search_complete,
			onFailure: gn_search_error
		}
	);
}

function gn_search_complete(req) {
		//if (req.status == 0) {
		//FIXME: Sometimes status is returned as 0 - must rerun search again
		// Needs more research as this happens in other javascript places!    
		//alert('Returned status 0, rerun search');
		//var GNCookie = Ext.state.Manager.getProvider();
		//var cookie = GNCookie.get('params');
		//var params = cookie.searchParams;
		//gn_search(params);
		//}
    var rlist = $('resultList');
    rlist.innerHTML = req.responseText;

    $('loadingMD').hide();
}

/********************************************************************
*
*  Show metadata content
*
********************************************************************/
function gn_showSingleMetadataUUID(uuid)
{
   var pars = 'uuid=' + uuid + '&control&currTab=simple';
   gn_showSingleMet(pars);
}

function gn_showSingleMetadata(id)
{
   var pars = 'id=' + id + '&currTab=simple';
   gn_showSingleMet(pars);
}

function gn_showSingleMet(pars)
{
   if ($("loadingMD")) $("loadingMD").show();

   var myAjax = new Ajax.Request(
        getGNServiceURL('metadata.show.embedded'),
        {
            method: 'get',
            parameters: pars,
            onSuccess: function (req) {
                if ($("loadingMD")) $("loadingMD").hide();

                var parent = $('resultList');
                clearNode(parent);
                // create new element
                var div = document.createElement('div');
                div.className = 'metadata_current';
                div.style.display = 'none';
                div.style.width = '100%';
                parent.appendChild(div);

                div.innerHTML = req.responseText;

                Effect.BlindDown(div);

                var tipman = new TooltipManager();
                ker.loadMan.wait(tipman);

                // Init maps contained in search results
                extentMap.initMapDiv();
            },
            onFailure: gn_search_error// FIXME
        });
}
function gn_showMetadata(id) {
	gn_showMetadataTab(id, 'simple');
}
function gn_showMetadataTab(id, currTab)
{
    var pars = 'id=' + id + '&currTab=' + currTab;

    $('gn_showmd_' + id) .hide();
    $('gn_loadmd_' + id) .show();

    var myAjax = new Ajax.Request(
        getGNServiceURL('metadata.show.embedded'),
        {
            method: 'get',
            parameters: pars,
            onSuccess: function (req) {
                var parent = $('mdwhiteboard_' + id);
                clearNode(parent);

                $('gn_loadmd_' + id) .hide();
                $('gn_hidemd_' + id) .show();

                // create new element
                var div = document.createElement('div');
                div.className = 'metadata_current';
                div.style.display = 'none';
                div.style.width = '100%';
                parent.appendChild(div);

                div.innerHTML = req.responseText;

                Effect.BlindDown(div);

                var tipman = new TooltipManager();
                ker.loadMan.wait(tipman);

                // Init maps contained in search results
                extentMap.initMapDiv();
            },
            onFailure: gn_search_error// FIXME
        });
}

function gn_hideMetadata(id)
{
    var parent = $('mdwhiteboard_' + id);
    var div = parent.firstChild;
    Effect.BlindUp(div, { afterFinish: function (obj) {
            clearNode(parent);
            $('gn_showmd_' + id) .show();
            $('gn_hidemd_' + id) .hide();
        }
    });
}

function a(msg) {
    alert(msg);
}

function gn_search_error(req) {
	//alert("ERROR " + req.responseText);
	Modalbox.show(req.responseText,{title: 'Search Error', width: 600} );
    $('loadingMD') .hide();
	return -1;
}

function gn_filteredSearch() {
    var params = "";
    if ($("advanced_search_pnl").visible()) {
		params = fetchParam('template');
	}
	
    var myAjax = new Ajax.Request(
		getGNServiceURL('selection.search'),
		{
			method: 'get',
			parameters: params,
			onSuccess: gn_search_complete,
			onFailure: gn_search_error
		}
	);
}

/**********************************************************
***
***		STUFF FOR CATEGORY SEARCH
***
**********************************************************/

function runCategorySearch(category)
{
	preparePresent();

	var pars = "category=" + category;

	// Load results via AJAX
	gn_search(pars);
}


/**********************************************************
*** Search helper functions
**********************************************************/

function fetchMultipleParam(p)
{
  var pL = $(p);
  var param = "&"+p+"=";
  if (!pL)
    return param;
  else {
    var pars = "";
    for (i = 0;i < pL.length;i++) {
      if (pL.options[i].selected) {
        var t = pL.options[i].value;
        pars += param+encodeURIComponent(t);
      }
    }
    if (pars == "") return param;
    else return pars;
  }
}

function fetchParam(p)
{
  var pL = $(p);
  if (!pL)
    return "";
  else {
  	var t = pL.value;
  	if(t)
  		return "&"+p+"="+encodeURIComponent(t);
  	else
  		return "";
	}
}

function fetchBoolParam(p)
{
  var pL = $(p);
  if (!pL)
    return "";
  else {
  	if(pL.checked )
  		return "&"+p+"=on";
  	else
  		return "&"+p+"=off";
  }
}

function fetchRadioParam(name)
{
	var radio = document.getElementsByName(name);
	var value = getCheckedValue(radio);
	return "&"+name+"="+value;
}

// return the value of the radio button that is checked
// return an empty string if none are checked, or there are no radio buttons
function getCheckedValue(radioObj) {
	if(!radioObj)
		return "";
	var radioLength = radioObj.length;
	if(radioLength == undefined)
		if(radioObj.checked)
			return radioObj.value;
		else
			return "";
	for(var i = 0; i < radioLength; i++) {
		if(radioObj[i].checked) {
			return radioObj[i].value;
		}
	}
	return "";
}


function setParam(p, val)
{
  var pL = $(p);
  if (pL) pL.value = val;
}

function setBoolParam(p, val)
{
  var pL = $(p);
	if (pL) pL.checked = val;
}

/**********************************************************
*** Selectors for keywords, title, credit etc
**********************************************************/

	function addQuote (id,li){
    $(id).value = '"'+li.innerHTML+'"';
	}

	var selectorIds = [];

  /**
   * Place popup according to input element position.
   * Load list of keywords
   * 
   * @param el
   * @param pop
   * @return
   */
  function popSelector(el, popId, selectorId, service, input){

		var pop = $(popId);
    if (pop.style.display == "block") {
        pop.style.display = "none";
        return false;
    }
                
    pop.style.top = el.cumulativeOffset().top + el.getHeight();
    pop.style.left = el.cumulativeOffset().left;
    pop.style.width = '250px'; //el.getWidth();
    pop.style.display = "block";

 		if (!(selectorId in selectorIds) || $(selectorId).empty()) {
			selectorIds[selectorId] = true;
			new Ajax.Updater(selectorId,service+"="+$(input).value);
		}
  }


  function keywordCheck(k, check){
       // FIXME k = '"'+ k + '"';
       if (check){     // add the keyword to the list
               if ($("themekey").value !== '') { // add the "or" keyword
                $("themekey").value += ' or ' + k;
            } else {
                $("themekey").value = k;
            }
       } else { // Remove that keyword
               $("themekey").value = $("themekey").value.replace(' or '+ k, '');
               $("themekey").value = $("themekey").value.replace(k, '');
               pos = $("themekey").value.indexOf(" or ");
               if (pos === 0){
                       $("themekey").value = $("themekey").value.substring (4, $("themekey").value.length);
               }
       }
  }

  /**
   * Used for fields other than themekey that would like to pop up a 
	 * selector of values retrieved from the index
   */
  function selectorCheck(k, check, input, prep){
	k = '"'+ k + '"';
	//alert (k+"-"+check);
	if (check){	// add the keyword to the list
		if ($(input).value != '') {// add the prep keyword
			$(input).value += ' '+prep+' '+k;
		} else {
			$(input).value = k;
		}
	}else{ // Remove that keyword
		$(input).value = $(input).value.replace(' '+prep+' '+ k, '');
		$(input).value = $(input).value.replace(k, '');
		pos = $(input).value.indexOf(' '+prep+' ');
		if (pos == 0){
			$(input).value = $(input).value.substring (prep.length + 2, $(input).value.length);
		}
	}
  }

function setDates(what)
{
	var xfrom = $('dateFrom');
	var xto = $('dateTo');

	var extfrom = $('extFrom');
	var extto = $('extTo');

	if (what==0) //anytime
	{
		xfrom.value = "";
		xto.value = "";
		extfrom.value = "";
		extto.value = "";
		return;
	}
	//BUILDS to DATE STRING AND UPDATES INPUT
	today=new Date();
	fday = today.getDate();
	if (fday.toString().length==1)
		fday = "0"+fday.toString();
	fmonth = today.getMonth()+1; //Month is 0-11 in JavaScript
	if (fmonth.toString().length==1)
		fmonth = "0"+fmonth.toString();
	fyear = today.getYear();
	if (fyear<1900)
		fyear = fyear + 1900;

	var todate = fyear+"-"+fmonth+"-"+fday+"T23:59:59";
	var fromdate = (fyear-10)+"-"+fmonth+"-"+fday+"T00:00:00";
	xto.value = todate;
	xfrom.value = fromdate;
	extto.value = todate;
	extfrom.value = fromdate;
}




/*
 *Check and uncheck selected metadata
 */
function check(status) {
	var checks = $('search-results-content').getElementsByTagName('INPUT');
	var checksLength = checks.length;
	for (var i = 0; i < checksLength; i++) {
		checks[i].checked = status;
	}
}
function metadataselect(id, selected){
	if (selected===true)
		selected='add';
	else if (selected===false)
		selected='remove';
	var param = 'id='+id+'&selected='+selected;
	var http = new Ajax.Request(
		Env.locService +'/'+ 'metadata.select',
		{
			method: 'get',
			parameters: param,
			onComplete: function(originalRequest){
				// console.log('onComplete');
			},
			onLoaded: function(originalRequest){
				// console.log('onLoaded');
			},
			onSuccess: function(originalRequest){
				var xmlString = originalRequest.responseText;

				// convert the string to an XML object
				var xmlobject = (new DOMParser()).parseFromString(xmlString, "text/xml");
				// get the XML root item
				var root = xmlobject.getElementsByTagName('response')[0];
				var nbSelected = root.getElementsByTagName('Selected')[0].firstChild.nodeValue;
				var item = document.getElementById('nbselected');
				item.innerHTML = nbSelected;
		},
		onFailure: function(originalRequest){
			alert(translate('metadataSelectionError'));
		}
	});
	if (selected=='remove-all') {
		check(false);
	};
	if (selected=='add-all') {
		check(true);
	};
}

function toggleMoreFields() {
  $("all_search_row").toggle();
  $("phrase_search_row").toggle();
  $("without_search_row").toggle();

  var src = $("i_morefields").getAttribute('src');
  var ndx = src.lastIndexOf('/');

  src = src.substring(0, ndx+1);

  if ($("all_search_row").visible() == true) {
	$("i_morefields").setAttribute('src', src +'minus.png');
  } else {
	$("i_morefields").setAttribute('src', src +'plus.gif');
  }
}

function toggleInspire() {
  $("inspiresearchfields").toggle();

  var src = $("i_inspire").getAttribute('src');
  var ndx = src.lastIndexOf('/');

  src = src.substring(0, ndx+1);

  if ($("inspiresearchfields").visible() == true) {
	$("i_inspire").setAttribute('src', src +'minus.png');
  } else {
	$("i_inspire").setAttribute('src', src +'plus.gif');
  }
}

function toggleWhen() {
  $("whensearchfields").toggle();
 
  var src = $("i_when").getAttribute('src');
  var ndx = src.lastIndexOf('/');

  src = src.substring(0, ndx+1);

  if ($("whensearchfields").visible() == true) {
	$("i_when").setAttribute('src', src +'minus.png');
  } else {
	$("i_when").setAttribute('src', src +'plus.gif');
  }
}

function addWMSLayer(layers) {
	Ext.getCmp("north-map-panel").expand();
    GeoNetwork.mapViewer.addWMSLayer(layers);
}

function addWMSServerLayers(url) {
	Ext.getCmp("north-map-panel").expand();
	mainViewport.doLayout();
    GeoNetwork.mapViewer.addWMSServerLayers(url);
}

function addSelectedWMSLayers(metadataIdForm) {
	var checkedBoxes = $$('#'+metadataIdForm+' input');
	var wmsLayers = new Array();
	for (var i=0;i<checkedBoxes.length;i++) {
		if (checkedBoxes[i].checked) {
			wmsLayers.push(checkedBoxes[i].value.split(','));
		}
	}
	addWMSLayer(wmsLayers);
}

/********************************************************************
* 
*  Show list of addable interactive maps
*
********************************************************************/

/**
 * This method is called by the "Interactive map [+]" button in a displayed metadata.
 * It will display the metadata distribution info in a div .
 *  
 * @param {int} id   		The Geonetwork metadata id
 */
function gn_showInterList(id) 
{
    var pars = 'id=' + id + "&currTab=distribution";
    
    // Change button appearance
    $('gn_showinterlist_' + id) .hide();
    $('gn_loadinterlist_' + id) .show();
    
    var myAjax = new Ajax.Request(
        getGNServiceURL('metadata.show.embedded'), 
        {
            method: 'get',
            parameters: pars,
            onSuccess: function (req) {
                // This is a normally invisible DIV below every MD 
                var parent = $('ilwhiteboard_' + id);
                clearNode(parent);
                parent.show();
                
                $('gn_loadinterlist_' + id) .hide();
                $('gn_hideinterlist_' + id) .show();
                
                // create new element
                var div = document.createElement('div');
                div.className = 'metadata_current';
				div.style.width = '100%'; 
                $(div).hide();
                parent.appendChild(div);
                
                div.innerHTML = req.responseText;
                Effect.BlindDown(div);
                
                var tipman = new TooltipManager();
                ker.loadMan.wait(tipman);
            },
            onFailure: gn_search_error// FIXME
        });
}

/**
 * This method is called by the "Interactive map [-]" button in a displayed metadata.
 * It will hide and delete the div displaying the metadata distribution info.
 *  
 * @param {int} id   		The Geonetwork metadata id
 */
function gn_hideInterList(id) 
{
    var parent = $('ilwhiteboard_' + id);
    var div = parent.firstChild;
    Effect.BlindUp(div, { afterFinish: function (obj) {
            clearNode(parent);
            $('gn_showinterlist_' + id) .show();
            $('gn_hideinterlist_' + id) .hide();
        }
    });
}

/********************************************************************/
/* INSPIRE                                                          */
/********************************************************************/
function showInspireSearch() {
   var inspire = $('inspire');
   if(inspire.checked) {
       inspire.value = 'true';
   } else {
       inspire.value = '';
   }

}

function inspireAnnexChanged(inspireannex) {
    var inspire = $('inspire');
    if (inspireannex!="") {
        // we have an Annex, so only INSPIRE metadata can be queried
        //    let the user know this, by checking the checkbox
        if (inspire) inspire.checked = true;
    } else {
        if (inspire) inspire.checked = false;
    }
}

function inspireClassificationDataServiceChanged(classification) {
    setParam('keyword', classification);
}

function taggleVisibility(elementId) {
	var element = $(elementId);
	if(element != null) {
		if(element.style.display == "none") {
			element.style.display = "block";
		}
		else {
			element.style.display = "none";
		}
	}
	else {
		return;
	}
}

function clearNode(node)
{
	var enode = $(node);
	while (enode.firstChild) 
	{
		enode.removeChild(enode.firstChild);
	}			
}

function im_mm_getURLselectedbbox(northBL, southBL, eastBL, westBL)
{
    return "geometry=POLYGON(( " + $("westBL").value + " "  + $("northBL").value + ", " +
            $("eastBL").value + " " + $("northBL").value + ", " +
            $("eastBL").value + " " + $("southBL").value + ", " +
            $("westBL").value + " " + $("southBL").value + ", " +
            $("westBL").value + " "  + $("northBL").value + "))";
}

function inspireSourceTypeChanged(sourceType) {
    if (sourceType == 'dataset') {
        $("serviceType").value = '';
        $("serviceType").disable();
        $("serviceTypeLabel").className = "labelFieldDisabled";

        $("classificationDataService").value = '';
        $("classificationDataService").disable();
        $("classificationDataServiceLabel").className = "labelFieldDisabled";

    } else {
        $("serviceType").enable();
        $("serviceTypeLabel").className = "labelField";

        $("classificationDataService").enable();
        $("classificationDataServiceLabel").className = "labelField";
    }
}
/*** EOF ***********************************************************/
