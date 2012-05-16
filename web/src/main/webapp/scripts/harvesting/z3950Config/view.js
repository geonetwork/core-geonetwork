//=====================================================================================
//===
//=== View (type:z3950Config)
//===
//=====================================================================================

Z3950Config.View = function(xmlLoader)
{
	HarvesterView.call(this);	

	var searchTransf = new XSLTransformer('harvesting/z3950Config/client-search-row.xsl', xmlLoader);
	var resultTransf = new XSLTransformer('harvesting/z3950Config/client-result-tip.xsl', xmlLoader);
	
	var loader = xmlLoader;
	var valid  = new Validator(loader);
	var shower = null;
	
	var currSearchId = 0;
		
	this.setPrefix('z3950Config');
	this.setResultTransf(resultTransf);
	
	//--- public methods
	
	this.init       = init;
	this.setEmpty   = setEmpty;
	this.setData    = setData;
	this.getData    = getData;
	this.isDataValid= isDataValid;

	this.addEmptySearch  = addEmptySearch;
	this.addSearch       = addSearch;
	this.removeSearch    = removeSearch;
	this.removeAllSearch = removeAllSearch;

	this.getHostData = getHostData;

//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function init()
{
	valid.add(
	[
		{ id:'z3950Config.name',     type:'length',   minSize :1,  maxSize :200 },
		{ id:'z3950Config.host',     type:'length',   minSize :1,  maxSize :200 },
		{ id:'z3950Config.host',     type:'hostname' },
		{ id:'z3950Config.port',     type:'integer', minValue :80, maxValue :65535, empty:true },

		{ id:'z3950Config.username', type:'length',   minSize :0,  maxSize :200 },
		{ id:'z3950Config.password', type:'length',   minSize :0,  maxSize :200 }
	]);

	gui.setupTooltips(loader.getNode('tips'));
}

//=====================================================================================

function setEmpty()
{
	this.setEmptyCommon();

	$('z3950Config.clearConfig').checked = false;

	removeAllSearch();

	$('z3950Config.host')      .value = '';
	$('z3950Config.port')      .value = '';
	
}

//=====================================================================================

function setData(node)
{
	this.setDataCommon(node);

	var site     = node.getElementsByTagName('site')            [0];
	var searches = node.getElementsByTagName('searches')        [0];
	var options  = node.getElementsByTagName('options')[0];

	hvutil.setOption(site, 'host',    'z3950Config.host');
	hvutil.setOption(site, 'port',    'z3950Config.port');

	hvutil.setOption(options, 'clearConfig',     'z3950Config.clearConfig');

	//--- add search entries
	
	var list = searches.getElementsByTagName('search');
	
	removeAllSearch();
	
	for (var i=0; i<list.length; i++)
		addSearch(list[i]);
	
}

//=====================================================================================

function getData()
{
	var data = this.getDataCommon();

	data.CLEARCONFIG = $('z3950Config.clearConfig').checked;

	data.HOST    = $F('z3950Config.host');
	data.PORT    = $F('z3950Config.port');

	//--- retrieve search information
	
	var searchData = [];
	var searchList = xml.children($('z3950Config.searches'));
	
	for(var i=0; i<searchList.length; i++)
	{
		var divElem    = searchList[i];
		var sourceElem = xml.getElementById(divElem, 'z3950Config.source')
	
		searchData.push(
		{
			TEXT        : xml.getElementById(divElem, 'z3950Config.text')    .value,
			TITLE       : xml.getElementById(divElem, 'z3950Config.title')   .value,
			ABSTRACT    : xml.getElementById(divElem, 'z3950Config.abstract').value,
			KEYWORDS    : xml.getElementById(divElem, 'z3950Config.keywords').value,
			CATEGORY    : xml.getElementById(divElem, 'z3950Config.category').value
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
//=== Search methods
//=====================================================================================

function addEmptySearch()
{
	var doc    = Sarissa.getDomDocument();
	var search = doc.createElement('search');
	
	doc.appendChild(search);		
	addSearch(search);
}

//=====================================================================================

function addSearch(search)
{
	var id = ''+ currSearchId++;
	search.setAttribute('id', id);
	
	var html = searchTransf.transformToText(search);
	
	//--- add the new search in list
	new Insertion.Bottom('z3950Config.searches', html);
	
	valid.add(
	[
		{ id:'z3950Config.text',     type:'length',   minSize :0,  maxSize :200 },
		{ id:'z3950Config.title',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'z3950Config.abstract', type:'length',   minSize :0,  maxSize :200 },
		{ id:'z3950Config.keywords', type:'length',   minSize :0,  maxSize :200 },
		{ id:'z3950Config.category', type:'length',   minSize :0,  maxSize :200 }
	], id);
}

//=====================================================================================

function removeSearch(id)
{
	valid.removeByParent(id);
	Element.remove(id);
}

//=====================================================================================

function removeAllSearch()
{
	$('z3950Config.searches').innerHTML = '';
	valid.removeByParent();	
}

//=====================================================================================

function getHostData()
{
	var data = 
	{
		HOST       : $F('z3950Config.host'),
		PORT       : $F('z3950Config.port'),
		USERNAME   : $F('z3950Config.username'),
		PASSWORD   : $F('z3950Config.password'),
		USE_ACCOUNT: $('z3950Config.useAccount').checked
	};
	
	return data;
}

//=====================================================================================

}

