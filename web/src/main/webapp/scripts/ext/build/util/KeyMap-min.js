/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.KeyMap=function(el,config,eventName){this.el=Ext.get(el);this.eventName=eventName||"keydown";this.bindings=[];if(config){this.addBinding(config);}
this.enable();};Ext.KeyMap.prototype={stopEvent:false,addBinding:function(config){if(Ext.isArray(config)){for(var i=0,len=config.length;i<len;i++){this.addBinding(config[i]);}
return;}
var keyCode=config.key,shift=config.shift,ctrl=config.ctrl,alt=config.alt,fn=config.fn||config.handler,scope=config.scope;if(config.stopEvent){this.stopEvent=config.stopEvent;}
if(typeof keyCode=="string"){var ks=[];var keyString=keyCode.toUpperCase();for(var j=0,len=keyString.length;j<len;j++){ks.push(keyString.charCodeAt(j));}
keyCode=ks;}
var keyArray=Ext.isArray(keyCode);var handler=function(e){if((!shift||e.shiftKey)&&(!ctrl||e.ctrlKey)&&(!alt||e.altKey)){var k=e.getKey();if(keyArray){for(var i=0,len=keyCode.length;i<len;i++){if(keyCode[i]==k){if(this.stopEvent){e.stopEvent();}
fn.call(scope||window,k,e);return;}}}else{if(k==keyCode){if(this.stopEvent){e.stopEvent();}
fn.call(scope||window,k,e);}}}};this.bindings.push(handler);},on:function(key,fn,scope){var keyCode,shift,ctrl,alt;if(typeof key=="object"&&!Ext.isArray(key)){keyCode=key.key;shift=key.shift;ctrl=key.ctrl;alt=key.alt;}else{keyCode=key;}
this.addBinding({key:keyCode,shift:shift,ctrl:ctrl,alt:alt,fn:fn,scope:scope})},handleKeyDown:function(e){if(this.enabled){var b=this.bindings;for(var i=0,len=b.length;i<len;i++){b[i].call(this,e);}}},isEnabled:function(){return this.enabled;},enable:function(){if(!this.enabled){this.el.on(this.eventName,this.handleKeyDown,this);this.enabled=true;}},disable:function(){if(this.enabled){this.el.removeListener(this.eventName,this.handleKeyDown,this);this.enabled=false;}}};