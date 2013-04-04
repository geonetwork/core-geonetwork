/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.menu.DateItem=function(config){Ext.menu.DateItem.superclass.constructor.call(this,new Ext.DatePicker(config),config);this.picker=this.component;this.picker.purgeListeners();this.addEvents('select');this.picker.on("render",function(picker){picker.getEl().swallowEvent("click");picker.container.addClass("x-menu-date-item");});this.picker.on("select",this.onSelect,this);};Ext.extend(Ext.menu.DateItem,Ext.menu.Adapter,{onSelect:function(picker,date){this.fireEvent("select",this,date,picker);Ext.menu.DateItem.superclass.handleClick.call(this);}});