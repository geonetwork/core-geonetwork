//=====================================================================================
//===
//=== Z3950Config controller
//===
//=====================================================================================

ker.include('harvesting/z3950Config/model.js');
ker.include('harvesting/z3950Config/view.js');

var z3950Config = new Object();

//=====================================================================================

function Z3950Config(xmlLoader)
{
	//--- call super constructor
	Harvester.call(this);
	
	var loader= xmlLoader;
	var model = new Z3950Config.Model(loader);
	var view  = new Z3950Config.View(loader);
	
	//--- public methods
	
	this.addSearchRow    = view.addEmptySearch;
	this.removeSearchRow = view.removeSearch;
	this.getResultTip    = view.getResultTip;
	
	this.model = model;
	this.view  = view;
	
//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

this.getType      = function() { return "z3950Config"; }
this.getLabel     = function() { return loader.eval("info[@type='z3950Config']/long"); }
this.getEditPanel = function() { return "z3950Config.editPanel"; }

//=====================================================================================

this.init = function()
{
	this.view.init();
	
	model.retrieveImportXslts     (ker.wrap(this, init_importXslts_OK));
    model.retrieveGroups(ker.wrap(this, init_groups_OK));
}

//-------------------------------------------------------------------------------------

    function init_groups_OK(data) {
        view.clearGroups();
        for (var i=0; i<data.length; i++) {
            view.addGroup(data[i].id, data[i].label[Env.lang]);
        }
    }

function init_importXslts_OK(data)
{
	view.clearImportXslt();
	
	view.addImportXslt('none','--None--');
	for (var i=0; i<data.length; i++) {
		view.addImportXslt(data[i].id,data[i].name);				
	}
}

//=====================================================================================
}
