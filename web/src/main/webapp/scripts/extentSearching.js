//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

var esearching = new ESearching();
var mode = '';

//=====================================================================================
//===
//=== ESearching class
//===
//=====================================================================================

function ESearching() {}

ESearching.prototype.search = function(form,page,geom)
{
	var req = '<request>\n';
	// Get keyword
	var word = $F('pattern');
	if (word==''){
		word='*';
	};
	word = word.replace(/<.*>/,"");
	
    req += '<pattern>'+ word +'</pattern>\n';
    req += '<property>desc</property>\n';


	//Search type
    req += '<mode>edit</mode>\n';

	// Number of result / page
	req += '<numResults>'+ $F('numResults') +'</numResults>\n';
    req += '<clearSelection>true</clearSelection>\n';
    req += '<page>'+page+'</page>\n';
    req += '<typename>gn:xlinks,gn:non_validated</typename>\n';

    if (geom) {
        req += '<geom>'+geom+'</geom>\n';
    }

	req += '</request>';

	ker.send('extent.search.paging', req, ker.wrap(this, this.searchSuccess), false);
};

ESearching.prototype.searchSuccess = function(xml)
{
    var html = xml;

    $('divResults').innerHTML = html;
    $('divResults').style.display = 'inline';
};

ESearching.prototype.selectAll = function(selected,wfs, typename)
{
    var req = '<request>\n';

    var boxes = $('extentResults').getElementsByTagName('input');
    for (i=0; i<boxes.length; i++){
        boxes[i].checked=selected;
        if(selected){
            req += '<add wfs="'+wfs+'" typename="'+typename+'" id="'+boxes[i].id.substring(3)+'"/>\n';
        }else {
            req += '<remove wfs="'+wfs+'" typename="'+typename+'" id="'+boxes[i].id.substring(3)+'"/>\n';
        }
    }

    $('del').disabled = !selected;
    $('deselectAll').disabled = !selected;
    $('selectAll').disabled = selected;

    req += '</request>';

    ker.send('extent.select', req, function(){}, false);
};


ESearching.prototype.select = function(wfs, typename, id)
{
    var boxes = $('extentResults').getElementsByTagName('input');
    var checked = $('chk'+id).checked

    var checkedCount = 0;
    var uncheckedCount = 0;
    for (i=0; i<boxes.length; i++){
        if (boxes[i].checked){
            checkedCount += 1;
        }else{
            uncheckedCount +=1;
        }
    }

    $('del').disabled = checkedCount == 0;
    $('deselectAll').disabled = checkedCount == 0;
    $('selectAll').disabled = uncheckedCount == 0;

    var req = '<request>\n';

    if(checked){
        req += '<add wfs="'+wfs+'" typename="'+typename+'" id="'+id+'"/>\n';
    }else {
        req += '<remove wfs="'+wfs+'" typename="'+typename+'" id="'+id+'"/>\n';
    }

    req += '</request>';

    ker.send('extent.select', req, function(){}, false);
};

ESearching.prototype.deleteExtent = function(form){
    var req = '<request>\n';
    req += '<extent.selection>true</extent.selection>\n';
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
                esearching.search(form,1);
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

    new Ajax.Request(Env.locService +'/'+ 'xml.extent.delete', opt);
};



ESearching.prototype.getPage = function(from, to)
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

ESearching.prototype.sort = function(tri)
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
