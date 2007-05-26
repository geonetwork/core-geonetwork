//=====================================================================================
//===
//=== Geonetwork controller
//===
//=====================================================================================

ker.include('harvesting/geonet/model.js');
ker.include('harvesting/geonet/view.js');

var gn = new Object();

//=====================================================================================

function Geonetwork(xmlLoader)
{
	//--- call super constructor
	Harvester.call(this);
	
	var loader= xmlLoader;
	var model = new gn.Model(loader);
	var view  = new gn.View(loader);
	
	//--- public methods
	
	this.addSearchRow    = addSearchRow;
	this.removeSearchRow = view.removeSearch;
	this.getResultTip    = view.getResultTip;
	this.retrieveSites   = retrieveSites;
	
	this.model = model;
	this.view  = view;
	
//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

this.getType      = function() { return "geonetwork"; }
this.getLabel     = function() { return loader.getText("typeGN"); }
this.getEditPanel = function() { return "gn.editPanel"; }

//=====================================================================================

function retrieveSites()
{
	var data = view.getHostData();
	
	if (data.HOST == '')
		alert(loader.getText('supplyHost'));
		
	else if (data.SERVLET == '')
		alert(loader.getText('supplyServlet'));
		
	else
		model.retrieveSites(data, ker.wrap(this, retrieveSites_OK));
}

//-------------------------------------------------------------------------------------

function retrieveSites_OK(data)
{
	view.clearSiteId();
	view.addSiteId(data.SITE_ID, data.SITE_NAME);
	
	for (var i=0; i<data.NODES.length; i++)
	{
		var node = data.NODES[i];
	
		view.addSiteId(node.SITE_ID, node.SITE_NAME +' ('+ node.MD_COUNT +')');				
	}
}

//=====================================================================================

function addSearchRow()
{
	var siteId   = view.getSiteId();
	var siteName = view.getSiteName();
	
	if (siteName == null)
		alert(loader.getText('pleaseRetrieve'));
	else
		view.addEmptySearch(siteId, siteName);
}

//=====================================================================================
}
