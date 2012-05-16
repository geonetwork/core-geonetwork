//=====================================================================================
//===
//=== View (type:wfsfeatures)
//===
//=====================================================================================

wfsfeatures.View = function(xmlLoader)
{
	HarvesterView.call(this);	
	
	var privilTransf = new XSLTransformer('harvesting/wfsfeatures/client-privil-row.xsl', xmlLoader);
	var resultTransf = new XSLTransformer('harvesting/wfsfeatures/client-result-tip.xsl', xmlLoader);
    
	var loader = xmlLoader;
	var valid  = new Validator(loader);
	var shower = null;
	var selectedSheet = '';
	var selectedTemplateId='';
	
	var currSearchId = 0;
	
	this.setPrefix('wfsfeatures');

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

	Event.observe('wfsfeatures.outputSchema', 'change', ker.wrap(this, changeFragmentSchemaOptions));

	Event.observe('wfsfeatures.icon', 'change', ker.wrap(this, updateIcon));

//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function init()
{
    valid.add(
	[
		{ id:'wfsfeatures.name',        type:'length',   minSize :1,  maxSize :200 },
		{ id:'wfsfeatures.url',    			type:'length',   minSize :1,  maxSize :200 },
		{ id:'wfsfeatures.url',    			type:'url' },
		{ id:'wfsfeatures.username',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'wfsfeatures.password',    type:'length',   minSize :0,  maxSize :200 },

		//--- Must specify a feature based stylesheet if streaming features
		
		{ id:'wfsfeatures.stylesheet', type:'length', minSize:1,
			precond: [{id:'wfsfeatures.streamFeatures', checked:true}]},

		//--- outschema, query and templateId must be specified
		
		{ id:'wfsfeatures.outputSchema', type:'length', minSize:1, maxSize: 200 },
		{ id:'wfsfeatures.query', type:'length', minSize:1, maxSize: 99999999999 },
		{ id:'wfsfeatures.templateId', type:'length', minValue:1, maxValue: 99999999 }
	]);
	shower = new Shower('wfsfeatures.useAccount', 'wfsfeatures.account');

}

//=====================================================================================

function setEmpty()
{
    this.setEmptyCommon();
	
	$('wfsfeatures.lang').value = 'eng';
	$('wfsfeatures.templateId').value = '0';
	$('wfsfeatures.url').value = '';
	$('wfsfeatures.query').value = '';
	$('wfsfeatures.outputSchema').value = '';
	$('wfsfeatures.stylesheet').value = '';
	$('wfsfeatures.streamFeatures').checked = false;
	$('wfsfeatures.createSubtemplates').checked = true;

	var icons = $('wfsfeatures.icon').options;

	for (var i=0; i<icons.length; i++) {
		if (icons[i].value == 'wfs.gif') {
			icons[i].selected = true;
			break;
		}
	}
	
	shower.update();
	updateIcon();

}

//=====================================================================================

function setData(node)
{
	this.setDataCommon(node);

	var site     = node.getElementsByTagName('site')    [0];
	var options  = node.getElementsByTagName('options')[0];

	hvutil.setOption(site, 		'url', 			  			'wfsfeatures.url');
	hvutil.setOption(options, 'lang', 			  		'wfsfeatures.lang');
	hvutil.setOption(options, 'query', 			  		'wfsfeatures.query');
	hvutil.setOption(options, 'outputSchema',   	'wfsfeatures.outputSchema');
	if ($('wfsfeatures.outputSchema').selectedIndex > 0) {
		selectedSheet = hvutil.find(options, 'stylesheet');
		selectedTemplateId = hvutil.find(options, 'templateId');
		changeFragmentSchemaOptions(); 
		hvutil.setOption(options, 'streamFeatures', 		'wfsfeatures.streamFeatures');
		hvutil.setOption(options, 'createSubtemplates', 	'wfsfeatures.createSubtemplates');
	}
	hvutil.setOption(options, 'recordsCategory',  'wfsfeatures.recordsCategory');
	
	//--- add privileges entries
	
	this.removeAllGroupRows();
	this.addGroupRows(node);
	
	//--- set categories

	this.unselectCategories();
	this.selectCategories(node);	
	
	shower.update();
	updateIcon();
}

//=====================================================================================

function getData()
{
    var data = this.getDataCommon();
	
	data.URL    			    	= $F('wfsfeatures.url');
	data.LANG             	= $F('wfsfeatures.lang');
	data.QUERY            	= $F('wfsfeatures.query');
	data.OUTPUTSCHEMA      	= $F('wfsfeatures.outputSchema');
	data.STYLESHEET       	= $F('wfsfeatures.stylesheet');
	data.STREAMFEATURES		= $('wfsfeatures.streamFeatures').checked;
	data.CREATESUBTEMPLATES = $('wfsfeatures.createSubtemplates').checked;
	data.TEMPLATEID       	= $F('wfsfeatures.templateId');
	data.RECORDSCATEGORY  	= $F('wfsfeatures.recordsCategory');
	data.ICON								= $F('wfsfeatures.icon');
	
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
	$('wfsfeatures.icon').options.length = 0;
}

//=====================================================================================

function addIcon(file)
{
	gui.addToSelect('wfsfeatures.icon', file, file);
}

//=====================================================================================

function updateIcon()
{
	var icon = $F('wfsfeatures.icon');
	var image= $('wfsfeatures.icon.image');
	
	image.setAttribute('src', Env.url +'/images/harvesting/'+icon);
}

//=====================================================================================

function changeFragmentSchemaOptions()
{
	var select = $('wfsfeatures.outputSchema');
	if (select.selectedIndex > 0) {

		this.selectedSchema = select[select.selectedIndex].value;

		// load the stylesheets for the chosen schema

		request = ker.createRequestFromObject({
			type: 'wfsFragmentStylesheets',
			schema: this.selectedSchema
		});
		ker.send('xml.harvesting.info', request, ker.wrap(this, retrieveStylesheets_OK));

		// load the templates for the chosen schema

		new InfoService(loader, 'templates', ker.wrap(this, updateTemplates_OK));
	}

	$('wfsFeaturesSchemaOptions').show();
}


//=====================================================================================

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
	}

	updateSelectFragmentStylesheets(data);
}

//=====================================================================================

function updateSelectFragmentStylesheets(data)
{
	$('wfsfeatures.stylesheet').options.length = 0;
	gui.addToSelect('wfsfeatures.stylesheet', 0, "");

	for (var i=0; i<data.length; i++) {
		var optionValue = '(' + data[i].schema + ') ' + data[i].name;
		if (data[i].id == selectedSheet) {
			gui.addToSelect('wfsfeatures.stylesheet', data[i].id, optionValue, true);
		} else {
			gui.addToSelect('wfsfeatures.stylesheet', data[i].id, optionValue);
		}
	}				
}

//=====================================================================================

function updateTemplates_OK(data)
{
	$('wfsfeatures.templateId').options.length = 0;
	gui.addToSelect('wfsfeatures.templateId', 0, "");

	for (var i=0; i<data.length; i++) {
		if (data[i].schema == this.selectedSchema) {
			var optionValue = '(' + data[i].schema + ') ' + data[i].title;
			if (data[i].id == selectedTemplateId) {
				gui.addToSelect('wfsfeatures.templateId', data[i].id, optionValue, true);
			} else {
				gui.addToSelect('wfsfeatures.templateId', data[i].id, optionValue);
			}
		}
	}				
}

//=====================================================================================

}

