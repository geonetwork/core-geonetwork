//=====================================================================================
//===
//=== View (type:z3950)
//===
//=====================================================================================

z3950.View = function(xmlLoader)
{
	HarvesterView.call(this);	

	var privilTransf = new XSLTransformer('harvesting/z3950/client-privil-row.xsl', xmlLoader);
	var resultTransf = new XSLTransformer('harvesting/z3950/client-result-tip.xsl', xmlLoader);
	
	var loader = xmlLoader;
	var valid  = new Validator(loader);
	var shower = null;
	
	var currSearchId = 0;
	
	this.setPrefix('z3950');
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
	this.clearRepositories     = clearRepositories;
	this.unselectRepositories     = unselectRepositories;
	this.addRepository     = addRepository;
	this.getSelectedRepositories     = getSelectedRepositories;

	Event.observe('z3950.icon', 'change', ker.wrap(this, updateIcon));

//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function init()
{
	valid.add(
	[
		{ id:'z3950.name',        type:'length',   minSize :1,  maxSize :200 },
		{ id:'z3950.repositories',type:'length',   minSize :1,  maxSize :200 },
		{ id:'z3950.query',       type:'length',   minSize :1,  maxSize :200 },
		{ id:'z3950.username',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'z3950.password',    type:'length',   minSize :0,  maxSize :200 }
	]);

	shower = new Shower('z3950.useAccount', 'z3950.account');
}

//=====================================================================================

function setEmpty()
{
	this.setEmptyCommon();

	this.unselectRepositories();
	$('z3950.query').value='';

	var icons = $('z3950.icon').options;
	
	for (var i=0; i<icons.length; i++)
		if (icons[i].value == 'default.gif')
		{
			icons[i].selected = true;
			break;
		}

	shower.update();
	updateIcon();
}

//=====================================================================================

function setData(node)
{
	this.setDataCommon(node);

	var site     = node.getElementsByTagName('site')    [0];

	unselectRepositories();
	selectRepositories(node);

	hvutil.setOption(site, 'query',           'z3950.query');
	hvutil.setOption(site, 'icon',            'z3950.icon');

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

	data.REPOSITORIES = this.getSelectedRepositories();
	data.QUERY 		 = $F('z3950.query');
	data.ICON      = $F('z3950.icon');

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
	$('z3950.icon').options.length = 0;
}

//=====================================================================================

function addIcon(file)
{
	var html='<option value="'+ file +'">'+ xml.escape(file) +'</option>';
	new Insertion.Bottom('z3950.icon', html);
}

//=====================================================================================

function updateIcon()
{
	var icon = $F('z3950.icon');
	var image= $('z3950.icon.image');
	
	image.setAttribute('src', Env.url +'/images/harvesting/'+icon);
}

//=====================================================================================

function clearRepositories()
{
	$('z3950.repositories').options.length = 0;
}

//=====================================================================================

function addRepository(id, label)
{
	gui.addToSelect('z3950.repositories', id, label);
}

//=====================================================================================

function unselectRepositories()
{
	var ctrl = $('z3950.repositories');

	for (var i=0; i<ctrl.options.length; i++)
		ctrl.options[i].selected = false;
}

//=====================================================================================

function selectRepositories(node)
{
	var repositories = node.getElementsByTagName('repositories');
	if (repositories.length == 0) return;

	var list = repositories[0].getElementsByTagName('repository');

	for (var i=0; i<list.length; i++)
		selectRepository(list[i]);
}

//=====================================================================================

function selectRepository(repository)
{
	var id = repository.getAttribute('id');
	var ctrl = $('z3950.repositories');

	for (var i=0; i<ctrl.options.length; i++) {
		if (ctrl.options[i].value == id) {
			ctrl.options[i].selected = true;
			return;
		}
	}
}

//=====================================================================================

function getSelectedRepositories(server)
{
	var ctrl = $('z3950.repositories');
	var result = [];
	for (var i=0; i<ctrl.options.length; i++) {
		if (ctrl.options[i].selected) {
			result.push({ ID : ctrl.options[i].value });
		}
	}
	return result;
}

//=====================================================================================
}
