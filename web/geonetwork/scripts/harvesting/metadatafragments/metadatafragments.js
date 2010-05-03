//=====================================================================================
//===
//=== metadatafragments controller
//===
//=====================================================================================

ker.include('harvesting/metadatafragments/model.js');
ker.include('harvesting/metadatafragments/view.js');

var metadatafragments = new Object();

//=====================================================================================

function MetadataFragments(xmlLoader)
{
	//--- call super constructor
	Harvester.call(this);
	
	var loader= xmlLoader;
    
	var model = new metadatafragments.Model(loader);
	var view  = new metadatafragments.View(loader);
    
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

this.getType      = function() { return "metadatafragments"; }
this.getLabel     = function() { return loader.eval("info[@type='metadatafragments']/long"); }
this.getEditPanel = function() { return "metadatafragments.editPanel"; }

//=====================================================================================

this.init = function()
{
	this.view.init();
	
	model.retrieveStylesheets (ker.wrap(this, init_stylesheets_OK));
	model.retrieveGroups      (ker.wrap(this, init_groups_OK));
	model.retrieveCategories  (ker.wrap(this, init_categ_OK));
	model.retrieveTemplates   (ker.wrap(this, init_templates_OK));
	model.retrieveIcons       (ker.wrap(this, init_icons_OK)); // not used 
}

//-------------------------------------------------------------------------------------

function init_stylesheets_OK(data)
{
	gui.addToSelect('metadatafragments.stylesheet', 0, "");
	for (var i=0; i<data.length; i++) {
		gui.addToSelect('metadatafragments.stylesheet', data[i].id, data[i].name);
	}				
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
		
		gui.addToSelect('metadatafragments.recordsCategory', data[i].id, data[i].label[Env.lang]);
	}				
}

//-------------------------------------------------------------------------------------
//
function init_templates_OK(data)
{
	gui.addToSelect('metadatafragments.templateId', 0, "");
	for (var i=0; i<data.length; i++) {
		gui.addToSelect('metadatafragments.templateId', data[i].id, data[i].title);
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
