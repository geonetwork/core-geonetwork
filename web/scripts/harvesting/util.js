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
//===
//=== InfoService
//===
//=====================================================================================

function InfoService(xmlLoader, type, callBack, forwardUrl, username, password)
{
	var loader   = xmlLoader;
	var callBackF= callBack;	

	if (forwardUrl == null)
	{
		var request = ker.createRequest('type', type);
	
		ker.send('xml.info', request, ker.wrap(this, retrieve_OK));
	}
	else
	{
		var accountTemp =
			'<account>'+
			'   <username>{USER}</username>'+
			'   <password>{PASS}</password>'+
			'</account>';
		
		var account = '';
		
		if (username != null)
			account = str.substitute(accountTemp, { USER : username, PASS : password});
			
		var forwardTemp =
			'<request>'+
			'   <site>'+
			'      <url>{URL}</url>'+
			'      <type>geonetwork</type>'+
					 account +
			'   </site>'+
			'   <params>'+
			'      <request>'+
			'         <type>{TYPE}</type>'+
			'      </request>'+ 
			'   </params>'+
			'</request>';

		var request = str.substitute(forwardTemp, { URL : forwardUrl, TYPE : type });

		ker.send('xml.forward', request, ker.wrap(this, retrieve_OK));
	}
        
//=====================================================================================

function retrieve_OK(xmlRes)
{
	if (xmlRes.nodeName == 'error')
		ker.showError(loader.getText('cannotRetrieve'), xmlRes);
	else
	{
		var data = [];
		var list = xml.children(xml.children(xmlRes)[0]);
		
		for (var i=0; i<list.length; i++)
			data.push(buildNode(list[i]));				
		
		callBackF(data);
	}
}

//=====================================================================================

function buildNode(node)
{
	var map = {}
	var id  = node.getAttribute('id');

	if (id != null)
		map['id'] = id;

	var list = xml.children(node);

	for (var i=0; i<list.length; i++)
	{
		var child     = list[i];
		var name      = child.nodeName;
		var childList = xml.children(child);

		if (childList.length == 0)
			map[name] = xml.textContent(child);
		else
			map[name] = buildNode(child);
	}

	return map;							
}

//=====================================================================================
}
