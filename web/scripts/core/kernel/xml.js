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
	if (text == '')
		return text;
		
	return text	.replace(/&/g, "&amp;")
					.replace(/</g, "&lt;")
					.replace(/>/g, "&gt;")
					.replace(/"/g, "&quot;")
					.replace(/'/g, "&apos;");
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
		node = node.firstChild;

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
		node = node.firstChild;
	
	var result = '';
	
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

xml.children = function(node)
{
	var result = [];
	
	if (node != null)
		node = node.firstChild;
		
	while (node != null)
	{
		if (node.nodeType == Node.ELEMENT_NODE)
			result.push(node);
			
		node = node.nextSibling;
	}
	
	return result;
}

//=====================================================================================

xml.evalXPath = function(node, xpath)
{
	var names = xpath.split('/');
			
	for (var i=0; i<names.length; i++)
	{	
		node = node.firstChild;
		
		var found = false;
		
		while (node != null && !found)
		{
			if (node.nodeType == Node.ELEMENT_NODE && node.nodeName == names[i])
				found = true;
			else
				node = node.nextSibling;
		}
		
		if (node == null)
			return null;
	
	}
	
	return xml.textContent(node);
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
