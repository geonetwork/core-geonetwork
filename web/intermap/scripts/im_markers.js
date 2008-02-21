/********************************************************************
 * im_markers.js
 * 
 *   This file contains functions related to the marker feature. 
 *   It must be included whenever bigmap.js is used.
 *   Note that some other markers related functions are in extras.js
 *  
 *******************************************************************/

/** Client side id for temporary markers -- there may be more than one
 * in a given moment (one is updating on server, another one is under editing)
 */ 
var im_mark_tmp_id = 1;

/** holds the markers' id */
var im_markarr = new Array();

/** the current temporary marker */
var im_tmpMarker;
//var im_markers = new Array();

//The marker offset ensures the point of the marker image is presented on the point clicked with the mouse
var marker_offset_x = 6;
var marker_offset_y = 22;

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
	img.src = '/intermap/images/marker.gif'; // FIXME context
	// FIXME the image should be centered on the mouse click

	img.style.left = pointerX - 6;
	img.style.top = pointerY - 21;           
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
	//im_mark_tmp_id
	
//	var seq = im_markers.length;
//	im_tmpMarker.seq = seq;
//	im_markers[seq] = im_tmpMarker; 
	
	//im_drawMarkerImage(im_tmpMarker);
	im_createMarker(im_mark_tmp_id, im_tmpMarker.lat, im_tmpMarker.lon, im_tmpMarker.title, true);
	imc_saveMarker(im_mark_tmp_id, im_tmpMarker.lat, im_tmpMarker.lon, im_tmpMarker.title);
	
	im_mark_tmp_id++;
	
	removeMarkerBox();
}

function removeMarkerBox()
{
    if($('im_marker_img_tmp'))
        $('im_marker_img_tmp').remove();
    if($('im_marker_box_tmp'))
        $('im_marker_box_tmp').remove();
}

/**
 * 
 * @param {IMMarker} marker
 */
function imc_saveMarker(tmp_id, lat, lon, title)
{
//	var par = "lat=" + lat
//			+ "&lon=" + lon
//			+ "&title=" + encodeURIComponent(title)
//			+ "&desc=" + encodeURIComponent("Description of " + title);
//	
    var myAjax = new Ajax.Request(    
    	getIMServiceURL('marker.add'), 
    	{
    		method: 'post',    
    		parameters: {lat: lat, 
						 lon: lon, 
						 title: title, 
						 desc: 'Description of '+title},
    		onSuccess: function(req)
    		{
				if(req.responseXML && req.responseXML.documentElement.tagName == "error")
				{
					var resp = req.responseXML.documentElement;
					var msg = resp.getElementsByTagName('message')[0].firstChild.nodeValue;
					alert(msg); 
					//div.innerHTML = msg;
					return;
				}
				else if(req.responseXML)
				{
					var resp = req.responseXML.documentElement;
					var id = resp.getElementsByTagName('added')[0].firstChild.nodeValue;
					
					im_concretizeTempMarker(tmp_id, id);
					//marker.id = id; 
					//im_registerMarkerObserver(marker.seq, id);						
					//alert("Added marker #" + id); 
				}
    		},
    		onFailure: function(req)
    		{
				div.innerHTML = i18n('genericError');     		
    		}
    	}
    );    
}	



function IMMarker(lat, lon, title)
{
	this.lat = parseFloat(lat);
	this.lon = parseFloat(lon);
	this.title = title;	
}

IMMarker.prototype.lat;
IMMarker.prototype.lon;
IMMarker.prototype.title;
/** Client-side identifier */
IMMarker.prototype.seq; 
/** Server-side identifier */
IMMarker.prototype.id;

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

function im_createMarkersDom(dommarkerlist)
{
	if(! dommarkerlist )
	{
		return;				
	}

	if(dommarkerlist.hasChildNodes())
	{
		var children = dommarkerlist.childNodes;
		for (var i = 0; i < children.length; i++) 
		{
			if(children[i].nodeType == Node.ELEMENT_NODE)
				im_createMarkerDom(children[i]);
		};
	};	
}

function im_createMarkerDom(dommarker)
{
	var id = dommarker.getAttribute('id');
	var lat = dommarker.getAttribute('lat');
	var lon = dommarker.getAttribute('lon');

	var title = "undef";
	var children = dommarker.childNodes;
	for (var i = 0; i < children.length; i++) 
	{
		if(children[i].nodeType == Node.ELEMENT_NODE && 
		   children[i].tagName == "title")
			title = children[i].firstChild.nodeValue;
	}
	
	im_createMarker(id, lat, lon, title);			
}

function im_createMarker(id, lat, lon, title, btemp)
{
	if (btemp === null)
		btemp = false;
				
	// get map image offset
	var offset = Position.cumulativeOffset($(im_bm.imageId));
	var offsetX = offset[0];
	var offsetY = offset[1];
		
	var y = im_bm.lat2y(lat);
	var x = im_bm.lon2x(lon);
	
	if(y<0 || x<0 || y>$(im_bm.imageId).height || x>$(im_bm.imageId).width)
	{
		// should not happen: markers are filtered server-side
		return;
	}
	
	var img = document.createElement("img");
	img.id='im_marker_' + (btemp?"tmp_":"") + id;
	img.className = 'im_marker';
	img.src = '/intermap/images/marker.gif'; // FIXME 
	img.title = title;

	// FIXME the image should be centered on the coords	
	img.style.left = x + offsetX - 6;
	img.style.top  = y + offsetY - 21;
	
	document.body.appendChild(img);

	if(! btemp)	
	{
		im_markarr[im_markarr.length] = id;
		img.onclick = function(e){im_markerClicked(e, id);};	
	}
	
//	if(marker.id) // already assigned?
//	{
//		im_registerMarkerObserver(marker.seq, marker.id);
//	}			
}

function im_concretizeTempMarker(tempid, id)
{
	var imgoldid = 'im_marker_tmp_' + tempid;
	var imgnewid = 'im_marker_' + id;

	var img = $(imgoldid);
	img.id = imgnewid;

	im_markarr[im_markarr.length] = id;	
	img.onclick = function(e){im_markerClicked(e, id);};	
}

function im_deleteClientMarkers()
{
	im_markarr.each(im_deleteClientMarker);
	im_markarr = new Array();
}

function im_deleteClientMarker(markerid)
{
	if(markerid) // may have been deleted
	{
		var img = $("im_marker_" + markerid);
		if(img)
		{
			img.remove();
		}											
	}
}


function im_redrawMarkers(dom)
{
	im_deleteClientMarkers();

	var markerlist = dom.getElementsByTagName('markers')[0];
	im_createMarkersDom(markerlist);		
}


function im_showClientMarkers(doShow)
{
	im_markarr.each(function (markerid)
	{
		if(markerid) // may have been deleted
		{
			var img = $("im_marker_" + markerid);
			if(img)
			{
				if(doShow)
					img.show();
				else
					img.hide();
			}											
		}
	});
}

function im_retitleClientMarker(markerid, title)
{
	if(markerid) // may have been deleted
	{
		var img = $("im_marker_" + markerid);
		if(img)
		{
			img.title = title;
		}											
	}
}


function im_markerClicked(e, id)
{
	//Event.stop(e); // prevents from dragging the map image (on Firefox)	
	im_markerList(id);	
}
