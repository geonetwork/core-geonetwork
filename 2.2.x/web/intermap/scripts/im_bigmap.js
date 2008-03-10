//===================================================================
//
//                 Functions related to the big IM map
//
//===================================================================

//===================================================================
//                      Main vars and setup
//===================================================================
 
 // these are the values of initial width and height. 
var im_bm_wsize0 = 368;
var im_bm_hsize0 = 276;
																					
var im_bm = new Intermap(im_bm_wsize0, im_bm_hsize0, 'im_bm_image');
	
im_bm.setBBox(90, 180, -90, -180);
 
im_bm.setSize = function (w, h) // override
{
    this.width  = parseInt(w);
    this.height = parseInt(h);

    if($('im_bm_image'))
    {
        $('im_bm_image').style.width = w;
        $('im_bm_image').style.height = h; 
        //$('im_map').style.width = w;
        //$('im_map').style.height = h;
        
        $('im_mapContainer').style.width = im_bm.width + 2; 
        $('im_mapContainer').style.height = im_bm.height + 2; 
        
        $('im_map').style.width = w; 
        $('im_map').style.height = h;
        
        $('im_bm_image_waitdiv').style.width = w;
    }
};

//===================================================================
//
//  Wrappers for GUI buttons
//
//===================================================================

/** Called by GUI button */
function setTool(tool) 
{
	im_bm.setTool(tool);
}

/** Called by GUI button */
function im_bm_fullExtent()
{
    im_bm.fullExtent();
}

/** Called by GUI button */
function im_bm_refresh()
{
    im_bm.rebuild();
}

//===================================================================
//
//                      Map operations (zoom, pan, identify)
//
//===================================================================

var startX, startY; // start (mousedown) coordinates

var startOffsetX;
var startOffsetY;


im_bm.afterToolSet = function(tool)
{
    $('intermap_root').className = tool; 
};

im_bm.beforeMouseDown = function(e)
{
	removeMarkerBox();
};

im_bm.unresolvedMouseDown = function(e)
{
	switch(this.tool)
	{
		case 'identify':
			identify(e);
			break;
		case 'mark':
			im_setMark(e);
			break;
	}
};

// IDENTIFY - TODO: move some part intermap.js
function identify(e)
{
	Event.stop(e); // prevents from dragging the map image (on Firefox)
	
	// get map image offset
	var offset = Position.cumulativeOffset($(im_bm.imageId));
	var offsetX = offset[0];
	var offsetY = offset[1];
	
	// store starting cursor position
	pointerX = Event.pointerX(e);
	pointerY = Event.pointerY(e);
	
	var url = getIMServiceURL('map.identify');
	var t1 = pointerX - offsetX;
	var t2 = pointerY - offsetY;
	var pars = 'mapimgx=' + t1 + '&mapimgy=' + t2 +
				"&width=" + im_bm.width + "&height=" + im_bm.height +
				"&"+ im_bm.getURLbbox() +
				'&activeLayer=' + activeLayerId + "&format=text%2Fhtml";
	
	window.open(url + "?" + pars, 
				"Queryresult", 
				"width=600,height=400,scrollbars=yes,toolbar=no,status=yes,menubar=no,location=yes,resizable=yes");
}

//==================================================
// RESIZE
//==================================================

function im_bm_resizeStart(e)
{
	Event.stop(e); // prevents from dragging the map image (on Firefox)
	
	// add mousemove and mouseup listeners
	Event.observe(document, 'mousemove', im_bm_resizeMove);
	Event.observe(document, 'mouseup', im_bm_resizeStop);

	// get map image offset
	var offset = Position.cumulativeOffset($('im_bm_image'));
	var offsetX = offset[0];
	var offsetY = offset[1];

	// store starting cursor position
	startX = offsetX + 1;
	startY = offsetY + 1;
	
	// dynamically create the zoombox div
	var resizebox = document.createElement('div');
	resizebox.setAttribute('id', 'im_bm_resizebox');
	MapUtils.drawBox(resizebox, startX, startY, 
			$('im_bm_image').clientWidth, 
			$('im_bm_image').clientHeight);
	document.body.appendChild(resizebox);
	
	// ghost image
	var resizeGhost = document.createElement('img');
	resizeGhost.id = 'im_bm_resizeGhost';
	resizeGhost.src = $('im_bm_image').src;
	MapUtils.drawBox(resizeGhost, startX, startY, 
			$('im_bm_image').clientWidth, 
			$('im_bm_image').clientHeight);
	document.body.appendChild(resizeGhost);			
}

// 
function im_bm_resizeMove(e)
{
	Event.stop(e); // prevents from dragging the map image (on Explorer)
	
	// get the current cursor position
	var pX = Event.pointerX(e);
	var pY = Event.pointerY(e);
	
	// get map image offset
	var offset = Position.cumulativeOffset($('im_bm_image'));
	var offsetX = offset[0];
	var offsetY = offset[1];

	// Keeps map large enough
	pX = Math.max(pX, offsetX+250);
	pY = Math.max(pY, offsetY+200);

	var windowsize = getWindowSize();
	var winw = windowsize[0];
	var winh = windowsize[1];

	// Keeps map inside portview
	pX = Math.min(pX,  winw - im_layer_width - 30); // 30 to stay comfortably within borders, padding and what else
	pY = Math.min(pY,  800);

	// set the zoom box position and size
	MapUtils.drawBox ($('im_bm_resizebox'),
		offsetX +1,  // left
		offsetY +1,  // top
		Math.abs(pX - startX), // width
		Math.abs(pY - startY)  // height
	);
	
	// ghost image
	MapUtils.drawBox( $('im_bm_resizeGhost'), 
		offsetX +1,  // left
		offsetY +1,  // top
		Math.abs(pX - startX), // width
		Math.abs(pY - startY)  // height
	);
}

// mouseup event listener
function im_bm_resizeStop(e)
{
	// get the current cursor position
	var pX = Event.pointerX(e);
	var pY = Event.pointerY(e);
	
	// get map image offset
	var offset = Position.cumulativeOffset($(im_bm.imageId));
	var offsetX = offset[0];
	var offsetY = offset[1];
	
	var w = $('im_bm_resizebox').clientWidth;
	var h = $('im_bm_resizebox').clientHeight;

	// remove listeners and div
	Event.stopObserving(document, 'mousemove', im_bm_resizeMove);
	Event.stopObserving(document, 'mouseup', im_bm_resizeStop);
	Element.remove($('im_bm_resizeGhost'));
	Element.remove($('im_bm_resizebox'));

	// do a preventive resizing, so user will get the whole layout before the image gets loaded
	im_bm.setSize(w, h);
	
	im_bm.rebuild();

/*	imc_updateBigMap( w, h,
				im_bm.getURLbbox(),
				false);
*/			
}

/*****************************************************************************
 *
 *                          Scale
 *
 *****************************************************************************/
 
function im_bm_setScale()
{	
	imc_bm_setScale(im_bm.width,  im_bm.height,
			    im_bm.getURLbbox(),
			    $('im_setscale').value);
}

function imc_bm_setScale(w, h, bbox, scale )
{
	var pars = "width=" + w + "&height="+h +
	                "&"+bbox+
	                "&scale=" + scale;

	im_bm.setStatus('busy');

	var myAjax = new Ajax.Request (
		getIMServiceURL('map.setScale'), 
		{
			method: 'get',
			parameters: pars,
			onComplete: im_bm.imageRebuilt.bindAsEventListener(im_bm),
			onFailure: reportError
		}
	);
}

/*****************************************************************************
 *
 *                          Generic utility functions
 *
 *****************************************************************************/

function reportError(request)
{
	alert('Sorry. There was an error.');
	alert(request.responseXML);
}

// shows the ajax response - useful for debugging
function showResponse(originalRequest)
{
	alert(originalRequest.responseText);
}

// delete all child nodes
function deleteChildNodes(target)
{
	while (target.childNodes.length > 0) {target.removeChild(target.childNodes[target.childNodes.length - 1]);}
}


//==================================================
// 
//==================================================


/*function layerAdded(req) // FIXME
{
	updateMapImage(req);
	imc_reloadLayers();
}
*/


//==================================================
// 
//==================================================

im_bm.afterImageRebuilt = function(req)
{
	//--- update the scale text
	var scale = req.responseXML.getElementsByTagName('scale')[0].firstChild.nodeValue;
	deleteChildNodes($('im_scale'));
	$('im_scale').appendChild( document.createTextNode('1:' + scale));
	
	$('im_currentscale').innerHTML= '1:' + scale;
	$('im_setscale').selectedIndex = 0;
	
	//--- update markers
//	im_deleteAllMarkersImages(); // obsolete
//	im_drawAllMarkersImages();

	im_redrawMarkers(req.responseXML);	
};

