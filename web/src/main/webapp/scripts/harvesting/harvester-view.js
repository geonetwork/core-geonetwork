//=====================================================================================
//===
//=== HarvesterView
//===
//=====================================================================================

function HarvesterView()
{
	var prefix       = '???';
	var privilTransf = null;
	var resultTransf = null;
	
//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

this.setPrefix = function(p)
{
	prefix = p;
}

//=====================================================================================

this.setPrivilTransf = function(transf)
{
	privilTransf = transf;
}

//=====================================================================================

this.setResultTransf = function(transf)
{
	resultTransf = transf;
}

//=====================================================================================

this.getResultTip = function(node)
{
	return resultTransf.transformToText(node);
}

//=====================================================================================
//=== Set/Get methods
//=====================================================================================

this.setEmptyCommon = function()
{
	$(prefix+'.name')      .value = '';
	if($(prefix+'.useAccount')) {
	$(prefix+'.useAccount').checked = true;
	$(prefix+'.username')  .value = '';
	$(prefix+'.password')  .value = '';
	}
	
	$(prefix+'.oneRunOnly').checked = false;

	new Cron().setUI(prefix);
	$(prefix+'.validate').checked = false;
	this.unselectImportXslt();

	this.removeAllGroupRows();
	this.unselectCategories();
}

//=====================================================================================

this.setDataCommon = function(node)
{
	var site   = node.getElementsByTagName('site')   [0];
	var options= node.getElementsByTagName('options')[0];
	var content= node.getElementsByTagName('content')[0];
	hvutil.setOption(site, 'name',     prefix+'.name');
	hvutil.setOptionIfExists(site, 'use',      prefix+'.useAccount');
	hvutil.setOptionIfExists(site, 'username', prefix+'.username');
	hvutil.setOptionIfExists(site, 'password', prefix+'.password');
	hvutil.setOption(options, 'oneRunOnly', prefix+'.oneRunOnly')

    var cron = new Cron(hvutil.find(options, 'every'));

    $(prefix+'.atHour') .value = cron.hours;
    $(prefix+'.atMin').value = cron.mins;
    $(prefix+'.atIntervalHours') .value = cron.intervalHours;
	var checkbox;
    for (day in cron.days) {
    	checkbox = $(prefix+"."+(day.toUpperCase()));
    	if(cron.days[day]) {
            checkbox.checked = true;
    	} else {
            checkbox.checked = false;
    	}
    }

	hvutil.setOption(content, 'validate', prefix+'.validate');
	hvutil.setOption(content, 'importxslt', prefix+'.importxslt');

    var ownerGroup = node.getElementsByTagName('ownerGroup')   [0];
    hvutil.setOption(ownerGroup, 'id', prefix+'.ownerGroup');
}

//=====================================================================================

this.getDataCommon = function()
{
	//var days  = $F(prefix+'.every.days');
	//var hours = $F(prefix+'.every.hours');
	//var mins  = $F(prefix+'.every.mins');

	var cron = new Cron().readUI(prefix);
    
	var data;
	if($(prefix+'.useAccount')) {
		data =
	{
		//--- site	
		NAME     : $F(prefix+'.name'),
        OWNERGROUP: $F(prefix+'.ownerGroup'),

		USE_ACCOUNT: $(prefix+'.useAccount').checked,
		USERNAME   : $F(prefix+'.username'),
		PASSWORD   : $F(prefix+'.password'),
	
		//--- options
        EVERY        : cron.asString(),

        AT_HOUR             : cron.hours,
        AT_MIN              : cron.mins,
        AT_INTERVAL_HOUR    : cron.intervalHours,
		ONE_RUN_ONLY : $(prefix+'.oneRunOnly').checked,
	
		//--- content		
		IMPORTXSLT   : $F(prefix+'.importxslt'),
		VALIDATE     : $(prefix+'.validate').checked
	}
	}
	else {
		data =
		{
			//--- site	
			NAME     : $F(prefix+'.name'),
			
			//--- options
            EVERY        : cron.asString(),

            AT_HOUR             : cron.hours,
            AT_MIN              : cron.mins,
            AT_INTERVAL_HOUR    : cron.intervalHours,

			ONE_RUN_ONLY : $(prefix+'.oneRunOnly').checked,

			//--- content		
			IMPORTXSLT   : $F(prefix+'.importxslt'),
			VALIDATE     : $(prefix+'.validate').checked
		}
	}
	
	return data;
}

//=====================================================================================

this.isDataValidCommon = function()
{
	/*var days  = $F(prefix+'.every.days');
	var hours = $F(prefix+'.every.hours');
	var mins  = $F(prefix+'.every.mins');
	
	if (Every.build(days, hours, mins) == 0)
	{
		alert(loader.getText('everyZero'));
		return false;
	} */
		
	return true;
}

//=====================================================================================
//=== Groups methods 
//=====================================================================================

this.clearGroups = function() 
{
    if($(prefix+ '.groups')) {
        $(prefix+ '.groups').options.length = 0;
    }
}

//=====================================================================================

this.addGroup = function(id, label)
{
    if($(prefix+ '.groups')) {
        gui.addToSelect(prefix+'.groups', id, label);
    }
    // do not add system groups [-1..1] to ownerGroup control
    if(id != '-1' && id != '0' && id != '1') {
        gui.addToSelect(prefix+'.ownerGroup', id, label);
    }
}

//=====================================================================================

this.getSelectedGroups = function() 
{ 
	var ctrl = $(prefix+'.groups');
	
	var result = [];
	
	for (var i=0; i<ctrl.options.length; i++)
		if (ctrl.options[i].selected)
			result.push(ctrl.options[i]);
			
	return result;
}

//=====================================================================================

this.addEmptyGroupRows = function(groups)
{
	for (var i=0; i<groups.length; i++)
	{
		var option = groups[i];
		var doc    = Sarissa.getDomDocument();
		var group  = doc.createElement('group');
		var groupId= option.value;
		
		group.setAttribute('id', groupId);
	
		this.addGroupRow(group);
	}
}

//=====================================================================================

this.addGroupRow = function(group)
{
	//--- retrieve group's name from the list of loaded groups
	
	var id   = group.getAttribute('id');
	var name = '???';
	var ctrl = $(prefix+'.groups');
	
	//--- discard group if it has already been added
	
	var list = $(prefix+'.privileges').getElementsByTagName('TR');
	
	for (var i=1; i<list.length; i++)
	{
		//-- string is '<prefix>.group.<id>'
		var groupID = list[i].getAttribute('id').split('.')[2];
		
		if (id == groupID)
			return;
	}
	
	//--- retrieve group's name
	
	for(var i=0; i<ctrl.options.length; i++)
		if (ctrl.options[i].value == id)
		{
			name = xml.textContent(ctrl.options[i]);
//			ctrl.options[i].style.background = '#D0D0D0';
			break;
		}
	
	group.setAttribute('name', name);
	
	//--- add group's row
	
	var xslRes = privilTransf.transform(group);
	
	//--- add the new privilege row to list
	gui.appendTableRow(prefix+'.privileges', xslRes);
}

//=====================================================================================

this.removeGroupRow = function(groupId)
{
	Element.remove(groupId);
}

//=====================================================================================

this.removeAllGroupRows = function()
{
	gui.removeAllButFirst(prefix+'.privileges');
}

//=====================================================================================

this.addGroupRows = function(node)
{
	var privil = node.getElementsByTagName('privileges');
	
	if (privil.length == 0)
		return;
		 
	var list = privil[0].getElementsByTagName('group');
	
	for (var i=0; i<list.length; i++)
		this.addGroupRow(list[i]);
}

//=====================================================================================

this.getPrivileges = function()
{
	var data = [];
	var list = $(prefix+'.privileges').getElementsByTagName('TR');
	
	for (var i=1; i<list.length; i++)
	{
		var trElem    = list[i];
		var inputList = trElem.getElementsByTagName('INPUT');
		var groupData = [];
		
		//-- the string is '<prefix>.group.<id>'
		var groupID = trElem.getAttribute('id').split('.')[2];
		
		for (var j=0; j<inputList.length; j++)
			if (inputList[j].checked)
				groupData.push(inputList[j].name);
				
		if (groupData.length != 0)
			data.push(
			{
				GROUP      : groupID,
				OPERATIONS : groupData
			});
	}
	
	return data;
}

//=====================================================================================
//=== ImportXslt methods
//=====================================================================================

this.clearImportXslt = function() 
{ 
	$(prefix+ '.importxslt').options.length = 0;
}

//=====================================================================================

this.addImportXslt = function(id,name)
{
	gui.addToSelect(prefix+'.importxslt', id, name);
}

//=====================================================================================

this.unselectImportXslt = function() 
{ 
	var ctrl = $(prefix+'.importxslt');
	
	for (var i=0; i<ctrl.options.length; i++)
		ctrl.options[i].selected = false;
}

//=====================================================================================
//=== Categories methods
//=====================================================================================

this.clearCategories = function() 
{ 
	$(prefix+ '.categories').options.length = 0;
}

//=====================================================================================

this.addCategory = function(id, label)
{
	gui.addToSelect(prefix+'.categories', id, label);
}

//=====================================================================================

this.getSelectedCategories = function() 
{ 
	var ctrl = $(prefix+'.categories');
	
	var result = [];
	
	for (var i=0; i<ctrl.options.length; i++)
		if (ctrl.options[i].selected)
			result.push({ ID : ctrl.options[i].value });
			
	return result;
}

//=====================================================================================

this.unselectCategories = function() 
{ 
	var ctrl = $(prefix+'.categories');
	
	for (var i=0; i<ctrl.options.length; i++)
		ctrl.options[i].selected = false;
}

//=====================================================================================

this.selectCategories = function(node)
{ 
	var categs = node.getElementsByTagName('categories');
	
	if (categs.length == 0)
		return;
		
	var list = categs[0].getElementsByTagName('category');
	
	for (var i=0; i<list.length; i++)
		selectCategory(list[i]);
}

//=====================================================================================
//===
//=== Private methods
//===
//=====================================================================================

function selectCategory(categ)
{
	var id   = categ.getAttribute('id');
	var ctrl = $(prefix+'.categories');
	
	for (var i=0; i<ctrl.options.length; i++)
		if (ctrl.options[i].value == id)
		{
			ctrl.options[i].selected = true;
			return;
		}
}

//=====================================================================================
}

