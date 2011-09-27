//=====================================================================================
//===
//=== GeoNetwork's kernel Ajax functions
//===
//=====================================================================================

var ker = new Object();

ker.loadMan = new LoadManager();
ker.includes= {};

//=====================================================================================

ker.include = function(file)
{
	file = Env.url +'/scripts/'+ file;
	
	if (ker.includes[file])
		return;
		
	ker.includes[file] = 'ok';
	document.write('<script type="text/javascript" src="'+file+'"></script>');	
}

//=====================================================================================

//ker.include('prototype.js');
//ker.include('sarissa.js');

ker.include('core/kernel/xml.js');
ker.include('core/kernel/string.js');
ker.include('core/kernel/xml-loader.js');
ker.include('core/kernel/xsl-transformer.js');
ker.include('core/kernel/info-service.js');

//=====================================================================================
/*	Creates an XML request like <request>...</request>.
 
	if 'params' is a string returns:
 		<request>
 			<'elemName'>params</'elemName'>
 		</request>
 
	otherwise 'params' must be an array and the result is:
 		<request>
 			<'elemName'>params[0]</'elemName'>
 			<'elemName'>params[1]</'elemName'>
			...
 		</request>
*/

ker.createRequest = function(elemName, params)
{
	var request = '<request>\n';

	if (typeof params == 'string')
		request += '<'+ elemName +'>'+ xml.escape(params) +'</'+ elemName +'>\n';
	else
	{ 
		for (var i=0; i<params.length; i++)
			request += '<'+ elemName +'>'+ xml.escape(params[i]) +'</'+ elemName +'>\n';
	}

	request += '</request>';

	return request;
}

/*  Creates an XML request like <request>....</request>

    Params should be a javascript object eg:

		{ type : 'threddsFragmentStyleSheets',  schema : 'iso19139' }
 */

ker.createRequestFromObject = function(params)
{
	var request = '<request>\n';

	for (var param in params) {
		request += '<'+ param +'>'+ xml.escape(params[param]) +'</'+ param +'>\n';
	}

	request += '</request>';

	return request;
}

//=====================================================================================
/*	Sends an XML request to the server. Params:
	- service : the geonetwork service to call
	- request : the XML request in string form
	- onSuccessFnc : function to call on success
	- xmlResponse : 'true' if the response is XML. 'false' for text (default is 'true').
*/

ker.send = function(service, request, onSuccessFnc, xmlResponse)
{
	if (xmlResponse != false)
		xmlResponse = true;

	var opt = 
	{
		method: 'post',
		postBody: request,
		requestHeaders: ['Content-type', 'application/xml'],

		onSuccess: function(t) 
		{
			ker.showAjaxWait(false);

			if (onSuccessFnc)
				if (xmlResponse)	onSuccessFnc(xml.ieFix(t.responseXML.firstChild));
					else				onSuccessFnc(t.responseText);

		},
		
		on404: function(t) 
		{
			ker.showAjaxWait(false);			
			alert('Error 404: service "' + t.statusText + '" was not found.');
		},
		
		onFailure: function(t) 
		{
			ker.showAjaxWait(false);
			
			if (t.status >= 400 && t.status <= 500)
			{
				if (onSuccessFnc)
					if (xmlResponse)	onSuccessFnc(xml.ieFix(t.responseXML.firstChild));
						else				onSuccessFnc(t.responseText);
			}
			else		
				alert('Error ' + t.status + ' -- ' + t.statusText);
		}
	}

	ker.showAjaxWait(true);
		
	new Ajax.Request(Env.locService +'/'+ service, opt);
}

//=====================================================================================

ker.loadURL = function(url, callBack)
{
	var opt = 
	{
		method: 'get',

		onSuccess: callBack,
		
		on404: function(t) 
		{
			alert('Error 404: location "' + url + '" not found.');
		},
		
		onFailure: function(t)
		{
			alert('Error ' + t.status + ' -- ' + t.statusText);
		}
	}

	new Ajax.Request(url, opt);
}

//=====================================================================================

ker.showAjaxWait = function(yesno)
{
	var waitImg = $('ajax.wait');
	
	if (waitImg == null)	
		return;
	
	if (yesno)	waitImg.show();
		else		waitImg.hide();
}

//=====================================================================================

ker.showError = function(message, xmlResult)
{
	var errId  = xmlResult.getAttribute('id');
	var errMsg = xmlResult.getElementsByTagName('message');
	var object = xmlResult.getElementsByTagName('object');
	var clazz  = xmlResult.getElementsByTagName('class');
	
	var text = message +'\n';
	
	if (errId)
		text += 'Error : '+ errId +'\n';
		
	if (errMsg.length != 0)
		text += 'Message : '+ xml.textContent(errMsg[0]) +'\n';
	
	if (clazz.length != 0)
		text += 'Class : '+ xml.textContent(clazz[0]) +'\n';

	if (object.length != 0)
		text += 'Object : '+ xml.textContent(object[0]) +'\n';
	
	alert(text);
}

//=====================================================================================

ker.wrap = function(oldThis, func)
{
	return function()
	{
		//--- trap function execution just to report errors

		//try
		//{
			return func.apply(oldThis, arguments);
		/*}
		catch(err)
		{ 
			alert(err);
			alert(func);
			alert(oldThis);
		}*/
	}
}

//=====================================================================================

ker.dump = function(obj, msg)
{
	if (msg == null)	msg = '';
		else				msg = '('+ msg +')';
		
	var title = 'ker.dump '+ msg +'\n\n';
	
	if (obj == null)
	{
		alert(title +'Object is null');
		return;
	}
	
	try
	{
		//--- is the object an xml one?
		
		if (typeof obj == 'string')
			alert(title +'Object is a string:\n\n'+ obj);
		
		else if (typeof obj == 'boolean')
			alert(title +'Object is a boolean:\n\n'+ obj);
		
		else if (obj.nodeType)	
			alert(title +'Object is XML:\n\n'+ xml.toString(obj));
			
		else
		{
			var type = typeof obj;
			var text = 	title +
							'Object is generic (type is '+ type +'):\n\n'+
							Sarissa.xmlize(obj, "object");
			
			alert(text);
		}
	}
	catch(e)
	{
		alert(title +'Raised error : '+ e);
	}
}

//=====================================================================================

ker.wait = function(millis)
{
	var date = new Date();
	var curDate = null;

	do 
	{ 
		curDate = new Date(); 
	} 
	while (curDate-date < millis);
}

//=====================================================================================
//===
//=== LoadManager
//===
//=====================================================================================

function LoadManager()
{
	var count = 0;
	var object= null;
	
	this.acquire = function() { count++; }
	this.release = function() { count--; }
	
//=====================================================================================
/* Waits untill all files have been loaded. Then, calls 'init' on provided object
*/

this.wait= function(obj) 
{
	object = obj;
	setTimeout(waitLoop, 10);
}

//=====================================================================================

function waitLoop()
{
	if (count != 0)	setTimeout(waitLoop, 100);
		else				object.init();
		
}

//=====================================================================================
}
