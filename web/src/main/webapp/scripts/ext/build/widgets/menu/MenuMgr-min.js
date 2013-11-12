/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.menu.MenuMgr=function(){var menus,active,groups={},attached=false,lastShow=new Date();function init(){menus={};active=new Ext.util.MixedCollection();Ext.getDoc().addKeyListener(27,function(){if(active.length>0){hideAll();}});}
function hideAll(){if(active&&active.length>0){var c=active.clone();c.each(function(m){m.hide();});}}
function onHide(m){active.remove(m);if(active.length<1){Ext.getDoc().un("mousedown",onMouseDown);attached=false;}}
function onShow(m){var last=active.last();lastShow=new Date();active.add(m);if(!attached){Ext.getDoc().on("mousedown",onMouseDown);attached=true;}
if(m.parentMenu){m.getEl().setZIndex(parseInt(m.parentMenu.getEl().getStyle("z-index"),10)+3);m.parentMenu.activeChild=m;}else if(last&&last.isVisible()){m.getEl().setZIndex(parseInt(last.getEl().getStyle("z-index"),10)+3);}}
function onBeforeHide(m){if(m.activeChild){m.activeChild.hide();}
if(m.autoHideTimer){clearTimeout(m.autoHideTimer);delete m.autoHideTimer;}}
function onBeforeShow(m){var pm=m.parentMenu;if(!pm&&!m.allowOtherMenus){hideAll();}else if(pm&&pm.activeChild){pm.activeChild.hide();}}
function onMouseDown(e){if(lastShow.getElapsed()>50&&active.length>0&&!e.getTarget(".x-menu")){hideAll();}}
function onBeforeCheck(mi,state){if(state){var g=groups[mi.group];for(var i=0,l=g.length;i<l;i++){if(g[i]!=mi){g[i].setChecked(false);}}}}
return{hideAll:function(){hideAll();},register:function(menu){if(!menus){init();}
menus[menu.id]=menu;menu.on("beforehide",onBeforeHide);menu.on("hide",onHide);menu.on("beforeshow",onBeforeShow);menu.on("show",onShow);var g=menu.group;if(g&&menu.events["checkchange"]){if(!groups[g]){groups[g]=[];}
groups[g].push(menu);menu.on("checkchange",onCheck);}},get:function(menu){if(typeof menu=="string"){if(!menus){return null;}
return menus[menu];}else if(menu.events){return menu;}else if(typeof menu.length=='number'){return new Ext.menu.Menu({items:menu});}else{return new Ext.menu.Menu(menu);}},unregister:function(menu){delete menus[menu.id];menu.un("beforehide",onBeforeHide);menu.un("hide",onHide);menu.un("beforeshow",onBeforeShow);menu.un("show",onShow);var g=menu.group;if(g&&menu.events["checkchange"]){groups[g].remove(menu);menu.un("checkchange",onCheck);}},registerCheckable:function(menuItem){var g=menuItem.group;if(g){if(!groups[g]){groups[g]=[];}
groups[g].push(menuItem);menuItem.on("beforecheckchange",onBeforeCheck);}},unregisterCheckable:function(menuItem){var g=menuItem.group;if(g){groups[g].remove(menuItem);menuItem.un("beforecheckchange",onBeforeCheck);}},getCheckedItem:function(groupId){var g=groups[groupId];if(g){for(var i=0,l=g.length;i<l;i++){if(g[i].checked){return g[i];}}}
return null;},setCheckedItem:function(groupId,itemId){var g=groups[groupId];if(g){for(var i=0,l=g.length;i<l;i++){if(g[i].id==itemId){g[i].setChecked(true);}}}
return null;}};}();