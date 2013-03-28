/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.StatusBar=Ext.extend(Ext.Toolbar,{cls:'x-statusbar',busyIconCls:'x-status-busy',busyText:'Loading...',autoClear:5000,activeThreadId:0,initComponent:function(){if(this.statusAlign=='right'){this.cls+=' x-status-right';}
Ext.StatusBar.superclass.initComponent.call(this);},afterRender:function(){Ext.StatusBar.superclass.afterRender.call(this);var right=this.statusAlign=='right',td=Ext.get(this.nextBlock());if(right){this.tr.appendChild(td.dom);}else{td.insertBefore(this.tr.firstChild);}
this.statusEl=td.createChild({cls:'x-status-text '+(this.iconCls||this.defaultIconCls||''),html:this.text||this.defaultText||''});this.statusEl.unselectable();this.spacerEl=td.insertSibling({tag:'td',style:'width:100%',cn:[{cls:'ytb-spacer'}]},right?'before':'after');},setStatus:function(o){o=o||{};if(typeof o=='string'){o={text:o};}
if(o.text!==undefined){this.setText(o.text);}
if(o.iconCls!==undefined){this.setIcon(o.iconCls);}
if(o.clear){var c=o.clear,wait=this.autoClear,defaults={useDefaults:true,anim:true};if(typeof c=='object'){c=Ext.applyIf(c,defaults);if(c.wait){wait=c.wait;}}else if(typeof c=='number'){wait=c;c=defaults;}else if(typeof c=='boolean'){c=defaults;}
c.threadId=this.activeThreadId;this.clearStatus.defer(wait,this,[c]);}
return this;},clearStatus:function(o){o=o||{};if(o.threadId&&o.threadId!==this.activeThreadId){return this;}
var text=o.useDefaults?this.defaultText:'',iconCls=o.useDefaults?(this.defaultIconCls?this.defaultIconCls:''):'';if(o.anim){this.statusEl.fadeOut({remove:false,useDisplay:true,scope:this,callback:function(){this.setStatus({text:text,iconCls:iconCls});this.statusEl.show();}});}else{this.statusEl.hide();this.setStatus({text:text,iconCls:iconCls});this.statusEl.show();}
return this;},setText:function(text){this.activeThreadId++;this.text=text||'';if(this.rendered){this.statusEl.update(this.text);}
return this;},getText:function(){return this.text;},setIcon:function(cls){this.activeThreadId++;cls=cls||'';if(this.rendered){if(this.currIconCls){this.statusEl.removeClass(this.currIconCls);this.currIconCls=null;}
if(cls.length>0){this.statusEl.addClass(cls);this.currIconCls=cls;}}else{this.currIconCls=cls;}
return this;},showBusy:function(o){if(typeof o=='string'){o={text:o};}
o=Ext.applyIf(o||{},{text:this.busyText,iconCls:this.busyIconCls});return this.setStatus(o);}});Ext.reg('statusbar',Ext.StatusBar);