//=====================================================================================
//===
//=== View
//=== 
//=== Handles all view related stuff in the MVC pattern
//===
//=== Needs : geonetwork-ajax.js
//===
//=====================================================================================

function View(strLoader)
{
	//--- setup validators
	
	this.strLoader = strLoader;
	this.validator = new Validator(strLoader);	
	this.validator.add(
	[
		{ id:'editor.destin', type:'length', minSize :1, maxSize :60 }
	]);

	$('editor.source').style.background = '#F0F0F0';

	Event.observe('lang.source',   'change',   gn.wrap(this, this.langSrcChange));
	Event.observe('lang.destin',   'change',   gn.wrap(this, this.langDesChange));
	Event.observe('editor.destin', 'keypress', gn.wrap(this, this.textChange));
	
	strLoader.addListener(gn.wrap(this, this.constr_OK));
}

//-------------------------------------------------------------------------------------

View.prototype.constr_OK = function()
{
	gui.setupTooltips(this.strLoader.getNode('tips'));
}

//=====================================================================================
 
View.prototype.getEntityType = function()
{
	return $F('entity.type');
}

//=====================================================================================
 
View.prototype.getSelectedEntity = function()
{
	return $F('entity.list');
}

//=====================================================================================

View.prototype.clearEntityList = function()
{
	$('entity.list').options.length = 0;
	this.setEntity(null);
}

//=====================================================================================

View.prototype.addEntityToList = function(entity)
{
	var id   = entity['id'];
	var name = entity['name'];
	
	//--- regions don't have names so we use the first localized name
	
	if (name == null)
		name = entity['label']['en'];
		
	var html='<option value="'+ id +'">'+ gn.escape(name) +'</option>';
	new Insertion.Bottom('entity.list', html);
}

//=====================================================================================

View.prototype.isDataValid = function()
{
	return this.validator.validate();
}

//=====================================================================================

View.prototype.setEntity = function(entity)
{
	this.currEntity = entity;
	
	var editSrc = $('editor.source');
	var editDes = $('editor.destin');
	
	editDes.style.background = '#FFFFFF';
	
	if (entity == null)
	{
		editSrc.value = '';
		editDes.value = '';
		editDes.readOnly = true;
	}
	else
	{
		var srcLang = $F('lang.source');
		var desLang = $F('lang.destin');
		
		//--- there is a strange bug here if we use $().update(). It seems that when 
		//--- 'editor.destin' is changed, the update function does not work anymore.
		
		editSrc.value = entity.label[srcLang];
		editDes.value = entity.label[desLang];
		editDes.readOnly = false;
	}
}

//=====================================================================================

View.prototype.getData = function()
{
	var data =
	{
		SITE_NAME   : $('site.name')  .value,	
	}
		
	return data;
}

//=====================================================================================
//===
//=== Listeners
//===
//=====================================================================================

View.prototype.langSrcChange = function()
{
	if (this.currEntity == null)
		return;
		
	var srcLang = $F('lang.source');
	
	$('editor.source').value = this.currEntity.label[srcLang];
}

//=====================================================================================

View.prototype.langDesChange = function()
{
	if (this.currEntity == null)
		return;
		
	var desLang = $F('lang.destin');
	var ctrl    = $('editor.destin');
	
	ctrl.value            = this.currEntity.label[desLang];
	ctrl.style.background = '#FFFFFF';
}

//=====================================================================================

View.prototype.textChange = function()
{
	if (this.currEntity != null)
		setTimeout(gn.wrap(this, this.colorUpdate), 10);
}

//-------------------------------------------------------------------------------------

View.prototype.colorUpdate = function()
{
	
	var desLang  = $F('lang.destin');
	var oldValue = this.currEntity.label[desLang];
	var newValue = $F('editor.destin');
	
	if (oldValue == newValue)	$('editor.destin').style.background = '#FFFFFF';
		else							$('editor.destin').style.background = '#FFFFA0';
}

//=====================================================================================
