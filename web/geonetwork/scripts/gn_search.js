/********************************************************************
* gn_search.js
*
* This file contains functions related to the dynamic behavior of geonetwork:
* - metadata search
* - metadata present
*
********************************************************************/


/*  */
function prepareSearch() 
{
    // Display results area
    clearNode('resultList');
    $('loadingMD').show();
}

/*  */
function doMetadataSearch() 
{
    prepareSearch();
    
    // Load results via AJAX
    gn_search($('any') .value);
    // FIXME add bb
}

function gn_search(text, bbn, bbe, bbs, bbw) 
{
    var pars = 'any=' + text;
// add bb
    
    var myAjax = new Ajax.Request(
    '/geonetwork/srv/en/main.search.embedded', {
        method: 'get',
        parameters: pars,
        onSuccess: gn_search_complete,
        onFailure: gn_search_error
    });
}

function gn_present(frompage, topage) 
{
    prepareSearch();
    
    var pars = 'from=' + frompage + "&to=" + topage;
    
    var myAjax = new Ajax.Request(
        '/geonetwork/srv/en/main.present.embedded', 
        {
            method: 'get',
            parameters: pars,
            onSuccess: gn_search_complete,
            onFailure: gn_search_error
        });
}


function gn_search_complete(req) {
    // remove all previous children
    //clearResultList();
    
    var rlist = $('resultList');
    
    rlist.innerHTML = req.responseText;
    
    $('loadingMD').hide();
}

function gn_toggleMetadata(id) 
{
    var parent = $('mdwhiteboard_' + id);
    if (parent.firstChild)
    gn_hideMetadata(id);
    else
    gn_showMetadata(id);
}

function gn_showMetadata(id) 
{
    var pars = 'id=' + id;
    
    $('gn_showmd_' + id) .hide();
    $('gn_loadmd_' + id) .show();
    
    var myAjax = new Ajax.Request(
        '/geonetwork/srv/en/metadata.show.embedded', 
        {
            method: 'get',
            parameters: pars,
            onSuccess: function (req) {
                // remove previous open md
                //var prev = document.getElementById('metadata_current');
                //if(prev)
                //	prev.parentNode.removeChild($('metadata_current'));
                
                var parent = $('mdwhiteboard_' + id);
                clearNode(parent);
                
                $('gn_loadmd_' + id) .hide();
                $('gn_hidemd_' + id) .show();
                
                // create new element
                var div = document.createElement('div');
                div.id = 'metadata_current';
                div.style.display = 'none';
                parent.appendChild(div);
                
                div.innerHTML = req.responseText;
                
                Effect.BlindDown(div);
                
                var tipman = new TooltipManager();
                ker.loadMan.wait(tipman);
            },
            onFailure: gn_search_error// FIXME
        });
}

function gn_hideMetadata(id) 
{
    var parent = $('mdwhiteboard_' + id);
    var div = parent.firstChild;
    Effect.BlindUp(div, { afterFinish: function (obj) {
            clearNode(parent);
            $('gn_showmd_' + id) .show();
            $('gn_hidemd_' + id) .hide();
        }
    });
}

function a(msg) {
    alert(msg);
}



function gn_search_error() {
    $('loadingMD') .hide();
// style.display = 'none';
    alert("ERROR)");
}
