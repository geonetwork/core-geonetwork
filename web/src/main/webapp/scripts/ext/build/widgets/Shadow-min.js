/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.Shadow=function(config){Ext.apply(this,config);if(typeof this.mode!="string"){this.mode=this.defaultMode;}
var o=this.offset,a={h:0};var rad=Math.floor(this.offset/2);switch(this.mode.toLowerCase()){case"drop":a.w=0;a.l=a.t=o;a.t-=1;if(Ext.isIE){a.l-=this.offset+rad;a.t-=this.offset+rad;a.w-=rad;a.h-=rad;a.t+=1;}
break;case"sides":a.w=(o*2);a.l=-o;a.t=o-1;if(Ext.isIE){a.l-=(this.offset-rad);a.t-=this.offset+rad;a.l+=1;a.w-=(this.offset-rad)*2;a.w-=rad+1;a.h-=1;}
break;case"frame":a.w=a.h=(o*2);a.l=a.t=-o;a.t+=1;a.h-=2;if(Ext.isIE){a.l-=(this.offset-rad);a.t-=(this.offset-rad);a.l+=1;a.w-=(this.offset+rad+1);a.h-=(this.offset+rad);a.h+=1;}
break;};this.adjusts=a;};Ext.Shadow.prototype={offset:4,defaultMode:"drop",show:function(target){target=Ext.get(target);if(!this.el){this.el=Ext.Shadow.Pool.pull();if(this.el.dom.nextSibling!=target.dom){this.el.insertBefore(target);}}
this.el.setStyle("z-index",this.zIndex||parseInt(target.getStyle("z-index"),10)-1);if(Ext.isIE){this.el.dom.style.filter="progid:DXImageTransform.Microsoft.alpha(opacity=50) progid:DXImageTransform.Microsoft.Blur(pixelradius="+(this.offset)+")";}
this.realign(target.getLeft(true),target.getTop(true),target.getWidth(),target.getHeight());this.el.dom.style.display="block";},isVisible:function(){return this.el?true:false;},realign:function(l,t,w,h){if(!this.el){return;}
var a=this.adjusts,d=this.el.dom,s=d.style;var iea=0;s.left=(l+a.l)+"px";s.top=(t+a.t)+"px";var sw=(w+a.w),sh=(h+a.h),sws=sw+"px",shs=sh+"px";if(s.width!=sws||s.height!=shs){s.width=sws;s.height=shs;if(!Ext.isIE){var cn=d.childNodes;var sww=Math.max(0,(sw-12))+"px";cn[0].childNodes[1].style.width=sww;cn[1].childNodes[1].style.width=sww;cn[2].childNodes[1].style.width=sww;cn[1].style.height=Math.max(0,(sh-12))+"px";}}},hide:function(){if(this.el){this.el.dom.style.display="none";Ext.Shadow.Pool.push(this.el);delete this.el;}},setZIndex:function(z){this.zIndex=z;if(this.el){this.el.setStyle("z-index",z);}}};Ext.Shadow.Pool=function(){var p=[];var markup=Ext.isIE?'<div class="x-ie-shadow"></div>':'<div class="x-shadow"><div class="xst"><div class="xstl"></div><div class="xstc"></div><div class="xstr"></div></div><div class="xsc"><div class="xsml"></div><div class="xsmc"></div><div class="xsmr"></div></div><div class="xsb"><div class="xsbl"></div><div class="xsbc"></div><div class="xsbr"></div></div></div>';return{pull:function(){var sh=p.shift();if(!sh){sh=Ext.get(Ext.DomHelper.insertHtml("beforeBegin",document.body.firstChild,markup));sh.autoBoxAdjust=false;}
return sh;},push:function(sh){p.push(sh);}};}();