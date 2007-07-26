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
		{ id:'wd.username',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'wd.password',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'wd.every.days',  type:'integer',  minValue:0, maxValue:99 },
		{ id:'wd.every.hours', type:'integer',  minValue:0, maxValue:23 },
		{ id:'wd.every.mins',  type:'integer',  minValue:0, maxValue:59 }
	]);

	shower = new Shower('wd.useAccount', 'wd.account');
}

//=====================================================================================

function setEmpty()
{
	this.setEmptyCommon();
	
	$('wd.url')       .value = '';
	
	$('wd.structure').checked = false;
	$('wd.validate') .checked = false;

	shower.update();
}

//=====================================================================================

function setData(node)
{
	this.setDataCommon(node);

	var site   = node.getElementsByTagName('site')   [0];
	var options= node.getElementsByTagName('options')[0];

	hvutil.setOption(site,    'url',        'wd.url');
	hvutil.setOption(options, 'structure',  'wd.structure');
	hvutil.setOption(options, 'validate',   'wd.validate');
	
	//--- add privileges entries
	
	this.removeAllGroupRows();
	this.addGroupRows(node);
	
	//--- set categories

	this.unselectCategories();
	this.selectCategories(node);	
	
	shower.update();
}

//=====================================================================================

function getData()
{
	var data = this.getDataCommon();
	
	data.URL       = $('wd.url')      .value;
	data.VALIDATE  = $('wd.validate') .checked;
	data.STRUCTURE = $('wd.structure').checked;
	
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
}

