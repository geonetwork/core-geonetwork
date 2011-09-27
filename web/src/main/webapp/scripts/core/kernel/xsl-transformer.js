//=====================================================================================
//===
//=== Applies an XSL transformation to an input XML and uses localized strings
//===
//=== Needs:	sarissa.js
//=== 			kernel.js
//===
//=====================================================================================

function XSLTransformer(stylesheet, xmlLoader)
{
	var loader  = xmlLoader;
	var xslProc = null
	var xslDoc  = null;
	var xslSheet= stylesheet;
	var error = 0;
	
	//--- load stylesheet

	ker.loadMan.acquire();
	ker.loadURL(Env.url +'/xsl/'+ stylesheet, importStyleSheet);
	// try again if we didn't get it
	if (xslDoc == null) ker.loadURL(Env.url +'/xsl/'+ stylesheet, importStyleSheet);
	ker.loadMan.release();
	
	
function importStyleSheet(t)
{
	if (t.responseXML != undefined) {
		try
		{
					
			xslDoc = t.responseXML;
			
			if (window.ActiveXObject == null)
			{
				xslProc = new XSLTProcessor();
				xslProc.importStylesheet(xslDoc);
			}
		}
		catch(e)
		{
			alert('error on : '+Env.url +'/xsl/'+ stylesheet+'\n'+t.responseText+" "+t.responseXML+" "+e);
		}
	}
}

//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

this.transform = function(node)
{
	var doc = buildDocument(node);
	
	if (window.ActiveXObject)
	{
		try
		{
			var text   = doc.transformNode(xslDoc);
			var xmlDoc = new ActiveXObject('Microsoft.XMLDOM');
			
			xmlDoc.async = 'false';
		
			if (xmlDoc.loadXML(text) == false)
				throw 'Parse error for:\n\n'+text;
		
			//--- convert XML result into a DOM result
			return xml.convert(xmlDoc);
		}
		catch(e)
		{
			throw 'Cannot transform with stylesheet.\nFile : '+ xslSheet;
		}
	}
	else
		return xslProc.transformToDocument(doc);
}

//=====================================================================================

this.transformToText = function(node)
{
	var doc = buildDocument(node);
	
	if (window.ActiveXObject)
	{
		try
		{
			return doc.transformNode(xslDoc);
		}
		catch(e)
		{
			throw 'Cannot transform with stylesheet.\nFile : '+ xslSheet;
		}
	}
	else
		return xml.toString(xslProc.transformToDocument(doc));
}

//=====================================================================================
//===
//=== Private methods
//===
//=====================================================================================

function buildDocument(node)
{
	var strings = (!loader) 
						? Sarissa.getDomDocument().createElement('strings')
						: loader.getNode();

	var doc  = Sarissa.getDomDocument();
	var root = doc.createElement('root');
	
	doc .appendChild(root);
	root.appendChild(node.cloneNode(true));
	root.appendChild(createEnv(doc));
	root.appendChild(strings.cloneNode(true));
	
	return doc;
}

//=====================================================================================

function createEnv(doc)
{
	var env      = doc.createElement('env');
	
	var elUrl    = doc.createElement('url');
	var elLocUrl = doc.createElement('locUrl');
	var elLocSer = doc.createElement('locService');
	var elLang   = doc.createElement('language');
	
	env.appendChild(elUrl);
	env.appendChild(elLocUrl);
	env.appendChild(elLocSer);
	env.appendChild(elLang);
	
	elUrl   .appendChild(doc.createTextNode(Env.url));
	elLocUrl.appendChild(doc.createTextNode(Env.locUrl));
	elLocSer.appendChild(doc.createTextNode(Env.locService));
	elLang  .appendChild(doc.createTextNode(Env.lang));
	
	return env;
}

//=====================================================================================
}

