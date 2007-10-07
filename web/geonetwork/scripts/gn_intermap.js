/**********************************************************
***
*** Here you can find functions related to gn adding layers to the 
*** integrated map viewer.
***
***********************************************************
*** 
*** In 19139/19115 md you can find online resources. 
*** If such a resource refers to a WMS server, you can add its layers to the
*** current map.
***
*** At the moment we can handle: 
*** - WMS getMap service links (they point to exactly one layer in one server)
*** - WMS getCapabilitites (they point to a WMS server, and the user shell select which service to use)
***
*** A metadata can have zero, one or more online resources.
*** - If a metadata holds only one onlineres, 
***    - if it is a getMap, its related layer can be automatically added to the map Viewer.
***    - if it is a getCapab, a list of the layers offered by the server will be shown, and the user shall select the wanted ones. 
***  If a metadata holds many onlineres, a list will be shown showing them all. 
***    Selecting getMap resources, the user can add related layers to the map viewer, 
***    selecting getCapab resources, a list of the layers offered by the server will be shown, and the user shall select the wanted ones.
*** 139/115 templates may call one two functions,  
*** 
*** 
**********************************************************/

/* 
## Hook from GN xsl for dynamic data
*/
function runIM_addService(url, service, type)
{
    imc_addService(url, service, type, function(req) {
             im_buildLayerList(req);
             refreshNeeded(true);
             //imc_mm_update(im_mm_width, im_mm_height);
        });    
}

/* 
## Hook from GN xsl for dynamic data
## A server url has been specified. A list of selectable services will be displayed
*/
function runIM_selectService(url, type, mdid)
{
    gn_showGetCapabilities(url, type, mdid);
}

/********************************************************************
* 
*  Show list of addable interactive maps
*
********************************************************************/
/*
## This method is called by the "Interactive map [+]" button in a displayed metadata.  
## It will display the metadata distribution info in a div .
*/
function gn_showInterList(id) 
{
    var pars = 'id=' + id + "&currTab=distribution";
    
    // Change button appearance
    $('gn_showinterlist_' + id) .hide();
    $('gn_loadinterlist_' + id) .show();
    
    var myAjax = new Ajax.Request(
        '/geonetwork/srv/'+Env.lang+'/metadata.show.embedded', 
        {
            method: 'get',
            parameters: pars,
            onSuccess: function (req) {
                // This is a normally invisible DIV below every MD 
                var parent = $('ilwhiteboard_' + id);
                clearNode(parent);
                
                parent.show();
                
                $('gn_loadinterlist_' + id) .hide();
                $('gn_hideinterlist_' + id) .show();
                
                // create new element
                var div = document.createElement('div');
                div.className = 'metadata_current';
                div.hide();
                parent.appendChild(div);
                
                div.innerHTML = req.responseText;
                
                Effect.BlindDown(div);
                
                var tipman = new TooltipManager();
                ker.loadMan.wait(tipman);
            },
            onFailure: gn_search_error// FIXME
        });
}

function gn_hideInterList(id) 
{
    var parent = $('ilwhiteboard_' + id);
    var div = parent.firstChild;
    Effect.BlindUp(div, { afterFinish: function (obj) {
            clearNode(parent);
            $('gn_showinterlist_' + id) .show();
            $('gn_hideinterlist_' + id) .hide();
        }
    });
}

/********************************************************************
* 
*  
*
********************************************************************/

/*
## Called 
*/
function gn_showGetCapabilities(url, type, id)
{
    // Change button appearance (they do not exist when we're showing getCapab for a 1-onlineresource-only md)
    if($('gn_showinterlist_' + id))    $('gn_showinterlist_' + id) .show();
    if($('gn_loadinterlist_' + id))     $('gn_loadinterlist_' + id) .hide();
    if($('gn_hideinterlist_' + id))     $('gn_hideinterlist_' + id) .hide();
    
    // This is a normally invisible DIV below every MD 
    var parent = $('ilwhiteboard_' + id);
    clearNode(parent);
    parent.show();

    var div = document.createElement('div');
    div.className = 'metadata_current';
    parent.appendChild(div);
    var t1 = document.createElement("p");
    t1.innerHTML = i18n("waitGetCap");
    div.appendChild(t1);
            
    // Load and transform server's getCapabilities:
    imc_loadURLServices(url, // WMS server 
                                    -2, // type = -2: free WMS server url 
                                   function(req) {gn_getCapabLoaded(req,id);}, // callback: function called when ajax request completes 
                                   "gn_layersRequested("+id+");" ); // jscallback: function called by the button in the HTML returned by the AJAX service (ugly hack)
}

function gn_getCapabLoaded(req, mdid)
{
    var parent = $('ilwhiteboard_' + mdid);
    clearNode(parent);
    gn_addCloser(parent);

    var div = document.createElement('div');
    div.className = 'metadata_current';
    parent.appendChild(div);
    
    div.innerHTML = req.responseText;    
}

/*
## Called by the ok button generated by service mapServers.getServices.embedded
*/
function gn_layersRequested(mdid)
{
            var im = $('ilwhiteboard_' + mdid);
	//var im = $('im_serverList');
	
	// next two elements are created by the mapServers.getServices.embedded service
	var url   = $('im_addlayer_serverurl').value;
	var type = $('im_addlayer_type').value;
	
	var services = new Array();	
	var lilist = im.getElementsByTagName("input");
			
	$A(lilist).each(
	    function (input)
	    {
	        var value = input.value;
	        var checked = input.checked;
	        
	        if(checked)
	        {
	            services.push(value);
	        }
	    }		
	);	
		
	// setStatus('busy'); <-- we may need it 
	
	imc_addServices(url, services, type, function(req) {gn_layersAdded(req,mdid);});	
}

function gn_layersAdded(req, mdid)
{
    var parent = $('ilwhiteboard_' + mdid);
    clearNode(parent);
    gn_addCloser(parent);
        
    var div = document.createElement('div');
    div.className = 'metadata_current';
    parent.appendChild(div);
    var t1 = document.createElement("p");
    t1.innerHTML = i18n('layersAdded');
    div.appendChild(t1);


    im_buildLayerList(req); // rebuild layers' list
    
    // refreshes should be chained
    refreshNeeded(true); // refresh big map 
    im_mm_refreshNeeded(); // refresh minimap			
}


function gn_addCloser(domnode, callback)
{
    var closer = Builder.node('div', {class: "im_wbcloser"});
    var img = Builder.node('img',
    {
        title: i18n("close"), 
        src: "/intermap/images/close.png"
    });
    closer.appendChild(img);
    
    domnode.appendChild(closer);
    Event.observe(closer, 'click', 
        function()
        {  
            clearNode(domnode);
            if(callback)
                callback();        
            // TODO we should also remove the current observer
        });
        
}


/* EOF ******************************************************* EOF */
