//=====================================================================================
//===
//=== ogcwxs controller
//===
//=====================================================================================

ker.include('harvesting/ogcwxs/model.js');
ker.include('harvesting/ogcwxs/view.js');

var ogcwxs = new Object();

//=====================================================================================

function OgcWxs(xmlLoader)
{
	//--- call super constructor
	Harvester.call(this);
	
	var loader= xmlLoader;
    
	var model = new ogcwxs.Model(loader);
	var view  = new ogcwxs.View(loader);
    
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

this.getType      = function() { return "ogcwxs"; }
this.getLabel     = function() { return loader.eval("info[@type='ogcwxs']/long"); }
this.getEditPanel = function() { return "ogcwxs.editPanel"; }

//=====================================================================================

this.init = function()
{
	this.view.init();
	
	model.retrieveGroups    (ker.wrap(this, init_groups_OK));
	model.retrieveCategories(ker.wrap(this, init_categ_OK));
	model.retrieveIcons     (ker.wrap(this, init_icons_OK));
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
		
	for (var i=0; i<data.length; i++) {
		view.addCategory(data[i].id, data[i].label[Env.lang]);
		
		gui.addToSelect('ogcwxs.datasetCategory', data[i].id, data[i].label[Env.lang]);
	}				
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
