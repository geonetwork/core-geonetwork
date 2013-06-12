/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.util.DelayedTask=function(fn,scope,args){var id=null;var call=function(){id=null;fn.apply(scope,args||[]);};this.delay=function(delay,newFn,newScope,newArgs){if(id){this.cancel();}
fn=newFn||fn;scope=newScope||scope;args=newArgs||args;if(!id){id=setTimeout(call,delay);}};this.cancel=function(){if(id){clearTimeout(id);id=null;}};};

Ext.util.MixedCollection=function(allowFunctions,keyFn){this.items=[];this.map={};this.keys=[];this.length=0;this.addEvents("clear","add","replace","remove","sort");this.allowFunctions=allowFunctions===true;if(keyFn){this.getKey=keyFn;}
Ext.util.MixedCollection.superclass.constructor.call(this);};Ext.extend(Ext.util.MixedCollection,Ext.util.Observable,{allowFunctions:false,add:function(key,o){if(arguments.length==1){o=arguments[0];key=this.getKey(o);}
if(typeof key!='undefined'&&key!==null){var old=this.map[key];if(typeof old!='undefined'){return this.replace(key,o);}
this.map[key]=o;}
this.length++;this.items.push(o);this.keys.push(key);this.fireEvent('add',this.length-1,o,key);return o;},getKey:function(o){return o.id;},replace:function(key,o){if(arguments.length==1){o=arguments[0];key=this.getKey(o);}
var old=this.map[key];if(typeof key=="undefined"||key===null||typeof old=="undefined"){return this.add(key,o);}
var index=this.indexOfKey(key);this.items[index]=o;this.map[key]=o;this.fireEvent("replace",key,old,o);return o;},addAll:function(objs){if(arguments.length>1||Ext.isArray(objs)){var args=arguments.length>1?arguments:objs;for(var i=0,len=args.length;i<len;i++){this.add(args[i]);}}else{for(var key in objs){if(this.allowFunctions||typeof objs[key]!="function"){this.add(key,objs[key]);}}}},each:function(fn,scope){var items=[].concat(this.items);for(var i=0,len=items.length;i<len;i++){if(fn.call(scope||items[i],items[i],i,len)===false){break;}}},eachKey:function(fn,scope){for(var i=0,len=this.keys.length;i<len;i++){fn.call(scope||window,this.keys[i],this.items[i],i,len);}},find:function(fn,scope){for(var i=0,len=this.items.length;i<len;i++){if(fn.call(scope||window,this.items[i],this.keys[i])){return this.items[i];}}
return null;},insert:function(index,key,o){if(arguments.length==2){o=arguments[1];key=this.getKey(o);}
if(this.containsKey(key)){this.suspendEvents();this.removeKey(key);this.resumeEvents();}
if(index>=this.length){return this.add(key,o);}
this.length++;this.items.splice(index,0,o);if(typeof key!="undefined"&&key!=null){this.map[key]=o;}
this.keys.splice(index,0,key);this.fireEvent("add",index,o,key);return o;},remove:function(o){return this.removeAt(this.indexOf(o));},removeAt:function(index){if(index<this.length&&index>=0){this.length--;var o=this.items[index];this.items.splice(index,1);var key=this.keys[index];if(typeof key!="undefined"){delete this.map[key];}
this.keys.splice(index,1);this.fireEvent("remove",o,key);return o;}
return false;},removeKey:function(key){return this.removeAt(this.indexOfKey(key));},getCount:function(){return this.length;},indexOf:function(o){return this.items.indexOf(o);},indexOfKey:function(key){return this.keys.indexOf(key);},item:function(key){var mk=this.map[key],item=mk!==undefined?mk:(typeof key=='number')?this.items[key]:undefined;return typeof item!='function'||this.allowFunctions?item:null;},itemAt:function(index){return this.items[index];},key:function(key){return this.map[key];},contains:function(o){return this.indexOf(o)!=-1;},containsKey:function(key){return typeof this.map[key]!="undefined";},clear:function(){this.length=0;this.items=[];this.keys=[];this.map={};this.fireEvent("clear");},first:function(){return this.items[0];},last:function(){return this.items[this.length-1];},_sort:function(property,dir,fn){var dsc=String(dir).toUpperCase()=="DESC"?-1:1;fn=fn||function(a,b){return a-b;};var c=[],k=this.keys,items=this.items;for(var i=0,len=items.length;i<len;i++){c[c.length]={key:k[i],value:items[i],index:i};}
c.sort(function(a,b){var v=fn(a[property],b[property])*dsc;if(v==0){v=(a.index<b.index?-1:1);}
return v;});for(var i=0,len=c.length;i<len;i++){items[i]=c[i].value;k[i]=c[i].key;}
this.fireEvent("sort",this);},sort:function(dir,fn){this._sort("value",dir,fn);},keySort:function(dir,fn){this._sort("key",dir,fn||function(a,b){var v1=String(a).toUpperCase(),v2=String(b).toUpperCase();return v1>v2?1:(v1<v2?-1:0);});},getRange:function(start,end){var items=this.items;if(items.length<1){return[];}
start=start||0;end=Math.min(typeof end=="undefined"?this.length-1:end,this.length-1);var r=[];if(start<=end){for(var i=start;i<=end;i++){r[r.length]=items[i];}}else{for(var i=start;i>=end;i--){r[r.length]=items[i];}}
return r;},filter:function(property,value,anyMatch,caseSensitive){if(Ext.isEmpty(value,false)){return this.clone();}
value=this.createValueMatcher(value,anyMatch,caseSensitive);return this.filterBy(function(o){return o&&value.test(o[property]);});},filterBy:function(fn,scope){var r=new Ext.util.MixedCollection();r.getKey=this.getKey;var k=this.keys,it=this.items;for(var i=0,len=it.length;i<len;i++){if(fn.call(scope||this,it[i],k[i])){r.add(k[i],it[i]);}}
return r;},findIndex:function(property,value,start,anyMatch,caseSensitive){if(Ext.isEmpty(value,false)){return-1;}
value=this.createValueMatcher(value,anyMatch,caseSensitive);return this.findIndexBy(function(o){return o&&value.test(o[property]);},null,start);},findIndexBy:function(fn,scope,start){var k=this.keys,it=this.items;for(var i=(start||0),len=it.length;i<len;i++){if(fn.call(scope||this,it[i],k[i])){return i;}}
return-1;},createValueMatcher:function(value,anyMatch,caseSensitive){if(!value.exec){value=String(value);value=new RegExp((anyMatch===true?'':'^')+Ext.escapeRe(value),caseSensitive?'':'i');}
return value;},clone:function(){var r=new Ext.util.MixedCollection();var k=this.keys,it=this.items;for(var i=0,len=it.length;i<len;i++){r.add(k[i],it[i]);}
r.getKey=this.getKey;return r;}});Ext.util.MixedCollection.prototype.get=Ext.util.MixedCollection.prototype.item;

Ext.util.JSON=new(function(){var useHasOwn=!!{}.hasOwnProperty;var pad=function(n){return n<10?"0"+n:n;};var m={"\b":'\\b',"\t":'\\t',"\n":'\\n',"\f":'\\f',"\r":'\\r','"':'\\"',"\\":'\\\\'};var encodeString=function(s){if(/["\\\x00-\x1f]/.test(s)){return'"'+s.replace(/([\x00-\x1f\\"])/g,function(a,b){var c=m[b];if(c){return c;}
c=b.charCodeAt();return"\\u00"+
Math.floor(c/16).toString(16)+
(c%16).toString(16);})+'"';}
return'"'+s+'"';};var encodeArray=function(o){var a=["["],b,i,l=o.length,v;for(i=0;i<l;i+=1){v=o[i];switch(typeof v){case"undefined":case"function":case"unknown":break;default:if(b){a.push(',');}
a.push(v===null?"null":Ext.util.JSON.encode(v));b=true;}}
a.push("]");return a.join("");};this.encodeDate=function(o){return'"'+o.getFullYear()+"-"+
pad(o.getMonth()+1)+"-"+
pad(o.getDate())+"T"+
pad(o.getHours())+":"+
pad(o.getMinutes())+":"+
pad(o.getSeconds())+'"';};this.encode=function(o){if(typeof o=="undefined"||o===null){return"null";}else if(Ext.isArray(o)){return encodeArray(o);}else if(Ext.isDate(o)){return Ext.util.JSON.encodeDate(o);}else if(typeof o=="string"){return encodeString(o);}else if(typeof o=="number"){return isFinite(o)?String(o):"null";}else if(typeof o=="boolean"){return String(o);}else{var a=["{"],b,i,v;for(i in o){if(!useHasOwn||o.hasOwnProperty(i)){v=o[i];switch(typeof v){case"undefined":case"function":case"unknown":break;default:if(b){a.push(',');}
a.push(this.encode(i),":",v===null?"null":this.encode(v));b=true;}}}
a.push("}");return a.join("");}};this.decode=function(json){return eval("("+json+')');};})();Ext.encode=Ext.util.JSON.encode;Ext.decode=Ext.util.JSON.decode;

Ext.util.Format=function(){var trimRe=/^\s+|\s+$/g;return{ellipsis:function(value,len){if(value&&value.length>len){return value.substr(0,len-3)+"...";}
return value;},undef:function(value){return value!==undefined?value:"";},defaultValue:function(value,defaultValue){return value!==undefined&&value!==''?value:defaultValue;},htmlEncode:function(value){return!value?value:String(value).replace(/&/g,"&amp;").replace(/>/g,"&gt;").replace(/</g,"&lt;").replace(/"/g,"&quot;");},htmlDecode:function(value){return!value?value:String(value).replace(/&gt;/g,">").replace(/&lt;/g,"<").replace(/&quot;/g,'"').replace(/&amp;/g,"&");},trim:function(value){return String(value).replace(trimRe,"");},substr:function(value,start,length){return String(value).substr(start,length);},lowercase:function(value){return String(value).toLowerCase();},uppercase:function(value){return String(value).toUpperCase();},capitalize:function(value){return!value?value:value.charAt(0).toUpperCase()+value.substr(1).toLowerCase();},call:function(value,fn){if(arguments.length>2){var args=Array.prototype.slice.call(arguments,2);args.unshift(value);return eval(fn).apply(window,args);}else{return eval(fn).call(window,value);}},usMoney:function(v){v=(Math.round((v-0)*100))/100;v=(v==Math.floor(v))?v+".00":((v*10==Math.floor(v*10))?v+"0":v);v=String(v);var ps=v.split('.');var whole=ps[0];var sub=ps[1]?'.'+ps[1]:'.00';var r=/(\d+)(\d{3})/;while(r.test(whole)){whole=whole.replace(r,'$1'+','+'$2');}
v=whole+sub;if(v.charAt(0)=='-'){return'-$'+v.substr(1);}
return"$"+v;},date:function(v,format){if(!v){return"";}
if(!Ext.isDate(v)){v=new Date(Date.parse(v));}
return v.dateFormat(format||"m/d/Y");},dateRenderer:function(format){return function(v){return Ext.util.Format.date(v,format);};},stripTagsRE:/<\/?[^>]+>/gi,stripTags:function(v){return!v?v:String(v).replace(this.stripTagsRE,"");},stripScriptsRe:/(?:<script.*?>)((\n|\r|.)*?)(?:<\/script>)/ig,stripScripts:function(v){return!v?v:String(v).replace(this.stripScriptsRe,"");},fileSize:function(size){if(size<1024){return size+" bytes";}else if(size<1048576){return(Math.round(((size*10)/1024))/10)+" KB";}else{return(Math.round(((size*10)/1048576))/10)+" MB";}},math:function(){var fns={};return function(v,a){if(!fns[a]){fns[a]=new Function('v','return v '+a+';');}
return fns[a](v);}}(),nl2br:function(v){return v===undefined||v===null?'':v.replace(/\n/g,'<br/>');}};}();

Ext.util.CSS=function(){var rules=null;var doc=document;var camelRe=/(-[a-z])/gi;var camelFn=function(m,a){return a.charAt(1).toUpperCase();};return{createStyleSheet:function(cssText,id){var ss;var head=doc.getElementsByTagName("head")[0];var rules=doc.createElement("style");rules.setAttribute("type","text/css");if(id){rules.setAttribute("id",id);}
if(Ext.isIE){head.appendChild(rules);ss=rules.styleSheet;ss.cssText=cssText;}else{try{rules.appendChild(doc.createTextNode(cssText));}catch(e){rules.cssText=cssText;}
head.appendChild(rules);ss=rules.styleSheet?rules.styleSheet:(rules.sheet||doc.styleSheets[doc.styleSheets.length-1]);}
this.cacheStyleSheet(ss);return ss;},removeStyleSheet:function(id){var existing=doc.getElementById(id);if(existing){existing.parentNode.removeChild(existing);}},swapStyleSheet:function(id,url){this.removeStyleSheet(id);var ss=doc.createElement("link");ss.setAttribute("rel","stylesheet");ss.setAttribute("type","text/css");ss.setAttribute("id",id);ss.setAttribute("href",url);doc.getElementsByTagName("head")[0].appendChild(ss);},refreshCache:function(){return this.getRules(true);},cacheStyleSheet:function(ss){if(!rules){rules={};}
try{var ssRules=ss.cssRules||ss.rules;for(var j=ssRules.length-1;j>=0;--j){rules[ssRules[j].selectorText]=ssRules[j];}}catch(e){}},getRules:function(refreshCache){if(rules==null||refreshCache){rules={};var ds=doc.styleSheets;for(var i=0,len=ds.length;i<len;i++){try{this.cacheStyleSheet(ds[i]);}catch(e){}}}
return rules;},getRule:function(selector,refreshCache){var rs=this.getRules(refreshCache);if(!Ext.isArray(selector)){return rs[selector];}
for(var i=0;i<selector.length;i++){if(rs[selector[i]]){return rs[selector[i]];}}
return null;},updateRule:function(selector,property,value){if(!Ext.isArray(selector)){var rule=this.getRule(selector);if(rule){rule.style[property.replace(camelRe,camelFn)]=value;return true;}}else{for(var i=0;i<selector.length;i++){if(this.updateRule(selector[i],property,value)){return true;}}}
return false;}};}();

Ext.util.ClickRepeater=function(el,config)
{this.el=Ext.get(el);this.el.unselectable();Ext.apply(this,config);this.addEvents("mousedown","click","mouseup");if(!this.disabled){this.disabled=true;this.enable();}
if(this.handler){this.on("click",this.handler,this.scope||this);}
Ext.util.ClickRepeater.superclass.constructor.call(this);};Ext.extend(Ext.util.ClickRepeater,Ext.util.Observable,{interval:20,delay:250,preventDefault:true,stopDefault:false,timer:0,enable:function(){if(this.disabled){this.el.on('mousedown',this.handleMouseDown,this);if(this.preventDefault||this.stopDefault){this.el.on('click',this.eventOptions,this);}}
this.disabled=false;},disable:function(force){if(force||!this.disabled){clearTimeout(this.timer);if(this.pressClass){this.el.removeClass(this.pressClass);}
Ext.getDoc().un('mouseup',this.handleMouseUp,this);this.el.removeAllListeners();}
this.disabled=true;},setDisabled:function(disabled){this[disabled?'disable':'enable']();},eventOptions:function(e){if(this.preventDefault){e.preventDefault();}
if(this.stopDefault){e.stopEvent();}},destroy:function(){this.disable(true);Ext.destroy(this.el);this.purgeListeners();},handleMouseDown:function(){clearTimeout(this.timer);this.el.blur();if(this.pressClass){this.el.addClass(this.pressClass);}
this.mousedownTime=new Date();Ext.getDoc().on("mouseup",this.handleMouseUp,this);this.el.on("mouseout",this.handleMouseOut,this);this.fireEvent("mousedown",this);this.fireEvent("click",this);if(this.accelerate){this.delay=400;}
this.timer=this.click.defer(this.delay||this.interval,this);},click:function(){this.fireEvent("click",this);this.timer=this.click.defer(this.accelerate?this.easeOutExpo(this.mousedownTime.getElapsed(),400,-390,12000):this.interval,this);},easeOutExpo:function(t,b,c,d){return(t==d)?b+c:c*(-Math.pow(2,-10*t/d)+1)+b;},handleMouseOut:function(){clearTimeout(this.timer);if(this.pressClass){this.el.removeClass(this.pressClass);}
this.el.on("mouseover",this.handleMouseReturn,this);},handleMouseReturn:function(){this.el.un("mouseover",this.handleMouseReturn,this);if(this.pressClass){this.el.addClass(this.pressClass);}
this.click();},handleMouseUp:function(){clearTimeout(this.timer);this.el.un("mouseover",this.handleMouseReturn,this);this.el.un("mouseout",this.handleMouseOut,this);Ext.getDoc().un("mouseup",this.handleMouseUp,this);this.el.removeClass(this.pressClass);this.fireEvent("mouseup",this);}});

Ext.KeyNav=function(el,config){this.el=Ext.get(el);Ext.apply(this,config);if(!this.disabled){this.disabled=true;this.enable();}};Ext.KeyNav.prototype={disabled:false,defaultEventAction:"stopEvent",forceKeyDown:false,prepareEvent:function(e){var k=e.getKey();var h=this.keyToHandler[k];if(Ext.isSafari2&&h&&k>=37&&k<=40){e.stopEvent();}},relay:function(e){var k=e.getKey();var h=this.keyToHandler[k];if(h&&this[h]){if(this.doRelay(e,this[h],h)!==true){e[this.defaultEventAction]();}}},doRelay:function(e,h,hname){return h.call(this.scope||this,e);},enter:false,left:false,right:false,up:false,down:false,tab:false,esc:false,pageUp:false,pageDown:false,del:false,home:false,end:false,keyToHandler:{37:"left",39:"right",38:"up",40:"down",33:"pageUp",34:"pageDown",46:"del",36:"home",35:"end",13:"enter",27:"esc",9:"tab"},enable:function(){if(this.disabled){if(this.isKeyDown()){this.el.on("keydown",this.relay,this);}else{this.el.on("keydown",this.prepareEvent,this);this.el.on("keypress",this.relay,this);}
this.disabled=false;}},disable:function(){if(!this.disabled){if(this.isKeyDown()){this.el.un("keydown",this.relay,this);}else{this.el.un("keydown",this.prepareEvent,this);this.el.un("keypress",this.relay,this);}
this.disabled=true;}},isKeyDown:function(){return this.forceKeyDown||Ext.isIE||(Ext.isWebKit&&!Ext.isSafari2)||Ext.isAir;}};

Ext.KeyMap=function(el,config,eventName){this.el=Ext.get(el);this.eventName=eventName||"keydown";this.bindings=[];if(config){this.addBinding(config);}
this.enable();};Ext.KeyMap.prototype={stopEvent:false,addBinding:function(config){if(Ext.isArray(config)){for(var i=0,len=config.length;i<len;i++){this.addBinding(config[i]);}
return;}
var keyCode=config.key,shift=config.shift,ctrl=config.ctrl,alt=config.alt,fn=config.fn||config.handler,scope=config.scope;if(config.stopEvent){this.stopEvent=config.stopEvent;}
if(typeof keyCode=="string"){var ks=[];var keyString=keyCode.toUpperCase();for(var j=0,len=keyString.length;j<len;j++){ks.push(keyString.charCodeAt(j));}
keyCode=ks;}
var keyArray=Ext.isArray(keyCode);var handler=function(e){if((!shift||e.shiftKey)&&(!ctrl||e.ctrlKey)&&(!alt||e.altKey)){var k=e.getKey();if(keyArray){for(var i=0,len=keyCode.length;i<len;i++){if(keyCode[i]==k){if(this.stopEvent){e.stopEvent();}
fn.call(scope||window,k,e);return;}}}else{if(k==keyCode){if(this.stopEvent){e.stopEvent();}
fn.call(scope||window,k,e);}}}};this.bindings.push(handler);},on:function(key,fn,scope){var keyCode,shift,ctrl,alt;if(typeof key=="object"&&!Ext.isArray(key)){keyCode=key.key;shift=key.shift;ctrl=key.ctrl;alt=key.alt;}else{keyCode=key;}
this.addBinding({key:keyCode,shift:shift,ctrl:ctrl,alt:alt,fn:fn,scope:scope})},handleKeyDown:function(e){if(this.enabled){var b=this.bindings;for(var i=0,len=b.length;i<len;i++){b[i].call(this,e);}}},isEnabled:function(){return this.enabled;},enable:function(){if(!this.enabled){this.el.on(this.eventName,this.handleKeyDown,this);this.enabled=true;}},disable:function(){if(this.enabled){this.el.removeListener(this.eventName,this.handleKeyDown,this);this.enabled=false;}}};

Ext.util.TextMetrics=function(){var shared;return{measure:function(el,text,fixedWidth){if(!shared){shared=Ext.util.TextMetrics.Instance(el,fixedWidth);}
shared.bind(el);shared.setFixedWidth(fixedWidth||'auto');return shared.getSize(text);},createInstance:function(el,fixedWidth){return Ext.util.TextMetrics.Instance(el,fixedWidth);}};}();Ext.util.TextMetrics.Instance=function(bindTo,fixedWidth){var ml=new Ext.Element(document.createElement('div'));document.body.appendChild(ml.dom);ml.position('absolute');ml.setLeftTop(-1000,-1000);ml.hide();if(fixedWidth){ml.setWidth(fixedWidth);}
var instance={getSize:function(text){ml.update(text);var s=ml.getSize();ml.update('');return s;},bind:function(el){ml.setStyle(Ext.fly(el).getStyles('font-size','font-style','font-weight','font-family','line-height','text-transform','letter-spacing'));},setFixedWidth:function(width){ml.setWidth(width);},getWidth:function(text){ml.dom.style.width='auto';return this.getSize(text).width;},getHeight:function(text){return this.getSize(text).height;}};instance.bind(bindTo);return instance;};Ext.Element.measureText=Ext.util.TextMetrics.measure;
