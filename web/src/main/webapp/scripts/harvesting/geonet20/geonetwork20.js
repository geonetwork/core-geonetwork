//=====================================================================================
//===
//=== Geonetwork 2.0.x controller
//===
//=====================================================================================

ker.include('harvesting/geonet20/model.js');
ker.include('harvesting/geonet20/view.js');

var gn20 = new Object();

//=====================================================================================

function Geonetwork20(xmlLoader)
{
	//--- call super constructor
	Harvester.call(this);
	
	var loader= xmlLoader;
	var model = new gn20.Model(loader);
	var view  = new gn20.View(loader);
	
	//--- public methods
	
	this.addSearchRow    = addSearchRow;
	this.removeSearchRow = view.removeSearch;
	this.getResultTip    = view.getResultTip;
	
	this.model = model;
	this.view  = view;
	
//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

this.getType      = function() { return "geonetwork20"; }
this.getLabel     = function() { return loader.eval("info[@type='geonetwork20']/long"); }
this.getEditPanel = function() { return "gn20.editPanel"; }

//=====================================================================================

function addSearchRow()
{
	var siteId = view.getSiteId();
	
	if (siteId == '')
		alert(loader.getText('pleaseSpecifySiteId'));
	else
	{
		view.addEmptySearch(siteId);
		view.clearSiteId();
	}
}

//=====================================================================================
}
