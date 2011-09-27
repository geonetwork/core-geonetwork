//=====================================================================================
//===
//=== Harvesting model
//===
//=====================================================================================

function Model(xmlLoader)
{
	var loader = xmlLoader;

	//--- public methods
	
	this.getNodes    = getNodes;
	this.getNode     = getNode;
	this.removeNodes = removeNodes;
	this.startNodes  = startNodes;
	this.stopNodes   = stopNodes;
	this.runNodes    = runNodes;
	this.addNode     = addNode;
	this.updateNode  = updateNode;
	this.cloneNode   = cloneNode;
	
//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

function getNodes(callBack)
{
	this.getNodesCB = callBack;
	ker.send('xml.harvesting.get', '<request/>', ker.wrap(this, getNodes_OK));	
}

//-------------------------------------------------------------------------------------

function getNodes_OK(xmlRes)
{
	if (xmlRes.nodeName == 'error')
		ker.showError(loader.getText('cannotGet'), xmlRes);
	else
		this.getNodesCB(xmlRes);
}

//=====================================================================================

function getNode(id, callBack)
{
	var req = ker.createRequest('id', id);
	
	this.getNodeCB = callBack;
	ker.send('xml.harvesting.get', req, ker.wrap(this, getNode_OK));
}

//-------------------------------------------------------------------------------------

function getNode_OK(xmlRes)
{
	if (xmlRes.nodeName == 'error')
		ker.showError(loader.getText('cannotGet'), xmlRes);
	else
		this.getNodeCB(xmlRes);
}

//=====================================================================================

function removeNodes(idList, callBack)
{
	var request= ker.createRequest('id', idList);
	
	this.removeNodesCB = callBack;
	ker.send('xml.harvesting.remove', request, ker.wrap(this, removeNodes_OK));
}

//-------------------------------------------------------------------------------------

function removeNodes_OK(xmlRes)
{
	if (xmlRes.nodeName == 'error')
		ker.showError(loader.getText('cannotRemove'), xmlRes);
	else
		this.removeNodesCB(buildIdList(xmlRes));
}

//=====================================================================================

function startNodes(idList, callBack)
{
	var request= ker.createRequest('id', idList);
	
	this.startNodesCB = callBack;
	ker.send('xml.harvesting.start', request, ker.wrap(this, startNodes_OK));
}
	
//-------------------------------------------------------------------------------------

function startNodes_OK(xmlRes)
{
	if (xmlRes.nodeName == 'error')
		ker.showError(loader.getText('cannotStart'), xmlRes);
	else
		this.startNodesCB(buildIdList(xmlRes));
}

//=====================================================================================

function cloneNode(idList, callBack)
{
	var request= ker.createRequest('id', idList);
	
	this.cloneNodeCB = callBack;
	ker.send('xml.harvesting.clone', request, ker.wrap(this, cloneNode_OK));
}
	
//=====================================================================================

function cloneNode_OK(xmlRes)
{
	if (xmlRes.nodeName == 'error')
		ker.showError(loader.getText('cannotGet'), xmlRes);
	else
		this.cloneNodeCB(xmlRes);
}

//=====================================================================================

function stopNodes(idList, callBack)
{
	var request= ker.createRequest('id', idList);
	
	this.stopNodesCB = callBack;
	ker.send('xml.harvesting.stop', request, ker.wrap(this, stopNodes_OK));
}
	
//-------------------------------------------------------------------------------------

function stopNodes_OK(xmlRes)
{
	if (xmlRes.nodeName == 'error')
		ker.showError(loader.getText('cannotStop'), xmlRes);
	else
		this.stopNodesCB(buildIdList(xmlRes));
}

//=====================================================================================

function runNodes(idList, callBack)
{
	var request= ker.createRequest('id', idList);
	
	this.runNodesCB = callBack;
	ker.send('xml.harvesting.run', request, ker.wrap(this, runNodes_OK));
}

//-------------------------------------------------------------------------------------

function runNodes_OK(xmlRes)
{
	if (xmlRes.nodeName == 'error')
		ker.showError(loader.getText('cannotRun'), xmlRes);
	else
		this.runNodesCB(buildIdList(xmlRes));
}
//=====================================================================================

function addNode(request, callBack)
{
	this.addNodeCB = callBack;	
	ker.send('xml.harvesting.add', request, ker.wrap(this, addNode_OK));
}

//-------------------------------------------------------------------------------------

function addNode_OK(xmlRes)
{
	if (xmlRes.nodeName == 'error')
		ker.showError(loader.getText('cannotAdd'), xmlRes);
	else
		this.addNodeCB(xmlRes);
}

//=====================================================================================

function updateNode(request, callBack)
{
	this.updateNodeCB = callBack;
	ker.send('xml.harvesting.update', request, ker.wrap(this, updateNode_OK));
}

//-------------------------------------------------------------------------------------

function updateNode_OK(xmlRes)
{
	if (xmlRes.nodeName == 'error')
		ker.showError(loader.getText('cannotUpdate'), xmlRes);
	else
		this.updateNodeCB(xmlRes);
}

//=====================================================================================
//===
//=== Private methods
//===
//=====================================================================================

function buildIdList(xmlRes)
{
	var ids = xmlRes.getElementsByTagName('id');
	var res = [];
	
	for (var i=0; i<ids.length; i++)
	{
		var id     = ids[i].firstChild.nodeValue;
		var status = ids[i].getAttribute('status');

		res.push({ ID:id, STATUS:status });
	}
	
	return res;
}

//=====================================================================================
}
