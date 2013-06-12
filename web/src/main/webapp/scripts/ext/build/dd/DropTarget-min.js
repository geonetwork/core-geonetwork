/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.dd.DropTarget=function(el,config){this.el=Ext.get(el);Ext.apply(this,config);if(this.containerScroll){Ext.dd.ScrollManager.register(this.el);}
Ext.dd.DropTarget.superclass.constructor.call(this,this.el.dom,this.ddGroup||this.group,{isTarget:true});};Ext.extend(Ext.dd.DropTarget,Ext.dd.DDTarget,{dropAllowed:"x-dd-drop-ok",dropNotAllowed:"x-dd-drop-nodrop",isTarget:true,isNotifyTarget:true,notifyEnter:function(dd,e,data){if(this.overClass){this.el.addClass(this.overClass);}
return this.dropAllowed;},notifyOver:function(dd,e,data){return this.dropAllowed;},notifyOut:function(dd,e,data){if(this.overClass){this.el.removeClass(this.overClass);}},notifyDrop:function(dd,e,data){return false;}});