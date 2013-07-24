//=====================================================================================
//===
//=== Model (type:Geonetwork)
//===
//=====================================================================================

gn.Model = function(xmlLoader)
{
	HarvesterModel.call(this);	
	
	var loader = xmlLoader;
	var callBackStyleSheets = null;

	this.retrieveImportXslts = retrieveImportXslts;
	this.retrieveSources   = retrieveSources;
	this.retrieveGroups    = retrieveGroups;
    this.retrieveLocalGroups = retrieveLocalGroups;
	this.retrieveCategories= retrieveCategories;
	this.getUpdateRequest  = getUpdateRequest;
	
//=====================================================================================

function retrieveLocalGroups(callBack) {
    new InfoService(loader, 'groupsIncludingSystemGroups', callBack);
}

function retrieveSources(data, callBack)
{
	this.retrieveSourcesCB = callBack;

    var url = data.HOST;
	//var url = 'http://'+ data.HOST;
	
	//if (data.PORT != '')
	//	url += ':'+data.PORT;
		
	//url += '/'+data.SERVLET+'/srv/'+Env.lang+'/xml.info';
    url += '/srv/'+Env.lang+'/xml.info';

	new InfoService(loader, 'sources', callBack, url);
}

//=====================================================================================

function retrieveGroups(data, callBack, username, password)
{
	this.retrieveGroupsCB = callBack;

    var url = data.HOST;
    url += '/srv/'+Env.lang+'/xml.info';

    // Check if GeoNetwork node is 2.10 or previous release in
    // order to properly retrieve groups (hack for #150).
    OpenLayers.Request.GET({
        url: url + "?type=groupsIncludingSystemGroups",
        success: function(response){
            if (response.responseXML) {
                new InfoService(loader, 'groupsIncludingSystemGroups', callBack, url, username, password);
            } else {
                new InfoService(loader, 'groups', callBack, url, username, password);
            }
        },
        failure: function(response){
            new InfoService(loader, 'groups', callBack, url, username, password);
        }
    });
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
'    <ownerGroup><id>{OWNERGROUP}</id></ownerGroup>'+
'    <site>'+
'      <name>{NAME}</name>'+
'      <host>{HOST}</host>'+
'      <createRemoteCategory>{CREATE_REMOTE_CATEGORY}</createRemoteCategory>'+
'      <mefFormatFull>{MEF_FULL}</mefFormatFull>'+
'      <xslfilter>{XSLFILTER}</xslfilter>'+
'      <account>'+
'        <use>{USE_ACCOUNT}</use>'+
'        <username>{USERNAME}</username>'+
'        <password>{PASSWORD}</password>'+
'      </account>'+
'    </site>'+
    
'    <options>'+
'      <oneRunOnly>{ONE_RUN_ONLY}</oneRunOnly>'+

'      <every>{EVERY}</every>'+

'    </options>'+

'    <content>'+
'      <validate>{VALIDATE}</validate>'+
'      <importxslt>{IMPORTXSLT}</importxslt>'+
'    </content>'+

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
'      <anyField>{ANYFIELD}</anyField>'+
'      <anyValue>{ANYVALUE}</anyValue>'+
'      <source>'+
'         <uuid>{SOURCE_UUID}</uuid>'+
'         <name>{SOURCE_NAME}</name>'+
'      </source>'+
'    </search>';

//=====================================================================================

var groupTemp = '<group name="{NAME}" policy="{POLICY}"/>';

//=====================================================================================
}
