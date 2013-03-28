/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.menu.Menu=function(config){if(Ext.isArray(config)){config={items:config};}
Ext.apply(this,config);this.id=this.id||Ext.id();this.addEvents('beforeshow','beforehide','show','hide','click','mouseover','mouseout','itemclick');Ext.menu.MenuMgr.register(this);Ext.menu.Menu.superclass.constructor.call(this);var mis=this.items;this.items=new Ext.util.MixedCollection();if(mis){this.add.apply(this,mis);}};Ext.extend(Ext.menu.Menu,Ext.util.Observable,{minWidth:120,shadow:"sides",subMenuAlign:"tl-tr?",defaultAlign:"tl-bl?",allowOtherMenus:false,ignoreParentClicks:false,hidden:true,createEl:function(){return new Ext.Layer({cls:"x-menu",shadow:this.shadow,constrain:false,parentEl:this.parentEl||document.body,zindex:15000});},render:function(){if(this.el){return;}
var el=this.el=this.createEl();if(!this.keyNav){this.keyNav=new Ext.menu.MenuNav(this);}
if(this.plain){el.addClass("x-menu-plain");}
if(this.cls){el.addClass(this.cls);}
this.focusEl=el.createChild({tag:"a",cls:"x-menu-focus",href:"#",onclick:"return false;",tabIndex:"-1"});var ul=el.createChild({tag:"ul",cls:"x-menu-list"});ul.on("click",this.onClick,this);ul.on("mouseover",this.onMouseOver,this);ul.on("mouseout",this.onMouseOut,this);this.items.each(function(item){var li=document.createElement("li");li.className="x-menu-list-item";ul.dom.appendChild(li);item.render(li,this);},this);this.ul=ul;this.autoWidth();},autoWidth:function(){var el=this.el,ul=this.ul;if(!el){return;}
var w=this.width;if(w){el.setWidth(w);}else if(Ext.isIE&&!Ext.isIE8){el.setWidth(this.minWidth);var t=el.dom.offsetWidth;el.setWidth(ul.getWidth()+el.getFrameWidth("lr"));}},delayAutoWidth:function(){if(this.el){if(!this.awTask){this.awTask=new Ext.util.DelayedTask(this.autoWidth,this);}
this.awTask.delay(20);}},findTargetItem:function(e){var t=e.getTarget(".x-menu-list-item",this.ul,true);if(t&&t.menuItemId){return this.items.get(t.menuItemId);}},onClick:function(e){var t;if(t=this.findTargetItem(e)){if(t.menu&&this.ignoreParentClicks){t.expandMenu();}else{t.onClick(e);this.fireEvent("click",this,t,e);}}},setActiveItem:function(item,autoExpand){if(item!=this.activeItem){if(this.activeItem){this.activeItem.deactivate();}
this.activeItem=item;item.activate(autoExpand);}else if(autoExpand){item.expandMenu();}},tryActivate:function(start,step){var items=this.items;for(var i=start,len=items.length;i>=0&&i<len;i+=step){var item=items.get(i);if(!item.disabled&&item.canActivate){this.setActiveItem(item,false);return item;}}
return false;},onMouseOver:function(e){var t;if(t=this.findTargetItem(e)){if(t.canActivate&&!t.disabled){this.setActiveItem(t,true);}}
this.over=true;this.fireEvent("mouseover",this,e,t);},onMouseOut:function(e){var t;if(t=this.findTargetItem(e)){if(t==this.activeItem&&t.shouldDeactivate(e)){this.activeItem.deactivate();delete this.activeItem;}}
this.over=false;this.fireEvent("mouseout",this,e,t);},isVisible:function(){return this.el&&!this.hidden;},show:function(el,pos,parentMenu){this.parentMenu=parentMenu;if(!this.el){this.render();}
this.fireEvent("beforeshow",this);this.showAt(this.el.getAlignToXY(el,pos||this.defaultAlign),parentMenu,false);},showAt:function(xy,parentMenu,_e){this.parentMenu=parentMenu;if(!this.el){this.render();}
if(_e!==false){this.fireEvent("beforeshow",this);xy=this.el.adjustForConstraints(xy);}
this.el.setXY(xy);this.el.show();this.hidden=false;this.focus();this.fireEvent("show",this);},focus:function(){if(!this.hidden){this.doFocus.defer(50,this);}},doFocus:function(){if(!this.hidden){this.focusEl.focus();}},hide:function(deep){if(this.el&&this.isVisible()){this.fireEvent("beforehide",this);if(this.activeItem){this.activeItem.deactivate();this.activeItem=null;}
this.el.hide();this.hidden=true;this.fireEvent("hide",this);}
if(deep===true&&this.parentMenu){this.parentMenu.hide(true);}},add:function(){var a=arguments,l=a.length,item;for(var i=0;i<l;i++){var el=a[i];if(el.render){item=this.addItem(el);}else if(typeof el=="string"){if(el=="separator"||el=="-"){item=this.addSeparator();}else{item=this.addText(el);}}else if(el.tagName||el.el){item=this.addElement(el);}else if(typeof el=="object"){Ext.applyIf(el,this.defaults);item=this.addMenuItem(el);}}
return item;},getEl:function(){if(!this.el){this.render();}
return this.el;},addSeparator:function(){return this.addItem(new Ext.menu.Separator());},addElement:function(el){return this.addItem(new Ext.menu.BaseItem(el));},addItem:function(item){this.items.add(item);if(this.ul){var li=document.createElement("li");li.className="x-menu-list-item";this.ul.dom.appendChild(li);item.render(li,this);this.delayAutoWidth();}
return item;},addMenuItem:function(config){if(!(config instanceof Ext.menu.Item)){if(typeof config.checked=="boolean"){config=new Ext.menu.CheckItem(config);}else{config=new Ext.menu.Item(config);}}
return this.addItem(config);},addText:function(text){return this.addItem(new Ext.menu.TextItem(text));},insert:function(index,item){this.items.insert(index,item);if(this.ul){var li=document.createElement("li");li.className="x-menu-list-item";this.ul.dom.insertBefore(li,this.ul.dom.childNodes[index]);item.render(li,this);this.delayAutoWidth();}
return item;},remove:function(item){this.items.removeKey(item.id);item.destroy();},removeAll:function(){if(this.items){var f;while(f=this.items.first()){this.remove(f);}}},destroy:function(){this.beforeDestroy();Ext.menu.MenuMgr.unregister(this);if(this.keyNav){this.keyNav.disable();}
this.removeAll();if(this.ul){this.ul.removeAllListeners();}
if(this.el){this.el.destroy();}},beforeDestroy:Ext.emptyFn});Ext.menu.MenuNav=function(menu){Ext.menu.MenuNav.superclass.constructor.call(this,menu.el);this.scope=this.menu=menu;};Ext.extend(Ext.menu.MenuNav,Ext.KeyNav,{doRelay:function(e,h){var k=e.getKey();if(!this.menu.activeItem&&e.isNavKeyPress()&&k!=e.SPACE&&k!=e.RETURN){this.menu.tryActivate(0,1);return false;}
return h.call(this.scope||this,e,this.menu);},up:function(e,m){if(!m.tryActivate(m.items.indexOf(m.activeItem)-1,-1)){m.tryActivate(m.items.length-1,-1);}},down:function(e,m){if(!m.tryActivate(m.items.indexOf(m.activeItem)+1,1)){m.tryActivate(0,1);}},right:function(e,m){if(m.activeItem){m.activeItem.expandMenu(true);}},left:function(e,m){m.hide();if(m.parentMenu&&m.parentMenu.activeItem){m.parentMenu.activeItem.activate();}},enter:function(e,m){if(m.activeItem){e.stopPropagation();m.activeItem.onClick(e);m.fireEvent("click",this,m.activeItem);return true;}}});