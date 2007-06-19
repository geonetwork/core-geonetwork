//=====================================================================================
//===
//=== View (type:WebDav)
//===
//=====================================================================================

wd.View = function(xmlLoader)
{
	var privilTransf = new XSLTransformer('harvesting/webdav/client-privil-row.xsl', xmlLoader);
	var resultTransf = new XSLTransformer('harvesting/webdav/client-result-tip.xsl', xmlLoader);
	
	var loader = xmlLoader;
	var valid  = new Validator(loader);
	var shower = null;
	
	//--- public methods
	
	this.init        = init;
	this.setEmpty    = setEmpty;
	this.setData     = setData;
	this.getData     = getData;
	this.isDataValid = isDataValid;
	this.getResultTip= getResultTip;

	this.clearGroups        = clearGroups;
	this.addGroup           = addGroup;
	this.getSelectedGroups  = getSelectedGroups;
	this.addEmptyGroupRows  = addEmptyGroupRows;
	this.addGroupRow        = addGroupRow;
	this.removeGroupRow     = removeGroupRow;
	this.removeAllGroupRows = removeAllGroupRows;

//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function init()
{
	valid.add(
	[
		{ id:'wd.name',        type:'length',   minSize :1,  maxSize :200 },
		{ id:'wd.url',         type:'length',   minSize :1,  maxSize :200 },
		{ id:'wd.url',         type:'url' },
		{ id:'wd.username',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'wd.password',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'wd.every.days',  type:'integer',  minValue:0, maxValue:99 },
		{ id:'wd.every.hours', type:'integer',  minValue:0, maxValue:23 },
		{ id:'wd.every.mins',  type:'integer',  minValue:0, maxValue:59 }
	]);

	shower = new Shower('wd.useAccount', 'wd.account');
}

//=====================================================================================

function setEmpty()
{
	$('wd.name')      .value = '';
	$('wd.url')       .value = '';
	$('wd.useAccount').checked = true;
	$('wd.username')  .value = '';
	$('wd.password')  .value = '';
	
	$('wd.oneRunOnly').checked = false;
	$('wd.structure') .checked = false;
	$('wd.validate')  .checked = false;

	$('wd.every.days') .value = '0';
	$('wd.every.hours').value = '1';
	$('wd.every.mins') .value = '30';
	
	removeAllGroupRows();
	shower.update();
}

//=====================================================================================

function setData(node)
{
	var site   = node.getElementsByTagName('site')      [0];
	var privil = node.getElementsByTagName('privileges')[0];
	var options= node.getElementsByTagName('options')   [0];

	$('wd.name').value = node.getAttribute('name');
	
	hvutil.setOption(site, 'url',      'wd.url');
	hvutil.setOption(site, 'use',      'wd.useAccount');
	hvutil.setOption(site, 'username', 'wd.username');
	hvutil.setOption(site, 'password', 'wd.password');
	
	//--- add privileges entries
	
	removeAllGroupRows();
	
	var privList = privil.getElementsByTagName('group');
	
	for (var i=0; i<privList.length; i++)
		addGroupRow(privList[i]);
	
	//--- setup other stuff
	
	hvutil.setOption(options, 'oneRunOnly', 'wd.oneRunOnly');
	hvutil.setOption(options, 'structure',  'wd.structure');
	hvutil.setOption(options, 'validate',   'wd.validate');

	var every = new Every(hvutil.find(options, 'every'));
	
	$('wd.every.days') .value = every.days;
	$('wd.every.hours').value = every.hours;
	$('wd.every.mins') .value = every.mins;
	
	shower.update();
}

//=====================================================================================

function getData()
{
	var days  = $('wd.every.days') .value;
	var hours = $('wd.every.hours').value;
	var mins  = $('wd.every.mins') .value;
	
	var data =
	{
		//--- site	
		NAME : $('wd.name').value,
		URL  : $('wd.url') .value,
	
		USE_ACCOUNT: $('wd.useAccount').checked,
		USERNAME   : $('wd.username')  .value,
		PASSWORD   : $('wd.password')  .value,
	
		//--- options		
		EVERY        : Every.build(days, hours, mins),
		ONE_RUN_ONLY : $('wd.oneRunOnly').checked,
		VALIDATE     : $('wd.validate')  .checked,
		STRUCTURE    : $('wd.structure') .checked
	}
	
	//--- retrieve privileges information
	
	data.PRIVILEGES = [];
	
	var privilList = $('wd.privileges').getElementsByTagName('TR');
	
	for (var i=1; i<privilList.length; i++)
	{
		var trElem    = privilList[i];
		var inputList = trElem.getElementsByTagName('INPUT');
		var groupData = [];
		
		//-- the string is 'wd.group.<id>'
		var groupID = trElem.getAttribute('id').split('.')[2];
		
		for (var j=0; j<inputList.length; j++)
			if (inputList[j].checked)
				groupData.push(inputList[j].name);
				
		if (groupData.length != 0)
			data.PRIVILEGES.push(
			{
				GROUP      : groupID,
				OPERATIONS : groupData
			});
	}
		
	return data;
}

//=====================================================================================

function isDataValid()
{
	if (!valid.validate())
		return false;
		
	var days  = $('wd.every.days') .value;
	var hours = $('wd.every.hours').value;
	var mins  = $('wd.every.mins') .value;
	
	if (Every.build(days, hours, mins) == 0)
	{
		alert(loader.getText('everyZero'));
		return false;
	}
		
	return true;
}

//=====================================================================================
//=== Groups methods 
//=====================================================================================

function clearGroups() { return $('wd.groups').options.length = 0; }

//=====================================================================================

function addGroup(id, label)
{
	var html='<option value="'+ id +'">'+ xml.escape(label) +'</option>';
	new Insertion.Bottom('wd.groups', html);
}

//=====================================================================================

function getSelectedGroups() 
{ 
	var ctrl = $('wd.groups');
	
	var result = [];
	
	for (var i=0; i<ctrl.options.length; i++)
		if (ctrl.options[i].selected)
			result.push(ctrl.options[i]);
			
	return result;
}

//=====================================================================================

function addEmptyGroupRows(groups)
{
	for (var i=0; i<groups.length; i++)
	{
		var option = groups[i];
		var doc    = Sarissa.getDomDocument();
		var group  = doc.createElement('group');
		var groupId= option.value;
		
		group.setAttribute('id', groupId);
	
		addGroupRow(group);
	}
}

//=====================================================================================

function addGroupRow(group)
{
	//--- retrieve group's name from the list of loaded groups
	
	var id   = group.getAttribute('id');
	var name = '???';
	var ctrl = $('wd.groups');
	
	//--- discard group if it has already been added
	
	var list = $('wd.privileges').getElementsByTagName('TR');
	
	for (var i=1; i<list.length; i++)
	{
		//-- string is 'wd.group.<id>'
		var groupID = list[i].getAttribute('id').split('.')[2];
		
		if (id == groupID)
			return;
	}
	
	//--- retrieve group's name
	
	for(var i=0; i<ctrl.options.length; i++)
		if (ctrl.options[i].value == id)
		{
			name = xml.textContent(ctrl.options[i]);
//			ctrl.options[i].style.background = '#D0D0D0';
			break;
		}
	
	group.setAttribute('name', name);
	
	//--- add group's row
	
	var html = xml.toString(privilTransf.transform(group));
	
	//--- add the new privilege row to list
	new Insertion.Bottom('wd.privileges', html);
}

//=====================================================================================

function removeGroupRow(groupId)
{
	Element.remove(groupId);
}

//=====================================================================================

function removeAllGroupRows()
{
	gui.removeAllButFirst('wd.privileges');
}

//=====================================================================================

function getResultTip(node)
{
	return xml.toString(resultTransf.transform(node));
}

//=====================================================================================
}

