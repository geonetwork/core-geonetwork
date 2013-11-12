/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.menu.Separator=function(config){Ext.menu.Separator.superclass.constructor.call(this,config);};Ext.extend(Ext.menu.Separator,Ext.menu.BaseItem,{itemCls:"x-menu-sep",hideOnClick:false,onRender:function(li){var s=document.createElement("span");s.className=this.itemCls;s.innerHTML="&#160;";this.el=s;li.addClass("x-menu-sep-li");Ext.menu.Separator.superclass.onRender.apply(this,arguments);}});