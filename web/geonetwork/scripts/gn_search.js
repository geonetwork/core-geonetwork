/********************************************************************
* gn_search.js
*
* This file contains functions related to the dynamic behavior of geonetwork:
* - metadata search
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
	
	var myAjax = new Ajax.Updater (
		'advancedsearch',    
		'/geonetwork/srv/en/main.searchform.advanced.embedded', 
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
	
	var myAjax = new Ajax.Updater (
		'simplesearch',    
		'/geonetwork/srv/en/main.searchform.simple.embedded', 
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
		'/geonetwork/srv/en/main.search.embedded', 
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
		'/geonetwork/srv/en/main.present.embedded', 
		{
			method: 'get',
			parameters: pars,
			onSuccess: gn_search_complete,
			onFailure: gn_search_error
		}
	);
}


function gn_search_complete(req) {
    // remove all previous children
    //clearResultList();
    
    var rlist = $('resultList');
    
    rlist.innerHTML = req.responseText;
    
    $('loadingMD').hide();
}

/*function gn_toggleMetadata(id) 
{
    var parent = $('mdwhiteboard_' + id);
    if (parent.firstChild)
        gn_hideMetadata(id);
    else
        gn_showMetadata(id);
}
*/

/********************************************************************
* 
*  Show metadata content
*
********************************************************************/

function gn_showMetadata(id) 
{
    var pars = 'id=' + id + '&currTab=simple';
    
    $('gn_showmd_' + id) .hide();
    $('gn_loadmd_' + id) .show();
    
    var myAjax = new Ajax.Request(
        '/geonetwork/srv/en/metadata.show.embedded', 
        {
            method: 'get',
            parameters: pars,
            onSuccess: function (req) {
                // remove previous open md
                //var prev = document.getElementById('metadata_current');
                //if(prev)
                //	prev.parentNode.removeChild($('metadata_current'));
                
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

/********************************************************************
* 
*  Show list of addable interactive maps
*
********************************************************************/

function gn_showInterList(id) 
{
    var pars = 'id=' + id + "&currTab=distribution";
    
    $('gn_showinterlist_' + id) .hide();
    $('gn_loadinterlist_' + id) .show();
    
    var myAjax = new Ajax.Request(
        '/geonetwork/srv/en/metadata.show.embedded', 
        {
            method: 'get',
            parameters: pars,
            onSuccess: function (req) {
                // remove previous open md
                //var prev = document.getElementById('metadata_current');
                //if(prev)
                //	prev.parentNode.removeChild($('metadata_current'));
                
                var parent = $('ilwhiteboard_' + id);
                clearNode(parent);
                
                parent.show();
                
                $('gn_loadinterlist_' + id) .hide();
                $('gn_hideinterlist_' + id) .show();
                
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


/**********************************************************
***
***		STUFF FOR SIMPLE SEARCH
***
**********************************************************/

function initSimpleSearch()
{
	im_mm_init(function()
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
	pars += "&attrset=geo";

	var region = $('region').value;
	if(region!="") 
		pars += "&region="+region;
	
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
		inputField     :    "datefrom",     // id of the input field
		ifFormat       :    "%Y-%m-%dT00:00:00",      // format of the input field           
		button         :    "from_trigger_c",  // trigger for the calendar (button ID)
		showsTime 		 		: 				true,
		align          :    "Tl",           // alignment (defaults to "Bl")
		singleClick    :    true
	});
	
	Calendar.setup({
		inputField	:    "dateto",     // id of the input field
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
		pars += fetchParam('datefrom');
		pars += fetchParam('dateto');
	}

	pars += fetchParam('group');
	pars += fetchParam('category');
	pars += fetchParam('siteId');
	
	pars += fetchBoolParam('digital');
	pars += fetchBoolParam('paper');

	// Load results via AJAX
	gn_search(pars);    
}

function fetchParam(p)
{
	var t = $(p).value;
	if(t)
		return "&"+p+"="+encodeURIComponent(t);
	else 
		return "";
}

function fetchBoolParam(p)
{
	if($(p).checked )
		return "&"+p+"=on";
	else 
		return "&"+p+"=off";
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
	var xfrom = $('datefrom');
	var xto = $('dateto');
	
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