
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

// performs the operations needed when the user
// has changed the layer order
function layersOrderChanged(newOrder)
{
	imc_setLayersOrder(newOrder);
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
 * EOF
 *****************************************************************************/
