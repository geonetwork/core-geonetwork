/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.menu.CheckItem=function(config){Ext.menu.CheckItem.superclass.constructor.call(this,config);this.addEvents("beforecheckchange","checkchange");if(this.checkHandler){this.on('checkchange',this.checkHandler,this.scope);}
Ext.menu.MenuMgr.registerCheckable(this);};Ext.extend(Ext.menu.CheckItem,Ext.menu.Item,{itemCls:"x-menu-item x-menu-check-item",groupClass:"x-menu-group-item",checked:false,ctype:"Ext.menu.CheckItem",onRender:function(c){Ext.menu.CheckItem.superclass.onRender.apply(this,arguments);if(this.group){this.el.addClass(this.groupClass);}
if(this.checked){this.checked=false;this.setChecked(true,true);}},destroy:function(){Ext.menu.MenuMgr.unregisterCheckable(this);Ext.menu.CheckItem.superclass.destroy.apply(this,arguments);},setChecked:function(state,suppressEvent){if(this.checked!=state&&this.fireEvent("beforecheckchange",this,state)!==false){if(this.container){this.container[state?"addClass":"removeClass"]("x-menu-item-checked");}
this.checked=state;if(suppressEvent!==true){this.fireEvent("checkchange",this,state);}}},handleClick:function(e){if(!this.disabled&&!(this.checked&&this.group)){this.setChecked(!this.checked);}
Ext.menu.CheckItem.superclass.handleClick.apply(this,arguments);}});