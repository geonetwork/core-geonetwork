
var minLayersDivWidth = 176;

/*
* The init procedure goes like this:
* - load minimap
* - enable the "Open Map Viewer"
* - wait for the "Open Map Viewer" command, then 
*    - load map viewer html skeleton 
*/
function im_init()
{
    im_mm_initTextControls($('northBL'), $('eastBL'), $('southBL'), $('westBL'));
    Event.observe('openIMBtn', 'click',  function(){openIntermap()} );						

    //imc_load();
    im_mm_refreshNeeded(); // load minimap
    im_mm_setTool('zoomin'); // set the default tool    
}


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
    	
        //Effect.BlindDown('im_mapContainer');
        Effect.BlindDown('im_map');
        Effect.BlindDown('im_mapImg');
        Effect.BlindDown('fillMeWithIntermap');
        //$('intermap').style.display='block';	
}				 

function closeIntermap()
{
        $('closeIMBtn').hide(); 
        $('openIMBtn').show(); 
        
        //Effect.BlindUp('im_mapContainer');
        Effect.BlindUp('im_map');
        Effect.BlindUp('fillMeWithIntermap');
        //$('intermap').style.display='none';	
}

function imc_init_loadSkel()
{
	var myAjax = new Ajax.Request (
		'/intermap/srv/en/map.getMain.embedded', 
		{
			method: 'get',
			parameters: '',
			onSuccess: ker.wrap(this, im_init_loadCompleted),
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
	$('im_mm_wait').hide(); //style.display='none';
	new Effect.Pulsate('openIMBtn');
			
	refreshNeeded(false, im_init_bmLoaded);
}

function im_init_bmLoaded()
{		
	Event.observe('im_map', 'mousedown', mousedownEventListener);
	Event.observe('im_mm_map', 'mousedown', im_mm_mousedownEventListener);
	
	setTool('zoomin'); // set the default tool	
	imc_reloadLayers(); // append layers to list

           openIntermap();
}


function im_reset()
{
            setStatus('busy');
            im_mm_setStatus('busy');
            
            imc_reset();
}

function imc_reset()
{
	var myAjax = new Ajax.Request (
		'/intermap/srv/en/map.reset', //'/intermap/srv/en/map.getMain.embedded', 
		{
			method: 'get',
			onSuccess: im_reset_complete,
			onFailure: im_load_error
		}
	);
}

function im_reset_complete(req)
{				
	setTool('zoomin'); // set the default tool
	im_mm_setTool('zoomin'); // set the default tool
	
	imc_reloadLayers(); // append layers to list

           imc_updateBigMap(im_bm_width, im_bm_height, null, false, 
               function() { imc_mm_update(im_mm_width, im_mm_height, null);});              
}


/********************************************************************
*** LAYERS
********************************************************************/

/* 
## Hook from GN xsl for dynamic data
*/
function runIM_addService(url, service, type)
{
    imc_addService(url, service, type, function() {
             imc_reloadLayers();
             imc_mm_update(im_mm_width, im_mm_height, null);
        });    
}


/* 
## Hook from GN xsl for dynamic data
## A server url has been specified. A list of selectable services will be diplayed
*/
function runIM_selectService(url, type)
{
    alert("TODO (runIM_selectService)");
    // TODO
}

