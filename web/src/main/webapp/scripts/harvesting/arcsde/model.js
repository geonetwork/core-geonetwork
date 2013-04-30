//=====================================================================================
//===
//=== Model (type:Arcsde)
//===
//=====================================================================================

arcsde.Model = function(xmlLoader)
{
	HarvesterModel.call(this);

	var loader = xmlLoader;
	var callBackF = null;

	this.retrieveGroups    = retrieveGroups;
	this.retrieveCategories = retrieveCategories;
	this.retrieveIcons     = retrieveIcons;
	this.getUpdateRequest = getUpdateRequest;
	
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
	callBackF = callBack;	

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
		
		callBackF(data);
	}
}

//=====================================================================================

function getUpdateRequest(data)
{
	var request = str.substitute(updateTemp, data);	
      return this.substituteCommon(data, request);
}

//=====================================================================================

var updateTemp = 
' <node id="{ID}" type="{TYPE}">'+ 
'    <ownerGroup><id>{OWNERGROUP}</id></ownerGroup>'+
'    <site>'+
'      <name>{NAME}</name>'+
'      <server>{SERVER}</server>'+
'      <port>{PORT}</port>'+
'      <username>{USERNAME}</username>'+
'      <password>{PASSWORD}</password>'+
'      <database>{DATABASE}</database>'+
'      <icon>{ICON}</icon>'+
'    </site>'+
'    <options>'+
'      <every>{EVERY}</every>'+
'      <oneRunOnly>{ONE_RUN_ONLY}</oneRunOnly>'+
'    </options>'+

'    <content>'+
'      <validate>{VALIDATE}</validate>'+
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
