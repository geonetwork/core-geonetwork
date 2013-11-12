/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.form.TimeField=Ext.extend(Ext.form.ComboBox,{minValue:null,maxValue:null,minText:"The time in this field must be equal to or after {0}",maxText:"The time in this field must be equal to or before {0}",invalidText:"{0} is not a valid time",format:"g:i A",altFormats:"g:ia|g:iA|g:i a|g:i A|h:i|g:i|H:i|ga|ha|gA|h a|g a|g A|gi|hi|gia|hia|g|H",increment:15,mode:'local',triggerAction:'all',typeAhead:false,initDate:'1/1/2008',initComponent:function(){Ext.form.TimeField.superclass.initComponent.call(this);if(typeof this.minValue=="string"){this.minValue=this.parseDate(this.minValue);}
if(typeof this.maxValue=="string"){this.maxValue=this.parseDate(this.maxValue);}
if(!this.store){var min=this.parseDate(this.minValue);if(!min){min=new Date(this.initDate).clearTime();}
var max=this.parseDate(this.maxValue);if(!max){max=new Date(this.initDate).clearTime().add('mi',(24*60)-1);}
var times=[];while(min<=max){times.push([min.dateFormat(this.format)]);min=min.add('mi',this.increment);}
this.store=new Ext.data.SimpleStore({fields:['text'],data:times});this.displayField='text';}},getValue:function(){var v=Ext.form.TimeField.superclass.getValue.call(this);return this.formatDate(this.parseDate(v))||'';},setValue:function(value){Ext.form.TimeField.superclass.setValue.call(this,this.formatDate(this.parseDate(value)));},validateValue:Ext.form.DateField.prototype.validateValue,parseDate:Ext.form.DateField.prototype.parseDate,formatDate:Ext.form.DateField.prototype.formatDate,beforeBlur:function(){var v=this.parseDate(this.getRawValue());if(v){this.setValue(v.dateFormat(this.format));}
Ext.form.TimeField.superclass.beforeBlur.call(this);}});Ext.reg('timefield',Ext.form.TimeField);