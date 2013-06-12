/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.form.FieldSet=Ext.extend(Ext.Panel,{baseCls:'x-fieldset',layout:'form',animCollapse:false,onRender:function(ct,position){if(!this.el){this.el=document.createElement('fieldset');this.el.id=this.id;if(this.title||this.header||this.checkboxToggle){this.el.appendChild(document.createElement('legend')).className='x-fieldset-header';}}
Ext.form.FieldSet.superclass.onRender.call(this,ct,position);if(this.checkboxToggle){var o=typeof this.checkboxToggle=='object'?this.checkboxToggle:{tag:'input',type:'checkbox',name:this.checkboxName||this.id+'-checkbox'};this.checkbox=this.header.insertFirst(o);this.checkbox.dom.checked=!this.collapsed;this.checkbox.on('click',this.onCheckClick,this);}},onCollapse:function(doAnim,animArg){if(this.checkbox){this.checkbox.dom.checked=false;}
Ext.form.FieldSet.superclass.onCollapse.call(this,doAnim,animArg);},onExpand:function(doAnim,animArg){if(this.checkbox){this.checkbox.dom.checked=true;}
Ext.form.FieldSet.superclass.onExpand.call(this,doAnim,animArg);},onCheckClick:function(){this[this.checkbox.dom.checked?'expand':'collapse']();},beforeDestroy:function(){if(this.checkbox){this.checkbox.un('click',this.onCheckClick,this);}
Ext.form.FieldSet.superclass.beforeDestroy.call(this);}});Ext.reg('fieldset',Ext.form.FieldSet);