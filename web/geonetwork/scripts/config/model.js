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
			CSW_ENABLE        : xml.evalXPath(node, 'csw/enable'),
			CSW_CONTACTID     : xml.evalXPath(node, 'csw/contactId'),
			CSW_INDIVIDUALNAME: xml.evalXPath(node, 'csw/individualName'),
			CSW_POSITIONNAME  : xml.evalXPath(node, 'csw/positionName'),
			CSW_VOICE         : xml.evalXPath(node, 'csw/contactInfo/phone/voice'),
			CSW_FACSIMILE     : xml.evalXPath(node, 'csw/contactInfo/phone/facsimile'),
			CSW_DELIVERYPOINT : xml.evalXPath(node, 'csw/contactInfo/address/deliveryPoint'),
			CSW_CITY          : xml.evalXPath(node, 'csw/contactInfo/address/city'),
			CSW_ADMINAREA     : xml.evalXPath(node, 'csw/contactInfo/address/administrativeArea'),
			CSW_POSTALCODE    : xml.evalXPath(node, 'csw/contactInfo/address/postalCode'),
			CSW_COUNTRY       : xml.evalXPath(node, 'csw/contactInfo/address/country'),
			CSW_EMAIL         : xml.evalXPath(node, 'csw/contactInfo/address/email'),
			CSW_HOURSOFSERVICE: xml.evalXPath(node, 'csw/contactInfo/hoursOfService'),
			CSW_INSTRUCTIONS  : xml.evalXPath(node, 'csw/contactInfo/contactInstructions'),
			CSW_ROLE          : xml.evalXPath(node, 'csw/role'),
			CSW_TITLE         : xml.evalXPath(node, 'csw/title'),
			CSW_ABSTRACT      : xml.evalXPath(node, 'csw/abstract'),
			CSW_FEES          : xml.evalXPath(node, 'csw/fees'),
			CSW_ACCESS        : xml.evalXPath(node, 'csw/accessConstraints'),
			CLICKABLE_HYPERLINKS         : xml.evalXPath(node, 'clickablehyperlinks/enable'),			
			LOCAL_RATING      : xml.evalXPath(node, 'localrating/enable'),			
			PROXY_USE         : xml.evalXPath(node, 'proxy/use'),
			PROXY_HOST        : xml.evalXPath(node, 'proxy/host'),
			PROXY_PORT        : xml.evalXPath(node, 'proxy/port'),
			PROXY_USER        : xml.evalXPath(node, 'proxy/username'),
			PROXY_PASS        : xml.evalXPath(node, 'proxy/password'),
			FEEDBACK_EMAIL    : xml.evalXPath(node, 'feedback/email'),
			FEEDBACK_MAIL_HOST: xml.evalXPath(node, 'feedback/mailServer/host'),
			FEEDBACK_MAIL_PORT: xml.evalXPath(node, 'feedback/mailServer/port'),
			REMOVEDMD_DIR     : xml.evalXPath(node, 'removedMetadata/dir'),

			LDAP_USE          : xml.evalXPath(node, 'ldap/use'),
			LDAP_HOST         : xml.evalXPath(node, 'ldap/host'),
			LDAP_PORT         : xml.evalXPath(node, 'ldap/port'),
			LDAP_DEF_PROFILE  : xml.evalXPath(node, 'ldap/defaultProfile'),
			LDAP_DN_BASE      : xml.evalXPath(node, 'ldap/distinguishedNames/base'),
			LDAP_DN_USERS     : xml.evalXPath(node, 'ldap/distinguishedNames/users'),
			LDAP_ATTR_NAME    : xml.evalXPath(node, 'ldap/userAttribs/name'),
			LDAP_ATTR_PROFILE : xml.evalXPath(node, 'ldap/userAttribs/profile'),

			SHIB_USE              : xml.evalXPath(node, 'shib/use'),
			SHIB_PATH             : xml.evalXPath(node, 'shib/path'),
			SHIB_ATTRIB_USERNAME  : xml.evalXPath(node, 'shib/attrib/username'),
			SHIB_ATTRIB_SURNAME   : xml.evalXPath(node, 'shib/attrib/surname'),
			SHIB_ATTRIB_FIRSTNAME : xml.evalXPath(node, 'shib/attrib/firstname'),
			SHIB_ATTRIB_PROFILE   : xml.evalXPath(node, 'shib/attrib/profile'),

			USERSELFREGISTRATION_ENABLE : xml.evalXPath(node, 'userSelfRegistration/enable')
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
'	<csw>'+
'		<enable>{CSW_ENABLE}</enable>'+
'		<contactId>{CSW_CONTACTID}</contactId>'+
'		<individualName>{CSW_INDIVIDUALNAME}</individualName>'+
'		<positionName>{CSW_POSITIONNAME}</positionName>'+
'		<contactInfo>'+
'			<phone>'+
'				<voice>{CSW_VOICE}</voice>'+
'				<facsimile>{CSW_FACSIMILE}</facsimile>'+
'			</phone>'+
'			<address>'+
'				<deliveryPoint>{CSW_DELIVERYPOINT}</deliveryPoint>'+
'				<city>{CSW_CITY}</city>'+
'				<administrativeArea>{CSW_ADMINAREA}</administrativeArea>'+
'				<postalCode>{CSW_POSTALCODE}</postalCode>'+
'				<country>{CSW_COUNTRY}</country>'+
'				<email>{CSW_EMAIL}</email>'+
'			</address>'+
'			<hoursOfService>{CSW_HOURSOFSERVICE}</hoursOfService>'+
'			<contactInstructions>{CSW_INSTRUCTIONS}</contactInstructions>'+
'		</contactInfo>'+
'		<role>{CSW_ROLE}</role>'+
'		<title>{CSW_TITLE}</title>'+
'		<abstract>{CSW_ABSTRACT}</abstract>'+
'		<fees>{CSW_FEES}</fees>'+
'		<accessConstraints>{CSW_ACCESS}</accessConstraints>'+
'	</csw>'+
'	<clickablehyperlinks>' +
'		<enable>{CLICKABLE_HYPERLINKS}</enable>'+
'	</clickablehyperlinks>' +
'	<localrating>' +
'		<enable>{LOCAL_RATING}</enable>'+
'	</localrating>' +
'	<proxy>'+
'		<use>{PROXY_USE}</use>'+
'		<host>{PROXY_HOST}</host>'+
'		<port>{PROXY_PORT}</port>'+
'		<username>{PROXY_USER}</username>'+
'		<password>{PROXY_PASS}</password>'+
'	</proxy>'+
'	<feedback>'+
'		<email>{FEEDBACK_EMAIL}</email>'+
'		<mailServer>'+
'			<host>{FEEDBACK_MAIL_HOST}</host>'+
'			<port>{FEEDBACK_MAIL_PORT}</port>'+
'		</mailServer>'+
'	</feedback>'+
'	<removedMetadata>'+
'		<dir>{REMOVEDMD_DIR}</dir>'+
'	</removedMetadata>'+
'	<ldap>'+
'		<use>{LDAP_USE}</use>'+
'		<host>{LDAP_HOST}</host>'+
'		<port>{LDAP_PORT}</port>'+
'		<defaultProfile>{LDAP_DEF_PROFILE}</defaultProfile>'+
'		<distinguishedNames>'+
'			<base>{LDAP_DN_BASE}</base>'+
'			<users>{LDAP_DN_USERS}</users>'+
'		</distinguishedNames>'+
'		<userAttribs>'+
'			<name>{LDAP_ATTR_NAME}</name>'+
'			<profile>{LDAP_ATTR_PROFILE}</profile>'+
'		</userAttribs>'+
'	</ldap>'+
'	<shib>'+
'		<use>{SHIB_USE}</use>'+
'		<path>{SHIB_PATH}</path>'+
'		<attrib>'+
'			<username>{SHIB_ATTRIB_USERNAME}</username>'+
'			<surname>{SHIB_ATTRIB_SURNAME}</surname>'+
'			<firstname>{SHIB_ATTRIB_FIRSTNAME}</firstname>'+
'			<profile>{SHIB_ATTRIB_PROFILE}</profile>'+
'		</attrib>'+
'	</shib>'+
' <userSelfRegistration>'+
'		<enable>{USERSELFREGISTRATION_ENABLE}</enable>'+
' </userSelfRegistration>'+
'</config>';

//=====================================================================================
