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
	var type = this.view.getEntityType();
	
	this.view.clearEntityList();
	this.model.getEntityList(type, gn.wrap(this, this.refresh_OK));
}

//-------------------------------------------------------------------------------------

Localiz.prototype.refresh_OK = function(data)
{
	var type = this.view.getEntityType();
	
	//--- cache data for later use
	this.cache[type] = data;
	
	//--- data is an array of maps where each map contains (id, name)

	for (var i=0; i<data.length; i++)
		this.view.addEntityToList(data[i]);
}

//=====================================================================================

Localiz.prototype.save = function()
{
	if (this.view.getSelectedIndex() == -1)
		return;
	
	if (!this.view.isDataValid())
		return;
			
	var data = 
	{
		ID   : this.view.getSelectedID(),
		LANG : this.view.getTargetLanguage(),
		TEXT : this.view.getTargetText(),
		TYPE : this.view.getEntityType()
	};
	
	this.model.update(data, gn.wrap(this, this.save_OK));
}

//-------------------------------------------------------------------------------------

Localiz.prototype.save_OK = function()
{
	//--- save is ok. Now store the new text into cache
	
	var type  = this.view.getEntityType();
	var data  = this.cache[type];
	var index = this.view.getSelectedIndex();	
	var lang  = this.view.getTargetLanguage();
	var text  = this.view.getTargetText();
	
	data[index].label[lang] = text;
			
	//--- Advance on the next list item
	
	var index = this.view.advanceOnList();

	this.view.setEntity(index == -1 ? null : data[index]);
}

//=====================================================================================
//===
//=== Listeners
//===
//=====================================================================================

Localiz.prototype.entityTypeChange = function(e)
{
	var type = this.view.getEntityType();
	var data = this.cache[type];
	
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
	var index= this.view.getSelectedIndex();	
	
	this.view.setEntity(data[index]);
}

//=====================================================================================
