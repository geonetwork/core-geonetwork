(function(win,doc){ // EC.EUROPA.EU 2013 "RESPONSIVE DESIGN" - 2.4.9

	var IE=(function(){var ie=(/*@cc_on!@*/false);if(ie){var v=3,div=doc.createElement('div'),all=div.getElementsByTagName('i');while(div.innerHTML='<!--[if gt IE ' + (++v) + ']><i></i><![endif]-->',all[0]);return v > 4 ? v : (/*@cc_on!@*/false);}return ie}());

	win.doc		= doc,
	win.docType	= doc.documentElement||doc.body,
	win.isIE	= IE;
	win.win 	= win;
	win.doc 	= doc;

	win.translations = { // All translations used in webservice and menu
	"en":{"mbS":"Browse section","wsD":"Ok","wsE":"Webservice is busy, try later","wsL":"Please wait","wsR":"No languages found"},
	"bg":{"mbS":"Прегледайте раздела","wsE":"Уеб приложението не отговаря, опитайте по-късно.","wsL":"Моля изчакайте","wsR":"Не са намерени преводи на други езици"},
	"cs":{"mbS":"Procházet rubriku","wsE":"Webová služba je vytížená, zkuste to znovu později","wsL":"Prosím čekejte","wsR":"Nebyly nalezeny žádné jiné jazykové verze"},
	"da":{"mbS":"Gennemse afsnit","wsE":"Webtjenesten er optaget, prøv igen senere","wsL":"Vent venligst","wsR":"Ingen sprog fundet"},
	"de":{"mbS":"Folgende Rubrik anschauen","wsE":"Webservice besetzt, später nochmals versuchen ","wsL":"Bitte warten","wsR":"Keine Sprache gefunden"},
	"el":{"mbS":"Φυλλομετρήστε το τμήμα","wsE":"Η δικτυακή πρόσβαση δεν είναι εφικτή, δοκιμάστε αργότερα","wsL":"Περιμένετε","wsR":"Δεν βρέθηκε καμία γλώσσα"},
	"es":{"mbS":"Ver la sección","wsE":"Servicio web ocupado, inténtelo más tarde","wsL":"Espere","wsR":"No se ha encontrado ningún idioma"},
	"et":{"mbS":"Sirvi rubriiki","wsE":"Veebiteenus on hõivatud, proovige hiljem uuesti","wsL":"Palun oodake","wsR":"Muid keeli ei leitud"},
	"fi":{"mbS":"Selaa osiota","wsE":"Verkkopalvelu ei ole tällä hetkellä käytettävissä, yritä myöhemmin uudelleen","wsL":"Odota","wsR":"Muita kieliä ei löytynyt"},
	"fr":{"mbS":"Parcourir la section","wsE":"Ce service web est occupé. Veuillez réessayer plus tard.","wsL":"Veuillez patienter","wsR":"Aucune autre langue trouvée"},
		"ga":{"mbS":"Browse section","wsE":"Tá an suíomh gréasáin gnóthach, bain triail as níos déanaí ","wsL":"Fan, le do thoil","wsR":"Ní bhfuarthas aon teanga "},
	"hu":{"mbS":"A következő rovat megtekintése","wsE":"A webszolgáltatás foglalt, kérjük, próbálja később!","wsL":"Kérjük várjon","wsR":"Nincs találat"},
	"it":{"mbS":"Consulta la sezione","wsE":"Servizio web occupato, prova più tardi","wsL":"Attendere prego","wsR":"Nessuna lingua trovata"},
	"lt":{"mbS":"Naršyti skyrelyje","wsE":"Tinklo tarnyba užimta, bandykite vėliau","wsL":"Prašome palaukti","wsR":"Kalbų nerasta"},
	"lv":{"mbS":"Pārlūkot nodaļu","wsE":"Šis tīmekļa pakalpojums ir noslogots, mēģiniet vēlāk.","wsL":"Lūdzu, uzgaidiet","wsR":"Nav atrasta neviena cita valoda"},
	"mt":{"mbS":"Taqsima tal-ibbrawżjar","wsE":"Is-servizz tal-web huwa okkupat, erġa pprova aktar tard","wsL":"Jekk jogħġbok stenna","wsR":"Ma nstabet l-ebda lingwa"},
	"nl":{"mbS":"Bekijk de rubriek","wsE":"De webservice is overbelast, probeer het later opnieuw.","wsL":"Even geduld","wsR":"Geen talen gevonden"},
	"pl":{"mbS":"Przeszukaj dział","wsE":"Serwis nie odpowiada, spróbuj później","wsL":"Proszę czekać","wsR":"Nie znaleziono innych języków"},
	"pt":{"mbS":"Procurar nesta secção","wsE":"Serviço Web ocupado. Tente mais tarde.","wsL":"Aguarde","wsR":"Nenhuma língua encontrada"},
	"ro":{"mbS":"Alegeţi secţiunea","wsE":"Serviciul web este ocupat, vă rugăm reveniţi.","wsL":"Vă rugăm aşteptaţi","wsR":"Nu au fost găsite alte limbi"},
	"sk":{"mbS":"Prejsť na časť","wsE":"Webová služba je preťažená, vyskúšajte neskôr","wsL":"Čakajte, prosím","wsR":"Preklady do iných jazykov nenájdené"},
	"sl":{"mbS":"Iskanje","wsE":"Spletna storitev trenutno ni na voljo. Poskusite znova kasneje.","wsL":"Počakajte trenutek.","wsR":"Drugi jeziki niso na voljo"},
		"sv":{"mbS":"Browse section","wsE":"Webbtjänsten är upptagen, försök igen senare","wsL":"Var god vänta","wsR":"Hittar inga andra språk"}
	}

var _$ = {

	ready		:function(func){if(_$.domIsReady){func();return;}if(!_$.loadEvents){_$.loadEvents=[];}var doc=document;function isReady(){_$.domIsReady=true;clearInterval(_$.loadTimer);while(_$.exec=_$.loadEvents.shift()){_$.exec();}if(_$.ieReady){_$.ieReady.onreadystatechange='';}}if(!_$.loadEvents[0]){if(doc.addEventListener){doc.addEventListener("DOMContentLoaded",isReady,false);}else if(isIE < 9){document.write("<script id='__ie_corporate' defer src='javascript:void(0)'><\/script>");var script=document.getElementById("__ie_corporate");script.onreadystatechange=function(){if(this.readyState=="complete"){isReady();}};}else if(/WebKit|KHTML|iCab/i.test(navigator.userAgent)){_$.loadTimer=setInterval(function(){if(/loaded|complete/.test(doc.readyState)){isReady();}},10);}_$.oldOnload=window.onload;window.onload=function(){isReady();if(_$.oldOnload){_$.oldOnload();}};}_$.loadEvents.push(func);},
	addEvent	:function(o,e,f){if(e=="load"&&doc.readyState=="complete"){f();_$.domIsReady=true;return;}if(o.addEventListener){o.addEventListener(e,f,false);}else if(o.attachEvent){o.attachEvent( "on"+e,f);}},
	extend		:function(o){for(var i in o){_$[i]=o[i];}},
	after		:function(newElm,targetElm){var p,t=targetElm,n=newElm;if(t){p=t.parentNode;if(p.lastchild==t){p.appendChild(n);}else{p.insertBefore(n,t.nextSibling);}}},
	before		:function(newElm,targetElm){var p,t=targetElm,n=newElm;if(t){p=t.parentNode;if(p){p.insertBefore(n,t);}}},
	remove		:function(e){if(e){if(e.parentNode){e.parentNode.removeChild(e);}}},
	addClass	:function(elm,cls){var e=elm,c=cls;if(!c||!e){return;}else if(!e.className){e.className=c;}else if(typeof e=="object"){e.className=e.className+" "+c;}},
	removeClass	:function(elm,cls){var e=elm,c=cls;if(_$.hasClass(e,c)){e.className=e.className.replace(new RegExp('(\\s|^)'+c+'(\\s|$)'),'$1$2');}},
	hasClass	:function(elm,cls){if(elm.className&&cls){return elm.className.match(new RegExp('(\\s|^)'+cls+'(\\s|$)'))}return false},
	getExtention:function(s){if(!s){return;}s=s.toLowerCase();s=(/[.]/.exec(s)) ? /[^.]+$/.exec(s):undefined;s=s+"";var b=s.split("#")[0].split("?")[0];return b;},
	include		:function(srcFile,callback,ext){if(!_$.isLoad){_$.isLoad={};}var i,s=srcFile,f=callback,t=(_$.isLoad[s])?true:false,j,e,h,r,doc=document;if(t==false){e=(ext)?ext:_$.getExtention(s);if(e=="js"){i=doc.createElement('script');i.setAttribute('type','text/javascript');i.setAttribute('src',s);h=doc.getElementsByTagName('body')[0];}else if(e=="css"){i=doc.createElement('link');i.setAttribute('type','text/css');i.setAttribute('rel','stylesheet');i.setAttribute('media','all');i.setAttribute('href',s);h=doc.getElementsByTagName('head')[0];}if(typeof f=="function"){if(isIE){i.onreadystatechange=function(){j=this.readyState;if(j=="loaded"||j=="complete"){f(i);}};}else{i.onload=f;}}if(h){h.appendChild(i);}_$.isLoad[s]=i;}else if(typeof f=="function"){f();}},
	setCook		:function(cookName,cookValue,cookDay){var s,e="";if(cookDay){s=new Date();s.setTime(s.getTime()+(cookDay*24*60*60*1000));e=";expires="+s.toGMTString();}doc.cookie=cookName+"="+cookValue+e+";path=/"},
	getCook		:function(cookName,cookDefaultValue){var c,o,n,i,t;o=doc.cookie.split(';');n=cookName+"=";for(i=0,t=o.length;i<t;i++){c=o[i];while(c.charAt(0)==' '){c=c.substring(1,c.length);}if(c.indexOf(n)===0){return c.substring(n.length,c.length);}}return cookDefaultValue||null},
	getDocLang	:function(){if(doc.lang){return;}var h=doc.getElementsByTagName('html');if(h.length==1){var l=(h[0].lang).split("_")[0];if(l){doc.lang=l;return;}}var v=_$.getMetaValue("content-language");if(v){doc.lang=v;return;}var l=window.location+"",u=l.replace( /(.*)(_|-|::|=)([a-zA-Z]{2})(\.|&|#)(.*)/ig,"$3");if(u.length==2&&u){doc.lang=u.toLowerCase();return;}if(!doc.lang){doc.lang="en";}},
	getMetaValue:function(h){var p=doc.getElementsByTagName("meta"),a,o="",l,q,v,n;for(var i=0,j=p.length;i<j;i++){if(p[i].nodeType==1){a=p[i].attributes;l="";q="";for(var k=0,f=a.length;k<f;k++){v=a[k].value;n=a[k].name;if(v!=""&&(n=="name"||n=="http-equiv")){l=v;}else{if(n=="content"){q=v;}}}if(l.toLowerCase()==h.toLowerCase()){o=q;break;}}}return o.toLowerCase();},
	label		:function(l){var c="",t=translations[doc.lang],d=translations["en"];if(t){c=(t[l])?t[l]:false;}if(c==""||!c){c=(d[l])?d[l]:"";}return c;},

// CORPORATE

	files		:["/wel/template-2013/scripts/ec-mobile.js","/wel/components/2013/scripts/respond.js"],
	_isMobile 	:function(){var k=["midp","240x320","160x160","bolt","blackberry","netfront","nokia","panasonic","portalmmm","sharp","sie-","sonyericsson","symbian","windows ce","benq","mda","mot-","opera mini","philips","pocket pc","sagem","samsung","sda","sgh-","vodafone","xda","palm","iphone","ipod","ipad","android"],u=navigator.userAgent.toLowerCase(),m=false;for(var i=0,l=k.length;i<l;i+=1){if(u.indexOf(k[i])!=-1){m=true;break;}}return m;},
	mobileRun	:function(){

		if(!doc.lang){_$.getDocLang();}

		function g(i){return doc.getElementById(i);};
		function t(a,b){if(!bo[0].currentOpener){bo[0].currentOpener=a;}if(!a.isOpen){a.isOpen=false;}for(var i=0,l=lb.length;i<l;i++){_$.removeClass(lb[i],"selected");}if(a.isOpen && bo[0].currentOpener==a){bo[0].className=bc;a.isOpen=false;}else{bo[0].className=bc+" "+b;a.isOpen=true;_$.addClass(a,"selected");};bo[0].currentOpener=a;a.blur();return false;};

		var bo=doc.getElementsByTagName('body'),
			me=g("menu"),
			ly=g("layout"),
			ls=g("language-selector"),
			am=g("accessibility-menu"),
			ad=g("additional-tools"),
			sf=g("search-form");

			bo[0].className +=" js"+((isMobile)?" mobile":" desktop");

		// MENU UPDATE
		var isMenu=false,isOpen=false;
		if(me){if((me.className).indexOf("level-1")==-1){
			var bSection = doc.createElement("p");
				bSection.innerHTML = _$.label("mbS") + ": <a href='javascript:void(0)' onclick='return components.mobile.section(this)'><span></span></a>";
				bSection.className = "visible-tablet menu-browse";
			_$.before(bSection,me);
		}}

		this.mobile={
			menu	:function(e){t(e,"show-menu");isOpen=false;var m=win.innerWidth||docType.clientWidth;if(m<768||isMobile){return false;}},
			search	:function(e){t(e,"show-search");isOpen=false;return false;},
			section :function(elm){if(!isOpen){_$.addClass(bo[0],"show-submenu");isOpen=true;}else{_$.removeClass(bo[0],"show-submenu");isOpen=false;}}
		};

		// accessibility menu
		if(am){
			am.innerHTML=(!sf)?am.innerHTML:am.innerHTML+'<li class="m-link m-form"><a href="#search-form" onclick="return components.mobile.search(this)"><span>'+doc.getElementsByTagName("label")[0].innerHTML+'</span></a></li>';
			var bc=bo[0].className,lb=am.getElementsByTagName('a');
			lb[0].innerHTML="<span>"+lb[0].innerHTML+"</span>";
			lb[0].parentNode.className="m-home";
			if(me){
				if(isIE){
					var o=lb[1].href;
					lb[1].onmouseover=function(){this.href="javascript://"};
					lb[1].onmouseout=function(){this.href=o;};
					lb[1].onclick=function(){components.mobile.menu(lb[1]);}
				}else{
					lb[1].setAttribute("onclick","return components.mobile.menu(this);");
				}
				lb[1].innerHTML="<span>"+lb[1].innerHTML+"</span>";
				lb[1].parentNode.className="m-menu";
			}else{
				lb[1].parentNode.className="m-hide";
			}
			lb[2].parentNode.className="m-hide";
		}

		// language selector to select form
		if(ls){
			var li=ls.getElementsByTagName("li"),
				lk=ls.getElementsByTagName("a"),
				tmp=doc.createElement('div'),
				slct=doc.createElement('select'),
				z=0,clk,cli,slc,a,c,d,e,f,Lng=li.length,lk;
				function aso(o,t,v,s,i){o.options[i]=new Option(t,v,false,s);}
				for(var i=0,l=li.length;i<l;i++){
					clk=lk[z];cli=li[i];a=cli.className;c=cli.title;
					if(clk){d=clk.href;e=clk.lang;f=clk.title;}
					if(a.indexOf("selected")!= -1){aso(slct,c+" ("+cli.lang+")","",true,i);}
					else{aso(slct,f+" ("+e+")",d,false,i);z++;}
				}
				slct.id="language-selector";
				slct.className="reset-list language-selector";
				slct.onchange=function(){if(this.options[this.selectedIndex].value){location.href=this.options[this.selectedIndex].value;}};
			_$.after(slct,ls);
			_$.remove(ls);
		}
		// search form
		if(sf){var f=sf;if(f){f.className="search-form search-on";var i=f.getElementsByTagName('input'),s=i[1],i=i[0];if (i.value!=""){f.className="search-form"};function hide(){f.className="search-form";}function show(){if(i.value==""&&!f.isFocus){f.className="search-form search-on";}}function foc(){f.isFocus=true;hide();}function blu(){f.isFocus=false;show();}i.onmouseout=show;i.onfocus=foc;i.onblur=blu;}}
		// hide additionnal tools for mobile
		if(ad&&isMobile){ad.style.display="none"}
		// mobile JS
		if(isMobile){_$.include(_$.files[0]);}
		// fallback for media query
		if(!win.matchMedia){_$.addEvent(win,"load",function(){_$.include(_$.files[1]);})}
		// hide defaut mobile toolbar on load
		if(isMobile){_$.addEvent(win,"load",function(){setTimeout(function(){window.scrollTo(0, 1);},0);})}
		// additional tools
		tools.fonts.init();
	}
};
win.tools = { // Some widget tools for the accessibility
	fonts : {
		fontSet			:[1,2,3,4],
		init			:function(){var t=doc.getElementById("additional-tools");if(t){tools.fonts.getFontSize();}},
		getFontSize		:function(){cfz=_$.getCook("fontSize");if(!cfz || cfz > 4 || cfz < 0 || isNaN(cfz)){cfz=1;}else{tools.fonts.applyFontSize(cfz);}},
		applyFontSize	:function(cfz){var n=tools.fonts.fontSet[cfz];if(n){var b=doc.body,c=b.className.replace(/ font-size-(1|2|3|4|5)/ig,"");b.className=c+" font-size-"+(Math.round(cfz));_$.setCook("fontSize",cfz);}},
		increase		:function(){var l=this.fontSet.length;cfz++;if( cfz > l-1 ){cfz = l-1;}tools.fonts.applyFontSize(cfz);},
		decrease		:function(){cfz--;if( cfz <= 0 ){cfz = 1;}tools.fonts.applyFontSize(cfz);}
	}
};
win.webservice={ // Retrieve any translations of any documents and showing inside a popup.
	img				:["/wel/images/languages/ws.gif","/wel/images/languages/loading.gif"],
	prevLink		:function(srcElm,tag){var e=srcElm,o=e;for(;e;e=e["previousSibling"]){if( e.nodeType === 1 && e!=o ){break;}}return e;},
	popup			:function(srcElm,coverage){var e=srcElm,span=e.parentNode,wsUrl=(span)?span.u:null;if(span.tagName!="SPAN"){span=document.createElement("span");span.className="ws-popup";_$.wrap(e,span);if(coverage){wsUrl=span.u=coverage;}else{var p=webservice.prevLink(span,"A");wsUrl=span.u="/cgi-bin/coverage/coverage?url="+encodeURIComponent(decodeURIComponent(p.href));}}
		var	iso=span,child=span.getElementsByTagName("span"),popup=child[1],img=span.getElementsByTagName("img")[0],imgSrc=(img)?img.src:webservice.img[0],lnk=e.href,cls=span.className.split(" ")[0],v=_$.getViewport(),p=_$.getPosition(span),st=docType.scrollTop||document.body.scrollTop,sl=docType.scrollLeft||document.body.scrollLeft,pSpan=webservice.prevPopup;e.href="javascript:void(0)";if(!span.oTitle){span.oTitle=e.title;}
		if(pSpan){clearTimeout(pSpan.timer);pSpan.getElementsByTagName("a")[0].title=pSpan.oTitle;var pImg=pSpan.getElementsByTagName("img")[0];if(pImg){pImg.src=imgSrc;pImg.alt=pSpan.oTitle;}setTimeout(function(){pSpan.isOpen=false;},50);pSpan.className=cls;}
		if(span.isOpen && wsUrl){if(popup){popup.innerHTML="";}close();}
		else if(wsUrl){wsUrl=wsUrl.replace(/&amp;/ig,"&");webservice.prevPopup=span;pop("wsL",cls+" ws-loading");if(popup){popup.innerHTML="";}else{popup=doc.createElement("span");popup.className="ws-links";span.appendChild(popup)}webservice.prevPopup.timer=setTimeout(function(){
		_$.load({url:wsUrl,success:success,error:error});},250);}
		else if(span.isOpen){if(popup){popup.style.display="none";}close();}
		else{if(popup){popup.style.display="block";popup.style.left="-5px";}show();}
		function out(){popup.timer=setTimeout(function(){close();},250);}
		function over(){clearTimeout(popup.timer);}
		function restore(elm,cls){if(cls){elm.className=cls;}elm.getElementsByTagName("a")[0].title=elm.oTitle;}
		function bindEvent(){var lnks=popup.getElementsByTagName("a");for(var i=0,l=lnks.length;i<l;i++){lnks[i].onblur=out;lnks[i].onfocus=over;}}
		function getOverflowParent(elm){if(elm.style){if(elm.style.overflow!=""){iso=elm;iso.doit=true;}else{iso=elm.parentNode;getOverflowParent(iso);}}}
		function error(xml){if(xml[0]){pop("wsE",cls + " ws-error",xml[0].firstChild.nodeValue);}else{pop("wsE",cls + " ws-error");}}
		function getList(xml){var h,s,t,v='',p,b,r,a,i,j,e,z,n,l='';z=xml.getElementsByTagName("message");d=xml.getElementsByTagName("document");p=d.length;n=z.length;c=false;k=doc.lang;for(i=0;i<p;i++){b=d[i];r=b.getAttribute("lang");a=b.getAttribute("label");t=b.getAttribute("type");e=b.getAttribute("href").split("#")[0]+window.location.hash;s=a.split("(")[0];l +='<a class="lang" href="'+e+'" hreflang="'+r+'" lang="'+r+'" title="'+a+'"><span class="off-screen">'+s+' (</span>'+r+'<span class="off-screen">)</span></a> ';}return {lst:l,nbr:n,cnt:p,error:z};}
		function success(txt,xml,cfg){var ws=getList(xml);if(ws.lst!==''){pop("wsD",cls,ws.lst);}else if(ws.nbr==0&&ws.cnt==0){pop("wsR",cls+" ws-retry");}else if(ws.nbr>0||ws.lst==""){error(ws.error);}}
		function pop(label,cls,content){var cnt=content,lbl=_$.label(label);cnt=(cnt)?cnt:lbl;span.className=cls;e.title=lbl;if(label!="wsL"){popup.innerHTML="<span class='ws-popup-layout'>"+cnt+"</span>";if(img){img.src=imgSrc;img.alt=lbl;}show();}else{if(img){img.src=webservice.img[1];img.alt=lbl;}}}
		function close(elm){span.getElementsByTagName("a")[0].title=span.oTitle;img=span.getElementsByTagName("img")[0];if(img){img.src=imgSrc;img.alt=span.oTitle;}span.isOpen=false;span.className=cls;var c=span.getElementsByTagName("span");if(c){if(c[1]){c[1].style.left="-9999px";}}if(popup){popup.style.display="none";}e.onblur=function(){};}
		function show(){span.isOpen=true;span.className=cls+" ws-popup-show";var a=span.getElementsByTagName("a");if(a[0]){a[0].focus();}if(popup){popup.style.display="";}var c=span.getElementsByTagName("span"),m=c[1],j=c[2],w,h,vl,vt,o;m.style.width="170px";m.style.zIndex="9999";m.style.top="-5px";m.style.left="-5px";w=j.offsetWidth;
		h=j.offsetHeight;if((p[0]+w)>(v.w+sl)){vl=((p[0]+w)-(v.w+sl));vl=(!isIE)?(vl+20):(vl+5);m.style.left="-"+vl+"px";}if((p[1]+h+16)>(v.h+st)){vt=((p[1]+h)-(v.h+st));vt=(!isIE)?(vt+20):(vt+5);m.style.top="-"+vt+"px";}
		getOverflowParent(span);if(iso.doit){o=_$.getPosition(iso);v.w=iso.offsetWidth;v.h=iso.offsetHeight;st=iso.scrollTop;sl=iso.scrollLeft;if(w>v.w){w=Math.round((v.w)-70);j.style.width=w+"px";h=j.offsetHeight;}
		if((p[0]+w)>o[0]+v.w+sl){m.style.left="-"+(((p[0]+w)-(o[0]+v.w+sl))+30)+"px";}if((p[1]+h+16)>(o[1]+v.h+st)){m.style.top="-"+((p[1]+h+5)-(o[1]+v.h+st)+10)+"px";}}bindEvent();span.onmouseover=over;span.onmouseout=out;if(w){m.style.width=w+"px";}setTimeout(function(){e.href=lnk;e.onblur=function(){restore(span);out();};},5);}
}}

win._$=win.corporate=win.components=_$;win.isMobile=_$.isMobile=_$._isMobile(); // PUBLIC

})(window,document);

_$.ready(_$.mobileRun);