//=====================================================================================
//===
//=== Model (type:Geonetwork)
//===
//=====================================================================================

gn.Model = function(xmlLoader)
{
	var loader = xmlLoader;

	this.retrieveSites    = retrieveSites;
	this.getUpdateRequest = getUpdateRequest;
	
//=====================================================================================

function retrieveSites(data, callBack)
{
	this.retrieveSitesCB = callBack;
	
	var url = 'http://'+ data.HOST;
	
	if (data.PORT != '')
		url += ':'+data.PORT;
		
	url += '/'+data.SERVLET+'/srv/en/xml.info';
	
	var request = str.substitute(retrieveTemp, { URL : url });

	ker.send('xml.forward', request, ker.wrap(this, retrieveSites_OK));
}

//-------------------------------------------------------------------------------------

function retrieveSites_OK(xmlRes)
{
	if (xmlRes.nodeName == 'error')
		ker.showError(loader.getText('cannotRetrieve'), xmlRes);
	else
	{
		var site  = xmlRes.getElementsByTagName('site')           [0];
		var nodes = xmlRes.getElementsByTagName('harvestingNodes')[0];
	
		var data =
		{
			SITE_NAME: xml.textContent(site.getElementsByTagName('name')  [0]),
			SITE_ID  : xml.textContent(site.getElementsByTagName('siteId')[0]),
			NODES    : []
		};
			
		var nodeList = nodes.getElementsByTagName('node');
		
		for (var i=0; i<nodeList.length; i++)
		{
			var node = nodeList[i];
		
			var name   = xml.textContent(node.getElementsByTagName('name')    [0]);
			var siteId = xml.textContent(node.getElementsByTagName('siteId')  [0]);
			var count  = xml.textContent(node.getElementsByTagName('metadata')[0]);
			
			if (count == '')
				count = '0';
				
			data.NODES.push(
			{
				SITE_NAME : name,
				SITE_ID   : siteId,
				MD_COUNT  : count
			});
		}
		
		this.retrieveSitesCB(data);
	}
}

//=====================================================================================

function getUpdateRequest(data)
{
	var request = str.substitute(updateTemp, data);
	
	var list = data.SEARCH_LIST;
	var text = '';
		
	for (var i=0; i<list.length; i++)
		text += str.substitute(searchTemp, list[i]);
	
	return str.replace(request, '{SEARCHES}', text);
}

//=====================================================================================

var retrieveTemp =
'<request>'+
'   <url>{URL}</url>'+
'   <params>'+
'      <request>'+
'         <type>site</type>'+
'         <type>harvestingNodes</type>'+
'      </request>'+
'   </params>'+
'</request>';

//=====================================================================================

var updateTemp = 
' <node id="{ID}" name="{NAME}" type="{TYPE}">'+ 
'    <site>'+
'      <host>{HOST}</host>'+
'      <port>{PORT}</port>'+
'      <servlet>{SERVLET}</servlet>'+
'      <account>'+
'        <use>{USE_ACCOUNT}</use>'+
'        <username>{USERNAME}</username>'+
'        <password>{PASSWORD}</password>'+
'      </account>'+
'    </site>'+
    
'    <searches>'+
'       {SEARCHES}'+
'    </searches>'+
    
'    <options>'+
'      <every>{EVERY}</every>'+
'      <createGroups>{CREATE_GROUPS}</createGroups>'+
'      <createCateg>{CREATE_CATEG}</createCateg>'+
'      <oneRunOnly>{ONE_RUN_ONLY}</oneRunOnly>'+
'    </options>'+
'  </node>';

//=====================================================================================

var searchTemp = 
'    <search>'+
'      <freeText>{TEXT}</freeText>'+
'      <title>{TITLE}</title>'+
'      <abstract>{ABSTRACT}</abstract>'+
'      <keywords>{KEYWORDS}</keywords>'+
'      <digital>{DIGITAL}</digital>'+
'      <hardcopy>{HARDCOPY}</hardcopy>'+
'      <siteId>{SITE_ID}</siteId>'+
'    </search>';


//=====================================================================================
}
