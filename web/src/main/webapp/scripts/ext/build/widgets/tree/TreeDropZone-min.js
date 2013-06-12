/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


if(Ext.dd.DropZone){Ext.tree.TreeDropZone=function(tree,config){this.allowParentInsert=config.allowParentInsert||false;this.allowContainerDrop=config.allowContainerDrop||false;this.appendOnly=config.appendOnly||false;Ext.tree.TreeDropZone.superclass.constructor.call(this,tree.getTreeEl(),config);this.tree=tree;this.dragOverData={};this.lastInsertClass="x-tree-no-status";};Ext.extend(Ext.tree.TreeDropZone,Ext.dd.DropZone,{ddGroup:"TreeDD",expandDelay:1000,expandNode:function(node){if(node.hasChildNodes()&&!node.isExpanded()){node.expand(false,null,this.triggerCacheRefresh.createDelegate(this));}},queueExpand:function(node){this.expandProcId=this.expandNode.defer(this.expandDelay,this,[node]);},cancelExpand:function(){if(this.expandProcId){clearTimeout(this.expandProcId);this.expandProcId=false;}},isValidDropPoint:function(n,pt,dd,e,data){if(!n||!data){return false;}
var targetNode=n.node;var dropNode=data.node;if(!(targetNode&&targetNode.isTarget&&pt)){return false;}
if(pt=="append"&&targetNode.allowChildren===false){return false;}
if((pt=="above"||pt=="below")&&(targetNode.parentNode&&targetNode.parentNode.allowChildren===false)){return false;}
if(dropNode&&(targetNode==dropNode||dropNode.contains(targetNode))){return false;}
var overEvent=this.dragOverData;overEvent.tree=this.tree;overEvent.target=targetNode;overEvent.data=data;overEvent.point=pt;overEvent.source=dd;overEvent.rawEvent=e;overEvent.dropNode=dropNode;overEvent.cancel=false;var result=this.tree.fireEvent("nodedragover",overEvent);return overEvent.cancel===false&&result!==false;},getDropPoint:function(e,n,dd){var tn=n.node;if(tn.isRoot){return tn.allowChildren!==false?"append":false;}
var dragEl=n.ddel;var t=Ext.lib.Dom.getY(dragEl),b=t+dragEl.offsetHeight;var y=Ext.lib.Event.getPageY(e);var noAppend=tn.allowChildren===false||tn.isLeaf();if(this.appendOnly||tn.parentNode.allowChildren===false){return noAppend?false:"append";}
var noBelow=false;if(!this.allowParentInsert){noBelow=tn.hasChildNodes()&&tn.isExpanded();}
var q=(b-t)/(noAppend?2:3);if(y>=t&&y<(t+q)){return"above";}else if(!noBelow&&(noAppend||y>=b-q&&y<=b)){return"below";}else{return"append";}},onNodeEnter:function(n,dd,e,data){this.cancelExpand();},onContainerOver:function(dd,e,data){if(this.allowContainerDrop&&this.isValidDropPoint({ddel:this.tree.getRootNode().ui.elNode,node:this.tree.getRootNode()},"append",dd,e,data)){return this.dropAllowed;}
return this.dropNotAllowed;},onNodeOver:function(n,dd,e,data){var pt=this.getDropPoint(e,n,dd);var node=n.node;if(!this.expandProcId&&pt=="append"&&node.hasChildNodes()&&!n.node.isExpanded()){this.queueExpand(node);}else if(pt!="append"){this.cancelExpand();}
var returnCls=this.dropNotAllowed;if(this.isValidDropPoint(n,pt,dd,e,data)){if(pt){var el=n.ddel;var cls;if(pt=="above"){returnCls=n.node.isFirst()?"x-tree-drop-ok-above":"x-tree-drop-ok-between";cls="x-tree-drag-insert-above";}else if(pt=="below"){returnCls=n.node.isLast()?"x-tree-drop-ok-below":"x-tree-drop-ok-between";cls="x-tree-drag-insert-below";}else{returnCls="x-tree-drop-ok-append";cls="x-tree-drag-append";}
if(this.lastInsertClass!=cls){Ext.fly(el).replaceClass(this.lastInsertClass,cls);this.lastInsertClass=cls;}}}
return returnCls;},onNodeOut:function(n,dd,e,data){this.cancelExpand();this.removeDropIndicators(n);},onNodeDrop:function(n,dd,e,data){var point=this.getDropPoint(e,n,dd);var targetNode=n.node;targetNode.ui.startDrop();if(!this.isValidDropPoint(n,point,dd,e,data)){targetNode.ui.endDrop();return false;}
var dropNode=data.node||(dd.getTreeNode?dd.getTreeNode(data,targetNode,point,e):null);return this.processDrop(targetNode,data,point,dd,e,dropNode);},onContainerDrop:function(dd,e,data){if(this.allowContainerDrop&&this.isValidDropPoint({ddel:this.tree.getRootNode().ui.elNode,node:this.tree.getRootNode()},"append",dd,e,data)){var targetNode=this.tree.getRootNode();targetNode.ui.startDrop();var dropNode=data.node||(dd.getTreeNode?dd.getTreeNode(data,targetNode,'append',e):null);return this.processDrop(targetNode,data,'append',dd,e,dropNode);}
return false;},processDrop:function(target,data,point,dd,e,dropNode){var dropEvent={tree:this.tree,target:target,data:data,point:point,source:dd,rawEvent:e,dropNode:dropNode,cancel:!dropNode,dropStatus:false};var retval=this.tree.fireEvent("beforenodedrop",dropEvent);if(retval===false||dropEvent.cancel===true||!dropEvent.dropNode){target.ui.endDrop();return dropEvent.dropStatus;}
target=dropEvent.target;if(point=='append'&&!target.isExpanded()){target.expand(false,null,function(){this.completeDrop(dropEvent);}.createDelegate(this));}else{this.completeDrop(dropEvent);}
return true;},completeDrop:function(de){var ns=de.dropNode,p=de.point,t=de.target;if(!Ext.isArray(ns)){ns=[ns];}
var n;for(var i=0,len=ns.length;i<len;i++){n=ns[i];if(p=="above"){t.parentNode.insertBefore(n,t);}else if(p=="below"){t.parentNode.insertBefore(n,t.nextSibling);}else{t.appendChild(n);}}
n.ui.focus();if(Ext.enableFx&&this.tree.hlDrop){n.ui.highlight();}
t.ui.endDrop();this.tree.fireEvent("nodedrop",de);},afterNodeMoved:function(dd,data,e,targetNode,dropNode){if(Ext.enableFx&&this.tree.hlDrop){dropNode.ui.focus();dropNode.ui.highlight();}
this.tree.fireEvent("nodedrop",this.tree,targetNode,data,dd,e);},getTree:function(){return this.tree;},removeDropIndicators:function(n){if(n&&n.ddel){var el=n.ddel;Ext.fly(el).removeClass(["x-tree-drag-insert-above","x-tree-drag-insert-below","x-tree-drag-append"]);this.lastInsertClass="_noclass";}},beforeDragDrop:function(target,e,id){this.cancelExpand();return true;},afterRepair:function(data){if(data&&Ext.enableFx){data.node.ui.highlight();}
this.hideProxy();}});}