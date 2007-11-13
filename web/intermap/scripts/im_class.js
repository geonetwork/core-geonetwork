/**
 *                      Intermap class 
 *
 * This js class handles the map, its properties (size, bbox) and some
 * functionalities, such as zoomin, zoomout and panning.
 *
 * You can customize the behaviour a great deal by defining some callback functions.
 *
 * <br/><b>Requires:</b>
 *   getIMServiceURL(service)
 *
 * @param {int} initWidth The initial width for the image in pixels 
 * @param {int} initHeight The initial height for the image in pixels
 * @param {string} imageId The id attribute of the img element containing the map
 * 
 * @constructor
 * 
 * @author ETj
 *
 */
function Intermap(initWidth, initHeight, imageId)
{
    this.width = initWidth;
    this.height = initHeight;
    this.imageId = imageId;
    
    this.mouseDownListener = null; //this.mousedownEventListener.bindAsEventListener(this);
}


/** This is the html id of the img element that holds the map */ 
Intermap.prototype.imageId;

/** Image dimensions */
Intermap.prototype.width;
Intermap.prototype.height;

/** Map tool (zoomin, zoomout, pan, etc) */
Intermap.prototype.tool;

/**  Map bounding box */
Intermap.prototype.north;
Intermap.prototype.east;
Intermap.prototype.south;
Intermap.prototype.west;

/** some cached listeners. this listeners are wrapped by prototype bindAsEventListener,
**  in order to maintain the right "this" reference when invoked. */ 
Intermap.prototype.cachedMouseDownListener;
Intermap.prototype.cachedMouseMoveListener;
Intermap.prototype.cachedMouseUpListener;


/** Callback functions */
Intermap.prototype.beforeMouseDown; // (e)
Intermap.prototype.unresolvedMouseDown; // (e)
Intermap.prototype.afterToolSet; // (tool)
Intermap.prototype.beforeAction;
Intermap.prototype.afterAction;
Intermap.prototype.beforePan;
Intermap.prototype.afterPan;

Intermap.prototype.afterImageRebuilt; // (req)


/** Parse a response and set map props accordingly 
 * @param {Element} response
 */
Intermap.prototype.set_dom = function(response)
{
	var mmurl = response.getElementsByTagName('imgUrl')[0].firstChild.nodeValue;           
	$(this.imageId).src = mmurl;
	
	this.setBBox_dom(response);
	this.setSize_dom(response);
};

/** 
 * Set BoundingBox 
 * @param {Element} response
 */
Intermap.prototype.setBBox_dom = function(response)
{
	var extent = response.getElementsByTagName('extent')[0];
	
	var minx = extent.getAttribute('minx');
	var maxx = extent.getAttribute('maxx');
	var miny = extent.getAttribute('miny');
	var maxy = extent.getAttribute('maxy');
	
	this.setBBox(maxy, maxx, miny, minx);    
};

Intermap.prototype.setBBox = function(n, e, s, w)
{
    this.north = parseFloat(n);
    this.east  = parseFloat(e);
    this.south = parseFloat(s);
    this.west  = parseFloat(w);
};

/** 
 * Set width, height  
 * @param {Element} response
 */
Intermap.prototype.setSize_dom = function(response)
{
    // Image size
    var w = response.getElementsByTagName('width')[0].firstChild.nodeValue;
    var h = response.getElementsByTagName('height')[0].firstChild.nodeValue;

    this.setSize(w, h);
};

/**
 * Set the image dimensions
 * @param {int} width The image width
 * @param {int} height The image height
 */
Intermap.prototype.setSize = function(width, height)
{
    this.width  = parseInt(width);
    this.height = parseInt(height);   
	
	var img = $(this.imageId);
	if(img)
	{
		 img.style.width  = width + 'px';
		 img.style.height = height + 'px';
	} 
};


/** 
 * Compute y given a latitude 
 * @param {int} lat
 * @return {int} the vertical number of pixels   
 */
Intermap.prototype.lat2y = function(lat)
{
	return Math.round(this.height - (lat-this.south) * this.height / (this.north - this.south));
};

/** Compute x given a longitude  
 * @param {int} lon
 * @return {int} the horizontal number of pixels   
 */
Intermap.prototype.lon2x = function(lon)
{
	return Math.round((lon - this.west) * this.width / ( this.east - this.west));
};

/** Compute a latitude given y  
 * @param {int} y the vertical number of pixels
 * @return {int} 
 */
Intermap.prototype.y2lat = function(y)
{
    return this.south +  (this.height - y) * (this.north - this.south) / this.height; 
};

/** Compute a longitude given x  
 * @param {int} 
 * @return {int} 
 */
Intermap.prototype.x2lon = function(x)
{
    return this.west +  x * (this.east - this.west) / this.width; 
};

/**
 * Return the current bbox in URLized form
 * @return {string} the current bbox in URLized form -- or null if the bbox has not be defined (?) 
 */
Intermap.prototype.getURLbbox = function()
{
    if(this.north)
		return MapUtils.urlizebb(this.north, this.east, this.south, this.west);    
    else
		return null;
};


/*****************************************************************************
 *
 *                      Intermap operations (zoom, pan, identify)
 *
 *****************************************************************************/

/** */
Intermap.prototype.setTool = function(tool) 
{
//alert("setting tool for " + this.imageId + " to " + tool);
	this.tool = tool;
	
	if(this.afterToolSet)
	{
	    this.afterToolSet(tool);
	}
};

Intermap.prototype.startX;
Intermap.prototype.startY; // start (mousedown) coordinates

Intermap.prototype.mousedownEventListener = function(e)
{
//alert("!!!"+this.tool+"!!!");

    if(this.beforeMouseDown)
        this.beforeMouseDown(e);

	switch(this.tool)
	{
		case 'zoomin':
			this.startZoombox(e);
			break;
		case 'zoomout':
			this.startZoombox(e);
			break;
		case 'pan':
			this.startDrag(e);
			break;

		default:
		    if(this.unresolvedMouseDown)
                                  this.unresolvedMouseDown(e);			
	}
};

//==================================================
// ZOOM
//==================================================

Intermap.prototype.zoombox; // zoombox div

Intermap.prototype.startZoombox = function(e)
{
   var element = Event.element(e);
	//append("startZoom from element " + element.tagName + " id " + element.id);

	Event.stop(e); // prevents from dragging the map image (on Firefox)
	
	// remove whatever listeners may be there (this step should not be needed) 	
	Event.stopObserving(document, 'mousemove', this.cachedMouseMoveListener);
	Event.stopObserving(document, 'mouseup', this.cachedMouseUpListener);
	
	// add mousemove and mouseup listeners
	this.cachedMouseMoveListener =
                    this.resizeZoombox.bindAsEventListener(this);	
	Event.observe(document, 'mousemove', this.cachedMouseMoveListener);

	this.cachedMouseUpListener =
                    this.stopZoombox.bindAsEventListener(this);	
	Event.observe(document, 'mouseup', this.cachedMouseUpListener);
	
	// store starting cursor position
	this.startX = Event.pointerX(e);
	this.startY = Event.pointerY(e);
	
	// dynamically create the zoombox div
	this.zoombox = document.createElement('div');
	this.zoombox.id = this.imageId+'_zoombox';
	document.body.appendChild(this.zoombox);
	MapUtils.drawBox(this.zoombox, this.startX, this.startY, 0, 0);
};

// mousemove event listener
Intermap.prototype.resizeZoombox = function(e)
{
	Event.stop(e); // prevents from dragging the map image (on Explorer)
	
	// get the current cursor position
	var pX = Event.pointerX(e);
	var pY = Event.pointerY(e);

	// get map image offset
	var offset = Position.cumulativeOffset($(this.imageId));
	var offsetX = offset[0];
	var offsetY = offset[1];

    // Prevents zoombox exiting from map area
	pX = Math.max(pX, offsetX + 2);
	pY = Math.max(pY, offsetY + 2);
	
	pX = Math.min(pX, offsetX + this.width - 2);
	pY = Math.min(pY, offsetY + this.height - 2);

	// set the zoom box position and size
	MapUtils.drawBox (this.zoombox,
		Math.min(pX, this.startX),  // left
		Math.min(pY, this.startY),  // top
		Math.abs(pX - this.startX), // width
		Math.abs(pY - this.startY)  // height
	);	
};

// mouseup event listener
Intermap.prototype.stopZoombox = function(e)
{
	Event.stop(e); // prevents from dragging the map image (on Explorer)
	
	// remove listeners 	
	Event.stopObserving(document, 'mousemove', this.cachedMouseMoveListener);
	Event.stopObserving(document, 'mouseup', this.cachedMouseUpListener);

    // get the current cursor position
	var pX = Event.pointerX(e);
	var pY = Event.pointerY(e);
	
	// WARNING: the pX and Py are not bound to current map size here
	//  so the user can also zoom to an area not currently displayed.
	//  We may prevent this behaviour by using the same min/max checks
	//  as in resizeZoombox    
	
    // get map image offset
	var offset = Position.cumulativeOffset($(this.imageId));
	var offsetX = offset[0];
	var offsetY = offset[1];

	this.setStatus('busy');

	this.ajax_action(this.tool, // may be zoomin or zoomout
		Math.min(pX, this.startX) - offsetX, // xmin
		Math.max(pY, this.startY) - offsetY, // ymax
		Math.max(pX, this.startX) - offsetX, // xmax
		Math.min(pY, this.startY) - offsetY  // ymin
	);
	
	// remove div
	//append("REMOVING ZOOMBOX: " + this.zoombox.id + " parent "+ this.zoombox.parentNode.id);
	Element.remove(this.zoombox);
	
            // TODO im_mm_setAOIandZoom();	
};



Intermap.prototype.ajax_action = function (tool, xmin, ymin, xmax, ymax)
{
	var pars = 'maptool=' + tool + 
		'&mapimgx=' + xmin + '&mapimgy=' + ymin + 
		'&mapimgx2=' + xmax + '&mapimgy2=' + ymax + 
		"&width=" + this.width + "&height="+this.height +
		"&"+ this.getURLbbox(); // FIXME: we should pass bb as param // FIXME may be null 
	
	var myAjax = new Ajax.Request (
		getIMServiceURL('map.action'), 
		{
			method: 'get',
			parameters: pars,
			onComplete: this.imageRebuilt.bindAsEventListener(this),
			onFailure: function(req)
						{
							alert("ERROR"); // FIXME TODO
						}
		}
	);
};

//==================================================
// DRAG
//==================================================

Intermap.prototype.startDragX;
Intermap.prototype.startDragY;

Intermap.prototype.startDrag = function(e)
{
	//alert("Startdrag");
	Event.stop(e); // prevents from dragging the map image (on Firefox)
	
	// remove whatever listeners may be there (this step should not be needed) 	
	Event.stopObserving(document, 'mousemove', this.cachedMouseMoveListener);
	Event.stopObserving(document, 'mouseup', this.cachedMouseUpListener);
	Event.stopObserving(document, 'mousedown', this.cachedMouseDownListener);
		
	// add mousemove and mouseup listeners
	this.cachedMouseMoveListener = this.dragImage.bindAsEventListener(this);	
	Event.observe(document, 'mousemove', this.cachedMouseMoveListener);

	this.cachedMouseUpListener = this.stopDrag.bindAsEventListener(this);	
    Event.observe(document, 'mouseup', this.cachedMouseUpListener);
	
	// get map initial image offset
	var offset = Position.cumulativeOffset($(this.imageId));
	this.startDragX = offset[0];
	this.startDragY = offset[1];
		
	// store starting cursor position
	this.startX = Event.pointerX(e);
	this.startY = Event.pointerY(e);
};

Intermap.prototype.dragImage = function(e)
{
	window.status = Event.pointerX(e) + ' - ' + Event.pointerY(e); // DEBUG
	
	Event.stop(e); // prevents from dragging the map image (on Explorer)
	
	var img = $(this.imageId);
	
	// get map image offset
	var offset = Position.cumulativeOffset(img);
	var offsetX = offset[0];
	var offsetY = offset[1];
	
	img.style.position = 'absolute';
	var x = Event.pointerX(e) - this.startX;
	var y = Event.pointerY(e) - this.startY;
	img.style.left = x + 'px';
	img.style.top = y + 'px';
};

Intermap.prototype.stopDrag = function(e)
{
	Event.stop(e); // prevents from dragging the map image (on Explorer)
	
	// remove listeners 	
	Event.stopObserving(document, 'mousemove', this.cachedMouseMoveListener);
	Event.stopObserving(document, 'mouseup', this.cachedMouseUpListener);

	var img = $(this.imageId);

	// get map image offset
	var offset = Position.cumulativeOffset(img);
	var offsetX = offset[0];
	var offsetY = offset[1];
	
	// get the current cursor position
	this.setStatus('busy');

	var w = img.clientWidth;
	var h = img.clientHeight;

	this.ajax_move(this.startDragX - offsetX, 
					offsetY - this.startDragY, 
					w, h);
};

Intermap.prototype.ajax_move = function(deltax, deltay, width, height)
{
	var pars = 'deltax=' + deltax + '&deltay=' + deltay + 
		"&width=" + width + "&height=" + height +
		"&" + this.getURLbbox();	 // FIXME: we need it as func param                
	
	var myAjax = new Ajax.Request (
		getIMServiceURL('map.move'), 
		{
			method: 'get',
			parameters: pars,
			onComplete: this.imageRebuilt.bindAsEventListener(this),
			onFailure: reportError
		}
	);
};

//==================================================
// Full extents
//==================================================

Intermap.prototype.fullExtent = function()
{
	this.setStatus('busy');
	
	this.setTool('zoomin');

	var pars = "width=" + this.width + "&height="+ this.height;

	var myAjax = new Ajax.Request (
		getIMServiceURL('map.fullExtent'), 
		{
			method: 'get',
			parameters: pars,
			onComplete: this.imageRebuilt.bindAsEventListener(this),
			onFailure: reportError
		}
	);
};


//==================================================
// updates the map image

/**
 * Update the map image, using current size and bbox.  
 * @param {Function} callback The function to invoke when the AJAX call has completed.
 *   The callback is invoked <b>after</b> the afterImageRebuilt event. 
 */
Intermap.prototype.rebuild = function(callback)
{
	// No image to display yet
	if (! $(this.imageId))
	{
		if(callback)
		{
			callback();
		}
		return;		
	}
	    
	var pars = "width=" + this.width 
			+ "&height=" + this.height;
    
	qbbox = this.getURLbbox();
	if(qbbox)
		pars +=  "&" + qbbox;
    
    this.setStatus('busy');
    
    var thismap = this;

    var myAjax = new Ajax.Request (
    	getIMServiceURL('map.update'), 
    	{
			method: 'get',
			parameters: pars,
			onComplete: function(req) 
			{ 
				thismap.imageRebuilt(req);
				if(callback)
				{
					callback();
				}
			}.bindAsEventListener(thismap),
    		 
    		onFailure: function(req)
    		{
    		    alert("ERROR"); // FIXME TODO
    		}
    	}
    );
};

/**
 * This function is automatically called when the image has been rebuilt.
 * @param {Element} req The AJAX request
 */
Intermap.prototype.imageRebuilt = function(req)
{
	this.set_dom(req.responseXML);
	
	var img = $(this.imageId);
	
	// set map image offset
	img.style.left = '0';
	img.style.top = '0';
		
	if(this.afterImageRebuilt)
	{
		this.afterImageRebuilt(req);
	}
	
	this.setStatus('idle'); 
};

/**
 * Set the status of this map.
 * It can be "busy" or "idle". 
 * While busy, the mouse listeners are turned off, and the <i>wait</i> element is displayed.
 * @param {string} status Can be 'idle' or 'busy'
 */
Intermap.prototype.setStatus = function(status)
{
    // init it
    if( ! this.cachedMouseDownListener)
	{
		//alert("INIT cachemousedown");
        this.cachedMouseDownListener = this.mousedownEventListener.bindAsEventListener(this);
	}

	switch(status)
	{
		case 'busy': // not allowed to refresh - wait for ajax transaction to finish
			// disable zoom, pan...
			
			Event.stopObserving(this.imageId, 'mousedown', this.cachedMousedownListener);
			Event.observe(this.imageId, 'mousedown', this.noOp);
			
			// change cursor
			$(this.imageId).style.cursor = 'wait';
			$(this.imageId+'_waitdiv').show(); // TODO

			break;
			
		case 'idle': // all operations allowed 
			// enable zoom, pan...
			Event.stopObserving(this.imageId, 'mousedown', this.cachedMousedownListener);
			
			Event.stopObserving(this.imageId, 'mousedown', this.noOp);
			Event.observe(this.imageId, 'mousedown', this.cachedMouseDownListener);
			
			// change refresh button status
			$(this.imageId).style.cursor = null; // use the CSS definition
			$(this.imageId+'_waitdiv').hide(); // TODO	
			
			/*append("THIS is now" + this + " -- " + this.imageId);*/
			
			break;
			
		default:
			alert("Unknown status '"+status+"'");
	}
};

/** Just prevents the user to drag the image while trying to zoom */
Intermap.prototype.noOp = function(e)
{
	Event.stop(e); // prevents from dragging the map image (on Firefox)
};


function append(msg)
{
	var qqq = document.createElement('div');
	qqq.innerHTML = msg;
	document.body.appendChild(qqq);

}

//==================================================
