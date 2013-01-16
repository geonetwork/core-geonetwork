//=====================================================================================
//===
//=== View (type:Arcsde)
//===
//=====================================================================================

arcsde.View = function(xmlLoader)
{
	HarvesterView.call(this);	
	
	var privilTransf = new XSLTransformer('harvesting/arcsde/client-privil-row.xsl', xmlLoader);
	var resultTransf = new XSLTransformer('harvesting/arcsde/client-result-tip.xsl', xmlLoader);
	
	var loader = xmlLoader;
	var valid  = new Validator(loader);
	
	this.setPrefix('arcsde');
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

	Event.observe('arcsde.icon', 'change', ker.wrap(this, updateIcon));
			
//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function init()
{
	valid.add(
	[
		{ id:'arcsde.name',     type:'length',   minSize :1,  maxSize :200 },
		{ id:'arcsde.server',   type:'length',   minSize :1,  maxSize :500 },
		{ id:'arcsde.port',     type:'integer',   minSize :1,  maxSize :10 },
		{ id:'arcsde.username', type:'length',   minSize :1,  maxSize :500 },
		{ id:'arcsde.password', type:'length',   minSize :1,  maxSize :500 },
		{ id:'arcsde.database', type:'length',   minSize :1,  maxSize :500 }
	]);

}

//=====================================================================================

function setEmpty()
{
	this.setEmptyCommon();

	var icons = $('arcsde.icon').options;
	
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
	var server = node.getElementsByTagName('server')    [0];
	var port = node.getElementsByTagName('port')    [0];
	var username = node.getElementsByTagName('username')    [0];
	var password = node.getElementsByTagName('password')    [0];
	var database = node.getElementsByTagName('database')    [0];
        hvutil.setOption(node, 'icon', 'arcsde.icon');
        hvutil.setOption(node, 'server', 'arcsde.server');
        hvutil.setOption(node, 'port', 'arcsde.port');
        hvutil.setOption(node, 'username', 'arcsde.username');
        hvutil.setOption(node, 'password', 'arcsde.password');
        hvutil.setOption(node, 'database', 'arcsde.database');

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

	data.SERVER = $F('arcsde.server');
	data.PORT = $F('arcsde.port');
	data.USERNAME = $F('arcsde.username');
	data.PASSWORD = $F('arcsde.password');
	data.DATABASE =$F('arcsde.database');

	data.ICON = $F('arcsde.icon');
	
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
	$('arcsde.icon').options.length = 0;
}

//=====================================================================================

function addIcon(file)
{
	gui.addToSelect('arcsde.icon', file, file);
}

//=====================================================================================

function updateIcon()
{
	var icon = $F('arcsde.icon');
	var image= $('arcsde.icon.image');
	
	image.setAttribute('src', Env.url +'/images/harvesting/'+icon);
}

}
