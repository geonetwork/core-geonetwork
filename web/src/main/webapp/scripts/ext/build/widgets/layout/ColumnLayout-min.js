/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.layout.ColumnLayout=Ext.extend(Ext.layout.ContainerLayout,{monitorResize:true,extraCls:'x-column',scrollOffset:0,isValidParent:function(c,target){return(c.getPositionEl?c.getPositionEl():c.getEl()).dom.parentNode==this.innerCt.dom;},onLayout:function(ct,target){var cs=ct.items.items,len=cs.length,c,i;if(!this.innerCt){target.addClass('x-column-layout-ct');this.innerCt=target.createChild({cls:'x-column-inner'});this.innerCt.createChild({cls:'x-clear'});}
this.renderAll(ct,this.innerCt);var size=Ext.isIE&&target.dom!=Ext.getBody().dom?target.getStyleSize():target.getViewSize();if(size.width<1&&size.height<1){return;}
var w=size.width-target.getPadding('lr')-this.scrollOffset,h=size.height-target.getPadding('tb'),pw=w;this.innerCt.setWidth(w);for(i=0;i<len;i++){c=cs[i];if(!c.columnWidth){pw-=(c.getSize().width+c.getEl().getMargins('lr'));}}
pw=pw<0?0:pw;for(i=0;i<len;i++){c=cs[i];if(c.columnWidth){c.setSize(Math.floor(c.columnWidth*pw)-c.getEl().getMargins('lr'));}}}});Ext.Container.LAYOUTS['column']=Ext.layout.ColumnLayout;