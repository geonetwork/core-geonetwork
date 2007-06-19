//=====================================================================================
//===
//=== Model (type:WebDav)
//===
//=====================================================================================

wd.Model = function(xmlLoader)
{
	var loader = xmlLoader;

	this.retrieveGroups   = retrieveGroups;
	this.getUpdateRequest = getUpdateRequest;

//=====================================================================================

function retrieveGroups(langCode, callBack)
{
	this.langCode         = langCode;
	this.retrieveGroupsCB = callBack;
	
	var request = ker.createRequest('type', 'groups');
	
	ker.send('xml.info', request, ker.wrap(this, retrieveGroups_OK), true);
}

//-------------------------------------------------------------------------------------

function retrieveGroups_OK(xmlRes)
{
	if (xmlRes.nodeName == 'error')
		ker.showError(loader.getText('cannotRetrieve'), xmlRes);
	else
	{
		var data   = [];
		var groups = xmlRes.getElementsByTagName('group');
		
		retrieveGroupsAdd('0', groups, data, this.langCode);
		retrieveGroupsAdd('1', groups, data, this.langCode);
		
		for (var i=0; i<groups.length; i++)
		{
			var group = groups[i];
		
			var id  = group.getAttribute('id');
			var name= xml.textContent(group.getElementsByTagName(this.langCode)[0]);
							
			if (id != '0' && id != '1')
				data.push({ ID:id, NAME:name });				
		}
		
		this.retrieveGroupsCB(data);
	}
}

//-------------------------------------------------------------------------------------

function retrieveGroupsAdd(selId, groups, data, langCode)
{
	for (var i=0; i<groups.length; i++)
	{
		var group = groups[i];
		
		var id  = group.getAttribute('id');
		var name= xml.textContent(group.getElementsByTagName(langCode)[0]);
							
		if (id == selId)
			data.push({ ID:id, NAME:name });				
	}
}

//=====================================================================================

function getUpdateRequest(data)
{
	var request = str.substitute(updateTemp, data);
	
	var list = data.PRIVILEGES;
	var text = '';
	
	for (var i=0; i<list.length; i++)
	{
		var groupID = list[i].GROUP;
		var operList= list[i].OPERATIONS;
		
		text += '<group id="'+ groupID +'">';
		
		for (var j=0; j<operList.length; j++)
			text += '<operation name="'+operList[j]+'"/>';			
		
		text += '</group>';			
	}

	return str.replace(request, '{PRIVIL_LIST}', text);
}

//=====================================================================================

var updateTemp = 
' <node id="{ID}" name="{NAME}" type="{TYPE}">'+ 
'    <site>'+
'      <url>{URL}</url>'+
'      <account>'+
'        <use>{USE_ACCOUNT}</use>'+
'        <username>{USERNAME}</username>'+
'        <password>{PASSWORD}</password>'+
'      </account>'+
'    </site>'+
    
'    <options>'+
'      <every>{EVERY}</every>'+
'      <oneRunOnly>{ONE_RUN_ONLY}</oneRunOnly>'+
'      <structure>{STRUCTURE}</structure>'+
'      <validate>{VALIDATE}</validate>'+
'    </options>'+

'    <privileges>'+
'       {PRIVIL_LIST}'+
'    </privileges>'+
'  </node>';

//=====================================================================================
}
