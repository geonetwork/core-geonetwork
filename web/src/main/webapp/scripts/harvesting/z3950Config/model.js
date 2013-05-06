//=====================================================================================
//===
//=== Model (type:z3950Config)
//===
//=====================================================================================

Z3950Config.Model = function(xmlLoader)
{
	HarvesterModel.call(this);	
	
	var loader = xmlLoader;
	var callBackStyleSheets = null;

	this.retrieveImportXslts = retrieveImportXslts;
    this.retrieveGroups    = retrieveGroups;
	this.getUpdateRequest  = getUpdateRequest;
	
//=====================================================================================

function retrieveGroups(callBack) {
    new InfoService(loader, 'groupsIncludingSystemGroups', callBack);
}

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

function getUpdateRequest(data)
{
	var request = str.substitute(updateTemp, data);
	
	//--- substitute search list
	
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
    '    <ownerGroup><id>{OWNERGROUP}</id></ownerGroup>'+
'    <site>'+
'      <name>{NAME}</name>'+
'      <host>{HOST}</host>'+
'      <port>{PORT}</port>'+
'      <account>'+
'        <use>{USE_ACCOUNT}</use>'+
'        <username>{USERNAME}</username>'+
'        <password>{PASSWORD}</password>'+
'      </account>'+
'    </site>'+
    
'    <options>'+
'      <clearConfig>{CLEARCONFIG}</clearConfig>'+

'      <every>{EVERY}</every>'+

'      <oneRunOnly>{ONE_RUN_ONLY}</oneRunOnly>'+
'    </options>'+

'    <content>'+
'      <validate>{VALIDATE}</validate>'+
'      <importxslt>{IMPORTXSLT}</importxslt>'+
'    </content>'+

'    <searches>'+
'       {SEARCH_LIST}'+
'    </searches>'+
    
'  </node>';

//=====================================================================================

var searchTemp = 
'    <search>'+
'      <freeText>{TEXT}</freeText>'+
'      <title>{TITLE}</title>'+
'      <abstract>{ABSTRACT}</abstract>'+
'      <keywords>{KEYWORDS}</keywords>'+
'      <category>{CATEGORY}</category>'+
'    </search>';

//=====================================================================================
}
