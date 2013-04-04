/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.Component=function(config){config=config||{};if(config.initialConfig){if(config.isAction){this.baseAction=config;}
config=config.initialConfig;}else if(config.tagName||config.dom||typeof config=="string"){config={applyTo:config,id:config.id||config};}
this.initialConfig=config;Ext.apply(this,config);this.addEvents('disable','enable','beforeshow','show','beforehide','hide','beforerender','render','beforedestroy','destroy','beforestaterestore','staterestore','beforestatesave','statesave');this.getId();Ext.ComponentMgr.register(this);Ext.Component.superclass.constructor.call(this);if(this.baseAction){this.baseAction.addComponent(this);}
this.initComponent();if(this.plugins){if(Ext.isArray(this.plugins)){for(var i=0,len=this.plugins.length;i<len;i++){this.plugins[i]=this.initPlugin(this.plugins[i]);}}else{this.plugins=this.initPlugin(this.plugins);}}
if(this.stateful!==false){this.initState(config);}
if(this.applyTo){this.applyToMarkup(this.applyTo);delete this.applyTo;}else if(this.renderTo){this.render(this.renderTo);delete this.renderTo;}};Ext.Component.AUTO_ID=1000;Ext.extend(Ext.Component,Ext.util.Observable,{disabledClass:"x-item-disabled",allowDomMove:true,autoShow:false,hideMode:'display',hideParent:false,hidden:false,disabled:false,rendered:false,ctype:"Ext.Component",actionMode:"el",getActionEl:function(){return this[this.actionMode];},initPlugin:function(p){p.init(this);return p;},initComponent:Ext.emptyFn,render:function(container,position){if(!this.rendered&&this.fireEvent("beforerender",this)!==false){if(!container&&this.el){this.el=Ext.get(this.el);container=this.el.dom.parentNode;this.allowDomMove=false;}
this.container=Ext.get(container);if(this.ctCls){this.container.addClass(this.ctCls);}
this.rendered=true;if(position!==undefined){if(typeof position=='number'){position=this.container.dom.childNodes[position];}else{position=Ext.getDom(position);}}
this.onRender(this.container,position||null);if(this.autoShow){this.el.removeClass(['x-hidden','x-hide-'+this.hideMode]);}
if(this.cls){this.el.addClass(this.cls);delete this.cls;}
if(this.style){this.el.applyStyles(this.style);delete this.style;}
if(this.overCls){this.el.addClassOnOver(this.overCls);}
this.fireEvent("render",this);this.afterRender(this.container);if(this.hidden){this.hide();}
if(this.disabled){this.disable();}
if(this.stateful!==false){this.initStateEvents();}}
return this;},initState:function(config){if(Ext.state.Manager){var id=this.getStateId();if(id){var state=Ext.state.Manager.get(id);if(state){if(this.fireEvent('beforestaterestore',this,state)!==false){this.applyState(state);this.fireEvent('staterestore',this,state);}}}}},getStateId:function(){return this.stateId||((this.id.indexOf('ext-comp-')==0||this.id.indexOf('ext-gen')==0)?null:this.id);},initStateEvents:function(){if(this.stateEvents){for(var i=0,e;e=this.stateEvents[i];i++){this.on(e,this.saveState,this,{delay:100});}}},applyState:function(state,config){if(state){Ext.apply(this,state);}},getState:function(){return null;},saveState:function(){if(Ext.state.Manager&&this.stateful!==false){var id=this.getStateId();if(id){var state=this.getState();if(this.fireEvent('beforestatesave',this,state)!==false){Ext.state.Manager.set(id,state);this.fireEvent('statesave',this,state);}}}},applyToMarkup:function(el){this.allowDomMove=false;this.el=Ext.get(el);this.render(this.el.dom.parentNode);},addClass:function(cls){if(this.el){this.el.addClass(cls);}else{this.cls=this.cls?this.cls+' '+cls:cls;}},removeClass:function(cls){if(this.el){this.el.removeClass(cls);}else if(this.cls){this.cls=this.cls.split(' ').remove(cls).join(' ');}},onRender:function(ct,position){if(this.autoEl){if(typeof this.autoEl=='string'){this.el=document.createElement(this.autoEl);}else{var div=document.createElement('div');Ext.DomHelper.overwrite(div,this.autoEl);this.el=div.firstChild;}
if(!this.el.id){this.el.id=this.getId();}}
if(this.el){this.el=Ext.get(this.el);if(this.allowDomMove!==false){ct.dom.insertBefore(this.el.dom,position);}}},getAutoCreate:function(){var cfg=typeof this.autoCreate=="object"?this.autoCreate:Ext.apply({},this.defaultAutoCreate);if(this.id&&!cfg.id){cfg.id=this.id;}
return cfg;},afterRender:Ext.emptyFn,destroy:function(){if(this.fireEvent("beforedestroy",this)!==false){this.beforeDestroy();if(this.rendered){this.el.removeAllListeners();this.el.remove();if(this.actionMode=="container"){this.container.remove();}}
this.onDestroy();Ext.ComponentMgr.unregister(this);this.fireEvent("destroy",this);this.purgeListeners();}},beforeDestroy:Ext.emptyFn,onDestroy:Ext.emptyFn,getEl:function(){return this.el;},getId:function(){return this.id||(this.id="ext-comp-"+(++Ext.Component.AUTO_ID));},getItemId:function(){return this.itemId||this.getId();},focus:function(selectText,delay){if(delay){this.focus.defer(typeof delay=='number'?delay:10,this,[selectText,false]);return;}
if(this.rendered){this.el.focus();if(selectText===true){this.el.dom.select();}}
return this;},blur:function(){if(this.rendered){this.el.blur();}
return this;},disable:function(){if(this.rendered){this.onDisable();}
this.disabled=true;this.fireEvent("disable",this);return this;},onDisable:function(){this.getActionEl().addClass(this.disabledClass);this.el.dom.disabled=true;},enable:function(){if(this.rendered){this.onEnable();}
this.disabled=false;this.fireEvent("enable",this);return this;},onEnable:function(){this.getActionEl().removeClass(this.disabledClass);this.el.dom.disabled=false;},setDisabled:function(disabled){this[disabled?"disable":"enable"]();},show:function(){if(this.fireEvent("beforeshow",this)!==false){this.hidden=false;if(this.autoRender){this.render(typeof this.autoRender=='boolean'?Ext.getBody():this.autoRender);}
if(this.rendered){this.onShow();}
this.fireEvent("show",this);}
return this;},onShow:function(){if(this.hideParent){this.container.removeClass('x-hide-'+this.hideMode);}else{this.getActionEl().removeClass('x-hide-'+this.hideMode);}},hide:function(){if(this.fireEvent("beforehide",this)!==false){this.hidden=true;if(this.rendered){this.onHide();}
this.fireEvent("hide",this);}
return this;},onHide:function(){if(this.hideParent){this.container.addClass('x-hide-'+this.hideMode);}else{this.getActionEl().addClass('x-hide-'+this.hideMode);}},setVisible:function(visible){if(visible){this.show();}else{this.hide();}
return this;},isVisible:function(){return this.rendered&&this.getActionEl().isVisible();},cloneConfig:function(overrides){overrides=overrides||{};var id=overrides.id||Ext.id();var cfg=Ext.applyIf(overrides,this.initialConfig);cfg.id=id;return new this.constructor(cfg);},getXType:function(){return this.constructor.xtype;},isXType:function(xtype,shallow){if(typeof xtype=='function'){xtype=xtype.xtype;}else if(typeof xtype=='object'){xtype=xtype.constructor.xtype;}
return!shallow?('/'+this.getXTypes()+'/').indexOf('/'+xtype+'/')!=-1:this.constructor.xtype==xtype;},getXTypes:function(){var tc=this.constructor;if(!tc.xtypes){var c=[],sc=this;while(sc&&sc.constructor.xtype){c.unshift(sc.constructor.xtype);sc=sc.constructor.superclass;}
tc.xtypeChain=c;tc.xtypes=c.join('/');}
return tc.xtypes;},findParentBy:function(fn){for(var p=this.ownerCt;(p!=null)&&!fn(p,this);p=p.ownerCt);return p||null;},findParentByType:function(xtype){return typeof xtype=='function'?this.findParentBy(function(p){return p.constructor===xtype;}):this.findParentBy(function(p){return p.constructor.xtype===xtype;});},mon:function(item,ename,fn,scope,opt){if(!this.mons){this.mons=[];this.on('beforedestroy',function(){for(var i=0,len=this.mons.length;i<len;i++){var m=this.mons[i];m.item.un(m.ename,m.fn,m.scope);}},this);}
this.mons.push({item:item,ename:ename,fn:fn,scope:scope});item.on(ename,fn,scope,opt);}});Ext.reg('component',Ext.Component);