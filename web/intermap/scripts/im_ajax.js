/*****************************************************************************
 *
 *       This file contains some of the Intermap's AJAX calls.
 *
 * Requires:
 *   getIMServiceURL(service)
 *****************************************************************************/


im_load_error = function() {alert("Loading error");};


/*****************************************************************************
 *                                                                           *
 *                                  Layers                                   *
 *                                                                           *
 *****************************************************************************/

/*
// Not used at the moment
// TODO: Should we restore it adding a button in each layer's tab? 

function imc_zoomToLayer(layerId)
{
	deleteAoi();
	//unsetAoi();
	//$('im_geonetRecords').className = 'hidden';	
	
	setStatus('busy');
	var pars = 'id=' + layerId;
	
	var myAjax = new Ajax.Request (
		getIMServiceURL('map.zoomToService'),
		{
			method: 'get',
			parameters: pars,
			onComplete: function(req) { im_bm_refreshNeeded(true);}, // FIXME
			onFailure: reportError
		}
	);
}

*/

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


/*****************************************************************************
 *                                                                           *
 *****************************************************************************/

function imc_addService(surl, service, type, callback)
{
	var pars = 'url=' + encodeURIComponent(surl) 
			+ '&service=' + encodeURIComponent(service) 
			+ '&type=' + type;
			
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

function imc_addServices(surl, serviceArray, type, callback)
{
	var pars = 'url=' + encodeURIComponent(surl) + '&type=' + type;
	
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
