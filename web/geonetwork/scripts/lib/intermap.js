var im_extra_drivingMap;var im_extra_afterWmcSet=function(){alert("im_extra_afterWmcSet in file im_extras.js is not set");};var im_extra_afterLayerUpdated=function(){alert("im_extra_afterLayerUpdated in file im_extras.js is not set");};function im_addLayer()
{clearNode('im_whiteboard');var WB=$('im_whiteboard');var wbtitle=im_createWBTitle(i18n('addLayer'));WB.appendChild(wbtitle);var closer=im_getWBCloser();WB.appendChild(closer);Event.observe(closer,'click',im_closeWhiteBoard);var div=document.createElement('div');div.id="im_serverList";div.className='im_wbcontent';WB.appendChild(div);var myAjax=new Ajax.Updater('im_serverList',getIMServiceURL('mapServers.listServers.embedded'),{method:'get',onFailure:im_load_error});}
function im_mapServerSelected(id,name,refreshCache)
{var im=$('im_serverList');clearNode(im);var t1=document.createElement("p");t1.innerHTML=i18n("wait");im.appendChild(t1);var t2=document.createElement("p");t2.innerHTML=i18n("loadingFrom")+" "+name;im.appendChild(t2);imc_loadServerServices(id,refreshCache,im_servicesLoaded,"im_servicesSelected();");}
function imc_loadServerServices(id,refreshCache,callback,jscallback)
{var pars='mapserver='+id
+"&jscallback="+encodeURIComponent(jscallback)
+"&refreshCache="+refreshCache;var myAjax=new Ajax.Request(getIMServiceURL('mapServers.getServices.embedded'),{method:'get',parameters:pars,onSuccess:callback,onFailure:im_load_error});}
function im_mapServerURL(url,refreshCache)
{var im=$('im_serverList');clearNode(im);var t1=document.createElement("p");t1.innerHTML=i18n("wait");im.appendChild(t1);var t2=document.createElement("p");t2.innerHTML=i18n("loadingFromWMS");im.appendChild(t2);imc_loadURLServices(url,refreshCache,-2,im_servicesLoaded,"im_servicesSelected();");}
function imc_loadURLServices(url,refreshCache,type,callback,jscallback)
{var pars='mapserver='+type
+"&url="+encodeURIComponent(url)
+"&jscallback="+encodeURIComponent(jscallback)
+"&refreshCache="+refreshCache;var myAjax=new Ajax.Request(getIMServiceURL('mapServers.getServices.embedded'),{method:'get',parameters:pars,onSuccess:callback,onFailure:im_load_error});}
function im_servicesLoaded(req)
{var im=$('im_serverList');im.innerHTML=req.responseText;var reqScripts=req.responseText.extractScripts();if(reqScripts.length>0)
eval(reqScripts[0]);}
function im_servicesSelected()
{var im=$('im_serverList');var url=$('im_addlayer_serverurl').value;var type=$('im_addlayer_type').value;var services=new Array();var lilist=im.getElementsByTagName("input");$A(lilist).each(function(input)
{var value=input.value;var checked=input.checked;if(checked)
{services.push(value);}});im_extra_drivingMap.setStatus('busy');imc_addServices(url,services,type,im_servicesAdded);}
function im_servicesAdded(req)
{var im=$('im_serverList');clearNode(im);var t1=document.createElement("p");t1.innerHTML=i18n("layersAdded");im.appendChild(t1);im_buildLayerList(req);im_extra_afterLayerUpdated();}
function im_openPDFform()
{clearNode('im_whiteboard');var WB=$('im_whiteboard');var wbtitle=im_createWBTitle(i18n('exportAsPDF'));WB.appendChild(wbtitle);var closer=im_getWBCloser();WB.appendChild(closer);Event.observe(closer,'click',im_closeWhiteBoard);var div=document.createElement('div');div.id="im_createPDF";div.className='im_wbcontent';WB.appendChild(div);var myAjax=new Ajax.Updater('im_createPDF',getIMServiceURL('static.form.pdf'),{method:'get',onFailure:im_load_error});}
function im_requestPDF()
{var orient=$('pdf_orientation').value;var psize=$('pdf_pagesize').value;var ptitle=$('pdf_title').value;var pcopy=$('pdf_copyright').value;var bllist=$('pdf_layerlist').checked;var bdetails=$('pdf_details').checked;var bbbox=$('pdf_boundingbox').checked;var bscale=$('pdf_scalebar').checked;var barrow=$('pdf_arrow').checked;var pars="orientation="+orient+"&pagesize="+psize+"&"+im_extra_drivingMap.getURLbbox();if(ptitle)
pars+="&title="+encodeURIComponent(ptitle);if(pcopy)
pars+="&copyright="+encodeURIComponent(pcopy);if(bllist)
pars+="&layerlist=on";if(bdetails)
pars+="&details=on";if(bbbox)
pars+="&boundingbox=on";if(bscale)
pars+="&scalebar=on";if(barrow)
pars+="&arrow=on";$('im_requestingpdf').show();$('im_requestpdf').hide();$('im_builtpdf').hide();var myAjax=new Ajax.Request(getIMServiceURL('create.pdf'),{method:'get',parameters:pars,onSuccess:im_openPDF,onFailure:im_load_error});}
function im_openPDF(req)
{var url=req.responseXML.documentElement.getElementsByTagName('url')[0].firstChild.nodeValue;window.open(url);$('im_requestpdf').show();$('im_requestingpdf').hide();$('im_builtpdf').show();}
function im_openWMCform(type)
{clearNode('im_whiteboard');var WB=$('im_whiteboard');var i18nkey="wmctitle";if(type)
i18nkey+=type;var wbtitle=im_createWBTitle(i18n(i18nkey));WB.appendChild(wbtitle);var closer=im_getWBCloser();WB.appendChild(closer);Event.observe(closer,'click',im_closeWhiteBoard);var div=document.createElement('div');div.id="im_wmcmenu";div.className='im_wbcontent';WB.appendChild(div);var pars="&width="+im_extra_drivingMap.width+"&height="+im_extra_drivingMap.height;if(type)
pars+="&type="+type;var myAjax=new Ajax.Updater('im_wmcmenu',getIMServiceURL('wmc.form'),{method:'get',parameters:pars,onFailure:im_load_error});}
function im_downloadWMC()
{var pars="width="+im_extra_drivingMap.width+"&height="+im_extra_drivingMap.height;window.open(getIMServiceURL('context.cml')+'?'+pars);}
function im_uploadWMC(bClearLayers)
{var form=$('im_fuploadwmc');form.action=getIMServiceURL('wmc.uploadContext');$('im_fup_clearLayers').value=bClearLayers?'true':'false';return AIM.submit(form,{'onStart':function()
{im_extra_drivingMap.setStatus('busy');im_wmc_showMessage("upload","start");return true;},'onComplete':function(domdoc)
{im_extra_drivingMap.setStatus('idle');var resp=domdoc.documentElement;if(resp.tagName=="error")
{var msg=resp.getElementsByTagName('message')[0].firstChild.nodeValue;im_wmc_showMessage("upload","error",msg);}
else
{im_wmc_showMessage("upload","ok");imc_reloadLayers();im_extra_afterWmcSet(resp);}}});}
function im_sendWMC()
{var title=$('wmc_title').value;var from=$('wmc_mailfrom').value;var to=$('wmc_mailto').value;if(!title||!from||!to)
{alert("Please fill in information");return;}
pars="wmc_title="+encodeURIComponent(title)
+"&wmc_mailfrom="+encodeURIComponent(from)
+"&wmc_mailto="+encodeURIComponent(to)
+"&width="+im_extra_drivingMap.width
+"&height="+im_extra_drivingMap.height;im_wmc_showMessage("mail","start");var myAjax=new Ajax.Request(getIMServiceURL('wmc.mailContext'),{method:'post',parameters:pars,onSuccess:function(req)
{var resp=req.responseXML.documentElement;if(resp.tagName=="error")
{var msg=resp.getElementsByTagName('message')[0].firstChild.nodeValue;im_wmc_showMessage("mail","error",msg);}
else
{im_wmc_showMessage("mail","ok");}},onFailure:function(req)
{im_wmc_showMessage("mail","error");}});}
function im_openWMC(req)
{var url=req.responseXML.documentElement.getElementsByTagName('url')[0].firstChild.nodeValue;window.open(url);$('im_requestpdf').show();$('im_requestingpdf').hide();$('im_builtpdf').show();}
function im_wmc_showMessage(task,status,more)
{im_showMessage('wmc',task,status,more);}
function im_showMessage(context,task,status,more)
{$('im_'+context+'_form').hide();$('im_'+context+'_msg_'+task+"_start").hide();$('im_'+context+'_msg_'+task+"_ok").hide();$('im_'+context+'_msg_'+task+"_error").hide();$('im_'+context+'_msg_'+task+"_"+status).show();if(more)
{var div=document.createElement('div');div.innerHTML=more;$('im_'+context+'_msg_'+task+"_"+status).appendChild(div);}}
function im_markerList(id)
{clearNode('im_whiteboard');var WB=$('im_whiteboard');var wbtitle=im_createWBTitle(i18n("markerlisttitle"));WB.appendChild(wbtitle);var closer=im_getWBCloser();WB.appendChild(closer);Event.observe(closer,'click',im_closeWhiteBoard);var div=document.createElement('div');div.id="im_markerlist";div.className='im_wbcontent';WB.appendChild(div);var par="";if(id)
{par="id="+id;}
var myAjax=new Ajax.Updater('im_markerlist',getIMServiceURL('marker.present'),{method:'get',parameters:par,onFailure:im_load_error});}
function im_selectMarkerFromList(id)
{var myAjax=new Ajax.Updater('im_markerlist',getIMServiceURL('marker.present'),{method:'get',parameters:'id='+id,onFailure:im_load_error});}
function im_updateMarker(id)
{var title=$('marker_title_'+id).value;var desc=$('marker_desc_'+id).value;var pars="id="+id
+"&title="+encodeURIComponent(title)
+"&desc="+encodeURIComponent(desc);im_marker_showMessage("update","start");var myAjax=new Ajax.Request(getIMServiceURL('marker.update'),{method:'post',parameters:pars,onSuccess:function(req)
{var resp=req.responseXML.documentElement;if(resp.tagName=="error")
{var msg=resp.getElementsByTagName('message')[0].firstChild.nodeValue;im_marker_showMessage("update","error",msg);}
else
{im_marker_showMessage("update","ok");im_retitleClientMarker(id,title)}},onFailure:function(req)
{im_marker_showMessage("update","error");}});}
function im_deleteMarker(id)
{im_marker_showMessage("delete","start");var myAjax=new Ajax.Request(getIMServiceURL('marker.delete'),{method:'post',parameters:{id:id},onSuccess:function(req)
{var resp=req.responseXML.documentElement;if(resp.tagName=="error")
{var msg=resp.getElementsByTagName('message')[0].firstChild.nodeValue;im_marker_showMessage("delete","error",msg);}
else
{im_marker_showMessage("delete","ok");im_deleteClientMarker(id);}},onFailure:function(req)
{im_marker_showMessage("delete","error");}});}
function im_marker_showMessage(task,status,more)
{im_showMessage('marker',task,status,more);}
function im_openPictureForm()
{clearNode('im_whiteboard');var div=document.createElement('div');div.id="im_createPic";$('im_whiteboard').appendChild(div);var wbtitle=im_createWBTitle("Export this map as image");div.appendChild(wbtitle);var closer=im_getWBCloser();div.appendChild(closer);Event.observe(closer,'click',im_closeWhiteBoard);var h1=document.createElement('h1');h1.innerHTML="TODO";div.appendChild(h1);}
function im_showLayerMD(id)
{clearNode('im_whiteboard');var WB=$('im_whiteboard');var wbtitle=im_createWBTitle(i18n('showLayerMD'));WB.appendChild(wbtitle);var closer=im_getWBCloser();WB.appendChild(closer);Event.observe(closer,'click',im_closeWhiteBoard);var div=document.createElement('div');div.id="im_showLayerMD";div.className='im_wbcontent';WB.appendChild(div);var myAjax=new Ajax.Request(getIMServiceURL('map.layers.getInfo'),{method:'get',parameters:'id='+id,onSuccess:function(req)
{if(req.responseXML&&req.responseXML.documentElement.tagName=="error")
{var resp=req.responseXML.documentElement;var msg=resp.getElementsByTagName('message')[0].firstChild.nodeValue;div.innerHTML=msg;return;}
else
{div.innerHTML=req.responseText;}},onFailure:function(req)
{div.innerHTML=i18n('genericError');}});}
function im_showStyles(id)
{clearNode('im_whiteboard');var WB=$('im_whiteboard');var wbtitle=im_createWBTitle(i18n('titleShowStyles'));WB.appendChild(wbtitle);var closer=im_getWBCloser();WB.appendChild(closer);Event.observe(closer,'click',im_closeWhiteBoard);var div=document.createElement('div');div.id="im_showstyles";div.className='im_wbcontent';WB.appendChild(div);var myAjax=new Ajax.Request(getIMServiceURL('map.layers.getStyles'),{method:'get',parameters:'id='+id,onSuccess:function(req)
{if(req.responseXML&&req.responseXML.documentElement&&req.responseXML.documentElement.tagName=="error")
{var resp=req.responseXML.documentElement;var msg=resp.getElementsByTagName('message')[0].firstChild.nodeValue;div.innerHTML=msg;return;}
else
{div.innerHTML=req.responseText;}},onFailure:function(req)
{div.innerHTML=i18n('genericError');}});}
function im_setStyle(layerid)
{var style=getRadioValue('styleradio');var pars="id="+layerid+"&style="+encodeURIComponent(style);im_showMessage('style','set','start');var myAjax=new Ajax.Request(getIMServiceURL('map.layers.setStyle'),{method:'get',parameters:pars,onSuccess:function(req)
{if(req.responseXML&&req.responseXML.documentElement&&req.responseXML.documentElement.tagName=="error")
{var resp=req.responseXML.documentElement;var msg=resp.getElementsByTagName('message')[0].firstChild.nodeValue;im_showMessage("style","set","error",msg);}
else
{im_showMessage("style","set","ok");im_buildLayerList(req);im_extra_afterLayerUpdated();}},onFailure:function(req)
{im_showMessage("style","set","error");}});}
function im_createWBTitle(title)
{var div=document.createElement('div');div.id="im_wbtitle";var h1=document.createElement('h1');h1.innerHTML=title;div.appendChild(h1);return div;}
function im_getWBCloser()
{var closer=document.createElement('div');closer.id="im_wbcloser";var img=document.createElement('img');img.title=i18n("close");img.src="/intermap/images/close.png";closer.appendChild(img);return closer;}
function im_closeWhiteBoard()
{clearNode('im_whiteboard');}
function getMouseX(e)
{var posx=0;if(!e)var e=window.event;if(e.pageX)
posx=e.pageX;else if(e.clientX)
posx=e.clientX+document.body.scrollLeft;return posx;}
function getMouseY(e)
{var posy=0;if(!e)var e=window.event;if(e.pageY)
posy=e.pageY;else if(e.clientY)
posy=e.clientY+document.body.scrollTop;return posy;}
function openGeoNetwork(what)
{gnWindow=window.open(what,"GeoNetwork");gnWindow.focus();}
function getWindowSize()
{var width=0,weight=0;if(typeof(window.innerWidth)=='number')
{width=window.innerWidth;weight=window.innerHeight;}
else if(document.documentElement&&(document.documentElement.clientWidth||document.documentElement.clientHeight))
{width=document.documentElement.clientWidth;weight=document.documentElement.clientHeight;}
else if(document.body&&(document.body.clientWidth||document.body.clientHeight))
{width=document.body.clientWidth;weight=document.body.clientHeight;}
return[width,weight];}
function clearNode(node)
{var enode=$(node);while(enode.firstChild)
{enode.removeChild(enode.firstChild);}}
function copyTree(src,parentDest)
{var newNode;if(src.nodeType==Node.TEXT_NODE)
{newNode=document.createTextNode(src.nodeValue);parentDest.appendChild(newNode);return;}
if(src.nodeType==Node.COMMENT_NODE)
{newNode=document.createElement("COMMENT");newNode.style.display="none";newNode.textContent=src.nodeValue;parentDest.appendChild(newNode);return;}
newNode=document.createElement(src.tagName);var tattr=" ";if(src.attributes)
{var attrs=src.attributes;for(var i=attrs.length-1;i>=0;i--)
{newNode.setAttribute(attrs[i].name,attrs[i].value);tattr+=(attrs[i].name+'="'+attrs[i].value+'"');}}
$(parentDest).appendChild(newNode);var child=src.firstChild;while(child)
{copyTree(child,newNode);child=child.nextSibling;}}
function xml2text(src)
{if(src.nodeType==Node.TEXT_NODE)
{return src.nodeValue;}
if(src.nodeType==Node.COMMENT_NODE)
{return"<!-- "+src.nodeValue+" -->";}
var tattr="";if(src.attributes)
{var attrs=src.attributes;for(var i=attrs.length-1;i>=0;i--)
{tattr+=(attrs[i].name+'="'+attrs[i].value+'" ');}}
var text="<"+src.tagName+" "+tattr+'>\n';var wrapstart="";if(src.tagName=="table")
{text+="<tbody>";wrapend="</tbody>";}
var child=src.firstChild;while(child)
{text+=xml2text(child)+"\n";child=child.nextSibling;}
text+=wrapend+"</"+src.tagName+">";return text;}
AIM={frame:function(c){var n='f'+Math.floor(Math.random()*99999);var d=document.createElement('DIV');d.innerHTML='<iframe style="display:none" src="about:blank" id="'+n+'" name="'+n+'" onload="AIM.loaded(\''+n+'\')"></iframe>';document.body.appendChild(d);var i=document.getElementById(n);if(c&&typeof(c.onComplete)=='function'){i.onComplete=c.onComplete;}
return n;},form:function(f,name){f.setAttribute('target',name);},submit:function(f,c){AIM.form(f,AIM.frame(c));if(c&&typeof(c.onStart)=='function'){return c.onStart();}else{return true;}},loaded:function(id){var i=document.getElementById(id);if(i.contentDocument){var d=i.contentDocument;}else if(i.contentWindow){var d=i.contentWindow.document;}else{var d=window.frames[id].document;}
if(d.location.href=="about:blank"){return;}
if(typeof(i.onComplete)=='function'){i.onComplete(d);}}};function getRadioValue(name)
{var radioarr=$A(document.getElementsByName(name));for(i=0;i<radioarr.length;i++)
{if(radioarr[i].checked)
return radioarr[i].value;}
return null;}
function MapUtils(){}
MapUtils.urlizebb=function(n,e,s,w)
{return"northBL="+n+"&eastBL="+e+"&southBL="+s+"&westBL="+w;};MapUtils.drawBox=function(box,left,top,width,height)
{box.style.left=left+'px';box.style.top=top+'px';box.style.width=width+'px';box.style.height=height+'px';};MapUtils.dezoom=function(n,e,s,w,factor)
{if(factor===null)
{factor=2;}
var dx=(e-w)/factor;var dy=(n-s)/factor;return MapUtils.urlizebb(n-dy,e+dx,s+dy,w-dx);};function FakeBox(name)
{this.name=name;}
FakeBox.prototype.name;FakeBox.prototype.draw=function(left,top,width,height)
{var n=$(this.name+'_n');var e=$(this.name+'_e');var s=$(this.name+'_s');var w=$(this.name+'_w');if(Prototype.Browser.IE)
{if(width>0){width--;}
if(height>0){height--;}}
n.style.left=left+'px';n.style.top=top+'px';n.style.width=width+'px';n.style.height='0px';e.style.left=(left+width-1)+'px';e.style.top=top+'px';e.style.width='0px';e.style.height=height+'px';s.style.left=left+'px';s.style.top=(top+height-1)+'px';s.style.width=width+'px';s.style.height='0px';w.style.left=left+'px';w.style.top=top+'px';w.style.width='0px';w.style.height=height+'px';if(Prototype.Browser.IE)
{n.style.height='1px';e.style.width='1px';s.style.height='1px';w.style.width='1px';}};FakeBox.prototype.hide=function()
{$(this.name+'_n').hide();$(this.name+'_e').hide();$(this.name+'_s').hide();$(this.name+'_w').hide();};FakeBox.prototype.show=function()
{$(this.name+'_n').show();$(this.name+'_e').show();$(this.name+'_s').show();$(this.name+'_w').show();};Ajax.Responders.register({onException:function(req,e){var debug=false;var qqq=document.createElement('div');qqq.innerHTML="Exception '"+e.message+"'";document.body.appendChild(qqq);if(debug){alert("Exception: "+e.message+"\nFile "+e.fileName+"\nLine "+e.lineNumber+"\nStack "+e.stack);}}});function im_mm_init(wmc,callback)
{im_mm_initTextControls($('northBL'),$('eastBL'),$('southBL'),$('westBL'));if(im_mm_ctrl_n.value===''){im_mm_ctrl_n.value=im_mm.north;}
if(im_mm_ctrl_e.value===''){im_mm_ctrl_e.value=im_mm.east;}
if(im_mm_ctrl_s.value===''){im_mm_ctrl_s.value=im_mm.south;}
if(im_mm_ctrl_w.value===''){im_mm_ctrl_w.value=im_mm.west;}
var newCallback=function()
{if(callback)
{callback();}
im_mm_fullAoI();};if(wmc&&Prototype.Browser.IE)
{alert("Sorry, but your browser can't handle long URLs.\n\n"+"If you arrived on this page following a link for viewing a map someone sent you,\n"+"please be advised that you can't use that feature with your current browser.\n"+"You are going to see a default map, and not the one you were looking for.\n\n"+"Please use the 'Upload a context' button in the Map Viewer and provide a valid context document.");wmc=null;}
if(wmc)
{im_setWMC(wmc,function(req)
{var resp=req.responseXML.documentElement;im_bm.setBBox_dom(resp);im_bm.setSize_dom(resp);openIntermap();im_mm.setBBox_dom(resp);im_mm.rebuild(newCallback);im_mm.setTool('zoomin');});}
else
{im_mm.rebuild(newCallback);im_mm.setTool('zoomin');}}
var im_refreshMiniMap=function()
{im_mm.rebuild();};var im_refreshBothMaps=function()
{im_bm.rebuild(im_refreshMiniMap);};im_extra_drivingMap=im_bm;im_extra_afterLayerUpdated=im_refreshBothMaps;im_extra_afterWmcSet=function(resp)
{im_bm.set_dom(resp);im_bm.setBBox_dom(resp);im_redrawMarkers(resp);im_mm.setBBox_dom(resp);im_mm.rebuild(im_mm_fullAoI);};var im_1stTimeIntermap=true;function openIntermap()
{if(im_1stTimeIntermap)
{im_1stTimeIntermap=false;$('openIMBtn').hide();$('loadIMBtn').show();imc_init_loadSkel();return;}
$('openIMBtn').hide();$('loadIMBtn').hide();$('closeIMBtn').show();if(!Prototype.Browser.IE)
{Effect.BlindDown('im_map');Effect.BlindDown('im_bm_image');Effect.BlindDown('fillMeWithIntermap');}
else
{$('im_map').show();$('im_bm_image').show();$('fillMeWithIntermap').show();}
im_showClientMarkers(true);}
function closeIntermap()
{if(!$('im_map'))
return;im_showClientMarkers(false);try{$('closeIMBtn').hide();$('openIMBtn').show();}catch(e){}
if(!Prototype.Browser.IE)
{Effect.BlindUp('im_map');Effect.BlindUp('fillMeWithIntermap');}
else
{$('im_map').hide();$('fillMeWithIntermap').hide();}}
function imc_init_loadSkel()
{var myAjax=new Ajax.Request(getIMServiceURL('map.getMain.embedded'),{method:'get',parameters:'',onSuccess:im_init_loadCompleted,onFailure:im_load_error});}
function im_init_loadCompleted(req)
{var im=$('fillMeWithIntermap');im.innerHTML=req.responseText;$('im_mm_image_waitdiv').hide();new Effect.Pulsate('openIMBtn');im_bm.rebuild(im_init_bmLoaded);}
function im_init_bmLoaded()
{im_mm.setStatus('idle');im_bm.setStatus('idle');Event.observe('im_resize','mousedown',im_bm_resizeStart);setTool('zoomin');imc_reloadLayers();openIntermap();}
function im_reset()
{im_bm.setStatus('busy');im_mm.setStatus('busy');imc_reset();}
function imc_reset()
{var myAjax=new Ajax.Request(getIMServiceURL('map.reset'),{method:'get',onSuccess:im_reset_complete,onFailure:im_load_error});}
function im_reset_complete(req)
{im_bm.setTool('zoomin');im_mm.setTool('zoomin');imc_reloadLayers();im_bm.setBBox_dom(req.responseXML);im_mm.setBBox_dom(req.responseXML);im_bm.rebuild(im_mm.rebuild.bindAsEventListener(im_mm));}
function i18n(key)
{var v=$('i18n_'+key);if(v)
{if(v.value==='')
return'{'+key+'}';else
return v.value;}
else
return'['+key+']';}
var im_mm=new Intermap(200,100,'im_mm_image');var im_aoi=new FakeBox('im_mm_aoibox');var im_mm_ctrl_n,im_mm_ctrl_e,im_mm_ctrl_s,im_mm_ctrl_w;function trace(fname)
{alert("Entering function --> "+fname);}
function im_mm_initTextControls(n,e,s,w)
{im_mm_ctrl_n=n;im_mm_ctrl_e=e;im_mm_ctrl_s=s;im_mm_ctrl_w=w;}
function im_mm_setTextCoords(north,east,south,west)
{im_mm_ctrl_n.value=north;im_mm_ctrl_e.value=east;im_mm_ctrl_s.value=south;im_mm_ctrl_w.value=west;}
function im_mm_setTextLenght(top,right,bottom,left){var north=im_mm.y2lat(top);var south=im_mm.y2lat(bottom);var west=im_mm.x2lon(left);var east=im_mm.x2lon(right);im_mm_setTextCoords(north,east,south,west);}
function im_mm_getURLbbox()
{var ret=im_mm.getURLbbox();return ret!==null?ret:im_mm_getURLselectedbbox();}
function im_mm_getURLselectedbbox()
{return MapUtils.urlizebb(im_mm_ctrl_n.value,im_mm_ctrl_e.value,im_mm_ctrl_s.value,im_mm_ctrl_w.value);}
function im_mm_setTool(tool)
{im_mm.setTool(tool);}
function im_mm_fullExtent()
{im_mm.fullExtent();}
im_mm.afterToolSet=function(tool)
{$('minimap_root').className=tool;};im_mm.beforeMouseDown=function(e)
{};im_mm.unresolvedMouseDown=function(e)
{switch(this.tool)
{case'aoi':im_aoi.show();im_mm_startAoi(e);break;}};function im_mm_startAoi(e)
{Event.stop(e);Event.observe(document,'mousemove',im_mm_resizeAoi);Event.observe(document,'mouseup',im_mm_stopAoi);var offset=Position.cumulativeOffset($(im_mm.imageId));var offsetX=offset[0];var offsetY=offset[1];im_mm_startX=Event.pointerX(e)-offsetX+1;im_mm_startY=Event.pointerY(e)-offsetY+1;im_aoi.draw(im_mm_startX,im_mm_startY,0,0);im_mm_setTextLenght(im_mm_startY,im_mm_startX,im_mm_startY,im_mm_startX);}
function im_mm_resizeAoi(e)
{Event.stop(e);var offset=Position.cumulativeOffset($(im_mm.imageId));var offsetX=offset[0];var offsetY=offset[1];var pX=Event.pointerX(e)-offsetX;var pY=Event.pointerY(e)-offsetY;pX=Math.max(pX,0);pY=Math.max(pY,0);pX=Math.min(pX,im_mm.width-1);pY=Math.min(pY,im_mm.height-1);im_aoi.draw(Math.min(pX,im_mm_startX),Math.min(pY,im_mm_startY),Math.abs(pX-im_mm_startX),Math.abs(pY-im_mm_startY));}
function im_mm_stopAoi(e)
{Event.stopObserving(document,'mousemove',im_mm_resizeAoi);Event.stopObserving(document,'mouseup',im_mm_stopAoi);var offset=Position.cumulativeOffset($(im_mm.imageId));var offsetX=offset[0];var offsetY=offset[1];var pX=Event.pointerX(e)-offsetX;var pY=Event.pointerY(e)-offsetY;pX=Math.max(pX,0);pY=Math.max(pY,0);pX=Math.min(pX,im_mm.width-1);pY=Math.min(pY,im_mm.height-1);im_mm_setTextLenght(Math.min(pY,im_mm_startY),Math.max(pX,im_mm_startX),Math.max(pY,im_mm_startY),Math.min(pX,im_mm_startX));var func=im_mm_aoiUpdated;if(typeof func=='function')
{func();}}
function im_mm_zoomToAoI()
{im_mm.setBBox(parseFloat(im_mm_ctrl_n.value),parseFloat(im_mm_ctrl_e.value),parseFloat(im_mm_ctrl_s.value),parseFloat(im_mm_ctrl_w.value));im_mm.rebuild();}
function im_mm_fullAoI()
{im_mm_setTextLenght(0,im_mm.width-1,im_mm.height-1,0);im_aoi.draw(0,0,im_mm.width,im_mm.height);}
function im_mm_redrawAoI()
{var x1=im_mm.lon2x(parseFloat(im_mm_ctrl_w.value));var x2=im_mm.lon2x(parseFloat(im_mm_ctrl_e.value));var y1=im_mm.lat2y(parseFloat(im_mm_ctrl_n.value));var y2=im_mm.lat2y(parseFloat(im_mm_ctrl_s.value));if(isNaN(x1)||isNaN(x2)||isNaN(y1)||isNaN(y2))
return;var w=Math.abs(x2-x1);var h=Math.abs(y2-y1);im_aoi.show();im_aoi.draw(x1,y1,w,h);}
im_mm.afterImageRebuilt=function(req)
{im_mm_redrawAoI();};var im_mark_tmp_id=1;var im_markarr=new Array();var im_tmpMarker;var marker_offset_x=6;var marker_offset_y=22;function im_setMark(e)
{Event.stop(e);var offset=Position.cumulativeOffset($(im_bm.imageId));var offsetX=offset[0];var offsetY=offset[1];pointerX=Event.pointerX(e);pointerY=Event.pointerY(e);var x=pointerX-offsetX;var y=pointerY-offsetY;var lat=im_bm.y2lat(y);var lon=im_bm.x2lon(x);if(lat<-90||lat>90||lon<-180||lon>180)
{return;}
im_tmpMarker=new IMMarker(lat,lon,"unknown");var img=document.createElement("img");img.id='im_marker_img_tmp';img.className='im_marker';img.src='/intermap/images/marker.gif';img.style.left=pointerX-6+"px";img.style.top=pointerY-21+"px";document.body.appendChild(img);var div=document.createElement("div");div.id="im_marker_box_tmp";div.className="im_markerbox";document.body.appendChild(div);var wbox=200;var hbox=50;var horEdge=10;var dybox=20;var dxbox=wbox/2;if(y+dybox+hbox>im_bm.height)
{dybox=-dybox-hbox;}
if(x-dxbox+wbox>im_bm.width-horEdge)
{dxbox=x+wbox+horEdge-im_bm.width;}
else if(x-dxbox<horEdge)
{dxbox=x-horEdge;}
div.style.left=(pointerX-dxbox)+"px";div.style.top=(pointerY+dybox)+"px";div.style.width=wbox+"px";div.style.height=hbox+"px";var rlat=Math.round(lat*10000)/10000;var rlon=Math.round(lon*10000)/10000;div.innerHTML="Lat:"+rlat+" Lon:"+rlon;var text=document.createElement("input");text.id="im_marker_input_tmp";text.type="text";div.appendChild(text);text.focus();var closer=document.createElement('div');closer.className="upperright";var img=document.createElement('img');img.title=i18n("close");img.src="/intermap/images/close.png";closer.appendChild(img);Event.observe(img,'click',im_closeMarkerBox);div.appendChild(closer);var saver=document.createElement('div');saver.className="lowerright";var simg=document.createElement('img');simg.title=i18n("save");simg.src="/intermap/images/filesave.png";saver.appendChild(simg);Event.observe(simg,'click',im_saveMarker);div.appendChild(saver);}
function im_closeMarkerBox(e)
{removeMarkerBox();}
function im_saveMarker(e)
{var tmpmarker=$('im_marker_img_tmp');im_tmpMarker.title=$('im_marker_input_tmp').value;im_createMarker(im_mark_tmp_id,im_tmpMarker.lat,im_tmpMarker.lon,im_tmpMarker.title,true);imc_saveMarker(im_mark_tmp_id,im_tmpMarker.lat,im_tmpMarker.lon,im_tmpMarker.title);im_mark_tmp_id++;removeMarkerBox();}
function removeMarkerBox()
{if($('im_marker_img_tmp'))
$('im_marker_img_tmp').remove();if($('im_marker_box_tmp'))
$('im_marker_box_tmp').remove();}
function imc_saveMarker(tmp_id,lat,lon,title)
{var myAjax=new Ajax.Request(getIMServiceURL('marker.add'),{method:'post',parameters:{lat:lat,lon:lon,title:title,desc:'Description of '+title},onSuccess:function(req)
{if(req.responseXML&&req.responseXML.documentElement.tagName=="error")
{var resp=req.responseXML.documentElement;var msg=resp.getElementsByTagName('message')[0].firstChild.nodeValue;alert(msg);return;}
else if(req.responseXML)
{var resp=req.responseXML.documentElement;var id=resp.getElementsByTagName('added')[0].firstChild.nodeValue;im_concretizeTempMarker(tmp_id,id);}},onFailure:function(req)
{div.innerHTML=i18n('genericError');}});}
function IMMarker(lat,lon,title)
{this.lat=parseFloat(lat);this.lon=parseFloat(lon);this.title=title;}
IMMarker.prototype.lat;IMMarker.prototype.lon;IMMarker.prototype.title;IMMarker.prototype.seq;IMMarker.prototype.id;function im_deleteAllMarkersImages()
{im_markers.each(function(marker)
{var seq=marker.seq;var img=$("im_marker_"+seq);if(img)
{img.remove();}});}
function im_createMarkersDom(dommarkerlist)
{if(!dommarkerlist)
{return;}
if(dommarkerlist.hasChildNodes())
{var children=dommarkerlist.childNodes;for(var i=0;i<children.length;i++)
{if(children[i].nodeType==Node.ELEMENT_NODE)
im_createMarkerDom(children[i]);};};}
function im_createMarkerDom(dommarker)
{var id=dommarker.getAttribute('id');var lat=dommarker.getAttribute('lat');var lon=dommarker.getAttribute('lon');var title="undef";var children=dommarker.childNodes;for(var i=0;i<children.length;i++)
{if(children[i].nodeType==Node.ELEMENT_NODE&&children[i].tagName=="title")
title=children[i].firstChild.nodeValue;}
im_createMarker(id,lat,lon,title);}
function im_createMarker(id,lat,lon,title,btemp)
{if(btemp===null)
btemp=false;var parent=$(im_bm.imageId);if(parent===null)
return;parent=parent.parentNode;var y=im_bm.lat2y(lat);var x=im_bm.lon2x(lon);if(y<0||x<0||y>$(im_bm.imageId).height||x>$(im_bm.imageId).width)
{return;}
var img=document.createElement("img");img.id='im_marker_'+(btemp?"tmp_":"")+id;img.className='im_marker';img.src='/intermap/images/marker.gif';img.title=title;img.style.left=x-6+"px";img.style.top=y-21+"px";parent.appendChild(img);if(!btemp)
{im_markarr[im_markarr.length]=id;img.onclick=function(e){im_markerClicked(e,id);};}}
function im_concretizeTempMarker(tempid,id)
{var imgoldid='im_marker_tmp_'+tempid;var imgnewid='im_marker_'+id;var img=$(imgoldid);img.id=imgnewid;im_markarr[im_markarr.length]=id;img.onclick=function(e){im_markerClicked(e,id);};}
function im_deleteClientMarkers()
{im_markarr.each(im_deleteClientMarker);im_markarr=new Array();}
function im_deleteClientMarker(markerid)
{if(markerid)
{var img=$("im_marker_"+markerid);if(img)
{img.remove();}}}
function im_redrawMarkers(dom)
{im_deleteClientMarkers();var markerlist=dom.getElementsByTagName('markers')[0];im_createMarkersDom(markerlist);}
function im_showClientMarkers(doShow)
{im_markarr.each(function(markerid)
{if(markerid)
{var img=$("im_marker_"+markerid);if(img)
{if(doShow)
img.show();else
img.hide();}}});}
function im_retitleClientMarker(markerid,title)
{if(markerid)
{var img=$("im_marker_"+markerid);if(img)
{img.title=title;}}}
function im_markerClicked(e,id)
{im_markerList(id);}
var im_layer_width=176;var activeLayerId=null;function imc_reloadLayers()
{var myAjax=new Ajax.Request(getIMServiceURL('map.getLayers.embedded'),{method:'get',onComplete:im_buildLayerList});}
function im_buildLayerList(req)
{if(im_checkError(req))
{im_showError(req);return;}
if(!$('im_layersDiv'))
return;$('im_layersDiv').innerHTML=req.responseText;if(!$('layerList_'+activeLayerId))
activeLayerId=im_getFirstLayerId();activateMapLayer(activeLayerId,true);if($('im_layerList'))
{if(!Prototype.Browser.IE)
createSortable();}}
function createSortable()
{Sortable.create($('im_layerList'),{dropOnEmpty:true,containment:['im_layerList'],constraint:false,onUpdate:function(){layersOrderChanged(Sortable.serialize('im_layerList'));}});}
function layerDblClickListener(id)
{imc_zoomToLayer(id);}
function imc_zoomToLayer(layerId)
{im_extra_drivingMap.setStatus('busy');var pars='id='+layerId+'&width='+im_extra_drivingMap.width+'&height='+im_extra_drivingMap.height;var myAjax=new Ajax.Request(getIMServiceURL('map.zoomToService'),{method:'get',parameters:pars,onComplete:function(req)
{var resp=req.responseXML;im_extra_afterWmcSet(resp);im_extra_drivingMap.setStatus('idle');},onFailure:reportError});}
function toggleVisibility(id){var pars='id='+id;var myAjax=new Ajax.Request(getIMServiceURL('map.layers.toggleVisibility'),{method:'get',parameters:pars,onComplete:function(request){setLayerVisibility(request,id);},onFailure:reportError});}
function setLayerVisibility(req,id)
{var visibility=req.responseXML.getElementsByTagName('visible')[0].firstChild.nodeValue;var img=$('visibility_'+id);if(visibility=='true')
img.src='/intermap/images/showLayer.png';else
img.src='/intermap/images/hideLayer.png';im_extra_afterLayerUpdated();}
function activateMapLayer(id,keepnew)
{disactivateAllMapLayers(keepnew);if(id)
{var mapLayer=$('layerList_'+id);mapLayer.className='im_activeLayer';$('layerControl_'+id).show();}
activeLayerId=id;}
function disactivateAllMapLayers(keepnew)
{var li=$('im_layersDiv').getElementsByTagName('li');var layers=$A(li);layers.each(function(mapLayer)
{if(!(keepnew&&mapLayer.className=="im_newLayer"))
{mapLayer.className='im_inactiveLayer';}
var trList=mapLayer.getElementsByTagName('tr');$A(trList).each(function(tr)
{if(new String(tr.id).search('layerControl_')!=-1)
$(tr).hide();});});}
function im_layerMoveDown(id)
{var myAjax=new Ajax.Request(getIMServiceURL('map.layers.moveDown'),{parameters:'id='+id,method:'get',onComplete:function(req)
{im_buildLayerList(req);im_extra_afterLayerUpdated();}});}
function im_layerMoveUp(id)
{var myAjax=new Ajax.Request(getIMServiceURL('map.layers.moveUp'),{parameters:'id='+id,method:'get',onComplete:function(req)
{im_buildLayerList(req);im_extra_afterLayerUpdated();}});}
function layersOrderChanged(order)
{var pars=order.replace(new RegExp("\\[\\]","g"),"");var myAjax=new Ajax.Request(getIMServiceURL('map.layers.setOrder'),{method:'get',parameters:pars,onComplete:function(req)
{im_buildLayerList(req);im_extra_afterLayerUpdated();},onFailure:reportError});}
function im_deleteLayer(id)
{var llist=$('im_layerList');var nodes=llist.getElementsByTagName('li');if($A(nodes).length==1)
{alert("Can't remove last layer");return;}
var nextid=im_getNextActivableLayer(id);if(nextid)
activateMapLayer(nextid);imc_deleteLayer(id);}
function imc_deleteLayer(id)
{var pars='id='+id;var myAjax=new Ajax.Request(getIMServiceURL('map.layers.deleteLayer'),{method:'get',parameters:pars,onComplete:function(req)
{im_buildLayerList(req);im_extra_afterLayerUpdated();},onFailure:reportError});}
function im_getNextActivableLayer(id)
{var child=$('layerList_'+id);if(child===null)
return null;else
{if(id!=activeLayerId)
return activeLayerId;else
{var nextActiveLayer=child.nextSibling;if(nextActiveLayer===null)
nextActiveLayer=child.previousSibling;if(nextActiveLayer===null)
return null;else
{var t=nextActiveLayer.getAttribute('id');var nextActiveLayerId=t.substr(t.indexOf('_')+1);return nextActiveLayerId;}}}}
function im_getFirstLayerId()
{var ul=$('im_layerList');if(!ul)
{return null;}
var li1=ul.getElementsByTagName("li")[0];if(li1===null)
return null;else
{var t=li1.getAttribute('id');return t.substr(t.indexOf('_')+1);}}
function im_layerTransparencyChanged(id)
{var transp=$('im_transp_'+id).value;imc_setLayerTransparency(id,transp);}
function imc_setLayerTransparency(id,transparency)
{var pars='id='+id+'&transparency='+transparency/100.0;var myAjax=new Ajax.Request(getIMServiceURL('map.layers.setTransparency'),{method:'get',parameters:pars,onComplete:im_extra_afterLayerUpdated,onFailure:reportError});}
function showActiveLayerLegend(id){showLegend(activeLayerId);}
function hideLegend()
{var div=$('im_legendPopup');if(div)
{Event.stopObserving(div,'click',hideLegend);document.body.removeChild(div);}}
function showLegend(url,btn){hideLegend();var div=document.createElement("div");div.id="im_legendPopup";div.style.position="absolute";document.body.appendChild(div);var offset=Position.cumulativeOffset($(btn));var x=offset[0];var y=offset[1];div.style.left=x+"px";div.style.top=y+"px";div.style.width="100px";div.style.height="50px";var img=document.createElement("img");img.src=url;img.alt="Loading legend...";img.style.position="absolute";img.style.border="solid black 1px";div.appendChild(img);Event.observe(div,'click',hideLegend);}
im_load_error=function(){alert("Loading error");};function im_checkError(req)
{var doc=req.responseXML;if(!doc)
{return false;}
if(doc.firstChild===null)
{return false;}
if(doc.firstChild.tagName=="error")
{return true;}
return false;}
function im_showError(req)
{var doc=req.responseXML;if(!doc)
{return false;}
if(doc.firstChild===null)
{return false;}
if(doc.firstChild.tagName=="error")
{var err=doc.firstChild;var mess=err.getElementsByTagName('message')[0].textContent;var clss=err.getElementsByTagName('class')[0].textContent;alert("Intermap error: "+mess+"\nException: "+clss);return true;}
return false;}
function im_setWMC(wmc,callback)
{var pars='wmc='+wmc;var myAjax=new Ajax.Request(getIMServiceURL('wmc.setContext'),{method:'get',parameters:pars,onComplete:callback,onFailure:reportError});}
function imc_setContextFromURL(url,doClearContext,callback)
{var pars='url='+encodeURIComponent(url);pars+="&clear="+(doClearContext===true?"true":"false");var myAjax=new Ajax.Request(getIMServiceURL('wmc.setContextFromURL'),{method:'post',parameters:pars,onComplete:callback,onFailure:reportError});}
function imc_addService(surl,service,type,doClearContext,callback)
{var pars='url='+encodeURIComponent(surl)
+'&service='+encodeURIComponent(service)
+'&type='+type;pars+="&clear="+(doClearContext===true?"true":"false");var myAjax=new Ajax.Request(getIMServiceURL('map.addServices.embedded'),{method:'get',parameters:pars,onComplete:callback,onFailure:reportError});}
function imc_addServices(surl,serviceArray,type,callback)
{var pars='url='+encodeURIComponent(surl)
+'&type='+type;serviceArray.each(function(service)
{pars+='&service='+encodeURIComponent(service);});var myAjax=new Ajax.Request(getIMServiceURL('map.addServices.embedded'),{method:'post',parameters:pars,onComplete:callback,onFailure:reportError});}
function imc_loadServices(id,callback)
{var myAjax=new Ajax.Request(getIMServiceURL('mapServers.getServices.xml'),{parameters:"mapserver="+id,method:'get',onComplete:callback,onFailure:reportError});}
function Intermap(initWidth,initHeight,imageId)
{this.width=initWidth;this.height=initHeight;this.imageId=imageId;this.mouseDownListener=null;}
Intermap.prototype.imageId;Intermap.prototype.width;Intermap.prototype.height;Intermap.prototype.tool;Intermap.prototype.north;Intermap.prototype.east;Intermap.prototype.south;Intermap.prototype.west;Intermap.prototype.cachedMouseDownListener;Intermap.prototype.cachedMouseMoveListener;Intermap.prototype.cachedMouseUpListener;Intermap.prototype.beforeMouseDown;Intermap.prototype.unresolvedMouseDown;Intermap.prototype.afterToolSet;Intermap.prototype.beforeAction;Intermap.prototype.afterAction;Intermap.prototype.beforePan;Intermap.prototype.afterPan;Intermap.prototype.afterImageRebuilt;Intermap.prototype.set_dom=function(response)
{this.setSize_dom(response);var mmurl=response.getElementsByTagName('imgUrl')[0].firstChild.nodeValue;$(this.imageId).src=mmurl;this.setBBox_dom(response);};Intermap.prototype.setBBox_dom=function(response)
{var extent=response.getElementsByTagName('extent')[0];var minx=extent.getAttribute('minx');var maxx=extent.getAttribute('maxx');var miny=extent.getAttribute('miny');var maxy=extent.getAttribute('maxy');this.setBBox(maxy,maxx,miny,minx);};Intermap.prototype.setBBox=function(n,e,s,w)
{this.north=parseFloat(n);this.east=parseFloat(e);this.south=parseFloat(s);this.west=parseFloat(w);};Intermap.prototype.setSize_dom=function(response)
{var w=response.getElementsByTagName('width')[0].firstChild.nodeValue;var h=response.getElementsByTagName('height')[0].firstChild.nodeValue;this.setSize(w,h);};Intermap.prototype.setSize=function(width,height)
{this.width=parseInt(width);this.height=parseInt(height);var img=$(this.imageId);if(img)
{img.style.width=width+'px';img.style.height=height+'px';}};Intermap.prototype.lat2y=function(lat)
{return Math.round(this.height-(lat-this.south)*this.height/(this.north-this.south));};Intermap.prototype.lon2x=function(lon)
{return Math.round((lon-this.west)*this.width/(this.east-this.west));};Intermap.prototype.y2lat=function(y)
{return this.south+(this.height-y)*(this.north-this.south)/this.height;};Intermap.prototype.x2lon=function(x)
{return this.west+x*(this.east-this.west)/this.width;};Intermap.prototype.getURLbbox=function()
{if(this.north)
return MapUtils.urlizebb(this.north,this.east,this.south,this.west);else
return null;};Intermap.prototype.setTool=function(tool)
{this.tool=tool;if(this.afterToolSet)
{this.afterToolSet(tool);}};Intermap.prototype.startX;Intermap.prototype.startY;Intermap.prototype.mousedownEventListener=function(e)
{if(this.beforeMouseDown)
this.beforeMouseDown(e);switch(this.tool)
{case'zoomin':this.startZoombox(e);break;case'zoomout':this.startZoombox(e);break;case'pan':this.startDrag(e);break;default:if(this.unresolvedMouseDown)
this.unresolvedMouseDown(e);}};Intermap.prototype.zoombox;Intermap.prototype.startZoombox=function(e)
{var element=Event.element(e);Event.stop(e);Event.stopObserving(document,'mousemove',this.cachedMouseMoveListener);Event.stopObserving(document,'mouseup',this.cachedMouseUpListener);this.cachedMouseMoveListener=this.resizeZoombox.bindAsEventListener(this);Event.observe(document,'mousemove',this.cachedMouseMoveListener);this.cachedMouseUpListener=this.stopZoombox.bindAsEventListener(this);Event.observe(document,'mouseup',this.cachedMouseUpListener);this.startX=Event.pointerX(e);this.startY=Event.pointerY(e);this.zoombox=document.createElement('div');this.zoombox.id=this.imageId+'_zoombox';document.body.appendChild(this.zoombox);MapUtils.drawBox(this.zoombox,this.startX,this.startY,0,0);};Intermap.prototype.resizeZoombox=function(e)
{Event.stop(e);var pX=Event.pointerX(e);var pY=Event.pointerY(e);var offset=Position.cumulativeOffset($(this.imageId));var offsetX=offset[0];var offsetY=offset[1];pX=Math.max(pX,offsetX+2);pY=Math.max(pY,offsetY+2);pX=Math.min(pX,offsetX+this.width-2);pY=Math.min(pY,offsetY+this.height-2);MapUtils.drawBox(this.zoombox,Math.min(pX,this.startX),Math.min(pY,this.startY),Math.abs(pX-this.startX),Math.abs(pY-this.startY));};Intermap.prototype.stopZoombox=function(e)
{Event.stop(e);Event.stopObserving(document,'mousemove',this.cachedMouseMoveListener);Event.stopObserving(document,'mouseup',this.cachedMouseUpListener);var pX=Event.pointerX(e);var pY=Event.pointerY(e);var offset=Position.cumulativeOffset($(this.imageId));var offsetX=offset[0];var offsetY=offset[1];this.setStatus('busy');this.ajax_action(this.tool,Math.min(pX,this.startX)-offsetX,Math.max(pY,this.startY)-offsetY,Math.max(pX,this.startX)-offsetX,Math.min(pY,this.startY)-offsetY);Element.remove(this.zoombox);};Intermap.prototype.ajax_action=function(tool,xmin,ymin,xmax,ymax)
{var pars='maptool='+tool+'&mapimgx='+xmin+'&mapimgy='+ymin+'&mapimgx2='+xmax+'&mapimgy2='+ymax+"&width="+this.width+"&height="+this.height+"&"+this.getURLbbox();var myAjax=new Ajax.Request(getIMServiceURL('map.action'),{method:'get',parameters:pars,onComplete:this.imageRebuilt.bindAsEventListener(this),onFailure:function(req)
{alert("ERROR");}});};Intermap.prototype.startDragX;Intermap.prototype.startDragY;Intermap.prototype.startDrag=function(e)
{Event.stop(e);Event.stopObserving(document,'mousemove',this.cachedMouseMoveListener);Event.stopObserving(document,'mouseup',this.cachedMouseUpListener);Event.stopObserving(document,'mousedown',this.cachedMouseDownListener);this.cachedMouseMoveListener=this.dragImage.bindAsEventListener(this);Event.observe(document,'mousemove',this.cachedMouseMoveListener);this.cachedMouseUpListener=this.stopDrag.bindAsEventListener(this);Event.observe(document,'mouseup',this.cachedMouseUpListener);var offset=Position.cumulativeOffset($(this.imageId));this.startDragX=offset[0];this.startDragY=offset[1];this.startX=Event.pointerX(e);this.startY=Event.pointerY(e);};Intermap.prototype.dragImage=function(e)
{window.status=Event.pointerX(e)+' - '+Event.pointerY(e);Event.stop(e);var img=$(this.imageId);var offset=Position.cumulativeOffset(img);var offsetX=offset[0];var offsetY=offset[1];img.style.position='absolute';var x=Event.pointerX(e)-this.startX;var y=Event.pointerY(e)-this.startY;img.style.left=x+'px';img.style.top=y+'px';};Intermap.prototype.stopDrag=function(e)
{Event.stop(e);Event.stopObserving(document,'mousemove',this.cachedMouseMoveListener);Event.stopObserving(document,'mouseup',this.cachedMouseUpListener);var img=$(this.imageId);var offset=Position.cumulativeOffset(img);var offsetX=offset[0];var offsetY=offset[1];this.setStatus('busy');var w=img.clientWidth;var h=img.clientHeight;this.ajax_move(this.startDragX-offsetX,offsetY-this.startDragY,w,h);};Intermap.prototype.ajax_move=function(deltax,deltay,width,height)
{var pars='deltax='+deltax+'&deltay='+deltay+"&width="+width+"&height="+height+"&"+this.getURLbbox();var myAjax=new Ajax.Request(getIMServiceURL('map.move'),{method:'get',parameters:pars,onComplete:this.imageRebuilt.bindAsEventListener(this),onFailure:reportError});};Intermap.prototype.fullExtent=function()
{this.setStatus('busy');this.setTool('zoomin');var pars="width="+this.width+"&height="+this.height;var myAjax=new Ajax.Request(getIMServiceURL('map.fullExtent'),{method:'get',parameters:pars,onComplete:this.imageRebuilt.bindAsEventListener(this),onFailure:reportError});};Intermap.prototype.rebuild=function(callback)
{if(!$(this.imageId))
{if(callback)
{callback();}
return;}
var pars="width="+this.width
+"&height="+this.height;qbbox=this.getURLbbox();if(qbbox)
pars+="&"+qbbox;this.setStatus('busy');var thismap=this;var myAjax=new Ajax.Request(getIMServiceURL('map.update'),{method:'get',parameters:pars,onComplete:function(req)
{thismap.imageRebuilt(req);if(callback)
{callback();}}.bindAsEventListener(thismap),onFailure:function(req)
{alert("ERROR");}});};Intermap.prototype.imageRebuilt=function(req)
{this.set_dom(req.responseXML);var img=$(this.imageId);img.style.left='0';img.style.top='0';if(this.afterImageRebuilt)
{this.afterImageRebuilt(req);}
this.setStatus('idle');};Intermap.prototype.setStatus=function(status)
{if(!this.cachedMouseDownListener)
{this.cachedMouseDownListener=this.mousedownEventListener.bindAsEventListener(this);}
switch(status)
{case'busy':Event.stopObserving(this.imageId,'mousedown',this.cachedMousedownListener);Event.observe(this.imageId,'mousedown',this.noOp);$(this.imageId).style.cursor='wait';$(this.imageId+'_waitdiv').show();break;case'idle':Event.stopObserving(this.imageId,'mousedown',this.cachedMousedownListener);Event.stopObserving(this.imageId,'mousedown',this.noOp);Event.observe(this.imageId,'mousedown',this.cachedMouseDownListener);$(this.imageId).style.cursor=null;$(this.imageId+'_waitdiv').hide();break;default:alert("Unknown status '"+status+"'");}};Intermap.prototype.noOp=function(e)
{Event.stop(e);};function append(msg)
{var qqq=document.createElement('div');qqq.innerHTML=msg;document.body.appendChild(qqq);}
var im_bm_wsize0=368;var im_bm_hsize0=276;var im_bm=new Intermap(im_bm_wsize0,im_bm_hsize0,'im_bm_image');im_bm.setSize=function(w,h)
{this.width=parseInt(w);this.height=parseInt(h);if($('im_bm_image'))
{$('im_bm_image').style.width=w+'px';$('im_bm_image').style.height=h+'px';$('im_mapContainer').style.width=(im_bm.width+2)+'px';$('im_mapContainer').style.height=(im_bm.height+2)+'px';$('im_map').style.width=w+'px';$('im_map').style.height=h+'px';$('im_bm_image_waitdiv').style.width=w+'px';}};function setTool(tool)
{im_bm.setTool(tool);}
function im_bm_fullExtent()
{im_bm.fullExtent();}
function im_bm_refresh()
{im_bm.rebuild();}
var startX,startY;var startOffsetX;var startOffsetY;im_bm.afterToolSet=function(tool)
{$('intermap_root').className=tool;};im_bm.beforeMouseDown=function(e)
{removeMarkerBox();};im_bm.unresolvedMouseDown=function(e)
{switch(this.tool)
{case'identify':identify(e);break;case'mark':im_setMark(e);break;}};function identify(e)
{Event.stop(e);var offset=Position.cumulativeOffset($(im_bm.imageId));var offsetX=offset[0];var offsetY=offset[1];pointerX=Event.pointerX(e);pointerY=Event.pointerY(e);var url=getIMServiceURL('map.identify');var t1=pointerX-offsetX;var t2=pointerY-offsetY;var pars='mapimgx='+t1+'&mapimgy='+t2+"&width="+im_bm.width+"&height="+im_bm.height+"&"+im_bm.getURLbbox()+'&activeLayer='+activeLayerId+"&format=text%2Fhtml";window.open(url+"?"+pars,"Queryresult","width=600,height=400,scrollbars=yes,toolbar=no,status=yes,menubar=no,location=yes,resizable=yes");}
function im_bm_resizeStart(e)
{Event.stop(e);Event.observe(document,'mousemove',im_bm_resizeMove);Event.observe(document,'mouseup',im_bm_resizeStop);var offset=Position.cumulativeOffset($('im_bm_image'));var offsetX=offset[0];var offsetY=offset[1];startX=offsetX+1;startY=offsetY+1;var resizebox=document.createElement('div');resizebox.setAttribute('id','im_bm_resizebox');MapUtils.drawBox(resizebox,startX,startY,$('im_bm_image').clientWidth,$('im_bm_image').clientHeight);document.body.appendChild(resizebox);var resizeGhost=document.createElement('img');resizeGhost.id='im_bm_resizeGhost';resizeGhost.src=$('im_bm_image').src;MapUtils.drawBox(resizeGhost,startX,startY,$('im_bm_image').clientWidth,$('im_bm_image').clientHeight);document.body.appendChild(resizeGhost);}
function im_bm_resizeMove(e)
{Event.stop(e);var pX=Event.pointerX(e);var pY=Event.pointerY(e);var offset=Position.cumulativeOffset($('im_bm_image'));var offsetX=offset[0];var offsetY=offset[1];pX=Math.max(pX,offsetX+250);pY=Math.max(pY,offsetY+200);var windowsize=getWindowSize();var winw=windowsize[0];var winh=windowsize[1];pX=Math.min(pX,winw-im_layer_width-30);pY=Math.min(pY,800);MapUtils.drawBox($('im_bm_resizebox'),offsetX+1,offsetY+1,Math.abs(pX-startX),Math.abs(pY-startY));MapUtils.drawBox($('im_bm_resizeGhost'),offsetX+1,offsetY+1,Math.abs(pX-startX),Math.abs(pY-startY));}
function im_bm_resizeStop(e)
{var pX=Event.pointerX(e);var pY=Event.pointerY(e);var offset=Position.cumulativeOffset($(im_bm.imageId));var offsetX=offset[0];var offsetY=offset[1];var w=$('im_bm_resizebox').clientWidth;var h=$('im_bm_resizebox').clientHeight;Event.stopObserving(document,'mousemove',im_bm_resizeMove);Event.stopObserving(document,'mouseup',im_bm_resizeStop);Element.remove($('im_bm_resizeGhost'));Element.remove($('im_bm_resizebox'));im_bm.setSize(w,h);im_bm.rebuild();}
function im_bm_setScale()
{imc_bm_setScale(im_bm.width,im_bm.height,im_bm.getURLbbox(),$('im_setscale').value);}
function imc_bm_setScale(w,h,bbox,scale)
{var pars="width="+w+"&height="+h+"&"+bbox+"&scale="+scale;im_bm.setStatus('busy');var myAjax=new Ajax.Request(getIMServiceURL('map.setScale'),{method:'get',parameters:pars,onComplete:im_bm.imageRebuilt.bindAsEventListener(im_bm),onFailure:reportError});}
function reportError(request)
{alert('Sorry. There was an error.');alert(request.responseXML);}
function showResponse(originalRequest)
{alert(originalRequest.responseText);}
function deleteChildNodes(target)
{while(target.childNodes.length>0){target.removeChild(target.childNodes[target.childNodes.length-1]);}}
im_bm.afterImageRebuilt=function(req)
{var scale=req.responseXML.getElementsByTagName('scale')[0].firstChild.nodeValue;deleteChildNodes($('im_scale'));$('im_scale').appendChild(document.createTextNode('1:'+scale));$('im_currentscale').innerHTML='1:'+scale;$('im_setscale').selectedIndex=0;im_redrawMarkers(req.responseXML);};