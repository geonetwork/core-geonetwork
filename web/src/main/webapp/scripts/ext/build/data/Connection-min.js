/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.data.Connection=function(config){Ext.apply(this,config);this.addEvents("beforerequest","requestcomplete","requestexception");Ext.data.Connection.superclass.constructor.call(this);};Ext.extend(Ext.data.Connection,Ext.util.Observable,{timeout:30000,autoAbort:false,disableCaching:true,disableCachingParam:'_dc',request:function(o){if(this.fireEvent("beforerequest",this,o)!==false){var p=o.params;if(typeof p=="function"){p=p.call(o.scope||window,o);}
if(typeof p=="object"){p=Ext.urlEncode(p);}
if(this.extraParams){var extras=Ext.urlEncode(this.extraParams);p=p?(p+'&'+extras):extras;}
var url=o.url||this.url;if(typeof url=='function'){url=url.call(o.scope||window,o);}
if(o.form){var form=Ext.getDom(o.form);url=url||form.action;var enctype=form.getAttribute("enctype");if(o.isUpload||(enctype&&enctype.toLowerCase()=='multipart/form-data')){return this.doFormUpload(o,p,url);}
var f=Ext.lib.Ajax.serializeForm(form);p=p?(p+'&'+f):f;}
var hs=o.headers;if(this.defaultHeaders){hs=Ext.apply(hs||{},this.defaultHeaders);if(!o.headers){o.headers=hs;}}
var cb={success:this.handleResponse,failure:this.handleFailure,scope:this,argument:{options:o},timeout:o.timeout||this.timeout};var method=o.method||this.method||((p||o.xmlData||o.jsonData)?"POST":"GET");if(method=='GET'&&(this.disableCaching&&o.disableCaching!==false)||o.disableCaching===true){var dcp=o.disableCachingParam||this.disableCachingParam;url+=(url.indexOf('?')!=-1?'&':'?')+dcp+'='+(new Date().getTime());}
if(typeof o.autoAbort=='boolean'){if(o.autoAbort){this.abort();}}else if(this.autoAbort!==false){this.abort();}
if((method=='GET'||o.xmlData||o.jsonData)&&p){url+=(url.indexOf('?')!=-1?'&':'?')+p;p='';}
this.transId=Ext.lib.Ajax.request(method,url,cb,p,o);return this.transId;}else{Ext.callback(o.callback,o.scope,[o,null,null]);return null;}},isLoading:function(transId){if(transId){return Ext.lib.Ajax.isCallInProgress(transId);}else{return this.transId?true:false;}},abort:function(transId){if(transId||this.isLoading()){Ext.lib.Ajax.abort(transId||this.transId);}},handleResponse:function(response){this.transId=false;var options=response.argument.options;response.argument=options?options.argument:null;this.fireEvent("requestcomplete",this,response,options);Ext.callback(options.success,options.scope,[response,options]);Ext.callback(options.callback,options.scope,[options,true,response]);},handleFailure:function(response,e){this.transId=false;var options=response.argument.options;response.argument=options?options.argument:null;this.fireEvent("requestexception",this,response,options,e);Ext.callback(options.failure,options.scope,[response,options]);Ext.callback(options.callback,options.scope,[options,false,response]);},doFormUpload:function(o,ps,url){var id=Ext.id();var frame=document.createElement('iframe');frame.id=id;frame.name=id;frame.className='x-hidden';if(Ext.isIE){frame.src=Ext.SSL_SECURE_URL;}
document.body.appendChild(frame);if(Ext.isIE){document.frames[id].name=id;}
var form=Ext.getDom(o.form),buf={target:form.target,method:form.method,encoding:form.encoding,enctype:form.enctype,action:form.action};form.target=id;form.method='POST';form.enctype=form.encoding='multipart/form-data';if(url){form.action=url;}
var hiddens,hd;if(ps){hiddens=[];ps=Ext.urlDecode(ps,false);for(var k in ps){if(ps.hasOwnProperty(k)){hd=document.createElement('input');hd.type='hidden';hd.name=k;hd.value=ps[k];form.appendChild(hd);hiddens.push(hd);}}}
function cb(){var r={responseText:'',responseXML:null};r.argument=o?o.argument:null;try{var doc;if(Ext.isIE){doc=frame.contentWindow.document;}else{doc=(frame.contentDocument||window.frames[id].document);}
if(doc&&doc.body){r.responseText=doc.body.innerHTML;}
if(doc&&doc.XMLDocument){r.responseXML=doc.XMLDocument;}else{r.responseXML=doc;}}
catch(e){}
Ext.EventManager.removeListener(frame,'load',cb,this);this.fireEvent("requestcomplete",this,r,o);Ext.callback(o.success,o.scope,[r,o]);Ext.callback(o.callback,o.scope,[o,true,r]);setTimeout(function(){Ext.removeNode(frame);},100);}
Ext.EventManager.on(frame,'load',cb,this);form.submit();form.target=buf.target;form.method=buf.method;form.enctype=buf.enctype;form.encoding=buf.encoding;form.action=buf.action;if(hiddens){for(var i=0,len=hiddens.length;i<len;i++){Ext.removeNode(hiddens[i]);}}}});Ext.Ajax=new Ext.data.Connection({autoAbort:false,serializeForm:function(form){return Ext.lib.Ajax.serializeForm(form);}});