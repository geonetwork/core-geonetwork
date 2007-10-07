/*****************************************************************************
 *
 *       This file contains most of the Intermap's AJAX calls.
 *
 *****************************************************************************/


var activeLayerId = null; // active layer

im_load_error = function() {alert("Loading error")};

function imc_reloadLayers()
{
/*	var url = '/intermap/srv/'+Env.lang+'/map.layers.getOrder';*/
	var url = '/intermap/srv/'+Env.lang+'/map.getLayers.embedded';	
	var myAjax = new Ajax.Request (
		url, 
		{
			method: 'get',
			onComplete: im_buildLayerList
		}
	);
}


/*****************************************************************************
 *                                                                           *
 *                                  Layers                                   *
 *                                                                           *
 *****************************************************************************/


function imc_zoomToLayer(layerId)
{
	deleteAoi();
	//unsetAoi();
	//$('im_geonetRecords').className = 'hidden';	
	
	setStatus('busy');
	var url = '/intermap/srv/'+Env.lang+'/map.zoomToService';
	var pars = 'id=' + layerId;
	
	var myAjax = new Ajax.Request (
		url,
		{
			method: 'get',
			parameters: pars,
			onComplete: function(req) { refreshNeeded(true) },
			onFailure: reportError
		}
	);
}

function toggleVisibility(id) {
	var url = '/intermap/srv/'+Env.lang+'/map.layers.toggleVisibility';
	var pars = 'id=' + id;
	
	var myAjax = new Ajax.Request (
		url, 
		{
			method: 'get',
			parameters: pars,
			onComplete: function(request) { setLayerVisibility(request, id); },
			onFailure: reportError
		}
	);
}

function showActiveLayerLegend(id) {
    showLegend(activeLayerId);
}

function hideLegend()
{
    var div=$('im_legendPopup');
    if(div)
    {
        Event.stopObserving(div, 'click', hideLegend);
        document.body.removeChild(div);
    }
}

function showLegend(url, btn) {
//	window.open('/intermap/srv/'+Env.lang+'/map.service.getLegend?id=' + id, 'dialog', 'HEIGHT=300,WIDTH=400,scrollbars=yes,toolbar=yes,status=yes,menubar=yes,location=yes,resizable=yes');
    hideLegend();

    var div = document.createElement("div");
    div.id="im_legendPopup";
    div.style.position = "absolute";
    document.body.appendChild(div);    

    var offset = Position.cumulativeOffset($(btn));
    var x = offset[0];
    var y = offset[1];

    div.style.left=x+"px";
    div.style.top=y+"px";
    div.style.width="100px";
    div.style.height="50px";
//    div.style.border="solid black 1px";
//    div.style.background-color="064377";
    
    var img = document.createElement("img");
//    img.src = "intermap/images/waiting.gif";
    img.src = url;
    img.alt = "Loading legend..."
    img.style.position = "absolute";
    img.style.border="solid black 1px";
    div.appendChild(img);    

    Event.observe(div, 'click', hideLegend);
    

    //window.open(url);
	
	
}


/*****************************************************************************
 *                                                                           *
 *                                Delete layer                               *
 *                                                                           *
 *****************************************************************************/


/*****************************************************************************
 *                                                                           *
 *                                 Add layer                                 *
 *                                                                           *
 *****************************************************************************/


/*****************************************************************************
 *                                                                           *
 *                            Layer transparency                             *
 *                                                                           *
 *****************************************************************************/

function imc_setLayerTransparency(id, transparency)
{
	var url = '/intermap/srv/'+Env.lang+'/map.layers.setTransparency';
	var pars = 'id=' + id + '&transparency=' + transparency / 100.0;
	
	var myAjax = new Ajax.Request (
		url, 
		{
			method: 'get',
			parameters: pars,
			onComplete: refreshNeeded,
			onFailure: reportError
		}
	);
}


/*****************************************************************************
 *                                                                           *
 *                    Map operations (zoom, pan, identify)                   *
 *                                                                           *
 *****************************************************************************/

// Starts ajax transaction to perform a map action (zoomin, zoomout)
function imc_bm_action(tool, xmin, ymin, xmax, ymax, w, h)
{
	var url = '/intermap/srv/'+Env.lang+'/map.action';
	var pars = 'maptool=' + tool + 
	                '&mapimgx=' + xmin + '&mapimgy=' + ymin + 
	                '&mapimgx2=' + xmax + '&mapimgy2=' + ymax + 
	                "&width=" + w + "&height="+h +
	                "&"+im_bm_getURLbbox(); // FIXME: we should pass bb as param 
	
	var myAjax = new Ajax.Request (
		url, 
		{
			method: 'get',
			parameters: pars,
			onComplete: function(req) 
			{
				updateMapImage(req);			
			    	im_mm_followBM();  
			},
			onFailure: reportError
		}
	);
}

function imc_mm_action(tool, xmin, ymin, xmax, ymax, w, h)
{
	var url = '/intermap/srv/'+Env.lang+'/map.action';
	var pars = 'maptool=' + tool + 
	                '&mapimgx=' + xmin + '&mapimgy=' + ymin + 
	                '&mapimgx2=' + xmax + '&mapimgy2=' + ymax + 
	                "&width=" + w + "&height="+h +
	                "&"+im_mm_getURLbbox(); // FIXME: we should pass bb as param 
	
	var myAjax = new Ajax.Request (
		url, 
		{
			method: 'get',
			parameters: pars,
			onComplete: im_mm_imageRebuilt,
			onFailure: reportError
		}
	);
}

function imc_bm_move(deltax, deltay, width, height, qbbox)
{
	var url = '/intermap/srv/'+Env.lang+'/map.move';
	var pars = 'deltax=' + deltax + '&deltay=' + deltay +
	                "&width=" + width + "&height=" + height +
	                "&" + qbbox;
	
	var myAjax = new Ajax.Request (
		url, 
		{
			method: 'get',
			parameters: pars,
			onComplete: updateMapImage,
			onFailure: reportError
		}
	);
}

function imc_mm_move(deltax, deltay, width, height)
{
	var url = '/intermap/srv/'+Env.lang+'/map.move';  
	var pars = 'deltax=' + deltax + '&deltay=' + deltay + 
	                "&width=" + width + "&height=" + height +
	                "&" + im_mm_getURLbbox();	 // FIXME: we need it as func param                
	
	var myAjax = new Ajax.Request (
		url, 
		{
			method: 'get',
			parameters: pars,
			onComplete: im_mm_imageRebuilt,
			onFailure: reportError
		}
	);
}

function imc_bm_setScale(w, h, bbox, scale )
{
	var url = '/intermap/srv/'+Env.lang+'/map.setScale';
	var pars = "width=" + w + "&height="+h +
	                "&"+bbox+
	                "&scale=" + scale;

	setStatus('busy');

	var myAjax = new Ajax.Request (
		url, 
		{
			method: 'get',
			parameters: pars,
			onComplete: updateMapImage,
			onFailure: reportError
		}
	);
}

/*****************************************************************************
 *                                                                           *
 *                              Area of interest                             *
 *                                                                           *
 *****************************************************************************/

function setAoi(xmin, ymin, xmax, ymax)
{
	var url = '/intermap/srv/'+Env.lang+'/map.setAoi';
	var pars = 'minx=' + xmin + '&miny=' + ymin + '&maxx=' + xmax + '&maxy=' + ymax;
	//alert(pars); // DEBUG
	var myAjax = new Ajax.Request (
		url, 
		{
			method: 'get',
			parameters: pars,
			//onComplete: showResponse,
			onFailure: reportError
		}
	);
}

function unsetAoi()
{
	var url = '/intermap/srv/'+Env.lang+'/map.unsetAoi';
	
	var myAjax = new Ajax.Request (
		url, 
		{
			method: 'get',
			//onComplete: showResponse,
			onFailure: reportError
		}
	);
}


/*****************************************************************************
 *                                                                           *
 *                                Map refresh                                *
 *                                                                           *
 *****************************************************************************/


function imc_updateBigMap(width, height, qbbox, doUpdateMM, callback)
{
    if(doUpdateMM == null)
        followmm = true;
    
    var pars = "width=" + width + "&height=" + height;
    if(qbbox)
        pars +=  "&" + qbbox;
    
    setStatus('busy');

    var myAjax = new Ajax.Request (
    	'/intermap/srv/'+Env.lang+'/map.update', 
    	{
    		method: 'get',
    		parameters: pars,
    		onComplete: function(req) 
    		{ 
                            updateMapImage(req);
                            if( doUpdateMM )
                                // !!! check if this refresh is not due to a minimap action, or we'll get a refresh loop !!!
                                im_mm_followBM();
                            if(callback)
                                callback();
    		 },
    		onFailure: reportError
    	}
    );
}

function im_mm_followBM()
{
	imc_mm_update(im_mm_width, im_mm_height, im_dezoom(im_bm_north, im_bm_east, im_bm_south, im_bm_west));
}

function imc_mm_update(width, height, qbbox, callback)
{
	im_mm_setStatus('busy');
	
	var url = '/intermap/srv/'+Env.lang+'/map.update';

	var pars = "width=" + width + "&height=" + height;
	if(qbbox)
	    pars += "&" + qbbox

	var myAjax = new Ajax.Request (
		url, 
		{
			method: 'get',
			parameters: pars,
			onComplete: function(req)
			{
				im_mm_imageRebuilt(req);
				if(callback) callback();
			},
			onFailure: reportError
		}
	);
}

/*
function imc_mm_action_mm()
{
	im_mm_setStatus('busy');
	var url = '/intermap/srv/'+Env.lang+'/map.update';
	
	var im_mm_bb=im_dezoomDegrees(im_mm_ctrl_n.value,im_mm_ctrl_e.value,im_mm_ctrl_s.value,im_mm_ctrl_w.value);

	var pars = 'maptool=' + 'zoomin' + 
	                "&width=" + '200' + "&height="+'100' +
	                "&"+im_mm_bb; // FIXME: we should pass bb as param 
	
	var myAjax = new Ajax.Request (
		url, 
		{
			method: 'get',
			parameters: pars,
			onComplete: im_mm_imageRebuilt,
			onFailure: reportError
		}
	);
}
*/

/*****************************************************************************
 *                                                                           *
 *                                Full extent                                *
 *                                                                           *
 *****************************************************************************/

function imc_bm_fullExtent(w,h)
{
	setStatus('busy');
	
//	deleteAoi();
	
	if (currentTool == 'zoomout' || currentTool == 'pan') 
	    setTool('zoomin');

           var pars = "width=" + w + "&height="+h;

	var myAjax = new Ajax.Request (
		'/intermap/srv/'+Env.lang+'/map.fullExtent', 
		{
			method: 'get',
			parameters: pars,
			onComplete: updateMapImage,
			onFailure: reportError
		}
	);
}

function imc_mm_fullExtent(w,h)
{
	im_mm_setStatus('busy');
	
//	im_mm_deleteAoi();
	
	if (im_mm_currentTool == 'zoomout' || im_mm_currentTool == 'pan') 
	    im_mm_setTool('zoomin');
	
	var pars = "width=" + w + "&height="+h;
		
	var myAjax = new Ajax.Request (
		'/intermap/srv/'+Env.lang+'/map.fullExtent', 
		{
			method: 'get',
			parameters: pars,
			onComplete: im_mm_imageRebuilt,
			onFailure: reportError
		}
	);
}



/*****************************************************************************
 *                                                                           *
 *****************************************************************************/

/* ETj:unused?
function addLayer(baseUrl, serviceName) // DEBUG
{
	setStatus('busy');
	
	var url = 'map.layers.add';
	var pars = 'url=' + baseUrl + '&service=' + serviceName;
	var myAjax = new Ajax.Request (
		url,
		{
			method: 'get',
			parameters: pars,
			onComplete: layerAdded,
			onFailure: reportError
		}
	);
}
*/


function imc_addService(surl, service, type, callback)
{
	var url = '/intermap/srv/'+Env.lang+'/map.addServices.embedded';
	
	var pars = 'url=' + encodeURIComponent(surl) 
			+ '&service=' + encodeURIComponent(service) 
			+ '&type=' + type;
			
	var myAjax = new Ajax.Request (
		url,
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
	var url = '/intermap/srv/'+Env.lang+'/map.addServices.embedded';	
	var pars = 'url=' + encodeURIComponent(surl) + '&type=' + type;
	
	serviceArray.each(
	    function(service)
	    {
	        pars += '&service=' + encodeURIComponent(service);
	    }
	);
	
	var myAjax = new Ajax.Request (
		url,
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
		'/intermap/srv/'+Env.lang+'/mapServers.getServices.xml',
		{
		           parameters: "mapserver="+id,
			method: 'get',

			onComplete: callback,
			onFailure: reportError
		}
	);

}
