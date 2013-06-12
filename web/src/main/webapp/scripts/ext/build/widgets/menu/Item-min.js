/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.menu.Item=function(config){Ext.menu.Item.superclass.constructor.call(this,config);if(this.menu){this.menu=Ext.menu.MenuMgr.get(this.menu);}};Ext.extend(Ext.menu.Item,Ext.menu.BaseItem,{itemCls:"x-menu-item",canActivate:true,showDelay:200,hideDelay:200,ctype:"Ext.menu.Item",onRender:function(container,position){var el=document.createElement("a");el.hideFocus=true;el.unselectable="on";el.href=this.href||"#";if(this.hrefTarget){el.target=this.hrefTarget;}
el.className=this.itemCls+(this.menu?" x-menu-item-arrow":"")+(this.cls?" "+this.cls:"");el.innerHTML=String.format('<img src="{0}" class="x-menu-item-icon {2}" />{1}',this.icon||Ext.BLANK_IMAGE_URL,this.itemText||this.text,this.iconCls||'');this.el=el;Ext.menu.Item.superclass.onRender.call(this,container,position);},setText:function(text){this.text=text;if(this.rendered){this.el.update(String.format('<img src="{0}" class="x-menu-item-icon {2}">{1}',this.icon||Ext.BLANK_IMAGE_URL,this.text,this.iconCls||''));this.parentMenu.autoWidth();}},setIconClass:function(cls){var oldCls=this.iconCls;this.iconCls=cls;if(this.rendered){this.el.child('img.x-menu-item-icon').replaceClass(oldCls,this.iconCls);}},beforeDestroy:function(){if(this.menu){this.menu.destroy();}
Ext.menu.Item.superclass.beforeDestroy.call(this);},handleClick:function(e){if(!this.href){e.stopEvent();}
Ext.menu.Item.superclass.handleClick.apply(this,arguments);},activate:function(autoExpand){if(Ext.menu.Item.superclass.activate.apply(this,arguments)){this.focus();if(autoExpand){this.expandMenu();}}
return true;},shouldDeactivate:function(e){if(Ext.menu.Item.superclass.shouldDeactivate.call(this,e)){if(this.menu&&this.menu.isVisible()){return!this.menu.getEl().getRegion().contains(e.getPoint());}
return true;}
return false;},deactivate:function(){Ext.menu.Item.superclass.deactivate.apply(this,arguments);this.hideMenu();},expandMenu:function(autoActivate){if(!this.disabled&&this.menu){clearTimeout(this.hideTimer);delete this.hideTimer;if(!this.menu.isVisible()&&!this.showTimer){this.showTimer=this.deferExpand.defer(this.showDelay,this,[autoActivate]);}else if(this.menu.isVisible()&&autoActivate){this.menu.tryActivate(0,1);}}},deferExpand:function(autoActivate){delete this.showTimer;this.menu.show(this.container,this.parentMenu.subMenuAlign||"tl-tr?",this.parentMenu);if(autoActivate){this.menu.tryActivate(0,1);}},hideMenu:function(){clearTimeout(this.showTimer);delete this.showTimer;if(!this.hideTimer&&this.menu&&this.menu.isVisible()){this.hideTimer=this.deferHide.defer(this.hideDelay,this);}},deferHide:function(){delete this.hideTimer;if(this.menu.over){this.parentMenu.setActiveItem(this,false);}else{this.menu.hide();}}});