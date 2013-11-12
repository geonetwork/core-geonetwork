/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.form.RadioGroup=Ext.extend(Ext.form.CheckboxGroup,{allowBlank:true,blankText:"You must select one item in this group",defaultType:'radio',groupCls:'x-form-radio-group',initComponent:function(){this.addEvents('change');Ext.form.RadioGroup.superclass.initComponent.call(this);},fireChecked:function(){if(!this.checkTask){this.checkTask=new Ext.util.DelayedTask(this.bufferChecked,this);}
this.checkTask.delay(10);},bufferChecked:function(){var out=null;this.items.each(function(item){if(item.checked){out=item;return false;}});this.fireEvent('change',this,out);},onDestroy:function(){if(this.checkTask){this.checkTask.cancel();this.checkTask=null;}
Ext.form.RadioGroup.superclass.onDestroy.call(this);}});Ext.reg('radiogroup',Ext.form.RadioGroup);