/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.layout.FormLayout=Ext.extend(Ext.layout.AnchorLayout,{labelSeparator:':',getAnchorViewSize:function(ct,target){return(ct.body||ct.el).getStyleSize();},setContainer:function(ct){Ext.layout.FormLayout.superclass.setContainer.call(this,ct);if(ct.labelAlign){ct.addClass('x-form-label-'+ct.labelAlign);}
if(ct.hideLabels){this.labelStyle="display:none";this.elementStyle="padding-left:0;";this.labelAdjust=0;}else{this.labelSeparator=ct.labelSeparator||this.labelSeparator;ct.labelWidth=ct.labelWidth||100;if(typeof ct.labelWidth=='number'){var pad=(typeof ct.labelPad=='number'?ct.labelPad:5);this.labelAdjust=ct.labelWidth+pad;this.labelStyle="width:"+ct.labelWidth+"px;";this.elementStyle="padding-left:"+(ct.labelWidth+pad)+'px';}
if(ct.labelAlign=='top'){this.labelStyle="width:auto;";this.labelAdjust=0;this.elementStyle="padding-left:0;";}}
if(!this.fieldTpl){var t=new Ext.Template('<div class="x-form-item {5}" tabIndex="-1">','<label for="{0}" style="{2}" class="x-form-item-label">{1}{4}</label>','<div class="x-form-element" id="x-form-el-{0}" style="{3}">','</div><div class="{6}"></div>','</div>');t.disableFormats=true;t.compile();Ext.layout.FormLayout.prototype.fieldTpl=t;}},getLabelStyle:function(s){var ls='',items=[this.labelStyle,s];for(var i=0,len=items.length;i<len;++i){if(items[i]){ls+=items[i];if(ls.substr(-1,1)!=';'){ls+=';'}}}
return ls;},renderItem:function(c,position,target){if(c&&!c.rendered&&c.isFormField&&c.inputType!='hidden'){var args=[c.id,c.fieldLabel,this.getLabelStyle(c.labelStyle),this.elementStyle||'',typeof c.labelSeparator=='undefined'?this.labelSeparator:c.labelSeparator,(c.itemCls||this.container.itemCls||'')+(c.hideLabel?' x-hide-label':''),c.clearCls||'x-form-clear-left'];if(typeof position=='number'){position=target.dom.childNodes[position]||null;}
if(position){this.fieldTpl.insertBefore(position,args);}else{this.fieldTpl.append(target,args);}
c.render('x-form-el-'+c.id);}else{Ext.layout.FormLayout.superclass.renderItem.apply(this,arguments);}},adjustWidthAnchor:function(value,comp){return value-(comp.isFormField?(comp.hideLabel?0:this.labelAdjust):0);},isValidParent:function(c,target){return true;}});Ext.Container.LAYOUTS['form']=Ext.layout.FormLayout;