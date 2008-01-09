/*****************************************************************************
 *
 *       This file contains some of the Intermap's AJAX calls.
 *
 * Requires:
 *   getIMServiceURL(service)
 *****************************************************************************/


im_load_error = function() {alert("Loading error");};


function im_checkError(req)
{
	var doc = req.responseXML;	
	if(!doc) // It's not XML 
	{
		return false;
	}	
	
	if(doc.firstChild.tagName=="error")
	{ 
		return true;
	}
	
	// TODO: refine check
	
	return false;	
}

function im_showError(req)
{
	var doc = req.responseXML;	
	if(!doc) // It's not XML 
	{
		return false;
	}	
	
	if(doc.firstChild.tagName=="error")
	{ 
		var err = doc.firstChild;
		var mess = err.getElementsByTagName('message')[0].textContent;
		var clss = err.getElementsByTagName('class')[0].textContent;
		
		alert("Intermap error: " + mess + "\nException: "+ clss);		
		return true;
	}
	
	// TODO: refine check
	
	return false;	
}

/*****************************************************************************
 *                                                                           *
 *                                  Layers                                   *
 *                                                                           *
 *****************************************************************************/

function im_setWMC(wmc, callback) 
{
    var pars = 'wmc=' + wmc;               //encodeURIComponent(surl) 
    		
    var myAjax = new Ajax.Request (
    	getIMServiceURL('wmc.setContext'),
    	{
    		method: 'get',
    		parameters: pars,
    
    		onComplete: callback,
    		onFailure: reportError
    	}
    );    
}

function imc_setContextFromURL(url, callback) 
{
	var pars = 'url=' + encodeURIComponent(url);
    		
    var myAjax = new Ajax.Request (
    	getIMServiceURL('wmc.setContextFromURL'),
    	{
    		method: 'post',
    		parameters: pars,
    
    		onComplete: callback,
    		onFailure: reportError
    	}
    );    
}

/*****************************************************************************
 *                                                                           *
 *****************************************************************************/

/**
 * 
 * @param {String} surl
 * @param {String} service
 * @param {int} type
 * @param {boolean} doClearContext
 * @param {function} callback
 */
function imc_addService(surl, service, type, doClearContext, callback)
{
	var pars = 'url=' + encodeURIComponent(surl) 
			+ '&service=' + encodeURIComponent(service) 
			+ '&type=' + type;
			
	if(doClearContext===true)
	{
		pars += "&clear=true";		
	}
			
	var myAjax = new Ajax.Request (
		getIMServiceURL('map.addServices.embedded'),
		{
			method: 'get',
			parameters: pars,

			onComplete: callback,
			onFailure: reportError
		}
	);

}

function imc_addServices(surl, serviceArray, type, /*doClearContext,*/ callback)
{
	var pars = 'url=' + encodeURIComponent(surl) 
			 + '&type=' + type;

//	if(doClearContext===true)
//	{
//		pars += "&clear=true";		
//	}
	
	serviceArray.each(
	    function(service)
	    {
	        pars += '&service=' + encodeURIComponent(service);
	    }
	);
	
	var myAjax = new Ajax.Request (
		getIMServiceURL('map.addServices.embedded'),
		{
			method: 'post',
			parameters: pars,

			onComplete: callback,
			onFailure: reportError
		}
	);
}

function imc_loadServices( id, callback )
{
	var myAjax = new Ajax.Request (
		getIMServiceURL('mapServers.getServices.xml'),
		{
			parameters: "mapserver="+id,
			method: 'get',

			onComplete: callback,
			onFailure: reportError
		}
	);

}
