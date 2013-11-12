/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


if(Ext.dd.DragZone){Ext.tree.TreeDragZone=function(tree,config){Ext.tree.TreeDragZone.superclass.constructor.call(this,tree.innerCt,config);this.tree=tree;};Ext.extend(Ext.tree.TreeDragZone,Ext.dd.DragZone,{ddGroup:"TreeDD",onBeforeDrag:function(data,e){var n=data.node;return n&&n.draggable&&!n.disabled;},onInitDrag:function(e){var data=this.dragData;this.tree.getSelectionModel().select(data.node);this.tree.eventModel.disable();this.proxy.update("");data.node.ui.appendDDGhost(this.proxy.ghost.dom);this.tree.fireEvent("startdrag",this.tree,data.node,e);},getRepairXY:function(e,data){return data.node.ui.getDDRepairXY();},onEndDrag:function(data,e){this.tree.eventModel.enable.defer(100,this.tree.eventModel);this.tree.fireEvent("enddrag",this.tree,data.node,e);},onValidDrop:function(dd,e,id){this.tree.fireEvent("dragdrop",this.tree,this.dragData.node,dd,e);this.hideProxy();},beforeInvalidDrop:function(e,id){var sm=this.tree.getSelectionModel();sm.clearSelections();sm.select(this.dragData.node);},afterRepair:function(){if(Ext.enableFx&&this.tree.hlDrop){Ext.Element.fly(this.dragData.ddel).highlight(this.hlColor||"c3daf9");}
this.dragging=false;}});}