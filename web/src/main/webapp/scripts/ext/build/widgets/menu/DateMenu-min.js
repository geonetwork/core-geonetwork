/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.menu.DateMenu=function(config){Ext.menu.DateMenu.superclass.constructor.call(this,config);this.plain=true;var di=new Ext.menu.DateItem(config);this.add(di);this.picker=di.picker;this.relayEvents(di,["select"]);this.on('beforeshow',function(){if(this.picker){this.picker.hideMonthPicker(true);}},this);};Ext.extend(Ext.menu.DateMenu,Ext.menu.Menu,{cls:'x-date-menu',beforeDestroy:function(){this.picker.destroy();}});