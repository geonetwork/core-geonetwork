//=====================================================================================
//===
//=== View (type:CGP)
//===
//=====================================================================================

cgp.View = function(xmlLoader)
{
	HarvesterView.call(this);

	var searchTransf = new XSLTransformer('harvesting/cgp/client-search-row.xsl', xmlLoader);
	var privilTransf = new XSLTransformer('harvesting/cgp/client-privil-row.xsl', xmlLoader);
	var resultTransf = new XSLTransformer('harvesting/cgp/client-result-tip.xsl', xmlLoader);

	var loader = xmlLoader;
	var valid = new Validator(loader);
	var shower = null;

	var currSearchId = 0;

	this.setPrefix('cgp');
	this.setPrivilTransf(privilTransf);
	this.setResultTransf(resultTransf);

	//--- public methods

	this.init = init;
	this.setEmpty = setEmpty;
	this.setData = setData;
	this.getData = getData;
	this.isDataValid = isDataValid;
	this.clearIcons = clearIcons;
	this.addIcon = addIcon;

	this.addEmptySearch = addEmptySearch;
	this.addSearch = addSearch;
	this.removeSearch = removeSearch;
	Event.observe('cgp.icon', 'change', ker.wrap(this, updateIcon));


	//=====================================================================================
	//===
	//=== API methods
	//===
	//=====================================================================================

	function init()
	{
		valid.add(
				[
					{ id:'cgp.name',        type:'length',   minSize :1,  maxSize :200 },
					{ id:'cgp.url',         type:'length',   minSize :1,  maxSize :200 },
					{ id:'cgp.url',         type:'url' },
					{ id:'cgp.username',    type:'length',   minSize :0,  maxSize :200 },
					{ id:'cgp.password',    type:'length',   minSize :0,  maxSize :200 }
				]);

		shower = new Shower('cgp.useAccount', 'cgp.account');

		gui.setupTooltips(loader.getNode('tips'));
	}

	//=====================================================================================

	function setEmpty()
	{
		this.setEmptyCommon();

		removeSearch();
		removeAllPolicyGroups();

		$('cgp.url').value = '';
		$('cgp.validate').checked = false;

		var icons = $('cgp.icon').options;

		for (var i = 0; i < icons.length; i++)
			if (icons[i].value == 'cgp.gif')
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

		var site = node.getElementsByTagName('site')            [0];
		var searches = node.getElementsByTagName('searches')        [0];
		var options  = node.getElementsByTagName('options') [0];

		hvutil.setOption(site, 'url', 'cgp.url');
		hvutil.setOption(site, 'icon', 'cgp.icon');
		hvutil.setOption(options, 'validate', 'cgp.validate');
		updateIcon();

		//--- add search entries

		var list = searches.getElementsByTagName('search');

		removeSearch();

		for (var i = 0; i < list.length; i++)
			addSearch(list[i]);

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

		data.URL = $F('cgp.url');
		data.ICON = $F('cgp.icon');
		data.VALIDATE = $('cgp.validate').checked;

		//--- retrieve search information

		var searchData = [];
		var searchList = xml.children($('cgp.searches'));

		for (var i = 0; i < searchList.length; i++)
		{
			var divElem = searchList[i];

			searchData.push(
			{
				ANY_TEXT : xml.getElementById(divElem, i+'.cgp.anytext').value,
				FROM       : $F(i+'.cgp.from'),
				UNTIL      : $F(i+'.cgp.until'),
				LAT_NORTH : $F(i+'.cgp.latnorth'),
				LAT_SOUTH : $F(i+'.cgp.latsouth'),
				LON_EAST : $F(i+'.cgp.loneast'),
				LON_WEST : $F(i+'.cgp.lonwest')
			});
		}

		data.SEARCH_LIST = searchData;

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
	//=== Search methods
	//=====================================================================================

	function addEmptySearch()
	{
		var doc = Sarissa.getDomDocument();
		var search = doc.createElement('search');

		addSearch(search);
	}

	//=====================================================================================

	function addSearch(search)
	{
		$('cgp.addSearch').hide();

		var id = '' + currSearchId++;
		search.setAttribute('id', id);

		var html = searchTransf.transformToText(search);

		//--- add the new search in list
		new Insertion.Bottom('cgp.searches', html);


  	//--- setup FROM and UNTIL parameters
  	initCalendar();
  	
		/*gui.setupTooltip(id+'.cgp.remove', loader.evalNode('tips/tip[@id="cgp.remove"]'));

		gui.setupTooltip('cgp.from',        loader.evalNode('tips/tip[@id="cgp.from"]'));
		gui.setupTooltip('cgp.from.set',    loader.evalNode('tips/tip[@id="cgp.from.set"]'));
		gui.setupTooltip('cgp.from.clear',  loader.evalNode('tips/tip[@id="cgp.from.clear"]'));
		gui.setupTooltip('cgp.until',       loader.evalNode('tips/tip[@id="cgp.until"]'));
		gui.setupTooltip('cgp.until.set',   loader.evalNode('tips/tip[@id="cgp.until.set"]'));
		gui.setupTooltip('cgp.until.clear', loader.evalNode('tips/tip[@id="cgp.until.clear"]'));
          */

	
	}

	//=====================================================================================

	function removeSearch()
	{
		$('cgp.searches').innerHTML = '';
		// valid.removeByParent();
		$('cgp.addSearch').show();

	}

	//=====================================================================================
	//=== Group copy policy methods
	//=====================================================================================

	function getPolicyGroups()
	{
		var groupData = [];
		var groupList = $('cgp.groups').getElementsByTagName('TR');

		//--- i=1 : skip header
		for (var i = 1; i < groupList.length; i++)
		{
			var rowElem = groupList[i];
			var id = rowElem.getAttribute('id');

			//--- format is : cgp.group.{@name}
			var name = id.substring(9);
			var list = rowElem.getElementsByTagName('SELECT');

			var policy = $F(list[0]);

			if (policy != 'dontCopy')
				groupData.push(
				{
					NAME   : name,
					POLICY : policy
				});
		}

		return groupData;
	}

	//=====================================================================================

	function getListedPolicyGroups()
	{
		var groupData = [];
		var groupList = $('cgp.groups').getElementsByTagName('TR');

		//--- i=1 : skip header
		for (var i = 1; i < groupList.length; i++)
		{
			var rowElem = groupList[i];
			var id = rowElem.getAttribute('id');

			//--- format is : cgp.group.{@name}
			var name = id.substring(9);

			groupData.push(name);
		}

		return groupData;
	}

	//=====================================================================================

	function addPolicyGroup(name, policy)
	{
		var doc = Sarissa.getDomDocument();
		var group = doc.createElement('group');

		group.setAttribute('name', name);
		group.setAttribute('policy', policy);

		var xslRes = policyTransf.transform(group);

		//--- add the new group policy row in list
		gui.appendTableRow('cgp.groups', xslRes);
	}

	//=====================================================================================

	function removePolicyGroup(name)
	{
		Element.remove('cgp.group.' + name);
	}

	//=====================================================================================

	function removeAllPolicyGroups()
	{
		var elm = $('cgp.groups');
		if (!elm)
		{
			return;
		}

		var rows = elm.getElementsByTagName('TR');

		for (var i = rows.length - 1; i > 0; i--)
			Element.remove(rows[i]);
	}

	//=====================================================================================

	function findPolicyGroup(name)
	{
		var list = $('cgp.groups').getElementsByTagName('TR');

		for (var i = 1; i < list.length; i++)
		{
			var row = list[i];
			var cgpame = row.getAttribute('id');

			if ('cgp.group.' + name == cgpame)
				return row;
		}

		return null;
	}
	//=====================================================================================

	function clearIcons()
	{
		$('cgp.icon').options.length = 0;
	}

	//=====================================================================================

	function addIcon(file)
	{
		gui.addToSelect('cgp.icon', file, file);
	}

	//=====================================================================================

	function updateIcon()
	{
		var icon = $F('cgp.icon');
		var image = $('cgp.icon.image');

		image.setAttribute('src', Env.url + '/images/harvesting/' + icon);
	}

	//=====================================================================================
	//=== Other methods
	//=====================================================================================

	function getHostData()
	{
		var data =
		{
			URL       : $F('cgp.url')
		};

		return data;
	}

	//=====================================================================================
}

