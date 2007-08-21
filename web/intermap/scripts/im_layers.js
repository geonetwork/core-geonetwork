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

	var layers = req.responseXML.getElementsByTagName('layer');
	
	// delete all layers from the list
	var ul = $('im_layersDiv').getElementsByTagName('ul')[0];
	deleteChildNodes(ul);
	
	// add each layer to the list
	for (var i = 0; i < layers.length; i++)
	{
//	alert('Adding layer ' + layers[i].getAttribute('title'));
	           var title = Builder.node('p',{},layers[i].getAttribute('title'));
//		var title = document.createTextNode(layers[i].getAttribute('title'));
                    
		var id = layers[i].getAttribute('id');
		var transp = layers[i].getAttribute('transparency');
		
		var addup = i>0;
		var adddown = (i<layers.length-1)&&(layers.length>1);

//zalert('Appending layer ' + layers[i].getAttribute('title'));		
		appendToLayerList(ul, title, id, transp,addup, adddown);
	}
	
	createSortable();
	
	// activate the first layer if none active
	if (!activeLayerId)
		activateMapLayer(layers[0].getAttribute('id'));
		
	forceIErefresh();
}
	

function forceIErefresh()
{
            // Force IE to refresh the page (layers are not properly displayed without this workaround
	var dummy = Builder.node('div');
	document.appendChild(dummy);
	document.removeChild(dummy);	
}



// Appends a single layer to the layer list
function appendToLayerList(list, title, id, transp, addup, adddown) // layer in a TABLEs layout
{
            var li = Builder.node('li', {id: 'layerList_' + id});
            
            var toggleLayer = Builder.node('img' ,
            {
                id: 'visibility_' + id,
                className: 'im_layerControl',
                src: '/intermap/images/showLayer.png',
                title:'Toggle layer visibility' // FIXME: i18n this string
            });
            
            var delLayer = Builder.node('img', 
            {
                id: 'deleteLayer_' + id,
                className: 'im_layerButton',
                src: '/intermap/images/deleteLayer.png', 
                title: 'Remove layer' // FIXME: i18n this string
            });
            
            var showLayerMD = Builder.node('img', 
            {
                id: 'showLayerMD_' + id,
                className: 'im_layerButton',
                src: '/intermap/images/info.png',
                title: 'Show metadata' // FIXME: i18n this string
            });
            
            var legend = Builder.node('img', 
            {
                id: 'legend_' + id,
                className: 'im_layerButton',
                src: '/intermap/images/legend.png',
                title: 'Show legend' // FIXME: i18n this string
            });

/*            var trans = Builder.node('select',
            {
                id: "im_transp_"+id,
                className: "layerSelectTransp"
            });
*/            
            var trans = document.createElement('select');
            trans.setAttribute("id", "im_transp_"+id);
            trans.setAttribute("className", "layerSelectTransp");
                        
            for (var t = 100; t >= 0; t-=10) {
               var opt = document.createElement('option');
               opt.setAttribute("value", t);
               if(transp==t)
               {
                   //alert ("Transp for " + title + " is set at " + t); 
                   opt.setAttribute('selected', 'true');
               }
               var label = t+"%";
               if(t==100)
                   label = "opaque"; // FIXME i18n
                   
               opt.innerHTML=label;
               trans.appendChild(opt);
           }

            // do layout
            list.appendChild(li);
            
	var table = Builder.node('table');
           li.appendChild(table);
           
           var tbody = Builder.node('tbody');
           table.appendChild(tbody);
	
	var trmainicons = Builder.node('tr');
	tbody.appendChild(trmainicons);
	
	var tdmainicons = Builder.node('td', 
	{
	    rowspan: "2",
	    height: "35px",
               className: 'im_layerControl'
           });
               
	trmainicons.appendChild(tdmainicons);

	tdmainicons.appendChild(toggleLayer);

	if(addup)
	{
                var upbtn = Builder.node('img',
                {
                    id: 'im_layerUp_'+id,
                    className: 'im_layerControl',
                    //style: 'position:absolute; bottom:3px;',
                    src: '/intermap/images/im_moveup.gif',
                    title: 'Move layer up' // FIXME: i18n this string                    
                });                    
                tdmainicons.appendChild(upbtn);                
	}

	if(adddown)
	{
                var downbtn = Builder.node('img',
                {
                    id: 'im_layerDown_'+id,
                    className: 'im_layerControl',                
                    src: '/intermap/images/im_movedown.gif',
                    title: 'Move layer down' // FIXME: i18n this string
                });
                
                tdmainicons.appendChild(downbtn);                
	}
	
	var tdtitle = Builder.node('td');
	trmainicons.appendChild(tdtitle);
	tdtitle.appendChild(title);
	
	var trcontrols = Builder.node('tr', {id: 'layerControl_' + id});
/*	trcontrols.style.display = "none";	*/
	tbody.appendChild(trcontrols);
	
           var tdcontrols = Builder.node('td', { /*colspan:"2" */});
	trcontrols.appendChild(tdcontrols);

/*	tdcontrols.appendChild(toggleLayer);*/
	tdcontrols.appendChild(delLayer);
	tdcontrols.appendChild(legend);
	tdcontrols.appendChild(showLayerMD);
           tdcontrols.appendChild(trans);
            
           //ker.wrap(this, function() {
               //$(trcontrols).hide();
           //} )();

//alert("observers");	
	// add event observers (mousedown to select layer and dblclick to zoom to layer)
	Event.observe('layerList_' + id, 'mousedown', function(e){ activateMapLayer(id); });
//	Event.observe('layerList_' + id, 'dblclick', function(e){ openInspector(id); });
	Event.observe('layerList_' + id, 'dblclick', function(e){ layerDblClickListener(id, e); });
	Event.observe('visibility_' + id, 'click', function(e){ visibilityButtonListener(id, e); });
	Event.observe('legend_' + id, 'click', function(e){ showLegend(id); });
	Event.observe('deleteLayer_' + id, 'click', function(e){ im_deleteLayer(id); });
	Event.observe('im_transp_' + id, 'change', function(e){ im_layerTransparencyChanged(id); });  
// TODO	Event.observe('showLayerMD_' + id, 'click', function(e){  });
           $(trcontrols).hide();
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

           //$('layerControl_' + id).style.display = "table-row";
           $('layerControl_' + id).show();

	activeLayerId = id;
	
	//updateInspector(id);
}


function updateInspectorControls(req)
{
	// transparency slider
//	var offsetX = Position.cumulativeOffset($('transparencySlider'))[0];
//	var transparency = parseFloat(req.responseXML.getElementsByTagName('transparency')[0].firstChild.nodeValue);
//	$('im_transparencyHandle').style.left = Math.round(transparency / 100 * 95) + 'px'; // handler width is 5px
	
	// transparency value
//	$('im_transparencyValue').innerHTML = transparency;
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
			
			var trList=mapLayer.getElementsByTagName('tr');
			$A(trList).each(
	                              function(tr)
			       {
            			if( new String(tr.id).search('layerControl_') != -1)
            			   tr.hide();
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
	// won't remove last layer
            var llist = $('im_layerList');
            var nodes=llist.getElementsByTagName('li');
            if( $A(nodes).length == 1)
            {
                alert("Can't remove last layer"); // fixme i18n!
                return;
            }
            
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
