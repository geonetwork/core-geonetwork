if(!window.Modalbox)
var Modalbox=new Object();Modalbox.Methods={overrideAlert:false,focusableElements:new Array,currFocused:0,initialized:false,active:true,options:{title:"ModalBox Window",overlayClose:true,width:500,height:90,overlayOpacity:.65,overlayDuration:.25,slideDownDuration:.5,slideUpDuration:.5,resizeDuration:.25,inactiveFade:true,transitions:false,loadingString:"Please wait. Loading...",closeString:"Close window",closeValue:"&times;",params:{},method:'get',autoFocusing:true,aspnet:false},_options:new Object,setOptions:function(options){Object.extend(this.options,options||{});},_init:function(options){Object.extend(this._options,this.options);this.setOptions(options);this.MBoverlay=new Element("div",{id:"MB_overlay",opacity:"0"});this.MBwindow=new Element("div",{id:"MB_window",style:"display: none"}).update(this.MBframe=new Element("div",{id:"MB_frame"}).update(this.MBheader=new Element("div",{id:"MB_header"}).update(this.MBcaption=new Element("div",{id:"MB_caption"}))));this.MBclose=new Element("a",{id:"MB_close",title:this.options.closeString,href:"#"}).update("<span>"+this.options.closeValue+"</span>");this.MBheader.insert({'bottom':this.MBclose});this.MBcontent=new Element("div",{id:"MB_content"}).update(this.MBloading=new Element("div",{id:"MB_loading"}).update(this.options.loadingString));this.MBframe.insert({'bottom':this.MBcontent});var injectToEl=this.options.aspnet?$(document.body).down('form'):$(document.body);injectToEl.insert({'top':this.MBwindow});injectToEl.insert({'top':this.MBoverlay});this.initScrollX=window.pageXOffset||document.body.scrollLeft||document.documentElement.scrollLeft;this.initScrollY=window.pageYOffset||document.body.scrollTop||document.documentElement.scrollTop;this.hideObserver=this._hide.bindAsEventListener(this);this.kbdObserver=this._kbdHandler.bindAsEventListener(this);this._initObservers();this.initialized=true;},show:function(content,options){if(!this.initialized)this._init(options);this.content=content;this.setOptions(options);if(this.options.title)
$(this.MBcaption).update(this.options.title);else{$(this.MBheader).hide();$(this.MBcaption).hide();}
if(this.MBwindow.style.display=="none"){this._appear();this.event("onShow");}
else{this._update();this.event("onUpdate");}},hide:function(options){if(this.initialized){if(options&&typeof options.element!='function')Object.extend(this.options,options);this.event("beforeHide");if(this.options.transitions)
Effect.SlideUp(this.MBwindow,{duration:this.options.slideUpDuration,transition:Effect.Transitions.sinoidal,afterFinish:this._deinit.bind(this)});else{$(this.MBwindow).hide();this._deinit();}}else throw("Modalbox is not initialized.");},_hide:function(event){event.stop();if(event.element().id=='MB_overlay'&&!this.options.overlayClose)return false;this.hide();},alert:function(message){var html='<div class="MB_alert"><p>'+message+'</p><input type="button" onclick="Modalbox.hide()" value="OK" /></div>';Modalbox.show(html,{title:'Alert: '+document.title,width:300});},_appear:function(){if(Prototype.Browser.IE&&!navigator.appVersion.match(/\b7.0\b/)){window.scrollTo(0,0);this._prepareIE("100%","hidden");}
this._setWidth();this._setPosition();if(this.options.transitions){$(this.MBoverlay).setStyle({opacity:0});new Effect.Fade(this.MBoverlay,{from:0,to:this.options.overlayOpacity,duration:this.options.overlayDuration,afterFinish:function(){new Effect.SlideDown(this.MBwindow,{duration:this.options.slideDownDuration,transition:Effect.Transitions.sinoidal,afterFinish:function(){this._setPosition();this.loadContent();}.bind(this)});}.bind(this)});}else{$(this.MBoverlay).setStyle({opacity:this.options.overlayOpacity});$(this.MBwindow).show();this._setPosition();this.loadContent();}
this._setWidthAndPosition=this._setWidthAndPosition.bindAsEventListener(this);Event.observe(window,"resize",this._setWidthAndPosition);},resize:function(byWidth,byHeight,options){var wHeight=$(this.MBwindow).getHeight();var wWidth=$(this.MBwindow).getWidth();var hHeight=$(this.MBheader).getHeight();var cHeight=$(this.MBcontent).getHeight();var newHeight=((wHeight-hHeight+byHeight)<cHeight)?(cHeight+hHeight-wHeight):byHeight;if(options)this.setOptions(options);if(this.options.transitions){new Effect.ScaleBy(this.MBwindow,byWidth,newHeight,{duration:this.options.resizeDuration,afterFinish:function(){this.event("_afterResize");this.event("afterResize");}.bind(this)});}else{this.MBwindow.setStyle({width:wWidth+byWidth+"px",height:wHeight+newHeight+"px"});if(Prototype.Browser.Gecko){this.MBwindow.setStyle({overflow:'hidden'});}
setTimeout(function(){this.event("_afterResize");this.event("afterResize");}.bind(this),1);}},resizeToContent:function(options){var byHeight=this.options.height-this.MBwindow.offsetHeight;if(byHeight!=0){if(options)this.setOptions(options);Modalbox.resize(0,byHeight);}},resizeToInclude:function(element,options){var el=$(element);var elHeight=el.getHeight()+parseInt(el.getStyle('margin-top'))+parseInt(el.getStyle('margin-bottom'))+parseInt(el.getStyle('border-top-width'))+parseInt(el.getStyle('border-bottom-width'));if(elHeight>0){if(options)this.setOptions(options);Modalbox.resize(0,elHeight);}},_update:function(){$(this.MBcontent).update("");this.MBcontent.appendChild(this.MBloading);$(this.MBloading).update(this.options.loadingString);this.currentDims=[this.MBwindow.offsetWidth,this.MBwindow.offsetHeight];Modalbox.resize((this.options.width-this.currentDims[0]),(this.options.height-this.currentDims[1]),{_afterResize:this._loadAfterResize.bind(this)});},loadContent:function(){if(this.event("beforeLoad")!=false){if(typeof this.content=='string'){var htmlRegExp=new RegExp(/<\/?[^>]+>/gi);if(htmlRegExp.test(this.content)){this._insertContent(this.content.stripScripts());this._putContent(function(){this.content.extractScripts().map(function(script){return eval(script.replace("<!--","").replace("// -->",""));}.bind(window));}.bind(this));}else
new Ajax.Request(this.content,{method:this.options.method.toLowerCase(),parameters:this.options.params,onSuccess:function(transport){var response=new String(transport.responseText);this._insertContent(transport.responseText.stripScripts());this._putContent(function(){response.extractScripts().map(function(script){return eval(script.replace("<!--","").replace("// -->",""));}.bind(window));});}.bind(this),onException:function(instance,exception){Modalbox.hide();throw('Modalbox Loading Error: '+exception);}});}else if(typeof this.content=='object'){this._insertContent(this.content);this._putContent();}else{Modalbox.hide();throw('Modalbox Parameters Error: Please specify correct URL or HTML element (plain HTML or object)');}}},_insertContent:function(content){$(this.MBcontent).hide().update("");if(typeof content=='string'){setTimeout(function(){this.MBcontent.update(content);}.bind(this),1);}else if(typeof content=='object'){var _htmlObj=content.cloneNode(true);if(content.id)content.id="MB_"+content.id;$(content).select('*[id]').each(function(el){el.id="MB_"+el.id;});this.MBcontent.appendChild(_htmlObj);this.MBcontent.down().show();if(Prototype.Browser.IE)
$$("#MB_content select").invoke('setStyle',{'visibility':''});}},_putContent:function(callback){if(this.options.height==this._options.height){setTimeout(function(){Modalbox.resize(0,$(this.MBcontent).getHeight()-$(this.MBwindow).getHeight()+$(this.MBheader).getHeight(),{afterResize:function(){this.MBcontent.show().makePositioned();this.focusableElements=this._findFocusableElements();this._setFocus();setTimeout(function(){if(callback!=undefined)
callback();this.event("afterLoad");}.bind(this),1);}.bind(this)});}.bind(this),1);}else{setTimeout(function(){this._setWidth();if(Prototype.Browser.Gecko){this.MBwindow.setStyle({overflow:'hidden'});}
this.MBcontent.setStyle({overflow:'auto',height:$(this.MBwindow).getHeight()-$(this.MBheader).getHeight()-20+'px'});this.MBcontent.show();this.focusableElements=this._findFocusableElements();this._setFocus();if(callback!=undefined)
callback();this.event("afterLoad");}.bind(this),1);}},activate:function(options){this.setOptions(options);this.active=true;$(this.MBclose).observe("click",this.hideObserver);if(this.options.overlayClose)
$(this.MBoverlay).observe("click",this.hideObserver);$(this.MBclose).show();if(this.options.transitions&&this.options.inactiveFade)
new Effect.Appear(this.MBwindow,{duration:this.options.slideUpDuration});},deactivate:function(options){this.setOptions(options);this.active=false;$(this.MBclose).stopObserving("click",this.hideObserver);if(this.options.overlayClose)
$(this.MBoverlay).stopObserving("click",this.hideObserver);$(this.MBclose).hide();if(this.options.transitions&&this.options.inactiveFade)
new Effect.Fade(this.MBwindow,{duration:this.options.slideUpDuration,to:.75});},_initObservers:function(){$(this.MBclose).observe("click",this.hideObserver);if(this.options.overlayClose)
$(this.MBoverlay).observe("click",this.hideObserver);if(Prototype.Browser.IE)
Event.observe(document,"keydown",this.kbdObserver);else
Event.observe(document,"keypress",this.kbdObserver);},_removeObservers:function(){$(this.MBclose).stopObserving("click",this.hideObserver);if(this.options.overlayClose)
$(this.MBoverlay).stopObserving("click",this.hideObserver);if(Prototype.Browser.IE)
Event.stopObserving(document,"keydown",this.kbdObserver);else
Event.stopObserving(document,"keypress",this.kbdObserver);},_loadAfterResize:function(){this._setWidth();this._setPosition();this.loadContent();},_setFocus:function(){if(this.focusableElements.length>0&&this.options.autoFocusing==true){var firstEl=this.focusableElements.find(function(el){return el.tabIndex==1;})||this.focusableElements.first();this.currFocused=this.focusableElements.toArray().indexOf(firstEl);firstEl.focus();}else if($(this.MBclose).visible())
$(this.MBclose).focus();},_findFocusableElements:function(){var mycontent=[];var content=this.MBcontent.descendants();for(var index=0,len=content.length;index<len;++index){var elem=content[index];if(["textarea","select","button"].include(elem.tagName.toLowerCase())){mycontent.push(elem);}else if(elem.tagName.toLowerCase()=="input"&&elem.visible()&&elem.type!="hidden"){mycontent.push(elem);}else if(elem.tagName.toLowerCase()=="a"&&elem.href){mycontent.push(elem);}}
mycontent.invoke('addClassName','MB_focusable');return mycontent;},_kbdHandler:function(event){var node=event.element();switch(event.keyCode){case Event.KEY_TAB:event.stop();if(node!=this.focusableElements[this.currFocused])
this.currFocused=this.focusableElements.toArray().indexOf(node);if(!event.shiftKey){if(this.currFocused==this.focusableElements.length-1){if(this.focusableElements.first()!=null){this.focusableElements.first().focus();}
this.currFocused=0;}else{this.currFocused++;this.focusableElements[this.currFocused].focus();}}else{if(this.currFocused==0){this.focusableElements.last().focus();this.currFocused=this.focusableElements.length-1;}else{this.currFocused--;this.focusableElements[this.currFocused].focus();}}
break;case Event.KEY_ESC:if(this.active)this._hide(event);break;case 32:this._preventScroll(event);break;case 0:if(event.which==32)this._preventScroll(event);break;case Event.KEY_UP:case Event.KEY_DOWN:case Event.KEY_PAGEDOWN:case Event.KEY_PAGEUP:case Event.KEY_HOME:case Event.KEY_END:if(Prototype.Browser.WebKit&&!["textarea","select"].include(node.tagName.toLowerCase()))
event.stop();else if((node.tagName.toLowerCase()=="input"&&["submit","button"].include(node.type))||(node.tagName.toLowerCase()=="a"))
event.stop();break;}},_preventScroll:function(event){if(!["input","textarea","select","button"].include(event.element().tagName.toLowerCase()))
event.stop();},_deinit:function()
{this._removeObservers();Event.stopObserving(window,"resize",this._setWidthAndPosition);if(this.options.transitions){Effect.toggle(this.MBoverlay,'appear',{duration:this.options.overlayDuration,afterFinish:this._removeElements.bind(this)});}else{this.MBoverlay.hide();this._removeElements();}
$(this.MBcontent).setStyle({overflow:'',height:''});},_removeElements:function(){$(this.MBoverlay).remove();$(this.MBwindow).remove();if(Prototype.Browser.IE&&!navigator.appVersion.match(/\b7.0\b/)){this._prepareIE("","");window.scrollTo(this.initScrollX,this.initScrollY);}
if(typeof this.content=='object'){if(this.content.id&&this.content.id.match(/MB_/)){this.content.id=this.content.id.replace(/MB_/,"");}
this.content.select('*[id]').each(function(el){el.id=el.id.replace(/MB_/,"");});}
this.initialized=false;this.event("afterHide");this.setOptions(this._options);},_setWidth:function(){$(this.MBwindow).setStyle({width:this.options.width+"px",height:this.options.height+"px"});},_setPosition:function(){$(this.MBwindow).setStyle({left:Math.round((Element.getWidth(document.body)-Element.getWidth(this.MBwindow))/2)+"px"});},_setWidthAndPosition:function(){$(this.MBwindow).setStyle({width:this.options.width+"px"});this._setPosition();},_getScrollTop:function(){var theTop;if(document.documentElement&&document.documentElement.scrollTop)
theTop=document.documentElement.scrollTop;else if(document.body)
theTop=document.body.scrollTop;return theTop;},_prepareIE:function(height,overflow){$$('html, body').invoke('setStyle',{width:height,height:height,overflow:overflow});$$("select").invoke('setStyle',{'visibility':overflow});},event:function(eventName){if(this.options[eventName]){var returnValue=this.options[eventName]();this.options[eventName]=null;if(returnValue!=undefined)
return returnValue;else
return true;}
return true;}};Object.extend(Modalbox,Modalbox.Methods);if(Modalbox.overrideAlert)window.alert=Modalbox.alert;Effect.ScaleBy=Class.create();Object.extend(Object.extend(Effect.ScaleBy.prototype,Effect.Base.prototype),{initialize:function(element,byWidth,byHeight,options){this.element=$(element)
var options=Object.extend({scaleFromTop:true,scaleMode:'box',scaleByWidth:byWidth,scaleByHeight:byHeight},arguments[3]||{});this.start(options);},setup:function(){this.elementPositioning=this.element.getStyle('position');this.originalTop=this.element.offsetTop;this.originalLeft=this.element.offsetLeft;this.dims=null;if(this.options.scaleMode=='box')
this.dims=[this.element.offsetHeight,this.element.offsetWidth];if(/^content/.test(this.options.scaleMode))
this.dims=[this.element.scrollHeight,this.element.scrollWidth];if(!this.dims)
this.dims=[this.options.scaleMode.originalHeight,this.options.scaleMode.originalWidth];this.deltaY=this.options.scaleByHeight;this.deltaX=this.options.scaleByWidth;},update:function(position){var currentHeight=this.dims[0]+(this.deltaY*position);var currentWidth=this.dims[1]+(this.deltaX*position);currentHeight=(currentHeight>0)?currentHeight:0;currentWidth=(currentWidth>0)?currentWidth:0;this.setDimensions(currentHeight,currentWidth);},setDimensions:function(height,width){var d={};d.width=width+'px';d.height=height+'px';var topd=Math.round((height-this.dims[0])/2);var leftd=Math.round((width-this.dims[1])/2);if(this.elementPositioning=='absolute'||this.elementPositioning=='fixed'){if(!this.options.scaleFromTop)d.top=this.originalTop-topd+'px';d.left=this.originalLeft-leftd+'px';}else{if(!this.options.scaleFromTop)d.top=-topd+'px';d.left=-leftd+'px';}
this.element.setStyle(d);}});var getGNServiceURL=function(service){return Env.locService+"/"+service;};function init(){};function translate(text){return translations[text]||text;};function get_cookie(cookie_name)
{var results=document.cookie.match(cookie_name+'=(.*?)(;|$)');if(results)
return(unescape(results[1]));else
return null;};function popNew(a)
{msgWindow=window.open(a,"displayWindow","location=no, toolbar=no, directories=no, status=no, menubar=no, scrollbars=yes, resizable=yes, width=800, height=600")
msgWindow.focus()}
function openPage(what,type)
{msgWindow=window.open(what,type,"location=yes, toolbar=yes, directories=yes, status=yes, menubar=yes, scrollbars=yes, resizable=yes, width=800, height=600")
msgWindow.focus()}
function popFeedback(a)
{msgWindow=window.open(a,"feedbackWindow","location=no, toolbar=no, directories=no, status=no, menubar=no, scrollbars=yes, resizable=yes, width=800, height=600")
msgWindow.focus()}
function popWindow(a)
{msgWindow=window.open(a,"popWindow","location=no, toolbar=no, directories=no, status=no, menubar=no, scrollbars=yes, resizable=yes, width=800, height=600")
msgWindow.focus()}
function popInterMap(a)
{msgWindow=window.open(a,"InterMap","location=no, toolbar=no, directories=no, status=no, menubar=no, scrollbars=yes, resizable=yes, width=800, height=600")
msgWindow.focus()}
function goSubmit(form_name){document.forms[form_name].submit();}
function goReset(form_name)
{document.forms[form_name].reset();}
function entSub(form_name){if(window.event&&window.event.keyCode==13)
goSubmit(form_name);else
return true;}
function goBack()
{history.back();}
function processCancel(){document.close();}
function load(url)
{document.location.href=url;}
function doConfirm(url,message)
{if(confirm(message))
{load(url);return true;}
return false;}
function feedbackSubmit()
{var f=$('feedbackf');if(isWhitespace(f.comments.value)){f.comments.value=translate('noComment');}
if(isWhitespace(f.name.value)||isWhitespace(f.org.value)){alert(translate("addName"));return;}else if(!isEmail(f.email.value)){alert(translate("checkEmail"));return;}
Modalbox.show(getGNServiceURL('file.download'),{height:400,width:600,params:f.serialize(true)});}
function doDownload(id,all){var list=$('downloadlist').getElementsByTagName('INPUT');var pars='&id='+id+'&access=private';var selected=false;for(var i=0;i<list.length;i++){if(list[i].checked||all!=null){selected=true;var name=list[i].getAttribute('name');pars+='&fname='+name;}}
if(!selected){alert(translate("selectOneFile"));return;}
Modalbox.show(getGNServiceURL('file.disclaimer')+"?"+pars,{height:400,width:600});}
function massiveOperation(service,title,width,message)
{if(message!=null){if(!confirm(message))
return;}
var url=Env.locService+'/'+service;Modalbox.show(url,{title:title,width:width,afterHide:function(){$('search-results-content').hide();}});}
function oActionsInit(name,id){if(id===undefined){id="";}
$(name+'Ele'+id).style.width=$(name+id).getWidth();$(name+'Ele'+id).style.top=$(name+id).positionedOffset().top+$(name+id).getHeight();$(name+'Ele'+id).style.left=$(name+id).positionedOffset().left;}
function oActions(name,id){var on="../../images/plus.gif";var off="../../images/minus.png";if(id===undefined){id="";}
if(!$(name+'Ele'+id).style.top)
oActionsInit(name,id);if($(name+'Ele'+id).style.display=='none'){$(name+'Ele'+id).style.display='block';$(name+'Img'+id).src=off;}else{$(name+'Ele'+id).style.display='none';$(name+'Img'+id).src=on;}}
function actionOnSelect(msg){if($('nbselected').innerHTML==0&&$('oAcOsEle').style.display=='none'){alert(msg);}else{oActions('oAcOs');}}
function checkMassiveNewOwner(action,title){if($('user').value==''){alert(translate("selectNewOwner"));return false;}
if($('group').value==''){alert(translate("selectOwnerGroup"));return false;}
Modalbox.show(getGNServiceURL(action),{title:title,params:$('massivenewowner').serialize(true),afterHide:function(){$('search-results-content').hide();}});}
function addGroups(xmlRes){var list=xml.children(xmlRes,'group');$('group').options.length=0;for(var i=0;i<list.length;i++){var id=xml.evalXPath(list[i],'id');var name=xml.evalXPath(list[i],'name');var opt=document.createElement('option');opt.text=name;opt.value=id;if(list.length==1)opt.selected=true;$('group').options.add(opt);}}
function addGroupsCallback_OK(xmlRes){if(xmlRes.nodeName=='error'){ker.showError(translate('cannotRetrieveGroup'),xmlRes);$('group').options.length=0;$('group').value='';var user=$('user');for(i=0;i<user.options.length;i++){user.options[i].selected=false;}}else{addGroups(xmlRes);}}
function doGroups(userid){var request=ker.createRequest('id',userid);ker.send('xml.usergroups.list',request,addGroupsCallback_OK);}
function processRegSub(url)
{var invalid=" ";var minLength=6;if(document.userregisterform.name.value.length==0){alert(translate('firstNameMandatory'));return;}
if(isWhitespace(document.userregisterform.name.value)){alert(translate('firstNameMandatory'));return;}
if(document.userregisterform.name.value.indexOf(invalid)>-1){alert(translate('spacesNot'));return;}
if(document.userregisterform.surname.value.length==0){alert(translate('lastNameMandatory'));return;}
if(isWhitespace(document.userregisterform.surname.value)){alert(translate('lastNameMandatory'));return;}
if(document.userregisterform.surname.value.indexOf(invalid)>-1){alert(translate('spacesNot'));return;}
if(!isEmail(document.userregisterform.email.value)){alert(translate('emailAddressInvalid'));return;}
var myAjax=new Ajax.Request(getGNServiceURL(url),{method:'post',parameters:$('userregisterform').serialize(true),onSuccess:function(req){var output=req.responseText;var title=translate('yourRegistration');Modalbox.show(output,{title:title,width:300});},onFailure:function(req){alert(translate("registrationFailed")+" "+req.responseText+" status: "+req.status+" - "+translate("tryAgain"));}});}
function displayBox(content,contentDivId,modal){var id=contentDivId+"Box";var w=Ext.getCmp(id);if(w==undefined){w=new Ext.Window({title:translate(contentDivId),id:id,layout:'fit',modal:modal,constrain:true,width:400,collapsible:(modal?false:true),autoScroll:true,iconCls:contentDivId+'Icon',closeAction:'hide',onEsc:'hide',listeners:{hide:function(){this.hide();}},contentEl:contentDivId});}
if(w){if(content!=null){$(contentDivId).innerHTML='';$(contentDivId).innerHTML=content;$(contentDivId).style.display='block'}
w.show();w.setHeight(345);w.anchorTo(Ext.getBody(),(modal?'c-c':'tr-tr'));}}