//=====================================================================================
//===
//=== OAI-PMH controller
//===
//=====================================================================================

ker.include('harvesting/oaipmh/model.js');
ker.include('harvesting/oaipmh/view.js');

var oaipmh = new Object();

//=====================================================================================

function OaiPmh(xmlLoader)
{
	//--- call super constructor
	Harvester.call(this);
	
	var loader= xmlLoader;
	var model = new oaipmh.Model(loader);
	var view  = new oaipmh.View(loader);

	//--- public methods
	
	this.addSearchRow   = view.addEmptySearch;
	this.addGroupRow    = addGroupRow;
	this.removeGroupRow = view.removeGroupRow;
	this.getResultTip   = view.getResultTip;
	this.retrieveInfo   = retrieveInfo;

	this.model = model;
	this.view  = view;

//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

this.getType      = function() { return "oaipmh"; }
this.getLabel     = function() { return loader.eval("info[@type='oaipmh']/long"); }
this.getEditPanel = function() { return "oai.editPanel"; }

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

function init_importXslts_OK(data)
{
	view.clearImportXslt();
	
	view.addImportXslt('none','--None--');
	for (var i=0; i<data.length; i++) {
		view.addImportXslt(data[i].id,data[i].name);				
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

function retrieveInfo()
{
	var url = view.getUrl();
	
	if ((url.indexOf('http://') != 0) && (url.indexOf('https://') != 0))
		alert(loader.getText('supplyUrl'));
	else
		model.retrieveInfo(url, ker.wrap(view, view.setInfo));
}

//=====================================================================================
}
