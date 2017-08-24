var _jsonix_factory = function(_jsonix_xmldom, _jsonix_xmlhttprequest, _jsonix_fs)
{
	// Complete Jsonix script is included below 
var Jsonix={singleFile:true};
Jsonix.Util={};
Jsonix.Util.extend=function(f,g){f=f||{};
if(g){for(var h in g){var e=g[h];
if(e!==undefined){f[h]=e
}}sourceIsEvt=typeof window!=="undefined"&&window!==null&&typeof window.Event==="function"&&g instanceof window.Event;
if(!sourceIsEvt&&g.hasOwnProperty&&g.hasOwnProperty("toString")){f.toString=g.toString
}}return f
};
Jsonix.Class=function(){var n=function(){this.initialize.apply(this,arguments)
};
var p={};
var k=function(){};
var l,i,o;
for(var m=0,j=arguments.length;
m<j;
++m){o=arguments[m];
if(typeof o=="function"){if(m===0&&j>1){i=o.prototype.initialize;
o.prototype.initialize=k;
p=new o();
if(i===undefined){delete o.prototype.initialize
}else{o.prototype.initialize=i
}}l=o.prototype
}else{l=o
}Jsonix.Util.extend(p,l)
}n.prototype=p;
return n
};
Jsonix.XML={XMLNS_NS:"http://www.w3.org/2000/xmlns/",XMLNS_P:"xmlns"};
Jsonix.DOM={isDomImplementationAvailable:function(){if(typeof _jsonix_xmldom!=="undefined"){return true
}else{if(typeof document!=="undefined"&&Jsonix.Util.Type.exists(document.implementation)&&Jsonix.Util.Type.isFunction(document.implementation.createDocument)){return true
}else{return false
}}},createDocument:function(){if(typeof _jsonix_xmldom!=="undefined"){return new (_jsonix_xmldom.DOMImplementation)().createDocument()
}else{if(typeof document!=="undefined"&&Jsonix.Util.Type.exists(document.implementation)&&Jsonix.Util.Type.isFunction(document.implementation.createDocument)){return document.implementation.createDocument("","",null)
}else{if(typeof ActiveXObject!=="undefined"){return new ActiveXObject("MSXML2.DOMDocument")
}else{throw new Error("Error created the DOM document.")
}}}},serialize:function(b){Jsonix.Util.Ensure.ensureExists(b);
if(typeof _jsonix_xmldom!=="undefined"){return(new (_jsonix_xmldom).XMLSerializer()).serializeToString(b)
}else{if(Jsonix.Util.Type.exists(XMLSerializer)){return(new XMLSerializer()).serializeToString(b)
}else{if(Jsonix.Util.Type.exists(b.xml)){return b.xml
}else{throw new Error("Could not serialize the node, neither XMLSerializer nor the [xml] property were found.")
}}}},parse:function(g){Jsonix.Util.Ensure.ensureExists(g);
if(typeof _jsonix_xmldom!=="undefined"){return(new (_jsonix_xmldom).DOMParser()).parseFromString(g,"application/xml")
}else{if(typeof DOMParser!="undefined"){return(new DOMParser()).parseFromString(g,"application/xml")
}else{if(typeof ActiveXObject!="undefined"){var h=Jsonix.DOM.createDocument("","");
h.loadXML(g);
return h
}else{var f="data:text/xml;charset=utf-8,"+encodeURIComponent(g);
var e=new XMLHttpRequest();
e.open("GET",f,false);
if(e.overrideMimeType){e.overrideMimeType("text/xml")
}e.send(null);
return e.responseXML
}}}},load:function(e,g,f){var h=Jsonix.Request.INSTANCE;
h.issue(e,function(a){var b;
if(Jsonix.Util.Type.exists(a.responseXML)&&Jsonix.Util.Type.exists(a.responseXML.documentElement)){b=a.responseXML
}else{if(Jsonix.Util.Type.isString(a.responseText)){b=Jsonix.DOM.parse(a.responseText)
}else{throw new Error("Response does not have valid [responseXML] or [responseText].")
}}g(b)
},function(a){throw new Error("Could not retrieve XML from URL ["+e+"].")
},f)
},xlinkFixRequired:null,isXlinkFixRequired:function(){if(Jsonix.DOM.xlinkFixRequired===null){if(typeof navigator==="undefined"){Jsonix.DOM.xlinkFixRequired=false
}else{if(!!navigator.userAgent&&(/Chrome/.test(navigator.userAgent)&&/Google Inc/.test(navigator.vendor))){var f=Jsonix.DOM.createDocument();
var d=f.createElement("test");
d.setAttributeNS("http://www.w3.org/1999/xlink","xlink:href","urn:test");
f.appendChild(d);
var e=Jsonix.DOM.serialize(f);
Jsonix.DOM.xlinkFixRequired=(e.indexOf("xmlns:xlink")===-1)
}else{Jsonix.DOM.xlinkFixRequired=false
}}}return Jsonix.DOM.xlinkFixRequired
}};
Jsonix.Request=Jsonix.Class({factories:[function(){return new XMLHttpRequest()
},function(){return new ActiveXObject("Msxml2.XMLHTTP")
},function(){return new ActiveXObject("Msxml2.XMLHTTP.6.0")
},function(){return new ActiveXObject("Msxml2.XMLHTTP.3.0")
},function(){return new ActiveXObject("Microsoft.XMLHTTP")
},function(){if(typeof _jsonix_xmlhttprequest!=="undefined"){var b=_jsonix_xmlhttprequest.XMLHttpRequest;
return new b()
}else{return null
}}],initialize:function(){},issue:function(y,q,x,n){Jsonix.Util.Ensure.ensureString(y);
if(Jsonix.Util.Type.exists(q)){Jsonix.Util.Ensure.ensureFunction(q)
}else{q=function(){}
}if(Jsonix.Util.Type.exists(x)){Jsonix.Util.Ensure.ensureFunction(x)
}else{x=function(){}
}if(Jsonix.Util.Type.exists(n)){Jsonix.Util.Ensure.ensureObject(n)
}else{n={}
}var w=this.createTransport();
var z=Jsonix.Util.Type.isString(n.method)?n.method:"GET";
var v=Jsonix.Util.Type.isBoolean(n.async)?n.async:true;
var p=Jsonix.Util.Type.isString(n.proxy)?n.proxy:Jsonix.Request.PROXY;
var u=Jsonix.Util.Type.isString(n.user)?n.user:null;
var o=Jsonix.Util.Type.isString(n.password)?n.password:null;
if(Jsonix.Util.Type.isString(p)&&(y.indexOf("http")===0)){y=p+encodeURIComponent(y)
}if(Jsonix.Util.Type.isString(u)){w.open(z,y,v,u,o)
}else{w.open(z,y,v)
}if(Jsonix.Util.Type.isObject(n.headers)){for(var s in n.headers){if(n.headers.hasOwnProperty(s)){w.setRequestHeader(s,n.headers[s])
}}}var t=Jsonix.Util.Type.exists(n.data)?n.data:null;
if(!v){w.send(t);
this.handleTransport(w,q,x)
}else{var r=this;
if(typeof window!=="undefined"){w.onreadystatechange=function(){r.handleTransport(w,q,x)
};
window.setTimeout(function(){w.send(t)
},0)
}else{w.onreadystatechange=function(){r.handleTransport(w,q,x)
};
w.send(t)
}}return w
},handleTransport:function(f,d,e){if(f.readyState==4){if(!f.status||(f.status>=200&&f.status<300)){d(f)
}if(f.status&&(f.status<200||f.status>=300)){e(f)
}}},createTransport:function(){for(var f=0,e=this.factories.length;
f<e;
f++){try{var g=this.factories[f]();
if(g!==null){return g
}}catch(h){}}throw new Error("Could not create XML HTTP transport.")
},CLASS_NAME:"Jsonix.Request"});
Jsonix.Request.INSTANCE=new Jsonix.Request();
Jsonix.Request.PROXY=null;
Jsonix.Schema={};
Jsonix.Model={};
Jsonix.Util.Type={exists:function(b){return(typeof b!=="undefined"&&b!==null)
},isUndefined:function(b){return typeof b==="undefined"
},isString:function(b){return typeof b==="string"
},isBoolean:function(b){return typeof b==="boolean"
},isObject:function(b){return typeof b==="object"
},isFunction:function(b){return typeof b==="function"
},isNumber:function(b){return(typeof b==="number")&&!isNaN(b)
},isNumberOrNaN:function(b){return(b===+b)||(Object.prototype.toString.call(b)==="[object Number]")
},isNaN:function(b){return Jsonix.Util.Type.isNumberOrNaN(b)&&isNaN(b)
},isArray:function(b){return !!(b&&b.concat&&b.unshift&&!b.callee)
},isDate:function(b){return !!(b&&b.getTimezoneOffset&&b.setUTCFullYear)
},isRegExp:function(b){return !!(b&&b.test&&b.exec&&(b.ignoreCase||b.ignoreCase===false))
},isNode:function(b){return(typeof Node==="object"||typeof Node==="function")?(b instanceof Node):(b&&(typeof b==="object")&&(typeof b.nodeType==="number")&&(typeof b.nodeName==="string"))
},isEqual:function(u,w,A){var a=Jsonix.Util.Type.isFunction(A);
var y=function(g,e,m){var i=slice.call(arguments);
var h=i.length<=1;
var f=h?0:i[0];
var d=h?i[0]:i[1];
var j=i[2]||1;
var k=Math.max(Math.ceil((d-f)/j),0);
var c=0;
var l=new Array(k);
while(c<k){l[c++]=f;
f+=j
}return l
};
var x=Object.keys||function(e){if(Jsonix.Util.Type.isArray(e)){return y(0,e.length)
}var c=[];
for(var d in e){if(e.hasOwnProperty(d)){c[c.length]=d
}}return c
};
if(u===w){return true
}if(Jsonix.Util.Type.isNaN(u)&&Jsonix.Util.Type.isNaN(w)){return true
}var C=typeof u;
var E=typeof w;
if(C!=E){if(a){A("Types differ ["+C+"], ["+E+"].")
}return false
}if(u==w){return true
}if((!u&&w)||(u&&!w)){if(a){A("One is falsy, the other is truthy.")
}return false
}if(Jsonix.Util.Type.isDate(u)&&Jsonix.Util.Type.isDate(w)){return u.getTime()===w.getTime()
}if(Jsonix.Util.Type.isNaN(u)&&Jsonix.Util.Type.isNaN(w)){return false
}if(Jsonix.Util.Type.isRegExp(u)&&Jsonix.Util.Type.isRegExp(w)){return u.source===w.source&&u.global===w.global&&u.ignoreCase===w.ignoreCase&&u.multiline===w.multiline
}if(Jsonix.Util.Type.isNode(u)&&Jsonix.Util.Type.isNode(w)){var b=Jsonix.DOM.serialize(u);
var z=Jsonix.DOM.serialize(w);
if(b!==z){if(a){A("Nodes differ.");
A("A="+b);
A("B="+z)
}return false
}else{return true
}}if(C!=="object"){return false
}if(Jsonix.Util.Type.isArray(u)&&(u.length!==w.length)){if(a){A("Lengths differ.");
A("A.length="+u.length);
A("B.length="+w.length)
}return false
}var s=x(u);
var v=x(w);
if(s.length!==v.length){if(a){A("Different number of properties ["+s.length+"], ["+v.length+"].")
}for(var D=0;
D<s.length;
D++){if(a){A("A ["+s[D]+"]="+u[s[D]])
}}for(var F=0;
F<v.length;
F++){if(a){A("B ["+v[F]+"]="+w[v[F]])
}}return false
}for(var B=0;
B<s.length;
B++){var t=s[B];
if(!(t in w)||!Jsonix.Util.Type.isEqual(u[t],w[t],A)){if(a){A("One of the properties differ.");
A("Key: ["+t+"].");
A("Left: ["+u[t]+"].");
A("Right: ["+w[t]+"].")
}return false
}}return true
},cloneObject:function(e,f){f=f||{};
for(var d in e){if(e.hasOwnProperty(d)){f[d]=e[d]
}}return f
},defaultValue:function(){var i=arguments;
if(i.length===0){return undefined
}else{var g=i[i.length-1];
var f=typeof g;
for(var j=0;
j<i.length-1;
j++){var h=i[j];
if(typeof h===f){return h
}}return g
}}};
Jsonix.Util.NumberUtils={isInteger:function(b){return Jsonix.Util.Type.isNumber(b)&&((b%1)===0)
}};
Jsonix.Util.StringUtils={trim:(!!String.prototype.trim)?function(b){Jsonix.Util.Ensure.ensureString(b);
return b.trim()
}:function(b){Jsonix.Util.Ensure.ensureString(b);
return b.replace(/^\s\s*/,"").replace(/\s\s*$/,"")
},isEmpty:function(h){var c=h.length;
if(!c){return true
}for(var f=0;
f<c;
f++){var g=h[f];
if(g===" "){}else{if(g>"\u000D"&&g<"\u0085"){return false
}else{if(g<"\u00A0"){if(g<"\u0009"){return false
}else{if(g>"\u0085"){return false
}}}else{if(g>"\u00A0"){if(g<"\u2028"){if(g<"\u180E"){if(g<"\u1680"){return false
}else{if(g>"\u1680"){return false
}}}else{if(g>"\u180E"){if(g<"\u2000"){return false
}else{if(g>"\u200A"){return false
}}}}}else{if(g>"\u2029"){if(g<"\u205F"){if(g<"\u202F"){return false
}else{if(g>"\u202F"){return false
}}}else{if(g>"\u205F"){if(g<"\u3000"){return false
}else{if(g>"\u3000"){return false
}}}}}}}}}}}return true
},isNotBlank:function(b){return Jsonix.Util.Type.isString(b)&&!Jsonix.Util.StringUtils.isEmpty(b)
},whitespaceCharacters:"\u0009\u000A\u000B\u000C\u000D \u0085\u00A0\u1680\u180E\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u2028\u2029\u202F\u205F\u3000",whitespaceCharactersMap:{"\u0009":true,"\u000A":true,"\u000B":true,"\u000C":true,"\u000D":true," ":true,"\u0085":true,"\u00A0":true,"\u1680":true,"\u180E":true,"\u2000":true,"\u2001":true,"\u2002":true,"\u2003":true,"\u2004":true,"\u2005":true,"\u2006":true,"\u2007":true,"\u2008":true,"\u2009":true,"\u200A":true,"\u2028":true,"\u2029":true,"\u202F":true,"\u205F":true,"\u3000":true},splitBySeparatorChars:function(n,u){Jsonix.Util.Ensure.ensureString(n);
Jsonix.Util.Ensure.ensureString(u);
var p=n.length;
if(p===0){return[]
}if(u.length===1){return n.split(u)
}else{var o=[];
var t=1;
var r=0;
var v=0;
var q=false;
var s=false;
var m=-1;
var i=false;
while(r<p){if(u.indexOf(n.charAt(r))>=0){if(q||i){s=true;
if(t++==m){r=p;
s=false
}o.push(n.substring(v,r));
q=false
}v=++r;
continue
}s=false;
q=true;
r++
}if(q||(i&&s)){o.push(n.substring(v,r))
}return o
}}};
Jsonix.Util.Ensure={ensureBoolean:function(b){if(!Jsonix.Util.Type.isBoolean(b)){throw new Error("Argument ["+b+"] must be a boolean.")
}},ensureString:function(b){if(!Jsonix.Util.Type.isString(b)){throw new Error("Argument ["+b+"] must be a string.")
}},ensureNumber:function(b){if(!Jsonix.Util.Type.isNumber(b)){throw new Error("Argument ["+b+"] must be a number.")
}},ensureNumberOrNaN:function(b){if(!Jsonix.Util.Type.isNumberOrNaN(b)){throw new Error("Argument ["+b+"] must be a number or NaN.")
}},ensureInteger:function(b){if(!Jsonix.Util.Type.isNumber(b)){throw new Error("Argument must be an integer, but it is not a number.")
}else{if(!Jsonix.Util.NumberUtils.isInteger(b)){throw new Error("Argument ["+b+"] must be an integer.")
}}},ensureDate:function(b){if(!(b instanceof Date)){throw new Error("Argument ["+b+"] must be a date.")
}},ensureObject:function(b){if(!Jsonix.Util.Type.isObject(b)){throw new Error("Argument ["+b+"] must be an object.")
}},ensureArray:function(b){if(!Jsonix.Util.Type.isArray(b)){throw new Error("Argument ["+b+"] must be an array.")
}},ensureFunction:function(b){if(!Jsonix.Util.Type.isFunction(b)){throw new Error("Argument ["+b+"] must be a function.")
}},ensureExists:function(b){if(!Jsonix.Util.Type.exists(b)){throw new Error("Argument ["+b+"] does not exist.")
}}};
Jsonix.XML.QName=Jsonix.Class({key:null,namespaceURI:null,localPart:null,prefix:null,string:null,initialize:function(n,j,r){var q;
var o;
var m;
var k;
var l;
if(!Jsonix.Util.Type.exists(j)){q="";
o=n;
m=""
}else{if(!Jsonix.Util.Type.exists(r)){q=Jsonix.Util.Type.exists(n)?n:"";
o=j;
var p=j.indexOf(":");
if(p>0&&p<j.length){m=j.substring(0,p);
o=j.substring(p+1)
}else{m="";
o=j
}}else{q=Jsonix.Util.Type.exists(n)?n:"";
o=j;
m=Jsonix.Util.Type.exists(r)?r:""
}}this.namespaceURI=q;
this.localPart=o;
this.prefix=m;
this.key=(q!==""?("{"+q+"}"):"")+o;
this.string=(q!==""?("{"+q+"}"):"")+(m!==""?(m+":"):"")+o
},toString:function(){return this.string
},toCanonicalString:function(c){var d=c?c.getPrefix(this.namespaceURI,this.prefix):this.prefix;
return this.prefix+(this.prefix===""?"":":")+this.localPart
},clone:function(){return new Jsonix.XML.QName(this.namespaceURI,this.localPart,this.prefix)
},equals:function(b){if(!b){return false
}else{return(this.namespaceURI==b.namespaceURI)&&(this.localPart==b.localPart)
}},CLASS_NAME:"Jsonix.XML.QName"});
Jsonix.XML.QName.fromString=function(n,r,m){var t=n.indexOf("{");
var l=n.lastIndexOf("}");
var s;
var k;
if((t===0)&&(l>0)&&(l<n.length)){s=n.substring(1,l);
k=n.substring(l+1)
}else{s=null;
k=n
}var q=k.indexOf(":");
var o;
var p;
if(q>0&&q<k.length){o=k.substring(0,q);
p=k.substring(q+1)
}else{o="";
p=k
}if(s===null){if(o===""&&Jsonix.Util.Type.isString(m)){s=m
}else{if(r){s=r.getNamespaceURI(o)
}}}if(!Jsonix.Util.Type.isString(s)){s=m||""
}return new Jsonix.XML.QName(s,p,o)
};
Jsonix.XML.QName.fromObject=function(h){Jsonix.Util.Ensure.ensureObject(h);
if(h instanceof Jsonix.XML.QName||(Jsonix.Util.Type.isString(h.CLASS_NAME)&&h.CLASS_NAME==="Jsonix.XML.QName")){return h
}var f=h.localPart||h.lp||null;
Jsonix.Util.Ensure.ensureString(f);
var e=h.namespaceURI||h.ns||"";
var g=h.prefix||h.p||"";
return new Jsonix.XML.QName(e,f,g)
};
Jsonix.XML.QName.fromObjectOrString=function(f,d,e){if(Jsonix.Util.Type.isString(f)){return Jsonix.XML.QName.fromString(f,d,e)
}else{return Jsonix.XML.QName.fromObject(f)
}};
Jsonix.XML.QName.key=function(f,e){Jsonix.Util.Ensure.ensureString(e);
if(f){var d=e.indexOf(":");
if(d>0&&d<e.length){localName=e.substring(d+1)
}else{localName=e
}return"{"+f+"}"+localName
}else{return e
}};
Jsonix.XML.Calendar=Jsonix.Class({year:NaN,month:NaN,day:NaN,hour:NaN,minute:NaN,second:NaN,fractionalSecond:NaN,timezone:NaN,date:null,initialize:function(f){Jsonix.Util.Ensure.ensureObject(f);
if(Jsonix.Util.Type.exists(f.year)){Jsonix.Util.Ensure.ensureInteger(f.year);
Jsonix.XML.Calendar.validateYear(f.year);
this.year=f.year
}else{this.year=NaN
}if(Jsonix.Util.Type.exists(f.month)){Jsonix.Util.Ensure.ensureInteger(f.month);
Jsonix.XML.Calendar.validateMonth(f.month);
this.month=f.month
}else{this.month=NaN
}if(Jsonix.Util.Type.exists(f.day)){Jsonix.Util.Ensure.ensureInteger(f.day);
if(Jsonix.Util.NumberUtils.isInteger(f.year)&&Jsonix.Util.NumberUtils.isInteger(f.month)){Jsonix.XML.Calendar.validateYearMonthDay(f.year,f.month,f.day)
}else{if(Jsonix.Util.NumberUtils.isInteger(f.month)){Jsonix.XML.Calendar.validateMonthDay(f.month,f.day)
}else{Jsonix.XML.Calendar.validateDay(f.day)
}}this.day=f.day
}else{this.day=NaN
}if(Jsonix.Util.Type.exists(f.hour)){Jsonix.Util.Ensure.ensureInteger(f.hour);
Jsonix.XML.Calendar.validateHour(f.hour);
this.hour=f.hour
}else{this.hour=NaN
}if(Jsonix.Util.Type.exists(f.minute)){Jsonix.Util.Ensure.ensureInteger(f.minute);
Jsonix.XML.Calendar.validateMinute(f.minute);
this.minute=f.minute
}else{this.minute=NaN
}if(Jsonix.Util.Type.exists(f.second)){Jsonix.Util.Ensure.ensureInteger(f.second);
Jsonix.XML.Calendar.validateSecond(f.second);
this.second=f.second
}else{this.second=NaN
}if(Jsonix.Util.Type.exists(f.fractionalSecond)){Jsonix.Util.Ensure.ensureNumber(f.fractionalSecond);
Jsonix.XML.Calendar.validateFractionalSecond(f.fractionalSecond);
this.fractionalSecond=f.fractionalSecond
}else{this.fractionalSecond=NaN
}if(Jsonix.Util.Type.exists(f.timezone)){if(Jsonix.Util.Type.isNaN(f.timezone)){this.timezone=NaN
}else{Jsonix.Util.Ensure.ensureInteger(f.timezone);
Jsonix.XML.Calendar.validateTimezone(f.timezone);
this.timezone=f.timezone
}}else{this.timezone=NaN
}var d=new Date(0);
d.setUTCFullYear(this.year||1970);
d.setUTCMonth(this.month-1||0);
d.setUTCDate(this.day||1);
d.setUTCHours(this.hour||0);
d.setUTCMinutes(this.minute||0);
d.setUTCSeconds(this.second||0);
d.setUTCMilliseconds((this.fractionalSecond||0)*1000);
var e=-60000*(this.timezone||0);
this.date=new Date(d.getTime()+e)
},CLASS_NAME:"Jsonix.XML.Calendar"});
Jsonix.XML.Calendar.MIN_TIMEZONE=-14*60;
Jsonix.XML.Calendar.MAX_TIMEZONE=14*60;
Jsonix.XML.Calendar.DAYS_IN_MONTH=[31,29,31,30,31,30,31,31,30,31,30,31];
Jsonix.XML.Calendar.fromObject=function(b){Jsonix.Util.Ensure.ensureObject(b);
if(Jsonix.Util.Type.isString(b.CLASS_NAME)&&b.CLASS_NAME==="Jsonix.XML.Calendar"){return b
}return new Jsonix.XML.Calendar(b)
};
Jsonix.XML.Calendar.validateYear=function(b){if(b===0){throw new Error("Invalid year ["+b+"]. Year must not be [0].")
}};
Jsonix.XML.Calendar.validateMonth=function(b){if(b<1||b>12){throw new Error("Invalid month ["+b+"]. Month must be in range [1, 12].")
}};
Jsonix.XML.Calendar.validateDay=function(b){if(b<1||b>31){throw new Error("Invalid day ["+b+"]. Day must be in range [1, 31].")
}};
Jsonix.XML.Calendar.validateMonthDay=function(f,d){Jsonix.XML.Calendar.validateMonth(f);
var e=Jsonix.XML.Calendar.DAYS_IN_MONTH[f-1];
if(d<1||d>Jsonix.XML.Calendar.DAYS_IN_MONTH[f-1]){throw new Error("Invalid day ["+d+"]. Day must be in range [1, "+e+"].")
}};
Jsonix.XML.Calendar.validateYearMonthDay=function(d,f,e){Jsonix.XML.Calendar.validateYear(d);
Jsonix.XML.Calendar.validateMonthDay(f,e)
};
Jsonix.XML.Calendar.validateHour=function(b){if(b<0||b>23){throw new Error("Invalid hour ["+b+"]. Hour must be in range [0, 23].")
}};
Jsonix.XML.Calendar.validateMinute=function(b){if(b<0||b>59){throw new Error("Invalid minute ["+b+"]. Minute must be in range [0, 59].")
}};
Jsonix.XML.Calendar.validateSecond=function(b){if(b<0||b>59){throw new Error("Invalid second ["+b+"]. Second must be in range [0, 59].")
}};
Jsonix.XML.Calendar.validateFractionalSecond=function(b){if(b<0||b>59){throw new Error("Invalid fractional second ["+b+"]. Fractional second must be in range [0, 1).")
}};
Jsonix.XML.Calendar.validateTimezone=function(b){if(b<Jsonix.XML.Calendar.MIN_TIMEZONE||b>Jsonix.XML.Calendar.MAX_TIMEZONE){throw new Error("Invalid timezone ["+b+"]. Timezone must not be in range ["+Jsonix.XML.Calendar.MIN_TIMEZONE+", "+Jsonix.XML.Calendar.MAX_TIMEZONE+"].")
}};
Jsonix.XML.Input=Jsonix.Class({root:null,node:null,attributes:null,eventType:null,pns:null,initialize:function(c){Jsonix.Util.Ensure.ensureExists(c);
this.root=c;
var d={"":""};
d[Jsonix.XML.XMLNS_P]=Jsonix.XML.XMLNS_NS;
this.pns=[d]
},hasNext:function(){if(this.node===null){return true
}else{if(this.node===this.root){var b=this.node.nodeType;
if(b===9&&this.eventType===8){return false
}else{if(b===1&&this.eventType===2){return false
}else{return true
}}}else{return true
}}},next:function(){if(this.eventType===null){return this.enter(this.root)
}if(this.eventType===7){var d=this.node.documentElement;
if(d){return this.enter(d)
}else{return this.leave(this.node)
}}else{if(this.eventType===1){var f=this.node.firstChild;
if(f){return this.enter(f)
}else{return this.leave(this.node)
}}else{if(this.eventType===2){var e=this.node.nextSibling;
if(e){return this.enter(e)
}else{return this.leave(this.node)
}}else{return this.leave(this.node)
}}}},enter:function(f){var e=f.nodeType;
this.node=f;
this.attributes=null;
if(e===1){this.eventType=1;
this.pushNS(f);
return this.eventType
}else{if(e===2){this.eventType=10;
return this.eventType
}else{if(e===3){var d=f.nodeValue;
if(Jsonix.Util.StringUtils.isEmpty(d)){this.eventType=6
}else{this.eventType=4
}return this.eventType
}else{if(e===4){this.eventType=12;
return this.eventType
}else{if(e===5){this.eventType=9;
return this.eventType
}else{if(e===6){this.eventType=15;
return this.eventType
}else{if(e===7){this.eventType=3;
return this.eventType
}else{if(e===8){this.eventType=5;
return this.eventType
}else{if(e===9){this.eventType=7;
return this.eventType
}else{if(e===10){this.eventType=12;
return this.eventType
}else{if(e===12){this.eventType=14;
return this.eventType
}else{throw new Error("Node type ["+e+"] is not supported.")
}}}}}}}}}}}},leave:function(h){if(h.nodeType===9){if(this.eventType==8){throw new Error("Invalid state.")
}else{this.node=h;
this.attributes=null;
this.eventType=8;
return this.eventType
}}else{if(h.nodeType===1){if(this.eventType==2){var g=h.nextSibling;
if(g){return this.enter(g)
}}else{this.node=h;
this.attributes=null;
this.eventType=2;
this.popNS();
return this.eventType
}}}var e=h.nextSibling;
if(e){return this.enter(e)
}else{var f=h.parentNode;
this.node=f;
this.attributes=null;
if(f.nodeType===9){this.eventType=8
}else{this.eventType=2
}return this.eventType
}},getName:function(){var b=this.node;
if(Jsonix.Util.Type.isString(b.nodeName)){if(Jsonix.Util.Type.isString(b.namespaceURI)){return new Jsonix.XML.QName(b.namespaceURI,b.nodeName)
}else{return new Jsonix.XML.QName(b.nodeName)
}}else{return null
}},getNameKey:function(){var b=this.node;
if(Jsonix.Util.Type.isString(b.nodeName)){return Jsonix.XML.QName.key(b.namespaceURI,b.nodeName)
}else{return null
}},getText:function(){return this.node.nodeValue
},nextTag:function(){var b=this.next();
while(b===7||b===4||b===12||b===6||b===3||b===5){b=this.next()
}if(b!==1&&b!==2){throw new Error("Expected start or end tag.")
}return b
},skipElement:function(){if(this.eventType!==Jsonix.XML.Input.START_ELEMENT){throw new Error("Parser must be on START_ELEMENT to skip element.")
}var d=1;
var c;
do{c=this.nextTag();
d+=(c===Jsonix.XML.Input.START_ELEMENT)?1:-1
}while(d>0);
return c
},getElementText:function(){if(this.eventType!=1){throw new Error("Parser must be on START_ELEMENT to read next text.")
}var c=this.next();
var d="";
while(c!==2){if(c===4||c===12||c===6||c===9){d=d+this.getText()
}else{if(c===3||c===5){}else{if(c===8){throw new Error("Unexpected end of document when reading element text content.")
}else{if(c===1){throw new Error("Element text content may not contain START_ELEMENT.")
}else{throw new Error("Unexpected event type ["+c+"].")
}}}}c=this.next()
}return d
},retrieveElement:function(){var b;
if(this.eventType===1){b=this.node
}else{if(this.eventType===10){b=this.node.parentNode
}else{throw new Error("Element can only be retrieved for START_ELEMENT or ATTRIBUTE nodes.")
}}return b
},retrieveAttributes:function(){var b;
if(this.attributes){b=this.attributes
}else{if(this.eventType===1){b=this.node.attributes;
this.attributes=b
}else{if(this.eventType===10){b=this.node.parentNode.attributes;
this.attributes=b
}else{throw new Error("Attributes can only be retrieved for START_ELEMENT or ATTRIBUTE nodes.")
}}}return b
},getAttributeCount:function(){var b=this.retrieveAttributes();
return b.length
},getAttributeName:function(d){var e=this.retrieveAttributes();
if(d<0||d>=e.length){throw new Error("Invalid attribute index ["+d+"].")
}var f=e[d];
if(Jsonix.Util.Type.isString(f.namespaceURI)){return new Jsonix.XML.QName(f.namespaceURI,f.nodeName)
}else{return new Jsonix.XML.QName(f.nodeName)
}},getAttributeNameKey:function(d){var e=this.retrieveAttributes();
if(d<0||d>=e.length){throw new Error("Invalid attribute index ["+d+"].")
}var f=e[d];
return Jsonix.XML.QName.key(f.namespaceURI,f.nodeName)
},getAttributeValue:function(d){var e=this.retrieveAttributes();
if(d<0||d>=e.length){throw new Error("Invalid attribute index ["+d+"].")
}var f=e[d];
return f.value
},getAttributeValueNS:null,getAttributeValueNSViaElement:function(d,e){var f=this.retrieveElement();
return f.getAttributeNS(d,e)
},getAttributeValueNSViaAttribute:function(d,e){var f=this.getAttributeNodeNS(d,e);
if(Jsonix.Util.Type.exists(f)){return f.nodeValue
}else{return null
}},getAttributeNodeNS:null,getAttributeNodeNSViaElement:function(d,e){var f=this.retrieveElement();
return f.getAttributeNodeNS(d,e)
},getAttributeNodeNSViaAttributes:function(o,p){var l=null;
var i=this.retrieveAttributes();
var m,k;
for(var n=0,j=i.length;
n<j;
++n){m=i[n];
if(m.namespaceURI===o){k=(m.prefix)?(m.prefix+":"+p):p;
if(k===m.nodeName){l=m;
break
}}}return l
},getElement:function(){if(this.eventType===1||this.eventType===2){this.eventType=2;
return this.node
}else{throw new Error("Parser must be on START_ELEMENT or END_ELEMENT to return current element.")
}},pushNS:function(x){var n=this.pns.length-1;
var t=this.pns[n];
var q=Jsonix.Util.Type.isObject(t)?n:t;
this.pns.push(q);
n++;
var w=true;
if(x.attributes){var u=x.attributes;
var p=u.length;
if(p>0){for(var v=0;
v<p;
v++){var y=u[v];
var s=y.nodeName;
var z=null;
var r=null;
var o=false;
if(s==="xmlns"){z="";
r=y.value;
o=true
}else{if(s.substring(0,6)==="xmlns:"){z=s.substring(6);
r=y.value;
o=true
}}if(o){if(w){q=Jsonix.Util.Type.cloneObject(this.pns[q],{});
this.pns[n]=q;
w=false
}q[z]=r
}}}}},popNS:function(){this.pns.pop()
},getNamespaceURI:function(d){var e=this.pns.length-1;
var f=this.pns[e];
f=Jsonix.Util.Type.isObject(f)?f:this.pns[f];
return f[d]
},CLASS_NAME:"Jsonix.XML.Input"});
Jsonix.XML.Input.prototype.getAttributeValueNS=(Jsonix.DOM.isDomImplementationAvailable())?Jsonix.XML.Input.prototype.getAttributeValueNSViaElement:Jsonix.XML.Input.prototype.getAttributeValueNSViaAttribute;
Jsonix.XML.Input.prototype.getAttributeNodeNS=(Jsonix.DOM.isDomImplementationAvailable())?Jsonix.XML.Input.prototype.getAttributeNodeNSViaElement:Jsonix.XML.Input.prototype.getAttributeNodeNSViaAttributes;
Jsonix.XML.Input.START_ELEMENT=1;
Jsonix.XML.Input.END_ELEMENT=2;
Jsonix.XML.Input.PROCESSING_INSTRUCTION=3;
Jsonix.XML.Input.CHARACTERS=4;
Jsonix.XML.Input.COMMENT=5;
Jsonix.XML.Input.SPACE=6;
Jsonix.XML.Input.START_DOCUMENT=7;
Jsonix.XML.Input.END_DOCUMENT=8;
Jsonix.XML.Input.ENTITY_REFERENCE=9;
Jsonix.XML.Input.ATTRIBUTE=10;
Jsonix.XML.Input.DTD=11;
Jsonix.XML.Input.CDATA=12;
Jsonix.XML.Input.NAMESPACE=13;
Jsonix.XML.Input.NOTATION_DECLARATION=14;
Jsonix.XML.Input.ENTITY_DECLARATION=15;
Jsonix.XML.Output=Jsonix.Class({document:null,documentElement:null,node:null,nodes:null,nsp:null,pns:null,namespacePrefixIndex:0,xmldom:null,initialize:function(e){if(typeof ActiveXObject!=="undefined"){this.xmldom=new ActiveXObject("Microsoft.XMLDOM")
}else{this.xmldom=null
}this.nodes=[];
var f={"":""};
f[Jsonix.XML.XMLNS_NS]=Jsonix.XML.XMLNS_P;
if(Jsonix.Util.Type.isObject(e)){if(Jsonix.Util.Type.isObject(e.namespacePrefixes)){Jsonix.Util.Type.cloneObject(e.namespacePrefixes,f)
}}this.nsp=[f];
var d={"":""};
d[Jsonix.XML.XMLNS_P]=Jsonix.XML.XMLNS_NS;
this.pns=[d]
},destroy:function(){this.xmldom=null
},writeStartDocument:function(){var b=Jsonix.DOM.createDocument();
this.document=b;
return this.push(b)
},writeEndDocument:function(){return this.pop()
},writeStartElement:function(p){Jsonix.Util.Ensure.ensureObject(p);
var j=p.localPart||p.lp||null;
Jsonix.Util.Ensure.ensureString(j);
var n=p.namespaceURI||p.ns||null;
var i=Jsonix.Util.Type.isString(n)?n:"";
var k=p.prefix||p.p;
var m=this.getPrefix(i,k);
var l=(!m?j:m+":"+j);
var o;
if(Jsonix.Util.Type.isFunction(this.document.createElementNS)){o=this.document.createElementNS(i,l)
}else{if(this.xmldom){o=this.xmldom.createNode(1,l,i)
}else{throw new Error("Could not create an element node.")
}}this.peek().appendChild(o);
this.push(o);
this.declareNamespace(i,m);
if(this.documentElement===null){this.documentElement=o;
this.declareNamespaces()
}return o
},writeEndElement:function(){return this.pop()
},writeCharacters:function(c){var d;
if(Jsonix.Util.Type.isFunction(this.document.createTextNode)){d=this.document.createTextNode(c)
}else{if(this.xmldom){d=this.xmldom.createTextNode(c)
}else{throw new Error("Could not create a text node.")
}}this.peek().appendChild(d);
return d
},writeCdata:function(c){var d;
if(Jsonix.Util.Type.isFunction(this.document.createCDATASection)){d=this.document.createCDATASection(c)
}else{if(this.xmldom){d=this.xmldom.createCDATASection(c)
}else{throw new Error("Could not create a CDATA section node.")
}}this.peek().appendChild(d);
return d
},writeAttribute:function(t,k){Jsonix.Util.Ensure.ensureString(k);
Jsonix.Util.Ensure.ensureObject(t);
var n=t.localPart||t.lp||null;
Jsonix.Util.Ensure.ensureString(n);
var l=t.namespaceURI||t.ns||null;
var q=Jsonix.Util.Type.isString(l)?l:"";
var s=t.prefix||t.p||null;
var m=this.getPrefix(q,s);
var o=(!m?n:m+":"+n);
var p=this.peek();
if(q===""){p.setAttribute(o,k)
}else{if(p.setAttributeNS){p.setAttributeNS(q,o,k)
}else{if(this.xmldom){var r=this.document.createNode(2,o,q);
r.nodeValue=k;
p.setAttributeNode(r)
}else{if(q===Jsonix.XML.XMLNS_NS){p.setAttribute(o,k)
}else{throw new Error("The [setAttributeNS] method is not implemented")
}}}this.declareNamespace(q,m)
}},writeNode:function(c){var d;
if(Jsonix.Util.Type.exists(this.document.importNode)){d=this.document.importNode(c,true)
}else{d=c
}this.peek().appendChild(d);
return d
},push:function(b){this.nodes.push(b);
this.pushNS();
return b
},peek:function(){return this.nodes[this.nodes.length-1]
},pop:function(){this.popNS();
var b=this.nodes.pop();
return b
},pushNS:function(){var k=this.nsp.length-1;
var g=this.pns.length-1;
var h=this.nsp[k];
var j=this.pns[g];
var l=Jsonix.Util.Type.isObject(h)?k:h;
var i=Jsonix.Util.Type.isObject(j)?g:j;
this.nsp.push(l);
this.pns.push(i)
},popNS:function(){this.nsp.pop();
this.pns.pop()
},declareNamespaces:function(){var f=this.nsp.length-1;
var h=this.nsp[f];
h=Jsonix.Util.Type.isNumber(h)?this.nsp[h]:h;
var e,g;
for(e in h){if(h.hasOwnProperty(e)){g=h[e];
this.declareNamespace(e,g)
}}},declareNamespace:function(j,i){var f=this.pns.length-1;
var h=this.pns[f];
var g;
if(Jsonix.Util.Type.isNumber(h)){g=true;
h=this.pns[h]
}else{g=false
}if(h[i]!==j){if(i===""){this.writeAttribute({lp:Jsonix.XML.XMLNS_P},j)
}else{this.writeAttribute({ns:Jsonix.XML.XMLNS_NS,lp:i,p:Jsonix.XML.XMLNS_P},j)
}if(g){h=Jsonix.Util.Type.cloneObject(h,{});
this.pns[f]=h
}h[i]=j
}},getPrefix:function(j,i){var g=this.nsp.length-1;
var k=this.nsp[g];
var h;
if(Jsonix.Util.Type.isNumber(k)){h=true;
k=this.nsp[k]
}else{h=false
}if(Jsonix.Util.Type.isString(i)){var l=k[j];
if(i===l){}else{if(h){k=Jsonix.Util.Type.cloneObject(k,{});
this.nsp[g]=k
}k[j]=i
}}else{i=k[j];
if(!Jsonix.Util.Type.exists(i)){i="p"+(this.namespacePrefixIndex++);
if(h){k=Jsonix.Util.Type.cloneObject(k,{});
this.nsp[g]=k
}k[j]=i
}}return i
},getNamespaceURI:function(d){var e=this.pns.length-1;
var f=this.pns[e];
f=Jsonix.Util.Type.isObject(f)?f:this.pns[f];
return f[d]
},CLASS_NAME:"Jsonix.XML.Output"});
Jsonix.Mapping={};
Jsonix.Mapping.Style=Jsonix.Class({marshaller:null,unmarshaller:null,module:null,elementInfo:null,classInfo:null,enumLeafInfo:null,anyAttributePropertyInfo:null,anyElementPropertyInfo:null,attributePropertyInfo:null,elementMapPropertyInfo:null,elementPropertyInfo:null,elementsPropertyInfo:null,elementRefPropertyInfo:null,elementRefsPropertyInfo:null,valuePropertyInfo:null,initialize:function(){},CLASS_NAME:"Jsonix.Mapping.Style"});
Jsonix.Mapping.Style.STYLES={};
Jsonix.Mapping.Styled=Jsonix.Class({mappingStyle:null,initialize:function(d){if(Jsonix.Util.Type.exists(d)){Jsonix.Util.Ensure.ensureObject(d);
if(Jsonix.Util.Type.isString(d.mappingStyle)){var c=Jsonix.Mapping.Style.STYLES[d.mappingStyle];
if(!c){throw new Error("Mapping style ["+d.mappingStyle+"] is not known.")
}this.mappingStyle=c
}else{if(Jsonix.Util.Type.isObject(d.mappingStyle)){this.mappingStyle=d.mappingStyle
}}}if(!this.mappingStyle){this.mappingStyle=Jsonix.Mapping.Style.STYLES.standard
}},CLASS_NAME:"Jsonix.Mapping.Styled"});
Jsonix.Binding={};
Jsonix.Binding.Marshalls={};
Jsonix.Binding.Marshalls.Element=Jsonix.Class({marshalElement:function(o,u,t,l){var q=this.convertToTypedNamedValue(o,u,t,l);
var p=q.typeInfo;
var v=undefined;
if(u.supportXsiType&&Jsonix.Util.Type.exists(q.value)){var r=u.getTypeInfoByValue(q.value);
if(r&&r.typeName){v=r
}}var m=v||p;
if(m){t.writeStartElement(q.name);
if(v&&p!==v){var n=v.typeName;
var s=Jsonix.Schema.XSD.QName.INSTANCE.print(n,u,t,l);
t.writeAttribute(Jsonix.Schema.XSI.TYPE_QNAME,s)
}if(Jsonix.Util.Type.exists(q.value)){m.marshal(q.value,u,t,l)
}t.writeEndElement()
}else{throw new Error("Element ["+q.name.key+"] is not known in this context, could not determine its type.")
}},getTypeInfoByElementName:function(f,e,g){var h=e.getElementInfo(f,g);
if(Jsonix.Util.Type.exists(h)){return h.typeInfo
}else{return undefined
}}});
Jsonix.Binding.Marshalls.Element.AsElementRef=Jsonix.Class({convertToTypedNamedValue:function(i,f,g,j){Jsonix.Util.Ensure.ensureObject(i);
var h=this.convertToNamedValue(i,f,g,j);
return{name:h.name,value:h.value,typeInfo:this.getTypeInfoByElementName(h.name,f,j)}
},convertToNamedValue:function(j,m,h,l){var n;
var k;
if(Jsonix.Util.Type.exists(j.name)&&!Jsonix.Util.Type.isUndefined(j.value)){n=Jsonix.XML.QName.fromObjectOrString(j.name,m);
k=Jsonix.Util.Type.exists(j.value)?j.value:null;
return{name:n,value:k}
}else{for(var i in j){if(j.hasOwnProperty(i)){n=Jsonix.XML.QName.fromObjectOrString(i,m);
k=j[i];
return{name:n,value:k}
}}}throw new Error("Invalid element value ["+j+"]. Element values must either have {name:'myElementName', value: elementValue} or {myElementName:elementValue} structure.")
}});
Jsonix.Binding.Unmarshalls={};
Jsonix.Binding.Unmarshalls.WrapperElement=Jsonix.Class({mixed:false,unmarshalWrapperElement:function(f,g,j,h){var i=g.next();
while(i!==Jsonix.XML.Input.END_ELEMENT){if(i===Jsonix.XML.Input.START_ELEMENT){this.unmarshalElement(f,g,j,h)
}else{if(this.mixed&&(i===Jsonix.XML.Input.CHARACTERS||i===Jsonix.XML.Input.CDATA||i===Jsonix.XML.Input.ENTITY_REFERENCE)){h(g.getText())
}else{if(i===Jsonix.XML.Input.SPACE||i===Jsonix.XML.Input.COMMENT||i===Jsonix.XML.Input.PROCESSING_INSTRUCTION){}else{throw new Error("Illegal state: unexpected event type ["+i+"].")
}}}i=g.next()
}}});
Jsonix.Binding.Unmarshalls.Element=Jsonix.Class({allowTypedObject:true,allowDom:false,unmarshalElement:function(q,o,j,l){if(o.eventType!=1){throw new Error("Parser must be on START_ELEMENT to read next element.")
}var k=this.getTypeInfoByInputElement(q,o,j);
var r=o.getName();
var p;
if(this.allowTypedObject){if(Jsonix.Util.Type.exists(k)){var n=k.unmarshal(q,o,j);
var m={name:r,value:n,typeInfo:k};
p=this.convertFromTypedNamedValue(m,q,o,j)
}else{if(this.allowDom){p=o.getElement()
}else{throw new Error("Element ["+r.toString()+"] could not be unmarshalled as is not known in this context and the property does not allow DOM content.")
}}}else{if(this.allowDom){p=o.getElement()
}else{throw new Error("Element ["+r.toString()+"] could not be unmarshalled as the property neither allows typed objects nor DOM as content. This is a sign of invalid mappings, do not use [allowTypedObject : false] and [allowDom : false] at the same time.")
}}l(p)
},getTypeInfoByInputElement:function(n,p,m){var i=null;
if(n.supportXsiType){var j=p.getAttributeValueNS(Jsonix.Schema.XSI.NAMESPACE_URI,Jsonix.Schema.XSI.TYPE);
if(Jsonix.Util.StringUtils.isNotBlank(j)){var k=Jsonix.Schema.XSD.QName.INSTANCE.parse(j,n,p,m);
i=n.getTypeInfoByTypeNameKey(k.key)
}}var o=p.getName();
var l=i?i:this.getTypeInfoByElementName(o,n,m);
return l
},getTypeInfoByElementName:function(f,e,g){var h=e.getElementInfo(f,g);
if(Jsonix.Util.Type.exists(h)){return h.typeInfo
}else{return undefined
}}});
Jsonix.Binding.Unmarshalls.Element.AsElementRef=Jsonix.Class({convertFromTypedNamedValue:function(g,e,f,h){return{name:g.name,value:g.value}
}});
Jsonix.Binding.Unmarshalls.Element.AsSimplifiedElementRef=Jsonix.Class({convertFromTypedNamedValue:function(i,l,g,k){var h=i.name.toCanonicalString(l);
var j={};
j[h]=i.value;
return j
}});
Jsonix.Binding.Marshaller=Jsonix.Class(Jsonix.Binding.Marshalls.Element,Jsonix.Binding.Marshalls.Element.AsElementRef,{context:null,initialize:function(b){Jsonix.Util.Ensure.ensureObject(b);
this.context=b
},marshalString:function(e){var d=this.marshalDocument(e);
var f=Jsonix.DOM.serialize(d);
return f
},marshalDocument:function(d){var e=new Jsonix.XML.Output({namespacePrefixes:this.context.namespacePrefixes});
var f=e.writeStartDocument();
this.marshalElement(d,this.context,e,undefined);
e.writeEndDocument();
return f
},CLASS_NAME:"Jsonix.Binding.Marshaller"});
Jsonix.Binding.Marshaller.Simplified=Jsonix.Class(Jsonix.Binding.Marshaller,{CLASS_NAME:"Jsonix.Binding.Marshaller.Simplified"});
Jsonix.Binding.Unmarshaller=Jsonix.Class(Jsonix.Binding.Unmarshalls.Element,Jsonix.Binding.Unmarshalls.Element.AsElementRef,{context:null,allowTypedObject:true,allowDom:false,initialize:function(b){Jsonix.Util.Ensure.ensureObject(b);
this.context=b
},unmarshalString:function(c){Jsonix.Util.Ensure.ensureString(c);
var d=Jsonix.DOM.parse(c);
return this.unmarshalDocument(d)
},unmarshalURL:function(d,f,e){Jsonix.Util.Ensure.ensureString(d);
Jsonix.Util.Ensure.ensureFunction(f);
if(Jsonix.Util.Type.exists(e)){Jsonix.Util.Ensure.ensureObject(e)
}that=this;
Jsonix.DOM.load(d,function(a){f(that.unmarshalDocument(a))
},e)
},unmarshalFile:function(g,h,e){if(typeof _jsonix_fs==="undefined"){throw new Error("File unmarshalling is only available in environments which support file systems.")
}Jsonix.Util.Ensure.ensureString(g);
Jsonix.Util.Ensure.ensureFunction(h);
if(Jsonix.Util.Type.exists(e)){Jsonix.Util.Ensure.ensureObject(e)
}that=this;
var f=_jsonix_fs;
f.readFile(g,e,function(d,c){if(d){throw d
}else{var a=c.toString();
var b=Jsonix.DOM.parse(a);
h(that.unmarshalDocument(b))
}})
},unmarshalDocument:function(i,j){var f=new Jsonix.XML.Input(i);
var g=null;
var h=function(a){g=a
};
f.nextTag();
this.unmarshalElement(this.context,f,j,h);
return g
},CLASS_NAME:"Jsonix.Binding.Unmarshaller"});
Jsonix.Binding.Unmarshaller.Simplified=Jsonix.Class(Jsonix.Binding.Unmarshaller,Jsonix.Binding.Unmarshalls.Element.AsSimplifiedElementRef,{CLASS_NAME:"Jsonix.Binding.Unmarshaller.Simplified"});
Jsonix.Model.TypeInfo=Jsonix.Class({module:null,name:null,baseTypeInfo:null,initialize:function(){},isBasedOn:function(c){var d=this;
while(d){if(c===d){return true
}d=d.baseTypeInfo
}return false
},CLASS_NAME:"Jsonix.Model.TypeInfo"});
Jsonix.Model.ClassInfo=Jsonix.Class(Jsonix.Model.TypeInfo,Jsonix.Mapping.Styled,{name:null,localName:null,typeName:null,instanceFactory:null,properties:null,propertiesMap:null,structure:null,targetNamespace:"",defaultElementNamespaceURI:"",defaultAttributeNamespaceURI:"",built:false,initialize:function(w,m){Jsonix.Model.TypeInfo.prototype.initialize.apply(this,[]);
Jsonix.Mapping.Styled.prototype.initialize.apply(this,[m]);
Jsonix.Util.Ensure.ensureObject(w);
var v=w.name||w.n||undefined;
Jsonix.Util.Ensure.ensureString(v);
this.name=v;
var q=w.localName||w.ln||null;
this.localName=q;
var r=w.defaultElementNamespaceURI||w.dens||w.targetNamespace||w.tns||"";
this.defaultElementNamespaceURI=r;
var n=w.targetNamespace||w.tns||w.defaultElementNamespaceURI||w.dens||this.defaultElementNamespaceURI;
this.targetNamespace=n;
var p=w.defaultAttributeNamespaceURI||w.dans||"";
this.defaultAttributeNamespaceURI=p;
var u=w.baseTypeInfo||w.bti||null;
this.baseTypeInfo=u;
var t=w.instanceFactory||w.inF||undefined;
if(Jsonix.Util.Type.exists(t)){Jsonix.Util.Ensure.ensureFunction(t);
this.instanceFactory=t
}var o=w.typeName||w.tn||undefined;
if(Jsonix.Util.Type.exists(o)){if(Jsonix.Util.Type.isString(o)){this.typeName=new Jsonix.XML.QName(this.targetNamespace,o)
}else{this.typeName=Jsonix.XML.QName.fromObject(o)
}}else{if(Jsonix.Util.Type.exists(q)){this.typeName=new Jsonix.XML.QName(n,q)
}}this.properties=[];
this.propertiesMap={};
var x=w.propertyInfos||w.ps||[];
Jsonix.Util.Ensure.ensureArray(x);
for(var s=0;
s<x.length;
s++){this.p(x[s])
}},getPropertyInfoByName:function(b){return this.propertiesMap[b]
},destroy:function(){},build:function(h){if(!this.built){this.baseTypeInfo=h.resolveTypeInfo(this.baseTypeInfo,this.module);
if(Jsonix.Util.Type.exists(this.baseTypeInfo)){this.baseTypeInfo.build(h)
}for(var e=0;
e<this.properties.length;
e++){var g=this.properties[e];
g.build(h,this.module)
}var f={elements:null,attributes:{},anyAttribute:null,value:null,any:null};
this.buildStructure(h,f);
this.structure=f
}},buildStructure:function(h,f){if(Jsonix.Util.Type.exists(this.baseTypeInfo)){this.baseTypeInfo.buildStructure(h,f)
}for(var e=0;
e<this.properties.length;
e++){var g=this.properties[e];
g.buildStructure(h,f)
}},unmarshal:function(B,w){this.build(B);
var p;
if(this.instanceFactory){p=new this.instanceFactory()
}else{p={TYPE_NAME:this.name}
}if(w.eventType!==1){throw new Error("Parser must be on START_ELEMENT to read a class info.")
}if(Jsonix.Util.Type.exists(this.structure.attributes)){var D=w.getAttributeCount();
if(D!==0){for(var z=0;
z<D;
z++){var u=w.getAttributeNameKey(z);
if(Jsonix.Util.Type.exists(this.structure.attributes[u])){var A=w.getAttributeValue(z);
if(Jsonix.Util.Type.isString(A)){var C=this.structure.attributes[u];
this.unmarshalPropertyValue(B,w,C,p,A)
}}}}}if(Jsonix.Util.Type.exists(this.structure.anyAttribute)){var y=this.structure.anyAttribute;
this.unmarshalProperty(B,w,y,p)
}if(Jsonix.Util.Type.exists(this.structure.elements)){var v=w.next();
while(v!==Jsonix.XML.Input.END_ELEMENT){if(v===Jsonix.XML.Input.START_ELEMENT){var q=w.getNameKey();
if(Jsonix.Util.Type.exists(this.structure.elements[q])){var r=this.structure.elements[q];
this.unmarshalProperty(B,w,r,p)
}else{if(Jsonix.Util.Type.exists(this.structure.any)){var s=this.structure.any;
this.unmarshalProperty(B,w,s,p)
}else{v=w.skipElement()
}}}else{if((v===Jsonix.XML.Input.CHARACTERS||v===Jsonix.XML.Input.CDATA||v===Jsonix.XML.Input.ENTITY_REFERENCE)){if(Jsonix.Util.Type.exists(this.structure.mixed)){var x=this.structure.mixed;
this.unmarshalProperty(B,w,x,p)
}}else{if(v===Jsonix.XML.Input.SPACE||v===Jsonix.XML.Input.COMMENT||v===Jsonix.XML.Input.PROCESSING_INSTRUCTION){}else{throw new Error("Illegal state: unexpected event type ["+v+"].")
}}}v=w.next()
}}else{if(Jsonix.Util.Type.exists(this.structure.value)){var t=this.structure.value;
this.unmarshalProperty(B,w,t,p)
}else{w.nextTag()
}}if(w.eventType!==2){throw new Error("Illegal state: must be END_ELEMENT.")
}return p
},unmarshalProperty:function(i,j,h,g){var f=h.unmarshal(i,j,this);
h.setProperty(g,f)
},unmarshalPropertyValue:function(k,l,i,h,j){var g=i.unmarshalValue(j,k,l,this);
i.setProperty(h,g)
},marshal:function(l,q,p,k){if(this.isMarshallable(l,q,k)){if(Jsonix.Util.Type.exists(this.baseTypeInfo)){this.baseTypeInfo.marshal(l,q,p)
}for(var n=0;
n<this.properties.length;
n++){var m=this.properties[n];
var o=l[m.name];
if(Jsonix.Util.Type.exists(o)){m.marshal(o,q,p,this)
}}}else{if(this.structure.value){var j=this.structure.value;
j.marshal(l,q,p,this)
}else{if(this.properties.length===1){var r=this.properties[0];
r.marshal(l,q,p,this)
}else{throw new Error("The passed value ["+l+"] is not an object and there is no single suitable property to marshal it.")
}}}},isMarshallable:function(f,e,d){return this.isInstance(f,e,d)||(Jsonix.Util.Type.isObject(f)&&!Jsonix.Util.Type.isArray(f))
},isInstance:function(f,e,d){if(this.instanceFactory){return f instanceof this.instanceFactory
}else{return Jsonix.Util.Type.isObject(f)&&Jsonix.Util.Type.isString(f.TYPE_NAME)&&f.TYPE_NAME===this.name
}},b:function(b){Jsonix.Util.Ensure.ensureObject(b);
this.baseTypeInfo=b;
return this
},ps:function(){return this
},p:function(e){Jsonix.Util.Ensure.ensureObject(e);
if(e instanceof Jsonix.Model.PropertyInfo){this.addProperty(e)
}else{e=Jsonix.Util.Type.cloneObject(e);
var f=e.type||e.t||"element";
if(Jsonix.Util.Type.isFunction(this.propertyInfoCreators[f])){var d=this.propertyInfoCreators[f];
d.call(this,e)
}else{throw new Error("Unknown property info type ["+f+"].")
}}},aa:function(b){this.addDefaultNamespaces(b);
return this.addProperty(new this.mappingStyle.anyAttributePropertyInfo(b,{mappingStyle:this.mappingStyle}))
},ae:function(b){this.addDefaultNamespaces(b);
return this.addProperty(new this.mappingStyle.anyElementPropertyInfo(b,{mappingStyle:this.mappingStyle}))
},a:function(b){this.addDefaultNamespaces(b);
return this.addProperty(new this.mappingStyle.attributePropertyInfo(b,{mappingStyle:this.mappingStyle}))
},em:function(b){this.addDefaultNamespaces(b);
return this.addProperty(new this.mappingStyle.elementMapPropertyInfo(b,{mappingStyle:this.mappingStyle}))
},e:function(b){this.addDefaultNamespaces(b);
return this.addProperty(new this.mappingStyle.elementPropertyInfo(b,{mappingStyle:this.mappingStyle}))
},es:function(b){this.addDefaultNamespaces(b);
return this.addProperty(new this.mappingStyle.elementsPropertyInfo(b,{mappingStyle:this.mappingStyle}))
},er:function(b){this.addDefaultNamespaces(b);
return this.addProperty(new this.mappingStyle.elementRefPropertyInfo(b,{mappingStyle:this.mappingStyle}))
},ers:function(b){this.addDefaultNamespaces(b);
return this.addProperty(new this.mappingStyle.elementRefsPropertyInfo(b,{mappingStyle:this.mappingStyle}))
},v:function(b){this.addDefaultNamespaces(b);
return this.addProperty(new this.mappingStyle.valuePropertyInfo(b,{mappingStyle:this.mappingStyle}))
},addDefaultNamespaces:function(b){if(Jsonix.Util.Type.isObject(b)){if(!Jsonix.Util.Type.isString(b.defaultElementNamespaceURI)){b.defaultElementNamespaceURI=this.defaultElementNamespaceURI
}if(!Jsonix.Util.Type.isString(b.defaultAttributeNamespaceURI)){b.defaultAttributeNamespaceURI=this.defaultAttributeNamespaceURI
}}},addProperty:function(b){this.properties.push(b);
this.propertiesMap[b.name]=b;
return this
},CLASS_NAME:"Jsonix.Model.ClassInfo"});
Jsonix.Model.ClassInfo.prototype.propertyInfoCreators={aa:Jsonix.Model.ClassInfo.prototype.aa,anyAttribute:Jsonix.Model.ClassInfo.prototype.aa,ae:Jsonix.Model.ClassInfo.prototype.ae,anyElement:Jsonix.Model.ClassInfo.prototype.ae,a:Jsonix.Model.ClassInfo.prototype.a,attribute:Jsonix.Model.ClassInfo.prototype.a,em:Jsonix.Model.ClassInfo.prototype.em,elementMap:Jsonix.Model.ClassInfo.prototype.em,e:Jsonix.Model.ClassInfo.prototype.e,element:Jsonix.Model.ClassInfo.prototype.e,es:Jsonix.Model.ClassInfo.prototype.es,elements:Jsonix.Model.ClassInfo.prototype.es,er:Jsonix.Model.ClassInfo.prototype.er,elementRef:Jsonix.Model.ClassInfo.prototype.er,ers:Jsonix.Model.ClassInfo.prototype.ers,elementRefs:Jsonix.Model.ClassInfo.prototype.ers,v:Jsonix.Model.ClassInfo.prototype.v,value:Jsonix.Model.ClassInfo.prototype.v};
Jsonix.Model.EnumLeafInfo=Jsonix.Class(Jsonix.Model.TypeInfo,{name:null,baseTypeInfo:"String",entries:null,keys:null,values:null,built:false,initialize:function(f){Jsonix.Model.TypeInfo.prototype.initialize.apply(this,[]);
Jsonix.Util.Ensure.ensureObject(f);
var g=f.name||f.n||undefined;
Jsonix.Util.Ensure.ensureString(g);
this.name=g;
var e=f.baseTypeInfo||f.bti||"String";
this.baseTypeInfo=e;
var h=f.values||f.vs||undefined;
Jsonix.Util.Ensure.ensureExists(h);
if(!(Jsonix.Util.Type.isObject(h)||Jsonix.Util.Type.isArray(h))){throw new Error("Enum values must be either an array or an object.")
}else{this.entries=h
}},build:function(m){if(!this.built){this.baseTypeInfo=m.resolveTypeInfo(this.baseTypeInfo,this.module);
this.baseTypeInfo.build(m);
var p=this.entries;
var j={};
var l=[];
var i=[];
var o=0;
var n;
var k;
if(Jsonix.Util.Type.isArray(p)){for(o=0;
o<p.length;
o++){k=p[o];
if(Jsonix.Util.Type.isString(k)){n=k;
if(!(Jsonix.Util.Type.isFunction(this.baseTypeInfo.parse))){throw new Error("Enum value is provided as string but the base type ["+this.baseTypeInfo.name+"] of the enum info ["+this.name+"] does not implement the parse method.")
}k=this.baseTypeInfo.parse(k,m,null,this)
}else{if(this.baseTypeInfo.isInstance(k,m,this)){if(!(Jsonix.Util.Type.isFunction(this.baseTypeInfo.print))){throw new Error("The base type ["+this.baseTypeInfo.name+"] of the enum info ["+this.name+"] does not implement the print method, unable to produce the enum key as string.")
}n=this.baseTypeInfo.print(k,m,null,this)
}else{throw new Error("Enum value ["+k+"] is not an instance of the enum base type ["+this.baseTypeInfo.name+"].")
}}j[n]=k;
l[o]=n;
i[o]=k
}}else{if(Jsonix.Util.Type.isObject(p)){for(n in p){if(p.hasOwnProperty(n)){k=p[n];
if(Jsonix.Util.Type.isString(k)){if(!(Jsonix.Util.Type.isFunction(this.baseTypeInfo.parse))){throw new Error("Enum value is provided as string but the base type ["+this.baseTypeInfo.name+"] of the enum info ["+this.name+"] does not implement the parse method.")
}k=this.baseTypeInfo.parse(k,m,null,this)
}else{if(!this.baseTypeInfo.isInstance(k,m,this)){throw new Error("Enum value ["+k+"] is not an instance of the enum base type ["+this.baseTypeInfo.name+"].")
}}j[n]=k;
l[o]=n;
i[o]=k;
o++
}}}else{throw new Error("Enum values must be either an array or an object.")
}}this.entries=j;
this.keys=l;
this.values=i;
this.built=true
}},unmarshal:function(e,f,h){var g=f.getElementText();
return this.parse(g,e,f,h)
},marshal:function(g,e,f,h){if(Jsonix.Util.Type.exists(g)){f.writeCharacters(this.reprint(g,e,f,h))
}},reprint:function(g,e,f,h){if(Jsonix.Util.Type.isString(g)&&!this.isInstance(g,e,h)){return this.print(this.parse(g,e,null,h),e,f,h)
}else{return this.print(g,e,f,h)
}},print:function(h,j,g,i){for(var f=0;
f<this.values.length;
f++){if(this.values[f]===h){return this.keys[f]
}}throw new Error("Value ["+h+"] is invalid for the enum type ["+this.name+"].")
},parse:function(g,e,f,h){Jsonix.Util.Ensure.ensureString(g);
if(this.entries.hasOwnProperty(g)){return this.entries[g]
}else{throw new Error("Value ["+g+"] is invalid for the enum type ["+this.name+"].")
}},isInstance:function(g,e,h){for(var f=0;
f<this.values.length;
f++){if(this.values[f]===g){return true
}}return false
},CLASS_NAME:"Jsonix.Model.EnumLeafInfo"});
Jsonix.Model.ElementInfo=Jsonix.Class({module:null,elementName:null,typeInfo:null,substitutionHead:null,scope:null,built:false,initialize:function(g){Jsonix.Util.Ensure.ensureObject(g);
var j=g.defaultElementNamespaceURI||g.dens||"";
this.defaultElementNamespaceURI=j;
var h=g.elementName||g.en||undefined;
if(Jsonix.Util.Type.isObject(h)){this.elementName=Jsonix.XML.QName.fromObject(h)
}else{Jsonix.Util.Ensure.ensureString(h);
this.elementName=new Jsonix.XML.QName(this.defaultElementNamespaceURI,h)
}var k=g.typeInfo||g.ti||"String";
this.typeInfo=k;
var l=g.substitutionHead||g.sh||null;
this.substitutionHead=l;
var i=g.scope||g.sc||null;
this.scope=i
},build:function(b){if(!this.built){this.typeInfo=b.resolveTypeInfo(this.typeInfo,this.module);
this.scope=b.resolveTypeInfo(this.scope,this.module);
this.built=true
}},CLASS_NAME:"Jsonix.Model.ElementInfo"});
Jsonix.Model.PropertyInfo=Jsonix.Class({name:null,collection:false,targetNamespace:"",defaultElementNamespaceURI:"",defaultAttributeNamespaceURI:"",built:false,initialize:function(r){Jsonix.Util.Ensure.ensureObject(r);
var p=r.name||r.n||undefined;
Jsonix.Util.Ensure.ensureString(p);
this.name=p;
var m=r.defaultElementNamespaceURI||r.dens||r.targetNamespace||r.tns||"";
this.defaultElementNamespaceURI=m;
var j=r.targetNamespace||r.tns||r.defaultElementNamespaceURI||r.dens||this.defaultElementNamespaceURI;
this.targetNamespace=j;
var k=r.defaultAttributeNamespaceURI||r.dans||"";
this.defaultAttributeNamespaceURI=k;
var o=r.collection||r.col||false;
this.collection=o;
var n=r.required||r.rq||false;
this.required=n;
if(this.collection){var l;
if(Jsonix.Util.Type.isNumber(r.minOccurs)){l=r.minOccurs
}else{if(Jsonix.Util.Type.isNumber(r.mno)){l=r.mno
}else{l=1
}}this.minOccurs=l;
var q;
if(Jsonix.Util.Type.isNumber(r.maxOccurs)){q=r.maxOccurs
}else{if(Jsonix.Util.Type.isNumber(r.mxo)){q=r.mxo
}else{q=null
}}this.maxOccurs=q
}},build:function(c,d){if(!this.built){this.doBuild(c,d);
this.built=true
}},doBuild:function(c,d){throw new Error("Abstract method [doBuild].")
},buildStructure:function(c,d){throw new Error("Abstract method [buildStructure].")
},setProperty:function(d,f){if(Jsonix.Util.Type.exists(f)){if(this.collection){Jsonix.Util.Ensure.ensureArray(f,"Collection property requires an array value.");
if(!Jsonix.Util.Type.exists(d[this.name])){d[this.name]=[]
}for(var e=0;
e<f.length;
e++){d[this.name].push(f[e])
}}else{d[this.name]=f
}}},CLASS_NAME:"Jsonix.Model.PropertyInfo"});
Jsonix.Model.AnyAttributePropertyInfo=Jsonix.Class(Jsonix.Model.PropertyInfo,{initialize:function(b){Jsonix.Util.Ensure.ensureObject(b);
Jsonix.Model.PropertyInfo.prototype.initialize.apply(this,[b])
},unmarshal:function(m,p,l){var n=p.getAttributeCount();
if(n===0){return null
}else{var j={};
for(var o=0;
o<n;
o++){var k=p.getAttributeValue(o);
if(Jsonix.Util.Type.isString(k)){var i=this.convertFromAttributeName(p.getAttributeName(o),m,p,l);
j[i]=k
}}return j
}},marshal:function(j,l,n,k){if(!Jsonix.Util.Type.isObject(j)){return
}for(var h in j){if(j.hasOwnProperty(h)){var i=j[h];
if(Jsonix.Util.Type.isString(i)){var m=this.convertToAttributeName(h,l,n,k);
n.writeAttribute(m,i)
}}}},convertFromAttributeName:function(e,h,f,g){return e.key
},convertToAttributeName:function(e,h,f,g){return Jsonix.XML.QName.fromObjectOrString(e,h)
},doBuild:function(c,d){},buildStructure:function(c,d){Jsonix.Util.Ensure.ensureObject(d);
d.anyAttribute=this
},CLASS_NAME:"Jsonix.Model.AnyAttributePropertyInfo"});
Jsonix.Model.AnyAttributePropertyInfo.Simplified=Jsonix.Class(Jsonix.Model.AnyAttributePropertyInfo,{convertFromAttributeName:function(e,h,f,g){return e.toCanonicalString(h)
}});
Jsonix.Model.SingleTypePropertyInfo=Jsonix.Class(Jsonix.Model.PropertyInfo,{typeInfo:"String",initialize:function(d){Jsonix.Util.Ensure.ensureObject(d);
Jsonix.Model.PropertyInfo.prototype.initialize.apply(this,[d]);
var c=d.typeInfo||d.ti||"String";
this.typeInfo=c
},doBuild:function(c,d){this.typeInfo=c.resolveTypeInfo(this.typeInfo,d)
},unmarshalValue:function(g,e,f,h){return this.parse(g,e,f,h)
},parse:function(g,e,f,h){return this.typeInfo.parse(g,e,f,h)
},print:function(g,e,f,h){return this.typeInfo.reprint(g,e,f,h)
},CLASS_NAME:"Jsonix.Model.SingleTypePropertyInfo"});
Jsonix.Model.AttributePropertyInfo=Jsonix.Class(Jsonix.Model.SingleTypePropertyInfo,{attributeName:null,initialize:function(d){Jsonix.Util.Ensure.ensureObject(d);
Jsonix.Model.SingleTypePropertyInfo.prototype.initialize.apply(this,[d]);
var c=d.attributeName||d.an||undefined;
if(Jsonix.Util.Type.isObject(c)){this.attributeName=Jsonix.XML.QName.fromObject(c)
}else{if(Jsonix.Util.Type.isString(c)){this.attributeName=new Jsonix.XML.QName(this.defaultAttributeNamespaceURI,c)
}else{this.attributeName=new Jsonix.XML.QName(this.defaultAttributeNamespaceURI,this.name)
}}},unmarshal:function(m,p,k){var n=p.getAttributeCount();
var j=null;
for(var o=0;
o<n;
o++){var i=p.getAttributeNameKey(o);
if(this.attributeName.key===i){var l=p.getAttributeValue(o);
if(Jsonix.Util.Type.isString(l)){j=this.unmarshalValue(l,m,p,k)
}}}return j
},marshal:function(g,e,f,h){if(Jsonix.Util.Type.exists(g)){f.writeAttribute(this.attributeName,this.print(g,e,f,h))
}},buildStructure:function(f,e){Jsonix.Util.Ensure.ensureObject(e);
Jsonix.Util.Ensure.ensureObject(e.attributes);
var d=this.attributeName.key;
e.attributes[d]=this
},CLASS_NAME:"Jsonix.Model.AttributePropertyInfo"});
Jsonix.Model.ValuePropertyInfo=Jsonix.Class(Jsonix.Model.SingleTypePropertyInfo,{initialize:function(d){Jsonix.Util.Ensure.ensureObject(d);
Jsonix.Model.SingleTypePropertyInfo.prototype.initialize.apply(this,[d]);
var c=d.asCDATA||d.c||false;
this.asCDATA=c
},unmarshal:function(e,f,h){var g=f.getElementText();
return this.unmarshalValue(g,e,f,h)
},marshal:function(g,e,f,h){if(!Jsonix.Util.Type.exists(g)){return
}if(this.asCDATA){f.writeCdata(this.print(g,e,f,h))
}else{f.writeCharacters(this.print(g,e,f,h))
}},buildStructure:function(c,d){Jsonix.Util.Ensure.ensureObject(d);
if(Jsonix.Util.Type.exists(d.elements)){throw new Error("The structure already defines element mappings, it cannot define a value property.")
}else{d.value=this
}},CLASS_NAME:"Jsonix.Model.ValuePropertyInfo"});
Jsonix.Model.AbstractElementsPropertyInfo=Jsonix.Class(Jsonix.Binding.Unmarshalls.Element,Jsonix.Binding.Unmarshalls.WrapperElement,Jsonix.Model.PropertyInfo,{wrapperElementName:null,allowDom:false,allowTypedObject:true,mixed:false,initialize:function(c){Jsonix.Util.Ensure.ensureObject(c);
Jsonix.Model.PropertyInfo.prototype.initialize.apply(this,[c]);
var d=c.wrapperElementName||c.wen||undefined;
if(Jsonix.Util.Type.isObject(d)){this.wrapperElementName=Jsonix.XML.QName.fromObject(d)
}else{if(Jsonix.Util.Type.isString(d)){this.wrapperElementName=new Jsonix.XML.QName(this.defaultElementNamespaceURI,d)
}else{this.wrapperElementName=null
}}},unmarshal:function(l,g,k){var h=null;
var j=this;
var i=function(a){if(j.collection){if(h===null){h=[]
}h.push(a)
}else{if(h===null){h=a
}else{throw new Error("Value already set.")
}}};
if(Jsonix.Util.Type.exists(this.wrapperElementName)){this.unmarshalWrapperElement(l,g,k,i)
}else{this.unmarshalElement(l,g,k,i)
}return h
},marshal:function(i,l,h,k){if(!Jsonix.Util.Type.exists(i)){return
}if(Jsonix.Util.Type.exists(this.wrapperElementName)){h.writeStartElement(this.wrapperElementName)
}if(!this.collection){this.marshalElement(i,l,h,k)
}else{Jsonix.Util.Ensure.ensureArray(i);
for(var g=0;
g<i.length;
g++){var j=i[g];
this.marshalElement(j,l,h,k)
}}if(Jsonix.Util.Type.exists(this.wrapperElementName)){h.writeEndElement()
}},convertFromTypedNamedValue:function(g,e,f,h){return g.value
},buildStructure:function(c,d){Jsonix.Util.Ensure.ensureObject(d);
if(Jsonix.Util.Type.exists(d.value)){throw new Error("The structure already defines a value property.")
}else{if(!Jsonix.Util.Type.exists(d.elements)){d.elements={}
}}if(Jsonix.Util.Type.exists(this.wrapperElementName)){d.elements[this.wrapperElementName.key]=this
}else{this.buildStructureElements(c,d)
}},buildStructureElements:function(c,d){throw new Error("Abstract method [buildStructureElements].")
},CLASS_NAME:"Jsonix.Model.AbstractElementsPropertyInfo"});
Jsonix.Model.ElementPropertyInfo=Jsonix.Class(Jsonix.Model.AbstractElementsPropertyInfo,Jsonix.Binding.Marshalls.Element,{typeInfo:"String",elementName:null,initialize:function(d){Jsonix.Util.Ensure.ensureObject(d);
Jsonix.Model.AbstractElementsPropertyInfo.prototype.initialize.apply(this,[d]);
var f=d.typeInfo||d.ti||"String";
if(Jsonix.Util.Type.isObject(f)){this.typeInfo=f
}else{Jsonix.Util.Ensure.ensureString(f);
this.typeInfo=f
}var e=d.elementName||d.en||undefined;
if(Jsonix.Util.Type.isObject(e)){this.elementName=Jsonix.XML.QName.fromObject(e)
}else{if(Jsonix.Util.Type.isString(e)){this.elementName=new Jsonix.XML.QName(this.defaultElementNamespaceURI,e)
}else{this.elementName=new Jsonix.XML.QName(this.defaultElementNamespaceURI,this.name)
}}},getTypeInfoByElementName:function(e,d,f){return this.typeInfo
},convertToTypedNamedValue:function(g,e,f,h){return{name:this.elementName,value:g,typeInfo:this.typeInfo}
},doBuild:function(c,d){this.typeInfo=c.resolveTypeInfo(this.typeInfo,d)
},buildStructureElements:function(c,d){d.elements[this.elementName.key]=this
},CLASS_NAME:"Jsonix.Model.ElementPropertyInfo"});
Jsonix.Model.ElementsPropertyInfo=Jsonix.Class(Jsonix.Model.AbstractElementsPropertyInfo,Jsonix.Binding.Marshalls.Element,{elementTypeInfos:null,elementTypeInfosMap:null,initialize:function(d){Jsonix.Util.Ensure.ensureObject(d);
Jsonix.Model.AbstractElementsPropertyInfo.prototype.initialize.apply(this,[d]);
var f=d.elementTypeInfos||d.etis||[];
Jsonix.Util.Ensure.ensureArray(f);
this.elementTypeInfos=[];
for(var e=0;
e<f.length;
e++){this.elementTypeInfos[e]=Jsonix.Util.Type.cloneObject(f[e])
}},getTypeInfoByElementName:function(e,d,f){return this.elementTypeInfosMap[e.key]
},convertToTypedNamedValue:function(q,v,u,o){for(var s=0;
s<this.elementTypeInfos.length;
s++){var r=this.elementTypeInfos[s];
var p=r.typeInfo;
if(p.isInstance(q,v,o)){var n=r.elementName;
return{name:n,value:q,typeInfo:p}
}}if(v.supportXsiType){var w=v.getTypeInfoByValue(q);
if(w&&w.typeName){for(var t=0;
t<this.elementTypeInfos.length;
t++){var z=this.elementTypeInfos[t];
var x=z.typeInfo;
if(w.isBasedOn(x)){var y=z.elementName;
return{name:y,value:q,typeInfo:x}
}}}}throw new Error("Could not find an element with type info supporting the value ["+q+"].")
},doBuild:function(k,l){this.elementTypeInfosMap={};
var i,j;
for(var g=0;
g<this.elementTypeInfos.length;
g++){var h=this.elementTypeInfos[g];
Jsonix.Util.Ensure.ensureObject(h);
i=h.typeInfo||h.ti||"String";
h.typeInfo=k.resolveTypeInfo(i,l);
j=h.elementName||h.en||undefined;
h.elementName=Jsonix.XML.QName.fromObjectOrString(j,k,this.defaultElementNamespaceURI);
this.elementTypeInfosMap[h.elementName.key]=h.typeInfo
}},buildStructureElements:function(g,f){for(var h=0;
h<this.elementTypeInfos.length;
h++){var e=this.elementTypeInfos[h];
f.elements[e.elementName.key]=this
}},CLASS_NAME:"Jsonix.Model.ElementsPropertyInfo"});
Jsonix.Model.ElementMapPropertyInfo=Jsonix.Class(Jsonix.Model.AbstractElementsPropertyInfo,{elementName:null,key:null,value:null,entryTypeInfo:null,initialize:function(g){Jsonix.Util.Ensure.ensureObject(g);
Jsonix.Model.AbstractElementsPropertyInfo.prototype.initialize.apply(this,[g]);
var e=g.key||g.k||undefined;
Jsonix.Util.Ensure.ensureObject(e);
var f=g.value||g.v||undefined;
Jsonix.Util.Ensure.ensureObject(f);
var h=g.elementName||g.en||undefined;
if(Jsonix.Util.Type.isObject(h)){this.elementName=Jsonix.XML.QName.fromObject(h)
}else{if(Jsonix.Util.Type.isString(h)){this.elementName=new Jsonix.XML.QName(this.defaultElementNamespaceURI,h)
}else{this.elementName=new Jsonix.XML.QName(this.defaultElementNamespaceURI,this.name)
}}this.entryTypeInfo=new Jsonix.Model.ClassInfo({name:"Map<"+e.name+","+f.name+">",propertyInfos:[e,f]})
},unmarshal:function(l,g,k){var h=null;
var j=this;
var i=function(a){if(Jsonix.Util.Type.exists(a)){Jsonix.Util.Ensure.ensureObject(a,"Map property requires an object.");
if(!Jsonix.Util.Type.exists(h)){h={}
}for(var c in a){if(a.hasOwnProperty(c)){var b=a[c];
if(j.collection){if(!Jsonix.Util.Type.exists(h[c])){h[c]=[]
}h[c].push(b)
}else{if(!Jsonix.Util.Type.exists(h[c])){h[c]=b
}else{throw new Error("Value was already set.")
}}}}}};
if(Jsonix.Util.Type.exists(this.wrapperElementName)){this.unmarshalWrapperElement(l,g,k,i)
}else{this.unmarshalElement(l,g,k,i)
}return h
},getTypeInfoByInputElement:function(d,e,f){return this.entryTypeInfo
},convertFromTypedNamedValue:function(i,l,g,k){var j=i.value;
var h={};
if(Jsonix.Util.Type.isString(j[this.key.name])){h[j[this.key.name]]=j[this.value.name]
}return h
},marshal:function(g,e,f,h){if(!Jsonix.Util.Type.exists(g)){return
}if(Jsonix.Util.Type.exists(this.wrapperElementName)){f.writeStartElement(this.wrapperElementName)
}this.marshalElement(g,e,f,h);
if(Jsonix.Util.Type.exists(this.wrapperElementName)){f.writeEndElement()
}},marshalElement:function(l,r,q,j){if(!!l){for(var m in l){if(l.hasOwnProperty(m)){var p=l[m];
if(!this.collection){var o={};
o[this.key.name]=m;
o[this.value.name]=p;
q.writeStartElement(this.elementName);
this.entryTypeInfo.marshal(o,r,q,j);
q.writeEndElement()
}else{for(var n=0;
n<p.length;
n++){var k={};
k[this.key.name]=m;
k[this.value.name]=p[n];
q.writeStartElement(this.elementName);
this.entryTypeInfo.marshal(k,r,q,j);
q.writeEndElement()
}}}}}},doBuild:function(c,d){this.entryTypeInfo.build(c,d);
this.key=this.entryTypeInfo.properties[0];
this.value=this.entryTypeInfo.properties[1]
},buildStructureElements:function(c,d){d.elements[this.elementName.key]=this
},setProperty:function(l,j){if(Jsonix.Util.Type.exists(j)){Jsonix.Util.Ensure.ensureObject(j,"Map property requires an object.");
if(!Jsonix.Util.Type.exists(l[this.name])){l[this.name]={}
}var i=l[this.name];
for(var g in j){if(j.hasOwnProperty(g)){var k=j[g];
if(this.collection){if(!Jsonix.Util.Type.exists(i[g])){i[g]=[]
}for(var h=0;
h<k.length;
h++){i[g].push(k[h])
}}else{i[g]=k
}}}}},CLASS_NAME:"Jsonix.Model.ElementMapPropertyInfo"});
Jsonix.Model.AbstractElementRefsPropertyInfo=Jsonix.Class(Jsonix.Binding.Marshalls.Element,Jsonix.Binding.Marshalls.Element.AsElementRef,Jsonix.Binding.Unmarshalls.Element,Jsonix.Binding.Unmarshalls.WrapperElement,Jsonix.Binding.Unmarshalls.Element.AsElementRef,Jsonix.Model.PropertyInfo,{wrapperElementName:null,allowDom:true,allowTypedObject:true,mixed:true,initialize:function(f){Jsonix.Util.Ensure.ensureObject(f,"Mapping must be an object.");
Jsonix.Model.PropertyInfo.prototype.initialize.apply(this,[f]);
var g=f.wrapperElementName||f.wen||undefined;
if(Jsonix.Util.Type.isObject(g)){this.wrapperElementName=Jsonix.XML.QName.fromObject(g)
}else{if(Jsonix.Util.Type.isString(g)){this.wrapperElementName=new Jsonix.XML.QName(this.defaultElementNamespaceURI,g)
}else{this.wrapperElementName=null
}}var h=Jsonix.Util.Type.defaultValue(f.allowDom,f.dom,true);
var j=Jsonix.Util.Type.defaultValue(f.allowTypedObject,f.typed,true);
var i=Jsonix.Util.Type.defaultValue(f.mixed,f.mx,true);
this.allowDom=h;
this.allowTypedObject=j;
this.mixed=i
},unmarshal:function(n,h,m){var i=null;
var l=this;
var j=function(a){if(l.collection){if(i===null){i=[]
}i.push(a)
}else{if(i===null){i=a
}else{throw new Error("Value already set.")
}}};
var k=h.eventType;
if(k===Jsonix.XML.Input.START_ELEMENT){if(Jsonix.Util.Type.exists(this.wrapperElementName)){this.unmarshalWrapperElement(n,h,m,j)
}else{this.unmarshalElement(n,h,m,j)
}}else{if(this.mixed&&(k===Jsonix.XML.Input.CHARACTERS||k===Jsonix.XML.Input.CDATA||k===Jsonix.XML.Input.ENTITY_REFERENCE)){j(h.getText())
}else{if(k===Jsonix.XML.Input.SPACE||k===Jsonix.XML.Input.COMMENT||k===Jsonix.XML.Input.PROCESSING_INSTRUCTION){}else{throw new Error("Illegal state: unexpected event type ["+k+"].")
}}}return i
},marshal:function(i,l,h,k){if(Jsonix.Util.Type.exists(i)){if(Jsonix.Util.Type.exists(this.wrapperElementName)){h.writeStartElement(this.wrapperElementName)
}if(!this.collection){this.marshalItem(i,l,h,k)
}else{Jsonix.Util.Ensure.ensureArray(i,"Collection property requires an array value.");
for(var g=0;
g<i.length;
g++){var j=i[g];
this.marshalItem(j,l,h,k)
}}if(Jsonix.Util.Type.exists(this.wrapperElementName)){h.writeEndElement()
}}},marshalItem:function(g,e,f,h){if(Jsonix.Util.Type.isString(g)){if(!this.mixed){throw new Error("Property is not mixed, can't handle string values.")
}else{f.writeCharacters(g)
}}else{if(this.allowDom&&Jsonix.Util.Type.exists(g.nodeType)){f.writeNode(g)
}else{if(Jsonix.Util.Type.isObject(g)){this.marshalElement(g,e,f,h)
}else{if(this.mixed){throw new Error("Unsupported content type, either objects or strings are supported.")
}else{throw new Error("Unsupported content type, only objects are supported.")
}}}}},getTypeInfoByElementName:function(g,f,i){var j=this.getPropertyElementTypeInfo(g,f);
if(Jsonix.Util.Type.exists(j)){return j.typeInfo
}else{var h=f.getElementInfo(g,i);
if(Jsonix.Util.Type.exists(h)){return h.typeInfo
}else{return undefined
}}},getPropertyElementTypeInfo:function(d,c){throw new Error("Abstract method [getPropertyElementTypeInfo].")
},buildStructure:function(c,d){Jsonix.Util.Ensure.ensureObject(d);
if(Jsonix.Util.Type.exists(d.value)){throw new Error("The structure already defines a value property.")
}else{if(!Jsonix.Util.Type.exists(d.elements)){d.elements={}
}}if(Jsonix.Util.Type.exists(this.wrapperElementName)){d.elements[this.wrapperElementName.key]=this
}else{this.buildStructureElements(c,d)
}if((this.allowDom||this.allowTypedObject)){d.any=this
}if(this.mixed&&!Jsonix.Util.Type.exists(this.wrapperElementName)){d.mixed=this
}},buildStructureElements:function(c,d){throw new Error("Abstract method [buildStructureElements].")
},buildStructureElementTypeInfos:function(k,h,g){h.elements[g.elementName.key]=this;
var l=k.getSubstitutionMembers(g.elementName);
if(Jsonix.Util.Type.isArray(l)){for(var i=0;
i<l.length;
i++){var j=l[i];
this.buildStructureElementTypeInfos(k,h,j)
}}},CLASS_NAME:"Jsonix.Model.AbstractElementRefsPropertyInfo"});
Jsonix.Model.ElementRefPropertyInfo=Jsonix.Class(Jsonix.Model.AbstractElementRefsPropertyInfo,{typeInfo:"String",elementName:null,initialize:function(d){Jsonix.Util.Ensure.ensureObject(d);
Jsonix.Model.AbstractElementRefsPropertyInfo.prototype.initialize.apply(this,[d]);
var f=d.typeInfo||d.ti||"String";
if(Jsonix.Util.Type.isObject(f)){this.typeInfo=f
}else{Jsonix.Util.Ensure.ensureString(f);
this.typeInfo=f
}var e=d.elementName||d.en||undefined;
if(Jsonix.Util.Type.isObject(e)){this.elementName=Jsonix.XML.QName.fromObject(e)
}else{if(Jsonix.Util.Type.isString(e)){this.elementName=new Jsonix.XML.QName(this.defaultElementNamespaceURI,e)
}else{this.elementName=new Jsonix.XML.QName(this.defaultElementNamespaceURI,this.name)
}}},getPropertyElementTypeInfo:function(e,f){var d=Jsonix.XML.QName.fromObjectOrString(e,f);
if(d.key===this.elementName.key){return this
}else{return null
}},doBuild:function(c,d){this.typeInfo=c.resolveTypeInfo(this.typeInfo,d)
},buildStructureElements:function(c,d){this.buildStructureElementTypeInfos(c,d,this)
},CLASS_NAME:"Jsonix.Model.ElementRefPropertyInfo"});
Jsonix.Model.ElementRefPropertyInfo.Simplified=Jsonix.Class(Jsonix.Model.ElementRefPropertyInfo,Jsonix.Binding.Unmarshalls.Element.AsSimplifiedElementRef,{CLASS_NAME:"Jsonix.Model.ElementRefPropertyInfo.Simplified"});
Jsonix.Model.ElementRefsPropertyInfo=Jsonix.Class(Jsonix.Model.AbstractElementRefsPropertyInfo,{elementTypeInfos:null,elementTypeInfosMap:null,initialize:function(d){Jsonix.Util.Ensure.ensureObject(d);
Jsonix.Model.AbstractElementRefsPropertyInfo.prototype.initialize.apply(this,[d]);
var f=d.elementTypeInfos||d.etis||[];
Jsonix.Util.Ensure.ensureArray(f);
this.elementTypeInfos=[];
for(var e=0;
e<f.length;
e++){this.elementTypeInfos[e]=Jsonix.Util.Type.cloneObject(f[e])
}},getPropertyElementTypeInfo:function(f,h){var e=Jsonix.XML.QName.fromObjectOrString(f,h);
var g=this.elementTypeInfosMap[e.key];
if(Jsonix.Util.Type.exists(g)){return{elementName:e,typeInfo:g}
}else{return null
}},doBuild:function(k,l){this.elementTypeInfosMap={};
var i,j;
for(var g=0;
g<this.elementTypeInfos.length;
g++){var h=this.elementTypeInfos[g];
Jsonix.Util.Ensure.ensureObject(h);
i=h.typeInfo||h.ti||"String";
h.typeInfo=k.resolveTypeInfo(i,l);
j=h.elementName||h.en||undefined;
h.elementName=Jsonix.XML.QName.fromObjectOrString(j,k,this.defaultElementNamespaceURI);
this.elementTypeInfosMap[h.elementName.key]=h.typeInfo
}},buildStructureElements:function(g,f){for(var h=0;
h<this.elementTypeInfos.length;
h++){var e=this.elementTypeInfos[h];
this.buildStructureElementTypeInfos(g,f,e)
}},CLASS_NAME:"Jsonix.Model.ElementRefsPropertyInfo"});
Jsonix.Model.ElementRefsPropertyInfo.Simplified=Jsonix.Class(Jsonix.Model.ElementRefsPropertyInfo,Jsonix.Binding.Unmarshalls.Element.AsSimplifiedElementRef,{CLASS_NAME:"Jsonix.Model.ElementRefsPropertyInfo.Simplified"});
Jsonix.Model.AnyElementPropertyInfo=Jsonix.Class(Jsonix.Binding.Marshalls.Element,Jsonix.Binding.Marshalls.Element.AsElementRef,Jsonix.Binding.Unmarshalls.Element,Jsonix.Binding.Unmarshalls.Element.AsElementRef,Jsonix.Model.PropertyInfo,{allowDom:true,allowTypedObject:true,mixed:true,initialize:function(f){Jsonix.Util.Ensure.ensureObject(f);
Jsonix.Model.PropertyInfo.prototype.initialize.apply(this,[f]);
var g=Jsonix.Util.Type.defaultValue(f.allowDom,f.dom,true);
var e=Jsonix.Util.Type.defaultValue(f.allowTypedObject,f.typed,true);
var h=Jsonix.Util.Type.defaultValue(f.mixed,f.mx,true);
this.allowDom=g;
this.allowTypedObject=e;
this.mixed=h
},unmarshal:function(n,h,m){var i=null;
var l=this;
var j=function(a){if(l.collection){if(i===null){i=[]
}i.push(a)
}else{if(i===null){i=a
}else{throw new Error("Value already set.")
}}};
var k=h.eventType;
if(k===Jsonix.XML.Input.START_ELEMENT){this.unmarshalElement(n,h,m,j)
}else{if(this.mixed&&(k===Jsonix.XML.Input.CHARACTERS||k===Jsonix.XML.Input.CDATA||k===Jsonix.XML.Input.ENTITY_REFERENCE)){j(h.getText())
}else{if(this.mixed&&(k===Jsonix.XML.Input.SPACE)){}else{if(k===Jsonix.XML.Input.COMMENT||k===Jsonix.XML.Input.PROCESSING_INSTRUCTION){}else{throw new Error("Illegal state: unexpected event type ["+k+"].")
}}}}return i
},marshal:function(h,j,g,i){if(!Jsonix.Util.Type.exists(h)){return
}if(!this.collection){this.marshalItem(h,j,g,i)
}else{Jsonix.Util.Ensure.ensureArray(h);
for(var f=0;
f<h.length;
f++){this.marshalItem(h[f],j,g,i)
}}},marshalItem:function(g,e,f,h){if(this.mixed&&Jsonix.Util.Type.isString(g)){f.writeCharacters(g)
}else{if(this.allowDom&&Jsonix.Util.Type.exists(g.nodeType)){f.writeNode(g)
}else{if(this.allowTypedObject){this.marshalElement(g,e,f,h)
}}}},doBuild:function(c,d){},buildStructure:function(c,d){Jsonix.Util.Ensure.ensureObject(d);
if(Jsonix.Util.Type.exists(d.value)){throw new Error("The structure already defines a value property.")
}else{if(!Jsonix.Util.Type.exists(d.elements)){d.elements={}
}}if((this.allowDom||this.allowTypedObject)){d.any=this
}if(this.mixed){d.mixed=this
}},CLASS_NAME:"Jsonix.Model.AnyElementPropertyInfo"});
Jsonix.Model.AnyElementPropertyInfo.Simplified=Jsonix.Class(Jsonix.Model.AnyElementPropertyInfo,Jsonix.Binding.Unmarshalls.Element.AsSimplifiedElementRef,{CLASS_NAME:"Jsonix.Model.AnyElementPropertyInfo.Simplified"});
Jsonix.Model.Module=Jsonix.Class(Jsonix.Mapping.Styled,{name:null,typeInfos:null,elementInfos:null,targetNamespace:"",defaultElementNamespaceURI:"",defaultAttributeNamespaceURI:"",initialize:function(r,j){Jsonix.Mapping.Styled.prototype.initialize.apply(this,[j]);
this.typeInfos=[];
this.elementInfos=[];
if(typeof r!=="undefined"){Jsonix.Util.Ensure.ensureObject(r);
var q=r.name||r.n||null;
this.name=q;
var n=r.defaultElementNamespaceURI||r.dens||r.targetNamespace||r.tns||"";
this.defaultElementNamespaceURI=n;
var k=r.targetNamespace||r.tns||r.defaultElementNamespaceURI||r.dens||this.defaultElementNamespaceURI;
this.targetNamespace=k;
var l=r.defaultAttributeNamespaceURI||r.dans||"";
this.defaultAttributeNamespaceURI=l;
var m=r.typeInfos||r.tis||[];
this.initializeTypeInfos(m);
for(var p in r){if(r.hasOwnProperty(p)){if(r[p] instanceof this.mappingStyle.classInfo){this.typeInfos.push(r[p])
}}}var o=r.elementInfos||r.eis||[];
this.initializeElementInfos(o)
}},initializeTypeInfos:function(f){Jsonix.Util.Ensure.ensureArray(f);
var h,e,g;
for(h=0;
h<f.length;
h++){e=f[h];
g=this.createTypeInfo(e);
this.typeInfos.push(g)
}},initializeElementInfos:function(h){Jsonix.Util.Ensure.ensureArray(h);
var e,f,g;
for(e=0;
e<h.length;
e++){f=h[e];
g=this.createElementInfo(f);
this.elementInfos.push(g)
}},createTypeInfo:function(f){Jsonix.Util.Ensure.ensureObject(f);
var g;
if(f instanceof Jsonix.Model.TypeInfo){g=f
}else{f=Jsonix.Util.Type.cloneObject(f);
var e=f.type||f.t||"classInfo";
if(Jsonix.Util.Type.isFunction(this.typeInfoCreators[e])){var h=this.typeInfoCreators[e];
g=h.call(this,f)
}else{throw new Error("Unknown type info type ["+e+"].")
}}return g
},initializeNames:function(e){var d=e.localName||e.ln||null;
e.localName=d;
var f=e.name||e.n||null;
e.name=f;
if(Jsonix.Util.Type.isString(e.name)){if(e.name.length>0&&e.name.charAt(0)==="."&&Jsonix.Util.Type.isString(this.name)){e.name=this.name+e.name
}}else{if(Jsonix.Util.Type.isString(d)){if(Jsonix.Util.Type.isString(this.name)){e.name=this.name+"."+d
}else{e.name=d
}}else{throw new Error("Neither [name/n] nor [localName/ln] was provided for the class info.")
}}},createClassInfo:function(f){Jsonix.Util.Ensure.ensureObject(f);
var j=f.defaultElementNamespaceURI||f.dens||this.defaultElementNamespaceURI;
f.defaultElementNamespaceURI=j;
var g=f.targetNamespace||f.tns||this.targetNamespace;
f.targetNamespace=g;
var h=f.defaultAttributeNamespaceURI||f.dans||this.defaultAttributeNamespaceURI;
f.defaultAttributeNamespaceURI=h;
this.initializeNames(f);
var i=new this.mappingStyle.classInfo(f,{mappingStyle:this.mappingStyle});
i.module=this;
return i
},createEnumLeafInfo:function(d){Jsonix.Util.Ensure.ensureObject(d);
this.initializeNames(d);
var c=new this.mappingStyle.enumLeafInfo(d,{mappingStyle:this.mappingStyle});
c.module=this;
return c
},createList:function(f){Jsonix.Util.Ensure.ensureObject(f);
var h=f.baseTypeInfo||f.typeInfo||f.bti||f.ti||"String";
var g=f.typeName||f.tn||null;
if(Jsonix.Util.Type.exists(g)){if(Jsonix.Util.Type.isString(g)){g=new Jsonix.XML.QName(this.targetNamespace,g)
}else{g=Jsonix.XML.QName.fromObject(g)
}}var i=f.separator||f.sep||" ";
Jsonix.Util.Ensure.ensureExists(h);
var j=new Jsonix.Schema.XSD.List(h,g,i);
j.module=this;
return j
},createElementInfo:function(g){Jsonix.Util.Ensure.ensureObject(g);
g=Jsonix.Util.Type.cloneObject(g);
var i=g.defaultElementNamespaceURI||g.dens||this.defaultElementNamespaceURI;
g.defaultElementNamespaceURI=i;
var h=g.elementName||g.en||undefined;
Jsonix.Util.Ensure.ensureExists(h);
var j=g.typeInfo||g.ti||"String";
Jsonix.Util.Ensure.ensureExists(j);
g.typeInfo=j;
if(Jsonix.Util.Type.isObject(h)){g.elementName=Jsonix.XML.QName.fromObject(h)
}else{if(Jsonix.Util.Type.isString(h)){g.elementName=new Jsonix.XML.QName(this.defaultElementNamespaceURI,h)
}else{throw new Error("Element info ["+g+"] must provide an element name.")
}}var l=g.substitutionHead||g.sh||null;
if(Jsonix.Util.Type.exists(l)){if(Jsonix.Util.Type.isObject(l)){g.substitutionHead=Jsonix.XML.QName.fromObject(l)
}else{Jsonix.Util.Ensure.ensureString(l);
g.substitutionHead=new Jsonix.XML.QName(this.defaultElementNamespaceURI,l)
}}var k=new this.mappingStyle.elementInfo(g,{mappingStyle:this.mappingStyle});
k.module=this;
return k
},registerTypeInfos:function(d){for(var e=0;
e<this.typeInfos.length;
e++){var f=this.typeInfos[e];
d.registerTypeInfo(f,this)
}},buildTypeInfos:function(d){for(var e=0;
e<this.typeInfos.length;
e++){var f=this.typeInfos[e];
f.build(d,this)
}},registerElementInfos:function(d){for(var e=0;
e<this.elementInfos.length;
e++){var f=this.elementInfos[e];
d.registerElementInfo(f,this)
}},buildElementInfos:function(d){for(var e=0;
e<this.elementInfos.length;
e++){var f=this.elementInfos[e];
f.build(d,this)
}},cs:function(){return this
},es:function(){return this
},CLASS_NAME:"Jsonix.Model.Module"});
Jsonix.Model.Module.prototype.typeInfoCreators={classInfo:Jsonix.Model.Module.prototype.createClassInfo,c:Jsonix.Model.Module.prototype.createClassInfo,enumInfo:Jsonix.Model.Module.prototype.createEnumLeafInfo,"enum":Jsonix.Model.Module.prototype.createEnumLeafInfo,list:Jsonix.Model.Module.prototype.createList,l:Jsonix.Model.Module.prototype.createList};
Jsonix.Mapping.Style.Standard=Jsonix.Class(Jsonix.Mapping.Style,{marshaller:Jsonix.Binding.Marshaller,unmarshaller:Jsonix.Binding.Unmarshaller,module:Jsonix.Model.Module,elementInfo:Jsonix.Model.ElementInfo,classInfo:Jsonix.Model.ClassInfo,enumLeafInfo:Jsonix.Model.EnumLeafInfo,anyAttributePropertyInfo:Jsonix.Model.AnyAttributePropertyInfo,anyElementPropertyInfo:Jsonix.Model.AnyElementPropertyInfo,attributePropertyInfo:Jsonix.Model.AttributePropertyInfo,elementMapPropertyInfo:Jsonix.Model.ElementMapPropertyInfo,elementPropertyInfo:Jsonix.Model.ElementPropertyInfo,elementsPropertyInfo:Jsonix.Model.ElementsPropertyInfo,elementRefPropertyInfo:Jsonix.Model.ElementRefPropertyInfo,elementRefsPropertyInfo:Jsonix.Model.ElementRefsPropertyInfo,valuePropertyInfo:Jsonix.Model.ValuePropertyInfo,initialize:function(){Jsonix.Mapping.Style.prototype.initialize.apply(this)
},CLASS_NAME:"Jsonix.Mapping.Style.Standard"});
Jsonix.Mapping.Style.STYLES.standard=new Jsonix.Mapping.Style.Standard();
Jsonix.Mapping.Style.Simplified=Jsonix.Class(Jsonix.Mapping.Style,{marshaller:Jsonix.Binding.Marshaller.Simplified,unmarshaller:Jsonix.Binding.Unmarshaller.Simplified,module:Jsonix.Model.Module,elementInfo:Jsonix.Model.ElementInfo,classInfo:Jsonix.Model.ClassInfo,enumLeafInfo:Jsonix.Model.EnumLeafInfo,anyAttributePropertyInfo:Jsonix.Model.AnyAttributePropertyInfo.Simplified,anyElementPropertyInfo:Jsonix.Model.AnyElementPropertyInfo.Simplified,attributePropertyInfo:Jsonix.Model.AttributePropertyInfo,elementMapPropertyInfo:Jsonix.Model.ElementMapPropertyInfo,elementPropertyInfo:Jsonix.Model.ElementPropertyInfo,elementsPropertyInfo:Jsonix.Model.ElementsPropertyInfo,elementRefPropertyInfo:Jsonix.Model.ElementRefPropertyInfo.Simplified,elementRefsPropertyInfo:Jsonix.Model.ElementRefsPropertyInfo.Simplified,valuePropertyInfo:Jsonix.Model.ValuePropertyInfo,initialize:function(){Jsonix.Mapping.Style.prototype.initialize.apply(this)
},CLASS_NAME:"Jsonix.Mapping.Style.Simplified"});
Jsonix.Mapping.Style.STYLES.simplified=new Jsonix.Mapping.Style.Simplified();
Jsonix.Schema.XSD={};
Jsonix.Schema.XSD.NAMESPACE_URI="http://www.w3.org/2001/XMLSchema";
Jsonix.Schema.XSD.PREFIX="xsd";
Jsonix.Schema.XSD.qname=function(b){Jsonix.Util.Ensure.ensureString(b);
return new Jsonix.XML.QName(Jsonix.Schema.XSD.NAMESPACE_URI,b,Jsonix.Schema.XSD.PREFIX)
};
Jsonix.Schema.XSD.AnyType=Jsonix.Class(Jsonix.Model.ClassInfo,{typeName:Jsonix.Schema.XSD.qname("anyType"),initialize:function(){Jsonix.Model.ClassInfo.prototype.initialize.call(this,{name:"AnyType",propertyInfos:[{type:"anyAttribute",name:"attributes"},{type:"anyElement",name:"content",collection:true}]})
},CLASS_NAME:"Jsonix.Schema.XSD.AnyType"});
Jsonix.Schema.XSD.AnyType.INSTANCE=new Jsonix.Schema.XSD.AnyType();
Jsonix.Schema.XSD.AnySimpleType=Jsonix.Class(Jsonix.Model.TypeInfo,{name:"AnySimpleType",typeName:Jsonix.Schema.XSD.qname("anySimpleType"),initialize:function(){Jsonix.Model.TypeInfo.prototype.initialize.apply(this,[])
},print:function(g,e,f,h){return g
},parse:function(g,e,f,h){return g
},isInstance:function(f,e,d){return true
},reprint:function(g,e,f,h){if(Jsonix.Util.Type.isString(g)&&!this.isInstance(g,e,h)){return this.print(this.parse(g,e,null,h),e,f,h)
}else{return this.print(g,e,f,h)
}},unmarshal:function(e,f,h){var g=f.getElementText();
if(Jsonix.Util.StringUtils.isNotBlank(g)){return this.parse(g,e,f,h)
}else{return null
}},marshal:function(g,e,f,h){if(Jsonix.Util.Type.exists(g)){f.writeCharacters(this.reprint(g,e,f,h))
}},build:function(c,d){},CLASS_NAME:"Jsonix.Schema.XSD.AnySimpleType"});
Jsonix.Schema.XSD.AnySimpleType.INSTANCE=new Jsonix.Schema.XSD.AnySimpleType();
Jsonix.Schema.XSD.List=Jsonix.Class(Jsonix.Schema.XSD.AnySimpleType,{name:null,typeName:null,typeInfo:null,separator:" ",trimmedSeparator:Jsonix.Util.StringUtils.whitespaceCharacters,simpleType:true,built:false,initialize:function(e,f,h){Jsonix.Util.Ensure.ensureExists(e);
this.typeInfo=e;
if(!Jsonix.Util.Type.exists(this.name)){this.name=e.name+"*"
}if(Jsonix.Util.Type.exists(f)){this.typeName=f
}if(Jsonix.Util.Type.isString(h)){this.separator=h
}else{this.separator=" "
}var g=Jsonix.Util.StringUtils.trim(this.separator);
if(g.length===0){this.trimmedSeparator=Jsonix.Util.StringUtils.whitespaceCharacters
}else{this.trimmedSeparator=g
}},build:function(b){if(!this.built){this.typeInfo=b.resolveTypeInfo(this.typeInfo,this.module);
this.built=true
}},print:function(i,k,g,j){if(!Jsonix.Util.Type.exists(i)){return null
}Jsonix.Util.Ensure.ensureArray(i);
var h="";
for(var l=0;
l<i.length;
l++){if(l>0){h=h+this.separator
}h=h+this.typeInfo.reprint(i[l],k,g,j)
}return h
},parse:function(j,l,n,k){Jsonix.Util.Ensure.ensureString(j);
var h=Jsonix.Util.StringUtils.splitBySeparatorChars(j,this.trimmedSeparator);
var i=[];
for(var m=0;
m<h.length;
m++){i.push(this.typeInfo.parse(Jsonix.Util.StringUtils.trim(h[m]),l,n,k))
}return i
},CLASS_NAME:"Jsonix.Schema.XSD.List"});
Jsonix.Schema.XSD.String=Jsonix.Class(Jsonix.Schema.XSD.AnySimpleType,{name:"String",typeName:Jsonix.Schema.XSD.qname("string"),unmarshal:function(e,f,h){var g=f.getElementText();
return this.parse(g,e,f,h)
},print:function(g,e,f,h){Jsonix.Util.Ensure.ensureString(g);
return g
},parse:function(g,e,f,h){Jsonix.Util.Ensure.ensureString(g);
return g
},isInstance:function(f,e,d){return Jsonix.Util.Type.isString(f)
},CLASS_NAME:"Jsonix.Schema.XSD.String"});
Jsonix.Schema.XSD.String.INSTANCE=new Jsonix.Schema.XSD.String();
Jsonix.Schema.XSD.String.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.String.INSTANCE);
Jsonix.Schema.XSD.Strings=Jsonix.Class(Jsonix.Schema.XSD.List,{name:"Strings",initialize:function(){Jsonix.Schema.XSD.List.prototype.initialize.apply(this,[Jsonix.Schema.XSD.String.INSTANCE,Jsonix.Schema.XSD.qname("strings")," "])
},CLASS_NAME:"Jsonix.Schema.XSD.Strings"});
Jsonix.Schema.XSD.Strings.INSTANCE=new Jsonix.Schema.XSD.Strings();
Jsonix.Schema.XSD.NormalizedString=Jsonix.Class(Jsonix.Schema.XSD.String,{name:"NormalizedString",typeName:Jsonix.Schema.XSD.qname("normalizedString"),CLASS_NAME:"Jsonix.Schema.XSD.NormalizedString"});
Jsonix.Schema.XSD.NormalizedString.INSTANCE=new Jsonix.Schema.XSD.NormalizedString();
Jsonix.Schema.XSD.NormalizedString.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.NormalizedString.INSTANCE);
Jsonix.Schema.XSD.Token=Jsonix.Class(Jsonix.Schema.XSD.NormalizedString,{name:"Token",typeName:Jsonix.Schema.XSD.qname("token"),CLASS_NAME:"Jsonix.Schema.XSD.Token"});
Jsonix.Schema.XSD.Token.INSTANCE=new Jsonix.Schema.XSD.Token();
Jsonix.Schema.XSD.Token.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.Token.INSTANCE);
Jsonix.Schema.XSD.Language=Jsonix.Class(Jsonix.Schema.XSD.Token,{name:"Language",typeName:Jsonix.Schema.XSD.qname("language"),CLASS_NAME:"Jsonix.Schema.XSD.Language"});
Jsonix.Schema.XSD.Language.INSTANCE=new Jsonix.Schema.XSD.Language();
Jsonix.Schema.XSD.Language.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.Language.INSTANCE);
Jsonix.Schema.XSD.Name=Jsonix.Class(Jsonix.Schema.XSD.Token,{name:"Name",typeName:Jsonix.Schema.XSD.qname("Name"),CLASS_NAME:"Jsonix.Schema.XSD.Name"});
Jsonix.Schema.XSD.Name.INSTANCE=new Jsonix.Schema.XSD.Name();
Jsonix.Schema.XSD.Name.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.Name.INSTANCE);
Jsonix.Schema.XSD.NCName=Jsonix.Class(Jsonix.Schema.XSD.Name,{name:"NCName",typeName:Jsonix.Schema.XSD.qname("NCName"),CLASS_NAME:"Jsonix.Schema.XSD.NCName"});
Jsonix.Schema.XSD.NCName.INSTANCE=new Jsonix.Schema.XSD.NCName();
Jsonix.Schema.XSD.NCName.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.NCName.INSTANCE);
Jsonix.Schema.XSD.NMToken=Jsonix.Class(Jsonix.Schema.XSD.Token,{name:"NMToken",typeName:Jsonix.Schema.XSD.qname("NMTOKEN"),CLASS_NAME:"Jsonix.Schema.XSD.NMToken"});
Jsonix.Schema.XSD.NMToken.INSTANCE=new Jsonix.Schema.XSD.NMToken();
Jsonix.Schema.XSD.NMTokens=Jsonix.Class(Jsonix.Schema.XSD.List,{name:"NMTokens",initialize:function(){Jsonix.Schema.XSD.List.prototype.initialize.apply(this,[Jsonix.Schema.XSD.NMToken.INSTANCE,Jsonix.Schema.XSD.qname("NMTOKEN")," "])
},CLASS_NAME:"Jsonix.Schema.XSD.NMTokens"});
Jsonix.Schema.XSD.NMTokens.INSTANCE=new Jsonix.Schema.XSD.NMTokens();
Jsonix.Schema.XSD.Boolean=Jsonix.Class(Jsonix.Schema.XSD.AnySimpleType,{name:"Boolean",typeName:Jsonix.Schema.XSD.qname("boolean"),print:function(g,e,f,h){Jsonix.Util.Ensure.ensureBoolean(g);
return g?"true":"false"
},parse:function(g,e,f,h){Jsonix.Util.Ensure.ensureString(g);
if(g==="true"||g==="1"){return true
}else{if(g==="false"||g==="0"){return false
}else{throw new Error("Either [true], [1], [0] or [false] expected as boolean value.")
}}},isInstance:function(f,e,d){return Jsonix.Util.Type.isBoolean(f)
},CLASS_NAME:"Jsonix.Schema.XSD.Boolean"});
Jsonix.Schema.XSD.Boolean.INSTANCE=new Jsonix.Schema.XSD.Boolean();
Jsonix.Schema.XSD.Boolean.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.Boolean.INSTANCE);
Jsonix.Schema.XSD.Base64Binary=Jsonix.Class(Jsonix.Schema.XSD.AnySimpleType,{name:"Base64Binary",typeName:Jsonix.Schema.XSD.qname("base64Binary"),charToByte:{},byteToChar:[],initialize:function(){Jsonix.Schema.XSD.AnySimpleType.prototype.initialize.apply(this);
var f="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
for(var h=0;
h<f.length;
h++){var e=f.charAt(h);
var g=f.charCodeAt(h);
this.byteToChar[h]=e;
this.charToByte[e]=h
}},print:function(g,e,f,h){Jsonix.Util.Ensure.ensureArray(g);
return this.encode(g)
},parse:function(g,e,f,h){Jsonix.Util.Ensure.ensureString(g);
return this.decode(g)
},encode:function(s){var w="";
var i;
var j;
var o;
var p;
var q;
var r;
var t;
var u=0;
var v=0;
var x=s.length;
for(u=0;
u<x;
u+=3){i=s[u]&255;
p=this.byteToChar[i>>2];
if(u+1<x){j=s[u+1]&255;
q=this.byteToChar[((i&3)<<4)|(j>>4)];
if(u+2<x){o=s[u+2]&255;
r=this.byteToChar[((j&15)<<2)|(o>>6)];
t=this.byteToChar[o&63]
}else{r=this.byteToChar[(j&15)<<2];
t="="
}}else{q=this.byteToChar[(i&3)<<4];
r="=";
t="="
}w=w+p+q+r+t
}return w
},decode:function(j){input=j.replace(/[^A-Za-z0-9\+\/\=]/g,"");
var x=(input.length/4)*3;
if(input.charAt(input.length-1)==="="){x--
}if(input.charAt(input.length-2)==="="){x--
}var t=new Array(x);
var i;
var o;
var p;
var q;
var r;
var s;
var u;
var v=0;
var w=0;
for(v=0;
v<x;
v+=3){q=this.charToByte[input.charAt(w++)];
r=this.charToByte[input.charAt(w++)];
s=this.charToByte[input.charAt(w++)];
u=this.charToByte[input.charAt(w++)];
i=(q<<2)|(r>>4);
o=((r&15)<<4)|(s>>2);
p=((s&3)<<6)|u;
t[v]=i;
if(s!=64){t[v+1]=o
}if(u!=64){t[v+2]=p
}}return t
},isInstance:function(f,e,d){return Jsonix.Util.Type.isArray(f)
},CLASS_NAME:"Jsonix.Schema.XSD.Base64Binary"});
Jsonix.Schema.XSD.Base64Binary.INSTANCE=new Jsonix.Schema.XSD.Base64Binary();
Jsonix.Schema.XSD.Base64Binary.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.Base64Binary.INSTANCE);
Jsonix.Schema.XSD.HexBinary=Jsonix.Class(Jsonix.Schema.XSD.AnySimpleType,{name:"HexBinary",typeName:Jsonix.Schema.XSD.qname("hexBinary"),charToQuartet:{},byteToDuplet:[],initialize:function(){Jsonix.Schema.XSD.AnySimpleType.prototype.initialize.apply(this);
var d="0123456789ABCDEF";
var f=d.toLowerCase();
var e;
for(e=0;
e<16;
e++){this.charToQuartet[d.charAt(e)]=e;
if(e>=10){this.charToQuartet[f.charAt(e)]=e
}}for(e=0;
e<256;
e++){this.byteToDuplet[e]=d[e>>4]+d[e&15]
}},print:function(g,e,f,h){Jsonix.Util.Ensure.ensureArray(g);
return this.encode(g)
},parse:function(g,e,f,h){Jsonix.Util.Ensure.ensureString(g);
return this.decode(g)
},encode:function(f){var e="";
for(var d=0;
d<f.length;
d++){e=e+this.byteToDuplet[f[d]&255]
}return e
},decode:function(j){var i=j.replace(/[^A-Fa-f0-9]/g,"");
var l=i.length>>1;
var n=new Array(l);
for(var h=0;
h<l;
h++){var k=i.charAt(2*h);
var m=i.charAt(2*h+1);
n[h]=this.charToQuartet[k]<<4|this.charToQuartet[m]
}return n
},isInstance:function(f,e,d){return Jsonix.Util.Type.isArray(f)
},CLASS_NAME:"Jsonix.Schema.XSD.HexBinary"});
Jsonix.Schema.XSD.HexBinary.INSTANCE=new Jsonix.Schema.XSD.HexBinary();
Jsonix.Schema.XSD.HexBinary.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.HexBinary.INSTANCE);
Jsonix.Schema.XSD.Number=Jsonix.Class(Jsonix.Schema.XSD.AnySimpleType,{name:"Number",typeName:Jsonix.Schema.XSD.qname("number"),print:function(i,f,g,j){Jsonix.Util.Ensure.ensureNumberOrNaN(i);
if(Jsonix.Util.Type.isNaN(i)){return"NaN"
}else{if(i===Infinity){return"INF"
}else{if(i===-Infinity){return"-INF"
}else{var h=String(i);
return h
}}}},parse:function(h,f,g,j){Jsonix.Util.Ensure.ensureString(h);
if(h==="-INF"){return -Infinity
}else{if(h==="INF"){return Infinity
}else{if(h==="NaN"){return NaN
}else{var i=Number(h);
Jsonix.Util.Ensure.ensureNumber(i);
return i
}}}},isInstance:function(f,e,d){return Jsonix.Util.Type.isNumberOrNaN(f)
},CLASS_NAME:"Jsonix.Schema.XSD.Number"});
Jsonix.Schema.XSD.Number.INSTANCE=new Jsonix.Schema.XSD.Number();
Jsonix.Schema.XSD.Number.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.Number.INSTANCE);
Jsonix.Schema.XSD.Float=Jsonix.Class(Jsonix.Schema.XSD.Number,{name:"Float",typeName:Jsonix.Schema.XSD.qname("float"),isInstance:function(f,e,d){return Jsonix.Util.Type.isNaN(f)||f===-Infinity||f===Infinity||(Jsonix.Util.Type.isNumber(f)&&f>=this.MIN_VALUE&&f<=this.MAX_VALUE)
},MIN_VALUE:-3.4028235e+38,MAX_VALUE:3.4028235e+38,CLASS_NAME:"Jsonix.Schema.XSD.Float"});
Jsonix.Schema.XSD.Float.INSTANCE=new Jsonix.Schema.XSD.Float();
Jsonix.Schema.XSD.Float.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.Float.INSTANCE);
Jsonix.Schema.XSD.Decimal=Jsonix.Class(Jsonix.Schema.XSD.AnySimpleType,{name:"Decimal",typeName:Jsonix.Schema.XSD.qname("decimal"),print:function(i,f,g,j){Jsonix.Util.Ensure.ensureNumber(i);
var h=String(i);
return h
},parse:function(h,f,g,j){Jsonix.Util.Ensure.ensureString(h);
var i=Number(h);
Jsonix.Util.Ensure.ensureNumber(i);
return i
},isInstance:function(f,e,d){return Jsonix.Util.Type.isNumber(f)
},CLASS_NAME:"Jsonix.Schema.XSD.Decimal"});
Jsonix.Schema.XSD.Decimal.INSTANCE=new Jsonix.Schema.XSD.Decimal();
Jsonix.Schema.XSD.Decimal.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.Decimal.INSTANCE);
Jsonix.Schema.XSD.Integer=Jsonix.Class(Jsonix.Schema.XSD.AnySimpleType,{name:"Integer",typeName:Jsonix.Schema.XSD.qname("integer"),print:function(i,f,g,j){Jsonix.Util.Ensure.ensureInteger(i);
var h=String(i);
return h
},parse:function(h,f,g,j){Jsonix.Util.Ensure.ensureString(h);
var i=Number(h);
Jsonix.Util.Ensure.ensureInteger(i);
return i
},isInstance:function(f,e,d){return Jsonix.Util.NumberUtils.isInteger(f)&&f>=this.MIN_VALUE&&f<=this.MAX_VALUE
},MIN_VALUE:-9223372036854776000,MAX_VALUE:9223372036854776000,CLASS_NAME:"Jsonix.Schema.XSD.Integer"});
Jsonix.Schema.XSD.Integer.INSTANCE=new Jsonix.Schema.XSD.Integer();
Jsonix.Schema.XSD.Integer.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.Integer.INSTANCE);
Jsonix.Schema.XSD.NonPositiveInteger=Jsonix.Class(Jsonix.Schema.XSD.Integer,{name:"NonPositiveInteger",typeName:Jsonix.Schema.XSD.qname("nonPositiveInteger"),MIN_VALUE:-9223372036854776000,MAX_VALUE:0,CLASS_NAME:"Jsonix.Schema.XSD.NonPositiveInteger"});
Jsonix.Schema.XSD.NonPositiveInteger.INSTANCE=new Jsonix.Schema.XSD.NonPositiveInteger();
Jsonix.Schema.XSD.NonPositiveInteger.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.NonPositiveInteger.INSTANCE);
Jsonix.Schema.XSD.NegativeInteger=Jsonix.Class(Jsonix.Schema.XSD.NonPositiveInteger,{name:"NegativeInteger",typeName:Jsonix.Schema.XSD.qname("negativeInteger"),MIN_VALUE:-9223372036854776000,MAX_VALUE:-1,CLASS_NAME:"Jsonix.Schema.XSD.NegativeInteger"});
Jsonix.Schema.XSD.NegativeInteger.INSTANCE=new Jsonix.Schema.XSD.NegativeInteger();
Jsonix.Schema.XSD.NegativeInteger.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.NegativeInteger.INSTANCE);
Jsonix.Schema.XSD.Long=Jsonix.Class(Jsonix.Schema.XSD.Integer,{name:"Long",typeName:Jsonix.Schema.XSD.qname("long"),MIN_VALUE:-9223372036854776000,MAX_VALUE:9223372036854776000,CLASS_NAME:"Jsonix.Schema.XSD.Long"});
Jsonix.Schema.XSD.Long.INSTANCE=new Jsonix.Schema.XSD.Long();
Jsonix.Schema.XSD.Long.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.Long.INSTANCE);
Jsonix.Schema.XSD.Int=Jsonix.Class(Jsonix.Schema.XSD.Long,{name:"Int",typeName:Jsonix.Schema.XSD.qname("int"),MIN_VALUE:-2147483648,MAX_VALUE:2147483647,CLASS_NAME:"Jsonix.Schema.XSD.Int"});
Jsonix.Schema.XSD.Int.INSTANCE=new Jsonix.Schema.XSD.Int();
Jsonix.Schema.XSD.Int.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.Int.INSTANCE);
Jsonix.Schema.XSD.Short=Jsonix.Class(Jsonix.Schema.XSD.Int,{name:"Short",typeName:Jsonix.Schema.XSD.qname("short"),MIN_VALUE:-32768,MAX_VALUE:32767,CLASS_NAME:"Jsonix.Schema.XSD.Short"});
Jsonix.Schema.XSD.Short.INSTANCE=new Jsonix.Schema.XSD.Short();
Jsonix.Schema.XSD.Short.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.Short.INSTANCE);
Jsonix.Schema.XSD.Byte=Jsonix.Class(Jsonix.Schema.XSD.Short,{name:"Byte",typeName:Jsonix.Schema.XSD.qname("byte"),MIN_VALUE:-128,MAX_VALUE:127,CLASS_NAME:"Jsonix.Schema.XSD.Byte"});
Jsonix.Schema.XSD.Byte.INSTANCE=new Jsonix.Schema.XSD.Byte();
Jsonix.Schema.XSD.Byte.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.Byte.INSTANCE);
Jsonix.Schema.XSD.NonNegativeInteger=Jsonix.Class(Jsonix.Schema.XSD.Integer,{name:"NonNegativeInteger",typeName:Jsonix.Schema.XSD.qname("nonNegativeInteger"),MIN_VALUE:0,MAX_VALUE:9223372036854776000,CLASS_NAME:"Jsonix.Schema.XSD.NonNegativeInteger"});
Jsonix.Schema.XSD.NonNegativeInteger.INSTANCE=new Jsonix.Schema.XSD.NonNegativeInteger();
Jsonix.Schema.XSD.NonNegativeInteger.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.NonNegativeInteger.INSTANCE);
Jsonix.Schema.XSD.UnsignedLong=Jsonix.Class(Jsonix.Schema.XSD.NonNegativeInteger,{name:"UnsignedLong",typeName:Jsonix.Schema.XSD.qname("unsignedLong"),MIN_VALUE:0,MAX_VALUE:18446744073709552000,CLASS_NAME:"Jsonix.Schema.XSD.UnsignedLong"});
Jsonix.Schema.XSD.UnsignedLong.INSTANCE=new Jsonix.Schema.XSD.UnsignedLong();
Jsonix.Schema.XSD.UnsignedLong.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.UnsignedLong.INSTANCE);
Jsonix.Schema.XSD.UnsignedInt=Jsonix.Class(Jsonix.Schema.XSD.UnsignedLong,{name:"UnsignedInt",typeName:Jsonix.Schema.XSD.qname("unsignedInt"),MIN_VALUE:0,MAX_VALUE:4294967295,CLASS_NAME:"Jsonix.Schema.XSD.UnsignedInt"});
Jsonix.Schema.XSD.UnsignedInt.INSTANCE=new Jsonix.Schema.XSD.UnsignedInt();
Jsonix.Schema.XSD.UnsignedInt.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.UnsignedInt.INSTANCE);
Jsonix.Schema.XSD.UnsignedShort=Jsonix.Class(Jsonix.Schema.XSD.UnsignedInt,{name:"UnsignedShort",typeName:Jsonix.Schema.XSD.qname("unsignedShort"),MIN_VALUE:0,MAX_VALUE:65535,CLASS_NAME:"Jsonix.Schema.XSD.UnsignedShort"});
Jsonix.Schema.XSD.UnsignedShort.INSTANCE=new Jsonix.Schema.XSD.UnsignedShort();
Jsonix.Schema.XSD.UnsignedShort.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.UnsignedShort.INSTANCE);
Jsonix.Schema.XSD.UnsignedByte=Jsonix.Class(Jsonix.Schema.XSD.UnsignedShort,{name:"UnsignedByte",typeName:Jsonix.Schema.XSD.qname("unsignedByte"),MIN_VALUE:0,MAX_VALUE:255,CLASS_NAME:"Jsonix.Schema.XSD.UnsignedByte"});
Jsonix.Schema.XSD.UnsignedByte.INSTANCE=new Jsonix.Schema.XSD.UnsignedByte();
Jsonix.Schema.XSD.UnsignedByte.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.UnsignedByte.INSTANCE);
Jsonix.Schema.XSD.PositiveInteger=Jsonix.Class(Jsonix.Schema.XSD.NonNegativeInteger,{name:"PositiveInteger",typeName:Jsonix.Schema.XSD.qname("positiveInteger"),MIN_VALUE:1,MAX_VALUE:9223372036854776000,CLASS_NAME:"Jsonix.Schema.XSD.PositiveInteger"});
Jsonix.Schema.XSD.PositiveInteger.INSTANCE=new Jsonix.Schema.XSD.PositiveInteger();
Jsonix.Schema.XSD.PositiveInteger.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.PositiveInteger.INSTANCE);
Jsonix.Schema.XSD.Double=Jsonix.Class(Jsonix.Schema.XSD.Number,{name:"Double",typeName:Jsonix.Schema.XSD.qname("double"),isInstance:function(f,e,d){return Jsonix.Util.Type.isNaN(f)||f===-Infinity||f===Infinity||(Jsonix.Util.Type.isNumber(f)&&f>=this.MIN_VALUE&&f<=this.MAX_VALUE)
},MIN_VALUE:-1.7976931348623157e+308,MAX_VALUE:1.7976931348623157e+308,CLASS_NAME:"Jsonix.Schema.XSD.Double"});
Jsonix.Schema.XSD.Double.INSTANCE=new Jsonix.Schema.XSD.Double();
Jsonix.Schema.XSD.Double.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.Double.INSTANCE);
Jsonix.Schema.XSD.AnyURI=Jsonix.Class(Jsonix.Schema.XSD.AnySimpleType,{name:"AnyURI",typeName:Jsonix.Schema.XSD.qname("anyURI"),print:function(g,e,f,h){Jsonix.Util.Ensure.ensureString(g);
return g
},parse:function(g,e,f,h){Jsonix.Util.Ensure.ensureString(g);
return g
},isInstance:function(f,e,d){return Jsonix.Util.Type.isString(f)
},CLASS_NAME:"Jsonix.Schema.XSD.AnyURI"});
Jsonix.Schema.XSD.AnyURI.INSTANCE=new Jsonix.Schema.XSD.AnyURI();
Jsonix.Schema.XSD.AnyURI.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.AnyURI.INSTANCE);
Jsonix.Schema.XSD.QName=Jsonix.Class(Jsonix.Schema.XSD.AnySimpleType,{name:"QName",typeName:Jsonix.Schema.XSD.qname("QName"),print:function(j,n,h,m){var k=Jsonix.XML.QName.fromObject(j);
var l;
var i=k.localPart;
if(h){l=h.getPrefix(k.namespaceURI,k.prefix||null);
h.declareNamespace(k.namespaceURI,l)
}else{l=k.prefix
}return !l?i:(l+":"+i)
},parse:function(k,r,l,j){Jsonix.Util.Ensure.ensureString(k);
k=Jsonix.Util.StringUtils.trim(k);
var m;
var n;
var p=k.indexOf(":");
if(p===-1){m="";
n=k
}else{if(p>0&&p<(k.length-1)){m=k.substring(0,p);
n=k.substring(p+1)
}else{throw new Error("Invalid QName ["+k+"].")
}}var o=l||r||null;
if(!o){return k
}else{var q=o.getNamespaceURI(m);
if(Jsonix.Util.Type.isString(q)){return new Jsonix.XML.QName(q,n,m)
}else{throw new Error("Prefix ["+m+"] of the QName ["+k+"] is not bound in this context.")
}}},isInstance:function(f,e,d){return(f instanceof Jsonix.XML.QName)||(Jsonix.Util.Type.isObject(f)&&Jsonix.Util.Type.isString(f.localPart||f.lp))
},CLASS_NAME:"Jsonix.Schema.XSD.QName"});
Jsonix.Schema.XSD.QName.INSTANCE=new Jsonix.Schema.XSD.QName();
Jsonix.Schema.XSD.QName.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.QName.INSTANCE);
Jsonix.Schema.XSD.Calendar=Jsonix.Class(Jsonix.Schema.XSD.AnySimpleType,{name:"Calendar",typeName:Jsonix.Schema.XSD.qname("calendar"),parse:function(g,e,f,h){Jsonix.Util.Ensure.ensureString(g);
if(g.match(new RegExp("^"+Jsonix.Schema.XSD.Calendar.DATETIME_PATTERN+"$"))){return this.parseDateTime(g,e,f,h)
}else{if(g.match(new RegExp("^"+Jsonix.Schema.XSD.Calendar.DATE_PATTERN+"$"))){return this.parseDate(g,e,f,h)
}else{if(g.match(new RegExp("^"+Jsonix.Schema.XSD.Calendar.TIME_PATTERN+"$"))){return this.parseTime(g,e,f,h)
}else{if(g.match(new RegExp("^"+Jsonix.Schema.XSD.Calendar.GYEAR_MONTH_PATTERN+"$"))){return this.parseGYearMonth(g,e,f,h)
}else{if(g.match(new RegExp("^"+Jsonix.Schema.XSD.Calendar.GYEAR_PATTERN+"$"))){return this.parseGYear(g,e,f,h)
}else{if(g.match(new RegExp("^"+Jsonix.Schema.XSD.Calendar.GMONTH_DAY_PATTERN+"$"))){return this.parseGMonthDay(g,e,f,h)
}else{if(g.match(new RegExp("^"+Jsonix.Schema.XSD.Calendar.GMONTH_PATTERN+"$"))){return this.parseGMonth(g,e,f,h)
}else{if(g.match(new RegExp("^"+Jsonix.Schema.XSD.Calendar.GDAY_PATTERN+"$"))){return this.parseGDay(g,e,f,h)
}else{throw new Error("Value ["+g+"] does not match xs:dateTime, xs:date, xs:time, xs:gYearMonth, xs:gYear, xs:gMonthDay, xs:gMonth or xs:gDay patterns.")
}}}}}}}}},parseGYearMonth:function(j,n,i,m){var l=new RegExp("^"+Jsonix.Schema.XSD.Calendar.GYEAR_MONTH_PATTERN+"$");
var h=j.match(l);
if(h!==null){var k={year:parseInt(h[1],10),month:parseInt(h[5],10),timezone:this.parseTimezoneString(h[7])};
return new Jsonix.XML.Calendar(k)
}throw new Error("Value ["+j+"] does not match the xs:gYearMonth pattern.")
},parseGYear:function(j,n,i,m){var l=new RegExp("^"+Jsonix.Schema.XSD.Calendar.GYEAR_PATTERN+"$");
var h=j.match(l);
if(h!==null){var k={year:parseInt(h[1],10),timezone:this.parseTimezoneString(h[5])};
return new Jsonix.XML.Calendar(k)
}throw new Error("Value ["+j+"] does not match the xs:gYear pattern.")
},parseGMonthDay:function(j,n,i,m){var k=new RegExp("^"+Jsonix.Schema.XSD.Calendar.GMONTH_DAY_PATTERN+"$");
var h=j.match(k);
if(h!==null){var l={month:parseInt(h[2],10),day:parseInt(h[3],10),timezone:this.parseTimezoneString(h[5])};
return new Jsonix.XML.Calendar(l)
}throw new Error("Value ["+j+"] does not match the xs:gMonthDay pattern.")
},parseGMonth:function(k,n,i,m){var j=new RegExp("^"+Jsonix.Schema.XSD.Calendar.GMONTH_PATTERN+"$");
var h=k.match(j);
if(h!==null){var l={month:parseInt(h[2],10),timezone:this.parseTimezoneString(h[3])};
return new Jsonix.XML.Calendar(l)
}throw new Error("Value ["+k+"] does not match the xs:gMonth pattern.")
},parseGDay:function(j,n,i,m){var l=new RegExp("^"+Jsonix.Schema.XSD.Calendar.GDAY_PATTERN+"$");
var h=j.match(l);
if(h!==null){var k={day:parseInt(h[2],10),timezone:this.parseTimezoneString(h[3])};
return new Jsonix.XML.Calendar(k)
}throw new Error("Value ["+j+"] does not match the xs:gDay pattern.")
},parseDateTime:function(j,n,i,m){Jsonix.Util.Ensure.ensureString(j);
var k=new RegExp("^"+Jsonix.Schema.XSD.Calendar.DATETIME_PATTERN+"$");
var h=j.match(k);
if(h!==null){var l={year:parseInt(h[1],10),month:parseInt(h[5],10),day:parseInt(h[7],10),hour:parseInt(h[9],10),minute:parseInt(h[10],10),second:parseInt(h[11],10),fractionalSecond:(h[12]?parseFloat(h[12]):0),timezone:this.parseTimezoneString(h[14])};
return new Jsonix.XML.Calendar(l)
}throw new Error("Value ["+value+"] does not match the xs:date pattern.")
},parseDate:function(j,n,i,m){Jsonix.Util.Ensure.ensureString(j);
var k=new RegExp("^"+Jsonix.Schema.XSD.Calendar.DATE_PATTERN+"$");
var h=j.match(k);
if(h!==null){var l={year:parseInt(h[1],10),month:parseInt(h[5],10),day:parseInt(h[7],10),timezone:this.parseTimezoneString(h[9])};
return new Jsonix.XML.Calendar(l)
}throw new Error("Value ["+value+"] does not match the xs:date pattern.")
},parseTime:function(j,n,i,m){Jsonix.Util.Ensure.ensureString(j);
var k=new RegExp("^"+Jsonix.Schema.XSD.Calendar.TIME_PATTERN+"$");
var h=j.match(k);
if(h!==null){var l={hour:parseInt(h[1],10),minute:parseInt(h[2],10),second:parseInt(h[3],10),fractionalSecond:(h[4]?parseFloat(h[4]):0),timezone:this.parseTimezoneString(h[6])};
return new Jsonix.XML.Calendar(l)
}throw new Error("Value ["+value+"] does not match the xs:time pattern.")
},parseTimezoneString:function(i){if(!Jsonix.Util.Type.isString(i)){return NaN
}else{if(i===""){return NaN
}else{if(i==="Z"){return 0
}else{if(i==="+14:00"){return 14*60
}else{if(i==="-14:00"){return -14*60
}else{var j=new RegExp("^"+Jsonix.Schema.XSD.Calendar.TIMEZONE_PATTERN+"$");
var l=i.match(j);
if(l!==null){var g=l[1]==="+"?1:-1;
var h=parseInt(l[4],10);
var k=parseInt(l[5],10);
return g*(h*60+k)
}throw new Error("Value ["+value+"] does not match the timezone pattern.")
}}}}}},print:function(g,e,f,h){Jsonix.Util.Ensure.ensureObject(g);
if(Jsonix.Util.NumberUtils.isInteger(g.year)&&Jsonix.Util.NumberUtils.isInteger(g.month)&&Jsonix.Util.NumberUtils.isInteger(g.day)&&Jsonix.Util.NumberUtils.isInteger(g.hour)&&Jsonix.Util.NumberUtils.isInteger(g.minute)&&Jsonix.Util.NumberUtils.isInteger(g.second)){return this.printDateTime(g)
}else{if(Jsonix.Util.NumberUtils.isInteger(g.year)&&Jsonix.Util.NumberUtils.isInteger(g.month)&&Jsonix.Util.NumberUtils.isInteger(g.day)){return this.printDate(g)
}else{if(Jsonix.Util.NumberUtils.isInteger(g.hour)&&Jsonix.Util.NumberUtils.isInteger(g.minute)&&Jsonix.Util.NumberUtils.isInteger(g.second)){return this.printTime(g)
}else{if(Jsonix.Util.NumberUtils.isInteger(g.year)&&Jsonix.Util.NumberUtils.isInteger(g.month)){return this.printGYearMonth(g)
}else{if(Jsonix.Util.NumberUtils.isInteger(g.month)&&Jsonix.Util.NumberUtils.isInteger(g.day)){return this.printGMonthDay(g)
}else{if(Jsonix.Util.NumberUtils.isInteger(g.year)){return this.printGYear(g)
}else{if(Jsonix.Util.NumberUtils.isInteger(g.month)){return this.printGMonth(g)
}else{if(Jsonix.Util.NumberUtils.isInteger(g.day)){return this.printGDay(g)
}else{throw new Error("Value ["+g+"] is not recognized as dateTime, date or time.")
}}}}}}}}},printDateTime:function(c){Jsonix.Util.Ensure.ensureObject(c);
Jsonix.Util.Ensure.ensureInteger(c.year);
Jsonix.Util.Ensure.ensureInteger(c.month);
Jsonix.Util.Ensure.ensureInteger(c.day);
Jsonix.Util.Ensure.ensureInteger(c.hour);
Jsonix.Util.Ensure.ensureInteger(c.minute);
Jsonix.Util.Ensure.ensureNumber(c.second);
if(Jsonix.Util.Type.exists(c.fractionalString)){Jsonix.Util.Ensure.ensureNumber(c.fractionalString)
}if(Jsonix.Util.Type.exists(c.timezone)&&!Jsonix.Util.Type.isNaN(c.timezone)){Jsonix.Util.Ensure.ensureInteger(c.timezone)
}var d=this.printDateString(c);
d=d+"T";
d=d+this.printTimeString(c);
if(Jsonix.Util.Type.exists(c.timezone)){d=d+this.printTimezoneString(c.timezone)
}return d
},printDate:function(c){Jsonix.Util.Ensure.ensureObject(c);
Jsonix.Util.Ensure.ensureNumber(c.year);
Jsonix.Util.Ensure.ensureNumber(c.month);
Jsonix.Util.Ensure.ensureNumber(c.day);
if(Jsonix.Util.Type.exists(c.timezone)&&!Jsonix.Util.Type.isNaN(c.timezone)){Jsonix.Util.Ensure.ensureInteger(c.timezone)
}var d=this.printDateString(c);
if(Jsonix.Util.Type.exists(c.timezone)){d=d+this.printTimezoneString(c.timezone)
}return d
},printTime:function(c){Jsonix.Util.Ensure.ensureObject(c);
Jsonix.Util.Ensure.ensureNumber(c.hour);
Jsonix.Util.Ensure.ensureNumber(c.minute);
Jsonix.Util.Ensure.ensureNumber(c.second);
if(Jsonix.Util.Type.exists(c.fractionalString)){Jsonix.Util.Ensure.ensureNumber(c.fractionalString)
}if(Jsonix.Util.Type.exists(c.timezone)&&!Jsonix.Util.Type.isNaN(c.timezone)){Jsonix.Util.Ensure.ensureInteger(c.timezone)
}var d=this.printTimeString(c);
if(Jsonix.Util.Type.exists(c.timezone)){d=d+this.printTimezoneString(c.timezone)
}return d
},printDateString:function(b){Jsonix.Util.Ensure.ensureObject(b);
Jsonix.Util.Ensure.ensureInteger(b.year);
Jsonix.Util.Ensure.ensureInteger(b.month);
Jsonix.Util.Ensure.ensureInteger(b.day);
return(b.year<0?("-"+this.printYear(-b.year)):this.printYear(b.year))+"-"+this.printMonth(b.month)+"-"+this.printDay(b.day)
},printTimeString:function(c){Jsonix.Util.Ensure.ensureObject(c);
Jsonix.Util.Ensure.ensureInteger(c.hour);
Jsonix.Util.Ensure.ensureInteger(c.minute);
Jsonix.Util.Ensure.ensureInteger(c.second);
if(Jsonix.Util.Type.exists(c.fractionalSecond)){Jsonix.Util.Ensure.ensureNumber(c.fractionalSecond)
}var d=this.printHour(c.hour);
d=d+":";
d=d+this.printMinute(c.minute);
d=d+":";
d=d+this.printSecond(c.second);
if(Jsonix.Util.Type.exists(c.fractionalSecond)){d=d+this.printFractionalSecond(c.fractionalSecond)
}return d
},printTimezoneString:function(j){if(!Jsonix.Util.Type.exists(j)||Jsonix.Util.Type.isNaN(j)){return""
}else{Jsonix.Util.Ensure.ensureInteger(j);
var l=j<0?-1:(j>0?1:0);
var k=j*l;
var i=k%60;
var g=Math.floor(k/60);
var h;
if(l===0){return"Z"
}else{if(l>0){h="+"
}else{if(l<0){h="-"
}}h=h+this.printHour(g);
h=h+":";
h=h+this.printMinute(i);
return h
}}},printGDay:function(i,l,g,k){Jsonix.Util.Ensure.ensureObject(i);
var h=undefined;
var j=undefined;
if(i instanceof Date){h=i.getDate()
}else{Jsonix.Util.Ensure.ensureInteger(i.day);
h=i.day;
j=i.timezone
}Jsonix.XML.Calendar.validateDay(h);
Jsonix.XML.Calendar.validateTimezone(j);
return"---"+this.printDay(h)+this.printTimezoneString(j)
},printGMonth:function(j,g,h,l){Jsonix.Util.Ensure.ensureObject(j);
var i=undefined;
var k=undefined;
if(j instanceof Date){i=j.getMonth()+1
}else{Jsonix.Util.Ensure.ensureInteger(j.month);
i=j.month;
k=j.timezone
}Jsonix.XML.Calendar.validateMonth(i);
Jsonix.XML.Calendar.validateTimezone(k);
return"--"+this.printMonth(i)+this.printTimezoneString(k)
},printGMonthDay:function(k,n,h,m){Jsonix.Util.Ensure.ensureObject(k);
var j=undefined;
var i=undefined;
var l=undefined;
if(k instanceof Date){j=k.getMonth()+1;
i=k.getDate()
}else{Jsonix.Util.Ensure.ensureInteger(k.month);
Jsonix.Util.Ensure.ensureInteger(k.day);
j=k.month;
i=k.day;
l=k.timezone
}Jsonix.XML.Calendar.validateMonthDay(j,i);
Jsonix.XML.Calendar.validateTimezone(l);
return"--"+this.printMonth(j)+"-"+this.printDay(i)+this.printTimezoneString(l)
},printGYear:function(i,g,h,k){Jsonix.Util.Ensure.ensureObject(i);
var l=undefined;
var j=undefined;
if(i instanceof Date){l=i.getFullYear()
}else{Jsonix.Util.Ensure.ensureInteger(i.year);
l=i.year;
j=i.timezone
}Jsonix.XML.Calendar.validateYear(l);
Jsonix.XML.Calendar.validateTimezone(j);
return this.printSignedYear(l)+this.printTimezoneString(j)
},printGYearMonth:function(k,h,i,m){Jsonix.Util.Ensure.ensureObject(k);
var n=undefined;
var j=undefined;
var l=undefined;
if(k instanceof Date){n=k.getFullYear();
j=k.getMonth()+1
}else{Jsonix.Util.Ensure.ensureInteger(k.year);
n=k.year;
j=k.month;
l=k.timezone
}Jsonix.XML.Calendar.validateYear(n);
Jsonix.XML.Calendar.validateMonth(j);
Jsonix.XML.Calendar.validateTimezone(l);
return this.printSignedYear(n)+"-"+this.printMonth(j)+this.printTimezoneString(l)
},printSignedYear:function(b){return b<0?("-"+this.printYear(b*-1)):(this.printYear(b))
},printYear:function(b){return this.printInteger(b,4)
},printMonth:function(b){return this.printInteger(b,2)
},printDay:function(b){return this.printInteger(b,2)
},printHour:function(b){return this.printInteger(b,2)
},printMinute:function(b){return this.printInteger(b,2)
},printSecond:function(b){return this.printInteger(b,2)
},printFractionalSecond:function(f){Jsonix.Util.Ensure.ensureNumber(f);
if(f<0||f>=1){throw new Error("Fractional second ["+f+"] must be between 0 and 1.")
}else{if(f===0){return""
}else{var e=String(f);
var d=e.indexOf(".");
if(d<0){return""
}else{return e.substring(d)
}}}},printInteger:function(g,h){Jsonix.Util.Ensure.ensureInteger(g);
Jsonix.Util.Ensure.ensureInteger(h);
if(h<=0){throw new Error("Length ["+g+"] must be positive.")
}if(g<0){throw new Error("Value ["+g+"] must not be negative.")
}var f=String(g);
for(var e=f.length;
e<h;
e++){f="0"+f
}return f
},isInstance:function(f,e,d){return Jsonix.Util.Type.isObject(f)&&((Jsonix.Util.NumberUtils.isInteger(f.year)&&Jsonix.Util.NumberUtils.isInteger(f.month)&&Jsonix.Util.NumberUtils.isInteger(f.day))||(Jsonix.Util.NumberUtils.isInteger(f.hour)&&Jsonix.Util.NumberUtils.isInteger(f.minute)&&Jsonix.Util.NumberUtils.isInteger(f.second)))
},CLASS_NAME:"Jsonix.Schema.XSD.Calendar"});
Jsonix.Schema.XSD.Calendar.YEAR_PATTERN="-?([1-9][0-9]*)?((?!(0000))[0-9]{4})";
Jsonix.Schema.XSD.Calendar.TIMEZONE_PATTERN="Z|([\\-\\+])(((0[0-9]|1[0-3]):([0-5][0-9]))|(14:00))";
Jsonix.Schema.XSD.Calendar.MONTH_PATTERN="(0[1-9]|1[0-2])";
Jsonix.Schema.XSD.Calendar.SINGLE_MONTH_PATTERN="\\-\\-"+Jsonix.Schema.XSD.Calendar.MONTH_PATTERN;
Jsonix.Schema.XSD.Calendar.DAY_PATTERN="(0[1-9]|[12][0-9]|3[01])";
Jsonix.Schema.XSD.Calendar.SINGLE_DAY_PATTERN="\\-\\-\\-"+Jsonix.Schema.XSD.Calendar.DAY_PATTERN;
Jsonix.Schema.XSD.Calendar.GYEAR_PATTERN="("+Jsonix.Schema.XSD.Calendar.YEAR_PATTERN+")("+Jsonix.Schema.XSD.Calendar.TIMEZONE_PATTERN+")?";
Jsonix.Schema.XSD.Calendar.GMONTH_PATTERN="("+Jsonix.Schema.XSD.Calendar.SINGLE_MONTH_PATTERN+")("+Jsonix.Schema.XSD.Calendar.TIMEZONE_PATTERN+")?";
Jsonix.Schema.XSD.Calendar.GDAY_PATTERN="("+Jsonix.Schema.XSD.Calendar.SINGLE_DAY_PATTERN+")("+Jsonix.Schema.XSD.Calendar.TIMEZONE_PATTERN+")?";
Jsonix.Schema.XSD.Calendar.GYEAR_MONTH_PATTERN="("+Jsonix.Schema.XSD.Calendar.YEAR_PATTERN+")-("+Jsonix.Schema.XSD.Calendar.DAY_PATTERN+")("+Jsonix.Schema.XSD.Calendar.TIMEZONE_PATTERN+")?";
Jsonix.Schema.XSD.Calendar.GMONTH_DAY_PATTERN="("+Jsonix.Schema.XSD.Calendar.SINGLE_MONTH_PATTERN+")-("+Jsonix.Schema.XSD.Calendar.DAY_PATTERN+")("+Jsonix.Schema.XSD.Calendar.TIMEZONE_PATTERN+")?";
Jsonix.Schema.XSD.Calendar.DATE_PART_PATTERN="("+Jsonix.Schema.XSD.Calendar.YEAR_PATTERN+")-("+Jsonix.Schema.XSD.Calendar.MONTH_PATTERN+")-("+Jsonix.Schema.XSD.Calendar.DAY_PATTERN+")";
Jsonix.Schema.XSD.Calendar.TIME_PART_PATTERN="([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])(\\.([0-9]+))?";
Jsonix.Schema.XSD.Calendar.TIME_PATTERN=Jsonix.Schema.XSD.Calendar.TIME_PART_PATTERN+"("+Jsonix.Schema.XSD.Calendar.TIMEZONE_PATTERN+")?";
Jsonix.Schema.XSD.Calendar.DATE_PATTERN=Jsonix.Schema.XSD.Calendar.DATE_PART_PATTERN+"("+Jsonix.Schema.XSD.Calendar.TIMEZONE_PATTERN+")?";
Jsonix.Schema.XSD.Calendar.DATETIME_PATTERN=Jsonix.Schema.XSD.Calendar.DATE_PART_PATTERN+"T"+Jsonix.Schema.XSD.Calendar.TIME_PART_PATTERN+"("+Jsonix.Schema.XSD.Calendar.TIMEZONE_PATTERN+")?";
Jsonix.Schema.XSD.Calendar.INSTANCE=new Jsonix.Schema.XSD.Calendar();
Jsonix.Schema.XSD.Calendar.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.Calendar.INSTANCE);
Jsonix.Schema.XSD.Duration=Jsonix.Class(Jsonix.Schema.XSD.AnySimpleType,{name:"Duration",typeName:Jsonix.Schema.XSD.qname("duration"),isInstance:function(f,e,d){return Jsonix.Util.Type.isObject(f)&&((Jsonix.Util.Type.exists(f.sign)?(f.sign===-1||f.sign===1):true)(Jsonix.Util.NumberUtils.isInteger(f.years)&&f.years>=0)||(Jsonix.Util.NumberUtils.isInteger(f.months)&&f.months>=0)||(Jsonix.Util.NumberUtils.isInteger(f.days)&&f.days>=0)||(Jsonix.Util.NumberUtils.isInteger(f.hours)&&f.hours>=0)||(Jsonix.Util.NumberUtils.isInteger(f.minutes)&&f.minutes>=0)||(Jsonix.Util.Type.isNumber(f.seconds)&&f.seconds>=0))
},validate:function(h){Jsonix.Util.Ensure.ensureObject(h);
if(Jsonix.Util.Type.exists(h.sign)){if(!(h.sign===1||h.sign===-1)){throw new Error("Sign of the duration ["+h.sign+"] must be either [1] or [-1].")
}}var e=true;
var g=function(b,a){if(Jsonix.Util.Type.exists(b)){if(!(Jsonix.Util.NumberUtils.isInteger(b)&&b>=0)){throw new Error(a.replace("{0}",b))
}else{return true
}}else{return false
}};
var f=function(b,a){if(Jsonix.Util.Type.exists(b)){if(!(Jsonix.Util.Type.isNumber(b)&&b>=0)){throw new Error(a.replace("{0}",b))
}else{return true
}}else{return false
}};
e=e&&!g(h.years,"Number of years [{0}] must be an unsigned integer.");
e=e&&!g(h.months,"Number of months [{0}] must be an unsigned integer.");
e=e&&!g(h.days,"Number of days [{0}] must be an unsigned integer.");
e=e&&!g(h.hours,"Number of hours [{0}] must be an unsigned integer.");
e=e&&!g(h.minutes,"Number of minutes [{0}] must be an unsigned integer.");
e=e&&!f(h.seconds,"Number of seconds [{0}] must be an unsigned number.");
if(e){throw new Error("At least one of the components (years, months, days, hours, minutes, seconds) must be set.")
}},print:function(h,j,f,i){this.validate(h);
var g="";
if(h.sign===-1){g+="-"
}g+="P";
if(Jsonix.Util.Type.exists(h.years)){g+=(h.years+"Y")
}if(Jsonix.Util.Type.exists(h.months)){g+=(h.months+"M")
}if(Jsonix.Util.Type.exists(h.days)){g+=(h.days+"D")
}if(Jsonix.Util.Type.exists(h.hours)||Jsonix.Util.Type.exists(h.minutes)||Jsonix.Util.Type.exists(h.seconds)){g+="T";
if(Jsonix.Util.Type.exists(h.hours)){g+=(h.hours+"H")
}if(Jsonix.Util.Type.exists(h.minutes)){g+=(h.minutes+"M")
}if(Jsonix.Util.Type.exists(h.seconds)){g+=(h.seconds+"S")
}}return g
},parse:function(m,p,j,o){var k=new RegExp("^"+Jsonix.Schema.XSD.Duration.PATTERN+"$");
var i=m.match(k);
if(i!==null){var n=true;
var l={};
if(i[1]){l.sign=-1
}if(i[3]){l.years=parseInt(i[3],10);
n=false
}if(i[5]){l.months=parseInt(i[5],10);
n=false
}if(i[7]){l.days=parseInt(i[7],10);
n=false
}if(i[10]){l.hours=parseInt(i[10],10);
n=false
}if(i[12]){l.minutes=parseInt(i[12],10);
n=false
}if(i[14]){l.seconds=Number(i[14]);
n=false
}return l
}else{throw new Error("Value ["+m+"] does not match the duration pattern.")
}},CLASS_NAME:"Jsonix.Schema.XSD.Duration"});
Jsonix.Schema.XSD.Duration.PATTERN="(-)?P(([0-9]+)Y)?(([0-9]+)M)?(([0-9]+)D)?(T(([0-9]+)H)?(([0-9]+)M)?(([0-9]+(\\.[0-9]+)?)S)?)?";
Jsonix.Schema.XSD.Duration.INSTANCE=new Jsonix.Schema.XSD.Duration();
Jsonix.Schema.XSD.Duration.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.Duration.INSTANCE);
Jsonix.Schema.XSD.DateTime=Jsonix.Class(Jsonix.Schema.XSD.Calendar,{name:"DateTime",typeName:Jsonix.Schema.XSD.qname("dateTime"),parse:function(g,e,f,h){return this.parseDateTime(g)
},print:function(g,e,f,h){return this.printDateTime(g)
},CLASS_NAME:"Jsonix.Schema.XSD.DateTime"});
Jsonix.Schema.XSD.DateTime.INSTANCE=new Jsonix.Schema.XSD.DateTime();
Jsonix.Schema.XSD.DateTime.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.DateTime.INSTANCE);
Jsonix.Schema.XSD.DateTimeAsDate=Jsonix.Class(Jsonix.Schema.XSD.Calendar,{name:"DateTimeAsDate",typeName:Jsonix.Schema.XSD.qname("dateTime"),parse:function(n,t,o,l){var q=this.parseDateTime(n);
var s=new Date();
s.setFullYear(q.year);
s.setMonth(q.month-1);
s.setDate(q.day);
s.setHours(q.hour);
s.setMinutes(q.minute);
s.setSeconds(q.second);
if(Jsonix.Util.Type.isNumber(q.fractionalSecond)){s.setMilliseconds(Math.floor(1000*q.fractionalSecond))
}var p;
var r;
var m=-s.getTimezoneOffset();
if(Jsonix.Util.NumberUtils.isInteger(q.timezone)){p=q.timezone;
r=false
}else{p=m;
r=true
}var k=new Date(s.getTime()+(60000*(-p+m)));
if(r){k.originalTimezone=null
}else{k.originalTimezone=q.timezone
}return k
},print:function(k,o,i,n){Jsonix.Util.Ensure.ensureDate(k);
var m;
var l=-k.getTimezoneOffset();
var p;
if(k.originalTimezone===null){return this.printDateTime(new Jsonix.XML.Calendar({year:k.getFullYear(),month:k.getMonth()+1,day:k.getDate(),hour:k.getHours(),minute:k.getMinutes(),second:k.getSeconds(),fractionalSecond:(k.getMilliseconds()/1000)}))
}else{if(Jsonix.Util.NumberUtils.isInteger(k.originalTimezone)){m=k.originalTimezone;
p=new Date(k.getTime()-(60000*(-m+l)))
}else{m=l;
p=k
}var j=this.printDateTime(new Jsonix.XML.Calendar({year:p.getFullYear(),month:p.getMonth()+1,day:p.getDate(),hour:p.getHours(),minute:p.getMinutes(),second:p.getSeconds(),fractionalSecond:(p.getMilliseconds()/1000),timezone:m}));
return j
}},isInstance:function(f,e,d){return Jsonix.Util.Type.isDate(f)
},CLASS_NAME:"Jsonix.Schema.XSD.DateTimeAsDate"});
Jsonix.Schema.XSD.DateTimeAsDate.INSTANCE=new Jsonix.Schema.XSD.DateTimeAsDate();
Jsonix.Schema.XSD.DateTimeAsDate.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.DateTimeAsDate.INSTANCE);
Jsonix.Schema.XSD.Time=Jsonix.Class(Jsonix.Schema.XSD.Calendar,{name:"Time",typeName:Jsonix.Schema.XSD.qname("time"),parse:function(g,e,f,h){return this.parseTime(g)
},print:function(g,e,f,h){return this.printTime(g)
},CLASS_NAME:"Jsonix.Schema.XSD.Time"});
Jsonix.Schema.XSD.Time.INSTANCE=new Jsonix.Schema.XSD.Time();
Jsonix.Schema.XSD.Time.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.Time.INSTANCE);
Jsonix.Schema.XSD.TimeAsDate=Jsonix.Class(Jsonix.Schema.XSD.Calendar,{name:"TimeAsDate",typeName:Jsonix.Schema.XSD.qname("time"),parse:function(n,t,o,l){var q=this.parseTime(n);
var s=new Date();
s.setFullYear(1970);
s.setMonth(0);
s.setDate(1);
s.setHours(q.hour);
s.setMinutes(q.minute);
s.setSeconds(q.second);
if(Jsonix.Util.Type.isNumber(q.fractionalSecond)){s.setMilliseconds(Math.floor(1000*q.fractionalSecond))
}var p;
var r;
var m=-s.getTimezoneOffset();
if(Jsonix.Util.NumberUtils.isInteger(q.timezone)){p=q.timezone;
r=false
}else{p=m;
r=true
}var k=new Date(s.getTime()+(60000*(-p+m)));
if(r){k.originalTimezone=null
}else{k.originalTimezone=p
}return k
},print:function(n,u,s,l){Jsonix.Util.Ensure.ensureDate(n);
var r=n.getTime();
if(r<=-86400000&&r>=86400000){throw new Error("Invalid time ["+n+"].")
}if(n.originalTimezone===null){return this.printTime(new Jsonix.XML.Calendar({hour:n.getHours(),minute:n.getMinutes(),second:n.getSeconds(),fractionalSecond:(n.getMilliseconds()/1000)}))
}else{var t;
var p;
var m=-n.getTimezoneOffset();
if(Jsonix.Util.NumberUtils.isInteger(n.originalTimezone)){p=n.originalTimezone;
t=new Date(n.getTime()-(60000*(-p+m)))
}else{p=m;
t=n
}var o=t.getTime();
if(o>=(-m*60000)){return this.printTime(new Jsonix.XML.Calendar({hour:t.getHours(),minute:t.getMinutes(),second:t.getSeconds(),fractionalSecond:(t.getMilliseconds()/1000),timezone:p}))
}else{var q=Math.ceil(-o/3600000);
var v=t.getSeconds()+t.getMinutes()*60+t.getHours()*3600+q*3600-p*60;
return this.printTime(new Jsonix.XML.Calendar({hour:v%86400,minute:v%3600,second:v%60,fractionalSecond:(t.getMilliseconds()/1000),timezone:q*60}))
}}},isInstance:function(f,e,d){return Jsonix.Util.Type.isDate(f)&&f.getTime()>-86400000&&f.getTime()<86400000
},CLASS_NAME:"Jsonix.Schema.XSD.TimeAsDate"});
Jsonix.Schema.XSD.TimeAsDate.INSTANCE=new Jsonix.Schema.XSD.TimeAsDate();
Jsonix.Schema.XSD.TimeAsDate.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.TimeAsDate.INSTANCE);
Jsonix.Schema.XSD.Date=Jsonix.Class(Jsonix.Schema.XSD.Calendar,{name:"Date",typeName:Jsonix.Schema.XSD.qname("date"),parse:function(g,e,f,h){return this.parseDate(g)
},print:function(g,e,f,h){return this.printDate(g)
},CLASS_NAME:"Jsonix.Schema.XSD.Date"});
Jsonix.Schema.XSD.Date.INSTANCE=new Jsonix.Schema.XSD.Date();
Jsonix.Schema.XSD.Date.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.Date.INSTANCE);
Jsonix.Schema.XSD.DateAsDate=Jsonix.Class(Jsonix.Schema.XSD.Calendar,{name:"DateAsDate",typeName:Jsonix.Schema.XSD.qname("date"),parse:function(n,t,o,l){var q=this.parseDate(n);
var s=new Date();
s.setFullYear(q.year);
s.setMonth(q.month-1);
s.setDate(q.day);
s.setHours(0);
s.setMinutes(0);
s.setSeconds(0);
s.setMilliseconds(0);
if(Jsonix.Util.Type.isNumber(q.fractionalSecond)){s.setMilliseconds(Math.floor(1000*q.fractionalSecond))
}var p;
var r;
var m=-s.getTimezoneOffset();
if(Jsonix.Util.NumberUtils.isInteger(q.timezone)){p=q.timezone;
r=false
}else{p=m;
r=true
}var k=new Date(s.getTime()+(60000*(-p+m)));
if(r){k.originalTimezone=null
}else{k.originalTimezone=p
}return k
},print:function(l,r,p,j){Jsonix.Util.Ensure.ensureDate(l);
var o=new Date(l.getTime());
o.setHours(0);
o.setMinutes(0);
o.setSeconds(0);
o.setMilliseconds(0);
if(l.originalTimezone===null){return this.printDate(new Jsonix.XML.Calendar({year:l.getFullYear(),month:l.getMonth()+1,day:l.getDate()}))
}else{if(Jsonix.Util.NumberUtils.isInteger(l.originalTimezone)){var q=new Date(l.getTime()-(60000*(-l.originalTimezone-l.getTimezoneOffset())));
return this.printDate(new Jsonix.XML.Calendar({year:q.getFullYear(),month:q.getMonth()+1,day:q.getDate(),timezone:l.originalTimezone}))
}else{var k=-l.getTime()+o.getTime();
if(k===0){return this.printDate(new Jsonix.XML.Calendar({year:l.getFullYear(),month:l.getMonth()+1,day:l.getDate()}))
}else{var n=k-(60000*l.getTimezoneOffset());
if(n>=-43200000){return this.printDate(new Jsonix.XML.Calendar({year:l.getFullYear(),month:l.getMonth()+1,day:l.getDate(),timezone:Math.floor(n/60000)}))
}else{var m=new Date(l.getTime()+86400000);
return this.printDate(new Jsonix.XML.Calendar({year:m.getFullYear(),month:m.getMonth()+1,day:m.getDate(),timezone:(Math.floor(n/60000)+1440)}))
}}}}},isInstance:function(f,e,d){return Jsonix.Util.Type.isDate(f)
},CLASS_NAME:"Jsonix.Schema.XSD.DateAsDate"});
Jsonix.Schema.XSD.DateAsDate.INSTANCE=new Jsonix.Schema.XSD.DateAsDate();
Jsonix.Schema.XSD.DateAsDate.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.DateAsDate.INSTANCE);
Jsonix.Schema.XSD.GYearMonth=Jsonix.Class(Jsonix.Schema.XSD.Calendar,{name:"GYearMonth",typeName:Jsonix.Schema.XSD.qname("gYearMonth"),CLASS_NAME:"Jsonix.Schema.XSD.GYearMonth",parse:function(g,e,f,h){return this.parseGYearMonth(g,e,f,h)
},print:function(g,e,f,h){return this.printGYearMonth(g,e,f,h)
}});
Jsonix.Schema.XSD.GYearMonth.INSTANCE=new Jsonix.Schema.XSD.GYearMonth();
Jsonix.Schema.XSD.GYearMonth.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.GYearMonth.INSTANCE);
Jsonix.Schema.XSD.GYear=Jsonix.Class(Jsonix.Schema.XSD.Calendar,{name:"GYear",typeName:Jsonix.Schema.XSD.qname("gYear"),CLASS_NAME:"Jsonix.Schema.XSD.GYear",parse:function(g,e,f,h){return this.parseGYear(g,e,f,h)
},print:function(g,e,f,h){return this.printGYear(g,e,f,h)
}});
Jsonix.Schema.XSD.GYear.INSTANCE=new Jsonix.Schema.XSD.GYear();
Jsonix.Schema.XSD.GYear.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.GYear.INSTANCE);
Jsonix.Schema.XSD.GMonthDay=Jsonix.Class(Jsonix.Schema.XSD.Calendar,{name:"GMonthDay",typeName:Jsonix.Schema.XSD.qname("gMonthDay"),CLASS_NAME:"Jsonix.Schema.XSD.GMonthDay",parse:function(g,e,f,h){return this.parseGMonthDay(g,e,f,h)
},print:function(g,e,f,h){return this.printGMonthDay(g,e,f,h)
}});
Jsonix.Schema.XSD.GMonthDay.INSTANCE=new Jsonix.Schema.XSD.GMonthDay();
Jsonix.Schema.XSD.GMonthDay.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.GMonthDay.INSTANCE);
Jsonix.Schema.XSD.GDay=Jsonix.Class(Jsonix.Schema.XSD.Calendar,{name:"GDay",typeName:Jsonix.Schema.XSD.qname("gDay"),CLASS_NAME:"Jsonix.Schema.XSD.GDay",parse:function(g,e,f,h){return this.parseGDay(g,e,f,h)
},print:function(g,e,f,h){return this.printGDay(g,e,f,h)
}});
Jsonix.Schema.XSD.GDay.INSTANCE=new Jsonix.Schema.XSD.GDay();
Jsonix.Schema.XSD.GDay.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.GDay.INSTANCE);
Jsonix.Schema.XSD.GMonth=Jsonix.Class(Jsonix.Schema.XSD.Calendar,{name:"GMonth",typeName:Jsonix.Schema.XSD.qname("gMonth"),CLASS_NAME:"Jsonix.Schema.XSD.GMonth",parse:function(g,e,f,h){return this.parseGMonth(g,e,f,h)
},print:function(g,e,f,h){return this.printGMonth(g,e,f,h)
}});
Jsonix.Schema.XSD.GMonth.INSTANCE=new Jsonix.Schema.XSD.GMonth();
Jsonix.Schema.XSD.GMonth.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.GMonth.INSTANCE);
Jsonix.Schema.XSD.ID=Jsonix.Class(Jsonix.Schema.XSD.String,{name:"ID",typeName:Jsonix.Schema.XSD.qname("ID"),CLASS_NAME:"Jsonix.Schema.XSD.ID"});
Jsonix.Schema.XSD.ID.INSTANCE=new Jsonix.Schema.XSD.ID();
Jsonix.Schema.XSD.ID.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.ID.INSTANCE);
Jsonix.Schema.XSD.IDREF=Jsonix.Class(Jsonix.Schema.XSD.String,{name:"IDREF",typeName:Jsonix.Schema.XSD.qname("IDREF"),CLASS_NAME:"Jsonix.Schema.XSD.IDREF"});
Jsonix.Schema.XSD.IDREF.INSTANCE=new Jsonix.Schema.XSD.IDREF();
Jsonix.Schema.XSD.IDREF.INSTANCE.LIST=new Jsonix.Schema.XSD.List(Jsonix.Schema.XSD.IDREF.INSTANCE);
Jsonix.Schema.XSD.IDREFS=Jsonix.Class(Jsonix.Schema.XSD.List,{name:"IDREFS",initialize:function(){Jsonix.Schema.XSD.List.prototype.initialize.apply(this,[Jsonix.Schema.XSD.IDREF.INSTANCE,Jsonix.Schema.XSD.qname("IDREFS")," "])
},CLASS_NAME:"Jsonix.Schema.XSD.IDREFS"});
Jsonix.Schema.XSD.IDREFS.INSTANCE=new Jsonix.Schema.XSD.IDREFS();
Jsonix.Schema.XSI={};
Jsonix.Schema.XSI.NAMESPACE_URI="http://www.w3.org/2001/XMLSchema-instance";
Jsonix.Schema.XSI.PREFIX="xsi";
Jsonix.Schema.XSI.TYPE="type";
Jsonix.Schema.XSI.NIL="nil";
Jsonix.Schema.XSI.qname=function(b){Jsonix.Util.Ensure.ensureString(b);
return new Jsonix.XML.QName(Jsonix.Schema.XSI.NAMESPACE_URI,b,Jsonix.Schema.XSI.PREFIX)
};
Jsonix.Schema.XSI.TYPE_QNAME=Jsonix.Schema.XSI.qname(Jsonix.Schema.XSI.TYPE);
Jsonix.Context=Jsonix.Class(Jsonix.Mapping.Styled,{modules:[],typeInfos:null,typeNameKeyToTypeInfo:null,elementInfos:null,options:null,substitutionMembersMap:null,scopedElementInfosMap:null,supportXsiType:true,initialize:function(i,l){Jsonix.Mapping.Styled.prototype.initialize.apply(this,[l]);
this.modules=[];
this.elementInfos=[];
this.typeInfos={};
this.typeNameKeyToTypeInfo={};
this.registerBuiltinTypeInfos();
this.namespacePrefixes={};
this.prefixNamespaces={};
this.substitutionMembersMap={};
this.scopedElementInfosMap={};
if(Jsonix.Util.Type.exists(l)){Jsonix.Util.Ensure.ensureObject(l);
if(Jsonix.Util.Type.isObject(l.namespacePrefixes)){this.namespacePrefixes=Jsonix.Util.Type.cloneObject(l.namespacePrefixes,{})
}if(Jsonix.Util.Type.isBoolean(l.supportXsiType)){this.supportXsiType=l.supportXsiType
}}for(var j in this.namespacePrefixes){if(this.namespacePrefixes.hasOwnProperty(j)){p=this.namespacePrefixes[j];
this.prefixNamespaces[p]=j
}}if(Jsonix.Util.Type.exists(i)){Jsonix.Util.Ensure.ensureArray(i);
var g,h,k;
for(g=0;
g<i.length;
g++){h=i[g];
k=this.createModule(h);
this.modules[g]=k
}}this.processModules()
},createModule:function(d){var c;
if(d instanceof this.mappingStyle.module){c=d
}else{d=Jsonix.Util.Type.cloneObject(d);
c=new this.mappingStyle.module(d,{mappingStyle:this.mappingStyle})
}return c
},registerBuiltinTypeInfos:function(){for(var b=0;
b<this.builtinTypeInfos.length;
b++){this.registerTypeInfo(this.builtinTypeInfos[b])
}},processModules:function(){var d,c;
for(d=0;
d<this.modules.length;
d++){c=this.modules[d];
c.registerTypeInfos(this)
}for(d=0;
d<this.modules.length;
d++){c=this.modules[d];
c.registerElementInfos(this)
}for(d=0;
d<this.modules.length;
d++){c=this.modules[d];
c.buildTypeInfos(this)
}for(d=0;
d<this.modules.length;
d++){c=this.modules[d];
c.buildElementInfos(this)
}},registerTypeInfo:function(d){Jsonix.Util.Ensure.ensureObject(d);
var c=d.name||d.n||null;
Jsonix.Util.Ensure.ensureString(c);
this.typeInfos[c]=d;
if(d.typeName&&d.typeName.key){this.typeNameKeyToTypeInfo[d.typeName.key]=d
}},resolveTypeInfo:function(f,j){if(!Jsonix.Util.Type.exists(f)){return null
}else{if(f instanceof Jsonix.Model.TypeInfo){return f
}else{if(Jsonix.Util.Type.isString(f)){var g;
if(f.length>0&&f.charAt(0)==="."){var h=j.name||j.n||undefined;
Jsonix.Util.Ensure.ensureObject(j,"Type info mapping can only be resolved if module is provided.");
Jsonix.Util.Ensure.ensureString(h,"Type info mapping can only be resolved if module name is provided.");
g=h+f
}else{g=f
}if(!this.typeInfos[g]){throw new Error("Type info ["+g+"] is not known in this context.")
}else{return this.typeInfos[g]
}}else{Jsonix.Util.Ensure.ensureObject(j,"Type info mapping can only be resolved if module is provided.");
var i=j.createTypeInfo(f);
i.build(this,j);
return i
}}}},registerElementInfo:function(k,m){Jsonix.Util.Ensure.ensureObject(k);
this.elementInfos.push(k);
if(Jsonix.Util.Type.exists(k.substitutionHead)){var n=k.substitutionHead;
var l=n.key;
var h=this.substitutionMembersMap[l];
if(!Jsonix.Util.Type.isArray(h)){h=[];
this.substitutionMembersMap[l]=h
}h.push(k)
}var i;
if(Jsonix.Util.Type.exists(k.scope)){i=this.resolveTypeInfo(k.scope,m).name
}else{i="##global"
}var j=this.scopedElementInfosMap[i];
if(!Jsonix.Util.Type.isObject(j)){j={};
this.scopedElementInfosMap[i]=j
}j[k.elementName.key]=k
},getTypeInfoByValue:function(f){if(!Jsonix.Util.Type.exists(f)){return undefined
}if(Jsonix.Util.Type.isObject(f)){var d=f.TYPE_NAME;
if(Jsonix.Util.Type.isString(d)){var e=this.getTypeInfoByName(d);
if(e){return e
}}}return undefined
},getTypeInfoByName:function(b){return this.typeInfos[b]
},getTypeInfoByTypeName:function(c){var d=Jsonix.XML.QName.fromObjectOrString(c,this);
return this.typeNameKeyToTypeInfo[d.key]
},getTypeInfoByTypeNameKey:function(b){return this.typeNameKeyToTypeInfo[b]
},getElementInfo:function(o,m){if(Jsonix.Util.Type.exists(m)){var j=m.name;
var k=this.scopedElementInfosMap[j];
if(Jsonix.Util.Type.exists(k)){var n=k[o.key];
if(Jsonix.Util.Type.exists(n)){return n
}}}var l="##global";
var q=this.scopedElementInfosMap[l];
if(Jsonix.Util.Type.exists(q)){var i=q[o.key];
if(Jsonix.Util.Type.exists(i)){return i
}}return null
},getSubstitutionMembers:function(b){return this.substitutionMembersMap[Jsonix.XML.QName.fromObject(b).key]
},createMarshaller:function(){return new this.mappingStyle.marshaller(this)
},createUnmarshaller:function(){return new this.mappingStyle.unmarshaller(this)
},getNamespaceURI:function(b){Jsonix.Util.Ensure.ensureString(b);
return this.prefixNamespaces[b]
},getPrefix:function(e,d){Jsonix.Util.Ensure.ensureString(e);
var f=this.namespacePrefixes[e];
if(Jsonix.Util.Type.isString(f)){return f
}else{return d
}},builtinTypeInfos:[Jsonix.Schema.XSD.AnyType.INSTANCE,Jsonix.Schema.XSD.AnySimpleType.INSTANCE,Jsonix.Schema.XSD.AnyURI.INSTANCE,Jsonix.Schema.XSD.Base64Binary.INSTANCE,Jsonix.Schema.XSD.Boolean.INSTANCE,Jsonix.Schema.XSD.Byte.INSTANCE,Jsonix.Schema.XSD.Calendar.INSTANCE,Jsonix.Schema.XSD.DateAsDate.INSTANCE,Jsonix.Schema.XSD.Date.INSTANCE,Jsonix.Schema.XSD.DateTimeAsDate.INSTANCE,Jsonix.Schema.XSD.DateTime.INSTANCE,Jsonix.Schema.XSD.Decimal.INSTANCE,Jsonix.Schema.XSD.Double.INSTANCE,Jsonix.Schema.XSD.Duration.INSTANCE,Jsonix.Schema.XSD.Float.INSTANCE,Jsonix.Schema.XSD.GDay.INSTANCE,Jsonix.Schema.XSD.GMonth.INSTANCE,Jsonix.Schema.XSD.GMonthDay.INSTANCE,Jsonix.Schema.XSD.GYear.INSTANCE,Jsonix.Schema.XSD.GYearMonth.INSTANCE,Jsonix.Schema.XSD.HexBinary.INSTANCE,Jsonix.Schema.XSD.ID.INSTANCE,Jsonix.Schema.XSD.IDREF.INSTANCE,Jsonix.Schema.XSD.IDREFS.INSTANCE,Jsonix.Schema.XSD.Int.INSTANCE,Jsonix.Schema.XSD.Integer.INSTANCE,Jsonix.Schema.XSD.Language.INSTANCE,Jsonix.Schema.XSD.Long.INSTANCE,Jsonix.Schema.XSD.Name.INSTANCE,Jsonix.Schema.XSD.NCName.INSTANCE,Jsonix.Schema.XSD.NegativeInteger.INSTANCE,Jsonix.Schema.XSD.NMToken.INSTANCE,Jsonix.Schema.XSD.NMTokens.INSTANCE,Jsonix.Schema.XSD.NonNegativeInteger.INSTANCE,Jsonix.Schema.XSD.NonPositiveInteger.INSTANCE,Jsonix.Schema.XSD.NormalizedString.INSTANCE,Jsonix.Schema.XSD.Number.INSTANCE,Jsonix.Schema.XSD.PositiveInteger.INSTANCE,Jsonix.Schema.XSD.QName.INSTANCE,Jsonix.Schema.XSD.Short.INSTANCE,Jsonix.Schema.XSD.String.INSTANCE,Jsonix.Schema.XSD.Strings.INSTANCE,Jsonix.Schema.XSD.TimeAsDate.INSTANCE,Jsonix.Schema.XSD.Time.INSTANCE,Jsonix.Schema.XSD.Token.INSTANCE,Jsonix.Schema.XSD.UnsignedByte.INSTANCE,Jsonix.Schema.XSD.UnsignedInt.INSTANCE,Jsonix.Schema.XSD.UnsignedLong.INSTANCE,Jsonix.Schema.XSD.UnsignedShort.INSTANCE],CLASS_NAME:"Jsonix.Context"});
	// Complete Jsonix script is included above
	return { Jsonix: Jsonix };
};

// If the require function exists ...
if (typeof require === 'function') {
	// ... but the define function does not exists
	if (typeof define !== 'function') {
		// Load the define function via amdefine
		var define = require('amdefine')(module);
		// If we're not in browser
		if (typeof window === 'undefined')
		{
			// Require xmldom, xmlhttprequest and fs
			define(["xmldom", "xmlhttprequest", "fs"], _jsonix_factory);
		}
		else
		{
			// We're probably in browser, maybe browserify
			// Do not require xmldom, xmlhttprequest as they'r provided by the browser
			// Do not require fs since file system is not available anyway
			define([], _jsonix_factory);
		}
	}
	else {
		// Otherwise assume we're in the browser/RequireJS environment
		// Load the module without xmldom and xmlhttprequests dependencies
		define([], _jsonix_factory);
	}
}
// If the require function does not exists, we're not in Node.js and therefore in browser environment
else
{
	// Just call the factory and set Jsonix as global.
	var Jsonix = _jsonix_factory().Jsonix;
}
