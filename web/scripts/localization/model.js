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

	gn.send('xml.info', request, gn.wrap(this, this.getEntityList_OK));		
}

//-------------------------------------------------------------------------------------

Model.prototype.getEntityList_OK = function(xml)
{
	//--- skip the document node
	xml = xml.firstChild;

	if (xml.nodeName == 'error')
		gn.showError(this.strLoader.getText('cannotGetList'), xml);
	else
	{
		//--- now xml = container
		xml = gn.children(xml)[0];
		             
		var data = [];
		var list = gn.children(xml);
		
		for (var i=0; i<list.length; i++)
			data.push(this.convertEntity(list[i]));
		
		this.callBack(data);
	}
}

//-------------------------------------------------------------------------------------

Model.prototype.convertEntity = function(xml)
{
	var data = 
	{
		id : xml.getAttribute('id')
	};
	
	var node = xml.firstChild;
	
	while (node != null)
	{
		if (node.nodeType == Node.ELEMENT_NODE)
		{
			var name = node.nodeName;
			var value= node.textContent;
			
			if (name == 'label')	data[name] = this.convertLabel(node);
				else					data[name] = value;
		}
		
		node = node.nextSibling;
	}
	
	return data;
}

//-------------------------------------------------------------------------------------

Model.prototype.convertLabel = function(xml)
{
	var data = {};	
	var node = xml.firstChild;
	
	while (node != null)
	{
		if (node.nodeType == Node.ELEMENT_NODE)
		{
			var name = node.nodeName;
			var value= node.textContent;
			
			data[name] = value;
		}
		
		node = node.nextSibling;
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
	
	var request = gn.substitute(Model.updateTemp, data);
	
	gn.send('xml.'+ entity +'.update', request, gn.wrap(this, this.update_OK));
}

//-------------------------------------------------------------------------------------

Model.prototype.update_OK = function(xml)
{
	//--- skip the document node
	xml = xml.firstChild;
	
	if (xml.nodeName == 'error')
		gn.showError(this.strLoader.getText('cannotSave'), xml);
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
	'regions'    : 'regions'
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
