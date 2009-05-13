// Functions called from the metadata viewer

			var getGNServiceURL = function(service) {
			  return Env.locService+"/"+service;
			};

// for processing categories and privileges buttons

			function setAll(id)
			{
				var list = $(id).getElementsByTagName('input');
			
				for (var i=0; i < list.length; i++)
					list[i].checked = true;
			}

			function clearAll(id)
			{
				var list = $(id).getElementsByTagName('input');
			
				for (var i=0; i < list.length; i++)
					list[i].checked = false;
			}

			function checkBoxModalUpdate(div,service,modalbox,title)
      {
        var boxes = $(div).getElementsBySelector('input[type="checkbox"]');
				var pars = "&id="+$('metadataid').value;
				boxes.each( function(s) {
					if (s.checked) {
						pars += "&"+s.name+"=on";
					}
				});

				if (modalbox != null && modalbox) {
					service = getGNServiceURL(service) + '?' + pars;
					Modalbox.show(service,{title: title, width: 600} );
				} else {
					var myAjax = new Ajax.Request(
						getGNServiceURL(service),
						{
							method: 'get',
							parameters: pars,
							onSuccess: function() {},
							onFailure: function(req) {
								alert("ERROR: "+service+" failed: status "+req.status+" text: "+req.statusText+" - Try again later?");
							}
						}
					);
					window.Modalbox.hide();
				}
      }

// check create metadata button 

			function checkCreate(service,id) {
				descs = $('groups').getValue();
				if (descs.length == 0) {
					alert("Please select at least one group!");
					return false;
				}
				return true;
			}

// processing delete button

			function doConfirmDelete(url, message, title, id, boxTitle)
			{
				if(confirm(message + " (" + title + ")"))
				{
					var divToHide;
					if (opener) divToHide = opener.$(id);
					else divToHide = $(id);
					if (divToHide) {
						divToHide.hide();
					}
					Modalbox.show(url,{title: boxTitle, width: 600, afterHide: function() { location.replace(getGNServiceURL('main.home')); }});
					return true;
				}
				return false;
			}
		
			function doOtherButton(url, title, width)
			{
				Modalbox.show(url,{title: title, width: width, height: 400});
				return true;
			}
			
			function doAction(action)
			{
				document.mainForm.action = action;
				goSubmit('mainForm');
			}
			
			function doTabAction(action, tab)
			{
				document.mainForm.currTab.value = tab;
				doAction(action);
			}

// stub to stop errors in metadata-show.xsl - real function is in 
// metadata-editor.js
			function setBunload(on) 
			{
			}

			function runFileDownload(href,title) {
				if (href.include("resources.get")) { // do the file download direct
					location.replace(getGNServiceURL(href));
				} else { // show some dialog beforehand eg. constraints
					Modalbox.show(getGNServiceURL(href),{title:title, height:400, width:600});
				}
			}

			function runFileDownloadSummary(uuid, title) {
				pars = "&uuid="+uuid;
				var myAjax = new Ajax.Request(
					getGNServiceURL('prepare.file.download'),
					{
						method: 'get',
						parameters: pars,
						onSuccess: function(req) {
							Modalbox.show(req.responseText ,{title: title, height:400, width: 600} );
						},
						onFailure: function(req) {
							alert("ERROR: "+getGNServiceURL('prepare.file.download')+" failed: status "+req.status+" text: "+req.statusText+" - Try again later?");
						}
					}
				);
			}

