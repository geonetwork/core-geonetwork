/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.Action=function(config){this.initialConfig=config;this.items=[];}
Ext.Action.prototype={isAction:true,setText:function(text){this.initialConfig.text=text;this.callEach('setText',[text]);},getText:function(){return this.initialConfig.text;},setIconClass:function(cls){this.initialConfig.iconCls=cls;this.callEach('setIconClass',[cls]);},getIconClass:function(){return this.initialConfig.iconCls;},setDisabled:function(v){this.initialConfig.disabled=v;this.callEach('setDisabled',[v]);},enable:function(){this.setDisabled(false);},disable:function(){this.setDisabled(true);},isDisabled:function(){return this.initialConfig.disabled;},setHidden:function(v){this.initialConfig.hidden=v;this.callEach('setVisible',[!v]);},show:function(){this.setHidden(false);},hide:function(){this.setHidden(true);},isHidden:function(){return this.initialConfig.hidden;},setHandler:function(fn,scope){this.initialConfig.handler=fn;this.initialConfig.scope=scope;this.callEach('setHandler',[fn,scope]);},each:function(fn,scope){Ext.each(this.items,fn,scope);},callEach:function(fnName,args){var cs=this.items;for(var i=0,len=cs.length;i<len;i++){cs[i][fnName].apply(cs[i],args);}},addComponent:function(comp){this.items.push(comp);comp.on('destroy',this.removeComponent,this);},removeComponent:function(comp){this.items.remove(comp);},execute:function(){this.initialConfig.handler.apply(this.initialConfig.scope||window,arguments);}};