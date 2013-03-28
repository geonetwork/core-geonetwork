/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.form.Label=Ext.extend(Ext.BoxComponent,{onRender:function(ct,position){if(!this.el){this.el=document.createElement('label');this.el.id=this.getId();this.el.innerHTML=this.text?Ext.util.Format.htmlEncode(this.text):(this.html||'');if(this.forId){this.el.setAttribute('for',this.forId);}}
Ext.form.Label.superclass.onRender.call(this,ct,position);},setText:function(t,encode){var e=encode===false;this[!e?'text':'html']=t;delete this[e?'text':'html'];if(this.rendered){this.el.dom.innerHTML=encode!==false?Ext.util.Format.htmlEncode(t):t;}
return this;}});Ext.reg('label',Ext.form.Label);