/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.data.HttpProxy=function(conn){Ext.data.HttpProxy.superclass.constructor.call(this);this.conn=conn;this.useAjax=!conn||!conn.events;};Ext.extend(Ext.data.HttpProxy,Ext.data.DataProxy,{getConnection:function(){return this.useAjax?Ext.Ajax:this.conn;},load:function(params,reader,callback,scope,arg){if(this.fireEvent("beforeload",this,params)!==false){var o={params:params||{},request:{callback:callback,scope:scope,arg:arg},reader:reader,callback:this.loadResponse,scope:this};if(this.useAjax){Ext.applyIf(o,this.conn);if(this.activeRequest){Ext.Ajax.abort(this.activeRequest);}
this.activeRequest=Ext.Ajax.request(o);}else{this.conn.request(o);}}else{callback.call(scope||this,null,arg,false);}},loadResponse:function(o,success,response){delete this.activeRequest;if(!success){this.fireEvent("loadexception",this,o,response);o.request.callback.call(o.request.scope,null,o.request.arg,false);return;}
var result;try{result=o.reader.read(response);}catch(e){this.fireEvent("loadexception",this,o,response,e);o.request.callback.call(o.request.scope,null,o.request.arg,false);return;}
this.fireEvent("load",this,o,o.request.arg);o.request.callback.call(o.request.scope,result,o.request.arg,true);},update:function(dataSet){},updateResponse:function(dataSet){},destroy:function(){if(!this.useAjax){this.conn.abort();}else if(this.activeRequest){Ext.Ajax.abort(this.activeRequest);}
Ext.data.HttpProxy.superclass.destroy.call(this);}});