// VARIABLE DECLARATIONS

function init() {}

// Read a cookie
function get_cookie ( cookie_name )
{
  var results = document.cookie.match ( cookie_name + '=(.*?)(;|$)' );

  if ( results )
    return ( unescape ( results[1] ) );
  else
    return null;
}

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

	function massiveOperation(service, title, width, message)
	{

		if (message != null) {
			if(!confirm(message))
				return;
		}

		var url = Env.locService +'/' + service;
		Modalbox.show(url,{title: title, width: width, afterHide: function() { $('search-results-content').hide();}});
	}

/**********************************************************
 * Select Actions 
 **********************************************************/

	function oActionsInit(name,id) {
   if (id === undefined) {
     id = "";
   }
   $(name+'Ele'+id).style.width = $(name+id).getWidth();
   $(name+'Ele'+id).style.top = $(name+id).positionedOffset().top + $(name+id).getHeight();
   $(name+'Ele'+id).style.left = $(name+id).positionedOffset().left;
	}

	function oActions(name,id) {
		if (id === undefined) {
			id = "";
  	}
		if (!$(name+'Ele'+id).style.top)
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

	// These two alerts should use localized versions of the strings 
	// in xml/metadata-massiveOwnership.xml 
	
		if ($('user').value == '') {
			alert("Select the user who will be the new owner");
			return false;
		}
		if ($('group').value == '') {
			alert("Select a group that the selected user belongs to");
			return false;
		}
		Modalbox.show(getGNServiceURL(action),{title: title, params: $('massivenewowner').serialize(true), afterHide: function() { $('search-results-content').hide(); }});
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
			ker.showError('Cannot retrieve groups', xmlRes);
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

