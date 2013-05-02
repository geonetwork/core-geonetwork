//=====================================================================================
//===
//=== Model (type:Filesystem)
//===
//=====================================================================================

filesystem.Model = function(xmlLoader)
{
	HarvesterModel.call(this);

	var loader = xmlLoader;
	var callBackF = null;
	var callBackStyleSheets = null;

	this.retrieveImportXslts = retrieveImportXslts;
	this.retrieveGroups    = retrieveGroups;
	this.retrieveCategories = retrieveCategories;
	this.retrieveIcons     = retrieveIcons;
	this.getUpdateRequest = getUpdateRequest;
	
//=====================================================================================

function retrieveGroups(callBack)
{
	new InfoService(loader, 'groupsIncludingSystemGroups', callBack);
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

function retrieveCategories(callBack)
{
	new InfoService(loader, 'categories', callBack);
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

function getUpdateRequest(data)
{
	var request = str.substitute(updateTemp, data);
	
        return this.substituteCommon(data, request);
}

//=====================================================================================

var updateTemp = 
' <node id="{ID}" type="{TYPE}">'+ 
'    <ownerGroup><id>{OWNERGROUP}</id></ownerGroup>'+
'    <site>'+
'      <name>{NAME}</name>'+
'      <directory>{DIRECTORYNAME}</directory>'+
'      <recurse>{RECURSE}</recurse>'+
'      <nodelete>{NODELETE}</nodelete>'+
'      <icon>{ICON}</icon>'+
'    </site>'+
'    <options>'+
'      <every>{EVERY}</every>'+
'      <oneRunOnly>{ONE_RUN_ONLY}</oneRunOnly>'+
'    </options>'+

'    <content>'+
'      <validate>{VALIDATE}</validate>'+
'      <importxslt>{IMPORTXSLT}</importxslt>'+
'    </content>'+

'    <privileges>'+
'       {PRIVIL_LIST}'+
'    </privileges>'+

'    <categories>'+
'       {CATEG_LIST}'+
'    </categories>'+
'  </node>';

//=====================================================================================
}
