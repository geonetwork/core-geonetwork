//=====================================================================================
//===
//=== Ownership model
//===
//=====================================================================================

function Model(xmlLoader)
{
	var loader    = xmlLoader;
	var callBackF = null;

	//--- public methods
	
	this.getEditors    = getEditors;
	this.getUserGroups = getUserGroups;
	this.transfer      = transfer;
	
//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function getEditors(callBack)
{
	callBackF = callBack;	
	
	ker.send('xml.ownership.editors', '<request/>', ker.wrap(this, getEditors_OK));
}

//-------------------------------------------------------------------------------------

function getEditors_OK(xmlRes)
{
	if (xmlRes.nodeName == 'error')
		ker.showError(loader.getText('cannotRetrieve'), xmlRes);
	else
	{
		var data = [];
		var list = xml.children(xmlRes);
		
		for (var i=0; i<list.length; i++)
			data.push(xml.toObject(list[i]));				
		
		callBackF(data);
	}
}

//=====================================================================================

function getUserGroups(userId, callBack)
{
	callBackF = callBack;	

	var request = ker.createRequest('id', userId);
	
	ker.send('xml.ownership.groups', request, ker.wrap(this, getUserGroups_OK));
}

//-------------------------------------------------------------------------------------

function getUserGroups_OK(xmlRes)
{
	if (xmlRes.nodeName == 'error')
		ker.showError(loader.getText('cannotRetrieve'), xmlRes);
	else
		callBackF(xmlRes);
}

//=====================================================================================

function transfer(sourceUsr, sourceGrp, targetUsr, targetGrp, callBack)
{
	callBackF = callBack;	

	var data = 
	{ 
		SOURCE_USR : sourceUsr,
		SOURCE_GRP : sourceGrp,
		TARGET_USR : targetUsr,
		TARGET_GRP : targetGrp 
	};

	var request = str.substitute(transferTemp, data);

	ker.send('xml.ownership.transfer', request, ker.wrap(this, transfer_OK));
}

//-------------------------------------------------------------------------------------

function transfer_OK(xmlRes)
{
	if (xmlRes.nodeName == 'error')
		ker.showError(loader.getText('cannotTransfer'), xmlRes);
	else
		callBackF(xmlRes);
}

//=====================================================================================

var transferTemp =
'<request>'+
'   <sourceUser>{SOURCE_USR}</sourceUser>'+
'   <sourceGroup>{SOURCE_GRP}</sourceGroup>'+
'   <targetUser>{TARGET_USR}</targetUser>'+
'   <targetGroup>{TARGET_GRP}</targetGroup>'+
'</request>';

//=====================================================================================
}
