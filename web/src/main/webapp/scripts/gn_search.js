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

/**
 * Trigger PDF search.
 *
 * If run on selection run pdf.selection.search
 * to retrieve the PDF document and clean current selection.
 *
 * If not, use the pdf.search service.
 */
function runPdfSearch(onSelection) {

	if (onSelection) {
		var serviceUrl = getGNServiceURL('pdf.selection.search');
		if ($("advanced_search_pnl").visible()) {
			serviceUrl = serviceUrl + "?" + fetchParam('template');
		}

		location.replace (serviceUrl);
		metadataselect(0, 'remove-all');
	} else {
	    if (document.cookie.indexOf("search=advanced")!=-1)
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

	var pars = "any=" + encodeURIComponent($('any') .value);

	var region = $('region_simple').value;
	if(region!="")
  {
		pars += "&"+im_mm_getURLselectedbbox();
		pars += fetchParam('relation');
		pars += "&attrset=geo";
		if(region!="userdefined")
		{
		pars += fetchParam('region');
	}
	}
	pars += fetchParam('sortBy');
	pars += fetchParam('sortOrder');
	pars += fetchParam('hitsPerPage');
	pars += fetchParam('output');

	if (type == "pdf")
       gn_searchpdf(pars);
    else
	   // Load results via AJAX
	   gn_search(pars);
}

function resetSimpleSearch()
{
/* make sure all values are completely reset (instead of just using the default
   form.reset that would only return to the values stored in the session */
    setParam('any','');
    setParam('relation','overlaps');	
    setParam('region_simple',null);
	// Clear also region in advanced search to keep synch
	setParam('region',null);
    
    $('northBL').value='90';
    $('southBL').value='-90';
    $('eastBL').value='180';
    $('westBL').value='-180';

	resetMinimaps();
	
    // FIXME: maybe we should zoom back to a fullExtent (not to the whole world)
    //im_mm_redrawAoI();
    //im_mm_zoomToAoI();
    setParam('sortBy',      'relevance');
    setParam('sortBy_simple',      'relevance');
    setParam('sortOrder',   '');
    setParam('hitsPerPage', '10');
    setParam('hitsPerPage_simple', '10');
    setParam('output',      'full');
    setParam('output_simple',      'full');

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
}
/********************************************************************
*
*  Toggling between simple/advanced search
*
********************************************************************/

function showAdvancedSearch()
{
	closeSearch('simple_search_pnl');
	openSearch('advanced_search_pnl');
	document.cookie = "search=advanced";
	initAdvancedSearch();
}

function showSimpleSearch()
{
	closeSearch('advanced_search_pnl');
	openSearch('simple_search_pnl');
	document.cookie = "search=default";
	initSimpleSearch();
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

	Calendar.setup({
		inputField     :    "dateFrom",     // id of the input field
		ifFormat       :    "%Y-%m-%dT%H:%M:00",      // format of the input field
		button         :    "from_trigger_c",  // trigger for the calendar (button ID)
		showsTime 		 :		true,
		align          :    "Tl",           // alignment (defaults to "Bl")
		singleClick    :    true
	});

	Calendar.setup({
		inputField	:    "dateTo",     // id of the input field
		ifFormat	:    "%Y-%m-%dT%H:%M:00",      // format of the input field
		button		:    "to_trigger_c",  // trigger for the calendar (button ID)
		showsTime	:    true,
		align		:    "Tl",           // alignment (defaults to "Bl")
		singleClick	:    true
	});

	Calendar.setup({
		inputField     :    "extFrom",     // id of the input field
		ifFormat       :    "%Y-%m-%dT%H:%M:00",      // format of the input field
		button         :    "extfrom_trigger_c",  // trigger for the calendar (button ID)
		showsTime 		 :		true,
		align          :    "Tl",           // alignment (defaults to "Bl")
		singleClick    :    true
	});

	Calendar.setup({
		inputField	:    "extTo",     // id of the input field
		ifFormat	:    "%Y-%m-%dT%H:%M:00",      // format of the input field
		button		:    "extto_trigger_c",  // trigger for the calendar (button ID)
		showsTime	:    true,
		align		:    "Tl",           // alignment (defaults to "Bl")
		singleClick	:    true
	});

}

function runAdvancedSearch(type)
{
    if (type != "pdf")
	   preparePresent();

	setSort();

	//var pars = "any=" + encodeURIComponent($('any') .value);
    var pars = fetchParam('all');
	pars += fetchParam('phrase');
	pars += fetchParam('or');
	pars += fetchParam('without');
	pars += fetchParam('title');
	pars += fetchParam('abstract');
	pars += fetchParam('themekey');
	pars += fetchRadioParam('similarity');

	var region = $('region').value;
	if(region!="")
  {
		pars += "&attrset=geo";
		pars += "&"+im_mm_getURLselectedbbox();
		pars += fetchParam('relation');
		if(region!="userdefined")
		{
		pars += fetchParam('region');
	}
	}

	if($('radfrom1').checked)
	{
		pars += fetchParam('dateFrom');
		pars += fetchParam('dateTo');
	}

	if($('radfromext1').checked)
	{
		pars += fetchParam('extFrom');
		pars += fetchParam('extTo');
	}

	pars += fetchParam('group');
	pars += fetchParam('category');
	pars += fetchParam('siteId');

	pars += fetchBoolParam('digital');
	pars += fetchBoolParam('paper');
	pars += fetchBoolParam('dynamic');
	pars += fetchBoolParam('download');
	pars += fetchParam('protocol').toLowerCase();
	pars += fetchParam('template');
	pars += fetchParam('sortBy');
	pars += fetchParam('sortOrder');
	pars += fetchParam('hitsPerPage');
	pars += fetchParam('output');

     //Inspire
    pars += fetchParam('inspireannex');
    pars += addINSPIREThemes();

	var inspire = $('inspire');

    if (inspire) {
        if (inspire.checked) pars += "&inspire=true";
    }
    // Inspire
    
    if (type == "pdf")
       gn_searchpdf(pars);
    else
	   // Load results via AJAX
	   gn_search(pars);
}

function resetAdvancedSearch()
{
/* make sure all values are completely reset (instead of just using the default
   form.reset that would only return to the values stored in the session */
	//setParam('any','');
    setParam('all','');
	setParam('phrase', '');
	setParam('or', '');
	setParam('without', '');
	setParam('title','');
	setParam('abstract','');
	setParam('themekey','');
	var radioSimil = document.getElementsByName('similarity');
	radioSimil[1].checked=true;
	setParam('relation','overlaps');

    setParam('region',null);
	// Clear also region in simple search to keep synch
	setParam('region_simple',null);
	
	$('northBL').value='90';
	$('southBL').value='-90';
	$('eastBL').value='180';
	$('westBL').value='-180';
	
	resetMinimaps();

	setParam('dateFrom','');
	setParam('dateTo','');
	$('radfrom0').checked=true;
	$('radfrom1').disabled='disabled';
	setParam('extFrom','');
	setParam('extTo','');
	$('radfromext1').disabled='disabled';
	setParam('group','');
	setParam('category','');
	setParam('siteId','');
	$('digital') .checked = false;
	$('paper')   .checked = false;
	$('dynamic') .checked = false;
	$('download').checked = false;
	setParam('protocol',    '');
	setParam('template',    'n');
 	setParam('sortBy',      'relevance');
    setParam('sortBy_simple',      'relevance');
    setParam('sortOrder',   '');
    setParam('hitsPerPage', '10');
    setParam('hitsPerPage_simple', '10');
    setParam('output',      'full');
    setParam('output_simple',      'full');


    // reset INSPIRE options
    resetInspireOptions();
    // End reset INSPIRE options    

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
}

//-------------------------------------------------------------------

function setSortAndSearch()
{
	$('sortBy').value = $F('sortBy.live');
	setSort();
  if (document.cookie.indexOf("search=advanced")!=-1) runAdvancedSearch();
	else runSimpleSearch();
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

function doRegionSearchSimple() {
  doRegionSearch('region_simple');
  $('region').value = $('region_simple').value;
}

function doRegionSearchAdvanced() {
  doRegionSearch('region');
  $('region_simple').value = $('region').value;
}

function doRegionSearch(regionlist)
{
    var region = $(regionlist).value;
    if(region=="")
    {
        region=null;
        $('northBL').value='90';
        $('southBL').value='-90';
        $('eastBL').value='180';
        $('westBL').value='-180';

		GeoNetwork.minimapSimpleSearch.updateExtentBox();
		GeoNetwork.minimapAdvancedSearch.updateExtentBox();

        //im_mm_redrawAoI();
        //im_mm_zoomToAoI();
    }  else if (region=="userdefined") {
		// Do nothing. AoI is set by the user
    } else
    {
        getRegion(region);
    }
}

function getRegion(region)
{
    if(region)
        var pars = "id="+region;

    var myAjax = new Ajax.Request(
        getGNServiceURL('xml.region.get'),
        {
            method: 'get',
            parameters: pars,
            onSuccess: getRegion_complete,
            onFailure: getRegion_error
        }
    );
}

function getRegion_complete(req) {
    //Response received
    var node = req.responseXML;
    var northcc = xml.evalXPath(node, 'response/record/north');
    var southcc = xml.evalXPath(node, 'response/record/south');
    var eastcc = xml.evalXPath(node, 'response/record/east');
    var westcc = xml.evalXPath(node, 'response/record/west');

    $('northBL').value=northcc;
    $('southBL').value=southcc;
    $('eastBL').value=eastcc;
    $('westBL').value=westcc;

	GeoNetwork.minimapSimpleSearch.updateExtentBox();
	GeoNetwork.minimapAdvancedSearch.updateExtentBox();
	
    //im_mm_redrawAoI();
    //im_mm_zoomToAoI();
}

function getRegion_error() {
    alert(translate("error"));
}

function updateAoIFromForm() {
  var nU = Number($('northBL').value);
  var sU = Number($('southBL').value);
  var eU = Number($('eastBL').value);
  var wU = Number($('westBL').value);

  if (nU < sU) { alert(translate("northSouth")); }
  else if (nU > 90) { alert(translate("north90")); }
  else if (sU < -90) { alert(translate("south90")); }
  else if (eU < wU) { alert(translate("eastWest")); }
  else if (eU > 180) { alert(translate("east180")); }
  else if (wU < -180) { alert(translate("west180")); }
  else
  {
    im_mm_redrawAoI();
    im_mm_zoomToAoI();
    $('updateBB').style.visibility="hidden";
  }
}

function AoIrefresh() {
  $('region').value="userdefined";
  $('updateBB').style.visibility="visible";
}

// Update the dropdown list
function im_mm_aoiUpdated(bUpdate) {
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
    pars = pars.replace(/hitsPerPage=\d{2,3}/, 'hitsPerPage=9999');
    // Update this value if more document required in PDF output
    // FIXME : Should be defined in service config.
    location.replace (
        getGNServiceURL('pdf.search') + "?" + pars
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

   var myAjax = new Ajax.Request(
        getGNServiceURL('metadata.show.embedded'),
        {
            method: 'get',
            parameters: pars,
            onSuccess: function (req) {
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
	alert("ERROR " + req.responseText);
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

/**********************************************************
*** Keywords
**********************************************************/

  var keyordsSelected = false;

  function addQuote (li){
  $("themekey").value = '"'+li.innerHTML+'"';
  }

  /**
   * Place popup according to input element position.
   * Load list of keywords
   *
   * @param el
   * @param pop
   * @return
   */
  function popKeyword(el, pop){
    if (pop.style.display == "block") {
        pop.style.display = "none";
        return false;
    }

    //pop.style.top = el.cumulativeOffset().top + el.getHeight();
    //pop.style.left = el.cumulativeOffset().left;
    pop.style.width = '250px'; //el.getWidth();
    pop.style.display = "block";

	if (!keyordsSelected){
		new Ajax.Updater("keywordSelector","portal.search.keywords?mode=selector&keyword="+$("themekey").value);
		keyordsSelected = true;
	}
  }

  function keywordCheck(k, check){
	k = '"'+ k + '"';
	//alert (k+"-"+check);
	if (check){	// add the keyword to the list
		if ($("themekey").value != '') // add the "or" keyword
			$("themekey").value += ' or '+ k;
		else
			$("themekey").value = k;
	}else{ // Remove that keyword
		$("themekey").value = $("themekey").value.replace(' or '+ k, '');
		$("themekey").value = $("themekey").value.replace(k, '');
		pos = $("themekey").value.indexOf(" or ");
		if (pos == 0){
			$("themekey").value = $("themekey").value.substring (4, $("themekey").value.length);
		}
	}
  }


 /*sets date string (user defined 'from' date to Now()) in advanced search [0: any;1: after; 2: change sel

 Function extracted by the current FAO site and adapted
 */

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
	mainViewport.doLayout();
    GeoNetwork.mapViewer.addWMSLayer(layers);
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

function inspireOrganisationChanged(groupId) {
	setParam('group',groupId);
}

// TODO: document asspumtion: there is an input field 'type'
function inspireBrontypeChanged(brontype) {
    setParam('type',brontype);
}

// TODO: document asspumtion: there is an input field 'protocol'
function inspireServiceTypeChanged(servicetype) {
    setParam('protocol',servicetype);
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

function addINSPIREThemes() {
	var allThemes = '';
	var prefix = '&inspiretheme=';
    // Select all checkboxes in inspirethemesdiv
    var inspireThemeChk = $$('#inspirethemesdiv input[type="checkbox"]');
    // console.log(inspireThemeChk.length);
    for (i=0;i<inspireThemeChk.length;i++) {
        if (inspireThemeChk[i].checked) {
            allThemes += prefix + inspireThemeChk[i].value+"*";
        }
    }
	return allThemes ;
}

function resetInspireOptions() {
     // reset INSPIRE options
	$('inspire').checked=false;
	setParam('title','');
	setParam('inspireannex','');
	setParam('inspirebrontype','');
	setParam('protocol','');
	setParam('orgselect_inspire','');

	$('inspire_GeographicalNames').checked=false;
	$('inspire_AdministrativeUnits').checked=false;
	$('inspire_Addresses').checked=false;
	$('inspire_CadastralParcels').checked=false;
	$('inspire_TransportNetworks').checked=false;
	$('inspire_Hydrography').checked=false;
	$('inspire_ProtectedSites').checked=false;
	$('inspire_Elevation').checked=false;
	$('inspire_LandCover').checked=false;
	$('inspire_Orthoimagery').checked=false;
	$('inspire_Geology').checked=false;
	$('inspire_StatisticalUnits').checked=false;
	$('inspire_Buildings').checked=false;
	$('inspire_Soil').checked=false;
	$('inspire_LandUse').checked=false;
	$('inspire_HumanHealthAndSafety').checked=false;
	$('inspire_UtilityAndGovernmentServices').checked=false;
	$('inspire_EnvironmentalMonitoringFacilities').checked=false;
	$('inspire_ProductionAndIndustrialFacilities').checked=false;
	$('inspire_AgriculturalAndAquacultureFacilities').checked=false;
	$('inspire_PopulationDistribution-Demography').checked=false;
	$('inspire_AreaManagementRestrictionRegulationZonesAndReportingUnits').checked=false;
	$('inspire_NaturalRiskZones').checked=false;
	$('inspire_AtmosphericConditions').checked=false;
	$('inspire_MeteorologicalGeographicalFeatures').checked=false;
	$('inspire_OceanographicGeographicalFeatures').checked=false;
	$('inspire_SeaRegions').checked=false;
	$('inspire_Bio-geographicalRegions').checked=false;
	$('inspire_HabitatsAndBiotopes').checked=false;
	$('inspire_SpeciesDistribution').checked=false;
	$('inspire_EnergyResources').checked=false;
	$('inspire_MineralResources').checked=false;
	$('inspire_MineralResources').checked=false;
	$('inspire_MineralResources').checked=false;
	// End reset INSPIRE options
}

function clearNode(node)
{
	var enode = $(node);
	while (enode.firstChild) 
	{
		enode.removeChild(enode.firstChild);
	}			
}

function im_mm_getURLselectedbbox()
{
	return urlizebb(
		$("northBL").value,
		$("eastBL").value,
		$("southBL").value,
		$("westBL").value);
}

/**
 * Get the URLized version of the given bbox
 * @param {int} n
 * @param {int} e
 * @param {int} s
 * @param {int} w
 * @return {String} URL
 */
 function urlizebb(n, e, s, w)
{
	return	"northBL="+n+
			"&eastBL="+e+
			"&southBL="+s+
			"&westBL="+w;
};
/*** EOF ***********************************************************/
