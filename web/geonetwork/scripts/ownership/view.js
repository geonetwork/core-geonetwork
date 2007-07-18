//=====================================================================================
//===
//=== Ownership View
//===
//=====================================================================================

function View(xmlLoader)
{
	var rowTransf = new XSLTransformer('ownership/client-group-row.xsl',  xmlLoader);
	
	var loader = xmlLoader;
		
	this.addSourceUser = addSourceUser;
	this.clearGroupList= clearGroupList;
	this.addGroupRows  = addGroupRows;
	
//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function addSourceUser(id, name)
{
	var html='<option value="'+ id +'">'+ xml.escape(name) +'</option>';
	new Insertion.Bottom('source.user', html);
}

//=====================================================================================

function clearGroupList()
{
	gui.removeAllButFirst('group.list');
	Element.hide('groups');
}

//=====================================================================================

function addGroupRows(groupList)
{
	var list = xml.children(groupList, 'group');
	
	for (var i=0; i<list.length; i++)
	{
		var xslRes = rowTransf.transform(list[i]);
	
		//--- add the new search in list
		new Insertion.Bottom('group.list', xml.toString(xslRes));
	}
	
	Element.show('groups');
}

//=====================================================================================
}
