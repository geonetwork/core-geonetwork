/*****************************************************************************
 *
 *                      MiniMap operations (zoom, pan, identify)
 *
 *
External references:
#im_mm_toolbar

Need:
	imc_minimapAction(
	callback im_mm_updateMapImage


 *****************************************************************************/

var im_mm_currentTool;



//DOC public function im_mm_setTool(tool)
function im_mm_setTool(tool) {
	im_mm_deleteAoi();
	im_mm_currentTool = tool;
	$('minimap_root').className = tool; 
}


var im_mm_startX, im_mm_startY; // start (mousedown) coordinates

function im_mm_mousedownEventListener(e)
{
	switch(im_mm_currentTool)
	{
		case 'zoomin':
			im_mm_startZoombox(e);
			break;
		case 'zoomout':
			im_mm_startZoombox(e);
			break;
		case 'pan':
			im_mm_startDrag(e);
			break;
		case 'aoi':
			im_mm_startAoi(e);
			break;
	}
}


// ZOOM
var im_mm_zoombox; // zoombox div

function im_mm_startZoombox(e)
{
	Event.stop(e); // prevents from dragging the map image (on Firefox)
	
	// add mousemove and mouseup listeners
	Event.observe(document, 'mousemove', im_mm_resizeZoombox);
	Event.observe(document, 'mouseup', im_mm_stopZoombox);
	
	// store starting cursor position
	im_mm_startX = Event.pointerX(e);
	im_mm_startY = Event.pointerY(e);
	
	// dynamically create the zoombox div
	im_mm_zoombox = document.createElement('div')
	im_mm_zoombox.setAttribute('id', 'im_mm_zoombox');
	im_mm_drawZoombox(im_mm_startX, im_mm_startY, 0, 0);
	document.body.appendChild(im_mm_zoombox);
}

// mousemove event listener
function im_mm_resizeZoombox(e)
{
	Event.stop(e); // prevents from dragging the map image (on Explorer)
	
	// get the current cursor position
	var pX = Event.pointerX(e);
	var pY = Event.pointerY(e);
	
	// set the zoom box position and size
	im_mm_drawZoombox (
		Math.min(pX, im_mm_startX),  // left
		Math.min(pY, im_mm_startY),  // top
		Math.abs(pX - im_mm_startX), // width
		Math.abs(pY - im_mm_startY)  // height
	);
}

// mouseup event listener
function im_mm_stopZoombox(e)
{
	// get the current cursor position
	var pX = Event.pointerX(e);
	var pY = Event.pointerY(e);
	
	// get map image offset
	var offset = Position.cumulativeOffset($('im_mm_image'));
	var offsetX = offset[0];
	var offsetY = offset[1];
	
	im_mm_setStatus('busy');
	
	imc_minimapAction(
		Math.min(pX, im_mm_startX) - offsetX, // xmin
		Math.max(pY, im_mm_startY) - offsetY, // ymax
		Math.max(pX, im_mm_startX) - offsetX, // xmax
		Math.min(pY, im_mm_startY) - offsetY  // ymin
	);
	
	// remove listeners and div
	Event.stopObserving(document, 'mousemove', im_mm_resizeZoombox);
	Event.stopObserving(document, 'mouseup', im_mm_stopZoombox);
	Element.remove($('im_mm_zoombox'));
}

// Draws the zoombox
function im_mm_drawZoombox(left, top, width, height)
{
	im_mm_zoombox.style.left = left + 'px';
	im_mm_zoombox.style.top = top + 'px';
	im_mm_zoombox.style.width = width + 'px';
	im_mm_zoombox.style.height = height + 'px';
}

// DRAG
function im_mm_startDrag(e)
{
	Event.stop(e); // prevents from dragging the map image (on Firefox)
	
	// get map initial image offset
	var offset = Position.cumulativeOffset($('im_mm_image'));
	im_mm_startOffsetX = offset[0];
	im_mm_startOffsetY = offset[1];
	
	// add mousemove and mouseup listeners
	Event.observe(document, 'mousemove', im_mm_dragImage);
	Event.observe(document, 'mouseup', im_mm_stopDrag);
	
	// store starting cursor position
	im_mm_startX = Event.pointerX(e);
	im_mm_startY = Event.pointerY(e);
}

function im_mm_dragImage(e)
{
	window.status = Event.pointerX(e) + ' - ' + Event.pointerY(e); // DEBUG
	
	Event.stop(e); // prevents from dragging the map image (on Explorer)
	
	var vMapImg = $('im_mm_image');
	
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

function im_mm_stopDrag(e)
{
	// get map image offset
	var offset = Position.cumulativeOffset($('im_mm_image'));
	var offsetX = offset[0];
	var offsetY = offset[1];
	
	// get the current cursor position
	im_mm_setStatus('busy');
	
	im_mm_move(im_mm_startOffsetX - offsetX, offsetY - im_mm_startOffsetY)
	
	Event.stopObserving(document, 'mousemove', im_mm_dragImage);
	Event.stopObserving(document, 'mouseup', im_mm_stopDrag);
}

// THIS FUNCTION MUST GO IN intermap.js
function im_mm_move(deltax, deltay)
{
	var url = '/intermap/srv/en/map.move';  // FIXME
	var pars = 'deltax=' + deltax + '&deltay=' + deltay;
	
	var myAjax = new Ajax.Request (
		url, 
		{
			method: 'get',
			parameters: pars,
			onComplete: im_mm_updateMapImage,
			onFailure: reportError
		}
	);
}

// updates the map images
function im_mm_updateMapImage(req)
{
	var vMapImg = $('im_mm_image');
	var vGhostImg = $('im_mm_ghost');
	
	// get the new values from response XML
	var imageSrc = req.responseXML.getElementsByTagName('imgUrl')[0].firstChild.nodeValue;
	
	// update the map image
	vMapImg.src = imageSrc;
	
	// set map image offset
	vMapImg.style.left = '0';
	vMapImg.style.top = '0';
	if (ghostImg != null)
	{
		vGhostImg.src = imageSrc;
		vGhostImg.style.left = '0';
		vGhostImg.style.top = '0';
	}

	
//	Event.observe(vMapImg, 'load', function(e) { setStatus('idle') }); // better behaviour but needs debugging on explorer (newer version of prototype?)
	im_mm_setStatus('idle');
}


// AOI (Area Of Interest)
var im_mm_aoi = null;
var im_mm_ghostImg = null;
//var dotUL, dotUR, dotBR, dotBL, dotUM, dotBM, dotMR, dotML;
var im_mm_divUL, im_mm_divUR, im_mm_divBR, im_mm_divBL, im_mm_divUM, im_mm_divBM, im_mm_divMR, im_mm_divML;
var im_mm_aoiLeft, im_mm_aoiRight, im_mm_aoiTop, im_mm_aoiBottom;
var im_mm_dragUL, im_mm_dragUR, im_mm_dragBL, im_mm_dragBR, im_mm_dragUM, im_mm_dragBM, im_mm_dragML, im_mm_dragMR;

var im_mm_mapImgOffsetX;
var im_mm_mapImgOffsetY;

// TODO: fa schifo
function im_mm_startAoi(e)
{
	// TODO: all this staff is useless here - too much javascript - move HTML to XSL
	Event.stop(e); // prevents from dragging the map image (on Firefox)
	
	// get map image offset
	var offset = Position.cumulativeOffset($('im_mm_image'));
	im_mm_mapImgOffsetX = offset[0];
	im_mm_mapImgOffsetY = offset[1];
	
	// add mousemove and mouseup listeners
	Event.observe(document, 'mousemove', im_mm_resizeAoi);
	Event.observe(document, 'mouseup', im_mm_stopAoi);
	
	// store starting cursor position
	im_mm_startX = Event.pointerX(e);
	im_mm_startY = Event.pointerY(e);
	
	vMap = $('im_mm_map');
	
	// dynamically create the aoi div
	im_mm_aoi = document.createElement('div')
	im_mm_aoi.setAttribute('id', 'im_mm_aoi');
	vMap.appendChild(aoi);
	new Draggable('im_mm_aoi', {change:im_mm_dragAoi,starteffect:null,endeffect:null,zindex:21000}); // set aoi div draggable
	
	// draw hide  mask (gray overlay over map image)
	var hideMask = document.createElement('div');
	hideMask.setAttribute('id', 'im_mm_hideMask');
	vMap.appendChild(hideMask);
	Position.clone(vMap, hideMask);
	
	// create ghost image
	im_mm_ghostImg = document.createElement('img');
	im_mm_ghostImg.setAttribute('id', 'im_mm_ghost');
	im_mm_aoi.appendChild(im_mm_ghostImg);
	im_mm_ghostImg.setAttribute('src', $('im_mm_image').src);
	
	
	// when user clicks outside the aoi a new aoi is drawn
	Event.observe($('im_mm_hideMask'), 'mousedown', im_mm_restartAoi);
	
	// add resoze handles images
	im_mm_dotUL = document.createElement('img');
	im_mm_dotUR = document.createElement('img');
	im_mm_dotBL = document.createElement('img');
	im_mm_dotBR = document.createElement('img');
	im_mm_dotUM = document.createElement('img');
	im_mm_dotBM = document.createElement('img');
	im_mm_dotML = document.createElement('img');
	im_mm_dotMR = document.createElement('img');
	
	im_mm_dotUL.setAttribute('src', '/intermap/images/dot.gif');
	im_mm_dotUR.setAttribute('src', '/intermap/images/dot.gif');
	im_mm_dotBL.setAttribute('src', '/intermap/images/dot.gif');
	im_mm_dotBR.setAttribute('src', '/intermap/images/dot.gif');
	im_mm_dotUM.setAttribute('src', '/intermap/images/dot.gif');
	im_mm_dotBM.setAttribute('src', '/intermap/images/dot.gif');
	im_mm_dotML.setAttribute('src', '/intermap/images/dot.gif');
	im_mm_dotMR.setAttribute('src', '/intermap/images/dot.gif');
	
	im_mm_dotUL.className = 'resizeDot';
	im_mm_dotUR.className = 'resizeDot';
	im_mm_dotBL.className = 'resizeDot';
	im_mm_dotBR.className = 'resizeDot';
	im_mm_dotUM.className = 'resizeDot';
	im_mm_dotBM.className = 'resizeDot';
	im_mm_dotML.className = 'resizeDot';
	im_mm_dotMR.className = 'resizeDot';
	
	vMap.appendChild(im_mm_dotUL);
	vMap.appendChild(im_mm_dotUR);
	vMap.appendChild(im_mm_dotBL);
	vMap.appendChild(im_mm_dotBR);
	vMap.appendChild(im_mm_dotUM);
	vMap.appendChild(im_mm_dotBM);
	vMap.appendChild(im_mm_dotML);
	vMap.appendChild(im_mm_dotMR);
	
	// add resize handles transparent divs
	im_mm_divUL = document.createElement('div');
	im_mm_divUR = document.createElement('div');
	im_mm_divBL = document.createElement('div');
	im_mm_divBR = document.createElement('div');
	im_mm_divUM = document.createElement('div');
	im_mm_divBM = document.createElement('div');
	im_mm_divML = document.createElement('div');
	im_mm_divMR = document.createElement('div');
	
	im_mm_divUL.className = 'transparentDrag';
	im_mm_divUR.className = 'transparentDrag';
	im_mm_divBL.className = 'transparentDrag';
	im_mm_divBR.className = 'transparentDrag';
	im_mm_divUM.className = 'transparentDrag';
	im_mm_divBM.className = 'transparentDrag';
	im_mm_divML.className = 'transparentDrag';
	im_mm_divMR.className = 'transparentDrag';
	
	vMap.appendChild(im_mm_divUL);
	vMap.appendChild(im_mm_divUR);
	vMap.appendChild(im_mm_divBL);
	vMap.appendChild(im_mm_divBR);
	vMap.appendChild(im_mm_divUM);
	vMap.appendChild(im_mm_divBM);
	vMap.appendChild(im_mm_divML);
	vMap.appendChild(im_mm_divMR);
	
	im_mm_dragUL = new Draggable(im_mm_divUL, {change:im_mm_dragULListener,starteffect:null,endeffect:null,zindex:32000});
	im_mm_dragUR = new Draggable(im_mm_divUR, {change:im_mm_dragURListener,starteffect:null,endeffect:null,zindex:32000});
	im_mm_dragBL = new Draggable(im_mm_divBL, {change:im_mm_dragBLListener,starteffect:null,endeffect:null,zindex:32000});
	im_mm_dragBR = new Draggable(im_mm_divBR, {change:im_mm_dragBRListener,starteffect:null,endeffect:null,zindex:32000});
	im_mm_dragUM = new Draggable(im_mm_divUM, {change:im_mm_dragUMListener,starteffect:null,endeffect:null,zindex:32000,constraint:'vertical'});
	im_mm_dragBM = new Draggable(im_mm_divBM, {change:im_mm_dragBMListener,starteffect:null,endeffect:null,zindex:32000,constraint:'vertical'});
	im_mm_dragML = new Draggable(im_mm_divML, {change:im_mm_dragMLListener,starteffect:null,endeffect:null,zindex:32000,constraint:'horizontal'});
	im_mm_dragMR = new Draggable(im_mm_divMR, {change:im_mm_dragMRListener,starteffect:null,endeffect:null,zindex:32000,constraint:'horizontal'});
	
	Draggables.addObserver(new im_mm_divDragEndObserver(im_mm_divUL));
	Draggables.addObserver(new im_mm_divDragEndObserver(im_mm_divUR));
	Draggables.addObserver(new im_mm_divDragEndObserver(im_mm_divBL));
	Draggables.addObserver(new im_mm_divDragEndObserver(im_mm_divBR));
	Draggables.addObserver(new im_mm_divDragEndObserver(im_mm_divUM));
	Draggables.addObserver(new im_mm_divDragEndObserver(im_mm_divBM));
	Draggables.addObserver(new im_mm_divDragEndObserver(im_mm_divML));
	Draggables.addObserver(new im_mm_divDragEndObserver(im_mm_divMR));
	
	Draggables.addObserver(new im_mm_divDragEndObserver(im_mm_aoi));
	
	Event.observe('im_mm_aoi', 'dblclick', im_mm_zoomToAoi);
}

function zoomToAoi() {
	var aoiOffset = Position.cumulativeOffset($('im_mm_aoi'));
	var left = aoiOffset[0];
	var top = aoiOffset[1];
	
	var d = Element.getDimensions($('im_mm_aoi'));
	var width = d.width;
	var height = d.height;
	
	// get map image offset
	var mapImgOffset = Position.cumulativeOffset($('im_mm_image'));
	var offsetX = mapImgOffset[0];
	var offsetY = mapImgOffset[1];
	
	im_mm_deleteAoi();
	im_mm_unsetAoi();
	
	im_mm_currentToolT = im_mm_currentTool;
	im_mm_currentTool = 'im_mm_zoomin';
	
	im_mm_setStatus('busy');
	
	imc_minimapAction(
	           left - offsetX,
		top - offsetY + height,
		left - offsetX + width,
		top - offsetY
	);
	
	im_mm_currentTool = im_mm_currentToolT;	
}

var im_mm_divDragEndObserver = Class.create();
im_mm_divDragEndObserver.prototype = {
	initialize: function(element) {
		this.element = $(element);
	},
	
	onEnd: function(eventName, draggable, event) {
		if (Draggables.activeDraggable.element == this.element)
			im_mm_handleMouseupListener();
	}
}

function im_mm_handleMouseupListener()
{
	var aoiOffset = Position.cumulativeOffset(im_mm_aoi);
	var left = aoiOffset[0];
	var top = aoiOffset[1];
	
	var d = Element.getDimensions(im_mm_aoi);
	var width = d.width;
	var height = d.height;
	
	// get map image offset
	var mapImgOffset = Position.cumulativeOffset($('im_mm_image'));
	var offsetX = mapImgOffset[0];
	var offsetY = mapImgOffset[1];
	
	im_mm_setAoi(
		left - offsetX,
		top - offsetY + height,
		left - offsetX + width,
		top - offsetY
	);
	
	im_mm_repositionHandleDivs(left, top, width, height);
	
	im_mm_getGeonetData(
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
function im_mm_deleteAoi()
{
	if (im_mm_aoi == null) return;
	
	Element.remove(im_mm_aoi);
	Element.remove($('im_mm_hideMask'));
	
	Element.remove(im_mm_dotUL);
	Element.remove(im_mm_dotUR);
	Element.remove(im_mm_dotBR);
	Element.remove(im_mm_dotBL);
	Element.remove(im_mm_dotUM);
	Element.remove(im_mm_dotBM);
	Element.remove(im_mm_dotMR);
	Element.remove(im_mm_dotML);
	
	Element.remove(im_mm_divUL);
	Element.remove(im_mm_divUR);
	Element.remove(im_mm_divBR);
	Element.remove(im_mm_divBL);
	Element.remove(im_mm_divUM);
	Element.remove(im_mm_divBM);
	Element.remove(im_mm_divMR);
	Element.remove(im_mm_divML);
	
	im_mm_aoi = null;
	im_mm_ghostImg = null;
}

// called when the user clicks in the grayed area
function im_mm_restartAoi(e)
{
	im_mm_deleteAoi();
	im_mm_startAoi(e);
}

// TODO: Clean and optimize!!!
function im_mm_dragULListener(e)
{
	var divBROffset = Position.cumulativeOffset(im_mm_divBR);
	var divULOffset = Position.cumulativeOffset(im_mm_divUL);
	
	var width = divBROffset[0] - divULOffset[0];
	var height = divBROffset[1] - divULOffset[1];
	
	width = (width > 5 ? width : 5);
	height = (height > 5 ? height : 5);
	
	var top = divULOffset[1] + 3;
	if (top > divBROffset[1] - 2) top = divBROffset[1] - 2;
	var left = divULOffset[0] + 3;
	if (left > divBROffset[0] - 2) left = divBROffset[0] - 2;
	
	im_mm_drawAoi(left, top, width, height);
	
	Position.clone($('im_mm_image'), $('im_mm_ghost'));
}

function im_mm_dragBRListener(e)
{
	var divBROffset = Position.cumulativeOffset(im_mm_divBR);
	var divULOffset = Position.cumulativeOffset(im_mm_divUL);
	
	var width = divBROffset[0] - divULOffset[0];
	var height = divBROffset[1] - divULOffset[1];
	
	width = (width > 5 ? width : 5);
	height = (height > 5 ? height : 5);
	
	im_mm_drawAoi(divULOffset[0] + 3, divULOffset[1] + 3, width, height);
	
	Position.clone($('im_mm_image'), $('im_mm_ghost'));
}

function im_mm_dragURListener()
{
	var divUROffset = Position.cumulativeOffset(im_mm_divUR);
	var divBLOffset = Position.cumulativeOffset(im_mm_divBL);
	
	var width = divUROffset[0] - divBLOffset[0];
	var height = divBLOffset[1] - divUROffset[1];
	
	width = (width > 5 ? width : 5);
	height = (height > 5 ? height : 5);
	
	var top = divUROffset[1] + 3;
	if (top > divBLOffset[1] - 2) top = divBLOffset[1] - 2;
	var left = divBLOffset[0] + 3;
	
	im_mm_drawAoi(left, top, width, height);
	
	Position.clone($('im_mm_image'), $('im_mm_ghost'));
}

function im_mm_dragBLListener()
{
	var divUROffset = Position.cumulativeOffset(im_mm_divUR);
	var divBLOffset = Position.cumulativeOffset(im_mm_divBL);
	
	var width = divUROffset[0] - divBLOffset[0];
	var height = divBLOffset[1] - divUROffset[1];
	
	width = (width > 5 ? width : 5);
	height = (height > 5 ? height : 5);
		
	var top = divUROffset[1] + 3;
//	if (top > divBLOffset[1] - 2) top = divBLOffset[1] - 2;
	var left = divBLOffset[0] + 3;
	if (left > divUROffset[0] - 2) left = divUROffset[0] - 2;
	
	im_mm_drawAoi(left, top, width, height);
	
	Position.clone($('im_mm_image'), $('im_mm_ghost'));
}

function im_mm_dragUMListener()
{
	var d = Element.getDimensions(im_mm_aoi);
	
	var height = Position.cumulativeOffset(im_mm_divBM)[1] - Position.cumulativeOffset(im_mm_divUM)[1];
	height = (height > 5 ? height : 5);
	
	var top = Position.cumulativeOffset(im_mm_divUM)[1] + 3;
	if (top > Position.cumulativeOffset(im_mm_divBM)[1] - 2) top = Position.cumulativeOffset(im_mm_divBM)[1] - 2;
	
	im_mm_drawAoi(Position.cumulativeOffset(im_mm_divUL)[0] + 3, top , d.width, height);
	
	Position.clone($('im_mm_image'), $('im_mm_ghost'));
}

function im_mm_dragBMListener()
{
	var d = Element.getDimensions(im_mm_aoi);
	
	var height = Position.cumulativeOffset(im_mm_divBM)[1] - Position.cumulativeOffset(im_mm_divUM)[1];
	height = (height > 5 ? height : 5);
	
	im_mm_drawAoi(Position.cumulativeOffset(im_mm_divUL)[0] + 3, Position.cumulativeOffset(im_mm_divUM)[1] + 3, d.width, height);
	
	Position.clone($('im_mm_image'), $('im_mm_ghost'));
}

function im_mm_dragMLListener()
{
	var d = Element.getDimensions(im_mm_aoi);
	
	var width =  Position.cumulativeOffset(im_mm_divMR)[0] - Position.cumulativeOffset(im_mm_divML)[0];
	width = (width > 5 ? width : 5);
	
	var left = Position.cumulativeOffset(im_mm_divML)[0] + 3;
	if (left > Position.cumulativeOffset(im_mm_divMR)[0] - 2) left = Position.cumulativeOffset(im_mm_divMR)[0] - 2;
	
	im_mm_drawAoi(left, Position.cumulativeOffset(im_mm_divUL)[1] + 3, width, d.height);
	
	Position.clone($('im_mm_image'), $('im_mm_ghost'));
}

function im_mm_dragMRListener()
{
	var d = Element.getDimensions(im_mm_aoi);
	
	var width = Position.cumulativeOffset(im_mm_divMR)[0] - Position.cumulativeOffset(im_mm_divML)[0];
	width = (width > 5 ? width : 5);
	
	im_mm_drawAoi(Position.cumulativeOffset(im_mm_divML)[0] + 3, Position.cumulativeOffset(im_mm_divUL)[1] + 3, width, d.height);
	
	Position.clone($('im_mm_image'), $('im_mm_ghost'));
}

// aoi mousemove event listener
function im_mm_resizeAoi(e)
{
	Event.stop(e); // prevents from dragging the map image (on Explorer)
	
	Position.clone($('im_mm_image'), $('im_mm_ghost'));
	
	// get the current cursor position
	var pX = Event.pointerX(e);
	var pY = Event.pointerY(e);
	
	// set the zoom box position and size
	im_mm_drawAoi (
		Math.min(pX, im_mm_startX),  // left
		Math.min(pY, im_mm_startY),  // top
		Math.abs(pX - im_mm_startX), // width
		Math.abs(pY - im_mm_startY)  // height
	);
	
	Position.clone($('im_mm_image'), $('im_mm_ghost'));
}

// aoi mouseup event listener
function im_mm_stopAoi(e)
{
	// remove listeners and div
	Event.stopObserving(document, 'mousemove', im_mm_resizeAoi);
	Event.stopObserving(document, 'mouseup', im_mm_stopAoi);
	
	// if area is 0 delete aoi
	var d = Element.getDimensions(im_mm_aoi);
	if (d.width == 0 || d.height == 0) {
		im_mm_deleteAoi();
		im_mm_unsetAoi();
/*		$('im_geonetRecords').className = 'im_hidden';*/
	}
	// else set the aoi
	else
		im_mm_handleMouseupListener();
}

// Draws the aoi
function im_mm_drawAoi(left, top, width, height)
{
	im_mm_aoi.style.left = left - mapImgOffsetX + 'px';
	im_mm_aoi.style.top = top - mapImgOffsetY + 'px';
	im_mm_aoi.style.width = width + 'px';
	im_mm_aoi.style.height = height + 'px';
	
	im_mm_drawResizeHandles(left, top, width, height);
}

function im_mm_dragAoi()
{
	Position.clone($('im_mm_image'), $('im_mm_ghost'))
	
	var offset = Position.cumulativeOffset(im_mm_aoi);
	var left = offset[0];
	var top = offset[1];
	
	var d = Element.getDimensions(im_mm_aoi);
	var width = d.width;
	var height = d.height;
	
	im_mm_drawResizeHandles(left, top, width, height);
}

function im_mm_drawResizeHandles(left, top, width, height)
{	
	var a = left - im_mm_mapImgOffsetX - 3;
	var b = top - im_mm_mapImgOffsetY - 3;
	var c = a + width;
	var d = b + height;
	
	im_mm_dotUL.style.left = a + 'px';
	im_mm_dotUL.style.top = b + 'px';
	im_mm_dotUR.style.left = c + 'px';
	im_mm_dotUR.style.top = b + 'px';
	im_mm_dotBL.style.left = a + 'px';
	im_mm_dotBL.style.top = d + 'px';
	im_mm_dotBR.style.left = c + 'px';
	im_mm_dotBR.style.top = d + 'px';
	var t = a + width / 2;
	im_mm_dotUM.style.left = t + 'px';
	im_mm_dotUM.style.top = b + 'px';
	im_mm_dotBM.style.left = im_mm_dotUM.style.left;
	im_mm_dotBM.style.top = d + 'px';
	im_mm_dotML.style.left = a + 'px';
	t = b + height / 2;
	im_mm_dotML.style.top = t + 'px';
	im_mm_dotMR.style.left = c + 'px';
	im_mm_dotMR.style.top = im_mm_dotML.style.top;
}

function im_mm_repositionHandleDivs(left, top, width, height)
{
	var a = left - im_mm_mapImgOffsetX - 3;
	var b = top - im_mm_mapImgOffsetY - 3;
	var c = a + width;
	var d = b + height;
	
	im_mm_divUL.style.left = a + 'px';
	im_mm_divUL.style.top = b + 'px';
	im_mm_divUR.style.left = c + 'px';
	im_mm_divUR.style.top = b + 'px';
	im_mm_divBL.style.left = a + 'px';
	im_mm_divBL.style.top = d + 'px';
	im_mm_divBR.style.left = c + 'px';
	im_mm_divBR.style.top = d + 'px';
	var t = a + width / 2;
	im_mm_divUM.style.left = t + 'px';
	im_mm_divUM.style.top = b + 'px';
	im_mm_divBM.style.left = im_mm_divUM.style.left;
	im_mm_divBM.style.top = d + 'px';
	im_mm_divML.style.left = a + 'px';
	t = b + height / 2;
	im_mm_divML.style.top = t + 'px';
	im_mm_divMR.style.left = c + 'px';
	im_mm_divMR.style.top = im_mm_divML.style.top;
}

function im_mm_setStatus(status)
{
	var refreshButton = $('im_refreshButton');
	
	switch(status)
	{
		case 'busy': // not allowed to refresh - wait for ajax transaction to finish
			// disable zoom, pan...
			
			// if(!$('map')) break; // ETj
			
			Event.stopObserving('im_mm_map', 'mousedown', im_mm_mousedownEventListener);
			Event.observe('im_mm_map', 'mousedown', noOp);
			
			// change cursor
			$('im_mm_map').style.cursor = 'wait'
			
			// change refresh button status
			refreshButton.className = 'im_disabled';
			refreshButton.disabled = true;
			
			$('im_pleaseWait').style.display = 'block';
			break;
		case 'idle': // all operations allowed
			// enable zoom, pan...
			Event.stopObserving('im_mm_map', 'mousedown', noOp);
			Event.observe('im_mm_map', 'mousedown', im_mm_mousedownEventListener);
			
			// change refresh button status
			$('im_mm_map').style.cursor = 'crosshair'
			refreshButton.className = 'im_disabled';
			refreshButton.disabled = true;
			
			$('im_pleaseWait').style.display = 'none';
			break;
		case 'refresh': // refresh buton highlighted - means that refresh is needed after the user made some operations on layers
			// change refresh button status
			refreshButton.className = 'im_refresh';
			refreshButton.disabled = false;
			break;
	}
}
