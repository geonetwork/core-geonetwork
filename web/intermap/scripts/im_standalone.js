//===================================================================
//
//  standalone.js
//
// This file contains the initialization, callback and eventhandler functions 
// to make a bigmap live on its own.
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

function imep_loadLayer(url, service)
{
	im_bm.setStatus('busy');
    imc_addService(url, service, 2, 
					function(req) 
					{
			             im_buildLayerList(req);
						 
						 if(im_extra_afterLayerUpdated)
						 	im_extra_afterLayerUpdated();
			        });		
}

function imep_setBBox(n,e,s,w)
{
	im_bm.setBBox(n,e,s,w);
	im_bm.rebuild();		
}

function imep_loadWmcFromUrl(url)
{
	im_bm.setStatus('busy');
	imc_setContextFromURL(url, 
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
//
// Next lines define the standalone map behaviour
// according to the Intermap class in im_class.js
//
//===================================================================

//var intermap = new Intermap(400, 200, 'im_bm_image');

function im_boot()
{	
	var size = getWindowSize();
	//alert("W:"+size[0]+" H:"+size[1]);
	
	im_bm.setSize( size[0] - im_layer_width - 35, size[1] - 50);	
	im_bm.rebuild();
	
	imc_reloadLayers(); // append layers to list
	
	setTool("zoomin");
	im_bm.setStatus("idle");
}

			
im_extra_drivingMap = im_bm;

im_extra_afterLayerUpdated = function()
{
	im_bm.rebuild();
}.bindAsEventListener(im_bm);

im_extra_afterWmcSet = function(resp)
{
	var xml = resp.responseXML;
	im_bm.set_dom(xml); // ?? maybe we don't want to resize the map
	im_bm.setBBox_dom(xml);	
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
