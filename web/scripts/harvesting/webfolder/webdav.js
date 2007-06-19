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
	
	this.refreshGroups  = refreshGroups;
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

function refreshGroups()
{
	model.retrieveGroups(Env.lang, ker.wrap(this, refreshGroups_OK));
}

//-------------------------------------------------------------------------------------

function refreshGroups_OK(data)
{
	view.clearGroups();
		
	for (var i=0; i<data.length; i++)
		view.addGroup(data[i].ID, data[i].NAME);				
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
