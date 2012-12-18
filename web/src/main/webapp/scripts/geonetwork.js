// VARIABLE DECLARATIONS

var getGNServiceURL = function(service) {
	return Env.locService+"/"+service;
};

function init() {};

function translate(text) {
	return translations[text] || text;
};

// the following is necessary to add a missing js function to 
// IE9 so that extjs doesn't cause problems (in IE9)
// - see http://stackoverflow.com/questions/5375616/extjs4-ie9-object-doesnt-support-property-or-method-createcontextualfragme
// Note: meta tag in header.xsl is set to compatibility with IE9
// June, 2012
var Browser = {
  Version: function() {
    var version = 1000;
    if (navigator.appVersion.indexOf("MSIE") != -1)
      version = parseFloat(navigator.appVersion.split("MSIE")[1]);
    return version;
  }
};

if (Browser.Version() >= 9) {
	if (typeof Range.prototype.createContextualFragment == "undefined") {
    Range.prototype.createContextualFragment = function (html) {
        var doc = window.document;
        var container = doc.createElement("div");
        container.innerHTML = html;
        var frag = doc.createDocumentFragment(), n;
        while ((n = container.firstChild)) {
            frag.appendChild(n);
        }
        return frag;
    };
	}
}

/**
 * Replaces parameters in a string (defined like $1, $2, ...) with the values provided in the params array
 *
 * @param text
 * @param params
 */
function replaceStringParams(text, params) {
    var newText = text;

    for(var i = 0; i < params.length; i++) {
        newText = newText.replace("$" + (i+1), params[i]);
    }

    return newText;
}

// Read a cookie
function get_cookie ( cookie_name )
{
  var results = document.cookie.match ( cookie_name + '=(.*?)(;|$)' );

  if ( results )
    return ( unescape ( results[1] ) );
  else
    return null;
};

// New browser windows
	function popNew(a)
	{
		msgWindow=window.open(a,"displayWindow","location=no, toolbar=no, directories=no, status=no, menubar=no, scrollbars=yes, resizable=yes, width=800, height=600")
		msgWindow.focus()
	}

	function openPage(what,type)
	{
		msgWindow=window.open(what,type,"location=yes, toolbar=yes, directories=yes, status=yes, menubar=yes, scrollbars=yes, resizable=yes, width=800, height=600")
		msgWindow.focus()
	}

	function popFeedback(a)
	{
		msgWindow=window.open(a,"feedbackWindow","location=no, toolbar=no, directories=no, status=no, menubar=no, scrollbars=yes, resizable=yes, width=800, height=600")
		msgWindow.focus()
	}

	function popWindow(a)
	{
		msgWindow=window.open(a,"popWindow","location=no, toolbar=no, directories=no, status=no, menubar=no, scrollbars=yes, resizable=yes, width=800, height=600")
		msgWindow.focus()
	}

	function popInterMap(a)
	{
		msgWindow=window.open(a,"InterMap","location=no, toolbar=no, directories=no, status=no, menubar=no, scrollbars=yes, resizable=yes, width=800, height=600")
		msgWindow.focus()
	}

	var ViewEditWindow = Class.create({
		initialize: function(pane, id) {
			this.pane = pane;
			this.id = id;
		},
		editing: function() {
			if (this.pane && this.pane.closed) return false;
			if (this.pane.$('editForm')) return true;
			else return false;
		},
		focus: function() {
			this.pane.focus();
		},
		close: function() {
			this.pane.close();
		}
	});

	var viewEditWindows = [];

	function findWindow(id) {
		for (var i = 0, len = viewEditWindows.length; i < len; ++i) {
			var item = viewEditWindows[i];
			if (item.id == id) return item;
		}
		return null;
	}

	function popEditorViewer(a, id)
	{
		var viewEdit = findWindow(id);
		if (viewEdit && viewEdit.editing()) {
			viewEdit.focus();
			alert(translate('editorInUse'));
			return;
		} 

		var addToArray = false;
		if (viewEdit == null) addToArray = true;

		viewEdit = new ViewEditWindow(window.open(a,"MetadataEditorViewer"+id,"location=no, toolbar=no, directories=no, status=no, menubar=no, scrollbars=yes, resizable=yes, width=900, height=700"),id);
		viewEdit.focus();
		if (addToArray) viewEditWindows.push(viewEdit);

	}

	function checkEditorAndClose() {
		for (var i = 0, len = viewEditWindows.length; i < len; ++i) {
			var item = viewEditWindows[i];
			if (item.editing()) {
				item.focus()
				alert(translate('editorInUse'));
				return false;
			}
			item.close();
		}
		return true;
	}

	function doCreateCheck(service, form, modal) {

		// Nothing to check at present

		var params = $(form).serialize();
		if (modal == '1') {
					Modalbox.hide();
		}
		location.replace(getGNServiceURL(service) + '?' + params);
	}

	function popCreateWindow(a)
	{
		createWindow=window.open(a,"CreateMetadataWindow","location=no, toolbar=no, directories=no, status=no, menubar=no, scrollbars=yes, resizable=yes, width=800, height=600")
		createWindow.focus()
	}

	function popAdminWindow(a)
	{

		adminWindow=window.open(a,"AdminWindow","location=no, toolbar=no, directories=no, status=no, menubar=no, scrollbars=yes, resizable=yes, width=800, height=600")
		adminWindow.focus()
	}

// Forms
	function goSubmit(form_name) {
		document.forms[form_name].submit();
	}

	function goReset(form_name)
	{
		document.forms[form_name].reset();
	}

	function entSub(form_name) {
		if (window.event && window.event.keyCode == 13)
			goSubmit(form_name);
		else
			return true;
	}
    
// Navigation
	function goBack()
	{
		history.back();
	}

	function processCancel() {
		document.close();
	}

	function load(url)
	{
		document.location.href = url;
	}

	function doConfirm(url, message)
	{
		if(confirm(message))
		{
			load(url);
			return true;
		}
		return false;
	}

/**********************************************************
 * Logout - need to clear cookie from ext
 **********************************************************/
	
	function doLogout() 
	{
		var GNCookie = Ext.state.Manager.getProvider();
		var cookie = GNCookie.clear('params');
		goSubmit('logout');
	}

/**********************************************************
 * Download support 
 **********************************************************/

	function feedbackSubmit()
	{
		var f = $('feedbackf');
		if (isWhitespace(f.comments.value)) {
			f.comments.value = translate('noComment');
		}

		if (isWhitespace(f.name.value) || isWhitespace(f.org.value)) {
			alert(translate("addName"));
			return;
		} else if (!isEmail(f.email.value)) {
			alert(translate("checkEmail"));
			return;
		} 

		Modalbox.show(getGNServiceURL('file.download'),{height: 400, width: 600, params: f.serialize(true)});
	}

	function doDownload(id, all) {
		var list = $('downloadlist').getElementsByTagName('INPUT');
		var pars = '&id='+id+'&access=private';

		var selected = false;
		for (var i=0; i<list.length; i++) {
			if (list[i].checked || all != null) {
				selected = true;
				var name = list[i].getAttribute('name');
				pars += '&fname='+name;
			}
		}

		if (!selected) {
			alert(translate("selectOneFileDownload"));
			return;
		}

		Modalbox.show(getGNServiceURL('file.disclaimer') + "?" + pars, {height: 400, width: 600});
	}


/**********************************************************
 * Batch Operations are called through this routine
 **********************************************************/

	function batchOperation(service, title, width, message, height)
	{

		if (message != null) {
			if(!confirm(message))
				return;
		}

		var url = Env.locService +'/' + service;
		if (height === undefined) {
			Modalbox.show(url,{title: title, width: width, afterHide: function() {

						var url = Env.locService +'/' + service;
                if ($("simple_search_pnl").visible()) {
                    runSimpleSearch();

                } else if ($("advanced_search_pnl").visible()) {
                    runAdvancedSearch();

                } else {
                  $('search-results-content').hide();
                }
		
		        runRssSearch();
		
            }});
		} else { 
			Modalbox.show(url,{title: title, width: width, height: height, afterHide: function() {

						var url = Env.locService +'/' + service;
                if ($("simple_search_pnl").visible()) {
                    runSimpleSearch();

                } else if ($("advanced_search_pnl").visible()) {
                    runAdvancedSearch();

                } else {
                  $('search-results-content').hide();
                }
		
		        runRssSearch();
		
            }});
		}
	}

/**********************************************************
 * Select Actions 
 **********************************************************/

	function oActionsInit(name,id) {
	    if (id === undefined) {
	    	id = "";
	    }
	    $(name+'Ele'+id).style.width = $(name+id).getWidth();
	    $(name+'Ele'+id).style.top = $(name+id).positionedOffset().top + $(name+id).getHeight() + "px";
	    $(name+'Ele'+id).style.left = $(name+id).positionedOffset().left + "px";
	}


	function oActions(name,id) {
		var on        = "../../images/plus.gif";
        var off       = "../../images/minus.png";
		
		if (id === undefined) {
			id = "";
		}

		oActionsInit (name, id);

	  	if ($(name+'Ele'+id).style.display == 'none') {
	    	$(name+'Ele'+id).style.display = 'block';
	    	$(name+'Img'+id).src = off;
	  	} else {
	    	$(name+'Ele'+id).style.display = 'none';
	    	$(name+'Img'+id).src = on;
	  	}
	}

	function actionOnSelect(msg) {
		if ($('nbselected').innerHTML == 0 && $('oAcOsEle').style.display == 'none') {
			alert(msg);
		} else {
			oActions('oAcOs');
		}
	}

/**********************************************************************
 * Batch Extract Subtemplates stuff
 **********************************************************************/

	function checkBatchExtractSubtemplates(action,title) {
		if ($('xpath').value == '') {
			alert(translate("selectXPath"));
			return false;
		}
		if ($('xpathTitle').value == '') {
			alert(translate("selectExtractTitle"));
			return false;
		}
		Modalbox.show(getGNServiceURL(action),{title: title, params: $('extractSubtemplatesForm').serialize(true), height: 400});

	}

/**********************************************************************
 * Batch Ownership Transfer stuff
 **********************************************************************/

	function checkBatchNewOwner(action,title) {
		if ($('user').value == '') {
			alert(translate("selectNewOwner"));
			return false;
		}
		if ($('group').value == '') {
			alert(translate("selectOwnerGroup"));
			return false;
		}
		Modalbox.show(getGNServiceURL(action),{title: title, params: $('batchnewowner').serialize(true), afterHide: function() {
                if ($("simple_search_pnl").visible()) {

                    runSimpleSearch();

                } else if ($("advanced_search_pnl").visible()) {
                    runAdvancedSearch();

                } else {
                  $('search-results-content').hide();
                }
		
		        runRssSearch();
        }});
	}

	function addGroups(xmlRes) {
		var list = xml.children(xmlRes, 'group');
		$('group').options.length = 0; // clear out the options
		for (var i=0; i<list.length; i++) {
			var id     = xml.evalXPath(list[i], 'id');
			var name	 = xml.evalXPath(list[i], 'name');
			var opt = document.createElement('option');
			opt.text  = name;
			opt.value = id;
			if (list.length == 1) opt.selected = true;
			$('group').options.add(opt);
		}
	}

	function addGroupsCallback_OK(xmlRes) {
		if (xmlRes.nodeName == 'error') {
			ker.showError(translate('cannotRetrieveGroup'), xmlRes);
			$('group').options.length = 0; // clear out the options
			$('group').value = ''; 
			var user = $('user'); 
			for (i=0;i<user.options.length;i++) {
				user.options[i].selected = false;
			}
		} else {
			addGroups(xmlRes);
		}
	}
	
	function doGroups(userid) {
		var request = ker.createRequest('id',userid);
		ker.send('xml.usergroups.list', request, addGroupsCallback_OK);
	}

/**********************************************************************
 * User self-registration actions
 **********************************************************************/

	function processRegSub(url)
	{
		// check start
		var invalid = " "; // Invalid character is a space
		var minLength = 6; // Minimum length
            
		if (document.userregisterform.name.value.length == 0) {
			alert(translate('firstNameMandatory'));
			return;
		} 
		if (isWhitespace(document.userregisterform.name.value)) {
			alert(translate('firstNameMandatory'));
			return;
		}    
		if (document.userregisterform.name.value.indexOf(invalid) > -1) {
			alert(translate('spacesNot'));
			return;
		}	
			
		if (document.userregisterform.surname.value.length == 0) {
			alert(translate('lastNameMandatory'));
			return;
		}  
		if (isWhitespace(document.userregisterform.surname.value)) {
			alert(translate('lastNameMandatory'));
			return;
		}
		if (document.userregisterform.surname.value.indexOf(invalid) > -1) {
			alert(translate('spacesNot'));
			return;
		}
			
		if (!isEmail(document.userregisterform.email.value)) {
			alert(translate('emailAddressInvalid'));
			return;
		}
			
		var myAjax = new Ajax.Request(
			getGNServiceURL(url), 
				{
					method: 'post',
					parameters: $('userregisterform').serialize(true), 
						onSuccess: function(req) {
            	var output = req.responseText;
							var title = translate('yourRegistration');
        			Modalbox.show(output,{title: title, width: 300});
						},
						onFailure: function(req) {
            	var output = req.responseText;
							var title = translate('registrationFailed');
        			Modalbox.show(output,{title: title, width: 300});
            	//alert(translate("registrationFailed") + " " + req.responseText + " status: " + req.status + " - " + translate("tryAgain"));
						}
				}
		);
	}

	/**********************************************************
	***
	***	FORGOTTEN PASSWORD ACTIONS	
	***
	**********************************************************/
	       			
	function processForgottenPwdSubmit(url) {

		var f = $('forgottenpwd');
		if (isWhitespace(f.username.value)) {
			alert(translate("usernameMandatory"));
			return false;
		}

		var myAjax = new Ajax.Request(
			getGNServiceURL(url), 
				{
					method: 'post',
					parameters: f.serialize(true), 
						onSuccess: function(req) {
            	var output = req.responseText;
							var title = translate('changePassword');
        			Modalbox.show(output,{title: title, width: 300});
						},
						onFailure: function(req) {
            	var output = req.responseText;
							var title = translate('changePasswordFailed');
        			Modalbox.show(output,{title: title, width: 300});
						}
				}
		);
 	}
	  
	
/**********************************************************
***
***		BANNER MENU ACTIONS EXCLUDING LOGIN/LOGOUT
***
**********************************************************/

	var adminWindow;

	function doBannerButton(url, title, modal, width, height)
	{
		if (modal == '1') {
			if (height != null && height > 0) {
				Modalbox.show(url,{ params: { modal: ''}, title: title, height: height, width: width});
			} else {
				Modalbox.show(url,{ params: { modal: ''}, title: title, width: width});
			}
		} else {
			location.replace(url);
		}
		return true;
	}

	// Same as doBannerButton but afterhide we close adminWindow 
	function doAdminBannerButton(url, title, modal, width, height)
	{
		if (modal == '1') {
			Modalbox.show(url,{ params: { modal: ''}, title: title, height: height, width: width, evalScripts: true, afterHide: function() { if (adminWindow) adminWindow.close(); }});
		} else {
			location.replace(url);
		}
		return true;
	}

	var ViewEditWindow = Class.create({
		initialize: function(pane, id) {
			this.pane = pane;
			this.id = id;
		},
		editing: function() {
			if (this.pane && this.pane.closed) return false;
			if (this.pane.$('editForm')) return true;
			else return false;
		},
		focus: function() {
			this.pane.focus();
		},
		close: function() {
			this.pane.close();
		}
	});

	

/**
 * Display a popup, update the content if needed.
 * modal box are collapsibled and centered.
 */
function displayBox(content, contentDivId, modal) {
	var id = contentDivId + "Box";
	var w = Ext.getCmp(id);
	if (w == undefined) {
		w = new Ext.Window({
	        title: translate(contentDivId),
	        id: id,
	        layout: 'fit',
	        modal: modal,
	        constrain: true,
	        width: 400,
	        collapsible: (modal?false:true),
	        autoScroll: true,
	        iconCls: contentDivId + 'Icon',
	        closeAction: 'hide',
	        onEsc: 'hide',
	        listeners: {
	            hide: function() {
	                this.hide();
	            }
	        },
	        contentEl: contentDivId
	    });
	}
    if (w) {
    	if (content != null) {
    		$(contentDivId).innerHTML = '';
    		$(contentDivId).innerHTML = content;
    		$(contentDivId).style.display = 'block'
    	}
    	w.show();
    	w.setHeight(345);
    	w.anchorTo(Ext.getBody(), (modal?'c-c':'tr-tr'));	// Align top right if not modal, or center
    }

}

/**
 * Toggle visibility of an element
 * updating button icon.
 * 
 * @param btn	The button with downBt or rightBt css class icon
 * @param elem	The element to display or not
 */
function toggleFieldset(btn, elem) {
	if (btn.hasClassName('downBt')) {
		btn.removeClassName('downBt');
		elem.style.display='none';
		btn.addClassName('rightBt');		
	} else {
		btn.removeClassName('rightBt');
		elem.style.display='block';
		btn.addClassName('downBt');
	}
}

/**
 * Add templates and sample data in Admin Menu
 *
 */

function addTemplate(msgSelectSomething, successMsg) {
                          
	var url = "metadata.templates.add.default?schema=";
	var selectedSchemas = $('metadata.schemas.select');
	var params = "";
	for (i = 0;i < selectedSchemas.length;i++) {
		if (selectedSchemas.options[i].selected) {
			if (params != "") params += ",";
				params += selectedSchemas.options[i].value;
		}
	}

	if (params == "") {
		alert(msgSelectSomething);
		return;
	} else {
		url = url + params;
	}
				
	var wait = 'waitLoadingTemplatesSamples';
	var btn = 'addTemplatesSamplesButtons';
	$(wait).style.display = 'block';
	$(btn).style.display = 'none';
				
	var http = new Ajax.Request(
				url, 
				{
						method: 'get', 
						parameters: null,
						onComplete: function(originalRequest){},
						onLoaded: function(originalRequest){},
						onSuccess: function(originalRequest){                                       
							// get the XML root item
							var root = originalRequest.responseXML.documentElement;
							var resp = root.getAttribute('status');
							$(wait).style.display = 'none';
							$(btn).style.display = 'block';

							if (resp == "true")
                                alert (successMsg);
							else
								alert(translate('error'));

							var selectedSchemas = $('metadata.schemas.select');
							for (i = 0;i < selectedSchemas.length;i++) {
								selectedSchemas.options[i].selected = false;
							}
						},
						onFailure: function(originalRequest){
							$(wait).style.display = 'none';
							$(btn).style.display = 'block';
							alert('Failed');
				}
	});
}
            
function addSampleData(msgSelectSomething, msgFailedAddSampleMetadata, msgSuccessAddSampleMetadata) {
	var url = "metadata.samples.add?file_type=mef&uuidAction=overwrite&schema=";
	var selectedSchemas = $('metadata.schemas.select');
	var params = "";

	for (i = 0;i < selectedSchemas.length;i++) {
		if (selectedSchemas.options[i].selected) {
			if (params != "") params += ",";
			params += selectedSchemas.options[i].value;
		}
	}

	if (params == "") {
		alert(msgSelectSomething);
		return;
	} else {
		url = url + params;
	}
				
	var wait = 'waitLoadingTemplatesSamples';
	var btn = 'addTemplatesSamplesButtons';
	$(wait).style.display = 'block';
	$(btn).style.display = 'none';
				
	var http = new Ajax.Request(
					url, 
					{
						method: 'get', 
						parameters: null,
						onComplete: function(originalRequest){},
						onLoaded: function(originalRequest){},
						onSuccess: function(originalRequest){                                       
							// get the XML root item
							var root = originalRequest.responseXML.documentElement;
							var resp = root.getAttribute('status');
							var error = root.getAttribute('error');
							$(wait).style.display = 'none';
							$(btn).style.display = 'block';

							if (resp == "true")
								alert (msgSuccessAddSampleMetadata);
							else
								alert(translate('error')+": "+error);

							var selectedSchemas = $('metadata.schemas.select');
							for (i = 0;i < selectedSchemas.length;i++) {
								selectedSchemas.options[i].selected = false 
							}
						},
						onFailure: function(originalRequest){
							$(wait).style.display = 'none';
							$(btn).style.display = 'block';							
                            alert(msgFailedAddSampleMetadata);
						}
	});
}

function idxOperation(service, wait, btn, warning)
{
	if (warning && !confirm(translate('doYouReallyWantToDoThis'))) return;

	var url = Env.locService + '/' + service;
	$(wait).style.display = 'block';
	$(btn).style.display = 'none';
	var http = new Ajax.Request(
					    url, 
					    {
					      method: 'get', 
					      parameters: null,
					      onComplete: function(originalRequest){},
					      onLoaded: function(originalRequest){},
					      onSuccess: function(originalRequest){                                       
					        // get the XML root item
   					        var root = originalRequest.responseXML.documentElement;
					
					        var resp = root.getElementsByTagName('status')[0].firstChild.nodeValue;
					        $(wait).style.display = 'none';
					        $(btn).style.display = 'block';
					        if (resp == "true")
  					          alert (translate('metadata.admin.index.success'));
					        else
 					          alert(translate('metadata.admin.index.wait'));
					      },
					      onFailure: function(originalRequest){
					        $(wait).style.display = 'none';
					        $(btn).style.display = 'block';
					        alert(translate('metadata.admin.index.failed'));
					      }
					    }
	);
}
