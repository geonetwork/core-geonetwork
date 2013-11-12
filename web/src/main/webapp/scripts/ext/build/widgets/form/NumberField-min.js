/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.form.NumberField=Ext.extend(Ext.form.TextField,{fieldClass:"x-form-field x-form-num-field",allowDecimals:true,decimalSeparator:".",decimalPrecision:2,allowNegative:true,minValue:Number.NEGATIVE_INFINITY,maxValue:Number.MAX_VALUE,minText:"The minimum value for this field is {0}",maxText:"The maximum value for this field is {0}",nanText:"{0} is not a valid number",baseChars:"0123456789",initEvents:function(){var allowed=this.baseChars+'';if(this.allowDecimals){allowed+=this.decimalSeparator;}
if(this.allowNegative){allowed+='-';}
this.maskRe=new RegExp('['+Ext.escapeRe(allowed)+']');Ext.form.NumberField.superclass.initEvents.call(this);},validateValue:function(value){if(!Ext.form.NumberField.superclass.validateValue.call(this,value)){return false;}
if(value.length<1){return true;}
value=String(value).replace(this.decimalSeparator,".");if(isNaN(value)){this.markInvalid(String.format(this.nanText,value));return false;}
var num=this.parseValue(value);if(num<this.minValue){this.markInvalid(String.format(this.minText,this.minValue));return false;}
if(num>this.maxValue){this.markInvalid(String.format(this.maxText,this.maxValue));return false;}
return true;},getValue:function(){return this.fixPrecision(this.parseValue(Ext.form.NumberField.superclass.getValue.call(this)));},setValue:function(v){v=typeof v=='number'?v:parseFloat(String(v).replace(this.decimalSeparator,"."));v=isNaN(v)?'':String(v).replace(".",this.decimalSeparator);Ext.form.NumberField.superclass.setValue.call(this,v);},parseValue:function(value){value=parseFloat(String(value).replace(this.decimalSeparator,"."));return isNaN(value)?'':value;},fixPrecision:function(value){var nan=isNaN(value);if(!this.allowDecimals||this.decimalPrecision==-1||nan||!value){return nan?'':value;}
return parseFloat(parseFloat(value).toFixed(this.decimalPrecision));},beforeBlur:function(){var v=this.parseValue(this.getRawValue());if(!Ext.isEmpty(v)){this.setValue(this.fixPrecision(v));}}});Ext.reg('numberfield',Ext.form.NumberField);