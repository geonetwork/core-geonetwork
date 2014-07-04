//=====================================================================================
//===
//=== View (type:csw)
//===
//=====================================================================================

csw.View = function(xmlLoader)
{
	HarvesterView.call(this);	
	
	var searchTransf = new XSLTransformer('harvesting/csw/client-search-row.xsl', xmlLoader);
	var searchCapTransf = new XSLTransformer('harvesting/csw/client-search-capability.xsl', xmlLoader);
	var searchTempTransf = new XSLTransformer('harvesting/csw/client-search-temp.xsl', xmlLoader);
	var editCapTransf = new XSLTransformer('harvesting/csw/client-edit-capability.xsl', xmlLoader);
	var elemCapTransf = new XSLTransformer('harvesting/csw/elem-capability.xsl', xmlLoader);
	var privilTransf = new XSLTransformer('harvesting/csw/client-privil-row.xsl', xmlLoader);
	var resultTransf = new XSLTransformer('harvesting/csw/client-result-tip.xsl', xmlLoader);
	
	var loader = xmlLoader;
	var valid  = new Validator(loader);
	var shower = null;
	
	var searchtemp;
	var elemCap;
	
	var currSearchId = 0;
	
	this.setPrefix('csw');
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
	this.addEmptySearch = addEmptySearch;
	this.removeSearch   = removeSearch;

	Event.observe('csw.icon', 'change', ker.wrap(this, updateIcon));

//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function init()
{
	valid.add(
	[
		{ id:'csw.name',        type:'length',   minSize :1,  maxSize :200 },
		{ id:'csw.capabUrl',    type:'length',   minSize :1,  maxSize :200 },
		{ id:'csw.capabUrl',    type:'url' },
		{ id:'csw.username',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'csw.password',    type:'length',   minSize :0,  maxSize :200 }
	]);

	shower = new Shower('csw.useAccount', 'csw.account');
}

//=====================================================================================

function setEmpty()
{
	this.setEmptyCommon();

    $('csw.rejectDuplicateResource').checked = false;
    
	removeAllSearch();
	
	$('csw.capabUrl').value = '';
	
	var icons = $('csw.icon').options;
	
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
	var searches = node.getElementsByTagName('searches')[0];

	hvutil.setOption(site, 'capabilitiesUrl', 'csw.capabUrl');
	hvutil.setOption(site, 'icon',            'csw.icon');
    hvutil.setOption(site, 'rejectDuplicateResource', 'csw.rejectDuplicateResource');
    
	//--- add search entries
	
	var list = searches.getElementsByTagName('search');
	
	removeAllSearch();
	
	for (var i=0; i<list.length; i++){
		addEditCap(list[i]);
	}

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
	
	data.CAPAB_URL = $F('csw.capabUrl');
	data.ICON      = $F('csw.icon');
	data.REJECTDUPLICATERESOURCE = $('csw.rejectDuplicateResource').checked;
	//--- retrieve search information
	
	var searchData = [];
	var searchList = xml.children($('csw.searches'));
	
	if (typeof(elemCap)=='undefined'){
		elemCap = elemCapTransf.transform(Sarissa.getDomDocument().createElement('search')); 
	}
	
	var capList = elemCap.getElementsByTagName('capability');
	var obj={};
		
	for(var i=0; i<searchList.length; i++)
	{
		var divElem = searchList[i];
		var obj={};
		
		for(var j=0; j<capList.length; j++){
			
				var capName = capList[j].getAttribute('name');
                var el = xml.getElementById(divElem, capList[j].textContent);
                if (el != null) {
                    obj[capName]= el.value;
                }
		}
		
		searchData.push(obj);
	}
	
		
	if(typeof(searchtemp)=='undefined'){ 
	
		var doc    = Sarissa.getDomDocument();
		var searchtmp = doc.createElement('search');
		for(var j=0; j < capList.length; j++) {
			
			var text = doc.createTextNode('{'+capList[j].getAttribute('name') +'}');
			var subtmp = doc.createElement(capList[j].getAttribute('name'));
			subtmp.appendChild(text);
			searchtmp.appendChild(subtmp);
		}
			
		addSearchTemp(searchtmp);
	} 
	

	
	data.SEARCH_LIST = searchData;
	data.SEARCH_TEMP = searchtemp;
	
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
	$('csw.icon').options.length = 0;
}

//=====================================================================================

function addIcon(file)
{
	gui.addToSelect('csw.icon', file, file);
}

//=====================================================================================

function updateIcon()
{
	var icon = $F('csw.icon');
	var image= $('csw.icon.image');
	
	image.setAttribute('src', Env.url +'/images/harvesting/'+icon);
}

//=====================================================================================
//=== Search methods
//=====================================================================================

/**
 * Old fixed fixed search criteria filters. Not used, now the search criteria fields
 * are retrieved from the remote server queryables. See https://trac.osgeo.org/geonetwork/ticket/1259
 */
function addEmptySearchOld()
{
		var doc    = Sarissa.getDomDocument();	
		var search = doc.createElement('search');

		addSearch(search);
}

/**
 * Do a GetCapabilities request and for each queryables list them on the configuration.
 */
function addEmptySearch()
{
	var url = $('csw.capabUrl').value;
		 
	if (url==''){
		return;
	}
	if (url.indexOf('GetCapabilities') === -1) {
          url += (url.indexOf('?') === -1 ? '?' : '&') + "request=GetCapabilities&version=2.0.2&service=CSW";
	}
	
	var proxyUrl = '../../proxy?url='+encodeURIComponent(url);

	OpenLayers.Request.GET({
		url: proxyUrl,
   		success: function(response) {
   			
   			var doc    = Sarissa.getDomDocument();	
   			var search = doc.createElement('search');
   			var searchtmp = doc.createElement('search');
   			
    		var format = new OpenLayers.Format.XML();
    		var doc = format.read(response.responseText);
    		var nodes = format.getElementsByTagNameNS(doc, '*', 'Constraint');
    		var queryables = [];
    		
			for(var i=0; i < nodes.length; i++) {
				if (nodes[i].attributes[0].value =='SupportedISOQueryables' || nodes[i].attributes[0].value =='AdditionalQueryables')
				for(var j=0; j < nodes[i].childNodes.length; j++) {
					if (nodes[i].childNodes[j].nodeName == 'ows:Value'){
					    queryables.push(nodes[i].childNodes[j].firstChild.nodeValue)
					}
				}
			}
			
			queryables.sort();
			
			for (var i=0; i < queryables.length; i++) {
			    var sub = doc.createElement(queryables[i]);
                search.appendChild(sub);
                var text = doc.createTextNode('{'+queryables[i] +'}');
                var subtmp = doc.createElement(queryables[i]);
                subtmp.appendChild(text);
                searchtmp.appendChild(subtmp);
			}
			
			addSearchTemp(searchtmp);
			
			addSearchCap(search);
    	},
    	failure: function(result) {
            alert("failure");
    	}
	});

	
}

//=====================================================================================

function addSearchCap(search)
{
	var id = ''+ currSearchId++;
	search.setAttribute('id', id);
	
	elemCap = elemCapTransf.transform(search); 
	
	var html = searchCapTransf.transformToText(search);
	
	//--- add the new search in list
	new Insertion.Bottom('csw.searches', html);

  // Only 1 search section is supported
  Element.hide("csw.addSearch");
}

//=====================================================================================

function addEditCap(search)
{
  // Settings return an empty search element if no searches defined, ignore in this case
  if (search.childElementCount == 0) return;

	var id = ''+ currSearchId++;
	search.setAttribute('id', id);
	
	elemCap = elemCapTransf.transform(search); 
		
	var html = editCapTransf.transformToText(search);
	
	//--- add the new search in list
	new Insertion.Bottom('csw.searches', html);

  // Only 1 search section is supported
  Element.hide("csw.addSearch");
}

//=====================================================================================


function addSearchTemp(searchtmp)
{
	searchtemp = xml.toString(searchtmp);
}


//=====================================================================================


/**
 * Old fixed fixed search criteria filters. Not used, now the search criteria fields
 * are retrieved from the remote server queryables. See https://trac.osgeo.org/geonetwork/ticket/1259
 */
function addSearch(search)
{
	var id = ''+ currSearchId++;
	search.setAttribute('id', id);
	

	var html = searchTransf.transformToText(search);

	
	//--- add the new search in list
	new Insertion.Bottom('csw.searches', html);
	
	
	valid.add(
	[
		{ id:'csw.anytext',  type:'length',   minSize :0,  maxSize :200 },
		{ id:'csw.title',    type:'length',   minSize :0,  maxSize :200 },
		{ id:'csw.abstract', type:'length',   minSize :0,  maxSize :200 },
		{ id:'csw.subject',  type:'length',   minSize :0,  maxSize :200 },
		{ id:'csw.minscale', type:'length',   minSize :0,  maxSize :200 },
		{ id:'csw.maxscale', type:'length',   minSize :0,  maxSize :200 }
	], id);
}

//=====================================================================================

function removeSearch(id)
{
	valid.removeByParent(id);
	Element.remove(id);

  // Only 1 search section is supported, if no search panel, show the Add button
  Element.show("csw.addSearch");
}

//=====================================================================================

function removeAllSearch()
{
	$('csw.searches').innerHTML = '';
	valid.removeByParent();

  // Only 1 search section is supported, if no search panel, show the Add button
  Element.show("csw.addSearch");
}

//=====================================================================================
}

