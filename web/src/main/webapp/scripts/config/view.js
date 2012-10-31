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
		{ id:'site.siteId',   type:'length',   minSize :1,  maxSize :200 },
		{ id:'site.name',     type:'length',   minSize :1,  maxSize :200 },
		{ id:'site.organ',    type:'length',   minSize :0,  maxSize :200 },
		
		{ id:'server.host',   type:'length',   minSize :1,  maxSize :200 },
		{ id:'server.host',   type:'hostname' },
		{ id:'server.port',   type:'integer',  minValue:80, maxValue:65535 },
		
		{ id:'intranet.network', type:'ipaddress' },
		{ id:'intranet.netmask', type:'ipaddress' },

		{ id:'selection.maxrecords',   type:'integer',  minValue:1000, maxValue:100000, empty:false },

		{ id:'threadedindexing.maxthreads',   type:'integer',  minValue:1, maxValue:10, empty:false },

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

	this.shibShower  = new Shower('shib.use',     'shib.subpanel');
    this.inspireShower  = new Shower('inspire.enable',     'inspire.subpanel');

    Event.observe($('inspire.enable'), 'click', function() {
        $('inspire.enableSearchPanel').checked = $('inspire.enable').checked;

        if (!$('inspire.enable').checked) {
            $('metadata.enableInspireView').checked = false;
        }

        $('metadata.enableInspireView').disabled = !$('inspire.enable').checked;

        if ($('metadata.enableInspireView').disabled) {
            $("metadata.defaultView.Inspire").hide();
            if ($("metadata.defaultView").value == $("metadata.defaultView.Inspire").value) {
        		$("metadata.defaultView").value = ($('metadata.enableSimpleView').checked)?
                                                        $("metadata.defaultView.Simple").value:$("metadata.defaultView.Advanced").value;
            }

        } else {
            $("metadata.defaultView.Inspire").show();
        }
    });

    Event.observe($('server.protocol'), 'change', function() {
        if ($('server.protocol').value == 'https') {
            $('server.port').value = '443';
        } else {
            $('server.port').value = '8080';
        }

    });

    Event.observe($('metadata.enableSimpleView'), 'click', function() {
        if (!$('metadata.enableSimpleView').checked) {
            $("metadata.defaultView.Simple").hide();
            if ($("metadata.defaultView").value == $("metadata.defaultView.Simple").value) {
        		$("metadata.defaultView").value = ($('metadata.enableSimpleView').checked)?
                                                        $("metadata.defaultView.Simple").value:$("metadata.defaultView.Advanced").value;
            }

        } else {
            $("metadata.defaultView.Simple").show();
        }

    });

    Event.observe($('metadata.enableIsoView'), 'click', function() {
        if (!$('metadata.enableIsoView').checked) {
            $("metadata.defaultView.Iso").hide();
            if ($("metadata.defaultView").value == $("metadata.defaultView.Iso").value) {
        		$("metadata.defaultView").value = ($('metadata.enableSimpleView').checked)?
                                                        $("metadata.defaultView.Simple").value:$("metadata.defaultView.Advanced").value;
            }

        } else {
            $("metadata.defaultView.Iso").show();
        }
    });

    Event.observe($('metadata.enableInspireView'), 'click', function() {
        if (!$('metadata.enableInspireView').checked) {
            $("metadata.defaultView.Inspire").hide();
            if ($("metadata.defaultView").value == $("metadata.defaultView.Inspire").value) {
        		$("metadata.defaultView").value = ($('metadata.enableSimpleView').checked)?
                                                        $("metadata.defaultView.Simple").value:$("metadata.defaultView.Advanced").value;
            }

        } else {
            $("metadata.defaultView.Inspire").show();
        }
    });

    Event.observe($('metadata.enableXmlView'), 'click', function() {
        if (!$('metadata.enableXmlView').checked) {
            $("metadata.defaultView.Xml").hide();
            if ($("metadata.defaultView").value == $("metadata.defaultView.Xml").value) {
        		$("metadata.defaultView").value = ($('metadata.enableSimpleView').checked)?
                                                        $("metadata.defaultView.Simple").value:$("metadata.defaultView.Advanced").value;
            }

        } else {
            $("metadata.defaultView.Xml").show();
        }

    });
}


//=====================================================================================

ConfigView.prototype.init = function()
{
	gui.setupTooltips(this.strLoader.getNode('tips'));
}

//=====================================================================================
 
ConfigView.prototype.setData = function(data)
{
	$('site.siteId')  .value = data['SITE_ID'];
	$('site.name')  .value = data['SITE_NAME'];
	$('site.organ') .value = data['SITE_ORGAN'];
    $('server.protocol').value = data['SERVER_PROTOCOL'];
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

    $('autodetect.enable').checked = data['AUTODETECT_ENABLE'] == 'true';
    $('requestedLanguage.only').checked = data['REQUESTED_LANGUAGE_ONLY'] == 'true';
    $('requestedLanguage.sorted').checked = data['REQUESTED_LANGUAGE_SORTED'] == 'true';
    $('requestedLanguage.ignored').checked = data['REQUESTED_LANGUAGE_IGNORED'] == 'true';

	$('searchStats.enable').checked = data['SEARCHSTATS_ENABLE'] == 'true';

	$('downloadservice.simple')        .checked = data['DOWNLOADSERVICE_SIMPLE'] == 'true';
	$('downloadservice.withdisclaimer').checked = data['DOWNLOADSERVICE_WITHDISCLAIMER'] == 'true';
	$('downloadservice.leave')         .checked = data['DOWNLOADSERVICE_LEAVE'] == 'true';

	$('selection.maxrecords')  .value   = data['SELECTION_MAXRECORDS'];

	$('threadedindexing.maxthreads')  .value   = data['THREADEDINDEXING_MAXTHREADS'];

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
    $('inspire.enableSearchPanel').checked = data['INSPIRE_SEARCH_PANEL'] == 'true';

    $('metadata.enableSimpleView').checked = data['METADATA_SIMPLE_VIEW'] == 'true';
    $('metadata.enableIsoView').checked = data['METADATA_ISO_VIEW'] == 'true';
    $('metadata.enableInspireView').checked = data['METADATA_INSPIRE_VIEW'] == 'true';
    $('metadata.enableXmlView').checked = data['METADATA_XML_VIEW'] == 'true';
    $('metadata.defaultView').value = data['METADATA_DEFAULT_VIEW'];
    
    $('metadata.usergrouponly').checked = data['METADATA_PRIVS_USERGROUPONLY'] == 'true';
    
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

	$('shib.use')           .checked = data['SHIB_USE'] == 'true';
	$('shib.path')            .value = data['SHIB_PATH'];
	$('shib.attrib.username') .value = data['SHIB_ATTRIB_USERNAME'];
	$('shib.attrib.surname')  .value = data['SHIB_ATTRIB_SURNAME'];
	$('shib.attrib.firstname').value = data['SHIB_ATTRIB_FIRSTNAME'];
	$('shib.attrib.profile')  .value = data['SHIB_ATTRIB_PROFILE'];
    $('shib.attrib.group')    .value = data['SHIB_ATTRIB_GROUP'];
    $('shib.defGroup')  .value = data['SHIB_DEF_GROUP'];

	$('userSelfRegistration.enable').checked = data['USERSELFREGISTRATION_ENABLE'] == 'true';

	this.z3950Shower.update();
	this.indexOptimizerShower.update();
	this.proxyShower.update();
	this.shibShower.update();
	this.inspireShower.update();

    if (!$('inspire.enable').checked) {
	    $('metadata.enableInspireView').checked = false;
		$('metadata.enableInspireView').disabled = true;
    }

    if (!$('metadata.enableSimpleView').checked) $("metadata.defaultView.Simple").hide();
    if (!$('metadata.enableIsoView').checked) $("metadata.defaultView.Iso").hide();
    if (!$('metadata.enableInspireView').checked) $("metadata.defaultView.Inspire").hide();
    if (!$('metadata.enableXmlView').checked) $("metadata.defaultView.Xml").hide();
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
		SITE_ID   : $('site.siteId')  .value,
		SITE_NAME   : $('site.name')  .value,	
		SITE_ORGAN  : $('site.organ') .value,
        SERVER_PROTOCOL : $('server.protocol').value,
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

        AUTODETECT_ENABLE : $('autodetect.enable').checked,
        REQUESTED_LANGUAGE_ONLY: $('requestedLanguage.only').checked,
        REQUESTED_LANGUAGE_SORTED : $('requestedLanguage.sorted').checked,
        REQUESTED_LANGUAGE_IGNORED : $('requestedLanguage.ignored').checked,

		SEARCHSTATS_ENABLE : $('searchStats.enable').checked,
	
		DOWNLOADSERVICE_SIMPLE : $('downloadservice.simple').checked,
		DOWNLOADSERVICE_WITHDISCLAIMER : $('downloadservice.withdisclaimer').checked,
		DOWNLOADSERVICE_LEAVE : $('downloadservice.leave').checked,

		SELECTION_MAXRECORDS   : $('selection.maxrecords')  .value,

		THREADEDINDEXING_MAXTHREADS   : $('threadedindexing.maxthreads')  .value,

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
        INSPIRE_SEARCH_PANEL : $('inspire.enableSearchPanel').checked && $('inspire.enable').checked,

        METADATA_SIMPLE_VIEW : $('metadata.enableSimpleView').checked,
        METADATA_ISO_VIEW : $('metadata.enableIsoView').checked,
        METADATA_INSPIRE_VIEW : $('metadata.enableInspireView').checked,
        METADATA_XML_VIEW : $('metadata.enableXmlView').checked,
        METADATA_DEFAULT_VIEW: $('metadata.defaultView').value,

        METADATA_PRIVS_USERGROUPONLY : $('metadata.usergrouponly').checked,
        
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

		SHIB_USE              : $('shib.use').checked,
		SHIB_PATH             : $('shib.path').value,
		SHIB_ATTRIB_USERNAME  : $('shib.attrib.username').value,
		SHIB_ATTRIB_SURNAME   : $('shib.attrib.surname').value,
		SHIB_ATTRIB_FIRSTNAME : $('shib.attrib.firstname').value,
		SHIB_ATTRIB_PROFILE   : $('shib.attrib.profile').value,
        SHIB_ATTRIB_GROUP     : $('shib.attrib.group').value,
        SHIB_DEF_GROUP    : $F('shib.defGroup'),

		USERSELFREGISTRATION_ENABLE : $('userSelfRegistration.enable').checked

	}
	
	return data;
}

//=====================================================================================
