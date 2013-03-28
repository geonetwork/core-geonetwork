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
		{ id:'filesystem.nodelete',    type:'length',   minSize :1,  maxSize :10 }
	]);

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
	updateIcon();
}

//=====================================================================================

function setData(node)
{
	this.setDataCommon(node);
	var name = node.getElementsByTagName('name')    [0];
	var directoryname = node.getElementsByTagName('directory')    [0];
	var recurse = node.getElementsByTagName('recurse')    [0];
	var nodelete = node.getElementsByTagName('nodelete')    [0];
	hvutil.setOption(node, 'directory', 'filesystem.directoryname');
	hvutil.setOption(node, 'recurse', 'filesystem.recurse');
	hvutil.setOption(node, 'nodelete', 'filesystem.nodelete');
	hvutil.setOption(node, 'icon', 'filesystem.icon');


	//--- add privileges entries
	
	this.removeAllGroupRows();
	this.addGroupRows(node);
	
	//--- set categories

	this.unselectCategories();
	this.selectCategories(node);		

	//--- setup other stuff	
	updateIcon();

}

//=====================================================================================

function getData()
{
	var data = this.getDataCommon();

	data.DIRECTORYNAME = $F('filesystem.directoryname');

	data.RECURSE = $F('filesystem.recurse');
	data.NODELETE = $F('filesystem.nodelete');

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
