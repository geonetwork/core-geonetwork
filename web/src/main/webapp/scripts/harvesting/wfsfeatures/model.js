//=====================================================================================
//===
//=== Model (type:wfsfeatures)
//===
//=====================================================================================

wfsfeatures.Model = function(xmlLoader)
{
	HarvesterModel.call(this);	

	var loader    = xmlLoader;
	var callBackF = null;
	
	this.retrieveGroups    = retrieveGroups;
	this.retrieveCategories= retrieveCategories;
	this.getUpdateRequest  = getUpdateRequest;
	this.retrieveIcons  	 = retrieveIcons;
	this.retrieveSchemas   = retrieveSchemas;

//=====================================================================================

function retrieveSchemas(callBack)
{
	callBackSchemasFragments = callBack;	

	var request = ker.createRequest('type', 'wfsFragmentSchemas');
	
	ker.send('xml.harvesting.info', request, ker.wrap(this, retrieveSchemas_OK));
}

//-------------------------------------------------------------------------------------

function retrieveSchemas_OK(xmlRes)
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
		
		callBackSchemasFragments(data);
	}
}

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

function retrieveIcons(callBack)
{
	callBackF = callBack;

	var request = ker.createRequest('type', 'icons');

	ker.send('xml.harvesting.info', request, ker.wrap(this, retrieveIcons_OK));
}

//=====================================================================================

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
	//alert (this.substituteCommon(data, request));
	return this.substituteCommon(data, request);
}

//=====================================================================================

var updateTemp = 
' <node id="{ID}" type="{TYPE}">'+
    '    <ownerGroup><id>{OWNERGROUP}</id></ownerGroup>'+
'    <site>'+
'      <name>{NAME}</name>'+
'      <url>{URL}</url>'+
'			 <icon>{ICON}</icon>'+
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
'      <outputSchema>{OUTPUTSCHEMA}</outputSchema>'+
'      <stylesheet>{STYLESHEET}</stylesheet>'+
'      <streamFeatures>{STREAMFEATURES}</streamFeatures>'+
'      <createSubtemplates>{CREATESUBTEMPLATES}</createSubtemplates>' +
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
