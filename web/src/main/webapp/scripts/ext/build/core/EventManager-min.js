/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.EventManager=function(){var docReadyEvent,docReadyProcId,docReadyState=false;var resizeEvent,resizeTask,textEvent,textSize;var E=Ext.lib.Event;var D=Ext.lib.Dom;var xname='Ex'+'t';var elHash={};var addListener=function(el,ename,fn,wrap,scope){var id=Ext.id(el);if(!elHash[id]){elHash[id]={};}
var es=elHash[id];if(!es[ename]){es[ename]=[];}
var ls=es[ename];ls.push({id:id,ename:ename,fn:fn,wrap:wrap,scope:scope});E.on(el,ename,wrap);if(ename=="mousewheel"&&el.addEventListener){el.addEventListener("DOMMouseScroll",wrap,false);E.on(window,'unload',function(){el.removeEventListener("DOMMouseScroll",wrap,false);});}
if(ename=="mousedown"&&el==document){Ext.EventManager.stoppedMouseDownEvent.addListener(wrap);}}
var removeListener=function(el,ename,fn,scope){el=Ext.getDom(el);var id=Ext.id(el),es=elHash[id],wrap;if(es){var ls=es[ename],l;if(ls){for(var i=0,len=ls.length;i<len;i++){l=ls[i];if(l.fn==fn&&(!scope||l.scope==scope)){wrap=l.wrap;E.un(el,ename,wrap);ls.splice(i,1);break;}}}}
if(ename=="mousewheel"&&el.addEventListener&&wrap){el.removeEventListener("DOMMouseScroll",wrap,false);}
if(ename=="mousedown"&&el==document&&wrap){Ext.EventManager.stoppedMouseDownEvent.removeListener(wrap);}}
var removeAll=function(el){el=Ext.getDom(el);var id=Ext.id(el),es=elHash[id],ls;if(es){for(var ename in es){if(es.hasOwnProperty(ename)){ls=es[ename];for(var i=0,len=ls.length;i<len;i++){E.un(el,ename,ls[i].wrap);ls[i]=null;}}
es[ename]=null;}
delete elHash[id];}}
var fireDocReady=function(){if(!docReadyState){docReadyState=true;Ext.isReady=true;if(docReadyProcId){clearInterval(docReadyProcId);}
if(Ext.isGecko||Ext.isOpera){document.removeEventListener("DOMContentLoaded",fireDocReady,false);}
if(Ext.isIE){var defer=document.getElementById("ie-deferred-loader");if(defer){defer.onreadystatechange=null;defer.parentNode.removeChild(defer);}}
if(docReadyEvent){docReadyEvent.fire();docReadyEvent.clearListeners();}}};var initDocReady=function(){docReadyEvent=new Ext.util.Event();if(Ext.isGecko||Ext.isOpera){document.addEventListener("DOMContentLoaded",fireDocReady,false);}else if(Ext.isIE){document.write("<s"+'cript id="ie-deferred-loader" defer="defer" src="/'+'/:"></s'+"cript>");var defer=document.getElementById("ie-deferred-loader");defer.onreadystatechange=function(){if(this.readyState=="complete"){fireDocReady();}};}else if(Ext.isWebKit){docReadyProcId=setInterval(function(){var rs=document.readyState;if(rs=="complete"){fireDocReady();}},10);}
E.on(window,"load",fireDocReady);};var createBuffered=function(h,o){var task=new Ext.util.DelayedTask(h);return function(e){e=new Ext.EventObjectImpl(e);task.delay(o.buffer,h,null,[e]);};};var createSingle=function(h,el,ename,fn,scope){return function(e){Ext.EventManager.removeListener(el,ename,fn,scope);h(e);};};var createDelayed=function(h,o){return function(e){e=new Ext.EventObjectImpl(e);setTimeout(function(){h(e);},o.delay||10);};};var listen=function(element,ename,opt,fn,scope){var o=(!opt||typeof opt=="boolean")?{}:opt;fn=fn||o.fn;scope=scope||o.scope;var el=Ext.getDom(element);if(!el){throw"Error listening for \""+ename+'\". Element "'+element+'" doesn\'t exist.';}
var h=function(e){if(!window[xname]){return;}
e=Ext.EventObject.setEvent(e);var t;if(o.delegate){t=e.getTarget(o.delegate,el);if(!t){return;}}else{t=e.target;}
if(o.stopEvent===true){e.stopEvent();}
if(o.preventDefault===true){e.preventDefault();}
if(o.stopPropagation===true){e.stopPropagation();}
if(o.normalized===false){e=e.browserEvent;}
fn.call(scope||el,e,t,o);};if(o.delay){h=createDelayed(h,o);}
if(o.single){h=createSingle(h,el,ename,fn,scope);}
if(o.buffer){h=createBuffered(h,o);}
addListener(el,ename,fn,h,scope);return h;};var propRe=/^(?:scope|delay|buffer|single|stopEvent|preventDefault|stopPropagation|normalized|args|delegate)$/,curWidth=0,curHeight=0;var pub={addListener:function(element,eventName,fn,scope,options){if(typeof eventName=="object"){var o=eventName;for(var e in o){if(propRe.test(e)){continue;}
if(typeof o[e]=="function"){listen(element,e,o,o[e],o.scope);}else{listen(element,e,o[e]);}}
return;}
return listen(element,eventName,options,fn,scope);},removeListener:function(element,eventName,fn,scope){return removeListener(element,eventName,fn,scope);},removeAll:function(element){return removeAll(element);},onDocumentReady:function(fn,scope,options){if(docReadyState){docReadyEvent.addListener(fn,scope,options);docReadyEvent.fire();docReadyEvent.clearListeners();return;}
if(!docReadyEvent){initDocReady();}
options=options||{};if(!options.delay){options.delay=1;}
docReadyEvent.addListener(fn,scope,options);},doResizeEvent:function(){var h=D.getViewHeight(),w=D.getViewWidth();if(curHeight!=h||curWidth!=w){resizeEvent.fire(curWidth=w,curHeight=h);}},onWindowResize:function(fn,scope,options){if(!resizeEvent){resizeEvent=new Ext.util.Event();resizeTask=new Ext.util.DelayedTask(this.doResizeEvent);E.on(window,"resize",this.fireWindowResize,this);}
resizeEvent.addListener(fn,scope,options);},fireWindowResize:function(){if(resizeEvent){if((Ext.isIE||Ext.isAir)&&resizeTask){resizeTask.delay(50);}else{resizeEvent.fire(D.getViewWidth(),D.getViewHeight());}}},onTextResize:function(fn,scope,options){if(!textEvent){textEvent=new Ext.util.Event();var textEl=new Ext.Element(document.createElement('div'));textEl.dom.className='x-text-resize';textEl.dom.innerHTML='X';textEl.appendTo(document.body);textSize=textEl.dom.offsetHeight;setInterval(function(){if(textEl.dom.offsetHeight!=textSize){textEvent.fire(textSize,textSize=textEl.dom.offsetHeight);}},this.textResizeInterval);}
textEvent.addListener(fn,scope,options);},removeResizeListener:function(fn,scope){if(resizeEvent){resizeEvent.removeListener(fn,scope);}},fireResize:function(){if(resizeEvent){resizeEvent.fire(D.getViewWidth(),D.getViewHeight());}},ieDeferSrc:false,textResizeInterval:50};pub.on=pub.addListener;pub.un=pub.removeListener;pub.stoppedMouseDownEvent=new Ext.util.Event();return pub;}();Ext.onReady=Ext.EventManager.onDocumentReady;(function(){var initExtCss=function(){var bd=document.body||document.getElementsByTagName('body')[0];if(!bd){return false;}
var cls=[' ',Ext.isIE?"ext-ie "+(Ext.isIE6?'ext-ie6':(Ext.isIE7?'ext-ie7':'ext-ie8')):Ext.isGecko?"ext-gecko "+(Ext.isGecko2?'ext-gecko2':'ext-gecko3'):Ext.isOpera?"ext-opera":Ext.isSafari?"ext-safari":Ext.isChrome?"ext-chrome":""];if(Ext.isMac){cls.push("ext-mac");}
if(Ext.isLinux){cls.push("ext-linux");}
if(Ext.isStrict||Ext.isBorderBox){var p=bd.parentNode;if(p){p.className+=Ext.isStrict?' ext-strict':' ext-border-box';}}
bd.className+=cls.join(' ');return true;}
if(!initExtCss()){Ext.onReady(initExtCss);}})();Ext.EventObject=function(){var E=Ext.lib.Event;var safariKeys={3:13,63234:37,63235:39,63232:38,63233:40,63276:33,63277:34,63272:46,63273:36,63275:35};var btnMap=Ext.isIE?{1:0,4:1,2:2}:(Ext.isWebKit?{1:0,2:1,3:2}:{0:0,1:1,2:2});Ext.EventObjectImpl=function(e){if(e){this.setEvent(e.browserEvent||e);}};Ext.EventObjectImpl.prototype={browserEvent:null,button:-1,shiftKey:false,ctrlKey:false,altKey:false,BACKSPACE:8,TAB:9,NUM_CENTER:12,ENTER:13,RETURN:13,SHIFT:16,CTRL:17,CONTROL:17,ALT:18,PAUSE:19,CAPS_LOCK:20,ESC:27,SPACE:32,PAGE_UP:33,PAGEUP:33,PAGE_DOWN:34,PAGEDOWN:34,END:35,HOME:36,LEFT:37,UP:38,RIGHT:39,DOWN:40,PRINT_SCREEN:44,INSERT:45,DELETE:46,ZERO:48,ONE:49,TWO:50,THREE:51,FOUR:52,FIVE:53,SIX:54,SEVEN:55,EIGHT:56,NINE:57,A:65,B:66,C:67,D:68,E:69,F:70,G:71,H:72,I:73,J:74,K:75,L:76,M:77,N:78,O:79,P:80,Q:81,R:82,S:83,T:84,U:85,V:86,W:87,X:88,Y:89,Z:90,CONTEXT_MENU:93,NUM_ZERO:96,NUM_ONE:97,NUM_TWO:98,NUM_THREE:99,NUM_FOUR:100,NUM_FIVE:101,NUM_SIX:102,NUM_SEVEN:103,NUM_EIGHT:104,NUM_NINE:105,NUM_MULTIPLY:106,NUM_PLUS:107,NUM_MINUS:109,NUM_PERIOD:110,NUM_DIVISION:111,F1:112,F2:113,F3:114,F4:115,F5:116,F6:117,F7:118,F8:119,F9:120,F10:121,F11:122,F12:123,setEvent:function(e){if(e==this||(e&&e.browserEvent)){return e;}
this.browserEvent=e;if(e){this.button=e.button?btnMap[e.button]:(e.which?e.which-1:-1);if(e.type=='click'&&this.button==-1){this.button=0;}
this.type=e.type;this.shiftKey=e.shiftKey;this.ctrlKey=e.ctrlKey||e.metaKey;this.altKey=e.altKey;this.keyCode=e.keyCode;this.charCode=e.charCode;this.target=E.getTarget(e);this.xy=E.getXY(e);}else{this.button=-1;this.shiftKey=false;this.ctrlKey=false;this.altKey=false;this.keyCode=0;this.charCode=0;this.target=null;this.xy=[0,0];}
return this;},stopEvent:function(){if(this.browserEvent){if(this.browserEvent.type=='mousedown'){Ext.EventManager.stoppedMouseDownEvent.fire(this);}
E.stopEvent(this.browserEvent);}},preventDefault:function(){if(this.browserEvent){E.preventDefault(this.browserEvent);}},isNavKeyPress:function(){var k=this.keyCode;k=Ext.isSafari?(safariKeys[k]||k):k;return(k>=33&&k<=40)||k==this.RETURN||k==this.TAB||k==this.ESC;},isSpecialKey:function(){var k=this.keyCode;k=Ext.isSafari?(safariKeys[k]||k):k;return(this.type=='keypress'&&this.ctrlKey)||this.isNavKeyPress()||(k==this.BACKSPACE)||(k>=16&&k<=20)||(k>=44&&k<=45);},stopPropagation:function(){if(this.browserEvent){if(this.browserEvent.type=='mousedown'){Ext.EventManager.stoppedMouseDownEvent.fire(this);}
E.stopPropagation(this.browserEvent);}},getCharCode:function(){return this.charCode||this.keyCode;},getKey:function(){var k=this.keyCode||this.charCode;return Ext.isSafari?(safariKeys[k]||k):k;},getPageX:function(){return this.xy[0];},getPageY:function(){return this.xy[1];},getTime:function(){if(this.browserEvent){return E.getTime(this.browserEvent);}
return null;},getXY:function(){return this.xy;},getTarget:function(selector,maxDepth,returnEl){return selector?Ext.fly(this.target).findParent(selector,maxDepth,returnEl):(returnEl?Ext.get(this.target):this.target);},getRelatedTarget:function(){if(this.browserEvent){return E.getRelatedTarget(this.browserEvent);}
return null;},getWheelDelta:function(){var e=this.browserEvent;var delta=0;if(e.wheelDelta){delta=e.wheelDelta/120;}else if(e.detail){delta=-e.detail/3;}
return delta;},hasModifier:function(){return((this.ctrlKey||this.altKey)||this.shiftKey)?true:false;},within:function(el,related,allowEl){var t=this[related?"getRelatedTarget":"getTarget"]();return t&&((allowEl?(t===Ext.getDom(el)):false)||Ext.fly(el).contains(t));},getPoint:function(){return new Ext.lib.Point(this.xy[0],this.xy[1]);}};return new Ext.EventObjectImpl();}();