/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.layout.ContainerLayout=function(config){Ext.apply(this,config);};Ext.layout.ContainerLayout.prototype={monitorResize:false,activeItem:null,layout:function(){var target=this.container.getLayoutTarget();this.onLayout(this.container,target);this.container.fireEvent('afterlayout',this.container,this);},onLayout:function(ct,target){this.renderAll(ct,target);},isValidParent:function(c,target){var el=c.getPositionEl?c.getPositionEl():c.getEl();return el.dom.parentNode==target.dom;},renderAll:function(ct,target){var items=ct.items.items;for(var i=0,len=items.length;i<len;i++){var c=items[i];if(c&&(!c.rendered||!this.isValidParent(c,target))){this.renderItem(c,i,target);}}},renderItem:function(c,position,target){if(c&&!c.rendered){c.render(target,position);if(this.extraCls){var t=c.getPositionEl?c.getPositionEl():c;t.addClass(this.extraCls);}
if(this.renderHidden&&c!=this.activeItem){c.hide();}}else if(c&&!this.isValidParent(c,target)){if(this.extraCls){var t=c.getPositionEl?c.getPositionEl():c;t.addClass(this.extraCls);}
if(typeof position=='number'){position=target.dom.childNodes[position];}
target.dom.insertBefore(c.getEl().dom,position||null);if(this.renderHidden&&c!=this.activeItem){c.hide();}}},onResize:function(){if(this.container.collapsed){return;}
var b=this.container.bufferResize;if(b){if(!this.resizeTask){this.resizeTask=new Ext.util.DelayedTask(this.layout,this);this.resizeBuffer=typeof b=='number'?b:100;}
this.resizeTask.delay(this.resizeBuffer);}else{this.layout();}},setContainer:function(ct){if(this.monitorResize&&ct!=this.container){if(this.container){this.container.un('resize',this.onResize,this);}
if(ct){ct.on('resize',this.onResize,this);}}
this.container=ct;},parseMargins:function(v){var ms=v.split(' ');var len=ms.length;if(len==1){ms[1]=ms[0];ms[2]=ms[0];ms[3]=ms[0];}
if(len==2){ms[2]=ms[0];ms[3]=ms[1];}
return{top:parseInt(ms[0],10)||0,right:parseInt(ms[1],10)||0,bottom:parseInt(ms[2],10)||0,left:parseInt(ms[3],10)||0};},destroy:Ext.emptyFn};Ext.Container.LAYOUTS['auto']=Ext.layout.ContainerLayout;