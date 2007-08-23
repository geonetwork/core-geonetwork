/*****************************************************************************
 *
 *                 Functions related to layers handling
 *
 *****************************************************************************/

// Builds the layer list
function im_buildLayerList(req) 
{
           if( ! $('im_layersDiv') ) // map viewer not yet loaded
               return;

	oldid = activeLayerId;
           $('im_layersDiv').innerHTML = req.responseText;           
	activateMapLayer(oldid, true); 
           
           if( ! Prototype.Browser.IE )
	    createSortable();
}
                      
// Makes the layer list sortable
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

// performs the operations needed when the user
// has changed the layer order
function layersOrderChanged(newOrder)
{
	imc_setLayersOrder(newOrder);
}	


/*****************************************************************************
 *
 *                                   Layers
 *
 *****************************************************************************/

function layerDblClickListener(id)
{
	deleteAoi();
	imc_zoomToLayer(id);
}



function setLayerVisibility(req, id) 
{
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

/*****************************************************************************
 *
 *                                   Activation
 *
 *****************************************************************************/

function activateMapLayer(id, keepnew)
{
	var mapLayer = $('layerList_' + id);
	
	disactivateAllMapLayers(keepnew);
	mapLayer.className = 'im_activeLayer';

           $('layerControl_' + id).show();

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
			
			var trList=mapLayer.getElementsByTagName('tr');
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
 *                               Layer position
 *
 *****************************************************************************/

function im_layerMoveDown(id)
{
	var url = '/intermap/srv/en/map.layers.moveDown';	
	var myAjax = new Ajax.Request (
		url,
		{
		    parameters: 'id='+id,
		    method: 'get',
		    onComplete: im_buildLayerList
		}
	);
}

function im_layerMoveUp(id)
{
	var url = '/intermap/srv/en/map.layers.moveUp';	
	var myAjax = new Ajax.Request (
		url, 
		{
		    parameters: 'id='+id,
		    method: 'get',
		    onComplete: im_buildLayerList
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
	var url = '/intermap/srv/en/map.layers.deleteLayer';
	var pars = 'id=' + id ;
	
	var myAjax = new Ajax.Request (
		url, 
		{
			method: 'get',
			parameters: pars,
			onComplete: function(req) 
			{
				im_buildLayerList(req);	
				refreshNeeded();
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
	if (child == null)
		return null;
	else
	{	    
		if (id != activeLayerId)
			return activeLayerId;
		else
		{	
			// choose the layer to activate next
			var nextActiveLayer = child.nextSibling;
			if (nextActiveLayer == null)
				nextActiveLayer = child.previousSibling; // may also be null: ok
			
			if(nextActiveLayer == null)
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
	
/*****************************************************************************
 * EOF
 *****************************************************************************/
