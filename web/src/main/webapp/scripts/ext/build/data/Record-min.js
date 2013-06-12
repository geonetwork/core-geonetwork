/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.data.Record=function(data,id){this.id=(id||id===0)?id:++Ext.data.Record.AUTO_ID;this.data=data||{};};Ext.data.Record.create=function(o){var f=Ext.extend(Ext.data.Record,{});var p=f.prototype;p.fields=new Ext.util.MixedCollection(false,function(field){return field.name;});for(var i=0,len=o.length;i<len;i++){p.fields.add(new Ext.data.Field(o[i]));}
f.getField=function(name){return p.fields.get(name);};return f;};Ext.data.Record.AUTO_ID=1000;Ext.data.Record.EDIT='edit';Ext.data.Record.REJECT='reject';Ext.data.Record.COMMIT='commit';Ext.data.Record.prototype={dirty:false,editing:false,error:null,modified:null,join:function(store){this.store=store;},set:function(name,value){if(String(this.data[name])==String(value)){return;}
this.dirty=true;if(!this.modified){this.modified={};}
if(typeof this.modified[name]=='undefined'){this.modified[name]=this.data[name];}
this.data[name]=value;if(!this.editing&&this.store){this.store.afterEdit(this);}},get:function(name){return this.data[name];},beginEdit:function(){this.editing=true;this.modified={};},cancelEdit:function(){this.editing=false;delete this.modified;},endEdit:function(){this.editing=false;if(this.dirty&&this.store){this.store.afterEdit(this);}},reject:function(silent){var m=this.modified;for(var n in m){if(typeof m[n]!="function"){this.data[n]=m[n];}}
this.dirty=false;delete this.modified;this.editing=false;if(this.store&&silent!==true){this.store.afterReject(this);}},commit:function(silent){this.dirty=false;delete this.modified;this.editing=false;if(this.store&&silent!==true){this.store.afterCommit(this);}},getChanges:function(){var m=this.modified,cs={};for(var n in m){if(m.hasOwnProperty(n)){cs[n]=this.data[n];}}
return cs;},hasError:function(){return this.error!=null;},clearError:function(){this.error=null;},copy:function(newId){return new this.constructor(Ext.apply({},this.data),newId||this.id);},isModified:function(fieldName){return!!(this.modified&&this.modified.hasOwnProperty(fieldName));}};