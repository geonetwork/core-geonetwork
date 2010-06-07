//=====================================================================================
//===
//=== Model (type:metadatafragments)
//===
//=====================================================================================

metadatafragments.Model = function(xmlLoader)
{
	HarvesterModel.call(this);	

	var loader    = xmlLoader;
	var callBackF = null;
	
	this.retrieveStylesheets = retrieveStylesheets;
	this.retrieveGroups    = retrieveGroups;
	this.retrieveCategories= retrieveCategories;
	this.getUpdateRequest  = getUpdateRequest;
	this.retrieveIcons  	 = retrieveIcons;
	this.retrieveTemplates = retrieveTemplates;

//=====================================================================================

function retrieveStylesheets(callBack)
{
	callBackF = callBack;	

	var request = ker.createRequest('type', 'wfsFragmentStylesheets');
	
	ker.send('xml.harvesting.info', request, ker.wrap(this, retrieveStylesheets_OK));
}

//-------------------------------------------------------------------------------------

function retrieveStylesheets_OK(xmlRes)
{
	if (xmlRes.nodeName == 'error')
		ker.showError(loader.getText('cannotRetrieve'), xmlRes);
	else
	{
		var data = [];
		var list = xml.children(xml.children(xmlRes)[0]);
		
		for (var i=0; i<list.length; i++) {
			data.push(xml.toObject(list[i]));
		}
		
		callBackF(data);
	}
}

//=====================================================================================

function retrieveGroups(callBack)
{
	new InfoService(loader, 'groups', callBack);
}

//=====================================================================================

function retrieveCategories(callBack)
{
	new InfoService(loader, 'categories', callBack);
}

//=====================================================================================

function retrieveTemplates(callBack)
{
	new InfoService(loader, 'templates', callBack);
}

//=====================================================================================

function retrieveIcons(callBack)
{
	// nothing - we don't have icons
}

//=====================================================================================

function getUpdateRequest(data)
{
	var request = str.substitute(updateTemp, data);
	//alert (this.substituteCommon(data, request));
	return this.substituteCommon(data, request);
}

//=====================================================================================

var updateTemp = 
' <node id="{ID}" type="{TYPE}">'+ 
'    <site>'+
'      <name>{NAME}</name>'+
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
'      <lang>{LANG}</lang>'+
'      <query>{QUERY}</query>'+
'      <stylesheet>{STYLESHEET}</stylesheet>'+
'      <templateId>{TEMPLATEID}</templateId>'+
'      <recordsCategory>{RECORDSCATEGORY}</recordsCategory>'+
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
