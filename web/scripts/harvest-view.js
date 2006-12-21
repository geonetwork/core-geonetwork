//=====================================================================================
//===
//=== HarvestView
//=== 
//=== Handles all view related stuff in the MVC pattern
//===
//=== Needs : geonetwork-ajax.js
//===
//=====================================================================================

var SHOW = new Object();

SHOW.LIST = new Object();
SHOW.ADD  = new Object();
SHOW.EDIT = new Object();

//=====================================================================================

function HarvestView(xmlLoader)
{
	this.rowTransf    = new XSLTransformer('harvesting/client-row-builder.xsl',    xmlLoader);
	this.searchTransf = new XSLTransformer('harvesting/client-search-builder.xsl', xmlLoader);
	this.privilTransf = new XSLTransformer('harvesting/client-privil-builder.xsl', xmlLoader);
	
	this.panelSwitcher = new TabSwitcher(['listPanel',   'addPanel',   'editPanel'], 
													 ['listButtons', 'addButtons', 'editButtons']);

	this.editSwitcher = new TabSwitcher(['editPanelGN','editPanelWAF']);
	
	//--- setup validators
	
	this.gnValid  = new Validator(xmlLoader);
	this.wafValid = new Validator(xmlLoader);
}

//=====================================================================================

HarvestView.prototype.init = function()
{
	this.show(SHOW.LIST);
	
	this.gnValid.add(
	[
		{ id:'gn.name',     type:'length',   minSize :1,  maxSize :200 },
		{ id:'gn.host',     type:'length',   minSize :1,  maxSize :200 },
		{ id:'gn.host',     type:'hostname' },
		{ id:'gn.port',     type:'integer',  minValue:80, maxValue:65535, empty:true },
		{ id:'gn.servlet',  type:'length',   minSize :1,  maxSize :200 },
		{ id:'gn.servlet',  type:'alphanum' },
		{ id:'gn.username', type:'length',   minSize :0,  maxSize :200 },
		{ id:'gn.password', type:'length',   minSize :0,  maxSize :200 },
		
		{ id:'gn.every.days',   type:'integer',  minValue:0, maxValue:99 },
		{ id:'gn.every.hours',  type:'integer',  minValue:0, maxValue:23 },
		{ id:'gn.every.mins',   type:'integer',  minValue:0, maxValue:59 }
	]);
	
	this.wafValid.add(
	[
		{ id:'waf.name',        type:'length',   minSize :1,  maxSize :200 },
		{ id:'waf.url',         type:'length',   minSize :1,  maxSize :200 },
		{ id:'waf.url',         type:'url' },
		{ id:'waf.username',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'waf.password',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'waf.every.days',  type:'integer',  minValue:0, maxValue:99 },
		{ id:'waf.every.hours', type:'integer',  minValue:0, maxValue:23 },
		{ id:'waf.every.mins',  type:'integer',  minValue:0, maxValue:59 }
	]);
	
	this.gnShower  = new Shower('gn.useAccount',  'gn.account');	
	this.wafShower = new Shower('waf.useAccount', 'waf.account');
}

//=====================================================================================

HarvestView.prototype.show = function(obj)
{
	if (obj == SHOW.LIST)
		this.panelSwitcher.show('listPanel', 'listButtons');
		
	else if (obj == SHOW.ADD)
		this.panelSwitcher.show('addPanel', 'addButtons');
		
	else if (obj == SHOW.EDIT)
		this.panelSwitcher.show('editPanel', 'editButtons');
		
	else
		throw 'Unknown object to show : '+ obj;		
}

//=====================================================================================
	
HarvestView.prototype.getEditType = function() { return $('edit.type').value; }
HarvestView.prototype.isAdding    = function() { return $('edit.id').value == ''; }

//=====================================================================================

HarvestView.prototype.getHostData = function() 
{
	var data = 
	{
		HOST :    $('gn.host')   .value,
		PORT :    $('gn.port')   .value,
		SERVLET : $('gn.servlet').value
	};
	
	return data;
}

//=====================================================================================

HarvestView.prototype.unselect = function(id) 
{
	$(id).getElementsByTagName('input')[0].checked = false;
}

//=====================================================================================

HarvestView.prototype.setStarted = function(id) 
{
	var img = gn.getElementById($(id), 'status');
	
	img.setAttribute('src', Env.url +'/images/clock.png');
}

//=====================================================================================

HarvestView.prototype.setStopped = function(id) 
{
	var img = gn.getElementById($(id), 'status');
	
	img.setAttribute('src', Env.url +'/images/fileclose.png');
}

//=====================================================================================

HarvestView.prototype.setRunning = function(id) 
{
	var img = gn.getElementById($(id), 'status');
	
	img.setAttribute('src', Env.url +'/images/exec.png');
}

//=====================================================================================
//=== SiteId methods
//=====================================================================================

HarvestView.prototype.getSiteName = function() 
{ 
	var ctrl = $('gn.siteId');
	
	if (ctrl.options.length == 0)
		return null;
		
	return ctrl.options[ctrl.selectedIndex].textContent;
}

//=====================================================================================

HarvestView.prototype.getSiteId = function() 
{ 
	var ctrl = $('gn.siteId');
	
	if (ctrl.options.length == 0)
		return null;
		
	return ctrl.options[ctrl.selectedIndex].value;
}

//=====================================================================================

HarvestView.prototype.clearSiteId = function() { return $('gn.siteId').options.length = 0; }

//=====================================================================================

HarvestView.prototype.addSiteId = function(id, label)
{
	var html='<option value="'+ id +'">'+ gn.escape(label) +'</option>';
	new Insertion.Bottom('gn.siteId', html);
}

//=====================================================================================
//=== Other methods
//=====================================================================================

/* Enters 'add' mode showing the GN or WAF editing panel and filling it with empty data
 */

HarvestView.prototype.add = function()
{
	var type = $('add.type').value;
	
	$('edit.id')  .value= "";
	$('edit.type').value= type;
	
	if (type == 'geonetwork')
	{
		this.setEmpty_GN();
		this.editSwitcher.show('editPanelGN');
	}
	else if (type == 'webFolder')
	{
		this.setEmpty_WAF();
		this.editSwitcher.show('editPanelWAF');
	}

	else
		throw 'Unknown type : '+ type;
	
	//--- show edit panel
	this.show(SHOW.EDIT);
}	

//=====================================================================================

HarvestView.prototype.append = function(xmlNode)
{
	this.rowTransf.transform(xmlNode, this.appendCallBack);
}

//-------------------------------------------------------------------------------------

HarvestView.prototype.appendCallBack = function(xml)
{
	var elRow    = xml.getElementsByTagName('tr')    [0];
	var elStatus = xml.getElementsByTagName('status')[0];
	var elError  = xml.getElementsByTagName('error') [0];
	
	var htmlRow   = gn.xmlToString(elRow);
	var htmlStatus= gn.xmlToString(elStatus);
	var htmlError = gn.xmlToString(elError);
	
	//--- add the new entry in list
	
	new Insertion.Bottom('table', htmlRow);
	
	//--- add proper tooltips for both status and error columns
	
	var id = elRow.getAttribute('id');
	
	var imgStatus = gn.getElementById($(id), 'status');
	var imgError  = gn.getElementById($(id), 'error');
	
	new Tooltip(imgStatus, htmlStatus);
	new Tooltip(imgError,  htmlError);
}

//=====================================================================================
/* Refreshes the content of an entry on the main list 
 */
 
HarvestView.prototype.refresh = function(xmlNode)
{
	this.rowTransf.transform(xmlNode, this.refreshCallBack);
}

//-------------------------------------------------------------------------------------

HarvestView.prototype.refreshCallBack = function(xml)
{
	var elRow    = xml.getElementsByTagName('tr')    [0];
	var elStatus = xml.getElementsByTagName('status')[0];
	var elError  = xml.getElementsByTagName('error') [0];
	
	var htmlRow   = gn.xmlToString(elRow);
	var htmlStatus= gn.xmlToString(elStatus);
	var htmlError = gn.xmlToString(elError);
	
	var id = elRow.getAttribute('id');
	
	//--- now we have to remove the <tr> </tr> root text
	
	var from = htmlRow.indexOf('>') +1;
	var to   = htmlRow.indexOf('</tr>');
	
	$(id).innerHTML = htmlRow.substring(from, to);	
	
	//--- add proper tooltips for both status and error columns
	
	var imgStatus = gn.getElementById($(id), 'status');
	var imgError  = gn.getElementById($(id), 'error');
	
	new Tooltip(imgStatus, htmlStatus);
	new Tooltip(imgError,  htmlError);
}

//=====================================================================================
/* Enters 'edit' mode showing the GN or WAF editing panel and filling it with given data
 */

HarvestView.prototype.edit = function(xmlEntry)
{
	var id   = xmlEntry.getAttribute('id');
	var type = xmlEntry.getAttribute('type');
	
	$('edit.id')  .value= id;
	$('edit.type').value= type;
	
	if (type == 'geonetwork')
	{
		this.setEdit_GN(xmlEntry);
		this.editSwitcher.show('editPanelGN');
	}
	else if (type == 'webFolder')
	{
		this.setEdit_WAF(xmlEntry);
		this.editSwitcher.show('editPanelWAF');
	}

	else
		throw 'Unknown type : '+ type;
	
	//--- show edit panel
	this.show(SHOW.EDIT);
}

//=====================================================================================

HarvestView.prototype.getIdList = function()
{
	var rows = $('table').getElementsByTagName('tr');
	var idList = new Array();
	
	//--- we have to skip the first row, the header
	for (var i=1; i<rows.length; i++)
	{
		var inputs = rows[i].getElementsByTagName('input');
	
		if (inputs[0].checked)
			idList.push(rows[i].id);
	}
	
	return idList;
}

//=====================================================================================

HarvestView.prototype.remove = function(id)
{
	Element.remove(id);
}

//=====================================================================================

HarvestView.prototype.removeAll = function()
{
	gui.removeAllButFirst('table');	
}

//=====================================================================================

HarvestView.prototype.isEditDataValid = function()
{
	var type = $('edit.type').value;
	
	if (type == 'geonetwork')
		return this.isValid_GN();
	
	else if (type == 'webFolder')
		return this.isValid_WAF();
	
	throw 'Unknown type : '+ type;
}

//=====================================================================================

HarvestView.prototype.getEditData = function()
{
	var type = $('edit.type').value;
	
	if (type == 'geonetwork')
		return this.getEdit_GN();
	
	else if (type == 'webFolder')
		return this.getEdit_WAF();
	
	throw 'Unknown type : '+ type;
}

//=====================================================================================
//=== Search methods
//=====================================================================================

HarvestView.prototype.addEmptySearch = function(siteName, siteId)
{
	var doc  = Sarissa.getDomDocument();
	
	var xmlSearch = doc.createElement('search');
	var xmlName   = doc.createElement('siteName');
	var xmlSiteId = doc.createElement('siteId');
	
	doc .appendChild(xmlSearch);	
	xmlSearch.appendChild(xmlName);	
	xmlSearch.appendChild(xmlSiteId);
	
	xmlName  .appendChild(doc.createTextNode(siteName));
	xmlSiteId.appendChild(doc.createTextNode(siteId));
	
	this.addSearch(xmlSearch);
}

//=====================================================================================

HarvestView.prototype.addSearch = function(xmlSearch)
{
	this.searchTransf.transform(xmlSearch, gn.wrap(this, this.addSearchCallBack));
}

//-------------------------------------------------------------------------------------

HarvestView.prototype.addSearchCallBack = function(xml)
{
	var html= gn.xmlToString(xml);
	
	//--- add the new search in list
	new Insertion.Bottom('gn.searches', html);
	
	var siteId = xml.getAttribute('id');
	 
	this.gnValid.add(
	[
		{ id:'gn.text',     type:'length',   minSize :0,  maxSize :200 },
		{ id:'gn.title',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'gn.abstract', type:'length',   minSize :0,  maxSize :200 },
		{ id:'gn.keywords', type:'length',   minSize :0,  maxSize :200 }
	], siteId);
}

//=====================================================================================

HarvestView.prototype.removeSearch = function(siteId)
{
	this.gnValid.removeByParent(siteId);
	Element.remove(siteId);
}

//=====================================================================================

HarvestView.prototype.removeAllSearch = function()
{
	$('gn.searches').innerHTML = '';
	this.gnValid.removeByParent();	
}

//=====================================================================================
//=== Groups methods 
//=====================================================================================

HarvestView.prototype.clearGroups = function() { return $('waf.groups').options.length = 0; }

//=====================================================================================

HarvestView.prototype.addGroup = function(id, label)
{
	var html='<option value="'+ id +'">'+ gn.escape(label) +'</option>';
	new Insertion.Bottom('waf.groups', html);
}

//=====================================================================================

HarvestView.prototype.getSelectedGroups = function() 
{ 
	var ctrl = $('waf.groups');
	
	var result = [];
	
	for (var i=0; i<ctrl.options.length; i++)
		if (ctrl.options[i].selected)
			result.push(ctrl.options[i]);
			
	return result;
}

//=====================================================================================

HarvestView.prototype.addEmptyGroupRows = function(groups)
{
	for (var i=0; i<groups.length; i++)
	{
		var option = groups[i];
		var doc    = Sarissa.getDomDocument();
		var group  = doc.createElement('group');
		var groupId= option.value;
		
		group.setAttribute('id', groupId);
	
		this.addGroupRow(group);
	}
}

//=====================================================================================

HarvestView.prototype.addGroupRow = function(xmlGroup)
{
	//--- retrieve group's name from the list of loaded groups
	
	var id   = xmlGroup.getAttribute('id');
	var name = '???';
	var ctrl = $('waf.groups');
	
	//--- discard group if it has already been added
	
	var list = $('waf.privileges').getElementsByTagName('TR');
	
	for (var i=1; i<list.length; i++)
	{
		//-- 6 is the length of 'group_'
		var groupID = list[i].getAttribute('id').substring(6);
		
		if (id == groupID)
			return;
	}
	
	//--- retrieve group's name
	
	for(var i=0; i<ctrl.options.length; i++)
		if (ctrl.options[i].value == id)
		{
			name = ctrl.options[i].textContent;
//			ctrl.options[i].style.background = '#D0D0D0';
			break;
		}
	
	xmlGroup.setAttribute('name', name);
	
	//--- add group's row
	
	this.privilTransf.transform(xmlGroup, gn.wrap(this, this.addGroupRowCallBack));
}

//-------------------------------------------------------------------------------------

HarvestView.prototype.addGroupRowCallBack = function(xml)
{
	var html= gn.xmlToString(xml);
	
	//--- add the new privilege row to list
	new Insertion.Bottom('waf.privileges', html);
}

//=====================================================================================

HarvestView.prototype.removeGroupRow = function(groupId)
{
	Element.remove(groupId);
}

//=====================================================================================

HarvestView.prototype.removeAllGroupRows = function()
{
	gui.removeAllButFirst('waf.privileges');
}

//=====================================================================================
//===
//=== Private methods. Type = geonetwork
//===
//=====================================================================================

HarvestView.prototype.setEmpty_GN = function()
{
	this.removeAllSearch();
	
	$('gn.name')      .value = '';	
	$('gn.host')      .value = '';	
	$('gn.port')      .value = '';
	$('gn.servlet')   .value = '';
	$('gn.useAccount').checked = true;
	$('gn.username')  .value = '';
	$('gn.password')  .value = '';
		
	$('gn.createGroups').checked = true;
	$('gn.createCateg') .checked = true;
	$('gn.oneRunOnly')  .checked = false;

	$('gn.every.days') .value = '0';
	$('gn.every.hours').value = '1';
	$('gn.every.mins') .value = '30';
	
	this.clearSiteId();
	this.gnShower.update();
}

//=====================================================================================

HarvestView.prototype.setEdit_GN = function(xmlNode)
{
	var xmlSite   = xmlNode.getElementsByTagName('site')    [0];
	var searches  = xmlNode.getElementsByTagName('searches')[0];
	var xmlOptions= xmlNode.getElementsByTagName('options') [0];

	searchesList = searches.getElementsByTagName('search');
	
	$('gn.name').value = xmlNode.getAttribute('name');
	
	this.set(xmlSite,    'host',       'gn.host');
	this.set(xmlSite,    'port',       'gn.port');
	this.set(xmlSite,    'servlet',    'gn.servlet');
	this.set(xmlSite,    'use',        'gn.useAccount');
	this.set(xmlSite,    'username',   'gn.username');
	this.set(xmlSite,    'password',   'gn.password');
	
	//--- add search entries
	
	this.removeAllSearch();
	
	for (var i=0; i<searchesList.length; i++)
		this.addSearch(searchesList[i]);
	
	//--- setup other stuff
	
	this.set(xmlOptions, 'createGroups','gn.createGroups');
	this.set(xmlOptions, 'createCateg', 'gn.createCateg');
	this.set(xmlOptions, 'oneRunOnly',  'gn.oneRunOnly');

	var every = new Every(this.find(xmlOptions, 'every'));
	
	$('gn.every.days') .value = every.days;
	$('gn.every.hours').value = every.hours;
	$('gn.every.mins') .value = every.mins;
	
	this.clearSiteId();
	this.gnShower.update();
}

//=====================================================================================

HarvestView.prototype.getEdit_GN = function()
{
	var days  = $('gn.every.days') .value;
	var hours = $('gn.every.hours').value;
	var mins  = $('gn.every.mins') .value;
	
	var data =
	{
		ID   : $('edit.id')  .value,	
		TYPE : $('edit.type').value,
		
		//--- site
		NAME    : $('gn.name')   .value,
		HOST    : $('gn.host')   .value,
		PORT    : $('gn.port')   .value,
		SERVLET : $('gn.servlet').value,
	
		USE_ACCOUNT: $('gn.useAccount').checked,
		USERNAME   : $('gn.username')  .value,
		PASSWORD   : $('gn.password')  .value,
	
		//--- options		
		EVERY         : Every.build(days, hours, mins),
		CREATE_GROUPS : $('gn.createGroups').checked,
		CREATE_CATEG  : $('gn.createCateg') .checked,
		ONE_RUN_ONLY  : $('gn.oneRunOnly')  .checked
	}
	
	//--- retrieve search information
	
	var searchData = [];
	var searchList = $('gn.searches').childNodes;
	
	for(var i=0; i<searchList.length; i++)
		if (searchList[i].nodeType == Node.ELEMENT_NODE)
		{
			var divElem = searchList[i];
			
			searchData.push(
			{
				TEXT     : gn.getElementById(divElem, 'gn.text')    .value,
				TITLE    : gn.getElementById(divElem, 'gn.title')   .value,
				ABSTRACT : gn.getElementById(divElem, 'gn.abstract').value,
				KEYWORDS : gn.getElementById(divElem, 'gn.keywords').value,		
				DIGITAL  : gn.getElementById(divElem, 'gn.digital') .checked,
				HARDCOPY : gn.getElementById(divElem, 'gn.hardcopy').checked,
				SITE_ID  : divElem.getAttribute('id')
			});
		}
	
	data.SEARCH_LIST = searchData;
	
	return data;
}

//=====================================================================================

HarvestView.prototype.isValid_GN = function()
{
	if (!this.gnValid.validate())
		return false;
		
	var days  = $('gn.every.days') .value;
	var hours = $('gn.every.hours').value;
	var mins  = $('gn.every.mins') .value;
	
	if (Every.build(days, hours, mins) == 0)
	{
		alert(this.xmlLoader.getText('everyZero'));
		return false;
	}
		
	return true;
}

//=====================================================================================
//===
//=== Private methods. Type = webFolder
//===
//=====================================================================================

HarvestView.prototype.setEmpty_WAF = function()
{
	$('waf.name')      .value = '';
	$('waf.url')       .value = '';
	$('waf.useAccount').checked = true;
	$('waf.username')  .value = '';
	$('waf.password')  .value = '';
	
	$('waf.oneRunOnly').checked = false;
	$('waf.structure') .checked = false;
	$('waf.validate')  .checked = false;

	$('waf.every.days') .value = '0';
	$('waf.every.hours').value = '1';
	$('waf.every.mins') .value = '30';
	
	this.removeAllGroupRows();
	this.wafShower.update();
}

//=====================================================================================

HarvestView.prototype.setEdit_WAF = function(xmlNode)
{
	var xmlSite   = xmlNode.getElementsByTagName('site')      [0];
	var xmlPrivil = xmlNode.getElementsByTagName('privileges')[0];
	var xmlOptions= xmlNode.getElementsByTagName('options')   [0];

	$('waf.name').value = xmlNode.getAttribute('name');
	
	this.set(xmlSite,    'url',        'waf.url');
	this.set(xmlSite,    'use',        'waf.useAccount');
	this.set(xmlSite,    'username',   'waf.username');
	this.set(xmlSite,    'password',   'waf.password');
	
	//--- add privileges entries
	
	this.removeAllGroupRows();
	
	var privil = xmlPrivil.getElementsByTagName('group');
	
	for (var i=0; i<privil.length; i++)
		this.addGroupRow(privil[i]);
	
	//--- setup other stuff
	
	this.set(xmlOptions, 'oneRunOnly',  'waf.oneRunOnly');
	this.set(xmlOptions, 'structure',   'waf.structure');
	this.set(xmlOptions, 'validate',    'waf.validate');

	var every = new Every(this.find(xmlOptions, 'every'));
	
	$('waf.every.days') .value = every.days;
	$('waf.every.hours').value = every.hours;
	$('waf.every.mins') .value = every.mins;
	
	this.wafShower.update();
}

//=====================================================================================

HarvestView.prototype.getEdit_WAF = function()
{
	
	var days  = $('waf.every.days') .value;
	var hours = $('waf.every.hours').value;
	var mins  = $('waf.every.mins') .value;
	
	var data =
	{
		ID   : $('edit.id')  .value,
		TYPE : $('edit.type').value,
	
		//--- site	
		NAME : $('waf.name').value,
		URL  : $('waf.url') .value,
	
		USE_ACCOUNT: $('waf.useAccount').checked,
		USERNAME   : $('waf.username')  .value,
		PASSWORD   : $('waf.password')  .value,
	
		//--- options		
		EVERY        : Every.build(days, hours, mins),
		ONE_RUN_ONLY : $('waf.oneRunOnly').checked,
		VALIDATE     : $('waf.validate')  .checked,
		STRUCTURE    : $('waf.structure') .checked
	}
	
	//--- retrieve privileges information
	
	data.PRIVILEGES = [];
	
	var privilList = $('waf.privileges').getElementsByTagName('TR');
	
	for (var i=1; i<privilList.length; i++)
	{
		var trElem    = privilList[i];
		var inputList = trElem.getElementsByTagName('INPUT');
		var groupData = [];
		
		//-- 6 is the length of 'group_'
		var groupID = trElem.getAttribute('id').substring(6);
		
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

HarvestView.prototype.isValid_WAF = function()
{
	if (!this.wafValid.validate())
		return false;
		
	var days  = $('waf.every.days') .value;
	var hours = $('waf.every.hours').value;
	var mins  = $('waf.every.mins') .value;
	
	if (Every.build(days, hours, mins) == 0)
	{
		alert(this.xmlLoader.getText('everyZero'));
		return false;
	}
		
	return true;
}

//=====================================================================================
//===
//=== Generic private methods
//===
//=====================================================================================

HarvestView.prototype.set = function(node, name, ctrlId)
{
	var value = this.find(node, name);
	var ctrl  = $(ctrlId);
	var type  = ctrl.getAttribute('type');
	
	if (value == null)
		throw 'Cannot find node with name : '+ name;
	
	if (!ctrl)
		throw 'Cannot find control with id : '+ ctrlId;
		
	if (type == 'checkbox')	ctrl.checked = (value == 'true');		
		else						ctrl.value = value;
}

//=====================================================================================

HarvestView.prototype.find = function(node, name)
{
	var array = [ node ];
	
	while (array.length != 0)
	{
		node = array.shift();
		
		if (node.nodeName == name)
			if (node.firstChild)	return node.firstChild.nodeValue;
				else					return '';
			
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
