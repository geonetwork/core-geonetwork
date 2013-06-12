/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.state.CookieProvider=function(config){Ext.state.CookieProvider.superclass.constructor.call(this);this.path="/";this.expires=new Date(new Date().getTime()+(1000*60*60*24*7));this.domain=null;this.secure=false;Ext.apply(this,config);this.state=this.readCookies();};Ext.extend(Ext.state.CookieProvider,Ext.state.Provider,{set:function(name,value){if(typeof value=="undefined"||value===null){this.clear(name);return;}
this.setCookie(name,value);Ext.state.CookieProvider.superclass.set.call(this,name,value);},clear:function(name){this.clearCookie(name);Ext.state.CookieProvider.superclass.clear.call(this,name);},readCookies:function(){var cookies={};var c=document.cookie+";";var re=/\s?(.*?)=(.*?);/g;var matches;while((matches=re.exec(c))!=null){var name=matches[1];var value=matches[2];if(name&&name.substring(0,3)=="ys-"){cookies[name.substr(3)]=this.decodeValue(value);}}
return cookies;},setCookie:function(name,value){document.cookie="ys-"+name+"="+this.encodeValue(value)+
((this.expires==null)?"":("; expires="+this.expires.toGMTString()))+
((this.path==null)?"":("; path="+this.path))+
((this.domain==null)?"":("; domain="+this.domain))+
((this.secure==true)?"; secure":"");},clearCookie:function(name){document.cookie="ys-"+name+"=null; expires=Thu, 01-Jan-70 00:00:01 GMT"+
((this.path==null)?"":("; path="+this.path))+
((this.domain==null)?"":("; domain="+this.domain))+
((this.secure==true)?"; secure":"");}});