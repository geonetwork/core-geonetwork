//=====================================================================================
//===
//=== Model (type:Geonetwork)
//===
//=====================================================================================

gn.Model = function(xmlLoader)
{
	HarvesterModel.call(this);	
	
	var loader = xmlLoader;

	this.retrieveSources   = retrieveSources;
	this.retrieveGroups    = retrieveGroups;
	this.retrieveCategories= retrieveCategories;
	this.getUpdateRequest  = getUpdateRequest;
	
//=====================================================================================

function retrieveSources(data, callBack)
{
	this.retrieveSourcesCB = callBack;
	
	var url = 'http://'+ data.HOST;
	
	if (data.PORT != '')
		url += ':'+data.PORT;
		
	url += '/'+data.SERVLET+'/srv/en/xml.info';
	
	new InfoService(loader, 'sources', callBack, url);
}

//=====================================================================================

function retrieveGroups(data, callBack, username, password)
{
	this.retrieveGroupsCB = callBack;
	
	var url = 'http://'+ data.HOST;
	
	if (data.PORT != '')
		url += ':'+data.PORT;
		
	url += '/'+data.SERVLET+'/srv/en/xml.info';
	
	new InfoService(loader, 'groups', callBack, url, username, password);
}

//=====================================================================================

function retrieveCategories(callBack)
{
	new InfoService(loader, 'categories', callBack);
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
	
	//--- substitute group mapping
	
	list = data.GROUP_LIST;
	text = '';
		
	for (var i=0; i<list.length; i++)
		text += str.substitute(groupTemp, list[i]);
	
	request = str.replace(request, '{GROUP_LIST}', text);
	
	return this.substituteCommon(data, request);	
}

//=====================================================================================

var updateTemp = 
' <node id="{ID}" type="{TYPE}">'+ 
'    <site>'+
'      <name>{NAME}</name>'+
'      <host>{HOST}</host>'+
'      <port>{PORT}</port>'+
'      <servlet>{SERVLET}</servlet>'+
'      <account>'+
'        <use>{USE_ACCOUNT}</use>'+
'        <username>{USERNAME}</username>'+
'        <password>{PASSWORD}</password>'+
'      </account>'+
'    </site>'+
    
'    <options>'+
'      <every>{EVERY}</every>'+
'      <oneRunOnly>{ONE_RUN_ONLY}</oneRunOnly>'+
'    </options>'+

'    <searches>'+
'       {SEARCH_LIST}'+
'    </searches>'+
    
'    <groupsCopyPolicy>'+
'       {GROUP_LIST}'+
'    </groupsCopyPolicy>'+

'    <categories>'+
'       {CATEG_LIST}'+
'    </categories>'+
'  </node>';

//=====================================================================================

var searchTemp = 
'    <search>'+
'      <freeText>{TEXT}</freeText>'+
'      <title>{TITLE}</title>'+
'      <abstract>{ABSTRACT}</abstract>'+
'      <keywords>{KEYWORDS}</keywords>'+
'      <digital>{DIGITAL}</digital>'+
'      <hardcopy>{HARDCOPY}</hardcopy>'+
'      <source>'+
'         <uuid>{SOURCE_UUID}</uuid>'+
'         <name>{SOURCE_NAME}</name>'+
'      </source>'+
'    </search>';

//=====================================================================================

var groupTemp = '<group name="{NAME}" policy="{POLICY}"/>';

//=====================================================================================
}
