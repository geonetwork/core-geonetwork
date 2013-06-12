/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.DomHelper=function(){var tempTableEl=null;var emptyTags=/^(?:br|frame|hr|img|input|link|meta|range|spacer|wbr|area|param|col)$/i;var tableRe=/^table|tbody|tr|td$/i;var createHtml=function(o){if(typeof o=='string'){return o;}
var b="";if(Ext.isArray(o)){for(var i=0,l=o.length;i<l;i++){b+=createHtml(o[i]);}
return b;}
if(!o.tag){o.tag="div";}
b+="<"+o.tag;for(var attr in o){if(attr=="tag"||attr=="children"||attr=="cn"||attr=="html"||typeof o[attr]=="function")continue;if(attr=="style"){var s=o["style"];if(typeof s=="function"){s=s.call();}
if(typeof s=="string"){b+=' style="'+s+'"';}else if(typeof s=="object"){b+=' style="';for(var key in s){if(typeof s[key]!="function"){b+=key+":"+s[key]+";";}}
b+='"';}}else{if(attr=="cls"){b+=' class="'+o["cls"]+'"';}else if(attr=="htmlFor"){b+=' for="'+o["htmlFor"]+'"';}else{b+=" "+attr+'="'+o[attr]+'"';}}}
if(emptyTags.test(o.tag)){b+="/>";}else{b+=">";var cn=o.children||o.cn;if(cn){b+=createHtml(cn);}else if(o.html){b+=o.html;}
b+="</"+o.tag+">";}
return b;};var createDom=function(o,parentNode){var el;if(Ext.isArray(o)){el=document.createDocumentFragment();for(var i=0,l=o.length;i<l;i++){createDom(o[i],el);}}else if(typeof o=="string"){el=document.createTextNode(o);}else{el=document.createElement(o.tag||'div');var useSet=!!el.setAttribute;for(var attr in o){if(attr=="tag"||attr=="children"||attr=="cn"||attr=="html"||attr=="style"||typeof o[attr]=="function")continue;if(attr=="cls"){el.className=o["cls"];}else{if(useSet)el.setAttribute(attr,o[attr]);else el[attr]=o[attr];}}
Ext.DomHelper.applyStyles(el,o.style);var cn=o.children||o.cn;if(cn){createDom(cn,el);}else if(o.html){el.innerHTML=o.html;}}
if(parentNode){parentNode.appendChild(el);}
return el;};var ieTable=function(depth,s,h,e){tempTableEl.innerHTML=[s,h,e].join('');var i=-1,el=tempTableEl;while(++i<depth){el=el.firstChild;}
return el;};var ts='<table>',te='</table>',tbs=ts+'<tbody>',tbe='</tbody>'+te,trs=tbs+'<tr>',tre='</tr>'+tbe;var insertIntoTable=function(tag,where,el,html){if(!tempTableEl){tempTableEl=document.createElement('div');}
var node;var before=null;if(tag=='td'){if(where=='afterbegin'||where=='beforeend'){return;}
if(where=='beforebegin'){before=el;el=el.parentNode;}else{before=el.nextSibling;el=el.parentNode;}
node=ieTable(4,trs,html,tre);}
else if(tag=='tr'){if(where=='beforebegin'){before=el;el=el.parentNode;node=ieTable(3,tbs,html,tbe);}else if(where=='afterend'){before=el.nextSibling;el=el.parentNode;node=ieTable(3,tbs,html,tbe);}else{if(where=='afterbegin'){before=el.firstChild;}
node=ieTable(4,trs,html,tre);}}else if(tag=='tbody'){if(where=='beforebegin'){before=el;el=el.parentNode;node=ieTable(2,ts,html,te);}else if(where=='afterend'){before=el.nextSibling;el=el.parentNode;node=ieTable(2,ts,html,te);}else{if(where=='afterbegin'){before=el.firstChild;}
node=ieTable(3,tbs,html,tbe);}}else{if(where=='beforebegin'||where=='afterend'){return;}
if(where=='afterbegin'){before=el.firstChild;}
node=ieTable(2,ts,html,te);}
el.insertBefore(node,before);return node;};return{useDom:false,markup:function(o){return createHtml(o);},applyStyles:function(el,styles){if(styles){el=Ext.fly(el);if(typeof styles=="string"){var re=/\s?([a-z\-]*)\:\s?([^;]*);?/gi;var matches;while((matches=re.exec(styles))!=null){el.setStyle(matches[1],matches[2]);}}else if(typeof styles=="object"){for(var style in styles){el.setStyle(style,styles[style]);}}else if(typeof styles=="function"){Ext.DomHelper.applyStyles(el,styles.call());}}},insertHtml:function(where,el,html){where=where.toLowerCase();if(el.insertAdjacentHTML){if(tableRe.test(el.tagName)){var rs;if(rs=insertIntoTable(el.tagName.toLowerCase(),where,el,html)){return rs;}}
switch(where){case"beforebegin":el.insertAdjacentHTML('BeforeBegin',html);return el.previousSibling;case"afterbegin":el.insertAdjacentHTML('AfterBegin',html);return el.firstChild;case"beforeend":el.insertAdjacentHTML('BeforeEnd',html);return el.lastChild;case"afterend":el.insertAdjacentHTML('AfterEnd',html);return el.nextSibling;}
throw'Illegal insertion point -> "'+where+'"';}
var range=el.ownerDocument.createRange();var frag;switch(where){case"beforebegin":range.setStartBefore(el);frag=range.createContextualFragment(html);el.parentNode.insertBefore(frag,el);return el.previousSibling;case"afterbegin":if(el.firstChild){range.setStartBefore(el.firstChild);frag=range.createContextualFragment(html);el.insertBefore(frag,el.firstChild);return el.firstChild;}else{el.innerHTML=html;return el.firstChild;}
case"beforeend":if(el.lastChild){range.setStartAfter(el.lastChild);frag=range.createContextualFragment(html);el.appendChild(frag);return el.lastChild;}else{el.innerHTML=html;return el.lastChild;}
case"afterend":range.setStartAfter(el);frag=range.createContextualFragment(html);el.parentNode.insertBefore(frag,el.nextSibling);return el.nextSibling;}
throw'Illegal insertion point -> "'+where+'"';},insertBefore:function(el,o,returnElement){return this.doInsert(el,o,returnElement,"beforeBegin");},insertAfter:function(el,o,returnElement){return this.doInsert(el,o,returnElement,"afterEnd","nextSibling");},insertFirst:function(el,o,returnElement){return this.doInsert(el,o,returnElement,"afterBegin","firstChild");},doInsert:function(el,o,returnElement,pos,sibling){el=Ext.getDom(el);var newNode;if(this.useDom){newNode=createDom(o,null);(sibling==="firstChild"?el:el.parentNode).insertBefore(newNode,sibling?el[sibling]:el);}else{var html=createHtml(o);newNode=this.insertHtml(pos,el,html);}
return returnElement?Ext.get(newNode,true):newNode;},append:function(el,o,returnElement){el=Ext.getDom(el);var newNode;if(this.useDom){newNode=createDom(o,null);el.appendChild(newNode);}else{var html=createHtml(o);newNode=this.insertHtml("beforeEnd",el,html);}
return returnElement?Ext.get(newNode,true):newNode;},overwrite:function(el,o,returnElement){el=Ext.getDom(el);el.innerHTML=createHtml(o);return returnElement?Ext.get(el.firstChild,true):el.firstChild;},createTemplate:function(o){var html=createHtml(o);return new Ext.Template(html);}};}();