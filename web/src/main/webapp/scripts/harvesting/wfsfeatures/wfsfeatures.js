//=====================================================================================
//===
//=== wfsfeatures controller
//===
//=====================================================================================

ker.include('harvesting/wfsfeatures/model.js');
ker.include('harvesting/wfsfeatures/view.js');

var wfsfeatures = new Object();

//=====================================================================================

function WfsFeatures(xmlLoader)
{
	//--- call super constructor
	Harvester.call(this);
	
	var loader= xmlLoader;
    
	var model = new wfsfeatures.Model(loader);
	var view  = new wfsfeatures.View(loader);
    
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

this.getType      = function() { return "wfsfeatures"; }
this.getLabel     = function() { return loader.eval("info[@type='wfsfeatures']/long"); }
this.getEditPanel = function() { return "wfsfeatures.editPanel"; }

//=====================================================================================

this.init = function()
{
	this.view.init();
	
	model.retrieveSchemas     (ker.wrap(this, init_schemas_OK));
	model.retrieveGroups      (ker.wrap(this, init_groups_OK));
	model.retrieveCategories  (ker.wrap(this, init_categ_OK));
	model.retrieveIcons       (ker.wrap(this, init_icons_OK)); // not used 
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
		
		gui.addToSelect('wfsfeatures.recordsCategory', data[i].id, data[i].label[Env.lang]);
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

function init_schemas_OK(data)
{
	gui.addToSelect('wfsfeatures.outputSchema', "", "");

	for (var i=0; i<data.length; i++) {
		gui.addToSelect('wfsfeatures.outputSchema', data[i].id, data[i].name);
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
