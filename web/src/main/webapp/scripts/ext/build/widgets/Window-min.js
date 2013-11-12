/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.Window=Ext.extend(Ext.Panel,{baseCls:'x-window',resizable:true,draggable:true,closable:true,constrain:false,constrainHeader:false,plain:false,minimizable:false,maximizable:false,minHeight:100,minWidth:200,expandOnShow:true,closeAction:'close',elements:'header,body',collapsible:false,initHidden:true,monitorResize:true,frame:true,floating:true,initComponent:function(){Ext.Window.superclass.initComponent.call(this);this.addEvents('resize','maximize','minimize','restore');},getState:function(){return Ext.apply(Ext.Window.superclass.getState.call(this)||{},this.getBox(true));},onRender:function(ct,position){Ext.Window.superclass.onRender.call(this,ct,position);if(this.plain){this.el.addClass('x-window-plain');}
this.focusEl=this.el.createChild({tag:"a",href:"#",cls:"x-dlg-focus",tabIndex:"-1",html:"&#160;"});this.focusEl.swallowEvent('click',true);this.proxy=this.el.createProxy("x-window-proxy");this.proxy.enableDisplayMode('block');if(this.modal){this.mask=this.container.createChild({cls:"ext-el-mask"},this.el.dom);this.mask.enableDisplayMode("block");this.mask.hide();this.mask.on('click',this.focus,this);}
this.initTools();},initEvents:function(){Ext.Window.superclass.initEvents.call(this);if(this.animateTarget){this.setAnimateTarget(this.animateTarget);}
if(this.resizable){this.resizer=new Ext.Resizable(this.el,{minWidth:this.minWidth,minHeight:this.minHeight,handles:this.resizeHandles||"all",pinned:true,resizeElement:this.resizerAction});this.resizer.window=this;this.resizer.on("beforeresize",this.beforeResize,this);}
if(this.draggable){this.header.addClass("x-window-draggable");}
this.el.on("mousedown",this.toFront,this);this.manager=this.manager||Ext.WindowMgr;this.manager.register(this);this.hidden=true;if(this.maximized){this.maximized=false;this.maximize();}
if(this.closable){var km=this.getKeyMap();km.on(27,this.onEsc,this);km.disable();}},initDraggable:function(){this.dd=new Ext.Window.DD(this);},onEsc:function(){this[this.closeAction]();},beforeDestroy:function(){if(this.rendered){this.hide();if(this.doAnchor){Ext.EventManager.removeResizeListener(this.doAnchor,this);Ext.EventManager.un(window,'scroll',this.doAnchor,this);}
Ext.destroy(this.focusEl,this.resizer,this.dd,this.proxy,this.mask);}
Ext.Window.superclass.beforeDestroy.call(this);},onDestroy:function(){if(this.manager){this.manager.unregister(this);}
Ext.Window.superclass.onDestroy.call(this);},initTools:function(){if(this.minimizable){this.addTool({id:'minimize',handler:this.minimize.createDelegate(this,[])});}
if(this.maximizable){this.addTool({id:'maximize',handler:this.maximize.createDelegate(this,[])});this.addTool({id:'restore',handler:this.restore.createDelegate(this,[]),hidden:true});this.header.on('dblclick',this.toggleMaximize,this);}
if(this.closable){this.addTool({id:'close',handler:this[this.closeAction].createDelegate(this,[])});}},resizerAction:function(){var box=this.proxy.getBox();this.proxy.hide();this.window.handleResize(box);return box;},beforeResize:function(){this.resizer.minHeight=Math.max(this.minHeight,this.getFrameHeight()+40);this.resizer.minWidth=Math.max(this.minWidth,this.getFrameWidth()+40);this.resizeBox=this.el.getBox();},updateHandles:function(){if(Ext.isIE&&this.resizer){this.resizer.syncHandleHeight();this.el.repaint();}},handleResize:function(box){var rz=this.resizeBox;if(rz.x!=box.x||rz.y!=box.y){this.updateBox(box);}else{this.setSize(box);}
this.focus();this.updateHandles();this.saveState();if(this.layout){this.doLayout();}
this.fireEvent("resize",this,box.width,box.height);},focus:function(){var f=this.focusEl,db=this.defaultButton,t=typeof db;if(t!='undefined'){if(t=='number'){f=this.buttons[db];}else if(t=='string'){f=Ext.getCmp(db);}else{f=db;}}
f.focus.defer(10,f);},setAnimateTarget:function(el){el=Ext.get(el);this.animateTarget=el;},beforeShow:function(){delete this.el.lastXY;delete this.el.lastLT;if(this.x===undefined||this.y===undefined){var xy=this.el.getAlignToXY(this.container,'c-c');var pos=this.el.translatePoints(xy[0],xy[1]);this.x=this.x===undefined?pos.left:this.x;this.y=this.y===undefined?pos.top:this.y;}
this.el.setLeftTop(this.x,this.y);if(this.expandOnShow){this.expand(false);}
if(this.modal){Ext.getBody().addClass("x-body-masked");this.mask.setSize(Ext.lib.Dom.getViewWidth(true),Ext.lib.Dom.getViewHeight(true));this.mask.show();}},show:function(animateTarget,cb,scope){if(!this.rendered){this.render(Ext.getBody());}
if(this.hidden===false){this.toFront();return;}
if(this.fireEvent("beforeshow",this)===false){return;}
if(cb){this.on('show',cb,scope,{single:true});}
this.hidden=false;if(animateTarget!==undefined){this.setAnimateTarget(animateTarget);}
this.beforeShow();if(this.animateTarget){this.animShow();}else{this.afterShow();}},afterShow:function(){this.proxy.hide();this.el.setStyle('display','block');this.el.show();if(this.maximized){this.fitContainer();}
if(Ext.isMac&&Ext.isGecko){this.cascade(this.setAutoScroll);}
if(this.monitorResize||this.modal||this.constrain||this.constrainHeader){Ext.EventManager.onWindowResize(this.onWindowResize,this);}
this.doConstrain();if(this.layout){this.doLayout();}
if(this.keyMap){this.keyMap.enable();}
this.toFront();this.updateHandles();this.fireEvent("show",this);},animShow:function(){this.proxy.show();this.proxy.setBox(this.animateTarget.getBox());this.proxy.setOpacity(0);var b=this.getBox(false);b.callback=this.afterShow;b.scope=this;b.duration=.25;b.easing='easeNone';b.opacity=.5;b.block=true;this.el.setStyle('display','none');this.proxy.shift(b);},hide:function(animateTarget,cb,scope){if(this.activeGhost){this.hide.defer(100,this,[animateTarget,cb,scope]);return;}
if(this.hidden||this.fireEvent("beforehide",this)===false){return;}
if(cb){this.on('hide',cb,scope,{single:true});}
this.hidden=true;if(animateTarget!==undefined){this.setAnimateTarget(animateTarget);}
if(this.animateTarget){this.animHide();}else{this.el.hide();this.afterHide();}},afterHide:function(){this.proxy.hide();if(this.monitorResize||this.modal||this.constrain||this.constrainHeader){Ext.EventManager.removeResizeListener(this.onWindowResize,this);}
if(this.modal){this.mask.hide();Ext.getBody().removeClass("x-body-masked");}
if(this.keyMap){this.keyMap.disable();}
this.fireEvent("hide",this);},animHide:function(){this.proxy.setOpacity(.5);this.proxy.show();var tb=this.getBox(false);this.proxy.setBox(tb);this.el.hide();var b=this.animateTarget.getBox();b.callback=this.afterHide;b.scope=this;b.duration=.25;b.easing='easeNone';b.block=true;b.opacity=0;this.proxy.shift(b);},onWindowResize:function(){if(this.maximized){this.fitContainer();}
if(this.modal){this.mask.setSize('100%','100%');var force=this.mask.dom.offsetHeight;this.mask.setSize(Ext.lib.Dom.getViewWidth(true),Ext.lib.Dom.getViewHeight(true));}
this.doConstrain();},doConstrain:function(){if(this.constrain||this.constrainHeader){var offsets;if(this.constrain){offsets={right:this.el.shadowOffset,left:this.el.shadowOffset,bottom:this.el.shadowOffset};}else{var s=this.getSize();offsets={right:-(s.width-100),bottom:-(s.height-25)};}
var xy=this.el.getConstrainToXY(this.container,true,offsets);if(xy){this.setPosition(xy[0],xy[1]);}}},ghost:function(cls){var ghost=this.createGhost(cls);var box=this.getBox(true);ghost.setLeftTop(box.x,box.y);ghost.setWidth(box.width);this.el.hide();this.activeGhost=ghost;return ghost;},unghost:function(show,matchPosition){if(show!==false){this.el.show();this.focus();if(Ext.isMac&&Ext.isGecko){this.cascade(this.setAutoScroll);}}
if(matchPosition!==false){this.setPosition(this.activeGhost.getLeft(true),this.activeGhost.getTop(true));}
this.activeGhost.hide();this.activeGhost.remove();delete this.activeGhost;},minimize:function(){this.fireEvent('minimize',this);},close:function(){if(this.fireEvent("beforeclose",this)!==false){this.hide(null,function(){this.fireEvent('close',this);this.destroy();},this);}},maximize:function(){if(!this.maximized){this.expand(false);this.restoreSize=this.getSize();this.restorePos=this.getPosition(true);if(this.maximizable){this.tools.maximize.hide();this.tools.restore.show();}
this.maximized=true;this.el.disableShadow();if(this.dd){this.dd.lock();}
if(this.collapsible){this.tools.toggle.hide();}
this.el.addClass('x-window-maximized');this.container.addClass('x-window-maximized-ct');this.setPosition(0,0);this.fitContainer();this.fireEvent('maximize',this);}},restore:function(){if(this.maximized){this.el.removeClass('x-window-maximized');this.tools.restore.hide();this.tools.maximize.show();this.setPosition(this.restorePos[0],this.restorePos[1]);this.setSize(this.restoreSize.width,this.restoreSize.height);delete this.restorePos;delete this.restoreSize;this.maximized=false;this.el.enableShadow(true);if(this.dd){this.dd.unlock();}
if(this.collapsible){this.tools.toggle.show();}
this.container.removeClass('x-window-maximized-ct');this.doConstrain();this.fireEvent('restore',this);}},toggleMaximize:function(){this[this.maximized?'restore':'maximize']();},fitContainer:function(){var vs=this.container.getViewSize();this.setSize(vs.width,vs.height);},setZIndex:function(index){if(this.modal){this.mask.setStyle("z-index",index);}
this.el.setZIndex(++index);index+=5;if(this.resizer){this.resizer.proxy.setStyle("z-index",++index);}
this.lastZIndex=index;},alignTo:function(element,position,offsets){var xy=this.el.getAlignToXY(element,position,offsets);this.setPagePosition(xy[0],xy[1]);return this;},anchorTo:function(el,alignment,offsets,monitorScroll){if(this.doAnchor){Ext.EventManager.removeResizeListener(this.doAnchor,this);Ext.EventManager.un(window,'scroll',this.doAnchor,this);}
this.doAnchor=function(){this.alignTo(el,alignment,offsets);};Ext.EventManager.onWindowResize(this.doAnchor,this);var tm=typeof monitorScroll;if(tm!='undefined'){Ext.EventManager.on(window,'scroll',this.doAnchor,this,{buffer:tm=='number'?monitorScroll:50});}
this.doAnchor();return this;},toFront:function(e){if(this.manager.bringToFront(this)){if(!e||!e.getTarget().focus){this.focus();}}
return this;},setActive:function(active){if(active){if(!this.maximized){this.el.enableShadow(true);}
this.fireEvent('activate',this);}else{this.el.disableShadow();this.fireEvent('deactivate',this);}},toBack:function(){this.manager.sendToBack(this);return this;},center:function(){var xy=this.el.getAlignToXY(this.container,'c-c');this.setPagePosition(xy[0],xy[1]);return this;}});Ext.reg('window',Ext.Window);Ext.Window.DD=function(win){this.win=win;Ext.Window.DD.superclass.constructor.call(this,win.el.id,'WindowDD-'+win.id);this.setHandleElId(win.header.id);this.scroll=false;};Ext.extend(Ext.Window.DD,Ext.dd.DD,{moveOnly:true,headerOffsets:[100,25],startDrag:function(){var w=this.win;this.proxy=w.ghost();if(w.constrain!==false){var so=w.el.shadowOffset;this.constrainTo(w.container,{right:so,left:so,bottom:so});}else if(w.constrainHeader!==false){var s=this.proxy.getSize();this.constrainTo(w.container,{right:-(s.width-this.headerOffsets[0]),bottom:-(s.height-this.headerOffsets[1])});}},b4Drag:Ext.emptyFn,onDrag:function(e){this.alignElWithMouse(this.proxy,e.getPageX(),e.getPageY());},endDrag:function(e){this.win.unghost();this.win.saveState();}});