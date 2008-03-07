//===================================================================
//
//                      MiniMap 
//
//===================================================================

var im_mm = new Intermap(200, 100, 'im_mm_image');
// im_mm.setBBox(90, 180, -90, -180);

/** The AoI FakeBox */
var im_aoi = new FakeBox('im_mm_aoibox');

/** The bbox inputtextes */
var im_mm_ctrl_n,im_mm_ctrl_e, im_mm_ctrl_s, im_mm_ctrl_w; 

function trace(fname)
{
    alert("Entering function --> " + fname);
}


function im_mm_initTextControls(n, e, s, w)
{
	im_mm_ctrl_n = n;
	im_mm_ctrl_e = e;
	im_mm_ctrl_s = s;
	im_mm_ctrl_w = w;
}

function im_mm_setTextCoords(north, east, south, west) 
{
	im_mm_ctrl_n.value = north;
	im_mm_ctrl_e.value = east;
	im_mm_ctrl_s.value = south;
	im_mm_ctrl_w.value = west;
}

/** Set coords into input text given a screen rect */
function im_mm_setTextLenght(top,right,bottom,left) {

	var north = im_mm.y2lat(top);
	var south = im_mm.y2lat(bottom);
	var west  = im_mm.x2lon(left);
	var east  = im_mm.x2lon(right);
	
	im_mm_setTextCoords(north, east, south, west);
}

function im_mm_getURLbbox()
{
	var ret = im_mm.getURLbbox();
	return ret !== null ? ret : im_mm_getURLselectedbbox();
}

function im_mm_getURLselectedbbox()
{
	return MapUtils.urlizebb(
		im_mm_ctrl_n.value,
		im_mm_ctrl_e.value,
		im_mm_ctrl_s.value,
		im_mm_ctrl_w.value);
}

//===================================================================
//
//   Wrappers for GUI buttons
//
//===================================================================

/** Called by GUI buttons */
function im_mm_setTool(tool) 
{
	im_mm.setTool(tool);
}

/** Called by GUI buttons */
function im_mm_fullExtent()
{
    im_mm.fullExtent();
}

//===================================================================
//
//   MiniMap operations (zoom, pan, identify)
//
//===================================================================


im_mm.afterToolSet = function(tool)
{
	$('minimap_root').className = tool; 	
};

im_mm.beforeMouseDown = function(e)
{
	//im_aoi.hide();
};

im_mm.unresolvedMouseDown = function(e)
{
	switch(this.tool)
	{
		case 'aoi':
			im_aoi.show();		
			im_mm_startAoi(e);
			break;
	}
};


//==================================================
// AOI
//==================================================

function im_mm_startAoi(e)
{
	Event.stop(e); // prevents from dragging the map image (on Firefox)
	
	// add mousemove and mouseup listeners
	Event.observe(document, 'mousemove', im_mm_resizeAoi);
	Event.observe(document, 'mouseup', im_mm_stopAoi);
	
	var offset = Position.cumulativeOffset($(im_mm.imageId));
	var offsetX = offset[0];
	var offsetY = offset[1];

	// store starting cursor position
	im_mm_startX = Event.pointerX(e) - offsetX + 1;
	im_mm_startY = Event.pointerY(e) - offsetY + 1;
	
	im_aoi.draw(im_mm_startX, im_mm_startY, 0, 0);
	
	im_mm_setTextLenght(im_mm_startY, im_mm_startX, im_mm_startY, im_mm_startX);
}

// mousemove event listener
function im_mm_resizeAoi(e)
{
	Event.stop(e); // prevents from dragging the map image (on Explorer)
	
	// get map image offset
	var offset = Position.cumulativeOffset($(im_mm.imageId));
	var offsetX = offset[0];
	var offsetY = offset[1];

	// get the current cursor position -- relative to image
	var pX = Event.pointerX(e) - offsetX;
	var pY = Event.pointerY(e) - offsetY;
	
	pX = Math.max(pX, 0);
	pY = Math.max(pY, 0);

	pX = Math.min(pX, im_mm.width - 1);
	pY = Math.min(pY, im_mm.height - 1);

	// set the zoom box position and size
	im_aoi.draw(Math.min(pX, im_mm_startX),  // left
				Math.min(pY, im_mm_startY),  // top
				Math.abs(pX - im_mm_startX), // width
				Math.abs(pY - im_mm_startY)  // height
	);
}

// mouseup event listener
function im_mm_stopAoi(e)
{
	// remove listeners and div
	Event.stopObserving(document, 'mousemove', im_mm_resizeAoi);
	Event.stopObserving(document, 'mouseup', im_mm_stopAoi);

	// get map image offset
	var offset = Position.cumulativeOffset($(im_mm.imageId));
	var offsetX = offset[0];
	var offsetY = offset[1];

	// get the current cursor position -- relative to image
	var pX = Event.pointerX(e) - offsetX;
	var pY = Event.pointerY(e) - offsetY;
	
	pX = Math.max(pX, 0);
	pY = Math.max(pY, 0);

	pX = Math.min(pX, im_mm.width - 1);
	pY = Math.min(pY, im_mm.height - 1);

	im_mm_setTextLenght(
		Math.min(pY, im_mm_startY),  // top
		Math.max(pX, im_mm_startX),  // right
		Math.max(pY, im_mm_startY),  // bottom
		Math.min(pX, im_mm_startX) // left
	);
    
	var func = im_mm_aoiUpdated;
	if(typeof func == 'function')
	{
		func();
	}
	// AoI has not an area (see drawFakeBox), so no need to redefine observers.	
}

//==================================================

function im_mm_zoomToAoI()
{
	im_mm.setBBox(	parseFloat(im_mm_ctrl_n.value), 
					parseFloat(im_mm_ctrl_e.value), 
					parseFloat(im_mm_ctrl_s.value), 
					parseFloat(im_mm_ctrl_w.value));
					
	im_mm.rebuild();		
}
    
/**
 * Expand the AoI to the fullext extent
 */
function im_mm_fullAoI()
{
	// set the text coords
	im_mm_setTextLenght(0, im_mm.width - 1,im_mm.height - 1, 0); //  top right bottom left 
	
	// set the box
	// im_mm_redrawAoI(); // <--- this is the proper way to draw the box...
	// ... but we can avoid ugly rounding errors by forcing to the full image extent 
	im_aoi.draw(0, 0, im_mm.width, im_mm.height); // x y width height	
}

/**
 * Redraw the AoI from the coords value held in input text 
 */
function im_mm_redrawAoI()
{
	var x1 = im_mm.lon2x(parseFloat(im_mm_ctrl_w.value));
	var x2 = im_mm.lon2x(parseFloat(im_mm_ctrl_e.value));
	var y1 = im_mm.lat2y(parseFloat(im_mm_ctrl_n.value));
	var y2 = im_mm.lat2y(parseFloat(im_mm_ctrl_s.value));
	var w = Math.abs(x2-x1);
	var h = Math.abs(y2-y1);
	
	im_aoi.show();	
	im_aoi.draw(x1, y1, w, h);
}

//==================================================
//==================================================

im_mm.afterImageRebuilt = function(req)
{
	im_mm_redrawAoI();	
};


