/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.tree.TreeSorter=function(tree,config){Ext.apply(this,config);tree.on("beforechildrenrendered",this.doSort,this);tree.on("append",this.updateSort,this);tree.on("insert",this.updateSort,this);tree.on("textchange",this.updateSortParent,this);var dsc=this.dir&&this.dir.toLowerCase()=="desc";var p=this.property||"text";var sortType=this.sortType;var fs=this.folderSort;var cs=this.caseSensitive===true;var leafAttr=this.leafAttr||'leaf';this.sortFn=function(n1,n2){if(fs){if(n1.attributes[leafAttr]&&!n2.attributes[leafAttr]){return 1;}
if(!n1.attributes[leafAttr]&&n2.attributes[leafAttr]){return-1;}}
var v1=sortType?sortType(n1):(cs?n1.attributes[p]:n1.attributes[p].toUpperCase());var v2=sortType?sortType(n2):(cs?n2.attributes[p]:n2.attributes[p].toUpperCase());if(v1<v2){return dsc?+1:-1;}else if(v1>v2){return dsc?-1:+1;}else{return 0;}};};Ext.tree.TreeSorter.prototype={doSort:function(node){node.sort(this.sortFn);},compareNodes:function(n1,n2){return(n1.text.toUpperCase()>n2.text.toUpperCase()?1:-1);},updateSort:function(tree,node){if(node.childrenRendered){this.doSort.defer(1,this,[node]);}},updateSortParent:function(node){var p=node.parentNode;if(p&&p.childrenRendered){this.doSort.defer(1,this,[p]);}}};