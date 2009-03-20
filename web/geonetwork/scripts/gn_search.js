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

function initSimpleSearch(wmc)
{
	im_mm_init(wmc, function()
			{
				$('openIMBtn').style.cursor = 'pointer';
				Event.observe('openIMBtn', 'click',  function(){openIntermap()} );
			}
		);		
}

function gn_anyKeyObserver(e)
{
	if(e.keyCode == Event.KEY_RETURN)
		runSimpleSearch();
}

/*  */
function runPdfSearch() {
    if (document.cookie.indexOf("search=advanced")!=-1)
        runAdvancedSearch("pdf");
    else
        runSimpleSearch("pdf");
}

function runSimpleSearch(type) 
{
    if (type != "pdf")
        preparePresent();

	var pars = "any=" + encodeURIComponent($('any') .value);

	var region = $('region').value;
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
    setParam('region',null);
    
    $('northBL').value='90'; 
    $('southBL').value='-90';
    $('eastBL').value='180';
    $('westBL').value='-180';		
    
    // FIXME: maybe we should zoom back to a fullExtent (not to the whole world)  
    im_mm_redrawAoI();
    im_mm_zoomToAoI(); 	
 	setParam('sortBy',      'relevance');
 	setParam('sortOrder',   '');
 	setParam('hitsPerPage', '10');
 	setParam('output',      'full');
}

/********************************************************************
* 
*  Toggling between simple/advanced search
*
********************************************************************/

function showAdvancedSearch()
{
	closeIntermap();
	closeSearch('simplesearch');
	document.cookie = "search=advanced";

	var myAjax = new Ajax.Updater (
		'advancedsearch',    
		getGNServiceURL('main.searchform.advanced.embedded'), 
		{
			method: 'get',    		    	
			onComplete: function()
			{
				openSearch('advancedsearch');
				initAdvancedSearch();				
			},
			onFailure: im_load_error
		}
	);	
 
}

function showSimpleSearch()
{
	closeSearch('advancedsearch');
	document.cookie = "search=default";
	
	var myAjax = new Ajax.Updater (
		'simplesearch',    
		getGNServiceURL('main.searchform.simple.embedded'), 
		{
			method: 'get',
			onComplete: function()
			{
				openSearch('simplesearch');
				initSimpleSearch();								
			},
			onFailure: im_load_error
		}
	);	
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
	clearNode('im_mm_map');

	if( ! Prototype.Browser.IE )
	{
		Effect.BlindUp($(s), {afterFinish: function(){ clearNode($(s)); } });
	}
	else
	{    
		$(s).hide();	
		clearNode($(s))	    
	}
}

/**********************************************************
***
***		ADVANCED SEARCH
***
**********************************************************/

function initAdvancedSearch()
{
	im_mm_init();

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

}

function runAdvancedSearch(type) 
{
    if (type != "pdf")
	   preparePresent();

	var pars = "any=" + encodeURIComponent($('any') .value);
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
	setParam('any','');
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
	$('northBL').value='90';
	$('southBL').value='-90';
	$('eastBL').value='180';
	$('westBL').value='-180';
            im_mm_redrawAoI();
            im_mm_zoomToAoI();	
 	
	setParam('dateFrom','');
	setParam('dateTo','');
	$('radfrom0').checked=true;
	$('radfrom1').disabled='disabled';
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
 	setParam('sortOrder',   '');
 	setParam('hitsPerPage', '10');
 	setParam('output',      'full');
}

/**********************************************************
***
***		RATING
***
**********************************************************/

function showOptions()
{
	var img = $('options.img');
	var src = img.getAttribute('src');
	var ndx = src.lastIndexOf('/');
	var div = $('options.div');
	
	src = src.substring(0, ndx+1);
	
	if (div.visible())	img.setAttribute('src', src +'plus.gif');
		else					img.setAttribute('src', src +'minus.png');
	
	div.toggle();
}

//-------------------------------------------------------------------

function setSortAndSearch()
{
	$('sortBy').value = $F('sortBy.live');
	if ($('sortBy').value=='title') 
	   $('sortOrder').value = 'reverse'; 
	else 
	   $('sortOrder').value = '';
	
	if ($('protocol') == null)	runSimpleSearch();
		else							runAdvancedSearch();
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
				document.body.appendChild(p);
				
				ratingPopup = p;
				
				setTimeout(ker.wrap(this, function(){ showRatingPopup(id); }), 10);	
			}));
		
		return;
	}
			
	var pos = Position.cumulativeOffset($('rating.link.'+ id));

	ratingPopup.style.left = pos[0] -100;
	ratingPopup.style.top  = pos[1] +16;
	
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
		ker.showError('Cannot rate metadata', xmlRes);
	else
		hideRatingPopup();
}


/********************************************************************
*** GET BOUNDINGBOX COORDINATES FOR A REGION
********************************************************************/

function doRegionSearch()
{
    var region = $('region').value;
    if(region=="") 
    {
        region=null;
        $('northBL').value='90';
        $('southBL').value='-90';
        $('eastBL').value='180';
        $('westBL').value='-180';
        
        im_mm_redrawAoI();
        im_mm_zoomToAoI();        
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

    im_mm_redrawAoI();
    im_mm_zoomToAoI();
}

function getRegion_error() {
    alert("ERROR)");
}

function updateAoIFromForm() {
  var nU = Number($('northBL').value);
  var sU = Number($('southBL').value);
  var eU = Number($('eastBL').value);
  var wU = Number($('westBL').value);
  
  if (nU < sU) { alert("North < South"); } 
  else if (nU > 90) { alert("North > 90 degrees"); }
  else if (sU < -90) { alert("South < -90 degrees"); }
  else if (eU < wU) { alert("East < West"); } 
  else if (eU > 180) { alert("East > 180 degrees"); }
  else if (wU < -180) { alert("West < -180 degrees"); }
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

/********************************************************************
*** DO THE SEARCH!
********************************************************************/

function preparePresent() 
{
	// Display results area
	clearNode('resultList');
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
   var pars = 'uuid=' + uuid + '&currTab=simple';
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
            },
            onFailure: gn_search_error// FIXME
        });
}

function gn_showMetadata(id) 
{
    var pars = 'id=' + id + '&currTab=simple';
    
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

function gn_search_error() {
    $('loadingMD') .hide();
// style.display = 'none';
    alert("ERROR)");
}

function gn_filteredSearch() {
	var myAjax = new Ajax.Request(
		getGNServiceURL('selection.search'), 
		{
			method: 'get',
			parameters: '',
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

	var pars = "any=''";
	pars += "&category="+category;
	
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

  function keywordSelector(){
	if ($("keywordSelectorFrame").style.display == 'none'){
		if (!keyordsSelected){
			new Ajax.Updater("keywordSelector","portal.search.keywords?mode=selector&keyword="+$("themekey").value);
			keyordsSelected = true;
		}
		$("keywordSelectorFrame").show();
	}else{
		$("keywordSelectorFrame").hide();
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
	
	if (what==0) //anytime 
	{ 
		xfrom.value = "";
		xto.value = "";
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
			alert('Error on metadata selection.'); // TODO : translate
		}
	});
	if (selected=='remove-all') {
		check(false);
	};
	if (selected=='add-all') {
		check(true);
	};
}
/*** EOF ***********************************************************/