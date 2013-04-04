/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.WindowGroup=function(){var list={};var accessList=[];var front=null;var sortWindows=function(d1,d2){return(!d1._lastAccess||d1._lastAccess<d2._lastAccess)?-1:1;};var orderWindows=function(){var a=accessList,len=a.length;if(len>0){a.sort(sortWindows);var seed=a[0].manager.zseed;for(var i=0;i<len;i++){var win=a[i];if(win&&!win.hidden){win.setZIndex(seed+(i*10));}}}
activateLast();};var setActiveWin=function(win){if(win!=front){if(front){front.setActive(false);}
front=win;if(win){win.setActive(true);}}};var activateLast=function(){for(var i=accessList.length-1;i>=0;--i){if(!accessList[i].hidden){setActiveWin(accessList[i]);return;}}
setActiveWin(null);};return{zseed:9000,register:function(win){list[win.id]=win;accessList.push(win);win.on('hide',activateLast);},unregister:function(win){delete list[win.id];win.un('hide',activateLast);accessList.remove(win);},get:function(id){return typeof id=="object"?id:list[id];},bringToFront:function(win){win=this.get(win);if(win!=front){win._lastAccess=new Date().getTime();orderWindows();return true;}
return false;},sendToBack:function(win){win=this.get(win);win._lastAccess=-(new Date().getTime());orderWindows();return win;},hideAll:function(){for(var id in list){if(list[id]&&typeof list[id]!="function"&&list[id].isVisible()){list[id].hide();}}},getActive:function(){return front;},getBy:function(fn,scope){var r=[];for(var i=accessList.length-1;i>=0;--i){var win=accessList[i];if(fn.call(scope||win,win)!==false){r.push(win);}}
return r;},each:function(fn,scope){for(var id in list){if(list[id]&&typeof list[id]!="function"){if(fn.call(scope||list[id],list[id])===false){return;}}}}};};Ext.WindowMgr=new Ext.WindowGroup();