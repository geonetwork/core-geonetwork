var autoRefresh = true;

var startOffsetX;
var startOffsetY;

// Didn't find a way to do it with prototype
function getWindowSize()
{
	var width = 0, weight = 0;
	if(typeof(window.innerWidth) == 'number')
	{
		// non IE
		width = window.innerWidth;
		weight = window.innerHeight;
	} 
	else if (document.documentElement && (document.documentElement.clientWidth || document.documentElement.clientHeight))
	{
		// IE 6+ in 'standards compliant mode'
		width = document.documentElement.clientWidth;
		weight = document.documentElement.clientHeight;
	}
	else if (document.body && (document.body.clientWidth || document.body.clientHeight))
	{
		// IE 4 compatible
		width = document.body.clientWidth;
		weight = document.body.clientHeight;
	}
	
	return [width, weight];
}

/*function resizeLayersDiv(handler)
{
	var x = Position.cumulativeOffset($('im_resizeBar'))[0];
	
	if (x >= minLayersDivWidth)
	{
		setLayersDivWidth(x);
	}
	else
	{
		setLayersDivWidth(minLayersDivWidth);
	}
}

function setLayersDivWidth(x)
{
	$('im_layerList').style.width = x + 'px';
	$('im_layers').style.width = x + 'px';
	$('im_layersToolbar').style.width = x + 'px';
	$('im_inspector').style.width = x + 'px';
	$('im_resizeBar').style.left = x + 'px'; // not really clean (operates on the draggable itself) but ok for now
	var t = x + 4 + 5 // 4 is the resize bar width... TODO: use functions
	$('im_map').style.left = t + 'px';
}
*/
var currentTool;
function setTool(tool) {
	deleteAoi();
	currentTool = tool;
	$('intermap_root').className = tool; //document.body.className = tool;
	if (tool != 'aoi') $('im_geonetRecords').className = 'im_hidden';
}

// Opens the print window
function print() {
	window.open('{/root/gui/locService}/map.getPrintImage','print', width='620', height='620');
}



// Builds the layer list
function im_buildLayerList(req) 
{
	var layers = req.responseXML.getElementsByTagName('layer');
	
	// delete all layers from the list
	var ul = $('im_layersDiv').getElementsByTagName('ul')[0];
	deleteChildNodes(ul);
	
	// add each layer to the list
	for (var i = 0; i < layers.length; i++)
	{
		var title = document.createTextNode(layers[i].getAttribute('title'));
		var id = layers[i].getAttribute('id');
		var transp = layers[i].getAttribute('transparency');
		
		var addup = i>0;
		var adddown = (i<layers.length-1)&&(layers.length>1);
		
		appendToLayerList(ul, title, id, transp,addup, adddown);
	}
	
	// activate the first layer if none active
	if (!activeLayerId)
		activateMapLayer(layers[0].getAttribute('id'));
}

// Appends a single layer to the layer list
function appendToLayerList(list, title, id, transp, addup, adddown) // layer in a TABLEs layout
{
            var li = document.createElement('li');
            li.setAttribute('id', 'layerList_' + id);
            
            var toggleLayer = document.createElement('img');
            toggleLayer.id= 'layerButtonToggle';
            toggleLayer.className = 'im_layerControl';
            toggleLayer.setAttribute('src','/intermap/images/showLayer.png');
            toggleLayer.setAttribute('title','Toggle layer visibility'); // FIXME: i18n this string
            toggleLayer.setAttribute('id','visibility_' + id);
            
            var delLayer = document.createElement('img');
            delLayer.id = 'layerButtonDelete';
            delLayer.className = 'im_layerButton';
            delLayer.setAttribute('src','/intermap/images/deleteLayer.png'); 
            delLayer.setAttribute('title','Remove layer'); // FIXME: i18n this string
            delLayer.setAttribute('id','deleteLayer_' + id);
            
            var showLayerMD = document.createElement('img');
            showLayerMD.id = 'layerButtonShowMD';
            showLayerMD.className = 'im_layerButton';
            showLayerMD.setAttribute('src','/intermap/images/info.png');
            showLayerMD.setAttribute('title','Show metadata'); // FIXME: i18n this string
            showLayerMD.setAttribute('id','showLayerMD_' + id);
            
            var legend = document.createElement('img');
            legend.id = 'layerLegend';
            legend.className = 'im_layerButton';
            legend.setAttribute('src','/intermap/images/legend.png');
            legend.setAttribute('title','Show legend'); // FIXME: i18n this string
            legend.setAttribute('id','legend_' + id);
            
            var trans = document.createElement('select');
            trans.className = "layerSelectTransp";
            trans.id="im_transp_"+id;
            
            var opt = document.createElement('OPTION');
            opt.setAttribute("value", 100);
            opt.innerHTML="opaque";
            trans.appendChild(opt);

            for (var t = 90; t >= 0; t-=10) {
               var opt = document.createElement('OPTION');
               opt.setAttribute("value", t);
               if(transp==t)
                   opt.setAttribute("selected", 'true');               
               opt.innerHTML=t+"%";
               trans.appendChild(opt);
           }

            // do layout
            list.appendChild(li);
            
	var table = document.createElement('table');
           li.appendChild(table);
	
	var trmainicons = document.createElement('tr');
	table.appendChild(trmainicons);
	var tdmainicons = document.createElement('td');
	tdmainicons.setAttribute("rowspan","2");
	tdmainicons.setAttribute("height","35px");
           tdmainicons.className = 'im_layerControl';                
	trmainicons.appendChild(tdmainicons);

	tdmainicons.appendChild(toggleLayer);


	if(addup)
	{
                var upbtn = document.createElement('img');
                upbtn.id = 'im_layerControlUp';
                upbtn.className = 'im_layerControl';
                upbtn.setAttribute('src','/intermap/images/im_moveup.gif');
                upbtn.setAttribute('title','Move layer up'); // FIXME: i18n this string
                //upbtn.setAttribute('id','showLayerMD_' + id);
                tdmainicons.appendChild(upbtn);                
	}

	if(adddown)
	{
                var downbtn = document.createElement('img');
                downbtn.id = 'im_layerControlDown';
                downbtn.className = 'im_layerControl';                
                downbtn.setAttribute('src','/intermap/images/im_movedown.gif');
                downbtn.setAttribute('title','Move layer down'); // FIXME: i18n this string
                //upbtn.setAttribute('id','showLayerMD_' + id);
                tdmainicons.appendChild(downbtn);                
	}
	
	
/*	var trtitle = document.createElement('tr');
	table.appendChild(trtitle);
*/	var tdtitle = document.createElement('td');
/*	trtitle.appendChild(tdtitle);*/
	trmainicons.appendChild(tdtitle);
	
	tdtitle.appendChild(title);
	

	var trcontrols = document.createElement('tr');
	trcontrols.id = 'layerControl_' + id;
/*	trcontrols.style.display = "none";	*/
           trcontrols.hide();
	table.appendChild(trcontrols);
	
           var tdcontrols = document.createElement('td');
	trcontrols.appendChild(tdcontrols);

/*	tdcontrols.appendChild(toggleLayer);*/
	tdcontrols.appendChild(delLayer);
	tdcontrols.appendChild(legend);
	tdcontrols.appendChild(showLayerMD);
           tdcontrols.appendChild(trans);

	
	// add event observers (mousedown to select layer and dblclick to zoom to layer)
	Event.observe('layerList_' + id, 'mousedown', function(e){ activateMapLayer(id); });
//	Event.observe('layerList_' + id, 'dblclick', function(e){ openInspector(id); });
	Event.observe('layerList_' + id, 'dblclick', function(e){ layerDblClickListener(id, e); });
	Event.observe('visibility_' + id, 'click', function(e){ visibilityButtonListener(id, e); });
	Event.observe('legend_' + id, 'click', function(e){ showLegend(id); });
	Event.observe('deleteLayer_' + id, 'click', function(e){ im_deleteLayer(id); });
	Event.observe('im_transp_' + id, 'change', function(e){ im_layerTransparencyChanged(id); });  
// TODO	Event.observe('showLayerMD_' + id, 'click', function(e){  });
	
	createSortable();
}


// Makes the layer list sortable
function createSortable()
{
	Sortable.create (
		'im_layerList',
		{
			dropOnEmpty:true,containment:['im_layerList'],constraint:false,
			onUpdate:function(){ layersOrderChanged(Sortable.serialize('im_layerList')); }
		}
	);
}

// Opens the inspector
function openInspector(id)
{
	Element.show('im_inspector');
}

// Closes the inspector
function closeInspector()
{
	Element.hide('im_inspector');
}


/*****************************************************************************
 *
 *                                   Layers
 *
 *****************************************************************************/

function layerDblClickListener(id, e)
{
	deleteAoi();
	imc_zoomToLayer(id);
}

function visibilityButtonListener(id, e)
{
	toggleVisibility(id);
}

function setLayerVisibility(req, id) {
	// get visibility value from response
	var visibility = req.responseXML.getElementsByTagName('visible')[0].firstChild.nodeValue;	
	
	// get the image element and set the source according to visibility value
	var img = $('visibility_' + id);
	if (visibility == 'true')
		img.src = '/intermap/images/showLayer.png';
	else
		img.src = '/intermap/images/hideLayer.png';
	
	refreshNeeded();
}

function activateMapLayer(id)
{
	var mapLayer = $('layerList_' + id);
	
	disactivateAllMapLayers();
	mapLayer.className = 'im_activeLayer';

           $('layerControl_' + id).style.display = "table-row";

	activeLayerId = id;
	
	updateInspector(id);
}


function updateInspectorControls(req)
{
	// transparency slider
//	var offsetX = Position.cumulativeOffset($('transparencySlider'))[0];
	var transparency = parseFloat(req.responseXML.getElementsByTagName('transparency')[0].firstChild.nodeValue);
	$('im_transparencyHandle').style.left = Math.round(transparency / 100 * 95) + 'px'; // handler width is 5px
	
	// transparency value
	$('im_transparencyValue').innerHTML = transparency;
}

function disactivateAllMapLayers()
{
//	var ul = $('layerList');
	var li = $('im_layersDiv').getElementsByTagName('li');
	var layers = $A(li);
	layers.each ( 
		function(mapLayer)
		{
			mapLayer.className = 'im_inactiveLayer';
			
			var tr=mapLayer.getElementsByTagName('tr');
			$A(tr).each(
	                              function(tr)
			       {
            			if( new String(tr.id).search('layerControl_') != -1)
            			   tr.style.display="none";
            	                  }
            	           );
		}
	);
	 
}

// performs the operations needed when the user
// has changed the layer order
function layersOrderChanged(newOrder)
{
	imc_setLayersOrder(newOrder);
}	


/*****************************************************************************
 *
 *                               Delete layer
 *
 *****************************************************************************/

function deleteButtonListener()
{
	if (activeLayerId != null)
	{
		imc_deleteLayer(activeLayerId);
		deleteLayerFromList(activeLayerId);
		//activeLayerId = null;
	}
}

function im_deleteLayer(id)
{
	imc_deleteLayer(id);
	deleteLayerFromList(id);
}


function deleteLayerFromList(id)
{
	var ul = $('im_layersDiv').getElementsByTagName('ul')[0];
	
	var child = $('layerList_' + id);
	
	if (child != null)
	{	    
    	    if (id==activeLayerId)
    	    {	
		// choose the layer to activate next
		var nextActiveLayer;
		if (child.nextSibling != null)
			nextActiveLayer = (child.nextSibling)
		else if (child.previousSibling != null)
			nextActiveLayer = (child.previousSibling)
		else nextActiveLayer = null;
		
		// activate the layer
		if (nextActiveLayer != null)
		{	
			var t = nextActiveLayer.getAttribute('id');
			var nextActiveLayerId = t.substr(t.indexOf('_') + 1);
			activateMapLayer(nextActiveLayerId);
		}
                }		
		
                ul.removeChild(child);		
	}
}


/*****************************************************************************
 *
 *                                 Add layer
 *
 *****************************************************************************/

function addButtonListener()
{
	setStatus('idle');
	$('addLayers').className = 'im_visible';
	setAddLayersWindowContent();
}


/*****************************************************************************
 *
 *                             Layer transparency
 *
 *****************************************************************************/

function transparencySliderMoved(transparency)
{
        if (activeLayerId != null)
        {
            imc_setLayerTransparency(activeLayerId, transparency);
        }
}

	
function im_layerTransparencyChanged(id)
{
        var transp = $('im_transp_' + id).value;
        imc_setLayerTransparency(id, transp);
}
	
/*****************************************************************************
 *
 *                      Map operations (zoom, pan, identify)
 *
 *****************************************************************************/

var startX, startY; // start (mousedown) coordinates

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
	
	var url = '/intermap/srv/'+Env.lang+'/map.identify';
	var t1 = pointerX - offsetX;
	var t2 = pointerY - offsetY;
	var pars = 'mapimgx=' + t1 + '&mapimgy=' + t2 + '&activeLayer=' + activeLayerId + "&format=" + "text%2Fhtml";
	
	window.open(url + '?' + pars, "Query result", "width=600,height=400,scrollbars=yes,toolbar=no,status=yes,menubar=no,location=yes,resizable=yes");
}

// ZOOM
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
	
	setStatus('busy');
	
	imc_mapAction(currentTool,
		Math.min(pX, startX) - offsetX, // xmin
		Math.max(pY, startY) - offsetY, // ymax
		Math.max(pX, startX) - offsetX, // xmax
		Math.min(pY, startY) - offsetY  // ymin
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

// DRAG
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
	
	move(startOffsetX - offsetX, offsetY - startOffsetY)
	
	Event.stopObserving(document, 'mousemove', dragImage);
	Event.stopObserving(document, 'mouseup', stopDrag);
}

function move(deltax, deltay)
{
	var url = '/intermap/srv/'+Env.lang+'/map.move';
	var pars = 'deltax=' + deltax + '&deltay=' + deltay;
	
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

// updates the map images
function updateMapImage(req)
{
	var vMapImg = $('im_mapImg');
	var vGhostImg = $('im_ghostImg');
	
	// get the new values from response XML
	var imageSrc = req.responseXML.getElementsByTagName('imgUrl')[0].firstChild.nodeValue;
	var scale = req.responseXML.getElementsByTagName('scale')[0].firstChild.nodeValue;
	
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

	// update the scale text
	deleteChildNodes($('im_scale'));
	$('im_scale').appendChild( document.createTextNode('1:' + scale));
	
//	Event.observe(vMapImg, 'load', function(e) { setStatus('idle') }); // better behaviour but needs debugging on explorer (newer version of prototype?)
	setStatus('idle');
}


// AOI (Area Of Interest)
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
	
	deleteAoi();
	unsetAoi();
		
	setStatus('busy');
	
	imc_mapAction('zoomin',
		left - offsetX,
		top - offsetY + height,
		left - offsetX + width,
		top - offsetY
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
		refreshButtonListener();
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
			
			$('im_pleaseWait').style.display = 'block';
			break;
		case 'idle': // all operations allowed
			// enable zoom, pan...
			Event.stopObserving('im_map', 'mousedown', noOp);
			Event.observe('im_map', 'mousedown', mousedownEventListener);
			
			// change refresh button status
			$('im_map').style.cursor = 'crosshair'
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

// Just prevents the user to drag the image while trying to zoom
function noOp(e)
{
	Event.stop(e); // prevents from dragging the map image (on Firefox)
}













function presentGeonetResponse(req)
{
    if( ! $('im_geonetRecords')) // ETj
        return;

	var geonetDiv = $('im_geonetRecords');
	// delete all layers from the list
	deleteChildNodes(geonetDiv);
	
	geonetDiv.className = '';
	
	
	// total number of records
	var summary = req.responseXML.getElementsByTagName('summary');
	var count = summary[0].getAttribute('count');
	
	// actual range
	var from = summary[0].getAttribute('from');	
	var to = summary[0].getAttribute('to');
	
	newFrom = from + 1;
	newTo = (parseInt(from) / 10 + 1) * 10;
	
	geonetDiv.appendChild(document.createTextNode(count + ' records found'));
	geonetDiv.appendChild(document.createElement('br'));
	geonetDiv.appendChild(document.createElement('br'));
	
	var metadata = req.responseXML.getElementsByTagName('metadata');
	
	// add each record to the list
	for (var i = 0; i < metadata.length; i++)
	{
		var title = document.createTextNode(' - ' + metadata[i].getAttribute('title'));
		//var id = metadata[i].getAttribute('id');
		geonetDiv.appendChild(title);
		geonetDiv.appendChild(document.createElement('br'));
		
		// append wms services
		var services = metadata[i].getElementsByTagName('service');
		var ul;
		if (services.length > 0) {
			ul = document.createElement('ul');
			var li;
			for (var j = 0; j < services.length; j++)
			{
				li = document.createElement('li');
				
				var service = services[j];
				
				var a = document.createElement('a');
				var serviceTitle = services[j].getElementsByTagName('title')[0].firstChild.nodeValue;
				var baseUrl = service.getElementsByTagName('baseUrl')[0].firstChild.nodeValue;
				var serviceName = service.getElementsByTagName('name')[0].firstChild.nodeValue;
				
				
				//a.setAttribute('onClick', 'map.layers.add?url=' + baseUrl + '&service=' + serviceName);
				a.setAttribute('onClick', 'javascript:addLayer(\'' + baseUrl + '\', \'' + serviceName+ '\')');
				
				geonetDiv.appendChild(document.createElement('br'));
				a.appendChild(document.createTextNode(serviceTitle));
				li.appendChild(a);
				ul.appendChild(li);
			}
			geonetDiv.appendChild(ul);
		}	
	}
	
	var a = document.createElement('a');
	a.setAttribute('onClick', 'javascript:alert(\'next\')'); // DEBUG
	a.appendChild(document.createTextNode('next >>'));
	geonetDiv.appendChild(a);
}

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
