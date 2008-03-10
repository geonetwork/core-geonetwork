//=====================================================================================
//===
//=== InfoService
//===
//=====================================================================================

function InfoService(xmlLoader, type, callBack, forwardUrl, username, password)
{
	var loader   = xmlLoader;
	var callBackF= callBack;	

	if (forwardUrl == null)
	{
		var request = ker.createRequest('type', type);
	
		ker.send('xml.info', request, ker.wrap(this, retrieve_OK));
	}
	else
	{
		var accountTemp =
			'<account>'+
			'   <username>{USER}</username>'+
			'   <password>{PASS}</password>'+
			'</account>';
		
		var account = '';
		
		if (username != null)
			account = str.substitute(accountTemp, { USER : username, PASS : password});
			
		var forwardTemp =
			'<request>'+
			'   <site>'+
			'      <url>{URL}</url>'+
			'      <type>geonetwork</type>'+
					 account +
			'   </site>'+
			'   <params>'+
			'      <request>'+
			'         <type>{TYPE}</type>'+
			'      </request>'+ 
			'   </params>'+
			'</request>';

		var request = str.substitute(forwardTemp, { URL : forwardUrl, TYPE : type });

		ker.send('xml.forward', request, ker.wrap(this, retrieve_OK));
	}
        
//=====================================================================================

function retrieve_OK(xmlRes)
{
	if (xmlRes.nodeName == 'error')
		ker.showError(loader.getText('cannotRetrieve'), xmlRes);
	else
	{
		var data = [];
		var list = xml.children(xml.children(xmlRes)[0]);
		
		for (var i=0; i<list.length; i++)
			data.push(xml.toObject(list[i]));				
		
		callBackF(data);
	}
}

//=====================================================================================
}
