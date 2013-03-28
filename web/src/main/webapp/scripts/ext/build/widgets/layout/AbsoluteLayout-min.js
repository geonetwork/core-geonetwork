/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.layout.AbsoluteLayout=Ext.extend(Ext.layout.AnchorLayout,{extraCls:'x-abs-layout-item',isForm:false,setContainer:function(ct){Ext.layout.AbsoluteLayout.superclass.setContainer.call(this,ct);if(ct.isXType('form')){this.isForm=true;}},onLayout:function(ct,target){if(this.isForm){ct.body.position();}else{target.position();}
Ext.layout.AbsoluteLayout.superclass.onLayout.call(this,ct,target);},getAnchorViewSize:function(ct,target){return this.isForm?ct.body.getStyleSize():Ext.layout.AbsoluteLayout.superclass.getAnchorViewSize.call(this,ct,target);},isValidParent:function(c,target){return this.isForm?true:Ext.layout.AbsoluteLayout.superclass.isValidParent.call(this,c,target);},adjustWidthAnchor:function(value,comp){return value?value-comp.getPosition(true)[0]:value;},adjustHeightAnchor:function(value,comp){return value?value-comp.getPosition(true)[1]:value;}});Ext.Container.LAYOUTS['absolute']=Ext.layout.AbsoluteLayout;