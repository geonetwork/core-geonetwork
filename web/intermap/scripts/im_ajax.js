
/* TODO:  adding to every function the "imc_" (InterMap Connector) prefix */

var activeLayerId = null; // active layer

im_load_error = function() {alert("Loading error")};

function imc_reloadLayers()
{
	var url = '/intermap/srv/en/map.layers.getOrder';
	
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

function updateInspector(layerId)
{
	var url = '/intermap/srv/en/map.layers.getInspectorData';
	var pars = 'id=' + layerId;
	
	var myAjax = new Ajax.Request (
		url, 
		{
			method: 'get',
			parameters: pars,
			onComplete: updateInspectorControls,
			onFailure: reportError
		}
	);
}

// start ajax transaction to set the layer order
function imc_setLayersOrder(order)
{
	var url = '/intermap/srv/en/map.layers.setOrder';
	var pars = order.replace(new RegExp("\\[\\]", "g"), ""); // remove all [ and ] - jeeves doesn't accept in parameter name otherwise
	
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

function imc_zoomToLayer(layerId)
{
	deleteAoi();
	//unsetAoi();
	//$('im_geonetRecords').className = 'hidden';	
	
	setStatus('busy');
	var url = '/intermap/srv/en/map.zoomToService';
	var pars = 'id=' + layerId;
	
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

function toggleVisibility(id) {
	var url = '/intermap/srv/en/map.layers.toggleVisibility';
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
function showLegend(id) {
	window.open('/intermap/srv/en/map.service.getLegend?id=' + id, 'dialog', 'HEIGHT=300,WIDTH=400,scrollbars=yes,toolbar=yes,status=yes,menubar=yes,location=yes,resizable=yes');
}


/*****************************************************************************
 *                                                                           *
 *                                Delete layer                               *
 *                                                                           *
 *****************************************************************************/

// start ajax transaction to delete a layer
function imc_deleteLayer(id)
{
	var url = '/intermap/srv/en/map.layers.deleteLayer';
	var pars = 'id=' + id ;
	
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
 *                                 Add layer                                 *
 *                                                                           *
 *****************************************************************************/

// start ajax transaction to delete a layer
function setAddLayersWindowContent()
{
	var url = '/intermap/srv/en/mapServers.listServers';
	
	Position.clone('im_map', 'im_addLayers');
	var myAjax = new Ajax.Updater
	(
		'im_addLayers',
		url, 
		{
			method: 'get',
			onFailure: reportError
		}
	);
}


/*****************************************************************************
 *                                                                           *
 *                            Layer transparency                             *
 *                                                                           *
 *****************************************************************************/

function imc_setLayerTransparency(id, transparency)
{
	var url = '/intermap/srv/en/map.layers.setTransparency';
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
	var url = '/intermap/srv/en/map.action';
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
			onComplete: updateMapImage,
			onFailure: reportError
		}
	);
}

function imc_mm_action(tool, xmin, ymin, xmax, ymax, w, h)
{
	var url = '/intermap/srv/en/map.action';
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
	var url = '/intermap/srv/en/map.move';
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
	var url = '/intermap/srv/en/map.move';  
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


/*****************************************************************************
 *                                                                           *
 *                              Area of interest                             *
 *                                                                           *
 *****************************************************************************/

function setAoi(xmin, ymin, xmax, ymax)
{
	var url = '/intermap/srv/en/map.setAoi';
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
	var url = '/intermap/srv/en/map.unsetAoi';
	
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
    	'/intermap/srv/en/map.update', 
    	{
    		method: 'get',
    		parameters: pars,
    		onComplete: function(req) { 
                            updateMapImage(req);
                            if( doUpdateMM )
                                // !!! check if this refresh is not due to a minimap action, or we'll get a refresh loop !!!
                                imc_mm_update(im_mm_width, im_mm_height, im_dezoom(im_bm_north, im_bm_east, im_bm_south, im_bm_west));
                            if(callback)
                                callback();
    		 },
    		onFailure: reportError
    	}
    );
}

function imc_mm_update(width, height, qbbox)
{
	im_mm_setStatus('busy');
	
	var url = '/intermap/srv/en/map.update';

	var pars = "width=" + width + "&height=" + height;
	if(qbbox)
	    pars += "&" + qbbox

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


/*****************************************************************************
 *                                                                           *
 *                                Full extent                                *
 *                                                                           *
 *****************************************************************************/

function imc_bm_fullExtent(w,h)
{
	setStatus('busy');
	
	deleteAoi();
	
	if (currentTool == 'zoomout' || currentTool == 'pan') 
	    setTool('zoomin');

           var pars = "width=" + w + "&height="+h;

	var myAjax = new Ajax.Request (
		'/intermap/srv/en/map.fullExtent', 
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
	
	im_mm_deleteAoi();
	
	if (im_mm_currentTool == 'zoomout' || im_mm_currentTool == 'pan') 
	    im_mm_setTool('zoomin');
	
	var pars = "width=" + w + "&height="+h;
		
	var myAjax = new Ajax.Request (
		'/intermap/srv/en/map.fullExtent', 
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




// improve me: this setting should be implemented client-side
// this setting refers to big map only, while ajax calls should be independent from which map are they called on
/*function imc_toggleImageSize()
{
	deleteAoi();
	unsetAoi();
	$('im_geonetRecords').className = 'hidden';	
	
	setStatus('busy');
	var url = '/intermap/srv/en/map.toggleImageSize';
	var myAjax = new Ajax.Request (
		url,
		{
			method: 'get',
			onComplete: im_imageSizeToggled,
			onFailure: reportError
		}
	);
}
*/
// improve me: this setting should be implemented client-side
// this setting refers to big map only, while ajax calls should be independent from which map are they called on
/*function im_imageSizeToggled(req)
{
            var w = req.responseXML.getElementsByTagName('width')[0].firstChild.nodeValue;
            var h = req.responseXML.getElementsByTagName('height')[0].firstChild.nodeValue;

            im_bm_setSize(w,h);
            	
	imc_updateBigMap(im_bm_width, im_bm_height, im_bm_getURLbbox());
}
*/
function imc_addService(surl, service, type, callback)
{
	var url = '/intermap/srv/en/map.addServices.xml';
	
	var pars = 'url=' + surl + '&service=' + service + '&type=' + type;
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
	var url = '/intermap/srv/en/map.addServices.xml';	
	var pars = 'url=' + surl + '&type=' + type;
	
	serviceArray.each(
	    function(service)
	    {
	        pars += '&service=' + service;
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
		'/intermap/srv/en/mapServers.getServices.xml',
		{
		           parameters: "mapserver="+id,
			method: 'get',

			onComplete: callback,
			onFailure: reportError
		}
	);

}
