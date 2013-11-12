/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.util.DelayedTask=function(fn,scope,args){var id=null;var call=function(){id=null;fn.apply(scope,args||[]);};this.delay=function(delay,newFn,newScope,newArgs){if(id){this.cancel();}
fn=newFn||fn;scope=newScope||scope;args=newArgs||args;if(!id){id=setTimeout(call,delay);}};this.cancel=function(){if(id){clearTimeout(id);id=null;}};};