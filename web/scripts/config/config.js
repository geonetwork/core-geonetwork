//=====================================================================================
//===
//=== Config controller class
//===
//=====================================================================================

ker.include('config/model.js');
ker.include('config/view.js');

var config = null;

//=====================================================================================

function init()
{
	config = new Config();
		
	//--- waits for all files to be loaded
	ker.loadMan.wait(config);
}

//=====================================================================================

function Config() 
{
	this.strLoader = new XMLLoader(Env.locUrl +'/xml/config.xml');		
	this.view      = new ConfigView (this.strLoader);
	this.model     = new ConfigModel(this.strLoader);
}

//=====================================================================================

Config.prototype.init = function()
{
	this.view.init();
	this.refresh();
}

//=====================================================================================

Config.prototype.refresh = function()
{
	this.model.getConfig(ker.wrap(this.view, this.view.setData));
}

//=====================================================================================

Config.prototype.save = function()
{
	if (!this.view.isDataValid())
		return;
			
	var data = this.view.getData();
	
	this.model.setConfig(data, ker.wrap(this, this.save_OK));
}

//-------------------------------------------------------------------------------------

Config.prototype.save_OK = function()
{
	alert(this.strLoader.getText('saveOk'));
}

//=====================================================================================

