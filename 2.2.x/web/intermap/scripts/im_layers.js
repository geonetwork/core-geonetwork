/*****************************************************************************
 *
 *                 Functions related to layers handling
 *
 * Requires:
 *    im_extra_afterLayerUpdated()
 *    getIMServiceURL(servicename)
 *    im_extra_afterWmcSet(resp) -- this function's name has to be changed
 *    im_extra_drivingMap
 *****************************************************************************/

var im_layer_width = 176;
//var minLayersDivWidth = 176;


var activeLayerId = null; // active layer

function imc_reloadLayers()
{
	var myAjax = new Ajax.Request (
		getIMServiceURL('map.getLayers.embedded'), 
		{
			method: 'get',
			onComplete: im_buildLayerList
		}
	);
}

/** Builds the layer list */
function im_buildLayerList(req) 
{
	if(im_checkError(req))
	{
		im_showError(req);
		return;
	}
	
	if( ! $('im_layersDiv') ) // map viewer not yet loaded
		return;
	
	$('im_layersDiv').innerHTML = req.responseText;

	if( ! $('layerList_'+activeLayerId)) // catches 1) activeLayerId null; 2) layerid totally changed (for instance after a map reset) 
		activeLayerId = im_getFirstLayerId();		

	activateMapLayer(activeLayerId, true); 
 
	if($('im_layerList'))
	{
		if( ! Prototype.Browser.IE )
			createSortable();
	} 	          
}
                      
           
/** Makes the layer list sortable */
function createSortable()
{
	Sortable.create (
		$('im_layerList'),
		{
			dropOnEmpty:true,containment:['im_layerList'],constraint:false,
			onUpdate:function(){ layersOrderChanged(Sortable.serialize('im_layerList')); }
		}
	);
}


/*****************************************************************************
 *
 *                    Double click
 *
 *****************************************************************************/

function layerDblClickListener(id)
{
	imc_zoomToLayer(id);
}

// TODO: Should we move this functionality into buttons in each layer's tab?
// TODO: what about the AOI?  (read also into im_extra_afterWmcSet)
function imc_zoomToLayer(layerId)
{
	im_extra_drivingMap.setStatus('busy');
	var pars = 'id=' + layerId + 
				'&width='  + im_extra_drivingMap.width +
				'&height=' + im_extra_drivingMap.height;
	
	var myAjax = new Ajax.Request (
		getIMServiceURL('map.zoomToService'),
		{
			method: 'get',
			parameters: pars,
			onComplete: function(req) 
			{ 
				var resp = req.responseXML;
				im_extra_afterWmcSet(resp); // function's name has to be changed 
				im_extra_drivingMap.setStatus('idle');
			}, 
			onFailure: reportError
		}
	);
}



/*****************************************************************************
 *
 *                      Visibility toggling
 *
 *****************************************************************************/

function toggleVisibility(id) {
	var pars = 'id=' + id;
	
	var myAjax = new Ajax.Request (
		getIMServiceURL('map.layers.toggleVisibility'), 
		{
			method: 'get',
			parameters: pars,
			onComplete: function(request) { setLayerVisibility(request, id); },
			onFailure: reportError
		}
	);
}

function setLayerVisibility(req, id) 
{
	// get visibility value from response
	var visibility = req.responseXML.getElementsByTagName('visible')[0].firstChild.nodeValue;	
	
	// get the image element and set the source according to visibility value
	var img = $('visibility_' + id);
	if (visibility == 'true')
		img.src = '/intermap/images/showLayer.png'; // FIXME
	else
		img.src = '/intermap/images/hideLayer.png'; // FIXME
	
	im_extra_afterLayerUpdated();	
}

/*****************************************************************************
 *
 *                                   Activation
 *
 *****************************************************************************/

function activateMapLayer(id, keepnew)
{
	disactivateAllMapLayers(keepnew);

	if(id)
	{
		var mapLayer = $('layerList_' + id);	
		mapLayer.className = 'im_activeLayer';
		
		$('layerControl_' + id).show();
	}
	
	activeLayerId = id;
}

function disactivateAllMapLayers(keepnew)
{
	var li = $('im_layersDiv').getElementsByTagName('li');
	var layers = $A(li);
	layers.each ( 
		function(mapLayer)
		{
			if( ! (keepnew && mapLayer.className ==  "im_newLayer") )
			{
				mapLayer.className = 'im_inactiveLayer';
			}
			
			var trList = mapLayer.getElementsByTagName('tr');
			$A(trList).each(
					function(tr)
					{
						if( new String(tr.id).search('layerControl_') != -1)
						$(tr).hide();
					}
			);
		}
	);	 
}

/*****************************************************************************
 *
 *                               Layer ordering
 *
 *****************************************************************************/

function im_layerMoveDown(id)
{
	var myAjax = new Ajax.Request (
		getIMServiceURL('map.layers.moveDown'),
		{
		    parameters: 'id='+id,
		    method: 'get',
			onComplete: function(req) 
			{
			    im_buildLayerList(req);	
				im_extra_afterLayerUpdated();
			}
		}
	);
}

function im_layerMoveUp(id)
{
	var myAjax = new Ajax.Request (
		getIMServiceURL('map.layers.moveUp'), 
		{
		    parameters: 'id='+id,
		    method: 'get',
			onComplete: function(req) 
			{
			    im_buildLayerList(req);		
				im_extra_afterLayerUpdated();
			}
		}
	);
}

// performs the operations needed when the user
// has changed the layer order via drag'n'drop
function layersOrderChanged(order)
{
	var pars = order.replace(new RegExp("\\[\\]", "g"), ""); // remove all [ and ] - jeeves doesn't accept in parameter name otherwise
	
	var myAjax = new Ajax.Request (
		getIMServiceURL('map.layers.setOrder'), 
		{
			method: 'get',
			parameters: pars,
			onComplete: function(req) 
			{
			    im_buildLayerList(req);			
				im_extra_afterLayerUpdated();  
			},
			onFailure: reportError
		}
	);
}

/*****************************************************************************
 *
 *                               Delete layer
 *
 *****************************************************************************/

function im_deleteLayer(id)
{
	// won't remove last layer
	var llist = $('im_layerList');
	var nodes=llist.getElementsByTagName('li');
	if( $A(nodes).length == 1)
	{
		// this should not happen, so never mind i18n here
		alert("Can't remove last layer");
		return;
	}
	
	// activate next available layer
	var nextid= im_getNextActivableLayer(id);
	if(nextid)
		activateMapLayer(nextid);	

	// delete it!
	imc_deleteLayer(id);	
	//deleteLayerFromList(id);
}

// start ajax transaction to delete a layer
function imc_deleteLayer(id)
{
	var pars = 'id=' + id ;
	
	var myAjax = new Ajax.Request (
		getIMServiceURL('map.layers.deleteLayer'), 
		{
			method: 'get',
			parameters: pars,
			onComplete: function(req) 
			{
				im_buildLayerList(req);
				im_extra_afterLayerUpdated();
			},
			onFailure: reportError
		}
	);
}

/*
## If layer id has to be removed, this function computes next layer to be activated.
*/
function im_getNextActivableLayer(id)
{
	
	var child = $('layerList_' + id);	
	if (child === null)
		return null;
	else
	{	    
		if (id != activeLayerId)
			return activeLayerId;
		else
		{	
			// choose the layer to activate next
			var nextActiveLayer = child.nextSibling;
			if (nextActiveLayer === null)
				nextActiveLayer = child.previousSibling; // may also be null: ok
			
			if(nextActiveLayer === null)
				return null;
			else
			{	
				var t = nextActiveLayer.getAttribute('id');
				var nextActiveLayerId = t.substr(t.indexOf('_') + 1);
				return nextActiveLayerId;				
			}
		}
	}
}

function im_getFirstLayerId()
{
	var ul= $('im_layerList');
	
	if( ! ul ) // error in loading layers
	{
		return null;		
	}
	
	var li1 = ul.getElementsByTagName("li")[0];
	
	if (li1 === null)
		return null;
	else
	{	    
		var t = li1.getAttribute('id');
		return t.substr(t.indexOf('_') + 1);						
	}
}


/*****************************************************************************
 *
 *                             Layer transparency
 *
 *****************************************************************************/
	
function im_layerTransparencyChanged(id)
{
        var transp = $('im_transp_' + id).value;
        imc_setLayerTransparency(id, transp);
}

function imc_setLayerTransparency(id, transparency)
{
	var pars = 'id=' + id + '&transparency=' + transparency / 100.0;
	
	var myAjax = new Ajax.Request (
		getIMServiceURL('map.layers.setTransparency'), 
		{
			method: 'get',
			parameters: pars,
			onComplete: im_extra_afterLayerUpdated,  //FIXME
			onFailure: reportError
		}
	);
}



//===================================================================
//   LEGEND POPUP
//===================================================================

// TODO can we remove this function?
function showActiveLayerLegend(id) {
    showLegend(activeLayerId);
}

function hideLegend()
{
    var div=$('im_legendPopup');
    if(div)
    {
        Event.stopObserving(div, 'click', hideLegend);
        document.body.removeChild(div);
    }
}

function showLegend(url, btn) {
//	window.open('/intermap/srv/'+Env.lang+'/map.service.getLegend?id=' + id, 'dialog', 'HEIGHT=300,WIDTH=400,scrollbars=yes,toolbar=yes,status=yes,menubar=yes,location=yes,resizable=yes');
    hideLegend();

    var div = document.createElement("div");
    div.id="im_legendPopup";
    div.style.position = "absolute";
    document.body.appendChild(div);    

    var offset = Position.cumulativeOffset($(btn));
    var x = offset[0];
    var y = offset[1];

    div.style.left=x+"px";
    div.style.top=y+"px";
    div.style.width="100px";
    div.style.height="50px";
//    div.style.border="solid black 1px";
//    div.style.background-color="064377";
    
    var img = document.createElement("img");
//    img.src = "intermap/images/waiting.gif";
    img.src = url;
    img.alt = "Loading legend..."; // fixme i18n
    img.style.position = "absolute";
    img.style.border="solid black 1px";
    div.appendChild(img);    

    Event.observe(div, 'click', hideLegend);	
}
	
/*****************************************************************************
 * EOF
 *****************************************************************************/
