//=====================================================================================
//===
//=== WebDav controller
//===
//=====================================================================================

ker.include('harvesting/webdav/model.js');
ker.include('harvesting/webdav/view.js');

var wd = new Object();

//=====================================================================================

function WebDav(xmlLoader)
{
	//--- call super constructor
	Harvester.call(this);
	
	var loader= xmlLoader;
	var model = new wd.Model(loader);
	var view  = new wd.View(loader);
	
	//--- public methods
	
	this.addGroupRow    = addGroupRow;
	this.removeGroupRow = view.removeGroupRow;
	this.getResultTip   = view.getResultTip;
		
	this.model = model;
	this.view  = view;

//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

this.getType      = function() { return "webdav"; }
this.getLabel     = function() { return loader.eval("info[@type='webdav']/long"); }
this.getEditPanel = function() { return "wd.editPanel"; }

//=====================================================================================

this.init = function()
{
	this.view.init();
	
	model.retrieveGroups    (ker.wrap(this, init_groups_OK));
	model.retrieveCategories(ker.wrap(this, init_categ_OK));
	model.retrieveIcons     (ker.wrap(this, init_icons_OK));
	model.retrieveImportXslts     (ker.wrap(this, init_importXslts_OK));
}

//-------------------------------------------------------------------------------------

function init_groups_OK(data)
{
	view.clearGroups();
		
	for (var i=0; i<data.length; i++)
		view.addGroup(data[i].id, data[i].label[Env.lang]);				
}

//-------------------------------------------------------------------------------------

function init_importXslts_OK(data)
{
	view.clearImportXslt();
	
	view.addImportXslt('none','--None--');
	for (var i=0; i<data.length; i++) {
		view.addImportXslt(data[i].id,data[i].name);				
	}
}

//-------------------------------------------------------------------------------------

function init_categ_OK(data)
{
	view.clearCategories();
		
	for (var i=0; i<data.length; i++)
		view.addCategory(data[i].id, data[i].label[Env.lang]);				
}

//-------------------------------------------------------------------------------------

function init_icons_OK(data)
{
	view.clearIcons();
		
	for (var i=0; i<data.length; i++)
		view.addIcon(data[i]);				
}

//=====================================================================================

function addGroupRow()
{
	var groups = view.getSelectedGroups();
	
	if (groups.length == 0) alert(loader.getText('pleaseSelectGroup'));
		else						view.addEmptyGroupRows(groups);
}

//=====================================================================================
}
