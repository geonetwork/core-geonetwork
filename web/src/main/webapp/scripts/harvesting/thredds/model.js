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
	var callBackSchemasDIF = null;
	var callBackSchemasFragments = null;
	
	this.retrieveGroups    = retrieveGroups;
	this.retrieveCategories= retrieveCategories;
	this.retrieveIcons     = retrieveIcons;
	this.getUpdateRequest  = getUpdateRequest;
	this.retrieveSchemasDIF = retrieveSchemasDIF;
	this.retrieveSchemasFragments = retrieveSchemasFragments;

//=====================================================================================

function retrieveSchemasDIF(callBack)
{
	callBackSchemasDIF = callBack;	

	var request = ker.createRequest('type', 'threddsDIFSchemas');
	
	ker.send('xml.harvesting.info', request, ker.wrap(this, retrieveSchemasDIF_OK));
}

//-------------------------------------------------------------------------------------

function retrieveSchemasFragments(callBack)
{
	callBackSchemasFragments = callBack;	

	var request = ker.createRequest('type', 'threddsFragmentSchemas');
	
	ker.send('xml.harvesting.info', request, ker.wrap(this, retrieveSchemasFragments_OK));
}

//-------------------------------------------------------------------------------------

function retrieveSchemasDIF_OK(xmlRes)
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
		
		callBackSchemasDIF(data);
	}
}

//-------------------------------------------------------------------------------------

function retrieveSchemasFragments_OK(xmlRes)
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

var updateTemp = 
' <node id="{ID}" type="{TYPE}">'+
    '    <ownerGroup><id>{OWNERGROUP}</id></ownerGroup>'+
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
'      <oneRunOnly>{ONE_RUN_ONLY}</oneRunOnly>'+

'      <every>{EVERY}</every>'+

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
'      <outputSchemaOnCollectionsDIF>{OUTPUTSCHEMAONCOLLECTIONSDIF}</outputSchemaOnCollectionsDIF>'+
'      <outputSchemaOnCollectionsFragments>{OUTPUTSCHEMAONCOLLECTIONSFRAGMENTS}</outputSchemaOnCollectionsFragments>'+
'      <ignoreHarvestOnAtomics>{IGNOREHARVESTONATOMICS}</ignoreHarvestOnAtomics>'+
'      <modifiedOnly>{MODIFIEDONLY}</modifiedOnly>'+
'      <atomicGeneration>{ATOMICGENERATION}</atomicGeneration>' +
'      <atomicFragmentStylesheet>{ATOMICFRAGMENTSTYLESHEET}</atomicFragmentStylesheet>' +
'      <createAtomicSubtemplates>{CREATEATOMICSUBTEMPLATES}</createAtomicSubtemplates>' +
'      <atomicMetadataTemplate>{ATOMICMETADATATEMPLATE}</atomicMetadataTemplate>' +
'      <outputSchemaOnAtomicsDIF>{OUTPUTSCHEMAONATOMICSDIF}</outputSchemaOnAtomicsDIF>'+
'      <outputSchemaOnAtomicsFragments>{OUTPUTSCHEMAONATOMICSFRAGMENTS}</outputSchemaOnAtomicsFragments>'+
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
