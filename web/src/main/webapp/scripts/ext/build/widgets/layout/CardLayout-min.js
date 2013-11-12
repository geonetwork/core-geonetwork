/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.layout.CardLayout=Ext.extend(Ext.layout.FitLayout,{deferredRender:false,renderHidden:true,setActiveItem:function(item){item=this.container.getComponent(item);if(this.activeItem!=item){if(this.activeItem){this.activeItem.hide();}
this.activeItem=item;item.show();this.layout();}},renderAll:function(ct,target){if(this.deferredRender){this.renderItem(this.activeItem,undefined,target);}else{Ext.layout.CardLayout.superclass.renderAll.call(this,ct,target);}}});Ext.Container.LAYOUTS['card']=Ext.layout.CardLayout;