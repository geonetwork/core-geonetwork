//=====================================================================================
//===
//=== View (type:Geonetwork)
//===
//=====================================================================================

gn20.View = function(xmlLoader)
{
	var searchTransf = new XSLTransformer('harvesting/geonet20/client-search-row.xsl', xmlLoader);
	var resultTransf = new XSLTransformer('harvesting/geonet20/client-result-tip.xsl', xmlLoader);
	
	var loader = xmlLoader;
	var valid  = new Validator(loader);
	var shower = null;
	
	//--- public methods
	
	this.init        = init;
	this.setEmpty    = setEmpty;
	this.setData     = setData;
	this.getData     = getData;
	this.isDataValid = isDataValid;
	this.getSiteId   = getSiteId;
	this.clearSiteId = clearSiteId;
	this.getResultTip= getResultTip;

	this.addEmptySearch  = addEmptySearch;
	this.addSearch       = addSearch;
	this.removeSearch    = removeSearch;
	this.removeAllSearch = removeAllSearch;
		
//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function init()
{
	valid.add(
	[
		{ id:'gn20.name',     type:'length',   minSize :1,  maxSize :200 },
		{ id:'gn20.host',     type:'length',   minSize :1,  maxSize :200 },
		{ id:'gn20.host',     type:'hostname' },
		{ id:'gn20.port',     type:'integer',  minValue:80, maxValue:65535, empty:true },
		{ id:'gn20.servlet',  type:'length',   minSize :1,  maxSize :200 },
		{ id:'gn20.servlet',  type:'alphanum' },
		{ id:'gn20.username', type:'length',   minSize :0,  maxSize :200 },
		{ id:'gn20.password', type:'length',   minSize :0,  maxSize :200 },
		
		{ id:'gn20.every.days',  type:'integer',  minValue:0, maxValue:99 },
		{ id:'gn20.every.hours', type:'integer',  minValue:0, maxValue:23 },
		{ id:'gn20.every.mins',  type:'integer',  minValue:0, maxValue:59 }
	]);

	shower = new Shower('gn20.useAccount',  'gn20.account');
}

//=====================================================================================

function setEmpty()
{
	removeAllSearch();
	
	$('gn20.name')      .value = '';	
	$('gn20.host')      .value = '';	
	$('gn20.port')      .value = '';
	$('gn20.servlet')   .value = '';
	$('gn20.useAccount').checked = true;
	$('gn20.username')  .value = '';
	$('gn20.password')  .value = '';
		
	$('gn20.oneRunOnly')  .checked = false;

	$('gn20.every.days') .value = '0';
	$('gn20.every.hours').value = '1';
	$('gn20.every.mins') .value = '30';
	
	clearSiteId();
	shower.update();
}

//=====================================================================================

function setData(node)
{
	var site     = node.getElementsByTagName('site')    [0];
	var searches = node.getElementsByTagName('searches')[0];
	var options  = node.getElementsByTagName('options') [0];

	var searchesList = searches.getElementsByTagName('search');
	
	$('gn20.name').value = node.getAttribute('name');
	
	hvutil.setOption(site, 'host',     'gn20.host');
	hvutil.setOption(site, 'port',     'gn20.port');
	hvutil.setOption(site, 'servlet',  'gn20.servlet');
	hvutil.setOption(site, 'use',      'gn20.useAccount');
	hvutil.setOption(site, 'username', 'gn20.username');
	hvutil.setOption(site, 'password', 'gn20.password');
	
	//--- add search entries
	
	removeAllSearch();
	
	for (var i=0; i<searchesList.length; i++)
		addSearch(searchesList[i]);
	
	//--- setup other stuff
	
	hvutil.setOption(options, 'oneRunOnly',  'gn20.oneRunOnly');

	var every = new Every(hvutil.find(options, 'every'));
	
	$('gn20.every.days') .value = every.days;
	$('gn20.every.hours').value = every.hours;
	$('gn20.every.mins') .value = every.mins;
	
	clearSiteId();
	shower.update();
}

//=====================================================================================

function getData()
{
	var days  = $('gn20.every.days') .value;
	var hours = $('gn20.every.hours').value;
	var mins  = $('gn20.every.mins') .value;
	
	var data =
	{
		//--- site
		NAME    : $('gn20.name')   .value,
		HOST    : $('gn20.host')   .value,
		PORT    : $('gn20.port')   .value,
		SERVLET : $('gn20.servlet').value,
	
		USE_ACCOUNT: $('gn20.useAccount').checked,
		USERNAME   : $('gn20.username')  .value,
		PASSWORD   : $('gn20.password')  .value,
	
		//--- options		
		EVERY         : Every.build(days, hours, mins),
		ONE_RUN_ONLY  : $('gn20.oneRunOnly')  .checked
	}
	
	//--- retrieve search information
	
	var searchData = [];
	var searchList = $('gn20.searches').childNodes;
	
	for(var i=0; i<searchList.length; i++)
		if (searchList[i].nodeType == Node.ELEMENT_NODE)
		{
			var divElem = searchList[i];
			
			searchData.push(
			{
				TEXT     : xml.getElementById(divElem, 'gn20.text')    .value,
				TITLE    : xml.getElementById(divElem, 'gn20.title')   .value,
				ABSTRACT : xml.getElementById(divElem, 'gn20.abstract').value,
				KEYWORDS : xml.getElementById(divElem, 'gn20.keywords').value,		
				DIGITAL  : xml.getElementById(divElem, 'gn20.digital') .checked,
				HARDCOPY : xml.getElementById(divElem, 'gn20.hardcopy').checked,
				SITE_ID  : divElem.getAttribute('id').substring(5) //---skip 'gn20.' prefix
			});
		}
	
	data.SEARCH_LIST = searchData;
	
	return data;
}

//=====================================================================================

function isDataValid()
{
	if (!valid.validate())
		return false;
		
	var days  = $('gn20.every.days') .value;
	var hours = $('gn20.every.hours').value;
	var mins  = $('gn20.every.mins') .value;
	
	if (Every.build(days, hours, mins) == 0)
	{
		alert(loader.getText('everyZero'));
		return false;
	}
		
	return true;
}

//=====================================================================================
//=== SiteId methods
//=====================================================================================

function getSiteId() 
{ 
	return $F('gn20.siteId');
}

//=====================================================================================

function clearSiteId() 
{ 
	$('gn20.siteId').value = '';
}

//=====================================================================================
//=== Search methods
//=====================================================================================

function addEmptySearch(siteId)
{
	var doc = Sarissa.getDomDocument();
	
	var xmlSearch = doc.createElement('search');
	var xmlSiteId = doc.createElement('siteId');
	
	doc.appendChild(xmlSearch);	
	xmlSearch.appendChild(xmlSiteId);
	xmlSiteId.appendChild(doc.createTextNode(siteId));
	
	addSearch(xmlSearch);
}

//=====================================================================================

function addSearch(xmlSearch)
{
	var xslRes = searchTransf.transform(xmlSearch);
	var siteId = xslRes.getAttribute('id');
	var div    = xml.getElementById($('gn20.searches'), siteId);

	//--- we must avoid adding more searches on the same site-id

	if (div != null)
		return;

	//--- add the new search in list
	new Insertion.Bottom('gn20.searches', xml.toString(xslRes));
	
	valid.add(
	[
		{ id:'gn20.text',     type:'length',   minSize :0,  maxSize :200 },
		{ id:'gn20.title',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'gn20.abstract', type:'length',   minSize :0,  maxSize :200 },
		{ id:'gn20.keywords', type:'length',   minSize :0,  maxSize :200 }
	], siteId);
}

//=====================================================================================

function removeSearch(siteId)
{
	valid.removeByParent(siteId);
	Element.remove(siteId);
}

//=====================================================================================

function removeAllSearch()
{
	$('gn20.searches').innerHTML = '';
	valid.removeByParent();	
}

//=====================================================================================

function getResultTip(node)
{
	return xml.toString(resultTransf.transform(node));
}

//=====================================================================================
}

