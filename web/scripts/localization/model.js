//=====================================================================================
//===
//=== Model class
//===
//=====================================================================================

function Model(strLoader)
{
	this.strLoader = strLoader;
}

//=====================================================================================

Model.prototype.getEntityList = function(entity, callBack)
{
	this.callBack = callBack;
	
	var request = 
		'<info>'+
		'	<type>'+entity+'</type>'+
		'</info>';

	ker.send('xml.info', request, ker.wrap(this, this.getEntityList_OK));		
}

//-------------------------------------------------------------------------------------

Model.prototype.getEntityList_OK = function(node)
{
	if (node.nodeName == 'error')
		ker.showError(this.strLoader.getText('cannotGetList'), node);
	else
	{
		//--- now node = container
		node = xml.children(node)[0];
		             
		var data = [];
		var list = xml.children(node);
		
		for (var i=0; i<list.length; i++)
			data.push(this.convertEntity(list[i]));
		
		this.callBack(data);
	}
}

//-------------------------------------------------------------------------------------

Model.prototype.convertEntity = function(node)
{
	var data = 
	{
		id : node.getAttribute('id')
	};
	
	var list = xml.children(node);
	
	for (var i=0; i<list.length; i++)
	{
		var node = list[i];
		var name = node.nodeName;
		var value= xml.textContent(node);
			
		if (name == 'label')	data[name] = this.convertLabel(node);
			else					data[name] = value;
	}
	
	return data;
}

//-------------------------------------------------------------------------------------

Model.prototype.convertLabel = function(node)
{
	var data = {};	
	
	var list = xml.children(node);
	
	for (var i=0; i<list.length; i++)
	{
		var node = list[i];
		var name = node.nodeName;
		var value= xml.textContent(node);
			
		data[name] = value;
	}
	
	return data;
}

//=====================================================================================

Model.prototype.update = function(data, callBack)
{
	this.callBack = callBack;
	
	var type   = data['TYPE'];
	var entity = Model.entities[type];
	
	data['ENTITY'] = entity;
	
	var request = str.substitute(Model.updateTemp, data);
	
	ker.send('xml.'+ entity +'.update', request, ker.wrap(this, this.update_OK));
}

//-------------------------------------------------------------------------------------

Model.prototype.update_OK = function(node)
{
	if (node.nodeName == 'error')
		ker.showError(this.strLoader.getText('cannotSave'), node);
	else
	{
		if (this.callBack)
			this.callBack();
	}
}

//=====================================================================================
//=== Private methods (or, at least, they should be so...)
//=====================================================================================

Model.entities = 
{
	'groups'     : 'group',
	'categories' : 'category',
	'operations' : 'operation',
	'regions'    : 'region'
};

//=====================================================================================

Model.updateTemp =
'<request>'+
'   <{ENTITY} id="{ID}">'+
'      <label>'+
'         <{LANG}>{TEXT}</{LANG}>'+
'      </label>'+
'   </{ENTITY}>'+
'</request>';

//=====================================================================================
