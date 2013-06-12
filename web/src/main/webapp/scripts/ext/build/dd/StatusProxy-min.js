/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.dd.StatusProxy=function(config){Ext.apply(this,config);this.id=this.id||Ext.id();this.el=new Ext.Layer({dh:{id:this.id,tag:"div",cls:"x-dd-drag-proxy "+this.dropNotAllowed,children:[{tag:"div",cls:"x-dd-drop-icon"},{tag:"div",cls:"x-dd-drag-ghost"}]},shadow:!config||config.shadow!==false});this.ghost=Ext.get(this.el.dom.childNodes[1]);this.dropStatus=this.dropNotAllowed;};Ext.dd.StatusProxy.prototype={dropAllowed:"x-dd-drop-ok",dropNotAllowed:"x-dd-drop-nodrop",setStatus:function(cssClass){cssClass=cssClass||this.dropNotAllowed;if(this.dropStatus!=cssClass){this.el.replaceClass(this.dropStatus,cssClass);this.dropStatus=cssClass;}},reset:function(clearGhost){this.el.dom.className="x-dd-drag-proxy "+this.dropNotAllowed;this.dropStatus=this.dropNotAllowed;if(clearGhost){this.ghost.update("");}},update:function(html){if(typeof html=="string"){this.ghost.update(html);}else{this.ghost.update("");html.style.margin="0";this.ghost.dom.appendChild(html);}
var el=this.ghost.dom.firstChild;if(el){Ext.fly(el).setStyle(Ext.isIE?'styleFloat':'cssFloat','none');}},getEl:function(){return this.el;},getGhost:function(){return this.ghost;},hide:function(clear){this.el.hide();if(clear){this.reset(true);}},stop:function(){if(this.anim&&this.anim.isAnimated&&this.anim.isAnimated()){this.anim.stop();}},show:function(){this.el.show();},sync:function(){this.el.sync();},repair:function(xy,callback,scope){this.callback=callback;this.scope=scope;if(xy&&this.animRepair!==false){this.el.addClass("x-dd-drag-repair");this.el.hideUnders(true);this.anim=this.el.shift({duration:this.repairDuration||.5,easing:'easeOut',xy:xy,stopFx:true,callback:this.afterRepair,scope:this});}else{this.afterRepair();}},afterRepair:function(){this.hide(true);if(typeof this.callback=="function"){this.callback.call(this.scope||this);}
this.callback=null;this.scope=null;}};