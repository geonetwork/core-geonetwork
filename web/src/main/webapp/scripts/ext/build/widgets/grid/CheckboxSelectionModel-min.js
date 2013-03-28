/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.grid.CheckboxSelectionModel=Ext.extend(Ext.grid.RowSelectionModel,{header:'<div class="x-grid3-hd-checker">&#160;</div>',width:20,sortable:false,menuDisabled:true,fixed:true,dataIndex:'',id:'checker',initEvents:function(){Ext.grid.CheckboxSelectionModel.superclass.initEvents.call(this);this.grid.on('render',function(){var view=this.grid.getView();view.mainBody.on('mousedown',this.onMouseDown,this);Ext.fly(view.innerHd).on('mousedown',this.onHdMouseDown,this);},this);},onMouseDown:function(e,t){if(e.button===0&&t.className=='x-grid3-row-checker'){e.stopEvent();var row=e.getTarget('.x-grid3-row');if(row){var index=row.rowIndex;if(this.isSelected(index)){this.deselectRow(index);}else{this.selectRow(index,true);}}}},onHdMouseDown:function(e,t){if(t.className=='x-grid3-hd-checker'){e.stopEvent();var hd=Ext.fly(t.parentNode);var isChecked=hd.hasClass('x-grid3-hd-checker-on');if(isChecked){hd.removeClass('x-grid3-hd-checker-on');this.clearSelections();}else{hd.addClass('x-grid3-hd-checker-on');this.selectAll();}}},renderer:function(v,p,record){return'<div class="x-grid3-row-checker">&#160;</div>';}});