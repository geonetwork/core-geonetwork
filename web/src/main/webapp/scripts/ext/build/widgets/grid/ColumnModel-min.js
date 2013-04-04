/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.grid.ColumnModel=function(config){this.defaultWidth=100;this.defaultSortable=false;if(config.columns){Ext.apply(this,config);this.setConfig(config.columns,true);}else{this.setConfig(config,true);}
this.addEvents("widthchange","headerchange","hiddenchange","columnmoved","columnlockchange","configchange");Ext.grid.ColumnModel.superclass.constructor.call(this);};Ext.extend(Ext.grid.ColumnModel,Ext.util.Observable,{getColumnId:function(index){return this.config[index].id;},setConfig:function(config,initial){if(!initial){delete this.totalWidth;for(var i=0,len=this.config.length;i<len;i++){var c=this.config[i];if(c.editor){c.editor.destroy();}}}
this.config=config;this.lookup={};for(var i=0,len=config.length;i<len;i++){var c=config[i];if(typeof c.renderer=="string"){c.renderer=Ext.util.Format[c.renderer];}
if(typeof c.id=="undefined"){c.id=i;}
if(c.editor&&c.editor.isFormField){c.editor=new Ext.grid.GridEditor(c.editor);}
this.lookup[c.id]=c;}
if(!initial){this.fireEvent('configchange',this);}},getColumnById:function(id){return this.lookup[id];},getIndexById:function(id){for(var i=0,len=this.config.length;i<len;i++){if(this.config[i].id==id){return i;}}
return-1;},moveColumn:function(oldIndex,newIndex){var c=this.config[oldIndex];this.config.splice(oldIndex,1);this.config.splice(newIndex,0,c);this.dataMap=null;this.fireEvent("columnmoved",this,oldIndex,newIndex);},isLocked:function(colIndex){return this.config[colIndex].locked===true;},setLocked:function(colIndex,value,suppressEvent){if(this.isLocked(colIndex)==value){return;}
this.config[colIndex].locked=value;if(!suppressEvent){this.fireEvent("columnlockchange",this,colIndex,value);}},getTotalLockedWidth:function(){var totalWidth=0;for(var i=0;i<this.config.length;i++){if(this.isLocked(i)&&!this.isHidden(i)){this.totalWidth+=this.getColumnWidth(i);}}
return totalWidth;},getLockedCount:function(){for(var i=0,len=this.config.length;i<len;i++){if(!this.isLocked(i)){return i;}}},getColumnCount:function(visibleOnly){if(visibleOnly===true){var c=0;for(var i=0,len=this.config.length;i<len;i++){if(!this.isHidden(i)){c++;}}
return c;}
return this.config.length;},getColumnsBy:function(fn,scope){var r=[];for(var i=0,len=this.config.length;i<len;i++){var c=this.config[i];if(fn.call(scope||this,c,i)===true){r[r.length]=c;}}
return r;},isSortable:function(col){if(typeof this.config[col].sortable=="undefined"){return this.defaultSortable;}
return this.config[col].sortable;},isMenuDisabled:function(col){return!!this.config[col].menuDisabled;},getRenderer:function(col){if(!this.config[col].renderer){return Ext.grid.ColumnModel.defaultRenderer;}
return this.config[col].renderer;},setRenderer:function(col,fn){this.config[col].renderer=fn;},getColumnWidth:function(col){return this.config[col].width||this.defaultWidth;},setColumnWidth:function(col,width,suppressEvent){this.config[col].width=width;this.totalWidth=null;if(!suppressEvent){this.fireEvent("widthchange",this,col,width);}},getTotalWidth:function(includeHidden){if(!this.totalWidth){this.totalWidth=0;for(var i=0,len=this.config.length;i<len;i++){if(includeHidden||!this.isHidden(i)){this.totalWidth+=this.getColumnWidth(i);}}}
return this.totalWidth;},getColumnHeader:function(col){return this.config[col].header;},setColumnHeader:function(col,header){this.config[col].header=header;this.fireEvent("headerchange",this,col,header);},getColumnTooltip:function(col){return this.config[col].tooltip;},setColumnTooltip:function(col,tooltip){this.config[col].tooltip=tooltip;},getDataIndex:function(col){return this.config[col].dataIndex;},setDataIndex:function(col,dataIndex){this.config[col].dataIndex=dataIndex;},findColumnIndex:function(dataIndex){var c=this.config;for(var i=0,len=c.length;i<len;i++){if(c[i].dataIndex==dataIndex){return i;}}
return-1;},isCellEditable:function(colIndex,rowIndex){return(this.config[colIndex].editable||(typeof this.config[colIndex].editable=="undefined"&&this.config[colIndex].editor))?true:false;},getCellEditor:function(colIndex,rowIndex){return this.config[colIndex].editor;},setEditable:function(col,editable){this.config[col].editable=editable;},isHidden:function(colIndex){return this.config[colIndex].hidden;},isFixed:function(colIndex){return this.config[colIndex].fixed;},isResizable:function(colIndex){return colIndex>=0&&this.config[colIndex].resizable!==false&&this.config[colIndex].fixed!==true;},setHidden:function(colIndex,hidden){var c=this.config[colIndex];if(c.hidden!==hidden){c.hidden=hidden;this.totalWidth=null;this.fireEvent("hiddenchange",this,colIndex,hidden);}},setEditor:function(col,editor){Ext.destroy(this.config[col].editor);this.config[col].editor=editor;},destroy:function(){var c=this.config;for(var i=0,c=this.config,len=c.length;i<len;i++){Ext.destroy(c[i].editor);}
this.purgeListeners();}});Ext.grid.ColumnModel.defaultRenderer=function(value){if(typeof value=="string"&&value.length<1){return"&#160;";}
return value;};Ext.grid.DefaultColumnModel=Ext.grid.ColumnModel;