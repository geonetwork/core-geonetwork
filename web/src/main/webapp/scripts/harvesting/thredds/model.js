//=====================================================================================
//===
//=== Model (type:thredds)
//===
//=====================================================================================

thredds.Model = function(xmlLoader)
{
	HarvesterModel.call(this);	

	var loader    = xmlLoader;
	var callBackIcons = null;
	var callBackStylesheets = null;
	
	this.retrieveGroups    = retrieveGroups;
	this.retrieveCategories= retrieveCategories;
	this.retrieveIcons     = retrieveIcons;
	this.getUpdateRequest  = getUpdateRequest;
	this.retrieveTemplates = retrieveTemplates;
	this.retrieveStylesheets = retrieveStylesheets;

//=====================================================================================

function retrieveStylesheets(callBack)
{
	callBackStylesheets = callBack;	

	var request = ker.createRequest('type', 'threddsFragmentStylesheets');
	
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
		
		callBackStylesheets(data);
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

function retrieveIcons(callBack)
{
	callBackIcons = callBack;	

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
		
		callBackIcons(data);
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

function retrieveTemplates(callBack)
{
	new InfoService(loader, 'templates', callBack);
}

//=====================================================================================

var updateTemp = 
' <node id="{ID}" type="{TYPE}">'+ 
'    <site>'+
'      <name>{NAME}</name>'+
'      <url>{CATA_URL}</url>'+
'      <icon>{ICON}</icon>'+
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
'      <topic>{TOPIC}</topic>' +
'      <createServiceMd>{CREATESERVICEMD}</createServiceMd>' +
'      <createThumbnails>{CREATETHUMBNAILS}</createThumbnails>' +
'      <createCollectionDatasetMd>{CREATECOLLECTIONDATASETMD}</createCollectionDatasetMd>' +
'      <createAtomicDatasetMd>{CREATEATOMICDATASETMD}</createAtomicDatasetMd>'+
'      <ignoreHarvestOnCollections>{IGNOREHARVESTONCOLLECTIONS}</ignoreHarvestOnCollections>'+
'      <collectionGeneration>{COLLECTIONGENERATION}</collectionGeneration>' +
'      <collectionFragmentStylesheet>{COLLECTIONFRAGMENTSTYLESHEET}</collectionFragmentStylesheet>' +
'      <createCollectionSubtemplates>{CREATECOLLECTIONSUBTEMPLATES}</createCollectionSubtemplates>' +
'      <collectionMetadataTemplate>{COLLECTIONMETADATATEMPLATE}</collectionMetadataTemplate>' +
'      <outputSchemaOnCollections>{OUTPUTSCHEMAONCOLLECTIONS}</outputSchemaOnCollections>'+
'      <ignoreHarvestOnAtomics>{IGNOREHARVESTONATOMICS}</ignoreHarvestOnAtomics>'+
'      <atomicGeneration>{ATOMICGENERATION}</atomicGeneration>' +
'      <atomicFragmentStylesheet>{ATOMICFRAGMENTSTYLESHEET}</atomicFragmentStylesheet>' +
'      <createAtomicSubtemplates>{CREATEATOMICSUBTEMPLATES}</createAtomicSubtemplates>' +
'      <atomicMetadataTemplate>{ATOMICMETADATATEMPLATE}</atomicMetadataTemplate>' +
'      <outputSchemaOnAtomics>{OUTPUTSCHEMAONATOMICS}</outputSchemaOnAtomics>'+
'      <datasetCategory>{DATASETCATEGORY}</datasetCategory>'+
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
