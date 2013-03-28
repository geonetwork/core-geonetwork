/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.StoreMgr=Ext.apply(new Ext.util.MixedCollection(),{register:function(){for(var i=0,s;s=arguments[i];i++){this.add(s);}},unregister:function(){for(var i=0,s;s=arguments[i];i++){this.remove(this.lookup(s));}},lookup:function(id){return typeof id=="object"?id:this.get(id);},getKey:function(o){return o.storeId||o.id;}});