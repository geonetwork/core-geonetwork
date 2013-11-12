/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.tree.TreeNode=function(attributes){attributes=attributes||{};if(typeof attributes=="string"){attributes={text:attributes};}
this.childrenRendered=false;this.rendered=false;Ext.tree.TreeNode.superclass.constructor.call(this,attributes);this.expanded=attributes.expanded===true;this.isTarget=attributes.isTarget!==false;this.draggable=attributes.draggable!==false&&attributes.allowDrag!==false;this.allowChildren=attributes.allowChildren!==false&&attributes.allowDrop!==false;this.text=attributes.text;this.disabled=attributes.disabled===true;this.hidden=attributes.hidden===true;this.addEvents("textchange","beforeexpand","beforecollapse","expand","disabledchange","collapse","beforeclick","click","checkchange","dblclick","contextmenu","beforechildrenrendered");var uiClass=this.attributes.uiProvider||this.defaultUI||Ext.tree.TreeNodeUI;this.ui=new uiClass(this);};Ext.extend(Ext.tree.TreeNode,Ext.data.Node,{preventHScroll:true,isExpanded:function(){return this.expanded;},getUI:function(){return this.ui;},getLoader:function(){var owner;return this.loader||((owner=this.getOwnerTree())&&owner.loader?owner.loader:new Ext.tree.TreeLoader());},setFirstChild:function(node){var of=this.firstChild;Ext.tree.TreeNode.superclass.setFirstChild.call(this,node);if(this.childrenRendered&&of&&node!=of){of.renderIndent(true,true);}
if(this.rendered){this.renderIndent(true,true);}},setLastChild:function(node){var ol=this.lastChild;Ext.tree.TreeNode.superclass.setLastChild.call(this,node);if(this.childrenRendered&&ol&&node!=ol){ol.renderIndent(true,true);}
if(this.rendered){this.renderIndent(true,true);}},appendChild:function(n){if(!n.render&&!Ext.isArray(n)){n=this.getLoader().createNode(n);}
var node=Ext.tree.TreeNode.superclass.appendChild.call(this,n);if(node&&this.childrenRendered){node.render();}
this.ui.updateExpandIcon();return node;},removeChild:function(node){this.ownerTree.getSelectionModel().unselect(node);Ext.tree.TreeNode.superclass.removeChild.apply(this,arguments);if(this.childrenRendered){node.ui.remove();}
if(this.childNodes.length<1){this.collapse(false,false);}else{this.ui.updateExpandIcon();}
if(!this.firstChild&&!this.isHiddenRoot()){this.childrenRendered=false;}
return node;},insertBefore:function(node,refNode){if(!node.render){node=this.getLoader().createNode(node);}
var newNode=Ext.tree.TreeNode.superclass.insertBefore.apply(this,arguments);if(newNode&&refNode&&this.childrenRendered){node.render();}
this.ui.updateExpandIcon();return newNode;},setText:function(text){var oldText=this.text;this.text=text;this.attributes.text=text;if(this.rendered){this.ui.onTextChange(this,text,oldText);}
this.fireEvent("textchange",this,text,oldText);},select:function(){this.getOwnerTree().getSelectionModel().select(this);},unselect:function(){this.getOwnerTree().getSelectionModel().unselect(this);},isSelected:function(){return this.getOwnerTree().getSelectionModel().isSelected(this);},expand:function(deep,anim,callback){if(!this.expanded){if(this.fireEvent("beforeexpand",this,deep,anim)===false){return;}
if(!this.childrenRendered){this.renderChildren();}
this.expanded=true;if(!this.isHiddenRoot()&&(this.getOwnerTree().animate&&anim!==false)||anim){this.ui.animExpand(function(){this.fireEvent("expand",this);if(typeof callback=="function"){callback(this);}
if(deep===true){this.expandChildNodes(true);}}.createDelegate(this));return;}else{this.ui.expand();this.fireEvent("expand",this);if(typeof callback=="function"){callback(this);}}}else{if(typeof callback=="function"){callback(this);}}
if(deep===true){this.expandChildNodes(true);}},isHiddenRoot:function(){return this.isRoot&&!this.getOwnerTree().rootVisible;},collapse:function(deep,anim){if(this.expanded&&!this.isHiddenRoot()){if(this.fireEvent("beforecollapse",this,deep,anim)===false){return;}
this.expanded=false;if((this.getOwnerTree().animate&&anim!==false)||anim){this.ui.animCollapse(function(){this.fireEvent("collapse",this);if(deep===true){this.collapseChildNodes(true);}}.createDelegate(this));return;}else{this.ui.collapse();this.fireEvent("collapse",this);}}
if(deep===true){var cs=this.childNodes;for(var i=0,len=cs.length;i<len;i++){cs[i].collapse(true,false);}}},delayedExpand:function(delay){if(!this.expandProcId){this.expandProcId=this.expand.defer(delay,this);}},cancelExpand:function(){if(this.expandProcId){clearTimeout(this.expandProcId);}
this.expandProcId=false;},toggle:function(){if(this.expanded){this.collapse();}else{this.expand();}},ensureVisible:function(callback){var tree=this.getOwnerTree();tree.expandPath(this.parentNode?this.parentNode.getPath():this.getPath(),false,function(){var node=tree.getNodeById(this.id);tree.getTreeEl().scrollChildIntoView(node.ui.anchor);Ext.callback(callback);}.createDelegate(this));},expandChildNodes:function(deep){var cs=this.childNodes;for(var i=0,len=cs.length;i<len;i++){cs[i].expand(deep);}},collapseChildNodes:function(deep){var cs=this.childNodes;for(var i=0,len=cs.length;i<len;i++){cs[i].collapse(deep);}},disable:function(){this.disabled=true;this.unselect();if(this.rendered&&this.ui.onDisableChange){this.ui.onDisableChange(this,true);}
this.fireEvent("disabledchange",this,true);},enable:function(){this.disabled=false;if(this.rendered&&this.ui.onDisableChange){this.ui.onDisableChange(this,false);}
this.fireEvent("disabledchange",this,false);},renderChildren:function(suppressEvent){if(suppressEvent!==false){this.fireEvent("beforechildrenrendered",this);}
var cs=this.childNodes;for(var i=0,len=cs.length;i<len;i++){cs[i].render(true);}
this.childrenRendered=true;},sort:function(fn,scope){Ext.tree.TreeNode.superclass.sort.apply(this,arguments);if(this.childrenRendered){var cs=this.childNodes;for(var i=0,len=cs.length;i<len;i++){cs[i].render(true);}}},render:function(bulkRender){this.ui.render(bulkRender);if(!this.rendered){this.getOwnerTree().registerNode(this);this.rendered=true;if(this.expanded){this.expanded=false;this.expand(false,false);}}},renderIndent:function(deep,refresh){if(refresh){this.ui.childIndent=null;}
this.ui.renderIndent();if(deep===true&&this.childrenRendered){var cs=this.childNodes;for(var i=0,len=cs.length;i<len;i++){cs[i].renderIndent(true,refresh);}}},beginUpdate:function(){this.childrenRendered=false;},endUpdate:function(){if(this.expanded&&this.rendered){this.renderChildren();}},destroy:function(){if(this.childNodes){for(var i=0,l=this.childNodes.length;i<l;i++){this.childNodes[i].destroy();}
this.childNodes=null;}
if(this.ui.destroy){this.ui.destroy();}}});Ext.tree.TreePanel.nodeTypes.node=Ext.tree.TreeNode;