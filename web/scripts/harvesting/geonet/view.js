//=====================================================================================
//===
//=== View (type:Geonetwork)
//===
//=====================================================================================

gn.View = function(xmlLoader)
{
	var searchTransf = new XSLTransformer('harvesting/geonet/client-search-row.xsl', xmlLoader);
	var resultTransf = new XSLTransformer('harvesting/geonet/client-result-tip.xsl', xmlLoader);
	
	var loader = xmlLoader;
	var valid  = new Validator(loader);
	var shower = null;
	
	//--- public methods
	
	this.init       = init;
	this.setEmpty   = setEmpty;
	this.setData    = setData;
	this.getData    = getData;
	this.isDataValid= isDataValid;

	this.getSiteName = getSiteName;
	this.getSiteId   = getSiteId;
	this.clearSiteId = clearSiteId;
	this.addSiteId   = addSiteId;

	this.addEmptySearch  = addEmptySearch;
	this.addSearch       = addSearch;
	this.removeSearch    = removeSearch;
	this.removeAllSearch = removeAllSearch;

	this.getHostData = getHostData;
	this.getResultTip= getResultTip;
		
//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function init()
{
	valid.add(
	[
		{ id:'gn.name',     type:'length',   minSize :1,  maxSize :200 },
		{ id:'gn.host',     type:'length',   minSize :1,  maxSize :200 },
		{ id:'gn.host',     type:'hostname' },
		{ id:'gn.port',     type:'integer',  minValue:80, maxValue:65535, empty:true },
		{ id:'gn.servlet',  type:'length',   minSize :1,  maxSize :200 },
		{ id:'gn.servlet',  type:'alphanum' },
		{ id:'gn.username', type:'length',   minSize :0,  maxSize :200 },
		{ id:'gn.password', type:'length',   minSize :0,  maxSize :200 },
		
		{ id:'gn.every.days',  type:'integer',  minValue:0, maxValue:99 },
		{ id:'gn.every.hours', type:'integer',  minValue:0, maxValue:23 },
		{ id:'gn.every.mins',  type:'integer',  minValue:0, maxValue:59 }
	]);

	shower = new Shower('gn.useAccount',  'gn.account');
}

//=====================================================================================

function setEmpty()
{
	removeAllSearch();
	
	$('gn.name')      .value = '';	
	$('gn.host')      .value = '';	
	$('gn.port')      .value = '';
	$('gn.servlet')   .value = '';
	$('gn.useAccount').checked = true;
	$('gn.username')  .value = '';
	$('gn.password')  .value = '';
		
	$('gn.createGroups').checked = true;
	$('gn.createCateg') .checked = true;
	$('gn.oneRunOnly')  .checked = false;

	$('gn.every.days') .value = '0';
	$('gn.every.hours').value = '1';
	$('gn.every.mins') .value = '30';
	
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
	
	$('gn.name').value = node.getAttribute('name');
	
	hvutil.setOption(site, 'host',     'gn.host');
	hvutil.setOption(site, 'port',     'gn.port');
	hvutil.setOption(site, 'servlet',  'gn.servlet');
	hvutil.setOption(site, 'use',      'gn.useAccount');
	hvutil.setOption(site, 'username', 'gn.username');
	hvutil.setOption(site, 'password', 'gn.password');
	
	//--- add search entries
	
	removeAllSearch();
	
	for (var i=0; i<searchesList.length; i++)
		addSearch(searchesList[i]);
	
	//--- setup other stuff
	
	hvutil.setOption(options, 'createGroups','gn.createGroups');
	hvutil.setOption(options, 'createCateg', 'gn.createCateg');
	hvutil.setOption(options, 'oneRunOnly',  'gn.oneRunOnly');

	var every = new Every(hvutil.find(options, 'every'));
	
	$('gn.every.days') .value = every.days;
	$('gn.every.hours').value = every.hours;
	$('gn.every.mins') .value = every.mins;
	
	clearSiteId();
	shower.update();
}

//=====================================================================================

function getData()
{
	var days  = $('gn.every.days') .value;
	var hours = $('gn.every.hours').value;
	var mins  = $('gn.every.mins') .value;
	
	var data =
	{
		//--- site
		NAME    : $('gn.name')   .value,
		HOST    : $('gn.host')   .value,
		PORT    : $('gn.port')   .value,
		SERVLET : $('gn.servlet').value,
	
		USE_ACCOUNT: $('gn.useAccount').checked,
		USERNAME   : $('gn.username')  .value,
		PASSWORD   : $('gn.password')  .value,
	
		//--- options		
		EVERY         : Every.build(days, hours, mins),
		CREATE_GROUPS : $('gn.createGroups').checked,
		CREATE_CATEG  : $('gn.createCateg') .checked,
		ONE_RUN_ONLY  : $('gn.oneRunOnly')  .checked
	}
	
	//--- retrieve search information
	
	var searchData = [];
	var searchList = $('gn.searches').childNodes;
	
	for(var i=0; i<searchList.length; i++)
		if (searchList[i].nodeType == Node.ELEMENT_NODE)
		{
			var divElem = searchList[i];
			
			searchData.push(
			{
				TEXT     : xml.getElementById(divElem, 'gn.text')    .value,
				TITLE    : xml.getElementById(divElem, 'gn.title')   .value,
				ABSTRACT : xml.getElementById(divElem, 'gn.abstract').value,
				KEYWORDS : xml.getElementById(divElem, 'gn.keywords').value,		
				DIGITAL  : xml.getElementById(divElem, 'gn.digital') .checked,
				HARDCOPY : xml.getElementById(divElem, 'gn.hardcopy').checked,
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
		
	var days  = $('gn.every.days') .value;
	var hours = $('gn.every.hours').value;
	var mins  = $('gn.every.mins') .value;
	
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

function getSiteName()
{ 
	var ctrl = $('gn.siteId');
	
	if (ctrl.options.length == 0)
		return null;
		
	return xml.textContent(ctrl.options[ctrl.selectedIndex]);
}

//=====================================================================================

function getSiteId() 
{ 
	var ctrl = $('gn.siteId');
	
	if (ctrl.options.length == 0)
		return null;
		
	return ctrl.options[ctrl.selectedIndex].value;
}

//=====================================================================================

function clearSiteId() { return $('gn.siteId').options.length = 0; }

//=====================================================================================

function addSiteId(id, label)
{
	var html='<option value="'+ id +'">'+ xml.escape(label) +'</option>';
	new Insertion.Bottom('gn.siteId', html);
}

//=====================================================================================
//=== Search methods
//=====================================================================================

function addEmptySearch(siteId, siteName)
{
	var doc = Sarissa.getDomDocument();
	
	var xmlSearch = doc.createElement('search');
	var xmlName   = doc.createElement('siteName');
	var xmlSiteId = doc.createElement('siteId');
	
	doc.appendChild(xmlSearch);	
	xmlSearch.appendChild(xmlName);	
	xmlSearch.appendChild(xmlSiteId);
	
	xmlName  .appendChild(doc.createTextNode(siteName));
	xmlSiteId.appendChild(doc.createTextNode(siteId));
	
	addSearch(xmlSearch);
}

//=====================================================================================

function addSearch(xmlSearch)
{
	var xslRes = searchTransf.transform(xmlSearch);
	var siteId = xslRes.getAttribute('id');
	var div    = xml.getElementById($('gn.searches'), siteId);

	//--- we must avoid adding more searches on the same site-id

	if (div != null)
		return;

	//--- add the new search in list
	new Insertion.Bottom('gn.searches', xml.toString(xslRes));
	
	valid.add(
	[
		{ id:'gn.text',     type:'length',   minSize :0,  maxSize :200 },
		{ id:'gn.title',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'gn.abstract', type:'length',   minSize :0,  maxSize :200 },
		{ id:'gn.keywords', type:'length',   minSize :0,  maxSize :200 }
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
	$('gn.searches').innerHTML = '';
	valid.removeByParent();	
}

//=====================================================================================

function getHostData()
{
	var data = 
	{
		HOST :    $('gn.host')   .value,
		PORT :    $('gn.port')   .value,
		SERVLET : $('gn.servlet').value
	};
	
	return data;
}

//=====================================================================================

function getResultTip(node)
{
	return xml.toString(resultTransf.transform(node));
}

//=====================================================================================
}

