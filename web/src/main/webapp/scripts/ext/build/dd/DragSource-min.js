/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.dd.DragSource=function(el,config){this.el=Ext.get(el);if(!this.dragData){this.dragData={};}
Ext.apply(this,config);if(!this.proxy){this.proxy=new Ext.dd.StatusProxy();}
Ext.dd.DragSource.superclass.constructor.call(this,this.el.dom,this.ddGroup||this.group,{dragElId:this.proxy.id,resizeFrame:false,isTarget:false,scroll:this.scroll===true});this.dragging=false;};Ext.extend(Ext.dd.DragSource,Ext.dd.DDProxy,{dropAllowed:"x-dd-drop-ok",dropNotAllowed:"x-dd-drop-nodrop",getDragData:function(e){return this.dragData;},onDragEnter:function(e,id){var target=Ext.dd.DragDropMgr.getDDById(id);this.cachedTarget=target;if(this.beforeDragEnter(target,e,id)!==false){if(target.isNotifyTarget){var status=target.notifyEnter(this,e,this.dragData);this.proxy.setStatus(status);}else{this.proxy.setStatus(this.dropAllowed);}
if(this.afterDragEnter){this.afterDragEnter(target,e,id);}}},beforeDragEnter:function(target,e,id){return true;},alignElWithMouse:function(){Ext.dd.DragSource.superclass.alignElWithMouse.apply(this,arguments);this.proxy.sync();},onDragOver:function(e,id){var target=this.cachedTarget||Ext.dd.DragDropMgr.getDDById(id);if(this.beforeDragOver(target,e,id)!==false){if(target.isNotifyTarget){var status=target.notifyOver(this,e,this.dragData);this.proxy.setStatus(status);}
if(this.afterDragOver){this.afterDragOver(target,e,id);}}},beforeDragOver:function(target,e,id){return true;},onDragOut:function(e,id){var target=this.cachedTarget||Ext.dd.DragDropMgr.getDDById(id);if(this.beforeDragOut(target,e,id)!==false){if(target.isNotifyTarget){target.notifyOut(this,e,this.dragData);}
this.proxy.reset();if(this.afterDragOut){this.afterDragOut(target,e,id);}}
this.cachedTarget=null;},beforeDragOut:function(target,e,id){return true;},onDragDrop:function(e,id){var target=this.cachedTarget||Ext.dd.DragDropMgr.getDDById(id);if(this.beforeDragDrop(target,e,id)!==false){if(target.isNotifyTarget){if(target.notifyDrop(this,e,this.dragData)){this.onValidDrop(target,e,id);}else{this.onInvalidDrop(target,e,id);}}else{this.onValidDrop(target,e,id);}
if(this.afterDragDrop){this.afterDragDrop(target,e,id);}}
delete this.cachedTarget;},beforeDragDrop:function(target,e,id){return true;},onValidDrop:function(target,e,id){this.hideProxy();if(this.afterValidDrop){this.afterValidDrop(target,e,id);}},getRepairXY:function(e,data){return this.el.getXY();},onInvalidDrop:function(target,e,id){this.beforeInvalidDrop(target,e,id);if(this.cachedTarget){if(this.cachedTarget.isNotifyTarget){this.cachedTarget.notifyOut(this,e,this.dragData);}
this.cacheTarget=null;}
this.proxy.repair(this.getRepairXY(e,this.dragData),this.afterRepair,this);if(this.afterInvalidDrop){this.afterInvalidDrop(e,id);}},afterRepair:function(){if(Ext.enableFx){this.el.highlight(this.hlColor||"c3daf9");}
this.dragging=false;},beforeInvalidDrop:function(target,e,id){return true;},handleMouseDown:function(e){if(this.dragging){return;}
var data=this.getDragData(e);if(data&&this.onBeforeDrag(data,e)!==false){this.dragData=data;this.proxy.stop();Ext.dd.DragSource.superclass.handleMouseDown.apply(this,arguments);}},onBeforeDrag:function(data,e){return true;},onStartDrag:Ext.emptyFn,startDrag:function(x,y){this.proxy.reset();this.dragging=true;this.proxy.update("");this.onInitDrag(x,y);this.proxy.show();},onInitDrag:function(x,y){var clone=this.el.dom.cloneNode(true);clone.id=Ext.id();this.proxy.update(clone);this.onStartDrag(x,y);return true;},getProxy:function(){return this.proxy;},hideProxy:function(){this.proxy.hide();this.proxy.reset(true);this.dragging=false;},triggerCacheRefresh:function(){Ext.dd.DDM.refreshCache(this.groups);},b4EndDrag:function(e){},endDrag:function(e){this.onEndDrag(this.dragData,e);},onEndDrag:function(data,e){},autoOffset:function(x,y){this.setDelta(-12,-20);}});