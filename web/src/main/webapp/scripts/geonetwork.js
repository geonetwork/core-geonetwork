// VARIABLE DECLARATIONS

var getGNServiceURL = function(service) {
	return Env.locService+"/"+service;
};

function init() {};

function translate(text) {
	return translations[text] || text;
};

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
			alert(translate("selectOneFile"));
			return;
		}

		Modalbox.show(getGNServiceURL('file.disclaimer') + "?" + pars, {height: 400, width: 600});
	}


/**********************************************************
 * Massive Operations are called through this routine
 **********************************************************/

	function massiveOperation(service, title, width, message)
	{

		if (message != null) {
			if(!confirm(message))
				return;
		}

		var url = Env.locService +'/' + service;
		Modalbox.show(url,{title: title, width: width, afterHide: function() {
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
 * Massive Ownership Transfer stuff
 **********************************************************************/

	function checkMassiveNewOwner(action,title) {
		if ($('user').value == '') {
			alert(translate("selectNewOwner"));
			return false;
		}
		if ($('group').value == '') {
			alert(translate("selectOwnerGroup"));
			return false;
		}
		Modalbox.show(getGNServiceURL(action),{title: title, params: $('massivenewowner').serialize(true), afterHide: function() {
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
            	alert(translate("registrationFailed") + " " + req.responseText + " status: " + req.status + " - " + translate("tryAgain"));
						}
				}
		);
	}
	
	
	
	

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