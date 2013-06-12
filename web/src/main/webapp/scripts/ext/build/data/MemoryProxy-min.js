/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.data.MemoryProxy=function(data){Ext.data.MemoryProxy.superclass.constructor.call(this);this.data=data;};Ext.extend(Ext.data.MemoryProxy,Ext.data.DataProxy,{load:function(params,reader,callback,scope,arg){params=params||{};var result;try{result=reader.readRecords(this.data);}catch(e){this.fireEvent("loadexception",this,arg,null,e);callback.call(scope,null,arg,false);return;}
callback.call(scope,result,arg,true);},update:function(params,records){}});