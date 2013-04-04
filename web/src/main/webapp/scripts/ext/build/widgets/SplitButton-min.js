/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.SplitButton=Ext.extend(Ext.Button,{arrowSelector:'button:last',initComponent:function(){Ext.SplitButton.superclass.initComponent.call(this);this.addEvents("arrowclick");},onRender:function(ct,position){var tpl=new Ext.Template('<table cellspacing="0" class="x-btn-menu-wrap x-btn"><tr><td>','<table cellspacing="0" class="x-btn-wrap x-btn-menu-text-wrap"><tbody>','<tr><td class="x-btn-left"><i>&#160;</i></td><td class="x-btn-center"><button class="x-btn-text" type="{1}">{0}</button></td></tr>',"</tbody></table></td><td>",'<table cellspacing="0" class="x-btn-wrap x-btn-menu-arrow-wrap"><tbody>','<tr><td class="x-btn-center"><button class="x-btn-menu-arrow-el" type="button">&#160;</button></td><td class="x-btn-right"><i>&#160;</i></td></tr>',"</tbody></table></td></tr></table>");var btn,targs=[this.text||'&#160;',this.type];if(position){btn=tpl.insertBefore(position,targs,true);}else{btn=tpl.append(ct,targs,true);}
var btnEl=this.btnEl=btn.child(this.buttonSelector);this.initButtonEl(btn,btnEl);this.arrowBtnTable=btn.child("table:last");this.arrowEl=btn.child(this.arrowSelector);if(this.arrowTooltip){this.arrowEl.dom[this.tooltipType]=this.arrowTooltip;}},autoWidth:function(){if(this.el){var tbl=this.el.child("table:first");var tbl2=this.el.child("table:last");this.el.setWidth("auto");tbl.setWidth("auto");if(Ext.isIE7&&Ext.isStrict){var ib=this.btnEl;if(ib&&ib.getWidth()>20){ib.clip();ib.setWidth(Ext.util.TextMetrics.measure(ib,this.text).width+ib.getFrameWidth('lr'));}}
if(this.minWidth){if((tbl.getWidth()+tbl2.getWidth())<this.minWidth){tbl.setWidth(this.minWidth-tbl2.getWidth());}}
this.el.setWidth(tbl.getWidth()+tbl2.getWidth());}},setArrowHandler:function(handler,scope){this.arrowHandler=handler;this.scope=scope;},onClick:function(e){e.preventDefault();if(!this.disabled){if(e.getTarget(".x-btn-menu-arrow-wrap")){if(this.menu&&!this.menu.isVisible()&&!this.ignoreNextClick){this.showMenu();}
this.fireEvent("arrowclick",this,e);if(this.arrowHandler){this.arrowHandler.call(this.scope||this,this,e);}}else{if(this.enableToggle){this.toggle();}
this.fireEvent("click",this,e);if(this.handler){this.handler.call(this.scope||this,this,e);}}}},getClickEl:function(e,isUp){if(!isUp){return(this.lastClickEl=e.getTarget("table",10,true));}
return this.lastClickEl;},onDisableChange:function(disabled){Ext.SplitButton.superclass.onDisableChange.call(this,disabled);if(this.arrowEl){this.arrowEl.dom.disabled=disabled;}},isMenuTriggerOver:function(e){return this.menu&&e.within(this.arrowBtnTable)&&!e.within(this.arrowBtnTable,true);},isMenuTriggerOut:function(e,internal){return this.menu&&!e.within(this.arrowBtnTable);},onDestroy:function(){Ext.destroy(this.arrowBtnTable);Ext.SplitButton.superclass.onDestroy.call(this);}});Ext.MenuButton=Ext.SplitButton;Ext.reg('splitbutton',Ext.SplitButton);