/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.layout.FitLayout=Ext.extend(Ext.layout.ContainerLayout,{monitorResize:true,onLayout:function(ct,target){Ext.layout.FitLayout.superclass.onLayout.call(this,ct,target);if(!this.container.collapsed){var sz=(Ext.isIE6&&Ext.isStrict&&target.dom==document.body)?target.getViewSize():target.getStyleSize();this.setItemSize(this.activeItem||ct.items.itemAt(0),sz);}},setItemSize:function(item,size){if(item&&size.height>0){item.setSize(size);}}});Ext.Container.LAYOUTS['fit']=Ext.layout.FitLayout;