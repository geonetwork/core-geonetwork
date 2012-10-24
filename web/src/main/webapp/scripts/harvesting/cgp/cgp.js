//=====================================================================================
//===
//=== CGP controller
//===
//=====================================================================================

ker.include('harvesting/cgp/model.js');
ker.include('harvesting/cgp/view.js');

var cgp = new Object();

//=====================================================================================

function Cgp(xmlLoader)
{
	//--- call super constructor
	Harvester.call(this);

	var loader = xmlLoader;
	var model = new cgp.Model(loader);
	var view = new cgp.View(loader);

	//--- public methods

	this.addSearch = view.addEmptySearch;
	this.removeSearch = view.removeSearch;
	this.getResultTip = view.getResultTip;
	this.addGroupRow    = addGroupRow;
	this.removeGroupRow = view.removeGroupRow;

	this.model = model;
	this.view = view;

	//=====================================================================================
	//===
	//=== API methods
	//===
	//=====================================================================================

	this.getType = function() { return "cgp"; }
	this.getLabel = function() { return loader.eval("info[@type='cgp']/long"); }
	this.getEditPanel = function() { return "cgp.editPanel"; }

	//=====================================================================================

	this.init = function()
	{
		this.view.init();
		model.retrieveIcons(ker.wrap(this, init_icons_OK));
		model.retrieveGroups(ker.wrap(this, init_groups_OK));
		model.retrieveCategories(ker.wrap(this, init_categ_OK));
	}

//-------------------------------------------------------------------------------------

	function init_groups_OK(data)
	{
		view.clearGroups();

		for (var i=0; i<data.length; i++)
			view.addGroup(data[i].id, data[i].label[Env.lang]);
	}

	//-------------------------------------------------------------------------------------

	function init_icons_OK(data)
	{
		view.clearIcons();

		for (var i = 0; i < data.length; i++)
			view.addIcon(data[i]);
	}

	//-------------------------------------------------------------------------------------

	function init_categ_OK(data)
	{
		view.clearCategories();

		for (var i = 0; i < data.length; i++)
			view.addCategory(data[i].id, data[i].label[Env.lang]);
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

}
