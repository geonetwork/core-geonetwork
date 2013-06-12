/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.tree.AsyncTreeNode=function(config){this.loaded=config&&config.loaded===true;this.loading=false;Ext.tree.AsyncTreeNode.superclass.constructor.apply(this,arguments);this.addEvents('beforeload','load');};Ext.extend(Ext.tree.AsyncTreeNode,Ext.tree.TreeNode,{expand:function(deep,anim,callback){if(this.loading){var timer;var f=function(){if(!this.loading){clearInterval(timer);this.expand(deep,anim,callback);}}.createDelegate(this);timer=setInterval(f,200);return;}
if(!this.loaded){if(this.fireEvent("beforeload",this)===false){return;}
this.loading=true;this.ui.beforeLoad(this);var loader=this.loader||this.attributes.loader||this.getOwnerTree().getLoader();if(loader){loader.load(this,this.loadComplete.createDelegate(this,[deep,anim,callback]));return;}}
Ext.tree.AsyncTreeNode.superclass.expand.call(this,deep,anim,callback);},isLoading:function(){return this.loading;},loadComplete:function(deep,anim,callback){this.loading=false;this.loaded=true;this.ui.afterLoad(this);this.fireEvent("load",this);this.expand(deep,anim,callback);},isLoaded:function(){return this.loaded;},hasChildNodes:function(){if(!this.isLeaf()&&!this.loaded){return true;}else{return Ext.tree.AsyncTreeNode.superclass.hasChildNodes.call(this);}},reload:function(callback){this.collapse(false,false);while(this.firstChild){this.removeChild(this.firstChild).destroy();}
this.childrenRendered=false;this.loaded=false;if(this.isHiddenRoot()){this.expanded=false;}
this.expand(false,false,callback);}});Ext.tree.TreePanel.nodeTypes.async=Ext.tree.AsyncTreeNode;