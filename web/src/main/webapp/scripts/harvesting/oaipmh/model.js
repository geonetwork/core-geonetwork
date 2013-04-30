//=====================================================================================
//===
//=== Model (type:oaipmh)
//===
//=====================================================================================

oaipmh.Model = function(xmlLoader)
{
	HarvesterModel.call(this);	

	var loader    = xmlLoader;
	var callBackF = null;
	var callBackStyleSheets = null;

	this.retrieveImportXslts = retrieveImportXslts;
	this.retrieveGroups    = retrieveGroups;
	this.retrieveCategories= retrieveCategories;
	this.retrieveIcons     = retrieveIcons;
	this.getUpdateRequest  = getUpdateRequest;
	this.retrieveInfo      = retrieveInfo;

//=====================================================================================

function retrieveGroups(callBack)
{
	new InfoService(loader, 'groupsIncludingSystemGroups', callBack);
}

//=====================================================================================

function retrieveCategories(callBack)
{
	new InfoService(loader, 'categories', callBack);
}

//=====================================================================================

function retrieveImportXslts(callBack)
{
	callBackStyleSheets = callBack;	

	var request = ker.createRequest('type', 'importStylesheets');
	ker.send('xml.harvesting.info', request, ker.wrap(this, retrieveXslts_OK));
}

//=====================================================================================

function retrieveXslts_OK(xmlRes)
{
	if (xmlRes.nodeName == 'error')
		ker.showError(loader.getText('cannotRetrieve'), xmlRes);
	else
	{
		var data = [];
		var list = xml.children(xml.children(xmlRes)[0]);
		
		for (var i=0; i<list.length; i++)
			data.push(xml.toObject(list[i]));
		
		callBackStyleSheets(data);
	}
}

//=====================================================================================

function retrieveIcons(callBack)
{
	callBackF = callBack;	

	var request = ker.createRequest('type', 'icons');
	
	ker.send('xml.harvesting.info', request, ker.wrap(this, retrieveIcons_OK));
}

//-------------------------------------------------------------------------------------

function retrieveIcons_OK(xmlRes)
{
	if (xmlRes.nodeName == 'error')
		ker.showError(loader.getText('cannotRetrieve'), xmlRes);
	else
	{
		var data = [];
		var list = xml.children(xml.children(xmlRes)[0]);
		
		for (var i=0; i<list.length; i++)
			data.push(xml.textContent(list[i]));
		
		callBackF(data);
	}
}

//=====================================================================================

function retrieveInfo(url, callBack)
{
	callBackF = callBack;	

	var request = 
		'<request>'+
		'   <type url="'+ url +'">oaiPmhServer</type>'+
		'</request>';
	
	ker.send('xml.harvesting.info', request, ker.wrap(this, retrieveInfo_OK));
}

//-------------------------------------------------------------------------------------

function retrieveInfo_OK(xmlRes)
{
	if (xmlRes.nodeName == 'error')
		ker.showError(loader.getText('cannotRetrieve'), xmlRes);
	else
	{
		var info  = xml.children(xmlRes)[0];		
		var error = xml.children(info)  [0];
		
		//--- check if the OAI server responded
		
		if (error.nodeName == 'error')
		{
			ker.showError(loader.getText('cannotQueryOai'), error);
			return;
		}
		
		//--- ok, normal processing
		
		var mdForm = xml.children(info, 'formats')[0];
		var mdSets = xml.children(info, 'sets')   [0];
		
		//--- build formats
		
		var formats = [];		
		var list    = xml.children(mdForm);
		
		for (var i=0; i<list.length; i++)
			formats.push(xml.textContent(list[i]));
		
		//--- build sets
		
		var sets = [];
		var list = xml.children(mdSets);
		
		for (var i=0; i<list.length; i++)
		{
			var set = list[i];
			var data=
			{
				NAME  : xml.evalXPath(set, 'name'),
				LABEL : xml.evalXPath(set, 'label')
			};
			
			sets.push(data);
		}
		
		//--- return data
		
		var data = 
		{ 
			FORMATS : formats, 
			SETS    : sets 
		};
		
		callBackF(data);
	}
}

//=====================================================================================

function getUpdateRequest(data)
{
	var request = str.substitute(updateTemp, data);
	
	var list = data.SEARCH_LIST;
	var text = '';
		
	for (var i=0; i<list.length; i++)
		text += str.substitute(searchTemp, list[i]);
	
	request = str.replace(request, '{SEARCH_LIST}', text);
	
	return this.substituteCommon(data, request);
}

//=====================================================================================

var updateTemp = 
' <node id="{ID}" type="{TYPE}">'+
    '<ownerGroup><id>{OWNERGROUP}</id></ownerGroup>'+
'    <site>'+
'      <name>{NAME}</name>'+
'      <url>{URL}</url>'+
'      <icon>{ICON}</icon>'+
'      <account>'+
'        <use>{USE_ACCOUNT}</use>'+
'        <username>{USERNAME}</username>'+
'        <password>{PASSWORD}</password>'+
'      </account>'+
'    </site>'+
    
'    <options>'+
'      <oneRunOnly>{ONE_RUN_ONLY}</oneRunOnly>'+

'      <every>{EVERY}</every>'+

'      <validate>{VALIDATE}</validate>'+
'    </options>'+

'    <content>'+
'      <validate>{VALIDATE}</validate>'+
'      <importxslt>{IMPORTXSLT}</importxslt>'+
'    </content>'+

'    <searches>'+
'       {SEARCH_LIST}'+
'    </searches>'+

'    <privileges>'+
'       {PRIVIL_LIST}'+
'    </privileges>'+

'    <categories>'+
'       {CATEG_LIST}'+
'    </categories>'+
'  </node>';

//=====================================================================================

var searchTemp = 
'    <search>'+
'      <from>{FROM}</from>'+
'      <until>{UNTIL}</until>'+
'      <set>{SET}</set>'+
'      <prefix>{PREFIX}</prefix>'+
'      <stylesheet>{STYLESHEET}</stylesheet>'+
'    </search>';


//=====================================================================================
}
