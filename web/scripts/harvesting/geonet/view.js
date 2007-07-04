//=====================================================================================
//===
//=== View (type:Geonetwork)
//===
//=====================================================================================

gn.View = function(xmlLoader)
{
	HarvesterView.call(this);	

	var searchTransf = new XSLTransformer('harvesting/geonet/client-search-row.xsl', xmlLoader);
	var policyTransf = new XSLTransformer('harvesting/geonet/client-policy-row.xsl', xmlLoader);
	var resultTransf = new XSLTransformer('harvesting/geonet/client-result-tip.xsl', xmlLoader);
	
	var loader = xmlLoader;
	var valid  = new Validator(loader);
	var shower = null;
	var sources= null;
	
	var currSearchId = 0;
		
	this.setPrefix('gn');
	this.setResultTransf(resultTransf);
	
	//--- public methods
	
	this.init       = init;
	this.setEmpty   = setEmpty;
	this.setData    = setData;
	this.getData    = getData;
	this.isDataValid= isDataValid;
	this.setSources = setSources;

	this.addEmptySearch  = addEmptySearch;
	this.addSearch       = addSearch;
	this.removeSearch    = removeSearch;
	this.removeAllSearch = removeAllSearch;

	this.getHostData = getHostData;

	this.getPolicyGroups       = getPolicyGroups;
	this.getListedPolicyGroups = getListedPolicyGroups;
	this.addPolicyGroup        = addPolicyGroup;
	this.removePolicyGroup     = removePolicyGroup;
	this.removeAllPolicyGroups = removeAllPolicyGroups;
	this.findPolicyGroup       = findPolicyGroup;
	
	this.removeAllGroupRows = function(){}
        
//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function init()
{
	valid.add(
	[
		{ id:'gn.name',     type:'length',   minSize :1,  maxSize :200 },
		{ id:'gn.host',     type:'length',   minSize :1,  maxSize :200 },
		{ id:'gn.host',     type:'hostname' },
		{ id:'gn.port',     type:'integer',  minValue:80, maxValue:65535, empty:true },
		{ id:'gn.servlet',  type:'length',   minSize :1,  maxSize :200 },
		{ id:'gn.servlet',  type:'alphanum' },
		{ id:'gn.username', type:'length',   minSize :0,  maxSize :200 },
		{ id:'gn.password', type:'length',   minSize :0,  maxSize :200 },
		
		{ id:'gn.every.days',  type:'integer',  minValue:0, maxValue:99 },
		{ id:'gn.every.hours', type:'integer',  minValue:0, maxValue:23 },
		{ id:'gn.every.mins',  type:'integer',  minValue:0, maxValue:59 }
	]);

	shower = new Shower('gn.useAccount',  'gn.account');
}

//=====================================================================================

function setEmpty()
{
	this.setEmptyCommon();
	sources = null;
	
	removeAllSearch();
	removeAllPolicyGroups();
	
	$('gn.host')      .value = '';	
	$('gn.port')      .value = '';
	$('gn.servlet')   .value = '';
		
	shower.update();
}

//=====================================================================================

function setData(node)
{
	this.setDataCommon(node);
	sources = null;

	var site     = node.getElementsByTagName('site')            [0];
	var searches = node.getElementsByTagName('searches')        [0];
	var policies = node.getElementsByTagName('groupsCopyPolicy')[0];

	hvutil.setOption(site, 'host',    'gn.host');
	hvutil.setOption(site, 'port',    'gn.port');
	hvutil.setOption(site, 'servlet', 'gn.servlet');
	
	//--- add search entries
	
	var list = searches.getElementsByTagName('search');
	
	removeAllSearch();
	
	for (var i=0; i<list.length; i++)
		addSearch(list[i]);
	
	//--- add group mapping
	
	var list = policies.getElementsByTagName('group');
	
	removeAllPolicyGroups();
	
	for (var i=0; i<list.length; i++)
	{
		var name  = list[i].getAttribute('name');
		var policy= list[i].getAttribute('policy');
		
		addPolicyGroup(name, policy);
	}
	
	//--- set categories

	this.unselectCategories();
	this.selectCategories(node);	
		
	shower.update();
}

//=====================================================================================

function getData()
{
	var data = this.getDataCommon();
	
	data.HOST    = $F('gn.host');
	data.PORT    = $F('gn.port');
	data.SERVLET = $F('gn.servlet');	
	
	//--- retrieve search information
	
	var searchData = [];
	var searchList = xml.children($('gn.searches'));
	
	for(var i=0; i<searchList.length; i++)
	{
		var divElem    = searchList[i];
		var sourceElem = xml.getElementById(divElem, 'gn.source')
	
		searchData.push(
		{
			TEXT        : xml.getElementById(divElem, 'gn.text')    .value,
			TITLE       : xml.getElementById(divElem, 'gn.title')   .value,
			ABSTRACT    : xml.getElementById(divElem, 'gn.abstract').value,
			KEYWORDS    : xml.getElementById(divElem, 'gn.keywords').value,
			DIGITAL     : xml.getElementById(divElem, 'gn.digital') .checked,
			HARDCOPY    : xml.getElementById(divElem, 'gn.hardcopy').checked,
			SOURCE_UUID : $F(sourceElem),
			SOURCE_NAME : xml.textContent(sourceElem.options[sourceElem.selectedIndex])
		});
	}
	
	data.SEARCH_LIST = searchData;
	
	//--- retrieve categories information
	
	data.CATEGORIES = this.getSelectedCategories();
	
	//--- add group mapping information
		
	data.GROUP_LIST = getPolicyGroups();
	
	return data;
}

//=====================================================================================

function isDataValid()
{
	if (!valid.validate())
		return false;
		
	return this.isDataValidCommon();
}

//=====================================================================================
//=== Sources methods
//=====================================================================================

function setSources(data)
{
	sources = data;
	
	//--- update searches
	
	var searchData = [];
	var list = xml.children($('gn.searches'));
	
	for(var i=0; i<list.length; i++)
	{
		var source  = xml.getElementById(list[i], 'gn.source');
		var selUuid = $F(source);
		
		//--- remove old sources and add blank option
		
		clearSources(source);
		addSource(source, '', '', selUuid);
		
		//--- add new sources
		
		for (var j=0; j<data.length; j++)
		{
			var uuid = data[j].uuid;
			var name = data[j].name;
			
			addSource(source, uuid, name, selUuid);
		}
	}
}

//=====================================================================================
//	return ctrl.options[ctrl.selectedIndex].value;
//=====================================================================================

function clearSources(elem) { elem.options.length = 0; }

//=====================================================================================

function addSource(elem, uuid, label, selUuid)
{
	var html='<option value="'+ uuid +'">'+ xml.escape(label) +'</option>';
	
	if (uuid == selUuid)
		html='<option value="'+ uuid +'" selected="on">'+ xml.escape(label) +'</option>';
	
	new Insertion.Bottom(elem, html);
}

//=====================================================================================
//=== Search methods
//=====================================================================================

function addEmptySearch()
{
	var doc    = Sarissa.getDomDocument();
	var search = doc.createElement('search');
	
	if (sources != null)
	{
		var src = doc.createElement('sources');
		
		for (var i=0; i<sources.length; i++)
		{
			var s = doc.createElement('source');
			
			s.setAttribute('name', sources[i].name);
			s.setAttribute('uuid', sources[i].uuid);
			
			src.appendChild(s);
		}
		
		search.appendChild(src);
	}
	
	doc.appendChild(search);		
	addSearch(search);
}

//=====================================================================================

function addSearch(search)
{
	var id = ''+ currSearchId++;
	search.setAttribute('id', id);
	
	var xslRes = searchTransf.transform(search);
	
	//--- add the new search in list
	new Insertion.Bottom('gn.searches', xml.toString(xslRes));
	
	valid.add(
	[
		{ id:'gn.text',     type:'length',   minSize :0,  maxSize :200 },
		{ id:'gn.title',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'gn.abstract', type:'length',   minSize :0,  maxSize :200 },
		{ id:'gn.keywords', type:'length',   minSize :0,  maxSize :200 }
	], id);
}

//=====================================================================================

function removeSearch(id)
{
	valid.removeByParent(id);
	Element.remove(id);
}

//=====================================================================================

function removeAllSearch()
{
	$('gn.searches').innerHTML = '';
	valid.removeByParent();	
}

//=====================================================================================
//=== Group copy policy methods
//=====================================================================================

function getPolicyGroups()
{
	var groupData = [];
	var groupList = $('gn.groups').getElementsByTagName('TR');
	
	//--- i=1 : skip header
	for(var i=1; i<groupList.length; i++)
	{
		var rowElem = groupList[i];
		var id      = rowElem.getAttribute('id');
		
		//--- format is : gn.group.{@name}
		var name = id.substring(9);
		var list = rowElem.getElementsByTagName('INPUT');
		
		var policy = 'dontCopy';
		
		for (var j=0; j<list.length; j++)
			if (list[j].checked)
				policy = list[j].value;
		
		if (policy != 'dontCopy')
			groupData.push(
			{
				NAME   : name, 
				POLICY : policy
			});
	}
	
	return groupData;
}

//=====================================================================================

function getListedPolicyGroups()
{
	var groupData = [];
	var groupList = $('gn.groups').getElementsByTagName('TR');
	
	//--- i=1 : skip header
	for(var i=1; i<groupList.length; i++)
	{
		var rowElem = groupList[i];
		var id      = rowElem.getAttribute('id');
		
		//--- format is : gn.group.{@name}
		var name = id.substring(9);
		
		groupData.push(name);
	}
	
	return groupData;
}

//=====================================================================================

function addPolicyGroup(name, policy)
{
	var doc  = Sarissa.getDomDocument();
	var group= doc.createElement('group');
	
	group.setAttribute('name',   name);
	group.setAttribute('policy', policy);
	
	var xslRes = policyTransf.transform(group);
	
	//--- add the new group policy row in list
	new Insertion.Bottom('gn.groups', xml.toString(xslRes));	
}

//=====================================================================================

function removePolicyGroup(name)
{
	Element.remove('gn.group.'+ name);
}

//=====================================================================================

function removeAllPolicyGroups()
{
	var rows = $('gn.groups').getElementsByTagName('TR');
	
	for (var i=rows.length-1; i>0; i--)
		Element.remove(rows[i]);		
}

//=====================================================================================

function findPolicyGroup(name)
{
	var list = $('gn.groups').getElementsByTagName('TR');
	
	for (var i=1; i<list.length; i++)
	{
		var row  = list[i];
		var gname= row.getAttribute('id');
		
		if ('gn.group.'+ name == gname)
			return row;
	}
	
	return null;
}

//=====================================================================================
//=== Other methods
//=====================================================================================

function getHostData()
{
	var data = 
	{
		HOST       : $F('gn.host'),
		PORT       : $F('gn.port'),
		SERVLET    : $F('gn.servlet'),
		USERNAME   : $F('gn.username'),
		PASSWORD   : $F('gn.password'),
		USE_ACCOUNT: $('gn.useAccount').checked
	};
	
	return data;
}

//=====================================================================================
}

