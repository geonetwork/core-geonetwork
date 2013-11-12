/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.QuickTip=Ext.extend(Ext.ToolTip,{interceptTitles:false,tagConfig:{namespace:"ext",attribute:"qtip",width:"qwidth",target:"target",title:"qtitle",hide:"hide",cls:"qclass",align:"qalign"},initComponent:function(){this.target=this.target||Ext.getDoc();this.targets=this.targets||{};Ext.QuickTip.superclass.initComponent.call(this);},register:function(config){var cs=Ext.isArray(config)?config:arguments;for(var i=0,len=cs.length;i<len;i++){var c=cs[i];var target=c.target;if(target){if(Ext.isArray(target)){for(var j=0,jlen=target.length;j<jlen;j++){this.targets[Ext.id(target[j])]=c;}}else{this.targets[Ext.id(target)]=c;}}}},unregister:function(el){delete this.targets[Ext.id(el)];},onTargetOver:function(e){if(this.disabled){return;}
this.targetXY=e.getXY();var t=e.getTarget();if(!t||t.nodeType!==1||t==document||t==document.body){return;}
if(this.activeTarget&&t==this.activeTarget.el){this.clearTimer('hide');this.show();return;}
if(t&&this.targets[t.id]){this.activeTarget=this.targets[t.id];this.activeTarget.el=t;this.delayShow();return;}
var ttp,et=Ext.fly(t),cfg=this.tagConfig;var ns=cfg.namespace;if(this.interceptTitles&&t.title){ttp=t.title;t.qtip=ttp;t.removeAttribute("title");e.preventDefault();}else{ttp=t.qtip||et.getAttributeNS(ns,cfg.attribute);}
if(ttp){var autoHide=et.getAttributeNS(ns,cfg.hide);this.activeTarget={el:t,text:ttp,width:et.getAttributeNS(ns,cfg.width),autoHide:autoHide!="user"&&autoHide!=='false',title:et.getAttributeNS(ns,cfg.title),cls:et.getAttributeNS(ns,cfg.cls),align:et.getAttributeNS(ns,cfg.align)};this.delayShow();}},onTargetOut:function(e){this.clearTimer('show');if(this.autoHide!==false){this.delayHide();}},showAt:function(xy){var t=this.activeTarget;if(t){if(!this.rendered){this.render(Ext.getBody());this.activeTarget=t;}
if(t.width){this.setWidth(t.width);this.body.setWidth(this.adjustBodyWidth(t.width-this.getFrameWidth()));this.measureWidth=false;}else{this.measureWidth=true;}
this.setTitle(t.title||'');this.body.update(t.text);this.autoHide=t.autoHide;this.dismissDelay=t.dismissDelay||this.dismissDelay;if(this.lastCls){this.el.removeClass(this.lastCls);delete this.lastCls;}
if(t.cls){this.el.addClass(t.cls);this.lastCls=t.cls;}
if(t.align){xy=this.el.getAlignToXY(t.el,t.align);this.constrainPosition=false;}else{this.constrainPosition=true;}}
Ext.QuickTip.superclass.showAt.call(this,xy);},hide:function(){delete this.activeTarget;Ext.QuickTip.superclass.hide.call(this);}});