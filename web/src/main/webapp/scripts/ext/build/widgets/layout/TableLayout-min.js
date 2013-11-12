/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.layout.TableLayout=Ext.extend(Ext.layout.ContainerLayout,{monitorResize:false,setContainer:function(ct){Ext.layout.TableLayout.superclass.setContainer.call(this,ct);this.currentRow=0;this.currentColumn=0;this.cells=[];},onLayout:function(ct,target){var cs=ct.items.items,len=cs.length,c,i;if(!this.table){target.addClass('x-table-layout-ct');this.table=target.createChild({tag:'table',cls:'x-table-layout',cellspacing:0,cn:{tag:'tbody'}},null,true);}
this.renderAll(ct,target);},getRow:function(index){var row=this.table.tBodies[0].childNodes[index];if(!row){row=document.createElement('tr');this.table.tBodies[0].appendChild(row);}
return row;},getNextCell:function(c){var cell=this.getNextNonSpan(this.currentColumn,this.currentRow);var curCol=this.currentColumn=cell[0],curRow=this.currentRow=cell[1];for(var rowIndex=curRow;rowIndex<curRow+(c.rowspan||1);rowIndex++){if(!this.cells[rowIndex]){this.cells[rowIndex]=[];}
for(var colIndex=curCol;colIndex<curCol+(c.colspan||1);colIndex++){this.cells[rowIndex][colIndex]=true;}}
var td=document.createElement('td');if(c.cellId){td.id=c.cellId;}
var cls='x-table-layout-cell';if(c.cellCls){cls+=' '+c.cellCls;}
td.className=cls;if(c.colspan){td.colSpan=c.colspan;}
if(c.rowspan){td.rowSpan=c.rowspan;}
this.getRow(curRow).appendChild(td);return td;},getNextNonSpan:function(colIndex,rowIndex){var cols=this.columns;while((cols&&colIndex>=cols)||(this.cells[rowIndex]&&this.cells[rowIndex][colIndex])){if(cols&&colIndex>=cols){rowIndex++;colIndex=0;}else{colIndex++;}}
return[colIndex,rowIndex];},renderItem:function(c,position,target){if(c&&!c.rendered){c.render(this.getNextCell(c));if(this.extraCls){var t=c.getPositionEl?c.getPositionEl():c;t.addClass(this.extraCls);}}},isValidParent:function(c,target){return true;}});Ext.Container.LAYOUTS['table']=Ext.layout.TableLayout;