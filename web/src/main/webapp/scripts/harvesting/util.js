//=====================================================================================
//===
//=== Utility methods
//===
//=====================================================================================

var hvutil = new Object();

//=====================================================================================

//return the value of the radio button that is checked
//return a null if none are checked

function getCheckedValue(radioGroup) {
	var checked = $$("input[type=radio][name="+radioGroup+"]").find(
	  function(re) {return re.checked;}
	);
	return (checked) ? $F(checked) : null;
}


//set the radio button with the given value as being checked
//if the given value does not exist, all the radio buttons
//are reset to unchecked
function setCheckedValue(radioGroup, newValue) {
	var radioButtons  = $$('input[type=radio][name='+radioGroup+']');
	radioButtons.each(function(radioButton) {
		radioButton.checked = false;
		if(radioButton.value == newValue.toString()) {
			radioButton.checked = true;
		}
	});
}

hvutil.setRadioOption = function(node, name, radioGroup)
{
	var value = hvutil.find(node, name);

	if (value == null)
		throw 'Cannot find node with name : '+ name;
	
	setCheckedValue(radioGroup, value);
}


hvutil.setOption = function(node, name, ctrlId)
{
	var value = hvutil.find(node, name);
	var ctrl  = $(ctrlId);
	var type  = ctrl.getAttribute('type');
	
	if (value == null)
		throw 'Cannot find node with name : '+ name;
	
	if (!ctrl)
		throw 'Cannot find control with id : '+ ctrlId;
		
	if (type == 'checkbox')	{
		ctrl.checked = (value == 'true');
	} else						ctrl.value   = value;
}

hvutil.setOptionIfExists = function(node, name, ctrlId)
{
	var value = hvutil.find(node, name);
	var ctrl  = $(ctrlId);
	if (!ctrl) return;	
	var type  = ctrl.getAttribute('type');
	
	if (value == null)
		return;
		
	if (type == 'checkbox')	ctrl.checked = (value == 'true');		
	else	ctrl.value   = value;
}
//=====================================================================================

hvutil.find = function(node, name)
{
	var array = [ node ];
	
	while (array.length != 0)
	{
		node = array.shift();
		
		if (node.nodeName == name)
			return xml.textContent(node);
			
		node = node.firstChild;
		
		while (node != null)
		{
			if (node.nodeType == Node.ELEMENT_NODE)
				array.push(node);
			
			node = node.nextSibling;
		}
	}
	
	return null;
}

//=====================================================================================
//===
//=== Every
//===
//=====================================================================================

function Every(every)
{
	if (typeof every == 'string')
		every = parseInt(every);
	
	this.mins = every % 60;
	
	every -= this.mins;	
	this.hours = every / 60 % 24;	
	this.days  = (every - this.hours * 60) / 1440;	
}

//=====================================================================================

Every.build = function(days, hours, mins)
{
	if (typeof days == 'string')
		days = parseInt(days);
	
	if (typeof hours == 'string')
		hours = parseInt(hours);
	
	if (typeof mins == 'string')
		mins = parseInt(mins);
		
	return days*1440 + hours*60 + mins;
}

//=====================================================================================

