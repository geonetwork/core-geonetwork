/*
 * Ext JS Library 0.30
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.namespace('Ext.air','Ext.sql');Ext.Template.prototype.compile=function(){var fm=Ext.util.Format;var useF=this.disableFormats!==true;var prevOffset=0;var arr=[];var tpl=this;var fn=function(m,name,format,args,offset,s){if(prevOffset!=offset){var action={type:1,value:s.substr(prevOffset,offset-prevOffset)};arr.push(action);}
prevOffset=offset+m.length;if(format&&useF){if(args){var re=/^\s*['"](.*)["']\s*$/;args=args.split(/,(?=(?:[^"]*"[^"]*")*(?![^"]*"))/);for(var i=0,len=args.length;i<len;i++){args[i]=args[i].replace(re,"$1");}
args=[''].concat(args);}else{args=[''];}
if(format.substr(0,5)!="this."){var action={type:3,value:name,format:fm[format],args:args,scope:fm};arr.push(action);}else{var action={type:3,value:name,format:tpl[format.substr(5)],args:args,scope:tpl};arr.push(action);}}else{var action={type:2,value:name};arr.push(action);}
return m;};var s=this.html.replace(this.re,fn);if(prevOffset!=(s.length-1)){var action={type:1,value:s.substr(prevOffset,s.length-prevOffset)};arr.push(action);}
this.compiled=function(values){function applyValues(el){switch(el.type){case 1:return el.value;case 2:return(values[el.value]?values[el.value]:'');default:el.args[0]=values[el.value];return el.format.apply(el.scope,el.args);}}
return arr.map(applyValues).join('');}
return this;};Ext.Template.prototype.call=function(fnName,value,allValues){return this[fnName](value,allValues);}
Ext.DomQuery=function(){var cache={},simpleCache={},valueCache={};var nonSpace=/\S/;var trimRe=/^\s+|\s+$/g;var tplRe=/\{(\d+)\}/g;var modeRe=/^(\s?[\/>+~]\s?|\s|$)/;var tagTokenRe=/^(#)?([\w-\*]+)/;var nthRe=/(\d*)n\+?(\d*)/,nthRe2=/\D/;function child(p,index){var i=0;var n=p.firstChild;while(n){if(n.nodeType==1){if(++i==index){return n;}}
n=n.nextSibling;}
return null;};function next(n){while((n=n.nextSibling)&&n.nodeType!=1);return n;};function prev(n){while((n=n.previousSibling)&&n.nodeType!=1);return n;};function children(d){var n=d.firstChild,ni=-1;while(n){var nx=n.nextSibling;if(n.nodeType==3&&!nonSpace.test(n.nodeValue)){d.removeChild(n);}else{n.nodeIndex=++ni;}
n=nx;}
return this;};function byClassName(c,a,v){if(!v){return c;}
var r=[],ri=-1,cn;for(var i=0,ci;ci=c[i];i++){if((' '+ci.className+' ').indexOf(v)!=-1){r[++ri]=ci;}}
return r;};function attrValue(n,attr){if(!n.tagName&&typeof n.length!="undefined"){n=n[0];}
if(!n){return null;}
if(attr=="for"){return n.htmlFor;}
if(attr=="class"||attr=="className"){return n.className;}
return n.getAttribute(attr)||n[attr];};function getNodes(ns,mode,tagName){var result=[],ri=-1,cs;if(!ns){return result;}
tagName=tagName||"*";if(typeof ns.getElementsByTagName!="undefined"){ns=[ns];}
if(!mode){for(var i=0,ni;ni=ns[i];i++){cs=ni.getElementsByTagName(tagName);for(var j=0,ci;ci=cs[j];j++){result[++ri]=ci;}}}else if(mode=="/"||mode==">"){var utag=tagName.toUpperCase();for(var i=0,ni,cn;ni=ns[i];i++){cn=ni.children||ni.childNodes;for(var j=0,cj;cj=cn[j];j++){if(cj.nodeName==utag||cj.nodeName==tagName||tagName=='*'){result[++ri]=cj;}}}}else if(mode=="+"){var utag=tagName.toUpperCase();for(var i=0,n;n=ns[i];i++){while((n=n.nextSibling)&&n.nodeType!=1);if(n&&(n.nodeName==utag||n.nodeName==tagName||tagName=='*')){result[++ri]=n;}}}else if(mode=="~"){for(var i=0,n;n=ns[i];i++){while((n=n.nextSibling)&&(n.nodeType!=1||(tagName=='*'||n.tagName.toLowerCase()!=tagName)));if(n){result[++ri]=n;}}}
return result;};function concat(a,b){if(b.slice){return a.concat(b);}
for(var i=0,l=b.length;i<l;i++){a[a.length]=b[i];}
return a;}
function byTag(cs,tagName){if(cs.tagName||cs==document){cs=[cs];}
if(!tagName){return cs;}
var r=[],ri=-1;tagName=tagName.toLowerCase();for(var i=0,ci;ci=cs[i];i++){if(ci.nodeType==1&&ci.tagName.toLowerCase()==tagName){r[++ri]=ci;}}
return r;};function byId(cs,attr,id){if(cs.tagName||cs==document){cs=[cs];}
if(!id){return cs;}
var r=[],ri=-1;for(var i=0,ci;ci=cs[i];i++){if(ci&&ci.id==id){r[++ri]=ci;return r;}}
return r;};function byAttribute(cs,attr,value,op,custom){var r=[],ri=-1,st=custom=="{";var f=Ext.DomQuery.operators[op];for(var i=0,ci;ci=cs[i];i++){var a;if(st){a=Ext.DomQuery.getStyle(ci,attr);}
else if(attr=="class"||attr=="className"){a=ci.className;}else if(attr=="for"){a=ci.htmlFor;}else if(attr=="href"){a=ci.getAttribute("href",2);}else{a=ci.getAttribute(attr);}
if((f&&f(a,value))||(!f&&a)){r[++ri]=ci;}}
return r;};function byPseudo(cs,name,value){return Ext.DomQuery.pseudos[name](cs,value);};eval("var batch = 30803;");var key=30803;function nodup(cs){if(!cs){return[];}
var len=cs.length,c,i,r=cs,cj,ri=-1;if(!len||typeof cs.nodeType!="undefined"||len==1){return cs;}
var d=++key;cs[0]._nodup=d;for(i=1;c=cs[i];i++){if(c._nodup!=d){c._nodup=d;}else{r=[];for(var j=0;j<i;j++){r[++ri]=cs[j];}
for(j=i+1;cj=cs[j];j++){if(cj._nodup!=d){cj._nodup=d;r[++ri]=cj;}}
return r;}}
return r;}
function quickDiff(c1,c2){var len1=c1.length;if(!len1){return c2;}
var d=++key;for(var i=0;i<len1;i++){c1[i]._qdiff=d;}
var r=[];for(var i=0,len=c2.length;i<len;i++){if(c2[i]._qdiff!=d){r[r.length]=c2[i];}}
return r;}
function quickId(ns,mode,root,id){if(ns==root){var d=root.ownerDocument||root;return d.getElementById(id);}
ns=getNodes(ns,mode,"*");return byId(ns,null,id);}
function search(path,root,type){type=type||"select";var n=root||document;var q=path,mode,lq;var tk=Ext.DomQuery.matchers;var tklen=tk.length;var mm;var lmode=q.match(modeRe);if(lmode&&lmode[1]){mode=lmode[1].replace(trimRe,"");q=q.replace(lmode[1],"");}
while(path.substr(0,1)=="/"){path=path.substr(1);}
while(q&&lq!=q){lq=q;var tm=q.match(tagTokenRe);if(type=="select"){if(tm){if(tm[1]=="#"){n=quickId(n,mode,root,tm[2]);}else{n=getNodes(n,mode,tm[2]);}
q=q.replace(tm[0],"");}else if(q.substr(0,1)!='@'){n=getNodes(n,mode,"*");}}else{if(tm){if(tm[1]=="#"){n=byId(n,null,tm[2]);}else{n=byTag(n,tm[2]);}
q=q.replace(tm[0],"");}}
while(!(mm=q.match(modeRe))){var matched=false;for(var j=0;j<tklen;j++){var t=tk[j];var m=q.match(t.re);if(m){switch(j){case 0:n=byClassName(n,null," "+m[1]+" ");break;case 1:n=byPseudo(n,m[1],m[2]);break;case 2:n=byAttribute(n,m[2],m[4],m[3],m[1]);break;case 3:n=byId(n,null,m[1]);break;case 4:return{firstChild:{nodeValue:attrValue(n,m[1])}};}
q=q.replace(m[0],"");matched=true;break;}}
if(!matched){throw'Error parsing selector, parsing failed at "'+q+'"';}}
if(mm[1]){mode=mm[1].replace(trimRe,"");q=q.replace(mm[1],"");}}
return nodup(n);}
return{getStyle:function(el,name){return Ext.fly(el).getStyle(name);},compile:function(path,type){return function(root){return search(path,root,type);}},select:function(path,root,type){if(!root||root==document){root=document;}
if(typeof root=="string"){root=document.getElementById(root);}
var paths=path.split(",");var results=[];for(var i=0,len=paths.length;i<len;i++){var p=paths[i].replace(trimRe,"");if(!cache[p]){cache[p]=Ext.DomQuery.compile(p);if(!cache[p]){throw p+" is not a valid selector";}}
var result=cache[p](root);if(result&&result!=document){results=results.concat(result);}}
if(paths.length>1){return nodup(results);}
return results;},selectNode:function(path,root){return Ext.DomQuery.select(path,root)[0];},selectValue:function(path,root,defaultValue){path=path.replace(trimRe,"");if(!valueCache[path]){valueCache[path]=Ext.DomQuery.compile(path,"select");}
var n=valueCache[path](root);n=n[0]?n[0]:n;var v=(n&&n.firstChild?n.firstChild.nodeValue:null);return((v===null||v===undefined||v==='')?defaultValue:v);},selectNumber:function(path,root,defaultValue){var v=Ext.DomQuery.selectValue(path,root,defaultValue||0);return parseFloat(v);},is:function(el,ss){if(typeof el=="string"){el=document.getElementById(el);}
var isArray=Ext.isArray(el);var result=Ext.DomQuery.filter(isArray?el:[el],ss);return isArray?(result.length==el.length):(result.length>0);},filter:function(els,ss,nonMatches){ss=ss.replace(trimRe,"");if(!simpleCache[ss]){simpleCache[ss]=Ext.DomQuery.compile(ss,"simple");}
var result=simpleCache[ss](els);return nonMatches?quickDiff(result,els):result;},matchers:[{re:/^\.([\w-]+)/,select:'n = byClassName(n, null, " {1} ");'},{re:/^\:([\w-]+)(?:\(((?:[^\s>\/]*|.*?))\))?/,select:'n = byPseudo(n, "{1}", "{2}");'},{re:/^(?:([\[\{])(?:@)?([\w-]+)\s?(?:(=|.=)\s?['"]?(.*?)["']?)?[\]\}])/,select:'n = byAttribute(n, "{2}", "{4}", "{3}", "{1}");'},{re:/^#([\w-]+)/,select:'n = byId(n, null, "{1}");'},{re:/^@([\w-]+)/,select:'return {firstChild:{nodeValue:attrValue(n, "{1}")}};'}],operators:{"=":function(a,v){return a==v;},"!=":function(a,v){return a!=v;},"^=":function(a,v){return a&&a.substr(0,v.length)==v;},"$=":function(a,v){return a&&a.substr(a.length-v.length)==v;},"*=":function(a,v){return a&&a.indexOf(v)!==-1;},"%=":function(a,v){return(a%v)==0;},"|=":function(a,v){return a&&(a==v||a.substr(0,v.length+1)==v+'-');},"~=":function(a,v){return a&&(' '+a+' ').indexOf(' '+v+' ')!=-1;}},pseudos:{"first-child":function(c){var r=[],ri=-1,n;for(var i=0,ci;ci=n=c[i];i++){while((n=n.previousSibling)&&n.nodeType!=1);if(!n){r[++ri]=ci;}}
return r;},"last-child":function(c){var r=[],ri=-1,n;for(var i=0,ci;ci=n=c[i];i++){while((n=n.nextSibling)&&n.nodeType!=1);if(!n){r[++ri]=ci;}}
return r;},"nth-child":function(c,a){var r=[],ri=-1;var m=nthRe.exec(a=="even"&&"2n"||a=="odd"&&"2n+1"||!nthRe2.test(a)&&"n+"+a||a);var f=(m[1]||1)-0,l=m[2]-0;for(var i=0,n;n=c[i];i++){var pn=n.parentNode;if(batch!=pn._batch){var j=0;for(var cn=pn.firstChild;cn;cn=cn.nextSibling){if(cn.nodeType==1){cn.nodeIndex=++j;}}
pn._batch=batch;}
if(f==1){if(l==0||n.nodeIndex==l){r[++ri]=n;}}else if((n.nodeIndex+l)%f==0){r[++ri]=n;}}
return r;},"only-child":function(c){var r=[],ri=-1;;for(var i=0,ci;ci=c[i];i++){if(!prev(ci)&&!next(ci)){r[++ri]=ci;}}
return r;},"empty":function(c){var r=[],ri=-1;for(var i=0,ci;ci=c[i];i++){var cns=ci.childNodes,j=0,cn,empty=true;while(cn=cns[j]){++j;if(cn.nodeType==1||cn.nodeType==3){empty=false;break;}}
if(empty){r[++ri]=ci;}}
return r;},"contains":function(c,v){var r=[],ri=-1;for(var i=0,ci;ci=c[i];i++){if((ci.textContent||ci.innerText||'').indexOf(v)!=-1){r[++ri]=ci;}}
return r;},"nodeValue":function(c,v){var r=[],ri=-1;for(var i=0,ci;ci=c[i];i++){if(ci.firstChild&&ci.firstChild.nodeValue==v){r[++ri]=ci;}}
return r;},"checked":function(c){var r=[],ri=-1;for(var i=0,ci;ci=c[i];i++){if(ci.checked==true){r[++ri]=ci;}}
return r;},"not":function(c,ss){return Ext.DomQuery.filter(c,ss,true);},"any":function(c,selectors){var ss=selectors.split('|');var r=[],ri=-1,s;for(var i=0,ci;ci=c[i];i++){for(var j=0;s=ss[j];j++){if(Ext.DomQuery.is(ci,s)){r[++ri]=ci;break;}}}
return r;},"odd":function(c){return this["nth-child"](c,"odd");},"even":function(c){return this["nth-child"](c,"even");},"nth":function(c,a){return c[a-1]||[];},"first":function(c){return c[0]||[];},"last":function(c){return c[c.length-1]||[];},"has":function(c,ss){var s=Ext.DomQuery.select;var r=[],ri=-1;for(var i=0,ci;ci=c[i];i++){if(s(ss,ci).length>0){r[++ri]=ci;}}
return r;},"next":function(c,ss){var is=Ext.DomQuery.is;var r=[],ri=-1;for(var i=0,ci;ci=c[i];i++){var n=next(ci);if(n&&is(n,ss)){r[++ri]=ci;}}
return r;},"prev":function(c,ss){var is=Ext.DomQuery.is;var r=[],ri=-1;for(var i=0,ci;ci=c[i];i++){var n=prev(ci);if(n&&is(n,ss)){r[++ri]=ci;}}
return r;}}};}();Ext.query=Ext.DomQuery.select;Date.precompileFormats=function(s){var formats=s.split('|');for(var i=0,len=formats.length;i<len;i++){Date.createFormat(formats[i]);Date.createParser(formats[i]);}}
Date.precompileFormats("D n/j/Y|n/j/Y|n/j/y|m/j/y|n/d/y|m/j/Y|n/d/Y|YmdHis|F d, Y|l, F d, Y|H:i:s|g:i A|g:ia|g:iA|g:i a|g:i A|h:i|g:i|H:i|ga|ha|gA|h a|g a|g A|gi|hi|gia|hia|g|H|m/d/y|m/d/Y|m-d-y|m-d-Y|m/d|m-d|md|mdy|mdY|d|Y-m-d|Y-m-d H:i:s|d/m/y|d/m/Y|d-m-y|d-m-Y|d/m|d-m|dm|dmy|dmY|Y-m-d|l|D m/d|D m/d/Y|m/d/Y");Ext.ColorPalette.prototype.tpl=new Ext.XTemplate('<tpl for="."><a href="#" class="color-{.}" hidefocus="on"><em><span style="background:#{.}" unselectable="on">&#160;</span></em></a></tpl>');Ext.override(Ext.grid.GroupingView,{startGroup:new Ext.XTemplate('<div id="{groupId}" class="x-grid-group {cls}">','<div id="{groupId}-hd" class="x-grid-group-hd" style="{style}"><div>',this.groupTextTpl,'</div></div>','<div id="{groupId}-bd" class="x-grid-group-body">')});Ext.uniqueId=function(){var t=String(new Date().getTime()).substr(4);var s='ABCDEFGHIJKLMNOPQRSTUVWXYZ';for(var i=0;i<4;i++){t+=s.charAt(Math.floor(Math.random()*26));}
return t;};Ext.data.JsonReader.override({getJsonAccessor:function(){var re=/[\[\.]/;return function(expr){try{if(re.test(expr)){var arr=expr.split('.');var ln=arr.length;return function(obj){var l=obj;for(var i=0;i<ln;i++){l=l[arr[i]];}
return l;};}else{return function(obj){return obj[expr];};}}catch(e){Ext.air.dir(e);}
return Ext.emptyFn;};}()});

Ext.air.FileProvider=function(config){Ext.air.FileProvider.superclass.constructor.call(this);this.defaultState={mainWindow:{width:780,height:580,x:10,y:10}};Ext.apply(this,config);this.state=this.readState();var provider=this;air.NativeApplication.nativeApplication.addEventListener('exiting',function(){provider.saveState();});};Ext.extend(Ext.air.FileProvider,Ext.state.Provider,{file:'extstate.data',readState:function(){var stateFile=air.File.applicationStorageDirectory.resolvePath(this.file);if(!stateFile.exists){return this.defaultState||{};}
var stream=new air.FileStream();stream.open(stateFile,air.FileMode.READ);var stateData=stream.readObject();stream.close();return stateData||this.defaultState||{};},saveState:function(name,value){var stateFile=air.File.applicationStorageDirectory.resolvePath(this.file);var stream=new air.FileStream();stream.open(stateFile,air.FileMode.WRITE);stream.writeObject(this.state);stream.close();}});

Ext.air.NativeObservable=Ext.extend(Ext.util.Observable,{addListener:function(name){this.proxiedEvents=this.proxiedEvents||{};if(!this.proxiedEvents[name]){var instance=this;var f=function(){var args=Array.prototype.slice.call(arguments,0);args.unshift(name);instance.fireEvent.apply(instance,args);};this.proxiedEvents[name]=f;this.getNative().addEventListener(name,f);}
Ext.air.NativeObservable.superclass.addListener.apply(this,arguments);}});Ext.air.NativeObservable.prototype.on=Ext.air.NativeObservable.prototype.addListener;

Ext.air.NativeWindow=function(config){Ext.apply(this,config);this.id=this.id||Ext.uniqueId();this.addEvents('close','closing','move','moving','resize','resizing','displayStateChange','displayStateChanging');Ext.air.NativeWindow.superclass.constructor.call(this);if(!this.instance){var options=new air.NativeWindowInitOptions();options.systemChrome=this.chrome;options.type=this.type;options.resizable=this.resizable;options.minimizable=this.minimizable;options.maximizable=this.maximizable;options.transparent=this.transparent;this.loader=window.runtime.flash.html.HTMLLoader.createRootWindow(false,options,false);if(this.file){this.loader.load(new air.URLRequest(this.file));}else{this.loader.loadString(this.html||'');}
this.instance=this.loader.window.nativeWindow;}else{this.loader=this.instance.stage.getChildAt(0);}
var provider=Ext.state.Manager;var b=air.Screen.mainScreen.visibleBounds;var state=provider.get(this.id)||{};provider.set(this.id,state);var win=this.instance;var width=Math.max(state.width||this.width,100);var height=Math.max(state.height||this.height,100);var centerX=b.x+((b.width/2)-(width/2));var centerY=b.y+((b.height/2)-(height/2));var x=!Ext.isEmpty(state.x,false)?state.x:(!Ext.isEmpty(this.x,false)?this.x:centerX);var y=!Ext.isEmpty(state.y,false)?state.y:(!Ext.isEmpty(this.y,false)?this.y:centerY);win.width=width;win.height=height;win.x=x;win.y=y;win.addEventListener('move',function(){if(win.displayState!=air.NativeWindowDisplayState.MINIMIZED&&win.width>100&&win.height>100){state.x=win.x;state.y=win.y;}});win.addEventListener('resize',function(){if(win.displayState!=air.NativeWindowDisplayState.MINIMIZED&&win.width>100&&win.height>100){state.width=win.width;state.height=win.height;}});Ext.air.NativeWindowManager.register(this);this.on('close',this.unregister,this);if(this.minimizeToTray){this.initMinimizeToTray(this.trayIcon,this.trayMenu);}};Ext.extend(Ext.air.NativeWindow,Ext.air.NativeObservable,{chrome:'standard',type:'normal',width:600,height:400,resizable:true,minimizable:true,maximizable:true,transparent:false,getNative:function(){return this.instance;},getCenterXY:function(){var b=air.Screen.mainScreen.visibleBounds;return{x:b.x+((b.width/2)-(this.width/2)),y:b.y+((b.height/2)-(this.height/2))};},show:function(){if(this.trayed){Ext.air.SystemTray.hideIcon();this.trayed=false;}
this.instance.visible=true;},activate:function(){this.show();this.instance.activate();},hide:function(){this.instance.visible=false;},close:function(){this.instance.close();},isMinimized:function(){return this.instance.displayState==air.NativeWindowDisplayState.MINIMIZED;},isMaximized:function(){return this.instance.displayState==air.NativeWindowDisplayState.MAXIMIZED;},moveTo:function(x,y){this.x=this.instance.x=x;this.y=this.instance.y=y;},fullscreen:function(nonInteractive){var SDS=runtime.flash.display.StageDisplayState;this.instance.stage.displayState=nonInteractive?SDS.FULL_SCREEN:SDS.FULL_SCREEN_INTERACTIVE;},bringToFront:function(){this.instance.orderToFront();},bringInFrontOf:function(win){this.instance.orderInFrontOf(win.instance?win.instance:win);},sendToBack:function(){this.instance.orderToBack();},sendBehind:function(win){this.instance.orderInBackOf(win.instance?win.instance:win);},resize:function(width,height){this.width=this.instance.width=width;this.height=this.instance.height=height;},unregister:function(){Ext.air.NativeWindowManager.unregister(this);},initMinimizeToTray:function(icon,menu){var tray=Ext.air.SystemTray;tray.setIcon(icon,this.trayTip);this.on('displayStateChanging',function(e){if(e.afterDisplayState=='minimized'){e.preventDefault();this.hide();tray.showIcon();this.trayed=true;}},this);tray.on('click',function(){this.activate();},this);if(menu){tray.setMenu(menu);}}});Ext.air.NativeWindow.getRootWindow=function(){return air.NativeApplication.nativeApplication.openedWindows[0];};Ext.air.NativeWindow.getRootHtmlWindow=function(){return Ext.air.NativeWindow.getRootWindow().stage.getChildAt(0).window;};Ext.air.NativeWindowGroup=function(){var list={};return{register:function(win){list[win.id]=win;},unregister:function(win){delete list[win.id];},get:function(id){return list[id];},closeAll:function(){for(var id in list){if(list.hasOwnProperty(id)){list[id].close();}}},each:function(fn,scope){for(var id in list){if(list.hasOwnProperty(id)){if(fn.call(scope||list[id],list[id])===false){return;}}}}};};Ext.air.NativeWindowManager=new Ext.air.NativeWindowGroup();

Ext.sql.Connection=function(config){Ext.apply(this,config);Ext.sql.Connection.superclass.constructor.call(this);this.addEvents({open:true,close:true});};Ext.extend(Ext.sql.Connection,Ext.util.Observable,{maxResults:10000,openState:false,open:function(file){},close:function(){},exec:function(sql){},execBy:function(sql,args){},query:function(sql){},queryBy:function(sql,args){},isOpen:function(){return this.openState;},getTable:function(name,keyName){return new Ext.sql.Table(this,name,keyName);},createTable:function(o){var tableName=o.name;var keyName=o.key;var fs=o.fields;if(!Ext.isArray(fs)){fs=fs.items;}
var buf=[];for(var i=0,len=fs.length;i<len;i++){var f=fs[i],s=f.name;switch(f.type){case"int":case"bool":case"boolean":s+=' INTEGER';break;case"float":s+=' REAL';break;default:s+=' TEXT';}
if(f.allowNull===false||f.name==keyName){s+=' NOT NULL';}
if(f.name==keyName){s+=' PRIMARY KEY';}
if(f.unique===true){s+=' UNIQUE';}
buf[buf.length]=s;}
var sql=['CREATE TABLE IF NOT EXISTS ',tableName,' (',buf.join(','),')'].join('');this.exec(sql);}});Ext.sql.Connection.getInstance=function(db,config){if(Ext.isAir){return new Ext.sql.AirConnection(config);}else{return new Ext.sql.GearsConnection(config);}};

Ext.sql.SQLiteStore=Ext.extend(Ext.data.Store,{constructor:function(config){config=config||{};config.reader=new Ext.data.JsonReader({id:config.key,fields:config.fields});var conn=Ext.sql.Connection.getInstance();conn.open(config.dbFile);conn.createTable({name:config.tableName,key:config.key,fields:config.reader.recordType.prototype.fields});Ext.sql.SQLiteStore.superclass.constructor.call(this,config);this.proxy=new Ext.sql.Proxy(conn,config.tableName,config.key,this,false);}});

Ext.sql.Table=function(conn,name,keyName){this.conn=conn;this.name=name;this.keyName=keyName;};Ext.sql.Table.prototype={update:function(o){var clause=this.keyName+" = ?";return this.updateBy(o,clause,[o[this.keyName]]);},updateBy:function(o,clause,args){var sql="UPDATE "+this.name+" set ";var fs=[],a=[];for(var key in o){if(o.hasOwnProperty(key)){fs[fs.length]=key+' = ?';a[a.length]=o[key];}}
for(var key in args){if(args.hasOwnProperty(key)){a[a.length]=args[key];}}
sql=[sql,fs.join(','),' WHERE ',clause].join('');return this.conn.execBy(sql,a);},insert:function(o){var sql="INSERT into "+this.name+" ";var fs=[],vs=[],a=[];for(var key in o){if(o.hasOwnProperty(key)){fs[fs.length]=key;vs[vs.length]='?';a[a.length]=o[key];}}
sql=[sql,'(',fs.join(','),') VALUES (',vs.join(','),')'].join('');return this.conn.execBy(sql,a);},lookup:function(id){return this.selectBy('where '+this.keyName+" = ?",[id])[0]||null;},exists:function(id){return!!this.lookup(id);},save:function(o){if(this.exists(o[this.keyName])){this.update(o);}else{this.insert(o);}},select:function(clause){return this.selectBy(clause,null);},selectBy:function(clause,args){var sql="select * from "+this.name;if(clause){sql+=' '+clause;}
args=args||{};return this.conn.queryBy(sql,args);},remove:function(clause){this.deleteBy(clause,null);},removeBy:function(clause,args){var sql="delete from "+this.name;if(clause){sql+=' where '+clause;}
args=args||{};this.conn.execBy(sql,args);}};

Ext.sql.Proxy=function(conn,table,keyName,store,readonly){Ext.sql.Proxy.superclass.constructor.call(this);this.conn=conn;this.table=this.conn.getTable(table,keyName);this.store=store;if(readonly!==true){this.store.on('add',this.onAdd,this);this.store.on('update',this.onUpdate,this);this.store.on('remove',this.onRemove,this);}};Ext.sql.Proxy.DATE_FORMAT='Y-m-d H:i:s';Ext.extend(Ext.sql.Proxy,Ext.data.DataProxy,{load:function(params,reader,callback,scope,arg){if(!this.conn.isOpen()){this.conn.on('open',function(){this.load(params,reader,callback,scope,arg);},this,{single:true});return;};if(this.fireEvent("beforeload",this,params,reader,callback,scope,arg)!==false){var clause=params.where||'';var args=params.args||[];var group=params.groupBy;var sort=params.sort;var dir=params.dir;if(group||sort){clause+=' ORDER BY ';if(group&&group!=sort){clause+=group+' ASC, ';}
clause+=sort+' '+(dir||'ASC');}
var rs=this.table.selectBy(clause,args);this.onLoad({callback:callback,scope:scope,arg:arg,reader:reader},rs);}else{callback.call(scope||this,null,arg,false);}},onLoad:function(trans,rs,e,stmt){if(rs===false){this.fireEvent("loadexception",this,null,trans.arg,e);trans.callback.call(trans.scope||window,null,trans.arg,false);return;}
var result=trans.reader.readRecords(rs);this.fireEvent("load",this,rs,trans.arg);trans.callback.call(trans.scope||window,result,trans.arg,true);},processData:function(o){var fs=this.store.fields;var r={};for(var key in o){var f=fs.key(key),v=o[key];if(f){if(f.type=='date'){r[key]=v?v.format(Ext.sql.Proxy.DATE_FORMAT,10):'';}else if(f.type=='boolean'){r[key]=v?1:0;}else{r[key]=v;}}}
return r;},onUpdate:function(ds,record){var changes=record.getChanges();var kn=this.table.keyName;this.table.updateBy(this.processData(changes),kn+' = ?',[record.data[kn]]);record.commit(true);},onAdd:function(ds,records,index){for(var i=0,len=records.length;i<len;i++){this.table.insert(this.processData(records[i].data));}},onRemove:function(ds,record,index){var kn=this.table.keyName;this.table.removeBy(kn+' = ?',[record.data[kn]]);}});

Ext.sql.AirConnection=Ext.extend(Ext.sql.Connection,{open:function(db){this.conn=new air.SQLConnection();var file=air.File.applicationDirectory.resolvePath(db);this.conn.open(file);this.openState=true;this.fireEvent('open',this);},close:function(){this.conn.close();this.fireEvent('close',this);},createStatement:function(type){var stmt=new air.SQLStatement();stmt.sqlConnection=this.conn;return stmt;},exec:function(sql){var stmt=this.createStatement('exec');stmt.text=sql;stmt.execute();},execBy:function(sql,args){var stmt=this.createStatement('exec');stmt.text=sql;this.addParams(stmt,args);stmt.execute();},query:function(sql){var stmt=this.createStatement('query');stmt.text=sql;stmt.execute(this.maxResults);return this.readResults(stmt.getResult());},queryBy:function(sql,args){var stmt=this.createStatement('query');stmt.text=sql;this.addParams(stmt,args);stmt.execute(this.maxResults);return this.readResults(stmt.getResult());},addParams:function(stmt,args){if(!args){return;}
for(var key in args){if(args.hasOwnProperty(key)){if(!isNaN(key)){var v=args[key];if(Ext.isDate(v)){v=v.format(Ext.sql.Proxy.DATE_FORMAT);}
stmt.parameters[parseInt(key)]=v;}else{stmt.parameters[':'+key]=args[key];}}}
return stmt;},readResults:function(rs){var r=[];if(rs&&rs.data){var len=rs.data.length;for(var i=0;i<len;i++){r[r.length]=rs.data[i];}}
return r;}});

Ext.air.SystemTray=function(){var app=air.NativeApplication.nativeApplication;var icon,isWindows=false,bitmaps;if(air.NativeApplication.supportsSystemTrayIcon){icon=app.icon;isWindows=true;}
if(air.NativeApplication.supportsDockIcon){icon=app.icon;}
return{setIcon:function(icon,tooltip,initWithIcon){if(!icon){return;}
var loader=new air.Loader();loader.contentLoaderInfo.addEventListener(air.Event.COMPLETE,function(e){bitmaps=new runtime.Array(e.target.content.bitmapData);if(initWithIcon){icon.bitmaps=bitmaps;}});loader.load(new air.URLRequest(icon));if(tooltip&&air.NativeApplication.supportsSystemTrayIcon){app.icon.tooltip=tooltip;}},bounce:function(priority){icon.bounce(priority);},on:function(eventName,fn,scope){icon.addEventListener(eventName,function(){fn.apply(scope||this,arguments);});},hideIcon:function(){if(!icon){return;}
icon.bitmaps=[];},showIcon:function(){if(!icon){return;}
icon.bitmaps=bitmaps;},setMenu:function(actions,_parentMenu){if(!icon){return;}
var menu=new air.NativeMenu();for(var i=0,len=actions.length;i<len;i++){var a=actions[i];if(a=='-'){menu.addItem(new air.NativeMenuItem("",true));}else{var item=menu.addItem(Ext.air.MenuItem(a));if(a.menu||(a.initialConfig&&a.initialConfig.menu)){item.submenu=Ext.air.SystemTray.setMenu(a.menu||a.initialConfig.menu,menu);}}
if(!_parentMenu){icon.menu=menu;}}
return menu;}};}();

Ext.air.DragType={TEXT:'text/plain',HTML:'text/html',URL:'text/uri-list',BITMAP:'image/x-vnd.adobe.air.bitmap',FILES:'application/x-vnd.adobe.air.file-list'};Ext.apply(Ext.EventObjectImpl.prototype,{hasFormat:function(format){if(this.browserEvent.dataTransfer){for(var i=0,len=this.browserEvent.dataTransfer.types.length;i<len;i++){if(this.browserEvent.dataTransfer.types[i]==format){return true;}}}
return false;},getData:function(type){return this.browserEvent.dataTransfer.getData(type);}});

Ext.air.Sound={play:function(file,startAt){var soundFile=air.File.applicationDirectory.resolvePath(file);var sound=new air.Sound();sound.load(new air.URLRequest(soundFile.url));sound.play(startAt);}};

Ext.air.SystemMenu=function(){var menu;if(air.NativeWindow.supportsMenu&&nativeWindow.systemChrome!=air.NativeWindowSystemChrome.NONE){menu=new air.NativeMenu();nativeWindow.menu=menu;}
if(air.NativeApplication.supportsMenu){menu=air.NativeApplication.nativeApplication.menu;}
function find(menu,text){for(var i=0,len=menu.items.length;i<len;i++){if(menu.items[i]['label']==text){return menu.items[i];}}
return null;}
return{add:function(text,actions,mindex){var item=find(menu,text);if(!item){item=menu.addItem(new air.NativeMenuItem(text));item.mnemonicIndex=mindex||0;item.submenu=new air.NativeMenu();}
for(var i=0,len=actions.length;i<len;i++){item.submenu.addItem(actions[i]=='-'?new air.NativeMenuItem("",true):Ext.air.MenuItem(actions[i]));}
return item.submenu;},get:function(){return menu;}};}();Ext.air.MenuItem=function(action){if(!action.isAction){action=new Ext.Action(action);}
var cfg=action.initialConfig;var nativeItem=new air.NativeMenuItem(cfg.itemText||cfg.text);nativeItem.enabled=!cfg.disabled;if(!Ext.isEmpty(cfg.checked)){nativeItem.checked=cfg.checked;}
var handler=cfg.handler;var scope=cfg.scope;nativeItem.addEventListener(air.Event.SELECT,function(){handler.call(scope||window,cfg);});action.addComponent({setDisabled:function(v){nativeItem.enabled=!v;},setText:function(v){nativeItem.label=v;},setVisible:function(v){nativeItem.enabled=!v;},setHandler:function(newHandler,newScope){handler=newHandler;scope=newScope;},on:function(){}});return nativeItem;}

Ext.ns('Ext.air');Ext.air.MusicPlayer=Ext.extend(Ext.util.Observable,{activeSound:null,activeChannel:null,activeTransform:new air.SoundTransform(1,0),pausePosition:0,progressInterval:500,constructor:function(config){config=config||{};Ext.apply(this,config);this.addEvents('stop','pause','play','load','id3info','complete','progress','skip');Ext.air.MusicPlayer.superclass.constructor.call(this,config);this.onSoundFinishedDelegate=this.onSoundFinished.createDelegate(this);this.onSoundLoadDelegate=this.onSoundLoad.createDelegate(this);this.onSoundID3LoadDelegate=this.onSoundID3Load.createDelegate(this);Ext.TaskMgr.start({run:this.notifyProgress,scope:this,interval:this.progressInterval});},adjustVolume:function(percent){this.activeTransform.volume=percent;if(this.activeChannel){this.activeChannel.soundTransform=this.activeTransform;}},stop:function(){this.pausePosition=0;if(this.activeChannel){this.activeChannel.stop();this.activeChannel=null;}
if(this.activeSound){this.activeSound.removeEventListener(air.Event.COMPLETE,this.onSoundLoadDelegate);this.activeSound.removeEventListener(air.Event.ID3,this.onSoundID3LoadDelegate);this.activeSound.removeEventListener(air.Event.SOUND_COMPLETE,this.onSoundFinishedDelegate);}},pause:function(){if(this.activeChannel){this.pausePosition=this.activeChannel.position;this.activeChannel.stop();}},play:function(url){if(url){this.stop();var req=new air.URLRequest(url);this.activeSound=new air.Sound();this.activeSound.addEventListener(air.Event.SOUND_COMPLETE,this.onSoundFinishedDelegate);this.activeSound.addEventListener(air.Event.COMPLETE,this.onSoundLoadDelegate);this.activeSound.addEventListener(air.Event.ID3,this.onSoundID3LoadDelegate);this.activeSound.load(req);}else{this.onSoundLoad();}},skipTo:function(pos){if(this.activeChannel){this.activeChannel.stop();this.activeChannel=this.activeSound.play(pos);this.activeChannel.soundTransform=this.activeTransform;this.fireEvent('skip',this.activeChannel,this.activeSound,pos);}},hasActiveChannel:function(){return!!this.activeChannel;},onSoundLoad:function(event){if(this.activeSound){if(this.activeChannel){this.activeChannel.stop();}
this.activeChannel=this.activeSound.play(this.pausePosition);this.activeChannel.soundTransform=this.activeTransform;this.fireEvent('load',this.activeChannel,this.activeSound);}},onSoundFinished:function(event){this.fireEvent('complete',event);},onSoundID3Load:function(event){this.activeSound.removeEventListener(air.Event.ID3,this.onSoundID3LoadDelegate);var id3=event.target.id3;this.fireEvent('id3info',id3);},notifyProgress:function(){if(this.activeChannel&&this.activeSound){var playbackPercent=100*(this.activeChannel.position/this.activeSound.length);if(playbackPercent>99.7){this.onSoundFinished();}else{this.fireEvent('progress',this.activeChannel,this.activeSound);}}}});

Ext.air.Notify=Ext.extend(Ext.air.NativeWindow,{winType:'notify',type:'lightweight',width:400,height:50,chrome:'none',transparent:true,alwaysOnTop:true,extraHeight:22,hideDelay:3000,msgId:'msg',iconId:'icon',icon:Ext.BLANK_IMAGE_URL,boxCls:'x-box',extAllCSS:'../extjs/resources/css/ext-all.css',xtpl:new Ext.XTemplate('<html><head><link rel="stylesheet" href="{extAllCSS}" /></head>','<body>','<div class="{boxCls}-tl"><div class="{boxCls}-tr"><div class="{boxCls}-tc"></div></div></div><div class="{boxCls}-ml"><div class="{boxCls}-mr"><div class="{boxCls}-mc">','<div id="{msgId}">','<span>{msg}</span>','<div id="{iconId}" style="float: right;"><img src="{icon}"></div>','</div>','</div></div></div><div class="{boxCls}-bl"><div class="{boxCls}-br"><div class="{boxCls}-bc"></div></div></div>','</body>','</html>'),constructor:function(config){config=config||{};Ext.apply(this,config);config.html=this.xtpl.apply(this);Ext.air.Notify.superclass.constructor.call(this,config);this.getNative().alwaysInFront=true;this.onCompleteDelegate=this.onComplete.createDelegate(this);this.loader.addEventListener(air.Event.COMPLETE,this.onCompleteDelegate);},onComplete:function(event){this.loader.removeEventListener(air.Event.COMPLETE,this.onCompleteDelegate);this.show(event);},show:function(event){var h=event.target.window.document.getElementById(this.msgId).clientHeight+this.extraHeight;var main=air.Screen.mainScreen;var xy=[0,0];xy[0]=main.visibleBounds.bottomRight.x-this.width;xy[1]=main.visibleBounds.bottomRight.y-this.height;this.moveTo(xy[0],xy[1]);Ext.air.Notify.superclass.show.call(this);this.close.defer(this.hideDelay,this);}});

Ext.air.Clipboard=function(){var clipboard=air.Clipboard.generalClipboard;return{hasData:function(format){return clipboard.hasFormat(format);},setData:function(format,data){clipboard.setData(format,data);},setDataHandler:function(format,fn){clipboard.setDataHandler(format,fn);},getData:function(format,transferMode){clipboard.getData(format,transferMode);},clear:function(){clipboard.clear();},clearData:function(format){clipboard.clearData(format);}};}();

Ext.air.App=function(){return{launchOnStartup:function(launch){air.NativeApplication.nativeApplication.startAtLogin=!!launch;},getActiveWindow:function(){return air.NativeApplication.activeWindow;}};}();

Ext.air.dir=function(obj,indent){indent=indent||0;var indentString="";for(var i=0;i<indent;i++){indentString+="\t";}
var val;for(var prop in obj){val=obj[prop];if(typeof(val)=="object"){air.trace(indentString+" "+prop+": [Object]");Ext.air.dir(val,indent+1);}else{air.trace(indentString+" "+prop+": "+val);}}};

Ext.tree.LocalTreeLoader=Ext.extend(Ext.tree.TreeLoader,{requestData:function(node,callback){if(this.fireEvent("beforeload",this,node,callback)!==false){var p=Ext.urlDecode(this.getParams(node));var response=this.dataFn(node);this.processResponse(response,node,callback);this.fireEvent("load",this,node,response);}else{if(typeof callback=="function"){callback();}}},processResponse:function(o,node,callback){try{node.beginUpdate();for(var i=0,len=o.length;i<len;i++){var n=this.createNode(o[i]);if(n){node.appendChild(n);}}
node.endUpdate();if(typeof callback=="function"){callback(this,node);}}catch(e){this.handleFailure(response);}},load:function(node,callback){if(this.clearOnLoad){while(node.firstChild){node.removeChild(node.firstChild);}}
if(this.doPreload(node)){if(typeof callback=="function"){callback();}}else if(this.dataFn||this.fn){this.requestData(node,callback);}}});Ext.air.FileTreeLoader=Ext.extend(Ext.tree.LocalTreeLoader,{extensionFilter:false,dataFn:function(currNode){var currDir;if(currNode.attributes.url){currDir=this.directory.resolvePath(currNode.attributes.url);}else{currDir=this.directory;}
var files=[];var c=currDir.getDirectoryListing();for(i=0;i<c.length;i++){if(c[i].isDirectory||this.extensionFilter===false||this.extensionFilter===c[i].extension)
files.push({text:c[i].name,url:c[i].url,extension:c[i].extension,leaf:!c[i].isDirectory});}
return files;}});

Ext.air.VideoPanel=Ext.extend(Ext.Panel,{autoResize:true,initComponent:function(){var connection=new air.NetConnection();connection.connect(null);this.stream=new runtime.flash.net.NetStream(connection);this.stream.client={onMetaData:Ext.emptyFn};Ext.air.VideoPanel.superclass.initComponent.call(this);this.on('bodyresize',this.onVideoResize,this);},afterRender:function(){Ext.air.VideoPanel.superclass.afterRender.call(this);(function(){var box=this.body.getBox();this.video=new air.Video(this.getInnerWidth(),this.getInnerHeight());if(this.url){this.video.attachNetStream(this.stream);this.stream.play(this.url);}
nativeWindow.stage.addChild(this.video);this.video.x=box.x;this.video.y=box.y;}).defer(500,this);},onVideoResize:function(pnl,w,h){if(this.video&&this.autoResize){var iw=this.getInnerWidth();var ih=this.getInnerHeight();this.video.width=iw
this.video.height=ih;var xy=this.body.getXY();if(xy[0]!==this.video.x){this.video.x=xy[0];}
if(xy[1]!==this.video.y){this.video.y=xy[1];}}},loadVideo:function(url){this.stream.close();this.video.attachNetStream(this.stream);this.stream.play(url);}});Ext.reg('videopanel',Ext.air.VideoPanel);
