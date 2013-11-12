/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.tree.DefaultSelectionModel=function(config){this.selNode=null;this.addEvents("selectionchange","beforeselect");Ext.apply(this,config);Ext.tree.DefaultSelectionModel.superclass.constructor.call(this);};Ext.extend(Ext.tree.DefaultSelectionModel,Ext.util.Observable,{init:function(tree){this.tree=tree;tree.getTreeEl().on("keydown",this.onKeyDown,this);tree.on("click",this.onNodeClick,this);},onNodeClick:function(node,e){this.select(node);},select:function(node){var last=this.selNode;if(node==last){node.ui.onSelectedChange(true);}else if(this.fireEvent('beforeselect',this,node,last)!==false){if(last){last.ui.onSelectedChange(false);}
this.selNode=node;node.ui.onSelectedChange(true);this.fireEvent("selectionchange",this,node,last);}
return node;},unselect:function(node){if(this.selNode==node){this.clearSelections();}},clearSelections:function(){var n=this.selNode;if(n){n.ui.onSelectedChange(false);this.selNode=null;this.fireEvent("selectionchange",this,null);}
return n;},getSelectedNode:function(){return this.selNode;},isSelected:function(node){return this.selNode==node;},selectPrevious:function(){var s=this.selNode||this.lastSelNode;if(!s){return null;}
var ps=s.previousSibling;if(ps){if(!ps.isExpanded()||ps.childNodes.length<1){return this.select(ps);}else{var lc=ps.lastChild;while(lc&&lc.isExpanded()&&lc.childNodes.length>0){lc=lc.lastChild;}
return this.select(lc);}}else if(s.parentNode&&(this.tree.rootVisible||!s.parentNode.isRoot)){return this.select(s.parentNode);}
return null;},selectNext:function(){var s=this.selNode||this.lastSelNode;if(!s){return null;}
if(s.firstChild&&s.isExpanded()){return this.select(s.firstChild);}else if(s.nextSibling){return this.select(s.nextSibling);}else if(s.parentNode){var newS=null;s.parentNode.bubble(function(){if(this.nextSibling){newS=this.getOwnerTree().selModel.select(this.nextSibling);return false;}});return newS;}
return null;},onKeyDown:function(e){var s=this.selNode||this.lastSelNode;var sm=this;if(!s){return;}
var k=e.getKey();switch(k){case e.DOWN:e.stopEvent();this.selectNext();break;case e.UP:e.stopEvent();this.selectPrevious();break;case e.RIGHT:e.preventDefault();if(s.hasChildNodes()){if(!s.isExpanded()){s.expand();}else if(s.firstChild){this.select(s.firstChild,e);}}
break;case e.LEFT:e.preventDefault();if(s.hasChildNodes()&&s.isExpanded()){s.collapse();}else if(s.parentNode&&(this.tree.rootVisible||s.parentNode!=this.tree.getRootNode())){this.select(s.parentNode,e);}
break;};}});Ext.tree.MultiSelectionModel=function(config){this.selNodes=[];this.selMap={};this.addEvents("selectionchange");Ext.apply(this,config);Ext.tree.MultiSelectionModel.superclass.constructor.call(this);};Ext.extend(Ext.tree.MultiSelectionModel,Ext.util.Observable,{init:function(tree){this.tree=tree;tree.getTreeEl().on("keydown",this.onKeyDown,this);tree.on("click",this.onNodeClick,this);},onNodeClick:function(node,e){if(e.ctrlKey&&this.isSelected(node)){this.unselect(node);}else{this.select(node,e,e.ctrlKey);}},select:function(node,e,keepExisting){if(keepExisting!==true){this.clearSelections(true);}
if(this.isSelected(node)){this.lastSelNode=node;return node;}
this.selNodes.push(node);this.selMap[node.id]=node;this.lastSelNode=node;node.ui.onSelectedChange(true);this.fireEvent("selectionchange",this,this.selNodes);return node;},unselect:function(node){if(this.selMap[node.id]){node.ui.onSelectedChange(false);var sn=this.selNodes;var index=sn.indexOf(node);if(index!=-1){this.selNodes.splice(index,1);}
delete this.selMap[node.id];this.fireEvent("selectionchange",this,this.selNodes);}},clearSelections:function(suppressEvent){var sn=this.selNodes;if(sn.length>0){for(var i=0,len=sn.length;i<len;i++){sn[i].ui.onSelectedChange(false);}
this.selNodes=[];this.selMap={};if(suppressEvent!==true){this.fireEvent("selectionchange",this,this.selNodes);}}},isSelected:function(node){return this.selMap[node.id]?true:false;},getSelectedNodes:function(){return this.selNodes;},onKeyDown:Ext.tree.DefaultSelectionModel.prototype.onKeyDown,selectNext:Ext.tree.DefaultSelectionModel.prototype.selectNext,selectPrevious:Ext.tree.DefaultSelectionModel.prototype.selectPrevious});