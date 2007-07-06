//=====================================================================================
//===
//=== View (type:z3950)
//===
//=====================================================================================

z3950.View = function(xmlLoader)
{
	HarvesterView.call(this);	

//	var searchTransf = new XSLTransformer('harvesting/csw/client-search-row.xsl', xmlLoader);
	var privilTransf = new XSLTransformer('harvesting/z3950/client-privil-row.xsl', xmlLoader);
	var resultTransf = new XSLTransformer('harvesting/z3950/client-result-tip.xsl', xmlLoader);
	
	var loader = xmlLoader;
	var valid  = new Validator(loader);
	var shower = null;
	
	var currSearchId = 0;
	
	this.setPrefix('z39');
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
//	this.addEmptySearch = addEmptySearch;
//	this.removeSearch   = removeSearch;

	Event.observe('z39.icon', 'change', ker.wrap(this, updateIcon));

//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function init()
{
	valid.add(
	[
		{ id:'z39.name',        type:'length',   minSize :1,  maxSize :200 },
		{ id:'z39.username',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'z39.password',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'z39.every.days',  type:'integer',  minValue:0, maxValue:99 },
		{ id:'z39.every.hours', type:'integer',  minValue:0, maxValue:23 },
		{ id:'z39.every.mins',  type:'integer',  minValue:0, maxValue:59 }
	]);

	shower = new Shower('z39.useAccount', 'z39.account');
}

//=====================================================================================

function setEmpty()
{
	this.setEmptyCommon();
	
//	removeAllSearch();
	
//	$('csw.capabUrl').value = '';
	
	var icons = $('z39.icon').options;
	
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

//	hvutil.setOption(site, 'capabilitiesUrl', 'csw.capabUrl');
	hvutil.setOption(site, 'icon',            'z39.icon');
	
	//--- add search entries
	
//	var list = searches.getElementsByTagName('search');
	
//	removeAllSearch();
	
//	for (var i=0; i<list.length; i++)
//		addSearch(list[i]);

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
	
//	data.CAPAB_URL = $F('csw.capabUrl');
	data.ICON      = $F('z39.icon');
	
	//--- retrieve search information
	
//	var searchData = [];
//	var searchList = xml.children($('csw.searches'));
	
//	for(var i=0; i<searchList.length; i++)
//	{
//		var divElem = searchList[i];
//		
//		searchData.push(
//		{
//			ANY_TEXT : xml.getElementById(divElem, 'csw.anytext') .value,
//			TITLE    : xml.getElementById(divElem, 'csw.title')   .value,
//			ABSTRACT : xml.getElementById(divElem, 'csw.abstract').value,
//			SUBJECT  : xml.getElementById(divElem, 'csw.subject') .value,		
//		});
//	}
//	
//	data.SEARCH_LIST = searchData;
	
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
	$('z39.icon').options.length = 0;
}

//=====================================================================================

function addIcon(file)
{
	var html='<option value="'+ file +'">'+ xml.escape(file) +'</option>';
	new Insertion.Bottom('z39.icon', html);
}

//=====================================================================================

function updateIcon()
{
	var icon = $F('z39.icon');
	var image= $('z39.icon.image');
	
	image.setAttribute('src', Env.url +'/images/harvesting/'+icon);
}

//=====================================================================================
//=== Search methods
//=====================================================================================

//function addEmptySearch()
//{
//	var doc    = Sarissa.getDomDocument();	
//	var search = doc.createElement('search');
//	
//	addSearch(search);
//}

//=====================================================================================
/*
function addSearch(search)
{
	var id = ''+ currSearchId++;
	search.setAttribute('id', id);
	
	var xslRes = searchTransf.transform(search);

	//--- add the new search in list
	new Insertion.Bottom('csw.searches', xml.toString(xslRes));
	
	valid.add(
	[
		{ id:'csw.anytext',  type:'length',   minSize :0,  maxSize :200 },
		{ id:'csw.title',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'csw.abstract', type:'length',   minSize :0,  maxSize :200 },
		{ id:'csw.subject',  type:'length',   minSize :0,  maxSize :200 }
	], id);
}
*/
//=====================================================================================
/*
function removeSearch(id)
{
	valid.removeByParent(id);
	Element.remove(id);
}
*/
//=====================================================================================
/*
function removeAllSearch()
{
	$('csw.searches').innerHTML = '';
	valid.removeByParent();	
}
*/
//=====================================================================================
}

