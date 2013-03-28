/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.tree.TreeEditor=function(tree,fc,config){fc=fc||{};var field=fc.events?fc:new Ext.form.TextField(fc);Ext.tree.TreeEditor.superclass.constructor.call(this,field,config);this.tree=tree;if(!tree.rendered){tree.on('render',this.initEditor,this);}else{this.initEditor(tree);}};Ext.extend(Ext.tree.TreeEditor,Ext.Editor,{alignment:"l-l",autoSize:false,hideEl:false,cls:"x-small-editor x-tree-editor",shim:false,shadow:"frame",maxWidth:250,editDelay:350,initEditor:function(tree){tree.on('beforeclick',this.beforeNodeClick,this);tree.on('dblclick',this.onNodeDblClick,this);this.on('complete',this.updateNode,this);this.on('beforestartedit',this.fitToTree,this);this.on('startedit',this.bindScroll,this,{delay:10});this.on('specialkey',this.onSpecialKey,this);},fitToTree:function(ed,el){var td=this.tree.getTreeEl().dom,nd=el.dom;if(td.scrollLeft>nd.offsetLeft){td.scrollLeft=nd.offsetLeft;}
var w=Math.min(this.maxWidth,(td.clientWidth>20?td.clientWidth:td.offsetWidth)-Math.max(0,nd.offsetLeft-td.scrollLeft)-5);this.setSize(w,'');},triggerEdit:function(node,defer){this.completeEdit();if(node.attributes.editable!==false){this.editNode=node;if(this.tree.autoScroll){Ext.fly(node.ui.getEl()).scrollIntoView(this.tree.body);}
var value=node.text||'';if(!Ext.isGecko&&Ext.isEmpty(node.text)){node.setText('&nbsp;');}
this.autoEditTimer=this.startEdit.defer(this.editDelay,this,[node.ui.textNode,value]);return false;}},bindScroll:function(){this.tree.getTreeEl().on('scroll',this.cancelEdit,this);},beforeNodeClick:function(node,e){clearTimeout(this.autoEditTimer);if(this.tree.getSelectionModel().isSelected(node)){e.stopEvent();return this.triggerEdit(node);}},onNodeDblClick:function(node,e){clearTimeout(this.autoEditTimer);},updateNode:function(ed,value){this.tree.getTreeEl().un('scroll',this.cancelEdit,this);this.editNode.setText(value);},onHide:function(){Ext.tree.TreeEditor.superclass.onHide.call(this);if(this.editNode){this.editNode.ui.focus.defer(50,this.editNode.ui);}},onSpecialKey:function(field,e){var k=e.getKey();if(k==e.ESC){e.stopEvent();this.cancelEdit();}else if(k==e.ENTER&&!e.hasModifier()){e.stopEvent();this.completeEdit();}}});