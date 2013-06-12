/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.tree.TreePanel=Ext.extend(Ext.Panel,{rootVisible:true,animate:Ext.enableFx,lines:true,enableDD:false,hlDrop:Ext.enableFx,pathSeparator:"/",initComponent:function(){Ext.tree.TreePanel.superclass.initComponent.call(this);if(!this.eventModel){this.eventModel=new Ext.tree.TreeEventModel(this);}
var l=this.loader;if(!l){l=new Ext.tree.TreeLoader({dataUrl:this.dataUrl});}else if(typeof l=='object'&&!l.load){l=new Ext.tree.TreeLoader(l);}
this.loader=l;this.nodeHash={};if(this.root){this.setRootNode(this.root);}
this.addEvents("append","remove","movenode","insert","beforeappend","beforeremove","beforemovenode","beforeinsert","beforeload","load","textchange","beforeexpandnode","beforecollapsenode","expandnode","disabledchange","collapsenode","beforeclick","click","checkchange","dblclick","contextmenu","beforechildrenrendered","startdrag","enddrag","dragdrop","beforenodedrop","nodedrop","nodedragover");if(this.singleExpand){this.on("beforeexpandnode",this.restrictExpand,this);}},proxyNodeEvent:function(ename,a1,a2,a3,a4,a5,a6){if(ename=='collapse'||ename=='expand'||ename=='beforecollapse'||ename=='beforeexpand'||ename=='move'||ename=='beforemove'){ename=ename+'node';}
return this.fireEvent(ename,a1,a2,a3,a4,a5,a6);},getRootNode:function(){return this.root;},setRootNode:function(node){if(!node.render){node=this.loader.createNode(node);}
this.root=node;node.ownerTree=this;node.isRoot=true;this.registerNode(node);if(!this.rootVisible){var uiP=node.attributes.uiProvider;node.ui=uiP?new uiP(node):new Ext.tree.RootTreeNodeUI(node);}
return node;},getNodeById:function(id){return this.nodeHash[id];},registerNode:function(node){this.nodeHash[node.id]=node;},unregisterNode:function(node){delete this.nodeHash[node.id];},toString:function(){return"[Tree"+(this.id?" "+this.id:"")+"]";},restrictExpand:function(node){var p=node.parentNode;if(p){if(p.expandedChild&&p.expandedChild.parentNode==p){p.expandedChild.collapse();}
p.expandedChild=node;}},getChecked:function(a,startNode){startNode=startNode||this.root;var r=[];var f=function(){if(this.attributes.checked){r.push(!a?this:(a=='id'?this.id:this.attributes[a]));}}
startNode.cascade(f);return r;},getEl:function(){return this.el;},getLoader:function(){return this.loader;},expandAll:function(){this.root.expand(true);},collapseAll:function(){this.root.collapse(true);},getSelectionModel:function(){if(!this.selModel){this.selModel=new Ext.tree.DefaultSelectionModel();}
return this.selModel;},expandPath:function(path,attr,callback){attr=attr||"id";var keys=path.split(this.pathSeparator);var curNode=this.root;if(curNode.attributes[attr]!=keys[1]){if(callback){callback(false,null);}
return;}
var index=1;var f=function(){if(++index==keys.length){if(callback){callback(true,curNode);}
return;}
var c=curNode.findChild(attr,keys[index]);if(!c){if(callback){callback(false,curNode);}
return;}
curNode=c;c.expand(false,false,f);};curNode.expand(false,false,f);},selectPath:function(path,attr,callback){attr=attr||"id";var keys=path.split(this.pathSeparator);var v=keys.pop();if(keys.length>0){var f=function(success,node){if(success&&node){var n=node.findChild(attr,v);if(n){n.select();if(callback){callback(true,n);}}else if(callback){callback(false,n);}}else{if(callback){callback(false,n);}}};this.expandPath(keys.join(this.pathSeparator),attr,f);}else{this.root.select();if(callback){callback(true,this.root);}}},getTreeEl:function(){return this.body;},onRender:function(ct,position){Ext.tree.TreePanel.superclass.onRender.call(this,ct,position);this.el.addClass('x-tree');this.innerCt=this.body.createChild({tag:"ul",cls:"x-tree-root-ct "+
(this.useArrows?'x-tree-arrows':this.lines?"x-tree-lines":"x-tree-no-lines")});},initEvents:function(){Ext.tree.TreePanel.superclass.initEvents.call(this);if(this.containerScroll){Ext.dd.ScrollManager.register(this.body);}
if((this.enableDD||this.enableDrop)&&!this.dropZone){this.dropZone=new Ext.tree.TreeDropZone(this,this.dropConfig||{ddGroup:this.ddGroup||"TreeDD",appendOnly:this.ddAppendOnly===true});}
if((this.enableDD||this.enableDrag)&&!this.dragZone){this.dragZone=new Ext.tree.TreeDragZone(this,this.dragConfig||{ddGroup:this.ddGroup||"TreeDD",scroll:this.ddScroll});}
this.getSelectionModel().init(this);},afterRender:function(){Ext.tree.TreePanel.superclass.afterRender.call(this);this.root.render();if(!this.rootVisible){this.root.renderChildren();}},onDestroy:function(){if(this.rendered){this.body.removeAllListeners();Ext.dd.ScrollManager.unregister(this.body);if(this.dropZone){this.dropZone.unreg();}
if(this.dragZone){this.dragZone.unreg();}}
this.root.destroy();this.nodeHash=null;Ext.tree.TreePanel.superclass.onDestroy.call(this);}});Ext.tree.TreePanel.nodeTypes={};Ext.reg('treepanel',Ext.tree.TreePanel);