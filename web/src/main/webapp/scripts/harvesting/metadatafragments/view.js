//=====================================================================================
//===
//=== View (type:metadatafragments)
//===
//=====================================================================================

metadatafragments.View = function(xmlLoader)
{
	HarvesterView.call(this);	
	
	var privilTransf = new XSLTransformer('harvesting/metadatafragments/client-privil-row.xsl', xmlLoader);
	var resultTransf = new XSLTransformer('harvesting/metadatafragments/client-result-tip.xsl', xmlLoader);
    
	var loader = xmlLoader;
	var valid  = new Validator(loader);
	var shower = null;
	
	
	var currSearchId = 0;
	
	this.setPrefix('metadatafragments');

	this.setPrivilTransf(privilTransf);
	this.setResultTransf(resultTransf);

	//--- public methods
	
	this.init           = init;
	this.setEmpty       = setEmpty;
	this.setData        = setData;
	this.getData        = getData;
	this.isDataValid    = isDataValid;

//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function init()
{
    valid.add(
	[
		{ id:'metadatafragments.name',        type:'length',   minSize :1,  maxSize :200 },
		{ id:'metadatafragments.url',    			type:'length',   minSize :1,  maxSize :200 },
		{ id:'metadatafragments.url',    			type:'url' },
		{ id:'metadatafragments.username',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'metadatafragments.password',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'metadatafragments.every.days',  type:'integer',  minValue:0, maxValue:99 },
		{ id:'metadatafragments.every.hours', type:'integer',  minValue:0, maxValue:23 },
		{ id:'metadatafragments.every.mins',  type:'integer',  minValue:0, maxValue:59 }
	]);
	shower = new Shower('metadatafragments.useAccount', 'metadatafragments.account');

}

//=====================================================================================

function setEmpty()
{
    this.setEmptyCommon();
	
	$('metadatafragments.lang').value = 'eng';
	$('metadatafragments.templateId').value = '0';
	$('metadatafragments.url').value = '';
	$('metadatafragments.query').value = '';
	$('metadatafragments.stylesheet').value = '';
	
	
	shower.update();
}

//=====================================================================================

function setData(node)
{
	this.setDataCommon(node);

	var site     = node.getElementsByTagName('site')    [0];
	var options  = node.getElementsByTagName('options')[0];

	hvutil.setOption(site, 		'url', 			  			'metadatafragments.url');
	hvutil.setOption(options, 'lang', 			  		'metadatafragments.lang');
	hvutil.setOption(options, 'query', 			  		'metadatafragments.query');
	hvutil.setOption(options, 'stylesheet', 			'metadatafragments.stylesheet');
	hvutil.setOption(options, 'templateId', 			'metadatafragments.templateId');
	hvutil.setOption(options, 'recordsCategory',  'metadatafragments.recordsCategory');
	
	//--- add privileges entries
	
	this.removeAllGroupRows();
	this.addGroupRows(node);
	
	//--- set categories

	this.unselectCategories();
	this.selectCategories(node);	
	
	shower.update();
}

//=====================================================================================

function getData()
{
    var data = this.getDataCommon();
	
	data.URL    			    	= $F('metadatafragments.url');
	data.LANG             	= $F('metadatafragments.lang');
	data.QUERY            	= $F('metadatafragments.query');
	data.STYLESHEET       	= $F('metadatafragments.stylesheet');
	data.TEMPLATEID       	= $F('metadatafragments.templateId');
	data.RECORDSCATEGORY  	= $F('metadatafragments.recordsCategory');
	
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

}

