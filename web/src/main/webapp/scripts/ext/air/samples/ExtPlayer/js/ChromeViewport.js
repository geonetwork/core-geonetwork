/*
 * Ext JS Library 0.30
 * Copyright(c) 2006-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

Ext.ns('Ext.air','Ext.air.Window');
Ext.air.ChromeViewport = Ext.extend(Ext.Viewport, {
	layout: 'fit',
	draggable: true,
	initComponent: function() {
		Ext.apply(this.panel, {itemId: 'panel'});
		this.items = [this.panel];
		Ext.air.ChromeViewport.superclass.initComponent.call(this);		
		this.panel = this.getComponent('panel');
	},
	afterRender: function() {
		Ext.air.ChromeViewport.superclass.afterRender.call(this);
		// error that element does not exist unless deferred 1ms.
		(function() {
			this.panel.dd = new Ext.air.ChromeViewport.DD(this.panel);
		}).defer(1, this);
	}	
});
Ext.air.ChromeViewport.DD = Ext.extend(Ext.dd.DD,{
    moveOnly:true,
    headerOffsets:[100, 25],
    startDrag: function(){
        window.nativeWindow.startMove();
    },
    onDrag : Ext.emptyFn,
	b4Drag: Ext.emptyFn,
	endDrag: Ext.emptyFn
});