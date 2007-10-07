/********************************************************************
* gn_search.js
*
* This file contains functions related to the dynamic behavior of geonetwork:
* - Metadata search & reset
* - Area of Interest Map behavior
* - metadata present
*
********************************************************************/


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
		'/geonetwork/srv/'+Env.lang+'/main.searchform.advanced.embedded', 
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
		'/geonetwork/srv/'+Env.lang+'/main.searchform.simple.embedded', 
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

/********************************************************************
*** GET BOUNDINGBOX COORDINATES FOR A REGION
********************************************************************/

function doRegionSearch()
{
	var region = $('region').value
	if(region=="") 
	{
		region=null;
    $('northBL').value='90';
    $('southBL').value='-90';
    $('eastBL').value='180';
    $('westBL').value='-180';		
   	im_mm_setAOIandZoom();
	}
	else
	{
   	getRegion(region);
	}
}

function getRegion(region) 
{
    if(region)
        var pars = "id="+region;
        
    var myAjax = new Ajax.Request(
    '/geonetwork/srv/'+Env.lang+'/xml.region.get', {
        method: 'get',
        parameters: pars,
        onSuccess: getRegion_complete,
        onFailure: getRegion_error
    });
}

function getRegion_complete(req) {
    var rlist = $('resultList');
    
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

	im_mm_setAOIandZoom();
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
    im_mm_setAOIandZoom(); 
    $('updateBB').style.visibility="hidden";
  }
}

function AoIrefresh() {
  $('region').value="";
  $('updateBB').style.visibility="visible";
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
		'/geonetwork/srv/'+Env.lang+'/main.search.embedded', 
		{
			method: 'get',
			parameters: pars,
			onSuccess: gn_search_complete,
			onFailure: gn_search_error
		}
	);
}

function gn_present(frompage, topage) 
{
	preparePresent();
	
	var pars = 'from=' + frompage + "&to=" + topage;
	
	var myAjax = new Ajax.Request(
		'/geonetwork/srv/'+Env.lang+'/main.present.embedded', 
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
function gn_showSingleMetadata(id)
{
   var pars = 'id=' + id + '&currTab=simple';

   var myAjax = new Ajax.Request(
        '/geonetwork/srv/'+Env.lang+'/metadata.show.embedded', 
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
        '/geonetwork/srv/'+Env.lang+'/metadata.show.embedded', 
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


/**********************************************************
***
***		STUFF FOR SIMPLE SEARCH
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
function runSimpleSearch() 
{
	preparePresent();

	var pars = "any=" + encodeURIComponent($('any') .value);
	pars += "&"+im_mm_getURLselectedbbox();
	pars += fetchParam('relation');
	pars += "&attrset=geo";
	pars += fetchParam('region');
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
	$('northBL').value='50'; // ETJ ??? what do these values mean ???
	$('southBL').value='-50';
	$('eastBL').value='100';
	$('westBL').value='-100';		
 	im_mm_setAOIandZoom();
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
***
***		STUFF FOR ADVANCED SEARCH
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

function runAdvancedSearch() 
{
	preparePresent();

	var pars = "any=" + encodeURIComponent($('any') .value);
	pars += "&attrset=geo";
	pars += fetchParam('title');
	pars += fetchParam('abstract');
	pars += fetchParam('themekey');
	pars += fetchRadioParam('similarity');

	pars += "&"+im_mm_getURLselectedbbox();
	pars += fetchParam('relation');
	pars += fetchParam('region');

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
	pars += fetchParam('template');
	pars += fetchParam('hitsPerPage');

	// Load results via AJAX
	gn_search(pars);    
}

function resetAdvancedSearch()
{
/* make sure all values are completely reset (instead of just using the default
   form.reset that would only return to the values stored in the session */
	setParam('any','');		
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
 	im_mm_setAOIandZoom();
	setParam('dateFrom','');
	setParam('dateTo','');
	$('radfrom0').checked=true;
	$('radfrom1').disabled='disabled';
	setParam('group','');		
	setParam('category','');		
	setParam('siteId','');		
	$('digital').checked=true;		
	$('paper').checked=false;		
	setParam('template','n');		
	setParam('hitsPerPage','10');		
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

/*** EOF ***********************************************************/