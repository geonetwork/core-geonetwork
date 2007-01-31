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
/*
	this.validator.add(
	[
		{ id:'site.name', type:'length', minSize :1, maxSize :200 }
	]);
*/
	strLoader.addListener(gn.wrap(this, this.constr_OK));
}

//-------------------------------------------------------------------------------------

View.prototype.constr_OK = function()
{
	gui.setupTooltips(this.strLoader.getNode('tips'));
}

//=====================================================================================
 
View.prototype.getSelectedEntity = function()
{
	var ctrl = $('entity.type');
	
	return ctrl.options[ctrl.selectedIndex].value;
}

//=====================================================================================
//--- data is an array of maps where each map contains (id, name)

View.prototype.setEntityList = function(data)
{
//	$('site.name')  .value = data['SITE_NAME'];
}






//=====================================================================================

View.prototype.isDataValid = function()
{
	return this.validator.validate();
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

