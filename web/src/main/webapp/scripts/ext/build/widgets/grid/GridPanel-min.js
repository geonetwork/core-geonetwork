/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.grid.GridPanel=Ext.extend(Ext.Panel,{ddText:"{0} selected row{1}",minColumnWidth:25,trackMouseOver:true,enableDragDrop:false,enableColumnMove:true,enableColumnHide:true,enableHdMenu:true,stripeRows:false,autoExpandColumn:false,autoExpandMin:50,autoExpandMax:1000,view:null,loadMask:false,deferRowRender:true,rendered:false,viewReady:false,stateEvents:["columnmove","columnresize","sortchange"],initComponent:function(){Ext.grid.GridPanel.superclass.initComponent.call(this);this.autoScroll=false;this.autoWidth=false;if(Ext.isArray(this.columns)){this.colModel=new Ext.grid.ColumnModel(this.columns);delete this.columns;}
if(this.ds){this.store=this.ds;delete this.ds;}
if(this.cm){this.colModel=this.cm;delete this.cm;}
if(this.sm){this.selModel=this.sm;delete this.sm;}
this.store=Ext.StoreMgr.lookup(this.store);this.addEvents("click","dblclick","contextmenu","mousedown","mouseup","mouseover","mouseout","keypress","keydown","cellmousedown","rowmousedown","headermousedown","cellclick","celldblclick","rowclick","rowdblclick","headerclick","headerdblclick","rowcontextmenu","cellcontextmenu","headercontextmenu","bodyscroll","columnresize","columnmove","sortchange");},onRender:function(ct,position){Ext.grid.GridPanel.superclass.onRender.apply(this,arguments);var c=this.body;this.el.addClass('x-grid-panel');var view=this.getView();view.init(this);c.on("mousedown",this.onMouseDown,this);c.on("click",this.onClick,this);c.on("dblclick",this.onDblClick,this);c.on("contextmenu",this.onContextMenu,this);c.on("keydown",this.onKeyDown,this);this.relayEvents(c,["mousedown","mouseup","mouseover","mouseout","keypress"]);this.getSelectionModel().init(this);this.view.render();},initEvents:function(){Ext.grid.GridPanel.superclass.initEvents.call(this);if(this.loadMask){this.loadMask=new Ext.LoadMask(this.bwrap,Ext.apply({store:this.store},this.loadMask));}},initStateEvents:function(){Ext.grid.GridPanel.superclass.initStateEvents.call(this);this.colModel.on('hiddenchange',this.saveState,this,{delay:100});},applyState:function(state){var cm=this.colModel;var cs=state.columns;if(cs){for(var i=0,len=cs.length;i<len;i++){var s=cs[i];var c=cm.getColumnById(s.id);if(c){c.hidden=s.hidden;c.width=s.width;var oldIndex=cm.getIndexById(s.id);if(oldIndex!=i){cm.moveColumn(oldIndex,i);}}}}
if(state.sort){this.store[this.store.remoteSort?'setDefaultSort':'sort'](state.sort.field,state.sort.direction);}
delete state.columns;delete state.sort;Ext.grid.GridPanel.superclass.applyState.call(this,state);},getState:function(){var o={columns:[]};for(var i=0,c;c=this.colModel.config[i];i++){o.columns[i]={id:c.id,width:c.width};if(c.hidden){o.columns[i].hidden=true;}}
var ss=this.store.getSortState();if(ss){o.sort=ss;}
return o;},afterRender:function(){Ext.grid.GridPanel.superclass.afterRender.call(this);this.view.layout();if(this.deferRowRender){this.view.afterRender.defer(10,this.view);}else{this.view.afterRender();}
this.viewReady=true;},reconfigure:function(store,colModel){if(this.loadMask){this.loadMask.destroy();this.loadMask=new Ext.LoadMask(this.bwrap,Ext.apply({},{store:store},this.initialConfig.loadMask));}
this.view.bind(store,colModel);this.store=store;this.colModel=colModel;if(this.rendered){this.view.refresh(true);}},onKeyDown:function(e){this.fireEvent("keydown",e);},onDestroy:function(){if(this.rendered){var c=this.body;c.removeAllListeners();c.update("");Ext.destroy(this.view,this.loadMask);}
Ext.destroy(this.colModel,this.selModel);Ext.grid.GridPanel.superclass.onDestroy.call(this);},processEvent:function(name,e){this.fireEvent(name,e);var t=e.getTarget();var v=this.view;var header=v.findHeaderIndex(t);if(header!==false){this.fireEvent("header"+name,this,header,e);}else{var row=v.findRowIndex(t);var cell=v.findCellIndex(t);if(row!==false){this.fireEvent("row"+name,this,row,e);if(cell!==false){this.fireEvent("cell"+name,this,row,cell,e);}}}},onClick:function(e){this.processEvent("click",e);},onMouseDown:function(e){this.processEvent("mousedown",e);},onContextMenu:function(e,t){this.processEvent("contextmenu",e);},onDblClick:function(e){this.processEvent("dblclick",e);},walkCells:function(row,col,step,fn,scope){var cm=this.colModel,clen=cm.getColumnCount();var ds=this.store,rlen=ds.getCount(),first=true;if(step<0){if(col<0){row--;first=false;}
while(row>=0){if(!first){col=clen-1;}
first=false;while(col>=0){if(fn.call(scope||this,row,col,cm)===true){return[row,col];}
col--;}
row--;}}else{if(col>=clen){row++;first=false;}
while(row<rlen){if(!first){col=0;}
first=false;while(col<clen){if(fn.call(scope||this,row,col,cm)===true){return[row,col];}
col++;}
row++;}}
return null;},onResize:function(){Ext.grid.GridPanel.superclass.onResize.apply(this,arguments);if(this.viewReady){this.view.layout();}},getGridEl:function(){return this.body;},stopEditing:Ext.emptyFn,getSelectionModel:function(){if(!this.selModel){this.selModel=new Ext.grid.RowSelectionModel(this.disableSelection?{selectRow:Ext.emptyFn}:null);}
return this.selModel;},getStore:function(){return this.store;},getColumnModel:function(){return this.colModel;},getView:function(){if(!this.view){this.view=new Ext.grid.GridView(this.viewConfig);}
return this.view;},getDragDropText:function(){var count=this.selModel.getCount();return String.format(this.ddText,count,count==1?'':'s');}});Ext.reg('grid',Ext.grid.GridPanel);