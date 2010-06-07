//=====================================================================================
//===
//=== XML related methods
//===
//=== Needs: -
//=====================================================================================

var xml = new Object();

//=====================================================================================

xml.escape = function(text)
{
	if (text == '' || text == null)
		return text;
		
	if (typeof text != 'string')
		return '***[not a string]***';
		
	return text	.replace(/&/g, '&amp;')
					.replace(/</g, '&lt;')
					.replace(/>/g, '&gt;')
					.replace(/"/g, '&quot;')
					.replace(/'/g, '&apos;');
}

//=====================================================================================

xml.toString = function(node, ident)
{
	if (node == null)
		throw 'xml.toString: node is null';
	
	if (!ident)
		ident = '';
		
	//--- skip document node

	if (node.nodeType == Node.DOCUMENT_NODE)
	{
		node = node.documentElement;
		
		if (node == null)
			throw 'Document node has no root element';
	}
	
	var result = ident +'<'+ node.nodeName;

	//--- handle attributes

	for (var i=0; i<node.attributes.length; i++)
	{
		var name  = node.attributes[i].name;
		var value = node.getAttribute(name);
		
		result += ' '+ name +'="'+ xml.escape(value) +'"';
	}

	result += '>\n';

	//--- handle children

	result += xml.toStringCont(node, ident);
	
	return result + ident +'</'+ node.nodeName +'>\n';
}

//=====================================================================================

xml.toStringCont = function(node, ident)
{
	if (node == null)
		throw 'xml.toStringCont: node is null';
		
	if (!ident)
		ident = '';
		
	//--- skip document node

	if (node.nodeType == Node.DOCUMENT_NODE)
	{
		node = node.documentElement;
		
		if (node == null)
			throw 'Document node has no root element';
	}
	
	var result = '';
	
	if (node.childNodes == null)
	{
		//--- maybe the given node is a simple text node
		
		if (node.nodeType == Node.TEXT_NODE)
			return xml.escape(node.nodeValue);
			
		throw 'Document node has no child nodes';
	}
	
	for (var i=0; i<node.childNodes.length; i++)
	{
		var child = node.childNodes[i];
		
		if (child.nodeType == Node.ELEMENT_NODE)
			result += xml.toString(child, ident +'   ');
			
		else if (child.nodeType == Node.TEXT_NODE)
			result += xml.escape(child.nodeValue);
	}

	return result;
}

//=====================================================================================

xml.visit = function(node, callBack)
{
	if (node.nodeType == Node.DOCUMENT_NODE)
		node = node.firstChild;
	
	var array = [ node ];
	
	while (array.length != 0)
	{
		node = array.shift();		
		
		if (!callBack(node))
			return;
			
		var list = xml.children(node);
			
		for (var i=0; i<list.length; i++)
			array.push(list[i]);
	}
}

//=====================================================================================

xml.children = function(node, name)
{
	var result = [];
	
	if (node != null)
		node = node.firstChild;
		
	while (node != null)
	{
		if (node.nodeType == Node.ELEMENT_NODE)
			if (name == null || name == node.nodeName)
				result.push(node);
			
		node = node.nextSibling;
	}
	
	return result;
}

//=====================================================================================

xml.evalXPath = function(node, xpath)
{
	var result = xml.evalXPathNode(node, xpath);
	
	return (result == null) ? null : xml.textContent(result);
}

//=====================================================================================

xml.evalXPathNode = function(node, xpath)
{
	var names = xpath.split('/');
			
	for (var i=0; i<names.length; i++)
	{	
		var pathElem = xml.extractPathElem(names[i]);
		var found    = false;
				
		node = node.firstChild;
		
		while (node != null && !found)
		{
			if (node.nodeType == Node.ELEMENT_NODE && xml.evalCond(node, pathElem))
				found = true;
			else
				node = node.nextSibling;
		}
		
		if (node == null)
			return null;
	}
	
	return node;
}

//-------------------------------------------------------------------------------------

xml.extractPathElem = function(name)
{
	var res = {};
	
	res.NAME      = name;
	res.CONDITION = '';
	
	var startPos = name.indexOf('[');
	var endPos   = name.indexOf(']');
	
	if (startPos != -1)
	{
		res.NAME      = name.substring(0, startPos);
		res.CONDITION = name.substring(startPos+1, endPos);
	}
	
	return res;
}

//-------------------------------------------------------------------------------------

xml.evalCond = function(node, pathElem)
{
	var name = pathElem.NAME;
	var cond = pathElem.CONDITION;
	
	if (node.nodeName != name)
		return false;
		
	if (cond == '')
		return true;
		
	//--- handle attribute condition
	
	if (cond.startsWith('@'))
	{
		var equPos = cond.indexOf('=');
		var attr   = cond.substring(1, equPos);
		var value  = cond.substring(equPos +1);
		
		return (node.getAttribute(attr) == xml.stripQuotes(value));
	}
	
	return false;
}

//-------------------------------------------------------------------------------------

xml.stripQuotes = function(text)
{
	return text.substring(1, text.length -1);
}

//=====================================================================================

xml.textContent = function(node)
{
	//--- firefox uses this part
	
	if (node.textContent)
		return node.textContent;
		
	//--- this is an hack for IE or other browsers
		
	var result = '';
	
	for (var i=0; i<node.childNodes.length; i++)
	{
		var child = node.childNodes[i];
		
		if (child.nodeType == Node.TEXT_NODE)
			result += child.nodeValue;
			
		else if (child.nodeType == Node.ELEMENT_NODE)
			result += xml.textContent(child);
	}

	return result;
}

//=====================================================================================

xml.getElementById = function(node, id)
{
	var result = null;
	
	xml.visit(node, function(node)
	{
		if (node.getAttribute('id') != id)
			return true;
		
		result = node;
		return false;
	});
	
	return result;
}

//=====================================================================================

xml.toObject = function(node)
{
	var map = {}
	var id  = node.getAttribute('id');

	if (id != null)
		map['id'] = id;

	var list = xml.children(node);

	for (var i=0; i<list.length; i++)
	{
		var child     = list[i];
		var name      = child.nodeName;
		var childList = xml.children(child);

		if (childList.length == 0)
			map[name] = xml.textContent(child);
		else
			map[name] = xml.toObject(child);
	}

	return map;							
}

//=====================================================================================

xml.ieFix = function(node)
{
	//--- this is a fix for IE: the first node is a processing instruction 
	//--- If we use Node.ELEMENT_NODE, it seems that IE raises errors on some
	//--- pages (with the same code!!!).
	
	if (node.nodeType != Node.ELEMENT_NODE)
		return node.nextSibling;
		
	return node;
}

//=====================================================================================

xml.createElement = function(name, content)
{
//--- it seems that the sarissa's method does not work with IE
//	var doc  = Sarissa.getDomDocument();
	var doc  = document;
	var elem = doc.createElement(name);
	
	if (content != null)
		elem.appendChild(doc.createTextNode(content));

	return elem;
}

//=====================================================================================
//=== this function is needed by IE to convert a general XML tree into another one
//=== which is a real DOM tree
//=====================================================================================

xml.convert = function(node)
{
	if (node.nodeType == Node.DOCUMENT_NODE)
		node = node.documentElement;

	var elem = document.createElement(node.nodeName);
	
	//--- copy attributes
			
	for (var i=0; i<node.attributes.length; i++)
	{
		var name  = node.attributes[i].name;
		var value = node.getAttribute(name);
		
		elem.setAttribute(name, value);
	}
	
	//--- copy children
	
	for (var i=0; i<node.childNodes.length; i++)
	{
		var child = node.childNodes[i];
		
		if (child.nodeType == Node.ELEMENT_NODE)
			elem.appendChild(xml.convert(child));
			
		else if (child.nodeType == Node.TEXT_NODE)
			elem.appendChild(document.createTextNode(child.nodeValue));
	}
	
	return elem;
}

//=====================================================================================
