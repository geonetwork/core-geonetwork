/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.Template=function(html){var a=arguments;if(Ext.isArray(html)){html=html.join("");}else if(a.length>1){var buf=[];for(var i=0,len=a.length;i<len;i++){if(typeof a[i]=='object'){Ext.apply(this,a[i]);}else{buf[buf.length]=a[i];}}
html=buf.join('');}
this.html=html;if(this.compiled){this.compile();}};Ext.Template.prototype={applyTemplate:function(values){if(this.compiled){return this.compiled(values);}
var useF=this.disableFormats!==true;var fm=Ext.util.Format,tpl=this;var fn=function(m,name,format,args){if(format&&useF){if(format.substr(0,5)=="this."){return tpl.call(format.substr(5),values[name],values);}else{if(args){var re=/^\s*['"](.*)["']\s*$/;args=args.split(',');for(var i=0,len=args.length;i<len;i++){args[i]=args[i].replace(re,"$1");}
args=[values[name]].concat(args);}else{args=[values[name]];}
return fm[format].apply(fm,args);}}else{return values[name]!==undefined?values[name]:"";}};return this.html.replace(this.re,fn);},set:function(html,compile){this.html=html;this.compiled=null;if(compile){this.compile();}
return this;},disableFormats:false,re:/\{([\w-]+)(?:\:([\w\.]*)(?:\((.*?)?\))?)?\}/g,compile:function(){var fm=Ext.util.Format;var useF=this.disableFormats!==true;var sep=Ext.isGecko?"+":",";var fn=function(m,name,format,args){if(format&&useF){args=args?','+args:"";if(format.substr(0,5)!="this."){format="fm."+format+'(';}else{format='this.call("'+format.substr(5)+'", ';args=", values";}}else{args='';format="(values['"+name+"'] == undefined ? '' : ";}
return"'"+sep+format+"values['"+name+"']"+args+")"+sep+"'";};var body;if(Ext.isGecko){body="this.compiled = function(values){ return '"+
this.html.replace(/\\/g,'\\\\').replace(/(\r\n|\n)/g,'\\n').replace(/'/g,"\\'").replace(this.re,fn)+"';};";}else{body=["this.compiled = function(values){ return ['"];body.push(this.html.replace(/\\/g,'\\\\').replace(/(\r\n|\n)/g,'\\n').replace(/'/g,"\\'").replace(this.re,fn));body.push("'].join('');};");body=body.join('');}
eval(body);return this;},call:function(fnName,value,allValues){return this[fnName](value,allValues);},insertFirst:function(el,values,returnElement){return this.doInsert('afterBegin',el,values,returnElement);},insertBefore:function(el,values,returnElement){return this.doInsert('beforeBegin',el,values,returnElement);},insertAfter:function(el,values,returnElement){return this.doInsert('afterEnd',el,values,returnElement);},append:function(el,values,returnElement){return this.doInsert('beforeEnd',el,values,returnElement);},doInsert:function(where,el,values,returnEl){el=Ext.getDom(el);var newNode=Ext.DomHelper.insertHtml(where,el,this.applyTemplate(values));return returnEl?Ext.get(newNode,true):newNode;},overwrite:function(el,values,returnElement){el=Ext.getDom(el);el.innerHTML=this.applyTemplate(values);return returnElement?Ext.get(el.firstChild,true):el.firstChild;}};Ext.Template.prototype.apply=Ext.Template.prototype.applyTemplate;Ext.DomHelper.Template=Ext.Template;Ext.Template.from=function(el,config){el=Ext.getDom(el);return new Ext.Template(el.value||el.innerHTML,config||'');};