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
	
	this.getUsers      = getUsers;
	this.getUserGroups = getUserGroups;
	
//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function getUsers(callBack)
{
	new InfoService(loader, 'users', callBack);
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
//===
//=== Private methods
//===
//=====================================================================================


//=====================================================================================
}
