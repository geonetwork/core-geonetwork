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

function im_setMark(e)
{
	Event.stop(e); // prevents from dragging the map image (on Firefox)
	
	// get map image offset
	var offset = Position.cumulativeOffset($(im_bm.imageId));
	var offsetX = offset[0];
	var offsetY = offset[1];
	
	// store starting cursor position
	pointerX = Event.pointerX(e);
	pointerY = Event.pointerY(e);
	
	var x = pointerX - offsetX;
	var y = pointerY - offsetY;
	
	var lat  = im_bm.y2lat(y);
	var lon = im_bm.x2lon(x);
	
	if(lat<-90||lat>90||lon <-180||lon >180)
	{
		return;		
	}

	im_tmpMarker = new IMMarker(lat, lon, "unknown");
	
	var img = document.createElement("img");
	img.id='im_marker_img_tmp';
	img.className = 'im_marker';
	img.src = '/intermap/images/marker.png'; // FIXME context
	// FIXME the image should be centered on the mouse click
	img.style.left = pointerX;
	img.style.top = pointerY;           
	document.body.appendChild(img);    
	
	
            var div = document.createElement("div");
            div.id="im_marker_box_tmp";
            div.className = "im_markerbox";
/*            div.style.position = "absolute";
            div.style.z-index = "10";*/
            document.body.appendChild(div);    

/*    var offset = Position.cumulativeOffset($(btn));
    var x = offset[0];
    var y = offset[1];
*/

	var wbox = 200; // the width of the marker info box
	var hbox =  50; // the height of the marker info box
	
	var horEdge = 10; // the horizontal distance the info box must have with the image borders 
	
	var dybox = 20; // the y offset of the info box with respect to the marker
	var dxbox = wbox/2; // the negative x offset of the info box with respect to the marker
		 

	// if the box runs out of lower border, draw it above the mark
	if(y+dybox+hbox > im_bm.height)
	{
		dybox = -dybox-hbox;		
	}	

	// if the box runs out of the right border, shift it to left 
	if(x - dxbox + wbox > im_bm.width - horEdge)
	{
		dxbox = x + wbox + horEdge - im_bm.width;  
	}
	 // if the box runs out of the right border, shift it to left
	else if(x - dxbox < horEdge)
	{
		dxbox = x - horEdge;  
	}

    div.style.left = (pointerX-dxbox)+"px";
    div.style.top = (pointerY+dybox)+"px";
    div.style.width = wbox+"px";
    div.style.height = hbox+"px";
	
	var rlat = Math.round(lat*10000)/10000;
	var rlon = Math.round(lon*10000)/10000;
	
	div.innerHTML="Lat:"+rlat +" Lon:"+rlon; 
	
	
	// add text field
	var text = document.createElement("input");
	text.id="im_marker_input_tmp";
	text.type = "text";
	div.appendChild(text);
	text.focus();
	
	// add closer button
	var closer = document.createElement('div');
	closer.className = "upperright";
	//closer.id = "im_wbcloser";
	var img = document.createElement('img');
	img.title = i18n("close");
	img.src = "/intermap/images/close.png";
    closer.appendChild(img);    
    Event.observe(img, 'click', im_closeMarkerBox);
	div.appendChild(closer);
	
	// add save button
	var saver = document.createElement('div');
	saver.className = "lowerright";
	//closer.id = "im_wbcloser";
	var simg = document.createElement('img');
	simg.title = i18n("save");
	simg.src = "/intermap/images/filesave.png";
    saver.appendChild(simg);    
    Event.observe(simg, 'click', im_saveMarker);
	div.appendChild(saver);
        
}

function im_closeMarkerBox(e)
{
	removeMarkerBox();
}

function im_saveMarker(e)
{
    var tmpmarker = $('im_marker_img_tmp');
	
	// fill in more values for marker
	im_tmpMarker.title = $('im_marker_input_tmp').value;
	var seq = im_markers.length;
	im_tmpMarker.seq = seq;
	im_markers[seq] = im_tmpMarker; 
	
	im_drawMarkerImage(im_tmpMarker);

	removeMarkerBox();
}

function removeMarkerBox()
{
    if($('im_marker_img_tmp'))
        $('im_marker_img_tmp').remove();
    if($('im_marker_box_tmp'))
        $('im_marker_box_tmp').remove();
}

var im_markers = new Array();
var im_tmpMarker;

function IMMarker(lat, lon, title)
{
	this.lat = parseFloat(lat);
	this.lon = parseFloat(lon);
	this.title = title;	
}

IMMarker.prototype.lat;
IMMarker.prototype.lon;
IMMarker.prototype.title;
IMMarker.prototype.seq;

function im_deleteAllMarkersImages()
{
	im_markers.each(
		function(marker)
		{
			var seq = marker.seq;
			var img = $("im_marker_"+seq);
			if(img)
			{
				img.remove();
			}			
		}	
	);
}

function im_drawAllMarkersImages()
{
	im_markers.each(im_drawMarkerImage);
}

function im_drawMarkerImage(marker)
{
	// get map image offset
	var offset = Position.cumulativeOffset($(im_bm.imageId));
	var offsetX = offset[0];
	var offsetY = offset[1];
	
	var y = im_bm.lat2y(marker.lat);
	var x = im_bm.lon2x(marker.lon);
	
	if(y<0 || x<0 || y>$(im_bm.imageId).height || x>$(im_bm.imageId).width)
	{
		return;
	}
	
	var img = document.createElement("img");
	img.id='im_marker_' + marker.seq;
	img.className = 'im_marker';
	img.src = '/intermap/images/marker2.gif'; // FIXME 
	img.title = marker.title;
	
	// FIXME the image should be centered on the coords	
	img.style.left = x + offsetX;
	img.style.top  = y + offsetY;
	
	document.body.appendChild(img);
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
	// update the scale text
	var scale = req.responseXML.getElementsByTagName('scale')[0].firstChild.nodeValue;
	deleteChildNodes($('im_scale'));
	$('im_scale').appendChild( document.createTextNode('1:' + scale));
	
	$('im_currentscale').innerHTML= '1:' + scale;
	$('im_setscale').selectedIndex = 0;
	
	im_deleteAllMarkersImages();
	im_drawAllMarkersImages();
};

