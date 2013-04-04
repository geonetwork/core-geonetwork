/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.form.DateField=Ext.extend(Ext.form.TriggerField,{format:"m/d/Y",altFormats:"m/d/Y|n/j/Y|n/j/y|m/j/y|n/d/y|m/j/Y|n/d/Y|m-d-y|m-d-Y|m/d|m-d|md|mdy|mdY|d|Y-m-d",disabledDaysText:"Disabled",disabledDatesText:"Disabled",minText:"The date in this field must be equal to or after {0}",maxText:"The date in this field must be equal to or before {0}",invalidText:"{0} is not a valid date - it must be in the format {1}",triggerClass:'x-form-date-trigger',showToday:true,defaultAutoCreate:{tag:"input",type:"text",size:"10",autocomplete:"off"},initComponent:function(){Ext.form.DateField.superclass.initComponent.call(this);this.addEvents('select');if(typeof this.minValue=="string"){this.minValue=this.parseDate(this.minValue);}
if(typeof this.maxValue=="string"){this.maxValue=this.parseDate(this.maxValue);}
this.disabledDatesRE=null;this.initDisabledDays();},initDisabledDays:function(){if(this.disabledDates){var dd=this.disabledDates;var re="(?:";for(var i=0;i<dd.length;i++){re+=dd[i];if(i!=dd.length-1)re+="|";}
this.disabledDatesRE=new RegExp(re+")");}},setDisabledDates:function(dd){this.disabledDates=dd;this.initDisabledDays();if(this.menu){this.menu.picker.setDisabledDates(this.disabledDatesRE);}},setDisabledDays:function(dd){this.disabledDays=dd;if(this.menu){this.menu.picker.setDisabledDays(dd);}},setMinValue:function(dt){this.minValue=(typeof dt=="string"?this.parseDate(dt):dt);if(this.menu){this.menu.picker.setMinDate(this.minValue);}},setMaxValue:function(dt){this.maxValue=(typeof dt=="string"?this.parseDate(dt):dt);if(this.menu){this.menu.picker.setMaxDate(this.maxValue);}},validateValue:function(value){value=this.formatDate(value);if(!Ext.form.DateField.superclass.validateValue.call(this,value)){return false;}
if(value.length<1){return true;}
var svalue=value;value=this.parseDate(value);if(!value){this.markInvalid(String.format(this.invalidText,svalue,this.format));return false;}
var time=value.getTime();if(this.minValue&&time<this.minValue.getTime()){this.markInvalid(String.format(this.minText,this.formatDate(this.minValue)));return false;}
if(this.maxValue&&time>this.maxValue.getTime()){this.markInvalid(String.format(this.maxText,this.formatDate(this.maxValue)));return false;}
if(this.disabledDays){var day=value.getDay();for(var i=0;i<this.disabledDays.length;i++){if(day===this.disabledDays[i]){this.markInvalid(this.disabledDaysText);return false;}}}
var fvalue=this.formatDate(value);if(this.disabledDatesRE&&this.disabledDatesRE.test(fvalue)){this.markInvalid(String.format(this.disabledDatesText,fvalue));return false;}
return true;},validateBlur:function(){return!this.menu||!this.menu.isVisible();},getValue:function(){return this.parseDate(Ext.form.DateField.superclass.getValue.call(this))||"";},setValue:function(date){Ext.form.DateField.superclass.setValue.call(this,this.formatDate(this.parseDate(date)));},parseDate:function(value){if(!value||Ext.isDate(value)){return value;}
var v=Date.parseDate(value,this.format);if(!v&&this.altFormats){if(!this.altFormatsArray){this.altFormatsArray=this.altFormats.split("|");}
for(var i=0,len=this.altFormatsArray.length;i<len&&!v;i++){v=Date.parseDate(value,this.altFormatsArray[i]);}}
return v;},onDestroy:function(){if(this.menu){this.menu.destroy();}
Ext.form.DateField.superclass.onDestroy.call(this);},formatDate:function(date){return Ext.isDate(date)?date.dateFormat(this.format):date;},menuListeners:{select:function(m,d){this.setValue(d);this.fireEvent('select',this,d);},show:function(){this.onFocus();},hide:function(){this.focus.defer(10,this);var ml=this.menuListeners;this.menu.un("select",ml.select,this);this.menu.un("show",ml.show,this);this.menu.un("hide",ml.hide,this);}},onTriggerClick:function(){if(this.disabled){return;}
if(this.menu==null){this.menu=new Ext.menu.DateMenu();}
Ext.apply(this.menu.picker,{minDate:this.minValue,maxDate:this.maxValue,disabledDatesRE:this.disabledDatesRE,disabledDatesText:this.disabledDatesText,disabledDays:this.disabledDays,disabledDaysText:this.disabledDaysText,format:this.format,showToday:this.showToday,minText:String.format(this.minText,this.formatDate(this.minValue)),maxText:String.format(this.maxText,this.formatDate(this.maxValue))});this.menu.on(Ext.apply({},this.menuListeners,{scope:this}));this.menu.picker.setValue(this.getValue()||new Date());this.menu.show(this.el,"tl-bl?");},beforeBlur:function(){var v=this.parseDate(this.getRawValue());if(v){this.setValue(v);}}});Ext.reg('datefield',Ext.form.DateField);