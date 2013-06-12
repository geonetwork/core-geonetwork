/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.dd.Registry=function(){var elements={};var handles={};var autoIdSeed=0;var getId=function(el,autogen){if(typeof el=="string"){return el;}
var id=el.id;if(!id&&autogen!==false){id="extdd-"+(++autoIdSeed);el.id=id;}
return id;};return{register:function(el,data){data=data||{};if(typeof el=="string"){el=document.getElementById(el);}
data.ddel=el;elements[getId(el)]=data;if(data.isHandle!==false){handles[data.ddel.id]=data;}
if(data.handles){var hs=data.handles;for(var i=0,len=hs.length;i<len;i++){handles[getId(hs[i])]=data;}}},unregister:function(el){var id=getId(el,false);var data=elements[id];if(data){delete elements[id];if(data.handles){var hs=data.handles;for(var i=0,len=hs.length;i<len;i++){delete handles[getId(hs[i],false)];}}}},getHandle:function(id){if(typeof id!="string"){id=id.id;}
return handles[id];},getHandleFromEvent:function(e){var t=Ext.lib.Event.getTarget(e);return t?handles[t.id]:null;},getTarget:function(id){if(typeof id!="string"){id=id.id;}
return elements[id];},getTargetFromEvent:function(e){var t=Ext.lib.Event.getTarget(e);return t?elements[t.id]||handles[t.id]:null;}};}();