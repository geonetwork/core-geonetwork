//=====================================================================================
//===
//=== HarvesterModel
//===
//=====================================================================================

function HarvesterModel()
{
	
//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

this.substituteCommon = function(data, request)
{
	//--- handle privileges
	
	var list = data.PRIVILEGES;
	var text = '';
	
	if (list != null)
	{
		for (var i=0; i<list.length; i++)
		{
			var groupID = list[i].GROUP;
			var operList= list[i].OPERATIONS;
			
			text += '<group id="'+ groupID +'">';
			
			for (var j=0; j<operList.length; j++)
				text += '<operation name="'+operList[j]+'"/>';			
			
			text += '</group>';			
		}

		request = str.replace(request, '{PRIVIL_LIST}', text);
	}
	
	//--- handle categories
	
	text = '';
	list = data.CATEGORIES;
	
	if (list != null)
	{
		for (var i=0; i<list.length; i++)
			text += '<category id="'+ list[i].ID +'"/>';
	
		request = str.replace(request, '{CATEG_LIST}', text);
	}
	
	return request;
}

//=====================================================================================
}

