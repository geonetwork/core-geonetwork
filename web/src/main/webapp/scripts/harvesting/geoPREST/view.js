//=====================================================================================
//===
//=== View (type:geoPREST)
//===
//=====================================================================================

geoPREST.View = function(xmlLoader)
{
	HarvesterView.call(this);	
	
	var searchTransf = new XSLTransformer('harvesting/geoPREST/client-search-row.xsl', xmlLoader);
	var privilTransf = new XSLTransformer('harvesting/geoPREST/client-privil-row.xsl', xmlLoader);
	var resultTransf = new XSLTransformer('harvesting/geoPREST/client-result-tip.xsl', xmlLoader);
	
	var loader = xmlLoader;
	var valid  = new Validator(loader);
	var shower = null;
	
	var currSearchId = 0;
	
	this.setPrefix('geoPREST');
	this.setPrivilTransf(privilTransf);
	this.setResultTransf(resultTransf);
	
	//--- public methods
	
	this.init           = init;
	this.setEmpty       = setEmpty;
	this.setData        = setData;
	this.getData        = getData;
	this.isDataValid    = isDataValid;
	this.clearIcons     = clearIcons;
	this.addIcon        = addIcon;		
	this.addEmptySearch = addEmptySearch;
	this.removeSearch   = removeSearch;

	Event.observe('geoPREST.icon', 'change', ker.wrap(this, updateIcon));

//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function init()
{
	valid.add(
	[
		{ id:'geoPREST.name',        type:'length',   minSize :1,  maxSize :200 },
		{ id:'geoPREST.baseUrl',    type:'length',   minSize :1,  maxSize :200 },
		{ id:'geoPREST.baseUrl',    type:'url' },
		{ id:'geoPREST.username',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'geoPREST.password',    type:'length',   minSize :0,  maxSize :200 }
	]);

	shower = new Shower('geoPREST.useAccount', 'geoPREST.account');
}

//=====================================================================================

function setEmpty()
{
	this.setEmptyCommon();
	
	removeAllSearch();
	
	$('geoPREST.baseUrl').value = '';
	
	var icons = $('geoPREST.icon').options;
	
	for (var i=0; i<icons.length; i++)
		if (icons[i].value == 'default.gif')
		{
			icons[i].selected = true;
			break;
		}

	shower.update();
	updateIcon();
}

//=====================================================================================

function setData(node)
{
	this.setDataCommon(node);

	var site     = node.getElementsByTagName('site')    [0];
	var searches = node.getElementsByTagName('searches')[0];

	hvutil.setOption(site, 'baseUrl', 'geoPREST.baseUrl');
	hvutil.setOption(site, 'icon',    'geoPREST.icon');
	
	//--- add search entries
	
	var list = searches.getElementsByTagName('search');
	
	removeAllSearch();
	
	for (var i=0; i<list.length; i++)
		addSearch(list[i]);

	//--- add privileges entries
	
	this.removeAllGroupRows();
	this.addGroupRows(node);
	
	//--- set categories

	this.unselectCategories();
	this.selectCategories(node);	
	
	shower.update();
	updateIcon();
}

//=====================================================================================

function getData()
{
	var data = this.getDataCommon();
	
	data.BASE_URL = $F('geoPREST.baseUrl');
	data.ICON     = $F('geoPREST.icon');
	
	//--- retrieve search information
	
	var searchData = [];
	var searchList = xml.children($('geoPREST.searches'));
	
	for(var i=0; i<searchList.length; i++)
	{
		var divElem = searchList[i];
		
		searchData.push(
		{
			ANY_TEXT : xml.getElementById(divElem, 'geoPREST.anytext') .value
		});
	}
	
	data.SEARCH_LIST = searchData;
	
	//--- retrieve privileges and categories information
	
	data.PRIVILEGES = this.getPrivileges();
	data.CATEGORIES = this.getSelectedCategories();
		
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

function clearIcons() 
{ 
	$('geoPREST.icon').options.length = 0;
}

//=====================================================================================

function addIcon(file)
{
	gui.addToSelect('geoPREST.icon', file, file);
}

//=====================================================================================

function updateIcon()
{
	var icon = $F('geoPREST.icon');
	var image= $('geoPREST.icon.image');
	
	image.setAttribute('src', Env.url +'/images/harvesting/'+icon);
}

//=====================================================================================
//=== Search methods
//=====================================================================================

function addEmptySearch()
{
	var doc    = Sarissa.getDomDocument();	
	var search = doc.createElement('search');
	
	addSearch(search);
}

//=====================================================================================

function addSearch(search)
{
	var id = ''+ currSearchId++;
	search.setAttribute('id', id);
	
	var html = searchTransf.transformToText(search);

	//--- add the new search in list
	new Insertion.Bottom('geoPREST.searches', html);
	
	valid.add(
	[
		{ id:'geoPREST.anytext',  type:'length',   minSize :0,  maxSize :200 }
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
	$('geoPREST.searches').innerHTML = '';
	valid.removeByParent();	
}

//=====================================================================================
}

