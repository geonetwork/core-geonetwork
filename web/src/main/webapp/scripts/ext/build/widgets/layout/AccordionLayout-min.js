/*
 * Ext JS Library 2.3.0
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */


Ext.layout.Accordion=Ext.extend(Ext.layout.FitLayout,{fill:true,autoWidth:true,titleCollapse:true,hideCollapseTool:false,collapseFirst:false,animate:false,sequence:false,activeOnTop:false,setActiveItem:function(item){item=this.container.getComponent(item);this.activeItem=item;this.layout();},renderItem:function(c){if(this.animate===false){c.animCollapse=false;}
c.collapsible=true;if(this.autoWidth){c.autoWidth=true;}
if(this.titleCollapse){c.titleCollapse=true;}
if(this.hideCollapseTool){c.hideCollapseTool=true;}
if(this.collapseFirst!==undefined){c.collapseFirst=this.collapseFirst;}
if(!this.activeItem&&!c.collapsed){this.activeItem=c;}else if(this.activeItem&&this.activeItem!=c){c.collapsed=true;}
Ext.layout.Accordion.superclass.renderItem.apply(this,arguments);c.header.addClass('x-accordion-hd');c.on('beforeexpand',this.beforeExpand,this);},beforeExpand:function(p,anim){var ai=this.activeItem;if(ai){if(this.sequence){delete this.activeItem;if(!ai.collapsed){ai.collapse({callback:function(){p.expand(anim||true);},scope:this});return false;}}else{ai.collapse(this.animate);}}
this.activeItem=p;if(this.activeOnTop){p.el.dom.parentNode.insertBefore(p.el.dom,p.el.dom.parentNode.firstChild);}
this.layout();},setItemSize:function(item,size){if(this.fill&&item){var items=this.container.items.items;var hh=0;for(var i=0,len=items.length;i<len;i++){var p=items[i];if(p!=item){hh+=(p.getSize().height-p.bwrap.getHeight());}}
size.height-=hh;item.setSize(size);}}});Ext.Container.LAYOUTS['accordion']=Ext.layout.Accordion;