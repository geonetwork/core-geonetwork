//=====================================================================================
//===
//=== View (type:Filesystem)
//===
//=====================================================================================

filesystem.View = function(xmlLoader)
{
	HarvesterView.call(this);	
	
	var privilTransf = new XSLTransformer('harvesting/filesystem/client-privil-row.xsl', xmlLoader);
	var resultTransf = new XSLTransformer('harvesting/filesystem/client-result-tip.xsl', xmlLoader);
	
	var loader = xmlLoader;
	var valid  = new Validator(loader);
	var shower = null;
	
	this.setPrefix('filesystem');
	this.setPrivilTransf(privilTransf);
	this.setResultTransf(resultTransf);
	
	//--- public methods
	
	this.init        = init;
	this.setEmpty    = setEmpty;
	this.setData     = setData;
	this.getData     = getData;
	this.isDataValid = isDataValid;
	this.clearIcons     = clearIcons;
	this.addIcon        = addIcon;
		
	this.removeAllGroupRows = function(){}
	this.unselectCategories = function(){};

	Event.observe('filesystem.icon', 'change', ker.wrap(this, updateIcon));
			
//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function init()
{
	valid.add(
	[
		{ id:'filesystem.name', type:'length',   minSize :1,  maxSize :200 },
		{ id:'filesystem.directoryname',     type:'length',   minSize :1,  maxSize :500 },
		{ id:'filesystem.recurse',     type:'length',   minSize :1,  maxSize :10 },		
		{ id:'filesystem.every.days',  type:'integer',  minValue:0, maxValue:99 },
		{ id:'filesystem.every.hours', type:'integer',  minValue:0, maxValue:23 },
		{ id:'filesystem.every.mins',  type:'integer',  minValue:0, maxValue:59 }
	]);

	shower = new Shower('filesystem.recurse',  'filesystem.recurse');
}

//=====================================================================================

function setEmpty()
{
	this.setEmptyCommon();

	var icons = $('filesystem.icon').options;
	
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
	var name = node.getElementsByTagName('name')    [0];
	var directoryname = node.getElementsByTagName('directoryname')    [0];
	var recurse = node.getElementsByTagName('recurse')    [0];


	//--- add privileges entries
	
	this.removeAllGroupRows();
	this.addGroupRows(node);
	
	//--- set categories

	this.unselectCategories();
	this.selectCategories(node);		

	//--- setup other stuff	
	shower.update();
	updateIcon();

}

//=====================================================================================

function getData()
{
	var data = this.getDataCommon();

	data.DIRECTORYNAME = $F('filesystem.directoryname');

	data.RECURSE = $F('filesystem.recurse');

	data.ICON = $F('filesystem.icon');
	
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
	$('filesystem.icon').options.length = 0;
}

//=====================================================================================

function addIcon(file)
{
	gui.addToSelect('filesystem.icon', file, file);
}

//=====================================================================================

function updateIcon()
{
	var icon = $F('filesystem.icon');
	var image= $('filesystem.icon.image');
	
	image.setAttribute('src', Env.url +'/images/harvesting/'+icon);
}

}