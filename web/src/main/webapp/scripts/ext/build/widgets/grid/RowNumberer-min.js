/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.grid.RowNumberer=function(config){Ext.apply(this,config);if(this.rowspan){this.renderer=this.renderer.createDelegate(this);}};Ext.grid.RowNumberer.prototype={header:"",width:23,sortable:false,fixed:true,menuDisabled:true,dataIndex:'',id:'numberer',rowspan:undefined,renderer:function(v,p,record,rowIndex){if(this.rowspan){p.cellAttr='rowspan="'+this.rowspan+'"';}
return rowIndex+1;}};