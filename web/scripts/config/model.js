//=====================================================================================
//===
//=== ConfigModel class
//===
//=====================================================================================

function ConfigModel(strLoader)
{
	this.strLoader = strLoader;
}

//=====================================================================================

ConfigModel.prototype.getConfig = function(callBack)
{
	this.getConfigCB = callBack;
	
	ker.send('xml.config.get', '<request/>', ker.wrap(this, this.getConfig_OK), true);
}

//-------------------------------------------------------------------------------------

ConfigModel.prototype.getConfig_OK = function(node)
{
	if (node.nodeName == 'error')
		ker.showError(this.strLoader.getText('cannotGet'), node);
	else
	{
		var data =
		{
			SITE_NAME         : xml.evalXPath(node, 'site/name'),
			SITE_ORGAN        : xml.evalXPath(node, 'site/organization'),
			SERVER_HOST       : xml.evalXPath(node, 'server/host'),
			SERVER_PORT       : xml.evalXPath(node, 'server/port'),
			INTRANET_NETWORK  : xml.evalXPath(node, 'intranet/network'),
			INTRANET_NETMASK  : xml.evalXPath(node, 'intranet/netmask'),
			Z3950_ENABLE      : xml.evalXPath(node, 'z3950/enable'),
			Z3950_PORT        : xml.evalXPath(node, 'z3950/port'),
			PROXY_USE         : xml.evalXPath(node, 'proxy/use'),
			PROXY_HOST        : xml.evalXPath(node, 'proxy/host'),
			PROXY_PORT        : xml.evalXPath(node, 'proxy/port'),
			FEEDBACK_EMAIL    : xml.evalXPath(node, 'feedback/email'),
			FEEDBACK_MAIL_HOST: xml.evalXPath(node, 'feedback/mailServer/host'),
			FEEDBACK_MAIL_PORT: xml.evalXPath(node, 'feedback/mailServer/port')
		}
		
		this.getConfigCB(data);
	}
}

//=====================================================================================

ConfigModel.prototype.setConfig = function(data, callBack)
{
	this.updateCB = callBack;
	
	var request = str.substitute(ConfigModel.updateTemp, data);
	
	ker.send('xml.config.set', request, ker.wrap(this, this.setConfig_OK), true);
}

//-------------------------------------------------------------------------------------

ConfigModel.prototype.setConfig_OK = function(node)
{
	if (node.nodeName == 'error')
		ker.showError(this.strLoader.getText('cannotSave'), node);
	else
	{
		if (this.updateCB)
			this.updateCB();
	}
}

//=====================================================================================
//=== Private methods (or, at least, they should be so...)
//=====================================================================================

ConfigModel.updateTemp = 
'<config>'+
'	<site>'+
'		<name>{SITE_NAME}</name>'+
'		<organization>{SITE_ORGAN}</organization>'+
'	</site>'+
'	<server>'+
'		<host>{SERVER_HOST}</host>'+
'		<port>{SERVER_PORT}</port>'+
'	</server>'+
'	<intranet>'+
'		<network>{INTRANET_NETWORK}</network>'+
'		<netmask>{INTRANET_NETMASK}</netmask>'+
'	</intranet>'+
'	<z3950>'+
'		<enable>{Z3950_ENABLE}</enable>'+
'		<port>{Z3950_PORT}</port>'+
'	</z3950>'+
'	<proxy>'+
'		<use>{PROXY_USE}</use>'+
'		<host>{PROXY_HOST}</host>'+
'		<port>{PROXY_PORT}</port>'+
'	</proxy>'+
'	<feedback>'+
'		<email>{FEEDBACK_EMAIL}</email>'+
'		<mailServer>'+
'			<host>{FEEDBACK_MAIL_HOST}</host>'+
'			<port>{FEEDBACK_MAIL_PORT}</port>'+
'		</mailServer>'+
'	</feedback>'+
'</config>';

//=====================================================================================
