/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.menu.Adapter=function(component,config){Ext.menu.Adapter.superclass.constructor.call(this,config);this.component=component;};Ext.extend(Ext.menu.Adapter,Ext.menu.BaseItem,{canActivate:true,onRender:function(container,position){this.component.render(container);this.el=this.component.getEl();},activate:function(){if(this.disabled){return false;}
this.component.focus();this.fireEvent("activate",this);return true;},deactivate:function(){this.fireEvent("deactivate",this);},disable:function(){this.component.disable();Ext.menu.Adapter.superclass.disable.call(this);},enable:function(){this.component.enable();Ext.menu.Adapter.superclass.enable.call(this);}});