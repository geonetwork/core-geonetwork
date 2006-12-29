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
	
	gn.send('xml.config.get', '<request/>', gn.wrap(this, this.getConfig_OK));
}

//-------------------------------------------------------------------------------------

ConfigModel.prototype.getConfig_OK = function(xml)
{
	//--- skip the document node
	xml = xml.firstChild;

	if (xml.nodeName == 'error')
		gn.showError(this.strLoader.getText('cannotGet'), xml);
	else
	{
		var data =
		{
			SITE_NAME         : gn.evalXPath(xml, 'site/name'),
			SITE_ORGAN        : gn.evalXPath(xml, 'site/organization'),
			SERVER_HOST       : gn.evalXPath(xml, 'server/host'),
			SERVER_PORT       : gn.evalXPath(xml, 'server/port'),
			INTRANET_NETWORK  : gn.evalXPath(xml, 'intranet/network'),
			INTRANET_NETMASK  : gn.evalXPath(xml, 'intranet/netmask'),
			Z3950_ENABLE      : gn.evalXPath(xml, 'z3950/enable'),
			Z3950_PORT        : gn.evalXPath(xml, 'z3950/port'),
			PROXY_USE         : gn.evalXPath(xml, 'proxy/use'),
			PROXY_HOST        : gn.evalXPath(xml, 'proxy/host'),
			PROXY_PORT        : gn.evalXPath(xml, 'proxy/port'),
			FEEDBACK_EMAIL    : gn.evalXPath(xml, 'feedback/email'),
			FEEDBACK_MAIL_HOST: gn.evalXPath(xml, 'feedback/mailServer/host'),
			FEEDBACK_MAIL_PORT: gn.evalXPath(xml, 'feedback/mailServer/port')
		}
		
		this.getConfigCB(data);
	}
}

//=====================================================================================

ConfigModel.prototype.setConfig = function(data, callBack)
{
	this.updateCB = callBack;
	
	var request = gn.substitute(ConfigModel.updateTemp, data);
	
	gn.send('xml.config.set', request, gn.wrap(this, this.setConfig_OK));
}

//-------------------------------------------------------------------------------------

ConfigModel.prototype.setConfig_OK = function(xml)
{
	//--- skip the document node
	xml = xml.firstChild;
	
	if (xml.nodeName == 'error')
		gn.showError(this.strLoader.getText('cannotSave'), xml);
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
