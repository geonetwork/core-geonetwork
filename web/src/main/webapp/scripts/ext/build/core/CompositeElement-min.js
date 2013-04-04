/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.CompositeElement=function(els){this.elements=[];this.addElements(els);};Ext.CompositeElement.prototype={isComposite:true,addElements:function(els){if(!els)return this;if(typeof els=="string"){els=Ext.Element.selectorFunction(els);}
var yels=this.elements;var index=yels.length-1;for(var i=0,len=els.length;i<len;i++){yels[++index]=Ext.get(els[i]);}
return this;},fill:function(els){this.elements=[];this.add(els);return this;},filter:function(selector){var els=[];this.each(function(el){if(el.is(selector)){els[els.length]=el.dom;}});this.fill(els);return this;},invoke:function(fn,args){var els=this.elements;for(var i=0,len=els.length;i<len;i++){Ext.Element.prototype[fn].apply(els[i],args);}
return this;},add:function(els){if(typeof els=="string"){this.addElements(Ext.Element.selectorFunction(els));}else if(els.length!==undefined){this.addElements(els);}else{this.addElements([els]);}
return this;},each:function(fn,scope){var els=this.elements;for(var i=0,len=els.length;i<len;i++){if(fn.call(scope||els[i],els[i],this,i)===false){break;}}
return this;},item:function(index){return this.elements[index]||null;},first:function(){return this.item(0);},last:function(){return this.item(this.elements.length-1);},getCount:function(){return this.elements.length;},contains:function(el){return this.indexOf(el)!==-1;},indexOf:function(el){return this.elements.indexOf(Ext.get(el));},removeElement:function(el,removeDom){if(Ext.isArray(el)){for(var i=0,len=el.length;i<len;i++){this.removeElement(el[i]);}
return this;}
var index=typeof el=='number'?el:this.indexOf(el);if(index!==-1&&this.elements[index]){if(removeDom){var d=this.elements[index];if(d.dom){d.remove();}else{Ext.removeNode(d);}}
this.elements.splice(index,1);}
return this;},replaceElement:function(el,replacement,domReplace){var index=typeof el=='number'?el:this.indexOf(el);if(index!==-1){if(domReplace){this.elements[index].replaceWith(replacement);}else{this.elements.splice(index,1,Ext.get(replacement))}}
return this;},clear:function(){this.elements=[];}};(function(){Ext.CompositeElement.createCall=function(proto,fnName){if(!proto[fnName]){proto[fnName]=function(){return this.invoke(fnName,arguments);};}};for(var fnName in Ext.Element.prototype){if(typeof Ext.Element.prototype[fnName]=="function"){Ext.CompositeElement.createCall(Ext.CompositeElement.prototype,fnName);}};})();Ext.CompositeElementLite=function(els){Ext.CompositeElementLite.superclass.constructor.call(this,els);this.el=new Ext.Element.Flyweight();};Ext.extend(Ext.CompositeElementLite,Ext.CompositeElement,{addElements:function(els){if(els){if(Ext.isArray(els)){this.elements=this.elements.concat(els);}else{var yels=this.elements;var index=yels.length-1;for(var i=0,len=els.length;i<len;i++){yels[++index]=els[i];}}}
return this;},invoke:function(fn,args){var els=this.elements;var el=this.el;for(var i=0,len=els.length;i<len;i++){el.dom=els[i];Ext.Element.prototype[fn].apply(el,args);}
return this;},item:function(index){if(!this.elements[index]){return null;}
this.el.dom=this.elements[index];return this.el;},addListener:function(eventName,handler,scope,opt){var els=this.elements;for(var i=0,len=els.length;i<len;i++){Ext.EventManager.on(els[i],eventName,handler,scope||els[i],opt);}
return this;},each:function(fn,scope){var els=this.elements;var el=this.el;for(var i=0,len=els.length;i<len;i++){el.dom=els[i];if(fn.call(scope||el,el,this,i)===false){break;}}
return this;},indexOf:function(el){return this.elements.indexOf(Ext.getDom(el));},replaceElement:function(el,replacement,domReplace){var index=typeof el=='number'?el:this.indexOf(el);if(index!==-1){replacement=Ext.getDom(replacement);if(domReplace){var d=this.elements[index];d.parentNode.insertBefore(replacement,d);Ext.removeNode(d);}
this.elements.splice(index,1,replacement);}
return this;}});Ext.CompositeElementLite.prototype.on=Ext.CompositeElementLite.prototype.addListener;if(Ext.DomQuery){Ext.Element.selectorFunction=Ext.DomQuery.select;}
Ext.Element.select=function(selector,unique,root){var els;if(typeof selector=="string"){els=Ext.Element.selectorFunction(selector,root);}else if(selector.length!==undefined){els=selector;}else{throw"Invalid selector";}
if(unique===true){return new Ext.CompositeElement(els);}else{return new Ext.CompositeElementLite(els);}};Ext.select=Ext.Element.select;