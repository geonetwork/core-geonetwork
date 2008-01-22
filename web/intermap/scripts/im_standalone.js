//===================================================================
//
//  standalone.js
//
// This file contains the initialization, callback and eventhandler functions 
// to make a bigmap live on its own.
//
// Requires:
//		im_bm as an Intermap object
//		imc_addService()
//		imc_setContextFromURL()
//		imc_reloadLayers()
//
//
//===================================================================

//===================================================================
//
// InterMap Entry Points
//
// External scripts can call the imep_* functions to update
// the intermap standalone map.  
//
//===================================================================

/**
 * Loads a layer into the map.
 * The map image and the layerlist will be updated.
 * 
 * @param {String} url: the WMS server URL.
 * @param {String} layer: the name of the layer as reported by the WMS server.
 * @param {boolean} doClearContext: set it to true if the existing context has to be cleared. 
 */
function imep_loadLayer(url, layer, doClearContext)
{
	im_bm.setStatus('busy');
    imc_addService(url, layer, 2, doClearContext,
					function(req) 
					{
			             im_buildLayerList(req);
						 
						 if(im_extra_afterLayerUpdated)
						 	im_extra_afterLayerUpdated();
			        });		
}

/**
 * Set a new bounding box for the map.
 * The map image will be updated.
 * 
 * @param {Float} n: north latitude
 * @param {Float} e: east longitude
 * @param {Float} s: south latitude
 * @param {Float} w: west longitude
 */
function imep_setBBox(n,e,s,w)
{
	im_bm.setBBox(n,e,s,w);
	im_bm.rebuild();		
}

/**
 * Loads a new WMC context into the map.
 * The map image and the layerlist will be updated.
 * 
 * @param {String} url: the WMC URL.
 * @param {boolean} doClearContext: set it to true if the existing context has to be cleared. 
 */
function imep_loadWmcFromUrl(url, doClearContext)
{
	im_bm.setStatus('busy');
	imc_setContextFromURL(url, doClearContext,
							function(req)
							{
								if(im_checkError(req))
								{
									im_bm.setStatus('idle');
									im_showError(req);
									return;
								}

 			                     imc_reloadLayers();
								 
			 					var xml = req.responseXML;
								 
								 if(im_extra_afterWmcSet)
								 	im_extra_afterWmcSet(xml);
		
								im_bm.setStatus('idle');
							}
	);	
	
}


//===================================================================
// DEBUG
//
// Next lines will popup an alert window 
// whenever a prototype error happens.
//===================================================================

Ajax.Responders.register({
  onException: function(req, e){
	var qqq = document.createElement('div');
	qqq.innerHTML = "Exception '" + e.message + "'";
	document.body.appendChild(qqq);
	
	alert("Exception: " + e.message + 
		  "\nFile "+e.fileName+ 
		  "\nLine " +e.lineNumber+
		  "\nStack " +e.stack);
  }
});

//===================================================================
// BOOT
// 
// Functions related to the map bootstrap
//===================================================================

/**
 * This function is called by the onload property in the Intermap frame 
 */
function im_boot()
{	
	setTool("zoomin");

	var size = getWindowSize();
	//alert("W:"+size[0]+" H:"+size[1]);	
	im_bm.setSize( size[0] - im_layer_width - 35, size[1] - 50);
	
	if( im_tryWMCBoot()) 
		return;

	if( im_tryLayerBoot()) 
		return;
			
	// These calls are skipped if the tryXXX functions load anything
	//    and these actions will be performed asynch'ly
	im_bm.rebuild(imc_reloadLayers);		
	im_bm.setStatus("idle");
}

/**
 * Try using an external defined function imcb_getBootWmcUrl.
 * If it exists, and returns a valid WMC context, the function will return true. 
 */
function im_tryWMCBoot()
{
	var func = parent.imcb_getBootWmcUrl;

	if(typeof func == 'function')
	{
		var arr = func();
		
		if(arr)
		{
			var wmcurl = arr[0];
			var clr    = arr[1];
			
			imc_setContextFromURL(wmcurl, clr,
									function(req)
									{
										if(im_checkError(req))
										{
											im_bm.setStatus('idle');
											im_showError(req);
											return;
										}
		
					                     imc_reloadLayers();
										 
					 					var xml = req.responseXML;
										im_bm.set_dom(xml); 
										im_bm.setBBox_dom(xml);								 
				
										im_bm.setStatus('idle');
									}
								);
			return true;
		}			
	}
	
	return false;
}

/**
 * Try using an external defined function imcb_getBootWmcUrl.
 * If it exists, and returns a valid WMC context, the function will return true. 
 */
function im_tryLayerBoot()
{
	var func = parent.imcb_getBootLayer;

	if(typeof func == 'function')
	{
		var arr = func();
		
		if(arr)
		{
			var url   = arr[0];
			var layer = arr[1];
			var clr   = arr[2];
			
			imc_addService(url, layer, 2, clr,
									function(req)
									{
										if(im_checkError(req))
										{
											im_bm.setStatus('idle');
											im_showError(req);
											return;
										}
										
										im_buildLayerList(req);

										im_bm.rebuild(function(){
											im_bm.setStatus('idle');});
									}
								);
			return true;
		}			
	}
	
	return false;
}

//===================================================================
//
// Next lines define the standalone map behaviour
// according to the Intermap class in im_class.js
//
//===================================================================
			
im_extra_drivingMap = im_bm;

im_extra_afterLayerUpdated = function()
{
	im_bm.rebuild();
}; //.bindAsEventListener(im_bm);

im_extra_afterWmcSet = function(resp)
{
	//var xml = resp.responseXML;
	im_bm.set_dom(resp); // ?? maybe we don't want to resize the map
	im_bm.setBBox_dom(resp);	
};


/********************************************************************
*** RESET
********************************************************************/

function im_reset()
{
	im_bm.setStatus('busy');
	
	var myAjax = new Ajax.Request (
		getIMServiceURL('map.reset'),  
		{
			method: 'get',
			onSuccess: im_reset_complete,
			onFailure: im_load_error
		}
	);
}

function im_reset_complete(req)
{				
	im_bm.setTool('zoomin'); // set the default tool
	
	imc_reloadLayers(); // append layers to list

	im_bm.setBBox_dom(req.responseXML);	
	im_bm.rebuild();      
}


/********************************************************************
*** UTILS
********************************************************************/
/**
 * Get a localized string.
 * So far, localized strings used in the GUI should have a <i>js="true"</i> attribute.
 * Such strings are imported in HTML pages by the <i>localization</i> subtemplate, 
 * using as id the string "i18n_"+key, to avoid id collisions.
 * 
 * @param {String} key The key used in the <i>strings.xml</i> file. 
 * @return {String} The localized String  
 */
function i18n(key)
{
    var v = $('i18n_'+key);
    if(v)
    {
        if(v.value==='')
            return '{'+key+'}';
        else 
            return v.value;
    }        
    else
        return '['+key+']';    
}
