//=====================================================================================

function init()
{
	config = new Config();	
}

//=====================================================================================
//===
//=== Config controller class
//===
//=====================================================================================

function Config() 
{
	try
	{
		this.strLoader = new XMLLoader(Env.locUrl +'/xml/config.xml');		
		this.view      = new ConfigView (this.strLoader);
		this.model     = new ConfigModel(this.strLoader);
		
		this.refresh();
	}
	catch(e) 
	{
		alert(e);
	}
}

//=====================================================================================

Config.prototype.refresh = function()
{
	this.model.getConfig(gn.wrap(this.view, this.view.setData));
}

//=====================================================================================

Config.prototype.save = function()
{
	if (!this.view.isDataValid())
		return;
			
	var data = this.view.getData();
	
	this.model.setConfig(data, gn.wrap(this, this.save_OK));
}

//-------------------------------------------------------------------------------------

Config.prototype.save_OK = function()
{
	alert(this.strLoader.getText('saveOk'));
}

//=====================================================================================

