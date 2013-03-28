/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.Button=Ext.extend(Ext.Component,{hidden:false,disabled:false,pressed:false,enableToggle:false,menuAlign:"tl-bl?",type:'button',menuClassTarget:'tr',clickEvent:'click',handleMouseEvents:true,tooltipType:'qtip',buttonSelector:"button:first-child",initComponent:function(){Ext.Button.superclass.initComponent.call(this);this.addEvents("click","toggle",'mouseover','mouseout','menushow','menuhide','menutriggerover','menutriggerout');if(this.menu){this.menu=Ext.menu.MenuMgr.get(this.menu);}
if(typeof this.toggleGroup==='string'){this.enableToggle=true;}},onRender:function(ct,position){if(!this.template){if(!Ext.Button.buttonTemplate){Ext.Button.buttonTemplate=new Ext.Template('<table border="0" cellpadding="0" cellspacing="0" class="x-btn-wrap"><tbody><tr>','<td class="x-btn-left"><i>&#160;</i></td><td class="x-btn-center"><em unselectable="on"><button class="x-btn-text" type="{1}">{0}</button></em></td><td class="x-btn-right"><i>&#160;</i></td>',"</tr></tbody></table>");}
this.template=Ext.Button.buttonTemplate;}
var btn,targs=[this.text||'&#160;',this.type];if(position){btn=this.template.insertBefore(position,targs,true);}else{btn=this.template.append(ct,targs,true);}
var btnEl=this.btnEl=btn.child(this.buttonSelector);btnEl.on('focus',this.onFocus,this);btnEl.on('blur',this.onBlur,this);this.initButtonEl(btn,btnEl);if(this.menu){this.el.child(this.menuClassTarget).addClass("x-btn-with-menu");}
Ext.ButtonToggleMgr.register(this);},initButtonEl:function(btn,btnEl){this.el=btn;btn.addClass("x-btn");if(this.id){var d=this.el.dom,c=Ext.Element.cache;delete c[d.id];d.id=this.el.id=this.id;c[d.id]=this.el;}
if(this.icon){btnEl.setStyle('background-image','url('+this.icon+')');}
if(this.iconCls){btnEl.addClass(this.iconCls);if(!this.cls){btn.addClass(this.text?'x-btn-text-icon':'x-btn-icon');}}
if(this.tabIndex!==undefined){btnEl.dom.tabIndex=this.tabIndex;}
if(this.tooltip){if(typeof this.tooltip=='object'){Ext.QuickTips.register(Ext.apply({target:btnEl.id},this.tooltip));}else{btnEl.dom[this.tooltipType]=this.tooltip;}}
if(this.pressed){this.el.addClass("x-btn-pressed");}
if(this.handleMouseEvents){btn.on("mouseover",this.onMouseOver,this);btn.on("mousedown",this.onMouseDown,this);}
if(this.menu){this.menu.on("show",this.onMenuShow,this);this.menu.on("hide",this.onMenuHide,this);}
if(this.repeat){var repeater=new Ext.util.ClickRepeater(btn,typeof this.repeat=="object"?this.repeat:{});repeater.on("click",this.onClick,this);}
btn.on(this.clickEvent,this.onClick,this);},afterRender:function(){Ext.Button.superclass.afterRender.call(this);if(Ext.isIE6){this.autoWidth.defer(1,this);}else{this.autoWidth();}},setIconClass:function(cls){if(this.el){this.btnEl.replaceClass(this.iconCls,cls);}
this.iconCls=cls;},beforeDestroy:function(){if(this.rendered){if(this.btnEl){if(typeof this.tooltip=='object'){Ext.QuickTips.unregister(this.btnEl);}
Ext.destroy(this.btnEl);}}
Ext.destroy(this.menu,this.repeater);},onDestroy:function(){var doc=Ext.getDoc();doc.un('mouseover',this.monitorMouseOver,this);doc.un('mouseup',this.onMouseUp,this);if(this.rendered){Ext.ButtonToggleMgr.unregister(this);}},autoWidth:function(){if(this.el){this.el.setWidth("auto");if(Ext.isIE7&&Ext.isStrict){var ib=this.btnEl;if(ib&&ib.getWidth()>20){ib.clip();ib.setWidth(Ext.util.TextMetrics.measure(ib,this.text).width+ib.getFrameWidth('lr'));}}
if(this.minWidth){if(this.el.getWidth()<this.minWidth){this.el.setWidth(this.minWidth);}}}},setHandler:function(handler,scope){this.handler=handler;this.scope=scope;},setText:function(text){this.text=text;if(this.el){this.el.child("td.x-btn-center "+this.buttonSelector).update(text);}
this.autoWidth();},getText:function(){return this.text;},toggle:function(state,suppressEvent){state=state===undefined?!this.pressed:!!state;if(state!=this.pressed){if(this.rendered){this.el[state?'addClass':'removeClass']("x-btn-pressed");}
this.pressed=state;if(!suppressEvent){this.fireEvent("toggle",this,state);if(this.toggleHandler){this.toggleHandler.call(this.scope||this,this,state);}}}},focus:function(){this.btnEl.focus();},onDisable:function(){this.onDisableChange(true);},onEnable:function(){this.onDisableChange(false);},onDisableChange:function(disabled){if(this.el){if(!Ext.isIE6||!this.text){this.el[disabled?'addClass':'removeClass'](this.disabledClass);}
this.el.dom.disabled=disabled;}
this.disabled=disabled;},showMenu:function(){if(this.menu){this.menu.show(this.el,this.menuAlign);}
return this;},hideMenu:function(){if(this.menu){this.menu.hide();}
return this;},hasVisibleMenu:function(){return this.menu&&this.menu.isVisible();},onClick:function(e){if(e){e.preventDefault();}
if(e.button!=0){return;}
if(!this.disabled){if(this.enableToggle&&(this.allowDepress!==false||!this.pressed)){this.toggle();}
if(this.menu&&!this.menu.isVisible()&&!this.ignoreNextClick){this.showMenu();}
this.fireEvent("click",this,e);if(this.handler){this.handler.call(this.scope||this,this,e);}}},isMenuTriggerOver:function(e,internal){return this.menu&&!internal;},isMenuTriggerOut:function(e,internal){return this.menu&&!internal;},onMouseOver:function(e){if(!this.disabled){var internal=e.within(this.el,true);if(!internal){this.el.addClass("x-btn-over");if(!this.monitoringMouseOver){Ext.getDoc().on('mouseover',this.monitorMouseOver,this);this.monitoringMouseOver=true;}
this.fireEvent('mouseover',this,e);}
if(this.isMenuTriggerOver(e,internal)){this.fireEvent('menutriggerover',this,this.menu,e);}}},monitorMouseOver:function(e){if(e.target!=this.el.dom&&!e.within(this.el)){if(this.monitoringMouseOver){Ext.getDoc().un('mouseover',this.monitorMouseOver,this);this.monitoringMouseOver=false;}
this.onMouseOut(e);}},onMouseOut:function(e){var internal=e.within(this.el)&&e.target!=this.el.dom;this.el.removeClass("x-btn-over");this.fireEvent('mouseout',this,e);if(this.isMenuTriggerOut(e,internal)){this.fireEvent('menutriggerout',this,this.menu,e);}},onFocus:function(e){if(!this.disabled){this.el.addClass("x-btn-focus");}},onBlur:function(e){this.el.removeClass("x-btn-focus");},getClickEl:function(e,isUp){return this.el;},onMouseDown:function(e){if(!this.disabled&&e.button==0){this.getClickEl(e).addClass("x-btn-click");Ext.getDoc().on('mouseup',this.onMouseUp,this);}},onMouseUp:function(e){if(e.button==0){this.getClickEl(e,true).removeClass("x-btn-click");Ext.getDoc().un('mouseup',this.onMouseUp,this);}},onMenuShow:function(e){this.ignoreNextClick=0;this.el.addClass("x-btn-menu-active");this.fireEvent('menushow',this,this.menu);},onMenuHide:function(e){this.el.removeClass("x-btn-menu-active");this.ignoreNextClick=this.restoreClick.defer(250,this);this.fireEvent('menuhide',this,this.menu);},restoreClick:function(){this.ignoreNextClick=0;}});Ext.reg('button',Ext.Button);Ext.ButtonToggleMgr=function(){var groups={};function toggleGroup(btn,state){if(state){var g=groups[btn.toggleGroup];for(var i=0,l=g.length;i<l;i++){if(g[i]!=btn){g[i].toggle(false);}}}}
return{register:function(btn){if(!btn.toggleGroup){return;}
var g=groups[btn.toggleGroup];if(!g){g=groups[btn.toggleGroup]=[];}
g.push(btn);btn.on("toggle",toggleGroup);},unregister:function(btn){if(!btn.toggleGroup){return;}
var g=groups[btn.toggleGroup];if(g){g.remove(btn);btn.un("toggle",toggleGroup);}}};}();