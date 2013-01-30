//=====================================================================================
//===
//=== View (type:csw)
//===
//=====================================================================================

csw.View = function(xmlLoader)
{
	HarvesterView.call(this);	
	
	var searchTransf = new XSLTransformer('harvesting/csw/client-search-row.xsl', xmlLoader);
	var privilTransf = new XSLTransformer('harvesting/csw/client-privil-row.xsl', xmlLoader);
	var resultTransf = new XSLTransformer('harvesting/csw/client-result-tip.xsl', xmlLoader);
	
	var loader = xmlLoader;
	var valid  = new Validator(loader);
	var shower = null;
	
	var currSearchId = 0;
	
	this.setPrefix('csw');
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

	Event.observe('csw.icon', 'change', ker.wrap(this, updateIcon));

//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function init()
{
	valid.add(
	[
		{ id:'csw.name',        type:'length',   minSize :1,  maxSize :200 },
		{ id:'csw.capabUrl',    type:'length',   minSize :1,  maxSize :200 },
		{ id:'csw.capabUrl',    type:'url' },
		{ id:'csw.username',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'csw.password',    type:'length',   minSize :0,  maxSize :200 }
	]);

	shower = new Shower('csw.useAccount', 'csw.account');
}

//=====================================================================================

function setEmpty()
{
	this.setEmptyCommon();

    $('csw.rejectDuplicateResource').checked = false;
    
	removeAllSearch();
	
	$('csw.capabUrl').value = '';
	
	var icons = $('csw.icon').options;
	
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

	hvutil.setOption(site, 'capabilitiesUrl', 'csw.capabUrl');
	hvutil.setOption(site, 'icon',            'csw.icon');
    hvutil.setOption(site, 'rejectDuplicateResource', 'csw.rejectDuplicateResource');
    
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
	
	data.CAPAB_URL = $F('csw.capabUrl');
	data.ICON      = $F('csw.icon');
	data.REJECTDUPLICATERESOURCE = $('csw.rejectDuplicateResource').checked;
	//--- retrieve search information
	
	var searchData = [];
	var searchList = xml.children($('csw.searches'));
	
	for(var i=0; i<searchList.length; i++)
	{
		var divElem = searchList[i];
		
		searchData.push(
		{
			ANY_TEXT : xml.getElementById(divElem, 'csw.anytext') .value,
			TITLE    : xml.getElementById(divElem, 'csw.title')   .value,
			ABSTRACT : xml.getElementById(divElem, 'csw.abstract').value,
			SUBJECT  : xml.getElementById(divElem, 'csw.subject') .value,
			MINSCALE : xml.getElementById(divElem, 'csw.minscale').value,
			MAXSCALE : xml.getElementById(divElem, 'csw.maxscale').value
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
	$('csw.icon').options.length = 0;
}

//=====================================================================================

function addIcon(file)
{
	gui.addToSelect('csw.icon', file, file);
}

//=====================================================================================

function updateIcon()
{
	var icon = $F('csw.icon');
	var image= $('csw.icon.image');
	
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
	new Insertion.Bottom('csw.searches', html);
	
	valid.add(
	[
		{ id:'csw.anytext',  type:'length',   minSize :0,  maxSize :200 },
		{ id:'csw.title',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'csw.abstract', type:'length',   minSize :0,  maxSize :200 },
		{ id:'csw.subject',  type:'length',   minSize :0,  maxSize :200 },
		{ id:'csw.minscale', type:'length',   minSize :0,  maxSize :200 },
		{ id:'csw.maxscale', type:'length',   minSize :0,  maxSize :200 }
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
	$('csw.searches').innerHTML = '';
	valid.removeByParent();	
}

//=====================================================================================
}

