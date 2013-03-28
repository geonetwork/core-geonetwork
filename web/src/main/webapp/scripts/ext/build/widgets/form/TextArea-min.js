/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.form.TextArea=Ext.extend(Ext.form.TextField,{growMin:60,growMax:1000,growAppend:'&#160;\n&#160;',growPad:Ext.isWebKit?-6:0,enterIsSpecial:false,preventScrollbars:false,onRender:function(ct,position){if(!this.el){this.defaultAutoCreate={tag:"textarea",style:"width:100px;height:60px;",autocomplete:"off"};}
Ext.form.TextArea.superclass.onRender.call(this,ct,position);if(this.grow){this.textSizeEl=Ext.DomHelper.append(document.body,{tag:"pre",cls:"x-form-grow-sizer"});if(this.preventScrollbars){this.el.setStyle("overflow","hidden");}
this.el.setHeight(this.growMin);}},onDestroy:function(){if(this.textSizeEl){Ext.removeNode(this.textSizeEl);}
Ext.form.TextArea.superclass.onDestroy.call(this);},fireKey:function(e){if(e.isSpecialKey()&&(this.enterIsSpecial||(e.getKey()!=e.ENTER||e.hasModifier()))){this.fireEvent("specialkey",this,e);}},onKeyUp:function(e){if(!e.isNavKeyPress()||e.getKey()==e.ENTER){this.autoSize();}
Ext.form.TextArea.superclass.onKeyUp.call(this,e);},autoSize:function(){if(!this.grow||!this.textSizeEl){return;}
var el=this.el;var v=el.dom.value;var ts=this.textSizeEl;ts.innerHTML='';ts.appendChild(document.createTextNode(v));v=ts.innerHTML;Ext.fly(ts).setWidth(this.el.getWidth());if(v.length<1){v="&#160;&#160;";}else{v+=this.growAppend;if(Ext.isIE){v=v.replace(/\n/g,'<br />');}}
ts.innerHTML=v;var h=Math.min(this.growMax,Math.max(ts.offsetHeight,this.growMin)+this.growPad);if(h!=this.lastHeight){this.lastHeight=h;this.el.setHeight(h);this.fireEvent("autosize",this,h);}}});Ext.reg('textarea',Ext.form.TextArea);