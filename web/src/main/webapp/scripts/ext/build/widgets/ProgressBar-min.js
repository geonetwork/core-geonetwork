/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.ProgressBar=Ext.extend(Ext.BoxComponent,{baseCls:'x-progress',animate:false,waitTimer:null,initComponent:function(){Ext.ProgressBar.superclass.initComponent.call(this);this.addEvents("update");},onRender:function(ct,position){Ext.ProgressBar.superclass.onRender.call(this,ct,position);var tpl=new Ext.Template('<div class="{cls}-wrap">','<div class="{cls}-inner">','<div class="{cls}-bar">','<div class="{cls}-text">','<div>&#160;</div>','</div>','</div>','<div class="{cls}-text {cls}-text-back">','<div>&#160;</div>','</div>','</div>','</div>');if(position){this.el=tpl.insertBefore(position,{cls:this.baseCls},true);}else{this.el=tpl.append(ct,{cls:this.baseCls},true);}
if(this.id){this.el.dom.id=this.id;}
var inner=this.el.dom.firstChild;this.progressBar=Ext.get(inner.firstChild);if(this.textEl){this.textEl=Ext.get(this.textEl);delete this.textTopEl;}else{this.textTopEl=Ext.get(this.progressBar.dom.firstChild);var textBackEl=Ext.get(inner.childNodes[1]);this.textTopEl.setStyle("z-index",99).addClass('x-hidden');this.textEl=new Ext.CompositeElement([this.textTopEl.dom.firstChild,textBackEl.dom.firstChild]);this.textEl.setWidth(inner.offsetWidth);}
this.progressBar.setHeight(inner.offsetHeight);},afterRender:function(){Ext.ProgressBar.superclass.afterRender.call(this);if(this.value){this.updateProgress(this.value,this.text);}else{this.updateText(this.text);}},updateProgress:function(value,text,animate){this.value=value||0;if(text){this.updateText(text);}
if(this.rendered){var w=Math.floor(value*this.el.dom.firstChild.offsetWidth);this.progressBar.setWidth(w,animate===true||(animate!==false&&this.animate));if(this.textTopEl){this.textTopEl.removeClass('x-hidden').setWidth(w);}}
this.fireEvent('update',this,value,text);return this;},wait:function(o){if(!this.waitTimer){var scope=this;o=o||{};this.updateText(o.text);this.waitTimer=Ext.TaskMgr.start({run:function(i){var inc=o.increment||10;this.updateProgress(((((i+inc)%inc)+1)*(100/inc))*.01,null,o.animate);},interval:o.interval||1000,duration:o.duration,onStop:function(){if(o.fn){o.fn.apply(o.scope||this);}
this.reset();},scope:scope});}
return this;},isWaiting:function(){return this.waitTimer!=null;},updateText:function(text){this.text=text||'&#160;';if(this.rendered){this.textEl.update(this.text);}
return this;},syncProgressBar:function(){if(this.value){this.updateProgress(this.value,this.text);}
return this;},setSize:function(w,h){Ext.ProgressBar.superclass.setSize.call(this,w,h);if(this.textTopEl){var inner=this.el.dom.firstChild;this.textEl.setSize(inner.offsetWidth,inner.offsetHeight);}
this.syncProgressBar();return this;},reset:function(hide){this.updateProgress(0);if(this.textTopEl){this.textTopEl.addClass('x-hidden');}
if(this.waitTimer){this.waitTimer.onStop=null;Ext.TaskMgr.stop(this.waitTimer);this.waitTimer=null;}
if(hide===true){this.hide();}
return this;}});Ext.reg('progress',Ext.ProgressBar);