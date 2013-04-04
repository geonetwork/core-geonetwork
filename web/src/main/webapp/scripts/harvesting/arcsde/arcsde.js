//=====================================================================================
//===
//=== arcsde controller
//===
//=====================================================================================

ker.include('harvesting/arcsde/model.js');
ker.include('harvesting/arcsde/view.js');

var arcsde= new Object();

//=====================================================================================

function Arcsde(xmlLoader)
{
	//--- call super constructor
	Harvester.call(this);
	
	var loader= xmlLoader;
	var model = new arcsde.Model(loader);
	var view  = new arcsde.View(loader);
	
	//--- public methods
	
	this.addGroupRow    = addGroupRow;
	this.removeGroupRow = view.removeGroupRow;
	this.getResultTip    = view.getResultTip;
	
	this.model = model;
	this.view  = view;
	
//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

this.getType      = function() { return "arcsde"; }
this.getLabel     = function() { return loader.eval("info[@type='arcsde']/long"); }
this.getEditPanel = function() { return "arcsde.editPanel"; }

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
