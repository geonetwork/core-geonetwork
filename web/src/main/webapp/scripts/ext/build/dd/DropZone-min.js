/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.dd.DropZone=function(el,config){Ext.dd.DropZone.superclass.constructor.call(this,el,config);};Ext.extend(Ext.dd.DropZone,Ext.dd.DropTarget,{getTargetFromEvent:function(e){return Ext.dd.Registry.getTargetFromEvent(e);},onNodeEnter:function(n,dd,e,data){},onNodeOver:function(n,dd,e,data){return this.dropAllowed;},onNodeOut:function(n,dd,e,data){},onNodeDrop:function(n,dd,e,data){return false;},onContainerOver:function(dd,e,data){return this.dropNotAllowed;},onContainerDrop:function(dd,e,data){return false;},notifyEnter:function(dd,e,data){return this.dropNotAllowed;},notifyOver:function(dd,e,data){var n=this.getTargetFromEvent(e);if(!n){if(this.lastOverNode){this.onNodeOut(this.lastOverNode,dd,e,data);this.lastOverNode=null;}
return this.onContainerOver(dd,e,data);}
if(this.lastOverNode!=n){if(this.lastOverNode){this.onNodeOut(this.lastOverNode,dd,e,data);}
this.onNodeEnter(n,dd,e,data);this.lastOverNode=n;}
return this.onNodeOver(n,dd,e,data);},notifyOut:function(dd,e,data){if(this.lastOverNode){this.onNodeOut(this.lastOverNode,dd,e,data);this.lastOverNode=null;}},notifyDrop:function(dd,e,data){if(this.lastOverNode){this.onNodeOut(this.lastOverNode,dd,e,data);this.lastOverNode=null;}
var n=this.getTargetFromEvent(e);return n?this.onNodeDrop(n,dd,e,data):this.onContainerDrop(dd,e,data);},triggerCacheRefresh:function(){Ext.dd.DDM.refreshCache(this.groups);}});