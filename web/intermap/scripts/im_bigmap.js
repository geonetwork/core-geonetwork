/*****************************************************************************
 *
 *                 Functions related to the big IM map
 *
 *****************************************************************************/

var autoRefresh = true;


// Opens the print window
function print() {
	window.open('{/root/gui/locService}/map.getPrintImage','print', width='620', height='620');
}

/*****************************************************************************
 *
 *                      Main vars and setup
 *
 *****************************************************************************/

// Current bounding box
var im_bm_north, im_bm_east, im_bm_south, im_bm_west;
// Current image size
var im_bm_width, im_bm_height; 

// Parse a response and set bm props accordingly
function im_bm_set(bmResponse)
{
           // Image URL
           var url = bmResponse.getElementsByTagName('imgUrl')[0].textContent;
	$('im_mapImg').src = url;
	
	// BBox
	var extent= bmResponse.getElementsByTagName('extent')[0];
	var minx  = extent.getAttribute('minx');
	var maxx = extent.getAttribute('maxx');
	var miny  = extent.getAttribute('miny');
	var maxy = extent.getAttribute('maxy');

           // Image size
	var w = bmResponse.getElementsByTagName('width')[0].textContent;
	var h = bmResponse.getElementsByTagName('height')[0].textContent;

         im_bm_setMapProp(maxy, maxx, miny, minx, w, h);
}

// Set width, height and bounding box
function im_bm_setMapProp(n, e, s, w, width, height)
{
    im_bm_north=new Number(n);
    im_bm_east = new Number(e);
    im_bm_south = new Number(s);
    im_bm_west = new Number(w);
    
    im_bm_width=new Number(width);
    im_bm_height=new Number(height);
    
    im_bm_setSize(width, height);
}

function im_bm_setSize(w, h)
{
	$('im_mapContainer').style.width = new Number(w)+2; 
	$('im_mapContainer').style.height = new Number(h)+2; 
	
	$('im_map').style.width = w; //<xsl:value-of select="/root/response/mapRoot/response/imageWidth" /> + 'px';
	$('im_map').style.height = h; //<xsl:value-of select="/root/response/mapRoot/response/imageHeight" /> + 'px';
	
	$('im_pleaseWait').style.width = w; 
/*	$('im_pleaseWait').style.height = h; 	
	$('im_pleaseWait').style.top = h/2;*/	
}



function im_bm_getURLbbox()
{
    return   "bbnorth="+im_bm_north+
                "&bbeast="+im_bm_east+
                "&bbsouth="+im_bm_south+
                "&bbwest="+im_bm_west;    
}


/*****************************************************************************
 *
 *                      Map operations (zoom, pan, identify)
 *
 *****************************************************************************/

var startX, startY; // start (mousedown) coordinates

var startOffsetX;
var startOffsetY;

var currentTool;

function setTool(tool) {
	deleteAoi();
	currentTool = tool;
	$('intermap_root').className = tool; //document.body.className = tool;
	if (tool != 'aoi') $('im_geonetRecords').className = 'im_hidden';
}


function mousedownEventListener(e)
{
	switch(currentTool)
	{
		case 'zoomin':
			startZoombox(e);
			break;
		case 'zoomout':
			startZoombox(e);
			break;
		case 'pan':
			startDrag(e);
			break;
		case 'aoi':
			startAoi(e);
			break;
		case 'identify':
			identify(e);
			break;
	}
}

// IDENTIFY - TODO: move some part intermap.js
function identify(e)
{
	Event.stop(e); // prevents from dragging the map image (on Firefox)
	
	// get map image offset
	var offset = Position.cumulativeOffset($('im_mapImg'));
	var offsetX = offset[0];
	var offsetY = offset[1];
	
	// store starting cursor position
	pointerX = Event.pointerX(e);
	pointerY = Event.pointerY(e);
	
	var url = '/intermap/srv/en/map.identify';
	var t1 = pointerX - offsetX;
	var t2 = pointerY - offsetY;
	var pars = 'mapimgx=' + t1 + '&mapimgy=' + t2 + '&activeLayer=' + activeLayerId + "&format=" + "text%2Fhtml";
	
	window.open(url + '?' + pars, "Query result", "width=600,height=400,scrollbars=yes,toolbar=no,status=yes,menubar=no,location=yes,resizable=yes");
}

//==================================================
// ZOOM
//==================================================

var zoombox; // zoombox div

function startZoombox(e)
{
	Event.stop(e); // prevents from dragging the map image (on Firefox)
	
	// add mousemove and mouseup listeners
	Event.observe(document, 'mousemove', resizeZoombox);
	Event.observe(document, 'mouseup', stopZoombox);
	
	// store starting cursor position
	startX = Event.pointerX(e);
	startY = Event.pointerY(e);
	
	// dynamically create the zoombox div
	zoombox = document.createElement('div')
	zoombox.setAttribute('id', 'im_zoombox');
	drawZoombox(startX, startY, 0, 0);
	document.body.appendChild(zoombox);
}

// mousemove event listener
function resizeZoombox(e)
{
	Event.stop(e); // prevents from dragging the map image (on Explorer)
	
	// get the current cursor position
	var pX = Event.pointerX(e);
	var pY = Event.pointerY(e);
	
	// set the zoom box position and size
	drawZoombox (
		Math.min(pX, startX),  // left
		Math.min(pY, startY),  // top
		Math.abs(pX - startX), // width
		Math.abs(pY - startY)  // height
	);
}

// mouseup event listener
function stopZoombox(e)
{
	// get the current cursor position
	var pX = Event.pointerX(e);
	var pY = Event.pointerY(e);
	
	// get map image offset
	var offset = Position.cumulativeOffset($('im_mapImg'));
	var offsetX = offset[0];
	var offsetY = offset[1];

            var w = $('im_mapImg').clientWidth;
            var h = $('im_mapImg').clientHeight;


	setStatus('busy');
	
	imc_bm_action(currentTool,
		Math.min(pX, startX) - offsetX, // xmin
		Math.max(pY, startY) - offsetY, // ymax
		Math.max(pX, startX) - offsetX, // xmax
		Math.min(pY, startY) - offsetY,  // ymin
		w,h
	);
	
	// remove listeners and div
	Event.stopObserving(document, 'mousemove', resizeZoombox);
	Event.stopObserving(document, 'mouseup', stopZoombox);
	Element.remove($('im_zoombox'));
}

// Draws the zoombox
function drawZoombox(left, top, width, height)
{
	zoombox.style.left = left + 'px';
	zoombox.style.top = top + 'px';
	zoombox.style.width = width + 'px';
	zoombox.style.height = height + 'px';
}

//==================================================
// MOVE
//==================================================

function startDrag(e)
{
	Event.stop(e); // prevents from dragging the map image (on Firefox)
	
	// get map initial image offset
	var offset = Position.cumulativeOffset($('im_mapImg'));
	startOffsetX = offset[0];
	startOffsetY = offset[1];
	
	// add mousemove and mouseup listeners
	Event.observe(document, 'mousemove', dragImage);
	Event.observe(document, 'mouseup', stopDrag);
	
	// store starting cursor position
	startX = Event.pointerX(e);
	startY = Event.pointerY(e);
}

function dragImage(e)
{
	window.status = Event.pointerX(e) + ' - ' + Event.pointerY(e); // DEBUG
	
	Event.stop(e); // prevents from dragging the map image (on Explorer)
	
	var vMapImg = $('im_mapImg');
	
	// get map image offset
	var offset = Position.cumulativeOffset(vMapImg);
	var offsetX = offset[0];
	var offsetY = offset[1];
	
	vMapImg.style.position = 'absolute';
	var t = Event.pointerX(e) - startX;
	vMapImg.style.left = t + 'px';
	t = Event.pointerY(e) - startY;
	vMapImg.style.top = t + 'px';
}

function stopDrag(e)
{
	// get map image offset
	var offset = Position.cumulativeOffset($('im_mapImg'));
	var offsetX = offset[0];
	var offsetY = offset[1];
	
	// get the current cursor position
	setStatus('busy');
	
	imc_bm_move(startOffsetX - offsetX, offsetY - startOffsetY, 
	                        im_bm_width, im_bm_height, im_bm_getURLbbox() );
	
	Event.stopObserving(document, 'mousemove', dragImage);
	Event.stopObserving(document, 'mouseup', stopDrag);
}

//==================================================
// AOI (Area Of Interest)
//==================================================

var aoi = null;
var ghostImg = null;
var dotUL, dotUR, dotBR, dotBL, dotUM, dotBM, dotMR, dotML;
var divUL, divUR, divBR, divBL, divUM, divBM, divMR, divML;
var aoiLeft, aoiRight, aoiTop, aoiBottom;
var dragUL, dragUR, dragBL, dragBR, dragUM, dragBM, dragML, dragMR;

var mapImgOffsetX;
var mapImgOffsetY;

// TODO: fa schifo
function startAoi(e)
{
	// TODO: all this staff is useless here - too much javascript - move HTML to XSL
	Event.stop(e); // prevents from dragging the map image (on Firefox)
	
	// get map image offset
	var offset = Position.cumulativeOffset($('im_mapImg'));
	mapImgOffsetX = offset[0];
	mapImgOffsetY = offset[1];
	
	// add mousemove and mouseup listeners
	Event.observe(document, 'mousemove', resizeAoi);
	Event.observe(document, 'mouseup', stopAoi);
	
	// store starting cursor position
	startX = Event.pointerX(e);
	startY = Event.pointerY(e);
	
	vMap = $('im_map');
	
	// dynamically create the aoi div
	aoi = document.createElement('div')
	aoi.setAttribute('id', 'im_aoi');
	vMap.appendChild(aoi);
	new Draggable('im_aoi', {change:dragAoi,starteffect:null,endeffect:null,zindex:21000}); // set aoi div draggable
	
	// draw hide  mask (gray overlay over map image)
	var hideMask = document.createElement('div');
	hideMask.setAttribute('id', 'im_hideMask');
	vMap.appendChild(hideMask);
	Position.clone(vMap, hideMask);
	
	// create ghost image
	ghostImg = document.createElement('img');
	ghostImg.setAttribute('id', 'im_ghostImg');
	aoi.appendChild(ghostImg);
	ghostImg.setAttribute('src', $('im_mapImg').src);
	
	
	// when user clicks outside the aoi a new aoi is drawn
	Event.observe($('im_hideMask'), 'mousedown', restartAoi);
	
	// add resoze handles images
	dotUL = document.createElement('img');
	dotUR = document.createElement('img');
	dotBL = document.createElement('img');
	dotBR = document.createElement('img');
	dotUM = document.createElement('img');
	dotBM = document.createElement('img');
	dotML = document.createElement('img');
	dotMR = document.createElement('img');
	
	dotUL.setAttribute('src', '/intermap/images/dot.gif');
	dotUR.setAttribute('src', '/intermap/images/dot.gif');
	dotBL.setAttribute('src', '/intermap/images/dot.gif');
	dotBR.setAttribute('src', '/intermap/images/dot.gif');
	dotUM.setAttribute('src', '/intermap/images/dot.gif');
	dotBM.setAttribute('src', '/intermap/images/dot.gif');
	dotML.setAttribute('src', '/intermap/images/dot.gif');
	dotMR.setAttribute('src', '/intermap/images/dot.gif');
	
	dotUL.className = 'im_resizeDot';
	dotUR.className = 'im_resizeDot';
	dotBL.className = 'im_resizeDot';
	dotBR.className = 'im_resizeDot';
	dotUM.className = 'im_resizeDot';
	dotBM.className = 'im_resizeDot';
	dotML.className = 'im_resizeDot';
	dotMR.className = 'im_resizeDot';
		
	vMap.appendChild(dotUL);
	vMap.appendChild(dotUR);
	vMap.appendChild(dotBL);
	vMap.appendChild(dotBR);
	vMap.appendChild(dotUM);
	vMap.appendChild(dotBM);
	vMap.appendChild(dotML);
	vMap.appendChild(dotMR);
	
	// add resize handles transparent divs
	divUL = document.createElement('div');
	divUR = document.createElement('div');
	divBL = document.createElement('div');
	divBR = document.createElement('div');
	divUM = document.createElement('div');
	divBM = document.createElement('div');
	divML = document.createElement('div');
	divMR = document.createElement('div');
	
	divUL.className = 'im_transparentDrag';
	divUR.className = 'im_transparentDrag';
	divBL.className = 'im_transparentDrag';
	divBR.className = 'im_transparentDrag';
	divUM.className = 'im_transparentDrag';
	divBM.className = 'im_transparentDrag';
	divML.className = 'im_transparentDrag';
	divMR.className = 'im_transparentDrag';
	
	divUL.style.cursor = "nw-resize";
	divUR.style.cursor = "ne-resize";
	divBL.style.cursor = "sw-resize";
	divBR.style.cursor = "se-resize";
	divUM.style.cursor = "n-resize";
	divBM.style.cursor = "s-resize";
	divML.style.cursor = "w-resize";
	divMR.style.cursor = "e-resize";
	
	vMap.appendChild(divUL);
	vMap.appendChild(divUR);
	vMap.appendChild(divBL);
	vMap.appendChild(divBR);
	vMap.appendChild(divUM);
	vMap.appendChild(divBM);
	vMap.appendChild(divML);
	vMap.appendChild(divMR);
	
	dragUL = new Draggable(divUL, {change:dragULListener,starteffect:null,endeffect:null,zindex:32000});
	dragUR = new Draggable(divUR, {change:dragURListener,starteffect:null,endeffect:null,zindex:32000});
	dragBL = new Draggable(divBL, {change:dragBLListener,starteffect:null,endeffect:null,zindex:32000});
	dragBR = new Draggable(divBR, {change:dragBRListener,starteffect:null,endeffect:null,zindex:32000});
	dragUM = new Draggable(divUM, {change:dragUMListener,starteffect:null,endeffect:null,zindex:32000,constraint:'vertical'});
	dragBM = new Draggable(divBM, {change:dragBMListener,starteffect:null,endeffect:null,zindex:32000,constraint:'vertical'});
	dragML = new Draggable(divML, {change:dragMLListener,starteffect:null,endeffect:null,zindex:32000,constraint:'horizontal'});
	dragMR = new Draggable(divMR, {change:dragMRListener,starteffect:null,endeffect:null,zindex:32000,constraint:'horizontal'});
	
	Draggables.addObserver(new divDragEndObserver(divUL));
	Draggables.addObserver(new divDragEndObserver(divUR));
	Draggables.addObserver(new divDragEndObserver(divBL));
	Draggables.addObserver(new divDragEndObserver(divBR));
	Draggables.addObserver(new divDragEndObserver(divUM));
	Draggables.addObserver(new divDragEndObserver(divBM));
	Draggables.addObserver(new divDragEndObserver(divML));
	Draggables.addObserver(new divDragEndObserver(divMR));
	
	Draggables.addObserver(new divDragEndObserver(aoi));
	
	Event.observe('im_aoi', 'dblclick', zoomToAoi);
}

function zoomToAoi() {
	var aoiOffset = Position.cumulativeOffset($('im_aoi'));
	var left = aoiOffset[0];
	var top = aoiOffset[1];
	
	var d = Element.getDimensions($('aoi'));
	var width = d.width;
	var height = d.height;
	
	// get map image offset
	var mapImgOffset = Position.cumulativeOffset($('im_mapImg'));
	var offsetX = mapImgOffset[0];
	var offsetY = mapImgOffset[1];

            var iw = $('im_mapImg').clientWidth;
            var ih = $('im_mapImg').clientHeight;

	deleteAoi();
	unsetAoi();
		
	setStatus('busy');
	
	imc_bm_action('zoomin',
		left - offsetX,
		top - offsetY + height,
		left - offsetX + width,
		top - offsetY,
		iw, ih
	);
	
}

var divDragEndObserver = Class.create();
divDragEndObserver.prototype = {
	initialize: function(element) {
		this.element = $(element);
	},
	
	onEnd: function(eventName, draggable, event) {
		if (Draggables.activeDraggable.element == this.element)
			handleMouseupListener();
	}
}

function handleMouseupListener()
{
	var aoiOffset = Position.cumulativeOffset(aoi);
	var left = aoiOffset[0];
	var top = aoiOffset[1];
	
	var d = Element.getDimensions(aoi);
	var width = d.width;
	var height = d.height;
	
	// get map image offset
	var mapImgOffset = Position.cumulativeOffset($('im_mapImg'));
	var offsetX = mapImgOffset[0];
	var offsetY = mapImgOffset[1];
	
	setAoi(
		left - offsetX,
		top - offsetY + height,
		left - offsetX + width,
		top - offsetY
	);
	
	repositionHandleDivs(left, top, width, height);
	
	getGeonetData(
		left - offsetX,
		top - offsetY + height,
		left - offsetX + width,
		top - offsetY,
		1,
		10
	); // DEBUG
}

// deletes the aoi
// TODO: Remove event handlers too???
function deleteAoi()
{
	if (aoi == null) return;
	
	Element.remove(aoi);
	Element.remove($('im_hideMask'));
	
	Element.remove(dotUL);
	Element.remove(dotUR);
	Element.remove(dotBR);
	Element.remove(dotBL);
	Element.remove(dotUM);
	Element.remove(dotBM);
	Element.remove(dotMR);
	Element.remove(dotML);
	
	Element.remove(divUL);
	Element.remove(divUR);
	Element.remove(divBR);
	Element.remove(divBL);
	Element.remove(divUM);
	Element.remove(divBM);
	Element.remove(divMR);
	Element.remove(divML);
	
	aoi = null;
	ghostImg = null;
}

// called when the user clicks in the grayed area
function restartAoi(e)
{
	deleteAoi();
	startAoi(e);
}

// TODO: Clean and optimize!!!
function dragULListener(e)
{
	var divBROffset = Position.cumulativeOffset(divBR);
	var divULOffset = Position.cumulativeOffset(divUL);
	
	var width = divBROffset[0] - divULOffset[0];
	var height = divBROffset[1] - divULOffset[1];
	
	width = (width > 5 ? width : 5);
	height = (height > 5 ? height : 5);
	
	var top = divULOffset[1] + 3;
	if (top > divBROffset[1] - 2) top = divBROffset[1] - 2;
	var left = divULOffset[0] + 3;
	if (left > divBROffset[0] - 2) left = divBROffset[0] - 2;
	
	drawAoi(left, top, width, height);
	
	Position.clone($('im_mapImg'), $('im_ghostImg'));
}

function dragBRListener(e)
{
	var divBROffset = Position.cumulativeOffset(divBR);
	var divULOffset = Position.cumulativeOffset(divUL);
	
	var width = divBROffset[0] - divULOffset[0];
	var height = divBROffset[1] - divULOffset[1];
	
	width = (width > 5 ? width : 5);
	height = (height > 5 ? height : 5);
	
	drawAoi(divULOffset[0] + 3, divULOffset[1] + 3, width, height);
	
	Position.clone($('im_mapImg'), $('im_ghostImg'));
}

function dragURListener()
{
	var divUROffset = Position.cumulativeOffset(divUR);
	var divBLOffset = Position.cumulativeOffset(divBL);
	
	var width = divUROffset[0] - divBLOffset[0];
	var height = divBLOffset[1] - divUROffset[1];
	
	width = (width > 5 ? width : 5);
	height = (height > 5 ? height : 5);
	
	var top = divUROffset[1] + 3;
	if (top > divBLOffset[1] - 2) top = divBLOffset[1] - 2;
	var left = divBLOffset[0] + 3;
	
	drawAoi(left, top, width, height);
	
	Position.clone($('im_mapImg'), $('im_ghostImg'));
}

function dragBLListener()
{
	var divUROffset = Position.cumulativeOffset(divUR);
	var divBLOffset = Position.cumulativeOffset(divBL);
	
	var width = divUROffset[0] - divBLOffset[0];
	var height = divBLOffset[1] - divUROffset[1];
	
	width = (width > 5 ? width : 5);
	height = (height > 5 ? height : 5);
		
	var top = divUROffset[1] + 3;
//	if (top > divBLOffset[1] - 2) top = divBLOffset[1] - 2;
	var left = divBLOffset[0] + 3;
	if (left > divUROffset[0] - 2) left = divUROffset[0] - 2;
	
	drawAoi(left, top, width, height);
	
	Position.clone($('im_mapImg'), $('im_ghostImg'));
}

function dragUMListener()
{
	var d = Element.getDimensions(aoi);
	
	var height = Position.cumulativeOffset(divBM)[1] - Position.cumulativeOffset(divUM)[1];
	height = (height > 5 ? height : 5);
	
	var top = Position.cumulativeOffset(divUM)[1] + 3;
	if (top > Position.cumulativeOffset(divBM)[1] - 2) top = Position.cumulativeOffset(divBM)[1] - 2;
	
	drawAoi(Position.cumulativeOffset(divUL)[0] + 3, top , d.width, height);
	
	Position.clone($('im_mapImg'), $('im_ghostImg'));
}

function dragBMListener()
{
	var d = Element.getDimensions(aoi);
	
	var height = Position.cumulativeOffset(divBM)[1] - Position.cumulativeOffset(divUM)[1];
	height = (height > 5 ? height : 5);
	
	drawAoi(Position.cumulativeOffset(divUL)[0] + 3, Position.cumulativeOffset(divUM)[1] + 3, d.width, height);
	
	Position.clone($('im_mapImg'), $('im_ghostImg'));
}

function dragMLListener()
{
	var d = Element.getDimensions(aoi);
	
	var width =  Position.cumulativeOffset(divMR)[0] - Position.cumulativeOffset(divML)[0];
	width = (width > 5 ? width : 5);
	
	var left = Position.cumulativeOffset(divML)[0] + 3;
	if (left > Position.cumulativeOffset(divMR)[0] - 2) left = Position.cumulativeOffset(divMR)[0] - 2;
	
	drawAoi(left, Position.cumulativeOffset(divUL)[1] + 3, width, d.height);
	
	Position.clone($('im_mapImg'), $('im_ghostImg'));
}

function dragMRListener()
{
	var d = Element.getDimensions(aoi);
	
	var width = Position.cumulativeOffset(divMR)[0] - Position.cumulativeOffset(divML)[0];
	width = (width > 5 ? width : 5);
	
	drawAoi(Position.cumulativeOffset(divML)[0] + 3, Position.cumulativeOffset(divUL)[1] + 3, width, d.height);
	
	Position.clone($('im_mapImg'), $('im_ghostImg'));
}

// aoi mousemove event listener
function resizeAoi(e)
{
	Event.stop(e); // prevents from dragging the map image (on Explorer)
	
	Position.clone($('im_mapImg'), $('im_ghostImg'));
	
	// get the current cursor position
	var pX = Event.pointerX(e);
	var pY = Event.pointerY(e);
	
	// set the zoom box position and size
	drawAoi (
		Math.min(pX, startX),  // left
		Math.min(pY, startY),  // top
		Math.abs(pX - startX), // width
		Math.abs(pY - startY)  // height
	);
	
	Position.clone($('im_mapImg'), $('im_ghostImg'));
}

// aoi mouseup event listener
function stopAoi(e)
{
	// remove listeners and div
	Event.stopObserving(document, 'mousemove', resizeAoi);
	Event.stopObserving(document, 'mouseup', stopAoi);
	
	// if area is 0 delete aoi
	var d = Element.getDimensions(aoi);
	if (d.width == 0 || d.height == 0) {
		deleteAoi();
		unsetAoi();
		$('im_geonetRecords').className = 'im_hidden';
	}
	// else set the aoi
	else
		handleMouseupListener();
}

// Draws the aoi
function drawAoi(left, top, width, height)
{
	aoi.style.left = left - mapImgOffsetX + 'px';
	aoi.style.top = top - mapImgOffsetY + 'px';
	aoi.style.width = width + 'px';
	aoi.style.height = height + 'px';
	
	drawResizeHandles(left, top, width, height);
}

function dragAoi()
{
	Position.clone($('im_mapImg'), $('im_ghostImg'))
	
	var offset = Position.cumulativeOffset(aoi);
	var left = offset[0];
	var top = offset[1];
	
	var d = Element.getDimensions(aoi);
	var width = d.width;
	var height = d.height;
	
	drawResizeHandles(left, top, width, height);
}

function drawResizeHandles(left, top, width, height)
{	
	var a = left - mapImgOffsetX - 3;
	var b = top - mapImgOffsetY - 3;
	var c = a + width;
	var d = b + height;
	
	dotUL.style.left = a + 'px';
	dotUL.style.top = b + 'px';
	dotUR.style.left = c + 'px';
	dotUR.style.top = b + 'px';
	dotBL.style.left = a + 'px';
	dotBL.style.top = d + 'px';
	dotBR.style.left = c + 'px';
	dotBR.style.top = d + 'px';
	var t = a + width / 2;
	dotUM.style.left = t + 'px';
	dotUM.style.top = b + 'px';
	dotBM.style.left = dotUM.style.left;
	dotBM.style.top = d + 'px';
	dotML.style.left = a + 'px';
	t = b + height / 2;
	dotML.style.top = t + 'px';
	dotMR.style.left = c + 'px';
	dotMR.style.top = dotML.style.top;
}

function repositionHandleDivs(left, top, width, height)
{
	var a = left - mapImgOffsetX - 3;
	var b = top - mapImgOffsetY - 3;
	var c = a + width;
	var d = b + height;
	
	divUL.style.left = a + 'px';
	divUL.style.top = b + 'px';
	divUR.style.left = c + 'px';
	divUR.style.top = b + 'px';
	divBL.style.left = a + 'px';
	divBL.style.top = d + 'px';
	divBR.style.left = c + 'px';
	divBR.style.top = d + 'px';
	var t = a + width / 2;
	divUM.style.left = t + 'px';
	divUM.style.top = b + 'px';
	divBM.style.left = divUM.style.left;
	divBM.style.top = d + 'px';
	divML.style.left = a + 'px';
	t = b + height / 2;
	divML.style.top = t + 'px';
	divMR.style.left = c + 'px';
	divMR.style.top = divML.style.top;
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

function refreshNeeded()
{
	if (autoRefresh)
	{
		imc_updateBigMap(im_bm_width, im_bm_height, im_bm_getURLbbox());
	}
	else
	{
		setStatus('refresh');
	}
}

function setStatus(status)
{
	var refreshButton = $('im_refreshButton');
	
	switch(status)
	{
		case 'busy': // not allowed to refresh - wait for ajax transaction to finish
			// disable zoom, pan...
			
			// if(!$('map')) break; // ETj
			
			Event.stopObserving('im_map', 'mousedown', mousedownEventListener);
			Event.observe('im_map', 'mousedown', noOp);
			
			// change cursor
			$('im_map').style.cursor = 'wait'
			
			// change refresh button status
			refreshButton.className = 'im_disabled';
			refreshButton.disabled = true;
			
			$('im_pleaseWait').show(); //style.display = 'block';
			break;
		case 'idle': // all operations allowed
			// enable zoom, pan...
			Event.stopObserving('im_map', 'mousedown', noOp);
			Event.observe('im_map', 'mousedown', mousedownEventListener);
			
			// change refresh button status
			$('im_map').style.cursor = 'crosshair'
			refreshButton.className = 'im_disabled';
			refreshButton.disabled = true;
			
			$('im_pleaseWait').hide(); //style.display = 'none';
			break;
		case 'refresh': // refresh buton highlighted - means that refresh is needed after the user made some operations on layers
			// change refresh button status
			refreshButton.className = 'im_refresh';
			refreshButton.disabled = false;
			break;
	}
}

// Just prevents the user to drag the image while trying to zoom
function noOp(e)
{
	Event.stop(e); // prevents from dragging the map image (on Firefox)
}

//==================================================
// 
//==================================================


function layerAdded(req)
{
	updateMapImage(req);
	imc_reloadLayers();
}

function resizeImage()
{
	imc_toggleImageSize();
}




/*****************************************************************************
 *
 *                             Keyboard events
 *
 *****************************************************************************/

function keyPressed(e)
{
	
	if (e.keyCode == 8) // backspace
	{ 
		deleteButtonListener();
		Event.stop(e);
	}
}

//==================================================
// 
//==================================================

// updates the map images
function updateMapImage(req)
{
	var vMapImg = $('im_mapImg');
	var vGhostImg = $('im_ghostImg');

	// set inner vars
            im_bm_set(req.responseXML);

	// get the new values from response XML
	var imageSrc = req.responseXML.getElementsByTagName('imgUrl')[0].firstChild.nodeValue;
	var scale = req.responseXML.getElementsByTagName('scale')[0].firstChild.nodeValue;
	
/*	src is set by im_bm_set() 
            // update the map image
	vMapImg.src = imageSrc;
*/	
	// set map image offset
	vMapImg.style.left = '0';
	vMapImg.style.top = '0';
	if (ghostImg != null)
	{
		vGhostImg.src = imageSrc;
		vGhostImg.style.left = '0';
		vGhostImg.style.top = '0';
	}

	// update the scale text
	deleteChildNodes($('im_scale'));
	$('im_scale').appendChild( document.createTextNode('1:' + scale));
	
	
//	Event.observe(vMapImg, 'load', function(e) { setStatus('idle') }); // better behaviour but needs debugging on explorer (newer version of prototype?)
	setStatus('idle');
	
	
	// the minimap must follow the bigger map 
	// !!! TODO check if this refresh is not due to a minimap action, or we'll get a refresh loop !!!
	
	imc_mm_update(im_mm_width, im_mm_height, im_dezoom(im_bm_north, im_bm_east, im_bm_south, im_bm_west));	
}

