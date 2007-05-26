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

	//--- load stylesheet

	ker.loadMan.acquire();
	ker.loadURL(Env.url +'/xsl/'+ stylesheet, ker.wrap(this, function(t)
	{
		xslProc = new XSLTProcessor();
		xslProc.importStylesheet(t.responseXML);
		ker.loadMan.release();
	}));

//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

this.transform = function(node)
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
	
	var doc = xslProc.transformToDocument(doc);
	
	return doc.firstChild;
}

//=====================================================================================
//===
//=== Private methods
//===
//=====================================================================================

function createEnv(doc)
{
	var env      = doc.createElement('env');
	
	var elUrl    = doc.createElement('url');
	var elLocUrl = doc.createElement('locUrl');
	var elLocSer = doc.createElement('locService');
	
	env.appendChild(elUrl);
	env.appendChild(elLocUrl);
	env.appendChild(elLocSer);
	
	elUrl   .appendChild(doc.createTextNode(Env.url));
	elLocUrl.appendChild(doc.createTextNode(Env.locUrl));
	elLocSer.appendChild(doc.createTextNode(Env.locService));
	
	return env;
}

//=====================================================================================
}

