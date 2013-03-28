/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.data.XmlReader=function(meta,recordType){meta=meta||{};Ext.data.XmlReader.superclass.constructor.call(this,meta,recordType||meta.fields);};Ext.extend(Ext.data.XmlReader,Ext.data.DataReader,{read:function(response){var doc=response.responseXML;if(!doc){throw{message:"XmlReader.read: XML Document not available"};}
return this.readRecords(doc);},readRecords:function(doc){this.xmlData=doc;var root=doc.documentElement||doc;var q=Ext.DomQuery;var recordType=this.recordType,fields=recordType.prototype.fields;var sid=this.meta.id;var totalRecords=0,success=true;if(this.meta.totalRecords){totalRecords=q.selectNumber(this.meta.totalRecords,root,0);}
if(this.meta.success){var sv=q.selectValue(this.meta.success,root,true);success=sv!==false&&sv!=='false';}
var records=[];var ns=q.select(this.meta.record,root);for(var i=0,len=ns.length;i<len;i++){var n=ns[i];var values={};var id=sid?q.selectValue(sid,n):undefined;for(var j=0,jlen=fields.length;j<jlen;j++){var f=fields.items[j];var v=q.selectValue(Ext.isEmpty(f.mapping,true)?f.name:f.mapping,n,f.defaultValue);v=f.convert(v,n);values[f.name]=v;}
var record=new recordType(values,id);record.node=n;records[records.length]=record;}
return{success:success,records:records,totalRecords:totalRecords||records.length};}});