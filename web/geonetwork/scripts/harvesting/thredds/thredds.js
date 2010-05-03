//=====================================================================================
//===
//=== thredds controller
//===
//=====================================================================================

ker.include('harvesting/thredds/model.js');
ker.include('harvesting/thredds/view.js');

var thredds = new Object();

//=====================================================================================

function Thredds(xmlLoader)
{
	//--- call super constructor
	Harvester.call(this);
	
	var loader= xmlLoader;
    
	var model = new thredds.Model(loader);
	var view  = new thredds.View(loader);
    
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

this.getType      = function() { return "thredds"; }
this.getLabel     = function() { return loader.eval("info[@type='thredds']/long"); }
this.getEditPanel = function() { return "thredds.editPanel"; }

//=====================================================================================

this.init = function()
{
	this.view.init();
	
	model.retrieveGroups    (ker.wrap(this, init_groups_OK));
	model.retrieveCategories(ker.wrap(this, init_categ_OK));
	model.retrieveIcons     (ker.wrap(this, init_icons_OK));
	model.retrieveStylesheets (ker.wrap(this, init_stylesheets_OK));
	model.retrieveTemplates   (ker.wrap(this, init_templates_OK));
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
		
		gui.addToSelect('thredds.datasetCategory', data[i].id, data[i].label[Env.lang]);
	}				
}

//-------------------------------------------------------------------------------------

function init_icons_OK(data)
{
	view.clearIcons();
		
	for (var i=0; i<data.length; i++)
		view.addIcon(data[i]);				
}

//-------------------------------------------------------------------------------------

function init_stylesheets_OK(data)
{
	gui.addToSelect('thredds.collectionFragmentStylesheet', "", "");
	gui.addToSelect('thredds.atomicFragmentStylesheet', "", "");
	
	for (var i=0; i<data.length; i++) {
		gui.addToSelect('thredds.collectionFragmentStylesheet', data[i].id, data[i].name);
		gui.addToSelect('thredds.atomicFragmentStylesheet', data[i].id, data[i].name);
	}				
}

//-------------------------------------------------------------------------------------
//
function init_templates_OK(data)
{
	gui.addToSelect('thredds.collectionMetadataTemplate', 0, "");
	gui.addToSelect('thredds.atomicMetadataTemplate', 0, "");

	for (var i=0; i<data.length; i++) {
		gui.addToSelect('thredds.collectionMetadataTemplate', data[i].id, data[i].title);
		gui.addToSelect('thredds.atomicMetadataTemplate', data[i].id, data[i].title);
	}				
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
