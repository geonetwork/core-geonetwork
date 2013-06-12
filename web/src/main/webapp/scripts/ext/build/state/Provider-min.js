/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.state.Provider=function(){this.addEvents("statechange");this.state={};Ext.state.Provider.superclass.constructor.call(this);};Ext.extend(Ext.state.Provider,Ext.util.Observable,{get:function(name,defaultValue){return typeof this.state[name]=="undefined"?defaultValue:this.state[name];},clear:function(name){delete this.state[name];this.fireEvent("statechange",this,name,null);},set:function(name,value){this.state[name]=value;this.fireEvent("statechange",this,name,value);},decodeValue:function(cookie){var re=/^(a|n|d|b|s|o)\:(.*)$/;var matches=re.exec(unescape(cookie));if(!matches||!matches[1])return;var type=matches[1];var v=matches[2];switch(type){case"n":return parseFloat(v);case"d":return new Date(Date.parse(v));case"b":return(v=="1");case"a":var all=[];var values=v.split("^");for(var i=0,len=values.length;i<len;i++){all.push(this.decodeValue(values[i]));}
return all;case"o":var all={};var values=v.split("^");for(var i=0,len=values.length;i<len;i++){var kv=values[i].split("=");all[kv[0]]=this.decodeValue(kv[1]);}
return all;default:return v;}},encodeValue:function(v){var enc;if(typeof v=="number"){enc="n:"+v;}else if(typeof v=="boolean"){enc="b:"+(v?"1":"0");}else if(Ext.isDate(v)){enc="d:"+v.toGMTString();}else if(Ext.isArray(v)){var flat="";for(var i=0,len=v.length;i<len;i++){flat+=this.encodeValue(v[i]);if(i!=len-1)flat+="^";}
enc="a:"+flat;}else if(typeof v=="object"){var flat="";for(var key in v){if(typeof v[key]!="function"&&v[key]!==undefined){flat+=key+"="+this.encodeValue(v[key])+"^";}}
enc="o:"+flat.substring(0,flat.length-1);}else{enc="s:"+v;}
return escape(enc);}});