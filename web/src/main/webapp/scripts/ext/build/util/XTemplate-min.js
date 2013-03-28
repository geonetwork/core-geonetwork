/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.XTemplate=function(){Ext.XTemplate.superclass.constructor.apply(this,arguments);var s=this.html;s=['<tpl>',s,'</tpl>'].join('');var re=/<tpl\b[^>]*>((?:(?=([^<]+))\2|<(?!tpl\b[^>]*>))*?)<\/tpl>/;var nameRe=/^<tpl\b[^>]*?for="(.*?)"/;var ifRe=/^<tpl\b[^>]*?if="(.*?)"/;var execRe=/^<tpl\b[^>]*?exec="(.*?)"/;var m,id=0;var tpls=[];while(m=s.match(re)){var m2=m[0].match(nameRe);var m3=m[0].match(ifRe);var m4=m[0].match(execRe);var exp=null,fn=null,exec=null;var name=m2&&m2[1]?m2[1]:'';if(m3){exp=m3&&m3[1]?m3[1]:null;if(exp){fn=new Function('values','parent','xindex','xcount','with(values){ return '+(Ext.util.Format.htmlDecode(exp))+'; }');}}
if(m4){exp=m4&&m4[1]?m4[1]:null;if(exp){exec=new Function('values','parent','xindex','xcount','with(values){ '+(Ext.util.Format.htmlDecode(exp))+'; }');}}
if(name){switch(name){case'.':name=new Function('values','parent','with(values){ return values; }');break;case'..':name=new Function('values','parent','with(values){ return parent; }');break;default:name=new Function('values','parent','with(values){ return '+name+'; }');}}
tpls.push({id:id,target:name,exec:exec,test:fn,body:m[1]||''});s=s.replace(m[0],'{xtpl'+id+'}');++id;}
for(var i=tpls.length-1;i>=0;--i){this.compileTpl(tpls[i]);}
this.master=tpls[tpls.length-1];this.tpls=tpls;};Ext.extend(Ext.XTemplate,Ext.Template,{re:/\{([\w-\.\#]+)(?:\:([\w\.]*)(?:\((.*?)?\))?)?(\s?[\+\-\*\\]\s?[\d\.\+\-\*\\\(\)]+)?\}/g,codeRe:/\{\[((?:\\\]|.|\n)*?)\]\}/g,applySubTemplate:function(id,values,parent,xindex,xcount){var t=this.tpls[id];if(t.test&&!t.test.call(this,values,parent,xindex,xcount)){return'';}
if(t.exec&&t.exec.call(this,values,parent,xindex,xcount)){return'';}
var vs=t.target?t.target.call(this,values,parent):values;parent=t.target?values:parent;if(t.target&&Ext.isArray(vs)){var buf=[];for(var i=0,len=vs.length;i<len;i++){buf[buf.length]=t.compiled.call(this,vs[i],parent,i+1,len);}
return buf.join('');}
return t.compiled.call(this,vs,parent,xindex,xcount);},compileTpl:function(tpl){var fm=Ext.util.Format;var useF=this.disableFormats!==true;var sep=Ext.isGecko?"+":",";var fn=function(m,name,format,args,math){if(name.substr(0,4)=='xtpl'){return"'"+sep+'this.applySubTemplate('+name.substr(4)+', values, parent, xindex, xcount)'+sep+"'";}
var v;if(name==='.'){v='values';}else if(name==='#'){v='xindex';}else if(name.indexOf('.')!=-1){v=name;}else{v="values['"+name+"']";}
if(math){v='('+v+math+')';}
if(format&&useF){args=args?','+args:"";if(format.substr(0,5)!="this."){format="fm."+format+'(';}else{format='this.call("'+format.substr(5)+'", ';args=", values";}}else{args='';format="("+v+" === undefined ? '' : ";}
return"'"+sep+format+v+args+")"+sep+"'";};var codeFn=function(m,code){return"'"+sep+'('+code+')'+sep+"'";};var body;if(Ext.isGecko){body="tpl.compiled = function(values, parent, xindex, xcount){ return '"+
tpl.body.replace(/(\r\n|\n)/g,'\\n').replace(/'/g,"\\'").replace(this.re,fn).replace(this.codeRe,codeFn)+"';};";}else{body=["tpl.compiled = function(values, parent, xindex, xcount){ return ['"];body.push(tpl.body.replace(/(\r\n|\n)/g,'\\n').replace(/'/g,"\\'").replace(this.re,fn).replace(this.codeRe,codeFn));body.push("'].join('');};");body=body.join('');}
eval(body);return this;},applyTemplate:function(values){return this.master.compiled.call(this,values,{},1,1);},compile:function(){return this;}});Ext.XTemplate.prototype.apply=Ext.XTemplate.prototype.applyTemplate;Ext.XTemplate.from=function(el){el=Ext.getDom(el);return new Ext.XTemplate(el.value||el.innerHTML);};