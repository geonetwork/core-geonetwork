function runIM_addService(url,service,type)
{imc_addService(url,service,type,false,function(req)
{im_buildLayerList(req);if(im_extra_afterLayerUpdated)
im_extra_afterLayerUpdated();});}
function runIM_selectService(url,type,mdid)
{gn_showGetCapabilities(url,type,mdid);}
function gn_showInterList(id)
{var pars='id='+id+"&currTab=distribution";$('gn_showinterlist_'+id).hide();$('gn_loadinterlist_'+id).show();var myAjax=new Ajax.Request(getGNServiceURL('metadata.show.embedded'),{method:'get',parameters:pars,onSuccess:function(req){var parent=$('ilwhiteboard_'+id);clearNode(parent);parent.show();$('gn_loadinterlist_'+id).hide();$('gn_hideinterlist_'+id).show();var div=document.createElement('div');div.className='metadata_current';div.style.width='100%';$(div).hide();parent.appendChild(div);div.innerHTML=req.responseText;Effect.BlindDown(div);var tipman=new TooltipManager();ker.loadMan.wait(tipman);},onFailure:gn_search_error});}
function gn_hideInterList(id)
{var parent=$('ilwhiteboard_'+id);var div=parent.firstChild;Effect.BlindUp(div,{afterFinish:function(obj){clearNode(parent);$('gn_showinterlist_'+id).show();$('gn_hideinterlist_'+id).hide();}});}
function gn_showGetCapabilities(url,type,id)
{if($('gn_showinterlist_'+id))$('gn_showinterlist_'+id).show();if($('gn_loadinterlist_'+id))$('gn_loadinterlist_'+id).hide();if($('gn_hideinterlist_'+id))$('gn_hideinterlist_'+id).hide();var parent=$('ilwhiteboard_'+id);clearNode(parent);parent.show();var div=document.createElement('div');div.className='metadata_current';parent.appendChild(div);var t1=document.createElement("p");t1.innerHTML=translate("waitGetCap");div.appendChild(t1);imc_loadURLServices(url,'0',-2,function(req){gn_getCapabLoaded(req,id);},"gn_layersRequested("+id+");");}
function gn_getCapabLoaded(req,mdid)
{var parent=$('ilwhiteboard_'+mdid);clearNode(parent);gn_addCloser(parent);var div=document.createElement('div');div.className='metadata_current';parent.appendChild(div);div.innerHTML=req.responseText;}
function gn_layersRequested(mdid)
{var im=$('ilwhiteboard_'+mdid);var url=$('im_addlayer_serverurl').value;var type=$('im_addlayer_type').value;var services=new Array();var lilist=im.getElementsByTagName("input");$A(lilist).each(function(input)
{var value=input.value;var checked=input.checked;if(checked)
{services.push(value);}});imc_addServices(url,services,type,function(req){gn_layersAdded(req,mdid);});}
function gn_layersAdded(req,mdid)
{var parent=$('ilwhiteboard_'+mdid);clearNode(parent);gn_addCloser(parent);var div=document.createElement('div');div.className='metadata_current';parent.appendChild(div);var t1=document.createElement("p");t1.innerHTML=translate('layersAdded');div.appendChild(t1);im_buildLayerList(req);if(im_extra_afterLayerUpdated)
im_extra_afterLayerUpdated();}
function gn_addCloser(domnode,callback)
{var closer=document.createElement('div');closer.className='im_wbcloser';var img=document.createElement('img');img.title=translate("close");img.src="/intermap/images/close.png";closer.appendChild(img);domnode.appendChild(closer);Event.observe(closer,'click',function()
{clearNode(domnode);if(callback)
callback();});}
function initSimpleSearch(wmc)
{im_mm_init(wmc,function()
{$('openIMBtn').style.cursor='pointer';Event.observe('openIMBtn','click',function(){openIntermap()});});}
function gn_anyKeyObserver(e)
{if(e.keyCode==Event.KEY_RETURN)
runSimpleSearch();}
function runPdfSearch(onSelection){if(onSelection){location.replace(getGNServiceURL('pdf.selection.search'));metadataselect(0,'remove-all');}else{if(document.cookie.indexOf("search=advanced")!=-1)
runAdvancedSearch("pdf");else
runSimpleSearch("pdf");}}
function runSimpleSearch(type)
{if(type!="pdf")
preparePresent();setSort();var pars="any="+encodeURIComponent($('any').value);var region=$('region').value;if(region!="")
{pars+="&"+im_mm_getURLselectedbbox();pars+=fetchParam('relation');pars+="&attrset=geo";if(region!="userdefined")
{pars+=fetchParam('region');}}
pars+=fetchParam('sortBy');pars+=fetchParam('sortOrder');pars+=fetchParam('hitsPerPage');pars+=fetchParam('output');if(type=="pdf")
gn_searchpdf(pars);else
gn_search(pars);}
function resetSimpleSearch()
{setParam('any','');setParam('relation','overlaps');setParam('region',null);$('northBL').value='90';$('southBL').value='-90';$('eastBL').value='180';$('westBL').value='-180';im_mm_redrawAoI();im_mm_zoomToAoI();setParam('sortBy','relevance');setParam('sortOrder','');setParam('hitsPerPage','10');setParam('output','full');}
function showAdvancedSearch()
{closeIntermap();closeSearch('simplesearch');document.cookie="search=advanced";var myAjax=new Ajax.Updater('advancedsearch',getGNServiceURL('main.searchform.advanced.embedded'),{method:'get',onComplete:function()
{openSearch('advancedsearch');initAdvancedSearch();},onFailure:im_load_error});}
function showSimpleSearch()
{closeSearch('advancedsearch');document.cookie="search=default";var myAjax=new Ajax.Updater('simplesearch',getGNServiceURL('main.searchform.simple.embedded'),{method:'get',onComplete:function()
{openSearch('simplesearch');initSimpleSearch();},onFailure:im_load_error});}
function openSearch(s)
{if(!Prototype.Browser.IE)
{Effect.BlindDown(s);}
else
{$(s).show();}}
function closeSearch(s)
{clearNode('im_mm_map');if(!Prototype.Browser.IE)
{Effect.BlindUp($(s),{afterFinish:function(){clearNode($(s));}});}
else
{$(s).hide();clearNode($(s))}}
function initAdvancedSearch()
{im_mm_init();new Ajax.Autocompleter('themekey','keywordList','portal.search.keywords?',{paramName:'keyword',updateElement:addQuote});Calendar.setup({inputField:"dateFrom",ifFormat:"%Y-%m-%dT%H:%M:00",button:"from_trigger_c",showsTime:true,align:"Tl",singleClick:true});Calendar.setup({inputField:"dateTo",ifFormat:"%Y-%m-%dT%H:%M:00",button:"to_trigger_c",showsTime:true,align:"Tl",singleClick:true});Calendar.setup({inputField:"extFrom",ifFormat:"%Y-%m-%dT%H:%M:00",button:"extfrom_trigger_c",showsTime:true,align:"Tl",singleClick:true});Calendar.setup({inputField:"extTo",ifFormat:"%Y-%m-%dT%H:%M:00",button:"extto_trigger_c",showsTime:true,align:"Tl",singleClick:true});}
function runAdvancedSearch(type)
{if(type!="pdf")
preparePresent();setSort();var pars="any="+encodeURIComponent($('any').value);pars+=fetchParam('phrase');pars+=fetchParam('or');pars+=fetchParam('without');pars+=fetchParam('title');pars+=fetchParam('abstract');pars+=fetchParam('themekey');pars+=fetchRadioParam('similarity');var region=$('region').value;if(region!="")
{pars+="&attrset=geo";pars+="&"+im_mm_getURLselectedbbox();pars+=fetchParam('relation');if(region!="userdefined")
{pars+=fetchParam('region');}}
if($('radfrom1').checked)
{pars+=fetchParam('dateFrom');pars+=fetchParam('dateTo');}
if($('radfromext1').checked)
{pars+=fetchParam('extFrom');pars+=fetchParam('extTo');}
pars+=fetchParam('group');pars+=fetchParam('category');pars+=fetchParam('siteId');pars+=fetchBoolParam('digital');pars+=fetchBoolParam('paper');pars+=fetchBoolParam('dynamic');pars+=fetchBoolParam('download');pars+=fetchParam('protocol').toLowerCase();pars+=fetchParam('template');pars+=fetchParam('sortBy');pars+=fetchParam('sortOrder');pars+=fetchParam('hitsPerPage');pars+=fetchParam('output');if(type=="pdf")
gn_searchpdf(pars);else
gn_search(pars);}
function resetAdvancedSearch()
{setParam('any','');setParam('phrase','');setParam('or','');setParam('without','');setParam('title','');setParam('abstract','');setParam('themekey','');var radioSimil=document.getElementsByName('similarity');radioSimil[1].checked=true;setParam('relation','overlaps');setParam('region',null);$('northBL').value='90';$('southBL').value='-90';$('eastBL').value='180';$('westBL').value='-180';im_mm_redrawAoI();im_mm_zoomToAoI();setParam('dateFrom','');setParam('dateTo','');$('radfrom0').checked=true;$('radfrom1').disabled='disabled';setParam('extFrom','');setParam('extTo','');$('radfromext1').disabled='disabled';setParam('group','');setParam('category','');setParam('siteId','');$('digital').checked=false;$('paper').checked=false;$('dynamic').checked=false;$('download').checked=false;setParam('protocol','');setParam('template','n');setParam('sortBy','relevance');setParam('sortOrder','');setParam('hitsPerPage','10');setParam('output','full');}
function showFields(img,div)
{var img=$(img);var src=img.getAttribute('src');var ndx=src.lastIndexOf('/');var div=$(div);src=src.substring(0,ndx+1);if(div.visible())img.setAttribute('src',src+'plus.gif');else img.setAttribute('src',src+'minus.png');div.toggle();}
function setSort()
{if($('sortBy').value=='title')
$('sortOrder').value='reverse';else
$('sortOrder').value='';}
function setSortAndSearch()
{$('sortBy').value=$F('sortBy.live');setSort();if(document.cookie.indexOf("search=advanced")!=-1)runAdvancedSearch();else runSimpleSearch();}
var ratingPopup=null;function showRatingPopup(id)
{if(ratingPopup==null)
{ker.loadURL('rating.popup',ker.wrap(this,function(t)
{var p=document.createElement('div');p.className='ratingBox';p.innerHTML=t.responseText;p.style.display='none';p.style.zIndex=32000;p.setAttribute('id','rating.popup');document.body.appendChild(p);ratingPopup=p;setTimeout(ker.wrap(this,function(){showRatingPopup(id);}),10);}));return;}
var pos=Position.cumulativeOffset($('rating.link.'+id));ratingPopup.style.left=pos[0]-100;ratingPopup.style.top=pos[1]+16;ratingPopup.setAttribute('mdid',id);Element.show(ratingPopup);}
function hideRatingPopup()
{var popup=$('rating.popup');if(popup!=null)
{Element.hide(popup);Element.hide('rating.image');}}
function rateMetadata(rating)
{var id=ratingPopup.getAttribute('mdid');Element.show('rating.image');var request='<request>'+'   <id>'+id+'</id>'+'   <rating>'+rating+'</rating>'+'</request>';ker.send('xml.metadata.rate',request,ker.wrap(this,rateMetadata_OK));}
function rateMetadata_OK(xmlRes)
{if(xmlRes.nodeName=='error')
ker.showError(translate('rateMetadataFailed'),xmlRes);else
hideRatingPopup();}
function doRegionSearch()
{var region=$('region').value;if(region=="")
{region=null;$('northBL').value='90';$('southBL').value='-90';$('eastBL').value='180';$('westBL').value='-180';im_mm_redrawAoI();im_mm_zoomToAoI();}else if(region=="userdefined"){}else
{getRegion(region);}}
function getRegion(region)
{if(region)
var pars="id="+region;var myAjax=new Ajax.Request(getGNServiceURL('xml.region.get'),{method:'get',parameters:pars,onSuccess:getRegion_complete,onFailure:getRegion_error});}
function getRegion_complete(req){var node=req.responseXML;var northcc=xml.evalXPath(node,'response/record/north');var southcc=xml.evalXPath(node,'response/record/south');var eastcc=xml.evalXPath(node,'response/record/east');var westcc=xml.evalXPath(node,'response/record/west');$('northBL').value=northcc;$('southBL').value=southcc;$('eastBL').value=eastcc;$('westBL').value=westcc;im_mm_redrawAoI();im_mm_zoomToAoI();}
function getRegion_error(){alert(translate("error"));}
function updateAoIFromForm(){var nU=Number($('northBL').value);var sU=Number($('southBL').value);var eU=Number($('eastBL').value);var wU=Number($('westBL').value);if(nU<sU){alert(translate("northSouth"));}
else if(nU>90){alert(translate("north90"));}
else if(sU<-90){alert(translate("south90"));}
else if(eU<wU){alert(translate("eastWest"));}
else if(eU>180){alert(translate("east180"));}
else if(wU<-180){alert(translate("west180"));}
else
{im_mm_redrawAoI();im_mm_zoomToAoI();$('updateBB').style.visibility="hidden";}}
function AoIrefresh(){$('region').value="userdefined";$('updateBB').style.visibility="visible";}
function im_mm_aoiUpdated(bUpdate){$('region').value="userdefined";}
function preparePresent()
{clearNode('resultList');$('loadingMD').show();}
function gn_search(pars)
{var myAjax=new Ajax.Request(getGNServiceURL('main.search.embedded'),{method:'get',parameters:pars,onSuccess:gn_search_complete,onFailure:gn_search_error});}
function gn_searchpdf(pars)
{pars=pars.replace(/hitsPerPage=\d{2,3}/,'hitsPerPage=9999');location.replace(getGNServiceURL('pdf.search')+"?"+pars);}
function gn_present(frompage,topage)
{preparePresent();var pars='from='+frompage+"&to="+topage;var myAjax=new Ajax.Request(getGNServiceURL('main.present.embedded'),{method:'get',parameters:pars,onSuccess:gn_search_complete,onFailure:gn_search_error});}
function gn_search_complete(req){var rlist=$('resultList');rlist.innerHTML=req.responseText;$('loadingMD').hide();}
function gn_showSingleMetadataUUID(uuid)
{var pars='uuid='+uuid+'&control&currTab=simple';gn_showSingleMet(pars);}
function gn_showSingleMetadata(id)
{var pars='id='+id+'&currTab=simple';gn_showSingleMet(pars);}
function gn_showSingleMet(pars)
{var myAjax=new Ajax.Request(getGNServiceURL('metadata.show.embedded'),{method:'get',parameters:pars,onSuccess:function(req){var parent=$('resultList');clearNode(parent);var div=document.createElement('div');div.className='metadata_current';div.style.display='none';div.style.width='100%';parent.appendChild(div);div.innerHTML=req.responseText;Effect.BlindDown(div);var tipman=new TooltipManager();ker.loadMan.wait(tipman);extentMap.initMapDiv();},onFailure:gn_search_error});}
function gn_showMetadata(id)
{var pars='id='+id+'&currTab=simple';$('gn_showmd_'+id).hide();$('gn_loadmd_'+id).show();var myAjax=new Ajax.Request(getGNServiceURL('metadata.show.embedded'),{method:'get',parameters:pars,onSuccess:function(req){var parent=$('mdwhiteboard_'+id);clearNode(parent);$('gn_loadmd_'+id).hide();$('gn_hidemd_'+id).show();var div=document.createElement('div');div.className='metadata_current';div.style.display='none';div.style.width='100%';parent.appendChild(div);div.innerHTML=req.responseText;Effect.BlindDown(div);var tipman=new TooltipManager();ker.loadMan.wait(tipman);extentMap.initMapDiv();},onFailure:gn_search_error});}
function gn_hideMetadata(id)
{var parent=$('mdwhiteboard_'+id);var div=parent.firstChild;Effect.BlindUp(div,{afterFinish:function(obj){clearNode(parent);$('gn_showmd_'+id).show();$('gn_hidemd_'+id).hide();}});}
function a(msg){alert(msg);}
function gn_search_error(){$('loadingMD').hide();alert("ERROR)");}
function gn_filteredSearch(){var myAjax=new Ajax.Request(getGNServiceURL('selection.search'),{method:'get',parameters:'',onSuccess:gn_search_complete,onFailure:gn_search_error});}
function runCategorySearch(category)
{preparePresent();var pars="category="+category;gn_search(pars);}
function fetchParam(p)
{var pL=$(p);if(!pL)
return"";else{var t=pL.value;if(t)
return"&"+p+"="+encodeURIComponent(t);else
return"";}}
function fetchBoolParam(p)
{var pL=$(p);if(!pL)
return"";else{if(pL.checked)
return"&"+p+"=on";else
return"&"+p+"=off";}}
function fetchRadioParam(name)
{var radio=document.getElementsByName(name);var value=getCheckedValue(radio);return"&"+name+"="+value;}
function getCheckedValue(radioObj){if(!radioObj)
return"";var radioLength=radioObj.length;if(radioLength==undefined)
if(radioObj.checked)
return radioObj.value;else
return"";for(var i=0;i<radioLength;i++){if(radioObj[i].checked){return radioObj[i].value;}}
return"";}
function setParam(p,val)
{var pL=$(p);if(pL)pL.value=val;}
var keyordsSelected=false;function addQuote(li){$("themekey").value='"'+li.innerHTML+'"';}
function popKeyword(el,pop){if(pop.style.display=="block"){pop.style.display="none";return false;}
pop.style.top=el.cumulativeOffset().top+el.getHeight();pop.style.left=el.cumulativeOffset().left;pop.style.width='250px';pop.style.display="block";if(!keyordsSelected){new Ajax.Updater("keywordSelector","portal.search.keywords?mode=selector&keyword="+$("themekey").value);keyordsSelected=true;}}
function keywordCheck(k,check){k='"'+k+'"';if(check){if($("themekey").value!='')
$("themekey").value+=' or '+k;else
$("themekey").value=k;}else{$("themekey").value=$("themekey").value.replace(' or '+k,'');$("themekey").value=$("themekey").value.replace(k,'');pos=$("themekey").value.indexOf(" or ");if(pos==0){$("themekey").value=$("themekey").value.substring(4,$("themekey").value.length);}}}
function setDates(what)
{var xfrom=$('dateFrom');var xto=$('dateTo');var extfrom=$('extFrom');var extto=$('extTo');if(what==0)
{xfrom.value="";xto.value="";extfrom.value="";extto.value="";return;}
today=new Date();fday=today.getDate();if(fday.toString().length==1)
fday="0"+fday.toString();fmonth=today.getMonth()+1;if(fmonth.toString().length==1)
fmonth="0"+fmonth.toString();fyear=today.getYear();if(fyear<1900)
fyear=fyear+1900;var todate=fyear+"-"+fmonth+"-"+fday+"T23:59:59";var fromdate=(fyear-10)+"-"+fmonth+"-"+fday+"T00:00:00";xto.value=todate;xfrom.value=fromdate;extto.value=todate;extfrom.value=fromdate;}
function check(status){var checks=$('search-results-content').getElementsByTagName('INPUT');var checksLength=checks.length;for(var i=0;i<checksLength;i++){checks[i].checked=status;}}
function metadataselect(id,selected){if(selected===true)
selected='add';else if(selected===false)
selected='remove';var param='id='+id+'&selected='+selected;var http=new Ajax.Request(Env.locService+'/'+'metadata.select',{method:'get',parameters:param,onComplete:function(originalRequest){},onLoaded:function(originalRequest){},onSuccess:function(originalRequest){var xmlString=originalRequest.responseText;var xmlobject=(new DOMParser()).parseFromString(xmlString,"text/xml");var root=xmlobject.getElementsByTagName('response')[0];var nbSelected=root.getElementsByTagName('Selected')[0].firstChild.nodeValue;var item=document.getElementById('nbselected');item.innerHTML=nbSelected;},onFailure:function(originalRequest){alert(translate('metadataSelectionError'));}});if(selected=='remove-all'){check(false);};if(selected=='add-all'){check(true);};}