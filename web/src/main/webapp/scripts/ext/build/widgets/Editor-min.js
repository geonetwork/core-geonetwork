/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.Editor=function(field,config){this.field=field;Ext.Editor.superclass.constructor.call(this,config);};Ext.extend(Ext.Editor,Ext.Component,{value:"",alignment:"c-c?",shadow:"frame",constrain:false,swallowKeys:true,completeOnEnter:false,cancelOnEsc:false,updateEl:false,initComponent:function(){Ext.Editor.superclass.initComponent.call(this);this.addEvents("beforestartedit","startedit","beforecomplete","complete","canceledit","specialkey");},onRender:function(ct,position){this.el=new Ext.Layer({shadow:this.shadow,cls:"x-editor",parentEl:ct,shim:this.shim,shadowOffset:4,id:this.id,constrain:this.constrain});this.el.setStyle("overflow",Ext.isGecko?"auto":"hidden");if(this.field.msgTarget!='title'){this.field.msgTarget='qtip';}
this.field.inEditor=true;this.field.render(this.el);if(Ext.isGecko){this.field.el.dom.setAttribute('autocomplete','off');}
this.field.on("specialkey",this.onSpecialKey,this);if(this.swallowKeys){this.field.el.swallowEvent(['keydown','keypress']);}
this.field.show();this.field.on("blur",this.onBlur,this);if(this.field.grow){this.field.on("autosize",this.el.sync,this.el,{delay:1});}},onSpecialKey:function(field,e){var key=e.getKey();if(this.completeOnEnter&&key==e.ENTER){e.stopEvent();this.completeEdit();}else if(this.cancelOnEsc&&key==e.ESC){this.cancelEdit();}else{this.fireEvent('specialkey',field,e);}
if(this.field.triggerBlur&&(key==e.ENTER||key==e.ESC||key==e.TAB)){this.field.triggerBlur();}},startEdit:function(el,value){if(this.editing){this.completeEdit();}
this.boundEl=Ext.get(el);var v=value!==undefined?value:this.boundEl.dom.innerHTML;if(!this.rendered){this.render(this.parentEl||document.body);}
if(this.fireEvent("beforestartedit",this,this.boundEl,v)===false){return;}
this.startValue=v;this.field.setValue(v);this.doAutoSize();this.el.alignTo(this.boundEl,this.alignment);this.editing=true;this.show();},doAutoSize:function(){if(this.autoSize){var sz=this.boundEl.getSize();switch(this.autoSize){case"width":this.setSize(sz.width,"");break;case"height":this.setSize("",sz.height);break;default:this.setSize(sz.width,sz.height);}}},setSize:function(w,h){delete this.field.lastSize;this.field.setSize(w,h);if(this.el){if(Ext.isGecko2||Ext.isOpera){this.el.setSize(w,h);}
this.el.sync();}},realign:function(){this.el.alignTo(this.boundEl,this.alignment);},completeEdit:function(remainVisible){if(!this.editing){return;}
var v=this.getValue();if(!this.field.isValid()){if(this.revertInvalid!==false){this.cancelEdit(remainVisible);}
return;}
if(String(v)===String(this.startValue)&&this.ignoreNoChange){this.hideEdit(remainVisible);return;}
if(this.fireEvent("beforecomplete",this,v,this.startValue)!==false){v=this.getValue();if(this.updateEl&&this.boundEl){this.boundEl.update(v);}
this.hideEdit(remainVisible);this.fireEvent("complete",this,v,this.startValue);}},onShow:function(){this.el.show();if(this.hideEl!==false){this.boundEl.hide();}
this.field.show();if(Ext.isIE&&!this.fixIEFocus){this.fixIEFocus=true;this.deferredFocus.defer(50,this);}else{this.field.focus();}
this.fireEvent("startedit",this.boundEl,this.startValue);},deferredFocus:function(){if(this.editing){this.field.focus();}},cancelEdit:function(remainVisible){if(this.editing){var v=this.getValue();this.setValue(this.startValue);this.hideEdit(remainVisible);this.fireEvent("canceledit",this,v,this.startValue);}},hideEdit:function(remainVisible){if(remainVisible!==true){this.editing=false;this.hide();}},onBlur:function(){if(this.allowBlur!==true&&this.editing){this.completeEdit();}},onHide:function(){if(this.editing){this.completeEdit();return;}
this.field.blur();if(this.field.collapse){this.field.collapse();}
this.el.hide();if(this.hideEl!==false){this.boundEl.show();}},setValue:function(v){this.field.setValue(v);},getValue:function(){return this.field.getValue();},beforeDestroy:function(){Ext.destroy(this.field);this.field=null;}});Ext.reg('editor',Ext.Editor);