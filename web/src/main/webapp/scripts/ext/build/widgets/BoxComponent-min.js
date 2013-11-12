/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.BoxComponent=Ext.extend(Ext.Component,{initComponent:function(){Ext.BoxComponent.superclass.initComponent.call(this);this.addEvents('resize','move');},boxReady:false,deferHeight:false,setSize:function(w,h){if(typeof w=='object'){h=w.height;w=w.width;}
if(!this.boxReady){this.width=w;this.height=h;return this;}
if(this.lastSize&&this.lastSize.width==w&&this.lastSize.height==h){return this;}
this.lastSize={width:w,height:h};var adj=this.adjustSize(w,h);var aw=adj.width,ah=adj.height;if(aw!==undefined||ah!==undefined){var rz=this.getResizeEl();if(!this.deferHeight&&aw!==undefined&&ah!==undefined){rz.setSize(aw,ah);}else if(!this.deferHeight&&ah!==undefined){rz.setHeight(ah);}else if(aw!==undefined){rz.setWidth(aw);}
this.onResize(aw,ah,w,h);this.fireEvent('resize',this,aw,ah,w,h);}
return this;},setWidth:function(width){return this.setSize(width);},setHeight:function(height){return this.setSize(undefined,height);},getSize:function(){return this.el.getSize();},getPosition:function(local){if(local===true){return[this.el.getLeft(true),this.el.getTop(true)];}
return this.xy||this.el.getXY();},getBox:function(local){var s=this.el.getSize();if(local===true){s.x=this.el.getLeft(true);s.y=this.el.getTop(true);}else{var xy=this.xy||this.el.getXY();s.x=xy[0];s.y=xy[1];}
return s;},updateBox:function(box){this.setSize(box.width,box.height);this.setPagePosition(box.x,box.y);return this;},getResizeEl:function(){return this.resizeEl||this.el;},getPositionEl:function(){return this.positionEl||this.el;},setPosition:function(x,y){if(x&&typeof x[1]=='number'){y=x[1];x=x[0];}
this.x=x;this.y=y;if(!this.boxReady){return this;}
var adj=this.adjustPosition(x,y);var ax=adj.x,ay=adj.y;var el=this.getPositionEl();if(ax!==undefined||ay!==undefined){if(ax!==undefined&&ay!==undefined){el.setLeftTop(ax,ay);}else if(ax!==undefined){el.setLeft(ax);}else if(ay!==undefined){el.setTop(ay);}
this.onPosition(ax,ay);this.fireEvent('move',this,ax,ay);}
return this;},setPagePosition:function(x,y){if(x&&typeof x[1]=='number'){y=x[1];x=x[0];}
this.pageX=x;this.pageY=y;if(!this.boxReady){return;}
if(x===undefined||y===undefined){return;}
var p=this.el.translatePoints(x,y);this.setPosition(p.left,p.top);return this;},onRender:function(ct,position){Ext.BoxComponent.superclass.onRender.call(this,ct,position);if(this.resizeEl){this.resizeEl=Ext.get(this.resizeEl);}
if(this.positionEl){this.positionEl=Ext.get(this.positionEl);}},afterRender:function(){Ext.BoxComponent.superclass.afterRender.call(this);this.boxReady=true;this.setSize(this.width,this.height);if(this.x||this.y){this.setPosition(this.x,this.y);}else if(this.pageX||this.pageY){this.setPagePosition(this.pageX,this.pageY);}},syncSize:function(){delete this.lastSize;this.setSize(this.autoWidth?undefined:this.el.getWidth(),this.autoHeight?undefined:this.el.getHeight());return this;},onResize:function(adjWidth,adjHeight,rawWidth,rawHeight){},onPosition:function(x,y){},adjustSize:function(w,h){if(this.autoWidth){w='auto';}
if(this.autoHeight){h='auto';}
return{width:w,height:h};},adjustPosition:function(x,y){return{x:x,y:y};}});Ext.reg('box',Ext.BoxComponent);