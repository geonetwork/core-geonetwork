/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.LoadMask=function(el,config){this.el=Ext.get(el);Ext.apply(this,config);if(this.store){this.store.on('beforeload',this.onBeforeLoad,this);this.store.on('load',this.onLoad,this);this.store.on('loadexception',this.onLoad,this);this.removeMask=Ext.value(this.removeMask,false);}else{var um=this.el.getUpdater();um.showLoadIndicator=false;um.on('beforeupdate',this.onBeforeLoad,this);um.on('update',this.onLoad,this);um.on('failure',this.onLoad,this);this.removeMask=Ext.value(this.removeMask,true);}};Ext.LoadMask.prototype={msg:'Loading...',msgCls:'x-mask-loading',disabled:false,disable:function(){this.disabled=true;},enable:function(){this.disabled=false;},onLoad:function(){this.el.unmask(this.removeMask);},onBeforeLoad:function(){if(!this.disabled){this.el.mask(this.msg,this.msgCls);}},show:function(){this.onBeforeLoad();},hide:function(){this.onLoad();},destroy:function(){if(this.store){this.store.un('beforeload',this.onBeforeLoad,this);this.store.un('load',this.onLoad,this);this.store.un('loadexception',this.onLoad,this);}else{var um=this.el.getUpdater();um.un('beforeupdate',this.onBeforeLoad,this);um.un('update',this.onLoad,this);um.un('failure',this.onLoad,this);}}};