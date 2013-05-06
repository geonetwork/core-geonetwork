//=====================================================================================
//===
//=== Model (type:Geonetwork 2.0.X)
//===
//=====================================================================================

gn20.Model = function(xmlLoader)
{
	var loader = xmlLoader;
    this.retrieveGroups    = retrieveGroups;
	this.getUpdateRequest = getUpdateRequest;
	
//=====================================================================================

function retrieveGroups(callBack) {
    new InfoService(loader, 'groupsIncludingSystemGroups', callBack);
}

function getUpdateRequest(data)
{
	var request = str.substitute(updateTemp, data);
	
	var list = data.SEARCH_LIST;
	var text = '';
		
	for (var i=0; i<list.length; i++)
		text += str.substitute(searchTemp, list[i]);
	
	return str.replace(request, '{SEARCHES}', text);
}

//=====================================================================================

var updateTemp = 
' <node id="{ID}" type="{TYPE}">'+
'<ownerGroup><id>{OWNERGROUP}</id></ownerGroup>'+
'    <site>'+
'      <name>{NAME}</name>'+
'      <host>{HOST}</host>'+
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
'      <oneRunOnly>{ONE_RUN_ONLY}</oneRunOnly>'+

'      <every>{EVERY}</every>'+

'    </options>'+

'    <content>'+
'      <validate>{VALIDATE}</validate>'+
'      <importxslt>{IMPORTXSLT}</importxslt>'+
'    </content>'+

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
'      <siteId>{SITE_ID}</siteId>'+
'    </search>';


//=====================================================================================
}
