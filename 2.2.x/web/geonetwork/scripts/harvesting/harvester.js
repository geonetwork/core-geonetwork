//=====================================================================================
//===
//=== Harvester
//===
//=====================================================================================

function Harvester()
{
	var currId = null;
	
//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

//--- abstract methods

this.getType      = function() {}
this.getLabel     = function() {}
this.getEditPanel = function() {}
this.getResultTip = function() {}

//=====================================================================================

this.init = function()     
{ 
	this.view.init();        
}

//=====================================================================================

this.setData = function(node) 
{ 
	currId = node.getAttribute('id');
	this.view.setData(node); 
}

//=====================================================================================

this.setEmpty = function()
{ 
	currId = "";
	this.view.setEmpty();    
}

//=====================================================================================

this.getUpdateRequest = function()
{
	if (!this.view.isDataValid())
		return null;
			
	var data = this.view.getData();
	
	data.ID   = currId;
	data.TYPE = this.getType();
	
	return this.model.getUpdateRequest(data);
}

//=====================================================================================
}

