//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

var ksearching = new KSearching();
var mode = '';

//=====================================================================================
//===
//=== KSearching class
//===
//=====================================================================================

function KSearching() {}

KSearching.prototype.search = function(form)
{	
	var req = '<request>\n';
	// Get keyword
	var word = $F('pKeyword');
	if (word==''){
		word='*';
	};
	req += '<pKeyword>'+ word +'</pKeyword>\n';		

	
	// Get thesauri
	if($F('thesaSelected')=='true'){
		req += '<pThesauri>'+ $F('pThesauri') +'</pThesauri>\n';
	}else{
		var thesauriNodeList = $('pThesauri').getElementsByTagName('option');
		var nodes = $A(thesauriNodeList);
			nodes.each(function(node){				
					if(node.selected){
			    	req += '<pThesauri>'+ node.value +'</pThesauri>\n';
					}
				});
	}			
					

	//Search type
	var radiovalue;
	Form.getInputs(form,'radio').each(function(input) { 
		if(input.name=="pNameTypeSearch" && input.checked) 
			{
				radiovalue=input.value;	
				}; 
			});

	req += '<pTypeSearch>'+ radiovalue +'</pTypeSearch>\n';
	
	// Number of result / page
	req += '<maxResults>'+ $F('maxResults') +'</maxResults>\n';
	req += '<pNewSearch>true</pNewSearch>\n';

	if(mode!=''){	
		req += '<pMode>'+mode+'</pMode>\n';
	}
	req += '</request>';

	ker.send('keywords.search', req, ker.wrap(this, this.searchSuccess), false);
};

KSearching.prototype.getPage = function(from, to)
{	
	var req = '<request>\n';
	req += '<pFrom>'+ from +'</pFrom>\n';
	req += '<pTo>'+ to +'</pTo>\n';
	req += '<pNewSearch>false</pNewSearch>\n';
	if(mode){	
		req += '<pMode>'+mode+'</pMode>\n';
	}	
	req += '</request>';

	// Send query
	ker.send('keywords.search', req, ker.wrap(this, this.searchSuccess), false);
};

KSearching.prototype.sort = function(tri)
{	
	var req = '<request>\n';
	req += '<pSort>'+ tri +'</pSort>\n';
	req += '<pNewSearch>false</pNewSearch>\n';
	if(mode){	
		req += '<pMode>'+mode+'</pMode>\n';
	}	
	req += '</request>';

	// Send query
	ker.send('keywords.sort', req, ker.wrap(this, this.searchSuccess), false);
};


KSearching.prototype.select = function(id)
{	
	var thesauriNodeList = $('pThesauri').getElementsByTagName('option');
	var nodes = $A(thesauriNodeList);
	
	var boxes = $('keywordResults').getElementsByTagName('input');
	var checked = 0;
  
	for (i=0; i<boxes.length; i++)
		if (boxes[i].checked) 
			checked += 1;
    

	if (checked == 0)
			$('del').disabled = true;
	else
			$('del').disabled = false;

	var req = '<request>\n';
	req += '<pIdKeyword>'+id+'</pIdKeyword>';
	req += '<pNewSearch>false</pNewSearch>\n';
	req += '</request>';

	ker.send('keywords.select', req, function(){}, false);
};


//-------------------------------------------------------------------------------------

KSearching.prototype.searchSuccess = function(xml)
{
	var html = xml;

	/* Parse labels that are not retreived by response */
	html = html.replace(new RegExp("foundWords", "g"), foundWords);
	html = html.replace(new RegExp("pages", "g"), pages);
	html = html.replace(new RegExp("selection", "g"), selection);
	html = html.replace(new RegExp("sort", "g"), sort);
	html = html.replace(new RegExp("label", "g"), label);
	html = html.replace(new RegExp("definition", "g"), definition);
	// FIXME : That could change some useful info in keywords name or definition ... !

	$('divResults').innerHTML = html;
	$('divResults').style.display = 'inline';
};

KSearching.prototype.deleteKeyword = function(form){
	var req = '<request>\n';
	req += '<pThesaurus>'+ $F('pThesauri') +'</pThesaurus>\n';	
	req += '</request>';

	var opt = 
	{
		method: 'post',
		postBody: req,
		requestHeaders: ['Content-type', 'application/xml'],

		onSuccess: function(t) 
		{
			try
			{
				ksearching.search(form);
			}
			catch(err)
			{ 
				alert(err);
			}			
		},
		on404: function(t) 
		{
			alert('Error 404: location "' + t.statusText + '" was not found.');
		},
		onFailure: function(t) 
		{
			alert('Error ' + t.status + ' -- ' + t.statusText);
		}
	}

	new Ajax.Request(locService +'/'+ 'thesaurus.deleteelement', opt);

};
