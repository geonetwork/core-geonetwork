/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.form.Radio=Ext.extend(Ext.form.Checkbox,{inputType:'radio',baseCls:'x-form-radio',getGroupValue:function(){var c=this.getParent().child('input[name='+this.el.dom.name+']:checked',true);return c?c.value:null;},getParent:function(){return this.el.up('form')||Ext.getBody();},toggleValue:function(){if(!this.checked){var els=this.getParent().select('input[name='+this.el.dom.name+']');els.each(function(el){if(el.dom.id==this.id){this.setValue(true);}else{Ext.getCmp(el.dom.id).setValue(false);}},this);}},setValue:function(v){if(typeof v=='boolean'){Ext.form.Radio.superclass.setValue.call(this,v);}else{var r=this.getParent().child('input[name='+this.el.dom.name+'][value='+v+']',true);if(r&&!r.checked){Ext.getCmp(r.id).toggleValue();};}},markInvalid:Ext.emptyFn,clearInvalid:Ext.emptyFn});Ext.reg('radio',Ext.form.Radio);