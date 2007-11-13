//===================================================================
//  This file contains function related to the integration of
//  the minimap and the bigmap inside the GN page.
//
//
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



/**
 * Init the mm+bm
 * 
 * @param {string} wmc The WMC context to set -- may be null 
 * @param {function} callback The callback function to pass to the rebuild call
 */
function im_mm_init(wmc, callback)
{
    im_mm_initTextControls($('northBL'), $('eastBL'),
						   $('southBL'), $('westBL'));

    if (im_mm_ctrl_n.value === '') {im_mm_ctrl_n.value=im_mm.north;}        
    if (im_mm_ctrl_e.value === '') {im_mm_ctrl_e.value=im_mm.east;}        
    if (im_mm_ctrl_s.value === '') {im_mm_ctrl_s.value=im_mm.south;}        
    if (im_mm_ctrl_w.value === '') {im_mm_ctrl_w.value=im_mm.west;}

	var newCallback = function()
	{
		if (callback)
		{ 
			callback();
		}
			 
		im_mm_fullAoI();		
	};
    
	if(wmc)
	{
		im_setWMC(wmc, function(req)
						{
							var resp = req.responseXML.documentElement;
							//TODO: force loading of big map?
							im_bm.setBBox_dom(resp);
							im_bm.setSize_dom(resp);
							
							im_mm.setBBox_dom(resp);                                    
							im_mm.rebuild(newCallback); // load minimap
							im_mm.setTool('zoomin'); // set the default tool                                        
						});
	}
	else
	{
		im_mm.rebuild(newCallback); // load minimap
		im_mm.setTool('zoomin'); // set the default tool    
	}
}



// static
var im_refreshMiniMap = function()
{
	im_mm.rebuild();
};

// static
var im_refreshBothMaps = function()
{
	im_bm.rebuild(im_refreshMiniMap);					
};


im_extra_drivingMap = im_bm;

im_extra_afterLayerUpdated = im_refreshBothMaps;

im_extra_afterWmcSet = function(resp)
{
	im_bm.set_dom(resp);
	im_bm.setBBox_dom(resp);
	  
	im_mm.setBBox_dom(resp);  
	im_mm.rebuild(im_mm_fullAoI);
	// TODO? extend the AoI to the whole current view
};

/*
* The init procedure goes like this:
* - load minimap
* - enable the "Open Map Viewer"
* - wait for the "Open Map Viewer" command, then 
*    - load map viewer html skeleton 
*/

var im_1stTimeIntermap = true;

function openIntermap()
{
    if(im_1stTimeIntermap)
    {
        im_1stTimeIntermap = false;
        
        $('openIMBtn').hide(); 
        $('loadIMBtn').show(); 
        
        imc_init_loadSkel();
        return;        // loadSkel should call us back
    }
    
    $('openIMBtn').hide(); 
    $('loadIMBtn').hide(); 
    $('closeIMBtn').show(); 
    
    if( ! Prototype.Browser.IE )
    {
        Effect.BlindDown('im_map');
        Effect.BlindDown('im_bm_image');
        Effect.BlindDown('fillMeWithIntermap');
    }
    else
    {    
        $('im_map').show();	
        $('im_bm_image').show();
        $('fillMeWithIntermap').show();
    }
        
//	forceIErefresh();        
}				 

function closeIntermap()
{
	if( ! $('im_map'))
		return;

	// These buttons may have been already removed from page if we're leaving simple search 
	try {
		$('closeIMBtn').hide();
		$('openIMBtn').show();
	} catch(e) { /*nothing to do*/} 
	
	if( ! Prototype.Browser.IE )
	{
		Effect.BlindUp('im_map');
		Effect.BlindUp('fillMeWithIntermap');
	}
	else
	{
		$('im_map').hide();
		$('fillMeWithIntermap').hide();
	}
}

function imc_init_loadSkel()
{
	var myAjax = new Ajax.Request (
		getIMServiceURL('map.getMain.embedded'), 
		{
			method: 'get',
			parameters: '',
			onSuccess: im_init_loadCompleted,
			onFailure: im_load_error
		}
	);
}
//			onSuccess: ker.wrap(this, im_load_complete),


function im_init_loadCompleted(req)
{				
	// Dinamically generate content
	var im = $('fillMeWithIntermap');          
    	im.innerHTML = req.responseText;
	$('im_mm_image_waitdiv').hide(); 
	new Effect.Pulsate('openIMBtn');
			
	im_bm.rebuild(im_init_bmLoaded); 
}

function im_init_bmLoaded()
{		
	im_mm.setStatus('idle'); // this will also bind the mousedowneventlistener
	im_bm.setStatus('idle'); // this will also bind the mousedowneventlistener

	Event.observe('im_resize', 'mousedown', im_bm_resizeStart);
	
	setTool('zoomin'); // set the default tool	
	imc_reloadLayers(); // append layers to list

	openIntermap();
}

/********************************************************************
*** RESET
********************************************************************/

function im_reset()
{
	im_bm.setStatus('busy');
	im_mm.setStatus('busy');
	
	imc_reset();
}

function imc_reset()
{
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
	im_mm.setTool('zoomin'); // set the default tool
	
	imc_reloadLayers(); // append layers to list

	im_bm.setBBox_dom(req.responseXML);
	im_mm.setBBox_dom(req.responseXML);
	
	im_bm.rebuild(im_mm.rebuild.bindAsEventListener(im_mm));
	
	// TODO: reset AoI also?      
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
