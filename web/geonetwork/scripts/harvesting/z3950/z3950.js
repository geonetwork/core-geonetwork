//=====================================================================================
//===
//=== Z3950 controller
//===
//=====================================================================================

ker.include('harvesting/z3950/model.js');
ker.include('harvesting/z3950/view.js');

var z3950 = new Object();

//=====================================================================================

function Z3950(xmlLoader)
{
	//--- call super constructor
	Harvester.call(this);
	
	var loader= xmlLoader;
	var model = new z3950.Model(loader);
	var view  = new z3950.View(loader);
	
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

this.getType      = function() { return "z3950"; }
this.getLabel     = function() { return loader.eval("info[@type='z3950']/long"); }
this.getEditPanel = function() { return "z3950.editPanel"; }

//=====================================================================================

this.init = function()
{
	this.view.init();
	
	model.retrieveGroups    (ker.wrap(this, init_groups_OK));
	model.retrieveCategories(ker.wrap(this, init_categ_OK));
	model.retrieveIcons     (ker.wrap(this, init_icons_OK));
	model.retrieveRepositories (ker.wrap(this, init_repositories_OK));
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

function init_repositories_OK(data)
{
	view.clearRepositories();

	for (var i=0; i<data.length; i++)
		view.addRepository(data[i].id, data[i].label);				
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
