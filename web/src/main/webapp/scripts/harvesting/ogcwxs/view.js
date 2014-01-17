//=====================================================================================
//===
//=== View (type:ogcwxs)
//===
//=====================================================================================

ogcwxs.View = function(xmlLoader)
{
	HarvesterView.call(this);	
	
	var privilTransf = new XSLTransformer('harvesting/ogcwxs/client-privil-row.xsl', xmlLoader);
	var resultTransf = new XSLTransformer('harvesting/ogcwxs/client-result-tip.xsl', xmlLoader);
    
	var loader = xmlLoader;
	var valid  = new Validator(loader);
	var shower = null;
	var shower1 = null; //COGS - NIWA OGC harvest to template mod
	
	var currSearchId = 0;
	var selectedSchema = '';
	
	this.setPrefix('ogcwxs');

	this.setPrivilTransf(privilTransf);
	this.setResultTransf(resultTransf);

	//--- public methods
	
	this.init           = init;
	this.setEmpty       = setEmpty;
	this.setData        = setData;
	this.getData        = getData;
	this.getType        = getType;
	this.isDataValid    = isDataValid;
	this.clearIcons     = clearIcons;
	this.addIcon        = addIcon;
	
	this.clearOutputSchemas = clearOutputSchemas;
	this.addOutputSchema    = addOutputSchema;
	this.reapplySelectedSchema  = reapplySelectedSchema;
	//COGS - NIWA OGC harvest to template mod
	this.retrieveTemplates	= retrieveTemplates;

	Event.observe('ogcwxs.icon', 'change', ker.wrap(this, updateIcon));
	Event.observe('ogcwxs.ogctype', 'change', ker.wrap(this, updateOutputSchemas));

	Event.observe('ogcwxs.outputSchema', 'change', retrieveTemplates);
    //COGS - NIWA OGC harvest to template mod

//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function init()
{
    valid.add(
	[
		{ id:'ogcwxs.name',        type:'length',   minSize :1,  maxSize :200 },
		{ id:'ogcwxs.capabUrl',    type:'length',   minSize :1,  maxSize :200 },
		{ id:'ogcwxs.capabUrl',    type:'url' },
		{ id:'ogcwxs.username',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'ogcwxs.password',    type:'length',   minSize :0,  maxSize :200 }
	]);
	shower = new Shower('ogcwxs.useAccount', 'ogcwxs.account');
	//---COGS - added to support harvest to template for NIWA 1-4-2013 Toggle layer template dropdown
	shower1 = new Shower('ogcwxs.useLayer', 'ogcwxs.template.layer.row');
	
}

//=====================================================================================

function setEmpty()
{
    this.setEmptyCommon();
	
	$('ogcwxs.useLayer').checked = false;
	$('ogcwxs.useLayerMd').checked = false;
	$('ogcwxs.createThumbnails').checked = false;
	$('ogcwxs.ogctype').value = 'WMS111';
	$('ogcwxs.lang').value = 'eng';
	$('ogcwxs.topic').value = '';	
	$('ogcwxs.capabUrl').value = '';
	
	var icons = $('ogcwxs.icon').options;
	
	for (var i=0; i<icons.length; i++)
		if (icons[i].value == 'default.gif')
		{
			icons[i].selected = true;
			break;
		}

	shower.update();
	
	shower1.update(); //---COGS - added to support harvest to template for NIWA 1-4-2013
	
	updateIcon();

	//-- set output schema
	selectedSchema = null; 	
	this.controller.retrieveOutputSchemas(); 

}

//=====================================================================================

function setData(node)
{
	this.setDataCommon(node);

	var site     = node.getElementsByTagName('site')    [0];
	var options  = node.getElementsByTagName('options')[0];

	hvutil.setOption(site, 'url', 				  'ogcwxs.capabUrl');
	hvutil.setOption(site, 'ogctype',		      'ogcwxs.ogctype');
	hvutil.setOption(site, 'icon',            	  'ogcwxs.icon');
	hvutil.setOption(options, 'topic',	 		  'ogcwxs.topic');
	hvutil.setOption(options, 'createThumbnails', 'ogcwxs.createThumbnails');
	hvutil.setOption(options, 'useLayer', 		  'ogcwxs.useLayer');
	hvutil.setOption(options, 'useLayerMd',		  'ogcwxs.useLayerMd');
	hvutil.setOption(options, 'lang', 			  'ogcwxs.lang');
	hvutil.setOption(options, 'datasetCategory',  'ogcwxs.datasetCategory');
	
	//--- add privileges entries
	
	this.removeAllGroupRows();
	this.addGroupRows(node);
	
	//--- set categories

	this.unselectCategories();
	this.selectCategories(node);	
	
	shower.update();

	shower1.update();//---COGS - added to support harvest to template for NIWA 1-4-2013

	updateIcon();

	//-- set output schema
	selectedSchema = hvutil.find(options, 'outputSchema'); 	
	this.controller.retrieveOutputSchemas(); 
	//-- COGS NIWA TODO set option for templates here
}

//-------------------------------------------------------------------------------------

function reapplySelectedSchema()
{
	if (selectedSchema != null)
		$('ogcwxs.outputSchema').value = selectedSchema;
}
		
//=====================================================================================

function getData()
{
    var data = this.getDataCommon();
	
	data.CAPAB_URL        = $F('ogcwxs.capabUrl');
	data.ICON             = $F('ogcwxs.icon');
	data.OGCTYPE          = $F('ogcwxs.ogctype');
	data.LANG             = $F('ogcwxs.lang');
	data.TOPIC            = $F('ogcwxs.topic');
	data.DATASETCATEGORY  = ($F('ogcwxs.datasetCategory')==null?'':$F('ogcwxs.datasetCategory'));
	data.OUTPUTSCHEMA     = $F('ogcwxs.outputSchema');
	data.CREATETHUMBNAILS = $('ogcwxs.createThumbnails').checked;
	data.USELAYER	      = $('ogcwxs.useLayer').checked;
	data.USELAYERMD       = $('ogcwxs.useLayerMd').checked;
	//--- COGS - added to support harvest to template for NIWA 1-4-2013
	data.TEMPLATESERVICE  = $F('ogcwxs.templateService');
	data.TEMPLATELAYER    = $F('ogcwxs.template.layer');
	
	//--- retrieve privileges and categories information
	
	data.PRIVILEGES = this.getPrivileges();
	data.CATEGORIES = this.getSelectedCategories();
		
	return data;
}

//=====================================================================================

function getType()
{
	return $F('ogcwxs.ogctype');
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
	$('ogcwxs.icon').options.length = 0;
}

//=====================================================================================

function addIcon(file)
{
	gui.addToSelect('ogcwxs.icon', file, file);
}

//=====================================================================================

function updateIcon()
{
	var icon = $F('ogcwxs.icon');
	var image= $('ogcwxs.icon.image');
	
	image.setAttribute('src', Env.url +'/images/harvesting/'+icon);
}

//=====================================================================================

function clearOutputSchemas() 
{ 
	$('ogcwxs.outputSchema').options.length = 0;
}

//=====================================================================================

function addOutputSchema(id, name)
{
	gui.addToSelect('ogcwxs.outputSchema', id, name);
}

//=====================================================================================

function updateOutputSchemas() {
	selectedSchema = $F('ogcwxs.outputSchema');
	
	if (this.controller) {
		this.controller.retrieveOutputSchemas();
	}
}

//=====================================================================================
// COGS: Begin edit for NIWA mods 
// COGS - TODO use method from fragments to populate dropdowns

function retrieveTemplates() {
	Ext.Ajax.request({
		url : 'q',
		params : {
			'_schema' : $('ogcwxs.outputSchema').value,
			'template' : 'y',
			'fast' : 'index'
		},
		success : function(r) {
			var xml_ = r.responseXML,
				metadata = xml_.getElementsByTagName('metadata');
			var hostUrl = window.location.href;
			var tmpltUrl = hostUrl.replace("harvesting", "xml.metadata.get?uuid=");

			$('ogcwxs.templateService').options.length = 0;
			$('ogcwxs.template.layer').options.length = 0;

			gui.addToSelect('ogcwxs.templateService', '', "---"); // TODO: I18N
			gui.addToSelect('ogcwxs.template.layer', '', "---"); // TODO: I18N

			for (var i=0; i < metadata.length; i++) {
				var row = metadata[i],
					title = xml.textContent(row.getElementsByTagName('title')[0]),
					uuid = xml.textContent(row.getElementsByTagName('uuid')[0]);
                // COGS check that a template can actual be retrieved with "xml.metadata.get?uuid="
                 // before adding to the dropdown list
                Ext.Ajax.request({url:tmpltUrl+uuid,
                                      success:(function(tmpltUrl, uuid, title) {
                                        return function(r) {
                                         gui.addToSelect('ogcwxs.templateService', uuid, title);
                                         gui.addToSelect('ogcwxs.template.layer', uuid, title);
                                         //gui.addToSelect('ogcwxs.templateService', r.responseText, title);
                                         //gui.addToSelect('ogcwxs.template.layer', r.responseText, title);
                                        }
                                      })(tmpltUrl, uuid, title),

				});

			}
		gui.addToSelect('ogcwxs.templateService', 'xx', "(NONE)"); // TODO: I18N
		gui.addToSelect('ogcwxs.template.layer', 'xx', "(NONE)"); // TODO: I18N
		}
	});
}

// COGS: End edit for NIWA mods
//=====================================================================================

}

