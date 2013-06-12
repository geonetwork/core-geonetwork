/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.menu.ColorItem=function(config){Ext.menu.ColorItem.superclass.constructor.call(this,new Ext.ColorPalette(config),config);this.palette=this.component;this.palette.purgeListeners();this.relayEvents(this.palette,["select"]);if(this.selectHandler){this.on('select',this.selectHandler,this.scope);}};Ext.extend(Ext.menu.ColorItem,Ext.menu.Adapter);