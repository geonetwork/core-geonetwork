/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.Viewport=Ext.extend(Ext.Container,{initComponent:function(){Ext.Viewport.superclass.initComponent.call(this);document.getElementsByTagName('html')[0].className+=' x-viewport';this.el=Ext.getBody();this.el.setHeight=Ext.emptyFn;this.el.setWidth=Ext.emptyFn;this.el.setSize=Ext.emptyFn;this.el.dom.scroll='no';this.allowDomMove=false;this.autoWidth=true;this.autoHeight=true;Ext.EventManager.onWindowResize(this.fireResize,this);this.renderTo=this.el;},fireResize:function(w,h){this.fireEvent('resize',this,w,h,w,h);}});Ext.reg('viewport',Ext.Viewport);