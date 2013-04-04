/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.grid.CellSelectionModel=function(config){Ext.apply(this,config);this.selection=null;this.addEvents("beforecellselect","cellselect","selectionchange");Ext.grid.CellSelectionModel.superclass.constructor.call(this);};Ext.extend(Ext.grid.CellSelectionModel,Ext.grid.AbstractSelectionModel,{initEvents:function(){this.grid.on("cellmousedown",this.handleMouseDown,this);this.grid.getGridEl().on(Ext.isIE||(Ext.isWebKit&&!Ext.isSafari2)?"keydown":"keypress",this.handleKeyDown,this);var view=this.grid.view;view.on("refresh",this.onViewChange,this);view.on("rowupdated",this.onRowUpdated,this);view.on("beforerowremoved",this.clearSelections,this);view.on("beforerowsinserted",this.clearSelections,this);if(this.grid.isEditor){this.grid.on("beforeedit",this.beforeEdit,this);}},beforeEdit:function(e){this.select(e.row,e.column,false,true,e.record);},onRowUpdated:function(v,index,r){if(this.selection&&this.selection.record==r){v.onCellSelect(index,this.selection.cell[1]);}},onViewChange:function(){this.clearSelections(true);},getSelectedCell:function(){return this.selection?this.selection.cell:null;},clearSelections:function(preventNotify){var s=this.selection;if(s){if(preventNotify!==true){this.grid.view.onCellDeselect(s.cell[0],s.cell[1]);}
this.selection=null;this.fireEvent("selectionchange",this,null);}},hasSelection:function(){return this.selection?true:false;},handleMouseDown:function(g,row,cell,e){if(e.button!==0||this.isLocked()){return;};this.select(row,cell);},select:function(rowIndex,colIndex,preventViewNotify,preventFocus,r){if(this.fireEvent("beforecellselect",this,rowIndex,colIndex)!==false){this.clearSelections();r=r||this.grid.store.getAt(rowIndex);this.selection={record:r,cell:[rowIndex,colIndex]};if(!preventViewNotify){var v=this.grid.getView();v.onCellSelect(rowIndex,colIndex);if(preventFocus!==true){v.focusCell(rowIndex,colIndex);}}
this.fireEvent("cellselect",this,rowIndex,colIndex);this.fireEvent("selectionchange",this,this.selection);}},isSelectable:function(rowIndex,colIndex,cm){return!cm.isHidden(colIndex);},handleKeyDown:function(e){if(!e.isNavKeyPress()){return;}
var g=this.grid,s=this.selection;if(!s){e.stopEvent();var cell=g.walkCells(0,0,1,this.isSelectable,this);if(cell){this.select(cell[0],cell[1]);}
return;}
var sm=this;var walk=function(row,col,step){return g.walkCells(row,col,step,sm.isSelectable,sm);};var k=e.getKey(),r=s.cell[0],c=s.cell[1];var newCell;switch(k){case e.TAB:if(e.shiftKey){newCell=walk(r,c-1,-1);}else{newCell=walk(r,c+1,1);}
break;case e.DOWN:newCell=walk(r+1,c,1);break;case e.UP:newCell=walk(r-1,c,-1);break;case e.RIGHT:newCell=walk(r,c+1,1);break;case e.LEFT:newCell=walk(r,c-1,-1);break;case e.ENTER:if(g.isEditor&&!g.editing){g.startEditing(r,c);e.stopEvent();return;}
break;};if(newCell){this.select(newCell[0],newCell[1]);e.stopEvent();}},acceptsNav:function(row,col,cm){return!cm.isHidden(col)&&cm.isCellEditable(col,row);},onEditorKey:function(field,e){var k=e.getKey(),newCell,g=this.grid,ed=g.activeEditor;if(k==e.TAB){if(e.shiftKey){newCell=g.walkCells(ed.row,ed.col-1,-1,this.acceptsNav,this);}else{newCell=g.walkCells(ed.row,ed.col+1,1,this.acceptsNav,this);}
e.stopEvent();}else if(k==e.ENTER){ed.completeEdit();e.stopEvent();}else if(k==e.ESC){e.stopEvent();ed.cancelEdit();}
if(newCell){g.startEditing(newCell[0],newCell[1]);}}});