/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.form.CheckboxGroup=Ext.extend(Ext.form.Field,{columns:'auto',vertical:false,allowBlank:true,blankText:"You must select at least one item in this group",defaultType:'checkbox',groupCls:'x-form-check-group',initComponent:function(){this.addEvents('change');Ext.form.CheckboxGroup.superclass.initComponent.call(this);},onRender:function(ct,position){if(!this.el){var panelCfg={cls:this.groupCls,layout:'column',border:false,renderTo:ct};var colCfg={defaultType:this.defaultType,layout:'form',border:false,defaults:{hideLabel:true,anchor:'100%'}}
if(this.items[0].items){Ext.apply(panelCfg,{layoutConfig:{columns:this.items.length},defaults:this.defaults,items:this.items})
for(var i=0,len=this.items.length;i<len;i++){Ext.applyIf(this.items[i],colCfg);};}else{var numCols,cols=[];if(typeof this.columns=='string'){this.columns=this.items.length;}
if(!Ext.isArray(this.columns)){var cs=[];for(var i=0;i<this.columns;i++){cs.push((100/this.columns)*.01);}
this.columns=cs;}
numCols=this.columns.length;for(var i=0;i<numCols;i++){var cc=Ext.apply({items:[]},colCfg);cc[this.columns[i]<=1?'columnWidth':'width']=this.columns[i];if(this.defaults){cc.defaults=Ext.apply(cc.defaults||{},this.defaults)}
cols.push(cc);};if(this.vertical){var rows=Math.ceil(this.items.length/numCols),ri=0;for(var i=0,len=this.items.length;i<len;i++){if(i>0&&i%rows==0){ri++;}
if(this.items[i].fieldLabel){this.items[i].hideLabel=false;}
cols[ri].items.push(this.items[i]);};}else{for(var i=0,len=this.items.length;i<len;i++){var ci=i%numCols;if(this.items[i].fieldLabel){this.items[i].hideLabel=false;}
cols[ci].items.push(this.items[i]);};}
Ext.apply(panelCfg,{layoutConfig:{columns:numCols},items:cols});}
this.panel=new Ext.Panel(panelCfg);this.panel.ownerCt=this;this.el=this.panel.getEl();if(this.forId&&this.itemCls){var l=this.el.up(this.itemCls).child('label',true);if(l){l.setAttribute('htmlFor',this.forId);}}
var fields=this.panel.findBy(function(c){return c.isFormField;},this);this.items=new Ext.util.MixedCollection();this.items.addAll(fields);}
Ext.form.CheckboxGroup.superclass.onRender.call(this,ct,position);},afterRender:function(){Ext.form.CheckboxGroup.superclass.afterRender.call(this);this.items.each(function(item){item.on('check',this.fireChecked,this);},this);},fireChecked:function(){var arr=[];this.items.each(function(item){if(item.checked){arr.push(item);}});this.fireEvent('change',this,arr);},validateValue:function(value){if(!this.allowBlank){var blank=true;this.items.each(function(f){if(f.checked){return blank=false;}},this);if(blank){this.markInvalid(this.blankText);return false;}}
return true;},onDestroy:function(){Ext.destroy(this.panel);Ext.form.CheckboxGroup.superclass.onDestroy.call(this);},onDisable:function(){this.items.each(function(item){item.disable();})},onEnable:function(){this.items.each(function(item){item.enable();})},isDirty:function(){if(this.disabled||!this.rendered){return false;}
var dirty=false;this.items.each(function(item){if(item.isDirty()){dirty=true;return false;}});return dirty;},onResize:function(w,h){this.panel.setSize(w,h);this.panel.doLayout();},reset:function(){Ext.form.CheckboxGroup.superclass.reset.call(this);this.items.each(function(c){if(c.reset){c.reset();}},this);},initValue:Ext.emptyFn,getValue:Ext.emptyFn,getRawValue:Ext.emptyFn,setValue:Ext.emptyFn,setRawValue:Ext.emptyFn});Ext.reg('checkboxgroup',Ext.form.CheckboxGroup);