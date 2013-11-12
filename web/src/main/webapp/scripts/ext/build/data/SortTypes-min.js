/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.data.SortTypes={none:function(s){return s;},stripTagsRE:/<\/?[^>]+>/gi,asText:function(s){return String(s).replace(this.stripTagsRE,"");},asUCText:function(s){return String(s).toUpperCase().replace(this.stripTagsRE,"");},asUCString:function(s){return String(s).toUpperCase();},asDate:function(s){if(!s){return 0;}
if(Ext.isDate(s)){return s.getTime();}
return Date.parse(String(s));},asFloat:function(s){var val=parseFloat(String(s).replace(/,/g,""));if(isNaN(val))val=0;return val;},asInt:function(s){var val=parseInt(String(s).replace(/,/g,""));if(isNaN(val))val=0;return val;}};