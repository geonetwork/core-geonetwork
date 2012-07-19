//=====================================================================================
//===
//=== Handles all view related stuff in the MVC pattern
//===
//=====================================================================================

function ConfigView(strLoader)
{
	//--- setup validators
	this.strLoader = strLoader;
	this.validator = new Validator(strLoader);

	this.validator.add(
	[
		{ id:'site.name',     type:'length',   minSize :1,  maxSize :200 },
		{ id:'site.organ',    type:'length',   minSize :0,  maxSize :200 },
		
		{ id:'server.host',   type:'length',   minSize :1,  maxSize :200 },
		{ id:'server.host',   type:'hostname' },
		{ id:'server.port',   type:'integer',  minValue:80, maxValue:65535 },
		
		{ id:'intranet.network', type:'ipaddress' },
		{ id:'intranet.netmask', type:'ipaddress' },

		{ id:'selection.maxrecords',   type:'integer',  minValue:1000, maxValue:10000, empty:false },

		{ id:'indexoptimizer.at.hour',   type:'integer',  minValue:0, maxValue:23, empty:false }, 
		{ id:'indexoptimizer.at.min',   type:'integer',  minValue:0, maxValue:59, empty:false }, 
		{ id:'indexoptimizer.at.sec',   type:'integer',  minValue:0, maxValue:59, empty:false }, 
		{ id:'indexoptimizer.interval.day',   type:'integer',  minValue:0, maxValue:14, empty:false }, 
		{ id:'indexoptimizer.interval.hour',   type:'integer',  minValue:0, maxValue:65535, empty:false }, 
		{ id:'indexoptimizer.interval.min',   type:'integer',  minValue:0, maxValue:59, empty:false }, 

		{ id:'z3950.port',   type:'integer',  minValue:80, maxValue:65535, empty:true },

		{ id:'oai.mdmode',   type:'integer',  minValue:1, maxValue:2, empty:false },
		{ id:'oai.tokentimeout',   type:'integer',  minValue:60, maxValue:86400, empty:false },
		{ id:'oai.cachesize',   type:'integer',  minValue:10, maxValue:1000, empty:false },

		{ id:'feedback.email',     type:'length',   minSize :0,  maxSize :200 },		
		{ id:'feedback.mail.host', type:'length',   minSize :0,  maxSize :200 },
		{ id:'feedback.mail.host', type:'hostname' },
		{ id:'feedback.mail.port', type:'integer',  minValue:25, maxValue:65535, empty:true },
		
		{ id:'proxy.host',     type:'length',   minSize :0,  maxSize :200 },
		{ id:'proxy.host',     type:'hostname' },
		{ id:'proxy.port',     type:'integer',  minValue:21, maxValue:65535, empty:true },
		{ id:'proxy.username', type:'length',  minSize :0,  maxSize :200 },
		{ id:'proxy.password', type:'length',  minSize :0,  maxSize :200 },

		{ id:'removedMd.dir', type:'length', minSize :0,  maxSize :200 },
		
		{ id:'ldap.host',         type:'length',   minSize :0, maxSize :200 },
		{ id:'ldap.host',         type:'hostname' },
		{ id:'ldap.port',         type:'integer',  minValue:80, maxValue:65535, empty:true },		
		{ id:'ldap.baseDN',       type:'length',  minSize :1,  maxSize :200 },
		{ id:'ldap.usersDN',      type:'length',  minSize :1,  maxSize :200 },
		{ id:'ldap.bindDN',       type:'length',  minSize :1,  maxSize :200 },
		{ id:'ldap.bindPW',       type:'length',  minSize :1,  maxSize :200 },
		{ id:'ldap.nameAttr',     type:'length',  minSize :1,  maxSize :200 },
        { id:'ldap.uidAttr',      type:'length',  minSize :1,  maxSize :20 },

		{ id:'shib.path',              type:'length',   minSize :0, maxSize :256 },
		{ id:'shib.attrib.username',   type:'length',   minSize :0, maxSize :150 },
		{ id:'shib.attrib.surname',    type:'length',   minSize :0, maxSize :150 },
		{ id:'shib.attrib.firstname',  type:'length',   minSize :0, maxSize :150 },
		{ id:'shib.attrib.profile',    type:'length',   minSize :0, maxSize :150 }
	]);
	
	this.z3950Shower = new Shower('z3950.enable', 'z3950.subpanel');	

	var dsTargetIds = ['downloadservice_simple.subpanel', 'downloadservice_withdisclaimer.subpanel', 'downloadservice_leave.subpanel'];
	this.downloadservicesimpleShower  = new RadioShower('downloadservice.simple',     'downloadservice_simple.subpanel', dsTargetIds);
	this.downloadservicewithdisclaimerShower  = new RadioShower('downloadservice.withdisclaimer',     'downloadservice_withdisclaimer.subpanel', dsTargetIds);
	this.downloadserviceleaveShower  = new RadioShower('downloadservice.leave',     'downloadservice_leave.subpanel', dsTargetIds);

	this.indexOptimizerShower = new Shower('indexoptimizer.enable', 'indexoptimizer.subpanel');
	this.proxyShower = new Shower('proxy.use',    'proxy.subpanel');

	var targetIds = ['ldap.subpanel', 'geonetworkdb.subpanel'];
	this.ldapShower  = new RadioShower('ldap.use',     'ldap.subpanel', targetIds);
	this.geonetworkdbShower  = new RadioShower('geonetworkdb.use',     'geonetworkdb.subpanel', targetIds);

	this.shibShower  = new Shower('shib.use',     'shib.subpanel');
}

//=====================================================================================

ConfigView.prototype.init = function()
{
	gui.setupTooltips(this.strLoader.getNode('tips'));
}

//=====================================================================================
 
ConfigView.prototype.setData = function(data)
{
	$('site.name')  .value = data['SITE_NAME'];
	$('site.organ') .value = data['SITE_ORGAN'];	
	$('server.host').value = data['SERVER_HOST'];
	$('server.port').value = data['SERVER_PORT'];
	
	$('intranet.network').value = data['INTRANET_NETWORK'];
	$('intranet.netmask').value = data['INTRANET_NETMASK'];
	
	$('z3950.enable').checked = data['Z3950_ENABLE'] == 'true';
	$('z3950.port')  .value   = data['Z3950_PORT'];

	$('oai.mdmode').value = data['OAI_MDMODE'];
	$('oai.cachesize').value   = data['OAI_CACHESIZE'];
	$('oai.tokentimeout').value   = data['OAI_TOKENTIMEOUT'];
	
	$('xlinkResolver.enable').checked = data['XLINKRESOLVER_ENABLE'] == 'true';

	$('searchStats.enable').checked = data['SEARCHSTATS_ENABLE'] == 'true';

	$('downloadservice.simple')        .checked = data['DOWNLOADSERVICE_SIMPLE'] == 'true';
	$('downloadservice.withdisclaimer').checked = data['DOWNLOADSERVICE_WITHDISCLAIMER'] == 'true';
	$('downloadservice.leave')         .checked = data['DOWNLOADSERVICE_LEAVE'] == 'true';

	$('selection.maxrecords')  .value   = data['SELECTION_MAXRECORDS'];

	$('indexoptimizer.enable').checked = data['INDEXOPTIMIZER_ENABLE'] == 'true';
	$('indexoptimizer.at.hour').value = data['INDEXOPTIMIZER_AT_HOUR'];
	$('indexoptimizer.at.min').value  = data['INDEXOPTIMIZER_AT_MIN'];
	$('indexoptimizer.at.sec').value  = data['INDEXOPTIMIZER_AT_SEC'];
	$('indexoptimizer.interval.day').value  = data['INDEXOPTIMIZER_INTERVAL_DAY'];
	$('indexoptimizer.interval.hour').value = data['INDEXOPTIMIZER_INTERVAL_HOUR'];
	$('indexoptimizer.interval.min').value  = data['INDEXOPTIMIZER_INTERVAL_MIN'];
	
	$('clickablehyperlinks.enable').checked = data['CLICKABLE_HYPERLINKS'] == 'true';

	$('localrating.enable').checked = data['LOCAL_RATING'] == 'true';
	$('autofixing.enable').checked = data['AUTO_FIXING'] == 'true';
    $('inspire.enable').checked = data['INSPIRE'] == 'true';
    $('harvester.enableEditing').checked = data['HARVESTER'] == 'true';

	$('proxy.use') .checked   = data['PROXY_USE'] == 'true';
	$('proxy.host').value     = data['PROXY_HOST'];
	$('proxy.port').value     = data['PROXY_PORT'];
	$('proxy.username').value = data['PROXY_USER'];
	$('proxy.password').value = data['PROXY_PASS'];
	
	$('feedback.email')    .value = data['FEEDBACK_EMAIL'];
	$('feedback.mail.host').value = data['FEEDBACK_MAIL_HOST'];
	$('feedback.mail.port').value = data['FEEDBACK_MAIL_PORT'];
	
	$('removedMd.dir').value = data['REMOVEDMD_DIR'];

	$('ldap.use')       .checked = data['LDAP_USE'] == 'true';
	$('ldap.host')        .value = data['LDAP_HOST'];
	$('ldap.port')        .value = data['LDAP_PORT'];
	$('ldap.defProfile')  .value = data['LDAP_DEF_PROFILE'];
    $('ldap.uidAttr')     .value = data['LDAP_ATTR_UID'];
	$('ldap.baseDN')      .value = data['LDAP_DN_BASE'];
	$('ldap.usersDN')     .value = data['LDAP_DN_USERS'];
	$('ldap.subtree')   .checked = data['LDAP_SUBTREE'] == 'true' ;
	$('ldap.anonBind')  .checked = data['LDAP_ANON_BIND'] == 'true' ; 
	$('ldap.bindDN')      .value = data['LDAP_DN_BIND'];
	$('ldap.bindPW')      .value = data['LDAP_PW_BIND'];
	$('ldap.nameAttr')    .value = data['LDAP_ATTR_NAME'];
	$('ldap.profileAttr') .value = data['LDAP_ATTR_PROFILE'];
	
	$('shib.use')           .checked = data['SHIB_USE'] == 'true';
	$('shib.path')            .value = data['SHIB_PATH'];
	$('shib.attrib.username') .value = data['SHIB_ATTRIB_USERNAME'];
	$('shib.attrib.surname')  .value = data['SHIB_ATTRIB_SURNAME'];
	$('shib.attrib.firstname').value = data['SHIB_ATTRIB_FIRSTNAME'];
	$('shib.attrib.profile')  .value = data['SHIB_ATTRIB_PROFILE'];

	$('geonetworkdb.use').checked = data['LDAP_USE'] != 'true';

	$('userSelfRegistration.enable').checked = data['USERSELFREGISTRATION_ENABLE'] == 'true' && data['LDAP_USE'] != 'true';

	this.z3950Shower.update();
	this.indexOptimizerShower.update();
	this.proxyShower.update();
	this.ldapShower.update();
	this.shibShower.update();
	this.geonetworkdbShower.update();
}

//=====================================================================================

ConfigView.prototype.isDataValid = function()
{
	return this.validator.validate();
}

//=====================================================================================

ConfigView.prototype.getData = function()
{
	var data =
	{
		SITE_NAME   : $('site.name')  .value,	
		SITE_ORGAN  : $('site.organ') .value,		
		SERVER_HOST : $('server.host').value,
		SERVER_PORT : $('server.port').value,
		
		INTRANET_NETWORK : $('intranet.network').value,
		INTRANET_NETMASK : $('intranet.netmask').value,
	
		Z3950_ENABLE : $('z3950.enable').checked,
		Z3950_PORT   : $('z3950.port')  .value,

		OAI_MDMODE				: $('oai.mdmode').value,
		OAI_TOKENTIMEOUT	: $('oai.tokentimeout')  .value,
		OAI_CACHESIZE			: $('oai.cachesize')  .value,

		XLINKRESOLVER_ENABLE : $('xlinkResolver.enable').checked,
	
		SEARCHSTATS_ENABLE : $('searchStats.enable').checked,
	
		DOWNLOADSERVICE_SIMPLE : $('downloadservice.simple').checked,
		DOWNLOADSERVICE_WITHDISCLAIMER : $('downloadservice.withdisclaimer').checked,
		DOWNLOADSERVICE_LEAVE : $('downloadservice.leave').checked,

		SELECTION_MAXRECORDS   : $('selection.maxrecords')  .value,

		INDEXOPTIMIZER_ENABLE: $('indexoptimizer.enable')  .checked,
		INDEXOPTIMIZER_AT_HOUR: $('indexoptimizer.at.hour').value,
		INDEXOPTIMIZER_AT_MIN:  $('indexoptimizer.at.min') .value,
		INDEXOPTIMIZER_AT_SEC:  $('indexoptimizer.at.sec') .value,
		INDEXOPTIMIZER_INTERVAL_DAY:  $('indexoptimizer.interval.day') .value,
		INDEXOPTIMIZER_INTERVAL_HOUR: $('indexoptimizer.interval.hour').value,
		INDEXOPTIMIZER_INTERVAL_MIN:  $('indexoptimizer.interval.min') .value,

		CLICKABLE_HYPERLINKS : $('clickablehyperlinks.enable').checked,
		
		LOCAL_RATING : $('localrating.enable').checked,
		AUTO_FIXING : $('autofixing.enable').checked,
        INSPIRE : $('inspire.enable').checked,
        HARVESTER : $('harvester.enableEditing').checked,

		PROXY_USE  : $('proxy.use') .checked,
		PROXY_HOST : $('proxy.host').value,
		PROXY_PORT : $('proxy.port').value,
		PROXY_USER : $('proxy.username').value,
		PROXY_PASS : $('proxy.password').value,
		
		FEEDBACK_EMAIL     : $('feedback.email')    .value,
		FEEDBACK_MAIL_HOST : $('feedback.mail.host').value,
		FEEDBACK_MAIL_PORT : $('feedback.mail.port').value,		

		REMOVEDMD_DIR : $('removedMd.dir').value,
		
		LDAP_USE           : $('ldap.use').checked,
		LDAP_HOST          : $F('ldap.host'),
		LDAP_PORT          : $F('ldap.port'),
		LDAP_DEF_PROFILE   : $F('ldap.defProfile'),
        LDAP_ATTR_UID      : $F('ldap.uidAttr'),                
		LDAP_DN_BASE       : $F('ldap.baseDN'),
		LDAP_DN_USERS      : $F('ldap.usersDN'),
		LDAP_SUBTREE       : $('ldap.subtree').checked,
		LDAP_ANON_BIND     : $('ldap.anonBind').checked,
		LDAP_DN_BIND       : $F('ldap.bindDN'),
		LDAP_PW_BIND       : $F('ldap.bindPW'),
		LDAP_ATTR_NAME     : $F('ldap.nameAttr'),
		LDAP_ATTR_PROFILE  : $F('ldap.profileAttr'),
		
		SHIB_USE              : $('shib.use').checked,
		SHIB_PATH             : $('shib.path').value,
		SHIB_ATTRIB_USERNAME  : $('shib.attrib.username').value,
		SHIB_ATTRIB_SURNAME   : $('shib.attrib.surname').value,
		SHIB_ATTRIB_FIRSTNAME : $('shib.attrib.firstname').value,
		SHIB_ATTRIB_PROFILE   : $('shib.attrib.profile').value,

		USERSELFREGISTRATION_ENABLE : $('userSelfRegistration.enable').checked && $('geonetworkdb.use').checked

	}
	
	return data;
}

//=====================================================================================
