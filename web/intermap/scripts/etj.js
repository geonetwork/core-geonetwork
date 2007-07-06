
var minLayersDivWidth = 236;

function im_load()
{
    imc_load();
}

function imc_load()
{
	var myAjax = new Ajax.Request (
		'/intermap/srv/en/map.getMain.embedded', 
		{
			method: 'get',
			parameters: '',
			onSuccess: im_load_complete,
			onFailure: im_load_error
		}
	);
}

function im_load_error()
{
	alert("Loading error");
}

function im_load_complete(req)
{				
	// Dinamically generate content
	var im = $('intermap');
	copyTree(req.responseXML.documentElement.getElementsByTagName('html')[0].firstChild , im);
	
	$('im_mm_wait').hide(); //style.display='none';
	$('openIMBtn').textContent="Open Intermap >>>";
	$('openIMBtn').className="imenabled";
	Event.observe('openIMBtn', 'click',  function(){openIntermap()} );						
	new Effect.Pulsate('openIMBtn');
	
	var bm =req.responseXML.documentElement.getElementsByTagName('bigmap')[0];
	var mm =req.responseXML.documentElement.getElementsByTagName('minimap')[0];
	
           //im_setMapSize(400,300);
//	var height = getWindowSize()[1]; // - Element.getHeight('banner');
//	$('im_layers').style.height = height + 'px';
		
	Event.observe('im_map', 'mousedown', mousedownEventListener);
	Event.observe('im_mm_map', 'mousedown', im_mm_mousedownEventListener);
	
	setTool('zoomin'); // set the default tool
	im_mm_setTool('zoomin'); // set the default tool
	
	imc_reloadLayers(); // append layers to list

	im_mm_initTextControls($('northBL'), $('eastBL'), $('southBL'), $('westBL'))

	im_mm_set(mm);
	im_bm_set(bm);	
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
		'/intermap/srv/en/map.getMain.embedded', 
		{
			method: 'get',
			parameters: 'reset=true',
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

	var bm =req.responseXML.documentElement.getElementsByTagName('bigmap')[0];
	var mm =req.responseXML.documentElement.getElementsByTagName('minimap')[0];

	im_mm_set(mm);
	im_bm_set(bm);
	
	setStatus('idle');
	im_mm_setStatus('idle');
}


/********************************************************************
*** LAYERS
********************************************************************/

/* 
## Hook from GN xsl for dynamic data
*/
function runIM_addService(url, service, type)
{
    imc_addService(url, service, type, function() {imc_reloadLayers();});    
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

/*
## Called by the bottom toolbar
*/
function im_addLayer()
{
    imc_loadMapServers( im_mapServersLoaded );
}

/*
## Display the map servers and allow to select one of them
*/
function im_mapServersLoaded(req)
{
    // setup WB
    clearNode('im_whiteboard');    
    var WB = $('im_whiteboard');

    var wbtitle = im_createWBTitle('Add a layer'); //FIXME i18n
    WB.appendChild(wbtitle);

    var closer = im_getWBCloser();
    WB.appendChild(closer);
    Event.observe(closer, 'click', im_closeWhiteBoard);

    // fill contents

    var servers = req.responseXML.documentElement.getElementsByTagName('mapServers')[0].getElementsByTagName('server');

    var div = document.createElement('div'); // main box
    div.id = "im_serverList";
    div.className = 'im_wbcontent';
    WB.appendChild(div);

    var h1 = document.createElement('h1');
    h1.textContent = "Server list " ; //FIXME i18n me!
    h1.id = "im_serverList_title";
    div.appendChild(h1);

    var ul = document.createElement('ul');
    ul.id = "im_serverList_list";
    div.appendChild(ul);
        
    $A(servers).each(
        function(server)
        {
            var id=server.getAttribute('id');
            var name =server.getAttribute('name'); 
        
            var li = document.createElement('li');
            ul.appendChild(li);
            
            var a = document.createElement('a');            
            a.textContent = name;
            a.id = "im_mapserver_" + id;
            a.setAttribute("onClick", "im_mapServerSelected("+id+",'"+name+"');");
            li.appendChild(a);

//            Event.observe(li, 'click',  function(){ imc_loadServices(id, im_servicesLoaded )} );            
        }
    );
    
    // add a textbox to enter a server directly
            var li = document.createElement('li');
            li.textContent = 'Other WMS server'; // FIXME i18n
            ul.appendChild(li);

            var input = document.createElement('input');
            input.id= "im_wmsservername";
            input.className = 'content';
            input.setAttribute("type", "text"); 
            input.setAttribute("size", "40");             
            li.appendChild(input);

           var button  = document.createElement('button');
           button.textContent = "Connect"; // FIXME i18n
           li.appendChild(button);
           
           Event.observe(button, "click", function() { im_mapServerURL($('im_wmsservername').value);});
}

/*
## Called when a known map server has been selected
*/
function im_mapServerSelected(id, name)
{
	var im = $('im_serverList');
	clearNode(im);
	
	var t1 = document.createElement("p");
	t1.textContent = "...please wait...";
	im.appendChild(t1);
	
	var t2 = document.createElement("p");
	t2.textContent = "Loading services from " +name;	
	im.appendChild(t2);
	
            imc_loadServerServices(id, im_servicesLoaded );
}

function imc_loadServerServices(id, callback)
{
	var url = '/intermap/srv/en/mapServers.getServices.embedded';
	var pars = 'mapserver='+id ; 				
	
	var myAjax = new Ajax.Request (
		url, 
		{
			method: 'get',
			parameters: pars,
			onSuccess: callback,
			onFailure: im_load_error
		}
	);
}

/*
## Called when the URL of an unknown map server has been given
*/
function im_mapServerURL(url)
{
	var im = $('im_serverList');
	clearNode(im);
	
	var t1 = document.createElement("p");
	t1.textContent = "...please wait...";
	im.appendChild(t1);
	
	var t2 = document.createElement("p");
	t2.textContent = "Loading services from given WMS server";	
	im.appendChild(t2);
	
            imc_loadURLServices(url, -2, im_servicesLoaded );
}

function imc_loadURLServices(url, type, callback)
{
	var pars = 'mapserver='+type+"&url="+url ; 				
	
	var myAjax = new Ajax.Request (
		'/intermap/srv/en/mapServers.getServices.embedded', 
		{
			method: 'get',
			parameters: pars,
			onSuccess: callback,
			onFailure: im_load_error
		}
	);
}

function im_servicesLoaded(req)
{
	// Dinamically generate content
	var im = $('im_serverList');
	im.innerHTML =req.responseText; 
}

/*
## Called by the ok button generated by service mapServers.getServices.embedded
*/
function im_servicesSelected()
{
	var im = $('im_serverList');
	
	// next two elements are created by the mapServers.getServices.embedded service
	var url   = $('im_addlayer_serverurl').value;
	var type = $('im_addlayer_type').value;
	
	var services = new Array();
	//var query = "url="+url+"&type="+type;
	
	var lilist = im.getElementsByTagName("input");
			
	$A(lilist).each(
	    function (input)
	    {
	        var value = input.value;
	        var checked = input.checked;
	        
	        if(checked)
	        {
	            services.push(value);
	            //query += "&service="+value;	        
	        }
	    }
	
	
	);
	
	//alert(query);
	
	setStatus('busy');
	imc_addServices(url, services, type, im_servicesAdded);
	
}

function im_servicesAdded(req)
{
	var im = $('im_serverList');
	clearNode(im);
	
	var t1 = document.createElement("p");
	t1.textContent = "Selected layers have been added"; // fixme i18n
	im.appendChild(t1);
	
	im_buildLayerList(req); // rebuild layers' list
	
	//imc_reloadLayers(); 
			
           // refreshes should be chained
	refreshNeeded(); // refresh big map
	// im_mm_refreshNeeded(); // refresh minimap	
		
}

/********************************************************************
*** MAIL
********************************************************************/
/*
## Called by the bottom toolbar
*/
function im_sendMail()
{
    clearNode('im_whiteboard');

    var div = document.createElement('div');
    div.id = "im_sendMail";
    div.className = 'im_wbcontent';
    $('im_whiteboard').appendChild(div);

    var wbtitle = im_createWBTitle("Send this map's context"); //FIXME i18n
    div.appendChild(wbtitle);

    var closer = im_getWBCloser();
    div.appendChild(closer);
    Event.observe(closer, 'click', im_closeWhiteBoard);

    var h1 = document.createElement('h1');
    h1.textContent = "Send this map context" ; //FIXME i18n 
    div.appendChild(h1);


    var input = document.createElement('input');
    input.setAttribute('name', 'emailaddress');
    input.setAttribute('size', '35');
    input.setAttribute('value', 'user@domain');
    div.appendChild(input);

}

/********************************************************************
*** PDF
********************************************************************/
/*
## Called by the bottom toolbar
*/
function im_createPDF()
{
    clearNode('im_whiteboard');

    var div = document.createElement('div');
    div.id = "im_createPDF";
    div.className = 'im_wbcontent';    
    $('im_whiteboard').appendChild(div);

    var wbtitle = im_createWBTitle("Export this map as PDF"); //FIXME i18n
    div.appendChild(wbtitle);

    var closer = im_getWBCloser();
    div.appendChild(closer);
    Event.observe(closer, 'click', im_closeWhiteBoard);

    var h1 = document.createElement('h1');
    h1.textContent = "TODO" ; //FIXME i18n 
    div.appendChild(h1);

}

/********************************************************************
*** Export image
********************************************************************/
/*
## Called by the bottom toolbar
*/
function im_createPic()
{
    clearNode('im_whiteboard');

    var div = document.createElement('div');
    div.id = "im_createPic";
    $('im_whiteboard').appendChild(div);

    var wbtitle = im_createWBTitle("Export this map as image"); //FIXME i18n
    div.appendChild(wbtitle);

    var closer = im_getWBCloser();
    div.appendChild(closer);
    Event.observe(closer, 'click', im_closeWhiteBoard);

    var h1 = document.createElement('h1');
    h1.textContent = "TODO" ; //FIXME i18n 
    div.appendChild(h1);

}

/********************************************************************
*** SUB TOOLBAR UTILITIES
********************************************************************/

function im_createWBTitle(title)
{
    var div = document.createElement('div');
    div.id = "im_wbtitle";
    var h1 = document.createElement('h1');
    h1.textContent = title;
    div.appendChild(h1);       

//    Effect.BlindDown('im_whiteboard');

    return div;
}


function im_getWBCloser()
{
    var closer = document.createElement('div');
    closer.id = "im_wbcloser";
    var img = document.createElement('img');
    img.setAttribute("title", "Close"); // FIXME i18N
    img.setAttribute("src", "/intermap/images/close.png"); 
    closer.appendChild(img);
    
    //Event.observe(closer, 'click', im_closeWhiteBoard);

        return closer;
}

function im_closeWhiteBoard()
{
//    Effect.SwitchOff('im_whiteboard');
//    Effect.BlindUp('im_whiteboard');
    clearNode('im_whiteboard');
}



