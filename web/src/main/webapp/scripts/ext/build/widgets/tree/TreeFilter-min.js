/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.tree.TreeFilter=function(tree,config){this.tree=tree;this.filtered={};Ext.apply(this,config);};Ext.tree.TreeFilter.prototype={clearBlank:false,reverse:false,autoClear:false,remove:false,filter:function(value,attr,startNode){attr=attr||"text";var f;if(typeof value=="string"){var vlen=value.length;if(vlen==0&&this.clearBlank){this.clear();return;}
value=value.toLowerCase();f=function(n){return n.attributes[attr].substr(0,vlen).toLowerCase()==value;};}else if(value.exec){f=function(n){return value.test(n.attributes[attr]);};}else{throw'Illegal filter type, must be string or regex';}
this.filterBy(f,null,startNode);},filterBy:function(fn,scope,startNode){startNode=startNode||this.tree.root;if(this.autoClear){this.clear();}
var af=this.filtered,rv=this.reverse;var f=function(n){if(n==startNode){return true;}
if(af[n.id]){return false;}
var m=fn.call(scope||n,n);if(!m||rv){af[n.id]=n;n.ui.hide();return false;}
return true;};startNode.cascade(f);if(this.remove){for(var id in af){if(typeof id!="function"){var n=af[id];if(n&&n.parentNode){n.parentNode.removeChild(n);}}}}},clear:function(){var t=this.tree;var af=this.filtered;for(var id in af){if(typeof id!="function"){var n=af[id];if(n){n.ui.show();}}}
this.filtered={};}};