/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.tree.TreeLoader=function(config){this.baseParams={};Ext.apply(this,config);this.addEvents("beforeload","load","loadexception");Ext.tree.TreeLoader.superclass.constructor.call(this);};Ext.extend(Ext.tree.TreeLoader,Ext.util.Observable,{uiProviders:{},clearOnLoad:true,load:function(node,callback){if(this.clearOnLoad){while(node.firstChild){node.removeChild(node.firstChild);}}
if(this.doPreload(node)){if(typeof callback=="function"){callback();}}else if(this.dataUrl||this.url){this.requestData(node,callback);}},doPreload:function(node){if(node.attributes.children){if(node.childNodes.length<1){var cs=node.attributes.children;node.beginUpdate();for(var i=0,len=cs.length;i<len;i++){var cn=node.appendChild(this.createNode(cs[i]));if(this.preloadChildren){this.doPreload(cn);}}
node.endUpdate();}
return true;}else{return false;}},getParams:function(node){var buf=[],bp=this.baseParams;for(var key in bp){if(typeof bp[key]!="function"){buf.push(encodeURIComponent(key),"=",encodeURIComponent(bp[key]),"&");}}
buf.push("node=",encodeURIComponent(node.id));return buf.join("");},requestData:function(node,callback){if(this.fireEvent("beforeload",this,node,callback)!==false){this.transId=Ext.Ajax.request({method:this.requestMethod,url:this.dataUrl||this.url,success:this.handleResponse,failure:this.handleFailure,scope:this,argument:{callback:callback,node:node},params:this.getParams(node)});}else{if(typeof callback=="function"){callback();}}},isLoading:function(){return!!this.transId;},abort:function(){if(this.isLoading()){Ext.Ajax.abort(this.transId);}},createNode:function(attr){if(this.baseAttrs){Ext.applyIf(attr,this.baseAttrs);}
if(this.applyLoader!==false){attr.loader=this;}
if(typeof attr.uiProvider=='string'){attr.uiProvider=this.uiProviders[attr.uiProvider]||eval(attr.uiProvider);}
if(attr.nodeType){return new Ext.tree.TreePanel.nodeTypes[attr.nodeType](attr);}else{return attr.leaf?new Ext.tree.TreeNode(attr):new Ext.tree.AsyncTreeNode(attr);}},processResponse:function(response,node,callback){var json=response.responseText;try{var o=eval("("+json+")");node.beginUpdate();for(var i=0,len=o.length;i<len;i++){var n=this.createNode(o[i]);if(n){node.appendChild(n);}}
node.endUpdate();if(typeof callback=="function"){callback(this,node);}}catch(e){this.handleFailure(response);}},handleResponse:function(response){this.transId=false;var a=response.argument;this.processResponse(response,a.node,a.callback);this.fireEvent("load",this,a.node,response);},handleFailure:function(response){this.transId=false;var a=response.argument;this.fireEvent("loadexception",this,a.node,response);if(typeof a.callback=="function"){a.callback(this,a.node);}}});