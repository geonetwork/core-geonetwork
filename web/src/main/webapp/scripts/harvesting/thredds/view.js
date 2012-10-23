//=====================================================================================
//===
//=== View (type:thredds)
//===
//=====================================================================================

thredds.View = function(xmlLoader)
{
	HarvesterView.call(this);	
	
	var privilTransf = new XSLTransformer('harvesting/thredds/client-privil-row.xsl', xmlLoader);
	var resultTransf = new XSLTransformer('harvesting/thredds/client-result-tip.xsl', xmlLoader);
    
	var loader = xmlLoader;
	var valid  = new Validator(loader);
	var shower = null;
	
	
	var currSearchId = 0;
	var selectedSheet = '';
	var selectedTemplateId = '';
	
	this.setPrefix('thredds');

	this.setPrivilTransf(privilTransf);
	this.setResultTransf(resultTransf);


	//--- public methods
	
	this.init           = init;
	this.setEmpty       = setEmpty;
	this.setData        = setData;
	this.getData        = getData;
	this.isDataValid    = isDataValid;
	this.clearIcons     = clearIcons;
	this.addIcon        = addIcon;		
	
	Event.observe('thredds.icon', 'change', ker.wrap(this, updateIcon));
	Event.observe('thredds.collectionDatasetMd', 'click', ker.wrap(this, toggleCollectionDatasetOptions));
	Event.observe('thredds.createDIFCollectionMd', 'click', ker.wrap(this, toggleCollectionMetadataOptions));
	Event.observe('thredds.createFragmentsForCollections', 'click', ker.wrap(this, toggleCollectionMetadataOptions));
	Event.observe('thredds.atomicDatasetMd', 'click', ker.wrap(this, toggleAtomicDatasetOptions));
	Event.observe('thredds.createDIFAtomicMd', 'click', ker.wrap(this, toggleAtomicMetadataOptions));
	Event.observe('thredds.createFragmentsForAtomics', 'click', ker.wrap(this, toggleAtomicMetadataOptions));
	Event.observe('thredds.outputSchemaOnCollectionsFragments', 'change', ker.wrap(this, changeCollectionFragmentSchemaOptions));
	Event.observe('thredds.outputSchemaOnAtomicsFragments', 'change', ker.wrap(this, changeAtomicFragmentSchemaOptions));

//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function init()
{
    valid.add(
	[
		{ id:'thredds.name',        type:'length',   minSize :1,  maxSize :200 },
		{ id:'thredds.cataUrl',    type:'length',   minSize :1,  maxSize :200 },
		{ id:'thredds.cataUrl',    type:'url' },
		{ id:'thredds.username',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'thredds.password',    type:'length',   minSize :0,  maxSize :200 },
		
		//--- stylesheet must be supplied if harvesting metadata fragments
		
		{ id:'thredds.collectionFragmentStylesheet', type:'length', minSize:1,
			precond: [{id:'thredds.collectionDatasetMd', checked:true},
			          {id:'thredds.createFragmentsForCollections', checked:true}]},
		{ id:'thredds.atomicFragmentStylesheet', type:'length', minSize:1,
			precond: [{id:'thredds.atomicDatasetMd', checked:true},
			          {id:'thredds.createFragmentsForAtomics', checked:true}]}
	]);
	shower = new Shower('thredds.useAccount', 'thredds.account');

}

//=====================================================================================

function setEmpty()
{
    this.setEmptyCommon();
	
    $('thredds.createServiceMd').checked = false;
	$('thredds.collectionDatasetMd').checked = false;
	$('thredds.atomicDatasetMd').checked = false;
	$('thredds.createThumbnails').checked = false;
	$('thredds.ignoreHarvestOnCollections').checked = false;
	$('thredds.createDIFCollectionMd').checked = true;
	$('thredds.collectionFragmentStylesheet').value = '';
	$('thredds.createCollectionSubtemplates').checked = false;
	$('thredds.collectionMetadataTemplate').value = '';
	$('thredds.outputSchemaOnCollectionsDIF').value = '';	
	$('thredds.outputSchemaOnCollectionsFragments').value = '';	
	$('thredds.outputSchemaOnAtomicsDIF').value = '';	
	$('thredds.outputSchemaOnAtomicsFragments').value = '';	
	$('thredds.ignoreHarvestOnAtomics').checked = false;
	$('thredds.modifiedOnly').checked = false;
	$('thredds.createDIFAtomicMd').checked = true;
	$('thredds.atomicFragmentStylesheet').value = ''; 
	$('thredds.createAtomicSubtemplates').checked = false;
	$('thredds.atomicMetadataTemplate').value = '';
	$('thredds.lang').value = 'eng';
	$('thredds.topic').value = '';	
	$('thredds.cataUrl').value = '';
	
	var icons = $('thredds.icon').options;
	
	for (var i=0; i<icons.length; i++)
		if (icons[i].value == 'default.gif')
		{
			icons[i].selected = true;
			break;
		}

	shower.update();
	updateIcon();
	toggleCollectionDatasetOptions();
	toggleAtomicDatasetOptions();
}

//=====================================================================================

function setData(node)
{
	this.setDataCommon(node);

	var site     = node.getElementsByTagName('site')    [0];
	var options  = node.getElementsByTagName('options')[0];

	hvutil.setOption(site, 'url', 				  'thredds.cataUrl');
	hvutil.setOption(site, 'icon',            	  'thredds.icon');
	hvutil.setOption(options, 'topic',	 		  'thredds.topic');
	hvutil.setOption(options, 'createThumbnails', 'thredds.createThumbnails');
	hvutil.setOption(options, 'createServiceMd',  'thredds.createServiceMd');

	// Collections
	hvutil.setOption(options, 'createCollectionDatasetMd', 		'thredds.collectionDatasetMd');
	hvutil.setOption(options, 'ignoreHarvestOnCollections', 	'thredds.ignoreHarvestOnCollections');
	hvutil.setRadioOption(options, 'collectionGeneration',		'thredds.collectionGenerationOption');
	hvutil.setOption(options, 'outputSchemaOnCollectionsDIF', 		'thredds.outputSchemaOnCollectionsDIF');
	hvutil.setOption(options, 'outputSchemaOnCollectionsFragments', 		'thredds.outputSchemaOnCollectionsFragments');
	if ($('thredds.outputSchemaOnCollectionsFragments').selectedIndex > 0) {
		selectedSheet = hvutil.find(options, 'collectionFragmentStylesheet');
		selectedTemplateId = hvutil.find(options, 'collectionMetadataTemplate');
		changeCollectionFragmentSchemaOptions();
		hvutil.setOption(options, 'createCollectionSubtemplates', 	'thredds.createCollectionSubtemplates');
	}

	// Atomics
	hvutil.setOption(options, 'createAtomicDatasetMd', 			'thredds.atomicDatasetMd');
	hvutil.setOption(options, 'ignoreHarvestOnAtomics', 		'thredds.ignoreHarvestOnAtomics');
	hvutil.setRadioOption(options, 'atomicGeneration', 			'thredds.atomicGenerationOption');
	hvutil.setOption(options, 'outputSchemaOnAtomicsDIF', 			'thredds.outputSchemaOnAtomicsDIF');
	hvutil.setOption(options, 'outputSchemaOnAtomicsFragments', 			'thredds.outputSchemaOnAtomicsFragments');
	if ($('thredds.outputSchemaOnAtomicsFragments').selectedIndex > 0) {
		selectedSheet = hvutil.find(options, 'atomicFragmentStylesheet');
		selectedTemplateId = hvutil.find(options, 'atomicMetadataTemplate');
		changeAtomicFragmentSchemaOptions();
		hvutil.setOption(options, 'createAtomicSubtemplates',	 	'thredds.createAtomicSubtemplates');
		hvutil.setOption(options, 'modifiedOnly', 					'thredds.modifiedOnly');
	}

	hvutil.setOption(options, 'lang', 			  				'thredds.lang');
	hvutil.setOption(options, 'datasetCategory',  				'thredds.datasetCategory');
	
	
	//--- add privileges entries
	
	this.removeAllGroupRows();
	this.addGroupRows(node);
	
	//--- set categories

	this.unselectCategories();
	this.selectCategories(node);	
	
	shower.update();
	updateIcon();
	toggleCollectionDatasetOptions();
	toggleCollectionMetadataOptions();
	toggleAtomicDatasetOptions();
	toggleAtomicMetadataOptions();
}

//=====================================================================================

function getData()
{
    var data = this.getDataCommon();
	
	data.CATA_URL         = $F('thredds.cataUrl');
	data.ICON             = $F('thredds.icon');
	data.LANG             = $F('thredds.lang');
	data.TOPIC            = $F('thredds.topic');
	data.CREATESERVICEMD			= $('thredds.createServiceMd').checked;
	data.OUTPUTSCHEMAONCOLLECTIONSDIF = $F('thredds.outputSchemaOnCollectionsDIF');
	data.OUTPUTSCHEMAONCOLLECTIONSFRAGMENTS    = $F('thredds.outputSchemaOnCollectionsFragments');
	data.IGNOREHARVESTONCOLLECTIONS   = $('thredds.ignoreHarvestOnCollections').checked;
	data.COLLECTIONGENERATION         = getCheckedValue('thredds.collectionGenerationOption');
	data.COLLECTIONFRAGMENTSTYLESHEET = $F('thredds.collectionFragmentStylesheet');
	data.CREATECOLLECTIONSUBTEMPLATES = $('thredds.createCollectionSubtemplates').checked;
	data.COLLECTIONMETADATATEMPLATE   = $F('thredds.collectionMetadataTemplate');
	data.OUTPUTSCHEMAONATOMICSDIF     = $F('thredds.outputSchemaOnAtomicsDIF');
	data.OUTPUTSCHEMAONATOMICSFRAGMENTS       = $F('thredds.outputSchemaOnAtomicsFragments');
	data.IGNOREHARVESTONATOMICS       = $('thredds.ignoreHarvestOnAtomics').checked;
	data.MODIFIEDONLY                 = $('thredds.modifiedOnly').checked;
	data.ATOMICGENERATION             = getCheckedValue('thredds.atomicGenerationOption');
	data.ATOMICFRAGMENTSTYLESHEET     = $F('thredds.atomicFragmentStylesheet');
	data.CREATEATOMICSUBTEMPLATES     = $('thredds.createAtomicSubtemplates').checked;
	data.ATOMICMETADATATEMPLATE       = $F('thredds.atomicMetadataTemplate');
	data.DATASETCATEGORY              = $F('thredds.datasetCategory');
	data.CREATETHUMBNAILS             = $('thredds.createThumbnails').checked;
	data.CREATECOLLECTIONDATASETMD    = $('thredds.collectionDatasetMd').checked;
	data.CREATEATOMICDATASETMD        = $('thredds.atomicDatasetMd').checked;
	
	//--- retrieve privileges and categories information
	
	data.PRIVILEGES = this.getPrivileges();
	data.CATEGORIES = this.getSelectedCategories();
		
	return data;
}

//=====================================================================================

function isDataValid()
{
	if (!valid.validate())
		return false;
		
	return this.isDataValidCommon();
}

//=====================================================================================

function clearIcons() 
{ 
	$('thredds.icon').options.length = 0;
}

//=====================================================================================

function addIcon(file)
{
	gui.addToSelect('thredds.icon', file, file);
}

//=====================================================================================

function updateIcon()
{
	var icon = $F('thredds.icon');
	var image= $('thredds.icon.image');
	
	image.setAttribute('src', Env.url +'/images/harvesting/'+icon);
}

//=====================================================================================

function toggleCollectionDatasetOptions()
{
	if ($('thredds.collectionDatasetMd').checked) {
		$('collectionDatasetsHarvested').show();	
	} else {
		$('collectionDatasetsHarvested').hide();	
	}
}

//=====================================================================================

function toggleCollectionMetadataOptions()
{
	if ($('thredds.createDIFCollectionMd').checked) {
		$('collectionDIFMetadataOptions').show();	
		$('collectionFragmentOptions').hide();	
	} else {
		$('collectionFragmentOptions').show();	
		if ($('thredds.outputSchemaOnCollectionsFragments').selectedIndex > 0) {
			$('collectionFragmentSchemaOptions').show();
		}
		$('collectionDIFMetadataOptions').hide();	
	}
}

//=====================================================================================

function toggleAtomicDatasetOptions()
{
	if ($('thredds.atomicDatasetMd').checked) {
		$('atomicDatasetsHarvested').show();	
	} else {
		$('atomicDatasetsHarvested').hide();	
	}
}

//=====================================================================================

function toggleAtomicMetadataOptions()
{
	if ($('thredds.createDIFAtomicMd').checked) {
		$('atomicDIFMetadataOptions').show();	
		$('atomicFragmentOptions').hide();	
	} else {
		$('atomicFragmentOptions').show();	
		if ($('thredds.outputSchemaOnAtomicsFragments').selectedIndex > 0) {
			$('atomicFragmentSchemaOptions').show();
		}
		$('atomicDIFMetadataOptions').hide();	
	}
}

//=====================================================================================


function changeAtomicFragmentSchemaOptions()
{
	var select = $('thredds.outputSchemaOnAtomicsFragments');
	if (select.selectedIndex > 0) {

		this.selectedSchema = select[select.selectedIndex].value;

		// load the stylesheets for the chosen schema

		request = ker.createRequestFromObject({
			type: 'threddsFragmentStylesheets',
			schema: this.selectedSchema
		});
		ker.send('xml.harvesting.info', request, ker.wrap(this, retrieveAtomicStylesheets_OK));

		// load the templates for the chosen schema

		new InfoService(loader, 'templates', ker.wrap(this, updateAtomicTemplates_OK));

	}
	$('atomicFragmentSchemaOptions').show();
}

//=====================================================================================

function retrieveAtomicStylesheets_OK(xmlRes)
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
	}

	updateSelectAtomicFragmentStylesheets(data);
}

//=====================================================================================

function updateSelectAtomicFragmentStylesheets(data)
{
	$('thredds.atomicFragmentStylesheet').options.length = 0;
	gui.addToSelect('thredds.atomicFragmentStylesheet', 0, "");

	for (var i=0; i<data.length; i++) {
		var optionValue = '(' + data[i].schema + ') ' + data[i].name;
			if (data[i].id == selectedSheet) {
				gui.addToSelect('thredds.atomicFragmentStylesheet', data[i].id, optionValue, true);
			} else {
				gui.addToSelect('thredds.atomicFragmentStylesheet', data[i].id, optionValue);
			}
	}				
}

//=====================================================================================

function updateAtomicTemplates_OK(data)
{
	$('thredds.atomicMetadataTemplate').options.length = 0;
	gui.addToSelect('thredds.atomicMetadataTemplate', 0, "");

	for (var i=0; i<data.length; i++) {
		if (data[i].schema == this.selectedSchema) {
			var optionValue = '(' + data[i].schema + ') ' + data[i].title;
			if (data[i].id == selectedTemplateId) {
				gui.addToSelect('thredds.atomicMetadataTemplate', data[i].id, optionValue, true);
			} else {
				gui.addToSelect('thredds.atomicMetadataTemplate', data[i].id, optionValue);
			}
		}
	}				
}

//=====================================================================================

function changeCollectionFragmentSchemaOptions()
{
	var select = $('thredds.outputSchemaOnCollectionsFragments');
	if (select.selectedIndex > 0) {

		this.selectedSchema = select[select.selectedIndex].value;

		// load the stylesheets for the chosen schema

		request = ker.createRequestFromObject({
			type: 'threddsFragmentStylesheets',
			schema: this.selectedSchema
		});
		ker.send('xml.harvesting.info', request, ker.wrap(this, retrieveCollectionStylesheets_OK));

		// load the templates for the chosen schema

		new InfoService(loader, 'templates', ker.wrap(this, updateCollectionTemplates_OK));
	}

	$('collectionFragmentSchemaOptions').show();
}

//=====================================================================================

function retrieveCollectionStylesheets_OK(xmlRes)
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
	}

	updateSelectCollectionFragmentStylesheets(data);
}

//=====================================================================================

function updateSelectCollectionFragmentStylesheets(data)
{
	$('thredds.collectionFragmentStylesheet').options.length = 0;
	gui.addToSelect('thredds.collectionFragmentStylesheet', 0, "");

	for (var i=0; i<data.length; i++) {
		var optionValue = '(' + data[i].schema + ') ' + data[i].name;
		if (data[i].id == selectedSheet) {
			gui.addToSelect('thredds.collectionFragmentStylesheet', data[i].id, optionValue, true);
		} else {
			gui.addToSelect('thredds.collectionFragmentStylesheet', data[i].id, optionValue);
		}
	}				
}

//=====================================================================================

function updateCollectionTemplates_OK(data)
{
	$('thredds.collectionMetadataTemplate').options.length = 0;
	gui.addToSelect('thredds.collectionMetadataTemplate', 0, "");

	for (var i=0; i<data.length; i++) {
		if (data[i].schema == this.selectedSchema) {
			var optionValue = '(' + data[i].schema + ') ' + data[i].title;
			if (data[i].id == selectedTemplateId) {
				gui.addToSelect('thredds.collectionMetadataTemplate', data[i].id, optionValue, true);
			} else {
				gui.addToSelect('thredds.collectionMetadataTemplate', data[i].id, optionValue);
			}
		}
	}				
}

//=====================================================================================
}

