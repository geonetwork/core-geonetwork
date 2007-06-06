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
		
		{ id:'z3950.port',   type:'integer',  minValue:80, maxValue:65535, empty:true },
		
		{ id:'feedback.email',     type:'length',   minSize :0,  maxSize :200 },		
		{ id:'feedback.mail.host', type:'length',   minSize :0,  maxSize :200 },
		{ id:'feedback.mail.host', type:'hostname' },
		{ id:'feedback.mail.port', type:'integer',  minValue:25, maxValue:65535, empty:true },
		
		{ id:'proxy.host',   type:'length',   minSize :0,  maxSize :200 },
		{ id:'proxy.host',   type:'hostname' },
		{ id:'proxy.port',   type:'integer',  minValue:21, maxValue:65535, empty:true },

		{ id:'removedMd.dir', type:'length', minSize :0,  maxSize :200 }	
	]);
	
	this.z3950Shower = new Shower('z3950.enable', 'z3950.subpanel');	
	this.proxyShower = new Shower('proxy.use',    'proxy.subpanel');
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
	
	$('proxy.use') .checked = data['PROXY_USE'] == 'true';
	$('proxy.host').value   = data['PROXY_HOST'];
	$('proxy.port').value   = data['PROXY_PORT'];
	
	$('feedback.email')    .value = data['FEEDBACK_EMAIL'];
	$('feedback.mail.host').value = data['FEEDBACK_MAIL_HOST'];
	$('feedback.mail.port').value = data['FEEDBACK_MAIL_PORT'];
	
	$('removedMd.dir').value = data['REMOVEDMD_DIR'];

	this.z3950Shower.update();
	this.proxyShower.update();
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
	
		PROXY_USE  : $('proxy.use') .checked,
		PROXY_HOST : $('proxy.host').value,
		PROXY_PORT : $('proxy.port').value,
		
		FEEDBACK_EMAIL     : $('feedback.email')    .value,
		FEEDBACK_MAIL_HOST : $('feedback.mail.host').value,
		FEEDBACK_MAIL_PORT : $('feedback.mail.port').value,		

		REMOVEDMD_DIR : $('removedMd.dir').value
	}
	
	return data;
}

//=====================================================================================

