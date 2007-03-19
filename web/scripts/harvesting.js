//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

var harvesting = new Harvesting();
var xmlLoader  = new XMLLoader(Env.locUrl +'/xml/harvesting.xml');
var view       = new HarvestView(xmlLoader);

//=====================================================================================

function init()
{
	view.init();
	harvesting.refresh();
	harvesting.refreshGroups();
}

//=====================================================================================
//===
//=== Harvesting class
//===
//=====================================================================================

function Harvesting() {}

//=====================================================================================

Harvesting.prototype.edit = function(id)
{
	var req = gn.createRequest('id', id);
	
	gn.send('xml.harvesting.get', req, gn.wrap(this, this.editSuccess), true);
}

//-------------------------------------------------------------------------------------

Harvesting.prototype.editSuccess = function(xml)
{
	//--- skip the document node
	var xmlEntry = xml.firstChild;

	if (xmlEntry.nodeName == 'error')
		gn.showError(xmlLoader.getText('cannotGet'), xmlEntry);
	else
		view.edit(xmlEntry);
}

//=====================================================================================

Harvesting.prototype.update = function()
{
	if (!view.isEditDataValid())
		return;
			
	//--- create request
	
	var data    = view.getEditData();
	var template= this.getTemplate();	
	var request = gn.substitute(template, data);
	
	//--- add search list to request (in case of a 'geonetwork' node)
	
	var list = data.SEARCH_LIST;
	
	if (list)
	{
		var text = '';
		
		for (var i=0; i<list.length; i++)
			text += gn.substitute(searchTemp_GN, list[i]);
	
		request = gn.replace(request, '{SEARCHES}', text);
	}
	
	//--- add privileges to request (in case of a 'webFolder' node)
	
	var list = data.PRIVILEGES;
	
	if (list)
	{
		var text = '';
		
		for (var i=0; i<list.length; i++)
		{
			var groupID = list[i].GROUP;
			var operList= list[i].OPERATIONS;
			
			text += '<group id="'+ groupID +'">';
			
			for (var j=0; j<operList.length; j++)
				text += '<operation name="'+operList[j]+'"/>';			
			
			text += '</group>';			
		}
	
		request = gn.replace(request, '{PRIVIL_LIST}', text);
	}
	
	//--- send add/update request
	
	if (view.isAdding())	
		gn.send('xml.harvesting.add', request, gn.wrap(this, this.addSuccess), true);
	else
		gn.send('xml.harvesting.update', request, gn.wrap(this, this.updateSuccess), true);
}

//-------------------------------------------------------------------------------------

Harvesting.prototype.addSuccess = function(xml)
{
	//--- skip the document node
	var xmlNode = xml.firstChild;
	
	if (xmlNode.nodeName == 'error')
		gn.showError(xmlLoader.getText('cannotAdd'), xmlNode);
	else
	{
		view.append(xmlNode);
		view.show(SHOW.LIST);
	}
}

//-------------------------------------------------------------------------------------

Harvesting.prototype.updateSuccess = function(xml)
{
	//--- skip the document node
	var xmlNode = xml.firstChild;
	
	if (xmlNode.nodeName == 'error')
		gn.showError(xmlLoader.getText('cannotUpdate'), xmlNode);
	else
	{
		view.refresh(xmlNode);
		view.show(SHOW.LIST);
	}
}

//=====================================================================================

Harvesting.prototype.remove = function()
{
	var idList = view.getIdList();
	var request= gn.createRequest('id', idList);
	
	if (idList.length == 0)
		alert(xmlLoader.getText('pleaseSelect'));
	else
	{
		if (confirm(xmlLoader.getText('confirmRemove')) == false)
			return;
		
		gn.send('xml.harvesting.remove', request, gn.wrap(this, this.removeSuccess), true);
	}
}

//-------------------------------------------------------------------------------------

Harvesting.prototype.removeSuccess = function(xml)
{
	//--- skip the document node
	var xmlRoot = xml.firstChild;

	if (xmlRoot.nodeName == 'error')
		gn.showError(xmlLoader.getText('cannotRemove'), xmlRoot);
	else
	{
		var ids = xmlRoot.getElementsByTagName('id');
	
		for (var i=0; i<ids.length; i++)
		{
			var id = ids[i].firstChild.nodeValue;
	
			//--- if the status is not ok we ignore it. Maybe the entry has been already 
			//--- removed or maybe the id is simply wrong. In this case a simple refresh 
			//--- should be enough.
			
			view.remove(id);
		}
	}
}

//=====================================================================================

Harvesting.prototype.refresh = function()
{
	view.removeAll();
	gn.send('xml.harvesting.get', '<request/>', gn.wrap(this, this.refreshSuccess), true);
}

//-------------------------------------------------------------------------------------

Harvesting.prototype.refreshSuccess = function(xml)
{
	var entries = xml.firstChild;
	
	if (entries.nodeName == 'error')
		gn.showError(xmlLoader.getText('cannotGet'), entries);
	else
	{
		//--- add all harvesting entries to list
	
		for (var i=0; i<entries.childNodes.length; i++)
			if (entries.childNodes[i].nodeType == Node.ELEMENT_NODE)
				view.append(entries.childNodes[i]);
	}
}

//=====================================================================================

Harvesting.prototype.start = function()
{
	var idList = view.getIdList();
	var request= gn.createRequest('id', idList);
	
	if (idList.length == 0)
		alert(xmlLoader.getText('pleaseSelect'));
	else
		gn.send('xml.harvesting.start', request, gn.wrap(this, this.startSuccess), true);
}
	
//-------------------------------------------------------------------------------------

Harvesting.prototype.startSuccess = function(xml)
{
	//--- skip the document node
	var xmlRoot = xml.firstChild;

	if (xmlRoot.nodeName == 'error')
		gn.showError(xmlLoader.getText('cannotStart'), xmlRoot);
	else
	{
		var ids = xmlRoot.getElementsByTagName('id');
	
		for (var i=0; i<ids.length; i++)
		{
			var id     = ids[i].firstChild.nodeValue;
			var status = ids[i].getAttribute('status');
	
			if (status == 'ok' || status=='already-active')
			{
				view.unselect(id);
				view.setStarted(id);
			}
		}
	}
}

//=====================================================================================

Harvesting.prototype.stop = function()
{
	var idList = view.getIdList();
	var request= gn.createRequest('id', idList);
	
	if (idList.length == 0)
		alert(xmlLoader.getText('pleaseSelect'));
	else
		gn.send('xml.harvesting.stop', request, gn.wrap(this, this.stopSuccess), true);
}

//-------------------------------------------------------------------------------------

Harvesting.prototype.stopSuccess = function(xml)
{
	//--- skip the document node
	var xmlRoot = xml.firstChild;

	if (xmlRoot.nodeName == 'error')
		gn.showError(xmlLoader.getText('cannotStop'), xmlRoot);
	else
	{
		var ids = xmlRoot.getElementsByTagName('id');
	
		for (var i=0; i<ids.length; i++)
		{
			var id     = ids[i].firstChild.nodeValue;
			var status = ids[i].getAttribute('status');
	
			if (status == 'ok' || status=='already-inactive')
			{
				view.unselect(id);
				view.setStopped(id);
			}
		}
	}
}

//=====================================================================================

Harvesting.prototype.run = function()
{
	var idList = view.getIdList();
	var request= gn.createRequest('id', idList);
	
	if (idList.length == 0)
		alert(xmlLoader.getText('pleaseSelect'));
	else
		gn.send('xml.harvesting.run', request, gn.wrap(this, this.runSuccess), true);
}

//-------------------------------------------------------------------------------------

Harvesting.prototype.runSuccess = function(xml)
{
	//--- skip the document node
	var xmlRoot = xml.firstChild;

	if (xmlRoot.nodeName == 'error')
		gn.showError(xmlLoader.getText('cannotRun'), xmlRoot);
	else
	{
		var ids = xmlRoot.getElementsByTagName('id');
	
		for (var i=0; i<ids.length; i++)
		{
			var id     = ids[i].firstChild.nodeValue;
			var status = ids[i].getAttribute('status');
	
			if (status == 'ok' || status=='already-running')
			{
				view.unselect(id);
				view.setRunning(id);
			}
		}
	}
}

//=====================================================================================

Harvesting.prototype.addSearch = function()
{
	var siteName = view.getSiteName();
	var siteId   = view.getSiteId();
	
	if (siteName == null)
		alert(xmlLoader.getText('pleaseRetrieve'));
	else
		view.addEmptySearch(siteName, siteId);
}

//=====================================================================================

Harvesting.prototype.retrieveSites = function()
{
	var data = view.getHostData();
	
	if (data.HOST == '')
		alert(xmlLoader.getText('supplyHost'));
		
	else if (data.SERVLET == '')
		alert(xmlLoader.getText('supplyServlet'));
		
	else
	{
		var url = 'http://'+ data.HOST;
		
		if (data.PORT != '')
			url += ':'+data.PORT;
			
		url += '/'+data.SERVLET+'/srv/en/xml.info';
		
		var request = gn.substitute(retrieveTemplate, { URL : url });
	
		gn.send('xml.forward', request, gn.wrap(this, this.retrieveSuccess), true);
	}
}

//-------------------------------------------------------------------------------------

Harvesting.prototype.retrieveSuccess = function(xml)
{
	//--- skip the document node
	var xmlNode = xml.firstChild;
	
	if (xmlNode.nodeName == 'error')
		gn.showError(xmlLoader.getText('cannotRetrieve'), xmlNode);
	else
	{
		view.clearSiteId();
		
		var xmlSite  = xmlNode.getElementsByTagName('site')           [0];
		var xmlNodes = xmlNode.getElementsByTagName('harvestingNodes')[0];
		
		var name   = xmlSite.getElementsByTagName('name')  [0].textContent;
		var siteId = xmlSite.getElementsByTagName('siteId')[0].textContent;
		
		view.addSiteId(siteId, name);
		
		var nodeList = xmlNodes.getElementsByTagName('node');
		
		for (var i=0; i<nodeList.length; i++)
		{
			xmlNode = nodeList[i];
		
			name   = xmlNode.getElementsByTagName('name')    [0].textContent;
			siteId = xmlNode.getElementsByTagName('siteId')  [0].textContent;
			count  = xmlNode.getElementsByTagName('metadata')[0].textContent;
			
			if (count == '')
				count = '0';
				
			view.addSiteId(siteId, name +' ('+count+')');				
		}
	}
}

//=====================================================================================
//=== WAF group methods
//=====================================================================================

Harvesting.prototype.refreshGroups = function()
{
	var request = gn.createRequest('type', 'groups');
	
	gn.send('xml.info', request, gn.wrap(this, this.refreshGroupsSuccess), true);
}

//-------------------------------------------------------------------------------------

Harvesting.prototype.refreshGroupsSuccess = function(xml)
{
	//--- skip the document node
	var xmlNode = xml.firstChild;
	
	if (xmlNode.nodeName == 'error')
		gn.showError(xmlLoader.getText('cannotRetrieve'), xmlNode);
	else
	{
		view.clearGroups();
		
		var groups = xmlNode.getElementsByTagName('group');
		
		this.refreshGroupsAdd('0', groups);
		this.refreshGroupsAdd('1', groups);
		
		for (var i=0; i<groups.length; i++)
		{
			var group = groups[i];
		
			var id  = group.getAttribute('id');
			var name= group.getElementsByTagName(Env.lang)[0].textContent;
							
			if (id != '0' && id != '1')
				view.addGroup(id, name);				
		}
	}
}

//-------------------------------------------------------------------------------------

Harvesting.prototype.refreshGroupsAdd = function(selId, groups)
{
	for (var i=0; i<groups.length; i++)
	{
		var group = groups[i];
		
		var id  = group.getAttribute('id');
		var name= group.getElementsByTagName(Env.lang)[0].textContent;
							
		if (id == selId)
			view.addGroup(id, name);				
	}
}

//=====================================================================================

Harvesting.prototype.addGroup = function()
{
	var groups = view.getSelectedGroups();
	
	if (groups.length == 0) alert(xmlLoader.getText('pleaseSelectGroup'));
		else						view.addEmptyGroupRows(groups);
}

//=====================================================================================
//=== Private methods (or, at least, they should be so...)
//=====================================================================================

Harvesting.prototype.getTemplate = function(xml)
{
	var type = view.getEditType();
	 
	 if (type == 'geonetwork')
	 	return updateTemp_GN;
	 	
	 if (type == 'webFolder')
	 	return updateTemp_WAF;
	 	
	 throw 'getEditTemplate : Unknown type : '+ type;
}

//=====================================================================================

var updateTemp_GN = 
' <node id="{ID}" name="{NAME}" type="{TYPE}">'+ 
'    <site>'+
'      <host>{HOST}</host>'+
'      <port>{PORT}</port>'+
'      <servlet>{SERVLET}</servlet>'+
'      <account>'+
'        <use>{USE_ACCOUNT}</use>'+
'        <username>{USERNAME}</username>'+
'        <password>{PASSWORD}</password>'+
'      </account>'+
'    </site>'+
    
'    <searches>'+
'       {SEARCHES}'+
'    </searches>'+
    
'    <options>'+
'      <every>{EVERY}</every>'+
'      <createGroups>{CREATE_GROUPS}</createGroups>'+
'      <createCateg>{CREATE_CATEG}</createCateg>'+
'      <oneRunOnly>{ONE_RUN_ONLY}</oneRunOnly>'+
'    </options>'+
'  </node>';

//=====================================================================================

var searchTemp_GN = 
'    <search>'+
'      <freeText>{TEXT}</freeText>'+
'      <title>{TITLE}</title>'+
'      <abstract>{ABSTRACT}</abstract>'+
'      <keywords>{KEYWORDS}</keywords>'+
'      <digital>{DIGITAL}</digital>'+
'      <hardcopy>{HARDCOPY}</hardcopy>'+
'      <siteId>{SITE_ID}</siteId>'+
'    </search>';

//=====================================================================================

var updateTemp_WAF = 
' <node id="{ID}" name="{NAME}" type="{TYPE}">'+ 
'    <site>'+
'      <url>{URL}</url>'+
'      <account>'+
'        <use>{USE_ACCOUNT}</use>'+
'        <username>{USERNAME}</username>'+
'        <password>{PASSWORD}</password>'+
'      </account>'+
'    </site>'+
    
'    <options>'+
'      <every>{EVERY}</every>'+
'      <oneRunOnly>{ONE_RUN_ONLY}</oneRunOnly>'+
'      <structure>{STRUCTURE}</structure>'+
'      <validate>{VALIDATE}</validate>'+
'    </options>'+

'    <privileges>'+
'       {PRIVIL_LIST}'+
'    </privileges>'+
'  </node>';

//=====================================================================================

var retrieveTemplate =
'<request>'+
'   <url>{URL}</url>'+
'   <params>'+
'      <request>'+
'         <type>site</type>'+
'         <type>harvestingNodes</type>'+
'      </request>'+
'   </params>'+
'</request>';

//=====================================================================================
