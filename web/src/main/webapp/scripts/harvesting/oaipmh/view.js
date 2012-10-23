//=====================================================================================
//===
//=== View (type:oaipmh)
//===
//=====================================================================================

oaipmh.View = function(xmlLoader)
{
	HarvesterView.call(this);	
	
	var searchTransf = new XSLTransformer('harvesting/oaipmh/client-search-row.xsl', xmlLoader);
	var privilTransf = new XSLTransformer('harvesting/oaipmh/client-privil-row.xsl', xmlLoader);
	var resultTransf = new XSLTransformer('harvesting/oaipmh/client-result-tip.xsl', xmlLoader);
	
	var loader = xmlLoader;
	var valid  = new Validator(loader);
	var shower = null;
	var info   = null;
	
	var currSearchId = 0;
	
	this.setPrefix('oai');
	this.setPrivilTransf(privilTransf);
	this.setResultTransf(resultTransf);

	// register event listeners on the Ajax requests to show/hide the busy 
	// indicator
	Ajax.Responders.register({
 		onCreate: function() {
   	 if (Ajax.activeRequestCount === 1) {
   	   var eBusy = $('harvesterBusy');
   	   if (eBusy) eBusy.show();
   	 }
  	},
  	onComplete: function() {
   	 if (Ajax.activeRequestCount === 0) {
   	   var eBusy = $('harvesterBusy');
   	   if (eBusy) eBusy.hide();
   	 }
  	}
	});
	
	//--- public methods
	
	this.init           = init;
	this.setEmpty       = setEmpty;
	this.setData        = setData;
	this.getData        = getData;
	this.isDataValid    = isDataValid;
	this.clearIcons     = clearIcons;
	this.addIcon        = addIcon;		
	this.addEmptySearch = addEmptySearch;
	this.setInfo        = setInfo;
	this.getUrl         = getUrl;
	
	Event.observe('oai.icon', 'change', ker.wrap(this, updateIcon));

//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function init()
{
	valid.add(
	[
		{ id:'oai.name',        type:'length',   minSize :1,  maxSize :200 },
		{ id:'oai.url',         type:'length',   minSize :1,  maxSize :200 },
		{ id:'oai.url',         type:'url' },
		{ id:'oai.username',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'oai.password',    type:'length',   minSize :0,  maxSize :200 }
	]);

	shower = new Shower('oai.useAccount', 'oai.account');
	
	gui.setupTooltips(loader.getNode('tips'));
}

//=====================================================================================

function setEmpty()
{
	this.setEmptyCommon();
	info = null;
	
	removeAllSearch();
	
	$('oai.url').value = '';
	
	$('wd.validate').checked = false;

	var icons = $('oai.icon').options;
	
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
	info = null;
	
	var site     = node.getElementsByTagName('site')    [0];
	var searches = node.getElementsByTagName('searches')[0];
	var options  = node.getElementsByTagName('options') [0];

	hvutil.setOption(site,    'url',      'oai.url');
	hvutil.setOption(site,    'icon',     'oai.icon');
	hvutil.setOption(options, 'validate', 'oai.validate');
	
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
	
	data.URL      = $F('oai.url');
	data.ICON     = $F('oai.icon');
	data.VALIDATE = $('oai.validate').checked;
	
	//--- retrieve search information
	
	var searchData = [];
	var searchList = xml.children($('oai.searches'));
	
	for(var i=0; i<searchList.length; i++)
	{
		var divElem = searchList[i];
		var id      = divElem.getAttribute('id');
		
		searchData.push(
		{
			FROM       : $F(id+'.oai.from'),
			UNTIL      : $F(id+'.oai.until'),
			SET        : $F(id+'.oai.set'),
			PREFIX     : $F(id+'.oai.prefix'),
			STYLESHEET : $F(id+'.oai.stylesheet')
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
	$('oai.icon').options.length = 0;
}

//=====================================================================================

function addIcon(file)
{
	gui.addToSelect('oai.icon', file, file);
}

//=====================================================================================

function updateIcon()
{
	var icon = $F('oai.icon');
	var image= $('oai.icon.image');
	
	image.setAttribute('src', Env.url +'/images/harvesting/'+icon);
}

//=====================================================================================
//=== Info methods
//=====================================================================================

function setInfo(data)
{
	info = data;
	
	//--- update searches
	
	var searchData = [];
	var searchList = xml.children($('oai.searches'));
	
	for(var i=0; i<searchList.length; i++)
	{
		var divElem = searchList[i];
		var id      = divElem.getAttribute('id');
		
		var setName = $F(id+'.oai.set_select');
		var mdfName = $F(id+'.oai.prefix_select');
		
		setupCombo(id, setName, mdfName);

		$(id+'.oai.prefixes.cols').show();
		$(id+'.oai.sets.cols').show();
	}
}

//=====================================================================================

function setupCombo(id, setName, mdfName)
{
	var setElem = $(id+'.oai.set_select');
	var mdfElem = $(id+'.oai.prefix_select');
	
	//--- remove old sets and add blank option
	
	clearCombo(setElem);
	addCombo(setElem, '', '', setName);
	
	//--- add new sets
	
	for (var j=0; j<info.SETS.length; j++)
	{
		var name = info.SETS[j].NAME;
		var label= info.SETS[j].LABEL;
		
		addCombo(setElem, name, label, setName);
	}
	
	//--- remove old formats and add the default oai_dc option
	
	clearCombo(mdfElem);
	addCombo(mdfElem, 'oai_dc', 'oai_dc', setName);
	
	//--- add new metadata formats
	
	for (var j=0; j<info.FORMATS.length; j++)
	{
		var name = info.FORMATS[j];
		
		addCombo(mdfElem, name, name, mdfName);
	}
}

//=====================================================================================

function clearCombo(elem) { elem.options.length = 0; }

//=====================================================================================

function addCombo(elem, name, label, selName)
{
	gui.addToSelect(elem, name, label, name==selName);
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
	new Insertion.Bottom('oai.searches', html);
	
	//--- setup REMOVE button
	
	Event.observe(id+'.oai.remove', 'click', ker.wrap(this, function()
	{ 
		Element.remove(id);
	}));
	
	//--- setup FROM and UNTIL parameters
	initCalendar();
	
	
	//--- setup dynamic tooltips
	
	gui.setupTooltip(id+'.oai.remove', loader.evalNode('tips/tip[@id="oai.remove"]'));
	
	gui.setupTooltip(id+'.oai.from',        loader.evalNode('tips/tip[@id="oai.from"]'));
	gui.setupTooltip(id+'.oai.until',       loader.evalNode('tips/tip[@id="oai.until"]'));
	
	gui.setupTooltip(id+'.oai.set',    loader.evalNode('tips/tip[@id="oai.set"]'));
	gui.setupTooltip(id+'.oai.prefix', loader.evalNode('tips/tip[@id="oai.prefix"]'));
	
	var set    = xml.evalXPath(search, 'set');
	var prefix = xml.evalXPath(search, 'prefix');
	
	if (info == null)
	{
		gui.addToSelect(id+'.oai.set_select',    '',       '',       false);
		gui.addToSelect(id+'.oai.prefix_select', 'oai_dc', 'oai_dc', false);
		
		if (set != null && set != '')
			gui.addToSelect(id+'.oai.set_select', set, set, true);
				
		if (prefix != null && prefix != 'oai_dc')
			gui.addToSelect(id+'.oai.prefix_select', prefix, prefix, true);
	}
	else
	{
		setupCombo(id, set, prefix);	
	}
}

//=====================================================================================

function removeAllSearch()
{
	$('oai.searches').innerHTML = '';
}

//=====================================================================================

function getUrl()
{
	return $F('oai.url');
}

//=====================================================================================
}

