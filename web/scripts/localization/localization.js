//=====================================================================================

function init()
{
	localiz = new Localiz();	
}

//=====================================================================================
//===
//=== Localization controller class
//===
//=====================================================================================

function Localiz() 
{
	try
	{
		this.strLoader = new XMLLoader(Env.locUrl +'/xml/localization.xml');		
		this.view      = new View (this.strLoader);
		this.model     = new Model(this.strLoader);
		
		this.refresh();
	}
	catch(e) 
	{
		alert(e);
	}
}

//=====================================================================================

Localiz.prototype.refresh = function()
{
	var entity  = this.view.getSelectedEntity();
	
	this.view.clearEntityList();
	this.model.getEntityList(entity, gn.wrap(this.view, this.view.setEntityList));
}

//=====================================================================================

Localiz.prototype.save = function()
{
	if (!this.view.isDataValid())
		return;
			
	var data = this.view.getData();
	
	this.model.setConfig(data, gn.wrap(this, this.save_OK));
}

//-------------------------------------------------------------------------------------

Localiz.prototype.save_OK = function()
{
	alert(this.strLoader.getText('saveOk'));
}

//=====================================================================================

