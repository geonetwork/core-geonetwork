//=====================================================================================
//===
//=== Loads an XML file from the server. Used for example to load localized strings 
//=== 
//=== Needs:	xml.js
//===				kernel.js
//=====================================================================================

function XMLLoader(file)
{
	var strings = null;

	ker.loadMan.acquire();
	ker.loadURL(file, ker.wrap(this, function(t)
	{
		strings = xml.ieFix(t.responseXML.firstChild);
		ker.loadMan.release();
	}));

//=====================================================================================

this.getText = function(name)
{
	var list = strings.getElementsByTagName(name);
	
	if (list.length == 0)	return '*not-found:'+ name +'*';
		else						return xml.textContent(list[0]);
}

//=====================================================================================

this.getNode = function(name)
{
	if (name)
		return strings.getElementsByTagName(name)[0];
	
	return strings;
}

//=====================================================================================

this.eval = function(xpath)
{
	return xml.evalXPath(strings, xpath);
}

//=====================================================================================

this.evalNode = function(xpath)
{
	return xml.evalXPathNode(strings, xpath);
}

//=====================================================================================
}