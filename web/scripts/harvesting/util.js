//=====================================================================================
//===
//=== Utility methods
//===
//=====================================================================================

var hvutil = new Object();

//=====================================================================================

hvutil.setOption = function(node, name, ctrlId)
{
	var value = hvutil.find(node, name);
	var ctrl  = $(ctrlId);
	var type  = ctrl.getAttribute('type');
	
	if (value == null)
		throw 'Cannot find node with name : '+ name;
	
	if (!ctrl)
		throw 'Cannot find control with id : '+ ctrlId;
		
	if (type == 'checkbox')	ctrl.checked = (value == 'true');		
		else						ctrl.value   = value;
}

//=====================================================================================

hvutil.find = function(node, name)
{
	var array = [ node ];
	
	while (array.length != 0)
	{
		node = array.shift();
		
		if (node.nodeName == name)
			return xml.textContent(node);
			
		node = node.firstChild;
		
		while (node != null)
		{
			if (node.nodeType == Node.ELEMENT_NODE)
				array.push(node);
			
			node = node.nextSibling;
		}
	}
	
	return null;
}

//=====================================================================================
//===
//=== Every
//===
//=====================================================================================

function Every(every)
{
	if (typeof every == 'string')
		every = parseInt(every);
	
	this.mins = every % 60;
	
	every -= this.mins;	
	this.hours = every / 60 % 24;	
	this.days  = (every - this.hours * 60) / 1440;	
}

//=====================================================================================

Every.build = function(days, hours, mins)
{
	if (typeof days == 'string')
		days = parseInt(days);
	
	if (typeof hours == 'string')
		hours = parseInt(hours);
	
	if (typeof mins == 'string')
		mins = parseInt(mins);
		
	return days*1440 + hours*60 + mins;
}

//=====================================================================================
