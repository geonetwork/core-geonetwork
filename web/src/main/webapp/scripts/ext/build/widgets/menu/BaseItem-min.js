/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.menu.BaseItem=function(config){Ext.menu.BaseItem.superclass.constructor.call(this,config);this.addEvents('click','activate','deactivate');if(this.handler){this.on("click",this.handler,this.scope);}};Ext.extend(Ext.menu.BaseItem,Ext.Component,{canActivate:false,activeClass:"x-menu-item-active",hideOnClick:true,hideDelay:100,ctype:"Ext.menu.BaseItem",actionMode:"container",destroy:function(){if(this.menu){this.menu.destroy();}
Ext.menu.BaseItem.superclass.destroy.call(this);},render:function(container,parentMenu){this.parentMenu=parentMenu;Ext.menu.BaseItem.superclass.render.call(this,container);this.container.menuItemId=this.id;},onRender:function(container,position){this.el=Ext.get(this.el);if(this.id){this.el.id=this.id;}
container.dom.appendChild(this.el.dom);},setHandler:function(handler,scope){if(this.handler){this.un("click",this.handler,this.scope);}
this.on("click",this.handler=handler,this.scope=scope);},onClick:function(e){if(!this.disabled&&this.fireEvent("click",this,e)!==false&&this.parentMenu.fireEvent("itemclick",this,e)!==false){this.handleClick(e);}else{e.stopEvent();}},activate:function(){if(this.disabled){return false;}
var li=this.container;li.addClass(this.activeClass);this.region=li.getRegion().adjust(2,2,-2,-2);this.fireEvent("activate",this);return true;},deactivate:function(){this.container.removeClass(this.activeClass);this.fireEvent("deactivate",this);},shouldDeactivate:function(e){return!this.region||!this.region.contains(e.getPoint());},handleClick:function(e){if(this.hideOnClick){this.parentMenu.hide.defer(this.hideDelay,this.parentMenu,[true]);}},expandMenu:function(autoActivate){},hideMenu:function(){}});