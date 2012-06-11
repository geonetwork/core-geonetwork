//=====================================================================================
//===
//=== View (type:Geonetwork 2.0.X)
//===
//=====================================================================================

gn20.View = function(xmlLoader)
{
	HarvesterView.call(this);	
	
	var searchTransf = new XSLTransformer('harvesting/geonet20/client-search-row.xsl', xmlLoader);
	var resultTransf = new XSLTransformer('harvesting/geonet20/client-result-tip.xsl', xmlLoader);
	
	var loader = xmlLoader;
	var valid  = new Validator(loader);
	var shower = null;
	
	this.setPrefix('gn20');
	this.setResultTransf(resultTransf);
	
	//--- public methods
	
	this.init        = init;
	this.setEmpty    = setEmpty;
	this.setData     = setData;
	this.getData     = getData;
	this.isDataValid = isDataValid;
	this.getSiteId   = getSiteId;
	this.clearSiteId = clearSiteId;

	this.addEmptySearch  = addEmptySearch;
	this.addSearch       = addSearch;
	this.removeSearch    = removeSearch;
	this.removeAllSearch = removeAllSearch;
		
	this.removeAllGroupRows = function(){}
	this.unselectCategories = function(){};
			
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
		{ id:'gn20.host',     type:'url' },
		// { id:'gn20.servlet',  type:'alphanum' }, // Does not work when servlet is mapped to root or subdirectory
		{ id:'gn20.username', type:'length',   minSize :0,  maxSize :200 },
		{ id:'gn20.password', type:'length',   minSize :0,  maxSize :200 }
	]);

	shower = new Shower('gn20.useAccount',  'gn20.account');
}

//=====================================================================================

function setEmpty()
{
	this.setEmptyCommon();
	
	removeAllSearch();
	
	$('gn20.host')      .value = '';
		
	clearSiteId();
	shower.update();
}

//=====================================================================================

function setData(node)
{
	this.setDataCommon(node);

	var site     = node.getElementsByTagName('site')    [0];
	var searches = node.getElementsByTagName('searches')[0];

	hvutil.setOption(site, 'host',    'gn20.host');
	
	//--- add search entries

	removeAllSearch();
	
	var list = searches.getElementsByTagName('search');

	for (var i=0; i<list.length; i++)
		addSearch(list[i]);

	//--- setup other stuff	
	
	clearSiteId();
	shower.update();
}

//=====================================================================================

function getData()
{
	var data = this.getDataCommon();
	
	data.HOST    = $F('gn20.host');
	
	//--- retrieve search information
	
	var searchData = [];
	var searchList = xml.children($('gn20.searches'));
	
	for(var i=0; i<searchList.length; i++)
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
			SITE_ID  : divElem.getAttribute('id')
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
		
	return this.isDataValidCommon();
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
	var siteId = xml.evalXPath(xmlSearch, 'siteId');
	var html   = searchTransf.transformToText(xmlSearch);
	var div    = xml.getElementById($('gn20.searches'), siteId);

	//--- we must avoid adding more searches on the same site-id

	if (div != null)
		return;
	
	//--- add the new search in list
	new Insertion.Bottom('gn20.searches', html);
	
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
}

