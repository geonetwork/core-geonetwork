//=====================================================================================
//===
//=== Ownership View
//===
//=====================================================================================

function View(xmlLoader)
{
	var rowTransf = new XSLTransformer('ownership/client-group-row.xsl',  xmlLoader);
	
	var loader = xmlLoader;
	var cache  = null;
	
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
	gui.addToSelect('source.user', id, name);
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
	cache = groupList;
	
	var list = xml.children(groupList, 'group');
	
	//--- add group rows
	
	for (var i=0; i<list.length; i++)
	{
		var id     = xml.evalXPath(list[i], 'id');
		var xslRes = rowTransf.transform(list[i]);
	
		//--- add the new search in list
		gui.appendTableRow('group.list', xslRes)
		
		var tr = $(id);
		
		var targetGrp = xml.getElementById(tr, 'target.group');
		var targetUsr = xml.getElementById(tr, 'target.user');
		
		Event.observe(targetGrp, 'change', ker.wrap(this, targetChange));
		
		addTargetGroups(id, targetGrp);
		setTargetUsers (id, targetUsr);
		
		targetGrp.targetUsr = targetUsr;
	}
	
	Element.show('groups');
}
//=====================================================================================

function addTargetGroups(groupId, ctrl)
{
	var list = xml.children(cache, 'targetGroup');
	
	for (var i=0; i<list.length; i++)
	{
		var tarId   = xml.evalXPath(list[i], 'id');
		var tarName = xml.evalXPath(list[i], 'label/'+Env.lang);
		
		gui.addToSelect(ctrl, tarId, tarName, groupId == tarId);
	}
}

//=====================================================================================

function setTargetUsers(groupId, ctrl)
{
	var srcId = $F('source.user');
	
	//--- remove all users
	$(ctrl).options.length = 0;

	var list = xml.children(cache, 'targetGroup');
	
	for (var i=0; i<list.length; i++)
	{
		var tarId = xml.evalXPath(list[i], 'id');
		
		if (tarId == groupId)
		{
			var editors = xml.children(list[i], 'editor');
			
			for (var j=0; j<editors.length; j++)
			{
				var edId = xml.evalXPath(editors[j], 'id');
				var name = xml.evalXPath(editors[j], 'name');
				var surn = xml.evalXPath(editors[j], 'surname');
                var username = xml.evalXPath(editors[j], 'username');
				
				if (edId != srcId)
					gui.addToSelect(ctrl, edId, surn +' '+ name +' ('+ username +') ');
			}
		}		
	}
}

//=====================================================================================
//=== Listener
//=====================================================================================

function targetChange(e)
{
	var ctrl = Event.element(e);
	var id   = $F(ctrl);
	
	setTargetUsers(id, ctrl.targetUsr);
}

//=====================================================================================
}
