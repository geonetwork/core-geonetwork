/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.layout.AnchorLayout=Ext.extend(Ext.layout.ContainerLayout,{monitorResize:true,getAnchorViewSize:function(ct,target){return target.dom==document.body?target.getViewSize():target.getStyleSize();},onLayout:function(ct,target){Ext.layout.AnchorLayout.superclass.onLayout.call(this,ct,target);var size=this.getAnchorViewSize(ct,target);var w=size.width,h=size.height;if(w<20||h<20){return;}
var aw,ah;if(ct.anchorSize){if(typeof ct.anchorSize=='number'){aw=ct.anchorSize;}else{aw=ct.anchorSize.width;ah=ct.anchorSize.height;}}else{aw=ct.initialConfig.width;ah=ct.initialConfig.height;}
var cs=ct.items.items,len=cs.length,i,c,a,cw,ch;for(i=0;i<len;i++){c=cs[i];if(c.anchor){a=c.anchorSpec;if(!a){var vs=c.anchor.split(' ');c.anchorSpec=a={right:this.parseAnchor(vs[0],c.initialConfig.width,aw),bottom:this.parseAnchor(vs[1],c.initialConfig.height,ah)};}
cw=a.right?this.adjustWidthAnchor(a.right(w),c):undefined;ch=a.bottom?this.adjustHeightAnchor(a.bottom(h),c):undefined;if(cw||ch){c.setSize(cw||undefined,ch||undefined);}}}},parseAnchor:function(a,start,cstart){if(a&&a!='none'){var last;if(/^(r|right|b|bottom)$/i.test(a)){var diff=cstart-start;return function(v){if(v!==last){last=v;return v-diff;}}}else if(a.indexOf('%')!=-1){var ratio=parseFloat(a.replace('%',''))*.01;return function(v){if(v!==last){last=v;return Math.floor(v*ratio);}}}else{a=parseInt(a,10);if(!isNaN(a)){return function(v){if(v!==last){last=v;return v+a;}}}}}
return false;},adjustWidthAnchor:function(value,comp){return value;},adjustHeightAnchor:function(value,comp){return value;}});Ext.Container.LAYOUTS['anchor']=Ext.layout.AnchorLayout;