//=====================================================================================
//===
//=== New Ajax support functions
//===
//=== Needs : prototype.js
//===
//=====================================================================================

var gn  = new Object();
var gui = new Object();

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

gn.createRequest = function(elemName, params)
{
	var request = '<request>\n';

	if (typeof params == 'string')
		request += '<'+ elemName +'>'+ params +'</'+ elemName +'>\n';
	else
	{ 
		for (var i=0; i<params.length; i++)
			request += '<'+ elemName +'>'+ params[i] +'</'+ elemName +'>\n';
	}

	request += '</request>';

	return request;
}

//=====================================================================================
/*	Sends an XML request to the server. Params:
	- service : the geonetwork service to call
	- request : the XML request in string form
	- onSuccessFnc : function to call on success
*/

gn.send = function(service, request, onSuccessFnc)
{
	var opt = 
	{
		method: 'post',
		postBody: request,
		requestHeaders: ['Content-type', 'application/xml'],

		onSuccess: function(t) 
		{
			gn.showAjaxWait(false);
			
			if (onSuccessFnc)
				onSuccessFnc(t.responseXML);
		},
		on404: function(t) 
		{
			gn.showAjaxWait(false);			
			alert('Error 404: service "' + t.statusText + '" was not found.');
		},
		onFailure: function(t) 
		{
			gn.showAjaxWait(false);
			
			if (t.status >= 400 && t.status <= 500)
			{
				if (onSuccessFnc)
					onSuccessFnc(t.responseXML);
			}
			else		
				alert('Error ' + t.status + ' -- ' + t.statusText);
		}
	}

	gn.showAjaxWait(true);
		
	new Ajax.Request(Env.locService +'/'+ service, opt);
}

//-------------------------------------------------------------------------------------

gn.showAjaxWait = function(yesno)
{
	var waitImg = $('ajax.wait');
	
	if (waitImg == null)	
		return;
	
	if (yesno)	waitImg.show();
		else		waitImg.hide();
}

//=====================================================================================

gn.xmlToString = function(xml, ident)
{
	if (!ident)
		ident = '';
		
	if (xml == null)
		throw 'gn.xmlToString: xml is null';
		
	//--- skip document node

	if (xml.nodeType == Node.DOCUMENT_NODE)
		xml = xml.firstChild;

	var result = ident +'<'+ xml.nodeName;

	//--- handle attributes

	for (var i=0; i<xml.attributes.length; i++)
	{
		var name  = xml.attributes[i].name;
		var value = xml.getAttribute(name);
		
		result += ' '+ name +'="'+ gn.escape(value) +'"';
	}

	result += '>\n'

	//--- handle children

	result += gn.xmlToStringCont(xml, ident);

	return result + ident +'</'+ xml.nodeName +'>\n';
}

//=====================================================================================

gn.xmlToStringCont = function(xml, ident)
{
	if (!ident)
		ident = '';
		
	if (xml == null)
		throw 'gn.xmlToStringCont: xml is null';
		
	//--- skip document node

	if (xml.nodeType == Node.DOCUMENT_NODE)
		xml = xml.firstChild;
	
	var result = '';
	
	for (var i=0; i<xml.childNodes.length; i++)
	{
		var child = xml.childNodes[i];
		
		if (child.nodeType == Node.ELEMENT_NODE)
			result += gn.xmlToString(child, ident +'   ');
			
		else if (child.nodeType == Node.TEXT_NODE)
			result += gn.escape(child.nodeValue);
	}

	return result;
}

//=====================================================================================

gn.visit = function(node, callBack)
{
	if (node.nodeType == Node.DOCUMENT_NODE)
		node = node.firstChild;
	
	var array = [ node ];
	
	while (array.length != 0)
	{
		node = array.shift();		
		
		if (!callBack(node))
			return;
			
		node = node.firstChild;
		
		while (node != null)
		{
			if (node.nodeType == Node.ELEMENT_NODE)
				array.push(node);
			
			node = node.nextSibling;
		}
	}
}

//=====================================================================================

gn.children = function(xml)
{
	var result = [];
	
	if (xml != null)
		xml = xml.firstChild;
		
	while (xml != null)
	{
		if (xml.nodeType == Node.ELEMENT_NODE)
			result.push(xml);
			
		xml = xml.nextSibling;
	}
	
	return result;
}

//=====================================================================================

gn.evalXPath = function(xmlNode, xpath)
{
	var names = xpath.split('/');
			
	for (var i=0; i<names.length; i++)
	{	
		xmlNode = xmlNode.firstChild;
		
		var found = false;
		
		while (xmlNode != null && !found)
		{
			if (xmlNode.nodeType == Node.ELEMENT_NODE && xmlNode.nodeName == names[i])
				found = true;
			else
				xmlNode = xmlNode.nextSibling;
		}
		
		if (xmlNode == null)
			return null;
	
	}
	
	return xmlNode.textContent;
}

//=====================================================================================

gn.getElementById = function(node, id)
{
	var result = null
	
	gn.visit(node, function(node)
	{
		if (node.getAttribute('id') != id)
			return true;
		
		result = node;
		return false;
	});
	
	return result;
}

//=====================================================================================

gn.wrap = function(oldThis, func)
{
	return function()
	{
		//--- trap function execution just to report errors

		try
		{
			return func.apply(oldThis, arguments);
		}
		catch(err)
		{ 
			alert(err);
		}
	}
}

//=====================================================================================

gn.replace = function(text, pattern, subst)
{
	var res = '';
	var pos = text.indexOf(pattern);

	while (pos != -1)
	{
		res  = res + text.substring(0, pos) + subst;
		text = text.substring(pos + pattern.length);
		pos  = text.indexOf(pattern);
	}

	return res + text;
}

//=====================================================================================
/* Takes a template and substitutes all constants like {NAME} with the value found
	inside the map. The value is escaped to be xml/html compliant.  
*/

gn.substitute = function(template, map)
{
	for (var name in map)
	{
		var value = map[name];
		
		//--- skip arrays and other objects
		if (typeof value == 'object')
			continue;
			
		if (typeof value == 'boolean')
			value = (value) ? 'true' : 'false';
			
		else if (typeof value == 'number')
			value = '' + value;
		
		template = gn.replace(template, '{'+name+'}', gn.escape(value));
	}
	
	return template;
}

//=====================================================================================

gn.escape = function(text)
{
	if (text == '')
		return text;
		
	return text	.replace(/&/g, "&amp;")
					.replace(/</g, "&lt;")
					.replace(/>/g, "&gt;")
					.replace(/"/g, "&quot;")
					.replace(/'/g, "&apos;");
};

//=====================================================================================

gn.showError = function(message, xmlResult)
{
	var errId  = xmlResult.getAttribute('id');
	var errMsg = xmlResult.getElementsByTagName('message');
	var object = xmlResult.getElementsByTagName('object');
	
	var text = message +'\n';
	
	if (errId)
		text += 'Error : '+ errId +'\n';
		
	if (errMsg.length != 0)
		text += 'Message : '+ errMsg[0].textContent +'\n';
	
	if (object.length != 0)
		text += 'Object : '+ object[0].textContent +'\n';
	
	alert(text);
}

//=====================================================================================

gn.wait = function(millis)
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
//=== GUI methods
//===
//=====================================================================================

/* Given a table, removes all rows but the first
 */

gui.removeAllButFirst = function(tableId)
{
	var rows = $(tableId).getElementsByTagName('TR');
	
	for (var i=rows.length-1; i>0; i--)
		Element.remove(rows[i]);		
}

//=====================================================================================
/* Creates tooltips for all provided elements in an XML node
 */

gui.setupTooltips = function(xml)
{
	var node = xml.firstChild;
	
	while (node != null)
	{
		if (node.nodeType == Node.ELEMENT_NODE)
		{
			var elem = $(node.getAttribute('id'));
			var mesg = gn.xmlToStringCont(node);
		
			new Tooltip(elem, mesg);
		}
		
		node = node.nextSibling;
	}
}

//=====================================================================================
//===
//=== URLLoader
//===
//=====================================================================================

URLLoader = function(file, onSuccessFnc)
{
	var opt = 
	{
		method: 'get',

		onSuccess: onSuccessFnc,
		on404: function(t) 
		{
			alert('Error 404: location "' + t.statusText + '" not found.');
		},
		onFailure: function(t)
		{
			alert('Error ' + t.status + ' -- ' + t.statusText);
		}
	}

	new Ajax.Request(file, opt);
}

//=====================================================================================
//===
//=== XMLLoader
//===
//=== Loads an XML file from the server. Used for example to load localized strings 
//=====================================================================================

function XMLLoader(file)
{
	this.listeners = [];

	var _this = this;
	
	new URLLoader(file, gn.wrap(this, function(t) 
	{
		_this.strings = t.responseXML.firstChild;
		
		var list = _this.listeners;
		
		for (var i=0; i<list.length; i++)
			list[i]();
	}));
}

//=====================================================================================

XMLLoader.prototype.isLoaded = function()
{
	return this.strings;
}

//=====================================================================================

XMLLoader.prototype.getText = function(name)
{
	if (!this.strings)
		return '*loading : '+ name +'*';
			
	var node = this.strings;	
	var list = node.getElementsByTagName(name);
	
	if (list.length == 0)	return '*not-found:'+ name +'*';
		else						return list[0].textContent;
}

//=====================================================================================

XMLLoader.prototype.getNode = function(name)
{
	if (name)
		return this.strings.getElementsByTagName(name)[0];
		
	return this.strings;
}

//=====================================================================================

XMLLoader.prototype.addListener = function(fnc)
{
	this.listeners.push(fnc);
}

//=====================================================================================
//===
//=== TabSwitcher
//===
//=== Given a list of DIVS or other containers with unique ids, allows to show only
//=== one of them at a time, hiding the others. It is like a java tabbed pane
//=====================================================================================

function TabSwitcher()
{
	this.idLists = new Array();
	
	for (var i=0; i<arguments.length; i++)
		this.idLists.push(arguments[i]);
}

//=====================================================================================

TabSwitcher.prototype.show = function()
{
	for (var i=0; i<arguments.length; i++)
		for (var j=0; j<this.idLists[i].length; j++)
			if (arguments[i] == this.idLists[i][j])	Element.show(this.idLists[i][j]);
				else												Element.hide(this.idLists[i][j]);
}

//=====================================================================================
//===
//=== Shower
//===
//=====================================================================================

function Shower(sourceId, targetId)
{
	this.source = $(sourceId);
	this.target = $(targetId);
	
	$(sourceId).onchange = gn.wrap(this, this.update);
	
	this.update();
}

//=====================================================================================

Shower.prototype.update = function()
{
	if (this.source.checked)	Element.show(this.target);
		else 							Element.hide(this.target);
}

//=====================================================================================
//===
//=== Tooltip
//===
//=== Shows a tooltip for an element.
//=====================================================================================

/* elem can be a DOM element or its id
*/

function Tooltip(elem, message)
{
	if (typeof elem == 'string')
		elem = $(elem);
		
	this.elem = elem;
	this.shown= false;
	this.msg  = message;
	
	this.initDelay = 1000;
	
	Event.observe(elem, 'mouseover', gn.wrap(this, this.mouseIn));
	Event.observe(elem, 'mouseout',  gn.wrap(this, this.mouseOut));	
}

//=====================================================================================

Tooltip.prototype.mouseIn = function(event)
{
	if (this.shown)
		return;
		
	if (!this.tip)
	{
		this.tip = document.createElement('div');
		this.tip.className     = 'tooltip';
		this.tip.innerHTML     = this.msg;
		this.tip.style.display = 'none';
		
		document.body.appendChild(this.tip);	
	}
		
	this.tip.style.left = Event.pointerX(event) +12;
	this.tip.style.top  = Event.pointerY(event) +12;
	
	this.timer = setTimeout(gn.wrap(this, this.mouseIn_CB), this.initDelay);	
}

//-------------------------------------------------------------------------------------

Tooltip.prototype.mouseIn_CB = function()
{
	Element.show(this.tip);	
	
	this.shown = true;
}

//=====================================================================================

Tooltip.prototype.mouseOut = function(event)
{
	if (this.timer)
	{
		clearTimeout(this.timer);
		this.timer = null;
	}
	
	if (!this.shown)
		return;
		
	Element.hide(this.tip);	
	
	this.shown = false;
}

//=====================================================================================

