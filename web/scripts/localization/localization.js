//=====================================================================================

Event.observe(window, 'load', function() 
{
	localiz = new Localiz();	
});

//=====================================================================================
//===
//=== Localization controller class
//===
//=====================================================================================

function Localiz() 
{
	try
	{
		Event.observe('btn.save',    'click',  gn.wrap(this, this.save));
		Event.observe('btn.refresh', 'click',  gn.wrap(this, this.refresh));
		Event.observe('entity.type', 'change', gn.wrap(this, this.entityTypeChange));
		Event.observe('entity.list', 'change', gn.wrap(this, this.entityListChange));
		
		this.strLoader = new XMLLoader(Env.locUrl +'/xml/localization.xml');		
		this.view      = new View (this.strLoader);
		this.model     = new Model(this.strLoader);
		this.cache     = {};
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
	var entity = this.view.getEntityType();
	
	this.view.clearEntityList();
	this.model.getEntityList(entity, gn.wrap(this, this.refresh_OK));
}

//-------------------------------------------------------------------------------------

Localiz.prototype.refresh_OK = function(data)
{
	var entity = this.view.getEntityType();
	
	//--- cache data for later use
	this.cache[entity] = data;
	
	//--- data is an array of maps where each map contains (id, name)

	for (var i=0; i<data.length; i++)
		this.view.addEntityToList(data[i]);
}

//=====================================================================================

Localiz.prototype.save = function()
{
	if (this.view.getSelectedEntity() == null)
		return;
	
	if (!this.view.isDataValid())
		return;
			
	var data = this.view.getData();
	
//	this.model.setConfig(data, gn.wrap(this, this.save_OK));
}

//-------------------------------------------------------------------------------------

Localiz.prototype.save_OK = function()
{
	alert(this.strLoader.getText('saveOk'));
}

//=====================================================================================
//=== Listeners
//=====================================================================================

Localiz.prototype.entityTypeChange = function(e)
{
	var entity = this.view.getEntityType();
	var data   = this.cache[entity];
	
	if (data == null)
		this.refresh();
	else
	{	
		this.view.clearEntityList();
		
		for (var i=0; i<data.length; i++)
			this.view.addEntityToList(data[i]);
	}
}

//=====================================================================================

Localiz.prototype.entityListChange = function(e)
{
	var type = this.view.getEntityType();
	var data = this.cache[type];
	var id   = this.view.getSelectedEntity();	
	
	for (var i=0; i<data.length; i++)
		if (data[i].id == id)
		{
			this.view.setEntity(data[i]);
			return;
		}
}

//=====================================================================================
