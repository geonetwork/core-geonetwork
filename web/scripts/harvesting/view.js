//=====================================================================================
//===
//=== View for the main harvesting code
//=== Handles all view related stuff in the MVC pattern
//===
//=== Needs : geonetwork-ajax.js
//=====================================================================================

var SHOW = new Object();

SHOW.LIST = new Object();
SHOW.ADD  = new Object();
SHOW.EDIT = new Object();

//=====================================================================================

function View(xmlLoader)
{
	var rowTransf = new XSLTransformer('harvesting/client-node-row.xsl',  xmlLoader);
	var errTransf = new XSLTransformer('harvesting/client-error-tip.xsl', xmlLoader);
	
	var panelSwitcher = new TabSwitcher(['listPanel',   'addPanel',   'editPanel'], 
													 ['listButtons', 'addButtons', 'editButtons']);

	var editSwitcher = new TabSwitcher([]);	

	var loader     = xmlLoader;
	var addingFlag = false;
	var harvesters = {};
	var currHarv   = null;
	
	this.register         = register;
	this.show             = show;
	this.isAdding         = isAdding;
	this.unselect         = unselect;
	this.setStarted       = setStarted
	this.setStopped       = setStopped;
	this.setRunning       = setRunning;
	this.getIdList        = getIdList;
	this.remove           = remove;
	this.removeAll        = removeAll;
	this.refresh          = refresh;
	this.append           = append;
	this.newNode          = newNode;
	this.edit             = edit;
	this.getUpdateRequest = getUpdateRequest;
		
//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function register(harvester)
{
	var type = harvester.getType();
	var label= harvester.getLabel();
	var panel= harvester.getEditPanel();
		
	harvesters[type] = harvester;	
	
	editSwitcher.add(panel);
	harvester.init();
	
	//--- add entry to dropdown list
	
	var html='<option value="'+ type +'">'+ xml.escape(label) +'</option>';
	new Insertion.Bottom('add.type', html);
}

//=====================================================================================

function show(obj)
{
	if (obj == SHOW.LIST)
		panelSwitcher.show('listPanel', 'listButtons');
		
	else if (obj == SHOW.ADD)
		panelSwitcher.show('addPanel', 'addButtons');
		
	else if (obj == SHOW.EDIT)
		panelSwitcher.show('editPanel', 'editButtons');
		
	else
		throw 'Unknown object to show : '+ obj;		
}

//=====================================================================================
	
function isAdding() { return addingFlag; }

//=====================================================================================

function unselect(id) 
{
	$(id).getElementsByTagName('input')[0].checked = false;
}

//=====================================================================================

function setStarted(id) 
{
	var img = xml.getElementById($(id), 'status');
	
	img.setAttribute('src', Env.url +'/images/clock.png');
}

//=====================================================================================

function setStopped(id) 
{
	var img = xml.getElementById($(id), 'status');
	
	img.setAttribute('src', Env.url +'/images/fileclose.png');
}

//=====================================================================================

function setRunning(id) 
{
	var img = xml.getElementById($(id), 'status');
	
	img.setAttribute('src', Env.url +'/images/exec.png');
}

//=====================================================================================

function getIdList()
{
	var rows = $('table').getElementsByTagName('tr');
	var idList = new Array();
	
	//--- we have to skip the first row, the header
	for (var i=1; i<rows.length; i++)
	{
		var inputs = rows[i].getElementsByTagName('input');
	
		if (inputs[0].checked)
			idList.push(rows[i].id);
	}
	
	return idList;
}

//=====================================================================================

function remove(id)
{
	Element.remove(id);
}

//=====================================================================================

function removeAll()
{
	gui.removeAllButFirst('table');	
}

//=====================================================================================
/* Refreshes the content of an entry on the main list 
 */
 
function refresh(node)
{
	var xslRes  = rowTransf.transform(node);
	var htmlRow = xml.toString(xslRes);
	
	var id = xslRes.getAttribute('id');
	
	//--- now we have to remove the <tr> </tr> root text
	
	var from = htmlRow.indexOf('>') +1;
	var to   = htmlRow.indexOf('</tr>');
	
	$(id).innerHTML = htmlRow.substring(from, to);	
	
	//--- add proper tooltips for both status and error columns
	
	setStatusTip(node);
	setErrorTip (node);
}

//=====================================================================================

function append(node)
{
	var xslRes  = rowTransf.transform(node);
	var htmlRow = xml.toString(xslRes);

	//--- add the new entry in list
	
	new Insertion.Bottom('table', htmlRow);
	
	//--- add proper tooltips for both status and error columns
	
	setStatusTip(node);
	setErrorTip (node);
}

//=====================================================================================

function setStatusTip(node)
{
	var id   = node.getAttribute('id');	
	var code = getStatusCode(node);
	
	var statusImg = xml.getElementById($(id), 'status');
	var statusTip = loader.eval('statusTip/'+code);
	
	new Tooltip(statusImg, statusTip);
}

//=====================================================================================

function getStatusCode(node)
{
	var status = xml.evalXPath(node, 'options/status');
	var running= xml.evalXPath(node, 'info/running');
	
	if (status == 'inactive')
		return status;
		
	if (running == 'true')
		return 'running';
		
	return status;
}

//=====================================================================================

function setErrorTip(node)
{
	var error = node.getElementsByTagName('error')[0];
	var tip   = null;
		
	if (xml.children(error).length == 0)
	{
		//--- harvesting ok
		
		var type = node.getAttribute('type');	
		var harv = harvesters[type];
		
		if (harv == null)		alert('Harvesting module not found!');
			else					tip = harv.getResultTip(node);
	}
	else
	{
		//--- we got some errors

		tip = xml.toString(errTransf.transform(node));		
	}
	
	var id = node.getAttribute('id');
	
	var errorImg = xml.getElementById($(id), 'error');
	
	new Tooltip(errorImg, tip);
}

//=====================================================================================

/* Enters 'add' mode showing the GN or WAF editing panel and filling it with empty data
 */

function newNode()
{
	//--- turn off adding mode
	addingFlag = true;
	
	var type = $('add.type').value;
	var harv = harvesters[type];
	
	if (harv == null)
		alert('Harvesting module not found!');
	else
	{
		harv.setEmpty();
		editSwitcher.show(harv.getEditPanel());
		currHarv = harv;
		
		//--- show edit panel
		show(SHOW.EDIT);
	}
}	

//=====================================================================================
/* Enters 'edit' mode showing the GN or WAF editing panel and filling it with given data
 */

function edit(node)
{
	//--- turn off adding mode
	addingFlag = false;
	
	var type = node.getAttribute('type');	
	var harv = harvesters[type];
	
	if (harv == null)
		alert('Harvesting module not found!');
	else
	{
		harv.setData(node);
		editSwitcher.show(harv.getEditPanel());
		currHarv = harv;
		
		//--- show edit panel
		show(SHOW.EDIT);
	}
}

//=====================================================================================

function getUpdateRequest()
{
	return currHarv.getUpdateRequest();
}

//=====================================================================================
}
