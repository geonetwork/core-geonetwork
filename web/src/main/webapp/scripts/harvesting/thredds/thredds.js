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
	model.retrieveSchemasDIF   (ker.wrap(this, init_schemasDIF_OK));
	model.retrieveSchemasFragments   (ker.wrap(this, init_schemasFragments_OK));
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

function init_schemasDIF_OK(data)
{
	gui.addToSelect('thredds.outputSchemaOnCollectionsDIF', "", "");
	gui.addToSelect('thredds.outputSchemaOnAtomicsDIF', "", "");
	
	for (var i=0; i<data.length; i++) {
		gui.addToSelect('thredds.outputSchemaOnCollectionsDIF', data[i].id, data[i].name);
		gui.addToSelect('thredds.outputSchemaOnAtomicsDIF', data[i].id, data[i].name);
	}				
}

//-------------------------------------------------------------------------------------

function init_schemasFragments_OK(data)
{
	gui.addToSelect('thredds.outputSchemaOnCollectionsFragments', "", "");
	gui.addToSelect('thredds.outputSchemaOnAtomicsFragments', "", "");

	for (var i=0; i<data.length; i++) {
		gui.addToSelect('thredds.outputSchemaOnCollectionsFragments', data[i].id, data[i].name);
		gui.addToSelect('thredds.outputSchemaOnAtomicsFragments', data[i].id, data[i].name);
	}				
}

//-------------------------------------------------------------------------------------

function addGroupRow()
{
	var groups = view.getSelectedGroups();
	
	if (groups.length == 0) alert(loader.getText('pleaseSelectGroup'));
		else						view.addEmptyGroupRows(groups);
}

//=====================================================================================
}
