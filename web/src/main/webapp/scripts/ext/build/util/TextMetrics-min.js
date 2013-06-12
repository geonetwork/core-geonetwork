/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.util.TextMetrics=function(){var shared;return{measure:function(el,text,fixedWidth){if(!shared){shared=Ext.util.TextMetrics.Instance(el,fixedWidth);}
shared.bind(el);shared.setFixedWidth(fixedWidth||'auto');return shared.getSize(text);},createInstance:function(el,fixedWidth){return Ext.util.TextMetrics.Instance(el,fixedWidth);}};}();Ext.util.TextMetrics.Instance=function(bindTo,fixedWidth){var ml=new Ext.Element(document.createElement('div'));document.body.appendChild(ml.dom);ml.position('absolute');ml.setLeftTop(-1000,-1000);ml.hide();if(fixedWidth){ml.setWidth(fixedWidth);}
var instance={getSize:function(text){ml.update(text);var s=ml.getSize();ml.update('');return s;},bind:function(el){ml.setStyle(Ext.fly(el).getStyles('font-size','font-style','font-weight','font-family','line-height','text-transform','letter-spacing'));},setFixedWidth:function(width){ml.setWidth(width);},getWidth:function(text){ml.dom.style.width='auto';return this.getSize(text).width;},getHeight:function(text){return this.getSize(text).height;}};instance.bind(bindTo);return instance;};Ext.Element.measureText=Ext.util.TextMetrics.measure;