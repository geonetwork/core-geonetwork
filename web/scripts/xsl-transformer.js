//=====================================================================================
//===
//=== XSLTransformer
//===
//=== Applies an XSL transformation to an input XML and uses localized strings
//===
//=== Needs : geonetwork-ajax.js, sarissa.js
//===
//=====================================================================================

function XSLTransformer(stylesheet, xmlLoader)
{
	this.buffer = new Array();
	
	var _this = this;

	//--- load stylesheet

	new URLLoader(Env.url +'/xsl/'+ stylesheet, function(t) 
	{
		_this.stylesheet = t.responseXML;
		_this.processor  = new XSLTProcessor();
		_this.processor.importStylesheet(t.responseXML);

		_this.flush();
	});

	if (!xmlLoader)
		this.strings = Sarissa.getDomDocument().createElement('strings');
	else
	{
		if (xmlLoader.isLoaded())
			this.strings = xmlLoader.strings;
		else
			xmlLoader.addListener(function()
			{
				_this.strings = xmlLoader.strings;
				_this.flush();
			});
	}
}

//=====================================================================================

XSLTransformer.prototype.transform = function(xml, callBack)
{
	if (!this.processor || !this.strings)
		this.buffer.push(xml, callBack);	
	else
	{
		var doc  = Sarissa.getDomDocument();
		var root = doc.createElement('root');
		
		doc.appendChild(root);
		root.appendChild(xml.cloneNode(true));
		root.appendChild(this.createEnv(doc));
		root.appendChild(this.strings.cloneNode(true));
		
		var doc = this.processor.transformToDocument(doc);
		
		callBack(doc.firstChild);
	}
}

//=====================================================================================
//=== Private methods
//=====================================================================================

XSLTransformer.prototype.flush = function()
{
	while (this.buffer.length != 0)
	{
		var callBack = _this.buffer.pop();
		var xml      = _this.buffer.pop();

		this.transform(xml, callBack);
	}
}

//=====================================================================================

XSLTransformer.prototype.createEnv = function(doc)
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
