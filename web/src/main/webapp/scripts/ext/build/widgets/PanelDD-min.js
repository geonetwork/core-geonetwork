/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.dd.PanelProxy=function(panel,config){this.panel=panel;this.id=this.panel.id+'-ddproxy';Ext.apply(this,config);};Ext.dd.PanelProxy.prototype={insertProxy:true,setStatus:Ext.emptyFn,reset:Ext.emptyFn,update:Ext.emptyFn,stop:Ext.emptyFn,sync:Ext.emptyFn,getEl:function(){return this.ghost;},getGhost:function(){return this.ghost;},getProxy:function(){return this.proxy;},hide:function(){if(this.ghost){if(this.proxy){this.proxy.remove();delete this.proxy;}
this.panel.el.dom.style.display='';this.ghost.remove();delete this.ghost;}},show:function(){if(!this.ghost){this.ghost=this.panel.createGhost(undefined,undefined,Ext.getBody());this.ghost.setXY(this.panel.el.getXY())
if(this.insertProxy){this.proxy=this.panel.el.insertSibling({cls:'x-panel-dd-spacer'});this.proxy.setSize(this.panel.getSize());}
this.panel.el.dom.style.display='none';}},repair:function(xy,callback,scope){this.hide();if(typeof callback=="function"){callback.call(scope||this);}},moveProxy:function(parentNode,before){if(this.proxy){parentNode.insertBefore(this.proxy.dom,before);}}};Ext.Panel.DD=function(panel,cfg){this.panel=panel;this.dragData={panel:panel};this.proxy=new Ext.dd.PanelProxy(panel,cfg);Ext.Panel.DD.superclass.constructor.call(this,panel.el,cfg);var h=panel.header;if(h){this.setHandleElId(h.id);}
(h?h:this.panel.body).setStyle('cursor','move');this.scroll=false;};Ext.extend(Ext.Panel.DD,Ext.dd.DragSource,{showFrame:Ext.emptyFn,startDrag:Ext.emptyFn,b4StartDrag:function(x,y){this.proxy.show();},b4MouseDown:function(e){var x=e.getPageX();var y=e.getPageY();this.autoOffset(x,y);},onInitDrag:function(x,y){this.onStartDrag(x,y);return true;},createFrame:Ext.emptyFn,getDragEl:function(e){return this.proxy.ghost.dom;},endDrag:function(e){this.proxy.hide();this.panel.saveState();},autoOffset:function(x,y){x-=this.startPageX;y-=this.startPageY;this.setDelta(x,y);}});