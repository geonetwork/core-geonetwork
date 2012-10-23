//=====================================================================================
//===
//=== View (type:WebDav)
//===
//=====================================================================================

wd.View = function(xmlLoader)
{
	HarvesterView.call(this);	
	
	var privilTransf = new XSLTransformer('harvesting/webdav/client-privil-row.xsl', xmlLoader);
	var resultTransf = new XSLTransformer('harvesting/webdav/client-result-tip.xsl', xmlLoader);
	
	var loader = xmlLoader;
	var valid  = new Validator(loader);
	var shower = null;
	
	this.setPrefix('wd');
	this.setPrivilTransf(privilTransf);
	this.setResultTransf(resultTransf);
	
	//--- public methods
	
	this.init        = init;
	this.setEmpty    = setEmpty;
	this.setData     = setData;
	this.getData     = getData;
	this.isDataValid = isDataValid;
	this.clearIcons  = clearIcons;
	this.addIcon     = addIcon;		

	Event.observe('wd.icon', 'change', ker.wrap(this, updateIcon));

//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function init()
{
	valid.add(
	[
		{ id:'wd.name',        type:'length',   minSize :1,  maxSize :200 },
		{ id:'wd.url',         type:'length',   minSize :1,  maxSize :200 },
		{ id:'wd.url',         type:'url' },
		{ id:'wd.subtype',     type:'length',   minSize :1,  maxSize :200 },
		{ id:'wd.username',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'wd.password',    type:'length',   minSize :0,  maxSize :200 }
	]);

	shower = new Shower('wd.useAccount', 'wd.account');
}

//=====================================================================================

function setEmpty()
{
	this.setEmptyCommon();
	
	$('wd.url')      .value = '';
	
	$('wd.recurse') .checked = false;
	$('wd.validate').checked = false;

	var icons = $('wd.icon').options;
	
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

	var site   = node.getElementsByTagName('site')   [0];
	var options= node.getElementsByTagName('options')[0];

	hvutil.setOption(site,    'url',      'wd.url');
	hvutil.setOption(site,    'icon',     'wd.icon');
	hvutil.setOption(options, 'validate', 'wd.validate');
	hvutil.setOption(options, 'recurse',  'wd.recurse');
	hvutil.setOption(options, 'subtype',  'wd.subtype');
	
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
	
	data.URL      = $F('wd.url');
	data.ICON     = $F('wd.icon');
	data.VALIDATE = $('wd.validate').checked;
	data.RECURSE  = $('wd.recurse') .checked;
	data.SUBTYPE  = $F('wd.subtype');
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
	$('wd.icon').options.length = 0;
}

//=====================================================================================

function addIcon(file)
{
	gui.addToSelect('wd.icon', file, file);
}

//=====================================================================================

function updateIcon()
{
	var icon = $F('wd.icon');
	var image= $('wd.icon.image');
	
	image.setAttribute('src', Env.url +'/images/harvesting/'+icon);
}

//=====================================================================================
}

