/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.data.GroupingStore=Ext.extend(Ext.data.Store,{constructor:function(config){Ext.data.GroupingStore.superclass.constructor.call(this,config);this.applyGroupField();},remoteGroup:false,groupOnSort:false,clearGrouping:function(){this.groupField=false;if(this.remoteGroup){if(this.baseParams){delete this.baseParams.groupBy;}
var lo=this.lastOptions;if(lo&&lo.params){delete lo.params.groupBy;}
this.reload();}else{this.applySort();this.fireEvent('datachanged',this);}},groupBy:function(field,forceRegroup){if(this.groupField==field&&!forceRegroup){return;}
this.groupField=field;this.applyGroupField();if(this.groupOnSort){this.sort(field);return;}
if(this.remoteGroup){this.reload();}else{var si=this.sortInfo||{};if(si.field!=field){this.applySort();}else{this.sortData(field);}
this.fireEvent('datachanged',this);}},applyGroupField:function(){if(this.remoteGroup){if(!this.baseParams){this.baseParams={};}
this.baseParams.groupBy=this.groupField;}},applySort:function(){Ext.data.GroupingStore.superclass.applySort.call(this);if(!this.groupOnSort&&!this.remoteGroup){var gs=this.getGroupState();if(gs&&gs!=this.sortInfo.field){this.sortData(this.groupField);}}},applyGrouping:function(alwaysFireChange){if(this.groupField!==false){this.groupBy(this.groupField,true);return true;}else{if(alwaysFireChange===true){this.fireEvent('datachanged',this);}
return false;}},getGroupState:function(){return this.groupOnSort&&this.groupField!==false?(this.sortInfo?this.sortInfo.field:undefined):this.groupField;}});