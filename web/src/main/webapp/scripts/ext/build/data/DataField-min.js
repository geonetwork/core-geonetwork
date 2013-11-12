/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.data.Field=function(config){if(typeof config=="string"){config={name:config};}
Ext.apply(this,config);if(!this.type){this.type="auto";}
var st=Ext.data.SortTypes;if(typeof this.sortType=="string"){this.sortType=st[this.sortType];}
if(!this.sortType){switch(this.type){case"string":this.sortType=st.asUCString;break;case"date":this.sortType=st.asDate;break;default:this.sortType=st.none;}}
var stripRe=/[\$,%]/g;if(!this.convert){var cv,dateFormat=this.dateFormat;switch(this.type){case"":case"auto":case undefined:cv=function(v){return v;};break;case"string":cv=function(v){return(v===undefined||v===null)?'':String(v);};break;case"int":cv=function(v){return v!==undefined&&v!==null&&v!==''?parseInt(String(v).replace(stripRe,""),10):'';};break;case"float":cv=function(v){return v!==undefined&&v!==null&&v!==''?parseFloat(String(v).replace(stripRe,""),10):'';};break;case"bool":case"boolean":cv=function(v){return v===true||v==="true"||v==1;};break;case"date":cv=function(v){if(!v){return'';}
if(Ext.isDate(v)){return v;}
if(dateFormat){if(dateFormat=="timestamp"){return new Date(v*1000);}
if(dateFormat=="time"){return new Date(parseInt(v,10));}
return Date.parseDate(v,dateFormat);}
var parsed=Date.parse(v);return parsed?new Date(parsed):null;};break;}
this.convert=cv;}};Ext.data.Field.prototype={dateFormat:null,defaultValue:"",mapping:null,sortType:null,sortDir:"ASC"};