(function(win,doc){ // EUROPEAN COMMISSION - COMPONENTS LIBRARY 2.9.7

	var IE=(function(){var ie=(/*@cc_on!@*/false);if(ie){var v=3,div=doc.createElement('div'),all=div.getElementsByTagName('i');while(div.innerHTML='<!--[if gt IE ' + (++v) + ']><i></i><![endif]-->',all[0]);return v > 4 ? v : (/*@cc_on!@*/false);}return ie}());

	win.widgets			= {};
	win.doc				= doc;
	win.docType			= doc.documentElement||doc.body;
	win.translations	= top.translations||{};
	win.isIE			= IE;
	win.isMobile		= win.isMobile||false;
	win._$				= win._$||{};
	win._$.extend		= function(o){for(var i in o){_$[i]=o[i]}};
	
	win._$.settings			= win._$.settings||{};
	win._$.settings.widgets = win._$.settings.widgets||{};
	
_$.extend({

	path	 	: "/wel/components/2013/scripts/",
	loader	 	: {"tabsConfig":"tabs","scrollBarConfig":"scrollbar","ediaGalleryConfig":"mediagallery","slideShowConfig":"slideshow","multipageConfig":"multipage","accordionConfig":"accordion","treeConfig":"tree","fusionChartsConfig":"chartsfusion","dynamicPanelConfig":"dynamicpanel","calendarSkin":"calendar","calendarDynamic":"calendar"},
	
// COMPONENTS
	
	run				:function(){if(!doc.lang){var b=doc.getElementsByTagName('body');b[0].className +=" js";_$.getDocLang()};_$._unobtrusive_I();_$._unobtrusive_II(_$.settings.widgets);if(!_$.isMobile){_$._lazyLoader(_$.settings.desktop)}else{_$._lazyLoader(_$.settings.mobile);}},
	getParams		:function(elm,classConfig,defaultConfig){var i,e,l,m,z,t,a,b,j,v,o=elm,n=classConfig,d=defaultConfig,c=o.className;if(c.indexOf(n+"[")!=-1){c=c.split("[")[1].split("]")[0];if(c.indexOf(":")!=-1){c=c.split(",");n=[];for(z=0,t=c.length;z<t;z++){a=c[z].split(":");b=a[0];j=a[1];n[b]=j;}m=n;}else{c=c.split(",");m=c;}if(!m){m=d;}else{for(v in d){m[v]=(m[v])?m[v]:d[v];}}}return m;},
	_process		:function(event){var n=window.event||event;if(!n){return;}var n=n.target||n.srcElement,n=n.src,n=n.split("/").reverse(),n=n[0].split(".")[0];if(components[n]){if(typeof components[n].init=="function"){components[n].init(0)}}},
	_unobtrusive_I	:function(){var x=0,z,u=doc.getElementsByTagName("div"),t,l=u.length,o,c,plg,pcl=_$.list={};function add(n){if(typeof pcl[n]!=="object"){pcl[n]=[];}pcl[n].push(o);}if(l!=0){var loaders=_$.loader,loadStr=[];for(var k in loaders){loadStr.push(k);}loadStr=loadStr.join("|");reg=new RegExp("(.*)("+loadStr+")(.*)");for(z=0;z<l;z++){o=u[z];c=o.className;if(c){var aa=c.replace( reg,'$2'),lsc=_$.loader[aa];if(lsc){add(lsc);}}if(z==l-1){for(var n in pcl){if(n!="undefined"){_$.include(_$.path+""+n+".js",_$._process);}}}}}},
	_pusher			:function(a,n,obj){if(!_$[a]){_$[a]=[];}if(!_$[a][n]){_$[a][n]=[]}_$[a][n].push(obj);},
	jsonToString	:function(obj){r="{";i=0;for(var key in obj){if(i>0){r +=','}r +='"'+key+'":';var t=typeof obj[key];if(t==="string"){r +='"'+obj[key]+'"'}else if(t==="number"){r +=obj[key]}else if(t==="boolean"){r +='"'+(obj[key])?"true":"false"+'"'}else if(t==="function"){r +='"'+ encodeURIComponent(obj[key]) +'"'}else if(obj[key] instanceof Array){r +='[';for(var ii=0,ll=obj[key].length;ii<ll;ii++){if(ii>0){r +=','}r +='"'+obj[key][ii]+'"';}r +=']';}else if(t=="object"){r +=this.jsonToString(obj[key])};i++;}r +="}";return r;},
	updateBy 		:function(obj,txt){var tmp=doc.createElement("div"),domer;tmp.innerHTML=txt;domer=tmp.getElementsByTagName("*")[0];obj.innerHTML=domer.innerHTML;tmp=null;if(isIE){setTimeout(function(){obj.innerHTML=obj.innerHTML},0);};domer=null;tmp=null;return obj;},
	_unobtrusive_II	:function(json){var o,c,s,d,y=0,l=false;_$.ajaxqueue=[];for(var n in json){o=doc.getElementById(n);if(o){var j=json[n],na=j["component"],dy=j["dynamic"];if(na){_$._pusher("list",na,o);_$._pusher("toLoad",na,o)}else if(dy){_$.ajaxqueue.push(o);}}};for(var n in _$.toLoad){_$.include(_$.path+""+n+".js",_$._process)}},
	
// CORE

	ready			:function(func){if(_$.domIsReady){func();return;}if(!_$.loadEvents){_$.loadEvents=[];}function isReady(){_$.domIsReady=true;clearInterval(_$.loadTimer);while(_$.exec=_$.loadEvents.shift()){_$.exec();}if(_$.ieReady){_$.ieReady.onreadystatechange='';}}if(!_$.loadEvents[0]){if(doc.addEventListener){doc.addEventListener("DOMContentLoaded",isReady,false);}else if(isIE < 9){document.write("<script id='__ie_components' defer src='javascript:void(0)'><\/script>");var script=document.getElementById("__ie_components");script.onreadystatechange=function(){if(this.readyState=="complete"){isReady();}};}else if(/WebKit|KHTML|iCab/i.test(navigator.userAgent)){_$.loadTimer=setInterval(function(){if(/loaded|complete/.test(doc.readyState)){isReady();}},10);}_$.oldOnload=window.onload;window.onload=function(){isReady();if(_$.oldOnload){_$.oldOnload();}};}_$.loadEvents.push(func);},
	addEvent		:function(o,e,f){if(e=="load"&&doc.readyState=="complete"){f();_$.domIsReady=true;return;}if(o.addEventListener){o.addEventListener(e,f,false);}else if(o.attachEvent){o.attachEvent( "on"+e,f);}},
	removeEvent 	:function(o,e,f){if(o.detachEvent){o.detachEvent('on'+e,f);}else{o.removeEventListener(e,f,false);}},
	captureEvent	:function(e){e=e||window.event;keyCode=null;if(e){srcElm=e.target||e.srcElement;var t=e.type.toLowerCase();if(t=="keypress"||t=="keydown"||t=="keyup"){keyCode=(e.keyCode?e.keyCode:(e.charCode?e.charCode:e.which));}}},
	delegate		:function(o,evt,t,f){_$.addEvent(o,evt,function(e){var	e=e||window.event,b=e.target||e.srcElement;if(b.tagName===t.toUpperCase()){f(b);}});},

// SELECTOR

	getByClass		:function(node,tag,cls){var b,r,a=[],i,j,e;if(!node){node=document;}if(!tag){tag='*';}e=node.getElementsByTagName(tag);b=e.length;r=new RegExp("(^|\\s)"+cls+"(\\s|$)");for(i=0,j=0;i<b;i++){if(r.test(e[i].className)){a[j]=e[i];j++;}}return a;},

// DOM

	after			:function(newElm,targetElm){var p,t=targetElm,n=newElm;if(t){p=t.parentNode;if(p.lastchild==t){p.appendChild(n);}else{p.insertBefore(n,t.nextSibling);}}},
	before			:function(newElm,targetElm){var p,t=targetElm,n=newElm;if(t){p=t.parentNode;if(p){p.insertBefore(n,t);}}},
	remove			:function(elm){if(elm){if(elm.parentNode){elm.parentNode.removeChild(elm);}}},
    wrap			:function(srcEl,newEl){if(!srcEl){return;};newEl.appendChild(srcEl.cloneNode(true));if(srcEl.parentNode){srcEl.parentNode.replaceChild(newEl,srcEl);}},
    each			:function(dom,fn){for(var i=0,l=dom.length;i<l;++i){if(fn.call(dom[i],i,dom)===false){break;}}return dom;},
	_nav 			:function(node,direction,checkTagName){var oldName = node.tagName;while ((node = node[direction + "Sibling"]) && (node.nodeType !== 1 || (checkTagName? node.tagName !== oldName : node.tagName === "!"))) {};return node;},
	prev 			:function(elm){return _$._nav(elm,"previous");},
	next			:function(elm){return _$._nav(elm,"next");},

// DOM - FX

	toggleDisplay	:function(o,s){if(!o){return;}if(o.style){o.style.display="";}var c=o.className||"";if(c){c=c.replace(/( )?(hide|show)/,"");}o.className=(s=="show")?c+" show":c+" hide";},
	show			:function(o){_$.toggleDisplay(o,"show");},
	hide			:function(o){_$.toggleDisplay(o,"hide");},
	display 		:function(elm,show){if(show==undefined){return elm.style.display;}else{elm.style.display=show;}},
	_slide			:function(elm,direction,func){var tim=10,spd=5,h=0,w=0,v,d=direction,end=false,gh=_$.height,gw=_$.width,dsp=_$.display;if(!elm||!direction){return;}if(!elm.info){elm.removeAttribute("style");dsp(elm,'block');elm.info=_$.getModelBox(elm);}if(!elm.first){elm.removeAttribute("style");dsp(elm,'block');if(d=="up"){elm.style.height="auto";}else if(d=="down"){elm.style.height="0px";}else if(d=="left"){elm.style.width="auto";elm.style.height=elm.info.IH+"px";}else if(d=="right"){elm.style.width="0px";elm.style.height=elm.info.IH+"px";}elm.style.overflow="hidden";elm.first=true;}if(d=="up"||d=="down"){h=gh(elm);}else{w=gw(elm);}if(d=="up"){if(h>0){v=Math.round(h/spd);v=(v<1)?1:v;v=(h-v);}else{end=true;}}else if(d=="down"){if(h<elm.info.IH){v=Math.round((elm.info.IH-h)/spd);v=(v<1)?1:v;v=(h+v);}else{end=true;}}else if(d=="left"){if(w>0){v=Math.round(w/spd);v=(v<1)?1:v;v=(w-v);}else{end=true;}}else if(d=="right"){if(w<elm.info.IW){v=Math.round((elm.info.IW-w)/spd);v=(v<1)?1:v;v=(w+v);}else{end=true;}}if(d=="up"||d=="down"){gh(elm,v+'px');}else{gw(elm,v+'px');}clearInterval(elm.timer);elm.style.position="relative";if(end||v==0||v>=elm.info.IH||v>=elm.info.IW){if(d=="up"){gh(elm,"0px");dsp(elm,'none');}else if(d=="down"){elm.removeAttribute("style");dsp(elm,'block');}else if(d=="left"){gw(elm,"0px");dsp(elm,'none');}else if(d=="right"){elm.removeAttribute("style");dsp(elm,'block');}elm.first=false;if(typeof func=="function"){func(elm);}}else{elm.timer=setInterval(function(){_$._slide(elm,direction,func);},tim);}},
	slideUp			:function(elm,func){_$._slide(elm,"up",func);},
	slideDown		:function(elm,func){_$._slide(elm,"down",func);},
	slideLeft		:function(elm,func){_$._slide(elm,"left",func);},
	slideRight		:function(elm,func){_$._slide(elm,"right",func);},

// ATTTRIBUTE

	addClass		:function(elm,cls){var e=elm,c=cls;if(!c||!e){return;}else if(!e.className){e.className=c;}else if(typeof e=="object"){e.className=e.className+" "+c;}},
	removeClass		:function(elm,cls){var e=elm,c=cls;if(_$.hasClass(e,c)){e.className=e.className.replace(new RegExp('(\\s|^)'+c+'(\\s|$)'),'$1$2');}},
	hasClass		:function(elm,cls){if(elm.className&&cls){return elm.className.match(new RegExp('(\\s|^)'+cls+'(\\s|$)'))}return false},
	getCSS			:function(elm,css,elmDefaultValue){var v,n,e=elm,a=css,d=elmDefaultValue,w=window;if(typeof e!="object"){return d;}if(w.getComputedStyle){v=w.getComputedStyle(e,null).getPropertyValue(a);}else if(e.currentStyle){while(a.indexOf('-')!=-1){n=a.charAt(a.indexOf('-')+1);a=a.replace(/-\S{1}/,n.toUpperCase());}v=e.currentStyle[a];}if(v){v=v.replace(/^\s+/,'').replace(/\s+$/,'').replace(/(px)?(#)?/ig,'');}if(isNaN(v)){v=d;}return (v!="auto")?v:d;},
	getModelBox		:function(elm){if(typeof elm!=="object"){return;}function g(v){return parseInt(_$.getCSS(elm,v,"0"),10)}var BT=g("border-Top-Width"),BR=g("border-Right-Width"),BB=g("border-Bottom-Width"),BL=g("border-Left-Width"),PT=g("padding-Top"),PR=g("padding-Right"),PB=g("padding-Bottom"),PL=g("padding-Left"),MT=g("margin-Top"),MR=g("margin-Right"),MB=g("margin-Bottom"),ML=g("margin-Left"),OW=elm.offsetWidth,OH=elm.offsetHeight,IW=(OW-PL-PR-BL-BR),IH=(OH-PT-PB-BT-BB);return {OW:OW,OH:OH,IW:IW,IH:IH,BT:BT,BR:BR,BB:BB,BL:BL,PT:PT,PR:PR,PB:PB,PL:PL,MT:MT,MR:MR,MB:MB,ML:ML};},
	_size			:function(elm,z,wh){var s=_$.display,v,o,r,i;if(z==undefined){if(s(elm)!='none'&&s(elm)!=''){i=_$.getModelBox(elm);return ((elm["offset"+wh])-((i.PT+i.PB)+(i.BT+i.BB)));}v=elm.style.visibility;elm.style.visibility='hidden';o=s(elm);s(elm,'block');r=parseInt(elm["offset"+wh]);s(elm,o);elm.style.visibility=v;return r;}else{if(wh=="Height"){elm.style.height=z;}else{elm.style.width=z;}}},
	height			:function(elm,size){return _$._size(elm,size,"Height")},
	width			:function(elm,size){return _$._size(elm,size,"Width")},
	position		:function(elm){var x=0,y=0,d=elm;if(d){try{if(d.offsetParent){do{x +=d.offsetLeft;y +=d.offsetTop;}while(d=d.offsetParent);}}catch(e){};}return [x,y];},
	unselectable	:function(elm,onOff){elm.style.MozUserSelect=(onOff)?"text":"none";elm.style.webkitUserSelect=(onOff)?"text":"none";elm.unselectable=(onOff)?"off":"on";},

// UTILITY

	cleanHTML		:function(s){if(!s){return "";}s=s.replace(/\n|\t|\r/ig,"").replace(/\s\s/ig," ").replace(/&(lt|gt);/ig, function (m, p1){return (p1 == "lt")? "<" : ">";}).replace(/<\/?[^>]+(>|$)/ig, "");return s;},
	trim			:function (st){return st.replace(/^\s+|\s+$/g,'');},
	rand			:function(min,max){var a=arguments.length;if(a===0){min=0;max=2147483647;}else if(a==1){max=min;min=0;}return Math.floor(Math.random()*(max-min+1))+min;},
	getExtention	:function(s){if(!s){return;}s=s.toLowerCase();s=(/[.]/.exec(s)) ? /[^.]+$/.exec(s):undefined;s=s+"";var b=s.split("#")[0].split("?")[0];return b;},
	getPosition		:function(domElm){var x=0,y=0,d=domElm;if(d){try{if(d.offsetParent){do{x +=d.offsetLeft;y +=d.offsetTop;}while(d=d.offsetParent);}}catch(e){};}return [x,y];},
	label			:function(l){var c="",t=translations[doc.lang],d=translations["en"];if(t){c=(t[l])?t[l]:false;}if(c==""||!c){c=(d[l])?d[l]:"";}return c;},
	add				:function(json){var t=translations,n=json;for(var i in t){if(n[i]){for(var l in n[i]){t[i][l]=n[i][l];}}}for(var i in n){if(!t[i]){t[i]={};for(var l in n[i]){t[i][l]=n[i][l];}}}return t;},
	getDocLang		:function(){if(doc.lang){return;}var h=doc.getElementsByTagName('html');if(h.length==1){var l=(h[0].lang).split("_")[0];if(l){doc.lang=l;return;}}var v=_$.getMetaValue("content-language");if(v){doc.lang=v;return;}var l=window.location+"",u=l.replace( /(.*)(_|-|::|=)([a-zA-Z]{2})(\.|&|#)(.*)/ig,"$3");if(u.length==2&&u){doc.lang=u.toLowerCase();return;}if(!doc.lang){doc.lang="en";}},
	getMetaValue	:function(h){var p=doc.getElementsByTagName("meta"),a,o="",l,q,v,n;for(var i=0,j=p.length;i<j;i++){if(p[i].nodeType==1){a=p[i].attributes;l="";q="";for(var k=0,f=a.length;k<f;k++){v=a[k].value;n=a[k].name;if(v!=""&&(n=="name"||n=="http-equiv")){l=v;}else{if(n=="content"){q=v;}}}if(l.toLowerCase()==h.toLowerCase()){o=q;break;}}}return o.toLowerCase();},

// AJAX

	include			:function(srcFile,callback,ext){if(!_$.isLoad){_$.isLoad={};}var i,s=srcFile,f=callback,t=(_$.isLoad[s])?true:false,j,e,h,r,doc=document;if(t==false){e=(ext)?ext:_$.getExtention(s);if(e=="js"){i=doc.createElement('script');i.setAttribute('type','text/javascript');i.setAttribute('src',s);h=doc.getElementsByTagName('body')[0];}else if(e=="css"){i=doc.createElement('link');i.setAttribute('type','text/css');i.setAttribute('rel','stylesheet');i.setAttribute('media','all');i.setAttribute('href',s);h=doc.getElementsByTagName('head')[0];}if(typeof f=="function"){if(isIE){i.onreadystatechange=function(){j=this.readyState;if(j=="loaded"||j=="complete"){f(i);}};}else{i.onload=f;}}if(h){h.appendChild(i);}_$.isLoad[s]=i;}else if(typeof f=="function"){f();}},
	_lazyLoader		:function(json){var h=false;for(var n in json){var j=json[n]["js"],c=json[n]["css"],f=json[n]["callback"],u=j||c||0,t=(j)?"js":"css";if(t=="css"){h=true}if(u){_$.include(u,f,t);}}if(h){if(typeof respond === "object"){respond.update();}}},
	xhr				:function(){var x=false,w=window;if(w.XMLHttpRequest){x=new XMLHttpRequest();}else if(w.ActiveXObject){x=new ActiveXObject("Microsoft.XMLHTTP");}return x;},
	load			:function(c){var u=c["url"],e=c["error"],s=c["success"],d=c["data"],f=c["dataType"],m="GET";if(u!=""&&u!=undefined&&u!=null){if(d){m='POST'};if(!f){f="application/x-www-form-urlencoded";}var r=_$.xhr();if(!r){return;}u=u.replace(/&amp;/ig,"&");r.onreadystatechange=function(){if(r.readyState==4){if(r.status!=200&&r.status!=304){if(typeof e=="function"){e(c)}}else{if(typeof s=="function"){s(r.responseText,r.responseXML,c)}else{return {txt:r.responseText,xml:r.responseXML};}}}};r.open(m,u,true);if(m=='POST'){r.setRequestHeader("Content-Type",f);r.send( d )}else{r.send(null)}}},

// CONTROLE

	getViewport		:function(){var v=window,w=v.innerWidth,h=v.innerHeight;return {w:(!w)?docType.clientWidth:w,h:(!h)?docType.clientHeight:h};},
	getUrlParam		:function(){return _$.getUrlParams()},
	getUrlParams	:function(s){var i,l,t,z,q={},u=s||(win.location.search).substring(1),d=(decodeURIComponent(u).split("+").join(" ")),d=decodeURIComponent(d);a=d.split("&");for(i=0,l=a.length;i<l;i++){t=a[i].split("=");if(t[0].indexOf("[")!=-1){if(!q[t[0]]){q[t[0]]=[];}if(t[1]){q[t[0]].push(t[1])}}else{q[t[0]]=t[1]}}return q;},
	wheel			:function(obj,callback,prevent){function wheel(e){var d=0;if(!e){e=window.event;}if(e.wheelDelta){d=e.wheelDelta/120;if(window.opera){d=-d;}}else if(e.detail){d= -e.detail/3;}if(d){if(typeof callback === "function"){callback(d);}}if(e.preventDefault && prevent){e.preventDefault();e.returnValue=false;}}if(obj.addEventListener){obj.addEventListener('DOMMouseScroll',wheel,false);obj.addEventListener('mousewheel',wheel,false);}else{obj.onmousewheel=wheel;}},
	mouse			:function(e){e=e||window.event;return {x:e.pageX||(e.clientX+(docType.scrollLeft)),y:e.pageY||(e.clientY+(docType.scrollTop))};}

});win._$=win.components=_$;if(isIE < 9){_$.addEvent(win,"load",_$.run);}else{_$.ready(_$.run);}

})(window, document);

function euVoting(lang,id){if(id===""){return;}var u="http://evoting.ec.europa.eu/vp/poll_distant.php?id_lg="+lang+"&poll_id="+id+"&our_remote_host=http://evoting.ec.europa.eu&main_action_url="+window.location.href;document.write('<'+'script language="JavaScript" src="'+u+'">'+"<"+"/script"+">");}