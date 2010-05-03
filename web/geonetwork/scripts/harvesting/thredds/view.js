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
	Event.observe('thredds.createDefaultCollectionMd', 'click', ker.wrap(this, toggleCollectionMetadataOptions));
	Event.observe('thredds.createFragmentsForCollections', 'click', ker.wrap(this, toggleCollectionMetadataOptions));
	Event.observe('thredds.atomicDatasetMd', 'click', ker.wrap(this, toggleAtomicDatasetOptions));
	Event.observe('thredds.createDefaultAtomicMd', 'click', ker.wrap(this, toggleAtomicMetadataOptions));
	Event.observe('thredds.createFragmentsForAtomics', 'click', ker.wrap(this, toggleAtomicMetadataOptions));

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
		{ id:'thredds.every.days',  type:'integer',  minValue:0, maxValue:99 },
		{ id:'thredds.every.hours', type:'integer',  minValue:0, maxValue:23 },
		{ id:'thredds.every.mins',  type:'integer',  minValue:0, maxValue:59 },
		
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
	$('thredds.createDefaultCollectionMd').checked = true;
	$('thredds.collectionFragmentStylesheet').value = '';
	$('thredds.createCollectionSubtemplates').checked = false;
	$('thredds.collectionMetadataTemplate').value = '';
	$('thredds.outputSchemaOnCollections').value = '';	
	$('thredds.ignoreHarvestOnAtomics').checked = false;
	$('thredds.createDefaultAtomicMd').checked = true;
	$('thredds.atomicFragmentStylesheet').value = ''; 
	$('thredds.createAtomicSubtemplates').checked = false;
	$('thredds.atomicMetadataTemplate').value = '';
	$('thredds.outputSchemaOnAtomics').value = '';	
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
	hvutil.setOption(options, 'createCollectionDatasetMd', 		'thredds.collectionDatasetMd');
	hvutil.setOption(options, 'createAtomicDatasetMd', 			'thredds.atomicDatasetMd');
	hvutil.setOption(options, 'ignoreHarvestOnAtomics', 		'thredds.ignoreHarvestOnAtomics');
	hvutil.setRadioOption(options, 'atomicGeneration', 			'thredds.atomicGenerationOption');
	hvutil.setOption(options, 'atomicFragmentStylesheet', 		'thredds.atomicFragmentStylesheet');
	hvutil.setOption(options, 'createAtomicSubtemplates',	 	'thredds.createAtomicSubtemplates');
	hvutil.setOption(options, 'atomicMetadataTemplate', 		'thredds.atomicMetadataTemplate');
	hvutil.setOption(options, 'outputSchemaOnAtomics', 			'thredds.outputSchemaOnAtomics');
	hvutil.setOption(options, 'ignoreHarvestOnCollections', 	'thredds.ignoreHarvestOnCollections');
	hvutil.setRadioOption(options, 'collectionGeneration',		'thredds.collectionGenerationOption');
	hvutil.setOption(options, 'collectionFragmentStylesheet', 	'thredds.collectionFragmentStylesheet');
	hvutil.setOption(options, 'createCollectionSubtemplates', 	'thredds.createCollectionSubtemplates');
	hvutil.setOption(options, 'collectionMetadataTemplate', 	'thredds.collectionMetadataTemplate');
	hvutil.setOption(options, 'outputSchemaOnCollections', 		'thredds.outputSchemaOnCollections');
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
	data.OUTPUTSCHEMAONCOLLECTIONS    = $F('thredds.outputSchemaOnCollections');
	data.IGNOREHARVESTONCOLLECTIONS   = $('thredds.ignoreHarvestOnCollections').checked;
	data.COLLECTIONGENERATION         = getCheckedValue('thredds.collectionGenerationOption');
	data.COLLECTIONFRAGMENTSTYLESHEET = $F('thredds.collectionFragmentStylesheet');
	data.CREATECOLLECTIONSUBTEMPLATES = $('thredds.createCollectionSubtemplates').checked;
	data.COLLECTIONMETADATATEMPLATE   = $F('thredds.collectionMetadataTemplate');
	data.OUTPUTSCHEMAONATOMICS        = $F('thredds.outputSchemaOnAtomics');
	data.IGNOREHARVESTONATOMICS       = $('thredds.ignoreHarvestOnAtomics').checked;
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
	if ($('thredds.createDefaultCollectionMd').checked) {
		$('collectionDefaultMetadataOptions').show();	
		$('collectionFragmentOptions').hide();	
	} else {
		$('collectionFragmentOptions').show();	
		$('collectionDefaultMetadataOptions').hide();	
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
	if ($('thredds.createDefaultAtomicMd').checked) {
		$('atomicDefaultMetadataOptions').show();	
		$('atomicFragmentOptions').hide();	
	} else {
		$('atomicFragmentOptions').show();	
		$('atomicDefaultMetadataOptions').hide();	
	}
}

//=====================================================================================
}

